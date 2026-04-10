<template>
  <view class="received-replies">
    <view class="header">
      <text class="title">收到回复</text>
    </view>

    <scroll-view
      class="scroll-view"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view v-if="replies.length > 0" class="reply-list">
        <view v-for="reply in replies" :key="reply.id" class="reply-item" @click="goToProduct(reply.productId)">
          <view class="reply-item__left">
            <UserAvatar :src="reply.fromAvatarUrl" :size="80" />
          </view>
          <view class="reply-item__center">
            <view class="reply-item__user-row">
              <text class="reply-item__user-name">{{ reply.fromNickName }}</text>
              <text class="reply-item__action-text">回复了我的留言</text>
            </view>
            <text class="reply-item__content">{{ reply.content }}</text>
            <text class="reply-item__time">{{ formatTime(reply.createTime) }}</text>
          </view>
          <view class="reply-item__right">
            <image class="reply-item__product-img" :src="reply.productImage" mode="aspectFill" />
          </view>
          <view v-if="reply.isRead === 0" class="reply-item__dot"></view>
        </view>
        <view v-if="loading" class="loading-more">加载中...</view>
        <view v-else-if="!hasMore" class="no-more">没有更多了</view>
      </view>
      <view v-else-if="!loading" class="empty-state">
        <EmptyState type="no-message" title="暂无回复" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, post } from '@/utils/request'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const replies = ref([])
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const hasMore = ref(true)
const isRefreshing = ref(false)

onMounted(() => {
  fetchReplies()
  markRead()
})

async function fetchReplies(isRefresh = false) {
  if (loading.value) return
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const data = await get('/mini/product/comment/received-replies', {
      page: page.value,
      pageSize: pageSize
    })
    if (isRefresh) {
      replies.value = data.records || []
    } else {
      replies.value = [...replies.value, ...(data.records || [])]
    }
    hasMore.value = replies.value.length < data.total
    page.value++
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

async function markRead() {
  try {
    await post('/mini/product/comment/mark-read')
  } catch (error) {
    console.error('Mark read error:', error)
  }
}

function onRefresh() {
  isRefreshing.value = true
  fetchReplies(true)
}

function loadMore() {
  fetchReplies()
}

function goToProduct(productId) {
  uni.navigateTo({ url: `/pages/product/detail/detail?id=${productId}` })
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
  
  const y = date.getFullYear()
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  
  if (y === now.getFullYear()) {
    return `${m}-${d}`
  }
  return `${y}-${m}-${d}`
}
</script>

<style lang="scss" scoped>
.received-replies {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--bg-page);
}

.header {
  padding: 30rpx;
  background-color: var(--bg-white);
  border-bottom: 1rpx solid var(--border-light);
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.scroll-view {
  flex: 1;
  overflow: hidden;
}

.reply-list {
  padding: 20rpx;
}

.reply-item {
  position: relative;
  display: flex;
  padding: 30rpx;
  background-color: var(--bg-white);
  border-radius: 16rpx;
  margin-bottom: 20rpx;

  &__left {
    flex-shrink: 0;
    margin-right: 20rpx;
  }

  &__center {
    flex: 1;
    overflow: hidden;
  }

  &__user-row {
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;
  }

  &__user-name {
    font-size: var(--font-md);
    font-weight: bold;
    color: var(--text-primary);
    margin-right: 12rpx;
  }

  &__action-text {
    font-size: 24rpx;
    color: var(--text-secondary);
  }

  &__content {
    font-size: var(--font-md);
    color: var(--text-primary);
    margin-bottom: 12rpx;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }

  &__time {
    font-size: 22rpx;
    color: var(--text-placeholder);
  }

  &__right {
    flex-shrink: 0;
    margin-left: 20rpx;
  }

  &__product-img {
    width: 120rpx;
    height: 120rpx;
    border-radius: 8rpx;
    background-color: var(--bg-grey);
  }

  &__dot {
    position: absolute;
    top: 30rpx;
    right: 30rpx;
    width: 16rpx;
    height: 16rpx;
    background-color: var(--error-color);
    border-radius: 50%;
  }
}

.loading-more, .no-more {
  text-align: center;
  padding: 30rpx;
  font-size: 24rpx;
  color: var(--text-placeholder);
}

.empty-state {
  padding-top: 200rpx;
}
</style>
