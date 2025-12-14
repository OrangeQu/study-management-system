<template>
  <div class="task-stats">
    <!-- 任务统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon><List /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ totalTasks }}</div>
          <div class="stat-label">总任务数</div>
        </div>
      </div>

      <div class="stat-card doing">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ doingTasks }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>

      <div class="stat-card done">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ doneTasks }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>

      <div class="stat-card urgent">
        <div class="stat-icon">
          <el-icon><AlarmClock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ urgentTasks }}</div>
          <div class="stat-label">紧急任务</div>
        </div>
      </div>
    </div>

    <!-- 计划统计 -->
    <div class="plan-stats-section">
      <h3 class="stats-title">今日计划统计</h3>
      <div class="plan-stats-grid">
        <div class="plan-stat-item">
          <div class="plan-stat-value">{{ planCount }}</div>
          <div class="plan-stat-label">计划数量</div>
        </div>
        <div class="plan-stat-item">
          <div class="plan-stat-value">{{ planDuration }}分钟</div>
          <div class="plan-stat-label">总时长</div>
        </div>
        <div class="plan-stat-item">
          <div class="plan-stat-value">{{ avgPlanDuration }}分钟</div>
          <div class="plan-stat-label">平均时长</div>
        </div>
      </div>
    </div>

    <!-- 进度条 -->
    <div class="progress-section">
      <div class="progress-header">
        <span class="progress-label">任务完成率</span>
        <span class="progress-percent">{{ completionRate }}%</span>
      </div>
      <el-progress 
        :percentage="completionRate" 
        :color="progressColor"
        :stroke-width="8"
        :show-text="false"
      />
    </div>

    <!-- 类型分布 -->
    <div class="type-distribution">
      <h3 class="stats-title">任务类型分布</h3>
      <div class="type-list">
        <div 
          v-for="type in typeStats" 
          :key="type.name"
          class="type-item"
        >
          <div class="type-info">
            <span class="type-name">{{ type.name }}</span>
            <span class="type-count">{{ type.count }}</span>
          </div>
          <el-progress 
            :percentage="type.percentage" 
            :stroke-width="6"
            :show-text="false"
            :color="getTypeColor(type.name)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineProps } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  tasks: {
    type: Array,
    default: () => []
  },
  plans: {
    type: Array,
    default: () => []
  }
})

// 任务相关统计
const totalTasks = computed(() => props.tasks.length)

const doingTasks = computed(() => {
  return props.tasks.filter(task => task.status === 'doing').length
})

const doneTasks = computed(() => {
  return props.tasks.filter(task => task.status === 'done').length
})

const urgentTasks = computed(() => {
  const today = dayjs()
  return props.tasks.filter(task => {
    return task.priority === 1 && 
           task.status !== 'done' &&
           task.deadline && 
           dayjs(task.deadline).diff(today, 'hour') <= 24
  }).length
})

// 计划相关统计
const planCount = computed(() => props.plans.length)

const planDuration = computed(() => {
  return props.plans.reduce((total, plan) => {
    if (!plan.time_start || !plan.time_end) return total
    const start = dayjs(`2000-01-01 ${plan.time_start}`)
    const end = dayjs(`2000-01-01 ${plan.time_end}`)
    return total + end.diff(start, 'minute')
  }, 0)
})

const avgPlanDuration = computed(() => {
  if (planCount.value === 0) return 0
  return Math.round(planDuration.value / planCount.value)
})

// 完成率
const completionRate = computed(() => {
  if (totalTasks.value === 0) return 0
  return Math.round((doneTasks.value / totalTasks.value) * 100)
})

// 进度条颜色
const progressColor = computed(() => {
  if (completionRate.value < 30) return '#f56c6c'
  if (completionRate.value < 70) return '#e6a23c'
  return '#67c23a'
})

// 类型分布
const typeStats = computed(() => {
  const typeMap = {}
  
  props.tasks.forEach(task => {
    const type = task.type || '其他'
    typeMap[type] = (typeMap[type] || 0) + 1
  })
  
  return Object.entries(typeMap).map(([name, count]) => ({
    name,
    count,
    percentage: Math.round((count / totalTasks.value) * 100)
  })).sort((a, b) => b.count - a.count)
})

// 类型颜色
const getTypeColor = (type) => {
  const colors = {
    '作业': '#409EFF',
    '复习': '#67C23A',
    '考试': '#E6A23C',
    '生活': '#F56C6C',
    '其他': '#909399'
  }
  return colors[type] || '#909399'
}
</script>

<style scoped>
.task-stats {
  padding: 15px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.stat-card.total {
  background: #f0f9ff;
  border-color: #409eff;
}

.stat-card.doing {
  background: #fff7e6;
  border-color: #e6a23c;
}

.stat-card.done {
  background: #f6ffed;
  border-color: #67c23a;
}

.stat-card.urgent {
  background: #fff2f0;
  border-color: #f56c6c;
}

.stat-icon {
  margin-right: 12px;
  font-size: 20px;
}

.stat-icon.total .el-icon {
  color: #409eff;
}

.stat-icon.doing .el-icon {
  color: #e6a23c;
}

.stat-icon.done .el-icon {
  color: #67c23a;
}

.stat-icon.urgent .el-icon {
  color: #f56c6c;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

/* 计划统计 */
.stats-title {
  font-size: 14px;
  color: #333;
  margin: 0 0 12px 0;
  font-weight: 500;
}

.plan-stats-section {
  margin-bottom: 20px;
}

.plan-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.plan-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
  border-radius: 6px;
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
}

.plan-stat-value {
  font-weight: 600;
  color: #409eff;
  font-size: 16px;
  margin-bottom: 4px;
}

.plan-stat-label {
  font-size: 12px;
  color: #666;
}

/* 进度条 */
.progress-section {
  margin-bottom: 20px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.progress-percent {
  font-weight: 600;
  color: #409eff;
  font-size: 16px;
}

/* 类型分布 */
.type-distribution {
  margin-bottom: 10px;
}

.type-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.type-item {
  cursor: pointer;
}

.type-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
}

.type-name {
  color: #333;
}

.type-count {
  color: #666;
  font-weight: 500;
}

/* 响应式设计 */
@media (min-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>