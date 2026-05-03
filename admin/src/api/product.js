import request from '@/utils/request'

/**
 * 获取商品分页列表
 */
export const getProductPage = (params) => {
  return request({
    url: '/admin/product/page',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 */
export const getProductDetail = (productId) => {
  return request({
    url: `/admin/product/detail/${productId}`,
    method: 'get'
  })
}

/**
 * 审核通过（单个）
 */
export const approveProduct = (productId) => {
  return request({
    url: '/admin/product/approve',
    method: 'post',
    params: { productId }
  })
}

/**
 * 审核驳回
 */
export const rejectProduct = (productId, rejectReason) => {
  return request({
    url: '/admin/product/reject',
    method: 'post',
    data: { productId, rejectReason }
  })
}

/**
 * 批量审核通过
 */
export const batchApproveProduct = (ids) => {
  return request({
    url: '/admin/product/batch-approve',
    method: 'post',
    data: ids
  })
}

/**
 * 强制下架商品
 */
export const forceOffShelf = (productId, reason) => {
  return request({
    url: '/admin/product/force-off',
    method: 'post',
    data: { productId, reason }
  })
}

/**
 * 批量强制下架
 */
export const batchForceOffShelf = (ids, reason) => {
  return request({
    url: '/admin/product/batch-force-off',
    method: 'post',
    data: { ids, reason }
  })
}

/**
 * 获取商品关联订单
 */
export const getRelatedOrders = (productId, page, pageSize) => {
  return request({
    url: '/admin/product/related-orders',
    method: 'get',
    params: { productId, page, pageSize }
  })
}

/**
 * 获取商品发布者信息
 */
export const getPublisherInfo = (productId) => {
  return request({
    url: '/admin/product/publisher-info',
    method: 'get',
    params: { productId }
  })
}

/**
 * 导出商品列表
 */
export const exportProduct = (params) => {
  return request({
    url: '/admin/product/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
