<template>
  <div class="user-manage">
    <!-- <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-desc">管理系统用户账户与权限</p>
    </div> -->

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
            placeholder="搜索用户名 / 邮箱 / 昵称"
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
        <span class="toolbar-total">
          共 <strong>{{ adminStore.users?.total ?? 0 }}</strong> 个用户
        </span>
      </div>

      <div class="table-wrap" v-loading="adminStore.loading">
        <table class="table">
          <thead>
            <tr>
              <th style="width:60px">序号</th>
              <th style="min-width:140px">用户信息</th>
              <th style="min-width:180px">邮箱</th>
              <th style="width:100px">昵称</th>
              <th style="width:100px">角色</th>
              <th style="width:90px">状态</th>
              <th style="min-width:150px">部门</th>
              <th style="width:150px">最后登录时间</th>
              <th style="width:150px">注册时间</th>
              <th style="width:250px;text-align:center;position:sticky;right:0;background:var(--bg-header)">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in adminStore.users?.items ?? []" :key="row.id">
              <td><span class="seq-num">{{ getSequenceNumber(index) }}</span></td>
              <td>
                <div class="user-cell">
                  <span class="avatar" :class="row.role === 'ADMIN' ? 'avatar-admin' : ''">
                    {{ (row.nickname || row.username || '?').charAt(0).toUpperCase() }}
                  </span>
                  <span class="user-name">{{ row.username }}</span>
                </div>
              </td>
              <td><span class="email-text">{{ row.email }}</span></td>
              <td><span class="nick-text">{{ row.nickname || '-' }}</span></td>
              <td>
                <span class="tag" :class="row.role === 'ADMIN' ? 'tag-info' : 'tag-default'">
                  {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
                </span>
              </td>
              <td>
                <span class="status" :class="row.status === 1 ? 'status-on' : 'status-off'">
                  <i class="status-dot"></i>
                  {{ row.status === 1 ? '正常' : '已禁用' }}
                </span>
              </td>
              <td><span class="dept-text">{{ row.department || '-' }}</span></td>
              <td><span class="time">{{ formatTime(row.lastLoginAt) }}</span></td>
              <td><span class="time">{{ formatTime(row.createdAt) }}</span></td>
              <td class="actions-cell">
                <div class="actions">
                  <button
                    class="btn-sm"
                    :class="row.status === 1 ? 'btn-sm-warn' : 'btn-sm-ok'"
                    @click="handleToggleStatus(row)"
                  >
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </button>
                  <button
                    class="btn-sm"
                    :class="row.role === 'ADMIN' ? 'btn-sm-warn' : 'btn-sm-primary'"
                    @click="handleToggleRole(row)"
                  >
                    {{ row.role === 'ADMIN' ? '降级' : '升级' }}
                  </button>
                  <button
                    class="btn-sm btn-sm-danger"
                    :disabled="row.role === 'ADMIN'"
                    @click="handleDelete(row)"
                  >
                    删除
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!adminStore.loading && (!adminStore.users?.items?.length)">
              <td colspan="10" class="empty-cell">
                <div class="empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="40" height="40">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <line x1="17" y1="11" x2="23" y2="11"/>
                  </svg>
                  <span>暂无用户数据</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="footer" v-if="adminStore.users?.total">
        <span class="footer-info">
          第 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, adminStore.users?.total ?? 0) }} 条，共 {{ adminStore.users?.total ?? 0 }} 条
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/store/admin'
import type { AdminUser } from '@/types/admin'

const adminStore = useAdminStore()
const keyword = ref('')
const currentPage = ref(1)
const pageSize = 10

const totalPages = computed(() => Math.ceil((adminStore.users?.total ?? 0) / pageSize))

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

const formatTime = (t?: string) => t ? new Date(t).toLocaleString('zh-CN') : '-'

const getSequenceNumber = (index: number) => {
  return (currentPage.value - 1) * pageSize + index + 1
}

const fetchData = () => adminStore.fetchUsers(currentPage.value, pageSize, keyword.value || undefined)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleClearSearch = () => { keyword.value = ''; handleSearch() }
const handlePageChange = (p: number) => { currentPage.value = p; fetchData() }

const handleToggleStatus = async (row: AdminUser) => {
  const next = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定要${next === 0 ? '禁用' : '启用'}用户 "${row.username}" 吗？`, '确认操作', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await adminStore.updateUserStatus(row.id, next)
    ElMessage.success(`已${next === 0 ? '禁用' : '启用'}`)
    fetchData()
  } catch {}
}

const handleToggleRole = async (row: AdminUser) => {
  const next = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  try {
    await ElMessageBox.confirm(`确定要将 "${row.username}" ${next === 'ADMIN' ? '设为管理员' : '降为普通用户'}吗？`, '确认操作', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await adminStore.updateUserRole(row.id, next)
    ElMessage.success('已更新')
    fetchData()
  } catch {}
}

const handleDelete = async (row: AdminUser) => {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.username}" 吗？不可恢复！`, '危险操作', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'error'
    })
    await adminStore.deleteUser(row.id)
    ElMessage.success('已删除')
    fetchData()
  } catch {}
}

onMounted(() => fetchData())
</script>

<style scoped>
/* ===== 颜色变量 ===== */
.user-manage {
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

/* ===== 用户信息 ===== */
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
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
.avatar-admin {
  background: var(--c-brand-light);
  color: var(--c-brand);
}

.user-name { font-size: 13px; font-weight: 600; color: var(--text-primary); }

.email-text { font-size: 13px; color: var(--text-secondary); }

.nick-text { font-size: 13px; color: var(--text-primary); }

.dept-text { font-size: 13px; color: var(--text-secondary); }

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
.status-on .status-dot { background: var(--c-green); }
.status-on { color: var(--c-green); }
.status-off .status-dot { background: var(--c-red); }
.status-off { color: var(--c-red); }

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

.btn-sm-ok { color: var(--c-green); border-color: #bbf7d0; background: var(--c-green-bg); }
.btn-sm-ok:hover { background: #dcfce7; border-color: #86efac; }

.btn-sm-warn { color: var(--c-amber); border-color: #fde68a; background: var(--c-amber-bg); }
.btn-sm-warn:hover { background: #fef3c7; border-color: #fcd34d; }

.btn-sm-danger { color: var(--c-red); border-color: #fecaca; background: var(--c-red-bg); }
.btn-sm-danger:hover:not(:disabled) { background: #fee2e2; border-color: #fca5a5; }
.btn-sm:disabled { opacity: .4; cursor: not-allowed; }

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
@media (max-width: 767px) {
  .toolbar { flex-wrap: wrap; }
  .search-wrap { max-width: none; }
  .toolbar-total { margin-left: 0; width: 100%; }
  .footer { flex-direction: column; gap: 10px; align-items: flex-start; }
}
</style>