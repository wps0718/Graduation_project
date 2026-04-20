<template>
  <view class="settings-page">
    <view class="settings-card">
      <view class="settings-item" @click="goEditProfile">
        <text class="settings-item__label">个人信息</text>
        <text class="settings-item__arrow">›</text>
      </view>
      <view class="settings-divider"></view>
      <view class="settings-item" @click="goAuth">
        <text class="settings-item__label">校园认证</text>
        <view class="settings-item__right">
          <StatusTag type="auth" :value="userInfo.authStatus || 0" />
          <text class="settings-item__arrow">›</text>
        </view>
      </view>
    </view>

    <view class="settings-card">
      <view class="settings-item" @click="goAgreement">
        <text class="settings-item__label">用户协议</text>
        <text class="settings-item__arrow">›</text>
      </view>
      <view class="settings-divider"></view>
      <view class="settings-item" @click="goPrivacy">
        <text class="settings-item__label">隐私政策</text>
        <text class="settings-item__arrow">›</text>
      </view>
      <view class="settings-divider"></view>
      <view class="settings-item" @click="goAbout">
        <text class="settings-item__label">关于我们</text>
        <text class="settings-item__arrow">›</text>
      </view>
    </view>

    <view class="settings-card">
      <view class="settings-item settings-item--danger" @click="onCancelAccount">
        <text class="settings-item__label">账号注销</text>
      </view>
    </view>

    <view class="settings-footer safe-area-bottom">
      <view class="settings-logout" @click="onLogout">退出登录</view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { post } from '@/utils/request'
import { useUserStore } from '@/store'
import StatusTag from '@/components/status-tag/status-tag.vue'

const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo || {})

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function ensureLogin() {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login-sub/login/login' })
    return false
  }
  return true
}

function goEditProfile() {
  if (!ensureLogin()) return
  uni.navigateTo({ url: '/pages/user-sub/settings/edit-profile' })
}

function goAuth() {
  if (!ensureLogin()) return
  uni.navigateTo({ url: '/pages/auth-sub/auth/auth' })
}

function goAgreement() {
  uni.navigateTo({ url: '/pages/login-sub/agreement/agreement' })
}

function goPrivacy() {
  uni.navigateTo({ url: '/pages/login-sub/privacy/privacy' })
}

function goAbout() {
  uni.navigateTo({ url: '/pages/user-sub/settings/about' })
}

function onLogout() {
  if (!ensureLogin()) return
  uni.showModal({
    title: '提示',
    content: '确认退出当前账号吗？',
    confirmText: '退出',
    cancelText: '取消',
    success: (res) => {
      if (res && res.confirm) {
        userStore.logout()
        uni.reLaunch({ url: '/pages/login-sub/login/login' })
      }
    }
  })
}

async function onCancelAccount() {
  if (!ensureLogin()) return
  const confirmed = await new Promise((resolve) => {
    uni.showModal({
      title: '账号注销',
      content: '注销后30天内可恢复，超过30天数据将永久删除',
      confirmText: '确认注销',
      cancelText: '取消',
      success: (res) => resolve(res && res.confirm)
    })
  })
  if (!confirmed) return
  const hasActiveTrade = false
  if (hasActiveTrade) {
    showToast('存在进行中的交易，暂无法注销')
    return
  }
  try {
    await post('/mini/user/deactivate', {}, { showLoading: true })
    userStore.logout()
    uni.reLaunch({ url: '/pages/login-sub/login/login' })
  } catch (error) {
    showToast('注销失败，请稍后重试')
  }
}

onShow(() => {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login-sub/login/login' })
  }
})
</script>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.settings-card {
  background-color: var(--bg-white);
  border-radius: 24rpx;
  padding: 6rpx 0;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}

.settings-item {
  padding: 22rpx var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--text-primary);
  font-size: var(--font-md);
}

.settings-item__label {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.settings-item__right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.settings-item__arrow {
  font-size: 34rpx;
  color: var(--text-secondary);
}

.settings-item--danger {
  color: var(--danger-color);
}

.settings-divider {
  height: 1rpx;
  background-color: var(--border-light);
  margin: 0 var(--spacing-md);
}

.settings-footer {
  margin-top: auto;
  padding-top: var(--spacing-sm);
}

.settings-logout {
  width: 100%;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 999rpx;
  border: 2rpx solid var(--danger-color);
  color: var(--danger-color);
  font-size: var(--font-md);
  background-color: var(--bg-white);
}
</style>
