<template>
  <div class="content-input">
    <div class="input-header">
      <div class="header-left">
        <svg class="panel-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <h3>Markdown 输入</h3>
        <el-tag type="info" size="small">仅支持 Markdown</el-tag>
        <span class="char-count">当前字符数：{{ charCount }}</span>
      </div>
      <div class="header-right">
        <button class="action-btn action-btn--danger" @click="$emit('clear')">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M3 6H5H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>清空</span>
        </button>
        <input
          ref="fileInputRef"
          type="file"
          accept=".md,.markdown,.txt,text/markdown,text/plain"
          style="display: none"
          @change="handleFileChange"
        />
        <button class="action-btn action-btn--secondary" @click="triggerFileSelect">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M17 8L12 3L7 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>导入</span>
        </button>
        <button class="action-btn action-btn--primary" @click="handleParse" :disabled="loading">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 5H7C6.46957 5 5.96086 5.21071 5.58579 5.58579C5.21071 5.96086 5 6.46957 5 7V19C5 19.5304 5.21071 20.0391 5.58579 20.4142C5.96086 20.7893 6.46957 21 7 21H17C17.5304 21 18.0391 20.7893 18.4142 20.4142C18.7893 20.0391 19 19.5304 19 19V7C19 6.46957 18.7893 5.96086 18.4142 5.58579C18.0391 5.21071 17.5304 5 17 5H15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 5C9 4.46957 9.21071 3.96086 9.58579 3.58579C9.96086 3.21071 10.4696 3 11 3H13C13.5304 3 14.0391 3.21071 14.4142 3.58579C14.7893 3.96086 15 4.46957 15 5C15 5.53043 14.7893 6.03914 14.4142 6.41421C14.0391 6.78929 13.5304 7 13 7H11C10.4696 7 9.96086 6.78929 9.58579 6.41421C9.21071 6.03914 9 5.53043 9 5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span v-if="loading">解析中...</span>
          <span v-else>解析</span>
        </button>
      </div>
    </div>
    <div class="input-body">
      <el-input
        ref="textareaRef"
        v-model="content"
        type="textarea"
        placeholder="请输入 Markdown 内容..."
        @input="handleInput"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { throttle, getLineHeight, type ScrollData } from '@/utils/scroll'
import { formatMathFormulas, hasBlockFormulas } from '@/utils/markdownFormatter'

const props = defineProps<{
  modelValue: string
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'scroll', data: ScrollData): void
  (e: 'parse'): void
  (e: 'clear'): void
  (e: 'selectionChange', range: { startOffset: number; endOffset: number } | null): void
}>()

const content = ref(props.modelValue)
const textareaRef = ref<any>(null)
let textareaElement: HTMLTextAreaElement | null = null

const fileInputRef = ref<HTMLInputElement | null>(null)

// 计算字符数
const charCount = computed(() => content.value.length)

// 保存滚动位置（用于独立滚动模式）
const savedScrollTop = ref<number>(0)

// 缓存的滚动数据
let cachedScrollData: ScrollData | null = null

watch(() => props.modelValue, (newVal) => {
  content.value = newVal
})

const handleInput = () => {
  emit('update:modelValue', content.value)
}

const handleParse = () => {
  emit('parse')
}

const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

const handleFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = () => {
    let text = typeof reader.result === 'string' ? reader.result : ''
    
    if (hasBlockFormulas(text)) {
      text = formatMathFormulas(text)
    }
    
    content.value = text
    handleInput()
    if (textareaElement) {
      textareaElement.focus()
      textareaElement.setSelectionRange(0, 0)
      textareaElement.scrollTop = 0
    }
  }
  reader.readAsText(file)

  input.value = ''
}

/**
 * 计算精确的可见行号
 * 考虑行高和滚动位置的精确计算
 */
const calculateVisibleLine = (): number => {
  if (!textareaElement || !content.value) return 1
  
  const lineHeight = getLineHeight(textareaElement)
  const { scrollTop } = textareaElement
  
  // 计算可见区域的第一行
  const visibleTopLine = Math.floor(scrollTop / lineHeight) + 1
  
  // 确保行号在有效范围内
  const totalLines = content.value.split('\n').length
  return Math.max(1, Math.min(visibleTopLine, totalLines))
}

// 使用节流处理滚动事件（100ms）
const handleScroll = throttle(() => {
  if (textareaElement) {
    // 计算当前可见的第一行行号
    const firstVisibleLine = calculateVisibleLine()
    
    cachedScrollData = {
      scrollTop: textareaElement.scrollTop,
      scrollHeight: textareaElement.scrollHeight,
      clientHeight: textareaElement.clientHeight,
      firstVisibleLine,
      visibleStartRatio: textareaElement.scrollTop / (textareaElement.scrollHeight - textareaElement.clientHeight || 1),
      visibleEndRatio: (textareaElement.scrollTop + textareaElement.clientHeight) / (textareaElement.scrollHeight || 1)
    }
    
    emit('scroll', cachedScrollData)
  }
}, 100)

// 暴露方法供父组件调用
const scrollToPercentage = (percentage: number) => {
  if (textareaElement) {
    const maxScroll = textareaElement.scrollHeight - textareaElement.clientHeight
    if (maxScroll > 0) {
      const targetScroll = maxScroll * percentage
      // 使用平滑滚动
      textareaElement.scrollTo({
        top: targetScroll,
        behavior: 'smooth'
      })
    }
  }
}

// 滚动到指定行号（改进版）
const scrollToLine = (lineNumber: number) => {
  if (!textareaElement || !content.value) return
  
  const lineHeight = getLineHeight(textareaElement)
  const targetScrollTop = (lineNumber - 1) * lineHeight
  const maxScroll = textareaElement.scrollHeight - textareaElement.clientHeight
  
  const finalScroll = Math.min(targetScrollTop, maxScroll)
  
  // 使用平滑滚动
  textareaElement.scrollTo({
    top: finalScroll,
    behavior: 'smooth'
  })
}

// 滚动到指定行号并考虑偏移比例（0-1）
const scrollToLineWithOffset = (lineNumber: number, offsetRatio: number = 0) => {
  if (!textareaElement || !content.value) return
  
  const lineHeight = getLineHeight(textareaElement)
  const lines = content.value.split('\n')
  const totalLines = lines.length
  
  // 确保行号在有效范围内
  const validLine = Math.max(1, Math.min(lineNumber, totalLines))
  
  // 计算目标滚动位置
  const baseScrollTop = (validLine - 1) * lineHeight
  const offsetScroll = offsetRatio * lineHeight
  const targetScrollTop = baseScrollTop + offsetScroll
  
  const maxScroll = textareaElement.scrollHeight - textareaElement.clientHeight
  const finalScroll = Math.min(Math.max(0, targetScrollTop), maxScroll)
  
  // 使用平滑滚动
  textareaElement.scrollTo({
    top: finalScroll,
    behavior: 'smooth'
  })
}

/**
 * 滚动到指定像素位置
 * @param pixelTop 目标像素位置
 * @param behavior 滚动行为 ('smooth' | 'auto')
 */
const scrollToPixel = (pixelTop: number, behavior: ScrollBehavior = 'smooth') => {
  if (!textareaElement) return
  
  const maxScroll = textareaElement.scrollHeight - textareaElement.clientHeight
  const finalScroll = Math.max(0, Math.min(pixelTop, maxScroll))
  
  textareaElement.scrollTo({
    top: finalScroll,
    behavior
  })
}

/**
 * 获取 textarea 元素
 * @returns textarea HTML元素
 */
const getTextareaElement = (): HTMLTextAreaElement | null => {
  return textareaElement
}

/**
 * 获取滚动容器元素（与 getTextareaElement 相同，为了接口一致性）
 * @returns 滚动容器 HTML元素
 */
const getScrollContainer = (): HTMLTextAreaElement | null => {
  return textareaElement
}

// 保存当前滚动位置
const saveScrollPosition = () => {
  if (textareaElement) {
    savedScrollTop.value = textareaElement.scrollTop
  }
}

// 恢复滚动位置
const restoreScrollPosition = () => {
  if (textareaElement) {
    textareaElement.scrollTop = savedScrollTop.value
  }
}

// 获取当前滚动数据
const getScrollData = (): ScrollData | null => {
  if (textareaElement) {
    const firstVisibleLine = calculateVisibleLine()
    return {
      scrollTop: textareaElement.scrollTop,
      scrollHeight: textareaElement.scrollHeight,
      clientHeight: textareaElement.clientHeight,
      firstVisibleLine,
      visibleStartRatio: textareaElement.scrollTop / (textareaElement.scrollHeight - textareaElement.clientHeight || 1),
      visibleEndRatio: (textareaElement.scrollTop + textareaElement.clientHeight) / (textareaElement.scrollHeight || 1)
    }
  }
  return cachedScrollData
}

const insertTextAtCursor = (text: string) => {
  if (!textareaElement) return
  
  const startPos = textareaElement.selectionStart
  const endPos = textareaElement.selectionEnd
  const scrollTop = textareaElement.scrollTop // 记录滚动位置
  
  const beforeText = content.value.substring(0, startPos)
  const afterText = content.value.substring(endPos)
  
  content.value = beforeText + text + afterText
  handleInput()
  
  // 重新聚焦并恢复状态
  nextTick(() => {
    if (textareaElement) {
      textareaElement.focus()
      const newCursorPos = startPos + text.length
      textareaElement.setSelectionRange(newCursorPos, newCursorPos)
      textareaElement.scrollTop = scrollTop // 恢复滚动位置
    }
  })
}

const insertBlockAtCursor = (blockText: string) => {
  if (!textareaElement) return

  const startPos = textareaElement.selectionStart
  const endPos = textareaElement.selectionEnd
  const scrollTop = textareaElement.scrollTop

  const beforeText = content.value.substring(0, startPos)
  const afterText = content.value.substring(endPos)

  const prefix = beforeText.length > 0 && !beforeText.endsWith('\n') ? '\n' : ''
  const suffix = afterText.length > 0 && !afterText.startsWith('\n') ? '\n' : ''

  const normalizedBlock = `\n${blockText.replace(/^\n+|\n+$/g, '')}\n`
  const insertion = `${prefix}${normalizedBlock}${suffix}`

  const newValue = beforeText + insertion + afterText
  content.value = newValue
  handleInput()

  nextTick(() => {
    if (textareaElement) {
      textareaElement.focus()
      const newCursorPos = beforeText.length + insertion.length
      textareaElement.setSelectionRange(newCursorPos, newCursorPos)
      textareaElement.scrollTop = scrollTop
    }
  })
}

const handleSelectionChange = () => {
  if (!textareaElement) return
  
  const startOffset = textareaElement.selectionStart
  const endOffset = textareaElement.selectionEnd
  
  if (startOffset !== endOffset) {
    emit('selectionChange', { startOffset, endOffset })
  } else {
    emit('selectionChange', null)
  }
}

const setSelection = (startOffset: number, endOffset: number) => {
  if (!textareaElement) return
  
  textareaElement.focus()
  textareaElement.setSelectionRange(startOffset, endOffset)
  
  const lineHeight = getLineHeight(textareaElement)
  const lines = content.value.substring(0, startOffset).split('\n')
  const lineNumber = lines.length
  
  const targetScrollTop = (lineNumber - 1) * lineHeight - textareaElement.clientHeight / 3
  const maxScroll = textareaElement.scrollHeight - textareaElement.clientHeight
  textareaElement.scrollTop = Math.max(0, Math.min(targetScrollTop, maxScroll))
}

const getSelection = (): { startOffset: number; endOffset: number } | null => {
  if (!textareaElement) return null
  
  const startOffset = textareaElement.selectionStart
  const endOffset = textareaElement.selectionEnd
  
  if (startOffset !== endOffset) {
    return { startOffset, endOffset }
  }
  return null
}

onMounted(() => {
  nextTick(() => {
    if (textareaRef.value && textareaRef.value.textarea) {
      textareaElement = textareaRef.value.textarea as HTMLTextAreaElement
      textareaElement.addEventListener('scroll', handleScroll, { passive: true })
      textareaElement.addEventListener('click', handleSelectionChange)
      textareaElement.addEventListener('keyup', handleSelectionChange)
      textareaElement.addEventListener('select', handleSelectionChange)
    }
  })
})

onBeforeUnmount(() => {
  if (textareaElement) {
    textareaElement.removeEventListener('scroll', handleScroll)
    textareaElement.removeEventListener('click', handleSelectionChange)
    textareaElement.removeEventListener('keyup', handleSelectionChange)
    textareaElement.removeEventListener('select', handleSelectionChange)
  }
})

defineExpose({
  scrollToPercentage,
  scrollToLine,
  scrollToLineWithOffset,
  scrollToPixel,
  getTextareaElement,
  getScrollContainer,
  saveScrollPosition,
  restoreScrollPosition,
  getScrollData,
  insertTextAtCursor,
  insertBlockAtCursor,
  setSelection,
  getSelection
})
</script>

<style scoped>
.content-input {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.input-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(to bottom, #f8fafc, #f1f5f9);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.char-count {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.panel-icon {
  width: 20px;
  height: 20px;
  color: #667eea;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.15s ease;
  background: white;
  color: #374151;
}

.action-btn .btn-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.action-btn--danger {
  color: #dc2626;
}

.action-btn--danger:hover {
  background: #fef2f2;
  border-color: #fecaca;
}

.action-btn--secondary:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.action-btn--primary {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.action-btn--primary:hover {
  background: #5a67d8;
  border-color: #5a67d8;
}

.action-btn--primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-body {
  flex: 1;
  padding: 16px;
  overflow: auto;
  display: flex;
  flex-direction: column;
  background: #f6f8fa;
}

:deep(.el-textarea) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.el-textarea__inner) {
  flex: 1;
  height: 100% !important;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  resize: none;
  padding: 16px;
  color: #24292e;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

:deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}
</style>
