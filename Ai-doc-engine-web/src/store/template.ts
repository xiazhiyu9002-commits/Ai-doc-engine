// 模板状态管理

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { templateApi } from '@/api/template'
import type { Template } from '@/types/template'

export const useTemplateStore = defineStore('template', () => {
  const templates = ref<Template[]>([])
  const selectedTemplate = ref<Template | null>(null)
  const loading = ref<boolean>(false)

  // 加载模板列表
  const fetchTemplates = async () => {
    loading.value = true
    try {
      const response = await templateApi.getTemplates()
      templates.value = response.data.templates
      
      // 自动选择默认模板
      if (!selectedTemplate.value) {
        const defaultTemplate = templates.value.find(t => t.isDefault)
        if (defaultTemplate) {
          selectedTemplate.value = defaultTemplate
        }
      }
    } finally {
      loading.value = false
    }
  }

  // 选择模板
  const selectTemplate = (template: Template | null) => {
    selectedTemplate.value = template
  }

  // 根据 ID 获取模板
  const getTemplateById = async (id: number) => {
    loading.value = true
    try {
      const response = await templateApi.getTemplateById(id)
      return response.data
    } finally {
      loading.value = false
    }
  }

  return {
    templates,
    selectedTemplate,
    loading,
    fetchTemplates,
    selectTemplate,
    getTemplateById
  }
})
