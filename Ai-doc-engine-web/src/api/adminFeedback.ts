import http from './http'
import type { ApiResponse } from '@/types/auth'
import type { PageResult } from '@/types/admin'
import type { Feedback, FeedbackDetail, FeedbackStats, FeedbackQueryParams, FeedbackUpdateRequest } from '@/types/feedback'

export const adminFeedbackApi = {
  getFeedbackList(params: FeedbackQueryParams): Promise<ApiResponse<PageResult<Feedback>>> {
    return http.get('/admin/feedback', { params })
  },

  getFeedbackStats(): Promise<ApiResponse<FeedbackStats>> {
    return http.get('/admin/feedback/stats')
  },

  getFeedbackDetail(id: number): Promise<ApiResponse<FeedbackDetail>> {
    return http.get(`/admin/feedback/${id}`)
  },

  updateFeedbackStatus(id: number, data: FeedbackUpdateRequest): Promise<ApiResponse<Feedback>> {
    return http.put(`/admin/feedback/${id}/status`, data)
  },

  replyFeedback(id: number, reply: string): Promise<ApiResponse<Feedback>> {
    return http.put(`/admin/feedback/${id}/reply`, { reply })
  },

  deleteFeedback(id: number): Promise<ApiResponse<void>> {
    return http.delete(`/admin/feedback/${id}`)
  }
}
