/**
 * 后端基础 URL
 * 开发环境: .env.development → http://localhost:8080
 * 生产环境: .env.production → /
 */
export const BASE_URL = import.meta.env.VITE_BASE_URL || 'http://localhost:8080'

/**
 * 将相对路径拼接为完整图片 URL
 */
export const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
}

/**
 * 批量转换图片路径
 */
export const getPreviewImages = (images) => {
  if (!images || !Array.isArray(images)) return []
  return images.map(img => getImageUrl(img))
}
