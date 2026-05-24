export type FeedbackType = 'suggestion' | 'bug' | 'feature' | 'other'
export type FeedbackStatus = 'pending' | 'processing' | 'resolved' | 'closed'
export type FeedbackPriority = 'low' | 'normal' | 'high' | 'urgent'

export interface Feedback {
  id: number
  userId: number
  username: string
  userEmail: string
  content: string
  feedbackType: FeedbackType
  status: FeedbackStatus
  priority: FeedbackPriority
  imageUrls?: string[]
  processedByName?: string
  createdAt: string
  updatedAt: string
}

export interface FeedbackDetail extends Feedback {
  adminReply?: string
  processedAt?: string
  operationLogs?: FeedbackOperationLog[]
}

export interface FeedbackOperationLog {
  id: number
  operationType: string
  oldValue?: string
  newValue?: string
  operatorName: string
  createdAt: string
}

export interface FeedbackStats {
  totalFeedbacks: number
  pendingFeedbacks: number
  processingFeedbacks: number
  resolvedFeedbacks: number
  closedFeedbacks: number
  todayFeedbacks: number
  weekFeedbacks: number
  monthFeedbacks: number
  bugCount: number
  suggestionCount: number
  featureCount: number
  highPriorityCount: number
}

export interface FeedbackSubmitRequest {
  content: string
  feedbackType: FeedbackType
}

export interface FeedbackQueryParams {
  page?: number
  size?: number
  status?: FeedbackStatus
  feedbackType?: FeedbackType
  priority?: FeedbackPriority
  keyword?: string
  userId?: number
}

export interface FeedbackUpdateRequest {
  status?: FeedbackStatus
  priority?: FeedbackPriority
  adminReply?: string
}

export const FEEDBACK_TYPE_LABELS: Record<FeedbackType, string> = {
  suggestion: '建议',
  bug: '问题反馈',
  feature: '功能请求',
  other: '其他'
}

export const FEEDBACK_STATUS_LABELS: Record<FeedbackStatus, string> = {
  pending: '待处理',
  processing: '处理中',
  resolved: '已解决',
  closed: '已关闭'
}

export const FEEDBACK_PRIORITY_LABELS: Record<FeedbackPriority, string> = {
  low: '低',
  normal: '普通',
  high: '高',
  urgent: '紧急'
}

export const FEEDBACK_STATUS_COLORS: Record<FeedbackStatus, string> = {
  pending: 'yellow',
  processing: 'blue',
  resolved: 'green',
  closed: 'gray'
}

export const FEEDBACK_PRIORITY_COLORS: Record<FeedbackPriority, string> = {
  low: 'gray',
  normal: 'blue',
  high: 'orange',
  urgent: 'red'
}
