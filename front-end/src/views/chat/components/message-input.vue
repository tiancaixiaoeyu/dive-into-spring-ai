<script lang="ts" setup>
import { ref } from 'vue'
import { Position } from '@element-plus/icons-vue'
import ImageUpload from '@/components/image/image-upload.vue'
import { ElMessage } from 'element-plus'
type Message = {
  text: string
  image: string
}
// 发送消息消息事件
const emit = defineEmits<{
  send: [message: Message]
}>()
// 输入框内的消息
const message = ref<Message>({ text: '', image: '' })
const sendMessage = () => {
  if (!message.value.text) {
    ElMessage.warning('请输入消息')
    return
  }
  emit('send', message.value)
  // 发送完清除
  message.value = { text: '', image: '' }
}
</script>

<template>
  <div class="message-input">
    <div class="input-wrapper">
      <el-input
        v-model="message.text"
        :autosize="false"
        :rows="3"
        class="input"
        resize="none"
        type="textarea"
        @keydown.enter.prevent="sendMessage"
      >
      </el-input>
      <div class="button-wrapper liquid-bar">
        <image-upload class="image" :size="40" v-model="message.image"></image-upload>
        <el-button class="send-btn" @click="sendMessage">
          <el-icon class="el-icon--left">
            <Position />
          </el-icon>
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.message-input {
  padding: 16px 16px 10px;
  border-top: 1px solid rgba(123, 230, 255, 0.2);
  background: linear-gradient(180deg, rgba(9, 16, 38, 0.7), rgba(8, 14, 34, 0.9)),
    radial-gradient(circle at 12% 120%, rgba(97, 252, 255, 0.2), transparent 45%);
  border-top-right-radius: 12px;
  border-top-left-radius: 12px;
  .el-form-item {
    align-items: center;
  }
  .input-wrapper {
    position: relative;
  }

  :deep(.el-textarea__inner) {
    color: #d6f5ff;
    background: rgba(15, 27, 59, 0.7);
    border: 1px solid rgba(121, 229, 255, 0.2);
    border-radius: 14px;
    transition: all 0.28s ease;
    box-shadow: inset 0 0 16px rgba(93, 238, 255, 0.08);
  }
  :deep(.el-textarea__inner:focus) {
    border-color: rgba(125, 247, 255, 0.62);
    box-shadow:
      0 0 20px rgba(93, 238, 255, 0.25),
      0 0 40px rgba(255, 103, 218, 0.16),
      inset 0 0 18px rgba(95, 240, 255, 0.16);
  }

  .input-wrapper::before {
    content: '';
    position: absolute;
    left: 12%;
    top: -2px;
    width: 42%;
    height: 6px;
    background: linear-gradient(
      90deg,
      rgba(103, 250, 255, 0),
      rgba(103, 250, 255, 0.86),
      rgba(255, 92, 220, 0)
    );
    filter: blur(3px);
    opacity: 0;
    transition: opacity 0.2s ease;
  }
  .input-wrapper:focus-within::before {
    opacity: 1;
    animation: beamPulse 1.4s ease-in-out infinite;
  }
}

.button-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 14px 14px 6px;
  margin-top: 8px;
  gap: 14px;
  .image {
    margin-right: 6px;
  }
}

.liquid-bar {
  border-radius: 14px;
  border: 1px solid rgba(114, 224, 255, 0.24);
  background: linear-gradient(130deg, rgba(42, 72, 139, 0.42), rgba(15, 28, 66, 0.66)),
    radial-gradient(circle at 20% 10%, rgba(110, 250, 255, 0.22), transparent 42%);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.2),
    0 0 20px rgba(90, 232, 255, 0.15);
}

.send-btn {
  border: 1px solid rgba(116, 237, 255, 0.36);
  color: #d5f8ff;
  background: linear-gradient(135deg, rgba(98, 243, 255, 0.25), rgba(255, 102, 215, 0.2));
  box-shadow:
    inset 0 0 20px rgba(118, 247, 255, 0.22),
    0 0 20px rgba(255, 96, 212, 0.2);
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease;
}

.send-btn:hover {
  transform: translateY(-1px);
  box-shadow:
    inset 0 0 24px rgba(118, 247, 255, 0.35),
    0 0 26px rgba(255, 96, 212, 0.3);
}

.send-btn:active {
  transform: scale(0.92);
  animation: rebound 0.34s ease;
}

@keyframes beamPulse {
  0%,
  100% {
    transform: scaleX(0.96);
    opacity: 0.75;
  }
  50% {
    transform: scaleX(1.08);
    opacity: 1;
  }
}

@keyframes rebound {
  0% {
    transform: scale(0.9);
  }
  45% {
    transform: scale(1.04);
  }
  100% {
    transform: scale(1);
  }
}
</style>
