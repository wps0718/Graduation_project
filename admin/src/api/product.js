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
