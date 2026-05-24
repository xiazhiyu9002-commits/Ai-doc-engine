<template>
  <router-view />
</template>

<script setup lang="ts">
// App 根组件
</script>

<style>
:root {
  /* 字体大小系统 - 基于 15px 基准 */
  --font-size-base: 15px;
  --font-size-sm: 12px;
  --font-size-md: 14px;
  --font-size-lg: 16px;
  --font-size-xl: 18px;
  --font-size-2xl: 20px;
  --font-size-3xl: 24px;
  --font-size-4xl: 32px;
  
  /* 行高 */
  --line-height-tight: 1.3;
  --line-height-base: 1.6;
  --line-height-relaxed: 1.8;
  
  /* 间距 */
  --spacing-xs: 8px;
  --spacing-sm: 12px;
  --spacing-md: 16px;
  --spacing-lg: 20px;
  --spacing-xl: 24px;
  
  /* 颜色 */
  --color-text-primary: #24292e;
  --color-text-secondary: #333;
  --color-text-muted: #666;
  --color-heading: #1f4e79;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: var(--font-size-base);
  line-height: var(--line-height-base);
  color: var(--color-text-primary);
}

/* 统一标题样式 */
h1, h2, h3, h4, h5, h6 {
  color: var(--color-heading);
  font-weight: bold;
  line-height: var(--line-height-tight);
  margin: var(--spacing-md) 0 var(--spacing-sm);
}

h1 { font-size: var(--font-size-4xl); margin-top: var(--spacing-xl); margin-bottom: var(--spacing-md); border-bottom: 2px solid var(--color-heading); padding-bottom: 6px; }
h2 { font-size: var(--font-size-3xl); margin-top: var(--spacing-lg); margin-bottom: var(--spacing-sm); }
h3 { font-size: var(--font-size-2xl); margin-top: var(--spacing-md); margin-bottom: var(--spacing-sm); }
h4 { font-size: var(--font-size-xl); margin-top: var(--spacing-md); margin-bottom: var(--spacing-xs); }
h5 { font-size: var(--font-size-lg); margin-top: var(--spacing-sm); margin-bottom: var(--spacing-xs); }
h6 { font-size: var(--font-size-md); margin-top: var(--spacing-sm); margin-bottom: 4px; color: var(--color-text-muted); }

/* 统一段落样式 */
p {
  line-height: var(--line-height-relaxed);
  color: var(--color-text-secondary);
  margin: var(--spacing-xs) 0;
  text-align: justify;
}

/* 统一代码样式 */
code {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.9em;
  color: #c7254e;
}

/* 统一表格样式 */
table {
  width: 100%;
  border-collapse: collapse;
  margin: var(--spacing-md) 0;
  font-size: var(--font-size-md);
}

table th,
table td {
  border: 1px solid #000000;
  padding: 8px 12px;
  text-align: left;
}

table th {
  background: #d9d9d9;
  font-weight: 600;
  color: #000;
}

table td {
  background: #ffffff;
}

/* KaTeX 公式全局样式优化 */
.katex-display {
  margin: 0 !important;
  padding: 0 !important;
}

.katex-display > .katex {
  margin: 0 !important;
  padding: 0 !important;
}

.katex {
  line-height: 1.2 !important;
}

.katex-html {
  line-height: 1.2 !important;
}

/* 隐藏 KaTeX 公式周围的菜单触发器（小三角形） */
.katex .katex-html .hide {
  display: none !important;
}

.katex .katex-html .mspace.hide {
  display: none !important;
}

/* 隐藏所有 KaTeX 菜单相关元素 */
.katex-html .hide,
.katex .hide {
  display: none !important;
  visibility: hidden !important;
}

/* 禁用公式的拖动功能，但保留文本选择 */
.katex,
.katex *,
.katex-display,
.katex-display *,
.formula .katex,
.formula .katex *,
.inline-formula .katex,
.inline-formula .katex * {
  -webkit-user-drag: none !important;
  -khtml-user-drag: none !important;
  -moz-user-drag: none !important;
  -o-user-drag: none !important;
  user-drag: none !important;
}

/* 禁用公式内所有图片的调整大小控制器 */
.katex img,
.katex-display img,
.formula img,
.inline-formula img,
.preview-content img {
  -webkit-user-drag: none !important;
  user-drag: none !important;
  pointer-events: none !important;
  -webkit-user-select: none !important;
  user-select: none !important;
}

/* 隐藏可能的调整大小控制器 */
.katex::before,
.katex::after,
.katex-display::before,
.katex-display::after,
.formula::before,
.formula::after {
  display: none !important;
  content: none !important;
}

/* 禁用 contenteditable 相关的调整大小功能 */
.katex[contenteditable],
.katex-display[contenteditable],
.formula[contenteditable],
.inline-formula[contenteditable] {
  -webkit-user-modify: read-only !important;
  user-modify: read-only !important;
}

/* 移除所有可能的 resize 句柄 */
.katex *::before,
.katex *::after,
.katex-display *::before,
.katex-display *::after {
  display: none !important;
  content: none !important;
}

/* 完全禁用浏览器的调整大小控制器 - 针对所有公式相关元素 */
.formula-preview,
.formula-preview *,
.preview-content,
.preview-content *,
.formula,
.formula *,
.inline-formula,
.inline-formula *,
.katex,
.katex *,
.katex-display,
.katex-display *,
.katex-html,
.katex-html * {
  resize: none !important;
  -webkit-appearance: none !important;
  -moz-appearance: none !important;
  appearance: none !important;
}

/* 禁用 Chromium 浏览器的调整大小句柄 */
.formula-preview::-webkit-resizer,
.preview-content::-webkit-resizer,
.formula::-webkit-resizer,
.inline-formula::-webkit-resizer,
.katex::-webkit-resizer,
.katex-display::-webkit-resizer {
  display: none !important;
  background: transparent !important;
  width: 0 !important;
  height: 0 !important;
}

/* 移除所有伪元素 */
.formula-preview::before,
.formula-preview::after,
.preview-content::before,
.preview-content::after {
  display: none !important;
  content: none !important;
}
</style>
