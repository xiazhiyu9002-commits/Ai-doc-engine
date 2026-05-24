import http from './http'
import type { ApiResponse } from '@/types/auth'
import type { PageResult } from '@/types/admin'
import type { Announcement, AnnouncementCreateRequest, AnnouncementType } from '@/types/announcement'

export const adminAnnouncementApi = {
  getAnnouncementList(
    page: number,
    size: number,
    type?: AnnouncementType,
    isPublished?: boolean
  ): Promise<ApiResponse<PageResult<Announcement>>> {
    const params: Record<string, string | number | boolean> = { page, size }
    if (type) params.type = type
    if (isPublished !== undefined) params.isPublished = isPublished
    return http.get('/admin/announcement', { params })
  },

  createAnnouncement(data: AnnouncementCreateRequest): Promise<ApiResponse<Announcement>> {
    return http.post('/admin/announcement', data)
  },

  updateAnnouncement(id: number, data: AnnouncementCreateRequest): Promise<ApiResponse<Announcement>> {
    return http.put(`/admin/announcement/${id}`, data)
  },

  deleteAnnouncement(id: number): Promise<ApiResponse<void>> {
    return http.delete(`/admin/announcement/${id}`)
  },

  publishAnnouncement(id: number): Promise<ApiResponse<Announcement>> {
    return http.put(`/admin/announcement/${id}/publish`)
  },

  unpublishAnnouncement(id: number): Promise<ApiResponse<Announcement>> {
    return http.put(`/admin/announcement/${id}/unpublish`)
  }
}
