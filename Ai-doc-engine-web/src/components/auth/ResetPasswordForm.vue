<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    class="auth-form"
    @submit.prevent="handleSubmit"
  >
    <div class="auth-alert auth-alert--info">
      <svg class="auth-alert-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
        <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <div class="auth-alert-content">
        <div class="auth-alert-title">重置密码</div>
        <div class="auth-alert-description">请输入新密码</div>
      </div>
    </div>
    
    <el-form-item label="新密码" prop="newPassword" class="auth-form-item">
      <el-input
        v-model="form.newPassword"
        type="password"
        size="large"
        placeholder="请输入新密码（需包含大小写字母和数字）"
        show-password
        clearable
        class="auth-input"
      />
    </el-form-item>
    
    <el-form-item label="确认新密码" prop="confirmPassword" class="auth-form-item">
      <el-input
        v-model="form.confirmPassword"
        type="password"
        size="large"
        placeholder="请再次输入新密码"
        show-password
        clearable
        class="auth-input"
      />
    </el-form-item>
    
    <el-form-item class="auth-form-item auth-form-item--button">
      <el-button
        type="primary"
        size="large"
        :loading="loading"
        class="auth-primary-btn"
        @click="handleSubmit"
      >
        <span v-if="!loading">重置密码</span>
        <span v-else>重置中...</span>
      </el-button>
    </el-form-item>
    
    <el-form-item class="auth-form-item auth-form-item--links">
      <div class="auth-links auth-links--center">
        <router-link to="/login" class="auth-link">返回登录</router-link>
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { authApi } from '@/api/auth'
import type { ResetPasswordRequest } from '@/types/auth'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<ResetPasswordRequest & { confirmPassword: string }>({
  token: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePassword = (_rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含大写字母、小写字母和数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(() => {
  const token = route.query.token as string
  if (!token) {
    ElMessage.error('重置链接无效')
    router.push('/login')
    return
  }
  form.token = token
})

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const { confirmPassword, ...resetData } = form
      await authApi.resetPassword(resetData)
      ElMessage.success('密码重置成功，请登录')
      router.push('/login')
    } catch (error: any) {
      ElMessage.error(error.message || '重置失败')
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
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.auth-alert-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  margin-top: 2px;
  color: #3b82f6;
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
