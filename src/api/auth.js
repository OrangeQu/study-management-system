import http from './http'

export const login = (data) => http.post('/auth/login', data)
export const register = (data) => http.post('/auth/register', data)
export const fetchMe = () => http.get('/user/me')
export const logout = () => http.post('/auth/logout')
export const refreshToken = () => http.post('/auth/refresh')
