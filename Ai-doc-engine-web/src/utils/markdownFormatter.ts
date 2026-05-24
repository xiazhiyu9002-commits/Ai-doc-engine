/**
 * Markdown 公式格式化工具
 * 用于处理 Markdown 文档中的数学公式格式
 */

/**
 * 格式化 Markdown 中的数学公式
 * 识别 $$...$$ 和 \[...\] 格式的块级公式，确保其前后有换行符
 * 
 * @param content - 原始 Markdown 内容
 * @returns 格式化后的 Markdown 内容
 * 
 * @example
 * // 输入: "文本$$E=mc^2$$文本"
 * // 输出: "文本\n$$E=mc^2$$\n文本"
 * 
 * @example
 * // 输入: "\[\lim_{x\to 0}\frac{\sin x}{x}=1\]\[\lim_{x\to\infty}\left(1+\frac{1}{x}\right)^x=e\]"
 * // 输出: "\[\lim_{x\to 0}\frac{\sin x}{x}=1\]\n\n\[\lim_{x\to\infty}\left(1+\frac{1}{x}\right)^x=e\]"
 */
export function formatMathFormulas(content: string): string {
  if (!content || typeof content !== 'string') {
    return content
  }

  let result = content
  
  // 第一步：处理 \[...\] 格式的公式
  // 先在连续的公式之间添加换行：\]\[ -> \]\n\n\[
  result = result.replace(/\\\]\s*\\\[/g, '\\]\n\n\\[')
  
  // 确保 \[ 前面有换行（除非在行首或前面已经有换行）
  result = result.replace(/([^\n])\\\[/g, '$1\n\n\\[')
  
  // 确保 \] 后面有换行（除非在行尾或后面已经有换行）
  result = result.replace(/\\\]([^\n])/g, '\\]\n\n$1')
  
  // 清理可能产生的多余换行（超过2个连续换行的情况）
  result = result.replace(/\n{3,}/g, '\n\n')
  
  // 第二步：处理 $$...$$ 格式的公式
  const parts: string[] = []
  let lastIndex = 0
  
  const blockFormulaRegex = /\$\$([\s\S]*?)\$\$/g
  
  let match: RegExpExecArray | null
  while ((match = blockFormulaRegex.exec(result)) !== null) {
    const matchStart = match.index
    const matchEnd = matchStart + match[0].length
    
    parts.push(result.slice(lastIndex, matchStart))
    
    let formula = match[0]
    
    if (parts.length > 0) {
      const lastPart = parts[parts.length - 1]
      if (lastPart && !lastPart.endsWith('\n\n')) {
        if (lastPart.endsWith('\n')) {
          formula = '\n' + formula
        } else if (lastPart.trim()) {
          formula = '\n\n' + formula
        }
      }
    }
    
    if (matchEnd < result.length) {
      const afterContent = result.charAt(matchEnd)
      if (afterContent !== '\n') {
        formula = formula + '\n\n'
      } else if (matchEnd + 1 < result.length && result.charAt(matchEnd + 1) !== '\n') {
        formula = formula + '\n'
      }
    }
    
    parts.push(formula)
    lastIndex = matchEnd
  }
  
  if (lastIndex < result.length) {
    parts.push(result.slice(lastIndex))
  }
  
  return parts.length > 0 ? parts.join('') : result
}

/**
 * 检查内容中是否包含块级数学公式
 * @param content - Markdown 内容
 * @returns 是否包含块级公式
 */
export function hasBlockFormulas(content: string): boolean {
  if (!content || typeof content !== 'string') {
    return false
  }
  return /\$\$[\s\S]+?\$\$|\\\[[\s\S]+?\\\]/.test(content)
}

/**
 * 统计内容中块级公式的数量
 * @param content - Markdown 内容
 * @returns 块级公式的数量
 */
export function countBlockFormulas(content: string): number {
  if (!content || typeof content !== 'string') {
    return 0
  }
  const dollarMatches = content.match(/\$\$[\s\S]+?\$\$/g)
  const bracketMatches = content.match(/\\\[[\s\S]+?\\\]/g)
  return (dollarMatches ? dollarMatches.length : 0) + (bracketMatches ? bracketMatches.length : 0)
}

/**
 * 格式化选项
 */
export interface FormatOptions {
  /** 是否格式化块级公式（$$...$$ 和 \[...\]） */
  formatBlockFormulas?: boolean
  /** 是否保留原始公式内容不变 */
  preserveFormulaContent?: boolean
}

/**
 * 综合格式化 Markdown 内容
 * @param content - 原始 Markdown 内容
 * @param options - 格式化选项
 * @returns 格式化后的内容
 */
export function formatMarkdown(
  content: string, 
  options: FormatOptions = {}
): string {
  const {
    formatBlockFormulas = true
  } = options

  let result = content

  if (formatBlockFormulas) {
    result = formatMathFormulas(result)
  }

  return result
}
