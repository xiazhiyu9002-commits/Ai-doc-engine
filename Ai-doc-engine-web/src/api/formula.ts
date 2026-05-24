// 公式相关接口

import http from './http'
import type {
  FormulaOcrResponse,
  FormulaValidateRequest,
  FormulaValidateResponse
} from '@/types/formula'
import type { ApiResponse } from '@/types/auth'

export const formulaApi = {
  // 公式截图 OCR 识别
  ocrImage(file: File): Promise<ApiResponse<FormulaOcrResponse>> {
    const formData = new FormData()
    formData.append('image', file)
    
    return http.post('/formula/ocr-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 校验公式
  validate(data: FormulaValidateRequest): Promise<ApiResponse<FormulaValidateResponse>> {
    return http.post('/formula/validate', data)
  }
}
