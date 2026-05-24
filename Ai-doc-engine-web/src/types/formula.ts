// 公式相关类型定义

export interface FormulaOcrRequest {
  image: File
}

export interface FormulaOcrResponse {
  latex: string
  confidence: number
  contentType?: string  // 'formula' | 'flowchart' | 'table'
  candidates?: FormulaCandidate[]
}

export interface FormulaCandidate {
  latex: string
  confidence: number
}

export interface FormulaValidateRequest {
  latex: string
}

export interface FormulaValidateResponse {
  valid: boolean
  error?: string
  mathml?: string
}
