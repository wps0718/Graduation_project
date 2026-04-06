<template>
  <view class="user-page">
    <view class="user-card" @click="handleAvatarClick">
      <view class="user-card__header">
        <UserAvatar
          :avatar-url="userInfo.avatarUrl"
          :nick-name="userInfo.nickName"
          :auth-status="userInfo.authStatus || 0"
          size="lg"
          :show-auth="true"
        />
        <view class="user-card__info">
          <view class="user-card__name-row">
            <text class="user-card__name">{{ displayName }}</text>
            <view v-if="isLogin" class="user-card__auth" @click.stop="handleAuthClick">
              <StatusTag type="auth" :value="userInfo.authStatus || 0" />
            </view>
          </view>
          <text v-if="isLogin" class="user-card__campus">
            {{ userInfo.campusName || '' }}
          </text>
        </view>
      </view>
      <view class="user-card__stats">
        <view class="user-card__stat">
          <text class="user-card__stat-value">{{ scoreText }}</text>
          <text class="user-card__stat-label">信誉评分</text>
        </view>
        <view class="user-card__stat">
          <text class="user-card__stat-value">{{ stats.onSaleCount || 0 }}</text>
          <text class="user-card__stat-label">在售</text>
        </view>
        <view class="user-card__stat">
          <text class="user-card__stat-value">{{ stats.soldCount || 0 }}</text>
          <text class="user-card__stat-label">已售</text>
        </view>
        <view class="user-card__stat">
          <text class="user-card__stat-value">{{ stats.favoriteCount || 0 }}</text>
          <text class="user-card__stat-label">收藏</text>
        </view>
      </view>
    </view>

    <view class="user-list">
      <view class="user-list__card">
        <view class="user-list__item" @click="goFootprint">
          <view class="user-list__left">
            <text class="user-list__icon">👣</text>
            <text class="user-list__text">足迹</text>
          </view>
          <text class="user-list__arrow">›</text>
        </view>
        <view class="user-list__divider"></view>
        <view class="user-list__item" @click="goFavorite">
          <view class="user-list__left">
            <text class="user-list__icon">❤</text>
            <text class="user-list__text">我收藏的</text>
          </view>
          <text class="user-list__arrow">›</text>
        </view>
        <view class="user-list__divider"></view>
        <view class="user-list__item" @click="goOrder">
          <view class="user-list__left">
            <text class="user-list__icon">📋</text>
            <text class="user-list__text">交易记录</text>
          </view>
          <text class="user-list__arrow">›</text>
        </view>
      </view>

      <view class="user-list__card">
        <view class="user-list__item" @click="goNotification">
          <view class="user-list__left">
            <text class="user-list__icon">🔔</text>
            <text class="user-list__text">消息通知</text>
          </view>
          <view class="user-list__right">
            <view v-if="unreadBadge" class="user-list__badge">
              <text class="user-list__badge-text">{{ unreadBadge }}</text>
            </view>
            <text class="user-list__arrow">›</text>
          </view>
        </view>
        <view class="user-list__divider"></view>
        <view class="user-list__item" @click="goSettings">
          <view class="user-list__left">
            <text class="user-list__icon">⚙</text>
            <text class="user-list__text">设置</text>
          </view>
          <text class="user-list__arrow">›</text>
        </view>
        <view class="user-list__divider"></view>
        <view class="user-list__item" @click="goHelp">
          <view class="user-list__left">
            <text class="user-list__icon">💬</text>
            <text class="user-list__text">客服与帮助</text>
          </view>
          <text class="user-list__arrow">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import { useUserStore } from '@/store'
import { AUTH_STATUS } from '@/utils/constant'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import StatusTag from '@/components/status-tag/status-tag.vue'

const userStore = useUserStore()

const unreadCount = ref(0)

const isLogin = computed(() => userStore.isLogin)
const userInfo = computed(() => userStore.userInfo || {})
const stats = computed(() => userStore.stats || {})

const displayName = computed(() => {
  if (!isLogin.value) {
    return '点击登录'
  }
  return userInfo.value.nickName || '未命名用户'
})

const scoreText = computed(() => {
  if (!isLogin.value) {
    return '0'
  }
  return String(userInfo.value.score || 0)
})

const unreadBadge = computed(() => {
  if (!unreadCount.value) {
    return ''
  }
  return unreadCount.value > 99 ? '99+' : String(unreadCount.value)
})

function handleAvatarClick() {
  if (!isLogin.value) {
    uni.navigateTo({ url: '/pages/login/login' })
  }
}

function handleAuthClick() {
  if (!isLogin.value) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  if (userInfo.value.authStatus !== AUTH_STATUS.VERIFIED) {
    uni.navigateTo({ url: '/pages/auth/auth' })
  }
}

function ensureLogin() {
  if (!isLogin.value) {
    uni.navigateTo({ url: '/pages/login/login' })
    return false
  }
  return true
}

function goFavorite() {
  if (!ensureLogin()) return
  uni.navigateTo({ url: '/pages/favorite/favorite' })
}

function goOrder() {
  if (!ensureLogin()) return
  uni.navigateTo({ url: '/pages/order/list/list' })
}

function goNotification() {
  if (!ensureLogin()) return
  uni.navigateTo({ url: '/pages/notification/notification' })
}

function goSettings() {
  if (!ensureLogin()) return
  uni.navigateTo({ url: '/pages/settings/settings' })
}

function goHelp() {
  if (!ensureLogin()) return
  uni.navigateTo({ url: '/pages/help/help' })
}

function goFootprint() {
  if (!ensureLogin()) return
  uni.navigateTo({ url: '/pages/footprint/footprint' })
}

async function loadUnreadCount() {
  if (!isLogin.value) {
    unreadCount.value = 0
    return
  }
  const data = await get('/mini/notification/unread-count', {}, { showLoading: false })
  unreadCount.value = (data && data.total) || 0
}

async function refreshAuthStatus() {
  if (!isLogin.value) {
    return
  }
  try {
    const data = await get('/mini/auth/status', {}, { showLoading: false })
    const status = typeof (data && data.status) === 'number' ? data.status : AUTH_STATUS.NONE
    userStore.setUserInfo({ ...(userStore.userInfo || {}), authStatus: status })
  } catch (error) {
  }
}

onShow(async () => {
  if (isLogin.value) {
    await Promise.all([userStore.fetchUserInfo(), refreshAuthStatus(), userStore.updateStats(), loadUnreadCount()])
  } else {
    unreadCount.value = 0
  }
})
</script>

<style lang="scss" scoped>
.user-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  box-sizing: border-box;
}

.user-card {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-light) 100%);
  border-radius: 28rpx;
  padding: 28rpx;
  color: var(--text-white);
}

.user-card__header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-card__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.user-card__name-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-card__name {
  font-size: var(--font-lg);
  font-weight: 600;
}

.user-card__campus {
  font-size: var(--font-sm);
  color: var(--text-white);
  opacity: 0.88;
}

.user-card__stats {
  margin-top: var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  text-align: center;
}

.user-card__stat {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.user-card__stat-value {
  font-size: 34rpx;
  font-weight: 600;
}

.user-card__stat-label {
  font-size: var(--font-xs);
  color: var(--text-white);
  opacity: 0.85;
}

.user-list {
  margin-top: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.user-list__card {
  background-color: var(--bg-white);
  border-radius: 24rpx;
  padding: 10rpx 0;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}

.user-list__item {
  padding: 22rpx var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-list__left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-list__icon {
  font-size: 30rpx;
}

.user-list__text {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.user-list__right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-list__arrow {
  font-size: 34rpx;
  color: var(--text-secondary);
}

.user-list__divider {
  height: 1rpx;
  background-color: var(--border-light);
  margin: 0 var(--spacing-md);
}

.user-list__badge {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  border-radius: 18rpx;
  background-color: var(--danger-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-list__badge-text {
  font-size: var(--font-xs);
  color: var(--text-white);
}
</style>
