<script setup lang="ts">
import { ChatRound, Close, Delete, EditPen } from '@element-plus/icons-vue'
import { nextTick, onMounted, ref } from 'vue'
import SessionItem from './components/session-item.vue'
import { storeToRefs } from 'pinia'
import { useChatStore } from '@/views/chat/store/chat-store'
import { api } from '@/utils/api-instance'
const API_PREFIX = import.meta.env.VITE_API_PREFIX
const chatStore = useChatStore()
const { handleDeleteSession, handleUpdateSession, handleClearMessage } = chatStore
const { activeSession, sessionList, isEdit } = storeToRefs(chatStore)
const messageListRef = ref<InstanceType<typeof HTMLDivElement>>()
const loading = ref(true)

onMounted(async () => {
  // 查询自己的聊天会话
  api.aiSessionController.findByUser().then((res) => {
    // 讲会话添加到列表中
    sessionList.value = res.map((row) => {
      return { ...row, checked: false }
    })
    // 默认选中的聊天会话是第一个
    if (sessionList.value.length > 0) {
      activeSession.value = sessionList.value[0]
    } else {
      chatStore.handleCreateSession({ name: '新的绘画' })
    }
    loading.value = false
  })
})
const handleSessionCreate = () => {
  chatStore.handleCreateSession({ name: '新的聊天' })
}
</script>

<template>
  <!-- 最外层页面于窗口同宽，使聊天面板居中 -->
  <div class="home-view">
    <!-- 整个聊天面板 -->
    <div class="chat-panel" v-loading="loading">
      <!-- 左侧的会话列表 -->
      <div class="session-panel">
        <div class="title">AI助手</div>
        <div class="session-list" v-if="activeSession">
          <!-- for循环遍历会话列表用会话组件显示，并监听点击事件和删除事件。点击时切换到被点击的会话，删除时从会话列表中提出被删除的会话。 -->
          <session-item
            v-for="session in sessionList"
            :key="session.id"
            :active="session.id === activeSession.id"
            :session="session"
            class="session"
            @click="activeSession = session"
            @delete="handleDeleteSession"
          ></session-item>
        </div>
        <div class="button-wrapper">
          <el-button
            style="margin-right: 20px"
            :icon="ChatRound"
            size="small"
            @click="handleSessionCreate"
            >创建会话
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>
<style lang="scss" scoped>
.home-view {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;

  .chat-panel {
    display: flex;
    background-color: white;
    width: 90%;
    height: 90%;
    box-shadow: 0 0 10px rgba(black, 0.1);
    border-radius: 10px;

    .session-panel {
      display: flex;
      flex-direction: column;
      box-sizing: border-box;
      padding: 20px;
      position: relative;
      border-right: 1px solid rgba(black, 0.07);
      background-color: rgb(231, 248, 255);
      height: 100%;
      /* 标题 */
      .title {
        margin-top: 20px;
        font-size: 20px;
      }

      .session-list {
        overflow-y: scroll;
        margin: 20px 0;
        flex: 1;

        .session {
          /* 每个会话之间留一些间距 */
          margin-top: 20px;
        }

        .session:first-child {
          margin-top: 0;
        }
      }

      .button-wrapper {
        /* entity-panel是相对布局，这边的button-wrapper是相对它绝对布局 */
        bottom: 20px;
        left: 0;
        display: flex;
        /* 让内部的按钮显示在右侧 */
        justify-content: flex-end;
        /* 宽度和session-panel一样宽*/
        width: 100%;

        /* 按钮于右侧边界留一些距离 */
        .new-session {
          margin-right: 20px;
        }
      }
    }
  }
}
</style>
