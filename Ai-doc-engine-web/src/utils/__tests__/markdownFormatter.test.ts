import { describe, it, expect } from 'vitest'
import { 
  formatMathFormulas, 
  hasBlockFormulas, 
  countBlockFormulas,
  formatMarkdown 
} from '@/utils/markdownFormatter'

describe('formatMathFormulas', () => {
  describe('基本功能', () => {
    it('应该处理空字符串', () => {
      expect(formatMathFormulas('')).toBe('')
    })

    it('应该处理 null 和 undefined', () => {
      expect(formatMathFormulas(null as any)).toBe(null)
      expect(formatMathFormulas(undefined as any)).toBe(undefined)
    })

    it('应该处理没有公式的内容', () => {
      const content = '这是一段普通文本，没有数学公式。'
      expect(formatMathFormulas(content)).toBe(content)
    })
  })

  describe('块级公式格式化', () => {
    it('应该在公式前后添加换行符（当缺少时）', () => {
      const input = '文本$$E=mc^2$$文本'
      const result = formatMathFormulas(input)
      expect(result).toContain('\n$$E=mc^2$$\n')
    })

    it('应该保留已有的换行符', () => {
      const input = '文本\n$$E=mc^2$$\n文本'
      const result = formatMathFormulas(input)
      expect(result).toBe(input)
    })

    it('应该处理文档开头的公式', () => {
      const input = '$$E=mc^2$$文本'
      const result = formatMathFormulas(input)
      expect(result).toBe('$$E=mc^2$$\n文本')
    })

    it('应该处理文档结尾的公式', () => {
      const input = '文本$$E=mc^2$$'
      const result = formatMathFormulas(input)
      expect(result).toBe('文本\n$$E=mc^2$$')
    })

    it('应该处理单独一行的公式', () => {
      const input = '文本\n$$E=mc^2$$\n文本'
      const result = formatMathFormulas(input)
      expect(result).toBe(input)
    })
  })

  describe('复杂公式处理', () => {
    it('应该处理包含复杂 LaTeX 语法的公式', () => {
      const formula = "$$f'_+(x_0) = \\lim_{\\Delta x \\to 0^+} \\frac{f(x_0 + \\Delta x) - f(x_0)}{\\Delta x}$$"
      const input = `文本${formula}文本`
      const result = formatMathFormulas(input)
      expect(result).toContain('\n' + formula + '\n')
    })

    it('应该处理包含分数的公式', () => {
      const input = '文本$$\\frac{a}{b}$$文本'
      const result = formatMathFormulas(input)
      expect(result).toContain('\n$$\\frac{a}{b}$$\n')
    })

    it('应该处理包含积分的公式', () => {
      const input = '文本$$\\int_{0}^{1} x dx$$文本'
      const result = formatMathFormulas(input)
      expect(result).toContain('\n$$\\int_{0}^{1} x dx$$\n')
    })

    it('应该处理包含矩阵的公式', () => {
      const input = '文本$$\\begin{bmatrix}a & b\\\\c & d\\end{bmatrix}$$文本'
      const result = formatMathFormulas(input)
      expect(result).toContain('\n$$\\begin{bmatrix}a & b\\\\c & d\\end{bmatrix}$$\n')
    })
  })

  describe('多个公式处理', () => {
    it('应该处理多个公式', () => {
      const input = '文本$$a$$文本$$b$$文本'
      const result = formatMathFormulas(input)
      expect(result).toContain('\n$$a$$\n')
      expect(result).toContain('\n$$b$$\n')
    })

    it('应该处理连续的公式', () => {
      const input = '$$a$$$$b$$'
      const result = formatMathFormulas(input)
      expect(result).toBe('$$a$$\n$$b$$')
    })

    it('应该处理文档中的多个公式', () => {
      const input = `# 标题

这是第一段$$E=mc^2$$继续文本。

这是第二段$$F=ma$$继续文本。`
      
      const result = formatMathFormulas(input)
      expect(result).toContain('\n$$E=mc^2$$\n')
      expect(result).toContain('\n$$F=ma$$\n')
    })
  })

  describe('边界情况', () => {
    it('应该处理只有公式的内容', () => {
      const input = '$$E=mc^2$$'
      const result = formatMathFormulas(input)
      expect(result).toBe('$$E=mc^2$$')
    })

    it('应该处理公式前有多个换行的情况', () => {
      const input = '文本\n\n$$E=mc^2$$文本'
      const result = formatMathFormulas(input)
      expect(result).toContain('$$E=mc^2$$\n')
    })

    it('应该处理公式后有多个换行的情况', () => {
      const input = '文本$$E=mc^2$$\n\n文本'
      const result = formatMathFormulas(input)
      expect(result).toContain('\n$$E=mc^2$$')
    })
  })

  describe('公式内容完整性', () => {
    it('应该保持公式内容不变', () => {
      const formula = "$$f'_+(x_0) = \\lim_{\\Delta x \\to 0^+} \\frac{f(x_0 + \\Delta x) - f(x_0)}{\\Delta x}$$"
      const input = `文本${formula}文本`
      const result = formatMathFormulas(input)
      
      const formulaMatch = result.match(/\$\$[^$]+\$\$/)
      expect(formulaMatch).not.toBeNull()
      expect(formulaMatch![0]).toBe(formula)
    })

    it('不应该修改公式内部的任何内容', () => {
      const originalFormula = '$$\\alpha + \\beta = \\gamma$$'
      const input = `文本${originalFormula}文本`
      const result = formatMathFormulas(input)
      
      expect(result).toContain(originalFormula)
    })
  })
})

describe('hasBlockFormulas', () => {
  it('应该检测到块级公式', () => {
    expect(hasBlockFormulas('文本$$E=mc^2$$文本')).toBe(true)
  })

  it('应该检测不到块级公式', () => {
    expect(hasBlockFormulas('普通文本')).toBe(false)
  })

  it('应该处理空字符串', () => {
    expect(hasBlockFormulas('')).toBe(false)
  })

  it('应该处理 null 和 undefined', () => {
    expect(hasBlockFormulas(null as any)).toBe(false)
    expect(hasBlockFormulas(undefined as any)).toBe(false)
  })

  it('不应该检测行内公式', () => {
    expect(hasBlockFormulas('文本$E=mc^2$文本')).toBe(false)
  })
})

describe('countBlockFormulas', () => {
  it('应该正确计数单个公式', () => {
    expect(countBlockFormulas('文本$$E=mc^2$$文本')).toBe(1)
  })

  it('应该正确计数多个公式', () => {
    expect(countBlockFormulas('文本$$a$$文本$$b$$文本$$c$$文本')).toBe(3)
  })

  it('应该处理没有公式的内容', () => {
    expect(countBlockFormulas('普通文本')).toBe(0)
  })

  it('应该处理空字符串', () => {
    expect(countBlockFormulas('')).toBe(0)
  })

  it('应该处理 null 和 undefined', () => {
    expect(countBlockFormulas(null as any)).toBe(0)
    expect(countBlockFormulas(undefined as any)).toBe(0)
  })
})

describe('formatMarkdown', () => {
  it('应该使用默认选项格式化', () => {
    const input = '文本$$E=mc^2$$文本'
    const result = formatMarkdown(input)
    expect(result).toContain('\n$$E=mc^2$$\n')
  })

  it('应该支持禁用块级公式格式化', () => {
    const input = '文本$$E=mc^2$$文本'
    const result = formatMarkdown(input, { formatBlockFormulas: false })
    expect(result).toBe(input)
  })

  it('应该处理空内容', () => {
    expect(formatMarkdown('')).toBe('')
  })
})
