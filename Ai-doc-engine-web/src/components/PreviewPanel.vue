<template>
  <div class="preview-panel">
    <div class="preview-body" ref="scrollContainer" @scroll="handleScroll" @mouseup="handlePreviewMouseUp">
      <el-empty v-if="!documentStore.udm" description="暂无内容，请输入或导入 Markdown 后点击解析" />
      
      <div v-else-if="documentStore.udm" class="document-content">
        <div
          v-for="(block, index) in documentStore.udm.blocks"
          :key="index"
          :ref="el => blockElements[index] = el as HTMLElement"
          class="block"
          :data-block-index="index"
          :data-source-start="block.sourceStartOffset"
          :data-source-end="block.sourceEndOffset"
        >
          <template v-if="block.type === 'heading'">
            <component
              :is="`h${block.content.level}`"
              class="heading"
              :class="`heading-${block.content.level}`"
            >
              {{ block.content.text }}
            </component>
          </template>

          <p v-else-if="block.type === 'paragraph'" class="paragraph">
            <template v-if="block.content.segments && block.content.segments.length > 0">
              <template v-for="(segment, i) in block.content.segments" :key="i">
                <a v-if="segment.linkUrl" :href="segment.linkUrl" target="_blank" class="link" :class="getRichTextClass(segment)">
                  {{ segment.text }}
                </a>
                <span v-else-if="segment.inlineFormula" class="inline-formula">
                  <FormulaPreview :latex="segment.inlineFormula" inline />
                </span>
                <span v-else-if="segment.text === '\n'" class="line-break"><br /></span>
                <span v-else :class="getRichTextClass(segment)">{{ segment.text }}</span>
              </template>
            </template>
            <template v-else>{{ block.content.text }}</template>
          </p>

          <template v-else-if="block.type === 'list'">
            <ListRenderer :content="block.content" :level="0" />
          </template>

          <table v-else-if="block.type === 'table'" class="table">
            <thead>
              <tr>
                <template v-if="block.content.headerCells && block.content.headerCells.length > 0">
                  <th v-for="(headerCell, i) in block.content.headerCells" :key="i">
                    <template v-if="Array.isArray(headerCell)">
                      <template v-for="(segment, k) in headerCell" :key="k">
                        <span v-if="segment.inlineFormula" class="inline-formula">
                          <FormulaPreview :latex="segment.inlineFormula" inline />
                        </span>
                        <span v-else-if="segment.text === '\n'" class="line-break"><br /></span>
                        <span v-else :class="getRichTextClass(segment)">{{ segment.text }}</span>
                      </template>
                    </template>
                    <template v-else-if="typeof headerCell === 'object' && headerCell !== null">
                      <span v-if="headerCell.inlineFormula" class="inline-formula">
                        <FormulaPreview :latex="headerCell.inlineFormula" inline />
                      </span>
                      <span v-else :class="getRichTextClass(headerCell)">{{ headerCell.text }}</span>
                    </template>
                    <template v-else>
                      {{ headerCell }}
                    </template>
                  </th>
                </template>
                <template v-else>
                  <th v-for="(header, i) in block.content.headers" :key="i">
                    {{ header }}
                  </th>
                </template>
              </tr>
            </thead>
            <tbody>
              <template v-if="block.content.rowCells && block.content.rowCells.length > 0">
                <tr v-for="(row, i) in block.content.rowCells" :key="i">
                  <td v-for="(cell, j) in row" :key="j">
                    <template v-if="Array.isArray(cell)">
                      <template v-for="(segment, k) in cell" :key="k">
                        <span v-if="segment.inlineFormula" class="inline-formula">
                          <FormulaPreview :latex="segment.inlineFormula" inline />
                        </span>
                        <span v-else-if="segment.text === '\n'" class="line-break"><br /></span>
                        <span v-else :class="getRichTextClass(segment)">{{ segment.text }}</span>
                      </template>
                    </template>
                    <template v-else>
                      {{ cell }}
                    </template>
                  </td>
                </tr>
              </template>
              <template v-else>
                <tr v-for="(row, i) in block.content.rows" :key="i">
                  <td v-for="(cell, j) in row" :key="j">
                    {{ cell }}
                  </td>
                </tr>
              </template>
            </tbody>
          </table>

          <div v-else-if="block.type === 'formula'" class="formula">
            <FormulaPreview :latex="block.content.latex" />
          </div>

          <div v-else-if="block.type === 'flowchart'" class="flowchart">
            <div v-if="block.content.imageBase64" class="flowchart-image">
              <img 
                :src="`data:image/png;base64,${block.content.imageBase64}`" 
                alt="流程图"
                class="flowchart-img"
              />
            </div>
            <div v-else-if="block.content.rawSource" class="flowchart-preview">
              <el-tag type="info" style="margin-bottom: 12px">Mermaid 流程图</el-tag>
              <div 
                :ref="el => setMermaidRef(el as HTMLElement, index)"
                class="mermaid-diagram"
                :data-mermaid-code="block.content.rawSource"
              >
                {{ block.content.rawSource }}
              </div>
              <el-alert type="info" :closable="false" style="margin-top: 12px">
                导出时将渲染为图片并嵌入 Word 文档
              </el-alert>
            </div>
            <div v-else class="flowchart-preview">
              <el-alert type="warning" :closable="false">
                流程图内容为空
              </el-alert>
            </div>
          </div>

          <div v-else-if="block.type === 'codeblock'" class="code-block">
            <div class="code-header">
              <el-tag size="small" type="info">{{ block.content.language || '代码' }}</el-tag>
            </div>
            <pre class="code-content"><code>{{ block.content.code }}</code></pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useDocumentStore } from '@/store/document'
import FormulaPreview from './formula/FormulaPreview.vue'
import ListRenderer from './ListRenderer.vue'
import type { RichText } from '@/types/document'
import { throttle, calculateScrollPosition, debugLog, type ScrollData } from '@/utils/scroll'
import mermaid from 'mermaid'
import 'katex/contrib/mhchem'

// 初始化 Mermaid
mermaid.initialize({
  startOnLoad: false,
  theme: 'default',
  securityLevel: 'loose',
  fontFamily: 'Arial, sans-serif'
})

const documentStore = useDocumentStore()
const scrollContainer = ref<HTMLElement | null>(null)

const blockElements = ref<(HTMLElement | null)[]>([])
const mermaidElements = ref<Map<number, HTMLElement>>(new Map())

const savedScrollTop = ref<number>(0)

const emit = defineEmits<{
  (e: 'scroll', data: ScrollData): void
  (e: 'selectionChange', range: { startOffset: number; endOffset: number } | null): void
}>()

const escapeHtml = (text: string): string => {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }
  return text.replace(/[&<>"']/g, m => map[m])
}

const setMermaidRef = (el: HTMLElement | null, index: number) => {
  if (el) {
    mermaidElements.value.set(index, el)
  }
}

// 渲染所有 Mermaid 图表
const renderMermaidDiagrams = async () => {
  await nextTick()
  
  for (const [index, element] of mermaidElements.value.entries()) {
    try {
      const code = element.getAttribute('data-mermaid-code')
      if (!code) continue
      
      // 清空元素内容
      element.innerHTML = ''
      
      // 校验 Mermaid 代码
      try {
        await mermaid.parse(code)
      } catch (parseError) {
        console.error('Mermaid parse failed:', parseError)
        element.innerHTML = `<div class="mermaid-error">
          <p>Mermaid 语法错误</p>
          <pre>${escapeHtml(code)}</pre>
        </div>`
        continue
      }
      
      // 生成唯一 ID
      const id = `mermaid-${index}-${Date.now()}`
      
      // 渲染 Mermaid
      const { svg } = await mermaid.render(id, code)
      element.innerHTML = svg
    } catch (error) {
      console.error('Mermaid rendering failed:', error)
      element.innerHTML = `<div class="mermaid-error">
        <p>渲染失败</p>
        <pre>${element.getAttribute('data-mermaid-code') || ''}</pre>
      </div>`
    }
  }
}

let cachedScrollData: ScrollData | null = null

const blockHeightCache = ref<Map<number, { height: number; top: number }>>(new Map())

let visibilityObserver: IntersectionObserver | null = null

const handleScroll = throttle(() => {
  if (scrollContainer.value) {
    cachedScrollData = {
      scrollTop: scrollContainer.value.scrollTop,
      scrollHeight: scrollContainer.value.scrollHeight,
      clientHeight: scrollContainer.value.clientHeight
    }
    emit('scroll', cachedScrollData)
  }
}, 100)

const updateBlockHeightCache = () => {
  if (!scrollContainer.value || blockElements.value.length === 0) return
  
  const newCache = new Map<number, { height: number; top: number }>()
  
  let accumulatedTop = 0
  
  blockElements.value.forEach((element, index) => {
    if (element) {
      const height = element.offsetHeight
      newCache.set(index, {
        height,
        top: accumulatedTop
      })
      accumulatedTop += height
    }
  })
  
  blockHeightCache.value = newCache
  debugLog('块高度缓存已更新:', newCache.size, '个块')
}

const scrollToPercentage = (percentage: number) => {
  if (scrollContainer.value) {
    const maxScroll = scrollContainer.value.scrollHeight - scrollContainer.value.clientHeight
    if (maxScroll > 0) {
      const targetScroll = maxScroll * percentage
      scrollContainer.value.scrollTo({
        top: targetScroll,
        behavior: 'smooth'
      })
    }
  }
}

const scrollToBlock = (blockIndex: number) => {
  if (!scrollContainer.value || blockIndex < 0 || blockIndex >= blockElements.value.length) {
    return
  }
  
  const targetElement = blockElements.value[blockIndex]
  if (targetElement && scrollContainer.value) {
    const targetScroll = calculateScrollPosition(targetElement, scrollContainer.value, 'top')
    scrollContainer.value.scrollTo({
      top: targetScroll,
      behavior: 'smooth'
    })
  }
}

const scrollToBlockWithOffset = (blockIndex: number, relativePosition: number = 0) => {
  if (!scrollContainer.value || blockIndex < 0 || blockIndex >= blockElements.value.length) {
    return
  }
  
  const targetElement = blockElements.value[blockIndex]
  if (targetElement && scrollContainer.value) {
    const containerRect = scrollContainer.value.getBoundingClientRect()
    const elementRect = targetElement.getBoundingClientRect()
    
    const elementTop = elementRect.top - containerRect.top + scrollContainer.value.scrollTop
    const elementHeight = elementRect.height
    
    const offsetInElement = elementHeight * relativePosition
    let targetScroll = elementTop + offsetInElement - 20
    
    const maxScroll = scrollContainer.value.scrollHeight - scrollContainer.value.clientHeight
    targetScroll = Math.max(0, Math.min(targetScroll, maxScroll))
    
    debugLog('滚动到块:', {
      blockIndex,
      relativePosition,
      elementTop,
      elementHeight,
      targetScroll
    })
    
    scrollContainer.value.scrollTo({
      top: targetScroll,
      behavior: 'smooth'
    })
  }
}

const scrollToBlockWithOffsetCached = (blockIndex: number, relativePosition: number = 0) => {
  if (!scrollContainer.value || blockIndex < 0) {
    return
  }
  
  const cachedInfo = blockHeightCache.value.get(blockIndex)
  if (cachedInfo) {
    const offsetInElement = cachedInfo.height * relativePosition
    let targetScroll = cachedInfo.top + offsetInElement - 20
    
    const maxScroll = scrollContainer.value.scrollHeight - scrollContainer.value.clientHeight
    targetScroll = Math.max(0, Math.min(targetScroll, maxScroll))
    
    scrollContainer.value.scrollTo({
      top: targetScroll,
      behavior: 'smooth'
    })
    return
  }
  
  scrollToBlockWithOffset(blockIndex, relativePosition)
}

const scrollToPixel = (pixelTop: number, behavior: ScrollBehavior = 'smooth') => {
  if (!scrollContainer.value) return
  
  const maxScroll = scrollContainer.value.scrollHeight - scrollContainer.value.clientHeight
  const finalScroll = Math.max(0, Math.min(pixelTop, maxScroll))
  
  scrollContainer.value.scrollTo({
    top: finalScroll,
    behavior
  })
}

const getScrollContainer = (): HTMLElement | null => {
  return scrollContainer.value
}

const setupVisibilityObserver = () => {
  // 不再需要 IntersectionObserver，已移除
}

const cleanupVisibilityObserver = () => {
  if (visibilityObserver) {
    visibilityObserver.disconnect()
    visibilityObserver = null
  }
}

const saveScrollPosition = () => {
  if (scrollContainer.value) {
    savedScrollTop.value = scrollContainer.value.scrollTop
  }
}

const restoreScrollPosition = () => {
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = savedScrollTop.value
  }
}

const getScrollData = (): ScrollData | null => {
  if (scrollContainer.value) {
    return {
      scrollTop: scrollContainer.value.scrollTop,
      scrollHeight: scrollContainer.value.scrollHeight,
      clientHeight: scrollContainer.value.clientHeight
    }
  }
  return cachedScrollData
}

const getVisibleBlockIndex = (): number => {
  if (!scrollContainer.value || blockElements.value.length === 0) return -1
  
  const containerRect = scrollContainer.value.getBoundingClientRect()
  const containerCenter = containerRect.top + containerRect.height / 2
  
  for (let i = 0; i < blockElements.value.length; i++) {
    const element = blockElements.value[i]
    if (!element) continue
    
    const rect = element.getBoundingClientRect()
    if (rect.top <= containerCenter && rect.bottom >= containerCenter) {
      return i
    }
  }
  
  return 0
}

const getRichTextClass = (segment: RichText): string[] => {
  const classes: string[] = []
  if (segment.bold) classes.push('rich-bold')
  if (segment.italic) classes.push('rich-italic')
  if (segment.strikethrough) classes.push('rich-strikethrough')
  if (segment.code) classes.push('rich-code')
  return classes
}

const highlightBySourceRange = (startOffset: number, endOffset: number) => {
  if (!scrollContainer.value) return
  
  const blocks = documentStore.udm?.blocks || []
  if (blocks.length === 0) return
  
  let startBlockIndex = -1
  let endBlockIndex = -1
  
  for (let i = 0; i < blocks.length; i++) {
    const block = blocks[i]
    if (block.sourceStartOffset !== undefined && block.sourceEndOffset !== undefined) {
      if (startBlockIndex < 0 && startOffset >= block.sourceStartOffset && startOffset <= block.sourceEndOffset) {
        startBlockIndex = i
      }
      if (endOffset >= block.sourceStartOffset && endOffset <= block.sourceEndOffset) {
        endBlockIndex = i
      }
    }
  }
  
  if (startBlockIndex < 0) return
  if (endBlockIndex < 0) endBlockIndex = startBlockIndex
  
  const selection = window.getSelection()
  if (!selection) return
  
  const collectTextNodes = (element: HTMLElement): Text[] => {
    const nodes: Text[] = []
    const walk = (node: Node) => {
      if (node.nodeType === Node.TEXT_NODE) {
        nodes.push(node as Text)
      } else {
        for (const child of Array.from(node.childNodes)) {
          walk(child)
        }
      }
    }
    walk(element)
    return nodes
  }
  
  const findPositionInBlock = (
    blockIndex: number,
    targetOffset: number
  ): { node: Text; offset: number } | null => {
    const block = blocks[blockIndex]
    const blockElement = blockElements.value[blockIndex]
    if (!block || !blockElement) return null
    
    const blockSourceStart = block.sourceStartOffset || 0
    const relativeOffset = targetOffset - blockSourceStart
    
    const textNodes = collectTextNodes(blockElement)
    if (textNodes.length === 0) return null
    
    let currentOffset = 0
    for (const textNode of textNodes) {
      const nodeLength = textNode.textContent?.length || 0
      if (currentOffset + nodeLength > relativeOffset) {
        return { node: textNode, offset: relativeOffset - currentOffset }
      }
      currentOffset += nodeLength
    }
    
    const lastNode = textNodes[textNodes.length - 1]
    return { node: lastNode, offset: lastNode.textContent?.length || 0 }
  }
  
  const startResult = findPositionInBlock(startBlockIndex, startOffset)
  if (!startResult) return
  
  let endResult: { node: Text; offset: number } | null = null
  if (startBlockIndex === endBlockIndex) {
    endResult = findPositionInBlock(endBlockIndex, endOffset) || startResult
  } else {
    const endBlock = blocks[endBlockIndex]
    const endBlockElement = blockElements.value[endBlockIndex]
    if (!endBlock || !endBlockElement) {
      endResult = startResult
    } else {
      const endBlockSourceStart = endBlock.sourceStartOffset || 0
      const relativeEndOffset = endOffset - endBlockSourceStart
      
      const endTextNodes = collectTextNodes(endBlockElement)
      if (endTextNodes.length === 0) {
        endResult = startResult
      } else {
        let currentOffset = 0
        for (const textNode of endTextNodes) {
          const nodeLength = textNode.textContent?.length || 0
          if (currentOffset + nodeLength > relativeEndOffset) {
            endResult = { node: textNode, offset: relativeEndOffset - currentOffset }
            break
          }
          currentOffset += nodeLength
        }
        if (!endResult) {
          const lastNode = endTextNodes[endTextNodes.length - 1]
          endResult = { node: lastNode, offset: lastNode.textContent?.length || 0 }
        }
      }
    }
  }
  
  if (!endResult) {
    endResult = startResult
  }
  
  try {
    const range = document.createRange()
    range.setStart(startResult.node, Math.max(0, Math.min(startResult.offset, startResult.node.textContent?.length || 0)))
    range.setEnd(endResult.node, Math.max(0, Math.min(endResult.offset, endResult.node.textContent?.length || 0)))
    
    selection.removeAllRanges()
    selection.addRange(range)
    
    const containerRect = scrollContainer.value.getBoundingClientRect()
    const rangeRect = range.getBoundingClientRect()
    
    if (rangeRect.top < containerRect.top || rangeRect.bottom > containerRect.bottom) {
      range.startContainer.parentElement?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  } catch (e) {
    console.warn('Failed to create selection range:', e)
  }
}

const clearHighlight = () => {
  const selection = window.getSelection()
  if (selection) {
    selection.removeAllRanges()
  }
}

const handlePreviewMouseUp = () => {
  const selection = window.getSelection()
  if (!selection || selection.isCollapsed) {
    return
  }
  
  const range = selection.getRangeAt(0)
  if (!range) return
  
  const startContainer = range.startContainer
  const endContainer = range.endContainer
  
  const findBlockElement = (node: Node): HTMLElement | null => {
    let current: Node | null = node
    while (current) {
      if (current instanceof HTMLElement && current.classList.contains('block')) {
        return current
      }
      current = current.parentNode
    }
    return null
  }
  
  const startBlock = findBlockElement(startContainer)
  const endBlock = findBlockElement(endContainer)
  
  if (!startBlock || !endBlock) return
  
  const startBlockSourceStart = parseInt(startBlock.dataset.sourceStart || '-1')
  const endBlockSourceStart = parseInt(endBlock.dataset.sourceStart || '-1')
  
  if (startBlockSourceStart < 0 || endBlockSourceStart < 0) return
  
  const collectTextNodes = (element: HTMLElement): Text[] => {
    const nodes: Text[] = []
    const walk = (node: Node) => {
      if (node.nodeType === Node.TEXT_NODE) {
        nodes.push(node as Text)
      } else {
        for (const child of Array.from(node.childNodes)) {
          walk(child)
        }
      }
    }
    walk(element)
    return nodes
  }
  
  const getOffsetInBlock = (container: Node, offset: number, block: HTMLElement): number => {
    const textNodes = collectTextNodes(block)
    
    let currentOffset = 0
    for (const textNode of textNodes) {
      if (textNode === container || textNode.contains(container)) {
        return currentOffset + offset
      }
      currentOffset += textNode.textContent?.length || 0
    }
    return currentOffset
  }
  
  const startOffsetInBlock = getOffsetInBlock(startContainer, range.startOffset, startBlock)
  const sourceStartOffset = startBlockSourceStart + startOffsetInBlock
  
  let sourceEndOffset: number
  
  if (startBlock === endBlock) {
    const endOffsetInBlock = getOffsetInBlock(endContainer, range.endOffset, endBlock)
    sourceEndOffset = startBlockSourceStart + endOffsetInBlock
  } else {
    const endOffsetInBlock = getOffsetInBlock(endContainer, range.endOffset, endBlock)
    sourceEndOffset = endBlockSourceStart + endOffsetInBlock
  }
  
  emit('selectionChange', {
    startOffset: sourceStartOffset,
    endOffset: sourceEndOffset
  })
}

watch(() => documentStore.udm?.blocks.length, (newLen, oldLen) => {
  if (newLen !== oldLen) {
    nextTick(() => {
      blockElements.value = []
      blockHeightCache.value.clear()
      mermaidElements.value.clear()
      
      setTimeout(() => {
        updateBlockHeightCache()
        renderMermaidDiagrams()
      }, 100)
    })
  }
})

// 监听 UDM 变化，重新渲染 Mermaid
watch(() => documentStore.udm, () => {
  nextTick(() => {
    renderMermaidDiagrams()
  })
}, { deep: true })

const handleResize = () => {
  updateBlockHeightCache()
}

onMounted(() => {
  setTimeout(() => {
    updateBlockHeightCache()
    setupVisibilityObserver()
    renderMermaidDiagrams()
  }, 200)
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  cleanupVisibilityObserver()
})

defineExpose({
  scrollToPercentage,
  scrollToBlock,
  scrollToBlockWithOffset,
  scrollToBlockWithOffsetCached,
  scrollToPixel,
  getScrollContainer,
  saveScrollPosition,
  restoreScrollPosition,
  getScrollData,
  getVisibleBlockIndex,
  updateBlockHeightCache,
  setupVisibilityObserver,
  cleanupVisibilityObserver,
  highlightBySourceRange,
  clearHighlight
})
</script>

<style scoped>
.preview-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.preview-header {
  display: none;
}

.preview-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  background: #f6f8fa;
}

.preview-mode-hint {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #e8f4fd;
  border-radius: 6px;
  border: 1px solid #b3d8ff;
}

.preview-mode-hint.parsed-mode {
  background: #f0f9ff;
  border-color: #67c23a;
}

.hint-text {
  font-size: 13px;
  color: #606266;
}

.markdown-preview {
  min-height: 100%;
}

.document-content :deep(::selection) {
  background: #b3d8ff;
  color: #000;
}

.document-content {
  max-width: 100%;
  margin: 0;
  padding-bottom: 40px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  font-size: 15px;
  line-height: 1.6;
  color: #24292e;
}

.block {
  margin-bottom: 12px;
}

.block:last-child {
  margin-bottom: 0;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.heading {
  color: #1f4e79;
  margin: 16px 0 8px;
  font-weight: bold;
  line-height: 1.3;
}

.heading-1 {
  font-size: 32px;
  margin-top: 24px;
  margin-bottom: 12px;
  border-bottom: 2px solid #1f4e79;
  padding-bottom: 6px;
}

.heading-2 {
  font-size: 24px;
  margin-top: 20px;
  margin-bottom: 10px;
}

.heading-3 {
  font-size: 20px;
  margin-top: 16px;
  margin-bottom: 8px;
}

.heading-4 {
  font-size: 18px;
  margin-top: 14px;
  margin-bottom: 6px;
}

.heading-5 {
  font-size: 16px;
  margin-top: 12px;
  margin-bottom: 6px;
}

.heading-6 {
  font-size: 14px;
  margin-top: 10px;
  margin-bottom: 4px;
  color: #666;
}

.paragraph {
  line-height: 1.8;
  color: #333;
  margin: 8px 0;
  text-align: justify;
  text-justify: inter-word;
  font-size: inherit;
  word-break: break-word;
  hyphens: auto;
}

.rich-bold {
  font-weight: bold;
}

.rich-italic {
  font-style: italic;
}

.rich-strikethrough {
  text-decoration: line-through;
}

.rich-code {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.9em;
  color: #c7254e;
}

.inline-code {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.9em;
  color: #c7254e;
}

.link {
  color: #0563c1;
  text-decoration: underline;
}

.inline-formula {
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
  resize: none;
  -webkit-user-drag: none;
  user-drag: none;
}

.inline-formula * {
  resize: none !important;
  -webkit-user-drag: none !important;
  user-drag: none !important;
}

.formula {
  margin: 16px 0;
  padding: 12px;
  background: #fafbfc;
  border-radius: 4px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow-x: auto;
  width: 100%;
  resize: none;
  -webkit-user-drag: none;
  user-drag: none;
}

.formula * {
  resize: none !important;
  -webkit-user-drag: none !important;
  user-drag: none !important;
}

.formula :deep(.katex) {
  line-height: 1.2;
  display: inline-block;
}

.formula :deep(.katex-display) {
  margin: 0;
  padding: 0;
  width: 100%;
  text-align: center;
}

.formula :deep(.katex-display > .katex) {
  margin: 0 auto;
  padding: 0;
  display: inline-block;
  text-align: center;
}

.formula-error {
  color: #e74c3c;
  font-family: monospace;
}

.table {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  font-size: 14px;
}

.table th,
.table td {
  border: 1px solid #000000;
  padding: 8px 12px;
  text-align: left;
}

.table th {
  background: #d9d9d9;
  font-weight: 600;
  color: #000;
}

.table td {
  background: #ffffff;
}

.flowchart {
  margin: 16px 0;
  padding: 16px;
  background: #f0f9ff;
  border: 1px dashed #409eff;
  border-radius: 4px;
}

.flowchart-image {
  text-align: center;
  background: #ffffff;
  padding: 16px;
  border-radius: 4px;
}

.flowchart-img {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 0 auto;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.flowchart-preview {
  text-align: center;
}

.mermaid-diagram {
  margin: 12px 0;
  padding: 20px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow-x: auto;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.mermaid-diagram :deep(svg) {
  max-width: 100%;
  height: auto;
}

.mermaid-error {
  color: #e74c3c;
  padding: 16px;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 4px;
  text-align: left;
}

.mermaid-error p {
  margin: 0 0 8px;
  font-weight: 600;
}

.mermaid-error pre {
  margin: 0;
  padding: 8px;
  background: #fff;
  border: 1px solid #fcc;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
}

.mermaid-code {
  margin: 12px 0;
  text-align: left;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.mermaid-code pre {
  margin: 0;
  padding: 16px;
  overflow-x: auto;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.flowchart-info {
  margin: 12px 0;
}

.flowchart-info p {
  margin: 6px 0;
  color: #666;
  font-size: 14px;
  text-align: left;
}

.code-block {
  margin: 16px 0;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  background: #f6f8fa;
}

.code-block code {
  display: block;
  padding: 16px;
  background: #f6f8fa;
  overflow-x: auto;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #24292e;
}

.code-header {
  padding: 8px 12px;
  background: #e8eaed;
  border-bottom: 1px solid #d0d0d0;
}

.code-content {
  margin: 0;
  padding: 16px;
  background: #f6f8fa;
  overflow-x: auto;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #24292e;
}

.code-content code {
  font-family: inherit;
  white-space: pre;
  display: block;
}
</style>
