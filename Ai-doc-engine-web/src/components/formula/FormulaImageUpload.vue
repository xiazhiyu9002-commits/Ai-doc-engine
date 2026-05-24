<template>
  <div class="formula-upload">
    <el-upload
      v-if="!recognized"
      class="upload-area"
      drag
      :auto-upload="false"
      :show-file-list="false"
      :disabled="loading"
      accept="image/*"
      :on-change="handleFileChange"
    >
      <div class="upload-content">
        <div class="upload-icon-wrapper">
          <svg class="upload-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M17 8L12 3L7 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="upload-text">
          <p class="upload-title">点击或拖拽上传公式截图</p>
          <p class="upload-hint">支持 JPG、PNG 格式，建议上传清晰的公式图片</p>
        </div>
      </div>
    </el-upload>

    <div v-if="previewUrl" class="preview-section">
      <div class="preview-header">
        <svg class="preview-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
          <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor"/>
          <path d="M21 15L16 10L5 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>图片预览</span>
      </div>
      <div class="preview-image-wrapper">
        <img :src="previewUrl" alt="公式截图" class="preview-image" />
      </div>
      <div class="preview-actions">
        <el-button
          v-if="!recognized"
          class="recognize-btn"
          :loading="loading"
          :disabled="loading"
          @click="handleUpload"
        >
          <svg v-if="!loading" class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 4H20L14 12L20 20H4L10 12L4 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>{{ loading ? '识别中...' : '开始识别' }}</span>
        </el-button>
        <el-tag v-else type="success" size="large">
          <svg class="success-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          识别完成
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { formulaApi } from '@/api/formula'
import type { FormulaOcrResponse } from '@/types/formula'
import type { UploadFile } from 'element-plus'

const emit = defineEmits<{
  (e: 'success', result: FormulaOcrResponse): void
}>()

const loading = ref(false)
const recognized = ref(false)
const previewUrl = ref('')
const currentFile = ref<File | null>(null)

const handleFileChange = (file: UploadFile) => {
  if (!file.raw) return
  
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  
  currentFile.value = file.raw
  previewUrl.value = URL.createObjectURL(file.raw)
  recognized.value = false
}

const handleUpload = async () => {
  if (!currentFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  loading.value = true
  try {
    const response = await formulaApi.ocrImage(currentFile.value)
    ElMessage.success('识别成功')
    recognized.value = true
    emit('success', response.data)
  } catch (error: any) {
    ElMessage.error(error.message || '识别失败')
  } finally {
    loading.value = false
  }
}

const reset = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = ''
  currentFile.value = null
  loading.value = false
  recognized.value = false
}

onBeforeUnmount(() => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
})

defineExpose({
  reset
})
</script>

<style scoped>
.formula-upload {
  padding: 0;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload) {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: #667eea;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.upload-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
}

.upload-icon {
  width: 32px;
  height: 32px;
  color: white;
}

.upload-text {
  text-align: center;
  width: 100%;
}

.upload-title {
  margin: 0 0 6px 0;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  text-align: center;
}

.upload-hint {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
  line-height: 1.5;
  text-align: center;
}

.preview-section {
  margin-top: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.preview-icon {
  width: 18px;
  height: 18px;
  color: #667eea;
}

.preview-image-wrapper {
  display: flex;
  justify-content: center;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.preview-image {
  max-width: 100%;
  max-height: 200px;
  border-radius: 6px;
  object-fit: contain;
}

.preview-actions {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.recognize-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 28px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.35);
  transition: all 0.2s ease;
}

.recognize-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.45);
}

.recognize-btn .btn-icon {
  width: 18px;
  height: 18px;
}
</style>


.recognize-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.recognize-btn:disabled:hover {
  transform: none;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.35);
}

.success-icon {
  width: 16px;
  height: 16px;
  margin-right: 6px;
}

.el-tag {
  display: inline-flex;
  align-items: center;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
}
