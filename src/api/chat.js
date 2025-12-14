import http from './http'

export const sendMessage = (data) => http.post('/chat/messages', data)
