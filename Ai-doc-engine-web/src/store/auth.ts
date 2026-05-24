// 认证状态管理

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'
import { authUtils } from '@/utils/auth'
import type { User, LoginRequest, RegisterRequest } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(authUtils.getCurrentUser())
  const token = ref<string | null>(authUtils.getToken())
  const isAuthenticated = ref<boolean>(authUtils.isAuthenticated())

  const applyAuth = (newToken: string, newUser: User) => {
    token.value = newToken
    user.value = newUser
    isAuthenticated.value = true
    
    authUtils.saveAuth(newToken, newUser)
  }

  // 登录
  const login = async (data: LoginRequest) => {
    const response = await authApi.login(data)
    const { token: newToken, user: newUser } = response.data
    token.value = newToken
    isAuthenticated.value = true
    authUtils.saveToken(newToken)
    
    await fetchCurrentUser()
  }

  // 校内统一登录回调完成
  const completeOAuthLogin = async (ticket: string) => {
    const response = await authApi.exchangeOAuthTicket(ticket)
    const { token: newToken } = response.data
    token.value = newToken
    isAuthenticated.value = true
    authUtils.saveToken(newToken)
    
    await fetchCurrentUser()
  }

  // 注册
  const register = async (data: RegisterRequest) => {
    const response = await authApi.register(data)
    const { token: newToken } = response.data
    token.value = newToken
    isAuthenticated.value = true
    authUtils.saveToken(newToken)
    
    await fetchCurrentUser()
  }

  // 退出登录
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
    } finally {
      token.value = null
      user.value = null
      isAuthenticated.value = false
      authUtils.clearAuth()
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async () => {
    const response = await authApi.getCurrentUser()
    user.value = response.data
    authUtils.setUser(response.data)
  }

  return {
    user,
    token,
    isAuthenticated,
    login,
    completeOAuthLogin,
    register,
    logout,
    fetchCurrentUser
  }
})
