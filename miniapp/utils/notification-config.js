// 通知类型图标配置（emoji 过渡方案）
export const NOTIFICATION_CONFIG = {
  1: {
    title: '交易成功',
    emoji: '🤝',
    color: '#67C23A',
    bgColor: '#f0f9eb',
    statusTag: 'success'
  },
  2: {
    title: '新消息',
    emoji: '💬',
    color: '#409EFF',
    bgColor: '#ecf5ff',
    statusTag: null
  },
  3: {
    title: '商品审核通过',
    emoji: '📦',
    color: '#67C23A',
    bgColor: '#f0f9eb',
    statusTag: 'success'
  },
  4: {
    title: '商品审核驳回',
    emoji: '📦',
    color: '#F56C6C',
    bgColor: '#fef0f0',
    statusTag: 'error'
  },
  5: {
    title: '系统公告',
    emoji: '📢',
    color: '#E6A23C',
    bgColor: '#fdf6ec',
    statusTag: null
  },
  6: {
    title: '商品被收藏',
    emoji: '⭐',
    color: '#F56C6C',
    bgColor: '#fef0f0',
    statusTag: null
  },
  7: {
    title: '订单已取消',
    emoji: '🚫',
    color: '#909399',
    bgColor: '#f4f4f5',
    statusTag: 'warning'
  },
  8: {
    title: '校园认证通过',
    emoji: '🎓',
    color: '#67C23A',
    bgColor: '#f0f9eb',
    statusTag: 'success'
  },
  9: {
    title: '校园认证被驳回',
    emoji: '🎓',
    color: '#F56C6C',
    bgColor: '#fef0f0',
    statusTag: 'error'
  },
  10: {
    title: '评价提醒',
    emoji: '⭐',
    color: '#E6A23C',
    bgColor: '#fdf6ec',
    statusTag: null
  },
  11: {
    title: '新增关注',
    emoji: '👤',
    color: '#409EFF',
    bgColor: '#ecf5ff',
    statusTag: null
  }
}

// 获取通知配置（带兜底）
export function getNotificationConfig(type) {
  return NOTIFICATION_CONFIG[type] || {
    title: '系统通知',
    emoji: '📢',
    color: '#909399',
    bgColor: '#f4f4f5',
    statusTag: null
  }
}
