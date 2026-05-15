import { describe, it, expect, vi } from 'vitest'

vi.stubEnv('VITE_BASE_URL', 'http://localhost:8080')

describe('getImageUrl', async () => {
  const { getImageUrl } = await import('../baseUrl.js')

  it('返回空字符串当 path 为空', () => {
    expect(getImageUrl('')).toBe('')
    expect(getImageUrl(null)).toBe('')
    expect(getImageUrl(undefined)).toBe('')
  })

  it('原样返回 http 开头的完整 URL', () => {
    expect(getImageUrl('https://example.com/a.png')).toBe('https://example.com/a.png')
  })

  it('拼接相对路径（不带 /）', () => {
    const result = getImageUrl('uploads/a.png')
    expect(result).toBe('http://localhost:8080/uploads/a.png')
  })

  it('拼接相对路径（带 /）', () => {
    const result = getImageUrl('/uploads/a.png')
    expect(result).toBe('http://localhost:8080/uploads/a.png')
  })
})

describe('getPreviewImages', async () => {
  const { getPreviewImages } = await import('../baseUrl.js')

  it('返回空数组当输入为空', () => {
    expect(getPreviewImages(null)).toEqual([])
    expect(getPreviewImages(undefined)).toEqual([])
    expect(getPreviewImages('not-array')).toEqual([])
  })

  it('批量转换图片路径', () => {
    const result = getPreviewImages(['/a.png', 'https://x.com/b.png'])
    expect(result).toHaveLength(2)
    expect(result[0]).toContain('/a.png')
    expect(result[1]).toBe('https://x.com/b.png')
  })
})
