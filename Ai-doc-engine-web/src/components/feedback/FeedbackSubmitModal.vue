<template>
  <div class="feedback-submit-modal">
    <div class="modal-overlay" @click="$emit('close')">
      <div class="modal-content" @click.stop>
        <button class="close-btn" @click="$emit('close')">&times;</button>
        
        <form @submit.prevent="handleSubmit" class="feedback-form">
          <div class="form-group">
            <label>反馈类型</label>
            <select v-model="form.feedbackType" required>
              <option value="suggestion">建议</option>
              <option value="bug">问题反馈</option>
              <option value="feature">功能请求</option>
              <option value="other">其他</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>详细描述</label>
            <textarea
              v-model="form.content"
              placeholder="请详细描述您的反馈内容..."
              maxlength="5000"
              rows="6"
              required
            ></textarea>
            <div class="char-count">{{ form.content.length }}/5000</div>
          </div>
          
          <div class="form-group">
            <label>上传图片（可选，最多5张）</label>
            <FeedbackImageUpload v-model="images" :max-count="5" />
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="$emit('close')">取消</button>
            <button type="submit" class="btn-submit" :disabled="loading">
              {{ loading ? '提交中...' : '提交反馈' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { feedbackApi } from '@/api/feedback'
import type { FeedbackType } from '@/types/feedback'
import FeedbackImageUpload from './FeedbackImageUpload.vue'

const emit = defineEmits<{
  close: []
  submitted: []
}>()

const loading = ref(false)
const images = ref<File[]>([])

const form = reactive({
  content: '',
  feedbackType: 'suggestion' as FeedbackType
})

const handleSubmit = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    await feedbackApi.submitFeedback(
      {
        content: form.content,
        feedbackType: form.feedbackType
      },
      images.value
    )
    
    alert('反馈提交成功！感谢您的宝贵意见。')
    emit('submitted')
    emit('close')
  } catch (error: any) {
    alert(error.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.feedback-submit-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  z-index: 1;
}

.feedback-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 10px;
}

.btn-cancel,
.btn-submit {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-cancel {
  background: #f5f5f5;
  border: 1px solid #ddd;
  color: #666;
}

.btn-submit {
  background: #1890ff;
  border: none;
  color: white;
}

.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
