<template>
  <div class="gpa-trend-chart">
    <div class="chart-header">
      <div class="header-left">
        <h3>{{ title }}</h3>
        <div class="chart-stats">
          <span class="stat-item">
            <span class="stat-label">最高:</span>
            <span class="stat-value">{{ maxGPA.toFixed(2) }}</span>
          </span>
          <span class="stat-item">
            <span class="stat-label">平均:</span>
            <span class="stat-value">{{ avgGPA.toFixed(2) }}</span>
          </span>
          <span class="stat-item">
            <span class="stat-label">趋势:</span>
            <span class="stat-value" :style="{ color: trendColor }">
              {{ trendText }}
            </span>
          </span>
        </div>
      </div>
      <!-- 移除 header-right 部分 -->
    </div>
    
    <div class="chart-container">
      <div ref="chartRef" class="chart-canvas"></div>
    </div>
    
    <div class="chart-footer">
      <div class="semester-labels">
        <span 
          v-for="(label, index) in semesterLabels" 
          :key="index"
          class="semester-label"
          :class="{ active: activeSemester === label }"
          @click="highlightSemester(label)"
        >
          {{ label }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: {
    type: String,
    default: 'GPA趋势图'
  },
  gpaData: {
    type: Array,
    default: () => []
  },
  height: {
    type: String,
    default: '300px'
  }
})

const emit = defineEmits(['semester-click'])

const chartRef = ref(null)
let chartInstance = null

// 计算属性 - 直接使用 props.gpaData
const semesterLabels = computed(() => {
  return props.gpaData.map(item => item.semester)
})

const gpaValues = computed(() => {
  return props.gpaData.map(item => item.gpa)
})

const maxGPA = computed(() => {
  return gpaValues.value.length > 0 ? Math.max(...gpaValues.value) : 0
})

const avgGPA = computed(() => {
  if (gpaValues.value.length === 0) return 0
  return gpaValues.value.reduce((a, b) => a + b, 0) / gpaValues.value.length
})

const trendDirection = computed(() => {
  if (gpaValues.value.length < 2) return 'stable'
  
  const last = gpaValues.value[gpaValues.value.length - 1]
  const secondLast = gpaValues.value[gpaValues.value.length - 2]
  
  if (last > secondLast) return 'up'
  if (last < secondLast) return 'down'
  return 'stable'
})

const trendColor = computed(() => {
  switch (trendDirection.value) {
    case 'up': return '#52c41a'
    case 'down': return '#f5222d'
    default: return '#999'
  }
})

const trendText = computed(() => {
  switch (trendDirection.value) {
    case 'up': return '上升'
    case 'down': return '下降'
    default: return '稳定'
  }
})

const activeSemester = ref('')

// 方法
const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const data = params[0]
        return `
          <div style="padding: 8px">
            <div style="font-weight: 600; margin-bottom: 4px">${data.name}</div>
            <div style="display: flex; align-items: center; gap: 8px">
              <span>GPA: </span>
              <span style="font-weight: 700; color: #1890ff">${data.value.toFixed(2)}</span>
            </div>
            <div style="margin-top: 4px; font-size: 12px; color: #666">
              排名: ${data.dataIndex + 1}/${gpaValues.value.length}
            </div>
          </div>
        `
      }
    },
    xAxis: {
      type: 'category',
      data: semesterLabels.value,
      axisLabel: {
        rotate: 45,
        color: '#666'
      },
      axisLine: {
        lineStyle: {
          color: '#e8e8e8'
        }
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 5,
      interval: 0.5,
      axisLabel: {
        formatter: '{value}',
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          // 背景虚线颜色（已调整为更柔和的灰色）
          color: '#FCF2EA'
        }
      }
    },
    series: [
      {
        name: 'GPA',
        type: 'line',
        data: gpaValues.value,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#1890ff'
        },
        itemStyle: {
          color: '#1890ff',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.4)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.1)' }
          ])
        },
        markLine: {
          silent: true,
          lineStyle: {
            // 平均线颜色（使用项目 accent 色调）
            color: '#fb8c4a',
            width: 2,
            type: 'dashed'
          },
          data: [
            {
              yAxis: avgGPA.value,
              label: {
                formatter: ({ value }) => `平均: ${Number(value).toFixed(2)}`,
                position: 'end',
                distance: 10,
                color: '#fb8c4a',
                backgroundColor: 'rgba(255,255,255,0.95)',
                padding: [2, 8],
                borderRadius: 4,
                fontWeight: 600
              }
            }
          ]
        }
      }
    ],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    }
  }
  
  chartInstance.setOption(option)
  
  // 添加点击事件
  chartInstance.off('click')
  chartInstance.on('click', (params) => {
    if (params.componentType === 'series') {
      const semester = semesterLabels.value[params.dataIndex]
      activeSemester.value = semester
      emit('semester-click', semester)
    }
  })
}

const highlightSemester = (semester) => {
  activeSemester.value = semester
  emit('semester-click', semester)
}

// 监听数据变化
watch(() => props.gpaData, () => {
  nextTick(() => {
    if (chartInstance) {
      updateChart()
    }
  })
}, { deep: true })

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    initChart()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.gpa-trend-chart {
  width: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 0 10px;
}

.header-left h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.chart-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  font-size: 13px;
}

.stat-label {
  color: #666;
  margin-right: 4px;
}

.stat-value {
  font-weight: 600;
}

.chart-container {
  height: v-bind(height);
  width: 100%;
}

.chart-canvas {
  width: 100%;
  height: 100%;
}

.chart-footer {
  margin-top: 15px;
  padding: 0 10px;
}

.semester-labels {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.semester-label {
  font-size: 12px;
  color: #999;
  padding: 2px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.semester-label:hover {
  background: #f0f0f0;
}

.semester-label.active {
  background: #1890ff;
  color: white;
}
</style>
