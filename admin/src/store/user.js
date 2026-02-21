import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    name: '',
    avatar: '',
    roles: []
  }),
  actions: {
    // login
    login(userInfo) {
      const { username, password } = userInfo
      return new Promise((resolve, reject) => {
        // login api call here
        setToken('admin-token') // mock token
        this.token = 'admin-token'
        resolve()
      })
    },

    // get user info
    getInfo() {
      return new Promise((resolve, reject) => {
        // get info api call here
        this.name = 'Admin'
        this.avatar = ''
        resolve({ name: 'Admin', roles: ['admin'] })
      })
    },

    // user logout
    logout() {
      return new Promise((resolve, reject) => {
        // logout api call here
        this.token = ''
        this.roles = []
        removeToken()
        resolve()
      })
    }
  }
})
