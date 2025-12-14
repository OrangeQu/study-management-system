import http from './http'

export const listTasks = (params = {}) => http.get('/tasks', { params })
export const createTask = (data) => http.post('/tasks', data)
export const updateTask = (id, data) => http.put(`/tasks/${id}`, data)
export const updateTaskStatus = (id, data) => http.patch(`/tasks/${id}/status`, data)
export const deleteTask = (id) => http.delete(`/tasks/${id}`)
export const deleteCompletedTasks = () => http.delete('/tasks/completed')
export const summaryTasks = () => http.get('/tasks/stats/summary')
