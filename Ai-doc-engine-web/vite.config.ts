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
      }
    }
  }
})
