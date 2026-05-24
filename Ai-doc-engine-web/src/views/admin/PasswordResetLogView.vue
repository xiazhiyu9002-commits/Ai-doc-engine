<template>
  <div class="password-reset-log">
    <div class="content-card">
      <div class="toolbar">
        <div class="search-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索用户"
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button v-if="keyword" class="search-clear" @click="handleClearSearch">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <select v-model="filterStatus" class="filter-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="pending">待使用</option>
          <option value="used">已使用</option>
          <option value="expired">已过期</option>
        </select>
        <button class="btn btn-primary" @click="handleSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          搜索
        </button>
        <button class="btn btn-danger" @click="handleClearExpired">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
          清理过期令牌
        </button>
        <span class="toolbar-stat">
          共 <strong>{{ adminStore.passwordResetLogs?.total ?? 0 }}</strong> 条
        </span>
      </div>

      <div class="table-wrap" v-loading="adminStore.loading">
        <table class="table">
          <thead>
            <tr>
              <th style="width:60px">序号</th>
              <th style="width:120px">用户</th>
              <th style="width:200px">令牌</th>
              <th style="width:90px">状态</th>
              <th style="width:170px">过期时间</th>
              <th style="width:170px">使用时间</th>
              <th style="width:170px">创建时间</th>
              <th style="width:140px;text-align:center;position:sticky;right:0;background:var(--bg-header)">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in adminStore.passwordResetLogs?.items ?? []" :key="row.id">
              <td><span class="seq-num">{{ getSequenceNumber(index) }}</span></td>
              <td>
                <div class="user-cell">
                  <span class="avatar">
                    {{ (row.nickname || row.username || '?').charAt(0).toUpperCase() }}
                  </span>
                  <span class="user-name">{{ row.nickname || row.username }}</span>
                </div>
              </td>
              <td>
                <span class="token-text" :title="row.token">{{ maskToken(row.token) }}</span>
              </td>
              <td>
                <span class="tag" :class="getStatusClass(row.status)">
                  {{ getStatusText(row.status) }}
                </span>
              </td>
              <td>
                <span class="time" :class="{ 'time-expired': isExpired(row.expireAt) && row.status === 'pending' }">
                  {{ formatTime(row.expireAt) }}
                </span>
              </td>
              <td>
                <span v-if="row.usedAt" class="time">{{ formatTime(row.usedAt) }}</span>
                <span v-else class="na-text">-</span>
              </td>
              <td><span class="time">{{ formatTime(row.createdAt) }}</span></td>
              <td class="actions-cell">
                <div class="actions">
                  <button class="btn-sm btn-sm-primary" @click="handleViewDetail(row)">详情</button>
                  <button class="btn-sm btn-sm-danger" @click="handleDelete(row)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="!adminStore.loading && (!adminStore.passwordResetLogs?.items?.length)">
              <td colspan="8" class="empty-cell">
                <div class="empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="40" height="40">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                  <span>暂无密码重置日志</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="footer" v-if="adminStore.passwordResetLogs?.total">
        <span class="footer-info">
          第 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, adminStore.passwordResetLogs?.total ?? 0) }} 条，共 {{ adminStore.passwordResetLogs?.total ?? 0 }} 条
        </span>
        <div class="pager">
          <button class="pager-btn" :disabled="currentPage === 1" @click="handlePageChange(currentPage - 1)">&laquo;</button>
          <button
            v-for="p in visiblePages"
            :key="p"
            class="pager-btn"
            :class="{ active: p === currentPage }"
            @click="handlePageChange(p)"
          >{{ p }}</button>
          <button class="pager-btn" :disabled="currentPage >= totalPages" @click="handlePageChange(currentPage + 1)">&raquo;</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="密码重置日志详情" width="500px" destroy-on-close>
      <div v-if="currentLog" class="detail-content">
        <div class="detail-row">
          <label>用户：</label>
          <span>{{ currentLog.nickname || currentLog.username }}</span>
        </div>
        <div class="detail-row">
          <label>状态：</label>
          <span class="tag" :class="getStatusClass(currentLog.status)">
            {{ getStatusText(currentLog.status) }}
          </span>
        </div>
        <div class="detail-row">
          <label>过期时间：</label>
          <span>{{ formatTime(currentLog.expireAt) }}</span>
        </div>
        <div class="detail-row">
          <label>使用时间：</label>
          <span>{{ currentLog.usedAt ? formatTime(currentLog.usedAt) : '-' }}</span>
        </div>
        <div class="detail-row">
          <label>创建时间：</label>
          <span>{{ formatTime(currentLog.createdAt) }}</span>
        </div>
        <div class="detail-row detail-row-full">
          <label>令牌：</label>
          <div class="token-box">{{ currentLog.token }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/store/admin'
import type { PasswordResetLog } from '@/types/admin'

const adminStore = useAdminStore()
const keyword = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = 10
const detailVisible = ref(false)
const currentLog = ref<PasswordResetLog | null>(null)

const totalPages = computed(() => Math.ceil((adminStore.passwordResetLogs?.total ?? 0) / pageSize))

const visiblePages = computed(() => {
  const pages: number[] = []
  const total = totalPages.value
  const current = currentPage.value
  let start = Math.max(1, current - 2)
  let end = Math.min(total, current + 2)
  if (end - start < 4) {
    if (start === 1) end = Math.min(total, start + 4)
    else start = Math.max(1, end - 4)
  }
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const formatTime = (t?: string) => {
  if (!t) return '-'
  const d = new Date(t)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const maskToken = (token: string) => {
  if (!token || token.length < 16) return token
  return token.slice(0, 8) + '****' + token.slice(-4)
}

const isExpired = (expireAt: string) => {
  return new Date(expireAt) < new Date()
}

const getSequenceNumber = (index: number) => {
  return (currentPage.value - 1) * pageSize + index + 1
}

const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    pending: 'tag-warn',
    used: 'tag-ok',
    expired: 'tag-fail'
  }
  return map[status] || 'tag-default'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待使用',
    used: '已使用',
    expired: '已过期'
  }
  return map[status] || status
}

const fetchData = () => {
  adminStore.fetchPasswordResetLogs({
    page: currentPage.value,
    size: pageSize,
    keyword: keyword.value || undefined,
    status: filterStatus.value || undefined
  })
}

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleClearSearch = () => { keyword.value = ''; handleSearch() }
const handlePageChange = (p: number) => { currentPage.value = p; fetchData() }

const handleViewDetail = (row: PasswordResetLog) => {
  currentLog.value = row
  detailVisible.value = true
}

const handleDelete = async (row: PasswordResetLog) => {
  try {
    await ElMessageBox.confirm(`确定要删除该密码重置记录吗？`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminStore.deletePasswordResetLog(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

const handleClearExpired = async () => {
  try {
    await ElMessageBox.confirm('确定要清理所有过期的令牌吗？此操作不可恢复。', '确认清理', {
      confirmButtonText: '清理',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const result = await adminStore.clearExpiredTokens()
    ElMessage.success(`已清理 ${result.count} 条过期令牌`)
    fetchData()
  } catch {}
}

onMounted(() => fetchData())
</script>

<style scoped>
/* ===== 颜色变量 ===== */
.password-reset-log {
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
}

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

.search-wrap {
  position: relative;
  flex: 1;
  max-width: 240px;
  min-width: 150px;
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

.filter-select {
  height: 36px;
  padding: 0 28px 0 12px;
  border: 1px solid var(--border-normal);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-primary);
  background: #fff;
  outline: none;
  cursor: pointer;
}

.toolbar-stat {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
}
.toolbar-stat strong { color: var(--text-primary); font-weight: 600; }

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
.btn-primary { background: var(--c-brand); color: #fff; }
.btn-primary:hover { background: #4f46e5; }
.btn-danger { background: var(--c-red); color: #fff; }
.btn-danger:hover { background: #b91c1c; }

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
  min-width: 1100px;
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

/* ===== 序号 ===== */
.seq-num {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

/* ===== 用户信息 ===== */
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--border-normal);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name { font-size: 13px; font-weight: 500; color: var(--text-primary); }

/* ===== 令牌 ===== */
.token-text {
  font-size: 12px;
  color: var(--text-secondary);
  font-family: 'SF Mono', Monaco, 'Consolas', monospace;
}

/* ===== 标签 ===== */
.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 4px;
  border: 1px solid transparent;
  white-space: nowrap;
}

.tag-ok { background: var(--c-green-bg); color: var(--c-green); border-color: #bbf7d0; }
.tag-fail { background: var(--c-red-bg); color: var(--c-red); border-color: #fecaca; }
.tag-warn { background: var(--c-amber-bg); color: var(--c-amber); border-color: #fde68a; }
.tag-default { background: var(--bg-header); color: var(--text-secondary); border-color: var(--border-normal); }

.na-text { font-size: 13px; color: var(--text-muted); }
.time { font-size: 12px; color: var(--text-secondary); }
.time-expired { color: var(--c-red); }

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
  gap: 8px;
  justify-content: center;
}

.btn-sm {
  height: 30px;
  padding: 0 12px;
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
.pager-btn:hover:not(:disabled):not(.active) { border-color: var(--c-brand); color: var(--c-brand); }
.pager-btn:disabled { opacity: .35; cursor: not-allowed; }
.pager-btn.active { background: var(--c-brand); border-color: var(--c-brand); color: #fff; }

/* ===== 详情弹窗 ===== */
.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-row label {
  font-size: 13px;
  color: var(--text-secondary);
  min-width: 70px;
}

.detail-row span {
  font-size: 13px;
  color: var(--text-primary);
}

.detail-row-full {
  grid-column: 1 / -1;
  flex-direction: column;
  align-items: flex-start;
}

.token-box {
  width: 100%;
  padding: 12px;
  background: var(--bg-header);
  border: 1px solid var(--border-light);
  border-radius: 6px;
  font-size: 12px;
  color: var(--text-secondary);
  font-family: 'SF Mono', Monaco, 'Consolas', monospace;
  word-break: break-all;
}

/* ===== 响应式 ===== */
@media (max-width: 767px) {
  .toolbar { flex-wrap: wrap; }
  .search-wrap { max-width: none; }
  .footer { flex-direction: column; gap: 10px; align-items: flex-start; }
}
</style>
