// 文档相关类型定义（仅支持 Markdown）

export interface ParseRequest {
  markdown: string
  templateId?: number
  fileName?: string
}

export interface RichText {
  text?: string
  bold?: boolean
  italic?: boolean
  strikethrough?: boolean
  code?: boolean
  linkUrl?: string
  inlineFormula?: string
}

export interface UdmBlock {
  type: 'heading' | 'paragraph' | 'list' | 'table' | 'formula' | 'flowchart' | 'codeblock' | 'tasklist' | 'footnote'
  content: any
  metadata?: Record<string, any>
  sourceStartOffset?: number
  sourceEndOffset?: number
  sourceStartLine?: number
  sourceEndLine?: number
}

export interface HeadingBlock extends UdmBlock {
  type: 'heading'
  content: {
    level: number
    text: string
  }
}

export interface ParagraphBlock extends UdmBlock {
  type: 'paragraph'
  content: {
    text?: string
    segments?: RichText[]
  }
}

export interface ListItemContent {
  segments: RichText[]
  nestedList?: ListContent
}

export interface ListContent {
  ordered: boolean
  items?: string[]
  itemContents?: ListItemContent[]
}

export interface ListBlock extends UdmBlock {
  type: 'list'
  content: ListContent
}

export interface TableBlock extends UdmBlock {
  type: 'table'
  content: {
    headers: string[]
    rows: string[][]
  }
}

export interface FormulaBlock extends UdmBlock {
  type: 'formula'
  content: {
    latex: string
    mathml?: string
    inline: boolean
  }
}

export interface FlowchartBlock extends UdmBlock {
  type: 'flowchart'
  content: {
    rawSource: string      // Mermaid 源码
    sourceType: string     // "mermaid"
    imageBase64?: string   // Base64 编码的图片
  }
}

// 保留旧的接口定义用于编辑器（已废弃）
export interface FlowchartNode {
  id: string
  text: string
  type: string
  position: {
    x: number
    y: number
  }
}

export interface FlowchartEdge {
  source: string
  target: string
  label?: string
}

export interface FlowchartLayout {
  direction?: 'TB' | 'LR' | 'BT' | 'RL'
  spacing?: {
    nodeSpacing: number
    rankSpacing: number
  }
}

export interface UdmDocument {
  blocks: UdmBlock[]
  metadata?: Record<string, any>
}

export interface ParseResponse {
  udm: UdmDocument
}

export interface ExportRequest {
  udm: UdmDocument
  templateId?: number
  fileName?: string
}

export interface ExportResponse {
  downloadUrl: string
  filename: string
}
