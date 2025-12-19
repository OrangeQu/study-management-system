<template>
  <div class="main-layout">
    <!-- 侧边栏导航 -->
    <aside class="side-nav">
      <div class="side-container">
        <div class="side-top">
          <div class="logo" @click="goToDashboard">
              <div class="logo-icon">
                <el-icon><Reading /></el-icon>
              </div>
              <h2>学习管理系统</h2>
            </div>

            <div class="side-profile">
              <div class="profile-top">
                <el-avatar :size="80" :src="userAvatar" class="profile-avatar">
                  {{ username.charAt(0) }}
                </el-avatar>
                <div class="profile-name">{{ username }}</div>
              </div>

              <!-- calendar moved to sidebar bottom -->

              <div class="profile-stats profile-stats-inline">
                <div class="stat-item">
                  <el-icon class="stat-icon"><Clock /></el-icon>
                  <div>
                    <div class="stat-value">{{ todayStudyTime }} 分钟</div>
                    <div class="stat-label">今日学习时长</div>
                  </div>
                </div>

                <div class="stat-item">
                  <el-icon class="stat-icon"><Checked /></el-icon>
                  <div>
                    <div class="stat-value">{{ completedTasks }}/{{ totalTasks }}</div>
                    <div class="stat-label">已完成任务</div>
                  </div>
                </div>
              </div>
            </div>

            <nav class="nav-menu-vertical">
            <router-link to="/dashboard" class="nav-item">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </router-link>
            <router-link to="/study-center" class="nav-item">
              <el-icon><Notebook /></el-icon>
              <span>学习中心</span>
            </router-link>
            <router-link to="/grade-data" class="nav-item">
              <el-icon><TrendCharts /></el-icon>
              <span>绩点数据</span>
            </router-link>
            <router-link to="/settings" class="nav-item">
              <el-icon><Setting /></el-icon>
              <span>设置</span>
            </router-link>
            <router-link to="/admin/users" class="nav-item">
              <el-icon><List /></el-icon>
              <span>管理端</span>
            </router-link>
          </nav>
          
          <!-- 日历移动到侧边栏最后 -->
          <div class="calendar-mini">
            <div class="calendar-header">{{ monthLabel }}</div>
            <div class="calendar-grid">
              <div class="weekday" v-for="w in weekdays" :key="w">{{ w }}</div>
              <div
                v-for="(d, idx) in daysGrid"
                :key="idx"
                class="calendar-day"
                :class="{ 'empty': d === null, 'today': d === todayNumber }"
              >
                <span v-if="d">{{ d }}</span>
              </div>
            </div>
          </div>
        </div>

        
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { useMainStore } from '../../stores'

import {
  HomeFilled,
  Reading,
  Notebook,
  TrendCharts,
  Setting,
  Clock,
  Checked
} from '@element-plus/icons-vue'
import { todayStats as apiTodayStats } from '@/api/study'

const router = useRouter()
const store = useMainStore()

const username = computed(() => store.userInfo.username || '同学')
const userAvatar = computed(() => store.userInfo.avatar || '')

const todayStudyTime = ref(0)

const completedTasks = computed(() => store.todayTasks.filter(t => t.completed).length)
const totalTasks = computed(() => store.todayTasks.length)

const monthLabel = computed(() => dayjs().format('MMMM YYYY'))
const todayNumber = Number(dayjs().format('D'))
const weekdays = ['日','一','二','三','四','五','六']

const daysGrid = computed(() => {
  const start = dayjs().startOf('month').day() // 0-6 (Sun-Sat)
  const total = dayjs().daysInMonth()
  const arr = []
  for (let i = 0; i < start; i++) arr.push(null)
  for (let d = 1; d <= total; d++) arr.push(d)
  return arr
})

const loadTodayStats = async () => {
  try {
    const resp = await apiTodayStats()
    todayStudyTime.value = resp.data?.data?.studyMinutes || 0
  } catch (e) {
    todayStudyTime.value = 0
  }
}

const goToDashboard = () => {
  router.push('/dashboard')
}

onMounted(() => {
  loadTodayStats()
})
</script>

<style scoped>
/* 全局固定宽度设置 */
.main-layout {
  min-height: 100vh;
  background: #ffffff; /* 主背景改为纯白，侧边栏保持自身样式 */
  /* 移除固定最小总宽，允许在小视口或浏览器放大时自适应避免横向滚动 */
}

/* 顶部导航栏 - 固定宽度居中 */
.side-nav {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 300px;
  background: linear-gradient(180deg, var(--nav-bg), #c6c5c543 60%);
  border-right: 1px solid rgba(0,0,0,0.04);
  box-shadow: var(--soft-shadow);
  z-index: 1000;
  backdrop-filter: blur(6px);
  overflow: auto;
}

.side-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px 18px;
}

.side-top {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.nav-menu-vertical {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 侧边栏个人信息卡 */
.side-profile {
  padding: 10px 0;
  border-radius: 10px;
}

.profile-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.profile-avatar {
  box-shadow: 0 6px 18px rgba(43,50,80,0.06);
}

.profile-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--nav-active);
}

.profile-date {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 10px;
}

.profile-stats {
  display: flex;
  gap: 12px;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 100%;
}

/* 让数值与标签居中对齐 */
.profile-stats .stat-item > div {
  text-align: center;
}

.profile-avatar {
  box-shadow: 0 8px 24px rgba(43,50,80,0.08);
}

/* 内联模式（侧边栏统计） */
.profile-stats-inline .stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-stats-inline .stat-block {
  display: flex;
  flex-direction: column;
}

.stat-item {
  display: flex;
  gap: 8px;
  align-items: center;
}

.stat-icon {
  font-size: 18px;
  color: var(--primary);
}

.stat-value {
  font-weight: 700;
  color: var(--nav-active);
}

.stat-label {
  font-size: 12px;
  color: var(--muted);
}

/* 小日历 */
.calendar-mini {
  width: 100%;
  border-radius: 8px;
  padding: 8px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(43,50,80,0.04);
  margin-bottom: 8px;
}

.calendar-header {
  font-size: 13px;
  font-weight: 700;
  color: var(--nav-active);
  text-align: center;
  padding-bottom: 6px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  font-size: 12px;
}

.calendar-grid .weekday {
  text-align: center;
  color: var(--muted);
}

.calendar-day {
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  color: #666;
}

.calendar-day.empty {
  background: transparent;
}

.calendar-day.today {
  background: linear-gradient(90deg, var(--primary), var(--accent));
  color: #fff;
  font-weight: 700;
}

/* Logo样式 */
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
}

.logo:hover {
  opacity: 0.98;
}

.logo-icon {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon .el-icon {
  font-size: 20px;
  color: white;
}

.logo h2 {
  font-size: 16px;
  color: var(--nav-active);
  font-weight: 700;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 70px; /* 增加左侧与右侧内边距，向内缩进图标与文字 */
  color: var(--muted);
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.15s;
  font-size: 14px;
  font-weight: 600;
  position: relative;
}

.nav-item .el-icon {
  font-size: 16px;
  color: rgba(0,0,0,0.45);
}

.nav-item:hover {
  color: var(--nav-active);
  background: rgba(44,199,183,0.06);
}

.nav-item.router-link-active {
  color: var(--nav-active);
  background: linear-gradient(90deg, rgba(44,199,183,0.08), rgba(251,140,74,0.04));
}

.nav-item.router-link-active::before {
  content: '';
  position: absolute;
  left: -16px; /* 微调高亮条位置以匹配更大内边距 */
  top: 8px;
  bottom: 8px;
  width: 4px;
  border-radius: 4px;
  background: linear-gradient(180deg, var(--primary), var(--accent));
}

/* 用户信息 */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.user-dropdown:hover {
  background: rgba(43,50,80,0.03);
}

.user-avatar {
  background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
  font-weight: 700;
  color: white;
}

.user-name {
  font-size: 14px;
  color: var(--nav-active);
  font-weight: 600;
}

.dropdown-icon {
  color: rgba(0,0,0,0.35);
  font-size: 12px;
}

/* 主内容区 - 固定宽度 */
.main-content {
  /* 主区为纯白背景，侧边栏样式不变 */
  margin-left: 300px;
  padding: 28px 20px;
  min-height: calc(100vh - 56px);
  box-sizing: border-box;
  background: #ffffff;
}

.content-wrapper {
  width: 100%;
  max-width: 1200px; /* 在大屏幕保持 1200px，缩小时按比例收缩以避免水平滚动 */
  margin: 0 auto;
  padding: 0 12px;
  box-sizing: border-box;
  background: #ffffff;
}

/* 侧边栏顶部：确保头像与名字两行显示并居中 */
.profile-top {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 12px;
  text-align: center;
}

.profile-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--nav-active);
  line-height: 1.1;
  display: block;
  margin-top: 4px;
}

/* 移除所有响应式设计，保持固定布局 */
</style>