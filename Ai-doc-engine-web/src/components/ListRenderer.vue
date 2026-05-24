<template>
  <component :is="content.ordered ? 'ol' : 'ul'" class="list" :class="['list-level-' + level]">
    <template v-if="content.itemContents && content.itemContents.length > 0">
      <li v-for="(item, i) in content.itemContents" :key="i" class="list-item">
        <span class="list-item-content">
          <template v-for="(segment, j) in item.segments" :key="j">
            <a v-if="segment.linkUrl" :href="segment.linkUrl" target="_blank" class="link" :class="getRichTextClass(segment)">
              {{ segment.text }}
            </a>
            <span v-else-if="segment.inlineFormula" class="inline-formula">
              <FormulaPreview :latex="segment.inlineFormula" inline />
            </span>
            <span v-else-if="segment.text === '\n'" class="line-break"><br /></span>
            <span v-else :class="getRichTextClass(segment)">{{ segment.text }}</span>
          </template>
        </span>
        <ListRenderer v-if="item.nestedList" :content="item.nestedList" :level="level + 1" />
      </li>
    </template>
    <template v-else-if="content.items && content.items.length > 0">
      <li v-for="(item, i) in content.items" :key="i" class="list-item">
        {{ item }}
      </li>
    </template>
  </component>
</template>

<script setup lang="ts">
import FormulaPreview from './formula/FormulaPreview.vue'
import type { ListContent, RichText } from '@/types/document'

defineProps<{
  content: ListContent
  level: number
}>()

const getRichTextClass = (segment: RichText): string[] => {
  const classes: string[] = []
  if (segment.bold) classes.push('rich-bold')
  if (segment.italic) classes.push('rich-italic')
  if (segment.strikethrough) classes.push('rich-strikethrough')
  if (segment.code) classes.push('rich-code')
  return classes
}
</script>

<style scoped>
.list {
  padding-left: 24px;
  line-height: 1.8;
  color: #333;
  margin: 8px 0;
}

.list-level-0 {
  padding-left: 24px;
}

.list-level-1 {
  padding-left: 48px;
}

.list-level-2 {
  padding-left: 72px;
}

.list-level-3 {
  padding-left: 96px;
}

.list-item {
  margin: 4px 0;
}

.list-item-content {
  display: inline;
}

ol.list {
  list-style-type: decimal;
}

ol.list ol.list {
  list-style-type: lower-alpha;
}

ol.list ol.list ol.list {
  list-style-type: lower-roman;
}

ul.list {
  list-style-type: disc;
}

ul.list ul.list {
  list-style-type: circle;
}

ul.list ul.list ul.list {
  list-style-type: square;
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
  font-family: 'Consolas', 'Courier New', monospace;
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
}
</style>
