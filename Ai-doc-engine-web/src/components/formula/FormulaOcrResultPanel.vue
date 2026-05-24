<template>
  <div class="ocr-result-panel">
    <div class="result-divider">
      <div class="divider-line"></div>
      <div class="divider-content">
        <svg class="divider-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M4 4H20L14 12L20 20H4L10 12L4 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>识别结果</span>
      </div>
      <div class="divider-line"></div>
    </div>
    
    <div class="result-section">
      <div class="result-header">
        <div class="header-left">
          <svg class="header-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <h4>{{ contentTypeLabel }}</h4>
        </div>
        <el-tag :type="confidenceType" size="small" class="confidence-tag">
          <svg class="tag-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 11.08V12C21.9988 14.1564 21.3005 16.2547 20.0093 17.9818C18.7182 19.709 16.9033 20.9725 14.8354 21.5839C12.7674 22.1953 10.5573 22.1219 8.53447 21.3746C6.51168 20.6273 4.78465 19.2461 3.61096 17.4371C2.43727 15.628 1.87979 13.4881 2.02168 11.3363C2.16356 9.18455 2.99721 7.13631 4.39828 5.49706C5.79935 3.85781 7.69279 2.71537 9.79619 2.24013C11.8996 1.7649 14.1003 1.98232 16.07 2.85999" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          置信度: {{ (result.confidence * 100).toFixed(1) }}%
        </el-tag>
      </div>
      
      <el-input
        v-model="editableLatex"
        type="textarea"
        :rows="contentRows"
        :placeholder="contentPlaceholder"
        class="latex-input"
      />
      
      <div class="action-buttons">
        <el-button class="action-btn action-btn--validate" @click="handleValidate" :loading="validating">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 11.08V12C21.9988 14.1564 21.3005 16.2547 20.0093 17.9818C18.7182 19.709 16.9033 20.9725 14.8354 21.5839C12.7674 22.1953 10.5573 22.1219 8.53447 21.3746C6.51168 20.6273 4.78465 19.2461 3.61096 17.4371C2.43727 15.628 1.87979 13.4881 2.02168 11.3363C2.16356 9.18455 2.99721 7.13631 4.39828 5.49706C5.79935 3.85781 7.69279 2.71537 9.79619 2.24013C11.8996 1.7649 14.1003 1.98232 16.07 2.85999" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          校验公式
        </el-button>
        <el-button class="action-btn action-btn--insert" type="primary" @click="handleInsert">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          插入文档
        </el-button>
      </div>
    </div>

    <div class="preview-section">
      <div class="section-header">
        <svg class="section-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M4 4H20L14 12L20 20H4L10 12L4 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <h4>预览</h4>
      </div>
      <div class="preview-wrapper">
        <!-- 公式预览 -->
        <FormulaPreview v-if="contentType === 'formula'" :latex="editableLatex" />
        
        <!-- 流程图预览 -->
        <div v-else-if="contentType === 'flowchart'" class="mermaid-preview" v-html="mermaidHtml"></div>
        
        <!-- 表格预览 -->
        <div v-else-if="contentType === 'table'" class="table-preview">
          <table class="markdown-table">
            <tbody>
              <tr v-for="(row, rowIndex) in tableRows" :key="rowIndex">
                <component 
                  :is="rowIndex === 0 ? 'th' : 'td'" 
                  v-for="(cell, cellIndex) in row" 
                  :key="cellIndex"
                >
                  <FormulaPreview v-if="isLatexFormula(cell)" :latex="extractLatex(cell)" :inline="true" />
                  <span v-else>{{ cell }}</span>
                </component>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 其他类型 -->
        <div v-else class="text-preview">{{ editableLatex }}</div>
      </div>
    </div>

    <div v-if="result.candidates && result.candidates.length" class="candidates-section">
      <div class="section-header">
        <svg class="section-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M8 6L21 6.00001M8 12L21 12.00001M8 18L21 18.0001M3 6L3.01 6M3 12L3.01 12M3 18L3.01 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <h4>候选结果</h4>
        <span class="section-hint">点击切换</span>
      </div>
      <FormulaCandidateList
        :candidates="result.candidates"
        @select="handleSelectCandidate"
      />
    </div>

    <div class="info-section">
      <div class="info-card">
        <div class="info-header">
          <svg class="info-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span class="info-title">公式处理说明</span>
        </div>
        <ul class="info-list">
          <li>OCR 识别结果为 LaTeX 格式，可手动修改</li>
          <li>支持在候选结果中切换不同的识别结果</li>
          <li>确认后的公式将写入文档模型（UDM）</li>
          <li>导出时转换为 Word 原生公式（OMML）</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { formulaApi } from '@/api/formula'
import FormulaPreview from './FormulaPreview.vue'
import FormulaCandidateList from './FormulaCandidateList.vue'
import type { FormulaOcrResponse } from '@/types/formula'

const props = defineProps<{
  result: FormulaOcrResponse
}>()

const emit = defineEmits<{
  (e: 'insert', latex: string): void
}>()

const editableLatex = ref(props.result.latex)
const validating = ref(false)

const contentType = computed(() => props.result.contentType || 'formula')

const contentTypeLabel = computed(() => {
  const type = contentType.value
  if (type === 'flowchart') return 'Mermaid 流程图'
  if (type === 'table') return 'Markdown 表格'
  return 'LaTeX 公式'
})

const contentRows = computed(() => {
  const type = contentType.value
  return type === 'flowchart' ? 8 : type === 'table' ? 6 : 3
})

const contentPlaceholder = computed(() => {
  const type = contentType.value
  if (type === 'flowchart') return 'Mermaid 流程图代码'
  if (type === 'table') return 'Markdown 表格'
  return 'LaTeX 公式'
})

// Mermaid预览HTML
const mermaidHtml = computed(() => {
  // 这里简单显示文本，实际渲染由Mermaid库完成
  return `<pre class="mermaid-code">${editableLatex.value}</pre>`
})

// 解析表格行
const tableRows = computed(() => {
  try {
    const lines = editableLatex.value.trim().split('\n')
    const rows: string[][] = []
    
    for (const line of lines) {
      const trimmed = line.trim()
      // 跳过空行和分隔线
      if (!trimmed || trimmed.match(/^\|?\s*[-:]+\s*(\|\s*[-:]+\s*)*\|?$/)) {
        continue
      }
      
      // 解析单元格
      const cells = trimmed.split('|').map(cell => cell.trim()).filter(cell => cell)
      if (cells.length > 0) {
        rows.push(cells)
      }
    }
    
    return rows
  } catch (e) {
    return []
  }
})

// 判断是否为LaTeX公式
const isLatexFormula = (text: string): boolean => {
  if (!text) return false
  const trimmed = text.trim()
  // 检测 $...$ 或 $$...$$ 格式
  return (trimmed.startsWith('$') && trimmed.endsWith('$')) || 
         (trimmed.includes('\\') && (trimmed.includes('{') || trimmed.includes('frac') || trimmed.includes('sum')))
}

// 提取LaTeX公式（去掉$符号）
const extractLatex = (text: string): string => {
  if (!text) return ''
  let result = text.trim()
  // 去掉外层的 $ 或 $$
  if (result.startsWith('$$') && result.endsWith('$$')) {
    result = result.slice(2, -2)
  } else if (result.startsWith('$') && result.endsWith('$')) {
    result = result.slice(1, -1)
  }
  return result.trim()
}

const confidenceType = computed(() => {
  const confidence = props.result.confidence
  if (confidence >= 0.9) return 'success'
  if (confidence >= 0.7) return 'warning'
  return 'danger'
})

const handleValidate = async () => {
  if (!editableLatex.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  
  // 只对公式进行校验
  const type = props.result.contentType || 'formula'
  if (type !== 'formula') {
    ElMessage.info('流程图和表格无需校验，可直接插入')
    return
  }
  
  validating.value = true
  try {
    const response = await formulaApi.validate({ latex: editableLatex.value })
    if (response.data.valid) {
      ElMessage.success('LaTeX 格式正确，可转换为 OMML')
    } else {
      ElMessage.error(`LaTeX 格式错误: ${response.data.error}`)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '校验失败')
  } finally {
    validating.value = false
  }
}

const handleInsert = () => {
  if (!editableLatex.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  
  emit('insert', editableLatex.value)
}

const handleSelectCandidate = (latex: string) => {
  editableLatex.value = latex
  ElMessage.success('已切换到候选公式')
}
</script>

<style scoped>
.ocr-result-panel {
  padding: 0;
}

.result-divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e5e7eb, transparent);
}

.divider-content {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  font-size: 14px;
  font-weight: 600;
}

.divider-icon {
  width: 18px;
  height: 18px;
}

.result-section {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  margin-bottom: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  width: 20px;
  height: 20px;
  color: #667eea;
}

.result-header h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

.confidence-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 6px;
}

.tag-icon {
  width: 14px;
  height: 14px;
}

.latex-input :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: white;
  transition: all 0.2s ease;
}

.latex-input :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 38px;
  padding: 0 18px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.action-btn .btn-icon {
  width: 16px;
  height: 16px;
}

.action-btn--validate {
  background: white;
  border: 1px solid #e5e7eb;
  color: #374151;
}

.action-btn--validate:hover {
  background: #f9fafb;
  border-color: #667eea;
  color: #667eea;
}

.action-btn--insert {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.action-btn--insert:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.preview-section,
.candidates-section {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-icon {
  width: 18px;
  height: 18px;
  color: #667eea;
}

.section-header h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

.section-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-left: auto;
}

.preview-wrapper {
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  overflow-x: auto;
}

.mermaid-preview {
  min-height: 100px;
}

.mermaid-code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.table-preview :deep(.markdown-table) {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table-preview :deep(.markdown-table th),
.table-preview :deep(.markdown-table td) {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  text-align: left;
}

.table-preview :deep(.markdown-table th) {
  background: #f3f4f6;
  font-weight: 600;
  color: #374151;
}

.table-preview :deep(.markdown-table tr:nth-child(even)) {
  background: #fafafa;
}

.text-preview {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
  white-space: pre-wrap;
  word-break: break-word;
}

.info-section {
  margin-top: 20px;
}

.info-card {
  padding: 16px;
  background: linear-gradient(135deg, #eff6ff 0%, #e0e7ff 100%);
  border-radius: 10px;
  border: 1px solid #bfdbfe;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.info-icon {
  width: 18px;
  height: 18px;
  color: #3b82f6;
}

.info-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e40af;
}

.info-list {
  margin: 0;
  padding-left: 20px;
}

.info-list li {
  margin: 6px 0;
  line-height: 1.6;
  font-size: 13px;
  color: #374151;
}
</style>
