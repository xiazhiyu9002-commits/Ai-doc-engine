export interface AdminUser {
  id: number
  username: string
  email: string
  nickname?: string
  avatarUrl?: string
  role: string
  status: number
  department?: string
  lastLoginAt?: string
  lastLoginIp?: string
  createdAt: string
  updatedAt: string
}

export interface LoginLog {
  id: number
  userId?: number
  usernameOrEmail: string
  nickname?: string
  loginResult: string
  browserType?: string
  ipAddress: string
  userAgent?: string
  failReason?: string
  createdAt: string
}

export interface Dashboard {
  totalUsers: number
  activeUsers: number
  disabledUsers: number
  todayLogins: number
  totalLoginLogs: number
  failedLoginLogs: number
  totalTemplates: number
  totalExports: number
  ocrFailedCount: number
  totalFeedbacks: number
  totalAnnouncements: number
  userGrowthRate: number
  exportGrowthRate: number
  ocrGrowthRate: number
  feedbackGrowthRate: number
}

export interface DashboardTrend {
  dates: string[]
  newUsers: number[]
  activeUsers: number[]
  exports: number[]
  ocrCount: number[]
  feedbacks: number[]
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
}

// OCR日志
export interface OcrLog {
  id: number
  userId: number
  username: string
  nickname?: string
  inputContent: string
  outputResult?: string
  status: string // RECOGNIZED, FAILED, PROCESSING
  processTime?: number // 毫秒
  createdAt: string
}

// 导出日志
export interface ExportLog {
  id: number
  userId: number
  username: string
  nickname?: string
  templateId?: number
  templateName?: string
  fileName?: string
  fileSize?: number
  charCount?: number
  status: string
  duration?: number
  errorMessage?: string
  createdAt: string
}

// 文档模板
export interface Template {
  id: number
  name: string
  type: string // contract, report, invoice, letter, other
  creatorId: number
  creatorName: string
  usageCount: number
  status: number // 1=启用, 0=禁用
  isDefault: number // 1=默认, 0=非默认
  content?: string
  createdAt: string
  updatedAt: string
}

// 密码重置日志
export interface PasswordResetLog {
  id: number
  userId: number
  username: string
  nickname?: string
  token: string
  status: string // pending, used, expired
  expireAt: string
  usedAt?: string
  createdAt: string
}
