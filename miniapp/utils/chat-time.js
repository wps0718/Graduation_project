export function shouldShowTime(current, prev) {
  if (!prev) return true
  if (current.from !== prev.from) return true
  return current.time - prev.time > 5 * 60 * 1000
}

export function formatMessageTime(timestamp) {
  if (!timestamp) return ''
  const time = new Date(timestamp)
  const now = new Date()
  const hour = `${time.getHours()}`.padStart(2, '0')
  const minute = `${time.getMinutes()}`.padStart(2, '0')
  const hm = `${hour}:${minute}`

  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)
  const msgDay = new Date(time.getFullYear(), time.getMonth(), time.getDate())

  if (msgDay >= today) return hm
  if (msgDay >= yesterday) return `昨天 ${hm}`
  if (time.getFullYear() === now.getFullYear()) {
    return `${time.getMonth() + 1}月${time.getDate()}日 ${hm}`
  }
  return `${time.getFullYear()}年${time.getMonth() + 1}月${time.getDate()}日 ${hm}`
}

export function formatLastActive(value) {
  const time = typeof value === 'number' ? value : parseActiveTime(value)
  if (!time) return '最近在线'
  const diff = Date.now() - time
  if (diff < 2 * 60 * 1000) return '刚刚在线'
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return `最近在线 ${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `最近在线 ${hours}小时`
  const days = Math.floor(hours / 24)
  return `最近在线 ${days}天`
}

export function parseActiveTime(value) {
  if (!value) return 0
  if (typeof value === 'number') return value
  return new Date(String(value).replace('T', ' ').replace(/-/g, '/')).getTime()
}
