<template>
  <div class="table-editor">
    <div class="editor-toolbar">
      <el-button size="small" @click="addRow">
        <el-icon><Plus /></el-icon>
        添加行
      </el-button>
      <el-button size="small" @click="addColumn">
        <el-icon><Plus /></el-icon>
        添加列
      </el-button>
      <el-button size="small" @click="removeRow" :disabled="tableData.rows.length === 0">
        <el-icon><Minus /></el-icon>
        删除行
      </el-button>
      <el-button size="small" @click="removeColumn" :disabled="tableData.headers.length === 0">
        <el-icon><Minus /></el-icon>
        删除列
      </el-button>
      <el-button type="primary" size="small" @click="handleInsert">
        <el-icon><Check /></el-icon>
        插入表格
      </el-button>
    </div>

    <div class="table-container">
      <table class="editable-table">
        <thead>
          <tr>
            <th v-for="(_header, index) in tableData.headers" :key="`header-${index}`">
              <el-input
                v-model="tableData.headers[index]"
                placeholder="列标题"
                size="small"
              />
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, rowIndex) in tableData.rows" :key="`row-${rowIndex}`">
            <td v-for="(_cell, colIndex) in row" :key="`cell-${rowIndex}-${colIndex}`">
              <el-input
                v-model="tableData.rows[rowIndex][colIndex]"
                placeholder="单元格内容"
                size="small"
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="editor-hint">
      <el-icon><InfoFilled /></el-icon>
      <span>表格将以原生可编辑格式导出到 Word</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Minus, Check, InfoFilled } from '@element-plus/icons-vue'
import type { TableBlock } from '@/types/document'

const emit = defineEmits<{
  (e: 'insert', table: TableBlock): void
}>()

const tableData = reactive({
  headers: ['列1', '列2', '列3'],
  rows: [
    ['', '', ''],
    ['', '', '']
  ]
})

const addRow = () => {
  const newRow = new Array(tableData.headers.length).fill('')
  tableData.rows.push(newRow)
}

const addColumn = () => {
  tableData.headers.push(`列${tableData.headers.length + 1}`)
  tableData.rows.forEach(row => {
    row.push('')
  })
}

const removeRow = () => {
  if (tableData.rows.length > 0) {
    tableData.rows.pop()
  }
}

const removeColumn = () => {
  if (tableData.headers.length > 0) {
    tableData.headers.pop()
    tableData.rows.forEach(row => {
      row.pop()
    })
  }
}

const handleInsert = () => {
  if (tableData.headers.length === 0) {
    ElMessage.warning('表格至少需要一列')
    return
  }
  
  const tableBlock: TableBlock = {
    type: 'table',
    content: {
      headers: [...tableData.headers],
      rows: tableData.rows.map(row => [...row])
    }
  }
  
  emit('insert', tableBlock)
}
</script>

<style scoped>
.table-editor {
  padding: 20px;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.table-container {
  overflow: auto;
  max-height: 400px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.editable-table {
  width: 100%;
  border-collapse: collapse;
}

.editable-table th,
.editable-table td {
  border: 1px solid #e0e0e0;
  padding: 8px;
  min-width: 120px;
}

.editable-table th {
  background: #f5f5f5;
  font-weight: 600;
}

.editor-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 8px 12px;
  background: #f0f9ff;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}
</style>
