<template>
  <div class="feedback-manage">
    <div class="stats-row">
      <div class="stat-item">
        <span class="stat-num">{{ stats.totalFeedbacks }}</span>
        <span class="stat-label">总反馈</span>
      </div>
      <div class="stat-item pending">
        <span class="stat-num">{{ stats.pendingFeedbacks }}</span>
        <span class="stat-label">待处理</span>
      </div>
      <div class="stat-item processing">
        <span class="stat-num">{{ stats.processingFeedbacks }}</span>
        <span class="stat-label">处理中</span>
      </div>
      <div class="stat-item resolved">
        <span class="stat-num">{{ stats.resolvedFeedbacks }}</span>
        <span class="stat-label">已解决</span>
      </div>
    </div>

    <div class="content-card">
      <div class="toolbar">
        <div class="filter-group">
          <select v-model="filters.status" @change="loadFeedbacks" class="filter-select">
            <option value="">全部状态</option>
            <option value="pending">待处理</option>
            <option value="processing">处理中</option>
            <option value="resolved">已解决</option>
            <option value="closed">已关闭</option>
          </select>
          
          <select v-model="filters.feedbackType" @change="loadFeedbacks" class="filter-select">
            <option value="">全部类型</option>
            <option value="suggestion">建议</option>
            <option value="bug">问题反馈</option>
            <option value="feature">功能请求</option>
            <option value="other">其他</option>
          </select>
          
          <select v-model="filters.priority" @change="loadFeedbacks" class="filter-select">
            <option value="">全部优先级</option>
            <option value="urgent">紧急</option>
            <option value="high">高</option>
            <option value="normal">普通</option>
            <option value="low">低</option>
          </select>
        </div>
        
        <!-- <div class="search-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="filters.keyword"
            type="text"
            placeholder="搜索标题或内容..."
            class="search-input"
            @keyup.enter="loadFeedbacks"
          />
          <button v-if="filters.keyword" class="search-clear" @click="handleClearSearch">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div> -->
        
        <!-- <button class="btn btn-primary" @click="loadFeedbacks">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          搜索
        </button> -->
        
        <span class="toolbar-total">
          共 <strong>{{ total }}</strong> 条反馈
        </span>
      </div>

      <div class="table-wrap" v-loading="loading">
        <table class="table">
          <thead>
            <tr>
              <th style="width:60px">序号</th>
              <th style="width:100px">反馈类型</th>
              <th style="min-width:200px">反馈内容</th>
              <th style="width:120px">图片</th>
              <th style="min-width:140px">反馈用户</th>
              <th style="width:90px">处理状态</th>
              <th style="width:80px">优先级</th>
              <th style="width:100px">处理人</th>
              <th style="width:110px">创建时间</th>
              <th style="width:110px">更新时间</th>
              <th style="width:200px;text-align:center;position:sticky;right:0;background:var(--bg-header)">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in feedbacks" :key="row.id">
              <td><span class="seq-num">{{ getSequenceNumber(index) }}</span></td>
              <td>
                <select 
                  class="type-select" 
                  :value="row.feedbackType"
                  @change="handleTypeChange(row, $event)"
                >
                  <option value="suggestion">建议</option>
                  <option value="bug">问题</option>
                  <option value="other">其他</option>
                </select>
              </td>
              <td>
                <el-tooltip 
                  :content="row.content" 
                  placement="top" 
                  :disabled="!isContentOverflow(row.content)"
                  popper-class="content-tooltip"
                >
                  <span class="content-text">{{ row.content }}</span>
                </el-tooltip>
              </td>
              <td>
                <div v-if="row.imageUrls && row.imageUrls.length > 0" class="image-cell">
                  <div 
                    v-for="(url, imgIndex) in row.imageUrls.slice(0, 3)" 
                    :key="imgIndex" 
                    class="image-thumb"
                    @click="previewImages(row.imageUrls, imgIndex)"
                  >
                    <img :src="getImageUrl(url)" alt="图片" />
                  </div>
                  <span v-if="row.imageUrls.length > 3" class="image-more" @click="previewImages(row.imageUrls, 0)">
                    +{{ row.imageUrls.length - 3 }}
                  </span>
                </div>
                <span v-else class="no-image">-</span>
              </td>
              <td>
                <div class="user-cell">
                  <span class="user-name">{{ row.username }}</span>
                  <span class="user-id">ID: {{ row.userId }}</span>
                </div>
              </td>
              <td>
                <span class="status" :class="getStatusClass(row.status)">
                  <i class="status-dot"></i>
                  {{ getStatusLabel(row.status) }}
                </span>
              </td>
              <td>
                <span class="priority-tag" :class="getPriorityClass(row.priority)">
                  <span class="priority-icon" v-html="getPriorityIcon(row.priority)"></span>
                  {{ getPriorityLabel(row.priority) }}
                </span>
              </td>
              <td><span class="processor-text">{{ row.processedByName || '-' }}</span></td>
              <td><span class="time">{{ formatDateOnly(row.createdAt) }}</span></td>
              <td><span class="time">{{ formatDateOnly(row.updatedAt) }}</span></td>
              <td class="actions-cell">
                <div class="actions">
                  <button class="btn-sm btn-sm-primary" @click="viewDetail(row.id)">查看</button>
                  <button class="btn-sm btn-sm-ok" @click="quickProcess(row)" v-if="row.status === 'pending'">处理</button>
                  <button class="btn-sm btn-sm-danger" @click="handleDelete(row)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && feedbacks.length === 0">
              <td colspan="11" class="empty-cell">
                <div class="empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="40" height="40">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  <span>暂无反馈数据</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="footer" v-if="total > 0">
        <span class="footer-info">
          第 {{ (page - 1) * pageSize + 1 }}-{{ Math.min(page * pageSize, total) }} 条，共 {{ total }} 条
        </span>
        <div class="pager">
          <button class="pager-btn" :disabled="page === 1" @click="changePage(page - 1)">&laquo;</button>
          <button
            v-for="p in visiblePages"
            :key="p"
            class="pager-btn"
            :class="{ active: p === page }"
            @click="changePage(p)"
          >{{ p }}</button>
          <button class="pager-btn" :disabled="page >= totalPages" @click="changePage(page + 1)">&raquo;</button>
        </div>
      </div>
    </div>

    <el-dialog v-model="showImagePreview" title="图片预览" width="800px" class="image-preview-dialog">
      <div class="preview-container">
        <button 
          v-if="previewImageUrls.length > 1" 
          class="preview-nav prev" 
          @click="prevImage"
          :disabled="currentImageIndex === 0"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path d="M15 18l-6-6 6-6"/>
          </svg>
        </button>
        <div class="preview-main">
          <img
            v-if="previewImageUrls[currentImageIndex]"
            :src="getImageUrl(previewImageUrls[currentImageIndex])"
            :alt="`图片 ${currentImageIndex + 1}`"
            class="preview-image"
          />
          <div class="preview-indicator" v-if="previewImageUrls.length > 1">
            {{ currentImageIndex + 1 }} / {{ previewImageUrls.length }}
          </div>
        </div>
        <button 
          v-if="previewImageUrls.length > 1" 
          class="preview-nav next" 
          @click="nextImage"
          :disabled="currentImageIndex >= previewImageUrls.length - 1"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path d="M9 18l6-6-6-6"/>
          </svg>
        </button>
      </div>
      <div class="preview-thumbnails" v-if="previewImageUrls.length > 1">
        <div 
          v-for="(url, index) in previewImageUrls" 
          :key="index"
          class="thumbnail-item"
          :class="{ active: index === currentImageIndex }"
          @click="currentImageIndex = index"
        >
          <img :src="getImageUrl(url)" :alt="`缩略图 ${index + 1}`" />
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showQuickProcess" title="快速处理" width="500px">
      <el-form :model="quickProcessForm" label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="quickProcessForm.status" placeholder="选择状态">
            <el-option label="处理中" value="processing" />
            <el-option label="已解决" value="resolved" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复">
          <el-input
            v-model="quickProcessForm.adminReply"
            type="textarea"
            :rows="4"
            placeholder="输入回复内容..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showQuickProcess = false">取消</el-button>
        <el-button type="primary" @click="submitQuickProcess" :loading="processing">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminFeedbackApi } from '@/api/adminFeedback'
import type { Feedback, FeedbackStats, FeedbackType, FeedbackStatus, FeedbackPriority, FeedbackUpdateRequest } from '@/types/feedback'

const router = useRouter()
const feedbacks = ref<Feedback[]>([])
const loading = ref(false)
const processing = ref(false)
const stats = ref<FeedbackStats>({
  totalFeedbacks: 0,
  pendingFeedbacks: 0,
  processingFeedbacks: 0,
  resolvedFeedbacks: 0,
  closedFeedbacks: 0,
  todayFeedbacks: 0,
  weekFeedbacks: 0,
  monthFeedbacks: 0,
  bugCount: 0,
  suggestionCount: 0,
  featureCount: 0,
  highPriorityCount: 0
})

const page = ref(1)
const pageSize = 10
const total = ref(0)

const filters = ref({
  status: '',
  feedbackType: '',
  priority: '',
  keyword: ''
})

const showImagePreview = ref(false)
const previewImageUrls = ref<string[]>([])
const currentImageIndex = ref(0)

const showQuickProcess = ref(false)
const currentFeedback = ref<Feedback | null>(null)
const quickProcessForm = ref<FeedbackUpdateRequest>({
  status: 'processing',
  adminReply: ''
})

const totalPages = computed(() => Math.ceil(total.value / pageSize))

const visiblePages = computed(() => {
  const pages: number[] = []
  const total = totalPages.value
  const current = page.value
  let start = Math.max(1, current - 2)
  let end = Math.min(total, current + 2)
  if (end - start < 4) {
    if (start === 1) end = Math.min(total, start + 4)
    else start = Math.max(1, end - 4)
  }
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

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

const formatDateOnly = (date: string) => {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getSequenceNumber = (index: number) => {
  return (page.value - 1) * pageSize + index + 1
}

const isContentOverflow = (content: string) => {
  return content && content.length > 50
}

const getPriorityIcon = (priority: FeedbackPriority) => {
  const icons: Record<FeedbackPriority, string> = {
    low: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M19 14l-7 7-7-7"/><path d="M19 10l-7-7-7 7"/></svg>',
    normal: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><circle cx="12" cy="12" r="10"/></svg>',
    high: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M12 2v20M2 12h20"/></svg>',
    urgent: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>'
  }
  return icons[priority]
}

const handleTypeChange = async (feedback: Feedback, event: Event) => {
  const target = event.target as HTMLSelectElement
  const newType = target.value as FeedbackType
  
  if (newType === feedback.feedbackType) return
  
  try {
    await adminFeedbackApi.updateFeedbackStatus(feedback.id, { feedbackType: newType } as any)
    ElMessage.success('类型已更新')
    loadFeedbacks()
  } catch (error) {
    console.error('Failed to update type:', error)
    ElMessage.error('更新失败')
    // 恢复原值
    target.value = feedback.feedbackType
  }
}

const getImageUrl = (url: string) => {
  if (url.startsWith('http')) return url
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  return `${baseUrl}${url.startsWith('/') ? '' : '/'}${url}`
}

const previewImages = (urls: string[], startIndex: number = 0) => {
  previewImageUrls.value = urls
  currentImageIndex.value = startIndex
  showImagePreview.value = true
}

const prevImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  }
}

const nextImage = () => {
  if (currentImageIndex.value < previewImageUrls.value.length - 1) {
    currentImageIndex.value++
  }
}

const loadStats = async () => {
  try {
    const res = await adminFeedbackApi.getFeedbackStats()
    if (res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const loadFeedbacks = async () => {
  loading.value = true
  try {
    const res = await adminFeedbackApi.getFeedbackList({
      page: page.value,
      size: pageSize,
      status: filters.value.status as FeedbackStatus || undefined,
      feedbackType: filters.value.feedbackType as FeedbackType || undefined,
      priority: filters.value.priority as FeedbackPriority || undefined,
      keyword: filters.value.keyword || undefined
    })
    if (res.data) {
      feedbacks.value = res.data.items
      total.value = res.data.total
    }
  } catch (error) {
    console.error('Failed to load feedbacks:', error)
  } finally {
    loading.value = false
  }
}

const changePage = (newPage: number) => {
  page.value = newPage
  loadFeedbacks()
}

const viewDetail = (id: number) => {
  router.push(`/admin/feedback/${id}`)
}

const quickProcess = (feedback: Feedback) => {
  currentFeedback.value = feedback
  quickProcessForm.value = {
    status: 'processing',
    adminReply: ''
  }
  showQuickProcess.value = true
}

const submitQuickProcess = async () => {
  if (!currentFeedback.value) return
  
  processing.value = true
  try {
    await adminFeedbackApi.updateFeedbackStatus(currentFeedback.value.id, quickProcessForm.value)
    ElMessage.success('处理成功')
    showQuickProcess.value = false
    loadFeedbacks()
    loadStats()
  } catch (error) {
    console.error('Failed to process feedback:', error)
    ElMessage.error('处理失败')
  } finally {
    processing.value = false
  }
}

const handleDelete = async (feedback: Feedback) => {
  try {
    await ElMessageBox.confirm(`确定要删除该反馈吗？此操作不可恢复。`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminFeedbackApi.deleteFeedback(feedback.id)
    ElMessage.success('删除成功')
    loadFeedbacks()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete feedback:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadStats()
  loadFeedbacks()
})
</script>

<style scoped>
/* ===== 颜色变量 ===== */
.feedback-manage {
  --border-light: #f0f0f0;
  --border-normal: #e5e7eb;
  --bg-header: #f5f5f5;
  --bg-hover: #f5f5f5;
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
}

/* ===== 统计卡片 ===== */
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  flex: 1;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-item.pending .stat-num { color: var(--c-amber); }
.stat-item.processing .stat-num { color: var(--c-info); }
.stat-item.resolved .stat-num { color: var(--c-green); }

/* ===== 内容卡片 ===== */
.content-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 16px;
}

/* ===== 工具栏 ===== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin: -16px -16px 16px -16px;
  background: var(--bg-header);
  border-bottom: 1px solid var(--border-light);
  border-radius: 8px 8px 0 0;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  gap: 8px;
}

.filter-select {
  height: 36px;
  padding: 0 28px 0 12px;
  border: 1px solid var(--border-normal);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--bg-header);
  cursor: pointer;
  outline: none;
  transition: border-color 0.15s;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%236b7280' stroke-width='2'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
}
.filter-select:focus { border-color: var(--c-brand); background-color: #fff; }

.search-wrap {
  position: relative;
  flex: 1;
  max-width: 360px;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 32px 0 32px;
  border: 1px solid var(--border-normal);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--bg-header);
  outline: none;
  transition: border-color 0.15s;
}
.search-input:focus { border-color: var(--c-brand); background: #fff; }
.search-input::placeholder { color: var(--text-muted); }

.search-clear {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 2px;
  line-height: 1;
}
.search-clear:hover { color: var(--text-secondary); }

.toolbar-total {
  margin-left: auto;
  font-size: 13px;
  color: var(--text-secondary);
}
.toolbar-total strong { color: var(--text-primary); }

/* ===== 通用按钮 ===== */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 36px;
  padding: 0 16px;
  border: 1px solid transparent;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-primary {
  background: var(--c-brand);
  color: #fff;
}
.btn-primary:hover { background: #4f46e5; }

/* ===== 表格 ===== */
.table-wrap { 
  overflow-x: auto;
  margin: 0 -16px;
  padding: 0 16px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
  min-width: 1200px;
  table-layout: auto;
  border: 1px solid var(--border-light);
}

.table thead { background: var(--bg-header); }
.table th {
  padding: 10px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
  white-space: nowrap;
}

.table td {
  padding: 10px 16px;
  border: 1px solid var(--border-light);
  vertical-align: middle;
  font-size: 13px;
  color: var(--text-primary);
  height: 44px;
}

.table tbody tr:nth-child(odd) { background-color: #FFFFFF; }
.table tbody tr:nth-child(even) { background-color: #FAFAFA; }
.table tbody tr:hover { background-color: #E8F4FD !important; }

/* ===== ID ===== */
.id-code {
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-header);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'SF Mono', Monaco, 'Consolas', monospace;
}

/* ===== 序号 ===== */
.seq-num {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

/* ===== 类型选择框 ===== */
.type-select {
  height: 28px;
  padding: 0 24px 0 8px;
  border: 1px solid var(--border-normal);
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-primary);
  background: #fff;
  cursor: pointer;
  outline: none;
  transition: border-color 0.15s;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%236b7280' stroke-width='2'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 6px center;
}
.type-select:focus { border-color: var(--c-brand); }
.type-select:hover { border-color: var(--text-muted); }

/* ===== 反馈内容 ===== */
.content-text {
  font-size: 13px;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
  max-height: 3em;
  word-break: break-all;
}

/* ===== 标题 ===== */
.title-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.user-text {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ===== 用户信息 ===== */
.user-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-id {
  font-size: 11px;
  color: var(--text-muted);
}

/* ===== 处理人 ===== */
.processor-text {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ===== 图片 ===== */
.image-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.image-thumb {
  position: relative;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border-normal);
  flex-shrink: 0;
}

.image-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-thumb:hover {
  border-color: var(--c-brand);
}

.image-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background: var(--bg-header);
  border: 1px dashed var(--border-normal);
  font-size: 11px;
  color: var(--text-secondary);
  cursor: pointer;
  flex-shrink: 0;
}

.image-more:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.image-count {
  position: absolute;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px 0 0 0;
}

.no-image {
  color: var(--text-muted);
}

/* ===== 图片预览 ===== */
.preview-container {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.preview-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.preview-image {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  object-fit: contain;
}

.preview-indicator {
  font-size: 13px;
  color: var(--text-secondary);
}

.preview-nav {
  width: 40px;
  height: 40px;
  border: 1px solid var(--border-normal);
  border-radius: 50%;
  background: #fff;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
  flex-shrink: 0;
}

.preview-nav:hover:not(:disabled) {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.preview-nav:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.preview-thumbnails {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.15s;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-item:hover {
  border-color: var(--c-brand);
}

.thumbnail-item.active {
  border-color: var(--c-brand);
}

/* ===== 标签 ===== */
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

/* ===== 优先级标签 ===== */
.priority-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 4px;
  border: 1px solid transparent;
}

.priority-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.priority-tag.tag-danger {
  background: var(--c-red-bg);
  color: var(--c-red);
  border-color: #fecaca;
}

.priority-tag.tag-warning {
  background: var(--c-amber-bg);
  color: var(--c-amber);
  border-color: #fde68a;
}

.priority-tag.tag-info {
  background: var(--c-info-bg);
  color: var(--c-info);
  border-color: #bfdbfe;
}

.priority-tag.tag-default {
  background: var(--bg-header);
  color: var(--text-secondary);
  border-color: var(--border-normal);
}

/* ===== 状态 ===== */
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

.time { font-size: 12px; color: var(--text-secondary); }

/* ===== 操作列固定 ===== */
.actions-cell {
  position: sticky;
  right: 0;
  background: #fff;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);
  z-index: 1;
}

/* ===== 操作按钮 ===== */
.actions {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.btn-sm {
  height: 28px;
  padding: 0 10px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid var(--border-normal);
  border-radius: 4px;
  background: #fff;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.btn-sm:hover { border-color: var(--text-muted); color: var(--text-primary); }

.btn-sm-primary { color: var(--c-brand); border-color: #c7d2fe; background: var(--c-brand-light); }
.btn-sm-primary:hover { background: #e0e7ff; border-color: #a5b4fc; }

.btn-sm-ok { color: var(--c-green); border-color: #bbf7d0; background: var(--c-green-bg); }
.btn-sm-ok:hover { background: #dcfce7; border-color: #86efac; }

.btn-sm-danger { color: var(--c-red); border-color: #fecaca; background: var(--c-red-bg); }
.btn-sm-danger:hover { background: #fee2e2; border-color: #fca5a5; }

/* ===== 空状态 ===== */
.empty-cell { padding: 64px 16px !important; border-bottom: none !important; }
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: var(--text-muted);
  font-size: 13px;
}

/* ===== 底部分页 ===== */
.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin: 16px -16px -16px -16px;
  background: var(--bg-header);
  border-top: 1px solid var(--border-light);
  border-radius: 0 0 8px 8px;
}
.footer-info { font-size: 13px; color: var(--text-secondary); }

.pager { display: flex; gap: 4px; }
.pager-btn {
  min-width: 34px;
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--border-normal);
  border-radius: 4px;
  background: #fff;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}
.pager-btn:hover:not(:disabled):not(.active) {
  border-color: var(--c-brand);
  color: var(--c-brand);
}
.pager-btn:disabled { opacity: .35; cursor: not-allowed; }
.pager-btn.active {
  background: var(--c-brand);
  border-color: var(--c-brand);
  color: #fff;
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 767px) {
  .stats-row {
    flex-direction: column;
  }
  .stat-item {
    flex-direction: row;
    justify-content: space-between;
  }
  .toolbar { flex-wrap: wrap; }
  .filter-group {
    width: 100%;
    flex-wrap: wrap;
  }
  .filter-select {
    flex: 1;
    min-width: 120px;
  }
  .search-wrap { max-width: none; width: 100%; }
  .toolbar-total { margin-left: 0; width: 100%; }
  .footer { flex-direction: column; gap: 10px; align-items: flex-start; }
}
</style>
