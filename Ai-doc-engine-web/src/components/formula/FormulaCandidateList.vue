<template>
  <div class="candidate-list">
    <div
      v-for="(candidate, index) in candidates"
      :key="index"
      class="candidate-item"
      @click="handleSelect(candidate.latex)"
    >
      <div class="candidate-info">
        <el-tag size="small" type="info" class="confidence-tag">
          <svg class="tag-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 11.08V12C21.9988 14.1564 21.3005 16.2547 20.0093 17.9818C18.7182 19.709 16.9033 20.9725 14.8354 21.5839C12.7674 22.1953 10.5573 22.1219 8.53447 21.3746C6.51168 20.6273 4.78465 19.2461 3.61096 17.4371C2.43727 15.628 1.87979 13.4881 2.02168 11.3363C2.16356 9.18455 2.99721 7.13631 4.39828 5.49706C5.79935 3.85781 7.69279 2.71537 9.79619 2.24013C11.8996 1.7649 14.1003 1.98232 16.07 2.85999" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ (candidate.confidence * 100).toFixed(1) }}%
        </el-tag>
        <code class="candidate-latex">{{ candidate.latex }}</code>
      </div>
      <div class="candidate-preview">
        <FormulaPreview :latex="candidate.latex" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import FormulaPreview from './FormulaPreview.vue'
import type { FormulaCandidate } from '@/types/formula'

defineProps<{
  candidates: FormulaCandidate[]
}>()

const emit = defineEmits<{
  (e: 'select', latex: string): void
}>()

const handleSelect = (latex: string) => {
  emit('select', latex)
}
</script>

<style scoped>
.candidate-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.candidate-item {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.candidate-item:hover {
  border-color: #667eea;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.candidate-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.confidence-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  flex-shrink: 0;
}

.tag-icon {
  width: 12px;
  height: 12px;
}

.candidate-latex {
  flex: 1;
  padding: 6px 10px;
  background: #f8fafc;
  border-radius: 6px;
  font-size: 12px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  color: #6b7280;
  overflow: auto;
  border: 1px solid #e5e7eb;
}

.candidate-preview {
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}
</style>
