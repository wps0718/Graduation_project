import request from '@/utils/request'

/**
 * 获取用户分页列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（默认1）
 * @param {number} params.pageSize - 每页条数（默认10）
 * @param {string} params.keyword - 搜索关键词（昵称/手机号）
 * @param {number} params.status - 账号状态（可选：0-封禁 1-正常 2-注销中）
 * @param {number} params.authStatus - 认证状态（可选：0-未认证 1-审核中 2-已认证 3-已驳回）
 * @param {number} params.campusId - 校区ID（可选）
 * @returns {Promise} - 返回分页数据
 */
export const getUserPage = (params) => {
  return request({
    url: '/admin/user/page',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 * @param {number} id - 用户ID
 * @returns {Promise} - 返回用户详情
 */
export const getUserDetail = (id) => {
  return request({
    url: `/admin/user/detail/${id}`,
    method: 'get'
  })
}

/**
 * 封禁用户
 * @param {Object} data - 封禁参数
 * @param {number} data.userId - 用户ID
 * @param {string} data.banReason - 封禁原因
 * @returns {Promise} - 返回封禁结果
 */
export const banUser = (data) => {
  return request({
    url: '/admin/user/ban',
    method: 'post',
    data
  })
}

/**
 * 解封用户
 * @param {Object} data - 解封参数
 * @param {number} data.userId - 用户ID
 * @returns {Promise} - 返回解封结果
 */
export const unbanUser = (data) => {
  return request({
    url: '/admin/user/unban',
    method: 'post',
    data
  })
}

/**
 * 获取校区列表（用于筛选下拉）
 * @returns {Promise} - 返回校区列表
 */
export const getCampusList = () => {
  return request({
    url: '/admin/campus/list',
    method: 'get'
  })
}
