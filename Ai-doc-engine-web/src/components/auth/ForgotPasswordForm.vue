<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    class="auth-form"
    @submit.prevent="handleSubmit"
  >
    <div v-if="!submitted" class="auth-alert auth-alert--info">
      <svg class="auth-alert-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
        <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <div class="auth-alert-content">
        <div class="auth-alert-title">忘记密码</div>
        <div class="auth-alert-description">请输入您的注册邮箱，我们将发送重置密码链接到您的邮箱。</div>
      </div>
    </div>
    
    <div v-else class="auth-alert auth-alert--success">
      <svg class="auth-alert-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
        <path d="M8 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <div class="auth-alert-content">
        <div class="auth-alert-title">请求已提交</div>
        <div class="auth-alert-description">如果该邮箱已注册，重置密码链接将发送到您的邮箱，请查收（可能需要几分钟）</div>
      </div>
    </div>
    
    <el-form-item v-if="!submitted" label="邮箱" prop="email" class="auth-form-item">
      <el-input
        v-model="form.email"
        type="email"
        size="large"
        placeholder="请输入注册邮箱"
        clearable
        class="auth-input"
      />
    </el-form-item>
    
    <el-form-item v-if="!submitted" class="auth-form-item auth-form-item--button">
      <el-button
        type="primary"
        size="large"
        :loading="loading"
        class="auth-primary-btn"
        @click="handleSubmit"
      >
        <span v-if="!loading">发送重置链接</span>
        <span v-else>发送中...</span>
      </el-button>
    </el-form-item>
    
    <el-form-item class="auth-form-item auth-form-item--links">
      <div class="auth-links auth-links--center">
        <a href="javascript:;" class="auth-link" @click="emit('switchToLogin')">返回登录</a>
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { authApi } from '@/api/auth'
import type { ForgotPasswordRequest } from '@/types/auth'

const emit = defineEmits<{
  (e: 'success'): void
  (e: 'switchToLogin'): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitted = ref(false)

const form = reactive<ForgotPasswordRequest>({
  email: ''
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await authApi.forgotPassword(form)
      submitted.value = true
      emit('success')
    } catch (error: any) {
      ElMessage.error(error.message || '发送失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-form {
  width: 100%;
}

.auth-alert {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 24px;
}

.auth-alert--info {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.auth-alert--success {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.auth-alert-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.auth-alert--info .auth-alert-icon {
  color: #3b82f6;
}

.auth-alert--success .auth-alert-icon {
  color: #22c55e;
}

.auth-alert-content {
  flex: 1;
}

.auth-alert-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.auth-alert-description {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
}

.auth-form-item {
  margin-bottom: 24px;
}

.auth-form-item:last-child {
  margin-bottom: 0;
}

.auth-form-item--button {
  margin-top: 8px;
  margin-bottom: 20px;
}

.auth-form-item--links {
  margin-bottom: 0;
}

.auth-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  line-height: 1.5;
  padding-bottom: 8px;
}

.auth-form :deep(.el-form-item__error) {
  font-size: 13px;
  padding-top: 6px;
}

.auth-input :deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e5e7eb;
  transition: all 0.2s ease;
}

.auth-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d1d5db;
}

.auth-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2), 0 0 0 1px #667eea;
}

.auth-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #111827;
  line-height: 1.5;
}

.auth-input :deep(.el-input__inner::placeholder) {
  color: #9ca3af;
}

.auth-primary-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.3);
  transition: all 0.2s ease;
}

.auth-primary-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.auth-primary-btn:active {
  transform: translateY(0);
}

.auth-primary-btn.is-loading {
  opacity: 0.8;
}

.auth-links {
  width: 100%;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.auth-links--center {
  justify-content: center;
  gap: 6px;
}

.auth-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
  cursor: pointer;
}

.auth-link:hover {
  color: #764ba2;
}

.auth-link:active {
  color: #5568d3;
}

@media (max-width: 480px) {
  .auth-alert {
    padding: 14px;
    margin-bottom: 20px;
  }

  .auth-alert-title {
    font-size: 13px;
  }

  .auth-alert-description {
    font-size: 12px;
  }

  .auth-form-item {
    margin-bottom: 20px;
  }

  .auth-form-item--button {
    margin-top: 4px;
    margin-bottom: 16px;
  }

  .auth-form :deep(.el-form-item__label) {
    font-size: 13px;
  }

  .auth-input :deep(.el-input__wrapper) {
    padding: 11px 14px;
  }

  .auth-input :deep(.el-input__inner) {
    font-size: 14px;
  }

  .auth-primary-btn {
    height: 46px;
    font-size: 15px;
  }

  .auth-links {
    font-size: 13px;
  }
}
</style>
