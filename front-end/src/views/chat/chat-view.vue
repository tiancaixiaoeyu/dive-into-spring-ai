<!--<script lang="ts" setup>-->
<!--import { ChatRound, Close, Delete, EditPen } from '@element-plus/icons-vue'-->
<!--import { nextTick, onMounted, ref } from 'vue'-->
<!--import SessionItem from './components/session-item.vue'-->
<!--import { storeToRefs } from 'pinia'-->
<!--import { ElIcon, type UploadProps, type UploadUserFile } from 'element-plus'-->
<!--import { api } from '@/utils/api-instance'-->
<!--import { ElMessage } from 'element-plus'-->
<!--import { type AiMessage, useChatStore } from './store/chat-store'-->
<!--import { SSE } from 'sse.js'-->
<!--import MessageRow from '@/views/chat/components/message-row.vue'-->
<!--import MessageInput from '@/views/chat/components/message-input.vue'-->
<!--//import type { AiMessageParams, AiMessageWrapper } from '@/apis/__generated/model/static'-->

<!--type ChatResponse = {-->
<!--  metadata: {-->
<!--    usage: {-->
<!--      totalTokens: number-->
<!--    }-->
<!--  }-->
<!--  result: {-->
<!--    metadata: {-->
<!--      finishReason: string-->
<!--    }-->
<!--    output: {-->
<!--      messageType: string-->
<!--      content: string-->
<!--    }-->
<!--  }-->
<!--}-->
<!--const API_PREFIX = import.meta.env.VITE_API_PREFIX-->
<!--const chatStore = useChatStore()-->
<!--const { handleDeleteSession, handleUpdateSession, handleClearMessage, handleCreateSession } =-->
<!--  chatStore-->
<!--const { activeSession, sessionList } = storeToRefs(chatStore)-->
<!--const messageListRef = ref<InstanceType<typeof HTMLDivElement>>()-->
<!--const loading = ref(true)-->

<!--onMounted(async () => {-->
<!--  // 查询自己的聊天会话-->
<!--  api.aiSessionController.findByUser().then((res) => {-->
<!--    // 讲会话添加到列表中-->
<!--    sessionList.value = res.map((row) => {-->
<!--      return { ...row, checked: false }-->
<!--    })-->
<!--    // 默认选中的聊天会话是第一个-->
<!--    if (sessionList.value.length > 0) {-->
<!--      activeSession.value = sessionList.value[0]-->
<!--    } else {-->
<!--      chatStore.handleCreateSession({ name: '新的绘画' })-->
<!--    }-->
<!--    loading.value = false-->
<!--  })-->
<!--})-->
<!--const isEdit = ref(false)-->

<!--// ChatGPT的回复-->
<!--const responseMessage = ref<AiMessage>({-->
<!--  id: new Date().getTime().toString(),-->
<!--  type: 'ASSISTANT',-->
<!--  medias: [],-->
<!--  textContent: 'multipart/form-data',-->
<!--  sessionId: ''-->
<!--})-->

<!--const handleSendMessage = async (message: { text: string; image: string }) => {-->
<!--  if (!activeSession.value) {-->
<!--    ElMessage.warning('请创建会话')-->
<!--    return-->
<!--  }-->

<!--  // 图片/语音-->
<!--  const medias: AiMessage['medias'] = []-->
<!--  if (message.image) {-->
<!--    medias.push({ type: 'image', data: message.image })-->
<!--  }-->
<!--  const now = new Date().toISOString()-->
<!--  // 用户的提问-->
<!--  const chatMessage = {-->
<!--    id: new Date().getTime().toString(),-->
<!--    sessionId: activeSession.value.id,-->
<!--    medias,-->
<!--    textContent: message.text,-->
<!--    type: 'USER',-->
<!--    createdTime: new Date().toISOString(), // 添加这行-->
<!--    editedTime: new Date().toISOString() // 添加这行-->
<!--  } satisfies AiMessage-->

<!--  responseMessage.value = {-->
<!--    id: new Date().getTime().toString(),-->
<!--    medias: [],-->
<!--    type: 'ASSISTANT',-->
<!--    textContent: '',-->
<!--    sessionId: activeSession.value.id-->
<!--  }-->
<!--  const body: AiMessageWrapper = { message: chatMessage, params: options.value }-->

<!--  const form = new FormData()-->
<!--  // 将消息对象转换为JSON字符串-->
<!--  form.append(-->
<!--    'input',-->
<!--    JSON.stringify({-->
<!--      message: chatMessage,-->
<!--      params: options.value-->
<!--    })-->
<!--  )-->

<!--  if (fileList.value.length && fileList.value[0].raw) {-->
<!--    form.append('file', fileList.value[0].raw)-->
<!--  }-->
<!--}-->
<!--const evtSource = new SSE(API_PREFIX + '/message/chat', {-->
<!--  withCredentials: true,-->
<!--  // 禁用自动启动，需要调用stream()方法才能发起请求-->
<!--  start: false,-->
<!--  method: 'POST',-->
<!--  payload: form-->
<!--})-->
<!--evtSource.addEventListener('message', async (event: any) => {-->
<!--  const response = JSON.parse(event.data) as ChatResponse-->
<!--  const finishReason = response.result.metadata.finishReason-->
<!--  if (response.result.output.content) {-->
<!--    responseMessage.value.textContent += response.result.output.content-->
<!--    // 滚动到底部-->
<!--    await nextTick(() => {-->
<!--      messageListRef.value?.scrollTo(0, messageListRef.value.scrollHeight)-->
<!--    })-->
<!--  }-->
<!--  if (finishReason && finishReason.toLowerCase() == 'stop') {-->
<!--    evtSource.close()-->
<!--    // 保存用户的提问-->
<!--    api.aiMessageController.save({ body: chatMessage })-->
<!--    console.log('11')-->
<!--    // 保存大模型的回复-->
<!--    api.aiMessageController.save({ body: responseMessage.value })-->
<!--  }-->
<!--})-->

<!--// 调用stream，发起请求。-->
<!--evtSource.stream()-->
<!--// 将两条消息显示在页面中-->
<!--activeSession.value.messages.push(...[chatMessage, responseMessage.value])-->
<!--await nextTick(() => {-->
<!--  messageListRef.value?.scrollTo(0, messageListRef.value.scrollHeight)-->
<!--})-->

<!--const handleSessionCreate = () => {-->
<!--  chatStore.handleCreateSession({ name: '新的聊天' })-->
<!--}-->
<!--const options = ref<AiMessageParams>({-->
<!--  enableVectorStore: false,-->
<!--  enableAgent: false-->
<!--})-->
<!--const embeddingLoading = ref(false)-->
<!--const onUploadSuccess = () => {-->
<!--  embeddingLoading.value = false-->
<!--  ElMessage.success('上传成功')-->
<!--}-->
<!--const beforeUpload: UploadProps['beforeUpload'] = () => {-->
<!--  embeddingLoading.value = true-->
<!--  return true-->
<!--}-->
<!--const fileList = ref<UploadUserFile[]>([])-->
<!--</script>-->

<!--<template>-->
<!--  &lt;!&ndash; 最外层页面于窗口同宽，使聊天面板居中 &ndash;&gt;-->
<!--  <div class="home-view">-->
<!--    &lt;!&ndash; 整个聊天面板 &ndash;&gt;-->
<!--    <div class="chat-panel" v-loading="loading">-->
<!--      &lt;!&ndash; 左侧的会话列表 &ndash;&gt;-->
<!--      <div class="session-panel">-->
<!--        <div class="title">Agent4U</div>-->
<!--        <div class="session-list" v-if="activeSession">-->
<!--          &lt;!&ndash; for循环遍历会话列表用会话组件显示，并监听点击事件和删除事件。点击时切换到被点击的会话，删除时从会话列表中提出被删除的会话。 &ndash;&gt;-->
<!--          <session-item-->
<!--            v-for="session in sessionList"-->
<!--            :key="session.id"-->
<!--            :active="session.id === activeSession.id"-->
<!--            :session="session"-->
<!--            class="session"-->
<!--            @click="activeSession = session"-->
<!--            @delete="handleDeleteSession"-->
<!--          ></session-item>-->
<!--        </div>-->
<!--        <div class="button-wrapper">-->
<!--          <el-button-->
<!--            style="margin-right: 20px"-->
<!--            :icon="ChatRound"-->
<!--            size="small"-->
<!--            @click="handleSessionCreate"-->
<!--            >创建会话-->
<!--          </el-button>-->
<!--        </div>-->
<!--      </div>-->

<!--      &lt;!&ndash; 右侧的消息记录 &ndash;&gt;-->
<!--      <div class="message-panel">-->
<!--        &lt;!&ndash; 会话名称 &ndash;&gt;-->
<!--        <div class="header" v-if="activeSession">-->
<!--          <div class="front">-->
<!--            &lt;!&ndash; 如果处于编辑状态则显示输入框让用户去修改 &ndash;&gt;-->
<!--            <div v-if="isEdit" class="title">-->
<!--              &lt;!&ndash; 按回车代表确认修改 &ndash;&gt;-->
<!--              <el-input-->
<!--                v-model="activeSession.name"-->
<!--                @keydown.enter="handleUpdateSession"-->
<!--              ></el-input>-->
<!--            </div>-->
<!--            &lt;!&ndash; 否则正常显示标题 &ndash;&gt;-->
<!--            <div v-else class="title">{{ activeSession.name }}</div>-->
<!--            <div class="description">{{ activeSession.messages.length }}条对话</div>-->
<!--          </div>-->
<!--          &lt;!&ndash; 尾部的编辑按钮 &ndash;&gt;-->
<!--          <div class="rear">-->
<!--            <el-icon :size="20" style="margin-right: 10px">-->
<!--              <Delete @click="handleClearMessage(activeSession.id)" />-->
<!--            </el-icon>-->
<!--            <el-icon :size="20">-->
<!--              &lt;!&ndash; 不处于编辑状态显示编辑按钮 &ndash;&gt;-->
<!--              <EditPen v-if="!isEdit" @click="isEdit = true" />-->
<!--              &lt;!&ndash; 处于编辑状态显示取消编辑按钮 &ndash;&gt;-->
<!--              <Close v-else @click="isEdit = false"></Close>-->
<!--            </el-icon>-->
<!--          </div>-->
<!--        </div>-->
<!--        <el-divider :border-style="'solid'" />-->
<!--        <div ref="messageListRef" class="message-list">-->
<!--          &lt;!&ndash; 过渡效果 &ndash;&gt;-->
<!--          <transition-group name="list" v-if="activeSession">-->
<!--            <message-row-->
<!--              v-for="message in activeSession.messages"-->
<!--              :key="message.id"-->
<!--              :message="message"-->
<!--            ></message-row>-->
<!--          </transition-group>-->
<!--        </div>-->
<!--        &lt;!&ndash;         监听发送事件 &ndash;&gt;-->
<!--        <message-input @send="handleSendMessage" v-if="activeSession"></message-input>-->
<!--      </div>-->
<!--      <div class="option-panel">-->
<!--        <el-form size="small">-->
<!--          <el-form-item>-->
<!--            <el-upload-->
<!--              v-loading="embeddingLoading"-->
<!--              :action="`${API_PREFIX}/document/embedding`"-->
<!--              :show-file-list="false"-->
<!--              :on-success="onUploadSuccess"-->
<!--              :before-upload="beforeUpload"-->
<!--            >-->
<!--              <el-button type="primary">上传文档</el-button>-->
<!--            </el-upload>-->
<!--          </el-form-item>-->
<!--          <el-form-item label="知识库">-->
<!--            <el-switch v-model="options.enableVectorStore"></el-switch>-->
<!--          </el-form-item>-->
<!--        </el-form>-->
<!--      </div>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->
<!--<style lang="scss" scoped>-->
<!--.home-view {-->
<!--  width: 100vw;-->
<!--  height: 100vh;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->

<!--  .chat-panel {-->
<!--    display: flex;-->
<!--    background-color: white;-->
<!--    width: 90%;-->
<!--    height: 90%;-->
<!--    box-shadow: 0 0 10px rgba(black, 0.1);-->
<!--    border-radius: 10px;-->

<!--    .session-panel {-->
<!--      display: flex;-->
<!--      flex-direction: column;-->
<!--      box-sizing: border-box;-->
<!--      padding: 20px;-->
<!--      position: relative;-->
<!--      border-right: 1px solid rgba(black, 0.07);-->
<!--      background-color: rgb(231, 200, 255);-->
<!--      height: 100%;-->
<!--      /* 标题 */-->
<!--      .title {-->
<!--        margin-top: 20px;-->
<!--        font-size: 20px;-->
<!--      }-->

<!--      .session-list {-->
<!--        overflow-y: scroll;-->
<!--        margin: 20px 0;-->
<!--        flex: 1;-->

<!--        .session {-->
<!--          /* 每个会话之间留一些间距 */-->
<!--          margin-top: 20px;-->
<!--        }-->

<!--        .session:first-child {-->
<!--          margin-top: 0;-->
<!--        }-->
<!--      }-->

<!--      .button-wrapper {-->
<!--        /* entity-panel是相对布局，这边的button-wrapper是相对它绝对布局 */-->
<!--        bottom: 20px;-->
<!--        left: 0;-->
<!--        display: flex;-->
<!--        /* 让内部的按钮显示在右侧 */-->
<!--        justify-content: flex-end;-->
<!--        /* 宽度和session-panel一样宽*/-->
<!--        width: 100%;-->

<!--        /* 按钮于右侧边界留一些距离 */-->
<!--        .new-session {-->
<!--          margin-right: 20px;-->
<!--        }-->
<!--      }-->
<!--    }-->

<!--    /* 右侧消息记录面板*/-->
<!--    .message-panel {-->
<!--      width: 100%;-->
<!--      height: 100%;-->
<!--      display: flex;-->
<!--      flex-direction: column;-->

<!--      .header {-->
<!--        padding: 20px 20px 0 20px;-->
<!--        display: flex;-->
<!--        /* 会话名称和编辑按钮在水平方向上分布左右两边 */-->
<!--        justify-content: space-between;-->

<!--        /* 前部的标题和消息条数 */-->
<!--        .front {-->
<!--          .title {-->
<!--            color: rgba(black, 0.7);-->
<!--            font-size: 20px;-->
<!--          }-->

<!--          .description {-->
<!--            margin-top: 10px;-->
<!--            color: rgba(black, 0.5);-->
<!--          }-->
<!--        }-->

<!--        /* 尾部的编辑和取消编辑按钮 */-->
<!--        .rear {-->
<!--          display: flex;-->
<!--          align-items: center;-->
<!--        }-->
<!--      }-->

<!--      .message-list {-->
<!--        padding: 15px;-->
<!--        width: 100%;-->
<!--        flex: 1;-->
<!--        box-sizing: border-box;-->
<!--        // 消息条数太多时，溢出部分滚动-->
<!--        overflow-y: scroll;-->
<!--        // 当切换聊天会话时，消息记录也随之切换的过渡效果-->
<!--        .list-enter-active,-->
<!--        .list-leave-active {-->
<!--          transition: all 0.5s ease;-->
<!--        }-->

<!--        .list-enter-from,-->
<!--        .list-leave-to {-->
<!--          opacity: 0;-->
<!--          transform: translateX(30px);-->
<!--        }-->
<!--      }-->
<!--    }-->

<!--    //  选项面板-->
<!--    .option-panel {-->
<!--      width: 200px;-->
<!--      padding: 20px;-->
<!--      border-left: 1px solid rgba(black, 0.07);-->

<!--      .upload {-->
<!--        width: 160px;-->
<!--      }-->
<!--    }-->
<!--  }-->
<!--}-->
<!--</style>-->
<script lang="ts" setup>
import { ChatRound, Close, Delete, EditPen } from '@element-plus/icons-vue'
import { nextTick, onMounted, ref } from 'vue'
import SessionItem from './components/session-item.vue'
import { storeToRefs } from 'pinia'
import { ElIcon, type UploadProps, type UploadUserFile } from 'element-plus'
import { api } from '@/utils/api-instance'
import { ElMessage } from 'element-plus'
import { type AiMessage, useChatStore } from './store/chat-store'
import MessageRow from '@/views/chat/components/message-row.vue'
import MessageInput from '@/views/chat/components/message-input.vue'
import { SSE } from 'sse.js'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
// import type { AiMessageParams, AiMessageWrapper } from '@/apis/__generated/model/static'
// 在setup中添加
const router = useRouter()

const handleLogout = () => {
  // 这里可以添加清除token等登出逻辑
 
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      localStorage.removeItem('token')
      router.push('/login')
    })
    .catch(() => {
      // 用户点击取消
    })
  
}
type ChatResponse = {
  metadata: {
    usage: {
      totalTokens: number
    }
  }
  result: {
    metadata: {
      finishReason: string
    }
    output: {
      messageType: string
      content: string
    }
  }
}

const API_PREFIX = import.meta.env.VITE_API_PREFIX
const chatStore = useChatStore()
const { handleDeleteSession, handleUpdateSession, handleClearMessage, handleCreateSession } =
  chatStore
const { activeSession, sessionList } = storeToRefs(chatStore)
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

const isEdit = ref(false)

// ChatGPT的回复
const responseMessage = ref<AiMessage>({
  id: new Date().getTime().toString(),
  type: 'ASSISTANT',
  medias: [],
  textContent: 'multipart/form-data',
  sessionId: ''
})

const handleSendMessage = async (message: { text: string; image: string }) => {
  if (!activeSession.value) {
    ElMessage.warning('请创建会话')
    return
  }

  // 图片/语音
  const medias: AiMessage['medias'] = []
  if (message.image) {
    medias.push({ type: 'image', data: message.image })
  }
  const now = new Date().toISOString()
  // 用户的提问
  const chatMessage = {
    id: new Date().getTime().toString(),
    sessionId: activeSession.value.id,
    medias,
    textContent: message.text,
    type: 'USER',
    createdTime: now, // 添加这行
    editedTime: now // 添加这行
  } satisfies AiMessage

  responseMessage.value = {
    id: new Date().getTime().toString(),
    medias: [],
    type: 'ASSISTANT',
    textContent: '',
    sessionId: activeSession.value.id
  }

  const form = new FormData()
  // 将消息对象转换为JSON字符串
  form.append(
    'input',
    JSON.stringify({
      message: chatMessage,
      params: options.value
    })
  )

  if (fileList.value.length && fileList.value[0].raw) {
    form.append('file', fileList.value[0].raw)
  }

  // 使用SSE发送请求
  const evtSource = new SSE(API_PREFIX + '/message/chat', {
    withCredentials: true,
    start: false, // 禁用自动启动，需要调用stream()方法才能发起请求
    method: 'POST',
    payload: form
  })

  evtSource.addEventListener('message', async (event: any) => {
    const response = JSON.parse(event.data) as ChatResponse
    const finishReason = response.result.metadata.finishReason
    if (response.result.output.content) {
      responseMessage.value.textContent += response.result.output.content
      // 滚动到底部
      await nextTick(() => {
        messageListRef.value?.scrollTo(0, messageListRef.value.scrollHeight)
      })
    }
    if (finishReason && finishReason.toLowerCase() == 'stop') {
      evtSource.close()
      // 保存用户的提问
      api.aiMessageController.save({ body: chatMessage })
      // 保存大模型的回复
      api.aiMessageController.save({ body: responseMessage.value })
    }
  })

  // 调用stream，发起请求。
  evtSource.stream()
  // 将两条消息显示在页面中
  activeSession.value.messages.push(...[chatMessage, responseMessage.value])
  await nextTick(() => {
    messageListRef.value?.scrollTo(0, messageListRef.value.scrollHeight)
  })
}

const handleSessionCreate = () => {
  chatStore.handleCreateSession({ name: '新的聊天' })
}

const options = ref<AiMessageParams>({
  enableVectorStore: false,
  enableAgent: false
})

const embeddingLoading = ref(false)
const onUploadSuccess = () => {
  embeddingLoading.value = false
  ElMessage.success('上传成功')
}

const beforeUpload: UploadProps['beforeUpload'] = () => {
  embeddingLoading.value = true
  return true
}

const fileList = ref<UploadUserFile[]>([])
</script>

<template>
  <div class="home-view">
   
    <div class="nav-bar">
      <el-menu mode="horizontal" :router="true" style="width: 100%">
        <el-menu-item index="/">AI 助手</el-menu-item>
        <el-menu-item index="/chatroom">公共聊天室</el-menu-item>
      </el-menu>
    </div>
    <div class="chat-panel" v-loading="loading">
      <div class="session-panel">
        <div class="title">Agent4U</div>
        <div class="session-list" v-if="activeSession">
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
          <el-button
      type="danger"
      size="small"
      @click="handleLogout"
      >退出登录
    </el-button>
        </div>
      </div>

      <div class="message-panel">
        <div class="header" v-if="activeSession">
          <div class="front">
            <div v-if="isEdit" class="title">
              <el-input
                v-model="activeSession.name"
                @keydown.enter="handleUpdateSession"
              ></el-input>
            </div>
            <div v-else class="title">{{ activeSession.name }}</div>
            <div class="description">{{ activeSession.messages.length }}条对话</div>
          </div>
          <div class="rear">
            <el-icon :size="20" style="margin-right: 10px">
              <Delete @click="handleClearMessage(activeSession.id)" />
            </el-icon>
            <el-icon :size="20">
              <EditPen v-if="!isEdit" @click="isEdit = true" />
              <Close v-else @click="isEdit = false"></Close>
            </el-icon>
          </div>
        </div>
        <el-divider :border-style="'solid'" />
        <div ref="messageListRef" class="message-list">
          <transition-group name="list" v-if="activeSession">
            <message-row
              v-for="message in activeSession.messages"
              :key="message.id"
              :message="message"
            ></message-row>
          </transition-group>
        </div>
        <message-input @send="handleSendMessage" v-if="activeSession"></message-input>
      </div>
      <div class="option-panel">
        <el-form size="small">
          <el-form-item>
            <el-upload
              v-loading="embeddingLoading"
              :action="`${API_PREFIX}/document/embedding`"
              :show-file-list="false"
              :on-success="onUploadSuccess"
              :before-upload="beforeUpload"
            >
              <el-button type="primary">上传文档</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="知识库">
            <el-switch v-model="options.enableVectorStore"></el-switch>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
// .home-view {
//   width: 100vw;
//   height: 100vh;
//   display: flex;
//   align-items: center;
//   justify-content: center;

//   .chat-panel {
//     display: flex;
//     background-color: white;
//     width: 90%;
//     height: 90%;
//     box-shadow: 0 0 10px rgba(black, 0.1);
//     border-radius: 10px;

//     .session-panel {
//       display: flex;
//       flex-direction: column;
//       box-sizing: border-box;
//       padding: 20px;
//       position: relative;
//       border-right: 1px solid rgba(black, 0.07);
//       background-color: rgb(231, 200, 255);
//       height: 100%;
//       /* 标题 */
//       .title {
//         margin-top: 20px;
//         font-size: 20px;
//       }

//       .session-list {
//         overflow-y: scroll;
//         margin: 20px 0;
//         flex: 1;

//         .session {
//           /* 每个会话之间留一些间距 */
//           margin-top: 20px;
//         }

//         .session:first-child {
//           margin-top: 0;
//         }
//       }

//       .button-wrapper {
//         /* entity-panel是相对布局，这边的button-wrapper是相对它绝对布局 */
//         bottom: 20px;
//         left: 0;
//         display: flex;
//         /* 让内部的按钮显示在右侧 */
//         justify-content: flex-end;
//         /* 宽度和session-panel一样宽*/
//         width: 100%;

//         /* 按钮于右侧边界留一些距离 */
//         .new-session {
//           margin-right: 20px;
//         }
//       }
//     }

//     /* 右侧消息记录面板*/
//     .message-panel {
//       width: 100%;
//       height: 100%;
//       display: flex;
//       flex-direction: column;

//       .header {
//         padding: 20px 20px 0 20px;
//         display: flex;
//         /* 会话名称和编辑按钮在水平方向上分布左右两边 */
//         justify-content: space-between;

//         /* 前部的标题和消息条数 */
//         .front {
//           .title {
//             color: rgba(black, 0.7);
//             font-size: 20px;
//           }

//           .description {
//             margin-top: 10px;
//             color: rgba(black, 0.5);
//           }
//         }

//         /* 尾部的编辑和取消编辑按钮 */
//         .rear {
//           display: flex;
//           align-items: center;
//         }
//       }

//       .message-list {
//         padding: 15px;
//         width: 100%;
//         flex: 1;
//         box-sizing: border-box;
//         // 消息条数太多时，溢出部分滚动
//         overflow-y: scroll;
//         // 当切换聊天会话时，消息记录也随之切换的过渡效果
//         .list-enter-active,
//         .list-leave-active {
//           transition: all 0.5s ease;
//         }

//         .list-enter-from,
//         .list-leave-to {
//           opacity: 0;
//           transform: translateX(30px);
//         }
//       }
//     }

//     //  选项面板
//     .option-panel {
//       width: 200px;
//       padding: 20px;
//       border-left: 1px solid rgba(black, 0.07);

//       .upload {
//         width: 160px;
//       }
//     }
//   }
// }
.nav-bar {
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 1000;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.home-view {
  padding-top: 60px; 
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); // 添加渐变背景

  .chat-panel {
    display: flex;
    background-color: rgba(255, 255, 255, 0.95); // 略微透明
    width: 90%;
    height: 90%;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1); // 更柔和的阴影
    border-radius: 20px; // 更圆润的边角
    backdrop-filter: blur(10px); // 毛玻璃效果

    .session-panel {
      // ... 保持其他属性不变 ...
      display: flex;
      flex-direction: column;
      box-sizing: border-box;
      padding: 20px;
      position: relative;
      border-right: 1px solid rgba(black, 0.07);
      background-color: rgb(231, 200, 255);
      height: 100%;
      background: linear-gradient(180deg, #9795f0 0%, #fbc8d4 100%); // 更现代的渐变背景
      min-width: 260px; // 确保最小宽度
      border-radius: 20px 0 0 20px; // 左侧圆角

      .title {
        margin-top: 20px;
        font-size: 24px;
        font-weight: 600;
        color: white;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); // 文字阴影
      }

      .session-list {
        overflow-y: scroll;
        margin: 20px 0;
        flex: 1;

        // ... 保持其他属性不变 ...
        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-thumb {
          background-color: rgba(255, 255, 255, 0.3);
          border-radius: 3px;
        }

        .session {
          transition: transform 0.2s ease; // 添加悬停动画

          &:hover {
            transform: translateX(5px);
          }
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
          // ... 保持其他属性不变 ...
          .el-button {
            background: rgba(255, 255, 255, 0.2);
            border: none;
            color: white;
            backdrop-filter: blur(5px);
            transition: all 0.3s ease;

            &:hover {
              background: rgba(255, 255, 255, 0.3);
              transform: translateY(-2px);
            }
          }
        }
      }
    }

    .message-panel {
      width: 70%;
      height: 100%;
      display: flex;
      flex-direction: column;
      // ... 保持其他属性不变 ...
      background: rgba(255, 255, 255, 0.5);

      .header {
        .front {
          text-align: center;
          .title {
            font-size: 22px;
            font-weight: 600;
            color: #2c3e50;
          }

          .description {
            color: #7f8c8d;
          }
        }

        .rear {
          .el-icon {
            display: flex;
            justify-content: flex-end;
            margin-left: auto;
            cursor: pointer;
            transition: all 0.3s ease;
            color: #7f8c8d;

            &:hover {
              color: #2c3e50;
              transform: scale(1.1);
            }
          }
        }
      }

      .message-list {
        padding: 15px;
        width: 100%;
        flex: 1;
        box-sizing: border-box;
        // 消息条数太多时，溢出部分滚动
        overflow-y: scroll;
        // 当切换聊天会话时，消息记录也随之切换的过渡效果
        .list-enter-active,
        .list-leave-active {
          transition: all 0.5s ease;
        }
        //... 保持其他属性不变 ...
        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-thumb {
          background-color: rgba(0, 0, 0, 0.1);
          border-radius: 3px;
        }
      }
    }

    .option-panel {
      background: rgba(255, 255, 255, 0.7);
      border-radius: 0 20px 20px 0; // 右侧圆角
      border-left: 1px solid rgba(0, 0, 0, 0.05);

      .el-button {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
        }
      }

      .el-switch {
        &.is-checked {
          .el-switch__core {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }
        }
      }
    }
  }
}
</style>

