import http from './http'
import type { ApiResponse } from '@/types/auth'
import type { PageResult } from '@/types/admin'
import type { Feedback, FeedbackDetail, FeedbackSubmitRequest } from '@/types/feedback'

export const feedbackApi = {
  submitFeedback(data: FeedbackSubmitRequest, images?: File[]): Promise<ApiResponse<Feedback>> {
    const formData = new FormData()
    formData.append('request', new Blob([JSON.stringify(data)], { type: 'application/json' }))
    
    if (images && images.length > 0) {
      images.forEach((image) => {
        formData.append('images', image)
      })
    }
    
    return http.post('/feedback/submit', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  getMyFeedbacks(page: number, size: number): Promise<ApiResponse<PageResult<Feedback>>> {
    return http.get('/feedback/my', { params: { page, size } })
  },

  getFeedbackDetail(id: number): Promise<ApiResponse<FeedbackDetail>> {
    return http.get(`/feedback/${id}`)
  }
}
