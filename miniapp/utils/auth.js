const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export function getToken() {
  const token = uni.getStorageSync(TOKEN_KEY) || ''
  console.log('[Auth] 当前 Token:', token ? `${token.substring(0, 20)}...` : '无')
  return token
}

export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token || '')
}

export function removeToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

export function getUserInfo() {
  const value = uni.getStorageSync(USER_INFO_KEY)
  if (!value) {
    return null
  }
  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch (e) {
      return null
    }
  }
  return value
}

export function setUserInfo(info) {
  if (!info) {
    uni.removeStorageSync(USER_INFO_KEY)
    return
  }
  uni.setStorageSync(USER_INFO_KEY, info)
}

export function removeUserInfo() {
  uni.removeStorageSync(USER_INFO_KEY)
}

export function isLogin() {
  return !!getToken()
}

export function isLoggedIn() {
  return isLogin()
}

export function logout() {
  removeToken()
  removeUserInfo()
  console.log('[Auth] 已退出登录')
}

