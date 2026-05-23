const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
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

export function logout() {
  removeToken()
  removeUserInfo()
}

