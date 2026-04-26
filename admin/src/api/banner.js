import request from '@/utils/request'

/**
 * 分页查询 Banner 列表
 * @param {Object} params - { page, pageSize, status, campusId }
 * @returns {Promise}
 */
export const getBannerPage = (params) => {
  return request({
    url: '/admin/banner/page',
    method: 'get',
    params
  })
}

/**
 * 新增 Banner
 * @param {Object} data - { title, image, linkType, linkUrl, campusId, sort, status, startTime, endTime }
 * @returns {Promise}
 */
export const addBanner = (data) => {
  return request({
    url: '/admin/banner/add',
    method: 'post',
    data
  })
}

/**
 * 修改 Banner
 * @param {number} id - Banner ID（query参数）
 * @param {Object} data - { title, image, linkType, linkUrl, campusId, sort, status, startTime, endTime }
 * @returns {Promise}
 */
export const updateBanner = (id, data) => {
  return request({
    url: '/admin/banner/update',
    method: 'post',
    params: { id },
    data
  })
}

/**
 * 删除 Banner
 * @param {number} id - Banner ID
 * @returns {Promise}
 */
export const deleteBanner = (id) => {
  return request({
    url: '/admin/banner/delete',
    method: 'post',
    params: { id }
  })
}

/**
 * 获取校区列表（用于下拉框）
 * @returns {Promise}
 */
export const getCampusListForBanner = () => {
  return request({
    url: '/admin/campus/list',
    method: 'get'
  })
}

/**
 * 上传图片
 * @param {FormData} formData - 包含 file 字段的 FormData
 * @returns {Promise}
 */
export const uploadBannerImage = (formData) => {
  return request.post('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
