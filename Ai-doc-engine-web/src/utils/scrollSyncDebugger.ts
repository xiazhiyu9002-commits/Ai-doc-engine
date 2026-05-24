/**
 * 滚动同步调试工具
 * 在浏览器控制台使用，实时调整参数
 */

import type { SmoothScrollSyncManager } from './smoothScrollSync'

export class ScrollSyncDebugger {
  private manager: SmoothScrollSyncManager | null = null

  setManager(manager: SmoothScrollSyncManager): void {
    this.manager = manager
    this.showHelp()
  }

  showHelp(): void {
    // 调试工具帮助信息
  }

  setSmoothness(value: number): void {
    if (!this.manager) {
      return
    }

    if (value < 0 || value > 1) {
      return
    }

    this.manager.updateConfig({ smoothness: value })
  }

  setDebounce(value: number): void {
    if (!this.manager) {
      return
    }

    if (value < 0 || value > 200) {
      return
    }

    this.manager.updateConfig({ debounceDelay: value })
  }

  enableSmartOffset(): void {
    if (!this.manager) {
      return
    }

    this.manager.updateConfig({ enableSmartOffset: true })
  }

  disableSmartOffset(): void {
    if (!this.manager) {
      return
    }

    this.manager.updateConfig({ enableSmartOffset: false })
  }

  showConfig(): void {
    if (!this.manager) {
      return
    }
  }

  reset(): void {
    if (!this.manager) {
      return
    }

    this.manager.updateConfig({
      smoothness: 0.15,
      debounceDelay: 30,
      enableSmartOffset: false
    })
  }

  // 快捷方法
  faster(): void {
    this.setSmoothness(0.1)
    this.setDebounce(20)
  }

  smoother(): void {
    this.setSmoothness(0.3)
    this.setDebounce(50)
  }

  balanced(): void {
    this.reset()
  }
}

// 创建全局调试器实例
export const scrollDebugger = new ScrollSyncDebugger()

// 挂载到 window 对象，方便在控制台使用
if (typeof window !== 'undefined') {
  (window as any).scrollSyncDebugger = scrollDebugger
}
