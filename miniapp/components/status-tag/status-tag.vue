<template>
  <view v-if="tag.text" class="status-tag" :class="tag.colorClass">
    <text class="status-tag__text">{{ tag.text }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import {
  AUTH_STATUS,
  PRODUCT_STATUS,
  ORDER_STATUS
} from '@/utils/constant'

const props = defineProps({
  type: {
    type: String,
    default: 'order'
  },
  value: {
    type: Number,
    default: 0
  }
})

const STATUS_MAP = {
  auth: {
    [AUTH_STATUS.NONE]: { text: '未认证', color: 'grey' },
    [AUTH_STATUS.PENDING]: { text: '审核中', color: 'orange' },
    [AUTH_STATUS.VERIFIED]: { text: '已认证', color: 'green' },
    [AUTH_STATUS.REJECTED]: { text: '已驳回', color: 'red' }
  },
  product: {
    [PRODUCT_STATUS.PENDING]: { text: '待审核', color: 'orange' },
    [PRODUCT_STATUS.ON_SALE]: { text: '在售', color: 'green' },
    [PRODUCT_STATUS.OFF_SHELF]: { text: '已下架', color: 'grey' },
    [PRODUCT_STATUS.SOLD]: { text: '已售出', color: 'blue' },
    [PRODUCT_STATUS.REJECTED]: { text: '已驳回', color: 'red' }
  },
  order: {
    [ORDER_STATUS.PENDING]: { text: '待面交', color: 'orange' },
    [ORDER_STATUS.COMPLETED]: { text: '已完成', color: 'green' },
    [ORDER_STATUS.REVIEWED]: { text: '已评价', color: 'blue' },
    [ORDER_STATUS.CANCELLED]: { text: '已取消', color: 'grey' }
  },
  condition: {
    1: { text: '全新', color: 'green' },
    2: { text: '几乎全新', color: 'green' },
    3: { text: '9成新', color: 'blue' },
    4: { text: '8成新', color: 'orange' },
    5: { text: '7成新及以下', color: 'grey' }
  }
}

const tag = computed(() => {
  const typeMap = STATUS_MAP[props.type] || {}
  const info = typeMap[props.value] || {}
  const colorClass = info.color ? `status-tag--${info.color}` : 'status-tag--grey'
  return {
    text: info.text || '',
    colorClass
  }
})
</script>

<style lang="scss" scoped>
.status-tag {
  padding: 4rpx 12rpx;
  border-radius: var(--radius-round);
  font-size: var(--font-xs);
}

.status-tag__text {
  white-space: nowrap;
}

.status-tag--green {
  color: #ffffff;
  background-color: var(--success-color);
}

.status-tag--orange {
  color: #ffffff;
  background-color: var(--warning-color);
}

.status-tag--red {
  color: #ffffff;
  background-color: var(--danger-color);
}

.status-tag--blue {
  color: #ffffff;
  background-color: var(--primary-color);
}

.status-tag--grey {
  color: var(--text-secondary);
  background-color: var(--bg-grey);
}
</style>

