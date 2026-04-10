<template>
  <view class="message-settings-page">
    <view :style="{ height: `${statusBarHeight}px` }"></view>
    <view class="settings-nav" :style="{ height: `${navBarHeight}px` }">
      <view class="settings-nav__left" @click="goBack">
        <image class="settings-nav__back" src="/static/svg/back.svg" mode="aspectFit" />
      </view>
      <text class="settings-nav__title">消息设置</text>
      <view class="settings-nav__right"></view>
    </view>

    <view class="settings-card">
      <view class="settings-item" @click="showToast('功能开发中')">
        <text class="settings-item__label">新消息通知</text>
        <text class="settings-item__value">开发中</text>
      </view>
      <view class="settings-divider"></view>
      <view class="settings-item" @click="showToast('功能开发中')">
        <text class="settings-item__label">声音</text>
        <text class="settings-item__value">开发中</text>
      </view>
      <view class="settings-divider"></view>
      <view class="settings-item" @click="showToast('功能开发中')">
        <text class="settings-item__label">震动</text>
        <text class="settings-item__value">开发中</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const statusBarHeight = ref(0)
const navBarHeight = ref(44)

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
})

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function goBack() {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: '/pages/chat/list/list' })
    return
  }
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.message-settings-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: 0 var(--spacing-md);
  box-sizing: border-box;
}

.settings-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}

.settings-nav__left,
.settings-nav__right {
  width: 140rpx;
  display: flex;
  align-items: center;
}

.settings-nav__right {
  justify-content: flex-end;
}

.settings-nav__back {
  width: 44rpx;
  height: 44rpx;
}

.settings-nav__title {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.settings-card {
  margin-top: var(--spacing-md);
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
}

.settings-item__label {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.settings-item__value {
  font-size: var(--font-sm);
  color: var(--text-placeholder);
}

.settings-divider {
  height: 1rpx;
  background-color: var(--border-light);
  margin: 0 var(--spacing-md);
}
</style>
