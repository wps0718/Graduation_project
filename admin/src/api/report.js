import request from '@/utils/request'

/**
 * 举报分页查询
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（默认1）
 * @param {number} params.pageSize - 每页条数（默认10）
 * @param {number} params.targetType - 必传，1-商品举报 / 2-用户举报
 * @param {number} params.status - 可选，0-待处理 / 1-已处理 / 2-已忽略
 * @param {string} params.keyword - 可选，搜索举报人昵称
 * @returns {Promise} - 返回分页数据
 */
export const getReportPage = (params) => {
  return request({
    url: '/admin/report/page',
    method: 'get',
    params
  })
}

/**
 * 举报详情
 * @param {number} id - 举报ID
 * @returns {Promise} - 返回举报详情
 */
export const getReportDetail = (id) => {
  return request({
    url: `/admin/report/detail/${id}`,
    method: 'get'
  })
}

/**
 * 处理举报
 * @param {Object} data - 处理参数
 * @param {number} data.id - 举报ID
 * @param {number} data.status - 1-已处理 / 2-已忽略
 * @param {string} data.handleResult - 处理结果说明
 * @returns {Promise} - 返回处理结果
 */
export const handleReport = (data) => {
  return request({
    url: '/admin/report/handle',
    method: 'post',
    data
  })
}
