
<template>
  <div class="settings-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>个人设置</h1>
      <div class="header-subtitle">管理您的个人化学习设置</div>
    </div>

    <div class="settings-container">
      <!-- 左侧导航 -->
      <div class="settings-nav">
        <el-menu
          :default-active="activeTab"
          class="settings-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          
          <el-menu-item index="account">
            <el-icon><Setting /></el-icon>
            <span>账户安全</span>
          </el-menu-item>
          
          <el-menu-item index="appearance">
            <el-icon><Brush /></el-icon>
            <span>外观设置</span>
          </el-menu-item>
          
          <el-menu-item index="study">
            <el-icon><Reading /></el-icon>
            <span>学习偏好</span>
          </el-menu-item>
          
          <el-menu-item index="data">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据管理</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容区域 -->
      <div class="settings-content">
        <!-- 个人信息 -->
        <div v-if="activeTab === 'profile'" class="settings-section">
          <div class="section-header">
            <h2>个人信息</h2>
            <div class="section-description">设置您的个人资料</div>
          </div>
          
          <div class="profile-container">
            <!-- 头像上传 -->
            <div class="avatar-section">
              <div class="avatar-upload">
                <el-avatar :size="100" :src="userInfo.avatar" class="user-avatar">
                  {{ userInfo.username?.charAt(0) || '用' }}
                </el-avatar>
                <div class="avatar-actions">
                  <el-button size="small" @click="uploadAvatar" :icon="Upload">
                    更换头像
                  </el-button>
                  <input
                    ref="avatarInput"
                    type="file"
                    accept="image/*"
                    style="display: none"
                    @change="handleAvatarChange"
                  />
                  <div class="avatar-tip">支持 JPG、PNG 格式，小于 2MB</div>
                </div>
              </div>
            </div>

            <!-- 个人信息表单 -->
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              class="profile-form"
            >
              <div class="form-section">
                <h3>基本信息</h3>
                <el-form-item label="用户名" prop="username">
                  <el-input
                    v-model="profileForm.username"
                    placeholder="请输入用户名"
                    style="width: 300px"
                  />
                </el-form-item>
                
                <el-form-item label="昵称" prop="nickname">
                  <el-input
                    v-model="profileForm.nickname"
                    placeholder="请输入昵称"
                    style="width: 300px"
                  />
                </el-form-item>
                
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="profileForm.gender">
                    <el-radio label="male">男</el-radio>
                    <el-radio label="female">女</el-radio>
                    <el-radio label="other">其他</el-radio>
                  </el-radio-group>
                </el-form-item>
              </div>

              <div class="form-section">
                <h3>联系信息</h3>
                <el-form-item label="手机号码" prop="phone">
                  <el-input
                    v-model="profileForm.phone"
                    placeholder="请输入手机号码"
                    style="width: 300px"
                  />
                </el-form-item>
                
                <el-form-item label="电子邮箱" prop="email">
                  <el-input
                    v-model="profileForm.email"
                    placeholder="请输入电子邮箱"
                    style="width: 300px"
                  />
                </el-form-item>
              </div>

              <div class="form-actions">
                <el-button type="primary" @click="saveProfile" :loading="savingProfile">
                  保存更改
                </el-button>
                <el-button @click="resetProfileForm">重置</el-button>
              </div>
            </el-form>
          </div>
        </div>

        <!-- 账户安全 -->
        <div v-else-if="activeTab === 'account'" class="settings-section">
          <div class="section-header">
            <h2>账户安全</h2>
            <div class="section-description">管理您的账户安全设置</div>
          </div>
          
          <div class="security-container">
            <!-- 密码修改 -->
            <div class="security-card">
              <div class="card-header">
                <div class="card-title">
                  <el-icon><Lock /></el-icon>
                  <h3>修改密码</h3>
                </div>
                <el-button type="primary" size="small" @click="showPasswordDialog">
                  修改密码
                </el-button>
              </div>
              
              <div class="card-content">
                <p>定期修改密码可以增强账户安全性</p>
                <div class="security-info">
                  <span>上次修改时间：{{ lastPasswordChange }}</span>
                  <el-tag type="success" size="small">已启用</el-tag>
                </div>
              </div>
            </div>

            <!-- 登录设备管理 -->
            <div class="security-card">
              <div class="card-header">
                <div class="card-title">
                  <el-icon><Monitor /></el-icon>
                  <h3>登录设备管理</h3>
                </div>
                <el-button size="small" @click="viewLoginDevices">
                  查看设备
                </el-button>
              </div>
              
              <div class="card-content">
                <p>管理已登录的设备，确保账户安全</p>
                <div class="device-list">
                  <div class="device-item" v-for="device in loginDevices" :key="device.id">
                    <div class="device-info">
                      <div class="device-name">
                        <el-icon><Monitor /></el-icon>
                        {{ device.deviceName }}
                      </div>
                      <div class="device-details">
                        <span>{{ device.location }}</span>
                        <span>{{ device.lastLogin }}</span>
                      </div>
                    </div>
                    <el-button
                      v-if="!device.isCurrent"
                      type="text"
                      size="small"
                      @click="logoutDevice(device.id)"
                    >
                      退出登录
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 外观设置 -->
        <div v-else-if="activeTab === 'appearance'" class="settings-section">
          <div class="section-header">
            <h2>外观设置</h2>
            <div class="section-description">自定义您的界面外观</div>
          </div>
          
          <div class="appearance-container">
            <!-- 主题颜色 - 减少到4种 -->
            <div class="appearance-card">
              <div class="card-header">
                <h3>主题颜色</h3>
                <div class="current-theme">
                  当前主题：<span class="theme-name">{{ currentThemeName }}</span>
                </div>
              </div>
              
              <div class="theme-colors">
                <div
                  v-for="theme in themes"
                  :key="theme.name"
                  class="theme-item"
                  :class="{ active: theme.name === currentTheme }"
                  @click="changeTheme(theme.name)"
                >
                  <div class="theme-preview" :style="{ background: theme.color }">
                    <el-icon v-if="theme.name === currentTheme">
                      <Check />
                    </el-icon>
                  </div>
                  <div class="theme-info">
                    <div class="theme-title">{{ theme.title }}</div>
                    <div class="theme-desc">{{ theme.description }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 界面设置 -->
            <div class="appearance-card">
              <div class="card-header">
                <h3>界面设置</h3>
              </div>
              
              <div class="interface-settings">
                <div class="setting-item">
                  <span class="setting-label">导航栏固定</span>
                  <el-switch v-model="fixedHeader" />
                  <div class="setting-desc">滚动时保持导航栏在顶部</div>
                </div>
                
                <div class="setting-item">
                  <span class="setting-label">显示面包屑</span>
                  <el-switch v-model="showBreadcrumb" />
                  <div class="setting-desc">显示页面路径导航</div>
                </div>
                
                <div class="setting-item">
                  <span class="setting-label">标签页模式</span>
                  <el-switch v-model="enableTagsView" />
                  <div class="setting-desc">启用多标签页功能</div>
                </div>
                
                <div class="setting-item">
                  <span class="setting-label">动画效果</span>
                  <el-switch v-model="enableAnimation" />
                  <div class="setting-desc">启用页面切换动画</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 学习偏好 -->
        <div v-else-if="activeTab === 'study'" class="settings-section">
          <div class="section-header">
            <h2>学习偏好</h2>
            <div class="section-description">个性化您的学习体验</div>
          </div>
          
          <div class="study-container">
            <!-- 番茄钟设置 -->
            <div class="study-card">
              <div class="card-header">
                <h3>番茄钟设置</h3>
              </div>
              
              <div class="pomodoro-settings">
                <div class="setting-item">
                  <span class="setting-label">专注时长</span>
                  <el-input-number
                    v-model="studySettings.pomodoroWork"
                    :min="5"
                    :max="60"
                    :step="5"
                    controls-position="right"
                  />
                  <span class="setting-unit">分钟</span>
                </div>
                
                <div class="setting-item">
                  <span class="setting-label">休息时长</span>
                  <el-input-number
                    v-model="studySettings.pomodoroBreak"
                    :min="5"
                    :max="30"
                    :step="5"
                    controls-position="right"
                  />
                  <span class="setting-unit">分钟</span>
                </div>
              </div>
            </div>

            <!-- 任务提醒 -->
            <div class="study-card">
              <div class="card-header">
                <h3>任务提醒设置</h3>
              </div>
              
              <div class="reminder-settings">
                <div class="setting-item">
                  <span class="setting-label">任务截止提醒</span>
                  <el-select v-model="studySettings.taskReminder" style="width: 150px">
                    <el-option label="不提醒" value="none" />
                    <el-option label="提前1小时" value="1h" />
                    <el-option label="提前3小时" value="3h" />
                    <el-option label="提前1天" value="1d" />
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 学习目标 -->
            <div class="study-card">
              <div class="card-header">
                <h3>学习目标设置</h3>
              </div>
              
              <div class="goal-settings">
                <div class="setting-item">
                  <span class="setting-label">每日学习目标</span>
                  <el-input-number
                    v-model="studySettings.dailyGoal"
                    :min="1"
                    :max="8"
                    controls-position="right"
                  />
                  <span class="setting-unit">小时</span>
                </div>
                
                <div class="setting-item">
                  <span class="setting-label">每周学习目标</span>
                  <el-input-number
                    v-model="studySettings.weeklyGoal"
                    :min="10"
                    :max="40"
                    controls-position="right"
                  />
                  <span class="setting-unit">小时</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 数据管理 -->
        <div v-else-if="activeTab === 'data'" class="settings-section">
          <div class="section-header">
            <h2>数据管理</h2>
            <div class="section-description">管理您的学习数据</div>
          </div>
          
          <div class="data-container">
            <!-- 数据导出 -->
            <div class="data-card">
              <div class="card-header">
                <div class="card-title">
                  <el-icon><Download /></el-icon>
                  <h3>数据导出</h3>
                </div>
              </div>
              
              <div class="card-content">
                <p>导出您的学习数据，包括成绩、任务、学习记录等</p>
                <div class="export-options">
                  <el-button @click="exportData('all')" :icon="Download">
                    导出全部数据
                  </el-button>
                  <el-button @click="exportData('grades')" :icon="DataAnalysis">
                    仅导出成绩数据
                  </el-button>
                  <el-button @click="exportData('tasks')" :icon="List">
                    仅导出任务数据
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 数据清理 -->
            <div class="data-card">
              <div class="card-header">
                <div class="card-title">
                  <el-icon><Delete /></el-icon>
                  <h3>数据清理</h3>
                </div>
              </div>
              
              <div class="card-content">
                <p>清理已完成的任务和历史记录</p>
                <div class="delete-options">
                  <el-button @click="clearCompletedTasks" :icon="Delete">
                    清理已完成任务
                  </el-button>
                  <el-button @click="clearHistory" :icon="Clock">
                    清理历史记录
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="400px"
      @close="resetPasswordForm"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="passwordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="changingPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Setting,
  Brush,
  Reading,
  DataAnalysis,
  Upload,
  Lock,
  Monitor,
  Check,
  Download,
  Delete,
  List,
  Clock
} from '@element-plus/icons-vue'
import { useMainStore } from '@/stores'
import {
  fetchProfile,
  updateProfile as updateProfileApi,
  changePassword as changePasswordApi,
  uploadAvatar as uploadAvatarApi,
  listDevices,
  removeDevice
} from '@/api/user'
import { getPreferences, savePreferences as savePreferencesApi } from '@/api/settings'
import { deleteCompletedTasks } from '@/api/tasks'
import { exportData as exportDataApi, clearHistory as clearHistoryApi } from '@/api/data'

const store = useMainStore()
const activeTab = ref('profile')
const savingProfile = ref(false)
const changingPassword = ref(false)
const showPasswordDialog = ref(false)
const savingPreferences = ref(false)
const avatarInput = ref(null)

const userInfo = ref({
  username: store.userInfo.username || '',
  nickname: store.userInfo.nickname || '',
  avatar: store.userInfo.avatar || '',
  gender: store.userInfo.gender || 'male',
  phone: store.userInfo.phone || '',
  email: store.userInfo.email || ''
})

const profileFormRef = ref(null)
const profileForm = ref({ ...userInfo.value })

const profileRules = {
  username: [
    { required: true, message: '??????', trigger: 'blur' },
    { min: 2, max: 10, message: '??????2-10???', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '??????????', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '?????????', trigger: 'blur' }
  ]
}

const passwordFormRef = ref(null)
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  currentPassword: [
    { required: true, message: '???????', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '??????', trigger: 'blur' },
    { min: 6, max: 20, message: '?????6-20???', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '??????', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('??????????'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const themes = ref([
  { name: 'blue', title: '????', description: '??????', color: '#1890ff' },
  { name: 'green', title: '????', description: '????', color: '#52c41a' },
  { name: 'purple', title: '????', description: '????', color: '#722ed1' },
  { name: 'orange', title: '????', description: '????', color: '#fa8c16' }
])

const currentTheme = ref(store.preferences.theme || 'blue')
const fixedHeader = ref(store.preferences.fixedHeader ?? true)
const showBreadcrumb = ref(store.preferences.showBreadcrumb ?? true)
const enableTagsView = ref(store.preferences.enableTagsView ?? true)
const enableAnimation = ref(store.preferences.enableAnimation ?? true)

const studySettings = ref({
  pomodoroWork: store.studySettings.pomodoroWork,
  pomodoroBreak: store.studySettings.pomodoroBreak,
  taskReminder: store.studySettings.taskReminder,
  dailyGoal: store.studySettings.dailyGoal,
  weeklyGoal: store.studySettings.weeklyGoal
})

const loginDevices = ref([])
let preferencesReady = false
let preferenceTimer = null

const lastPasswordChange = computed(() => userInfo.value.updated_at || '????')

const currentThemeName = computed(() => {
  const theme = themes.value.find(t => t.name === currentTheme.value)
  return theme ? theme.title : '????'
})

const buildPreferencesPayload = () => ({
  theme: currentTheme.value,
  fixedHeader: fixedHeader.value,
  showBreadcrumb: showBreadcrumb.value,
  enableTagsView: enableTagsView.value,
  enableAnimation: enableAnimation.value,
  studySettings: { ...studySettings.value }
})

const schedulePreferencesSave = () => {
  if (!preferencesReady) return
  store.setPreferences(buildPreferencesPayload())
  clearTimeout(preferenceTimer)
  preferenceTimer = setTimeout(async () => {
    try {
      savingPreferences.value = true
      await savePreferencesApi(buildPreferencesPayload())
    } catch (error) {
      console.error(error)
    } finally {
      savingPreferences.value = false
    }
  }, 600)
}

watch([currentTheme, fixedHeader, showBreadcrumb, enableTagsView, enableAnimation], () => {
  schedulePreferencesSave()
})

watch(studySettings, () => {
  schedulePreferencesSave()
}, { deep: true })

const handleMenuSelect = (key) => {
  activeTab.value = key
}

const uploadAvatar = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('????????2MB')
    return
  }
  try {
    const resp = await uploadAvatarApi(file)
    const avatar = resp.data?.data?.avatar
    if (avatar) {
      userInfo.value.avatar = avatar
      profileForm.value.avatar = avatar
      store.setUserInfo({ ...store.userInfo, avatar })
      ElMessage.success('??????')
    }
  } catch (error) {
    ElMessage.error(error.message || '??????')
  } finally {
    event.target.value = ''
  }
}

const saveProfile = async () => {
  if (!profileFormRef.value) return
  try {
    await profileFormRef.value.validate()
    savingProfile.value = true
    await updateProfileApi(profileForm.value)
    userInfo.value = { ...profileForm.value }
    store.setUserInfo(userInfo.value)
    ElMessage.success('????????')
  } catch (error) {
    ElMessage.error(error.message || '??????????')
  } finally {
    savingProfile.value = false
  }
}

const resetProfileForm = () => {
  profileForm.value = { ...userInfo.value }
  profileFormRef.value?.clearValidate()
}

const resetPasswordForm = () => {
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordFormRef.value?.clearValidate()
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    await changePasswordApi(passwordForm.value)
    ElMessage.success('??????')
    showPasswordDialog.value = false
    resetPasswordForm()
  } catch (error) {
    ElMessage.error(error.message || '??????')
  } finally {
    changingPassword.value = false
  }
}

const changeTheme = (themeName) => {
  currentTheme.value = themeName
  ElMessage.success('主题已切换')
}

const viewLoginDevices = async () => {
  await loadDevices()
  ElMessage.info('???????')
}

const logoutDevice = (deviceId) => {
  ElMessageBox.confirm(
    '?????????????',
    '??????',
    {
      confirmButtonText: '??',
      cancelButtonText: '??',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await removeDevice(deviceId)
      await loadDevices()
      ElMessage.success('???????')
    } catch (error) {
      ElMessage.error(error.message || '????')
    }
  })
}

const exportData = async (type) => {
  try {
    const resp = await exportDataApi({ type })
    const blob = new Blob([resp.data], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `study-data-${type || 'all'}.json`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('??????')
  } catch (error) {
    ElMessage.error(error.message || '????')
  }
}

const clearCompletedTasks = () => {
  ElMessageBox.confirm(
    '??????????????',
    '????',
    {
      confirmButtonText: '??',
      cancelButtonText: '??',
      type: 'info'
    }
  ).then(async () => {
    try {
      await deleteCompletedTasks()
      ElMessage.success('????????')
    } catch (error) {
      ElMessage.error(error.message || '????')
    }
  })
}

const clearHistory = () => {
  ElMessageBox.confirm(
    '?????????????',
    '????',
    {
      confirmButtonText: '??',
      cancelButtonText: '??',
      type: 'info'
    }
  ).then(async () => {
    try {
      await clearHistoryApi({ type: 'study' })
      ElMessage.success('???????')
    } catch (error) {
      ElMessage.error(error.message || '????')
    }
  })
}

const loadProfile = async () => {
  try {
    const resp = await fetchProfile()
    userInfo.value = resp.data?.data || userInfo.value
    profileForm.value = { ...userInfo.value }
    store.setUserInfo(userInfo.value)
  } catch (error) {
    ElMessage.error(error.message || '????????')
  }
}

const loadPreferences = async () => {
  try {
    const resp = await getPreferences()
    const prefs = resp.data?.data || {}
    preferencesReady = false
    const merged = {
      theme: prefs.theme ?? currentTheme.value,
      fixedHeader: prefs.fixedHeader ?? fixedHeader.value,
      showBreadcrumb: prefs.showBreadcrumb ?? showBreadcrumb.value,
      enableTagsView: prefs.enableTagsView ?? enableTagsView.value,
      enableAnimation: prefs.enableAnimation ?? enableAnimation.value,
      studySettings: {
        ...studySettings.value,
        ...(prefs.studySettings || {})
      }
    }
    currentTheme.value = merged.theme
    fixedHeader.value = merged.fixedHeader
    showBreadcrumb.value = merged.showBreadcrumb
    enableTagsView.value = merged.enableTagsView
    enableAnimation.value = merged.enableAnimation
    studySettings.value = { ...merged.studySettings }
    store.setPreferences(merged)
  } catch (error) {
    console.error(error)
  } finally {
    preferencesReady = true
  }
}

const loadDevices = async () => {
  try {
    const resp = await listDevices()
    loginDevices.value = resp.data?.data || []
  } catch (error) {
    loginDevices.value = []
  }
}

onMounted(async () => {
  await Promise.all([loadProfile(), loadPreferences(), loadDevices()])
})
</script>


<style scoped>
/* 固定宽度布局 */
.settings-page {
  width: 1200px;
  margin: 0 auto;
  padding: 20px 0;
  min-height: 100vh;
}

/* 页面标题 */
.page-header {
  margin-bottom: 30px;
  padding: 0 10px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #333;
}

.header-subtitle {
  font-size: 14px;
  color: #666;
}

/* 设置容器 */
.settings-container {
  display: flex;
  gap: 20px;
  width: 100%;
}

/* 左侧导航 */
.settings-nav {
  width: 220px;
  flex-shrink: 0;
}

.settings-menu {
  border-right: none;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.settings-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  font-size: 14px;
}

.settings-menu .el-menu-item.is-active {
  color: #1890ff;
  background-color: #f0f9ff;
}

/* 右侧内容 */
.settings-content {
  flex: 1;
  min-width: 0;
}

.settings-section {
  background: white;
  border-radius: 8px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  width: 100%;
}

/* 区域头部 */
.section-header {
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h2 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #333;
}

.section-description {
  font-size: 14px;
  color: #666;
}

/* 个人信息 */
.profile-container {
  width: 100%;
}

.avatar-section {
  margin-bottom: 30px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 25px;
}

.user-avatar {
  border: 2px solid #f0f0f0;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-tip {
  font-size: 12px;
  color: #999;
}

/* 表单 */
.profile-form {
  width: 100%;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f5f5f5;
}

.form-section h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #333;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f5f5f5;
}

/* 安全卡片 */
.security-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.security-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  background: #fafafa;
}

.security-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 0;
  border: none;
}

.security-card .card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.security-card .card-title h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.security-card .card-content p {
  margin: 0 0 10px 0;
  color: #666;
}

.security-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #666;
}

/* 设备列表 */
.device-list {
  margin-top: 15px;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  margin-bottom: 10px;
}

.device-info {
  flex: 1;
}

.device-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  margin-bottom: 4px;
}

.device-details {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

/* 外观设置 */
.appearance-container {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.appearance-card {
  width: 100%;
}

.appearance-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0;
  border: none;
}

.appearance-card .card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.current-theme {
  font-size: 14px;
  color: #666;
}

.theme-name {
  color: #1890ff;
  font-weight: 500;
}

/* 主题颜色 - 改为2列布局 */
.theme-colors {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.theme-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.theme-item:hover {
  border-color: #1890ff;
  background: #fafafa;
}

.theme-item.active {
  border-color: #1890ff;
  background: #f0f9ff;
}

.theme-preview {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.theme-info {
  flex: 1;
}

.theme-title {
  font-weight: 500;
  margin-bottom: 4px;
  color: #333;
}

.theme-desc {
  font-size: 12px;
  color: #666;
}

/* 界面设置 */
.interface-settings {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.setting-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-label {
  width: 120px;
  font-size: 14px;
  color: #333;
}

.setting-desc {
  flex: 1;
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

/* 学习设置 */
.study-container {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.study-card {
  width: 100%;
}

.pomodoro-settings,
.reminder-settings,
.goal-settings {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.setting-unit {
  margin-left: 8px;
  color: #666;
}

/* 数据管理 */
.data-container {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.data-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
}

.data-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 0;
  border: none;
}

.data-card .card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.data-card .card-title h3 {
  margin: 0;
  font-size: 16px;
}

.data-card .card-content p {
  margin: 0 0 15px 0;
  color: #666;
}

.export-options,
.delete-options {
  display: flex;
  gap: 10px;
}
</style>
