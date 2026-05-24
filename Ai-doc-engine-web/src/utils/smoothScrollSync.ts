/**
 * 平滑滚动同步工具 - 用户体验优先版本
 * 
 * 设计理念：
 * 1. 使用百分比映射作为基础，确保始终对齐
 * 2. 添加智能偏移修正，处理内容密度差异
 * 3. 平滑的滚动动画，避免跳跃感
 * 4. 防抖和节流，避免卡顿
 */

import { debugLog } from './scroll'

export interface SyncConfig {
  smoothness: number      // 平滑度 (0-1)，越大越平滑
  debounceDelay: number   // 防抖延迟（毫秒）
  enableSmartOffset: boolean  // 是否启用智能偏移
}

export class SmoothScrollSyncManager {
  private editorElement: HTMLElement | null = null
  private previewElement: HTMLElement | null = null
  
  private config: SyncConfig = {
    smoothness: 0.15,
    debounceDelay: 50,
    enableSmartOffset: true
  }
  
  private syncTimeout: number | null = null
  private isEditorScrolling = false
  private isPreviewScrolling = false
  
  // 高度比例缓存
  private heightRatio: number = 1.0
  
  // 内容密度缓存
  private editorContentDensity: Map<number, number> = new Map()
  private previewContentDensity: Map<number, number> = new Map()

  constructor(config?: Partial<SyncConfig>) {
    if (config) {
      this.config = { ...this.config, ...config }
    }
  }

  /**
   * 初始化滚动同步
   */
  init(editorElement: HTMLElement, previewElement: HTMLElement): void {
    this.editorElement = editorElement
    this.previewElement = previewElement
    
    // 计算高度比例
    this.calculateHeightRatio()
    
    // 分析内容密度
    this.analyzeContentDensity()
    
    debugLog('滚动同步管理器已初始化', {
      editorHeight: editorElement.scrollHeight,
      previewHeight: previewElement.scrollHeight,
      heightRatio: this.heightRatio.toFixed(3),
      config: this.config
    })
  }

  /**
   * 计算编辑器和预览的高度比例
   * 用于修正滚动速度差异
   */
  private calculateHeightRatio(): void {
    if (!this.editorElement || !this.previewElement) return

    const editorScrollHeight = this.editorElement.scrollHeight
    const editorClientHeight = this.editorElement.clientHeight
    const previewScrollHeight = this.previewElement.scrollHeight
    const previewClientHeight = this.previewElement.clientHeight

    const editorMaxScroll = Math.max(1, editorScrollHeight - editorClientHeight)
    const previewMaxScroll = Math.max(1, previewScrollHeight - previewClientHeight)

    // 计算高度比例：预览可滚动高度 / 编辑器可滚动高度
    this.heightRatio = previewMaxScroll / editorMaxScroll

    debugLog('高度比例计算', {
      editorMaxScroll,
      previewMaxScroll,
      heightRatio: this.heightRatio.toFixed(3),
      adjustment: this.heightRatio < 1 ? '预览较短，需要减速' : '预览较长，需要加速'
    })
  }

  /**
   * 分析内容密度
   * 将编辑器和预览区域分成多个区段，计算每个区段的内容密度
   */
  private analyzeContentDensity(): void {
    if (!this.editorElement || !this.previewElement) return

    const segments = 20 // 分成20个区段
    
    // 编辑器密度分析
    for (let i = 0; i < segments; i++) {
      // 简化版：假设密度均匀，实际可以根据内容类型调整
      this.editorContentDensity.set(i, 1.0)
    }
    
    // 预览密度分析
    for (let i = 0; i < segments; i++) {
      this.previewContentDensity.set(i, 1.0)
    }
  }

  /**
   * 编辑器滚动处理
   */
  handleEditorScroll(scrollTop: number, scrollHeight: number, clientHeight: number): void {
    if (this.isPreviewScrolling) {
      return // 正在同步预览滚动，避免循环
    }

    this.isEditorScrolling = true
    
    if (this.syncTimeout) {
      clearTimeout(this.syncTimeout)
    }

    // 使用 requestAnimationFrame 确保流畅
    requestAnimationFrame(() => {
      const targetPosition = this.calculatePreviewPosition(scrollTop, scrollHeight, clientHeight)
      this.scrollPreviewTo(targetPosition)
    })

    this.syncTimeout = window.setTimeout(() => {
      this.isEditorScrolling = false
    }, this.config.debounceDelay)
  }

  /**
   * 预览滚动处理
   */
  handlePreviewScroll(scrollTop: number, scrollHeight: number, clientHeight: number): void {
    if (this.isEditorScrolling) {
      return // 正在同步编辑器滚动，避免循环
    }

    this.isPreviewScrolling = true
    
    if (this.syncTimeout) {
      clearTimeout(this.syncTimeout)
    }

    requestAnimationFrame(() => {
      const targetPosition = this.calculateEditorPosition(scrollTop, scrollHeight, clientHeight)
      this.scrollEditorTo(targetPosition)
    })

    this.syncTimeout = window.setTimeout(() => {
      this.isPreviewScrolling = false
    }, this.config.debounceDelay)
  }

  /**
   * 计算预览位置（编辑器 -> 预览）
   */
  private calculatePreviewPosition(
    editorScrollTop: number,
    editorScrollHeight: number,
    editorClientHeight: number
  ): number {
    if (!this.previewElement) return 0

    const previewScrollHeight = this.previewElement.scrollHeight
    const previewClientHeight = this.previewElement.clientHeight

    // 计算编辑器的滚动百分比
    const editorMaxScroll = Math.max(1, editorScrollHeight - editorClientHeight)
    const editorPercentage = editorScrollTop / editorMaxScroll

    // 直接使用百分比映射（不使用高度比例，因为已经在 maxScroll 中体现）
    const previewMaxScroll = Math.max(1, previewScrollHeight - previewClientHeight)
    let targetScroll = previewMaxScroll * editorPercentage

    // 智能偏移修正（可选）
    if (this.config.enableSmartOffset) {
      // 在中间区域应用轻微的非线性修正
      // 这可以补偿内容密度的差异
      const adjustedPercentage = this.applyNonlinearAdjustment(editorPercentage)
      targetScroll = previewMaxScroll * adjustedPercentage
    }

    // 边界检查
    targetScroll = Math.max(0, Math.min(targetScroll, previewMaxScroll))

    debugLog('编辑器->预览', {
      editorScrollTop: editorScrollTop.toFixed(1),
      editorPercentage: (editorPercentage * 100).toFixed(1) + '%',
      targetScroll: targetScroll.toFixed(1),
      previewMaxScroll: previewMaxScroll.toFixed(1)
    })

    return targetScroll
  }

  /**
   * 计算编辑器位置（预览 -> 编辑器）
   */
  private calculateEditorPosition(
    previewScrollTop: number,
    previewScrollHeight: number,
    previewClientHeight: number
  ): number {
    if (!this.editorElement) return 0

    const editorScrollHeight = this.editorElement.scrollHeight
    const editorClientHeight = this.editorElement.clientHeight

    // 计算预览的滚动百分比
    const previewMaxScroll = Math.max(1, previewScrollHeight - previewClientHeight)
    const previewPercentage = previewScrollTop / previewMaxScroll

    // 直接使用百分比映射
    const editorMaxScroll = Math.max(1, editorScrollHeight - editorClientHeight)
    let targetScroll = editorMaxScroll * previewPercentage

    // 智能偏移修正（可选）
    if (this.config.enableSmartOffset) {
      const adjustedPercentage = this.applyNonlinearAdjustment(previewPercentage)
      targetScroll = editorMaxScroll * adjustedPercentage
    }

    // 边界检查
    targetScroll = Math.max(0, Math.min(targetScroll, editorMaxScroll))

    debugLog('预览->编辑器', {
      previewScrollTop: previewScrollTop.toFixed(1),
      previewPercentage: (previewPercentage * 100).toFixed(1) + '%',
      targetScroll: targetScroll.toFixed(1),
      editorMaxScroll: editorMaxScroll.toFixed(1)
    })

    return targetScroll
  }

  /**
   * 应用非线性调整
   * 在中间区域应用轻微的修正，补偿内容密度差异
   */
  private applyNonlinearAdjustment(percentage: number): number {
    // 在开头和结尾保持线性（确保精确对齐）
    if (percentage < 0.05 || percentage > 0.95) {
      return percentage
    }

    // 在中间区域应用轻微的 S 曲线调整
    // 这可以补偿预览内容通常比编辑器更紧凑的情况
    const t = percentage
    const adjusted = t * t * (3 - 2 * t) // smoothstep 函数
    
    // 混合原始值和调整值（只应用 10% 的调整）
    return percentage * 0.9 + adjusted * 0.1
  }

  /**
   * 平滑滚动到预览位置
   */
  private scrollPreviewTo(targetPosition: number): void {
    if (!this.previewElement) return

    const currentPosition = this.previewElement.scrollTop
    const distance = targetPosition - currentPosition

    // 如果距离很小，直接设置
    if (Math.abs(distance) < 2) {
      this.previewElement.scrollTop = targetPosition
      return
    }

    // 使用平滑滚动
    const smoothDistance = currentPosition + distance * (1 - this.config.smoothness)
    
    this.previewElement.scrollTo({
      top: smoothDistance,
      behavior: 'auto' // 使用 auto 而不是 smooth，因为我们自己控制平滑度
    })
  }

  /**
   * 平滑滚动到编辑器位置
   */
  private scrollEditorTo(targetPosition: number): void {
    if (!this.editorElement) return

    const currentPosition = this.editorElement.scrollTop
    const distance = targetPosition - currentPosition

    // 如果距离很小，直接设置
    if (Math.abs(distance) < 2) {
      this.editorElement.scrollTop = targetPosition
      return
    }

    // 使用平滑滚动
    const smoothDistance = currentPosition + distance * (1 - this.config.smoothness)
    
    this.editorElement.scrollTo({
      top: smoothDistance,
      behavior: 'auto'
    })
  }

  /**
   * 更新配置
   */
  updateConfig(config: Partial<SyncConfig>): void {
    this.config = { ...this.config, ...config }
    debugLog('配置已更新', this.config)
  }

  /**
   * 重新分析内容（当文档内容变化时调用）
   */
  refresh(): void {
    this.calculateHeightRatio()
    this.analyzeContentDensity()
    debugLog('内容已重新分析', {
      heightRatio: this.heightRatio.toFixed(3)
    })
  }

  /**
   * 清理
   */
  destroy(): void {
    if (this.syncTimeout) {
      clearTimeout(this.syncTimeout)
    }
    this.editorElement = null
    this.previewElement = null
    this.editorContentDensity.clear()
    this.previewContentDensity.clear()
  }
}

/**
 * 创建默认的滚动同步管理器
 */
export function createScrollSyncManager(config?: Partial<SyncConfig>): SmoothScrollSyncManager {
  return new SmoothScrollSyncManager(config)
}
