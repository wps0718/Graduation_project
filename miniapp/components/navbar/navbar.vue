<template>
  <view class="navbar">
    <view :style="statusBarStyle"></view>
    <view class="navbar__content" :style="contentStyle">
      <view class="navbar__left" :style="sideStyle">
        <slot name="left">
          <view v-if="showBack" class="navbar__back" @click="goBack">
            <text class="navbar__back-icon">‹</text>
          </view>
        </slot>
      </view>
      <view class="navbar__title">
        <text class="navbar__title-text">{{ title }}</text>
      </view>
      <view class="navbar__right" :style="sideStyle">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  showBack: {
    type: Boolean,
    default: true
  }
})

const statusBarHeight = ref(20)
const navBarHeight = ref(44)
const sideWidth = ref(120)

onMounted(() => {
  const info = uni.getSystemInfoSync()
  if (info && info.statusBarHeight) {
    statusBarHeight.value = info.statusBarHeight
  }
  const menuButton = typeof uni.getMenuButtonBoundingClientRect === 'function'
    ? uni.getMenuButtonBoundingClientRect()
    : null
  if (menuButton && menuButton.top) {
    const padding = menuButton.top - statusBarHeight.value
    navBarHeight.value = menuButton.height + padding * 2
    if (info && info.windowWidth) {
      sideWidth.value = info.windowWidth - menuButton.left
    }
  } else {
    navBarHeight.value = 44
  }
})

const statusBarStyle = computed(() => ({
  height: `${statusBarHeight.value}px`
}))

const contentStyle = computed(() => ({
  height: `${navBarHeight.value}px`
}))

const sideStyle = computed(() => ({
  width: `${sideWidth.value}px`
}))

function goBack() {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: '/pages/index/index' })
    return
  }
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.navbar {
  width: 100%;
  background-color: var(--bg-white);
}

.navbar__content {
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-md);
}

.navbar__left,
.navbar__right {
  width: 120rpx;
  display: flex;
  align-items: center;
}

.navbar__title {
  flex: 1;
  display: flex;
  justify-content: center;
}

.navbar__title-text {
  font-size: var(--font-lg);
  color: var(--text-primary);
}

.navbar__back {
  padding: var(--spacing-xs);
}

.navbar__back-icon {
  font-size: 40rpx;
  color: var(--text-primary);
}
</style>
