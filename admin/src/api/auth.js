import request from '@/utils/request'

export const getAuthPage = (params) => {
  return request({
    url: '/admin/auth/page',
    method: 'get',
    params
  })
}

export const getAuthDetail = (id) => {
  return request({
    url: `/admin/auth/detail/${id}`,
    method: 'get'
  })
}

export const getAuthHistory = (authId) => {
  return request({
    url: `/admin/auth/history/${authId}`,
    method: 'get'
  })
}

export const approveAuth = (id) => {
  return request({
    url: '/admin/auth/approve',
    method: 'post',
    data: { id }
  })
}

export const rejectAuth = (id, rejectReason) => {
  return request({
    url: '/admin/auth/reject',
    method: 'post',
    data: { id, rejectReason }
  })
}

