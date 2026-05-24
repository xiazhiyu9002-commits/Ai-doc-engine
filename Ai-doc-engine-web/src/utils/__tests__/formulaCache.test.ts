import { describe, it, expect, beforeEach, vi } from 'vitest'
import { FormulaCacheService } from '@/utils/formulaCache'

vi.mock('katex', () => ({
  default: {
    renderToString: vi.fn((latex: string, options: any) => {
      if (latex.includes('invalid')) {
        throw new Error('Invalid LaTeX')
      }
      return `<span class="katex">${latex}</span>`
    })
  }
}))

describe('FormulaCacheService', () => {
  beforeEach(() => {
    FormulaCacheService.clear()
  })

  describe('Basic Rendering', () => {
    it('should render simple formula', () => {
      const result = FormulaCacheService.render('x^2', false)
      expect(result).toContain('x^2')
    })

    it('should render inline formula', () => {
      const result = FormulaCacheService.render('x^2', true)
      expect(result).toContain('x^2')
    })

    it('should render block formula', () => {
      const result = FormulaCacheService.render('\\frac{a}{b}', false)
      expect(result).toContain('frac')
    })
  })

  describe('Caching', () => {
    it('should cache rendered formulas', () => {
      FormulaCacheService.render('x^2', false)
      FormulaCacheService.render('x^2', false)
      
      const stats = FormulaCacheService.getStats()
      expect(stats.hitCount).toBe(1)
    })

    it('should separate inline and block cache', () => {
      FormulaCacheService.render('x^2', true)
      FormulaCacheService.render('x^2', false)
      
      const stats = FormulaCacheService.getStats()
      expect(stats.missCount).toBe(2)
      expect(stats.hitCount).toBe(0)
    })

    it('should clear cache', () => {
      FormulaCacheService.render('x^2', false)
      FormulaCacheService.clear()
      
      const stats = FormulaCacheService.getStats()
      expect(stats.size).toBe(0)
    })

    it('should evict old entries when cache is full', () => {
      FormulaCacheService.setMaxSize(3)
      
      FormulaCacheService.render('a', false)
      FormulaCacheService.render('b', false)
      FormulaCacheService.render('c', false)
      FormulaCacheService.render('d', false)
      
      const stats = FormulaCacheService.getStats()
      expect(stats.size).toBe(3)
    })
  })

  describe('Error Handling', () => {
    it('should handle invalid LaTeX gracefully', () => {
      const result = FormulaCacheService.render('invalid{formula', false)
      expect(result).toContain('formula-error')
    })

    it('should return error message for failed rendering', () => {
      const result = FormulaCacheService.render('invalid', false)
      expect(result).toContain('Invalid LaTeX')
    })
  })

  describe('Statistics', () => {
    it('should track hit rate', () => {
      FormulaCacheService.render('x^2', false)
      FormulaCacheService.render('x^2', false)
      FormulaCacheService.render('y^2', false)
      
      const stats = FormulaCacheService.getStats()
      expect(stats.hitRate).toBe('33.33%')
    })

    it('should track cache size', () => {
      FormulaCacheService.render('x^2', false)
      FormulaCacheService.render('y^2', false)
      
      const stats = FormulaCacheService.getStats()
      expect(stats.size).toBe(2)
    })
  })

  describe('Configuration', () => {
    it('should allow setting max cache size', () => {
      FormulaCacheService.setMaxSize(100)
      const stats = FormulaCacheService.getStats()
      expect(stats.maxSize).toBe(100)
    })

    it('should evict entries when reducing max size', () => {
      FormulaCacheService.render('a', false)
      FormulaCacheService.render('b', false)
      FormulaCacheService.render('c', false)
      
      FormulaCacheService.setMaxSize(1)
      
      const stats = FormulaCacheService.getStats()
      expect(stats.size).toBe(1)
    })
  })
})

describe('FormulaCacheService - Complex Formulas', () => {
  beforeEach(() => {
    FormulaCacheService.clear()
  })

  it('should handle fractions', () => {
    const result = FormulaCacheService.render('\\frac{a}{b}', false)
    expect(result).toBeDefined()
  })

  it('should handle summations', () => {
    const result = FormulaCacheService.render('\\sum_{i=1}^{n} i', false)
    expect(result).toBeDefined()
  })

  it('should handle integrals', () => {
    const result = FormulaCacheService.render('\\int_{0}^{1} x dx', false)
    expect(result).toBeDefined()
  })

  it('should handle matrices', () => {
    const result = FormulaCacheService.render('\\begin{bmatrix}a & b\\\\c & d\\end{bmatrix}', false)
    expect(result).toBeDefined()
  })

  it('should handle Greek letters', () => {
    const result = FormulaCacheService.render('\\alpha + \\beta = \\gamma', false)
    expect(result).toBeDefined()
  })
})
