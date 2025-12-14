<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="top-header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo" @click="goToDashboard">
            <div class="logo-icon">
              <el-icon><Reading /></el-icon>
            </div>
            <h2>学习管理系统</h2>
          </div>
          <nav class="nav-menu">
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
          </nav>
        </div>
        
        <div class="header-right">
          <div class="user-info">
            <el-dropdown trigger="click">
              <div class="user-dropdown">
                <el-avatar :size="36" :src="userAvatar" class="user-avatar">
                  {{ username.charAt(0) }}
                </el-avatar>
                <span class="user-name">{{ username }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleProfile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleSettings">
                    <el-icon><Setting /></el-icon>
                    系统设置
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </header>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMainStore } from '../../stores'
import { ElMessage } from 'element-plus'
import {
  HomeFilled,
  Reading,
  Notebook,
  TrendCharts,
  Setting,
  User,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const store = useMainStore()

const username = computed(() => store.userInfo.username || '同学')
const userAvatar = computed(() => store.userInfo.avatar || '')

const goToDashboard = () => {
  router.push('/dashboard')
}

const handleProfile = () => {
  router.push('/settings')
}

const handleSettings = () => {
  router.push('/settings')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  store.setUserInfo({ id: null, username: '', avatar: '' })
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
/* 全局固定宽度设置 */
.main-layout {
  min-height: 100vh;
  background: #f6f7f8;
}

/* 顶部导航栏 - 固定宽度居中 */
.top-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: white;
  border-bottom: 1px solid #e7e7e7;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  z-index: 1000;
}

.header-container {
  width: 1200px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

/* Logo样式 */
.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
}

.logo:hover {
  opacity: 0.8;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon .el-icon {
  font-size: 20px;
  color: white;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  letter-spacing: 0.5px;
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: #666;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
}

.nav-item:hover {
  color: #1890ff;
  background: #f0f7ff;
}

.nav-item.router-link-active {
  color: #1890ff;
  background: #e6f7ff;
  font-weight: 600;
}

.nav-item .el-icon {
  font-size: 16px;
}

/* 用户信息 */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.user-dropdown:hover {
  background: #f5f5f5;
}

.user-avatar {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  font-weight: 600;
  color: white;
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.dropdown-icon {
  color: #999;
  font-size: 12px;
}

/* 主内容区 - 固定宽度 */
.main-content {
  width: 1200px;
  margin: 80px auto 20px;
  min-height: calc(100vh - 100px);
}

.content-wrapper {
  width: 100%;
  margin: 0 auto;
}

/* 移除所有响应式设计，保持固定布局 */
</style>