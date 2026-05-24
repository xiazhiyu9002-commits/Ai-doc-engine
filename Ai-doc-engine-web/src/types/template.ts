// 模板相关类型定义

export interface Template {
  id: number
  name: string
  description?: string
  isDefault?: boolean
  config: TemplateConfig
  createdAt?: string
}

export interface TemplateConfig {
  fontSize?: number
  fontFamily?: string
  lineSpacing?: number
  margins?: {
    top: number
    right: number
    bottom: number
    left: number
  }
  headerFooter?: {
    header?: string
    footer?: string
  }
}

export interface TemplateListResponse {
  templates: Template[]
}
