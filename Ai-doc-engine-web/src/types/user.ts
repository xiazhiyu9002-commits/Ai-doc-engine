// 用户相关类型定义

import type { ApiResponse } from './auth'

// 用户资料更新请求
export interface UpdateProfileRequest {
  email?: string
  nickname?: string
  avatarUrl?: string
  department?: string
}

// 修改密码请求
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

// 第三方账户绑定信息
export interface ThirdPartyBinding {
  id: number
  provider: string
  providerName: string
  bindAt: string
  avatar?: string
  username?: string
}

// 通知偏好设置
export interface NotificationSettings {
  emailNotification: boolean
  systemNotification: boolean
}

// 用户详细信息
export interface UserProfile {
  id: number
  username: string
  email: string
  nickname?: string
  avatarUrl?: string
  department?: string
  role?: string
  createdAt?: string
  updatedAt?: string
  bindings?: ThirdPartyBinding[]
  notificationSettings?: NotificationSettings
}

// 头像上传响应
export interface AvatarUploadResponse {
  url: string
}

// API 响应类型
export type UserProfileResponse = ApiResponse<UserProfile>
export type ThirdPartyBindingsResponse = ApiResponse<ThirdPartyBinding[]>
export type NotificationSettingsResponse = ApiResponse<NotificationSettings>
export type AvatarUploadResponseWrapper = ApiResponse<AvatarUploadResponse>
