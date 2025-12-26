const STORAGE_KEY_PREFIX = 'study_ai_chat_history'

const parseStoredUser = () => {
  if (typeof window === 'undefined') return null
  const raw = localStorage.getItem('user')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

const resolveUserId = (explicitId) => {
  if (explicitId) return explicitId
  const stored = parseStoredUser()
  return stored?.id || stored?.userId || stored?.user_id || 'guest'
}

const getStorageKey = (userId) => `${STORAGE_KEY_PREFIX}_${userId}`

export const loadChatHistory = () => {
  if (typeof window === 'undefined') return null
  const key = getStorageKey(resolveUserId())
  const raw = localStorage.getItem(key)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (error) {
    return null
  }
}

export const saveChatHistory = (payload = {}) => {
  if (typeof window === 'undefined') return
  const key = getStorageKey(resolveUserId())
  if (!payload.messages?.length && !payload.conversationId) {
    localStorage.removeItem(key)
    return
  }
  const serialized = JSON.stringify({
    conversationId: payload.conversationId || null,
    messages: payload.messages || []
  })
  localStorage.setItem(key, serialized)
}

export const clearChatHistory = (userId) => {
  if (typeof window === 'undefined') return
  const key = getStorageKey(resolveUserId(userId))
  localStorage.removeItem(key)
}
