<template>
  <div class="admin-layout" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && !sidebarCollapsed" 
      class="sidebar-overlay"
      @click="toggleSidebar"
    ></div>

    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ 'sidebar-open': !sidebarCollapsed || !isMobile }">
      <div class="sidebar-header">
        <div class="logo-wrapper">
          <img src="/logo.png" alt="Logo" class="sidebar-logo" />
          <transition name="fade">
            <span v-show="!sidebarCollapsed" class="sidebar-title">后台管理</span>
          </transition>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link 
          v-for="item in navItems" 
          :key="item.path"
          :to="item.path" 
          class="nav-item" 
          active-class="nav-item--active"
          :title="sidebarCollapsed ? item.label : ''"
        >
          <span class="nav-icon" v-html="item.icon"></span>
          <transition name="fade">
            <span v-show="!sidebarCollapsed" class="nav-label">{{ item.label }}</span>
          </transition>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <router-link to="/workbench" class="nav-item" :title="sidebarCollapsed ? '返回工作台' : ''">
          <span class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 12H5"/>
              <polyline points="12 19 5 12 12 5"/>
            </svg>
          </span>
          <transition name="fade">
            <span v-show="!sidebarCollapsed" class="nav-label">返回工作台</span>
          </transition>
        </router-link>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="admin-main">
      <!-- 顶部导航栏 -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <!-- 移动端菜单按钮 -->
          <button v-if="isMobile" class="menu-toggle" @click="toggleSidebar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="6" x2="21" y2="6" />
              <line x1="3" y1="12" x2="21" y2="12" />
              <line x1="3" y1="18" x2="21" y2="18" />
            </svg>
          </button>
          <!-- 面包屑导航 -->
          <nav class="breadcrumb">
            <!-- 折叠按钮放在首页前面 -->
            <button 
              v-if="!isMobile" 
              class="collapse-btn" 
              @click="toggleSidebar"
              :title="sidebarCollapsed ? '展开菜单' : '收起菜单'"
            >
              <svg v-if="sidebarCollapsed" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="12" x2="21" y2="12"/>
                <line x1="3" y1="6" x2="21" y2="6"/>
                <line x1="3" y1="18" x2="21" y2="18"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2"/>
                <line x1="9" y1="3" x2="9" y2="21"/>
              </svg>
            </button>
            <span class="breadcrumb-item">
              <span>首页</span>
            </span>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-item breadcrumb-item--active">{{ pageTitle }}</span>
          </nav>
        </div>

        <div class="topbar-right">
          <span class="admin-badge">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="badge-icon">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            管理员
          </span>
          <div class="user-info">
            <div class="user-avatar">
              <img v-if="authStore.user?.avatarUrl" :src="authStore.user.avatarUrl" alt="头像" />
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <span class="topbar-username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
          </div>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const authStore = useAuthStore()

// 响应式状态
const isMobile = ref(false)
const sidebarCollapsed = ref(false)

// 导航项配置
const navItems = [
  {
    path: '/admin/dashboard',
    label: '仪表盘',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>'
  },
  {
    path: '/admin/users',
    label: '用户管理',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>'
  },
  {
    path: '/admin/templates',
    label: '模板管理',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>'
  },
  {
    path: '/admin/feedback',
    label: '反馈管理',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z"/></svg>'
  },
  {
    path: '/admin/announcements',
    label: '公告管理',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 17H2a3 3 0 0 0 3-3V9a7 7 0 0 1 14 0v5a3 3 0 0 0 3 3zm-8.27 4a2 2 0 0 1-3.46 0"/></svg>'
  },
  {
    path: '/admin/login-logs',
    label: '登录日志',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>'
  },
  {
    path: '/admin/ocr-logs',
    label: 'OCR日志',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>'
  },
  {
    path: '/admin/export-logs',
    label: '导出日志',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>'
  },
  {
    path: '/admin/password-reset-logs',
    label: '密码重置日志',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>'
  }
]

// 页面标题映射
const pageTitle = computed(() => {
  const map: Record<string, string> = {
    '/admin/dashboard': '仪表盘',
    '/admin/users': '用户管理',
    '/admin/templates': '模板管理',
    '/admin/feedback': '反馈管理',
    '/admin/announcements': '公告管理',
    '/admin/login-logs': '登录日志',
    '/admin/ocr-logs': 'OCR日志',
    '/admin/export-logs': '导出日志',
    '/admin/password-reset-logs': '密码重置日志'
  }
  return map[route.path] || '后台管理'
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 检测屏幕尺寸
const checkScreenSize = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    sidebarCollapsed.value = true
  }
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})
</script>

<style scoped>
/* ==================== CSS 变量系统 ==================== */
.admin-layout {
  /* 主色调 */
  --color-primary: #6366f1;
  --color-primary-hover: #4f46e5;
  --color-primary-light: #eef2ff;
  --color-primary-dark: #4338ca;

  /* 语义色 */
  --color-success: #10b981;
  --color-success-light: #ecfdf5;
  --color-warning: #f59e0b;
  --color-warning-light: #fffbeb;
  --color-danger: #ef4444;
  --color-danger-light: #fef2f2;
  --color-info: #3b82f6;
  --color-info-light: #eff6ff;

  /* 中性色 */
  --color-slate-50: #f8fafc;
  --color-slate-100: #f1f5f9;
  --color-slate-200: #e2e8f0;
  --color-slate-300: #cbd5e1;
  --color-slate-400: #94a3b8;
  --color-slate-500: #64748b;
  --color-slate-600: #475569;
  --color-slate-700: #334155;
  --color-slate-800: #1e293b;
  --color-slate-900: #0f172a;

  /* 字体 */
  --font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif;
  --font-size-xs: 12px;
  --font-size-sm: 14px;
  --font-size-base: 16px;
  --font-size-lg: 18px;
  --font-size-xl: 24px;
  --line-height: 1.5;

  /* 间距系统 (8px 基准) */
  --spacing-1: 4px;
  --spacing-2: 8px;
  --spacing-3: 12px;
  --spacing-4: 16px;
  --spacing-5: 20px;
  --spacing-6: 24px;
  --spacing-8: 32px;

  /* 圆角 */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-full: 9999px;

  /* 阴影 */
  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
  --shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);

  /* 过渡 */
  --transition-fast: 150ms ease;
  --transition-base: 200ms ease;
  --transition-slow: 300ms ease;

  /* 侧边栏宽度 */
  --sidebar-width: 240px;
  --sidebar-collapsed-width: 72px;

  /* 布局 */
  display: flex;
  height: 100vh;
  background: var(--color-slate-100);
  font-family: var(--font-family);
  line-height: var(--line-height);
}

/* ==================== 侧边栏 ==================== */
.admin-sidebar {
  width: var(--sidebar-width);
  background: linear-gradient(180deg, var(--color-slate-800) 0%, var(--color-slate-900) 100%);
  color: var(--color-slate-300);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width var(--transition-base);
  position: relative;
  z-index: 100;
  box-shadow: var(--shadow-xl);
}

.admin-layout.sidebar-collapsed .admin-sidebar {
  width: var(--sidebar-collapsed-width);
}

/* 移动端侧边栏 */
@media (max-width: 767px) {
  .admin-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    transform: translateX(-100%);
    transition: transform var(--transition-base);
    width: var(--sidebar-width);
  }

  .admin-sidebar.sidebar-open {
    transform: translateX(0);
  }

  .admin-layout.sidebar-collapsed .admin-sidebar {
    width: var(--sidebar-width);
  }
}

/* 侧边栏遮罩 */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
  animation: fadeIn var(--transition-fast);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 侧边栏头部 */
.sidebar-header {
  display: flex;
  align-items: center;
  padding: var(--spacing-5) var(--spacing-4);
  border-bottom: 1px solid var(--color-slate-700);
  min-height: 64px;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  overflow: hidden;
}

.sidebar-logo {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  flex-shrink: 0;
  box-shadow: var(--shadow-md);
}

.sidebar-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}

/* 导航项样式 */
.sidebar-nav {
  flex: 1;
  padding: var(--spacing-3) var(--spacing-2);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  padding: var(--spacing-3) var(--spacing-4);
  border-radius: var(--radius-md);
  color: var(--color-slate-400);
  text-decoration: none;
  font-size: var(--font-size-sm);
  transition: all var(--transition-base);
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: var(--color-primary);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  transition: height var(--transition-fast);
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon :deep(svg) {
  width: 20px;
  height: 20px;
}

.nav-label {
  white-space: nowrap;
  overflow: hidden;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.nav-item--active {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

.nav-item--active::before {
  height: 60%;
}

.nav-item--active:hover {
  background: linear-gradient(135deg, var(--color-primary-hover) 0%, var(--color-primary-dark) 100%);
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: var(--spacing-3) var(--spacing-2);
  border-top: 1px solid var(--color-slate-700);
}

/* ==================== 主内容区 ==================== */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* ==================== 顶部导航栏 ==================== */
.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-4) var(--spacing-6);
  background: #fff;
  border-bottom: 1px solid var(--color-slate-200);
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);
}

@media (max-width: 767px) {
  .admin-topbar {
    padding: var(--spacing-3) var(--spacing-4);
  }
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
}

/* 菜单切换按钮 */
.menu-toggle {
  width: 40px;
  height: 40px;
  border: none;
  background: var(--color-slate-100);
  border-radius: var(--radius-md);
  color: var(--color-slate-600);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.menu-toggle:hover {
  background: var(--color-slate-200);
  color: var(--color-slate-800);
}

.menu-toggle svg {
  width: 20px;
  height: 20px;
}

/* 面包屑导航 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: var(--font-size-sm);
  color: var(--color-slate-500);
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  transition: color var(--transition-fast);
}

/* 折叠按钮样式 */
.collapse-btn {
  width: 32px;
  height: 32px;
  border: 1px solid var(--color-slate-200);
  border-radius: var(--radius-sm);
  background: #fff;
  color: var(--color-slate-600);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  margin-right: var(--spacing-2);
}

.collapse-btn:hover {
  background: var(--color-slate-50);
  border-color: var(--color-slate-300);
  color: var(--color-slate-700);
}

.collapse-btn svg {
  width: 18px;
  height: 18px;
}

.breadcrumb-item:hover {
  color: var(--color-primary);
}

.breadcrumb-separator {
  color: var(--color-slate-300);
}

.breadcrumb-item--active {
  color: var(--color-slate-800);
  font-weight: 500;
}

/* 顶部右侧 */
.topbar-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
}

.admin-badge {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  background: linear-gradient(135deg, var(--color-primary-light) 0%, #e0e7ff 100%);
  color: var(--color-primary);
  padding: var(--spacing-1) var(--spacing-3);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: 600;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.badge-icon {
  width: 14px;
  height: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar svg {
  width: 18px;
  height: 18px;
}

.topbar-username {
  font-size: var(--font-size-sm);
  color: var(--color-slate-700);
  font-weight: 500;
}

@media (max-width: 767px) {
  .topbar-username {
    display: none;
  }
}

/* ==================== 内容区域 ==================== */
.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-6);
}

@media (max-width: 767px) {
  .admin-content {
    padding: var(--spacing-4);
  }
}

/* ==================== 过渡动画 ==================== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-fast);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ==================== 滚动条样式 ==================== */
.admin-content::-webkit-scrollbar,
.sidebar-nav::-webkit-scrollbar {
  width: 6px;
}

.admin-content::-webkit-scrollbar-track,
.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.admin-content::-webkit-scrollbar-thumb,
.sidebar-nav::-webkit-scrollbar-thumb {
  background: var(--color-slate-300);
  border-radius: var(--radius-full);
}

.admin-content::-webkit-scrollbar-thumb:hover,
.sidebar-nav::-webkit-scrollbar-thumb:hover {
  background: var(--color-slate-400);
}
</style>
