import request from '@/utils/request'

/**
 * 分页查询公告列表
 * @param {Object} params - { page, pageSize, type, status }
 * @returns {Promise}
 */
export const getNoticePage = (params) => {
  return request({
    url: '/admin/notice/page',
    method: 'get',
    params
  })
}

/**
 * 新增公告
 * @param {Object} data - { title, content, type, status }
 * @returns {Promise}
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
 * @param {Object} data - { id, title, content, type, status }
 * @returns {Promise}
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
 * @param {number} id - 公告ID
 * @returns {Promise}
 */
export const deleteNotice = (id) => {
  return request({
    url: '/admin/notice/delete',
    method: 'post',
    params: { id }
  })
}
