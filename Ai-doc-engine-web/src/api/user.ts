// 用户相关接口

import http from './http'
import type {
  UpdateProfileRequest,
  ChangePasswordRequest,
  ThirdPartyBinding,
  NotificationSettings,
  UserProfile
} from '@/types/user'
import type { ApiResponse } from '@/types/auth'

export const userApi = {
  // 获取用户详细资料
  getProfile(): Promise<ApiResponse<UserProfile>> {
    return http.get('/user/profile')
  },

  // 更新用户资料
  updateProfile(data: UpdateProfileRequest): Promise<ApiResponse<UserProfile>> {
    return http.put('/user/profile', data)
  },

  // 上传头像
  uploadAvatar(file: File): Promise<ApiResponse<{ url: string }>> {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 修改密码
  changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>> {
    return http.put('/user/password', data)
  },

  // 获取第三方账户绑定列表
  getBindings(): Promise<ApiResponse<ThirdPartyBinding[]>> {
    return http.get('/user/bindings')
  },

  // 解绑第三方账户
  unbind(provider: string): Promise<ApiResponse<void>> {
    return http.delete(`/user/bindings/${provider}`)
  },

  // 获取通知偏好设置
  getNotificationSettings(): Promise<ApiResponse<NotificationSettings>> {
    return http.get('/user/notification-settings')
  },

  // 更新通知偏好设置
  updateNotificationSettings(data: NotificationSettings): Promise<ApiResponse<NotificationSettings>> {
    return http.put('/user/notification-settings', data)
  }
}
