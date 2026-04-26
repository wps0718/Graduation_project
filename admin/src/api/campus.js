import request from '@/utils/request'

/**
 * 获取校区列表（全量，无分页）
 * @returns {Promise}
 */
export const getCampusList = () => {
  return request({
    url: '/admin/campus/list',
    method: 'get'
  })
}

/**
 * 新增校区
 * @param {Object} data - { name, code, sort, status }
 * @returns {Promise}
 */
export const addCampus = (data) => {
  return request({
    url: '/admin/campus/add',
    method: 'post',
    data
  })
}

/**
 * 修改校区
 * @param {Object} data - { id, name, code, sort, status }
 * @returns {Promise}
 */
export const updateCampus = (data) => {
  return request({
    url: '/admin/campus/update',
    method: 'post',
    data
  })
}

/**
 * 获取某校区的面交地点列表
 * @param {number} campusId - 校区ID
 * @returns {Promise}
 */
export const getMeetingPointList = (campusId) => {
  return request({
    url: `/admin/campus/meeting-point/list/${campusId}`,
    method: 'get'
  })
}

/**
 * 新增面交地点
 * @param {Object} data - { campusId, name, description, sort, status }
 * @returns {Promise}
 */
export const addMeetingPoint = (data) => {
  return request({
    url: '/admin/campus/meeting-point/add',
    method: 'post',
    data
  })
}

/**
 * 修改面交地点
 * @param {Object} data - { id, campusId, name, description, sort, status }
 * @returns {Promise}
 */
export const updateMeetingPoint = (data) => {
  return request({
    url: '/admin/campus/meeting-point/update',
    method: 'post',
    data
  })
}

/**
 * 删除面交地点
 * @param {number} id - 面交地点ID
 * @returns {Promise}
 */
export const deleteMeetingPoint = (id) => {
  return request({
    url: '/admin/campus/meeting-point/delete',
    method: 'post',
    params: { id }
  })
}
