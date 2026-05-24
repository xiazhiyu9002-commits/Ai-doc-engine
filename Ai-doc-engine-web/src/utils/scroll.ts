/**
 * 滚动相关工具函数
 */

/**
 * 节流函数
 * @param fn 要节流的函数
 * @param delay 延迟时间（毫秒）
 * @returns 节流后的函数
 */
export function throttle<T extends (...args: any[]) => any>(
  fn: T,
  delay: number
): (...args: Parameters<T>) => void {
  let lastCall = 0
  let timeoutId: number | null = null

  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()
    const timeSinceLastCall = now - lastCall

    // 如果距离上次调用时间超过delay，立即执行
    if (timeSinceLastCall >= delay) {
      lastCall = now
      fn.apply(this, args)
    } else {
      // 否则，设置定时器在剩余时间后执行
      if (timeoutId) {
        clearTimeout(timeoutId)
      }
      timeoutId = window.setTimeout(() => {
        lastCall = Date.now()
        fn.apply(this, args)
        timeoutId = null
      }, delay - timeSinceLastCall)
    }
  }
}

/**
 * 获取文本中指定位置所在的行号（从1开始）
 * @param text 完整文本
 * @param position 字符位置
 * @returns 行号（从1开始）
 */
export function getLineNumber(text: string, position: number): number {
  if (position <= 0) return 1
  const textBeforePosition = text.substring(0, position)
  const lineBreaks = textBeforePosition.split('\n').length
  return lineBreaks
}

/**
 * 获取指定行号在文本中的起始位置
 * @param text 完整文本
 * @param lineNumber 行号（从1开始）
 * @returns 起始位置
 */
export function getLineStartPosition(text: string, lineNumber: number): number {
  if (lineNumber <= 1) return 0
  
  const lines = text.split('\n')
  let position = 0
  
  for (let i = 0; i < lineNumber - 1 && i < lines.length; i++) {
    position += lines[i].length + 1 // +1 是换行符
  }
  
  return position
}

/**
 * 根据滚动位置计算可见的第一行行号
 * @param textarea textarea元素
 * @param text 文本内容
 * @returns 行号（从1开始）
 */
export function getFirstVisibleLine(
  textarea: HTMLTextAreaElement,
  _text: string
): number {
  const { scrollTop } = textarea
  
  // 计算每个字符的垂直位置（近似）
  const lineHeight = getLineHeight(textarea)
  const visibleTopLine = Math.floor(scrollTop / lineHeight) + 1
  
  return Math.max(1, visibleTopLine)
}

/**
 * 获取textarea的行高
 * @param textarea textarea元素
 * @returns 行高（像素）
 */
export function getLineHeight(textarea: HTMLTextAreaElement): number {
  const computedStyle = window.getComputedStyle(textarea)
  const lineHeight = parseFloat(computedStyle.lineHeight)
  
  // 如果lineHeight是normal，使用fontSize * 1.2作为近似值
  if (isNaN(lineHeight)) {
    const fontSize = parseFloat(computedStyle.fontSize)
    return fontSize * 1.2
  }
  
  return lineHeight
}

/**
 * 滚动数据接口
 */
export interface ScrollData {
  scrollTop: number
  scrollHeight: number
  clientHeight: number
  firstVisibleLine?: number // 可见的第一行行号
  visibleStartRatio?: number // 可见区域开始位置的比例
  visibleEndRatio?: number // 可见区域结束位置的比例
}

/**
 * 块位置映射接口（增强版）
 */
export interface BlockPosition {
  index: number // 块索引
  startLine: number // 起始行号
  endLine: number // 结束行号
  startChar: number // 起始字符位置
  endChar: number // 结束字符位置
  blockType: string // 块类型
  element?: HTMLElement // 对应的DOM元素（预览面板）
  renderHeight?: number // 渲染高度（像素）
}

/**
 * Markdown块类型检测模式
 */
interface BlockPattern {
  type: string
  startPattern: RegExp
  endPattern?: RegExp
  isMultiline: boolean
}

/**
 * Markdown块模式定义
 */
const BLOCK_PATTERNS: BlockPattern[] = [
  {
    type: 'codeblock',
    startPattern: /^```/,
    endPattern: /^```/,
    isMultiline: true
  },
  {
    type: 'formula',
    startPattern: /^\$\$$/,
    endPattern: /^\$\$$/,
    isMultiline: true
  },
  {
    type: 'heading',
    startPattern: /^#{1,6}\s/,
    isMultiline: false
  },
  {
    type: 'table',
    startPattern: /^\|/,
    isMultiline: true
  },
  {
    type: 'list',
    startPattern: /^(\s*[-*+]|\s*\d+\.)\s/,
    isMultiline: true
  },
  {
    type: 'flowchart',
    startPattern: /^```(?:mermaid|flowchart|graph)/,
    endPattern: /^```/,
    isMultiline: true
  }
]

/**
 * 解析Markdown文本，构建精确的块位置映射
 * @param markdown Markdown文本
 * @param udmBlocks UDM块数组（可选，用于验证）
 * @returns 块位置映射数组
 */
export function buildPreciseBlockPositions(
  markdown: string,
  udmBlocks?: any[]
): BlockPosition[] {
  const positions: BlockPosition[] = []
  const lines = markdown.split('\n')
  
  let currentChar = 0
  let i = 0
  
  while (i < lines.length) {
    const line = lines[i]
    const lineStartChar = currentChar
    const lineEndChar = currentChar + line.length
    
    // 跳过空行
    if (line.trim().length === 0) {
      currentChar = lineEndChar + 1 // +1 for newline
      i++
      continue
    }
    
    // 检测块类型
    const blockInfo = detectBlockType(lines, i)
    
    if (blockInfo) {
      const blockEndLine = findBlockEnd(lines, i, blockInfo)
      const blockEndChar = calculateCharPosition(lines, blockEndLine)
      
      positions.push({
        index: positions.length,
        startLine: i + 1, // 行号从1开始
        endLine: blockEndLine + 1,
        startChar: lineStartChar,
        endChar: blockEndChar,
        blockType: blockInfo.type
      })
      
      // 跳到块结束后的下一行
      i = blockEndLine + 1
      currentChar = blockEndChar + 1
    } else {
      // 普通段落
      const paragraphEndLine = findParagraphEnd(lines, i)
      const paragraphEndChar = calculateCharPosition(lines, paragraphEndLine)
      
      positions.push({
        index: positions.length,
        startLine: i + 1,
        endLine: paragraphEndLine + 1,
        startChar: lineStartChar,
        endChar: paragraphEndChar,
        blockType: 'paragraph'
      })
      
      i = paragraphEndLine + 1
      currentChar = paragraphEndChar + 1
    }
  }
  
  // 如果提供了UDM块，进行匹配验证和修正
  if (udmBlocks && udmBlocks.length > 0) {
    return alignWithUdmBlocks(positions, udmBlocks)
  }
  
  return positions
}

/**
 * 检测块的类型
 */
function detectBlockType(lines: string[], lineIndex: number): BlockPattern | null {
  const line = lines[lineIndex].trim()
  
  for (const pattern of BLOCK_PATTERNS) {
    if (pattern.startPattern.test(line)) {
      return pattern
    }
  }
  
  return null
}

/**
 * 查找块的结束行
 */
function findBlockEnd(lines: string[], startLine: number, blockInfo: BlockPattern): number {
  if (!blockInfo.isMultiline) {
    return startLine
  }
  
  // 对于代码块、公式块等，查找结束标记
  if (blockInfo.endPattern) {
    for (let i = startLine + 1; i < lines.length; i++) {
      if (blockInfo.endPattern.test(lines[i].trim())) {
        return i
      }
    }
    return lines.length - 1
  }
  
  // 对于表格，查找非表格行
  if (blockInfo.type === 'table') {
    for (let i = startLine + 1; i < lines.length; i++) {
      const line = lines[i].trim()
      if (line.length === 0 || !line.startsWith('|')) {
        return i - 1
      }
    }
    return lines.length - 1
  }
  
  // 对于列表，查找非列表项
  if (blockInfo.type === 'list') {
    for (let i = startLine + 1; i < lines.length; i++) {
      const line = lines[i].trim()
      if (line.length === 0) {
        // 空行后可能还有列表项（缩进列表）
        continue
      }
      if (!/^(\s*[-*+]|\s*\d+\.)\s/.test(line)) {
        return i - 1
      }
    }
    return lines.length - 1
  }
  
  return startLine
}

/**
 * 查找段落的结束行
 */
function findParagraphEnd(lines: string[], startLine: number): number {
  for (let i = startLine + 1; i < lines.length; i++) {
    const line = lines[i].trim()
    
    // 空行表示段落结束
    if (line.length === 0) {
      return i - 1
    }
    
    // 检测是否是新块的开始
    for (const pattern of BLOCK_PATTERNS) {
      if (pattern.startPattern.test(line)) {
        return i - 1
      }
    }
  }
  
  return lines.length - 1
}

/**
 * 计算指定行的结束字符位置
 */
function calculateCharPosition(lines: string[], lineIndex: number): number {
  let position = 0
  for (let i = 0; i <= lineIndex && i < lines.length; i++) {
    position += lines[i].length
    if (i < lineIndex) {
      position += 1 // newline
    }
  }
  return position
}

/**
 * 与UDM块对齐（修正位置映射）
 */
function alignWithUdmBlocks(markdownPositions: BlockPosition[], udmBlocks: any[]): BlockPosition[] {
  const result: BlockPosition[] = []
  
  // 简单策略：按顺序匹配，优先使用Markdown解析的位置
  const maxLen = Math.max(markdownPositions.length, udmBlocks.length)
  
  for (let i = 0; i < maxLen; i++) {
    const mdPos = markdownPositions[i]
    const udmBlock = udmBlocks[i]
    
    if (mdPos && udmBlock) {
      // 使用Markdown解析的位置，但使用UDM的类型
      result.push({
        ...mdPos,
        blockType: udmBlock.type || mdPos.blockType
      })
    } else if (mdPos) {
      result.push(mdPos)
    }
  }
  
  return result
}

/**
 * 根据行号找到对应的块索引（精确版）
 * @param blockPositions 块位置映射数组
 * @param lineNumber 行号
 * @returns 块索引和块内相对位置
 */
export function findBlockByLinePrecise(
  blockPositions: BlockPosition[],
  lineNumber: number
): { blockIndex: number; relativePosition: number } | null {
  for (let i = 0; i < blockPositions.length; i++) {
    const block = blockPositions[i]
    if (lineNumber >= block.startLine && lineNumber <= block.endLine) {
      // 计算块内相对位置（0-1）
      const blockLines = block.endLine - block.startLine + 1
      const relativeLine = lineNumber - block.startLine
      const relativePosition = blockLines > 1 ? relativeLine / (blockLines - 1) : 0
      
      return { blockIndex: i, relativePosition }
    }
  }
  
  // 如果行号超出所有块，返回最后一个块
  if (blockPositions.length > 0 && lineNumber > blockPositions[blockPositions.length - 1].endLine) {
    return { blockIndex: blockPositions.length - 1, relativePosition: 1 }
  }
  
  return null
}

/**
 * 根据字符位置找到对应的块索引
 * @param blockPositions 块位置映射数组
 * @param charPosition 字符位置
 * @returns 块索引和块内相对位置
 */
export function findBlockByCharPosition(
  blockPositions: BlockPosition[],
  charPosition: number
): { blockIndex: number; relativePosition: number } | null {
  for (let i = 0; i < blockPositions.length; i++) {
    const block = blockPositions[i]
    if (charPosition >= block.startChar && charPosition <= block.endChar) {
      // 计算块内相对位置（0-1）
      const blockLength = block.endChar - block.startChar + 1
      const relativeChar = charPosition - block.startChar
      const relativePosition = blockLength > 1 ? relativeChar / (blockLength - 1) : 0
      
      return { blockIndex: i, relativePosition }
    }
  }
  
  // 如果字符位置超出所有块，返回最后一个块
  if (blockPositions.length > 0 && charPosition > blockPositions[blockPositions.length - 1].endChar) {
    return { blockIndex: blockPositions.length - 1, relativePosition: 1 }
  }
  
  return null
}

/**
 * 根据行号找到对应的块索引（向后兼容）
 * @param blockPositions 块位置映射数组
 * @param lineNumber 行号
 * @returns 块索引，如果找不到返回0
 */
export function findBlockByLine(
  blockPositions: BlockPosition[],
  lineNumber: number
): number {
  const result = findBlockByLinePrecise(blockPositions, lineNumber)
  return result ? result.blockIndex : 0
}

/**
 * 计算滚动位置的精确映射
 * @param sourceScrollTop 源滚动位置
 * @param sourceScrollHeight 源滚动高度
 * @param targetScrollHeight 目标滚动高度
 * @param blockPositions 块位置映射
 * @param currentLine 当前行号
 * @returns 目标滚动位置
 */
export function calculatePreciseScrollPosition(
  sourceScrollTop: number,
  sourceScrollHeight: number,
  targetScrollHeight: number,
  blockPositions: BlockPosition[],
  currentLine: number
): number {
  const result = findBlockByLinePrecise(blockPositions, currentLine)
  
  if (!result) {
    // 降级为百分比映射
    const maxSourceScroll = sourceScrollHeight
    const maxTargetScroll = targetScrollHeight
    const percentage = sourceScrollTop / maxSourceScroll
    return maxTargetScroll * percentage
  }
  
  // 使用块索引和相对位置计算
  // 这里需要结合预览面板的实际渲染高度
  // 暂时返回百分比映射的结果
  const maxSourceScroll = sourceScrollHeight
  const maxTargetScroll = targetScrollHeight
  const percentage = sourceScrollTop / maxSourceScroll
  return maxTargetScroll * percentage
}

/**
 * 平滑滚动到指定位置
 * @param element 要滚动的元素
 * @param scrollTop 目标滚动位置
 * @param duration 动画持续时间（毫秒）
 */
export function smoothScrollTo(
  element: HTMLElement,
  scrollTop: number,
  duration: number = 200
): void {
  const startScrollTop = element.scrollTop
  const distance = scrollTop - startScrollTop
  
  // 如果距离很小，直接设置
  if (Math.abs(distance) < 5) {
    element.scrollTop = scrollTop
    return
  }
  
  const startTime = performance.now()

  function step(currentTime: number) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // 使用easeInOutQuad缓动函数
    const easeProgress = progress < 0.5
      ? 2 * progress * progress
      : 1 - Math.pow(-2 * progress + 2, 2) / 2
    
    element.scrollTop = startScrollTop + distance * easeProgress
    
    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }

  requestAnimationFrame(step)
}

/**
 * 获取元素在容器中的可见比例
 * @param element 目标元素
 * @param container 容器元素
 * @returns 可见比例（0-1）
 */
export function getVisibleRatio(element: HTMLElement, container: HTMLElement): number {
  const containerRect = container.getBoundingClientRect()
  const elementRect = element.getBoundingClientRect()
  
  const visibleTop = Math.max(elementRect.top, containerRect.top)
  const visibleBottom = Math.min(elementRect.bottom, containerRect.bottom)
  
  if (visibleTop >= visibleBottom) {
    return 0 // 完全不可见
  }
  
  const visibleHeight = visibleBottom - visibleTop
  const totalHeight = elementRect.height
  
  return visibleHeight / totalHeight
}

/**
 * 计算元素在容器中的滚动位置
 * @param element 目标元素
 * @param container 容器元素
 * @param alignment 对齐方式 ('top' | 'center' | 'bottom')
 * @returns 滚动位置
 */
export function calculateScrollPosition(
  element: HTMLElement,
  container: HTMLElement,
  alignment: 'top' | 'center' | 'bottom' = 'top'
): number {
  const containerRect = container.getBoundingClientRect()
  const elementRect = element.getBoundingClientRect()
  
  const offsetTop = elementRect.top - containerRect.top + container.scrollTop
  
  switch (alignment) {
    case 'top':
      return offsetTop - 20 // 留出20px顶部间距
    case 'center':
      return offsetTop - container.clientHeight / 2 + elementRect.height / 2
    case 'bottom':
      return offsetTop - container.clientHeight + elementRect.height + 20
    default:
      return offsetTop - 20
  }
}

/**
 * 查找当前可见的块索引
 * @param blockElements 块元素数组
 * @param container 滚动容器
 * @returns 当前可见的块索引和可见比例
 */
export function findVisibleBlock(
  blockElements: (HTMLElement | null)[],
  container: HTMLElement
): { index: number; ratio: number } | null {
  let maxRatio = 0
  let maxRatioIndex = -1
  
  for (let i = 0; i < blockElements.length; i++) {
    const element = blockElements[i]
    if (!element) continue
    
    const ratio = getVisibleRatio(element, container)
    if (ratio > maxRatio) {
      maxRatio = ratio
      maxRatioIndex = i
    }
  }
  
  if (maxRatioIndex >= 0) {
    return { index: maxRatioIndex, ratio: maxRatio }
  }
  
  return null
}

/**
 * 调试日志（仅在开发环境输出）
 */
export function debugLog(..._args: any[]): void {
}

/**
 * ============================================
 * 基于DOM测量的精确映射系统
 * ============================================
 */

/**
 * 行位置信息（编辑器）
 */
export interface LinePosition {
  top: number      // 累积的顶部位置
  height: number   // 行的实际高度（考虑换行）
}

/**
 * 块位置信息（预览）
 */
export interface BlockPositionInfo {
  top: number      // 块的顶部位置
  height: number   // 块的高度
}

/**
 * 基于DOM测量的精确滚动同步映射器
 * 解决行内换行导致的映射不准确问题
 */
export class ScrollSyncMapper {
  private editorLinePositions: Map<number, LinePosition> = new Map()
  
  private previewBlockPositions: Map<number, BlockPositionInfo> = new Map()
  
  private lineToBlockMap: Map<number, number> = new Map()
  
  private blockToLineMap: Map<number, number> = new Map()
  
  private cachedText: string = ''
  
  private sortedEditorLines: number[] = []
  
  private measureTimeout: number | null = null
  private pendingMeasure = false

  measureEditorLines(textarea: HTMLTextAreaElement, text: string): void {
    if (this.pendingMeasure) {
      return
    }
    
    this.pendingMeasure = true
    
    if (this.measureTimeout) {
      clearTimeout(this.measureTimeout)
    }
    
    this.measureTimeout = window.setTimeout(() => {
      this.pendingMeasure = false
      this._doMeasureEditorLines(textarea, text)
    }, 150) // 减少延迟，提高响应速度
  }
  
  private _doMeasureEditorLines(textarea: HTMLTextAreaElement, text: string): void {
    const startTime = performance.now()
    
    if (this.cachedText === text && this.editorLinePositions.size > 0) {
      debugLog('编辑器行位置未变化，跳过测量')
      return
    }
    
    this.editorLinePositions.clear()
    this.cachedText = text
    
    const style = getComputedStyle(textarea)
    const lineHeight = parseFloat(style.lineHeight) || parseFloat(style.fontSize) * 1.6
    
    // 使用固定行高计算，更准确且性能更好
    const lines = text.split('\n')
    let accumulatedTop = 0
    
    lines.forEach((_, index) => {
      this.editorLinePositions.set(index + 1, {
        top: accumulatedTop,
        height: lineHeight
      })
      
      accumulatedTop += lineHeight
    })
    
    this.buildSortedArrays()
    
    const endTime = performance.now()
    debugLog(`编辑器行位置测量完成: ${this.editorLinePositions.size} 行, 耗时 ${(endTime - startTime).toFixed(2)}ms`)
  }
  
  private buildSortedArrays(): void {
    this.sortedEditorLines = Array.from(this.editorLinePositions.keys()).sort((a, b) => a - b)
  }
  
  measurePreviewBlocks(container: HTMLElement): void {
    const startTime = performance.now()
    
    this.previewBlockPositions.clear()
    
    if (!container) {
      debugLog('预览块位置测量失败: 容器为空')
      return
    }
    
    const containerRect = container.getBoundingClientRect()
    const blocks = container.querySelectorAll('[data-block-index]')
    
    blocks.forEach((block) => {
      const index = parseInt(block.getAttribute('data-block-index') || '0')
      const rect = block.getBoundingClientRect()
      
      this.previewBlockPositions.set(index, {
        top: rect.top - containerRect.top + container.scrollTop,
        height: rect.height
      })
    })
    
    this.buildSortedArrays()
    
    const endTime = performance.now()
    debugLog(`预览块位置测量完成: ${this.previewBlockPositions.size} 块, 耗时 ${(endTime - startTime).toFixed(2)}ms`)
  }
  
  /**
   * 建立行号到块索引的映射
   * 基于Markdown解析结果
   * @param markdown Markdown文本
   * @param udmBlocks UDM块数组
   */
  buildLineToBlockMapping(
    markdown: string, 
    udmBlocks: any[]
  ): void {
    this.lineToBlockMap.clear()
    this.blockToLineMap.clear()
    
    if (!udmBlocks || udmBlocks.length === 0) {
      debugLog('行-块映射构建失败: UDM块为空')
      return
    }
    
    const lines = markdown.split('\n')
    let currentBlockIndex = 0
    
    // 遍历每一行，建立映射
    for (let i = 0; i < lines.length && currentBlockIndex < udmBlocks.length; i++) {
      const line = lines[i].trim()
      
      if (line.length === 0) {
        continue
      }
      
      // 获取当前块信息
      const currentBlock = udmBlocks[currentBlockIndex]
      if (!currentBlock) {
        break
      }
      
      const blockType = currentBlock.type
      
      // 根据块类型确定跨越的行数
      let blockEndLine = i + 1
      
      if (blockType === 'codeblock' || blockType === 'code') {
        // 代码块：找到结束标记
        for (let j = i + 1; j < lines.length; j++) {
          if (lines[j].trim().startsWith('```')) {
            blockEndLine = j + 1
            break
          }
        }
      } else if (blockType === 'table') {
        // 表格：找到非表格行
        for (let j = i + 1; j < lines.length; j++) {
          if (!lines[j].trim().startsWith('|') && lines[j].trim().length > 0) {
            blockEndLine = j
            break
          }
        }
      } else if (blockType === 'formula') {
        // 公式块：找到结束标记
        for (let j = i + 1; j < lines.length; j++) {
          if (lines[j].trim().endsWith('$$')) {
            blockEndLine = j + 1
            break
          }
        }
      } else if (blockType === 'flowchart') {
        // 流程图：找到结束标记
        for (let j = i + 1; j < lines.length; j++) {
          if (lines[j].trim().startsWith('```')) {
            blockEndLine = j + 1
            break
          }
        }
      } else if (blockType === 'list') {
        // 列表：找到非列表项
        for (let j = i + 1; j < lines.length; j++) {
          const nextLine = lines[j].trim()
          if (nextLine.length > 0 && !/^(\s*[-*+]|\s*\d+\.)\s/.test(nextLine)) {
            blockEndLine = j
            break
          }
        }
      } else {
        // 普通段落：找到空行或新块
        for (let j = i + 1; j < lines.length; j++) {
          const nextLine = lines[j].trim()
          if (nextLine.length === 0) {
            blockEndLine = j
            break
          }
          // 检测是否是新块的开始
          if (/^(#{1,6}\s|```|\$\$|\||[-*+]\s|\d+\.\s)/.test(nextLine)) {
            blockEndLine = j
            break
          }
        }
      }
      
      // 建立映射：该块覆盖的所有行都映射到当前块索引
      for (let lineNum = i + 1; lineNum <= blockEndLine; lineNum++) {
        this.lineToBlockMap.set(lineNum, currentBlockIndex)
      }
      
      // 块索引映射到起始行
      this.blockToLineMap.set(currentBlockIndex, i + 1)
      
      currentBlockIndex++
      i = blockEndLine - 1
    }
    
  }
  
  findPreviewPosition(editorScrollTop: number): { scrollTop: number } | null {
    if (this.editorLinePositions.size === 0 || this.previewBlockPositions.size === 0) {
      return null
    }
    
    const currentLine = this.findLineByScrollTopBinary(editorScrollTop)
    
    const blockIndex = this.lineToBlockMap.get(currentLine)
    if (blockIndex === undefined) {
      return this.fallbackToPercentage(editorScrollTop, 'editor')
    }
    
    const blockPos = this.previewBlockPositions.get(blockIndex)
    if (!blockPos) {
      return this.fallbackToPercentage(editorScrollTop, 'editor')
    }
    
    const linePos = this.editorLinePositions.get(currentLine)
    if (!linePos) {
      return { scrollTop: blockPos.top }
    }
    
    const blockLines = this.getBlockLines(blockIndex)
    const totalBlockHeight = this.calculateBlockHeight(blockLines.start, blockLines.end)
    
    const lineOffsetInBlock = this.calculateOffsetInBlock(blockLines.start, currentLine, editorScrollTop)
    
    let targetScrollTop = blockPos.top
    if (totalBlockHeight > 0) {
      const progress = lineOffsetInBlock / totalBlockHeight
      targetScrollTop = blockPos.top + blockPos.height * Math.min(progress, 1)
    }
    
    return { scrollTop: targetScrollTop }
  }
  
  private findLineByScrollTopBinary(scrollTop: number): number {
    if (this.sortedEditorLines.length === 0) return 1
    
    let left = 0
    let right = this.sortedEditorLines.length - 1
    
    while (left < right) {
      const mid = Math.floor((left + right) / 2)
      const lineNum = this.sortedEditorLines[mid]
      const pos = this.editorLinePositions.get(lineNum)
      
      if (!pos) {
        left = mid + 1
        continue
      }
      
      if (pos.top + pos.height <= scrollTop) {
        left = mid + 1
      } else {
        right = mid
      }
    }
    
    return this.sortedEditorLines[left] || 1
  }
  
  /**
   * 根据预览滚动位置找到对应的编辑器位置
   * @param previewScrollTop 预览滚动位置
   * @returns 编辑器位置
   */
  findEditorPosition(previewScrollTop: number): { scrollTop: number; line: number } | null {
    if (this.previewBlockPositions.size === 0 || this.editorLinePositions.size === 0) {
      return null
    }
    
    // 找到当前可见的块
    let currentBlock = 0
    let foundBlock = false
    
    for (const [blockIndex, pos] of this.previewBlockPositions) {
      if (pos.top <= previewScrollTop && previewScrollTop < pos.top + pos.height) {
        currentBlock = blockIndex
        foundBlock = true
        break
      }
    }
    
    // 如果没找到，使用最后一个块
    if (!foundBlock && this.previewBlockPositions.size > 0) {
      const lastBlock = this.previewBlockPositions.size - 1
      const lastPos = this.previewBlockPositions.get(lastBlock)
      if (lastPos && previewScrollTop >= lastPos.top) {
        currentBlock = lastBlock
      }
    }
    
    // 找到对应的起始行
    const startLine = this.blockToLineMap.get(currentBlock)
    if (startLine === undefined) {
      return this.fallbackToPercentage(previewScrollTop, 'preview')
    }
    
    const linePos = this.editorLinePositions.get(startLine)
    if (!linePos) {
      return this.fallbackToPercentage(previewScrollTop, 'preview')
    }
    
    // 计算块内相对位置
    const blockPos = this.previewBlockPositions.get(currentBlock)
    if (!blockPos) {
      return { scrollTop: linePos.top, line: startLine }
    }
    
    const blockOffset = previewScrollTop - blockPos.top
    const blockProgress = blockPos.height > 0 ? blockOffset / blockPos.height : 0
    
    // 找到块内的所有行
    const blockLines = this.getBlockLines(currentBlock)
    
    // 计算对应的编辑器位置
    const totalBlockHeight = this.calculateBlockHeight(blockLines.start, blockLines.end)
    const targetOffset = totalBlockHeight * Math.min(blockProgress, 1)
    
    const targetScrollTop = this.findScrollTopByOffset(blockLines.start, targetOffset)
    const targetLine = this.findLineByScrollTop(targetScrollTop)
    
    return { scrollTop: targetScrollTop, line: targetLine }
  }
  
  /**
   * 获取块覆盖的行范围
   */
  private getBlockLines(blockIndex: number): { start: number; end: number } {
    let startLine = -1
    let endLine = -1
    
    for (const [lineNum, bi] of this.lineToBlockMap) {
      if (bi === blockIndex) {
        if (startLine === -1 || lineNum < startLine) {
          startLine = lineNum
        }
        if (endLine === -1 || lineNum > endLine) {
          endLine = lineNum
        }
      }
    }
    
    return { start: startLine, end: endLine }
  }
  
  /**
   * 计算块的编辑器高度
   */
  private calculateBlockHeight(startLine: number, endLine: number): number {
    let height = 0
    for (let i = startLine; i <= endLine; i++) {
      const pos = this.editorLinePositions.get(i)
      if (pos) {
        height += pos.height
      }
    }
    return height
  }
  
  /**
   * 计算行在块内的偏移量
   */
  private calculateOffsetInBlock(startLine: number, currentLine: number, editorScrollTop: number): number {
    let offset = 0
    
    for (let i = startLine; i < currentLine; i++) {
      const pos = this.editorLinePositions.get(i)
      if (pos) {
        offset += pos.height
      }
    }
    
    // 加上当前行内的偏移
    const currentPos = this.editorLinePositions.get(currentLine)
    if (currentPos) {
      offset += editorScrollTop - currentPos.top
    }
    
    return offset
  }
  
  /**
   * 根据偏移量找到滚动位置
   */
  private findScrollTopByOffset(startLine: number, offset: number): number {
    let accumulated = 0
    
    for (let i = startLine; ; i++) {
      const pos = this.editorLinePositions.get(i)
      if (!pos) break
      
      if (accumulated + pos.height > offset) {
        return pos.top + (offset - accumulated)
      }
      accumulated += pos.height
    }
    
    const lastPos = this.editorLinePositions.get(this.editorLinePositions.size)
    return lastPos ? lastPos.top : 0
  }
  
  /**
   * 根据滚动位置找到行号
   */
  private findLineByScrollTop(scrollTop: number): number {
    for (const [lineNum, pos] of this.editorLinePositions) {
      if (scrollTop >= pos.top && scrollTop < pos.top + pos.height) {
        return lineNum
      }
    }
    return this.editorLinePositions.size
  }
  
  /**
   * 降级为百分比映射
   */
  private fallbackToPercentage(scrollTop: number, source: 'editor' | 'preview'): { scrollTop: number; line: number } {
    debugLog('降级为百分比映射')
    
    if (source === 'editor') {
      // 编辑器 -> 预览
      const totalEditorHeight = this.getTotalEditorHeight()
      const totalPreviewHeight = this.getTotalPreviewHeight()
      
      if (totalEditorHeight > 0 && totalPreviewHeight > 0) {
        const percentage = scrollTop / totalEditorHeight
        return { scrollTop: totalPreviewHeight * percentage, line: 1 }
      }
    } else {
      // 预览 -> 编辑器
      const totalEditorHeight = this.getTotalEditorHeight()
      const totalPreviewHeight = this.getTotalPreviewHeight()
      
      if (totalPreviewHeight > 0 && totalEditorHeight > 0) {
        const percentage = scrollTop / totalPreviewHeight
        const targetScrollTop = totalEditorHeight * percentage
        const targetLine = this.findLineByScrollTop(targetScrollTop)
        return { scrollTop: targetScrollTop, line: targetLine }
      }
    }
    
    return { scrollTop: 0, line: 1 }
  }
  
  /**
   * 获取编辑器总高度
   */
  private getTotalEditorHeight(): number {
    let total = 0
    for (const pos of this.editorLinePositions.values()) {
      total += pos.height
    }
    return total
  }
  
  /**
   * 获取预览总高度
   */
  private getTotalPreviewHeight(): number {
    let total = 0
    for (const pos of this.previewBlockPositions.values()) {
      total += pos.height
    }
    return total
  }
  
  /**
   * 清空映射
   */
  clear(): void {
    this.editorLinePositions.clear()
    this.previewBlockPositions.clear()
    this.lineToBlockMap.clear()
    this.blockToLineMap.clear()
    this.cachedText = ''
  }
  
  /**
   * 获取统计信息
   */
  getStats() {
    return {
      editorLines: this.editorLinePositions.size,
      previewBlocks: this.previewBlockPositions.size,
      lineMappings: this.lineToBlockMap.size,
      blockMappings: this.blockToLineMap.size
    }
  }
  
  // ============================================
  // 兼容旧接口的方法
  // ============================================
  
  /**
   * 构建编辑器字符位置映射（兼容旧接口）
   * @deprecated 使用 measureEditorLines 代替
   */
  buildCharPositions(textarea: HTMLTextAreaElement, text: string): void {
    this.measureEditorLines(textarea, text)
  }
  
  /**
   * 构建预览元素位置映射（兼容旧接口）
   * @deprecated 使用 measurePreviewBlocks 和 buildLineToBlockMapping 代替
   */
  buildElementPositions(
    container: HTMLElement, 
    blocks: any[],
    _blockPositions?: BlockPosition[]
  ): void {
    this.measurePreviewBlocks(container)
    this.buildLineToBlockMapping(this.cachedText, blocks)
  }
}
