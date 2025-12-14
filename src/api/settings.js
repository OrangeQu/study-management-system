import http from './http'

export const getPreferences = () => http.get('/settings/preferences')
export const savePreferences = (data) => http.put('/settings/preferences', data)
