import http from './http'

export const listUsers = (params) => http.get('/admin/users', { params })
export const getUser = (id) => http.get(`/admin/users/${id}`)
export const createUser = (data) => http.post('/admin/users', data)
export const updateUser = (id, data) => http.put(`/admin/users/${id}`, data)
export const deleteUser = (id) => http.delete(`/admin/users/${id}`)
