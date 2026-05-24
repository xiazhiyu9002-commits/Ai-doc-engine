<template>
  <el-dropdown 
    @command="handleCommand" 
    trigger="click"
    :teleported="true"
    :persistent="false"
    :popper-options="{
      modifiers: [
        { name: 'preventOverflow', options: { mainAxis: false } },
        { name: 'flip', options: { fallbackPlacements: ['bottom-end'] } }
      ]
    }"
    popper-class="user-dropdown-popper"
  >
    <div class="user-menu">
      <div class="user-avatar" :style="avatarStyle">
        <span v-if="!user?.avatarUrl">{{ avatarText }}</span>
      </div>
      <span class="username">{{ displayName }}</span>
      <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <div class="dropdown-header">
          <div class="dropdown-avatar" :style="dropdownAvatarStyle">
            <span v-if="!user?.avatarUrl">{{ avatarText }}</span>
          </div>
          <div class="dropdown-user-info">
            <span class="dropdown-username">{{ displayName }}</span>
            <span v-if="user?.department" class="dropdown-department">{{ user.department }}</span>
          </div>
        </div>
        <el-dropdown-item v-if="isAdmin" command="admin" class="admin-item">
          <svg class="menu-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="3" y="3" width="7" height="7" rx="1" stroke="currentColor" stroke-width="2"/>
            <rect x="14" y="3" width="7" height="7" rx="1" stroke="currentColor" stroke-width="2"/>
            <rect x="3" y="14" width="7" height="7" rx="1" stroke="currentColor" stroke-width="2"/>
            <rect x="14" y="14" width="7" height="7" rx="1" stroke="currentColor" stroke-width="2"/>
          </svg>
          <span>后台管理</span>
        </el-dropdown-item>
        <el-dropdown-item command="profile" class="profile-item">
          <svg class="menu-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
          </svg>
          <span>个人中心</span>
        </el-dropdown-item>
        <el-dropdown-item command="feedback" class="feedback-item">
          <svg class="menu-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>意见反馈</span>
        </el-dropdown-item>
        <el-dropdown-item divided command="logout" class="logout-item">
          <svg class="menu-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M10 17L15 12L10 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M15 12H3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>退出登录</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { getAvatarUrl } from '@/utils/config'

const authStore = useAuthStore()
const router = useRouter()

const user = computed(() => authStore.user)
const displayName = computed(() => user.value?.nickname || user.value?.username || '用户')
const isAdmin = computed(() => user.value?.role === 'ADMIN')

const avatarText = computed(() => {
  return displayName.value.charAt(0).toUpperCase()
})

const avatarStyle = computed(() => {
  if (user.value?.avatarUrl) {
    return {
      backgroundImage: `url(${getAvatarUrl(user.value.avatarUrl)})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return {}
})

const dropdownAvatarStyle = computed(() => {
  if (user.value?.avatarUrl) {
    return {
      backgroundImage: `url(${getAvatarUrl(user.value.avatarUrl)})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return {}
})

const handleCommand = async (command: string) => {
  if (command === 'admin') {
    router.push('/admin/dashboard')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'feedback') {
    router.push('/feedback')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await authStore.logout()
      ElMessage.success('已退出登录')
    } catch (error) {
      // 用户取消
    }
  }
}
</script>

<style scoped>
.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.user-menu:hover {
  background-color: #f3f4f6;
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar span {
  color: white;
  font-size: 13px;
  font-weight: 600;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.arrow-icon {
  width: 14px;
  height: 14px;
  color: #9ca3af;
}

:deep(.el-dropdown) {
  display: flex;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.dropdown-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dropdown-avatar span {
  color: white;
  font-size: 15px;
  font-weight: 600;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  overflow: hidden;
}

.dropdown-username {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  line-height: 1.4;
}

.dropdown-department {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.menu-icon {
  width: 16px;
  height: 16px;
}
</style>

<style>
.user-dropdown-popper {
  margin-top: 8px !important;
}

.user-dropdown-popper .el-dropdown-menu {
  padding: 0;
  border-radius: 8px;
  min-width: 200px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12);
}

.user-dropdown-popper .el-popper__arrow::before {
  display: none;
}

.user-dropdown-popper .admin-item {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4f46e5;
}

.user-dropdown-popper .admin-item:hover {
  background-color: #eef2ff;
}

.user-dropdown-popper .profile-item {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6366f1;
}

.user-dropdown-popper .profile-item:hover {
  background-color: #eef2ff;
}

.user-dropdown-popper .feedback-item {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #10b981;
}

.user-dropdown-popper .feedback-item:hover {
  background-color: #ecfdf5;
}

.user-dropdown-popper .logout-item {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #dc2626;
}

.user-dropdown-popper .logout-item:hover {
  background-color: #fef2f2;
}

.user-dropdown-popper .el-dropdown-menu__item--divided {
  margin-top: 0;
  border-top: none;
}
</style>
