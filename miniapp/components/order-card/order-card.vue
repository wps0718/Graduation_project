<template>
  <view class="order-card" @click="goDetail">
    <view class="order-card__main">
      <view class="order-card__image">
        <image
          :src="order.productImage"
          class="order-card__image-inner"
          mode="aspectFill"
        />
      </view>
      <view class="order-card__content">
        <view class="order-card__title-row">
          <text class="order-card__title text-ellipsis-2">
            {{ order.productTitle }}
          </text>
          <StatusTag type="order" :value="order.status" />
        </view>
        <view class="order-card__user">
          <UserAvatar
            :avatar-url="order.otherUser && order.otherUser.avatarUrl"
            :nick-name="order.otherUser && order.otherUser.nickName"
            :auth-status="2"
            size="sm"
          />
          <text class="order-card__user-name">
            {{ order.otherUser && order.otherUser.nickName }}
          </text>
          <text class="order-card__campus">{{ order.campusName }}</text>
        </view>
        <view class="order-card__bottom">
          <Price :price="order.price" size="md" />
          <text class="order-card__time">{{ order.createTime }}</text>
        </view>
      </view>
    </view>
    <view v-if="actions.length" class="order-card__actions" @click.stop="">
      <view
        v-for="item in actions"
        :key="item.key"
        class="order-card__action-btn"
        @click.stop="onActionClick(item.key)"
      >
        <text class="order-card__action-text">{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import Price from '@/components/price/price.vue'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import StatusTag from '@/components/status-tag/status-tag.vue'
import { ORDER_STATUS } from '@/utils/constant'

const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  role: {
    type: String,
    default: 'buyer'
  }
})

const emit = defineEmits(['action'])

const actions = computed(() => {
  const list = []
  const status = props.order.status
  const role = props.role
  if (status === ORDER_STATUS.PENDING) {
    if (role === 'buyer') {
      list.push(
        { key: 'contact', label: '联系卖家' },
        { key: 'cancel', label: '取消交易' },
        { key: 'confirm', label: '确认收货' }
      )
    } else if (role === 'seller') {
      list.push(
        { key: 'contact', label: '联系买家' },
        { key: 'cancel', label: '取消交易' }
      )
    }
  } else if (status === ORDER_STATUS.COMPLETED) {
    if (role === 'buyer') {
      list.push({ key: 'review', label: '去评价' })
    }
  } else if (status === ORDER_STATUS.REVIEWED) {
    if (role === 'buyer') {
      list.push(
        { key: 'viewReview', label: '查看评价' },
        { key: 'reorder', label: '再次购买' }
      )
    } else if (role === 'seller') {
      list.push({ key: 'viewReview', label: '查看评价' })
    }
  } else if (status === ORDER_STATUS.CANCELLED) {
    list.push({ key: 'delete', label: '删除订单' })
  }
  return list
})

function goDetail() {
  if (!props.order || !props.order.id) {
    return
  }
  uni.navigateTo({
    url: `/pages/order/detail/detail?id=${props.order.id}`
  })
}

function onActionClick(action) {
  emit('action', {
    action,
    order: props.order
  })
}
</script>

<style lang="scss" scoped>
.order-card {
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: var(--radius-md);
}

.order-card__main {
  display: flex;
}

.order-card__image {
  width: 200rpx;
  height: 200rpx;
  border-radius: var(--radius-md);
  overflow: hidden;
  background-color: var(--bg-grey);
  margin-right: var(--spacing-md);
}

.order-card__image-inner {
  width: 100%;
  height: 100%;
}

.order-card__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.order-card__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.order-card__title {
  flex: 1;
  font-size: var(--font-lg);
  color: var(--text-primary);
  margin-right: var(--spacing-sm);
}

.order-card__user {
  margin-top: var(--spacing-sm);
  display: flex;
  align-items: center;
}

.order-card__user-name {
  margin-left: var(--spacing-sm);
  font-size: var(--font-md);
  color: var(--text-regular);
}

.order-card__campus {
  margin-left: var(--spacing-md);
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.order-card__bottom {
  margin-top: var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-card__time {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.order-card__actions {
  margin-top: var(--spacing-md);
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.order-card__action-btn {
  padding: 10rpx 24rpx;
  border-radius: var(--radius-round);
  border-width: 1rpx;
  border-style: solid;
  border-color: var(--primary-color);
  margin-left: var(--spacing-sm);
}

.order-card__action-text {
  font-size: var(--font-sm);
  color: var(--primary-color);
}
</style>
