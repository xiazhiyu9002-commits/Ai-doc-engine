<template>
  <div class="feedback-detail">
    <div class="detail-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        返回列表
      </button>
      <h2>反馈详情</h2>
    </div>

    <div class="detail-content" v-loading="loading">
      <template v-if="feedback">
        <!-- 基本信息卡片 -->
        <div class="info-card">
          <div class="info-header">
            <div class="info-meta">
              <span class="id-badge">#{{ feedback.id }}</span>
              <span class="tag" :class="getTypeClass(feedback.feedbackType)">
                {{ getTypeLabel(feedback.feedbackType) }}
              </span>
              <span class="status" :class="getStatusClass(feedback.status)">
                <i class="status-dot"></i>
                {{ getStatusLabel(feedback.status) }}
              </span>
              <span class="tag" :class="getPriorityClass(feedback.priority)">
                {{ getPriorityLabel(feedback.priority) }}
              </span>
            </div>
            <div class="info-actions">
              <button class="btn-sm btn-sm-primary" @click="showEditModal = true">编辑</button>
              <button class="btn-sm btn-sm-danger" @click="handleDelete">删除</button>
            </div>
          </div>

          <div class="meta-grid">
            <div class="meta-item">
              <div class="meta-icon user">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
              </div>
              <div class="meta-content">
                <span class="meta-label">提交用户</span>
                <span class="meta-value">{{ feedback.username }}</span>
                <span class="meta-sub">{{ feedback.userEmail }}</span>
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-icon time">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
              </div>
              <div class="meta-content">
                <span class="meta-label">提交时间</span>
                <span class="meta-value">{{ formatDate(feedback.createdAt) }}</span>
              </div>
            </div>
            <div class="meta-item" v-if="feedback.processedByName">
              <div class="meta-icon admin">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
              </div>
              <div class="meta-content">
                <span class="meta-label">处理人</span>
                <span class="meta-value">{{ feedback.processedByName }}</span>
              </div>
            </div>
            <div class="meta-item" v-if="feedback.processedAt">
              <div class="meta-icon done">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                  <polyline points="22 4 12 14.01 9 11.01"/>
                </svg>
              </div>
              <div class="meta-content">
                <span class="meta-label">处理时间</span>
                <span class="meta-value">{{ formatDate(feedback.processedAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 反馈内容卡片 -->
        <div class="content-card">
          <div class="card-header">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
            <h4 class="section-title">反馈内容</h4>
          </div>
          <div class="content-text">{{ feedback.content }}</div>
        </div>

        <!-- 附件图片卡片 -->
        <div class="content-card" v-if="feedback.imageUrls && feedback.imageUrls.length > 0">
          <div class="card-header">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            <h4 class="section-title">附件图片</h4>
            <span class="image-count-badge">{{ feedback.imageUrls.length }} 张</span>
          </div>
          <div class="image-grid">
            <div
              v-for="(url, index) in feedback.imageUrls"
              :key="index"
              class="image-item"
              @click="previewImage(url)"
            >
              <img :src="getImageUrl(url)" :alt="`图片 ${index + 1}`" />
              <div class="image-overlay">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
                  <circle cx="11" cy="11" r="8"/>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                  <line x1="11" y1="8" x2="11" y2="14"/>
                  <line x1="8" y1="11" x2="14" y2="11"/>
                </svg>
              </div>
            </div>
          </div>
        </div>

        <!-- 管理员回复卡片 -->
        <div class="content-card reply-card" v-if="feedback.adminReply">
          <div class="card-header">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <h4 class="section-title">管理员回复</h4>
          </div>
          <div class="reply-content">{{ feedback.adminReply }}</div>
        </div>

        <!-- 操作记录卡片 -->
        <div class="content-card" v-if="feedback.operationLogs && feedback.operationLogs.length > 0">
          <div class="card-header">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
            </svg>
            <h4 class="section-title">操作记录</h4>
          </div>
          <div class="logs-list">
            <div v-for="log in feedback.operationLogs" :key="log.id" class="log-item">
              <div class="log-dot"></div>
              <div class="log-content">
                <div class="log-header">
                  <span class="log-operator">{{ log.operatorName }}</span>
                  <span class="log-action">{{ getOperationLabel(log.operationType) }}</span>
                </div>
                <div class="log-detail" v-if="log.oldValue || log.newValue">
                  <template v-if="log.oldValue">
                    <span class="log-old">{{ log.oldValue }}</span>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                      <path d="M5 12h14M12 5l7 7-7 7"/>
                    </svg>
                  </template>
                  <span class="log-new" v-if="log.newValue">{{ log.newValue }}</span>
                </div>
                <div class="log-time">{{ formatDate(log.createdAt) }}</div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-else-if="!loading" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <p>反馈不存在或已被删除</p>
        <button class="btn-primary" @click="goBack">返回列表</button>
      </div>
    </div>

    <el-dialog v-model="showEditModal" title="编辑反馈" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="选择状态">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已解决" value="resolved" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="editForm.priority" placeholder="选择优先级">
            <el-option label="低" value="low" />
            <el-option label="普通" value="normal" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复">
          <el-input
            v-model="editForm.adminReply"
            type="textarea"
            :rows="4"
            placeholder="输入管理员回复..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showImagePreview" title="图片预览" width="800px">
      <img :src="previewImageUrl" style="width: 100%;" alt="预览图片" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminFeedbackApi } from '@/api/adminFeedback'
import type { FeedbackDetail, FeedbackType, FeedbackStatus, FeedbackPriority, FeedbackUpdateRequest } from '@/types/feedback'

const router = useRouter()
const route = useRoute()
const feedback = ref<FeedbackDetail | null>(null)
const loading = ref(false)
const updating = ref(false)
const showEditModal = ref(false)
const showImagePreview = ref(false)
const previewImageUrl = ref('')

const editForm = ref<FeedbackUpdateRequest>({
  status: undefined,
  priority: undefined,
  adminReply: undefined
})

const feedbackId = computed(() => Number(route.params.id))

const getTypeLabel = (type: FeedbackType) => {
  const labels: Record<FeedbackType, string> = {
    suggestion: '建议',
    bug: '问题',
    feature: '功能',
    other: '其他'
  }
  return labels[type]
}

const getTypeClass = (type: FeedbackType) => {
  const classes: Record<FeedbackType, string> = {
    suggestion: 'tag-info',
    bug: 'tag-warning',
    feature: 'tag-purple',
    other: 'tag-default'
  }
  return classes[type]
}

const getStatusLabel = (status: FeedbackStatus) => {
  const labels: Record<FeedbackStatus, string> = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    closed: '已关闭'
  }
  return labels[status]
}

const getStatusClass = (status: FeedbackStatus) => {
  const classes: Record<FeedbackStatus, string> = {
    pending: 'status-pending',
    processing: 'status-processing',
    resolved: 'status-resolved',
    closed: 'status-closed'
  }
  return classes[status]
}

const getPriorityLabel = (priority: FeedbackPriority) => {
  const labels: Record<FeedbackPriority, string> = {
    low: '低',
    normal: '普通',
    high: '高',
    urgent: '紧急'
  }
  return labels[priority]
}

const getPriorityClass = (priority: FeedbackPriority) => {
  const classes: Record<FeedbackPriority, string> = {
    low: 'tag-default',
    normal: 'tag-info',
    high: 'tag-warning',
    urgent: 'tag-danger'
  }
  return classes[priority]
}

const getOperationLabel = (type: string) => {
  const labels: Record<string, string> = {
    CREATE: '创建反馈',
    UPDATE_STATUS: '更新状态',
    UPDATE_PRIORITY: '更新优先级',
    REPLY: '回复',
    DELETE: '删除'
  }
  return labels[type] || type
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

const getImageUrl = (url: string) => {
  if (url.startsWith('http')) return url
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  return `${baseUrl}${url.startsWith('/') ? '' : '/'}${url}`
}

const loadFeedback = async () => {
  loading.value = true
  try {
    const res = await adminFeedbackApi.getFeedbackDetail(feedbackId.value)
    if (res.data) {
      feedback.value = res.data
      editForm.value = {
        status: res.data.status,
        priority: res.data.priority,
        adminReply: res.data.adminReply
      }
    }
  } catch (error) {
    console.error('Failed to load feedback:', error)
    ElMessage.error('加载反馈详情失败')
  } finally {
    loading.value = false
  }
}

const handleUpdate = async () => {
  updating.value = true
  try {
    await adminFeedbackApi.updateFeedbackStatus(feedbackId.value, editForm.value)
    ElMessage.success('更新成功')
    showEditModal.value = false
    loadFeedback()
  } catch (error) {
    console.error('Failed to update feedback:', error)
    ElMessage.error('更新失败')
  } finally {
    updating.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除此反馈吗？此操作不可恢复。', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminFeedbackApi.deleteFeedback(feedbackId.value)
    ElMessage.success('删除成功')
    goBack()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete feedback:', error)
      ElMessage.error('删除失败')
    }
  }
}

const previewImage = (url: string) => {
  previewImageUrl.value = getImageUrl(url)
  showImagePreview.value = true
}

const goBack = () => {
  router.push('/admin/feedback')
}

onMounted(() => {
  loadFeedback()
})
</script>

<style scoped>
/* ===== 颜色变量 ===== */
.feedback-detail {
  --border-light: #f0f0f0;
  --border-normal: #e5e7eb;
  --bg-header: #f5f5f5;
  --bg-card: #ffffff;
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --text-muted: #9ca3af;
  --c-brand: #6366f1;
  --c-brand-light: #eef2ff;
  --c-green: #16a34a;
  --c-green-bg: #f0fdf4;
  --c-red: #dc2626;
  --c-red-bg: #fef2f2;
  --c-amber: #d97706;
  --c-amber-bg: #fffbeb;
  --c-info: #3b82f6;
  --c-info-bg: #eff6ff;
  --c-purple: #7c3aed;
  --c-purple-bg: #f5f3ff;
  padding: 20px;
}

/* ===== 页面头部 ===== */
.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--border-normal);
  border-radius: 6px;
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.15s;
}

.back-btn:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
  background: var(--c-brand-light);
}

.detail-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

/* ===== 内容区域 ===== */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 卡片通用样式 ===== */
.info-card,
.content-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 20px;
  transition: box-shadow 0.2s;
}

.info-card:hover,
.content-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* ===== 信息卡片头部 ===== */
.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.info-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.id-badge {
  background: var(--bg-header);
  color: var(--text-secondary);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'SF Mono', Monaco, 'Consolas', monospace;
}

.info-actions {
  display: flex;
  gap: 8px;
}

/* ===== 标签样式 ===== */
.tag {
  display: inline-block;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 4px;
  border: 1px solid transparent;
}

.tag-info {
  background: var(--c-info-bg);
  color: var(--c-info);
  border-color: #bfdbfe;
}

.tag-default {
  background: var(--bg-header);
  color: var(--text-secondary);
  border-color: var(--border-normal);
}

.tag-warning {
  background: var(--c-amber-bg);
  color: var(--c-amber);
  border-color: #fde68a;
}

.tag-danger {
  background: var(--c-red-bg);
  color: var(--c-red);
  border-color: #fecaca;
}

.tag-purple {
  background: var(--c-purple-bg);
  color: var(--c-purple);
  border-color: #ddd6fe;
}

/* ===== 状态样式 ===== */
.status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-pending .status-dot { background: var(--c-amber); }
.status-pending { color: var(--c-amber); }
.status-processing .status-dot { background: var(--c-info); }
.status-processing { color: var(--c-info); }
.status-resolved .status-dot { background: var(--c-green); }
.status-resolved { color: var(--c-green); }
.status-closed .status-dot { background: var(--text-muted); }
.status-closed { color: var(--text-muted); }

/* ===== 标题 ===== */
.feedback-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
}

/* ===== 元信息网格 ===== */
.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: var(--bg-header);
  border-radius: 8px;
  transition: background 0.15s;
}

.meta-item:hover {
  background: #f0f0f0;
}

.meta-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.meta-icon.user {
  background: var(--c-brand-light);
  color: var(--c-brand);
}

.meta-icon.time {
  background: var(--c-info-bg);
  color: var(--c-info);
}

.meta-icon.admin {
  background: var(--c-amber-bg);
  color: var(--c-amber);
}

.meta-icon.done {
  background: var(--c-green-bg);
  color: var(--c-green);
}

.meta-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.meta-label {
  font-size: 12px;
  color: var(--text-muted);
}

.meta-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.meta-sub {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ===== 卡片头部 ===== */
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.card-header svg {
  color: var(--c-brand);
}

.section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
}

.image-count-badge {
  background: var(--c-brand-light);
  color: var(--c-brand);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* ===== 内容文本 ===== */
.content-text {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-primary);
  white-space: pre-wrap;
  padding: 16px;
  background: var(--bg-header);
  border-radius: 8px;
}

/* ===== 图片网格 ===== */
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border-normal);
  transition: transform 0.2s, box-shadow 0.2s;
}

.image-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-overlay svg {
  color: white;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

/* ===== 回复卡片 ===== */
.reply-card .reply-content {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.8;
  color: #0369a1;
}

/* ===== 操作记录 ===== */
.logs-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  position: relative;
  padding-left: 16px;
}

.logs-list::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 12px;
  bottom: 12px;
  width: 2px;
  background: var(--border-light);
}

.log-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  position: relative;
}

.log-item + .log-item {
  border-top: 1px solid var(--border-light);
}

.log-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--c-brand);
  margin-top: 4px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  box-shadow: 0 0 0 3px var(--bg-card);
}

.log-content {
  flex: 1;
  min-width: 0;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.log-operator {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.log-action {
  font-size: 12px;
  color: var(--c-brand);
  background: var(--c-brand-light);
  padding: 2px 8px;
  border-radius: 4px;
}

.log-detail {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 13px;
}

.log-old {
  color: var(--text-muted);
  text-decoration: line-through;
}

.log-new {
  color: var(--c-green);
  font-weight: 500;
}

.log-time {
  font-size: 12px;
  color: var(--text-muted);
}

/* ===== 按钮样式 ===== */
.btn-sm {
  height: 30px;
  padding: 0 12px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid var(--border-normal);
  border-radius: 4px;
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.btn-sm:hover {
  border-color: var(--text-muted);
  color: var(--text-primary);
}

.btn-sm-primary {
  color: var(--c-brand);
  border-color: #c7d2fe;
  background: var(--c-brand-light);
}

.btn-sm-primary:hover {
  background: #e0e7ff;
  border-color: #a5b4fc;
}

.btn-sm-danger {
  color: var(--c-red);
  border-color: #fecaca;
  background: var(--c-red-bg);
}

.btn-sm-danger:hover {
  background: #fee2e2;
  border-color: #fca5a5;
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 8px;
}

.empty-state svg {
  margin-bottom: 16px;
  color: var(--text-muted);
}

.empty-state p {
  margin: 0 0 20px;
  font-size: 14px;
}

.btn-primary {
  background: var(--c-brand);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.15s;
}

.btn-primary:hover {
  background: #4f46e5;
}

/* ===== 响应式设计 ===== */
@media (max-width: 767px) {
  .feedback-detail {
    padding: 12px;
  }

  .detail-header {
    flex-wrap: wrap;
  }

  .detail-header h2 {
    font-size: 18px;
  }

  .info-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-actions {
    width: 100%;
  }

  .info-actions .btn-sm {
    flex: 1;
  }

  .meta-grid {
    grid-template-columns: 1fr;
  }

  .feedback-title {
    font-size: 16px;
  }

  .image-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .card-header {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .image-grid {
    grid-template-columns: 1fr;
  }
}
</style>
