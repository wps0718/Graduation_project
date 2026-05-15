import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../user.js'

describe('user store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('初始状态未登录', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.token).toBe('')
  })

  it('setToken 更新状态和 localStorage', () => {
    const store = useUserStore()
    store.setToken('abc')
    expect(store.token).toBe('abc')
    expect(localStorage.getItem('admin_token')).toBe('abc')
    expect(store.isLoggedIn).toBe(true)
  })

  it('setUserInfo 更新用户信息', () => {
    const store = useUserStore()
    store.setUserInfo({ id: 1, name: 'Admin', username: 'admin', role: 1 })
    expect(store.userInfo.name).toBe('Admin')
    expect(store.isSuperAdmin).toBe(true)
  })

  it('logout 清除所有状态', async () => {
    const store = useUserStore()
    store.setToken('abc')
    store.setUserInfo({ id: 1, name: 'Admin', username: 'admin', role: 1 })

    await store.logout()

    expect(store.token).toBe('')
    expect(store.isLoggedIn).toBe(false)
    expect(store.userInfo.id).toBeNull()
    expect(localStorage.getItem('admin_token')).toBeNull()
  })

  it('isSuperAdmin 根据 role 判断', () => {
    const store = useUserStore()
    store.setUserInfo({ id: 1, name: 'User', username: 'u', role: 2 })
    expect(store.isSuperAdmin).toBe(false)

    store.setUserInfo({ id: 1, name: 'Admin', username: 'a', role: 1 })
    expect(store.isSuperAdmin).toBe(true)
  })
})
