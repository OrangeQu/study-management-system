<template>
  <div :class="['chat-message', message.type]">
    <div class="message-avatar">
      <el-avatar :size="32">
        {{ message.type === 'user' ? '我' : 'AI' }}
      </el-avatar>
    </div>
    
    <div class="message-content">
      <div class="message-text">{{ message.content }}</div>
      <div class="message-time">{{ formatTime(message.time) }}</div>
    </div>
  </div>
</template>

<script setup>
// import { defineProps } from 'vue'
import dayjs from 'dayjs'

// const props = defineProps({
//   message: {
//     type: Object,
//     required: true,
//     default: () => ({})
//   }
// })

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}
</script>

<style scoped>
.chat-message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.chat-message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-message.ai {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: calc(100% - 44px);
}

.chat-message.user .message-content {
  align-items: flex-end;
}

.message-text {
  padding: 10px 15px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.chat-message.ai .message-text {
  background: #f0f2f5;
  color: #333;
  border-top-left-radius: 0;
}

.chat-message.user .message-text {
  background: #409eff;
  color: white;
  border-top-right-radius: 0;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-message {
    max-width: 90%;
  }
  
  .message-text {
    padding: 8px 12px;
    font-size: 13px;
  }
}
</style>