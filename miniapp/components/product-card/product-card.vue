<template>
  <view class="product-card" @click="goDetail">
    <view class="product-card__image">
      <image :src="product.coverImage" mode="aspectFill" class="product-card__image-inner" />
    </view>
    <view class="product-card__info">
      <text class="product-card__title text-ellipsis-2">{{ product.title }}</text>
      <view class="product-card__meta">
        <view v-if="product.conditionText" class="product-card__condition">
          <text class="product-card__condition-text">{{ product.conditionText }}</text>
        </view>
        <text class="product-card__campus">{{ product.campusName }}</text>
      </view>
      <view class="product-card__bottom">
        <Price :price="product.price" :original-price="product.originalPrice" size="md" />
        <text class="product-card__time">{{ product.createTime }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import Price from '@/components/price/price.vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

function goDetail() {
  if (!props.product || !props.product.id) {
    return
  }
  uni.navigateTo({
    url: `/pages/product/detail/detail?id=${props.product.id}`
  })
}
</script>

<style lang="scss" scoped>
.product-card {
  display: flex;
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-md);
}

.product-card__image {
  width: 220rpx;
  height: 220rpx;
  margin-right: var(--spacing-md);
  border-radius: var(--radius-md);
  overflow: hidden;
  background-color: var(--bg-grey);
}

.product-card__image-inner {
  width: 100%;
  height: 100%;
}

.product-card__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-card__title {
  font-size: var(--font-lg);
  color: var(--text-primary);
}

.product-card__meta {
  margin-top: var(--spacing-sm);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-card__condition {
  padding: 4rpx 12rpx;
  border-radius: var(--radius-round);
  background-color: var(--primary-bg);
}

.product-card__condition-text {
  font-size: var(--font-xs);
  color: var(--primary-color);
}

.product-card__campus {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.product-card__bottom {
  margin-top: var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-card__time {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}
</style>

