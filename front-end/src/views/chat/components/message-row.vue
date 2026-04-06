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
  perspective: 800px;

  &.right {
    justify-content: flex-end;

    .row {
      .avatar-wrapper {
        display: flex;
        justify-content: flex-end;
      }

      .message {
        background: linear-gradient(140deg, rgba(71, 245, 255, 0.2), rgba(255, 102, 215, 0.16));
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
        box-shadow:
          0 0 0 1px rgba(125, 241, 255, 0.25),
          0 0 20px rgba(80, 218, 255, 0.2);
        margin-bottom: 6px;
      }
    }

    .message {
      font-size: 15px;
      padding: 2px;
      max-width: 800px;
      border-radius: 14px;
      border: 1px solid rgba(122, 230, 255, 0.35);
      background: linear-gradient(145deg, rgba(25, 43, 87, 0.62), rgba(10, 18, 44, 0.86));
      box-shadow:
        inset 0 0 18px rgba(102, 243, 255, 0.08),
        0 8px 24px rgba(0, 0, 0, 0.3),
        0 0 18px rgba(76, 221, 255, 0.12);
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        inset: 0;
        background: linear-gradient(
          120deg,
          transparent 12%,
          rgba(128, 251, 255, 0.16) 28%,
          rgba(255, 105, 227, 0.14) 45%,
          transparent 64%
        );
        mix-blend-mode: screen;
        pointer-events: none;
      }

      .image {
        width: min(600px, 68vw);
        height: min(600px, 68vw);
        border-radius: 12px;
      }

      :deep(.md-editor-preview-wrapper) {
        color: #eaf8ff;
      }

      :deep(.md-editor-preview-wrapper p),
      :deep(.md-editor-preview-wrapper li),
      :deep(.md-editor-preview-wrapper blockquote) {
        color: #eaf8ff;
      }

      :deep(.md-editor-preview-wrapper code) {
        background: rgba(8, 18, 45, 0.72);
        color: #8effff;
        padding: 2px 6px;
        border-radius: 6px;
      }
    }

    .assistant-bubble {
      border-color: rgba(127, 222, 255, 0.3);
      background: linear-gradient(145deg, rgba(18, 34, 71, 0.82), rgba(8, 15, 35, 0.94));
    }

    .user-bubble {
      border-color: rgba(116, 245, 255, 0.46);
      background: linear-gradient(140deg, rgba(42, 86, 122, 0.82), rgba(32, 51, 98, 0.92));
      box-shadow:
        inset 0 0 24px rgba(102, 243, 255, 0.14),
        0 10px 30px rgba(0, 0, 0, 0.34),
        0 0 20px rgba(255, 102, 215, 0.18);

      :deep(.md-editor-preview-wrapper),
      :deep(.md-editor-preview-wrapper p),
      :deep(.md-editor-preview-wrapper li),
      :deep(.md-editor-preview-wrapper blockquote) {
        color: #f7fdff;
      }
    }
  }
}
</style>
