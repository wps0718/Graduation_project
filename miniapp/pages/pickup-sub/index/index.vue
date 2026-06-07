<template>
  <view class="page">
    <!-- 顶部操作栏 -->
    <view class="top-bar">
      <view class="sort-group">
        <view
          v-for="opt in sortOptions"
          :key="opt.value"
          class="sort-tab"
          :class="{ 'sort-tab--active': sortBy === opt.value }"
          @click="changeSort(opt.value)"
        >
          <text class="sort-text">{{ opt.label }}</text>
        </view>
      </view>
      <view class="my-group">
        <view class="my-entry" @click="goMyPicked">
          <view class="my-icon picked-icon">
            <image class="icon-img" src="/static/pic/Pick_up_packages.png" mode="aspectFit" />
          </view>
          <text class="my-label">我的代拿</text>
        </view>
        <view class="my-entry" @click="goMyPublished">
          <view class="my-icon published-icon">
            <image class="icon-img" src="/static/pic/My_Orders.png" mode="aspectFit" />
          </view>
          <text class="my-label">我发布的</text>
        </view>
      </view>
    </view>

    <!-- 需求列表 -->
    <scroll-view
      scroll-y
      class="pickup-list"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && list.length === 0" class="loading-status">
        <view class="loading-more">
          <view class="loading-dot"></view>
          <view class="loading-dot"></view>
          <view class="loading-dot"></view>
          <text class="loading-text">加载中</text>
        </view>
      </view>

      <view v-else-if="list.length === 0" class="empty-state">
        <text class="empty-icon"> </text>
        <text class="empty-title">暂无代拿需求</text>
        <text class="empty-desc">有快递需要帮忙？发布一个需求试试</text>
      </view>

      <view v-else class="list-inner">
        <PickupCard
          v-for="item in list"
          :key="item.id"
          :item="item"
          :show-accept-btn="true"
          @click="goDetail(item)"
          @accept="handleAccept(item)"
        />
        <view v-if="!hasMore && list.length > 0" class="no-more">
          <view class="no-more-line"></view>
          <text class="no-more-text">没有更多了</text>
          <view class="no-more-line"></view>
        </view>
      </view>
    </scroll-view>

    <!-- 悬浮发布按钮 -->
    <view class="fab-btn" @click="goPublish">
      <text class="fab-icon">+</text>
      <text class="fab-text">发布需求</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { PICKUP_SORT_OPTIONS } from '@/utils/constant'
import PickupCard from '@/components/pickup-card/pickup-card.vue'

const list = ref([])
const page = ref(1)
const pageSize = 10
const total = ref(0)
const hasMore = computed(() => list.value.length < total.value)
const loading = ref(false)
const isRefreshing = ref(false)
const sortBy = ref('urgent')
const sortOptions = PICKUP_SORT_OPTIONS

async function loadList(isRefresh = false) {
  if (loading.value) return
  if (isRefresh) {
    page.value = 1
  }
  if (!isRefresh && !hasMore.value) return

  loading.value = true
  try {
    const res = await get('/mini/pickup/pool', {
      page: page.value,
      pageSize,
      sortBy: sortBy.value
    }, { showLoading: false })
    const records = res?.records || []
    if (isRefresh) {
      list.value = records
    } else {
      list.value = [...list.value, ...records]
    }
    total.value = Number(res?.total || 0)
    page.value++
  } catch (e) {
    console.error('加载代拿需求池失败', e)
  } finally {
    loading.value = false
  }
}

function onRefresh() {
  isRefreshing.value = true
  loadList(true).finally(() => {
    isRefreshing.value = false
  })
}

function loadMore() {
  loadList(false)
}

function changeSort(val) {
  if (sortBy.value === val) return
  sortBy.value = val
  loadList(true)
}

function goDetail(item) {
  uni.navigateTo({ url: `/pages/pickup-sub/detail/detail?id=${item.id}` })
}

function handleAccept(item) {
  uni.showModal({
    title: '确认接单',
    content: `确定要接下这单吗？报酬 ¥${Number(item.proposedPrice).toFixed(2)}`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await post('/mini/pickup/accept', { orderId: item.id })
          uni.showToast({ title: '接单成功', icon: 'success' })
          setTimeout(() => {
            uni.navigateTo({ url: `/pages/pickup-sub/detail/detail?id=${item.id}` })
          }, 500)
        } catch (e) {
          console.error('接单失败', e)
        }
      }
    }
  })
}

function goPublish() {
  uni.navigateTo({ url: '/pages/pickup-sub/publish/publish' })
}

function goMyPicked() {
  uni.navigateTo({ url: '/pages/pickup-sub/my-picked/my-picked' })
}

function goMyPublished() {
  uni.navigateTo({ url: '/pages/pickup-sub/my-published/my-published' })
}

onLoad(() => {
  loadList(true)
})

onShow(() => {
  if (list.value.length > 0) {
    loadList(true)
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f4fd 0%, #f5f7fa 30%, #f5f7fa 100%);
}

/* ========== 顶部操作栏 ========== */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 10;
}

.sort-group {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.sort-tab {
  padding: 14rpx 28rpx;
  border-radius: 40rpx;
  background: #f3f4f6;
  transition: all 0.3s ease;
}

.sort-tab--active {
  background: #3b82f6;
  box-shadow: 0 4rpx 16rpx rgba(59, 130, 246, 0.35);
}

.sort-text {
  font-size: 26rpx;
  color: #6b7280;
  font-weight: 500;
}

.sort-tab--active .sort-text {
  color: #ffffff;
  font-weight: 600;
}

/* ========== 我的入口 ========== */
.my-group {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex-shrink: 0;
}

.my-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  transition: all 0.2s ease;
}

.my-entry:active {
  transform: scale(0.92);
}

.my-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.picked-icon {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
}

.published-icon {
  background: linear-gradient(135deg, #f97316, #ea580c);
  box-shadow: 0 4rpx 12rpx rgba(249, 115, 22, 0.3);
}

.icon-img {
  width: 52rpx;
  height: 52rpx;
}

.my-label {
  font-size: 22rpx;
  color: #6b7280;
  font-weight: 500;
}

/* ========== 列表区域 ========== */
.pickup-list {
  height: calc(100vh - 100rpx - env(safe-area-inset-bottom));
}

.list-inner {
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* ========== 加载状态 ========== */
.loading-status {
  padding: 200rpx 0 60rpx;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.loading-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #3b82f6;
  animation: bounce 1.2s ease-in-out infinite;
}

.loading-dot:nth-child(2) { animation-delay: 0.2s; }
.loading-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.loading-text {
  font-size: 24rpx;
  color: #9ca3af;
  margin-left: 8rpx;
}

/* ========== 没有更多了 ========== */
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  padding: 40rpx 0;
}

.no-more-line {
  width: 80rpx;
  height: 2rpx;
  background: linear-gradient(90deg, transparent, #d1d5db, transparent);
}

.no-more-text {
  font-size: 24rpx;
  color: #9ca3af;
}

/* ========== 空状态 ========== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-title {
  font-size: 32rpx;
  color: #374151;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #9ca3af;
}

/* ========== 悬浮发布按钮 ========== */
.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: 120rpx;
  display: flex;
  align-items: center;
  padding: 24rpx 40rpx;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 48rpx;
  box-shadow:
    0 8rpx 24rpx rgba(37, 99, 235, 0.4),
    0 2rpx 8rpx rgba(37, 99, 235, 0.2);
  z-index: 100;
  transition: all 0.2s ease;
}

.fab-btn:active {
  transform: scale(0.95);
}

.fab-icon {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: 300;
  margin-right: 8rpx;
  line-height: 1;
}

.fab-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 600;
}
</style>
