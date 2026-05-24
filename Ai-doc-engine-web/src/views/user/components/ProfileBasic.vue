<template>
  <div class="profile-basic">
    <div class="section-header">
      <h3 class="section-title">基本信息</h3>
      <p class="section-desc">管理您的个人资料信息</p>
    </div>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-position="top"
      class="profile-form"
      v-loading="loading"
    >
      <!-- 头像上传 -->
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <div class="avatar-preview" :style="avatarStyle">
            <span v-if="!formData.avatarUrl && !avatarText" class="avatar-placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </span>
            <span v-else-if="!formData.avatarUrl" class="avatar-text">{{ avatarText }}</span>
          </div>
          <div class="avatar-actions">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <el-button type="primary" size="small" :loading="uploading">
                <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="17 8 12 3 7 8"/>
                  <line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
                {{ uploading ? '上传中...' : '更换头像' }}
              </el-button>
            </el-upload>
            <p class="avatar-tip">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
          </div>
        </div>
      </div>

      <!-- 用户名（只读） -->
      <el-form-item label="用户名">
        <el-input v-model="formData.username" disabled>
          <template #prefix>
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <!-- 邮箱 -->
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="formData.email" placeholder="请输入邮箱地址">
          <template #prefix>
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <!-- 昵称 -->
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="formData.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit>
          <template #prefix>
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <!-- 部门（仅当有数据时显示，不可修改） -->
      <el-form-item label="部门" prop="department" v-if="hasDepartment">
        <el-input v-model="formData.department" disabled>
          <template #prefix>
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="角色">
        <el-tag :type="roleTagType" size="large" class="role-tag">
          {{ roleLabel }}
        </el-tag>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item class="form-actions">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
            <polyline points="17 21 17 13 7 13 7 21"/>
            <polyline points="7 3 7 8 15 8"/>
          </svg>
          保存修改
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus'
import { userApi } from '@/api/user'
import type { UserProfile } from '@/types/user'
import { useAuthStore } from '@/store/auth'

const props = defineProps<{
  profile: UserProfile | null
}>()

const emit = defineEmits<{
  (e: 'update:profile', profile: UserProfile): void
}>()

const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)

const formData = ref({
  username: '',
  email: '',
  nickname: '',
  department: '',
  avatarUrl: '',
  role: ''
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  nickname: [
    { max: 20, message: '昵称长度不能超过20个字符', trigger: 'blur' }
  ],
  department: [
    { max: 50, message: '部门名称长度不能超过50个字符', trigger: 'blur' }
  ]
}

// 头像文字
const avatarText = computed(() => {
  const name = formData.value.nickname || formData.value.username
  return name ? name.charAt(0).toUpperCase() : ''
})

// 头像样式
const avatarStyle = computed(() => {
  if (formData.value.avatarUrl) {
    return {
      backgroundImage: `url(${formData.value.avatarUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return {}
})

// 角色标签类型
const roleTagType = computed(() => {
  const role = formData.value.role
  if (role === 'ADMIN') return 'danger'
  if (role === 'TEACHER') return 'warning'
  return 'info'
})

// 角色标签文字
const roleLabel = computed(() => {
  const role = formData.value.role
  if (role === 'ADMIN') return '管理员'
  if (role === 'TEACHER') return '教师'
  if (role === 'STUDENT') return '学生'
  return '普通用户'
})

// 是否有部门信息
const hasDepartment = computed(() => {
  return !!props.profile?.department
})

// 监听 profile 变化
watch(() => props.profile, (newProfile) => {
  if (newProfile) {
    formData.value = {
      username: newProfile.username,
      email: newProfile.email,
      nickname: newProfile.nickname || '',
      department: newProfile.department || '',
      avatarUrl: newProfile.avatarUrl || '',
      role: newProfile.role || ''
    }
  }
}, { immediate: true })

// 头像上传前校验
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 上传头像
const handleAvatarUpload = async (options: UploadRequestOptions) => {
  uploading.value = true
  try {
    const response = await userApi.uploadAvatar(options.file as File)
    formData.value.avatarUrl = response.data.url
    ElMessage.success('头像上传成功')
  } catch (error) {
    // 错误已在 http 拦截器中处理
  } finally {
    uploading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const response = await userApi.updateProfile({
        email: formData.value.email,
        nickname: formData.value.nickname,
        avatarUrl: formData.value.avatarUrl
      })
      
      // 更新 auth store 中的用户信息
      if (authStore.user) {
        authStore.user.email = formData.value.email
        authStore.user.nickname = formData.value.nickname
        authStore.user.avatarUrl = formData.value.avatarUrl
        authStore.user.department = formData.value.department
      }
      
      emit('update:profile', response.data)
      ElMessage.success('资料更新成功')
    } catch (error) {
      // 错误已在 http 拦截器中处理
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const handleReset = () => {
  if (props.profile) {
    formData.value = {
      username: props.profile.username,
      email: props.profile.email,
      nickname: props.profile.nickname || '',
      department: props.profile.department || '',
      avatarUrl: props.profile.avatarUrl || '',
      role: props.profile.role || ''
    }
  }
  formRef.value?.clearValidate()
}

onMounted(() => {
  if (props.profile) {
    formData.value = {
      username: props.profile.username,
      email: props.profile.email,
      nickname: props.profile.nickname || '',
      department: props.profile.department || '',
      avatarUrl: props.profile.avatarUrl || '',
      role: props.profile.role || ''
    }
  }
})
</script>

<style scoped>
.profile-basic {
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

/* 头像区域 */
.avatar-section {
  margin-bottom: 24px;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-placeholder svg {
  width: 36px;
  height: 36px;
  color: rgba(255, 255, 255, 0.8);
}

.avatar-text {
  font-size: 28px;
  font-weight: 600;
  color: #fff;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-tip {
  font-size: 12px;
  color: #9ca3af;
  margin: 0;
}

/* 表单样式 */
.profile-form {
  max-width: 480px;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

.input-icon {
  width: 16px;
  height: 16px;
  color: #9ca3af;
}

.role-tag {
  font-size: 14px;
  padding: 4px 12px;
}

/* 操作按钮 */
.form-actions {
  margin-top: 32px;
}

.btn-icon {
  width: 16px;
  height: 16px;
  margin-right: 6px;
}

/* 响应式 */
@media (max-width: 640px) {
  .profile-basic {
    padding: 16px;
  }

  .avatar-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .avatar-actions {
    align-items: flex-start;
  }
}
</style>
