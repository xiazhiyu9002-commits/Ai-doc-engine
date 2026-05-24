<template>
  <div v-if="announcements.length > 0" class="announcement-banner">
    <div class="banner-content">
      <div class="banner-icon">
        <svg viewBox="0 0 24 24" width="20" height="20">
          <path
            fill="currentColor"
            d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
          />
        </svg>
      </div>
      <div class="banner-text">
        <span class="banner-title">{{ currentAnnouncement?.title }}</span>
        <button class="view-btn" @click="showDetail">查看详情</button>
      </div>
      <button class="close-btn" @click="dismissBanner">&times;</button>
    </div>
    
    <div v-if="showModal" class="announcement-modal" @click="showModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ currentAnnouncement?.title }}</h3>
          <button class="close-btn" @click="showModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="announcement-meta">
            <span class="type-tag" :class="currentAnnouncement?.announcementType">
              {{ getTypeLabel(currentAnnouncement?.announcementType) }}
            </span>
            <span class="date">{{ formatDate(currentAnnouncement?.publishedAt) }}</span>
          </div>
          <div class="announcement-content">{{ currentAnnouncement?.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { announcementApi } from '@/api/announcement'
import type { Announcement, AnnouncementType } from '@/types/announcement'

const announcements = ref<Announcement[]>([])
const currentIndex = ref(0)
const showModal = ref(false)
const dismissed = ref(false)

const currentAnnouncement = computed(() => {
  return announcements.value[currentIndex.value]
})

const getTypeLabel = (type?: AnnouncementType) => {
  const labels: Record<AnnouncementType, string> = {
    notice: '通知',
    thanks: '感谢信',
    update: '更新'
  }
  return type ? labels[type] : ''
}

const formatDate = (date?: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const showDetail = () => {
  showModal.value = true
}

const dismissBanner = () => {
  dismissed.value = true
}

const loadAnnouncements = async () => {
  try {
    const res = await announcementApi.getPublishedAnnouncements()
    if (res.data) {
      announcements.value = res.data
    }
  } catch (error) {
    console.error('Failed to load announcements:', error)
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcement-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px 20px;
  position: relative;
}

.banner-content {
  display: flex;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.banner-icon {
  margin-right: 12px;
  display: flex;
  align-items: center;
}

.banner-text {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.banner-title {
  font-weight: 500;
}

.view-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.view-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  opacity: 0.8;
}

.close-btn:hover {
  opacity: 1;
}

.announcement-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.announcement-modal .modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  color: #333;
}

.announcement-modal .modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.announcement-modal .modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.announcement-modal .close-btn {
  color: #666;
}

.announcement-modal .modal-body {
  padding: 20px;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.type-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.type-tag.notice {
  background: #e6f7ff;
  color: #1890ff;
}

.type-tag.thanks {
  background: #f6ffed;
  color: #52c41a;
}

.type-tag.update {
  background: #f9f0ff;
  color: #722ed1;
}

.date {
  color: #999;
  font-size: 12px;
}

.announcement-content {
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
