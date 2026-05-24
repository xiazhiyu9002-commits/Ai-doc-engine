import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { authUtils } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/workbench'
  },
  {
    path: '/workbench',
    name: 'Workbench',
    component: () => import('@/views/DocumentWorkbench.vue')
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/auth/ResetPasswordView.vue')
  },
  {
    path: '/oauth/callback',
    name: 'OAuthCallback',
    component: () => import('@/views/auth/OAuthCallbackView.vue')
  },
  {
    path: '/feedback',
    name: 'MyFeedback',
    component: () => import('@/views/feedback/MyFeedbackView.vue')
  },
  {
    path: '/feedback/:id',
    name: 'FeedbackDetail',
    component: () => import('@/views/feedback/MyFeedbackView.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManageView.vue')
      },
      {
        path: 'login-logs',
        name: 'AdminLoginLogs',
        component: () => import('@/views/admin/LoginLogView.vue')
      },
      {
        path: 'feedback',
        name: 'AdminFeedback',
        component: () => import('@/views/admin/FeedbackListView.vue')
      },
      {
        path: 'feedback/:id',
        name: 'AdminFeedbackDetail',
        component: () => import('@/views/admin/FeedbackDetailView.vue')
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/AnnouncementView.vue')
      },
      {
        path: 'templates',
        name: 'AdminTemplates',
        component: () => import('@/views/admin/TemplateManageView.vue')
      },
      {
        path: 'ocr-logs',
        name: 'AdminOcrLogs',
        component: () => import('@/views/admin/OcrLogView.vue')
      },
      {
        path: 'export-logs',
        name: 'AdminExportLogs',
        component: () => import('@/views/admin/ExportLogView.vue')
      },
      {
        path: 'password-reset-logs',
        name: 'AdminPasswordResetLogs',
        component: () => import('@/views/admin/PasswordResetLogView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const isAuthenticated = authUtils.isAuthenticated()
  
  const publicRoutes = ['/reset-password', '/oauth/callback']
  
  if (to.path === '/workbench' || publicRoutes.includes(to.path)) {
    next()
    return
  }

  // 需要管理员权限的路由
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    if (!isAuthenticated) {
      next('/workbench')
      return
    }
    const user = authUtils.getCurrentUser()
    if (user?.role !== 'ADMIN') {
      next('/workbench')
      return
    }
    next()
    return
  }

  // 需要登录的路由
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      next('/workbench')
      return
    }
    next()
    return
  }
  
  if (!isAuthenticated) {
    next('/workbench')
  } else {
    next()
  }
})

export default router
