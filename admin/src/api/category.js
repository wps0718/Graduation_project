import request from '@/utils/request'

export const getCategoryList = () => {
  return request({
    url: '/admin/category/list',
    method: 'get'
  })
}
