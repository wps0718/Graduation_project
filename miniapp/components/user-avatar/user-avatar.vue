<template>
  <view class="user-avatar" :class="sizeClass">
    <view class="user-avatar__image-wrapper">
      <image
        v-if="avatarUrl"
        :src="avatarUrl"
        class="user-avatar__image"
        mode="aspectFill"
      />
      <text v-else class="user-avatar__placeholder">{{ initial }}</text>
      <view
        v-if="showAuth && authStatus === 2"
        class="user-avatar__auth-badge"
      >
        <text class="user-avatar__auth-text">✔</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  avatarUrl: {
    type: String,
    default: ''
  },
  nickName: {
    type: String,
    default: ''
  },
  authStatus: {
    type: Number,
    default: 0
  },
  size: {
    type: String,
    default: 'md'
  },
  showAuth: {
    type: Boolean,
    default: true
  }
})

const initial = computed(() => {
  if (props.nickName && props.nickName.length > 0) {
    return props.nickName.slice(0, 1)
  }
  return ''
})

const sizeClass = computed(() => `user-avatar--${props.size}`)
</script>

<style lang="scss" scoped>
.user-avatar {
  display: inline-flex;
}

.user-avatar__image-wrapper {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  background-color: var(--bg-grey);
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar--lg .user-avatar__image-wrapper {
  width: 120rpx;
  height: 120rpx;
}

.user-avatar--md .user-avatar__image-wrapper {
  width: 80rpx;
  height: 80rpx;
}

.user-avatar--sm .user-avatar__image-wrapper {
  width: 60rpx;
  height: 60rpx;
}

.user-avatar__image {
  width: 100%;
  height: 100%;
}

.user-avatar__placeholder {
  width: 100%;
  height: 100%;
  font-size: var(--font-lg);
  color: var(--text-white);
  background-color: var(--primary-color);
  text-align: center;
  line-height: 80rpx;
}

.user-avatar--lg .user-avatar__placeholder {
  line-height: 120rpx;
}

.user-avatar--sm .user-avatar__placeholder {
  line-height: 60rpx;
}

.user-avatar__auth-badge {
  position: absolute;
  right: 0;
  bottom: 0;
  background-color: var(--primary-color);
  border-radius: 50%;
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar__auth-text {
  font-size: 20rpx;
  color: var(--text-white);
}
</style>

