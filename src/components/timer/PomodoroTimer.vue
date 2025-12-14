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
    
    <div class="timer-display" :class="{ 'warning': minutes < 5 && minutes > 0 }">
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
  height: 100%;
}

.pomodoro-timer {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.timer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-shrink: 0;
}

.timer-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.timer-display {
  text-align: center;
  margin: 20px 0;
  padding: 30px 0;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transition: all 0.3s;
  flex-shrink: 0;
}

.timer-display.warning {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa726 100%);
}

.time-text {
  font-size: 60px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  line-height: 1;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.mode-text {
  font-size: 16px;
  opacity: 0.9;
}

.timer-controls {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin: 20px 0;
  flex-shrink: 0;
}

.timer-controls button {
  min-width: 100px;
}

.timer-settings {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 15px;
  color: #666;
}

.setting-item span:first-child {
  min-width: 80px;
  text-align: right;
}

.setting-item :deep(.el-input-number) {
  width: 100px;
}

.pomodoro-count {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
  padding: 12px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  color: #52c41a;
  font-weight: 500;
}

.pomodoro-count .el-icon {
  font-size: 18px;
}
</style>
