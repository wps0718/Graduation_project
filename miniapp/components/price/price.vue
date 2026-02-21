<template>
  <view class="price" :class="`price--${size}`">
    <text class="price__symbol">¥</text>
    <text class="price__value">{{ formattedPrice }}</text>
    <text v-if="hasOriginal" class="price__original">¥{{ formattedOriginalPrice }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  price: {
    type: Number,
    required: true
  },
  originalPrice: {
    type: Number,
    default: null
  },
  size: {
    type: String,
    default: 'md'
  }
})

const formattedPrice = computed(() => {
  if (typeof props.price === 'number') {
    return props.price.toFixed(2)
  }
  return props.price
})

const hasOriginal = computed(() => typeof props.originalPrice === 'number')

const formattedOriginalPrice = computed(() => {
  if (typeof props.originalPrice === 'number') {
    return props.originalPrice.toFixed(2)
  }
  return props.originalPrice
})
</script>

<style lang="scss" scoped>
.price {
  display: flex;
  align-items: baseline;
  color: var(--price-color);
}

.price__symbol {
  margin-right: 4rpx;
}

.price--lg {
  font-size: var(--font-xl);
}

.price--md {
  font-size: var(--font-lg);
}

.price--sm {
  font-size: var(--font-md);
}

.price__value {
  font-weight: 600;
}

.price__original {
  margin-left: 12rpx;
  font-size: var(--font-sm);
  color: var(--text-secondary);
  text-decoration: line-through;
}
</style>

