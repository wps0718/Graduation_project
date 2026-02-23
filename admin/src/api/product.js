import request from '@/utils/request'

export const rejectProduct = data => {
  return request({
    url: '/admin/product/reject',
    method: 'post',
    data
  })
}
