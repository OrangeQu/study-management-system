<template>
  <div class="dashboard">
    <!-- 头部欢迎信息 -->
    <div class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-left">
          <h1>你好，{{ username }}同学！</h1>
          <div class="date-info">
            <el-icon><Calendar /></el-icon>
            <span>{{ currentDate }}（{{ currentWeekday }}）</span>
          </div>
        </div>
        <div class="welcome-right">
          <div class="study-stats">
            <div class="stat-item">
              <div class="stat-label">今日学习时长</div>
              <div class="stat-value">{{ todayStudyTime }}分钟</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">已完成任务</div>
              <div class="stat-value">{{ completedTasks }}/{{ totalTasks }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 主要功能区 -->
    <div class="main-area">
      <!-- 左侧区域：番茄钟 + 计划 -->
      <div class="left-area">
        <!-- 番茄钟 -->
        <div class="pomodoro-card">
          <PomodoroTimer />
        </div>
        
        <!-- 今日学习计划 - 使用学习中心的组件 -->
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
      </div>
      
      <!-- 中间区域：统计卡片 + 图表 -->
      <div class="center-area">
        <!-- 统计卡片 -->
        <div class="stats-cards">
          <div class="stat-card gpa-card">
            <div class="stat-icon">
              <el-icon><Trophy /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ currentGPA.toFixed(2) }}</div>
              <div class="stat-title">当前学期GPA</div>
              <div class="stat-subtitle">累计：{{ totalGPA.toFixed(2) }}</div>
            </div>
          </div>
          
          <div class="stat-card progress-card">
            <div class="stat-icon">
              <el-icon><Checked /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ completionRate }}%</div>
              <div class="stat-title">任务完成率</div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: completionRate + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 学习统计图表 -->
        <div class="chart-card">
          <StudyChart />
        </div>
        
        <!-- 今日待办事项 - 使用学习中心的组件 -->
        <div class="todo-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon><List /></el-icon>
              <h3>今日待办事项</h3>
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
              <el-empty description="今日暂无任务" :image-size="60">
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
      
      <!-- 右侧区域：快速操作 -->
      <div class="right-area">
        <div class="quick-actions">
          <div class="section-title">
            <el-icon><Lightning /></el-icon>
            <h3>快速操作</h3>
          </div>
          <div class="action-buttons">
            <el-button type="primary" class="action-btn" @click="showTaskForm('create')">
              <el-icon><Plus /></el-icon>
              新建任务
            </el-button>
            <el-button class="action-btn" @click="showPlanForm('create')">
              <el-icon><Clock /></el-icon>
              添加计划
            </el-button>
            <el-button class="action-btn" @click="goToAIAssistant">
              <el-icon><ChatDotRound /></el-icon>
              AI助手
            </el-button>
            <router-link to="/settings">
              <el-button class="action-btn">
                <el-icon><Setting /></el-icon>
                学习设置
              </el-button>
            </router-link>
          </div>
        </div>
        
        <!-- 即将截止 -->
        <div class="upcoming-deadlines">
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
        
        <!-- GPA趋势 -->
        <div class="gpa-trend">
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
import { useMainStore } from '../stores'
import dayjs from 'dayjs'
import { 
  Calendar, 
  Clock, 
  Checked, 
  Trophy, 
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
const currentGPA = computed(() => store.currentGPA || 3.42)
const totalGPA = ref(3.30)

const username = computed(() => store.userInfo.username || '同学')
const currentDate = computed(() => dayjs().format('YYYY-MM-DD'))
const currentWeekday = computed(() => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[dayjs().day()]
})

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

const completedTasks = computed(() => dashboardTasks.value.filter(item => item.completed).length)
const totalTasks = computed(() => dashboardTasks.value.length)
const completionRate = computed(() => totalTasks.value ? Math.round((completedTasks.value / totalTasks.value) * 100) : 0)

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
/* 固定宽度布局 */
.dashboard {
  width: 1200px;
  margin: 0 auto;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  border-radius: 10px;
  padding: 25px 30px;
  margin-bottom: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-left h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
}

.date-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  opacity: 0.9;
}

.welcome-right {
  display: flex;
  gap: 30px;
}

.study-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
}

/* 主区域布局 - 三栏固定宽度 */
.main-area {
  display: flex;
  gap: 20px;
}

.left-area {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.center-area {
  width: 580px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-area {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  border: 1px solid #e5e5e5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 番茄钟卡片 */
.pomodoro-card {
  padding: 20px;
}

/* 计划卡片 */
.plan-card {
  padding: 20px;
  min-height: 300px;
  display: flex;
  flex-direction: column;
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
  color: #333;
}

.card-title .el-icon {
  color: #1890ff;
  font-size: 18px;
}

.plan-actions {
  display: flex;
  gap: 8px;
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
  background: #f8f9fa;
  border-left: 3px solid #e8e8e8;
  transition: all 0.2s;
  flex-shrink: 0;
}

.plan-item:hover {
  background: #f0f0f0;
  transform: translateX(2px);
}

.plan-item.active {
  background: #fff7e6;
  border-left-color: #faad14;
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
  color: #1890ff;
}

.time-range {
  font-size: 12px;
  color: #666;
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
  color: #333;
  margin-bottom: 2px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plan-course {
  font-size: 12px;
  color: #999;
}

.plan-status {
  flex-shrink: 0;
}

/* 统计卡片 */
.stats-cards {
  display: flex;
  gap: 20px;
}

.stat-card {
  flex: 1;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 15px;
  height: 100px;
}

.gpa-card {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  color: white;
}

.progress-card {
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  color: white;
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.stat-subtitle {
  font-size: 12px;
  opacity: 0.8;
}

.progress-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
  overflow: hidden;
  margin-top: 8px;
}

.progress-fill {
  height: 100%;
  background: white;
  border-radius: 3px;
  transition: width 0.3s;
}

/* 图表卡片 */
.chart-card {
  padding: 20px;
}

/* 待办卡片 */
.todo-card {
  padding: 20px;
  min-height: 320px;
  display: flex;
  flex-direction: column;
}

.todo-header-actions {
  display: flex;
  gap: 8px;
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
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  transition: all 0.2s;
  flex-shrink: 0;
}

.todo-item:hover {
  background: #f0f0f0;
  transform: translateX(2px);
}

.todo-item.high-priority {
  border-left: 3px solid #ff4d4f;
  background: #fff2f0;
}

.todo-item.completed {
  opacity: 0.7;
  background: #f5f5f5;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
  color: #999;
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
  color: #333;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.todo-deadline {
  color: #ff4d4f;
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
  color: #333;
}

.section-title .el-icon {
  color: #1890ff;
  font-size: 18px;
}

.quick-actions,
.upcoming-deadlines,
.gpa-trend {
  padding: 20px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
  height: 40px;
  font-size: 14px;
}

.action-btn .el-icon {
  margin-right: 8px;
}

.deadline-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.deadline-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
  transition: all 0.2s;
  cursor: pointer;
}

.deadline-item:hover {
  background: #f0f0f0;
  transform: translateX(2px);
}

.deadline-title {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.deadline-time {
  font-size: 12px;
  color: #ff4d4f;
  font-weight: 500;
  margin-left: 10px;
  flex-shrink: 0;
}

.empty-deadline {
  padding: 15px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.trend-chart {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.trend-placeholder {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 100px;
  margin-bottom: 10px;
}

.trend-bar {
  width: 30px;
  background: #1890ff;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s;
}

.trend-bar.active {
  background: #52c41a;
}

.trend-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.trend-labels .active {
  color: #1890ff;
  font-weight: 500;
}

/* 滚动条样式 */
.plan-list::-webkit-scrollbar,
.todo-list::-webkit-scrollbar {
  width: 4px;
}

.plan-list::-webkit-scrollbar-track,
.todo-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.plan-list::-webkit-scrollbar-thumb,
.todo-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.plan-list::-webkit-scrollbar-thumb:hover,
.todo-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
