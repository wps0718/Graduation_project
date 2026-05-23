import { BASE_URL } from './constant.js'

const DEFAULT_PRODUCT_PLACEHOLDER = '/static/pic/校徽.png'

function isAbsoluteUrl(url) {
  return /^(https?:)?\/\//i.test(url)
}

// 从绝对 URL 中提取路径部分（兼容数据库中已存储的 localhost 绝对路径）
function extractPath(url) {
  try {
    // 处理 http://localhost:8080/uploads/... 这种情况
    if (url.includes('localhost:') || url.includes('127.0.0.1:')) {
      const idx = url.indexOf('/uploads/')
      if (idx >= 0) return url.substring(idx)
    }
  } catch (e) {
    // ignore
  }
  return null
}

function joinBaseUrl(path) {
  const base = String(BASE_URL || '').replace(/\/+$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${base}${normalizedPath}`
}

function appendVersion(url, version) {
  if (!version) return url
  const connector = url.includes('?') ? '&' : '?'
  return `${url}${connector}v=${encodeURIComponent(version)}`
}

export function resolveImageUrl(url, options = {}) {
  const { fallback = '', version = '' } = options
  if (!url) {
    return fallback
  }
  const raw = String(url).trim()
  if (!raw) {
    return fallback
  }
  if (raw.startsWith('data:') || raw.startsWith('wxfile://')) {
    return raw
  }
  if (raw.startsWith('/static/')) {
    return raw
  }
  if (isAbsoluteUrl(raw)) {
    // 兼容数据库中已存储的 localhost 绝对路径，转换为当前 BASE_URL
    const path = extractPath(raw)
    if (path) {
      return appendVersion(joinBaseUrl(path), version)
    }
    return appendVersion(raw, version)
  }
  return appendVersion(joinBaseUrl(raw), version)
}

export function normalizeProductCardData(product, options = {}) {
  const { fallback = DEFAULT_PRODUCT_PLACEHOLDER, version = '' } = options
  const next = { ...(product || {}) }
  const sourceImages = Array.isArray(next.images) ? next.images : []
  const images = sourceImages
    .map((item) => resolveImageUrl(item, { version }))
    .filter(Boolean)

  const coverCandidate = next.coverImage || images[0] || ''
  const coverImage = resolveImageUrl(coverCandidate, { fallback, version })

  next.coverImage = coverImage
  next.images = images.length > 0 ? images : (coverImage ? [coverImage] : [])
  return next
}

export default {
  resolveImageUrl,
  normalizeProductCardData
}
