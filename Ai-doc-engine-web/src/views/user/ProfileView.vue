<template>
  <div class="profile-view">
    <div class="page-header">
      <button class="back-home-btn" @click="goHome">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        返回首页
      </button>
      <h2 class="page-title">个人中心</h2>
    </div>
    
    <div class="profile-container">
      <div class="profile-sidebar">
        <div class="user-card">
          <div class="user-avatar">
            <img v-if="avatarSrc" :src="avatarSrc" alt="头像" />
            <span v-else class="avatar-text">{{ avatarText }}</span>
          </div>
          <div class="user-info">
            <h3 class="user-name">{{ profile?.nickname || profile?.username || '用户' }}</h3>
            <p class="user-department" v-if="profile?.department">{{ profile.department }}</p>
          </div>
        </div>
        
        <div class="sidebar-menu">
          <div 
            class="menu-item" 
            :class="{ active: activeTab === 'basic' }"
            @click="activeTab = 'basic'"
          >
            <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span>基本信息</span>
          </div>
          <div 
            class="menu-item" 
            :class="{ active: activeTab === 'security' }"
            @click="activeTab = 'security'"
          >
            <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <span>安全设置</span>
          </div>
        </div>
      </div>
      
      <div class="profile-content">
        <ProfileBasic 
          v-if="activeTab === 'basic'" 
          :profile="profile"
          @update:profile="handleProfileUpdate"
        />
        <ProfileSecurity v-if="activeTab === 'security'" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { userApi } from '@/api/user'
import { getAvatarUrl } from '@/utils/config'
import type { UserProfile } from '@/types/user'
import ProfileBasic from './components/ProfileBasic.vue'
import ProfileSecurity from './components/ProfileSecurity.vue'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('basic')
const profile = ref<UserProfile | null>(null)
const loading = ref(false)

const avatarText = computed(() => {
  const name = profile.value?.nickname || profile.value?.username
  return name ? name.charAt(0).toUpperCase() : ''
})

const avatarSrc = computed(() => {
  if (!profile.value?.avatarUrl) return ''
  return getAvatarUrl(profile.value.avatarUrl)
})

const fetchProfile = async () => {
  loading.value = true
  try {
    const response = await userApi.getProfile()
    profile.value = response.data
  } catch (error) {
    console.error('获取用户资料失败:', error)
  } finally {
    loading.value = false
  }
}

const handleProfileUpdate = (updatedProfile: UserProfile) => {
  profile.value = updatedProfile
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/workbench')
    return
  }
  fetchProfile()
})
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 24px;
}

.page-header {
  max-width: 1000px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
}

.back-home-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: white;
  color: #6b7280;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.15s;
}

.back-home-btn:hover {
  border-color: #667eea;
  color: #667eea;
  background: #eef2ff;
}

.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  gap: 24px;
}

.profile-sidebar {
  width: 260px;
  flex-shrink: 0;
}

.user-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  font-size: 28px;
  font-weight: 600;
  color: white;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.user-role {
  margin: 0 0 8px 0;
}

.user-department {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  text-align: center;
}

.sidebar-menu {
  background: white;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #6b7280;
}

.menu-item:hover {
  background: #f3f4f6;
  color: #374151;
}

.menu-item.active {
  background: #eef2ff;
  color: #667eea;
}

.menu-icon {
  width: 20px;
  height: 20px;
}

.profile-content {
  flex: 1;
  min-width: 0;
}

@media (max-width: 768px) {
  .profile-view {
    padding: 16px;
  }

  .page-header {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 18px;
  }

  .profile-container {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
  }

  .sidebar-menu {
    display: flex;
    gap: 8px;
  }

  .menu-item {
    flex: 1;
    justify-content: center;
  }
}
</style>
