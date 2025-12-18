<template>
  <div class="dashboard">
    
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
                      <span class="action-text">学习设置</span>
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
          <StudyChart />
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
          <div class="trend-chart">
            <div class="trend-placeholder">
              <div class="trend-bar" style="height: 80%"></div>
              <div class="trend-bar" style="height: 85%"></div>
              <div class="trend-bar" style="height: 90%"></div>
              <div class="trend-bar" style="height: 88%"></div>
              <div class="trend-bar active" style="height: 92%"></div>
            </div>
            <div class="trend-labels">
              <span>大一上</span>
              <span>大一下</span>
              <span>大二上</span>
              <span>大二下</span>
              <span class="active">大三上</span>
            </div>
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
import { ref, computed, onMounted } from 'vue'
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
  ChatDotRound
} from '@element-plus/icons-vue'
import PomodoroTimer from '../components/timer/PomodoroTimer.vue'
import StudyChart from '../components/charts/StudyChart.vue'
import TaskForm from '@/components/task/TaskForm.vue'
import PlanForm from '@/components/plan/PlanForm.vue'
import { listTasks, updateTaskStatus as apiUpdateTaskStatus, createTask, updateTask, summaryTasks } from '@/api/tasks'
import { listPlans, createPlan, updatePlan } from '@/api/plans'
import { todayStats as apiTodayStats } from '@/api/study'

const router = useRouter()

const showTaskDialog = ref(false)
const showPlanDialog = ref(false)
const taskFormMode = ref('create')
const planFormMode = ref('create')
const currentTask = ref(null)
const currentPlan = ref(null)

const tasks = ref([])
const plans = ref([])
const todayStudyTime = ref(0)

// welcome card removed — unused computed values omitted

const dashboardTasks = computed(() => {
  const now = dayjs()
  const today = now.format('YYYY-MM-DD')
  const todayTasks = tasks.value.filter(task => {
    if (!task.deadline) return false
    const taskDate = dayjs(task.deadline).format('YYYY-MM-DD')
    return taskDate === today
  })
  const upcomingTasks = tasks.value.filter(task => {
    if (!task.deadline) return false
    const diffHours = dayjs(task.deadline).diff(now, 'hour')
    return diffHours >= 0 && diffHours <= 24
  })
  const source = todayTasks.length > 0
    ? todayTasks
    : (upcomingTasks.length > 0 ? upcomingTasks : tasks.value)
  const priorityOrder = { 1: 1, 2: 2, 3: 3 }
  return source
    .sort((a, b) => (priorityOrder[a.priority] || 3) - (priorityOrder[b.priority] || 3))
    .sort((a, b) => Number(a.completed) - Number(b.completed))
    .slice(0, 3)
})

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
      displayStatus: plan.status || '未开始'
    }))
})

const urgentDeadlines = computed(() => {
  const now = dayjs()
  return tasks.value
    .filter(task => {
      if (!task.deadline || task.completed) return false
      const deadline = dayjs(task.deadline)
      const diffHours = deadline.diff(now, 'hour')
      return diffHours > 0 && diffHours <= 24
    })
    .map(task => ({
      id: task.id,
      title: task.title,
      time: formatTimeRemaining(task.deadline)
    }))
    .slice(0, 3)
})

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
    await Promise.all([loadTasks(), loadSummary()])
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
    await Promise.all([loadTasks(), loadSummary()])
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

const loadStudyStats = async () => {
  try {
    const resp = await apiTodayStats()
    todayStudyTime.value = resp.data?.data?.studyMinutes || 0
  } catch (e) {
    todayStudyTime.value = 0
  }
}

onMounted(async () => {
  await Promise.all([loadTasks(), loadPlans(), loadStudyStats(), loadSummary()])
})
</script>

<style scoped>
/* 固定宽度布局（响应式） */
.dashboard {
  width: 1100px;
  margin: 0 auto;
}

/* 欢迎卡片（已移除） */

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

.right-column {
  width: 260px; /* 番茄钟宽度减小 */
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  background: var(--page-bg);
  border-left: 3px solid rgba(0,0,0,0.04);
  transition: all 0.2s;
  flex-shrink: 0;
}

.plan-item:hover {
  background: var(--page-bg);
  transform: translateX(2px);
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
  background: var(--page-bg);
  border: 1px solid rgba(0,0,0,0.04);
  transition: all 0.2s;
  flex-shrink: 0;
}

.todo-item:hover {
  background: var(--page-bg);
  transform: translateX(2px);
}

.todo-item.high-priority {
  border-left: 3px solid var(--accent);
  background: linear-gradient(180deg, var(--soft-peach), rgba(252,240,228,0.6));
}

.todo-item.completed {
  opacity: 0.7;
  background: var(--page-bg);
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
  color: var(--accent);
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
}

/* 将全家色系应用到卡片内元素 */
.upcoming-deadlines .section-title .el-icon {
  color: var(--fam-primary) !important;
}
.upcoming-deadlines .deadline-item {
  background: linear-gradient(90deg, rgba(255,255,255,0.95), rgba(255,255,255,0.98));
  border-left: 4px solid var(--fam-primary);
}
.upcoming-deadlines .deadline-item:hover {
  transform: translateX(2px);
  box-shadow: 0 4px 12px rgba(0,168,89,0.08);
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
}

.trend-placeholder {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 140px; /* 放大柱状图占位高度 */
}

.trend-bar {
  width: 12px; /* Reduced width to make bars thinner */
  background-color: var(--primary);
  border-radius: 4px;
}

.trend-bar.active {
  background-color: var(--accent);
}

.trend-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: var(--muted);
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
