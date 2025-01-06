<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useChatRoomStore } from './store/chatroom-store'
import { storeToRefs } from 'pinia'
import MessageRow from '../chat/components/message-row.vue'
import type { UserDTO } from '@/apis/__generated/model/user'
import { ElMessage } from 'element-plus'
const chatRoomStore = useChatRoomStore()
const { activeRoom, onlineCount } = storeToRefs(chatRoomStore)
const messageInput = ref<HTMLTextAreaElement>()
const messageText = ref('')
const userId = localStorage.getItem('userId')
const userInfo = ref<UserDTO>()
  const nickname = ref('')
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

const roomIdInput = ref('')

const joinRoom = () => {
  if (!roomIdInput.value.trim()) {
    ElMessage.error('请输入房间ID')
    return
  }
  if (!userId) {
    ElMessage.error('请先登录')
    return
  }
  chatRoomStore.initWebSocket(roomIdInput.value, userId, nickname.value)
}

const leaveRoom = () => {
  chatRoomStore.closeConnection()
  roomIdInput.value = ''
}

onMounted(async () => {
  if (!userId) {
    ElMessage.error('请先登录')
    return
  }
})

onUnmounted(() => {
  chatRoomStore.closeConnection()
})
</script>

<template>
  <div class="chatroom-view">
    <div class="nav-bar">
      <el-menu mode="horizontal" :router="true" class="custom-menu">
        <el-menu-item index="/">
          <el-icon><ChatLineRound /></el-icon>AI 助手
        </el-menu-item>
        <el-menu-item index="/chatroom">
          <el-icon><Service /></el-icon>公共聊天室
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>个人中心
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 添加房间选择/输入表单 -->
    <div class="room-selector" v-if="!activeRoom">
      <el-form @submit.prevent="joinRoom">
        <el-form-item label="房间ID">
          <el-input v-model="roomIdInput" placeholder="请输入房间ID"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="joinRoom">加入房间</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 聊天室主界面 -->
    <div class="chat-panel" v-else>
      <div class="room-info">
        <h2>房间: {{ activeRoom.id }}</h2>
        <span class="online-count">在线人数: {{ onlineCount }}</span>
        <el-button @click="leaveRoom" size="small">离开房间</el-button>
      </div>
      <div class="message-list" ref="messageListRef">
        <div v-for="message in activeRoom?.messages" :key="message.id" class="message-item">
          <div class="message-info">
            <div class="sender-name">{{ message.userId || '未知用户' }}</div>
            <div class="message-content">
              {{ getMessageContent(message.content) }}
              <div class="message-time">{{ message.timestamp }}</div>
            </div>
          </div>
        </div>
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

.room-selector {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
</style>
