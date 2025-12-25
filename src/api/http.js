import axios from 'axios'

const baseURL = process.env.VUE_APP_API_BASE || 'http://localhost:8080/api'

const http = axios.create({
  baseURL,
  timeout: 100000
})

let isRefreshing = false
const failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  failedQueue.length = 0
}

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config || {}
    const status = error.response?.status
    if (status === 401 && !originalRequest._retry) {
      const token = localStorage.getItem('token')
      if (!token) {
        return Promise.reject(new Error('未登录或登录已过期'))
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then((newToken) => {
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return http(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true
      try {
        const refreshResp = await axios.post(
          `${baseURL}/auth/refresh`,
          {},
          {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        )
        const newToken = refreshResp.data?.data?.token
        if (!newToken) throw new Error('刷新令牌失败')
        localStorage.setItem('token', newToken)
        processQueue(null, newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return http(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    const message =
      error.response?.data?.message ||
      error.message ||
      '请求失败，请稍后重试'
    return Promise.reject(new Error(message))
  }
)

export default http
