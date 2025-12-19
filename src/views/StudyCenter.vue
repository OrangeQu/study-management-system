<template>
  <div class="study-center">
    <div class="page-header">
      <h1>学习中心</h1>
      <div class="quick-actions">
        <el-button type="primary" @click="showTaskForm('create')">
          新建任务
        </el-button>
        <el-button @click="addPlan">
          添加计划
        </el-button>
      </div>
    </div>

    <div class="main-content">

      

      <div class="left-panel">
        <div class="section today-plan card-tone card-tone--peach">
          <div class="section-header">
            <h2>今日学习计划</h2>
            <div class="plan-stats">
              <span>共 {{ todayPlans.length }} 个计划</span>
              <el-button size="small" @click="addPlan">
                添加计划
              </el-button>
            </div>
          </div>
          
          <div class="plan-content" ref="planContent">
            <div v-if="todayPlans.length === 0" class="empty-plan">
              <el-empty description="今日暂无计划" :image-size="60">
                <el-button size="small" @click="addPlan">添加第一个计划</el-button>
              </el-empty>
            </div>
            
            <div v-else class="plan-list">
              <div 
                v-for="plan in todayPlans" 
                :key="plan.id"
                class="plan-item"
              >
                <div class="plan-time">
                  <el-icon><Clock /></el-icon>
                  <span>{{ plan.time_start }} - {{ plan.time_end }}</span>
                </div>
                
                <div class="plan-info">
                  <div class="plan-title">{{ plan.task_title || '学习时间' }}</div>
                  <div v-if="plan.description" class="plan-description">
                    {{ plan.description }}
                  </div>
                  <div class="plan-tags">
                    <el-tag v-if="plan.course" size="small" type="info">{{ plan.course }}</el-tag>
                  </div>
                </div>
                
                <div class="plan-actions">
                  <el-button 
                    type="text" 
                    size="small" 
                    @click="editPlan(plan)"
                    :icon="Edit"
                  />
                  <el-button 
                    type="text" 
                    size="small" 
                    @click="deletePlan(plan.id)"
                    :icon="Delete"
                    class="delete-btn"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        
        
        
      </div>

      <div class="right-panel">
        <div class="section ai-chatroom card-tone card-tone--green">
          <div class="section-header">
            <h2>AI学习助手</h2>
          </div>
          <div class="chatroom-content">
            <div class="message-list" ref="messageList">
              <div v-for="message in chatMessages" :key="message.id" 
                   :class="['message-item', message.type]">
                <div class="message-avatar">
                  <el-avatar size="small">
                    {{ message.type === 'user' ? '我' : 'AI' }}
                  </el-avatar>
                </div>
                <div class="message-content">
                  <div class="message-text">{{ message.content }}</div>
                  <div class="message-time">{{ formatTime(message.time) }}</div>
                </div>
              </div>
            </div>

            <div class="message-input">
              <el-input
                v-model="userInput"
                placeholder="向AI助手提问..."
                type="textarea"
                :rows="2"
                resize="none"
                @keydown.enter.exact.prevent="sendMessage"
              />
              <div class="input-actions">
                <div class="quick-questions">
                  <span class="quick-label">快速提问：</span>
                  <el-button size="small" @click="quickQuestion('如何制定学习计划？')">
                    学习计划
                  </el-button>
                  <el-button size="small" @click="quickQuestion('如何提高学习效率？')">
                    学习效率
                  </el-button>
                </div>
                <el-button type="primary" @click="sendMessage" :loading="isSending">
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 将任务管理放在今日计划与 AI 聊天室下面，横跨整页 -->
      <div class="section task-management card-tone card-tone--purple">
        <div class="section-header">
          <h2>任务管理</h2>
          <div class="task-header-actions">
            <TaskFilter 
              v-model:search="searchKeyword"
              v-model:status="filterStatus"
              v-model:priority="filterPriority"
              v-model:type="filterType"
            />
            <el-button size="small" @click="showTaskForm('create')">
              新建任务
            </el-button>
          </div>
        </div>

        <div class="task-content" ref="taskContent">
          <div v-if="filteredTasks.length === 0" class="empty-task">
            <el-empty description="暂无任务" :image-size="60">
              <el-button size="small" @click="showTaskForm('create')">创建第一个任务</el-button>
            </el-empty>
          </div>
          
          <div v-else class="task-list">
            <div 
              v-for="task in filteredTasks" 
              :key="task.id"
              class="task-item"
              :class="{
                'task-done': task.status === 'done',
                'task-doing': task.status === 'doing'
              }"
            >
              <div class="task-checkbox">
                <el-checkbox 
                  v-model="task.completed" 
                  @change="toggleTaskStatus(task)"
                />
              </div>
              
              <div class="task-info">
                <div class="task-header">
                  <span class="task-title" :class="{ completed: task.status === 'done' }">
                    {{ task.title }}
                  </span>
                  <div class="task-tags">
                    <el-tag 
                      size="small" 
                      :type="getPriorityType(task.priority)"
                    >
                      {{ getPriorityText(task.priority) }}
                    </el-tag>
                    <el-tag size="small" type="info">{{ task.type }}</el-tag>
                  </div>
                </div>
                
                <div class="task-details">
                  <div v-if="task.description" class="task-description">
                    {{ task.description }}
                  </div>
                  <div class="task-deadline">
                    <el-icon size="12"><Clock /></el-icon>
                    <span :class="{ urgent: isUrgent(task.deadline) }">
                      {{ formatDeadline(task.deadline) }}
                    </span>
                  </div>
                </div>
              </div>
              
              <div class="task-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  @click="editTask(task)"
                  :icon="Edit"
                />
                <el-button 
                  type="text" 
                  size="small" 
                  @click="deleteTask(task.id)"
                  :icon="Delete"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

      <!-- 学习总览卡片：左侧学习统计，右侧若干数据卡 -->
      <div class="section learning-overview">
        <div class="overview-grid">
          <div class="overview-left">
            <div class="chart-wrapper">
              <StudyChart />
            </div>
          </div>

          <div class="overview-right">
            <div class="stat-card stat-card--teal">
              <div class="stat-top">
                <div class="stat-icon">🕒</div>
                <div class="stat-title">总学习时长</div>
              </div>
              <div class="stat-value">{{ totalStudyDuration }}</div>
            </div>

            <div class="stat-card stat-card--green">
              <div class="stat-top">
                <div class="stat-icon">⏱</div>
                <div class="stat-title">今日学习时长</div>
              </div>
              <div class="stat-value">{{ todayStudyDuration }}</div>
            </div>

            <div class="stat-card stat-card--blue">
              <div class="stat-top">
                <div class="stat-icon">📝</div>
                <div class="stat-title">任务数量</div>
              </div>
              <div class="stat-value">{{ todayTaskCount }}</div>
            </div>

            <div class="stat-card stat-card--purple">
              <div class="stat-top">
                <div class="stat-icon">✅</div>
                <div class="stat-title">已完成任务数量</div>
              </div>
              <div class="stat-value">{{ completedTaskCount }}</div>
            </div>
          </div>
        </div>
      </div>

    <TaskForm
      v-model="showTaskDialog"
      :mode="taskFormMode"
      :task="currentTask"
      @submit="saveTask"
      @close="showTaskDialog = false"
    />

    <PlanForm
      v-model="showPlanDialog"
      :mode="planFormMode"
      :plan="currentPlan"
      :tasks="tasks"
      @submit="savePlan"
      @close="showPlanDialog = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { Clock, Edit, Delete } from '@element-plus/icons-vue'
import TaskFilter from '@/components/task/TaskFilter.vue'
import StudyChart from '@/components/charts/StudyChart.vue'
import TaskForm from '@/components/task/TaskForm.vue'
import PlanForm from '@/components/plan/PlanForm.vue'
import { listTasks, createTask, updateTask, deleteTask as apiDeleteTask, updateTaskStatus } from '@/api/tasks'
import { listPlans, createPlan, updatePlan, deletePlan as apiDeletePlan } from '@/api/plans'
import { sendMessage as sendChatMessage } from '@/api/chat'
import { overviewStats as apiOverviewStats } from '@/api/study'

const showTaskDialog = ref(false)
const showPlanDialog = ref(false)
const searchKeyword = ref('')
const filterStatus = ref('')
const filterPriority = ref('')
const filterType = ref('')
const userInput = ref('')
const isSending = ref(false)

const taskFormMode = ref('create')
const planFormMode = ref('create')
const currentTask = ref(null)
const currentPlan = ref(null)

const planContent = ref(null)
const taskContent = ref(null)
const messageList = ref(null)

const tasks = ref([])
const todayPlans = ref([])
const chatMessages = ref([
  {
    id: 1,
    type: 'ai',
    content: '你好！我是你的学习助手。我可以帮你制定学习计划、解决学习问题等。',
    time: new Date()
  }
])
const conversationId = ref(null)
const overviewStats = ref({})

const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    const matchesSearch = !searchKeyword.value || 
      (task.title || '').toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchesStatus = !filterStatus.value || task.status === filterStatus.value
    const matchesPriority = !filterPriority.value || task.priority === Number(filterPriority.value)
    const matchesType = !filterType.value || task.type === filterType.value
    return matchesSearch && matchesStatus && matchesPriority && matchesType
  })
})

const formatDuration = (mins) => {
  if (!mins || mins <= 0) return '0分'
  const h = Math.floor(mins / 60)
  const m = mins % 60
  return h > 0 ? `${h}小时${m}分` : `${m}分`
}

const todayStudyDuration = computed(() => formatDuration(overviewStats.value.today_minutes || 0))
const totalStudyDuration = computed(() => formatDuration(overviewStats.value.total_minutes || 0))
const todayTaskCount = computed(() => tasks.value.length || overviewStats.value.today_task_count || 0)
const completedTaskCount = computed(() => {
  if (tasks.value.length > 0) {
    return tasks.value.filter(task => task.status === 'done' || task.completed).length
  }
  return overviewStats.value.completed_task_count || 0
})

const getPriorityType = (priority) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[priority] || 'info'
}

const getPriorityText = (priority) => {
  const texts = { 1: '高', 2: '中', 3: '低' }
  return texts[priority] || '低'
}

const isUrgent = (deadline) => {
  if (!deadline) return false
  const deadlineTime = dayjs(deadline)
  const now = dayjs()
  const diffHours = deadlineTime.diff(now, 'hour')
  return diffHours <= 24 && diffHours > 0
}

const formatDeadline = (deadline) => {
  if (!deadline) return '无截止时间'
  const deadlineTime = dayjs(deadline)
  const now = dayjs()
  if (deadlineTime.isBefore(now)) return '已过期'
  const diffHours = deadlineTime.diff(now, 'hour')
  const diffDays = deadlineTime.diff(now, 'day')
  if (diffHours < 24) return `${diffHours}小时内`
  if (diffDays < 7) return `${diffDays}天内`
  return deadlineTime.format('MM-DD HH:mm')
}

const formatTime = (time) => dayjs(time).format('HH:mm')

const showTaskForm = (mode, task = null) => {
  taskFormMode.value = mode
  currentTask.value = task
  showTaskDialog.value = true
}

const editTask = (task) => showTaskForm('edit', task)

const deleteTask = async (taskId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiDeleteTask(taskId)
    await Promise.all([loadTasks(), loadOverviewStats()])
    ElMessage.success('任务已删除')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

const toggleTaskStatus = async (task) => {
  try {
    task.status = task.completed ? 'done' : 'todo'
    await updateTaskStatus(task.id, { status: task.status, completed: task.completed })
    await Promise.all([loadTasks(), loadOverviewStats()])
    ElMessage.success('任务状态已更新')
  } catch (e) {
    ElMessage.error(e.message || '更新状态失败')
  }
}

const saveTask = async (taskData) => {
  try {
    if (taskFormMode.value === 'create') {
      await createTask({ ...taskData, status: 'todo', completed: false })
      ElMessage.success('任务创建成功')
    } else if (currentTask.value) {
      await updateTask(currentTask.value.id, taskData)
      ElMessage.success('任务更新成功')
    }
    showTaskDialog.value = false
    await Promise.all([loadTasks(), loadOverviewStats()])
  } catch (e) {
    ElMessage.error(e.message || '保存任务失败')
  }
}

const addPlan = () => {
  planFormMode.value = 'create'
  currentPlan.value = null
  showPlanDialog.value = true
}

const editPlan = (plan) => {
  planFormMode.value = 'edit'
  currentPlan.value = plan
  showPlanDialog.value = true
}

const deletePlan = async (planId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个计划吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiDeletePlan(planId)
    await loadPlans()
    ElMessage.success('计划已删除')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

const savePlan = async (planData) => {
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
    ElMessage.error(e.message || '保存计划失败')
  }
}

const scrollChatToBottom = () => {
  nextTick(() => {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight
    }
  })
}

const normalizeChatMessage = (message) => ({
  id: message.id || Date.now(),
  type: message.role === 'user' ? 'user' : 'ai',
  content: message.content || '',
  time: message.time ? new Date(message.time) : new Date()
})

const sendMessage = async () => {
  const content = userInput.value.trim()
  if (!content || isSending.value) return
  const tempId = Date.now()
  const userMessage = { id: tempId, type: 'user', content, time: new Date() }
  chatMessages.value.push(userMessage)
  userInput.value = ''
  isSending.value = true
  scrollChatToBottom()
  try {
    const resp = await sendChatMessage({
      content,
      conversation_id: conversationId.value || undefined
    })
    const data = resp.data?.data || {}
    if (data.conversation_id) {
      conversationId.value = data.conversation_id
    }
    if (Array.isArray(data.history) && data.history.length > 0) {
      chatMessages.value = data.history.map(normalizeChatMessage)
    } else if (data.reply) {
      chatMessages.value.push(
        normalizeChatMessage({
          id: Date.now() + 1,
          role: 'assistant',
          content: data.reply,
          time: new Date()
        })
      )
    }
    scrollChatToBottom()
  } catch (error) {
    chatMessages.value = chatMessages.value.filter((msg) => msg.id !== tempId)
    ElMessage.error(error.message || '发送失败，请稍后重试')
  } finally {
    isSending.value = false
  }
}

const quickQuestion = (question) => {
  userInput.value = question
  sendMessage()
}

const loadTasks = async () => {
  const resp = await listTasks({ page: 1, pageSize: 200 })
  tasks.value = resp.data.data?.list || []
}

const loadPlans = async () => {
  const resp = await listPlans({ date: dayjs().format('YYYY-MM-DD') })
  todayPlans.value = resp.data.data || []
}

const loadOverviewStats = async () => {
  try {
    const resp = await apiOverviewStats()
    overviewStats.value = resp.data?.data || {}
  } catch (error) {
    overviewStats.value = {}
  }
}

const handleSessionRecorded = () => {
  loadOverviewStats()
}

onMounted(async () => {
  await Promise.all([loadTasks(), loadPlans(), loadOverviewStats()])
  window.addEventListener('study-session-recorded', handleSessionRecorded)
})

onUnmounted(() => {
  window.removeEventListener('study-session-recorded', handleSessionRecorded)
})
</script>

<style scoped>
/* 固定宽度布局，参考首页Dashboard */
.study-center {
  width: 1100px; /* match Dashboard */
  margin: 0 auto;
  padding: 20px 0;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-shrink: 0;
  width: 100%;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.quick-actions {
  display: flex;
  gap: 10px;
}
.main-content {
  display: grid;
  grid-template-columns: 1fr 380px; /* left / right: make right wider */
  gap: 20px;
  flex: 1;
  min-height: 0;
  width: 100%;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  width: auto; /* allow left column to be flexible */
}

.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  width: 380px; /* make AI 聊天室区域更宽 */
}

.section {
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 主题色卡片变体，参考 Dashboard 配色 */
.card-tone--peach {
  background: #F7EDE1;
  border: 1px solid rgba(166,90,0,0.08);
}
.card-tone--purple {
  background: #E0E1F5;
  border: 1px solid rgba(47,47,122,0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: transparent;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  flex-shrink: 0;
}

.section-header h2 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.today-plan {
  flex: 1;
  min-height: 500px;
  max-height: 500px;
}

.plan-stats {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.plan-content {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  max-height: 380px;
}

/* 同步空状态按钮风格 */
.empty-plan :deep(.el-button),
.empty-task :deep(.el-button) {
  border-radius: 6px;
  padding: 6px 10px;
  border: none !important;
}

/* 添加计划按钮（在今日学习计划卡片内） */
.card-tone--purple .plan-stats :deep(.el-button),
.card-tone--purple .section-header :deep(.el-button) {
  background: #D6D9F6 !important;
  color: #2F2F7A !important;
}

/* 今日学习计划顶部“添加计划”按钮使用橙色风格 */
.today-plan .plan-stats :deep(.el-button) {
  background: #F8E2C9 !important;
  color: #A65A00 !important;
  border: none !important;
  border-radius: 6px;
  padding: 6px 10px;
}
.today-plan .plan-stats :deep(.el-button):hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

/* 新建任务按钮（页头） */
.quick-actions :deep(.el-button[type="primary"]).el-button {
  background: #F8E2C9 !important;
  color: #A65A00 !important;
  border: none !important;
}

/* 任务管理内的新建任务按钮（紫色） */
.task-management .task-header-actions :deep(.el-button) {
  background: #D6D9F6 !important;
  color: #2F2F7A !important;
  border: none !important;
  border-radius: 6px;
  padding: 6px 10px;
}

/* 任务管理空状态“创建第一个任务”按钮（紫色） */
.task-management .empty-task :deep(.el-button) {
  background: #D6D9F6 !important;
  color: #2F2F7A !important;
  border: none !important;
  border-radius: 6px;
  padding: 6px 10px;
}
.task-management .empty-task :deep(.el-button):hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

/* 计划与任务列表项背景微调，贴近 Dashboard 的卡片感 */
.plan-item,
.task-item {
  background: var(--page-bg, #f8f9fa);
  border: 1px solid rgba(0,0,0,0.04);
}

/* AI 聊天气泡配色微调（使用全局绿色配色） */
.message-item.ai .message-text {
  background: var(--soft-green, rgba(44,199,183,0.08));
  color: var(--nav-active);
}
.message-item.user .message-text {
  background: var(--primary);
  color: #fff;
}

.empty-plan {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 150px;
}

/* 空状态内“添加第一个计划”按钮，与顶部添加计划保持一致的橙色 */
.today-plan .empty-plan :deep(.el-button) {
  background: #F8E2C9 !important;
  color: #A65A00 !important;
  border: none !important;
  border-radius: 6px;
  padding: 6px 10px;
}
.today-plan .empty-plan :deep(.el-button):hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.plan-item {
  display: flex;
  align-items: flex-start;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.plan-item:hover {
  border-color: #1890ff;
  background: #f0f9ff;
}

.plan-time {
  min-width: 120px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1890ff;
  font-weight: 500;
  margin-right: 20px;
}

.plan-info {
  flex: 1;
  min-width: 0;
}

.plan-title {
  font-weight: 500;
  color: #333;
  font-size: 15px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plan-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.plan-tags {
  display: flex;
  gap: 8px;
}

.plan-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  flex-shrink: 0;
}

.plan-item:hover .plan-actions {
  opacity: 1;
}

.delete-btn:hover {
  color: #ff4d4f;
}

.task-management {
  flex: 1;
  min-height: 530px;
  max-height: 530px;
  grid-column: 1 / -1; /* span full width of main-content */
}

.task-header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.task-content {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  max-height: 450px;
}

.empty-task {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  align-items: flex-start;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  transition: all 0.3s ease;
}

.task-item:hover {
  border-color: #1890ff;
  background: #f8f9fa;
}

.task-item.task-done {
  opacity: 0.7;
  background: #f6ffed;
}

.task-item.task-doing {
  background: #fff7e6;
}

.task-checkbox {
  margin-right: 15px;
  margin-top: 4px;
  flex-shrink: 0;
}

.task-info {
  flex: 1;
  min-width: 0;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  gap: 10px;
}

.task-title {
  font-weight: 500;
  color: #333;
  font-size: 15px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-title.completed {
  text-decoration: line-through;
  color: #999;
}

.task-tags {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.task-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-deadline {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.task-deadline .urgent {
  color: #ff4d4f;
  font-weight: 500;
}

.task-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  flex-shrink: 0;
}

.task-item:hover .task-actions {
  opacity: 1;
}

.stats-panel {
  flex: 0 0 auto;
  height: 550px;
  min-height: auto;
}

.ai-chatroom {
  flex: 1;
  min-height: 350px;
  max-height: 500px;
}

/* 覆盖 AI 聊天室卡片为指定的浅绿背景 */
.section.ai-chatroom {
  background: #EFF7E0 !important;
  border-color: rgba(44,199,183,0.12) !important;
}

.chatroom-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  max-height: 420px;
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 300px;
}

.message-item {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.ai {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.message-item.user .message-content {
  align-items: flex-end;
}

.message-text {
  padding: 10px 15px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  max-width: 100%;
}

.message-item.ai .message-text {
  background: #f0f2f5;
  color: #333;
}

.message-item.user .message-text {
  background: #1890ff;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.message-input {
  padding: 15px 20px;
  border-top: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.quick-questions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quick-label {
  font-size: 12px;
  color: #999;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.time-separator {
  color: #999;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.plan-content::-webkit-scrollbar,
.task-content::-webkit-scrollbar,
.message-list::-webkit-scrollbar {
  width: 6px;
}

.plan-content::-webkit-scrollbar-track,
.task-content::-webkit-scrollbar-track,
.message-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.plan-content::-webkit-scrollbar-thumb,
.task-content::-webkit-scrollbar-thumb,
.message-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.plan-content::-webkit-scrollbar-thumb:hover,
.task-content::-webkit-scrollbar-thumb:hover,
.message-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Chart row spans full grid width under task-management */
.chart-row {
  grid-column: 1 / -1;
  margin-top: 18px;
}

.chart-row .study-chart {
  width: 100%;
}

/* 学习总览卡片布局 */
.learning-overview {
  grid-column: 1 / -1;
  padding: 0;
  margin-top: 20px; /* space from the card above */
}
.overview-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}
.overview-grid {
  align-items: stretch; /* make left/right columns match heights */
}
.overview-left { display: flex; flex-direction: column; }
.overview-left .chart-wrapper {
  padding: 16px;
  min-height: 240px;
  flex: 1; /* allow left body to define height */
}
.overview-right {
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* evenly distribute cards vertically */
  gap: 16px;
  height: 100%; /* fill the full height of the grid row */
  padding: 20px; /* increased outer spacing inside the learning-overview */
  box-sizing: border-box;
}
.stat-card {
  /* base card */
  background: #fff;
  border-radius: 12px;
  padding: 18px; /* larger inner padding */
  border: 1px solid rgba(0,0,0,0.04);
  box-shadow: 0 8px 20px rgba(43,50,80,0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 110px;
  width: 100%;
}

/* 卡片纯色主题变量（局部覆盖，使用全局变量作为回退） */
.overview-right {
  --card-teal: var(--primary);
  --card-green: var(--soft-green-600, #cdeccd);
  --card-blue: #e8f2ff;
  --card-purple: var(--soft-purple-600, #c9bff6);
}
.stat-title {
  font-size: 13px;
  color: var(--muted, #666);
  margin-bottom: 6px;
}
.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--nav-active, #2b3250);
}


.stat-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.stat-icon {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  font-size: 18px;
  background: rgba(0,0,0,0.03);
}

/* use global variables for colors */
.stat-card--teal {
  /* 改为浅橙色背景，使用全局浅桃变量为回退 */
  background: var(--soft-peach, #FFF2E6);
  color: var(--accent, #fb8c4a);
}
.stat-card--teal .stat-icon { background: rgba(255,255,255,0.7); color: var(--accent, #fb8c4a) }

.stat-card--green {
  background: var(--card-green);
  color: var(--nav-active);
}
.stat-card--green .stat-icon { background: rgba(255,255,255,0.35); color: var(--nav-active) }

.stat-card--blue {
  background: var(--card-blue);
  color: var(--nav-active);
}
.stat-card--blue .stat-icon { background: rgba(255,255,255,0.6); color: var(--nav-active) }

.stat-card--purple {
  /* 浅紫色主题，使用全局回退变量 */
  background: var(--soft-purple, #EDE8FF);
  color: var(--nav-active, #2b3250);
}
.stat-card--purple .stat-icon { background: rgba(255,255,255,0.7); color: var(--nav-active, #2b3250); }

/* make stacked cards look better on narrow screens */
@media (max-width: 900px) {
  .overview-right { gap: 18px; padding: 12px }
  .stat-card { min-height: 110px; padding: 12px }
}

@media (max-width: 900px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
  .overview-right {
    flex-direction: row;
    gap: 10px;
  }
  .stat-card { flex: 1 }
}
</style>
