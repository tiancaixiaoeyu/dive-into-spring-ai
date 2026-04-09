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
  border-top: 1px solid rgba(139, 90, 43, 0.1);
  background: white;
  border-top-right-radius: 10px;
  border-top-left-radius: 10px;
  .el-form-item {
    align-items: center;
  }
  .input-wrapper {
    position: relative;
  }

  :deep(.el-textarea__inner) {
    color: #3D2B1F;
    background: #FEFBF7;
    border: 1px solid rgba(139, 90, 43, 0.12);
    border-radius: 12px;
    transition: all 0.28s ease;
  }
  :deep(.el-textarea__inner:focus) {
    border-color: #D4804A;
    box-shadow: 0 0 0 2px rgba(212, 128, 74, 0.12);
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
  border-radius: 12px;
  border: 1px solid rgba(139, 90, 43, 0.08);
  background: #FFF8F0;
}

.send-btn {
  border: 1px solid rgba(212, 128, 74, 0.3);
  color: white;
  background: #D4804A;
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease;
}

.send-btn:hover {
  transform: translateY(-1px);
  background: #C06830;
  box-shadow: 0 4px 12px rgba(212, 128, 74, 0.25);
}

.send-btn:active {
  transform: scale(0.95);
}
</style>
