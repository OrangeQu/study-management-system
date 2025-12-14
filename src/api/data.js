import http from './http'

export const exportData = (params = {}) =>
  http.get('/data/export', { params, responseType: 'blob' })

export const clearHistory = (params = {}) => http.delete('/data/history', { params })
