import { defineStore } from 'pinia'
import { ref } from 'vue'

interface Message {
  id: string
  userId: string
  username: string
  content: string
  timestamp: string
  type: 'CHAT' | 'JOIN' | 'LEAVE'
}

interface ChatRoom {
  id: string
  users: string[]
  messages: Message[]
}

export const useChatRoomStore = defineStore('chatroom', () => {
  const activeRoom = ref<ChatRoom | null>(null)
  const ws = ref<WebSocket | null>(null)
  const onlineCount = ref(0) // 添加在线人数状态

  const initWebSocket = (roomId: string, userId: string, nickname: string) => {
    if (!activeRoom.value || activeRoom.value.id !== roomId) {
      activeRoom.value = {
        id: roomId,
        users: [userId],
        messages: []
      }
    }

    ws.value = new WebSocket(
      `ws://localhost:9902/ws/chat/room/${roomId}?userId=${userId}&nickname=${nickname}`
    )

    ws.value.onopen = () => {
      console.log('WebSocket 连接已建立')
    }

    ws.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        console.log('收到消息:', data)
        if (data.type === 'ONLINE_COUNT') {
          // 更新在线人数
          onlineCount.value = parseInt(data.content)
        } else if (activeRoom.value) {
          // 检查消息是否已存在，避免重复
          const messageExists = activeRoom.value.messages.some((msg) => msg.id === data.id)
          if (!messageExists) {
            activeRoom.value.messages.push({
              id: data.id,
              userId: data.userId,
              
              username: data.username || data.userId,
              content: data.content,
              timestamp: data.timestamp,
              type: data.type
            })
          }
        }
      } catch (error) {
        console.error('消息处理错误:', error)
      }
    }

    ws.value.onerror = (error) => {
      console.error('WebSocket 错误:', error)
    }

    ws.value.onclose = () => {
      console.log('WebSocket 连接已关闭')
    }
  }

  const sendMessage = (content: string, userId: string) => {
    if (!activeRoom.value || !ws.value) return

    const message = {
      id: crypto.randomUUID(),
      userId: userId,
      username: userId,
      content: content,
      timestamp: new Date().toISOString(),
      type: 'CHAT'
    }

    ws.value.send(JSON.stringify(message))
  }

  const closeConnection = () => {
    ws.value?.close()
  }

  return {
    activeRoom,
    initWebSocket,
    sendMessage,
    closeConnection,
    onlineCount
  }
})
