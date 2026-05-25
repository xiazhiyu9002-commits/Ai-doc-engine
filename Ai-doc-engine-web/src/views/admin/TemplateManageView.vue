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
              <th style="min-width:200px">模板描述</th>
              <th style="width:100px">类型</th>
              <th style="width:120px">创建者</th>
              <th style="width:100px">使用次数</th>
              <th style="width:90px">状态</th>
              <th style="width:90px">公开</th>
              <th style="width:170px">创建时间</th>
              <th style="width:170px">更新时间</th>
              <th style="width:260px;text-align:center;position:sticky;right:0;background:var(--bg-header)">操作</th>
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
              <td><span class="desc-text" :title="row.description">{{ row.description || '-' }}</span></td>
              <td>
                <span class="tag tag-type">{{ getTypeText(row.templateType) }}</span>
              </td>
              <td><span class="creator-text">{{ row.creatorName }}</span></td>
              <td><span class="count-text">{{ row.usageCount }}</span></td>
              <td>
                <span class="status" :class="row.status === 1 ? 'status-on' : 'status-off'">
                  <i class="status-dot"></i>
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td>
                <span class="status" :class="row.isPublic ? 'status-on' : 'status-off'">
                  <i class="status-dot"></i>
                  {{ row.isPublic ? '公开' : '私有' }}
                </span>
              </td>
              <td><span class="time">{{ formatTime(row.createdAt) }}</span></td>
              <td><span class="time">{{ formatTime(row.updatedAt) }}</span></td>
              <td class="actions-cell">
                <div class="actions">
                  <button class="btn-sm btn-sm-view" @click="handleView(row)">查看</button>
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
              <td colspan="11" class="empty-cell">
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
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑模板' : '新建模板'" width="750px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入模板名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="模板描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入模板描述" maxlength="500" />
        </el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="formData.templateType" placeholder="请选择类型" style="width: 100%">
            <el-option label="自定义" value="custom" />
            <el-option label="系统" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否公开" prop="isPublic">
          <el-switch v-model="formData.isPublic" />
          <span class="status-label">{{ formData.isPublic ? '公开' : '私有' }}</span>
        </el-form-item>
        <el-form-item label="模板内容" prop="configJson">
          <el-input
            v-model="formData.configJson"
            type="textarea"
            :rows="12"
            placeholder="请输入模板配置JSON（可为空，将使用默认配置）"
            class="config-textarea"
          />
          <div class="config-hint">留空将使用默认模板配置，输入需为有效的JSON格式</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="模板详情" width="700px" destroy-on-close>
      <div class="view-detail" v-if="viewData">
        <div class="view-row">
          <span class="view-label">模板ID</span>
          <span class="view-value">{{ viewData.id }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">模板名称</span>
          <span class="view-value">{{ viewData.name }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">模板描述</span>
          <span class="view-value">{{ viewData.description || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">模板类型</span>
          <span class="view-value">{{ getTypeText(viewData.templateType) }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">创建者ID</span>
          <span class="view-value">{{ viewData.creatorId }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">创建者名称</span>
          <span class="view-value">{{ viewData.creatorName || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">使用次数</span>
          <span class="view-value">{{ viewData.usageCount }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">状态</span>
          <span class="view-value">
            <span class="status" :class="viewData.status === 1 ? 'status-on' : 'status-off'">
              <i class="status-dot"></i>
              {{ viewData.status === 1 ? '启用' : '禁用' }}
            </span>
          </span>
        </div>
        <div class="view-row">
          <span class="view-label">是否默认</span>
          <span class="view-value">{{ viewData.isDefault === 1 ? '是' : '否' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">是否公开</span>
          <span class="view-value">{{ viewData.isPublic ? '是' : '否' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">创建时间</span>
          <span class="view-value">{{ formatTime(viewData.createdAt) }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">更新时间</span>
          <span class="view-value">{{ formatTime(viewData.updatedAt) }}</span>
        </div>
        <div class="view-row" v-if="viewData.config">
          <span class="view-label">模板配置</span>
          <div class="view-config">
            <div class="config-section" v-if="viewData.config.fontSettings">
              <div class="config-title">字体设置</div>
              <div class="config-item">字体: {{ viewData.config.fontSettings.fontFamily || '-' }}</div>
              <div class="config-item">字号: {{ viewData.config.fontSettings.fontSize || '-' }}</div>
              <div class="config-item">代码字体: {{ viewData.config.fontSettings.codeFontFamily || '-' }}</div>
              <div class="config-item">代码字号: {{ viewData.config.fontSettings.codeFontSize || '-' }}</div>
            </div>
            <div class="config-section" v-if="viewData.config.pageSettings">
              <div class="config-title">页面设置</div>
              <div class="config-item">纸张大小: {{ viewData.config.pageSettings.pageSize || '-' }}</div>
              <div class="config-item">方向: {{ viewData.config.pageSettings.orientation === 'portrait' ? '纵向' : '横向' }}</div>
              <div class="config-item" v-if="viewData.config.pageSettings.margins">
                边距: 上{{ viewData.config.pageSettings.margins.top }}cm / 下{{ viewData.config.pageSettings.margins.bottom }}cm / 左{{ viewData.config.pageSettings.margins.left }}cm / 右{{ viewData.config.pageSettings.margins.right }}cm
              </div>
            </div>
            <div class="config-section" v-if="viewData.config.paragraphSettings">
              <div class="config-title">段落设置</div>
              <div class="config-item">行距: {{ viewData.config.paragraphSettings.lineSpacing || '-' }}</div>
              <div class="config-item">首行缩进: {{ viewData.config.paragraphSettings.firstLineIndent || 0 }} 字符</div>
            </div>
            <div class="config-section" v-if="viewData.config.headerFooterSettings">
              <div class="config-title">页眉页脚</div>
              <div class="config-item">页眉: {{ viewData.config.headerFooterSettings.header || '-' }}</div>
              <div class="config-item">页脚: {{ viewData.config.headerFooterSettings.footer || '-' }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
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
const viewVisible = ref(false)
const viewData = ref<Template | null>(null)

const formData = reactive({
  name: '',
  templateType: 'custom',
  description: '',
  isPublic: true,
  configJson: ''
})

const defaultTemplateConfig = {
  fontSettings: {
    fontSize: 12,
    fontFamily: '宋体',
    codeFontSize: 10,
    headingFonts: {
      h1: { bold: true, size: 22, family: '黑体' },
      h2: { bold: true, size: 18, family: '黑体' },
      h3: { bold: true, size: 16, family: '黑体' },
      h4: { bold: true, size: 14, family: '黑体' },
      h5: { bold: true, size: 12, family: '黑体' },
      h6: { bold: true, size: 12, family: '黑体' }
    },
    codeFontFamily: 'Consolas'
  },
  pageSettings: {
    margins: { top: 2.5, left: 2.8, right: 2.5, bottom: 2.5, gutter: 0.5 },
    pageSize: 'A4',
    orientation: 'portrait'
  },
  paragraphSettings: {
    alignment: 'left',
    lineSpacing: 1.5,
    firstLineIndent: 2,
    paragraphSpacing: { after: 0, before: 0 }
  },
  headerFooterSettings: {
    footer: '第 {page} 页，共 {total} 页',
    header: '论文标题',
    footerHeight: 1.5,
    headerHeight: 1.5,
    oddEvenDifferent: false,
    firstPageDifferent: true
  }
}

const formRules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }]
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
  formData.templateType = 'custom'
  formData.description = ''
  formData.isPublic = true
  formData.configJson = ''
  currentId.value = null
  isEdit.value = false
}

const handleCreate = () => {
  resetForm()
  formVisible.value = true
}

const handleView = (row: Template) => {
  viewData.value = row
  viewVisible.value = true
}

const handleEdit = (row: Template) => {
  resetForm()
  isEdit.value = true
  currentId.value = row.id
  formData.name = row.name
  formData.templateType = row.templateType || 'custom'
  formData.description = row.description || ''
  formData.isPublic = row.isPublic ?? true
  formData.configJson = row.config ? JSON.stringify(row.config, null, 2) : JSON.stringify(defaultTemplateConfig, null, 2)
  formVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    let config
    if (formData.configJson && formData.configJson.trim()) {
      try {
        config = JSON.parse(formData.configJson)
      } catch (e) {
        ElMessage.error('模板内容JSON格式错误，请检查')
        return
      }
    } else {
      config = defaultTemplateConfig
    }
    
    submitting.value = true
    try {
      if (isEdit.value && currentId.value) {
        await adminStore.updateTemplate(currentId.value, {
          name: formData.name,
          description: formData.description,
          config: config,
          isPublic: formData.isPublic
        })
        ElMessage.success('更新成功')
      } else {
        await adminStore.createTemplate({
          name: formData.name,
          description: formData.description,
          templateType: formData.templateType,
          config: config,
          isPublic: formData.isPublic
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
  min-width: 1500px;
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
.desc-text { 
  font-size: 13px; 
  color: var(--text-secondary); 
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
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

.btn-sm-view { color: var(--c-purple); border-color: #ddd6fe; background: var(--c-purple-bg); }
.btn-sm-view:hover { background: #ede9fe; border-color: #c4b5fd; }

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

/* ===== 查看弹窗样式 ===== */
.view-detail {
  padding: 8px 0;
}

.view-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
}

.view-row:last-child {
  border-bottom: none;
}

.view-label {
  flex-shrink: 0;
  width: 100px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.view-value {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  word-break: break-all;
}

.view-config {
  flex: 1;
  background: #f9fafb;
  border-radius: 6px;
  padding: 12px;
}

.config-section {
  margin-bottom: 12px;
}

.config-section:last-child {
  margin-bottom: 0;
}

.config-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px dashed var(--border-normal);
}

.config-item {
  font-size: 13px;
  color: var(--text-secondary);
  padding: 4px 0;
}

.config-textarea :deep(textarea) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
}

.config-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}
</style>
