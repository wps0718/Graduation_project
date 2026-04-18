import request from '@/utils/request'

/**
 * 管理员登录
 * @param {Object} data - 登录参数
 * @param {string} data.username - 账号
 * @param {string} data.password - 密码
 * @returns {Promise} - 返回登录结果
 */
export const adminLogin = (data) => {
  return request({
    url: '/admin/employee/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前登录管理员信息
 * @returns {Promise} - 返回管理员信息
 */
export const getAdminInfo = () => {
  return request({
    url: '/admin/employee/info',
    method: 'get'
  })
}

/**
 * 管理员退出登录
 * @returns {Promise} - 返回退出结果
 */
export const adminLogout = () => {
  return request({
    url: '/admin/employee/logout',
    method: 'post'
  })
}

// ==================== 认证审核接口 ====================

/**
 * 分页查询认证列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（默认1）
 * @param {number} params.size - 每页条数（默认10）
 * @param {number} params.status - 状态（可选：0-待审核 1-已通过 2-已驳回）
 * @param {number} params.collegeId - 学院ID（可选）
 * @returns {Promise} - 返回分页数据
 */
export const getAuthPage = (params) => {
  return request({
    url: '/admin/auth/page',
    method: 'get',
    params
  })
}

/**
 * 获取认证详情
 * @param {number} id - 认证申请ID
 * @returns {Promise} - 返回认证详情
 */
export const getAuthDetail = (id) => {
  return request({
    url: `/admin/auth/detail/${id}`,
    method: 'get'
  })
}

/**
 * 获取认证历史记录
 * @param {number} authId - 认证申请ID
 * @returns {Promise} - 返回历史记录列表
 */
export const getAuthHistory = (authId) => {
  return request({
    url: `/admin/auth/history/${authId}`,
    method: 'get'
  })
}

/**
 * 审核通过
 * @param {number} id - 认证申请ID
 * @returns {Promise} - 返回审核结果
 */
export const approveAuth = (id) => {
  return request({
    url: '/admin/auth/approve',
    method: 'post',
    data: { id }
  })
}

/**
 * 审核驳回
 * @param {number} id - 认证申请ID
 * @param {string} rejectReason - 驳回原因
 * @returns {Promise} - 返回审核结果
 */
export const rejectAuth = (id, rejectReason) => {
  return request({
    url: '/admin/auth/reject',
    method: 'post',
    data: { id, rejectReason }
  })
}
