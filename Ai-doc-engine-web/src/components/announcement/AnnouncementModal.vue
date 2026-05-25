<template>
  <el-dialog
    v-model="visible"
    title="公告通知"
    width="520px"
    class="announcement-dialog"
    :close-on-click-modal="true"
    @close="handleClose"
  >
    <div class="announcement-list" v-loading="loading">
      <div v-if="announcements.length === 0 && !loading" class="empty-state">
        <svg class="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M18 8C18 6.4087 17.3679 4.88258 16.2426 3.75736C15.1174 2.63214 13.5913 2 12 2C10.4087 2 8.88258 2.63214 7.75736 3.75736C6.63214 4.88258 6 6.4087 6 8C6 15 3 17 3 17H21C21 17 18 15 18 8Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M13.73 21C13.5542 21.3031 13.3019 21.5547 12.9982 21.7295C12.6946 21.9044 12.3504 21.9965 12 21.9965C11.6496 21.9965 11.3054 21.9044 11.0018 21.7295C10.6982 21.5547 10.4458 21.3031 10.27 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p class="empty-text">暂无公告</p>
      </div>

      <div
        v-for="announcement in announcements"
        :key="announcement.id"
        class="announcement-item"
        :class="{ unread: !isRead(announcement.id) }"
        @click="showDetail(announcement)"
      >
        <div class="item-header">
          <span class="item-type" :class="announcement.announcementType">
            {{ getTypeLabel(announcement.announcementType) }}
          </span>
          <span v-if="!isRead(announcement.id)" class="unread-dot"></span>
        </div>
        <div class="item-title">{{ announcement.title }}</div>
        <div class="item-meta">
          <span class="item-date">{{ formatDate(announcement.publishedAt) }}</span>
        </div>
      </div>
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="currentAnnouncement?.title"
      width="500px"
      class="announcement-detail-dialog"
      append-to-body
    >
      <div class="detail-content" v-if="currentAnnouncement">
        <div class="detail-meta">
          <span class="detail-type" :class="currentAnnouncement.announcementType">
            {{ getTypeLabel(currentAnnouncement.announcementType) }}
          </span>
          <span class="detail-date">{{ formatDate(currentAnnouncement.publishedAt) }}</span>
          <span class="detail-author">{{ currentAnnouncement.createdByName }}</span>
        </div>
        <div class="detail-body">
          <p class="detail-text">{{ currentAnnouncement.content }}</p>
        </div>
        <div v-if="currentAnnouncement.imageUrls && currentAnnouncement.imageUrls.length > 0" class="detail-images">
          <img
            v-for="(url, index) in currentAnnouncement.imageUrls"
            :key="index"
            :src="url"
            alt="公告图片"
            class="detail-image"
          />
        </div>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { announcementApi } from '@/api/announcement'
import type { Announcement, AnnouncementType } from '@/types/announcement'
import { ANNOUNCEMENT_TYPE_LABELS } from '@/types/announcement'

const STORAGE_KEY = 'ai-doc-read-announcements'

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'update:unreadCount', count: number): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const announcements = ref<Announcement[]>([])
const showDetailDialog = ref(false)
const currentAnnouncement = ref<Announcement | null>(null)
const readIds = ref<Set<number>>(new Set())

// 从 localStorage 加载已读公告ID
const loadReadIds = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const ids = JSON.parse(stored) as number[]
      readIds.value = new Set(ids)
    }
  } catch (error) {
    console.error('Failed to load read announcement ids:', error)
  }
}

// 保存已读公告ID到 localStorage
const saveReadIds = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify([...readIds.value]))
  } catch (error) {
    console.error('Failed to save read announcement ids:', error)
  }
}

// 检查公告是否已读
const isRead = (id: number): boolean => {
  return readIds.value.has(id)
}

// 标记公告为已读
const markAsRead = (id: number) => {
  readIds.value.add(id)
  saveReadIds()
  updateUnreadCount()
}

// 计算未读数量
const updateUnreadCount = () => {
  const count = announcements.value.filter(a => !isRead(a.id)).length
  emit('update:unreadCount', count)
}

// 获取公告类型标签
const getTypeLabel = (type: AnnouncementType): string => {
  return ANNOUNCEMENT_TYPE_LABELS[type] || type
}

// 格式化日期
const formatDate = (date?: string): string => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 显示公告详情
const showDetail = (announcement: Announcement) => {
  currentAnnouncement.value = announcement
  markAsRead(announcement.id)
  showDetailDialog.value = true
}

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await announcementApi.getPublishedAnnouncements()
    if (res.data) {
      announcements.value = res.data
      updateUnreadCount()
    }
  } catch (error) {
    console.error('Failed to load announcements:', error)
  } finally {
    loading.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false)
}

// 监听弹窗打开
watch(visible, (newVal) => {
  if (newVal) {
    loadAnnouncements()
  }
})

// 组件挂载时加载已读ID
onMounted(() => {
  loadReadIds()
})
</script>

<style scoped>
.announcement-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}

.announcement-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.announcement-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 60vh;
  overflow-y: auto;
}

.announcement-list {
  min-height: 200px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.empty-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  color: #d1d5db;
}

.empty-text {
  font-size: 14px;
  margin: 0;
}

.announcement-item {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background-color 0.2s ease;
  position: relative;
}

.announcement-item:hover {
  background-color: #f9fafb;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item.unread {
  background-color: #fefefe;
}

.announcement-item.unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.item-type {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.item-type.notice {
  background-color: #eff6ff;
  color: #3b82f6;
}

.item-type.thanks {
  background-color: #ecfdf5;
  color: #10b981;
}

.item-type.update {
  background-color: #f5f3ff;
  color: #8b5cf6;
}

.unread-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #ef4444;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 6px;
  line-height: 1.4;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-date {
  font-size: 12px;
  color: #9ca3af;
}

/* 详情弹窗样式 */
.announcement-detail-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}

.announcement-detail-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.announcement-detail-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.detail-type {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.detail-type.notice {
  background-color: #eff6ff;
  color: #3b82f6;
}

.detail-type.thanks {
  background-color: #ecfdf5;
  color: #10b981;
}

.detail-type.update {
  background-color: #f5f3ff;
  color: #8b5cf6;
}

.detail-date {
  font-size: 12px;
  color: #9ca3af;
}

.detail-author {
  font-size: 12px;
  color: #6b7280;
}

.detail-body {
  margin-bottom: 16px;
}

.detail-text {
  font-size: 14px;
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 0;
}

.detail-images {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-image {
  max-width: 100%;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}
</style>
