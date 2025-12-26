export const getFriendlyErrorMessage = (error) => {
  if (!error) return '网络繁忙，请稍后再试'
  if (typeof error === 'string') return error
  if (error.response) {
    const data = error.response.data || {}
    if (data.message) return data.message
    if (data.error) return data.error
    if (data.errors) {
      if (Array.isArray(data.errors) && data.errors.length) return data.errors[0]
      if (typeof data.errors === 'string') return data.errors
    }
    if (error.response.status) {
      return `请求失败（${error.response.status}）`
    }
  }
  if (error.message) return error.message
  return '请求失败，请稍后再试'
}
