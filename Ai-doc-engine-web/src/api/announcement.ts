import http from './http'
import type { ApiResponse } from '@/types/auth'
import type { Announcement } from '@/types/announcement'

export const announcementApi = {
  getPublishedAnnouncements(): Promise<ApiResponse<Announcement[]>> {
    return http.get('/announcement/list')
  },

  getAnnouncementById(id: number): Promise<ApiResponse<Announcement>> {
    return http.get(`/announcement/${id}`)
  }
}
