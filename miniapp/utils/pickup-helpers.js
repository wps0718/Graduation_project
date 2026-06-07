import { PICKUP_STATUS_TEXT, PICKUP_STATUS_COLOR } from './constant'

export function getPickupStatusText(status) {
  return PICKUP_STATUS_TEXT[status] || '未知'
}

export function getPickupStatusColor(status) {
  return PICKUP_STATUS_COLOR[status] || 'grey'
}

function parseDate(dateStr) {
  if (!dateStr) return null
  // 兼容 "2026-05-31T14:30:00"、"2026-05-31 14:30:00"、时间戳等格式
  let str = String(dateStr).replace('T', ' ').replace(/-/g, '/')
  const date = new Date(str)
  if (isNaN(date.getTime())) return null
  return date
}

export function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const date = parseDate(dateStr)
  if (!date) return ''
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

export function formatDeliveryTime(dateStr) {
  if (!dateStr) return ''
  const date = parseDate(dateStr)
  if (!date) return ''
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const targetDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.floor((targetDay - today) / 86400000)

  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const timeStr = `${hours}:${minutes}`

  if (diffDays === 0) return `今天 ${timeStr}`
  if (diffDays === 1) return `明天 ${timeStr}`
  if (diffDays === 2) return `后天 ${timeStr}`
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day} ${timeStr}`
}

export function getCountdown(deadlineStr) {
  if (!deadlineStr) return ''
  const deadline = parseDate(deadlineStr)
  if (!deadline) return ''
  const now = new Date()
  const diff = deadline - now
  if (diff <= 0) return '已超时'
  const hours = Math.floor(diff / 3600000)
  const minutes = Math.floor((diff % 3600000) / 60000)
  if (hours > 24) {
    const days = Math.floor(hours / 24)
    return `${days}天${hours % 24}小时`
  }
  if (hours > 0) return `${hours}小时${minutes}分钟`
  return `${minutes}分钟`
}
