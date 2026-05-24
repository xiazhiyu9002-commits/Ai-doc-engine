<template>
  <div class="template-panel">
    <div class="panel-header">
      <h3>模板设置</h3>
      <el-button size="small" @click="handleRefresh" :loading="templateStore.loading">
        <el-icon><Refresh /></el-icon>
      </el-button>
    </div>
    <div class="panel-body">
      <el-empty v-if="!templateStore.templates.length" description="暂无模板" />
      <div v-else class="template-list">
        <div
          v-for="template in templateStore.templates"
          :key="template.id"
          class="template-item"
          :class="{ active: templateStore.selectedTemplate?.id === template.id }"
          @click="handleSelect(template)"
        >
          <div class="template-info">
            <h4>{{ template.name }}</h4>
            <p>{{ template.description || '无描述' }}</p>
          </div>
          <el-icon v-if="templateStore.selectedTemplate?.id === template.id" color="#409eff">
            <Check />
          </el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Check } from '@element-plus/icons-vue'
import { useTemplateStore } from '@/store/template'
import type { Template } from '@/types/template'

const emit = defineEmits<{
  (e: 'select', template: Template): void
}>()

const templateStore = useTemplateStore()

onMounted(() => {
  handleRefresh()
})

const handleRefresh = async () => {
  try {
    await templateStore.fetchTemplates()
  } catch (error: any) {
    ElMessage.error(error.message || '加载模板失败')
  }
}

const handleSelect = (template: Template) => {
  templateStore.selectTemplate(template)
  emit('select', template)
  ElMessage.success(`已选择模板：${template.name}`)
}
</script>

<style scoped>
.template-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e0e0e0;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.panel-body {
  flex: 1;
  padding: 12px;
  overflow: auto;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.template-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.template-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.template-item.active {
  border-color: #409eff;
  background: #e6f7ff;
}

.template-info h4 {
  margin: 0 0 4px;
  font-size: 14px;
  color: #333;
}

.template-info p {
  margin: 0;
  font-size: 12px;
  color: #999;
}
</style>
