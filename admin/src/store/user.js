import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { adminLogin, getAdminInfo } from '@/api/auth'
import { setToken as saveToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  // ==================== State ====================
  const token = ref(localStorage.getItem('admin_token') || '')
  
  const userInfo = ref({
    id: null,
    name: '',
    username: '',
    role: null
  })

  // ==================== Getters ====================
  const isLoggedIn = computed(() => !!token.value)
  
  const isSuperAdmin = computed(() => userInfo.value.role === 1)

  // ==================== Actions ====================
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('admin_token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = { ...info }
  }

  /**
   * 退出登录
   * ⚠️ 注意：不在这里处理路由跳转，由调用方处理
   * 返回 Promise 以支持 .then() 链式调用
   */
  const logout = () => {
    return new Promise((resolve) => {
      // 清除 state
      token.value = ''
      userInfo.value = {
        id: null,
        name: '',
        username: '',
        role: null
      }
      // 清除 localStorage
      localStorage.removeItem('admin_token')
      resolve()
    })
  }

  /**
   * 登录
   * @param {Object} form - 登录表单
   * @param {string} form.username - 账号
   * @param {string} form.password - 密码
   * @returns {Promise} - 返回登录结果
   */
  const login = async (form) => {
    const res = await adminLogin(form)
    const { token: newToken } = res.data
    setToken(newToken)
    saveToken(newToken)
    const infoRes = await getAdminInfo()
    setUserInfo(infoRes.data)
    return res
  }

  const initUserInfo = async () => {
    const storedToken = localStorage.getItem('admin_token')
    if (storedToken) {
      token.value = storedToken
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isSuperAdmin,
    setToken,
    setUserInfo,
    login,
    logout,
    initUserInfo
  }
})