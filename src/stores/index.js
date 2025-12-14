import { createPinia, defineStore } from 'pinia'

const pinia = createPinia()

export default pinia

const buildDefaultPreferences = () => ({
  theme: 'blue',
  fixedHeader: true,
  showBreadcrumb: true,
  enableTagsView: true,
  enableAnimation: true,
  studySettings: {
    pomodoroWork: 25,
    pomodoroBreak: 5,
    taskReminder: '1d',
    dailyGoal: 4,
    weeklyGoal: 20
  }
})

// 主store
export const useMainStore = defineStore('main', {
  state: () => ({
    userInfo: {
      id: null,
      username: '',
      avatar: ''
    },
    theme: buildDefaultPreferences().theme,
    preferences: buildDefaultPreferences(),
    studySettings: { ...buildDefaultPreferences().studySettings },
    tasks: [],
    planSlots: [],
    courseGrades: []
  }),
  getters: {
    isLoggedIn: (state) => !!state.userInfo.id,
    todayTasks: (state) => {
      const today = new Date().toISOString().split('T')[0]
      return state.tasks.filter(task => 
        task.deadline && task.deadline.startsWith(today)
      )
    }
  },
  actions: {
    setUserInfo(user = {}) {
      this.userInfo = { ...this.userInfo, ...user }
    },
    setPreferences(prefs = {}) {
      const studySettings = {
        ...this.studySettings,
        ...(prefs.studySettings || {})
      }
      this.preferences = {
        ...this.preferences,
        ...prefs,
        studySettings
      }
      this.studySettings = studySettings
      if (prefs.theme) {
        this.theme = prefs.theme
      }
    },
    addTask(task) {
      this.tasks.push(task)
    },
    updateTask(id, updates) {
      const index = this.tasks.findIndex(t => t.id === id)
      if (index !== -1) {
        this.tasks[index] = { ...this.tasks[index], ...updates }
      }
    },
    toggleTheme(theme) {
      if (theme) {
        this.theme = theme
      } else {
        this.theme = this.theme === 'light' ? 'dark' : 'light'
      }
      this.preferences.theme = this.theme
    },
    resetPreferences() {
      const defaults = buildDefaultPreferences()
      this.preferences = defaults
      this.studySettings = { ...defaults.studySettings }
      this.theme = defaults.theme
    }
  }
})
