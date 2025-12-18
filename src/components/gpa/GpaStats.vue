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
      <div class="main-value" :style="{ color: 'var(--primary)' }">
        {{ displayValue }}
      </div>
      <div class="value-label" :style="{ color: 'var(--muted)' }">{{ label }}</div>

      <!-- 进度条（如果有） -->
      <div v-if="showProgress" class="progress-container">
        <el-progress 
          :percentage="percentage" 
          :color="'var(--accent)'"
          :stroke-width="6"
          :show-text="false"
        />
        <div class="progress-info">
          <span class="current" :style="{ color: 'var(--primary)' }">{{ progressCurrent }}</span>
          <span class="total" :style="{ color: 'var(--muted)' }">/ {{ progressTotal }}</span>
        </div>
      </div>

      <!-- 子信息 -->
      <div v-if="subInfo" class="sub-info" :style="{ color: 'var(--muted)' }">
        {{ subInfo }}
      </div>

      <!-- 趋势指示器 -->
      <div v-if="showTrend" class="trend-indicator">
        <el-icon :color="trendColor" :size="16">
          <component :is="trendIcon" />
        </el-icon>
        <span class="trend-value" :style="{ color: 'var(--muted)' }">{{ trendValue }}</span>
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
    default: 'var(--primary)'
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
    default: 'var(--accent)'
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

const trendIcon = computed(() => {
  switch (props.trendDirection) {
    case 'up': return 'Top'
    case 'down': return 'Bottom'
    default: return 'Minus'
  }
})

const trendColor = computed(() => {
  switch (props.trendDirection) {
    case 'up': return 'var(--soft-green)'
    case 'down': return 'var(--soft-red)'
    default: return 'var(--muted)'
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
  box-shadow: 0 4px 12px var(--soft-shadow);
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
  color: var(--muted);
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
  color: var(--muted);
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
  color: var(--muted);
}

.progress-info .current {
  font-weight: 600;
  color: var(--primary);
}

.sub-info {
  font-size: 12px;
  color: var(--muted);
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
  color: var(--muted);
}
</style>