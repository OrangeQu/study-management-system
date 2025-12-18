import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../components/layout/MainLayout.vue'
import { useMainStore } from '@/stores'
import { fetchProfile } from '@/api/user'

const routes = [
  {
    path: '/',
    redirect: '/dashboard',
    component: MainLayout,
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'study-center',
        name: 'StudyCenter',
        component: () => import('../views/StudyCenter.vue')
      },
      {
        path: 'grade-data',
        name: 'GradeData',
        component: () => import('../views/GradeData.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const authWhitelist = ['Login', 'Register']

router.beforeEach(async (to, from, next) => {
  const store = useMainStore()
  const token = localStorage.getItem('token')

  // 临时本地绕过登录（开发用），可手动在控制台设置 localStorage.DISABLE_AUTH = 'true'
  const disableAuth = localStorage.getItem('DISABLE_AUTH') === 'true'
  if (disableAuth) return next()

  if (!token && !authWhitelist.includes(to.name)) {
    return next('/login')
  }

  if (token && !store.userInfo.id) {
    try {
      const resp = await fetchProfile()
      store.setUserInfo(resp.data?.data || {})
    } catch (e) {
      console.error(e)
      localStorage.removeItem('token')
      return next('/login')
    }
  }

  if (token && authWhitelist.includes(to.name)) {
    return next('/dashboard')
  }

  next()
})

export default router
