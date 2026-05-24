// 文档相关接口（仅支持 Markdown）

import http from './http'
import type {
  ParseRequest,
  ParseResponse,
  ExportRequest
} from '@/types/document'
import type { ApiResponse } from '@/types/auth'

export const documentApi = {
  // 解析 Markdown 为 UDM
  parseMarkdown(data: ParseRequest): Promise<ApiResponse<ParseResponse>> {
    return http.post('/document/parse', data)
  },

  // 导出 Word（返回二进制文件）
  exportWord(data: ExportRequest): Promise<Blob> {
    return http.post('/document/export', data, {
      responseType: 'blob'
    })
  },

  // 直接从 Markdown 导出 Word（一步到位，绕过 UDM 序列化）
  exportWordFromMarkdown(data: ParseRequest): Promise<Blob> {
    return http.post('/document/export/word/from-markdown', data, {
      responseType: 'blob'
    })
  }
}
