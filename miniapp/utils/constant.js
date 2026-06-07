// ============================================
// API 基础地址配置
// 开发时修改 SERVER_DOMAIN 为你的服务器地址
// ============================================
// const SERVER_DOMAIN = 'your-server-domain.com'  // 部署时取消注释并替换为真实域名
const SERVER_DOMAIN = 'localhost:3576'  // 本地调试用

// #ifdef H5
const isDev = process.env.NODE_ENV === 'development'
// #endif
// #ifndef H5
const isDev = false
// #endif

// 部署时改为: export const BASE_URL = isDev ? 'http://localhost:3576' : `https://${SERVER_DOMAIN}`
export const BASE_URL = isDev ? 'http://localhost:3576' : `http://${SERVER_DOMAIN}`

export const CONDITION_LEVELS = [
  { value: 1, label: '全新' },
  { value: 2, label: '几乎全新(99新)' },
  { value: 3, label: '9成新' },
  { value: 4, label: '8成新' },
  { value: 5, label: '7成及以下' }
]

export const ORDER_STATUS = {
  PENDING: 1,
  PENDING_MEET: 2,
  COMPLETED: 3,
  REVIEWED: 4,
  CANCELLED: 5
}

export const ORDER_STATUS_TEXT = {
  1: '待接单',
  2: '待面交',
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

export const PICKUP_STATUS = {
  PENDING_ACCEPT: 0,
  ACCEPTED: 1,
  PRICE_CONFIRMED: 2,
  PICKING_UP: 3,
  DELIVERED: 4,
  COMPLETED: 5,
  RATED: 6,
  CANCELLED: 7,
  DISPUTE: 8
}

export const PICKUP_STATUS_TEXT = {
  0: '待接单',
  1: '已接单',
  2: '价格已确认',
  3: '代拿中',
  4: '已代拿',
  5: '已完成',
  6: '已评价',
  7: '已取消',
  8: '纠纷中'
}

export const PICKUP_STATUS_COLOR = {
  0: 'orange',
  1: 'orange',
  2: 'blue',
  3: 'blue',
  4: 'orange',
  5: 'green',
  6: 'blue',
  7: 'grey',
  8: 'red'
}

export const PICKUP_DISPUTE_STATUS = {
  PENDING_RESPONSE: 0,
  RESPONDED: 1,
  AUTO_WIN: 2,
  JUDGED: 3,
  WITHDRAWN: 4
}

export const PICKUP_DISPUTE_TYPES = [
  { value: 1, label: '未送达' },
  { value: 2, label: '物品损坏' },
  { value: 3, label: '超时未完成' },
  { value: 4, label: '价格争议' },
  { value: 5, label: '其他' }
]

export const PICKUP_SORT_OPTIONS = [
  { value: 'urgent', label: '最紧急' },
  { value: 'price', label: '报酬最高' },
  { value: 'newest', label: '最新发布' }
]

