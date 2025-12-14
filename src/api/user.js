import http from './http'

export const fetchProfile = () => http.get('/user/me')
export const updateProfile = (data) => http.put('/user/profile', data)
export const changePassword = (data) => http.put('/user/password', data)
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const listDevices = () => http.get('/user/devices')
export const removeDevice = (id) => http.delete(`/user/devices/${id}`)
