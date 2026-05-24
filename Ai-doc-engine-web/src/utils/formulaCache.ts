import katex from 'katex'
import 'katex/contrib/mhchem'

function decodeHtmlEntities(text: string): string {
  if (!text) return text
  
  let result = text
  
  result = result.replace(/&lt;/g, '<')
  result = result.replace(/&gt;/g, '>')
  result = result.replace(/&amp;/g, '&')
  result = result.replace(/&quot;/g, '"')
  result = result.replace(/&apos;/g, "'")
  result = result.replace(/&nbsp;/g, ' ')
  
  result = result.replace(/&#(\d+);/g, (_, num) => String.fromCharCode(parseInt(num)))
  
  return result
}

export class FormulaCacheService {
  private static cache = new Map<string, string>()
  private static maxSize = 500
  private static hitCount = 0
  private static missCount = 0

  static render(latex: string, inline: boolean): string {
    const decodedLatex = decodeHtmlEntities(latex)
    const cacheKey = `${inline ? 'inline' : 'block'}_${decodedLatex}`

    if (this.cache.has(cacheKey)) {
      this.hitCount++
      return this.cache.get(cacheKey)!
    }

    this.missCount++

    try {
      const html = katex.renderToString(decodedLatex, {
        throwOnError: false,
        displayMode: !inline,
        strict: false,
        trust: true,
        output: 'html', // 使用纯 HTML 输出，禁用菜单功能
        macros: {
          '\\N': '\\mathbb{N}',
          '\\R': '\\mathbb{R}',
          '\\Z': '\\mathbb{Z}',
          '\\Q': '\\mathbb{Q}',
          '\\C': '\\mathbb{C}'
        }
      })

      if (this.cache.size >= this.maxSize) {
        const firstKey = this.cache.keys().next().value
        if (firstKey) {
          this.cache.delete(firstKey)
        }
      }

      this.cache.set(cacheKey, html)
      return html
    } catch (e: any) {
      return `<span class="formula-error" style="color: #f56c6c;">${e.message || '公式渲染失败'}</span>`
    }
  }

  static clear(): void {
    this.cache.clear()
    this.hitCount = 0
    this.missCount = 0
  }

  static getStats() {
    return {
      size: this.cache.size,
      maxSize: this.maxSize,
      hitCount: this.hitCount,
      missCount: this.missCount,
      hitRate: this.hitCount + this.missCount > 0 
        ? (this.hitCount / (this.hitCount + this.missCount) * 100).toFixed(2) + '%'
        : '0%'
    }
  }

  static setMaxSize(size: number): void {
    this.maxSize = size
    if (this.cache.size > this.maxSize) {
      const keysToDelete = Array.from(this.cache.keys()).slice(0, this.cache.size - this.maxSize)
      keysToDelete.forEach(key => this.cache.delete(key))
    }
  }
}
