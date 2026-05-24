import { describe, it, expect, beforeEach } from 'vitest'
import katex from 'katex'

describe('KaTeX Integration Tests', () => {
  describe('Basic Formula Rendering', () => {
    it('should render simple variable', () => {
      const html = katex.renderToString('x', { throwOnError: false })
      expect(html).toContain('katex')
    })

    it('should render superscript', () => {
      const html = katex.renderToString('x^2', { throwOnError: false })
      expect(html).toContain('katex')
    })

    it('should render subscript', () => {
      const html = katex.renderToString('x_i', { throwOnError: false })
      expect(html).toContain('katex')
    })

    it('should render fraction', () => {
      const html = katex.renderToString('\\frac{a}{b}', { throwOnError: false })
      expect(html).toContain('katex')
    })
  })

  describe('Display Mode', () => {
    it('should render inline mode', () => {
      const html = katex.renderToString('x^2', { 
        throwOnError: false, 
        displayMode: false 
      })
      expect(html).toContain('katex')
    })

    it('should render display mode', () => {
      const html = katex.renderToString('x^2', { 
        throwOnError: false, 
        displayMode: true 
      })
      expect(html).toContain('katex-display')
    })
  })

  describe('Complex Formulas', () => {
    it('should render quadratic formula', () => {
      const latex = 'x = \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a}'
      const html = katex.renderToString(latex, { throwOnError: false })
      expect(html).toContain('katex')
    })

    it('should render summation', () => {
      const latex = '\\sum_{i=1}^{n} i = \\frac{n(n+1)}{2}'
      const html = katex.renderToString(latex, { throwOnError: false })
      expect(html).toContain('katex')
    })

    it('should render integral', () => {
      const latex = '\\int_{0}^{\\infty} e^{-x^2} dx'
      const html = katex.renderToString(latex, { throwOnError: false })
      expect(html).toContain('katex')
    })

    it('should render matrix', () => {
      const latex = '\\begin{bmatrix}a & b \\\\ c & d\\end{bmatrix}'
      const html = katex.renderToString(latex, { throwOnError: false })
      expect(html).toContain('katex')
    })

    it('should render limit', () => {
      const latex = '\\lim_{x \\to \\infty} f(x)'
      const html = katex.renderToString(latex, { throwOnError: false })
      expect(html).toContain('katex')
    })
  })

  describe('Greek Letters', () => {
    it('should render lowercase Greek letters', () => {
      const letters = ['\\alpha', '\\beta', '\\gamma', '\\delta', '\\theta']
      letters.forEach(letter => {
        const html = katex.renderToString(letter, { throwOnError: false })
        expect(html).toContain('katex')
      })
    })

    it('should render uppercase Greek letters', () => {
      const letters = ['\\Gamma', '\\Delta', '\\Theta', '\\Lambda', '\\Omega']
      letters.forEach(letter => {
        const html = katex.renderToString(letter, { throwOnError: false })
        expect(html).toContain('katex')
      })
    })
  })

  describe('Special Symbols', () => {
    it('should render infinity', () => {
      const html = katex.renderToString('\\infty', { throwOnError: false })
      expect(html).toContain('katex')
    })

    it('should render partial derivative', () => {
      const html = katex.renderToString('\\frac{\\partial f}{\\partial x}', { throwOnError: false })
      expect(html).toContain('katex')
    })

    it('should render nabla', () => {
      const html = katex.renderToString('\\nabla f', { throwOnError: false })
      expect(html).toContain('katex')
    })
  })

  describe('Error Handling', () => {
    it('should not throw on invalid LaTeX when throwOnError is false', () => {
      expect(() => {
        katex.renderToString('\\invalidCommand', { throwOnError: false })
      }).not.toThrow()
    })

    it('should throw on invalid LaTeX when throwOnError is true', () => {
      expect(() => {
        katex.renderToString('\\invalidCommand', { throwOnError: true })
      }).toThrow()
    })
  })

  describe('Performance', () => {
    it('should render multiple formulas efficiently', () => {
      const formulas = [
        'x^2',
        '\\frac{a}{b}',
        '\\sum_{i=1}^{n} i',
        '\\int_{0}^{1} x dx',
        '\\sqrt{x}'
      ]

      const startTime = performance.now()
      
      for (let i = 0; i < 100; i++) {
        formulas.forEach(formula => {
          katex.renderToString(formula, { throwOnError: false })
        })
      }
      
      const endTime = performance.now()
      const duration = endTime - startTime
      
      expect(duration).toBeLessThan(1000)
    })
  })
})
