import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3514,
    // 开发环境代理配置
    // 将 /api、/oauth、/uploads 及子目录请求代理到后端服务
    proxy: {
      '/api': {
        target: 'http://localhost:8687',
        changeOrigin: true
      },
      '/oauth': {
        target: 'http://localhost:8687',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8687',
        changeOrigin: true
      },
      // 兼容旧数据格式
      '/avatar': {
        target: 'http://localhost:8687',
        changeOrigin: true
      },
      '/feedback': {
        target: 'http://localhost:8687',
        changeOrigin: true
      },
      '/document': {
        target: 'http://localhost:8687',
        changeOrigin: true
      },
      '/temp': {
        target: 'http://localhost:8687',
        changeOrigin: true
      }
    }
  },
  build: {
    // 生产环境构建配置
    outDir: 'dist',
    assetsDir: 'assets',
    // 生成 sourcemap 便于调试（生产环境可关闭）
    sourcemap: false,
    // 代码分割策略
    rollupOptions: {
      output: {
        // 将第三方库分离到单独的 chunk
        manualChunks: {
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia']
        }
      }
    }
  }
})
