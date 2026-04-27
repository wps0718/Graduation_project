<template>
  <view class="container">
    <!-- 顶部状态筛选Tab -->
    <view class="status-tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="status-tab"
        :class="{ 'is-active': currentStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >
        <text class="status-tab__text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="product-list" v-if="productList.length > 0">
      <view
        v-for="item in productList"
        :key="item.id"
        class="product-item"
      >
        <!-- 商品信息区 -->
        <view class="product-item__main" @click="goDetail(item.id)">
          <view class="product-item__img-wrap">
            <image
              :src="item.coverImage || item.images?.[0]"
              mode="aspectFill"
              class="product-item__img"
            />
            <!-- 已售出遮罩 -->
            <view v-if="item.status === 3" class="product-item__sold-mask">
              <text class="product-item__sold-text">已售出</text>
            </view>
            <!-- 已下架遮罩 -->
            <view v-if="item.status === 2" class="product-item__off-mask">
              <text class="product-item__off-text">已下架</text>
            </view>
            <!-- 待审核标签 -->
            <view v-if="item.status === 0" class="product-item__pending-tag">
              <text>待审核</text>
            </view>
            <!-- 审核驳回标签 -->
            <view v-if="item.status === 4" class="product-item__reject-tag">
              <text>已驳回</text>
            </view>
          </view>
          <view class="product-item__info">
            <text class="product-item__title">{{ item.title }}</text>
            <text class="product-item__price">¥{{ item.price }}</text>
            <text class="product-item__time">{{ item.createTime }}</text>
          </view>
        </view>

        <!-- 操作按钮区 -->
        <view class="product-item__actions">
          <!-- 在售状态的操作 -->
          <template v-if="item.status === 1">
            <view class="action-btn action-btn--default" @click="handleEdit(item)">
              <text>编辑</text>
            </view>
            <view class="action-btn action-btn--warning" @click="handleOffShelf(item)">
              <text>下架</text>
            </view>
            <view class="action-btn action-btn--primary" @click="handleMarkSold(item)">
              <text>标记售出</text>
            </view>
          </template>

          <!-- 已下架状态的操作 -->
          <template v-if="item.status === 2">
            <view class="action-btn action-btn--primary" @click="handleOnShelf(item)">
              <text>重新上架</text>
            </view>
            <view class="action-btn action-btn--danger" @click="handleDelete(item)">
              <text>删除</text>
            </view>
          </template>

          <!-- 审核驳回状态的操作 -->
          <template v-if="item.status === 4">
            <view class="action-btn action-btn--default" @click="handleEdit(item)">
              <text>修改后重新提交</text>
            </view>
          </template>
        </view>
      </view>
    </view>

    <empty-state v-else-if="!loading" type="no-data" text="暂无商品" />

    <view v-if="productList.length > 0" class="load-more">
      <text>{{ hasMore ? '加载中...' : '没有更多了' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
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
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10

onLoad((options) => {
  // 支持从个人中心"在售"入口跳转并自动选中对应Tab
  if (options && options.status !== undefined) {
    currentStatus.value = Number(options.status)
  }
  loadList(true)
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
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
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

async function handleMarkSold(item) {
  uni.showModal({
    title: '确认已售出',
    content: `确定「${item.title}」已售出吗？标记后商品将显示"已售出"状态。`,
    confirmText: '确认售出',
    success: async (res) => {
      if (!res.confirm) return
      try {
        // 调用下架接口将status改为已售出
        // 注意：需要后端支持 /mini/product/mark-sold 接口
        // 如果后端暂无此接口，临时使用off-shelf接口
        await post('/mini/product/mark-sold', { productId: item.id })
        uni.showToast({ title: '已标记为售出', icon: 'success' })
        loadList(true)
      } catch(e) {
        // 如果接口不存在，提示用户
        uni.showToast({ title: '操作失败，请联系管理员', icon: 'none' })
      }
    }
  })
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

onReachBottom(() => {
  if (hasMore.value && !loading.value) loadList(false)
})

onPullDownRefresh(async () => {
  await loadList(true)
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss">
.container {
  min-height: 100vh;
  background-color: var(--bg-page);
}
.status-tabs {
  display: flex;
  background: var(--bg-white);
  padding: var(--spacing-sm) var(--spacing-md);
  gap: var(--spacing-sm);
  position: sticky;
  top: 0;
  z-index: 10;
  overflow-x: auto;
}
.status-tab {
  padding: 12rpx 28rpx;
  border-radius: var(--radius-round);
  background: var(--bg-grey);
  white-space: nowrap;
  flex-shrink: 0;
}
.status-tab.is-active {
  background: var(--primary-bg);
}
.status-tab__text {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}
.status-tab.is-active .status-tab__text {
  color: var(--primary-color);
  font-weight: 600;
}
.product-list {
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}
.product-item {
  background: var(--bg-white);
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}
.product-item__main {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
}
.product-item__img-wrap {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  flex-shrink: 0;
  border-radius: 12rpx;
  overflow: hidden;
}
.product-item__img {
  width: 100%;
  height: 100%;
}
.product-item__sold-mask,
.product-item__off-mask {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}
.product-item__sold-text,
.product-item__off-text {
  color: #fff;
  font-size: var(--font-sm);
  font-weight: bold;
}
.product-item__pending-tag,
.product-item__reject-tag {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  padding: 4rpx 12rpx;
  border-radius: var(--radius-sm);
  font-size: var(--font-xs);
}
.product-item__pending-tag {
  background: var(--warning-color);
  color: #fff;
}
.product-item__reject-tag {
  background: var(--danger-color);
  color: #fff;
}
.product-item__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.product-item__title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 500;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.product-item__price {
  font-size: var(--font-lg);
  color: var(--danger-color);
  font-weight: bold;
}
.product-item__time {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}
.product-item__actions {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md) var(--spacing-md);
  justify-content: flex-end;
}
.action-btn {
  padding: 12rpx 28rpx;
  border-radius: var(--radius-round);
  font-size: var(--font-sm);
  border: 1rpx solid;
}
.action-btn--default {
  border-color: var(--border-base);
  color: var(--text-regular);
}
.action-btn--primary {
  border-color: var(--primary-color);
  color: var(--primary-color);
}
.action-btn--warning {
  border-color: var(--warning-color);
  color: var(--warning-color);
}
.action-btn--danger {
  border-color: var(--danger-color);
  color: var(--danger-color);
}
.load-more {
  text-align: center;
  padding: var(--spacing-md);
  font-size: var(--font-sm);
  color: var(--text-secondary);
}
</style>