// 认证相关接口

import http from './http'
import type {
  LoginRequest,
  RegisterRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  AuthResponse,
  User,
  ApiResponse
} from '@/types/auth'

export const authApi = {
  // 用户登录
  login(data: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    return http.post('/auth/login', data)
  },

  // 使用校内统一登录票据换取系统登录态
  exchangeOAuthTicket(ticket: string): Promise<ApiResponse<AuthResponse>> {
    return http.get('/auth/oauth/exchange', {
      params: { ticket }
    })
  },

  // 用户注册
  register(data: RegisterRequest): Promise<ApiResponse<AuthResponse>> {
    return http.post('/auth/register', data)
  },

  // 忘记密码
  forgotPassword(data: ForgotPasswordRequest): Promise<ApiResponse<void>> {
    return http.post('/auth/forgot-password', data)
  },

  // 重置密码
  resetPassword(data: ResetPasswordRequest): Promise<ApiResponse<void>> {
    return http.post('/auth/reset-password', data)
  },

  // 获取当前用户信息
  getCurrentUser(): Promise<ApiResponse<User>> {
    return http.get('/auth/me')
  },

  // 退出登录
  logout(): Promise<ApiResponse<void>> {
    return http.post('/auth/logout')
  }
}
