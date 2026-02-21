<template>
  <view class="empty-state">
    <view class="empty-state__icon">
      <text>{{ icon }}</text>
    </view>
    <text class="empty-state__text">{{ displayText }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const DEFAULT_TEXT = {
  'no-data': '暂无数据',
  'no-result': '未找到相关商品',
  'no-order': '暂无订单记录',
  'no-message': '暂无消息',
  'no-favorite': '暂无收藏',
  'network-error': '网络异常，请重试'
}

const ICON_MAP = {
  'no-data': '📄',
  'no-result': '🔍',
  'no-order': '📦',
  'no-message': '💬',
  'no-favorite': '⭐',
  'network-error': '⚠️'
}

const props = defineProps({
  type: {
    type: String,
    default: 'no-data'
  },
  text: {
    type: String,
    default: ''
  }
})

const displayText = computed(
  () => props.text || DEFAULT_TEXT[props.type] || DEFAULT_TEXT['no-data']
)

const icon = computed(() => ICON_MAP[props.type] || ICON_MAP['no-data'])
</script>

<style lang="scss" scoped>
.empty-state {
  padding: var(--spacing-xl) 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.empty-state__icon {
  font-size: 72rpx;
  margin-bottom: var(--spacing-md);
}

.empty-state__text {
  font-size: var(--font-md);
}
</style>

