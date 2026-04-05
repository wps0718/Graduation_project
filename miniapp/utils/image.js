import { BASE_URL } from './constant.js'

const DEFAULT_PRODUCT_PLACEHOLDER = '/static/pic/校徽.png'

function isAbsoluteUrl(url) {
  return /^(https?:)?\/\//i.test(url)
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
