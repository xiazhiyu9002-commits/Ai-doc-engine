import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminApi } from '@/api/admin'
import type { AdminUser, LoginLog, Dashboard, DashboardTrend, PageResult, OcrLog, ExportLog, Template, PasswordResetLog } from '@/types/admin'

export const useAdminStore = defineStore('admin', () => {
  const dashboard = ref<Dashboard | null>(null)
  const trend = ref<DashboardTrend | null>(null)
  const users = ref<PageResult<AdminUser> | null>(null)
  const loginLogs = ref<PageResult<LoginLog> | null>(null)
  const ocrLogs = ref<PageResult<OcrLog> | null>(null)
  const exportLogs = ref<PageResult<ExportLog> | null>(null)
  const templates = ref<PageResult<Template> | null>(null)
  const passwordResetLogs = ref<PageResult<PasswordResetLog> | null>(null)
  const loading = ref(false)

  const fetchDashboard = async () => {
    loading.value = true
    try {
      const res = await adminApi.getDashboard()
      dashboard.value = res.data
    } finally {
      loading.value = false
    }
  }

  const fetchTrend = async (range: string = 'week') => {
    try {
      const res = await adminApi.getDashboardTrend(range)
      trend.value = res.data
    } catch (error) {
      console.error('Failed to fetch trend data:', error)
      throw error
    }
  }

  const fetchUsers = async (page: number, size: number, keyword?: string) => {
    loading.value = true
    try {
      const res = await adminApi.getUsers(page, size, keyword)
      users.value = res.data
    } finally {
      loading.value = false
    }
  }

  const updateUserStatus = async (id: number, status: number) => {
    const res = await adminApi.updateUserStatus(id, status)
    return res.data
  }

  const updateUserRole = async (id: number, role: string) => {
    const res = await adminApi.updateUserRole(id, role)
    return res.data
  }

  const deleteUser = async (id: number) => {
    await adminApi.deleteUser(id)
  }

  const fetchLoginLogs = async (page: number, size: number, keyword?: string) => {
    loading.value = true
    try {
      const res = await adminApi.getLoginLogs(page, size, keyword)
      loginLogs.value = res.data
    } finally {
      loading.value = false
    }
  }

  // OCR日志相关
  const fetchOcrLogs = async (params: {
    page: number
    size: number
    keyword?: string
    status?: string
    startTime?: string
    endTime?: string
  }) => {
    loading.value = true
    try {
      const res = await adminApi.getOcrLogs(params)
      ocrLogs.value = res.data
    } finally {
      loading.value = false
    }
  }

  const deleteOcrLog = async (id: number) => {
    await adminApi.deleteOcrLog(id)
  }

  // 导出日志相关
  const fetchExportLogs = async (params: {
    page: number
    size: number
    keyword?: string
    status?: string
    format?: string
    startTime?: string
    endTime?: string
  }) => {
    loading.value = true
    try {
      const res = await adminApi.getExportLogs(params)
      exportLogs.value = res.data
    } finally {
      loading.value = false
    }
  }

  const deleteExportLog = async (id: number) => {
    await adminApi.deleteExportLog(id)
  }

  const retryExport = async (id: number) => {
    await adminApi.retryExport(id)
  }

  // 模板管理相关
  const fetchTemplates = async (params: {
    page: number
    size: number
    keyword?: string
    type?: string
    status?: number
  }) => {
    loading.value = true
    try {
      const res = await adminApi.getTemplates(params)
      templates.value = res.data
    } finally {
      loading.value = false
    }
  }

  const createTemplate = async (data: Partial<Template>) => {
    const res = await adminApi.createTemplate(data)
    return res.data
  }

  const updateTemplate = async (id: number, data: Partial<Template>) => {
    const res = await adminApi.updateTemplate(id, data)
    return res.data
  }

  const deleteTemplate = async (id: number) => {
    await adminApi.deleteTemplate(id)
  }

  const setDefaultTemplate = async (id: number) => {
    await adminApi.setDefaultTemplate(id)
  }

  const copyTemplate = async (id: number) => {
    const res = await adminApi.copyTemplate(id)
    return res.data
  }

  // 密码重置日志相关
  const fetchPasswordResetLogs = async (params: {
    page: number
    size: number
    keyword?: string
    status?: string
  }) => {
    loading.value = true
    try {
      const res = await adminApi.getPasswordResetLogs(params)
      passwordResetLogs.value = res.data
    } finally {
      loading.value = false
    }
  }

  const deletePasswordResetLog = async (id: number) => {
    await adminApi.deletePasswordResetLog(id)
  }

  const clearExpiredTokens = async () => {
    const res = await adminApi.clearExpiredTokens()
    return res.data
  }

  return {
    dashboard,
    trend,
    users,
    loginLogs,
    ocrLogs,
    exportLogs,
    templates,
    passwordResetLogs,
    loading,
    fetchDashboard,
    fetchTrend,
    fetchUsers,
    updateUserStatus,
    updateUserRole,
    deleteUser,
    fetchLoginLogs,
    fetchOcrLogs,
    deleteOcrLog,
    fetchExportLogs,
    deleteExportLog,
    retryExport,
    fetchTemplates,
    createTemplate,
    updateTemplate,
    deleteTemplate,
    setDefaultTemplate,
    copyTemplate,
    fetchPasswordResetLogs,
    deletePasswordResetLog,
    clearExpiredTokens
  }
})
