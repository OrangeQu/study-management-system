import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import pinia from './stores'
import router from './router'
import { useMainStore } from '@/stores'

const app = createApp(App)

app.use(ElementPlus)
app.use(pinia)
// 在挂载前读取本地偏好并应用主题
const store = useMainStore()
try {
  const raw = localStorage.getItem('preferences')
  if (raw) {
    const prefs = JSON.parse(raw)
    store.setPreferences(prefs)
  }
} catch (e) {
  // ignore
}

// 应用主题到 :root（设置 CSS 变量）
const applyThemeColors = (themeName) => {
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
    if (preset[key]) root.style.setProperty(cssVar, preset[key])
  })
}

applyThemeColors(store.preferences?.theme || store.theme)
app.use(router)

// 注册 Element Plus 所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
