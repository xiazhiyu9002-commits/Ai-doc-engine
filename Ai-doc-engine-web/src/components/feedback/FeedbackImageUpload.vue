<template>
  <div class="feedback-image-upload">
    <div class="upload-area">
      <div
        v-for="(image, index) in previewImages"
        :key="index"
        class="image-preview"
      >
        <img :src="image" alt="preview" />
        <button type="button" class="remove-btn" @click="removeImage(index)">
          &times;
        </button>
      </div>
      
      <label
        v-if="modelValue.length < maxCount"
        class="upload-btn"
        :class="{ disabled: uploading }"
      >
        <input
          type="file"
          accept="image/*"
          multiple
          @change="handleFileChange"
          :disabled="uploading"
          hidden
        />
        <span v-if="uploading">上传中...</span>
        <span v-else>
          <svg viewBox="0 0 24 24" width="24" height="24">
            <path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
          </svg>
          <span class="upload-text">添加图片</span>
        </span>
      </label>
    </div>
    
    <div class="upload-tips">
      支持 jpg、png、gif、webp 格式，单张最大 5MB，最多 {{ maxCount }} 张
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  modelValue: File[]
  maxCount?: number
}>()

const emit = defineEmits<{
  'update:modelValue': [files: File[]]
}>()

const uploading = ref(false)
const maxCount = props.maxCount || 5

const previewImages = computed(() => {
  return props.modelValue.map((file) => URL.createObjectURL(file))
})

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = Array.from(target.files || [])
  
  const validFiles: File[] = []
  const maxSize = 5 * 1024 * 1024
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  
  for (const file of files) {
    if (!allowedTypes.includes(file.type)) {
      alert(`不支持的文件类型: ${file.name}`)
      continue
    }
    if (file.size > maxSize) {
      alert(`文件过大: ${file.name}，最大允许 5MB`)
      continue
    }
    validFiles.push(file)
  }
  
  const remaining = maxCount - props.modelValue.length
  const filesToAdd = validFiles.slice(0, remaining)
  
  if (filesToAdd.length < validFiles.length) {
    alert(`最多上传 ${maxCount} 张图片，已自动选择前 ${filesToAdd.length} 张`)
  }
  
  emit('update:modelValue', [...props.modelValue, ...filesToAdd])
  target.value = ''
}

const removeImage = (index: number) => {
  const newFiles = [...props.modelValue]
  newFiles.splice(index, 1)
  emit('update:modelValue', newFiles)
}
</script>

<style scoped>
.feedback-image-upload {
  width: 100%;
}

.upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ddd;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.upload-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #999;
  transition: all 0.3s;
}

.upload-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.upload-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.upload-text {
  font-size: 12px;
  margin-top: 4px;
}

.upload-tips {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
