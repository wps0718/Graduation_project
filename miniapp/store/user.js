import { defineStore } from 'pinia'
import { get } from '@/utils/request'
import {
  getToken,
  setToken,
  removeToken,
  getUserInfo,
  setUserInfo,
  removeUserInfo,
  isLogin as authIsLogin,
  logout as authLogout
} from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getUserInfo(),
    isLogin: authIsLogin(),
    stats: {
      onSaleCount: 0,
      soldCount: 0,
      favoriteCount: 0
    }
  }),
  actions: {
    setToken(token) {
      this.token = token || ''
      setToken(this.token)
      this.isLogin = !!this.token
    },
    setUserInfo(info) {
      this.userInfo = info || null
      setUserInfo(this.userInfo)
    },
    async login(payload) {
      if (payload && payload.token) {
        this.setToken(payload.token)
      }
      if (payload && payload.userInfo) {
        this.setUserInfo(payload.userInfo)
      } else if (payload && payload.userId) {
        // 登录接口直接返回用户信息（LoginVO），无嵌套 userInfo
        this.setUserInfo({
          id: payload.userId,
          nickName: payload.nickName || '',
          avatarUrl: payload.avatarUrl || '',
          authStatus: payload.authStatus || 0
        })
      }
    },
    async fetchUserInfo() {
      if (!this.token) {
        return
      }
      const data = await get('/mini/user/info', {}, { showLoading: false })
      this.setUserInfo(data)
    },
    async updateStats() {
      if (!this.token) {
        return
      }
      const data = await get('/mini/user/stats', {}, { showLoading: false })
      this.stats = {
        onSaleCount: data.onSaleCount || 0,
        soldCount: data.soldCount || 0,
        favoriteCount: data.favoriteCount || 0
      }
    },
    logout() {
      authLogout()
      this.token = ''
      this.userInfo = null
      this.isLogin = false
      this.stats = {
        onSaleCount: 0,
        soldCount: 0,
        favoriteCount: 0
      }
    }
  }
})

