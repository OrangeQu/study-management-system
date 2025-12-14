<template>
  <el-card class="gpa-stat-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <div class="header-left">
          <el-icon :size="20" :color="iconColor">
            <component :is="icon" />
          </el-icon>
          <span class="card-title">{{ title }}</span>
        </div>
        <el-tag v-if="tag" :type="tagType" size="small">{{ tag }}</el-tag>
      </div>
    </template>
    
    <div class="card-content">
      <div class="main-value" :style="{ color: valueColor }">
        {{ displayValue }}
      </div>
      <div class="value-label">{{ label }}</div>
      
      <!-- 进度条（如果有） -->
      <div v-if="showProgress" class="progress-container">
        <el-progress 
          :percentage="percentage" 
          :color="progressColor"
          :stroke-width="6"
          :show-text="false"
        />
        <div class="progress-info">
          <span class="current">{{ progressCurrent }}</span>
          <span class="total">/ {{ progressTotal }}</span>
        </div>
      </div>
      
      <!-- 子信息 -->
      <div v-if="subInfo" class="sub-info">
        {{ subInfo }}
      </div>
      
      <!-- 趋势指示器 -->
      <div v-if="showTrend" class="trend-indicator">
        <el-icon :color="trendColor" :size="16">
          <component :is="trendIcon" />
        </el-icon>
        <span class="trend-value">{{ trendValue }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

// 定义props
const props = defineProps({
  // 基本属性
  title: {
    type: String,
    required: true
  },
  value: {
    type: [Number, String],
    required: true
  },
  label: {
    type: String,
    default: ''
  },
  
  // 图标
  icon: {
    type: String,
    default: 'Trophy'
  },
  iconColor: {
    type: String,
    default: '#1890ff'
  },
  
  // 标签
  tag: {
    type: String,
    default: ''
  },
  tagType: {
    type: String,
    default: 'info'
  },
  
  // 进度条
  showProgress: {
    type: Boolean,
    default: false
  },
  percentage: {
    type: Number,
    default: 0
  },
  progressColor: {
    type: String,
    default: '#1890ff'
  },
  progressCurrent: {
    type: [Number, String],
    default: ''
  },
  progressTotal: {
    type: [Number, String],
    default: ''
  },
  
  // 子信息
  subInfo: {
    type: String,
    default: ''
  },
  
  // 趋势
  showTrend: {
    type: Boolean,
    default: false
  },
  trendValue: {
    type: String,
    default: ''
  },
  trendDirection: {
    type: String,
    default: 'up' // 'up', 'down', 'stable'
  }
})

// 计算属性
const displayValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toFixed(2)
  }
  return props.value
})

const valueColor = computed(() => {
  if (props.title.includes('GPA')) {
    const value = parseFloat(props.value)
    if (value >= 3.5) return '#52c41a'
    if (value >= 3.0) return '#1890ff'
    if (value >= 2.5) return '#faad14'
    return '#f5222d'
  }
  return '#1890ff'
})

const trendIcon = computed(() => {
  switch (props.trendDirection) {
    case 'up': return 'Top'
    case 'down': return 'Bottom'
    default: return 'Minus'
  }
})

const trendColor = computed(() => {
  switch (props.trendDirection) {
    case 'up': return '#52c41a'
    case 'down': return '#f5222d'
    default: return '#999'
  }
})
</script>

<style scoped>
.gpa-stat-card {
  height: 160px;
  transition: all 0.3s;
}

.gpa-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 !important;
  border-bottom: none !important;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.card-content {
  padding: 10px 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.main-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 8px;
}

.value-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 12px;
}

.progress-container {
  margin: 10px 0;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  font-size: 12px;
  color: #666;
}

.progress-info .current {
  font-weight: 600;
  color: #1890ff;
}

.sub-info {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}

.trend-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  font-size: 12px;
}

.trend-value {
  color: #666;
}
</style>