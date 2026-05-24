// 认证工具函数

import { storage } from './storage'

export const authUtils = {
  // 检查是否已登录
  isAuthenticated(): boolean {
    return !!storage.getToken()
  },

  // 获取 token
  getToken(): string | null {
    return storage.getToken()
  },

  // 保存 token
  saveToken(token: string): void {
    storage.setToken(token)
  },

  // 保存认证信息
  saveAuth(token: string, user: any): void {
    storage.setToken(token)
    storage.setUser(user)
  },

  // 清除认证信息
  clearAuth(): void {
    storage.clear()
  },

  // 获取当前用户
  getCurrentUser(): any | null {
    return storage.getUser()
  },

  // 更新当前用户
  setUser(user: any): void {
    storage.setUser(user)
  }
}
