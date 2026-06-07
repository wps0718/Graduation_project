<template>
  <view class="product-card" @click="goDetail" hover-class="product-card--active" :hover-stay-time="100">
    <view class="product-card__image">
      <image :src="imageSrc" mode="aspectFill" class="product-card__image-inner" @error="onImageError" />
    </view>
    <view class="product-card__info">
      <text class="product-card__title">{{ product.title }}</text>
      <view v-if="product.campusName || product.conditionText" class="product-card__tags">
        <view v-if="product.campusName" class="product-card__location">
          <text class="product-card__location-text">{{ product.campusName }}</text>
        </view>
        <view v-if="product.conditionText" class="product-card__condition">
          <text class="product-card__condition-text">{{ product.conditionText }}</text>
        </view>
      </view>
      <view class="product-card__bottom">
        <view class="product-card__price-group">
          <text class="product-card__price">¥{{ formattedPrice }}</text>
          <text v-if="hasOriginal" class="product-card__original">¥{{ formattedOriginal }}</text>
        </view>
        <text class="product-card__time">{{ formattedDate }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { resolveImageUrl } from '@/utils/image'

export default {
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      imageLoadFailed: false
    }
  },
  computed: {
    imageSrc() {
      if (this.imageLoadFailed) {
        return '/static/pic/校徽.png'
      }
      return resolveImageUrl(this.product && this.product.coverImage, {
        fallback: '/static/pic/校徽.png'
      })
    },
    formattedPrice() {
      const p = this.product?.price
      if (typeof p === 'number') return p.toFixed(2)
      return p ?? ''
    },
    hasOriginal() {
      return typeof this.product?.originalPrice === 'number'
    },
    formattedOriginal() {
      const p = this.product?.originalPrice
      if (typeof p === 'number') return p.toFixed(2)
      return p ?? ''
    },
    formattedDate() {
      const raw = this.product?.createTime
      if (!raw) return ''
      const match = String(raw).match(/^(\d{4}-\d{2}-\d{2})/)
      return match ? match[1] : raw
    }
  },
  watch: {
    'product.coverImage'() {
      this.imageLoadFailed = false
    }
  },
  methods: {
    onImageError() {
      this.imageLoadFailed = true
    },
    goDetail() {
      if (!this.product || !this.product.id) return
      uni.navigateTo({
        url: `/pages/product-sub/detail/detail?id=${this.product.id}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.product-card {
  display: flex;
  padding: 24rpx;
  background-color: #fff;
  border-radius: 8rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 1rpx 8rpx rgba(0, 0, 0, 0.04);
}

.product-card--active {
  background-color: #f9f9f9;
}

.product-card__image {
  width: 180rpx;
  height: 180rpx;
  margin-right: 24rpx;
  border-radius: 8rpx;
  overflow: hidden;
  background-color: #f5f5f5;
  flex-shrink: 0;
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
  min-width: 0;
}

.product-card__title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-card__tags {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 12rpx;
}

.product-card__location {
  background: #f5f5f5;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.product-card__location-text {
  font-size: 22rpx;
  color: #666;
}

.product-card__condition {
  background: #f5f5f5;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.product-card__condition-text {
  font-size: 22rpx;
  color: #666;
}

.product-card__bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 12rpx;
}

.product-card__price-group {
  display: flex;
  align-items: baseline;
}

.product-card__price {
  font-size: 34rpx;
  font-weight: bold;
  color: #ff4a4a;
}

.product-card__original {
  font-size: 22rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 8rpx;
}

.product-card__time {
  font-size: 22rpx;
  color: #999;
  flex-shrink: 0;
}
</style>
