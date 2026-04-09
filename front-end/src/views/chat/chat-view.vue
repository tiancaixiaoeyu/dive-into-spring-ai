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
import {
  ChatLineRound,
  ChatRound,
  Close,
  Delete,
  EditPen,
  Service,
  User
} from '@element-plus/icons-vue'
import { computed, nextTick, onMounted, ref } from 'vue'
import SessionItem from './components/session-item.vue'
import { storeToRefs } from 'pinia'
import { ElIcon, type UploadProps, type UploadUserFile } from 'element-plus'
import { api } from '@/utils/api-instance'
import { request } from '@/utils/request'
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

  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
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

type CitationPayload = {
  items: Array<{
    title: string
    content: string
    sourceUrl: string
    chunkIndex: string
    sourceType: string
  }>
}

type ExercisePayload = {
  type: 'single_choice' | 'fill_blank'
  stem: string
  options: string[]
  answer: string
  explanation: string
  extension: string
}

type ExerciseRecordItem = {
  id: string
  question: string
  userAnswer: string
  correctAnswer: string
  correct: boolean
  exerciseType: string
  explanation?: string
  extension?: string
  gradeLevel?: string
  mode?: string
  options?: string[]
  sessionId: string
  createdTime: string
}

type AiMessageParams = {
  enableVectorStore: boolean
  enableAgent: boolean
  enableCitation: boolean
  mode: 'explain' | 'question' | 'recite' | 'practice'
  gradeLevel: string
}

const API_PREFIX = import.meta.env.VITE_API_PREFIX
const chatStore = useChatStore()
const { handleDeleteSession, handleUpdateSession, handleClearMessage } = chatStore
const { activeSession, sessionList } = storeToRefs(chatStore)
const messageListRef = ref<InstanceType<typeof HTMLDivElement>>()
const loading = ref(true)
const citations = ref<CitationPayload['items']>([])
const practiceExercise = ref<ExercisePayload | null>(null)
const practiceAnswer = ref('')
const practiceSubmitted = ref(false)
const practiceRecordSaved = ref(false)
const recentExerciseRecords = ref<ExerciseRecordItem[]>([])
const wrongExerciseRecords = ref<ExerciseRecordItem[]>([])
const exercisePanelLoading = ref(false)

const buildAuthHeaders = (): Record<string, string> => {
  const token = localStorage.getItem('token')
  if (!token) {
    return {}
  }
  return {
    token,
    Authorization: token
  }
}

const toSavePayload = (message: AiMessage) => {
  return {
    sessionId: message.sessionId,
    medias: message.medias,
    textContent: message.textContent,
    type: message.type
  }
}

const loadExerciseRecords = async () => {
  exercisePanelLoading.value = true
  try {
    const [recent, wrong] = await Promise.all([
      request.get('/exercise-record/user', { params: { limit: 6 } }),
      request.get('/exercise-record/user', { params: { limit: 6, wrongOnly: true } })
    ])
    recentExerciseRecords.value = recent as unknown as ExerciseRecordItem[]
    wrongExerciseRecords.value = wrong as unknown as ExerciseRecordItem[]
  } catch {
    recentExerciseRecords.value = []
    wrongExerciseRecords.value = []
  } finally {
    exercisePanelLoading.value = false
  }
}

const saveExerciseRecord = async () => {
  if (!practiceExercise.value || !activeSession.value || practiceRecordSaved.value) {
    return
  }
  await request.post('/exercise-record', {
    sessionId: activeSession.value.id,
    question: practiceExercise.value.stem,
    userAnswer: practiceAnswer.value.trim(),
    correctAnswer: practiceExercise.value.answer,
    correct: exerciseIsCorrect.value,
    exerciseType: practiceExercise.value.type,
    explanation: practiceExercise.value.explanation,
    extension: practiceExercise.value.extension,
    gradeLevel: options.value.gradeLevel,
    mode: options.value.mode,
    options: practiceExercise.value.options
  })
  practiceRecordSaved.value = true
  await loadExerciseRecords()
}

const modeDescriptions: Record<AiMessageParams['mode'], { title: string; detail: string }> = {
  explain: {
    title: '讲解模式',
    detail: '适合理解诗意、作者情感和考试考点。'
  },
  question: {
    title: '提问模式',
    detail: '通过追问和引导，让学生自己想出答案。'
  },
  recite: {
    title: '背诵模式',
    detail: '按首字、关键词、整句逐层提示，适合背诵训练。'
  },
  practice: {
    title: '练习模式',
    detail: '自动生成小题并给出答案解析，适合课堂巩固。'
  }
}

const gradeDescriptions: Record<string, string> = {
  小学低年级: '表达更口语化，重点讲字面意思和画面感。',
  小学中年级: '加入基础情感与修辞解释，适合建立赏析意识。',
  小学高年级: '补充主旨、意境和常见考点，适合综合理解。',
  初中: '引入文学术语和答题思路，贴近考试表达。'
}

const resolveCitationUrl = (sourceUrl: string) => {
  if (!sourceUrl) {
    return ''
  }
  if (sourceUrl.startsWith('http://') || sourceUrl.startsWith('https://')) {
    return sourceUrl
  }
  return `${window.location.origin}${API_PREFIX}${sourceUrl}`
}

const handleOpenCitation = (citation: CitationPayload['items'][number]) => {
  const url = resolveCitationUrl(citation.sourceUrl)
  if (!url) {
    ElMessage.info('当前依据暂无可跳转来源')
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}

const handleSubmitExercise = () => {
  if (!practiceAnswer.value) {
    ElMessage.warning('请先完成作答')
    return
  }
  practiceSubmitted.value = true
  saveExerciseRecord().catch(() => {
    ElMessage.warning('练习记录保存失败')
  })
}

const normalizedExerciseAnswer = computed(() => practiceAnswer.value.trim().toUpperCase())
const exerciseAnswerDisplay = computed(
  () => practiceExercise.value?.answer.trim().toUpperCase() ?? ''
)
const exerciseIsCorrect = computed(
  () => normalizedExerciseAnswer.value === exerciseAnswerDisplay.value
)

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    loading.value = false
    await router.replace('/login')
    return
  }
  try {
    const res = await api.aiSessionController.findByUser()
    sessionList.value = res.map((row) => ({
      ...row,
      messages: row.messages.map((message) => ({
        id: message.id,
        sessionId: message.sessionId,
        medias: (message.medias ?? []).map((media) => ({
          type: media.type,
          data: media.data
        })),
        textContent: message.textContent,
        type: message.type as AiMessage['type'],
        createdTime: message.createdTime,
        editedTime: message.editedTime
      }))
    }))
    if (sessionList.value.length > 0) {
      activeSession.value = sessionList.value[0]
    } else {
      await chatStore.handleCreateSession({ name: '新的绘画' })
    }
    await loadExerciseRecords()
  } catch {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    await router.replace('/login')
  } finally {
    loading.value = false
  }
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
  citations.value = []
  practiceExercise.value = null
  practiceAnswer.value = ''
  practiceSubmitted.value = false
  practiceRecordSaved.value = false

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
    headers: buildAuthHeaders(),
    payload: form as unknown as string
  } as any)

  evtSource.addEventListener('citation', (event: any) => {
    const payload = JSON.parse(event.data) as CitationPayload
    citations.value = payload.items ?? []
  })

  evtSource.addEventListener('exercise', (event: any) => {
    practiceExercise.value = JSON.parse(event.data) as ExercisePayload
    practiceAnswer.value = ''
    practiceSubmitted.value = false
    practiceRecordSaved.value = false
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
      request.post('/message', toSavePayload(chatMessage))
      request.post('/message', toSavePayload(responseMessage.value))
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
  enableAgent: false,
  enableCitation: true,
  mode: 'explain',
  gradeLevel: '小学高年级'
})

const activeModeHint = computed(() => modeDescriptions[options.value.mode])
const activeGradeHint = computed(() => gradeDescriptions[options.value.gradeLevel] ?? '')

const embeddingLoading = ref(false)
const onUploadSuccess = () => {
  embeddingLoading.value = false
  ElMessage.success('上传成功')
}

const beforeUpload: UploadProps['beforeUpload'] = () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('登录状态已失效，请重新登录')
    router.replace('/login')
    return false
  }
  embeddingLoading.value = true
  return true
}

const fileList = ref<UploadUserFile[]>([])
</script>

<template>
  <div class="home-view">
    <div class="nav-bar holo-glass">
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
            class="holo-btn"
            style="margin-right: 20px"
            :icon="ChatRound"
            size="small"
            @click="handleSessionCreate"
            >创建会话
          </el-button>
          <el-button class="holo-btn danger" size="small" @click="handleLogout"
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
        <el-divider :border-style="'solid'" class="holo-divider" />
        <div ref="messageListRef" class="message-list">
          <div class="exercise-panel" v-if="practiceExercise">
            <div class="exercise-title">随堂练习</div>
            <div class="exercise-stem">{{ practiceExercise.stem }}</div>
            <div class="exercise-options" v-if="practiceExercise.type === 'single_choice'">
              <el-radio-group v-model="practiceAnswer" class="exercise-radio-group">
                <el-radio
                  v-for="option in practiceExercise.options"
                  :key="option"
                  :label="option.charAt(0)"
                  class="exercise-option"
                >
                  {{ option }}
                </el-radio>
              </el-radio-group>
            </div>
            <div class="exercise-options" v-else>
              <el-input
                v-model="practiceAnswer"
                class="exercise-input"
                placeholder="请输入你的答案"
              />
            </div>
            <div class="exercise-actions">
              <el-button class="holo-btn" size="small" @click="handleSubmitExercise"
                >提交作答</el-button
              >
            </div>
            <div class="exercise-result" v-if="practiceSubmitted">
              <div :class="['exercise-judge', exerciseIsCorrect ? 'correct' : 'wrong']">
                {{ exerciseIsCorrect ? '回答正确' : '再想一想' }}
              </div>
              <div class="exercise-answer">标准答案：{{ practiceExercise.answer }}</div>
              <div class="exercise-explanation">解析：{{ practiceExercise.explanation }}</div>
              <div class="exercise-extension">举一反三：{{ practiceExercise.extension }}</div>
            </div>
          </div>
          <div class="citation-panel" v-if="citations.length">
            <div class="citation-title">知识依据</div>
            <div class="citation-list">
              <div
                class="citation-card"
                v-for="(citation, index) in citations"
                :key="`${citation.title}-${index}`"
                @click="handleOpenCitation(citation)"
              >
                <div class="citation-source">
                  <span>{{ citation.title }}</span>
                  <span v-if="citation.chunkIndex" class="citation-meta"
                    >片段 {{ citation.chunkIndex }}</span
                  >
                </div>
                <div class="citation-content">{{ citation.content }}</div>
                <div class="citation-link" v-if="citation.sourceUrl">查看来源</div>
              </div>
            </div>
          </div>
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
      <div class="option-panel holo-glass">
        <el-form size="small">
          <el-form-item>
            <el-upload
              v-loading="embeddingLoading"
              :action="`${API_PREFIX}/document/embedding`"
              :headers="buildAuthHeaders()"
              :show-file-list="false"
              :on-success="onUploadSuccess"
              :before-upload="beforeUpload"
            >
              <el-button class="holo-upload">上传文档</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item class="knowledge-switch" label="知识库">
            <el-switch v-model="options.enableVectorStore"></el-switch>
          </el-form-item>
          <el-form-item class="knowledge-switch" label="依据展示">
            <el-switch v-model="options.enableCitation"></el-switch>
          </el-form-item>
          <el-form-item label="教学模式">
            <el-select v-model="options.mode" style="width: 100%">
              <el-option label="讲解模式" value="explain" />
              <el-option label="提问模式" value="question" />
              <el-option label="背诵模式" value="recite" />
              <el-option label="练习模式" value="practice" />
            </el-select>
          </el-form-item>
          <el-form-item label="学段">
            <el-select v-model="options.gradeLevel" style="width: 100%">
              <el-option label="小学低年级" value="小学低年级" />
              <el-option label="小学中年级" value="小学中年级" />
              <el-option label="小学高年级" value="小学高年级" />
              <el-option label="初中" value="初中" />
            </el-select>
          </el-form-item>
          <div class="teaching-hint">
            <div class="teaching-hint__title">{{ activeModeHint.title }}</div>
            <div class="teaching-hint__text">{{ activeModeHint.detail }}</div>
            <div class="teaching-hint__title">当前学段</div>
            <div class="teaching-hint__text">{{ activeGradeHint }}</div>
          </div>
          <div class="record-panel" v-loading="exercisePanelLoading">
            <div class="record-panel__title">最近练习</div>
            <div class="record-empty" v-if="!recentExerciseRecords.length">暂无练习记录</div>
            <div class="record-list" v-else>
              <div class="record-card" v-for="record in recentExerciseRecords" :key="record.id">
                <div class="record-card__question">{{ record.question }}</div>
                <div :class="['record-card__status', record.correct ? 'correct' : 'wrong']">
                  {{ record.correct ? '已答对' : '待巩固' }}
                </div>
                <div class="record-card__meta">你的答案：{{ record.userAnswer }}</div>
              </div>
            </div>
          </div>
          <div class="record-panel danger-panel" v-loading="exercisePanelLoading">
            <div class="record-panel__title">错题本</div>
            <div class="record-empty" v-if="!wrongExerciseRecords.length">当前没有错题</div>
            <div class="record-list" v-else>
              <div class="record-card" v-for="record in wrongExerciseRecords" :key="record.id">
                <div class="record-card__question">{{ record.question }}</div>
                <div class="record-card__meta">你的答案：{{ record.userAnswer }}</div>
                <div class="record-card__meta">标准答案：{{ record.correctAnswer }}</div>
              </div>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.holo-glass {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(139, 90, 43, 0.1);
  box-shadow: 0 4px 20px rgba(139, 90, 43, 0.06);
  backdrop-filter: blur(12px);
}

.nav-bar {
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 8px rgba(139, 90, 43, 0.06);
  :deep(.custom-menu) {
    background: transparent;
    border-bottom: none;
  }
  :deep(.el-menu-item) {
    color: #5D4037;
    transition: transform 0.25s ease;
  }
  :deep(.el-menu-item.is-active) {
    color: #D4804A;
  }
  :deep(.el-menu-item:hover) {
    background: rgba(212, 128, 74, 0.06);
    transform: translateY(-1px);
  }
}

.home-view {
  padding-top: 60px;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;

  .chat-panel {
    display: flex;
    width: 90%;
    height: 90%;
    border-radius: 18px;
    overflow: hidden;
    border: 1px solid rgba(139, 90, 43, 0.08);
    background: white;
    box-shadow: 0 8px 40px rgba(139, 90, 43, 0.08);

    .session-panel {
      display: flex;
      flex-direction: column;
      box-sizing: border-box;
      padding: 24px 16px;
      position: relative;
      height: 100%;
      min-width: 280px;
      background: #FFF3E8;
      border-right: 1px solid rgba(139, 90, 43, 0.1);

      .title {
        margin-top: 8px;
        font-size: 24px;
        font-weight: 700;
        color: #3D2B1F;
        letter-spacing: 0.04em;
      }

      .session-list {
        overflow-y: auto;
        margin: 20px 0;
        flex: 1;
        scroll-behavior: smooth;
        overscroll-behavior: contain;

        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-thumb {
          background: rgba(139, 90, 43, 0.18);
          border-radius: 20px;
        }

        .session {
          transition: transform 0.24s ease;

          &:hover {
            transform: translateX(4px) scale(1.01);
          }
        }
      }

      .button-wrapper {
        display: flex;
        justify-content: flex-end;
        width: 100%;
        gap: 10px;

        :deep(.holo-btn) {
          border: 1px solid rgba(212, 128, 74, 0.3);
          color: #5D4037;
          background: rgba(212, 128, 74, 0.08);
          transition:
            transform 0.2s ease,
            box-shadow 0.2s ease;
        }

        :deep(.holo-btn:hover) {
          transform: translateY(-2px);
          background: rgba(212, 128, 74, 0.14);
          box-shadow: 0 4px 12px rgba(212, 128, 74, 0.15);
        }

        :deep(.holo-btn:active) {
          transform: scale(0.95);
        }

        :deep(.holo-btn.danger) {
          border-color: rgba(211, 47, 47, 0.3);
          color: #C62828;
          background: rgba(211, 47, 47, 0.06);
        }
      }
    }

    .message-panel {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      background: white;

      .header {
        padding: 22px 24px 10px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        .front {
          .title {
            font-size: 22px;
            font-weight: 600;
            color: #3D2B1F;
          }

          .description {
            margin-top: 6px;
            color: rgba(61, 43, 31, 0.55);
          }
        }

        .rear {
          display: flex;
          gap: 12px;
          .el-icon {
            cursor: pointer;
            transition: all 0.3s ease;
            color: #8D6E63;
            padding: 8px;
            border-radius: 10px;
            background: rgba(139, 90, 43, 0.06);
            border: 1px solid rgba(139, 90, 43, 0.1);

            &:hover {
              color: #D4804A;
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(212, 128, 74, 0.15);
            }
          }
        }
      }

      .message-list {
        padding: 12px 18px;
        width: 100%;
        flex: 1;
        box-sizing: border-box;
        overflow-y: auto;
        scroll-behavior: smooth;
        overscroll-behavior: contain;
        -webkit-overflow-scrolling: touch;
        background: #FEFBF7;
        border-top: 1px solid rgba(139, 90, 43, 0.08);
        border-bottom: 1px solid rgba(139, 90, 43, 0.08);

        .list-enter-active,
        .list-leave-active {
          transition: all 0.5s ease;
        }
        .list-enter-from,
        .list-leave-to {
          opacity: 0;
          transform: translateY(16px);
        }

        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-thumb {
          background: rgba(139, 90, 43, 0.18);
          border-radius: 999px;
        }

        .exercise-panel {
          margin-bottom: 14px;
          padding: 14px;
          border-radius: 14px;
          border: 1px solid rgba(212, 128, 74, 0.2);
          background: #FFF8F0;
        }

        .exercise-title {
          color: #D4804A;
          font-size: 14px;
          font-weight: 600;
          letter-spacing: 0.04em;
          margin-bottom: 10px;
        }

        .exercise-stem {
          color: #3D2B1F;
          font-size: 14px;
          line-height: 1.7;
          margin-bottom: 12px;
        }

        .exercise-radio-group {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .exercise-option {
          margin-right: 0;
          padding: 10px 12px;
          border-radius: 10px;
          background: white;
          border: 1px solid rgba(139, 90, 43, 0.12);
          color: #3D2B1F;
        }

        .exercise-input :deep(.el-input__wrapper) {
          background: white;
        }

        .exercise-actions {
          margin-top: 12px;
        }

        .exercise-result {
          margin-top: 14px;
          padding-top: 12px;
          border-top: 1px solid rgba(139, 90, 43, 0.1);
          color: #3D2B1F;
          line-height: 1.7;
        }

        .exercise-judge {
          font-weight: 700;
          margin-bottom: 8px;
        }

        .exercise-judge.correct {
          color: #2E7D32;
        }

        .exercise-judge.wrong {
          color: #C62828;
        }

        .exercise-answer,
        .exercise-explanation,
        .exercise-extension {
          font-size: 13px;
        }

        .citation-panel {
          margin-bottom: 14px;
          padding: 12px;
          border-radius: 12px;
          border: 1px solid rgba(212, 128, 74, 0.2);
          background: #FFF8F0;
        }

        .citation-title {
          color: #D4804A;
          font-size: 13px;
          font-weight: 600;
          letter-spacing: 0.04em;
          margin-bottom: 10px;
        }

        .citation-list {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .citation-card {
          border: 1px solid rgba(139, 90, 43, 0.12);
          border-radius: 10px;
          padding: 10px;
          background: white;
          cursor: pointer;
          transition:
            transform 0.2s ease,
            border-color 0.2s ease,
            box-shadow 0.2s ease;
        }

        .citation-card:hover {
          transform: translateY(-1px);
          border-color: rgba(212, 128, 74, 0.3);
          box-shadow: 0 4px 12px rgba(212, 128, 74, 0.1);
        }

        .citation-source {
          display: flex;
          justify-content: space-between;
          gap: 12px;
          font-size: 12px;
          color: #D4804A;
          margin-bottom: 6px;
        }

        .citation-meta {
          color: rgba(61, 43, 31, 0.6);
        }

        .citation-content {
          font-size: 13px;
          color: #5D4037;
          line-height: 1.5;
        }

        .citation-link {
          margin-top: 8px;
          font-size: 12px;
          color: #D4804A;
        }
      }
    }

    .option-panel {
      width: 220px;
      padding: 18px 14px;
      border-left: 1px solid rgba(139, 90, 43, 0.1);
      background: #FFFAF5;
      overflow-y: auto;

      :deep(.el-form-item__label) {
        color: #5D4037;
      }

      :deep(.holo-upload) {
        width: 100%;
      }
      :deep(.holo-upload:hover) {
        transform: translateY(-1px);
      }

      :deep(.knowledge-switch) {
        position: relative;
        overflow: hidden;
        border-radius: 12px;
        padding: 10px 8px;
      }

      .teaching-hint {
        margin-top: 10px;
        padding: 12px 10px;
        border-radius: 12px;
        border: 1px solid rgba(139, 90, 43, 0.1);
        background: #FFF8F0;
      }

      .teaching-hint__title {
        color: #D4804A;
        font-size: 12px;
        font-weight: 600;
        letter-spacing: 0.04em;
        margin-bottom: 4px;
      }

      .teaching-hint__title + .teaching-hint__title {
        margin-top: 10px;
      }

      .teaching-hint__text {
        color: #5D4037;
        font-size: 12px;
        line-height: 1.6;
      }

      .record-panel {
        margin-top: 12px;
        padding: 12px 10px;
        border-radius: 12px;
        border: 1px solid rgba(139, 90, 43, 0.1);
        background: #FFF8F0;
      }

      .danger-panel {
        border-color: rgba(198, 40, 40, 0.15);
        background: #FFF5F5;
      }

      .record-panel__title {
        color: #D4804A;
        font-size: 12px;
        font-weight: 600;
        letter-spacing: 0.04em;
        margin-bottom: 10px;
      }

      .record-empty {
        color: rgba(93, 64, 55, 0.6);
        font-size: 12px;
        line-height: 1.6;
      }

      .record-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .record-card {
        padding: 10px;
        border-radius: 10px;
        border: 1px solid rgba(139, 90, 43, 0.08);
        background: white;
      }

      .record-card__question {
        color: #3D2B1F;
        font-size: 12px;
        line-height: 1.55;
        margin-bottom: 6px;
      }

      .record-card__status {
        font-size: 12px;
        margin-bottom: 4px;
      }

      .record-card__status.correct {
        color: #2E7D32;
      }

      .record-card__status.wrong {
        color: #C62828;
      }

      .record-card__meta {
        color: rgba(93, 64, 55, 0.7);
        font-size: 12px;
        line-height: 1.5;
      }
    }
  }
}

.holo-divider {
  :deep(.el-divider__text) {
    background: transparent;
  }
  :deep(.el-divider--horizontal) {
    border-color: rgba(139, 90, 43, 0.1);
  }
}

@media (max-width: 1280px) {
  .home-view .chat-panel {
    width: 96%;
  }
  .home-view .chat-panel .option-panel {
    width: 180px;
  }
}

@media (max-width: 980px) {
  .home-view .chat-panel {
    flex-direction: row;
    height: 90vh;
    min-width: 1200px;
  }
  .home-view .chat-panel .session-panel {
    min-width: 260px;
    width: auto;
    max-height: none;
  }
  .home-view .chat-panel .option-panel {
    width: 180px;
    border-left: 1px solid rgba(139, 90, 43, 0.1);
    border-top: none;
  }
  .home-view .chat-panel .message-panel {
    min-width: 680px;
    flex: 1 1 auto;
  }
}
</style>
