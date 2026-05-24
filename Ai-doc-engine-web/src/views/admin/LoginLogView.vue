<template>
  <div class="login-log">
  

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
            placeholder="搜索用户名 / IP 地址"
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
        <button class="btn btn-primary" @click="handleSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          搜索
        </button>
        <span class="toolbar-stat">
          共 <strong>{{ adminStore.loginLogs?.total ?? 0 }}</strong> 条
        </span>
        <span class="toolbar-stat toolbar-stat-ok">
          成功 <strong>{{ successCount }}</strong>
        </span>
        <span class="toolbar-stat toolbar-stat-fail">
          失败 <strong>{{ failCount }}</strong>
        </span>
      </div>

      <div class="table-wrap" v-loading="adminStore.loading">
        <table class="table">
          <thead>
            <tr>
              <th style="width:60px">序号</th>
              <th style="min-width:140px">登录账号</th>
              <th style="width:100px">用户昵称</th>
              <th style="width:90px">登录结果</th>
              <th style="width:90px">浏览器</th>
              <th style="width:140px">IP 地址</th>
              <th style="min-width:160px">失败原因</th>
              <th style="width:170px">登录时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in adminStore.loginLogs?.items ?? []" :key="row.id">
              <td><span class="seq-num">{{ getSequenceNumber(index) }}</span></td>
              <td>
                <div class="acct-cell">
                  <span class="acct-icon">
                    {{ (row.nickname || row.usernameOrEmail || '?').charAt(0).toUpperCase() }}
                  </span>
                  <span class="acct-text">{{ row.usernameOrEmail }}</span>
                </div>
              </td>
              <td><span class="nick-text">{{ row.nickname || '-' }}</span></td>
              <td>
                <span class="tag" :class="row.loginResult === 'success' ? 'tag-ok' : 'tag-fail'">
                  {{ row.loginResult === 'success' ? '成功' : '失败' }}
                </span>
              </td>
              <td>
                <span class="tag tag-type">
                  {{ row.browserType || '密码登录' }}
                </span>
              </td>
              <td><span class="ip-text">{{ row.ipAddress || '-' }}</span></td>
              <td>
                <span v-if="row.failReason" class="reason-text">{{ row.failReason }}</span>
                <span v-else class="na-text">-</span>
              </td>
              <td><span class="time">{{ formatTime(row.createdAt) }}</span></td>
            </tr>
            <tr v-if="!adminStore.loading && (!adminStore.loginLogs?.items?.length)">
              <td colspan="8" class="empty-cell">
                <div class="empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="40" height="40">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                  <span>暂无登录记录</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="footer" v-if="adminStore.loginLogs?.total">
        <span class="footer-info">
          第 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, adminStore.loginLogs?.total ?? 0) }} 条，共 {{ adminStore.loginLogs?.total ?? 0 }} 条
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAdminStore } from '@/store/admin'

const adminStore = useAdminStore()
const keyword = ref('')
const currentPage = ref(1)
const pageSize = 10

const totalPages = computed(() => Math.ceil((adminStore.loginLogs?.total ?? 0) / pageSize))

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

const successCount = computed(() =>
  adminStore.loginLogs?.items?.filter(item => item.loginResult === 'success').length ?? 0
)
const failCount = computed(() =>
  adminStore.loginLogs?.items?.filter(item => item.loginResult !== 'success').length ?? 0
)

const formatTime = (t?: string) => {
  if (!t) return '-'
  const d = new Date(t)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const getSequenceNumber = (index: number) => {
  return (currentPage.value - 1) * pageSize + index + 1
}

const fetchData = () => adminStore.fetchLoginLogs(currentPage.value, pageSize, keyword.value || undefined)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleClearSearch = () => { keyword.value = ''; handleSearch() }
const handlePageChange = (p: number) => { currentPage.value = p; fetchData() }

onMounted(() => fetchData())
</script>

<style scoped>
/* ===== 颜色变量 ===== */
.login-log {
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
  --c-blue: #2563eb;
  --c-blue-bg: #eff6ff;
}

/* ===== 页面头部 ===== */
.page-header { margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0; }
.page-desc { font-size: 13px; color: var(--text-secondary); margin: 0; }

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
}

.search-wrap {
  position: relative;
  flex: 1;
  max-width: 320px;
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

.toolbar-stat {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
}
.toolbar-stat strong { color: var(--text-primary); font-weight: 600; }
.toolbar-stat-ok strong { color: var(--c-green); }
.toolbar-stat-fail strong { color: var(--c-red); }

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
  min-width: 1000px;
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

/* 交叉行颜色 - 仅应用于tbody，符合WCAG 2.1 AA标准 */
/* 奇数行：纯白色背景 */
.table tbody tr:nth-child(odd) { 
  background-color: #FFFFFF; 
}
/* 偶数行：比表头(#f5f5f5)浅一点的灰色 */
.table tbody tr:nth-child(even) { 
  background-color: #FAFAFA; 
}
/* 悬停效果 */
.table tbody tr:hover { 
  background-color: #E8F4FD !important;
}

/* ===== 序号 ===== */
.seq-num {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

/* ===== 账号 ===== */
.acct-cell { display: flex; align-items: center; gap: 10px; }
.acct-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--border-normal);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}
.acct-text { font-size: 13px; font-weight: 600; color: var(--text-primary); }

/* ===== 昵称 ===== */
.nick-text { font-size: 13px; color: var(--text-primary); }

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
.tag-type { background: var(--c-blue-bg); color: var(--c-blue); border-color: #bfdbfe; }

/* ===== IP ===== */
.ip-text {
  font-size: 12px;
  color: var(--text-secondary);
  font-family: 'SF Mono', Monaco, 'Consolas', monospace;
}

/* ===== 失败原因 ===== */
.reason-text {
  font-size: 13px;
  color: var(--c-red);
}

.na-text { font-size: 13px; color: var(--text-muted); }

.time { font-size: 12px; color: var(--text-secondary); }

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

/* ===== 响应式 ===== */
@media (max-width: 767px) {
  .toolbar { flex-wrap: wrap; }
  .search-wrap { max-width: none; }
  .footer { flex-direction: column; gap: 10px; align-items: flex-start; }
}
</style>