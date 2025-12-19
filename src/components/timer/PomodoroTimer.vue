<template>
  <div class="pomodoro-timer">
    <div class="timer-header">
      <h3>番茄钟</h3>
      <div class="mode-selector">
        <el-radio-group v-model="mode" size="small" @change="switchMode">
          <el-radio-button label="work">专注</el-radio-button>
          <el-radio-button label="break">休息</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <div class="timer-display" :class="{ work: mode === 'work' }" :style="{ backgroundColor: 'var(--accent)' }">
      <div class="time-text">
        {{ minutes.toString().padStart(2, '0') }}:{{ seconds.toString().padStart(2, '0') }}
      </div>
      <div class="mode-text">
        {{ modeText }}
      </div>
    </div>
    
    <div class="timer-controls">
      <el-button 
        type="primary" 
        :icon="isRunning ? 'VideoPause' : 'VideoPlay'" 
        @click="toggleTimer"
        round
        size="large"
      >
        {{ isRunning ? '暂停' : '开始' }}
      </el-button>
      
      <el-button 
        icon="Refresh" 
        @click="resetTimer"
        round
        size="large"
      >
        重置
      </el-button>
    </div>
    
    <div class="timer-settings">
      <div class="setting-item">
        <span>专注时长：</span>
        <el-input-number 
          v-model="workDuration" 
          :min="5" 
          :max="60" 
          :step="5" 
          size="small"
          @change="updateSettings"
        />
        <span>分钟</span>
      </div>
      
      <div class="setting-item">
        <span>休息时长：</span>
        <el-input-number 
          v-model="breakDuration" 
          :min="1" 
          :max="15" 
          :step="1" 
          size="small"
          @change="updateSettings"
        />
        <span>分钟</span>
      </div>
      
      <div class="pomodoro-count">
        <el-icon><Check /></el-icon>
        <span>今日已完成：{{ completedPomodoros }}个番茄</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { startSession, updateSession, todayStats } from '@/api/study'
import { useMainStore } from '@/stores'

const store = useMainStore()
const STORAGE_KEY = 'pomodoroActiveSession'

const mode = ref('work')
const isRunning = ref(false)
const timeLeft = ref(0)
const timerInterval = ref(null)
const workDuration = ref(store.studySettings.pomodoroWork || 25)
const breakDuration = ref(store.studySettings.pomodoroBreak || 5)
const completedPomodoros = ref(0)

const activeSessionId = ref(null)
const sessionDuration = ref(0) // seconds
const elapsedSeconds = ref(0)
const runStartedAt = ref(null)
const sessionEndAt = ref(null)

const minutes = computed(() => Math.floor(timeLeft.value / 60))
const seconds = computed(() => timeLeft.value % 60)

const modeText = computed(() => (mode.value === 'work' ? '专注时间' : '休息时间'))

const currentElapsed = () => {
  if (!runStartedAt.value) {
    return elapsedSeconds.value
  }
  const diff = Math.floor((Date.now() - runStartedAt.value.getTime()) / 1000)
  return Math.min(sessionDuration.value, elapsedSeconds.value + Math.max(diff, 0))
}

const getModeDurationSeconds = () => {
  const duration = mode.value === 'work' ? workDuration.value : breakDuration.value
  return Math.max(1, duration) * 60
}

const elapsedMinutesText = () => {
  const minutes = Math.max(1, Math.ceil(currentElapsed() / 60))
  return minutes >= 60 ? `${(minutes / 60).toFixed(1)}小时` : `${minutes}分钟`
}

const updateTimeLeft = () => {
  if (sessionDuration.value <= 0) return
  timeLeft.value = Math.max(sessionDuration.value - currentElapsed(), 0)
}

const persistActiveSession = () => {
  if (!activeSessionId.value) {
    localStorage.removeItem(STORAGE_KEY)
    return
  }
  const payload = {
    id: activeSessionId.value,
    mode: mode.value,
    durationSeconds: sessionDuration.value,
    elapsedSeconds: currentElapsed(),
    isRunning: isRunning.value,
    runStartedAt: runStartedAt.value ? runStartedAt.value.toISOString() : null,
    endTimestamp: sessionEndAt.value ? sessionEndAt.value.getTime() : null
  }
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
  } catch (e) {
    // ignore storage errors
  }
}

const clearTimerInterval = () => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
    timerInterval.value = null
  }
}

const initializeTime = () => {
  sessionDuration.value = getModeDurationSeconds()
  elapsedSeconds.value = 0
  runStartedAt.value = null
  timeLeft.value = sessionDuration.value
  isRunning.value = false
  if (mode.value !== 'work') {
    activeSessionId.value = null
    sessionEndAt.value = null
    persistActiveSession()
  }
}

const loadTodaySummary = async () => {
  try {
    const resp = await todayStats()
    completedPomodoros.value = resp.data?.data?.pomodoroCount || 0
  } catch (error) {
    completedPomodoros.value = 0
  }
}

const beginTicking = () => {
  clearTimerInterval()
  timerInterval.value = setInterval(async () => {
    updateTimeLeft()
    if (timeLeft.value <= 0) {
      clearTimerInterval()
      await handleTimerFinished()
    } else {
      persistActiveSession()
    }
  }, 1000)
}

const resumeTimer = () => {
  if (timeLeft.value <= 0) return
  runStartedAt.value = new Date()
  isRunning.value = true
  beginTicking()
  persistActiveSession()
}

const createRemoteSession = async () => {
  const durationMinutes = mode.value === 'work' ? workDuration.value : breakDuration.value
  sessionDuration.value = durationMinutes * 60
  elapsedSeconds.value = 0
  timeLeft.value = sessionDuration.value
  try {
    const resp = await startSession({
      type: 'pomodoro',
      mode: mode.value,
      duration_minutes: durationMinutes
    })
    activeSessionId.value = resp.data?.data?.id
    const endAt = resp.data?.data?.end_at
    sessionEndAt.value = endAt ? new Date(endAt) : new Date(Date.now() + sessionDuration.value * 1000)
  } catch (error) {
    ElMessage.error(error.message || '开启番茄钟失败')
    throw error
  }
  persistActiveSession()
}

const startTimer = async () => {
  if (timeLeft.value <= 0) {
    ElMessage.warning('时间已到！请切换模式或重置')
    return
  }
  if (mode.value === 'work' && !activeSessionId.value) {
    await createRemoteSession()
  }
  resumeTimer()
}

const pauseTimer = () => {
  if (!isRunning.value) return
  elapsedSeconds.value = currentElapsed()
  runStartedAt.value = null
  isRunning.value = false
  clearTimerInterval()
  persistActiveSession()
}

const promptFinishOnPause = async () => {
  try {
    await ElMessageBox.confirm(
      `${mode.value === 'work' ? '已专注' : '已休息'} ${elapsedMinutesText()}，是否立即结束？`,
      '结束本次',
      {
        confirmButtonText: '结束本次',
        cancelButtonText: '继续计时',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    await handleManualCompletion()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      resumeTimer()
    }
  }
}

const abortActiveSession = async () => {
  if (!activeSessionId.value) return
  try {
    await updateSession(activeSessionId.value, {}, { action: 'abort' })
  } catch (error) {
    // ignore abort errors
  } finally {
    activeSessionId.value = null
    elapsedSeconds.value = 0
    runStartedAt.value = null
    persistActiveSession()
  }
}

const resetTimer = async () => {
  pauseTimer()
  const hasProgress = currentElapsed() > 0
  if (hasProgress) {
    try {
      await ElMessageBox.confirm(
        `${mode.value === 'work' ? '已专注' : '已休息'} ${elapsedMinutesText()}，是否保存？`,
        '结束本次',
        {
          confirmButtonText: '保存',
          cancelButtonText: '放弃',
          type: 'warning',
          distinguishCancelAndClose: true
        }
      )
      await handleManualCompletion()
      return
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') return
    }
  }
  await abortActiveSession()
  initializeTime()
}

const toggleTimer = async () => {
  if (isRunning.value) {
    pauseTimer()
    await promptFinishOnPause()
  } else {
    await startTimer()
  }
}

const playCompletionSound = () => {
  const audio = new Audio('https://assets.mixkit.co/sfx/preview/mixkit-correct-answer-tone-2870.mp3')
  audio.play().catch(() => {})
}

const completeRemoteSession = async () => {
  if (!activeSessionId.value || mode.value !== 'work') return
  const durationMinutes = Math.max(1, Math.ceil(currentElapsed() / 60))
  try {
    await updateSession(
      activeSessionId.value,
      {
        duration_minutes: durationMinutes,
        mode: mode.value,
        type: 'pomodoro'
      },
      { action: 'complete' }
    )
    window.dispatchEvent(new CustomEvent('study-session-recorded'))
    await loadTodaySummary()
  } catch (error) {
    ElMessage.error(error.message || '记录学习时间失败')
  } finally {
    activeSessionId.value = null
    elapsedSeconds.value = 0
    runStartedAt.value = null
    sessionEndAt.value = null
    isRunning.value = false
    persistActiveSession()
  }
}

const handleBreakCompletion = () => {
  ElMessage.success('休息结束，开始专注！')
  mode.value = 'work'
  initializeTime()
}

const handleTransitionAfterWork = () => {
  completedPomodoros.value++
  ElMessage.success('专注结束，开始休息！')
  mode.value = 'break'
  initializeTime()
}

const handleManualCompletion = async () => {
  if (mode.value === 'work') {
    await completeRemoteSession()
    handleTransitionAfterWork()
  } else {
    handleBreakCompletion()
  }
}

const handleTimerFinished = async () => {
  elapsedSeconds.value = sessionDuration.value
  runStartedAt.value = null
  playCompletionSound()

  if (mode.value === 'work') {
    await completeRemoteSession()
    handleTransitionAfterWork()
  } else {
    handleBreakCompletion()
  }
}

const switchMode = async (newMode) => {
  if (mode.value === newMode) return
  pauseTimer()
  await abortActiveSession()
  mode.value = newMode
  initializeTime()
}

const restoreSession = async () => {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    initializeTime()
    return
  }
  try {
    const saved = JSON.parse(raw)
    if (!saved?.id || !saved.durationSeconds) {
      initializeTime()
      return
    }
    activeSessionId.value = saved.id
    mode.value = saved.mode || mode.value
    sessionDuration.value = saved.durationSeconds
    elapsedSeconds.value = Math.min(saved.elapsedSeconds || 0, sessionDuration.value)
    isRunning.value = Boolean(saved.isRunning) && elapsedSeconds.value < sessionDuration.value
    runStartedAt.value = saved.isRunning && saved.runStartedAt ? new Date(saved.runStartedAt) : null
    sessionEndAt.value = saved.endTimestamp ? new Date(saved.endTimestamp) : null
    updateTimeLeft()
    if (timeLeft.value <= 0 && mode.value === 'work') {
      try {
        await ElMessageBox.confirm(
          '已超过预计时间，是否结束本次？',
          '超时提醒',
          {
            confirmButtonText: '结束本次',
            cancelButtonText: '放弃',
            type: 'warning',
            distinguishCancelAndClose: true
          }
        )
        await handleManualCompletion()
        return
      } catch (error) {
        await abortActiveSession()
        initializeTime()
        return
      }
    }
    if (isRunning.value) {
      beginTicking()
    }
  } catch (error) {
    initializeTime()
    localStorage.removeItem(STORAGE_KEY)
  }
}

const updateSettings = () => {
  if (activeSessionId.value) return
  initializeTime()
}

const beforeUnloadHandler = () => {
  persistActiveSession()
}

onMounted(async () => {
  await Promise.all([restoreSession(), loadTodaySummary()])
  window.addEventListener('beforeunload', beforeUnloadHandler)
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
  clearTimerInterval()
  persistActiveSession()
})

watch(() => store.studySettings.pomodoroWork, (val) => {
  if (typeof val === 'number' && val > 0) {
    workDuration.value = val
    if (mode.value === 'work' && !activeSessionId.value) {
      initializeTime()
    }
  }
})

watch(() => store.studySettings.pomodoroBreak, (val) => {
  if (typeof val === 'number' && val > 0) {
    breakDuration.value = val
    if (mode.value === 'break' && !activeSessionId.value) {
      initializeTime()
    }
  }
})
</script>

<style scoped>
.pomodoro-timer {
  width: 100%;
  background: var(--card-bg);
  border-radius: 10px;
  padding: 16px;
  box-shadow: var(--soft-shadow);
  border: 1px solid rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
}

.timer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.timer-header h3 {
  margin: 0;
  color: var(--nav-active);
  font-size: 18px;
}

.timer-display {
  text-align: center;
  margin: 12px 0;
  padding: 18px 0;
  border-radius: 10px;
  background-color: var(--accent); /* Replace gradient with solid orange */
  color: #fff;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  flex: 1 1 auto; /* allow this area to expand to fill pomodoro height */
}

.timer-display.warning {
  background: var(--accent);
}

/* 专注（橙色）模式下高度加高，数字更突出 */
.timer-display.work {
  padding: 28px 0;
  min-height: 180px;
}
.timer-display.work .time-text {
  font-size: 56px;
}

.time-text {
  font-size: 48px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  line-height: 1;
  margin-bottom: 8px;
  text-shadow: 0 1px 6px rgba(43,50,80,0.10);
}

.mode-text {
  font-size: 14px;
  opacity: 0.95;
}

.timer-controls {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin: 12px 0;
  flex-shrink: 0;
}

.timer-controls button {
  min-width: 88px;
}

.timer-settings {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.04);
  flex-shrink: 0;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
  color: var(--muted);
}

.setting-item span:first-child {
  min-width: 70px;
  text-align: right;
}

.setting-item :deep(.el-input-number) {
  width: 100px;
}

.pomodoro-count {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 12px;
  padding: 8px;
  background: var(--soft-green);
  border: 1px solid var(--soft-green-600);
  border-radius: 6px;
  color: #635a5a; /* 改为黑色文字 */
  font-weight: 500;
}

.pomodoro-count .el-icon {
  font-size: 18px;
}
</style>
