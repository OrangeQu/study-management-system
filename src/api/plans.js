import http from './http'

export const listPlans = (params) => http.get('/plans', { params })
export const createPlan = (data) => http.post('/plans', data)
export const updatePlan = (id, data) => http.put(`/plans/${id}`, data)
export const updatePlanStatus = (id, data) => http.patch(`/plans/${id}/status`, data)
export const deletePlan = (id) => http.delete(`/plans/${id}`)
