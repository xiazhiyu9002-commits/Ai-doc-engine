// 文档状态管理（仅支持 Markdown）

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { documentApi } from '@/api/document'
import type { UdmDocument, UdmBlock } from '@/types/document'

export const useDocumentStore = defineStore('document', () => {
  const markdown = ref<string>('')
  const udm = ref<UdmDocument | null>(null)
  const loading = ref<boolean>(false)

  // 解析 Markdown
  const parseMarkdown = async (content: string) => {
    loading.value = true
    try {
      const response = await documentApi.parseMarkdown({ markdown: content })
      udm.value = response.data
      markdown.value = content
    } finally {
      loading.value = false
    }
  }

  // 导出 Word（使用 UDM）
  const exportWord = async (templateId?: number) => {
    if (!udm.value) {
      throw new Error('请先解析文档内容')
    }
    
    loading.value = true
    try {
      const blob = await documentApi.exportWord({
        udm: udm.value,
        templateId
      })
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5)
      const filename = `document_${timestamp}.docx`
      
      return { url, filename }
    } finally {
      loading.value = false
    }
  }

  // 直接从 Markdown 导出 Word（一步到位，绕过 UDM 序列化问题）
  const exportWordDirect = async (templateId?: number, fileName?: string) => {
    if (!markdown.value) {
      throw new Error('请先输入 Markdown 内容')
    }
    
    let processedMarkdown = markdown.value
    
    processedMarkdown = processedMarkdown.replace(
      /\\\[([\s\S]*?)\\\]|\$\$([\s\S]*?)\$\$/g,
      (match, group1, group2) => {
        const formula = group1 || group2
        if (formula && /\\begin\{[^}]*matrix\}/.test(formula) && /\\\\/.test(formula)) {
          const oneLine = formula.replace(/\\\\\s*/g, '\\\\ ')
          return group1 ? `\\[${oneLine}\\]` : `$$${oneLine}$$`
        }
        return match
      }
    )
    
    loading.value = true
    try {
      const blob = await documentApi.exportWordFromMarkdown({
        markdown: processedMarkdown,
        templateId,
        fileName
      })
      
      const url = window.URL.createObjectURL(blob)
      const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5)
      const filename = fileName ? `${fileName}.docx` : `document_${timestamp}.docx`
      
      return { url, filename }
    } finally {
      loading.value = false
    }
  }

  // 插入块到 UDM
  const insertBlock = (block: UdmBlock, index?: number) => {
    if (!udm.value) {
      udm.value = { blocks: [] }
    }
    
    if (index !== undefined) {
      udm.value.blocks.splice(index, 0, block)
    } else {
      udm.value.blocks.push(block)
    }
  }

  // 清空文档
  const clearDocument = () => {
    markdown.value = ''
    udm.value = null
  }

  return {
    markdown,
    udm,
    loading,
    parseMarkdown,
    exportWord,
    exportWordDirect,
    insertBlock,
    clearDocument
  }
})
