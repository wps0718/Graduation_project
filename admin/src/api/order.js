import request from '@/utils/request'

/**
 * 订单分页查询
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（默认1）
 * @param {number} params.pageSize - 每页数量（默认10）
 * @param {number} [params.status] - 订单状态（可选：1-待面交 2-预留 3-已完成 4-已评价 5-已取消）
 * @param {string} [params.keyword] - 关键词（可选，搜索订单号/买家昵称/商品标题）
 * @param {string} [params.startTime] - 开始时间（可选，格式yyyy-MM-dd）
 * @param {string} [params.endTime] - 结束时间（可选，格式yyyy-MM-dd）
 */
export const getOrderPage = (params) => {
  return request({
    url: '/admin/order/page',
    method: 'get',
    params
  })
}

/**
 * 订单详情
 * @param {number} id - 订单ID
 * @returns {Promise} - 返回订单详情
 */
export const getOrderDetail = (id) => {
  return request({
    url: `/admin/order/detail/${id}`,
    method: 'get'
  })
}
