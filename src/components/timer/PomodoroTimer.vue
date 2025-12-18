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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { recordSession } from '@/api/study'

// 计时器状态
const mode = ref('work') // work, break
const isRunning = ref(false)
const timeLeft = ref(0) // 以秒为单位
const timerInterval = ref(null)

// 设置
const workDuration = ref(25)
const breakDuration = ref(5)
const completedPomodoros = ref(0)
const sessionStart = ref(null)

// 计算属性
const minutes = computed(() => Math.floor(timeLeft.value / 60))
const seconds = computed(() => timeLeft.value % 60)

const modeText = computed(() => {
  const texts = {
    work: '专注时间',
    break: '休息时间'
  }
  return texts[mode.value]
})

// 初始化时间
const initializeTime = () => {
  switch(mode.value) {
    case 'work':
      timeLeft.value = workDuration.value * 60
      break
    case 'break':
      timeLeft.value = breakDuration.value * 60
      break
  }
}

// 切换模式
const switchMode = (newMode) => {
  resetTimer()
  mode.value = newMode
  initializeTime()
  sessionStart.value = null
}

// 切换计时器状态
const toggleTimer = () => {
  if (isRunning.value) {
    pauseTimer()
  } else {
    startTimer()
  }
}

// 开始计时
const startTimer = () => {
  if (timeLeft.value <= 0) {
    ElMessage.warning('时间已到！请切换模式或重置')
    return
  }
  
  isRunning.value = true
  sessionStart.value = new Date()
  timerInterval.value = setInterval(async () => {
    timeLeft.value--
    
    if (timeLeft.value <= 0) {
      clearInterval(timerInterval.value)
      isRunning.value = false
      await handleSessionComplete(mode.value)
      playCompletionSound()
      
      if (mode.value === 'work') {
        completedPomodoros.value++
        ElMessage.success('专注时间结束，开始休息！')
        mode.value = 'break'
      } else {
        ElMessage.success('休息结束，开始专注！')
        mode.value = 'work'
      }
      
      initializeTime()
    }
  }, 1000)
}

// 暂停计时
const pauseTimer = () => {
  isRunning.value = false
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
    timerInterval.value = null
  }
}

// 重置计时
const resetTimer = () => {
  pauseTimer()
  initializeTime()
  sessionStart.value = null
}

// 播放完成声音
const playCompletionSound = () => {
  const audio = new Audio('https://assets.mixkit.co/sfx/preview/mixkit-correct-answer-tone-2870.mp3')
  audio.play().catch(e => console.log('音频播放失败:', e))
}

// 更新设置
const updateSettings = () => {
  resetTimer()
}

const handleSessionComplete = async (finishedMode) => {
  const duration = finishedMode === 'work' ? workDuration.value : breakDuration.value
  try {
    await recordSession({
      type: 'pomodoro',
      mode: finishedMode,
      duration_minutes: duration,
      started_at: sessionStart.value ? sessionStart.value.toISOString() : undefined
    })
    window.dispatchEvent(new CustomEvent('study-session-recorded'))
  } catch (error) {
    ElMessage.error(error.message || '记录学习时长失败')
  } finally {
    sessionStart.value = null
  }
}

// 初始化
onMounted(() => {
  // 从localStorage加载设置
  const savedSettings = localStorage.getItem('pomodoroSettings')
  if (savedSettings) {
    const settings = JSON.parse(savedSettings)
    workDuration.value = settings.workDuration || 25
    breakDuration.value = settings.breakDuration || 5
    completedPomodoros.value = settings.completedPomodoros || 0
  }
  
  initializeTime()
})

// 保存设置
onUnmounted(() => {
  const settings = {
    workDuration: workDuration.value,
    breakDuration: breakDuration.value,
    completedPomodoros: completedPomodoros.value
  }
  localStorage.setItem('pomodoroSettings', JSON.stringify(settings))
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
  color: var(--soft-green-600);
  font-weight: 500;
}

.pomodoro-count .el-icon {
  font-size: 18px;
}
</style>
