/**
 * AI 输出文本空格清理工具
 * 解决中英文混排产生的多余空格问题
 *
 * 保护区域（以下内容原样保留，不做空格清理）：
 * 1. 公式：$$...$$, $...$, \[...\], \(...\), \begin{}...\end{}
 * 2. 代码块：```...```, ~~~...~~~
 * 3. 行内代码：`...`
 * 4. MathML：<math>...</math>
 * 5. Markdown 表格（含管道符的多行）
 *
 * 清理规则（仅作用于保护区之外的普通文本）：
 * - 中文之间的多余空格移除
 * - 中文与英文/数字之间保留一个空格
 * - 中文与中文标点之间移除多余空格
 * - 仅折叠水平空白，保留换行符
 */

/**
 * 移除AI输出中的不可见字符
 * 这些字符常见于LLM输出，会破坏正则匹配和公式解析
 */
function removeInvisibleChars(text: string): string {
  return text
    .replace(/\u200B/g, '')    // 零宽空格 (Zero-width space)
    .replace(/\u200C/g, '')    // 零宽非连接符
    .replace(/\u200D/g, '')    // 零宽连接符
    .replace(/\uFEFF/g, '')    // 零宽不换行空格 / BOM
    .replace(/\u200E/g, '')    // 左至右标记
    .replace(/\u200F/g, '')    // 右至左标记
    .replace(/\u2060/g, '')    // 词连接符
    .replace(/\u00A0/g, ' ')   // 不换行空格 → 普通空格
    .replace(/\u3000/g, ' ')   // 全角空格 → 普通空格
    .replace(/\u2028/g, '\n')  // 行分隔符 → 换行
    .replace(/\u2029/g, '\n\n')// 段分隔符 → 双换行
}

/**
 * 保护模式列表（按优先级排列，先匹配的优先级高）
 * 每个模式包含正则和名称，匹配的内容会被原样保留
 */
function getProtectedPatterns(): Array<{ regex: RegExp; name: string }> {
  return [
    // LaTeX 环境：\begin{xxx} ... \end{xxx}（equation, align, matrix, cases, array等）
    // 必须在 $$ 前面，否则 \begin 会被 $$ 吞掉
    { regex: /\\begin\{[^}]+\}[\s\S]*?\\end\{[^}]+\}/g, name: 'latex-env' },

    // 块级公式 $$
    { regex: /\$\$[\s\S]*?\$\$/g, name: 'display-formula' },

    // LaTeX 原生块级 \[...\]
    { regex: /\\\[[\s\S]*?\\\]/g, name: 'bracket-display-formula' },

    // 行内公式 $...$（不跨行，防止误匹配货币符号）
    { regex: /(?<!\\)\$[^$\n]+?(?<!\\)\$/g, name: 'inline-formula' },

    // LaTeX 原生行内 \(...\)
    { regex: /\\\([\s\S]*?\\\)/g, name: 'paren-inline-formula' },

    // 围栏代码块 ```...```（三反引号）
    { regex: /```[\s\S]*?```/g, name: 'fenced-code-tick' },

    // 围栏代码块 ~~~...~~~（三波浪线，GitHub Flavored Markdown）
    { regex: /~~~[\s\S]*?~~~/g, name: 'fenced-code-tilde' },

    // MathML 标签
    { regex: /<math[\s\S]*?<\/math>/g, name: 'mathml' },

    // Markdown 表格：连续的 |...| 行（含表头分隔线）
    { regex: /(?:\|.+\|\s*\n)+(?=\n|$)/g, name: 'markdown-table' },

    // 行内代码 `...`（最短匹配，不跨行）
    { regex: /`[^`\n]+?`/g, name: 'inline-code' },
  ]
}

/**
 * 清理AI输出中英文混排的多余空格
 * @param text 原始文本
 * @returns 清理后的文本
 */
export function cleanAiOutputSpaces(text: string): string {
  if (!text || text.trim().length === 0) {
    return text
  }

  text = removeInvisibleChars(text)

  const blocks: Array<{ type: 'protected' | 'normal'; content: string }> = []
  let currentPos = 0

  const protectedPatterns = getProtectedPatterns()
  const allMatches: Array<{ start: number; end: number; content: string }> = []

  for (const { regex } of protectedPatterns) {
    let match: RegExpExecArray | null
    regex.lastIndex = 0
    while ((match = regex.exec(text)) !== null) {
      const start = match.index
      const end = match.index + match[0].length
      if (!isInsideExisting(allMatches, start, end)) {
        allMatches.push({ start, end, content: match[0] })
      }
    }
  }

  allMatches.sort((a, b) => a.start - b.start)

  const merged = mergeOverlappingIntervals(allMatches, text)

  for (const interval of merged) {
    if (interval.start > currentPos) {
      blocks.push({
        type: 'normal',
        content: text.slice(currentPos, interval.start),
      })
    }
    blocks.push({
      type: 'protected',
      content: interval.content,
    })
    currentPos = interval.end
  }

  if (currentPos < text.length) {
    blocks.push({
      type: 'normal',
      content: text.slice(currentPos),
    })
  }

  let result = ''
  for (const block of blocks) {
    if (block.type === 'protected') {
      result += block.content
    } else {
      result += cleanNormalText(block.content)
    }
  }

  return result
}

/**
 * 检查新区间是否与已有区间重叠
 * 用于实现"先匹配的优先级高"策略
 */
function isInsideExisting(
  existing: Array<{ start: number; end: number; content: string }>,
  start: number,
  end: number
): boolean {
  for (const m of existing) {
    if (start >= m.start && end <= m.end) {
      return true
    }
  }
  return false
}

function mergeOverlappingIntervals(
  intervals: Array<{ start: number; end: number; content: string }>,
  text: string
): Array<{ start: number; end: number; content: string }> {
  if (intervals.length === 0) return []

  intervals.sort((a, b) => a.start - b.start)
  const result = [intervals[0]]

  for (let i = 1; i < intervals.length; i++) {
    const last = result[result.length - 1]
    const current = intervals[i]

    if (current.start <= last.end) {
      last.end = Math.max(last.end, current.end)
      last.content = text.slice(last.start, last.end)
    } else {
      result.push(current)
    }
  }

  return result
}

/**
 * 清理普通文本（非保护区）中的空格
 * 仅折叠水平空白字符，保留换行符
 */
function cleanNormalText(text: string): string {
  let cleaned = text

  cleaned = cleaned.replace(/([\u4e00-\u9fa5])\s+([\u4e00-\u9fa5])/g, '$1$2')

  cleaned = cleaned.replace(/([\u4e00-\u9fa5])\s+([a-zA-Z0-9])/g, '$1 $2')
  cleaned = cleaned.replace(/([a-zA-Z0-9])\s+([\u4e00-\u9fa5])/g, '$1 $2')

  cleaned = cleaned.replace(
    /([\u4e00-\u9fa5])\s+([，。！？；：、《》【】「」『』“”'（）])/g,
    '$1$2'
  )
  cleaned = cleaned.replace(
    /([，。！？；：、《》【】「」『』“”'（）])\s+([\u4e00-\u9fa5])/g,
    '$1$2'
  )

  cleaned = cleaned.replace(/([\u4e00-\u9fa5])\s+([\(\[\{（【])/g, '$1$2')
  cleaned = cleaned.replace(/([\)\]\}）】])\s+([\u4e00-\u9fa5])/g, '$1$2')

  cleaned = cleaned.replace(/[^\S\n]+/g, ' ')

  return cleaned
}

/**
 * 清理多余换行
 * 将连续3个以上换行替换为2个
 * @param text 原始文本
 * @returns 清理后的文本
 */
export function cleanExtraNewlines(text: string): string {
  if (!text) return text
  return text.replace(/\n{3,}/g, '\n\n')
}

/**
 * 完整清理AI输出文本
 * @param text 原始文本
 * @returns 清理后的文本
 */
export function cleanAiOutputFull(text: string): string {
  let result = cleanAiOutputSpaces(text)
  result = cleanExtraNewlines(result)
  return result
}

/**
 * 获取统计信息（用于显示给用户）
 * @param original 原始文本
 * @param cleaned 清理后的文本
 * @returns 统计信息
 */
export function getCleanStats(original: string, cleaned: string): {
  removedInvisible: number
  removedSpaces: number
  removedNewlines: number
} {
  const originalSpaceCount = (original.match(/\s/g) || []).length
  const cleanedSpaceCount = (cleaned.match(/\s/g) || []).length
  const originalNewlineCount = (original.match(/\n/g) || []).length
  const cleanedNewlineCount = (cleaned.match(/\n/g) || []).length
  const originalInvisibleCount = (original.match(/[\u200B-\u200F\uFEFF\u2060]/g) || []).length

  return {
    removedInvisible: originalInvisibleCount,
    removedSpaces: Math.max(0, originalSpaceCount - cleanedSpaceCount),
    removedNewlines: Math.max(0, originalNewlineCount - cleanedNewlineCount),
  }
}