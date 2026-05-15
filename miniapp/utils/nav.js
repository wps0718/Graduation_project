import { useUserStore } from '@/store/user'

const LOGIN_URL = '/pages/login-sub/login/login'
const HOME_TAB = '/pages/index/index'

/**
 * 返回上一页，无历史记录时跳转到指定 tab
 * @param {string} fallbackTab - 无历史时跳转的 tab 页路径
 */
export function goBack(fallbackTab) {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: fallbackTab || HOME_TAB })
    return
  }
  uni.navigateBack()
}

/**
 * 检查登录状态，未登录则跳转登录页
 * @returns {boolean} 是否已登录
 */
export function ensureLogin() {
  const userStore = useUserStore()
  if (!userStore.isLogin) {
    uni.navigateTo({ url: LOGIN_URL })
    return false
  }
  return true
}

/**
 * 快捷 toast
 */
export function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}
