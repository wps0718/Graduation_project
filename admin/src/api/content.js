import request from '@/utils/request'

/**
 * ==================== Banner 管理接口 ====================
 */

/**
 * 获取 Banner 列表
 * @param {number} page - 页码
 * @param {number} pageSize - 每页数量
 * @param {number} status - 状态（可选）：0-停用，1-启用
 * @param {number} campusId - 校区ID（可选）
 */
export const getBannerList = (page, pageSize, status, campusId) => {
  return request({
    url: '/admin/banner/page',
    method: 'get',
    params: {
      page,
      pageSize,
      ...(status !== undefined && { status }),
      ...(campusId && { campusId })
    }
  })
}

/**
 * 新增 Banner
 * @param {object} data - Banner 数据
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
 * @param {object} data - Banner 数据（包含 id）
 */
export const updateBanner = (data) => {
  return request({
    url: '/admin/banner/update',
    method: 'post',
    data
  })
}

/**
 * 删除 Banner
 * @param {number} id - Banner ID
 */
export const deleteBanner = (id) => {
  return request({
    url: '/admin/banner/delete',
    method: 'post',
    params: { id }
  })
}

/**
 * ==================== 公告管理接口 ====================
 */

/**
 * 获取公告列表
 * @param {number} page - 页码
 * @param {number} pageSize - 每页数量
 * @param {number} type - 公告类型（可选）：1-系统公告，2-活动公告
 * @param {number} status - 显示状态（可选）：0-隐藏，1-显示
 */
export const getNoticeList = (page, pageSize, type, status) => {
  return request({
    url: '/admin/notice/page',
    method: 'get',
    params: {
      page,
      pageSize,
      ...(type !== undefined && { type }),
      ...(status !== undefined && { status })
    }
  })
}

/**
 * 新增公告
 * @param {object} data - 公告数据
 */
export const addNotice = (data) => {
  return request({
    url: '/admin/notice/add',
    method: 'post',
    data
  })
}

/**
 * 修改公告
 * @param {object} data - 公告数据（包含 id）
 */
export const updateNotice = (data) => {
  return request({
    url: '/admin/notice/update',
    method: 'post',
    data
  })
}

/**
 * 删除公告
 * @param {number} id - 公告 ID
 */
export const deleteNotice = (id) => {
  return request({
    url: '/admin/notice/delete',
    method: 'post',
    params: { id }
  })
}

/**
 * ==================== 辅助接口 ====================
 */

/**
 * 获取校区列表
 */
export const getCampusList = () => {
  return request({
    url: '/admin/campus/list',
    method: 'get'
  })
}

/**
 * 上传文件 URL 常量
 */
export const UPLOAD_URL = import.meta.env.VITE_APP_BASE_API
  ? `${import.meta.env.VITE_APP_BASE_API}/common/upload`
  : 'http://localhost:8080/common/upload'
