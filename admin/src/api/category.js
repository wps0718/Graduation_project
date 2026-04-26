import request from '@/utils/request'

/**
 * 分页查询分类列表
 * @param {Object} params - { page, pageSize, name }
 * @returns {Promise}
 */
export const getCategoryPage = (params) => {
  return request({
    url: '/admin/category/page',
    method: 'get',
    params
  })
}

/**
 * 获取分类全量列表（下拉框用）
 * @returns {Promise}
 */
export const getCategoryList = () => {
  return request({
    url: '/admin/category/list',
    method: 'get'
  })
}

/**
 * 新增分类
 * @param {Object} data - { name, icon, sort, status }
 * @returns {Promise}
 */
export const addCategory = (data) => {
  return request({
    url: '/admin/category/add',
    method: 'post',
    data
  })
}

/**
 * 修改分类
 * @param {Object} data - { id, name, icon, sort, status }
 * @returns {Promise}
 */
export const updateCategory = (data) => {
  return request({
    url: '/admin/category/update',
    method: 'post',
    data
  })
}

/**
 * 删除分类
 * @param {number} id - 分类ID
 * @returns {Promise}
 */
export const deleteCategory = (id) => {
  return request({
    url: '/admin/category/delete',
    method: 'post',
    params: { id }
  })
}
