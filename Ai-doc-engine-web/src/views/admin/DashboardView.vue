<template>
  <div class="dashboard" v-loading="adminStore.loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">数据概览</h1>
        <p class="page-subtitle">实时监控系统运行状态</p>
      </div>
      <div class="header-actions">
        <button class="refresh-btn" @click="handleRefresh" :disabled="adminStore.loading">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="{ 'spinning': adminStore.loading }">
            <path d="M23 4v6h-6"/>
            <path d="M1 20v-6h6"/>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
          </svg>
          <span>刷新数据</span>
        </button>
      </div>
    </div>

    <!-- 统计卡片网格 -->
    <div class="stats-grid">
      <div 
        v-for="(stat, index) in stats" 
        :key="stat.key"
        class="stat-card"
        :style="{ '--delay': `${index * 0.1}s` }"
      >
        <div class="stat-card-inner">
          <div class="stat-icon" :class="`stat-icon--${stat.type}`">
            <span v-html="stat.icon"></span>
          </div>
          <div class="stat-content">
            <div class="stat-header">
              <span class="stat-label">{{ stat.label }}</span>
              <div class="stat-trend" v-if="stat.showTrend && getTrendValue(stat.key)" :class="getTrendClass(stat.key)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline :points="getTrendValue(stat.key)! >= 0 ? '18 15 12 9 6 15' : '6 9 12 15 18 9'"/>
                </svg>
                <span>{{ formatTrend(getTrendValue(stat.key)!) }}</span>
              </div>
            </div>
            <div class="stat-value-wrapper">
              <span class="stat-value">{{ formatNumber(getStatValue(stat.key)) }}</span>
              <span class="stat-unit" v-if="stat.unit">{{ stat.unit }}</span>
            </div>
          </div>
        </div>
        <div class="stat-decoration"></div>
      </div>
    </div>

    <!-- 趋势图区域 -->
    <div class="charts-section">
      <div class="section-header">
        <h2 class="section-title">数据趋势</h2>
        <div class="range-tabs">
          <button 
            v-for="range in rangeOptions" 
            :key="range.value"
            class="range-tab"
            :class="{ active: currentRange === range.value }"
            @click="handleRangeChange(range.value)"
          >
            {{ range.label }}
            <span class="range-description">{{ range.description }}</span>
          </button>
        </div>
      </div>
      
      <div class="charts-grid" v-loading="trendLoading">
        <div v-if="trendError" class="chart-error">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <p>{{ trendError }}</p>
          <button class="retry-btn" @click="handleRetryTrend">重试</button>
        </div>
        
        <template v-else>
          <!-- 用户趋势图 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">用户趋势</h3>
              <div class="chart-legend">
                <span class="legend-item">
                  <i class="legend-dot legend-dot--primary"></i>
                  新增用户
                </span>
                <span class="legend-item">
                  <i class="legend-dot legend-dot--success"></i>
                  活跃用户
                </span>
              </div>
            </div>
            <div ref="userChartRef" class="chart-container"></div>
          </div>

          <!-- 导出趋势图 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">导出趋势</h3>
            </div>
            <div ref="exportChartRef" class="chart-container"></div>
          </div>

          <!-- OCR趋势图 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">OCR使用趋势</h3>
            </div>
            <div ref="ocrChartRef" class="chart-container"></div>
          </div>

          <!-- 反馈趋势图 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">反馈趋势</h3>
            </div>
            <div ref="feedbackChartRef" class="chart-container"></div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, computed, ref, watch, nextTick } from 'vue'
import { useAdminStore } from '@/store/admin'
import * as echarts from 'echarts'

const adminStore = useAdminStore()
const dashboard = computed(() => adminStore.dashboard)
const trend = computed(() => adminStore.trend)

const trendLoading = ref(false)
const trendError = ref<string | null>(null)

// 图表引用
const userChartRef = ref<HTMLElement | null>(null)
const exportChartRef = ref<HTMLElement | null>(null)
const ocrChartRef = ref<HTMLElement | null>(null)
const feedbackChartRef = ref<HTMLElement | null>(null)

// 图表实例
let userChart: echarts.ECharts | null = null
let exportChart: echarts.ECharts | null = null
let ocrChart: echarts.ECharts | null = null
let feedbackChart: echarts.ECharts | null = null

// 时间范围
const currentRange = ref('day')
const rangeOptions = [
  { label: '日级', value: 'day', description: '最近7天' },
  { label: '月级', value: 'year', description: '最近12个月' }
]

// 统计数据配置
interface StatItem {
  key: string
  label: string
  type: string
  icon: string
  showTrend?: boolean
  unit?: string
}

const stats: StatItem[] = [
  {
    key: 'totalUsers',
    label: '总用户数',
    type: 'primary',
    showTrend: true,
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>'
  },
  {
    key: 'totalExports',
    label: '总导出数',
    type: 'success',
    showTrend: true,
    unit: '次',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>'
  },
  {
    key: 'totalTemplates',
    label: '模板数',
    type: 'purple',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>'
  },
  {
    key: 'ocrFailedCount',
    label: 'OCR使用次数',
    type: 'danger',
    showTrend: true,
    unit: '次',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>'
  },
  {
    key: 'totalFeedbacks',
    label: '总反馈数',
    type: 'warning',
    showTrend: true,
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>'
  },
  {
    key: 'totalAnnouncements',
    label: '公告总数',
    type: 'info',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 17H2a3 3 0 0 0 3-3V9a7 7 0 0 1 14 0v5a3 3 0 0 0 3 3zm-8.27 4a2 2 0 0 1-3.46 0"/></svg>'
  }
]

// 获取统计值
const getStatValue = (key: string): number => {
  const data = dashboard.value
  if (!data) return 0
  const keyMap: Record<string, number> = {
    totalUsers: data.totalUsers ?? 0,
    totalExports: data.totalExports ?? 0,
    totalTemplates: data.totalTemplates ?? 0,
    ocrFailedCount: data.ocrFailedCount ?? 0,
    totalFeedbacks: data.totalFeedbacks ?? 0,
    totalAnnouncements: data.totalAnnouncements ?? 0
  }
  return keyMap[key] ?? 0
}

// 获取趋势值
const getTrendValue = (key: string): number | null => {
  const data = dashboard.value
  if (!data) return null
  const keyMap: Record<string, number | undefined> = {
    totalUsers: data.userGrowthRate,
    totalExports: data.exportGrowthRate,
    ocrFailedCount: data.ocrGrowthRate,
    totalFeedbacks: data.feedbackGrowthRate
  }
  const value = keyMap[key]
  return value !== undefined ? value : null
}

// 获取趋势样式类
const getTrendClass = (key: string): string => {
  const value = getTrendValue(key)
  if (value === null) return ''
  // 对于OCR失败次数，增长是负面的
  if (key === 'ocrFailedCount') {
    return value > 0 ? 'trend--down' : 'trend--up'
  }
  return value >= 0 ? 'trend--up' : 'trend--down'
}

// 格式化趋势值
const formatTrend = (value: number): string => {
  const prefix = value >= 0 ? '+' : ''
  return `${prefix}${value.toFixed(1)}%`
}

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toLocaleString()
}

// 刷新数据
const handleRefresh = async () => {
  adminStore.fetchDashboard()
  await fetchTrendData()
}

// 切换时间范围
const handleRangeChange = async (range: string) => {
  currentRange.value = range
  await fetchTrendData()
}

// 获取趋势数据
const fetchTrendData = async () => {
  trendLoading.value = true
  trendError.value = null
  
  try {
    const apiRange = currentRange.value === 'day' ? 'week' : 'year'
    await adminStore.fetchTrend(apiRange)
  } catch (error) {
    trendError.value = '加载趋势数据失败，请稍后重试'
    console.error('Failed to fetch trend data:', error)
  } finally {
    trendLoading.value = false
  }
}

// 重试加载趋势数据
const handleRetryTrend = () => {
  fetchTrendData()
}

// 初始化图表
const initCharts = () => {
  // 销毁旧图表
  userChart?.dispose()
  exportChart?.dispose()
  ocrChart?.dispose()
  feedbackChart?.dispose()

  // 初始化新图表
  if (userChartRef.value) {
    userChart = echarts.init(userChartRef.value)
  }
  if (exportChartRef.value) {
    exportChart = echarts.init(exportChartRef.value)
  }
  if (ocrChartRef.value) {
    ocrChart = echarts.init(ocrChartRef.value)
  }
  if (feedbackChartRef.value) {
    feedbackChart = echarts.init(feedbackChartRef.value)
  }
}

// 更新图表数据
const updateCharts = () => {
  const trendData = trend.value
  if (!trendData) return

  const commonOption = {
    animation: true,
    animationDuration: 800,
    animationEasing: 'cubicOut' as const,
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: {
        color: '#374151'
      },
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.dates,
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280',
        rotate: currentRange.value === 'year' ? 45 : 0,
        interval: currentRange.value === 'year' ? 0 : 'auto'
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#6b7280'
      },
      splitLine: {
        lineStyle: {
          color: '#f3f4f6'
        }
      }
    }
  }

  // 用户趋势图
  if (userChart) {
    userChart.setOption({
      ...commonOption,
      series: [
        {
          name: '新增用户',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            color: '#6366f1',
            width: 2
          },
          itemStyle: {
            color: '#6366f1'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(99, 102, 241, 0.2)' },
              { offset: 1, color: 'rgba(99, 102, 241, 0)' }
            ])
          },
          data: trendData.newUsers
        },
        {
          name: '活跃用户',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            color: '#10b981',
            width: 2
          },
          itemStyle: {
            color: '#10b981'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(16, 185, 129, 0.2)' },
              { offset: 1, color: 'rgba(16, 185, 129, 0)' }
            ])
          },
          data: trendData.activeUsers
        }
      ]
    }, true)
  }

  // 导出趋势图
  if (exportChart) {
    exportChart.setOption({
      ...commonOption,
      series: [
        {
          name: '导出次数',
          type: 'bar',
          barWidth: '40%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#10b981' },
              { offset: 1, color: '#34d399' }
            ]),
            borderRadius: [4, 4, 0, 0]
          },
          data: trendData.exports
        }
      ]
    }, true)
  }

  // OCR趋势图
  if (ocrChart) {
    ocrChart.setOption({
      ...commonOption,
      series: [
        {
          name: 'OCR次数',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            color: '#f59e0b',
            width: 2
          },
          itemStyle: {
            color: '#f59e0b'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(245, 158, 11, 0.2)' },
              { offset: 1, color: 'rgba(245, 158, 11, 0)' }
            ])
          },
          data: trendData.ocrCount
        }
      ]
    }, true)
  }

  // 反馈趋势图
  if (feedbackChart) {
    feedbackChart.setOption({
      ...commonOption,
      series: [
        {
          name: '反馈次数',
          type: 'bar',
          barWidth: '40%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#f59e0b' },
              { offset: 1, color: '#fbbf24' }
            ]),
            borderRadius: [4, 4, 0, 0]
          },
          data: trendData.feedbacks
        }
      ]
    }, true)
  }
}

// 监听窗口大小变化
const handleResize = () => {
  userChart?.resize()
  exportChart?.resize()
  ocrChart?.resize()
  feedbackChart?.resize()
}

// 监听趋势数据变化
watch(trend, () => {
  nextTick(() => {
    updateCharts()
  })
}, { deep: true })

onMounted(async () => {
  adminStore.fetchDashboard()
  await fetchTrendData()
  
  nextTick(() => {
    initCharts()
    updateCharts()
  })
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  userChart?.dispose()
  exportChart?.dispose()
  ocrChart?.dispose()
  feedbackChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* ==================== CSS 变量 ==================== */
.dashboard {
  /* 主色调 */
  --color-primary: #6366f1;
  --color-primary-light: #eef2ff;
  --color-success: #10b981;
  --color-success-light: #ecfdf5;
  --color-warning: #f59e0b;
  --color-warning-light: #fffbeb;
  --color-danger: #ef4444;
  --color-danger-light: #fef2f2;
  --color-info: #3b82f6;
  --color-info-light: #eff6ff;
  --color-purple: #8b5cf6;
  --color-purple-light: #f5f3ff;

  /* 中性色 */
  --color-slate-50: #f8fafc;
  --color-slate-100: #f1f5f9;
  --color-slate-200: #e2e8f0;
  --color-slate-300: #cbd5e1;
  --color-slate-400: #94a3b8;
  --color-slate-500: #64748b;
  --color-slate-600: #475569;
  --color-slate-700: #334155;
  --color-slate-800: #1e293b;
  --color-slate-900: #0f172a;

  /* 字体 */
  --font-size-xs: 12px;
  --font-size-sm: 14px;
  --font-size-base: 16px;
  --font-size-lg: 18px;
  --font-size-xl: 24px;
  --font-size-2xl: 30px;

  /* 间距 */
  --spacing-1: 4px;
  --spacing-2: 8px;
  --spacing-3: 12px;
  --spacing-4: 16px;
  --spacing-5: 20px;
  --spacing-6: 24px;
  --spacing-8: 32px;

  /* 圆角 */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-full: 9999px;

  /* 阴影 */
  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);

  /* 过渡 */
  --transition-fast: 150ms ease;
  --transition-base: 200ms ease;
  --transition-slow: 300ms ease;
}

/* ==================== 页面头部 ==================== */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--spacing-6);
  flex-wrap: wrap;
  gap: var(--spacing-4);
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--color-slate-800);
  margin: 0 0 var(--spacing-1) 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-slate-500);
  margin: 0;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-4);
  background: #fff;
  border: 1px solid var(--color-slate-200);
  border-radius: var(--radius-md);
  color: var(--color-slate-600);
  font-size: var(--font-size-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
}

.refresh-btn:hover:not(:disabled) {
  background: var(--color-slate-50);
  border-color: var(--color-slate-300);
  color: var(--color-slate-700);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-btn svg {
  width: 16px;
  height: 16px;
}

.refresh-btn svg.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ==================== 统计卡片网格 ==================== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--spacing-5);
  margin-bottom: var(--spacing-8);
}

.stat-card {
  background: #fff;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
  animation: slideUp 0.5s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
  transform: translateY(20px);
  position: relative;
}

@keyframes slideUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stat-card-inner {
  padding: var(--spacing-5);
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-4);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform var(--transition-base);
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
}

.stat-icon :deep(svg) {
  width: 26px;
  height: 26px;
}

.stat-icon--primary {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.stat-icon--success {
  background: var(--color-success-light);
  color: var(--color-success);
}

.stat-icon--danger {
  background: var(--color-danger-light);
  color: var(--color-danger);
}

.stat-icon--warning {
  background: var(--color-warning-light);
  color: var(--color-warning);
}

.stat-icon--info {
  background: var(--color-info-light);
  color: var(--color-info);
}

.stat-icon--purple {
  background: var(--color-purple-light);
  color: var(--color-purple);
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-2);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-slate-500);
  font-weight: 500;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.stat-trend svg {
  width: 14px;
  height: 14px;
}

.trend--up {
  color: var(--color-success);
}

.trend--down {
  color: var(--color-danger);
}

.stat-value-wrapper {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-1);
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--color-slate-800);
  line-height: 1;
}

.stat-unit {
  font-size: var(--font-size-sm);
  color: var(--color-slate-400);
}

.stat-decoration {
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary) 0%, transparent 100%);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.stat-card:hover .stat-decoration {
  opacity: 1;
}

/* ==================== 趋势图区域 ==================== */
.charts-section {
  margin-top: var(--spacing-6);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-5);
  flex-wrap: wrap;
  gap: var(--spacing-4);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-slate-800);
  margin: 0;
}

.range-tabs {
  display: flex;
  gap: var(--spacing-2);
  background: var(--color-slate-100);
  padding: 4px;
  border-radius: var(--radius-md);
}

.range-tab {
  padding: var(--spacing-2) var(--spacing-4);
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-slate-500);
  cursor: pointer;
  transition: all var(--transition-base);
}

.range-tab:hover {
  color: var(--color-slate-700);
}

.range-tab.active {
  background: #fff;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.range-description {
  font-size: 11px;
  opacity: 0.7;
  margin-left: 4px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-5);
}

.chart-card {
  background: #fff;
  border-radius: var(--radius-xl);
  padding: var(--spacing-5);
  box-shadow: var(--shadow-sm);
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-4);
}

.chart-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-slate-700);
  margin: 0;
}

.chart-legend {
  display: flex;
  gap: var(--spacing-4);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  font-size: var(--font-size-xs);
  color: var(--color-slate-500);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-dot--primary {
  background: var(--color-primary);
}

.legend-dot--success {
  background: var(--color-success);
}

.chart-container {
  height: 240px;
  width: 100%;
}

.chart-error {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-8);
  background: #fff;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  text-align: center;
}

.chart-error svg {
  width: 48px;
  height: 48px;
  color: var(--color-danger);
  margin-bottom: var(--spacing-4);
}

.chart-error p {
  color: var(--color-slate-600);
  margin: 0 0 var(--spacing-4) 0;
  font-size: var(--font-size-sm);
}

.retry-btn {
  padding: var(--spacing-2) var(--spacing-4);
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
}

.retry-btn:hover {
  background: #4f46e5;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    display: flex;
    justify-content: flex-end;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .range-tabs {
    width: 100%;
    justify-content: center;
  }
}
</style>
