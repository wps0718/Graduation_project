<template>
  <view class="chat-order-card">
    <view class="chat-order-card__inner">
      <text class="chat-order-card__icon">{{ icon }}</text>
      <view class="chat-order-card__body">
        <text class="chat-order-card__title">{{ title }}</text>
        <view class="chat-order-card__details">
          <text class="chat-order-card__detail">订单号：{{ item.orderNo }}</text>
          <text class="chat-order-card__detail">成交价：¥{{ item.orderPrice }}</text>
          <text v-if="item.orderMeetingPoint" class="chat-order-card__detail">面交地点：{{ item.orderMeetingPoint }}</text>
        </view>
        <text class="chat-order-card__hint">{{ hint }}</text>
        <!-- status=1 待接单：卖家确认发货，买家等待 -->
        <view v-if="item.orderStatus === 1 && item.sellerId === selfId" class="chat-order-card__action" @click="$emit('confirm-ship', item)">
          <text class="chat-order-card__action-text">确认发货</text>
        </view>
        <view v-else-if="item.orderStatus === 1 && item.buyerId === selfId" class="chat-order-card__waiting">
          <text class="chat-order-card__waiting-text">⏳ 等待卖家确认发货</text>
        </view>
        <!-- status=2 待面交：双方各自确认 -->
        <view v-else-if="item.orderStatus === 2 && item.sellerId === selfId && !item.sellerConfirmed" class="chat-order-card__action" @click="$emit('seller-confirm-receive', item)">
          <text class="chat-order-card__action-text">已交付</text>
        </view>
        <view v-else-if="item.orderStatus === 2 && item.sellerId === selfId && item.sellerConfirmed" class="chat-order-card__waiting">
          <text class="chat-order-card__waiting-text">✓ 已确认交付，等待买家确认</text>
        </view>
        <view v-else-if="item.orderStatus === 2 && item.buyerId === selfId && !item.buyerConfirmed" class="chat-order-card__action" @click="$emit('buyer-confirm-receive', item)">
          <text class="chat-order-card__action-text">完成交易</text>
        </view>
        <view v-else-if="item.orderStatus === 2 && item.buyerId === selfId && item.buyerConfirmed" class="chat-order-card__waiting">
          <text class="chat-order-card__waiting-text">✓ 已确认，等待卖家确认交付</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  item: { type: Object, required: true },
  selfId: { type: Number, default: 0 }
})

defineEmits(['confirm-ship', 'seller-confirm-receive', 'buyer-confirm-receive'])

const iconMap = { 1: '📋', 2: '📦', 3: '✅', 5: '❌' }
const titleMap = { 1: '订单已创建', 2: '待面交', 3: '交易完成', 5: '订单已取消' }

const icon = computed(() => iconMap[props.item.orderStatus] || '📋')
const title = computed(() => titleMap[props.item.orderStatus] || '订单')
const hint = computed(() => {
  const s = props.item.orderStatus
  if (s === 1) return '⏰ 72小时内面交有效，请及时联系对方'
  if (s === 2) return '📦 面交后双方各自确认，都确认即完成交易'
  if (s === 3) return '🎉 交易已完成'
  if (s === 5) return '订单已取消'
  return ''
})
</script>

<style lang="scss" scoped>
.chat-order-card {
  display: flex;
  justify-content: center;
  margin-bottom: 16rpx;
}

.chat-order-card__inner {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-sm);
  background-color: #fffbf0;
  border: 1rpx solid #ffe58f;
  border-radius: var(--radius-md);
  padding: 20rpx 24rpx;
  max-width: 540rpx;
}

.chat-order-card__icon {
  font-size: 36rpx;
  flex-shrink: 0;
}

.chat-order-card__body {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.chat-order-card__title {
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-primary);
}

.chat-order-card__details {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.chat-order-card__detail {
  font-size: var(--font-sm);
  color: var(--text-regular);
}

.chat-order-card__hint {
  font-size: 22rpx;
  color: #ff9800;
  margin-top: 4rpx;
}

.chat-order-card__action {
  display: flex;
  justify-content: center;
  margin-top: 16rpx;
  width: 100%;
}

.chat-order-card__action-text {
  width: 100%;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #fff;
  background-color: var(--primary-color);
  border-radius: 36rpx;
}

.chat-order-card__waiting {
  display: flex;
  justify-content: center;
  margin-top: 16rpx;
  width: 100%;
}

.chat-order-card__waiting-text {
  width: 100%;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #999;
  background-color: #f5f5f5;
  border-radius: 36rpx;
}
</style>
