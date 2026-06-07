import request from '@/utils/request'

export const getPickupPage = (params) => {
  return request({ url: '/admin/pickup/page', method: 'get', params })
}

export const getPickupDetail = (id) => {
  return request({ url: `/admin/pickup/detail/${id}`, method: 'get' })
}

export const getDisputePage = (params) => {
  return request({ url: '/admin/pickup/dispute/page', method: 'get', params })
}

export const getDisputeDetail = (orderId) => {
  return request({ url: `/admin/pickup/dispute/detail/${orderId}`, method: 'get' })
}

export const handleDispute = (data) => {
  return request({ url: '/admin/pickup/dispute/handle', method: 'post', data })
}
