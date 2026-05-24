<template>
  <div class="oauth-callback-page">
    <el-result
      :icon="resultIcon"
      :title="title"
      :sub-title="subtitle"
      class="oauth-result"
    >
      <template #extra>
        <el-button type="primary" @click="router.replace('/workbench')">
          返回工作台
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const status = ref<'loading' | 'success' | 'error'>('loading')
const errorMessage = ref('')

const resultIcon = computed(() => {
  if (status.value === 'success') return 'success'
  if (status.value === 'error') return 'error'
  return 'info'
})

const title = computed(() => {
  if (status.value === 'success') return '登录成功'
  if (status.value === 'error') return '登录失败'
  return '正在完成校内统一登录'
})

const subtitle = computed(() => {
  if (status.value === 'success') return '正在进入工作台...'
  if (status.value === 'error') return errorMessage.value || '请重新发起校内统一登录'
  return '请稍候'
})

onMounted(async () => {
  const error = getQueryValue(route.query.error)
  if (error) {
    status.value = 'error'
    errorMessage.value = error
    ElMessage.error(error)
    return
  }

  const ticket = getQueryValue(route.query.ticket)
  if (!ticket) {
    status.value = 'error'
    errorMessage.value = '登录票据缺失，请重新发起校内统一登录'
    ElMessage.error(errorMessage.value)
    return
  }

  try {
    await authStore.completeOAuthLogin(ticket)
    status.value = 'success'
    ElMessage.success('校内统一登录成功')
    window.setTimeout(() => {
      router.replace('/workbench')
    }, 500)
  } catch (error: any) {
    status.value = 'error'
    errorMessage.value = error?.message || '校内统一登录失败'
  }
})

const getQueryValue = (value: unknown): string => {
  if (Array.isArray(value)) {
    return String(value[0] || '')
  }
  return typeof value === 'string' ? value : ''
}
</script>

<style scoped>
.oauth-callback-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f8fafc;
}

.oauth-result {
  width: min(520px, calc(100vw - 32px));
}
</style>
