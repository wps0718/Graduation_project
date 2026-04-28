<template>
  <view class="container">
    <!-- 状态筛选Tab -->
    <view class="status-tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="status-tab"
        :class="{ 'active': currentStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >
        <text class="status-tab__text">{{ tab.label }}</text>
        <view v-if="currentStatus === tab.value" class="status-tab__line"></view>
      </view>
    </view>

    <!-- 商品列表 -->
    <scroll-view
      scroll-y
      class="scroll-view"
      @scrolltolower="onReachBottom"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="product-list" v-if="productList.length > 0">
        <view
          v-for="item in productList"
          :key="item.id"
          class="product-item"
          @click="goDetail(item.id)"
        >
          <!-- 商品图片区 -->
          <view class="product-item__image-wrap">
            <image
              :src="item.coverImage || item.images?.[0] || '/static/pic/placeholder.png'"
              mode="aspectFill"
              class="product-item__image"
              lazy-load
            />
            <!-- 状态标签 -->
            <view v-if="item.status === 3" class="status-badge status-badge--sold">
              已售出
            </view>
            <view v-else-if="item.status === 2" class="status-badge status-badge--off">
              已下架
            </view>
            <view v-else-if="item.status === 0" class="status-badge status-badge--pending">
              待审核
            </view>
            <view v-else-if="item.status === 4" class="status-badge status-badge--reject">
              已驳回
            </view>
          </view>

          <!-- 商品信息区 -->
          <view class="product-item__info">
            <text class="product-item__title">{{ item.title }}</text>
            <view class="product-item__meta">
              <text class="product-item__price">¥{{ item.price }}</text>
              <text class="product-item__time">{{ formatTime(item.createTime) }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="product-item__actions" @click.stop>
            <!-- 在售状态 -->
            <template v-if="item.status === 1">
              <view class="action-btn action-btn--text" @click="handleEdit(item)">
                编辑
              </view>
              <view class="action-btn action-btn--text" @click="handleOffShelf(item)">
                下架
              </view>
            </template>

            <!-- 已下架 -->
            <template v-else-if="item.status === 2">
              <view class="action-btn action-btn--primary" @click="handleOnShelf(item)">
                上架
              </view>
              <view class="action-btn action-btn--text" @click="handleDelete(item)">
                删除
              </view>
            </template>

            <!-- 待审核 -->
            <template v-else-if="item.status === 0">
              <text class="tips-text">审核中，请耐心等待</text>
            </template>

            <!-- 已驳回 -->
            <template v-else-if="item.status === 4">
              <view class="action-btn action-btn--primary" @click="handleEdit(item)">
                重新编辑
              </view>
            </template>

            <!-- 已售出 -->
            <template v-else-if="item.status === 3">
              <text class="tips-text">交易已完成</text>
            </template>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <empty-state v-else-if="!loading" type="no-data" text="暂无商品" />

      <!-- 加载更多 -->
      <view v-if="productList.length > 0" class="load-more">
        <text class="load-more__text">{{ hasMore ? '加载中...' : '没有更多了' }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { isLoggedIn } from '@/utils/auth'
import EmptyState from '@/components/empty-state/empty-state.vue'

const statusTabs = [
  { label: '全部', value: null },
  { label: '在售', value: 1 },
  { label: '待审核', value: 0 },
  { label: '已下架', value: 2 },
  { label: '已售出', value: 3 },
]

const currentStatus = ref(null)
const productList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10

onLoad((options) => {
  if (!isLoggedIn()) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      showCancel: false,
      success: () => {
        uni.reLaunch({ url: '/pages/login-sub/login/login' })
      }
    })
    return
  }

  if (options && options.status !== undefined) {
    currentStatus.value = Number(options.status)
  }
  loadList(true)
})

onShow(() => {
  if (isLoggedIn() && productList.value.length === 0) {
    loadList(true)
  }
})

function switchStatus(status) {
  if (currentStatus.value === status) return
  currentStatus.value = status
  loadList(true)
}

async function loadList(refresh = false) {
  if (loading.value) return
  if (refresh) {
    page.value = 1
    hasMore.value = true
  }
  loading.value = true
  
  try {
    const params = {
      page: page.value,
      pageSize,
    }
    if (currentStatus.value !== null) {
      params.status = currentStatus.value
    }
    
    const res = await get('/mini/product/my-list', params)
    const list = res?.records || []
    
    if (refresh) {
      productList.value = list
    } else {
      productList.value = [...productList.value, ...list]
    }
    
    if (list.length < pageSize) {
      hasMore.value = false
    } else {
      page.value++
    }
  } catch(e) {
    console.error('❌ 加载失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace(/-/g, '/'))
  const now = new Date()
  const diff = now - date
  const day = 24 * 60 * 60 * 1000
  
  if (diff < day) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    if (hours === 0) {
      const minutes = Math.floor(diff / (60 * 1000))
      return minutes === 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  }
  
  const days = Math.floor(diff / day)
  if (days < 7) return `${days}天前`
  
  return timeStr.substring(0, 10)
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/product/detail/detail?id=${id}` })
}

function handleEdit(item) {
  uni.navigateTo({ url: `/pages/product/edit/edit?id=${item.id}` })
}

async function handleOffShelf(item) {
  uni.showModal({
    title: '确认下架',
    content: `确定要下架「${item.title}」吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/product/off-shelf', { productId: item.id })
        uni.showToast({ title: '已下架', icon: 'success' })
        loadList(true)
      } catch(e) {}
    }
  })
}

async function handleOnShelf(item) {
  try {
    await post('/mini/product/on-shelf', { productId: item.id })
    uni.showToast({ title: '已重新上架', icon: 'success' })
    loadList(true)
  } catch(e) {}
}

async function handleDelete(item) {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除「${item.title}」吗？删除后无法恢复。`,
    confirmText: '删除',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/product/delete', { productId: item.id })
        uni.showToast({ title: '已删除', icon: 'success' })
        loadList(true)
      } catch(e) {}
    }
  })
}

function onReachBottom() {
  if (hasMore.value && !loading.value) {
    loadList(false)
  }
}

function onRefresh() {
  refreshing.value = true
  loadList(true)
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f7f7f7;
}

.status-tabs {
  display: flex;
  background: #fff;
  padding: 0 32rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.status-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0;
  position: relative;
}

.status-tab__text {
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;
}

.status-tab.active .status-tab__text {
  color: #1677ff;
  font-weight: 600;
  font-size: 30rpx;
}

.status-tab__line {
  width: 40rpx;
  height: 6rpx;
  background: #1677ff;
  border-radius: 3rpx;
  margin-top: 8rpx;
}

.scroll-view {
  height: calc(100vh - 88rpx);
}

.product-list {
  padding: 24rpx 32rpx;
}

.product-item {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
}

.product-item__image-wrap {
  position: relative;
  width: 100%;
  height: 420rpx;
  background: #f5f5f5;
}

.product-item__image {
  width: 100%;
  height: 100%;
}

.status-badge {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  color: #fff;
  font-weight: 500;
}

.status-badge--sold {
  background: rgba(0,0,0,0.6);
}

.status-badge--off {
  background: rgba(255,152,0,0.9);
}

.status-badge--pending {
  background: rgba(24,144,255,0.9);
}

.status-badge--reject {
  background: rgba(245,34,45,0.9);
}

.product-item__info {
  padding: 24rpx 32rpx 16rpx;
}

.product-item__title {
  font-size: 30rpx;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.product-item__meta {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-top: 16rpx;
}

.product-item__price {
  font-size: 36rpx;
  color: #ff4d4f;
  font-weight: 600;
}

.product-item__time {
  font-size: 24rpx;
  color: #999;
}

.product-item__actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 32rpx 24rpx;
}

.action-btn {
  padding: 12rpx 32rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
  transition: all 0.3s;
}

.action-btn--text {
  color: #666;
  border: 1rpx solid #e5e5e5;
  background: #fff;
}

.action-btn--primary {
  color: #1677ff;
  border: 1rpx solid #1677ff;
  background: #e6f7ff;
}

.tips-text {
  font-size: 24rpx;
  color: #999;
}

.load-more {
  padding: 32rpx;
  text-align: center;
}

.load-more__text {
  font-size: 26rpx;
  color: #999;
}
</style>