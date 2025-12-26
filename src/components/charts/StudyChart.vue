<template>
  <div class="study-chart">
    <div class="chart-header">
      <h3>学习统计</h3>
      <div class="chart-tabs">
        <el-radio-group v-model="activeChart" size="small">
          <el-radio-button label="daily">每日时长</el-radio-button>
          <el-radio-button label="subject">学习类型</el-radio-button>
          <el-radio-button label="completion">完成率</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <div class="chart-container">
      <v-chart 
        class="chart" 
        :option="chartOption" 
        :autoresize="true"
        :update-options="{ notMerge: true }"
        v-loading="loading"
      />
    </div>
    
    <div class="chart-stats">
      <div class="stat-item">
        <div class="stat-value">{{ totalStudyTime }}h</div>
        <div class="stat-label">本周学习总时长</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ avgDailyTime }}h</div>
        <div class="stat-label">本周日均学习</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ completionRate }}%</div>
        <div class="stat-label">任务完成率</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, defineProps } from 'vue'
import dayjs from 'dayjs'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import { trendStats } from '@/api/study'

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
])

const palette = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2']
const props = defineProps({
  tasks: {
    type: Array,
    default: () => []
  }
})
const TYPE_ORDER = ['作业', '复习', '考试', '生活']
const TYPE_COLORS = {
  作业: '#409EFF',
  复习: '#67C23A',
  考试: '#E6A23C',
  生活: '#F56C6C'
}
const buildEmptySubjectSeries = () => ([
  { name: '暂无数据', value: 1, color: '#d9d9d9', disabled: true }
])
const getTypeColor = (type) => TYPE_COLORS[type] || '#909399'
const isPendingTask = (task = {}) => {
  if (!task) return false
  if (task.completed) return false
  return task.status !== 'done'
}
const activeChart = ref('daily')
const loading = ref(false)
const chartOption = ref({})

const defaultStats = () => ({
  daily: [],
  distribution: [],
  completion: [],
  total_minutes: 0,
  average_minutes: 0,
  completion_rate: 0
})

const stats = ref(defaultStats())

const dailySeries = computed(() => {
  if (!stats.value.daily?.length) {
    return [
      { label: '暂无数据', value: 0 }
    ]
  }
  return stats.value.daily.map(item => {
    const minutes = Number(item.minutes || 0)
    return {
      label: dayjs(item.date).format('MM/DD'),
      value: Number((minutes / 60).toFixed(1))
    }
  })
})

const subjectSeries = computed(() => {
  const pendingTasks = props.tasks.filter(isPendingTask)
  if (!pendingTasks.length) {
    return buildEmptySubjectSeries()
  }
  const typeCounts = pendingTasks.reduce((map, task) => {
    const normalizedType = task.type || '其他'
    map[normalizedType] = (map[normalizedType] || 0) + 1
    return map
  }, {})
  const series = []
  TYPE_ORDER.forEach(type => {
    if (typeCounts[type]) {
      series.push({
        name: type,
        value: typeCounts[type],
        color: getTypeColor(type)
      })
    }
  })
  Object.entries(typeCounts).forEach(([type, count]) => {
    if (TYPE_ORDER.includes(type)) return
    series.push({
      name: type,
      value: count,
      color: getTypeColor(type)
    })
  })
  return series.length ? series : buildEmptySubjectSeries()
})

const completionSeries = computed(() => {
  const mapping = {
    completed: '已完成',
    ongoing: '未完成',
    todo: '待处理'
  }
  const list = stats.value.completion?.length ? stats.value.completion : []
  if (!list.length) {
    return [
      { name: '已完成', value: 0, color: '#52c41a' },
      { name: '未完成', value: 0, color: '#faad14' }
    ]
  }
  return list.map((item, idx) => ({
    name: mapping[item.name] || item.name,
    value: item.value || 0,
    color: palette[idx % palette.length]
  }))
})

const totalStudyTime = computed(() => {
  return (stats.value.total_minutes / 60).toFixed(1)
})

const avgDailyTime = computed(() => {
  return (stats.value.average_minutes / 60).toFixed(1)
})

const completionRate = computed(() => {
  return stats.value.completion_rate || 0
})

const loadStats = async () => {
  loading.value = true
  try {
    const resp = await trendStats()
    stats.value = {
      ...defaultStats(),
      ...(resp.data?.data)
    }
    renderChart()
  } catch (error) {
    ElMessage.error(error.message || '加载学习统计失败')
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  switch (activeChart.value) {
    case 'subject':
      renderSubjectChart()
      break
    case 'completion':
      renderCompletionChart()
      break
    default:
      renderDailyChart()
      break
  }
}

// Helper: read global CSS variables with fallback
const getCssVar = (name, fallback) => {
  try {
    if (typeof window === 'undefined') return fallback
    const style = getComputedStyle(document.documentElement)
    const v = style.getPropertyValue(name)
    return v ? v.trim() : fallback
  } catch (e) {
    return fallback
  }
}

const VAR_PRIMARY = () => getCssVar('--primary', '#1890ff')

const renderDailyChart = () => {
  chartOption.value = {
    title: {
      text: '本周学习时长趋势',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal',
        color: getCssVar('--nav-active', '#333')
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>学习时长：{c}小时',
      backgroundColor: getCssVar('--card-bg', '#ffffff'),
      textStyle: { color: getCssVar('--nav-active', '#333') }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dailySeries.value.map(item => item.label),
      axisLine: {
        lineStyle: {
          color: getCssVar('--page-border-color', '#d9d9d9')
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '小时',
      axisLine: {
        lineStyle: {
          color: getCssVar('--page-border-color', '#d9d9d9')
        }
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: getCssVar('--split-line', '#f0f0f0')
        }
      }
    },
    series: [
      {
        name: '学习时长',
        type: 'line',
        data: dailySeries.value.map(item => item.value),
        smooth: true,
        lineStyle: {
          width: 3,
          color: VAR_PRIMARY()
        },
        itemStyle: {
          color: VAR_PRIMARY()
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(24, 144, 255, 0.3)'
            }, {
              offset: 1, color: 'rgba(24, 144, 255, 0.05)'
            }]
          }
        },
        markPoint: {
          data: [
            { type: 'max', name: '最大值' },
            { type: 'min', name: '最小值' }
          ]
        }
      }
    ]
  }
}

const renderSubjectChart = () => {
  chartOption.value = {
    title: {
      text: '学习类型分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal',
        color: getCssVar('--nav-active', '#333')
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}个任务 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center',
      itemHeight: 12,
      itemWidth: 12,
      textStyle: { color: getCssVar('--muted', '#666') }
    },
    series: [
      {
        name: '任务类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: getCssVar('--card-bg', '#fff'),
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: subjectSeries.value.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: { color: item.color },
          label: {
            show: !item.disabled
          }
        }))
      }
    ]
  }
}

const renderCompletionChart = () => {
  chartOption.value = {
    title: {
      text: '任务完成情况',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal',
        color: getCssVar('--nav-active', '#333')
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}个 ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      left: 'center',
      textStyle: { color: getCssVar('--muted', '#666') }
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: getCssVar('--card-bg', '#fff'),
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14
          }
        },
        labelLine: {
          show: false
        },
        data: completionSeries.value.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: { color: item.color }
        }))
      }
    ]
  }
}

const handleSessionRecorded = () => {
  loadStats()
}

onMounted(() => {
  loadStats()
  window.addEventListener('study-session-recorded', handleSessionRecorded)
})

onUnmounted(() => {
  window.removeEventListener('study-session-recorded', handleSessionRecorded)
})

watch(activeChart, () => {
  renderChart()
})
watch(
  () => props.tasks,
  () => {
    if (activeChart.value === 'subject') {
      renderSubjectChart()
    }
  },
  { deep: true }
)
</script>

<style scoped>
.study-chart {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.chart-header h3 {
  margin: 0;
  color: var(--nav-active);
  font-size: 18px;
}

/* 将选中时的分段按钮颜色改为全局主色 */
.chart-tabs :deep(.el-radio-button.is-active) {
  background-color: var(--primary) !important;
  border-color: var(--primary) !important;
  color: #ffffff !important;
}
.chart-tabs :deep(.el-radio-button.is-active) .el-radio-button__inner {
  color: #ffffff !important;
}

.chart-container {
  height: 360px;
  margin-bottom: 24px;
}

.chart {
  width: 100%;
  height: 100%;
}

.chart-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
  padding: 12px;
  border-radius: 8px;
  background: #fafafa;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--muted);
}
</style>
