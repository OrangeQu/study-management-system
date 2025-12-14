import http from './http'

export const recordSession = (data) => http.post('/study/sessions', data)
export const todayStats = () => http.get('/study/stats/today')
export const trendStats = (params = {}) => http.get('/study/stats/trend', { params })
