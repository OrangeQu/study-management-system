import http from './http'

export const recordSession = (data) => http.post('/study/sessions', data)
export const startSession = (data) => http.post('/study/sessions/start', data)
export const updateSession = (id, data, params = {}) => http.patch(`/study/sessions/${id}`, data, { params })
export const todayStats = () => http.get('/study/stats/today')
export const trendStats = (params = {}) => http.get('/study/stats/trend', { params })
export const overviewStats = () => http.get('/study/stats/overview')
