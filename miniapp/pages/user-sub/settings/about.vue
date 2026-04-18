<template>
  <view class="about-page">
    <view class="about-card">
      <image class="about-logo" src="/static/pic/校徽.png" mode="aspectFit" />
      <text class="about-title">轻院二手</text>
      <text class="about-version">v1.0.0</text>
      <text class="about-school">广东轻工职业技术大学</text>
    </view>
  </view>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.about-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-lg);
  box-sizing: border-box;
}

.about-card {
  background-color: var(--bg-white);
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}

.about-logo {
  width: 140rpx;
  height: 140rpx;
  border-radius: 40rpx;
  background-color: var(--bg-grey);
}

.about-title {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.about-version {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.about-school {
  font-size: var(--font-md);
  color: var(--text-regular);
}
</style>
