<template>
  <div class="template-manage">
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
            placeholder="搜索模板名称"
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
        <select v-model="filterType" class="filter-select" @change="handleSearch">
          <option value="">全部类型</option>
          <option value="contract">合同</option>
          <option value="report">报告</option>
          <option value="invoice">发票</option>
          <option value="letter">信函</option>
          <option value="other">其他</option>
        </select>
        <select v-model="filterStatus" class="filter-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="1">启用</option>
          <option value="0">禁用</option>
        </select>
        <button class="btn btn-primary" @click="handleSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          搜索
        </button>
        <button class="btn btn-success" @click="handleCreate">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          新建模板
        </button>
        <span class="toolbar-stat">
          共 <strong>{{ adminStore.templates?.total ?? 0 }}</strong> 个模板
        </span>
      </div>

      <div class="table-wrap" v-loading="adminStore.loading">
        <table class="table">
          <thead>
            <tr>
              <th style="width:60px">序号</th>
              <th style="min-width:180px">模板名称</th>
              <th style="width:100px">类型</th>
              <th style="width:120px">创建者</th>
              <th style="width:100px">使用次数</th>
              <th style="width:90px">状态</th>
              <th style="width:170px">创建时间</th>
              <th style="width:170px">更新时间</th>
              <th style="width:220px;text-align:center;position:sticky;right:0;background:var(--bg-header)">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in adminStore.templates?.items ?? []" :key="row.id">
              <td>
                <span class="seq-num">
                  {{ getSequenceNumber(index) }}
                  <span v-if="row.isDefault === 1" class="default-badge">默认</span>
                </span>
              </td>
              <td><span class="template-name">{{ row.name }}</span></td>
              <td>
                <span class="tag tag-type">{{ getTypeText(row.type) }}</span>
              </td>
              <td><span class="creator-text">{{ row.creatorName }}</span></td>
              <td><span class="count-text">{{ row.usageCount }}</span></td>
              <td>
                <span class="status" :class="row.status === 1 ? 'status-on' : 'status-off'">
                  <i class="status-dot"></i>
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td><span class="time">{{ formatTime(row.createdAt) }}</span></td>
              <td><span class="time">{{ formatTime(row.updatedAt) }}</span></td>
              <td class="actions-cell">
                <div class="actions">
                  <button class="btn-sm btn-sm-primary" @click="handleEdit(row)">编辑</button>
                  <button
                    v-if="row.isDefault !== 1"
                    class="btn-sm btn-sm-info"
                    @click="handleSetDefault(row)"
                  >设为默认</button>
                  <button class="btn-sm btn-sm-warn" @click="handleCopy(row)">复制</button>
                  <button class="btn-sm btn-sm-danger" @click="handleDelete(row)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="!adminStore.loading && (!adminStore.templates?.items?.length)">
              <td colspan="9" class="empty-cell">
                <div class="empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="40" height="40">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                  <span>暂无模板数据</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="footer" v-if="adminStore.templates?.total">
        <span class="footer-info">
          第 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, adminStore.templates?.total ?? 0) }} 条，共 {{ adminStore.templates?.total ?? 0 }} 条
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

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑模板' : '新建模板'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入模板名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="模板类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="合同" value="contract" />
            <el-option label="报告" value="report" />
            <el-option label="发票" value="invoice" />
            <el-option label="信函" value="letter" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
          <span class="status-label">{{ formData.status === 1 ? '启用' : '禁用' }}</span>
        </el-form-item>
        <el-form-item label="模板内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="8"
            placeholder="请输入模板内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useAdminStore } from '@/store/admin'
import type { Template } from '@/types/admin'

const adminStore = useAdminStore()
const keyword = ref('')
const filterType = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = 10
const formVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const currentId = ref<number | null>(null)

const formData = reactive({
  name: '',
  type: '',
  status: 1,
  content: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择模板类型', trigger: 'change' }]
}

const totalPages = computed(() => Math.ceil((adminStore.templates?.total ?? 0) / pageSize))

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

const getSequenceNumber = (index: number) => {
  return (currentPage.value - 1) * pageSize + index + 1
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    system: '系统',
    contract: '合同',
    report: '报告',
    invoice: '发票',
    letter: '信函',
    other: '其他',
    custom: '自定义'
  }
  return map[type] || type || '系统'
}

const fetchData = () => {
  adminStore.fetchTemplates({
    page: currentPage.value,
    size: pageSize,
    keyword: keyword.value || undefined,
    type: filterType.value || undefined,
    status: filterStatus.value ? parseInt(filterStatus.value) : undefined
  })
}

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleClearSearch = () => { keyword.value = ''; handleSearch() }
const handlePageChange = (p: number) => { currentPage.value = p; fetchData() }

const resetForm = () => {
  formData.name = ''
  formData.type = ''
  formData.status = 1
  formData.content = ''
  currentId.value = null
  isEdit.value = false
}

const handleCreate = () => {
  resetForm()
  formVisible.value = true
}

const handleEdit = (row: Template) => {
  resetForm()
  isEdit.value = true
  currentId.value = row.id
  formData.name = row.name
  formData.type = row.type
  formData.status = row.status
  formData.content = row.content || ''
  formVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value && currentId.value) {
        await adminStore.updateTemplate(currentId.value, {
          name: formData.name,
          type: formData.type,
          status: formData.status,
          content: formData.content
        })
        ElMessage.success('更新成功')
      } else {
        await adminStore.createTemplate({
          name: formData.name,
          type: formData.type,
          status: formData.status,
          content: formData.content
        })
        ElMessage.success('创建成功')
      }
      formVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleSetDefault = async (row: Template) => {
  try {
    await ElMessageBox.confirm(`确定要将 "${row.name}" 设为默认模板吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await adminStore.setDefaultTemplate(row.id)
    ElMessage.success('已设为默认模板')
    fetchData()
  } catch {}
}

const handleCopy = async (row: Template) => {
  try {
    await ElMessageBox.confirm(`确定要复制模板 "${row.name}" 吗？`, '确认复制', {
      confirmButtonText: '复制',
      cancelButtonText: '取消',
      type: 'info'
    })
    await adminStore.copyTemplate(row.id)
    ElMessage.success('复制成功')
    fetchData()
  } catch {}
}

const handleDelete = async (row: Template) => {
  try {
    await ElMessageBox.confirm(`确定要删除模板 "${row.name}" 吗？不可恢复！`, '危险操作', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    await adminStore.deleteTemplate(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(() => fetchData())
</script>

<style scoped>
/* ===== 颜色变量 ===== */
.template-manage {
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
  --c-blue: #2563eb;
  --c-blue-bg: #eff6ff;
  --c-purple: #7c3aed;
  --c-purple-bg: #f5f3ff;
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
.btn-success { background: var(--c-green); color: #fff; }
.btn-success:hover { background: #15803d; }

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

/* ===== 序号 ===== */
.seq-num {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.default-badge {
  font-size: 10px;
  font-weight: 500;
  padding: 2px 6px;
  background: var(--c-purple-bg);
  color: var(--c-purple);
  border-radius: 3px;
}

.template-name { font-size: 13px; font-weight: 500; color: var(--text-primary); }
.creator-text { font-size: 13px; color: var(--text-secondary); }
.count-text { font-size: 13px; color: var(--text-secondary); }

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

.tag-type { background: var(--c-blue-bg); color: var(--c-blue); border-color: #bfdbfe; }

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
  gap: 6px;
  justify-content: center;
  flex-wrap: nowrap;
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

.btn-sm-info { color: var(--c-blue); border-color: #bfdbfe; background: var(--c-blue-bg); }
.btn-sm-info:hover { background: #dbeafe; border-color: #93c5fd; }

.btn-sm-warn { color: var(--c-amber); border-color: #fde68a; background: var(--c-amber-bg); }
.btn-sm-warn:hover { background: #fef3c7; border-color: #fcd34d; }

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

/* ===== 表单状态标签 ===== */
.status-label {
  margin-left: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}

/* ===== 响应式 ===== */
@media (max-width: 767px) {
  .toolbar { flex-wrap: wrap; }
  .search-wrap { max-width: none; }
  .footer { flex-direction: column; gap: 10px; align-items: flex-start; }
}
</style>
