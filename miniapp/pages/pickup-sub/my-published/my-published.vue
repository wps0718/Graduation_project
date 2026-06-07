<template>
  <view class="page">
    <!-- 顶部Tab -->
    <view class="tab-bar">
      <view
        class="tab-item"
        :class="{ 'tab-item--active': activeTab === 'active' }"
        @click="switchTab('active')"
      >
        <text class="tab-text">进行中</text>
      </view>
      <view
        class="tab-item"
        :class="{ 'tab-item--active': activeTab === 'done' }"
        @click="switchTab('done')"
      >
        <text class="tab-text">已完成/已取消</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view
      scroll-y
      class="order-list"
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
        <text class="empty-title">暂无发布记录</text>
        <text class="empty-desc">点击右下角发布一个代拿需求吧</text>
      </view>

      <view v-else class="list-inner">
        <PickupCard
          v-for="item in list"
          :key="item.id"
          :item="item"
          :show-status="true"
          @click="goDetail(item)"
        />
        <view v-if="!hasMore && list.length > 0" class="no-more">
          <view class="no-more-line"></view>
          <text class="no-more-text">没有更多了</text>
          <view class="no-more-line"></view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import PickupCard from '@/components/pickup-card/pickup-card.vue'

const activeTab = ref('active')
const list = ref([])
const page = ref(1)
const pageSize = 20
const total = ref(0)
const loading = ref(false)
const isRefreshing = ref(false)

const hasMore = computed(() => list.value.length < total.value)

async function loadList(isRefresh = false) {
  if (loading.value) return
  if (isRefresh) {
    page.value = 1
  }
  if (!isRefresh && !hasMore.value) return

  loading.value = true
  try {
    const res = await get('/mini/pickup/my-published', {
      page: page.value,
      pageSize,
      statusGroup: activeTab.value
    }, { showLoading: isRefresh })
    const records = res?.records || []
    const totalCount = Number(res?.total || 0)

    if (isRefresh) {
      list.value = records
    } else {
      list.value = [...list.value, ...records]
    }
    total.value = totalCount
    page.value++
  } catch (e) {
    console.error('加载我发布的代拿列表失败', e)
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

function switchTab(val) {
  if (activeTab.value === val) return
  activeTab.value = val
  loadList(true)
}

function goDetail(item) {
  uni.navigateTo({ url: `/pages/pickup-sub/detail/detail?id=${item.id}` })
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
  background: #f5f7fa;
}

/* ========== Tab栏 ========== */
.tab-bar {
  display: flex;
  background: #ffffff;
  border-bottom: 1rpx solid #f3f4f6;
  padding: 0 24rpx;
  position: sticky;
  top: 0;
  z-index: 10;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 28rpx 0;
  position: relative;
  transition: all 0.3s ease;
}

.tab-text {
  font-size: 28rpx;
  color: #6b7280;
  font-weight: 500;
}

.tab-item--active .tab-text {
  color: #3b82f6;
  font-weight: 600;
}

.tab-item--active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: #3b82f6;
}

.tab-badge {
  background: #ef4444;
  border-radius: 20rpx;
  padding: 2rpx 12rpx;
  min-width: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge-text {
  font-size: 20rpx;
  color: #ffffff;
  font-weight: 600;
}

/* ========== 列表区域 ========== */
.order-list {
  height: calc(100vh - 100rpx);
}

.list-inner {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
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
  padding-top: 240rpx;
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
</style>
