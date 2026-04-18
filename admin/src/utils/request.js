import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getToken, removeToken } from '@/utils/auth'

/**
 * Axios 请求封装
 * 统一处理请求拦截、响应拦截、错误处理
 */

// 创建 axios 实例
const request = axios.create({
  // 基础URL，根据实际情况修改
  baseURL: 'http://localhost:8080',
  // 请求超时时间：10秒
  timeout: 10000,
  // 请求头配置
  headers: {
    'Content-Type': 'application/json'
  }
})

// ==================== 请求拦截器 ====================

request.interceptors.request.use(
  (config) => {
    // 从 localStorage 读取 token
    const token = getToken()
    
    // 如果存在 token，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    // 请求发送失败
    console.error('请求发送失败:', error)
    return Promise.reject(error)
  }
)

// ==================== 响应拦截器 ====================

request.interceptors.response.use(
  (response) => {
    // 获取响应数据
    const res = response.data
    
    // 如果响应不是对象格式，直接返回
    if (typeof res !== 'object' || res === null) {
      return res
    }
    
    // 判断业务状态码
    if (res.code !== 1) {
      // 业务错误处理
      
      // 处理 token 过期或无效（401）
      if (res.code === 401) {
        ElMessage.error('登录已过期，请重新登录')
        
        // 清除本地 token
        const userStore = useUserStore()
        userStore.logout()
        
        return Promise.reject({ msg: '登录已过期', code: 401 })
      }
      
      // 其他业务错误，显示错误信息
      ElMessage.error(res.msg || '请求失败')
      
      // 返回错误对象，让调用方处理
      return Promise.reject({ msg: res.msg || '请求失败', code: res.code, data: res.data })
    }
    
    // 返回完整的响应对象（包含 code, msg, data）
    return res
  },
  (error) => {
    // HTTP 错误处理
    console.error('响应错误:', error)
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      const msg = error.response.data?.msg || error.message
      
      switch (status) {
        case 401:
          // 未授权，清除 token 并跳转到登录页
          ElMessage.error('登录已过期，请重新登录')
          removeToken()
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(msg || `请求失败 (${status})`)
      }
      
      return Promise.reject({ msg: msg || `HTTP ${status}`, status })
    } else if (error.request) {
      // 请求已发送但未收到响应（网络问题）
      ElMessage.error('网络异常，请检查网络连接')
      return Promise.reject({ msg: '网络异常', type: 'network' })
    } else {
      // 请求配置出错
      ElMessage.error('请求配置错误')
      return Promise.reject({ msg: error.message, type: 'config' })
    }
  }
)

export default request
