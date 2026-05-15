import { describe, it, expect, beforeEach } from 'vitest'
import { getToken, setToken, removeToken } from '../auth.js'

describe('auth token 管理', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('初始状态返回 null', () => {
    expect(getToken()).toBeNull()
  })

  it('setToken 后 getToken 能取到值', () => {
    setToken('test-token-123')
    expect(getToken()).toBe('test-token-123')
  })

  it('removeToken 后 getToken 返回 null', () => {
    setToken('temp')
    removeToken()
    expect(getToken()).toBeNull()
  })

  it('setToken 覆盖旧值', () => {
    setToken('old')
    setToken('new')
    expect(getToken()).toBe('new')
  })
})
