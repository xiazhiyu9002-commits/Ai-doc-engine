<template>
  <div class="latex-editor">
    <div class="editor-toolbar">
      <el-button-group>
        <el-button size="small" @click="insertSymbol('\\frac{}{}')">
          分数
        </el-button>
        <el-button size="small" @click="insertSymbol('\\sqrt{}')">
          根号
        </el-button>
        <el-button size="small" @click="insertSymbol('^{}')">
          上标
        </el-button>
        <el-button size="small" @click="insertSymbol('_{}')">
          下标
        </el-button>
        <el-button size="small" @click="insertSymbol('\\int_{}^{}')">
          积分
        </el-button>
        <el-button size="small" @click="insertSymbol('\\sum_{}^{}')">
          求和
        </el-button>
        <el-button size="small" @click="insertSymbol('\\lim_{}')">
          极限
        </el-button>
      </el-button-group>
    </div>
    
    <el-input
      ref="inputRef"
      v-model="latex"
      type="textarea"
      :rows="5"
      placeholder="请输入 LaTeX 公式，例如：x = \frac{-b \pm \sqrt{b^2-4ac}}{2a}"
      @input="handleInput"
    />
    
    <div class="editor-hint">
      <el-icon><InfoFilled /></el-icon>
      <div>
        <p><strong>LaTeX 公式编辑说明：</strong></p>
        <ul>
          <li>LaTeX 是唯一的公式输入格式</li>
          <li>支持标准 LaTeX 数学公式语法</li>
          <li>公式将转换为 Word 原生公式（OMML）</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const inputRef = ref()
const latex = ref(props.modelValue)

watch(() => props.modelValue, (newVal) => {
  latex.value = newVal
})

const handleInput = () => {
  emit('update:modelValue', latex.value)
}

const insertSymbol = (symbol: string) => {
  const textarea = inputRef.value?.textarea
  if (!textarea) {
    latex.value += symbol
    handleInput()
    return
  }
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = latex.value
  
  latex.value = text.substring(0, start) + symbol + text.substring(end)
  handleInput()
  
  // 设置光标位置到插入符号的中间
  setTimeout(() => {
    const cursorPos = start + symbol.indexOf('{}')
    textarea.setSelectionRange(cursorPos, cursorPos)
    textarea.focus()
  }, 0)
}
</script>

<style scoped>
.latex-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.editor-hint {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.editor-hint p {
  margin: 0 0 8px;
  font-weight: 600;
  color: #333;
}

.editor-hint ul {
  margin: 0;
  padding-left: 20px;
}

.editor-hint li {
  margin: 4px 0;
  line-height: 1.5;
}

:deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
}
</style>
