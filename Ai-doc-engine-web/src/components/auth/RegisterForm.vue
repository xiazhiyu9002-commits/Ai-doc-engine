<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    class="auth-form"
    @submit.prevent="handleSubmit"
  >
    <el-form-item label="用户名" prop="username" class="auth-form-item">
      <el-input
        v-model="form.username"
        size="large"
        placeholder="请输入用户名"
        clearable
        class="auth-input"
      />
    </el-form-item>
    
    <el-form-item label="昵称" prop="nickname" class="auth-form-item">
      <el-input
        v-model="form.nickname"
        size="large"
        placeholder="请输入昵称（可选）"
        clearable
        class="auth-input"
      />
    </el-form-item>
    
    <el-form-item label="邮箱" prop="email" class="auth-form-item">
      <el-input
        v-model="form.email"
        type="email"
        size="large"
        placeholder="请输入邮箱"
        clearable
        class="auth-input"
      />
    </el-form-item>
    
    <el-form-item label="密码" prop="password" class="auth-form-item">
      <el-input
        v-model="form.password"
        type="password"
        size="large"
        placeholder="请输入密码（需包含大小写字母和数字）"
        show-password
        clearable
        class="auth-input"
      />
    </el-form-item>
    
    <el-form-item label="确认密码" prop="confirmPassword" class="auth-form-item">
      <el-input
        v-model="form.confirmPassword"
        type="password"
        size="large"
        placeholder="请再次输入密码"
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
        <span v-if="!loading">注册</span>
        <span v-else>注册中...</span>
      </el-button>
    </el-form-item>
    
    <el-form-item class="auth-form-item auth-form-item--links">
      <div class="auth-links auth-links--center">
        <span class="auth-links-text">已有账号？</span>
        <a href="javascript:;" class="auth-link" @click="emit('switchToLogin')">立即登录</a>
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import type { RegisterRequest } from '@/types/auth'

const emit = defineEmits<{
  (e: 'success'): void
  (e: 'switchToLogin'): void
}>()

const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<RegisterRequest & { confirmPassword: string }>({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validatePassword = (_rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含大写字母、小写字母和数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }
  ],
  nickname: [
    { max: 20, message: '昵称最多20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const { confirmPassword, ...registerData } = form
      await authStore.register(registerData)
      emit('success')
    } catch (error: any) {
      ElMessage.error(error.message || '注册失败')
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

.auth-links-text {
  color: #6b7280;
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
