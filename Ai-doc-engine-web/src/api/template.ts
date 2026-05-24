// 模板相关接口

import http from './http'
import type { Template, TemplateListResponse } from '@/types/template'
import type { ApiResponse } from '@/types/auth'

export const templateApi = {
  // 获取模板列表
  getTemplates(): Promise<ApiResponse<TemplateListResponse>> {
    return http.get('/template/list')
  },

  // 获取模板详情
  getTemplateById(id: number): Promise<ApiResponse<Template>> {
    return http.get(`/template/${id}`)
  }
}
