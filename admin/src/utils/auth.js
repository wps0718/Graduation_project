/**
 * Token 管理工具函数
 * 提供 localStorage 中 token 的存取方法
 */

const TOKEN_KEY = 'admin_token'

/**
 * 获取 Token
 * @returns {string|null} - 返回 token 或 null
 */
export const getToken = () => {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置 Token
 * @param {string} token - 要保存的 token
 */
export const setToken = (token) => {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除 Token
 */
export const removeToken = () => {
  localStorage.removeItem(TOKEN_KEY)
}
