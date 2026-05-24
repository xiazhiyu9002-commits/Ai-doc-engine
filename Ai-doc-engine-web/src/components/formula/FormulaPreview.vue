<template>
  <div class="formula-preview" :class="{ 'inline-mode': inline }">
    <div v-if="error" class="error-message">
      <el-icon color="#f56c6c"><CircleClose /></el-icon>
      <span>{{ error }}</span>
    </div>
    <div v-else ref="previewRef" class="preview-content"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { CircleClose } from '@element-plus/icons-vue'
import 'katex/dist/katex.min.css'
import { FormulaCacheService } from '@/utils/formulaCache'

const props = withDefaults(defineProps<{
  latex: string
  inline?: boolean
}>(), {
  inline: false
})

const previewRef = ref<HTMLElement>()
const error = ref('')

const renderedHtml = computed(() => {
  if (!props.latex) return ''
  try {
    return FormulaCacheService.render(props.latex, props.inline || false)
  } catch (e: any) {
    error.value = e.message || '公式渲染失败'
    return ''
  }
})

watch(renderedHtml, (html) => {
  if (previewRef.value) {
    if (html) {
      previewRef.value.innerHTML = html
      error.value = ''
    } else {
      // 清空预览内容
      previewRef.value.innerHTML = ''
      error.value = ''
    }
  }
})

// 监听 latex 变化，确保空值时立即清空
watch(() => props.latex, (newLatex) => {
  if (!newLatex && previewRef.value) {
    previewRef.value.innerHTML = ''
    error.value = ''
  }
})

onMounted(() => {
  if (previewRef.value && renderedHtml.value) {
    previewRef.value.innerHTML = renderedHtml.value
  }
})
</script>

<style scoped>
.formula-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 6px 0;
  padding: 0;
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
  min-height: auto;
}

.formula-preview.inline-mode {
  display: inline-flex;
  min-height: auto;
  padding: 0;
  background: transparent;
  border: none;
  vertical-align: middle;
  margin: 0;
  box-shadow: none;
}

.preview-content {
  font-size: 1em;
  overflow: auto;
  max-width: 100%;
  color: #1a1a1a;
  line-height: 1.2;
  width: 100%;
  text-align: center;
  resize: none;
  -webkit-user-drag: none;
  user-drag: none;
}

.preview-content * {
  resize: none !important;
  -webkit-user-drag: none !important;
  user-drag: none !important;
}

.preview-content :deep(.katex) {
  line-height: 1.2;
  display: inline-block;
}

.preview-content :deep(.katex-display) {
  margin: 0;
  padding: 0;
  width: 100%;
  text-align: center;
}

.preview-content :deep(.katex-display > .katex) {
  margin: 0 auto;
  padding: 0;
  display: inline-block;
  text-align: center;
}

.inline-mode .preview-content {
  font-size: 1em;
  color: inherit;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
  font-size: 14px;
}
</style>
