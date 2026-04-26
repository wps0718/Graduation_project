import request from '@/utils/request'

/**
 * 分页查询员工列表
 * @param {Object} params - { page, pageSize, keyword }
 * @returns {Promise}
 */
export const getEmployeePage = (params) => {
  return request({
    url: '/admin/employee/page',
    method: 'get',
    params
  })
}

/**
 * 新增员工
 * @param {Object} data - { username, name, phone, role, status }
 * @returns {Promise}
 */
export const addEmployee = (data) => {
  return request({
    url: '/admin/employee/add',
    method: 'post',
    data
  })
}

/**
 * 修改员工
 * @param {Object} data - { id, username, name, phone, role, status }
 * @returns {Promise}
 */
export const updateEmployee = (data) => {
  return request({
    url: '/admin/employee/update',
    method: 'post',
    data
  })
}

/**
 * 重置密码
 * @param {number} id - 员工ID
 * @returns {Promise}
 */
export const resetEmployeePassword = (id) => {
  return request({
    url: '/admin/employee/reset-password',
    method: 'post',
    params: { id }
  })
}
