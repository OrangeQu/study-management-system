
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
          mode="horizontal"
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
                <el-button type="primary" size="small" @click="showPasswordDialog = true">
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
                </div>
              </div>
            </div>
            </div>

            <!-- 退出当前账号 -->
            <div class="security-card">
              <div class="card-header">
                <div class="card-title">
                  <el-icon><SwitchButton /></el-icon>
                  <h3>退出当前账号</h3>
                </div>
                <el-button
                  type="danger"
                  plain
                  size="small"
                  :loading="loggingOut"
                  @click="handleLogout"
                >
                  退出登录
                </el-button>
              </div>

              <div class="card-content">
                <p>退出后需要重新登录才能继续访问账户数据。</p>
                <div class="security-info">
                  <span>当前账号：{{ userInfo.username || '未登录' }}</span>
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
import { useRouter } from 'vue-router'
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
  Clock,
  SwitchButton
} from '@element-plus/icons-vue'
import { useMainStore } from '@/stores'
import {
  fetchProfile,
  updateProfile as updateProfileApi,
  changePassword as changePasswordApi,
  uploadAvatar as uploadAvatarApi,
  listDevices
} from '@/api/user'
import { logout as logoutApi } from '@/api/auth'
import { getPreferences, savePreferences as savePreferencesApi } from '@/api/settings'
import { deleteCompletedTasks } from '@/api/tasks'
import { exportData as exportDataApi, clearHistory as clearHistoryApi } from '@/api/data'

const store = useMainStore()
const router = useRouter()
const activeTab = ref('profile')
const savingProfile = ref(false)
const changingPassword = ref(false)
const showPasswordDialog = ref(false)
const savingPreferences = ref(false)
const avatarInput = ref(null)
const loggingOut = ref(false)

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
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 2, max: 10, message: '用户名长度需 2-10 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
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
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '新密码需 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const THEME_PRESETS = {
  blue: {
    primary: '#2cc7b7',
    accent: '#fb8c4a',
    pageBg: '#f6f7f9',
    navBg: '#f4f6f7',
    navActive: '#2b3250',
    cardBg: '#ffffff'
  },
  green: {
    primary: '#36c187',
    accent: '#ffb74d',
    pageBg: '#f3f9f4',
    navBg: '#e6f4ec',
    navActive: '#1f3c2e',
    cardBg: '#ffffff'
  },
  purple: {
    primary: '#7f6dff',
    accent: '#ff9acb',
    pageBg: '#f8f6ff',
    navBg: '#efe9ff',
    navActive: '#3c2f6b',
    cardBg: '#ffffff'
  },
  orange: {
    primary: '#ff8d4e',
    accent: '#ffd166',
    pageBg: '#fff7f0',
    navBg: '#ffeede',
    navActive: '#5c3a1c',
    cardBg: '#ffffff'
  }
}

const themes = ref([
  { name: 'blue', title: '清爽蓝', description: '默认主题，清晰易读', color: THEME_PRESETS.blue.primary },
  { name: 'green', title: '活力绿', description: '温和护眼，适合长时间学习', color: THEME_PRESETS.green.primary },
  { name: 'purple', title: '冷静紫', description: '沉稳配色，帮助集中注意力', color: THEME_PRESETS.purple.primary },
  { name: 'orange', title: '暖心橙', description: '明快活泼，激发学习动力', color: THEME_PRESETS.orange.primary }
])

const currentTheme = ref(store.preferences.theme || 'blue')
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

const lastPasswordChange = computed(() => userInfo.value.updated_at || '暂无记录')

const currentThemeName = computed(() => {
  const theme = themes.value.find(t => t.name === currentTheme.value)
  return theme ? theme.title : '未选择'
})

const buildPreferencesPayload = () => ({
  theme: currentTheme.value,
  studySettings: { ...studySettings.value }
})

const applyThemeColors = (themeName) => {
  const preset = THEME_PRESETS[themeName] || THEME_PRESETS.blue
  const root = document.documentElement
  const mapping = {
    primary: '--primary',
    accent: '--accent',
    pageBg: '--page-bg',
    navBg: '--nav-bg',
    navActive: '--nav-active',
    cardBg: '--card-bg'
  }
  Object.entries(mapping).forEach(([key, cssVar]) => {
    if (preset[key]) {
      root.style.setProperty(cssVar, preset[key])
    }
  })
}

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

watch(currentTheme, (value) => {
  applyThemeColors(value)
  schedulePreferencesSave()
}, { immediate: true })

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
    ElMessage.error('头像大小不能超过 2MB')
    return
  }
  try {
    const resp = await uploadAvatarApi(file)
    const avatar = resp.data?.data?.avatar
    if (avatar) {
      userInfo.value.avatar = avatar
      profileForm.value.avatar = avatar
      store.setUserInfo({ ...store.userInfo, avatar })
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
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
    ElMessage.success('个人资料已更新')
  } catch (error) {
    ElMessage.error(error.message || '保存个人资料失败')
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
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    resetPasswordForm()
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
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
  ElMessage.info(`已刷新设备列表（${loginDevices.value.length} 台）`)
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
    ElMessage.success('数据导出成功')
  } catch (error) {
    ElMessage.error(error.message || '数据导出失败')
  }
}

const clearCompletedTasks = () => {
  ElMessageBox.confirm(
    '确定清理所有已完成的任务吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await deleteCompletedTasks()
      ElMessage.success('已清理已完成的任务')
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    }
  })
}

const clearHistory = () => {
  ElMessageBox.confirm(
    '确定要清空历史记录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await clearHistoryApi({ type: 'study' })
      ElMessage.success('历史记录已清空')
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    }
  })
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定退出当前账号吗？', '退出登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    loggingOut.value = true
    await logoutApi().catch(() => {})
  } finally {
    loggingOut.value = false
  }

  localStorage.removeItem('token')
  localStorage.removeItem('user')
  store.$reset?.()
  router.replace('/login')
  ElMessage.success('已退出登录')
}

const loadProfile = async () => {
  try {
    const resp = await fetchProfile()
    userInfo.value = resp.data?.data || userInfo.value
    profileForm.value = { ...userInfo.value }
    store.setUserInfo(userInfo.value)
  } catch (error) {
    ElMessage.error(error.message || '加载个人信息失败')
  }
}

const loadPreferences = async () => {
  try {
    const resp = await getPreferences()
    const prefs = resp.data?.data || {}
    preferencesReady = false
    const merged = {
      theme: prefs.theme ?? currentTheme.value,
      studySettings: {
        ...studySettings.value,
        ...(prefs.studySettings)
      }
    }
    currentTheme.value = merged.theme
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

/* 固定宽度布局（响应式） */
.settings-page {
  background: #ffffff; /* 纯白背景 */
  width: 900px;
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
  color: var(--nav-active);
}

.header-subtitle {
  font-size: 14px;
  color: var(--muted);
}

/* 设置容器 */
.settings-container {
  display: flex;
  flex-direction: column; /* 菜单放顶部，内容在下 */
  gap: 20px;
  width: 100%;
}

/* 左侧导航 */
.settings-nav {
  width: 100%;
  flex-shrink: 0;
  margin-bottom: 6px;
}

.settings-menu {
  border-right: none;
  border-radius: 12px;
  background: var(--card-bg, #ffffff);
  box-shadow: var(--soft-shadow, 0 6px 16px rgba(43,50,80,0.04));
  padding: 6px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  /* 附件颜色变量：主色为青绿色，便于全局替换 */
  --menu-primary: var(--menu-primary, #2ee7c7);
  --menu-accent: var(--menu-accent, #9ff3ea);
}

.settings-menu .el-menu-item {
  height: 56px;
  line-height: 56px;
  font-size: 15px;
  color: var(--nav-active, #2b3250);
  border-radius: 8px;
  margin: 0 6px;
  padding: 0 14px;
  transition: background 0.18s ease, color 0.18s ease;
}

.settings-menu .el-menu-item.is-active {
  /* 改为白底、文字使用全局绿色，并带有底部绿色下划线（更接近图片） */
  background-color: #ffffff;
  color: var(--primary, var(--menu-primary));
  border-bottom: 3px solid var(--primary, var(--menu-primary));
  box-shadow: 0 6px 0 rgba(0,0,0,0.03);
}

.settings-menu .el-menu-item:hover {
  background: linear-gradient(90deg, var(--menu-primary), var(--menu-accent));
  color: #fff;
}

.settings-menu .el-menu-item .el-icon svg {
  color: var(--accent) !important; /* 图标保持全局 accent 颜色 */
}

/* 某些 Element Plus 内部结构会在子元素上覆盖文字颜色，下面强制把选中文字设为全局绿色 */
.settings-menu .el-menu-item.is-active span,
.settings-menu .el-menu-item.is-active .el-menu-item__content,
.settings-menu .el-menu-item.is-active > * {
  color: var(--primary, var(--menu-primary)) !important;
}

/* 右侧内容 */
.settings-content {
  flex: 1;
  min-width: 0;
}

.settings-section {
  background: #ffffff; /* 卡片背景设置为纯白 */
  border-radius: 12px;
  padding: 25px;
  box-shadow: var(--soft-shadow);
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
  color: var(--nav-active);
}

.section-description {
  font-size: 14px;
  color: var(--muted);
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
  border: 2px solid rgba(44,199,183,0.12);
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-tip {
  font-size: 12px;
  color: var(--muted);
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
  color: var(--nav-active);
}

.form-tip {
  font-size: 12px;
  color: var(--muted);
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
  border: 1px solid rgba(43,50,80,0.06);
  border-radius: 10px;
  padding: 20px;
  background: #ffffff; /* 纯白，去掉渐变 */
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
  background: #fff;
  border: 1px solid rgba(43,50,80,0.04);
  border-radius: 8px;
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
  color: var(--nav-active);
}

.current-theme {
  font-size: 14px;
  color: var(--muted);
}

.theme-name {
  color: var(--accent);
  font-weight: 600;
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
  border: 1px solid rgba(43,50,80,0.06);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  background: linear-gradient(180deg,#fff,#fbfdff);
}

.theme-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(43,50,80,0.06);
}

.theme-item.active {
  border-color: var(--primary);
  background: linear-gradient(90deg, var(--primary), var(--accent));
  box-shadow: 0 8px 24px rgba(43,50,80,0.06);
}

.theme-preview {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  box-shadow: 0 6px 16px rgba(43,50,80,0.04);
}

.theme-info {
  flex: 1;
}

.theme-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--nav-active);
}

.theme-desc {
  font-size: 12px;
  color: var(--muted);
}

/* 界面设置 */
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
  color: var(--nav-active);
}

.setting-desc {
  flex: 1;
  font-size: 12px;
  color: var(--muted);
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
  border: 1px solid rgba(43,50,80,0.06);
  border-radius: 10px;
  padding: 20px;
}

/* 小号强调色按钮 */
</style>
