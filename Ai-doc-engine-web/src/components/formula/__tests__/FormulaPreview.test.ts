import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import FormulaPreview from '@/components/formula/FormulaPreview.vue'

describe('FormulaPreview - 样式统一测试', () => {
  beforeEach(() => {
    // 清理 KaTeX 样式
    document.head.innerHTML = ''
    document.body.innerHTML = ''
  })

  describe('块级公式样式', () => {
    it('应该有白色背景', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      expect(preview.element).toBeDefined()
      
      const styles = window.getComputedStyle(preview.element)
      expect(styles.backgroundColor).toBe('rgb(255, 255, 255)')
    })

    it('应该有灰色边框', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.border).toContain('1px solid rgb(224, 224, 224)')
    })

    it('应该有圆角', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.borderRadius).toBe('6px')
    })

    it('应该有阴影', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.boxShadow).toContain('rgba(0, 0, 0, 0.05)')
    })

    it('应该有正确的内边距', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.paddingTop).toBe('20px')
      expect(styles.paddingBottom).toBe('20px')
    })

    it('应该有正确的外边距', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.marginTop).toBe('12px')
      expect(styles.marginBottom).toBe('12px')
    })

    it('应该居中对齐', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.justifyContent).toBe('center')
    })

    it('应该有正确的字体大小', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.preview-content')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.fontSize).toBe('20px')
    })

    it('应该有正确的文字颜色', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.preview-content')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.color).toBe('rgb(51, 51, 51)')
    })

    it('应该有正确的行高', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: false
        }
      })
      
      const preview = wrapper.find('.preview-content')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.lineHeight).toBe('1.6')
    })
  })

  describe('行内公式样式', () => {
    it('应该有透明背景', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: true
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.backgroundColor).toBe('transparent')
    })

    it('应该没有边框', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: true
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.border).toBe('none')
    })

    it('应该没有阴影', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: true
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.boxShadow).toBe('none')
    })

    it('应该使用较小的内边距', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: true
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.paddingTop).toBe('2px')
      expect(styles.paddingBottom).toBe('2px')
    })

    it('应该使用继承的字体大小', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: true
        }
      })
      
      const preview = wrapper.find('.preview-content')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.fontSize).toBe('1em')
    })

    it('应该继承文字颜色', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: true
        }
      })
      
      const preview = wrapper.find('.preview-content')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.color).toBe('inherit')
    })

    it('应该垂直居中对齐', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: true
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.verticalAlign).toBe('middle')
    })

    it('应该使用 inline-flex 布局', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: 'x^2',
          inline: true
        }
      })
      
      const preview = wrapper.find('.formula-preview')
      const styles = window.getComputedStyle(preview.element)
      expect(styles.display).toBe('inline-flex')
    })
  })

  describe('复杂公式渲染', () => {
    it('应该正确渲染分数公式', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: '\\frac{a}{b}',
          inline: false
        }
      })
      
      const preview = wrapper.find('.preview-content')
      expect(preview.html()).toContain('katex')
    })

    it('应该正确渲染求和公式', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: '\\sum_{i=1}^{n} i',
          inline: false
        }
      })
      
      const preview = wrapper.find('.preview-content')
      expect(preview.html()).toContain('katex')
    })

    it('应该正确渲染积分公式', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: '\\int_{0}^{1} x dx',
          inline: false
        }
      })
      
      const preview = wrapper.find('.preview-content')
      expect(preview.html()).toContain('katex')
    })

    it('应该正确渲染矩阵', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: '\\begin{bmatrix}a & b \\\\ c & d\\end{bmatrix}',
          inline: false
        }
      })
      
      const preview = wrapper.find('.preview-content')
      expect(preview.html()).toContain('katex')
    })
  })

  describe('错误处理', () => {
    it('应该显示错误消息而不是崩溃', () => {
      const wrapper = mount(FormulaPreview, {
        props: {
          latex: '\\invalid{command}',
          inline: false
        }
      })
      
      // 应该显示错误消息或降级处理
      expect(wrapper.exists()).toBe(true)
    })
  })
})
