export type AnnouncementType = 'notice' | 'thanks' | 'update'

export interface Announcement {
  id: number
  title: string
  content: string
  announcementType: AnnouncementType
  imageUrls?: string[]
  isPublished: boolean
  publishedAt?: string
  expireAt?: string
  createdByName: string
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface AnnouncementCreateRequest {
  title: string
  content: string
  announcementType: AnnouncementType
  imageUrls?: string[]
  expireAt?: string
}

export const ANNOUNCEMENT_TYPE_LABELS: Record<AnnouncementType, string> = {
  notice: '通知',
  thanks: '感谢信',
  update: '更新日志'
}

export const ANNOUNCEMENT_TYPE_COLORS: Record<AnnouncementType, string> = {
  notice: 'blue',
  thanks: 'green',
  update: 'purple'
}
