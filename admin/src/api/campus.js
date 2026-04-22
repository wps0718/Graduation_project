import request from '@/utils/request'

export const getCampusList = () => {
  return request({
    url: '/admin/campus/list',
    method: 'get'
  })
}
