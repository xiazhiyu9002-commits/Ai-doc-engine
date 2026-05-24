// Axios 封装

import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { authUtils } from '@/utils/auth'
import router from '@/router'

// API 基础路径配置
// 开发环境: 使用 Vite 代理，baseURL 为 '/api'
// 生产环境: 使用环境变量 VITE_API_BASE_URL，如果未设置则使用相对路径 '/api'
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL 
  ? `${import.meta.env.VITE_API_BASE_URL}/api`
  : '/api'

// 创建 axios 实例
const http: AxiosInstance = axios.create({
  baseURL: apiBaseUrl,
  timeout: 3000000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    // 添加 token
    const token = authUtils.getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
http.interceptors.response.use(
  (response: AxiosResponse) => {
    // 如果是 blob 类型，直接返回 data
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const { code, message } = response.data
    
    // 成功响应
    if (code === 200 || code === 0) {
      return response.data
    }
    
    // 业务错误
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    // HTTP 错误
    if (error.response) {
      const { status, data, config } = error.response
      
      if (status === 401) {
        // 判断是否是登录接口
        const isLoginRequest = config.url?.includes('/auth/login') || 
                               config.url?.includes('/auth/register')
        
        if (isLoginRequest) {
          // 登录/注册失败，显示服务器返回的错误信息
          const errorMessage = data?.message || '用户名或密码错误'
          ElMessage.error(errorMessage)
        } else {
          // Token 过期或未授权
          // 对于需要登录的接口（如OCR和导出），不自动跳转，让组件处理
          const isProtectedApi = config.url?.includes('/formula/ocr-image') || 
                                 config.url?.includes('/document/export')
          
          if (!isProtectedApi) {
            // 其他需要认证的接口，清除 token 并跳转登录
            authUtils.clearAuth()
            router.push('/workbench')
            ElMessage.error('登录已过期，请重新登录')
          } else {
            // 受保护的API，只显示错误信息，不跳转
            ElMessage.error(data?.message || '请先登录后再使用此功能')
          }
        }
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器错误')
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default http
