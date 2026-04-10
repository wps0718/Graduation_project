<template>
  <view class="favorites-page">
    <view class="favorites-nav" :style="{ height: `${navBarHeight}px` }">
      <text class="favorites-nav__title">收到收藏</text>
    </view>

    <scroll-view
      class="favorites-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="favorites.length > 0" class="favorites-list">
        <view
          v-for="item in favorites"
          :key="item.id"
          class="favorites-item"
          @click="goToProduct(item)"
        >
          <view v-if="!item.isRead" class="favorites-item__dot"></view>
          <view class="favorites-item__avatar">
            <UserAvatar :src="item.avatarUrl" :size="96" />
          </view>
          <view class="favorites-item__content">
            <view class="favorites-item__header">
              <text class="favorites-item__username">{{ item.nickName }}</text>
            </view>
            <text class="favorites-item__action">收藏了我的商品</text>
            <text class="favorites-item__time">{{ formatTime(item.createTime) }}</text>
          </view>
          <view class="favorites-item__product">
            <image class="favorites-item__product-img" :src="item.productImage" mode="aspectFill" />
            <view class="favorites-item__product-info">
              <text class="favorites-item__product-title">{{ item.productTitle }}</text>
              <text class="favorites-item__product-price">￥{{ item.productPrice }}</text>
            </view>
          </view>
        </view>
        <view v-if="loading" class="favorites-loading">加载中...</view>
        <view v-else-if="!hasMore" class="favorites-loading">没有更多了</view>
      </view>
      <view v-else-if="!loading" class="favorites-empty">
        <EmptyState type="no-message" title="暂无收藏通知" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { post, get } from '@/utils/request'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const statusBarHeight = ref(0)
const navBarHeight = ref(44)

const favorites = ref([])
const page = ref(1)
const pageSize = 20
const loading = ref(false)
const hasMore = ref(true)
const isRefreshing = ref(false)

onMounted(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = (info && info.statusBarHeight) || 0
  const menuButton = typeof uni.getMenuButtonBoundingClientRect === 'function'
    ? uni.getMenuButtonBoundingClientRect()
    : null
  if (menuButton && menuButton.top) {
    const padding = menuButton.top - statusBarHeight.value
    navBarHeight.value = menuButton.height + padding * 2
  } else {
    navBarHeight.value = 44
  }
  fetchFavorites()
})

async function markAsRead(id) {
  try {
    await post('/mini/notification/read', { id })
    const item = favorites.value.find(f => f.id === id)
    if (item) item.isRead = 1
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

function goToProduct(item) {
  if (item.productId) {
    if (!item.isRead) {
      markAsRead(item.id)
    }
    uni.navigateTo({ url: `/pages/product/detail/detail?id=${item.productId}` })
  }
}

async function fetchFavorites(isRefresh = false) {
  if (loading.value && !isRefresh) return
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
    isRefreshing.value = true
  }
  if (!hasMore.value) {
    isRefreshing.value = false
    return
  }

  loading.value = true
  try {
    const data = await get('/mini/notification/favorite-list', {
      page: page.value,
      pageSize: pageSize
    })
    const records = data?.records || []
    if (isRefresh) {
      favorites.value = records
    } else {
      favorites.value = [...favorites.value, ...records]
    }
    hasMore.value = favorites.value.length < (data?.total || 0)
    page.value++
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

function onRefresh() {
  fetchFavorites(true)
}

function loadMore() {
  if (!hasMore.value || loading.value) return
  fetchFavorites()
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace('T', ' ').replace(/-/g, '/'))
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return Math.floor(diff / minute) + '分钟前'
  if (diff < day) return Math.floor(diff / hour) + '小时前'

  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  return `${m}-${d}`
}
</script>

<style lang="scss" scoped>
.favorites-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  display: flex;
  flex-direction: column;
}

.favorites-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--spacing-md);
  background-color: var(--bg-white);
  box-shadow: 0 6rpx 20rpx rgba(15, 23, 42, 0.05);
}

.favorites-nav__title {
  font-size: var(--font-lg);
  color: var(--text-primary);
  font-weight: 600;
}

.favorites-scroll {
  flex: 1;
  overflow: hidden;
}

.favorites-list {
  padding: var(--spacing-md);
}

.favorites-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-md);
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}

.favorites-item__dot {
  position: absolute;
  left: 16rpx;
  top: 24rpx;
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: var(--danger-color);
}

.favorites-item__avatar {
  flex-shrink: 0;
  margin-right: var(--spacing-md);
}

.favorites-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.favorites-item__header {
  display: flex;
  align-items: center;
}

.favorites-item__username {
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-primary);
}

.favorites-item__action {
  font-size: var(--font-sm);
  color: var(--text-regular);
}

.favorites-item__time {
  font-size: 22rpx;
  color: var(--text-placeholder);
}

.favorites-item__product {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-left: var(--spacing-sm);
  max-width: 180rpx;
}

.favorites-item__product-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: var(--radius-sm);
  background-color: var(--bg-grey);
  margin-bottom: 8rpx;
}

.favorites-item__product-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.favorites-item__product-title {
  font-size: 22rpx;
  color: var(--text-secondary);
  max-width: 160rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  text-align: right;
}

.favorites-item__product-price {
  font-size: 24rpx;
  color: var(--danger-color);
  font-weight: 500;
  margin-top: 4rpx;
}

.favorites-loading {
  text-align: center;
  padding: var(--spacing-md) 0;
  color: var(--text-placeholder);
  font-size: var(--font-sm);
}

.favorites-empty {
  padding-top: 200rpx;
}
</style>
