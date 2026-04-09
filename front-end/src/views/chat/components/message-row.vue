<script lang="ts" setup>
import TextLoading from './text-loading.vue'
import logo from '@/assets/logo.jpg'
import MarkdownMessage from './markdown-message.vue'
import type { AiMessage } from '../store/chat-store'
import { computed } from 'vue'
// message：接受消息对象，展示消息内容和头像，并且根据角色调整消息位置。
// avatar：用户头像，如果角色是 Assistant则使用 logo。
const props = defineProps<{
  message: AiMessage
  avatar?: string
}>()

const images = computed(() => {
  const medias = props.message.medias || []
  return medias.filter((media) => media.type === 'image').map((media) => media.data)
})
</script>

<!-- 整个div是用来调整内部消息的位置，每条消息占的空间都是一整行，然后根据right还是left来调整内部的消息是靠右边还是靠左边 -->
<template>
  <div :class="['message-row', message.type === 'USER' ? 'right' : 'left']">
    <!-- 消息展示，分为上下，上面是头像，下面是消息 -->
    <div class="row">
      <!-- 头像， -->
      <div class="avatar-wrapper">
        <el-avatar
          :src="avatar || logo"
          class="avatar"
          shape="square"
          v-if="message.type === 'USER'"
        />
        <el-avatar :src="logo" class="avatar" shape="square" v-else />
      </div>
      <!-- 发送的消息或者回复的消息 -->
      <div :class="['message', message.type === 'USER' ? 'user-bubble' : 'assistant-bubble']">
        <!-- 如果消息是文本，用markdown展示 -->
        <markdown-message
          :type="message.type"
          :message="message.textContent"
          v-if="message.textContent"
        ></markdown-message>
        <!-- 如果消息的内容是图片，则显示图片  -->
        <el-image
          v-for="image in images"
          :key="image"
          class="image"
          fit="cover"
          :preview-src-list="images"
          :src="image"
        ></el-image>
        <!-- 如果消息的内容为空则显示加载动画 -->
        <TextLoading v-if="!message.textContent && !images.length"></TextLoading>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.message-row {
  display: flex;
  margin-bottom: 14px;

  &.right {
    justify-content: flex-end;

    .row {
      .avatar-wrapper {
        display: flex;
        justify-content: flex-end;
      }

      .message {
        background: #FFF0E0;
        :deep(.md-editor) {
          background: transparent;
        }
      }
    }
  }

  .row {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .avatar-wrapper {
      .avatar {
        box-shadow: 0 2px 8px rgba(139, 90, 43, 0.08);
        margin-bottom: 6px;
      }
    }

    .message {
      font-size: 15px;
      padding: 2px;
      max-width: 800px;
      border-radius: 12px;
      border: 1px solid rgba(139, 90, 43, 0.1);
      background: #F9F5F0;

      .image {
        width: min(600px, 68vw);
        height: min(600px, 68vw);
        border-radius: 10px;
      }

      :deep(.md-editor-preview-wrapper) {
        color: #3D2B1F;
      }

      :deep(.md-editor-preview-wrapper p),
      :deep(.md-editor-preview-wrapper li),
      :deep(.md-editor-preview-wrapper blockquote) {
        color: #3D2B1F;
      }

      :deep(.md-editor-preview-wrapper code) {
        background: rgba(139, 90, 43, 0.06);
        color: #8D6E63;
        padding: 2px 6px;
        border-radius: 6px;
      }
    }

    .assistant-bubble {
      border-color: rgba(139, 90, 43, 0.1);
      background: #F9F5F0;
    }

    .user-bubble {
      border-color: rgba(212, 128, 74, 0.2);
      background: #FFF0E0;
    }
  }
}
</style>
