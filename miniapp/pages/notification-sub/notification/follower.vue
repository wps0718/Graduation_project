<template>
  <view class="follower-page">
    <view :style="{ height: `${statusBarHeight}px` }"></view>
    <view class="follower-nav" :style="{ height: `${navBarHeight}px` }">
      <view class="follower-nav__left" @click="goBack">
        <text class="follower-nav__back">‹</text>
      </view>
      <text class="follower-nav__title">新增粉丝</text>
      <view class="follower-nav__right" @click="goSettings">
        <text class="follower-nav__icon">⚙</text>
      </view>
    </view>

    <view class="follower-content">
      <view v-if="followerList.length" class="follower-list">
        <view
          v-for="item in followerList"
          :key="item.id"
          class="follower-item"
          @click="goProfile(item.userId)"
        >
          <view class="follower-item__avatar">
            <UserAvatar :avatar-url="item.avatarUrl" :nick-name="item.nickName" size="md" />
          </view>
          <view class="follower-item__info">
            <view class="follower-item__header">
              <text class="follower-item__name">{{ item.nickName }}</text>
              <text class="follower-item__action-text">关注了我</text>
            </view>
            <text class="follower-item__time">{{ formatTime(item.createTime) }}</text>
          </view>
          <view class="follower-item__btn-wrap">
            <view
              v-if="!item.isFollowing"
              class="follower-item__btn is-follow"
              @click.stop="toggleFollow(item)"
            >
              <text class="follower-item__btn-text">回关</text>
            </view>
            <view v-else class="follower-item__btn is-mutual">
              <text class="follower-item__btn-text">已互粉</text>
            </view>
          </view>
        </view>
        <view v-if="loadingMore" class="follower-loading">加载中...</view>
        <view v-else-if="!hasMore" class="follower-loading">没有更多了</view>
      </view>
      <view v-else class="follower-empty">
        <EmptyState type="no-message" text="暂无新粉丝" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { useUserStore } from '@/store'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const navBarHeight = ref(44)

const followerList = ref([])
const page = ref(1)
const pageSize = 20
const hasMore = ref(true)
const loading = ref(false)
const loadingMore = ref(false)

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
  fetchFollowers(1, true)
})

onPullDownRefresh(async () => {
  await fetchFollowers(1, true)
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    fetchFollowers(page.value + 1)
  }
})

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function goBack() {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: '/pages/index/index' })
    return
  }
  uni.navigateBack()
}

function goSettings() {
  uni.navigateTo({ url: '/pages/user-sub/settings/settings' })
}

function goProfile(userId) {
  uni.navigateTo({ url: `/pages/user-sub/seller/profile?id=${userId}` })
}

function formatTime(value) {
  if (!value) return ''
  let date
  if (typeof value === 'number') {
    date = new Date(value)
  } else if (typeof value === 'string') {
    // 兼容 iOS 端的日期解析
    const normalized = value.replace('T', ' ').replace(/-/g, '/')
    date = new Date(normalized)
  } else {
    date = new Date(value)
  }

  if (isNaN(date.getTime())) return ''

  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  
  return `${year}年${month}月${day}日`
}

async function fetchFollowers(targetPage, refresh = false) {
  if (loading.value) return
  loading.value = true
  if (!refresh) loadingMore.value = true
  
  try {
    const res = await get('/mini/notification/follower-list', {
      page: targetPage,
      pageSize
    })
    
    const records = (res && res.records) || []
    if (refresh) {
      followerList.value = records
    } else {
      followerList.value = followerList.value.concat(records)
    }
    
    page.value = targetPage
    hasMore.value = (res && typeof res.total === 'number') ? followerList.value.length < res.total : records.length >= pageSize
    
    // 标记为已读：如果是第一页刷新，直接将该类型全部标为已读
    if (refresh) {
      post('/mini/notification/read-type?type=11').catch(() => {})
    }
  } catch (error) {
    showToast('加载失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function toggleFollow(item) {
  if (item.loading) return
  item.loading = true
  try {
    await post('/mini/follow/follow', { userId: item.userId })
    item.isFollowing = true
    showToast('关注成功')
  } catch (error) {
    showToast(error.message || '操作失败')
  } finally {
    item.loading = false
  }
}
</script>

<style scoped>
.follower-page {
  min-height: 100vh;
  background-color: #fff;
}

.follower-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1rpx solid #f8f8f8;
}

.follower-nav__back {
  font-size: 56rpx;
  color: #333;
  font-weight: 300;
}

.follower-nav__title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.follower-nav__icon {
  font-size: 44rpx;
  color: #333;
}

.follower-content {
  padding: 0 30rpx;
}

.follower-item {
  display: flex;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #f2f2f2;
}

.follower-item:last-child {
  border-bottom: none;
}

.follower-item:active {
  background-color: #fafafa;
}

.follower-item__avatar {
  margin-right: 24rpx;
}

.follower-item__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.follower-item__header {
  display: flex;
  align-items: center;
}

.follower-item__name {
  font-size: 30rpx;
  color: #333;
  font-weight: 600;
}

.follower-item__action-text {
  font-size: 28rpx;
  color: #999;
  margin-left: 12rpx;
}

.follower-item__time {
  font-size: 24rpx;
  color: #bbb;
}

.follower-item__btn {
  width: 140rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 40rpx; /* 20px */
  font-size: 26rpx;
  transition: all 0.2s ease;
}

.is-follow {
  border: 2rpx solid #ff2442; /* 小红书红 */
  color: #ff2442;
  font-weight: 500;
}

.is-mutual {
  background-color: #f5f5f5;
  color: #999;
}

.follower-loading {
  text-align: center;
  padding: 40rpx;
  font-size: 24rpx;
  color: #999;
}

.follower-empty {
  padding-top: 240rpx;
}
</style>
