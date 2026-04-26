import request from '@/utils/request'

/**
 * 获取学院列表（全量，无分页）
 * @returns {Promise}
 */
export const getCollegeList = () => {
  return request({
    url: '/admin/college/list',
    method: 'get'
  })
}

/**
 * 新增学院
 * @param {Object} data - { name, sort, status }
 * @returns {Promise}
 */
export const addCollege = (data) => {
  return request({
    url: '/admin/college/add',
    method: 'post',
    data
  })
}

/**
 * 修改学院
 * @param {Object} data - { id, name, sort, status }
 * @returns {Promise}
 */
export const updateCollege = (data) => {
  return request({
    url: '/admin/college/update',
    method: 'post',
    data
  })
}

/**
 * 删除学院
 * @param {number} id - 学院ID
 * @returns {Promise}
 */
export const deleteCollege = (id) => {
  return request({
    url: '/admin/college/delete',
    method: 'post',
    params: { id }
  })
}
