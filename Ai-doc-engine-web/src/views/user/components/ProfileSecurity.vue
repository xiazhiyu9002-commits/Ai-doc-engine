<template>
  <div class="profile-security">
    <div class="section-header">
      <h3 class="section-title">安全设置</h3>
      <p class="section-desc">管理您的账户安全</p>
    </div>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-position="top"
      class="security-form"
    >
      <!-- 旧密码 -->
      <el-form-item label="当前密码" prop="oldPassword">
        <el-input
          v-model="formData.oldPassword"
          type="password"
          placeholder="请输入当前密码"
          show-password
        >
          <template #prefix>
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <!-- 新密码 -->
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="formData.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
        >
          <template #prefix>
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <!-- 密码强度指示器 -->
      <div class="password-strength" v-if="formData.newPassword">
        <div class="strength-label">密码强度：</div>
        <div class="strength-bars">
          <div
            v-for="i in 4"
            :key="i"
            class="strength-bar"
            :class="{ 'active': i <= passwordStrength.level }"
            :style="{ backgroundColor: i <= passwordStrength.level ? passwordStrength.color : '' }"
          ></div>
        </div>
        <span class="strength-text" :style="{ color: passwordStrength.color }">
          {{ passwordStrength.text }}
        </span>
      </div>

      <!-- 密码强度提示 -->
      <div class="password-tips" v-if="formData.newPassword">
        <div class="tip-title">密码要求：</div>
        <ul class="tip-list">
          <li :class="{ 'met': formData.newPassword.length >= 8 }">
            <svg class="tip-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            至少 8 个字符
          </li>
          <li :class="{ 'met': /[a-z]/.test(formData.newPassword) }">
            <svg class="tip-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            包含小写字母
          </li>
          <li :class="{ 'met': /[A-Z]/.test(formData.newPassword) }">
            <svg class="tip-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            包含大写字母
          </li>
          <li :class="{ 'met': /\d/.test(formData.newPassword) }">
            <svg class="tip-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            包含数字
          </li>
          <li :class="{ 'met': hasSpecialChar }">
            <svg class="tip-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            包含特殊字符
          </li>
        </ul>
      </div>

      <!-- 确认密码 -->
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="formData.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        >
          <template #prefix>
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item class="form-actions">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          修改密码
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { userApi } from '@/api/user'

const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证
const validatePass = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else if (value.length < 8) {
    callback(new Error('密码长度不能少于8位'))
  } else {
    if (formData.value.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== formData.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

// 密码强度计算
const passwordStrength = computed(() => {
  const password = formData.value.newPassword
  let score = 0

  if (password.length >= 8) score++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++
  if (/\d/.test(password)) score++
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score++

  const levels = [
    { level: 1, text: '弱', color: '#ef4444' },
    { level: 2, text: '一般', color: '#f59e0b' },
    { level: 3, text: '强', color: '#10b981' },
    { level: 4, text: '非常强', color: '#059669' }
  ]

  return levels[Math.min(score, 4) - 1] || { level: 0, text: '', color: '' }
})

// 是否包含特殊字符
const hasSpecialChar = computed(() => {
  return /[!@#$%^&*(),.?":{}|<>]/.test(formData.value.newPassword)
})

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await userApi.changePassword({
        oldPassword: formData.value.oldPassword,
        newPassword: formData.value.newPassword,
        confirmPassword: formData.value.confirmPassword
      })
      
      ElMessage.success('密码修改成功')
      handleReset()
    } catch (error) {
      // 错误已在 http 拦截器中处理
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const handleReset = () => {
  formData.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  formRef.value?.clearValidate()
}
</script>

<style scoped>
.profile-security {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 表单样式 */
.security-form {
  max-width: 480px;
}

.security-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

.input-icon {
  width: 16px;
  height: 16px;
  color: #9ca3af;
}

/* 密码强度指示器 */
.password-strength {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: -8px 0 16px 0;
  padding-left: 2px;
}

.strength-label {
  font-size: 13px;
  color: #6b7280;
}

.strength-bars {
  display: flex;
  gap: 4px;
}

.strength-bar {
  width: 24px;
  height: 4px;
  border-radius: 2px;
  background-color: #e5e7eb;
  transition: background-color 0.3s ease;
}

.strength-text {
  font-size: 13px;
  font-weight: 500;
}

/* 密码提示 */
.password-tips {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.tip-title {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.tip-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 6px 16px;
}

.tip-list li {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9ca3af;
  transition: color 0.2s ease;
}

.tip-list li.met {
  color: #10b981;
}

.tip-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

/* 操作按钮 */
.form-actions {
  margin-top: 24px;
}

.btn-icon {
  width: 16px;
  height: 16px;
  margin-right: 6px;
}

/* 响应式 */
@media (max-width: 640px) {
  .profile-security {
    padding: 16px;
  }

  .tip-list {
    grid-template-columns: 1fr;
  }
}
</style>
