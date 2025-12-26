<template>
  <div class="dashboard">
    <div class="welcome-card card-wrapper">
      <div class="welcome-left">
        <p class="welcome-subtitle">你好，亲爱的</p>
        <h2 class="welcome-title">{{ displayName }}同学</h2>
        <p class="welcome-desc">今天也要保持热情，完成学习目标！</p>
      </div>
      <div class="welcome-right">
        <div v-if="goalAlerts.length" class="goal-alerts">
          <div
            v-for="(alert, idx) in goalAlerts"
            :key="idx"
            class="goal-alert-item"
          >
            <div class="alert-title">{{ alert.title }}</div>
            <div class="alert-desc">{{ alert.description }}</div>
          </div>
        </div>
        <div v-else class="goal-alert-empty">
          <el-icon><Check /></el-icon>
          <span>目标都已完成，继续保持！</span>
        </div>
      </div>
    </div>

    <!-- 主要功能区（两列布局：左主区，右番茄钟） -->
    <div class="main-area">
      <!-- 左主列：上排三张卡片（快速操作 / 即将截止 / GPA趋势），中间学习统计，底部计划+待办 -->
      <div class="left-column">
        <div class="top-row">
          <div class="quick-actions card-wrapper">
            <div class="section-title">
              <el-icon><Lightning /></el-icon>
              <h3>快速操作</h3>
            </div>
            
              <div class="action-buttons">
                <div class="action-item">
                  <el-button type="primary" class="action-btn" @click="showTaskForm('create')">
                    <el-icon><Plus /></el-icon>
                    <span class="action-text">新建任务</span>
                  </el-button>
                </div>

                <div class="action-item">
                  <el-button type="primary" class="action-btn" @click="showPlanForm('create')">
                    <el-icon><Clock /></el-icon>
                    <span class="action-text">添加计划</span>
                  </el-button>
                </div>

                <div class="action-item">
                  <el-button type="primary" class="action-btn" @click="goToAIAssistant">
                    <el-icon><ChatDotRound /></el-icon>
                    <span class="action-text">AI助手</span>
                  </el-button>
                </div>

                <div class="action-item">
                  <router-link to="/settings" class="action-link">
                    <el-button type="primary" class="action-btn">
                      <el-icon><Setting /></el-icon>
                      <span class="action-text">设置</span>
                    </el-button>
                  </router-link>
                </div>
              </div>
          </div>

          <div class="upcoming-deadlines card-wrapper">
            <div class="section-title">
              <el-icon><Clock /></el-icon>
              <h3>即将截止</h3>
            </div>
            <div class="deadline-list">
              <div 
                v-for="deadline in urgentDeadlines" 
                :key="deadline.id"
                class="deadline-item"
                @click="handleDeadlineClick(deadline)"
              >
                <div class="deadline-title">{{ deadline.title }}</div>
                <div class="deadline-time">{{ deadline.time }}</div>
              </div>
              <div v-if="urgentDeadlines.length === 0" class="empty-deadline">
                <span class="no-deadline">暂无即将截止的任务</span>
              </div>
            </div>
          </div>

          <!-- GPA趋势已移除到右侧番茄钟下方 -->
        </div>

        <div class="chart-row">
          <StudyChart :tasks="tasks" />
        </div>

        <!-- bottom-row moved below to span full width -->
        
      </div>

      <!-- 右列：番茄钟 -->
      <div class="right-column">
        <PomodoroTimer />
        <div class="gpa-trend card-wrapper">
          <div class="section-title">
            <el-icon><TrendCharts /></el-icon>
            <h3>GPA趋势</h3>
          </div>
          <div v-if="gpaTrendDisplay.length" class="trend-chart">
            <div class="gpa-summary">
              <div class="summary-item">
                <span class="summary-label">最高</span>
                <span class="summary-value">{{ formatGpaValue(gpaSummary.max) }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">平均</span>
                <span class="summary-value">{{ formatGpaValue(gpaSummary.avg) }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">趋势</span>
                <span class="summary-value" :class="gpaSummary.trend">{{ gpaTrendText }}</span>
              </div>
            </div>
            <div class="trend-bars">
              <div 
                v-for="(item, index) in gpaTrendDisplay" 
                :key="item.semester || index" 
                class="bar-item"
              >
                <span class="bar-value">{{ formatGpaValue(item.gpa) }}</span>
                <div class="bar-track">
                  <div 
                    class="bar-fill" 
                    :class="{ active: index === gpaTrendDisplay.length - 1 }"
                    :style="{ height: getBarHeight(item.gpa) }"
                  />
                </div>
              </div>
            </div>
            <div class="trend-labels">
              <span 
                v-for="(item, index) in gpaTrendDisplay" 
                :key="item.semester || `label-${index}`"
                :class="{ active: index === gpaTrendDisplay.length - 1 }"
              >
                {{ formatSemesterLabel(item.semester) }}
              </span>
            </div>
          </div>
          <div v-else class="empty-gpa">
            <el-empty :image-size="60" description="暂无 GPA 数据" />
          </div>
        </div>
      </div>
    </div>

    <!-- 将计划和待办移到页面底部，横跨整页宽度 -->
    <div class="bottom-row full-width">
      <div class="plan-card">
        <div class="card-header">
          <div class="card-title">
            <el-icon><Calendar /></el-icon>
            <h3>今日学习计划</h3>
          </div>
          <div class="plan-actions">
            <el-button type="text" size="small" @click="showPlanForm('create')">
              添加计划
            </el-button>
            <router-link to="/study-center">
              <el-button type="text" size="small">查看全部</el-button>
            </router-link>
          </div>
        </div>
        <div class="plan-list">
          <div v-if="dashboardPlans.length === 0" class="empty-plan">
            <el-empty description="今日暂无计划" :image-size="60">
              <el-button size="small" @click="showPlanForm('create')">添加计划</el-button>
            </el-empty>
          </div>
          
          <div v-else>
            <div 
              v-for="plan in dashboardPlans" 
              :key="plan.id" 
              class="plan-item"
              :class="{ 'active': plan.status === '进行中' }"
            >
              <div class="plan-time">
                <div class="time-circle">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="time-range">{{ plan.displayTime }}</div>
              </div>
              <div class="plan-content">
                <div class="plan-title">{{ plan.displayTitle }}</div>
                <div class="plan-course">{{ plan.displayCourse }}</div>
              </div>
              <div class="plan-status">
                <el-tag 
                  :type="plan.displayStatus === '已完成' ? 'success' : plan.displayStatus === '进行中' ? 'warning' : 'info'" 
                  size="small"
                  :style="{ color: '#000000' }"
                >
                  {{ plan.displayStatus }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="todo-card">
        <div class="card-header">
          <div class="card-title">
            <el-icon><List /></el-icon>
            <h3>待办任务</h3>
          </div>
          <div class="todo-header-actions">
            <el-button type="text" size="small" @click="showTaskForm('create')">
              新建任务
            </el-button>
            <router-link to="/study-center">
              <el-button type="text" size="small">管理</el-button>
            </router-link>
          </div>
        </div>
        <div class="todo-list">
          <div v-if="dashboardTasks.length === 0" class="empty-todo">
            <el-empty description="暂无任务" :image-size="60">
              <el-button size="small" @click="showTaskForm('create')">创建任务</el-button>
            </el-empty>
          </div>
          
          <div v-else>
            <div 
              v-for="item in dashboardTasks" 
              :key="item.id" 
              class="todo-item"
              :class="{ 'high-priority': item.priority === 'high', 'completed': item.completed }"
            >
              <el-checkbox 
                v-model="item.completed" 
                @change="updateTaskStatus(item)"
                class="todo-checkbox"
              />
              <div class="todo-content">
                <div class="todo-header">
                  <span class="todo-title">{{ item.title }}</span>
                  <el-tag 
                    v-if="item.priority === 'high'" 
                    type="danger" 
                    size="small"
                  >
                    高
                  </el-tag>
                  <el-tag 
                    v-else-if="item.priority === 'medium'" 
                    type="warning" 
                    size="small"
                  >
                    中
                  </el-tag>
                </div>
                <div class="todo-info">
                  <span class="todo-course">{{ item.course }}</span>
                  <span class="todo-deadline">{{ item.deadline }}</span>
                </div>
              </div>
              <div class="todo-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  @click="editTask(item)"
                  :icon="Edit"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 使用学习中心的TaskForm组件 -->
    <TaskForm
      v-if="showTaskDialog"
      v-model="showTaskDialog"
      :mode="taskFormMode"
      :task="currentTask"
      @submit="handleTaskSubmit"
      @close="showTaskDialog = false"
    />

    <!-- 使用学习中心的PlanForm组件 -->
    <PlanForm
      v-if="showPlanDialog"
      v-model="showPlanDialog"
      :mode="planFormMode"
      :plan="currentPlan"
      :tasks="dashboardTasks"
      @submit="handlePlanSubmit"
      @close="showPlanDialog = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { 
  Calendar, 
  Clock, 
  List,
  Lightning,
  Plus,
  Setting,
  Edit,
  ChatDotRound,
  Check
} from '@element-plus/icons-vue'
import PomodoroTimer from '../components/timer/PomodoroTimer.vue'
import StudyChart from '../components/charts/StudyChart.vue'
import TaskForm from '@/components/task/TaskForm.vue'
import PlanForm from '@/components/plan/PlanForm.vue'
import { listTasks, updateTaskStatus as apiUpdateTaskStatus, createTask, updateTask, summaryTasks } from '@/api/tasks'
import { listPlans, createPlan, updatePlan } from '@/api/plans'
import { overviewStats as apiOverviewStats } from '@/api/study'
import { statsGrades } from '@/api/grades'
import { useMainStore } from '@/stores'

const router = useRouter()
const store = useMainStore()

const showTaskDialog = ref(false)
const showPlanDialog = ref(false)
const taskFormMode = ref('create')
const planFormMode = ref('create')
const currentTask = ref(null)
const currentPlan = ref(null)

const tasks = ref([])
const plans = ref([])
const todayStudyTime = ref(0)
const gpaStats = ref({ trend: [] })
const weeklyStudyMinutes = ref(0)

const GPA_FULL_SCORE = 5
const studySettings = computed(() => store.studySettings || {})
const reminderThresholdHours = computed(() => {
  const map = { none: 0, '1h': 1, '3h': 3, '1d': 24 }
  const value = studySettings.value.taskReminder
  if (typeof value === 'number') return value
  return map[value] ?? 24
})

// welcome card removed — unused computed values omitted

const dashboardTasks = computed(() => {
  const priorityOrder = { 1: 1, 2: 2, 3: 3 }
  return [...tasks.value]
    .sort((a, b) => {
      const aDeadline = a.deadline ? dayjs(a.deadline).valueOf() : Number.MAX_SAFE_INTEGER
      const bDeadline = b.deadline ? dayjs(b.deadline).valueOf() : Number.MAX_SAFE_INTEGER
      return aDeadline - bDeadline
    })
    .sort((a, b) => (priorityOrder[a.priority] || 3) - (priorityOrder[b.priority] || 3))
    .sort((a, b) => Number(a.completed) - Number(b.completed))
})

const PLAN_STATUS = {
  notStarted: '未开始',
  inProgress: '进行中',
  finished: '已结束'
}

const resolvePlanStatus = (plan) => {
  const start = plan.time_start
  const end = plan.time_end
  const fallback = plan.status || PLAN_STATUS.notStarted
  if (!start || !end) return fallback
  const planDate = plan.date ? dayjs(plan.date).format('YYYY-MM-DD') : dayjs().format('YYYY-MM-DD')
  const startTime = dayjs(`${planDate} ${start}`)
  const endTime = dayjs(`${planDate} ${end}`)
  if (!startTime.isValid() || !endTime.isValid()) return fallback
  const now = dayjs()
  if (now.isBefore(startTime)) return PLAN_STATUS.notStarted
  if (now.isAfter(endTime)) return PLAN_STATUS.finished
  return PLAN_STATUS.inProgress
}

const dashboardPlans = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return plans.value
    .filter(plan => plan.date && dayjs(plan.date).format('YYYY-MM-DD') === today)
    .sort((a, b) => (a.time_start || '').localeCompare(b.time_start || ''))
    .slice(0, 4)
    .map(plan => ({
      ...plan,
      displayTime: plan.time_start && plan.time_end
        ? `${plan.time_start} - ${plan.time_end}`
        : (plan.time || ''),
      displayTitle: plan.task_title || plan.title || '自由学习',
      displayCourse: plan.course || plan.description || '',
      displayStatus: resolvePlanStatus(plan)
    }))
})

const urgentDeadlines = computed(() => {
  const now = dayjs()
  const threshold = reminderThresholdHours.value
  if (threshold <= 0) return []
  return tasks.value
    .filter(task => {
      if (!task.deadline || task.completed) return false
      const deadline = dayjs(task.deadline)
      const diffHours = deadline.diff(now, 'hour')
      return diffHours > 0 && diffHours <= threshold
    })
    .map(task => ({
      id: task.id,
      title: task.title,
      time: formatTimeRemaining(task.deadline)
    }))
    .slice(0, 3)
})

const gpaTrendDisplay = computed(() => {
  if (!gpaStats.value || !Array.isArray(gpaStats.value.trend)) {
    return []
  }
  return [...gpaStats.value.trend]
    .map(item => ({
      semester: item.semester || '',
      gpa: Number(item.gpa) || 0
    }))
    .sort((a, b) => a.semester.localeCompare(b.semester))
    .slice(-5)
})

const gpaSummary = computed(() => {
  const values = gpaTrendDisplay.value.map(item => item.gpa)
  if (values.length === 0) {
    return { max: 0, avg: 0, trend: 'stable' }
  }
  const max = Math.max(...values)
  const avg = values.reduce((sum, val) => sum + val, 0) / values.length
  let trend = 'stable'
  if (values.length > 1) {
    const last = values[values.length - 1]
    const prev = values[values.length - 2]
    if (last > prev + 0.001) trend = 'up'
    else if (last < prev - 0.001) trend = 'down'
  }
  return { max, avg, trend }
})

const gpaTrendText = computed(() => {
  switch (gpaSummary.value.trend) {
    case 'up':
      return '上升'
    case 'down':
      return '下降'
    default:
      return '持平'
  }
})

const formatGpaValue = (value) => (Number(value) || 0).toFixed(2)

const formatSemesterLabel = (semester = '') => {
  if (!semester) return '—'
  const match = semester.match(/(\d{4})-(\d{4})-(\d)/)
  if (match) {
    return `${match[1].slice(-2)}-${match[2].slice(-2)}-${match[3]}`
  }
  return semester
}

const getBarHeight = (value) => {
  const numeric = Number(value) || 0
  const clamped = Math.min(Math.max(numeric, 0), GPA_FULL_SCORE)
  return `${(clamped / GPA_FULL_SCORE) * 100}%`
}

const formatHours = (minutes) => Number((minutes / 60).toFixed(1))

const goalAlerts = computed(() => {
  const alerts = []
  const dailyGoal = Number(studySettings.value.dailyGoal || 0)
  const weeklyGoal = Number(studySettings.value.weeklyGoal || 0)
  if (dailyGoal > 0) {
    const target = dailyGoal * 60
    if (todayStudyTime.value < target) {
      alerts.push({
        title: '今日学习目标：',
        description: `已学习 ${formatHours(todayStudyTime.value)} 小时 / 目标 ${dailyGoal} 小时`
      })
    }
  }
  if (weeklyGoal > 0) {
    const target = weeklyGoal * 60
    if (weeklyStudyMinutes.value < target) {
      alerts.push({
        title: '本周学习目标：',
        description: `已累计 ${formatHours(weeklyStudyMinutes.value)} 小时 / 目标 ${weeklyGoal} 小时`
      })
    }
  }
  return alerts
})

const displayName = computed(() => store.userInfo.nickname || store.userInfo.username || '同学')

// completed/total/completionRate removed because not used in the current layout

const formatTimeRemaining = (deadline) => {
  if (!deadline) return ''
  const deadlineTime = dayjs(deadline)
  const now = dayjs()
  if (deadlineTime.isBefore(now)) return '已过期'
  const diffHours = deadlineTime.diff(now, 'hour')
  const diffMinutes = deadlineTime.diff(now, 'minute')
  if (diffHours < 1) return `${diffMinutes}分钟后`
  if (diffHours < 24) return `${diffHours}小时后`
  return `${Math.floor(diffHours / 24)}天后`
}

const showTaskForm = (mode, task = null) => {
  taskFormMode.value = mode
  currentTask.value = task
  showTaskDialog.value = true
}

const editTask = (task) => showTaskForm('edit', task)

const updateTaskStatus = async (item) => {
  try {
    await apiUpdateTaskStatus(item.id, { status: item.completed ? 'done' : 'todo', completed: item.completed })
    await Promise.all([loadTasks(), loadSummary(), loadOverviewStats()])
    ElMessage.success('任务状态已更新')
  } catch (e) {
    ElMessage.error(e.message || '更新任务状态失败')
  }
}

const handleTaskSubmit = async (taskData) => {
  try {
    if (taskFormMode.value === 'create') {
      await createTask({ ...taskData, completed: false, status: 'todo' })
      ElMessage.success('任务创建成功')
    } else if (currentTask.value) {
      await updateTask(currentTask.value.id, taskData)
      ElMessage.success('任务更新成功')
    }
    showTaskDialog.value = false
    await Promise.all([loadTasks(), loadSummary(), loadOverviewStats()])
  } catch (e) {
    ElMessage.error(e.message || '提交任务失败')
  }
}

const showPlanForm = (mode, plan = null) => {
  planFormMode.value = mode
  currentPlan.value = plan
  showPlanDialog.value = true
}

const handlePlanSubmit = async (planData) => {
  try {
    const payload = { ...planData, date: dayjs().format('YYYY-MM-DD') }
    if (planFormMode.value === 'create') {
      await createPlan(payload)
      ElMessage.success('计划添加成功')
    } else if (currentPlan.value) {
      await updatePlan(currentPlan.value.id, payload)
      ElMessage.success('计划更新成功')
    }
    showPlanDialog.value = false
    await loadPlans()
  } catch (e) {
    ElMessage.error(e.message || '提交计划失败')
  }
}

const handleDeadlineClick = (deadline) => {
  const task = tasks.value.find(t => t.id === deadline.id)
  if (task) showTaskForm('edit', task)
}

const goToAIAssistant = () => {
  router.push('/study-center')
}

const loadTasks = async () => {
  const resp = await listTasks({ page: 1, pageSize: 100 })
  tasks.value = resp.data.data?.list || []
}

const loadPlans = async () => {
  const resp = await listPlans({ date: dayjs().format('YYYY-MM-DD') })
  plans.value = resp.data.data || []
}

const loadSummary = async () => {
  const resp = await summaryTasks()
  if (resp.data?.data) {
    // 直接复用任务列表，避免重复计算
  }
}

const loadOverviewStats = async () => {
  try {
    const resp = await apiOverviewStats()
    const data = resp.data?.data || {}
    todayStudyTime.value = data.today_minutes || 0
    weeklyStudyMinutes.value = data.week_minutes || 0
  } catch (e) {
    todayStudyTime.value = 0
    weeklyStudyMinutes.value = 0
  }
}

const loadGpaStats = async () => {
  try {
    const resp = await statsGrades()
    gpaStats.value = resp?.data?.data || { trend: [] }
  } catch (e) {
    gpaStats.value = { trend: [] }
  }
}

const handleSessionRecorded = () => {
  loadOverviewStats()
}

onMounted(async () => {
  await Promise.all([
    loadTasks(),
    loadPlans(),
    loadOverviewStats(),
    loadSummary(),
    loadGpaStats()
  ])
  window.addEventListener('study-session-recorded', handleSessionRecorded)
})

onUnmounted(() => {
  window.removeEventListener('study-session-recorded', handleSessionRecorded)
})
</script>

<style scoped>
/* 固定宽度布局（响应式） */
.dashboard {
  width: 1100px;
  margin: 0 auto;
}

/* 欢迎卡片（占满整行） */

/* 主区域布局 - 三栏固定宽度 */
.main-area {
  display: grid;
  grid-template-columns: 1fr 260px; /* 左侧主区 + 右侧番茄钟（减小番茄钟宽度） */
  gap: 20px;
}

.left-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-card {
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  padding: 24px;
  width: 100%;
  box-sizing: border-box;
  margin-bottom: 20px;
  background: var(--primary); /* fallback */
  background: linear-gradient(
    120deg,
    color-mix(in srgb, var(--primary) 75%, #ffffff),
    color-mix(in srgb, var(--primary) 55%, #ffffff)
  );
  color: #fff;
  border-radius: 12px;
  box-shadow: var(--soft-shadow, 0 10px 30px rgba(43,50,80,0.08));
  overflow: hidden;
}

.welcome-left {
  flex: 1;
}

.welcome-subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.welcome-title {
  margin: 6px 0 4px;
  font-size: 26px;
  font-weight: 700;
}

.welcome-desc {
  margin: 0;
  opacity: 0.9;
}

.welcome-right {
  width: 55%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.goal-alerts {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.goal-alert-item {
  background: rgba(255, 255, 255, 0.22);
  border-radius: 10px;
  padding: 12px 16px;
  color: #fff;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.goal-alert-item .alert-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.goal-alert-item .alert-desc {
  font-size: 13px;
  opacity: 0.9;
}

.goal-alert-empty {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
}

.right-column {
  width: 260px; /* 番茄钟宽度减小 */
  display: flex;
  flex-direction: column;
  gap: 20px;
  justify-content: flex-end;
  padding-bottom: 20px;
}

.top-row {
  display: flex;
  gap: 20px;
  align-items: stretch; /* 统一高度，卡片等高 */
}

.top-row .card-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 增大快速操作宽度，其他两个卡片保持默认 */
.top-row .quick-actions {
  flex: 0.7; /* 变窄 */
}

.top-row .upcoming-deadlines {
  flex: 1.6; /* 变宽 */
}

.top-row .gpa-trend {
  flex: 1; /* 保持原来或默认比例 */
}

.chart-row {
  /* 学习统计占满左列宽度 */
  display: block;
}

.bottom-row {
  display: flex;
  gap: 20px;
}

.bottom-row .plan-card,
.bottom-row .todo-card {
  flex: 1;
}

/* 当底部横跨整页时的样式 */
.bottom-row.full-width {
  margin-top: 20px;
  display: flex;
  gap: 20px;
}

.bottom-row.full-width .plan-card,
.bottom-row.full-width .todo-card {
  flex: 1;
}

/* 卡片通用样式 */
.pomodoro-card,
.plan-card,
.chart-card,
.todo-card,
.quick-actions,
.upcoming-deadlines,
.gpa-trend {
  background: white;
  border-radius: 8px;
  border: 1px solid rgba(0,0,0,0.04);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 番茄钟外层卡片样式已移除，直接使用组件自身样式 */

/* 计划卡片 */
.plan-card {
  padding: 20px;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  background: #E0E1F5; /* 今日学习任务卡片背景 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--nav-active);
}

.card-title .el-icon {
  color: var(--primary);
  font-size: 18px;
}

.plan-actions {
  display: flex;
  gap: 8px;
}

/* 将“添加计划”按钮配色与 plan-card 协调 */
.plan-card .plan-actions .el-button {
  background: #D6D9F6 !important;
  color: #2F2F7A !important;
  border-radius: 6px;
  padding: 6px 10px;
  border: none !important;
}
.plan-card .plan-actions .el-button:hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

/* 空状态（今日暂无计划）中的“添加计划”按钮颜色 */
.plan-card .plan-list .empty-plan :deep(.el-button) {
  background: #D6D9F6 !important;
  color: #2F2F7A !important;
  border-radius: 6px;
  padding: 6px 10px;
  border: none !important;
}
.plan-card .plan-list .empty-plan :deep(.el-button):hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

/* 空状态（暂无任务）中的“创建任务”按钮颜色 */
.todo-card .todo-list .empty-todo :deep(.el-button) {
  background: #F8E2C9 !important;
  color: #A65A00 !important;
  border-radius: 6px;
  padding: 6px 10px;
  border: none !important;
}
.todo-card .todo-list .empty-todo :deep(.el-button):hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

.plan-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  overflow-x: hidden;
  max-height: 220px;
}

.plan-list .empty-plan {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.plan-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  background: #D7D9F3;
  border-left: 3px solid rgba(0,0,0,0.04);
  transition: all 0.2s;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
}

.plan-item:hover {
  background: #D7D9F3;
  transform: translateY(-1px);
}

.plan-item.active {
  background: linear-gradient(180deg, var(--soft-peach), var(--soft-peach-600));
  border-left-color: var(--accent);
}

.plan-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 70px;
  flex-shrink: 0;
}

.time-circle {
  width: 28px;
  height: 28px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
  color: var(--primary);
}

.time-range {
  font-size: 12px;
  color: var(--muted);
  font-weight: 500;
  white-space: nowrap;
}

.plan-content {
  flex: 1;
  margin: 0 12px;
  min-width: 0;
}

.plan-title {
  font-weight: 500;
  color: var(--nav-active);
  margin-bottom: 2px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plan-course {
  font-size: 12px;
  color: var(--muted);
}

.plan-status {
  flex-shrink: 0;
}

.plan-status :deep(.el-tag),
.plan-status :deep(.el-tag__content) {
  color: #000 !important;
}

/* 统计卡片样式已移除（使用统一卡片样式） */

/* 图表卡片 样式已移除，StudyChart 不再被外层卡片包裹 */

/* 待办卡片 */
.todo-card {
  padding: 20px;
  min-height: 320px;
  display: flex;
  flex-direction: column;
  background: #F7EDE1; /* 待办任务卡片背景 */
}

.todo-header-actions {
  display: flex;
  gap: 8px;
}

/* 将“新建任务/创建任务”按钮配色与 todo-card 协调 */
.todo-card .todo-header-actions .el-button {
  background: #F8E2C9 !important;
  color: #A65A00 !important;
  border-radius: 6px;
  padding: 6px 10px;
  border: none !important;
}
.todo-card .todo-header-actions .el-button:hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

.todo-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  overflow-x: hidden;
  max-height: 240px;
}

.todo-list .empty-todo {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  background: #F4E3CC;
  border: 1px solid rgba(0,0,0,0.04);
  transition: all 0.2s;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
}

.todo-item:hover {
  background: #F4E3CC;
  transform: translateY(-1px);
}

.todo-item.high-priority {
  border-left: 3px solid var(--accent);
  background: linear-gradient(180deg, var(--soft-peach), rgba(252,240,228,0.6));
}

.todo-item.completed {
  opacity: 0.7;
  background: #F4E3CC;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
  color: var(--muted);
}

.todo-checkbox {
  margin-right: 12px;
  flex-shrink: 0;
}

.todo-content {
  flex: 1;
  min-width: 0;
  margin-right: 12px;
}

.todo-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.todo-title {
  font-weight: 500;
  color: var(--nav-active);
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--muted);
}

.todo-deadline {
  color: #333333;
  font-weight: 500;
}

.todo-actions {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.todo-item:hover .todo-actions {
  opacity: 1;
}

/* 右侧区域 */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--nav-active);
}

.section-title .el-icon {
  color: var(--primary);
  font-size: 18px;
}

.quick-actions,
.upcoming-deadlines,
.gpa-trend {
  padding: 12px; /* 保持内边距 */
  min-height: 220px; /* 增加卡片高度以放大内容（GPA趋势） */
}

/* 将“即将截止”卡片高度固定，并允许内容滚动 */
.upcoming-deadlines {
  /* 全家色系（FamilyMart 风格）局部变量 */
  --fam-primary: #00A859; /* 主绿 */
  --fam-accent: #0073A8;  /* 副蓝/强调 */
  --fam-bg-start: #EFF7E0;
  --fam-bg-end: #DFF8E8;
  box-sizing: border-box;
  /* 使用单一主色作为卡片底色，顶部使用用户指定颜色 */
  background: linear-gradient(180deg, var(--fam-bg-start), var(--fam-bg-end));
  border: 1px solid rgba(0,0,0,0.04);
  overflow-x: hidden; /* 禁止水平滚动/位移，允许垂直滚动 */
}

/* 将全家色系应用到卡片内元素 */
.upcoming-deadlines .section-title .el-icon {
  color: var(--fam-primary) !important;
}
.upcoming-deadlines .deadline-item {
  background: linear-gradient(90deg, rgba(255,255,255,0.95), rgba(255,255,255,0.98));
  border-left: 4px solid var(--fam-primary);
}
.upcoming-deadlines .deadline-item {
  width: 100%;
  box-sizing: border-box;
}

.upcoming-deadlines .deadline-item:hover {
  transform: translateY(-4px); /* 仅做垂直位移，避免左右移动 */
  box-shadow: 0 6px 18px rgba(0,168,89,0.08);
}
.upcoming-deadlines .deadline-time {
  color: var(--fam-accent);
  font-weight: 600;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: stretch; /* 让按钮宽度填充容器，便于对齐 */
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
  height: 36px; /* 略微减小按钮高度以配合卡片高度 */
  font-size: 14px;
  padding-left: 12px; /* 统一左侧内边距，保证图标与文字对齐 */
  display: flex;
  align-items: center;
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
  height: 36px;
  font-size: 14px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

/* Anchor wrapping buttons (router-link) should not show underline and should be block-level */
.quick-actions a,
.action-btn a {
  text-decoration: none;
  color: inherit;
  display: block;
}

/* Ensure Element Plus button inner content aligns (support different versions) */
.action-btn .el-button__content,
.action-btn .el-button__inner,
.action-btn .el-button__text {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  justify-content: flex-start;
}

/* Icon container sizing to avoid misalignment */
.action-btn .el-icon,
.action-btn .el-button__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
}

/* New: ensure each action-item uses full-width and buttons align */
.action-item {
  width: 100%;
}

.action-item .el-button {
  width: 100%;
  justify-content: flex-start;
}

.action-link {
  display: block;
  width: 100%;
}

.action-text {
  display: inline-block;
}

/* 交替按钮颜色：奇数为深橙，偶数为浅橙 */
.action-buttons .action-item:nth-child(odd) .action-btn {
  background-color: #FB8C4A !important;
  color: #ffffff !important;
  border: none !important;
}
.action-buttons .action-item:nth-child(odd) .action-btn .el-icon,
.action-buttons .action-item:nth-child(odd) .action-btn .el-button__icon {
  color: #ffffff !important;
}
.action-buttons .action-item:nth-child(even) .action-btn {
  background-color: #F8EDE1 !important;
  color: #FB8C4A !important;
  border: none !important;
}
.action-buttons .action-item:nth-child(even) .action-btn .el-icon,
.action-buttons .action-item:nth-child(even) .action-btn .el-button__icon {
  color: #FB8C4A !important;
}
.action-buttons .action-item .action-btn:hover {
  opacity: 0.94;
  transform: translateY(-1px);
}

.deadline-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  max-height: calc(100% - 56px); /* 留出标题/内边距空间，使列表在卡内滚动 */
}

.deadline-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: var(--page-bg);
  border-radius: 6px;
  transition: all 0.2s;
  cursor: pointer;
}

.deadline-item:hover {
  background: var(--page-bg);
  transform: translateX(2px);
}

.deadline-title {
  font-size: 14px;
  color: var(--nav-active);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.deadline-time {
  font-size: 12px;
  color: var(--accent);
  font-weight: 500;
  margin-left: 10px;
  flex-shrink: 0;
}

.empty-deadline {
  padding: 15px;
  text-align: center;
  color: var(--muted);
  font-size: 14px;
}

.trend-chart {
  padding: 15px;
  background: var(--page-bg);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.gpa-summary {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.summary-label {
  font-size: 12px;
  color: var(--muted);
}

.summary-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--nav-active);
}

.summary-value.up {
  color: #52c41a;
}

.summary-value.down {
  color: #f5222d;
}

.trend-bars {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 140px;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.bar-value {
  font-size: 12px;
  font-weight: 600;
  color: var(--nav-active);
}

.bar-track {
  width: 18px;
  height: 110px;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 999px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  border-radius: 999px;
  background: var(--primary);
  transition: height 0.3s ease;
}

.bar-fill.active {
  background: var(--accent);
}

.trend-labels {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: var(--muted);
}

.trend-labels span {
  flex: 1;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.trend-labels span.active {
  color: var(--accent);
  font-weight: 600;
}

.empty-gpa {
  padding: 10px 0;
}

/* 滚动条样式 */
.plan-list::-webkit-scrollbar,
.todo-list::-webkit-scrollbar {
  width: 4px;
}

.plan-list::-webkit-scrollbar-track,
.todo-list::-webkit-scrollbar-track {
  background: rgba(0,0,0,0.04);
  border-radius: 2px;
}

.plan-list::-webkit-scrollbar-thumb,
.todo-list::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.12);
  border-radius: 2px;
}

.plan-list::-webkit-scrollbar-thumb:hover,
.todo-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0,0,0,0.18);
}
</style>
