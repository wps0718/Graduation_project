import { BASE_URL } from './constant'
import { getToken, logout } from './auth'
import { mockData } from './mock'

const USE_MOCK = false

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

  // 未登录统一处理
  if (code === 0 && (msg === '未登录' || msg === 'Unauthorized')) {
    uni.showModal({
      title: '登录过期',
      content: '您的登录已过期，请重新登录',
      showCancel: false,
      success: () => {
        logout()
        uni.reLaunch({ url: '/pages/login-sub/login/login' })
      }
    })
    reject(response)
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

  // 过滤无效参数
  const cleanData = {}
  if (data && typeof data === 'object') {
    Object.keys(data).forEach(key => {
      const val = data[key]
      if (val !== undefined && val !== null && val !== '') {
        cleanData[key] = val
      }
    })
  }

  const token = getToken()

  // GET 请求参数拼接到 URL
  let finalUrl = BASE_URL + url
  let requestData = cleanData

  if (method === 'GET' && Object.keys(cleanData).length > 0) {
    const queryString = Object.keys(cleanData)
      .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(cleanData[key]))
      .join('&')
    finalUrl = finalUrl + '?' + queryString
    requestData = {}
  }

  // 调试日志（生产环境可删除）
  console.log('📍 [Request]', method, url, cleanData)
  console.log('Token:', token ? '✅ 已携带' : '❌ 无')

  return new Promise((resolve, reject) => {
    uni.request({
      url: finalUrl,
      method,
      data: requestData,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (showLoading) {
          uni.hideLoading()
        }
        console.log('📍 [Response]', url, res.data)
        const response = res.data || {}
        handleResponse(response, resolve, reject)
      },
      fail: (err) => {
        if (showLoading) {
          uni.hideLoading()
        }
        console.error('❌ [Request Failed]', url, err)
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

export function uploadFile(url, filePath, options = {}) {
  const mergedOptions = {
    showLoading: true,
    name: 'file',
    formData: {},
    ...options
  }
  const { showLoading, name, formData } = mergedOptions
  if (showLoading) {
    uni.showLoading({
      title: '上传中',
      mask: true
    })
  }
  const token = getToken()
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + url,
      filePath,
      name,
      formData,
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (showLoading) {
          uni.hideLoading()
        }
        let response = {}
        try {
          response = JSON.parse(res.data || '{}')
        } catch (error) {
          uni.showToast({
            title: '上传响应解析失败',
            icon: 'none'
          })
          reject(error)
          return
        }
        handleResponse(response, resolve, reject)
      },
      fail: (err) => {
        if (showLoading) {
          uni.hideLoading()
        }
        uni.showToast({
          title: '上传失败，请稍后重试',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default {
  get,
  post,
  uploadFile
}
