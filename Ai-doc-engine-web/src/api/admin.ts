import http from './http'
import type { ApiResponse } from '@/types/auth'
import type { AdminUser, LoginLog, Dashboard, DashboardTrend, PageResult, OcrLog, ExportLog, Template, PasswordResetLog } from '@/types/admin'

export const adminApi = {
  getDashboard(): Promise<ApiResponse<Dashboard>> {
    return http.get('/admin/dashboard')
  },

  getDashboardTrend(range: string = 'week'): Promise<ApiResponse<DashboardTrend>> {
    return http.get('/admin/dashboard/trend', { params: { range } })
  },

  getUsers(page: number, size: number, keyword?: string): Promise<ApiResponse<PageResult<AdminUser>>> {
    const params: Record<string, string | number> = { page, size }
    if (keyword) params.keyword = keyword
    return http.get('/admin/users', { params })
  },

  getUserById(id: number): Promise<ApiResponse<AdminUser>> {
    return http.get(`/admin/users/${id}`)
  },

  updateUserStatus(id: number, status: number): Promise<ApiResponse<AdminUser>> {
    return http.put(`/admin/users/${id}/status`, { status })
  },

  updateUserRole(id: number, role: string): Promise<ApiResponse<AdminUser>> {
    return http.put(`/admin/users/${id}/role`, { role })
  },

  deleteUser(id: number): Promise<ApiResponse<void>> {
    return http.delete(`/admin/users/${id}`)
  },

  getLoginLogs(page: number, size: number, keyword?: string): Promise<ApiResponse<PageResult<LoginLog>>> {
    const params: Record<string, string | number> = { page, size }
    if (keyword) params.keyword = keyword
    return http.get('/admin/login-logs', { params })
  },

  // OCR日志相关
  getOcrLogs(params: {
    page: number
    size: number
    keyword?: string
    status?: string
    startTime?: string
    endTime?: string
  }): Promise<ApiResponse<PageResult<OcrLog>>> {
    return http.get('/admin/ocr/records', { params })
  },

  getOcrLogById(id: number): Promise<ApiResponse<OcrLog>> {
    return http.get(`/admin/ocr/records/${id}`)
  },

  deleteOcrLog(id: number): Promise<ApiResponse<void>> {
    return http.delete(`/admin/ocr/records/${id}`)
  },

  // 导出日志相关
  getExportLogs(params: {
    page: number
    size: number
    keyword?: string
    status?: string
    format?: string
    startTime?: string
    endTime?: string
  }): Promise<ApiResponse<PageResult<ExportLog>>> {
    return http.get('/admin/export/tasks', { params })
  },

  getExportLogById(id: number): Promise<ApiResponse<ExportLog>> {
    return http.get(`/admin/export/tasks/${id}`)
  },

  deleteExportLog(id: number): Promise<ApiResponse<void>> {
    return http.delete(`/admin/export/tasks/${id}`)
  },

  retryExport(id: number): Promise<ApiResponse<void>> {
    return http.post(`/admin/export/tasks/${id}/retry`)
  },

  // 模板管理相关
  getTemplates(params: {
    page: number
    size: number
    keyword?: string
    type?: string
    status?: number
  }): Promise<ApiResponse<PageResult<Template>>> {
    return http.get('/template/page', { params })
  },

  getTemplateById(id: number): Promise<ApiResponse<Template>> {
    return http.get(`/template/${id}`)
  },

  createTemplate(data: Partial<Template>): Promise<ApiResponse<Template>> {
    return http.post('/template', data)
  },

  updateTemplate(id: number, data: Partial<Template>): Promise<ApiResponse<Template>> {
    return http.put(`/template/${id}`, data)
  },

  deleteTemplate(id: number): Promise<ApiResponse<void>> {
    return http.delete(`/template/${id}`)
  },

  setDefaultTemplate(id: number): Promise<ApiResponse<void>> {
    return http.post(`/template/${id}/default`)
  },

  copyTemplate(id: number): Promise<ApiResponse<Template>> {
    return http.post(`/template/${id}/copy`)
  },

  // 密码重置日志相关
  getPasswordResetLogs(params: {
    page: number
    size: number
    keyword?: string
    status?: string
  }): Promise<ApiResponse<PageResult<PasswordResetLog>>> {
    return http.get('/admin/password/tokens', { params })
  },

  getPasswordResetLogById(id: number): Promise<ApiResponse<PasswordResetLog>> {
    return http.get(`/admin/password/tokens/${id}`)
  },

  deletePasswordResetLog(id: number): Promise<ApiResponse<void>> {
    return http.delete(`/admin/password/tokens/${id}`)
  },

  clearExpiredTokens(): Promise<ApiResponse<{ count: number }>> {
    return http.post('/admin/password/tokens/cleanup')
  }
}
