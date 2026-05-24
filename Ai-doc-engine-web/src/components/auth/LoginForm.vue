<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    class="auth-form"
    @submit.prevent="handleSubmit"
  >
    <el-form-item label="用户名或邮箱" prop="username" class="auth-form-item">
      <el-input
        v-model="form.username"
        size="large"
        placeholder="请输入用户名或邮箱"
        clearable
        class="auth-input"
      />
    </el-form-item>
    
    <el-form-item label="密码" prop="password" class="auth-form-item">
      <el-input
        v-model="form.password"
        type="password"
        size="large"
        placeholder="请输入密码(密码必须包含大写字母、小写字母和数字)"
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
        <span v-if="!loading">登录</span>
        <span v-else>登录中...</span>
      </el-button>
    </el-form-item>

    <div class="auth-divider">
      <span>或</span>
    </div>

    <el-form-item class="auth-form-item auth-form-item--oauth">
      <el-button
        size="large"
        class="auth-oauth-btn"
        @click="handleSchoolLogin"
      >
        <el-icon class="oauth-icon"><Connection /></el-icon>
        校内统一登录
      </el-button>
    </el-form-item>
    
    <el-form-item class="auth-form-item auth-form-item--links">
      <div class="auth-links">
        <a href="javascript:;" class="auth-link auth-link--register" @click="emit('switchToRegister')">
          注册账号
        </a>
        <a href="javascript:;" class="auth-link auth-link--forgot" @click="emit('switchToForgot')">
          忘记密码？
        </a>
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import type { LoginRequest } from '@/types/auth'

const emit = defineEmits<{
  (e: 'success'): void
  (e: 'switchToRegister'): void
  (e: 'switchToForgot'): void
}>()

const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<LoginRequest>({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码必须包含大写字母、小写字母和数字', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await authStore.login(form)
      emit('success')
    } catch (error: any) {
    } finally {
      loading.value = false
    }
  })
}

const handleSchoolLogin = () => {
  window.location.assign('/oauth/login')
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

.auth-form-item--oauth {
  margin-bottom: 20px;
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

.auth-divider {
  position: relative;
  display: flex;
  justify-content: center;
  margin: 0 0 20px;
  color: #9ca3af;
  font-size: 13px;
}

.auth-divider::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e5e7eb;
}

.auth-divider span {
  position: relative;
  padding: 0 12px;
  background: #ffffff;
}

.auth-oauth-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  border-color: #d1d5db;
  color: #1f2937;
  font-weight: 600;
}

.auth-oauth-btn:hover {
  border-color: #2563eb;
  color: #2563eb;
  background: #eff6ff;
}

.oauth-icon {
  margin-right: 6px;
}

.auth-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  font-size: 14px;
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

  .auth-form-item--oauth {
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
