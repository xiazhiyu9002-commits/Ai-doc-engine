<template>
  <div class="announcement-view">
    <div class="page-header">
      <h2>公告管理</h2>
      <button class="btn-primary" @click="showCreateModal = true">发布公告</button>
    </div>
    
    <div class="filter-bar">
      <select v-model="filters.type" @change="loadAnnouncements">
        <option value="">全部类型</option>
        <option value="notice">通知</option>
        <option value="thanks">感谢信</option>
        <option value="update">更新日志</option>
      </select>
      
      <select v-model="filters.isPublished" @change="loadAnnouncements">
        <option value="">全部状态</option>
        <option value="true">已发布</option>
        <option value="false">未发布</option>
      </select>
    </div>
    
    <div class="announcement-list">
      <div v-for="item in announcements" :key="item.id" class="announcement-card">
        <div class="card-header">
          <span class="type-tag" :class="item.announcementType">
            {{ getTypeLabel(item.announcementType) }}
          </span>
          <span class="status-tag" :class="{ published: item.isPublished }">
            {{ item.isPublished ? '已发布' : '未发布' }}
          </span>
        </div>
        <h4 class="card-title">{{ item.title }}</h4>
        <p class="card-content">{{ truncate(item.content, 150) }}</p>
        <div class="card-footer">
          <span class="card-meta">
            创建者: {{ item.createdByName }} | {{ formatDate(item.createdAt) }}
          </span>
          <div class="card-actions">
            <button v-if="!item.isPublished" class="btn-publish" @click="handlePublish(item.id)">
              发布
            </button>
            <button v-else class="btn-unpublish" @click="handleUnpublish(item.id)">
              取消发布
            </button>
            <button class="btn-edit" @click="handleEdit(item)">编辑</button>
            <button class="btn-delete" @click="handleDelete(item.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="announcements.length === 0" class="empty-state">
      <p>暂无公告</p>
    </div>
    
    <div v-if="total > pageSize" class="pagination">
      <button :disabled="page === 1" @click="changePage(page - 1)">上一页</button>
      <span>{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
    </div>
    
    <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ editingItem ? '编辑公告' : '发布公告' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit" class="announcement-form">
          <div class="form-group">
            <label>公告类型</label>
            <select v-model="form.announcementType" required>
              <option value="notice">通知</option>
              <option value="thanks">感谢信</option>
              <option value="update">更新日志</option>
            </select>
          </div>
          <div class="form-group">
            <label>标题</label>
            <input v-model="form.title" type="text" maxlength="200" required />
          </div>
          <div class="form-group">
            <label>内容</label>
            <textarea v-model="form.content" rows="6" maxlength="5000" required></textarea>
          </div>
          <div class="form-group">
            <label>过期时间（可选）</label>
            <input v-model="form.expireAt" type="datetime-local" />
          </div>
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-submit" :disabled="loading">
              {{ loading ? '提交中...' : '提交' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminAnnouncementApi } from '@/api/adminAnnouncement'
import type { Announcement, AnnouncementType } from '@/types/announcement'

const announcements = ref<Announcement[]>([])
const showCreateModal = ref(false)
const editingItem = ref<Announcement | null>(null)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = ref({
  type: '',
  isPublished: ''
})

const form = ref({
  title: '',
  content: '',
  announcementType: 'notice' as AnnouncementType,
  expireAt: ''
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const getTypeLabel = (type: AnnouncementType) => {
  const labels: Record<AnnouncementType, string> = {
    notice: '通知',
    thanks: '感谢信',
    update: '更新'
  }
  return labels[type]
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN')
}

const truncate = (text: string, length: number) => {
  return text.length > length ? text.slice(0, length) + '...' : text
}

const loadAnnouncements = async () => {
  try {
    const res = await adminAnnouncementApi.getAnnouncementList(
      page.value,
      pageSize.value,
      filters.value.type as AnnouncementType || undefined,
      filters.value.isPublished === '' ? undefined : filters.value.isPublished === 'true'
    )
    if (res.data) {
      announcements.value = res.data.items
      total.value = res.data.total
    }
  } catch (error) {
    console.error('Failed to load announcements:', error)
  }
}

const changePage = (newPage: number) => {
  page.value = newPage
  loadAnnouncements()
}

const handleEdit = (item: Announcement) => {
  editingItem.value = item
  form.value = {
    title: item.title,
    content: item.content,
    announcementType: item.announcementType,
    expireAt: item.expireAt ? item.expireAt.slice(0, 16) : ''
  }
  showCreateModal.value = true
}

const closeModal = () => {
  showCreateModal.value = false
  editingItem.value = null
  form.value = {
    title: '',
    content: '',
    announcementType: 'notice',
    expireAt: ''
  }
}

const handleSubmit = async () => {
  if (loading.value) return
  loading.value = true
  
  try {
    const data = {
      title: form.value.title,
      content: form.value.content,
      announcementType: form.value.announcementType,
      expireAt: form.value.expireAt ? new Date(form.value.expireAt).toISOString() : undefined
    }
    
    if (editingItem.value) {
      await adminAnnouncementApi.updateAnnouncement(editingItem.value.id, data)
    } else {
      await adminAnnouncementApi.createAnnouncement(data)
    }
    
    closeModal()
    loadAnnouncements()
  } catch (error: any) {
    alert(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

const handlePublish = async (id: number) => {
  try {
    await adminAnnouncementApi.publishAnnouncement(id)
    loadAnnouncements()
  } catch (error: any) {
    alert(error.response?.data?.message || '发布失败')
  }
}

const handleUnpublish = async (id: number) => {
  try {
    await adminAnnouncementApi.unpublishAnnouncement(id)
    loadAnnouncements()
  } catch (error: any) {
    alert(error.response?.data?.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确定要删除此公告吗？')) return
  
  try {
    await adminAnnouncementApi.deleteAnnouncement(id)
    loadAnnouncements()
  } catch (error: any) {
    alert(error.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcement-view {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.btn-primary {
  background: #1890ff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.filter-bar select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.type-tag {
  padding: 2px 8px;
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

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #f5f5f5;
  color: #999;
}

.status-tag.published {
  background: #f6ffed;
  color: #52c41a;
}

.card-title {
  margin: 0 0 8px;
  font-size: 16px;
}

.card-content {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.card-meta {
  color: #999;
  font-size: 12px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.btn-publish,
.btn-unpublish,
.btn-edit,
.btn-delete {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
}

.btn-publish {
  background: #52c41a;
  color: white;
}

.btn-unpublish {
  background: #faad14;
  color: white;
}

.btn-edit {
  background: #1890ff;
  color: white;
}

.btn-delete {
  background: #ff4d4f;
  color: white;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-overlay {
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

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.announcement-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  padding: 10px 20px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
}

.btn-submit {
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
