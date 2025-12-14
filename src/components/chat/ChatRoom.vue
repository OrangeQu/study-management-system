<template>
  <div class="chat-room">
    <!-- 消息列表 -->
    <div class="message-list" ref="messageList">
      <div v-if="messages.length === 0" class="empty-message">
        <div class="welcome-content">
          <div class="welcome-avatar">
            <el-avatar :size="48">AI</el-avatar>
          </div>
          <div class="welcome-text">
            <h4>我是你的学习助手</h4>
            <p>我可以帮助你：制定学习计划、解答学习问题、提供学习建议</p>
          </div>
        </div>
      </div>
      
      <div v-else>
        <ChatMessage
          v-for="message in messages"
          :key="message.id"
          :message="message"
        />
      </div>
    </div>

    <!-- 消息输入 -->
    <div class="message-input">
      <el-input
        v-model="userInput"
        placeholder="输入你的问题..."
        type="textarea"
        :rows="3"
        :maxlength="500"
        resize="none"
        @keydown.enter="sendMessage"
      />
      <div class="input-actions">
        <el-button 
          type="primary" 
          @click="sendMessage"
          :loading="sending"
          :disabled="!userInput.trim()"
        >
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,  defineEmits, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import ChatMessage from './ChatMessage.vue'

// const props = defineProps({
//   messages: {
//     type: Array,
//     default: () => []
//   }
// })

const emit = defineEmits(['send'])

const userInput = ref('')
const sending = ref(false)
const messageList = ref(null)

const sendMessage = async () => {
  if (!userInput.value.trim()) return
  
  const message = userInput.value.trim()
  userInput.value = ''
  sending.value = true
  
  try {
    // 发送用户消息
    emit('send', message)
    
    // 滚动到底部
    nextTick(() => {
      if (messageList.value) {
        messageList.value.scrollTop = messageList.value.scrollHeight
      }
    })
  } catch (error) {
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.chat-room {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.empty-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.welcome-content {
  text-align: center;
  max-width: 300px;
}

.welcome-avatar {
  margin-bottom: 16px;
}

.welcome-text h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #333;
}

.welcome-text p {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.message-input {
  padding: 15px 20px;
  border-top: 1px solid #e8e8e8;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

/* 滚动条样式 */
.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-list {
    padding: 15px;
  }
  
  .message-input {
    padding: 12px 15px;
  }
}
</style>