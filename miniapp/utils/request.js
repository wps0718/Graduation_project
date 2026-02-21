import { BASE_URL } from './constant'
import { getToken, logout } from './auth'
import { mockData } from './mock'

const USE_MOCK = true

function getMockResponse(url, method) {
  const key = `${method.toUpperCase()} ${url}`
  if (mockData[key]) {
    return mockData[key]
  }
  return {
    code: 0,
    msg: 'Mock 数据未找到',
    data: null
  }
}

function handleResponse(response, resolve, reject) {
  const { code, msg, data } = response || {}
  if (code === 1) {
    resolve(data)
    return
  }
  if (code === 0) {
    uni.showToast({
      title: msg || '请求失败',
      icon: 'none'
    })
    reject(response)
    return
  }
  if (code === 401) {
    logout()
    uni.showToast({
      title: '登录已过期，请重新登录',
      icon: 'none'
    })
    uni.reLaunch({
      url: '/pages/login/login'
    })
    reject(response)
    return
  }
  uni.showToast({
    title: msg || '服务异常',
    icon: 'none'
  })
  reject(response)
}

function mockRequest(url, method, data, options) {
  const { showLoading } = options
  if (showLoading) {
    uni.showLoading({
      title: '加载中',
      mask: true
    })
  }
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (showLoading) {
        uni.hideLoading()
      }
      const response = getMockResponse(url, method)
      handleResponse(response, resolve, reject)
    }, 300)
  })
}

function realRequest(url, method, data, options) {
  const { showLoading } = options
  if (showLoading) {
    uni.showLoading({
      title: '加载中',
      mask: true
    })
  }
  const token = getToken()
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: {
        Authorization: token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (showLoading) {
          uni.hideLoading()
        }
        const response = res.data || {}
        handleResponse(response, resolve, reject)
      },
      fail: (err) => {
        if (showLoading) {
          uni.hideLoading()
        }
        uni.showToast({
          title: '网络异常，请稍后重试',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

function request(url, method = 'GET', data = {}, options = {}) {
  const mergedOptions = {
    showLoading: true,
    ...options
  }
  if (USE_MOCK) {
    return mockRequest(url, method, data, mergedOptions)
  }
  return realRequest(url, method, data, mergedOptions)
}

export function get(url, params = {}, options = {}) {
  return request(url, 'GET', params, options)
}

export function post(url, data = {}, options = {}) {
  return request(url, 'POST', data, options)
}

export default {
  get,
  post
}

