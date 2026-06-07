<template>
  <view class="pickup-card" @click="$emit('click')">
    <view class="card-accent"></view>
    <view class="card-content">
      <view class="card-header">
        <view class="location-group">
          <view class="location-item">
            <view class="dot dot--green"></view>
            <text class="location-text">{{ item.pickupLocation || '' }}</text>
          </view>
          <view class="location-arrow">
            <view class="arrow-line"></view>
            <text class="arrow-head">›</text>
          </view>
          <view class="location-item">
            <view class="dot dot--red"></view>
            <text class="location-text">{{ item.deliveryLocation || '' }}</text>
          </view>
        </view>
      </view>
      <view class="card-body">
        <view class="price-group">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ priceInteger }}</text>
          <text class="price-decimal">.{{ priceDecimal }}</text>
        </view>
        <view class="time-tag">
          <text class="time-tag-text">{{ deliveryTimeText }}</text>
        </view>
      </view>
      <view class="card-footer">
        <text class="publish-time">{{ createTimeText }}</text>
        <view class="card-footer__right">
          <StatusTag v-if="showStatus" type="pickup" :value="item.status" />
          <view v-if="showAcceptBtn" class="accept-btn" @click.stop="$emit('accept')">
            <text class="btn-text">立即接单</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import StatusTag from '@/components/status-tag/status-tag.vue'
import { formatRelativeTime, formatDeliveryTime } from '@/utils/pickup-helpers'

const props = defineProps({
  item: { type: Object, required: true },
  showAcceptBtn: { type: Boolean, default: false },
  showStatus: { type: Boolean, default: false }
})

defineEmits(['click', 'accept'])

const priceInteger = computed(() => {
  const price = Number(props.item.agreedPrice || props.item.proposedPrice)
  return isNaN(price) ? '0' : Math.floor(price).toString()
})

const priceDecimal = computed(() => {
  const price = Number(props.item.agreedPrice || props.item.proposedPrice)
  return isNaN(price) ? '00' : (price % 1).toFixed(2).slice(2)
})

const deliveryTimeText = computed(() => {
  const raw = formatDeliveryTime(props.item.expectedDeliveryTime)
  if (!raw) return '尽快送达'
  return '⏰ ' + raw + ' 前送达'
})

const createTimeText = computed(() => {
  return formatRelativeTime(props.item.createTime) || '刚刚'
})
</script>

<style lang="scss" scoped>
.pickup-card {
  display: flex;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.pickup-card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.08);
}

.card-accent {
  width: 8rpx;
  background: linear-gradient(180deg, #3b82f6, #60a5fa);
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  padding: 28rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.location-group {
  display: flex;
  align-items: center;
  gap: 12rpx;
  width: 100%;
}

.location-item {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  flex-shrink: 0;
  margin-right: 10rpx;
}

.dot--green {
  background: #10b981;
  box-shadow: 0 0 8rpx rgba(16, 185, 129, 0.4);
}

.dot--red {
  background: #ef4444;
  box-shadow: 0 0 8rpx rgba(239, 68, 68, 0.4);
}

.location-text {
  font-size: 28rpx;
  color: #1f2937;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location-arrow {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin: 0 4rpx;
}

.arrow-line {
  width: 24rpx;
  height: 2rpx;
  background: #d1d5db;
}

.arrow-head {
  font-size: 24rpx;
  color: #d1d5db;
  line-height: 1;
  margin-left: -4rpx;
}

.card-body {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.price-group {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 26rpx;
  color: #ef4444;
  font-weight: 600;
}

.price-value {
  font-size: 44rpx;
  color: #ef4444;
  font-weight: 700;
  line-height: 1;
}

.price-decimal {
  font-size: 24rpx;
  color: #ef4444;
  font-weight: 600;
}

.time-tag {
  background: #fff7ed;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.time-tag-text {
  font-size: 24rpx;
  color: #f97316;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 20rpx;
  border-top: 1rpx solid #f3f4f6;
}

.card-footer__right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.publish-time {
  font-size: 24rpx;
  color: #9ca3af;
}

.accept-btn {
  padding: 16rpx 40rpx;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 36rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
  transition: all 0.2s ease;
}

.accept-btn:active {
  transform: scale(0.95);
}

.btn-text {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 600;
}
</style>
