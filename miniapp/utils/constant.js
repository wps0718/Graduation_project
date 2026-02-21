export const BASE_URL = 'http://localhost:8080'

export const CONDITION_LEVELS = [
  { value: 1, label: '全新' },
  { value: 2, label: '几乎全新(99新)' },
  { value: 3, label: '9成新' },
  { value: 4, label: '8成新' },
  { value: 5, label: '7成及以下' }
]

export const ORDER_STATUS = {
  PENDING: 1,
  COMPLETED: 3,
  REVIEWED: 4,
  CANCELLED: 5
}

export const ORDER_STATUS_TEXT = {
  1: '待面交',
  3: '已完成',
  4: '已评价',
  5: '已取消'
}

export const AUTH_STATUS = {
  NONE: 0,
  PENDING: 1,
  VERIFIED: 2,
  REJECTED: 3
}

export const AUTH_STATUS_TEXT = {
  0: '未认证',
  1: '审核中',
  2: '已认证',
  3: '已驳回'
}

export const PRODUCT_STATUS = {
  PENDING: 0,
  ON_SALE: 1,
  OFF_SHELF: 2,
  SOLD: 3,
  REJECTED: 4
}

export const PRODUCT_STATUS_TEXT = {
  0: '待审核',
  1: '在售中',
  2: '已下架',
  3: '已售出',
  4: '已驳回'
}

export const REPORT_REASONS = [
  { value: 1, label: '虚假商品' },
  { value: 2, label: '违禁物品' },
  { value: 3, label: '价格异常' },
  { value: 4, label: '骚扰信息' },
  { value: 5, label: '其他' }
]

export const QUICK_REPLIES = [
  '还在吗？',
  '可以小刀吗？',
  '什么时候方便？'
]

