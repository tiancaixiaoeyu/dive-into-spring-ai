<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useChatRoomStore } from './store/chatroom-store'
import { storeToRefs } from 'pinia'
import MessageRow from '../chat/components/message-row.vue'

import { ElMessage } from 'element-plus'
const chatRoomStore = useChatRoomStore()
const { activeRoom, onlineCount } = storeToRefs(chatRoomStore)
const messageInput = ref<HTMLTextAreaElement>()
const messageText = ref('')
const userId = localStorage.getItem('userId')
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateTo = (path: string) => {
  router.push(path)
}

// 监听在线用户数变化
watch(
  () => activeRoom.value?.users,
  (newUsers) => {
    if (newUsers) {
      onlineCount.value = newUsers.length
    }
  },
  { deep: true }
)

// 解析 JSON 字符串并提取 content 字段
const getMessageContent = (content: string): string => {
  try {
    const parsed = JSON.parse(content)
    return parsed.content || '内容字段不存在'
  } catch (error) {
    console.error('JSON 解析错误:', error)
    return content
  }
}
const sendMessage = () => {
  if (!messageText.value.trim()) return
  if (!userId) {
    ElMessage.error('请先登录')
    return
  }
  chatRoomStore.sendMessage(messageText.value, userId)
  messageText.value = ''
  console.log(messageText)
}

onMounted(() => {
  if (!userId) {
    ElMessage.error('请先登录')
    return
  }
  
  chatRoomStore.initWebSocket('public-room', userId, nickname)
  console.log('WebSocket initialized with userId:', userId)
})

onUnmounted(() => {
  chatRoomStore.closeConnection()
})
</script>

<template>
  <div class="chatroom-view">
    <div class="nav-bar">
      <el-menu mode="horizontal" style="width: 100%">
        <el-menu-item index="ai-assistant" @click="navigateTo('/')">AI 助手</el-menu-item>
        <el-menu-item index="chatroom" @click="navigateTo('/chatroom')">公共聊天室</el-menu-item>
        <el-menu-item index="profile" @click="navigateTo('/profile')">个人中心</el-menu-item>
      </el-menu>
    </div>
    <div class="chat-panel">
      <div class="room-info">
        <h2>公共聊天室</h2>
        <span class="online-count">在线人数: {{ onlineCount }}</span>
      </div>

      <div class="message-list" ref="messageListRef">
        <div v-for="message in activeRoom?.messages" :key="message.id" class="message-item">
          <div class="message-info">
            <div class="sender-name">{{ message.nickname }}</div>
            <div class="message-content">
              {{ getMessageContent(message.content) }}
              <div class="message-time">{{ message.timestamp }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <el-input
          v-model="messageText"
          type="textarea"
          :rows="3"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
        />
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.chatroom-view {
  width: 100vw;
  height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  .nav-bar {
    position: fixed;
    top: 0;
    width: 100%;
    z-index: 1000;
    background: white;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .chat-panel {
    width: 80%;
    height: 90%;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;

    .room-info {
      padding: 20px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.1);
      display: flex;
      justify-content: space-between;
      align-items: center;

      .online-count {
        color: #666;
      }
    }

    .message-list {
      padding: 15px;
      width: 100%;
      flex: 1;
      box-sizing: border-box;
      // 消息条数太多时，溢出部分滚动
      overflow-y: scroll;

      //... 保持其他属性不变 ...
      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-thumb {
        background-color: rgba(0, 0, 0, 0.1);
        border-radius: 3px;
      }
    }
    .message-item {
      padding: 10px;
      margin: 5px 0;
    }

    .message-content {
      margin-bottom: 5px;
    }

    .message-time {
      font-size: 12px;
      color: #999;
    }

    .input-area {
      padding: 20px;
      border-top: 1px solid rgba(0, 0, 0, 0.1);

      .el-button {
        margin-top: 10px;
        width: 100%;
      }
    }
  }
}

.message-info {
  .sender-name {
    font-size: 12px;
    color: #666;
    margin-bottom: 4px;
  }

  .message-content {
    background: white;
    padding: 8px 12px;
    border-radius: 4px;
    display: inline-block;
  }
}
</style>
