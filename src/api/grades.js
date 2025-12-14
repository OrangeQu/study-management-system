import http from './http'

export const listGrades = (params = {}) => http.get('/grades', { params })
export const createGrade = (data) => http.post('/grades', data)
export const updateGrade = (id, data) => http.put(`/grades/${id}`, data)
export const deleteGrade = (id) => http.delete(`/grades/${id}`)
export const statsGrades = () => http.get('/grades/stats')
export const exportGrades = (params = {}) =>
  http.get('/grades/export', { params, responseType: 'blob' })
export const importGrades = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/grades/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
