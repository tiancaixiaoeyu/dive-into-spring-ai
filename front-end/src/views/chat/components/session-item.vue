<script lang="ts" setup>
import { CircleClose } from '@element-plus/icons-vue'
import type { AiSession } from '@/views/chat/store/chat-store'

// entity：用于展示的会话信息
const prop = defineProps<{ active: boolean; session: AiSession }>()
// 定义删除事件，当触发删除事件时会向外部发送被删除的会话。
const emit = defineEmits<{
  delete: [session: AiSession]
}>()
// 当鼠标放到会话上时，会弹出删除图标，点击删除图标发送删除事件。
const handleDeleteSession = () => {
  emit('delete', prop.session)
}
</script>

<template>
  <!-- 如果处于激活状态则增加 active class -->
  <div :class="['session-item', active ? 'active' : '']">
    <!-- 会话的名称 -->
    <div class="name">{{ session.name }}</div>
    <!-- 会话内的消息数量和最近修改的时间 -->
    <div class="count-time">
      <div class="count">{{ session.messages ? session.messages.length : 0 }}条对话</div>
      <div class="time">{{ session.editedTime }}</div>
    </div>
    <!-- 当鼠标放在会话上时会弹出遮罩 -->
    <div class="mask"></div>
    <!-- 当鼠标放在会话上时会弹出删除按钮 -->
    <div class="btn-wrapper">
      <el-icon :size="15" class="close">
        <el-popconfirm title="是否确认永久删除该聊天会话？" @confirm="handleDeleteSession">
          <template #reference>
            <CircleClose />
          </template>
        </el-popconfirm>
      </el-icon>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.session-item {
  padding: 12px;
  background: linear-gradient(135deg, rgba(35, 58, 109, 0.56), rgba(14, 24, 57, 0.84));
  border-radius: 14px;
  width: 250px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(122, 224, 255, 0.22);
  box-shadow:
    inset 0 0 18px rgba(106, 240, 255, 0.08),
    0 6px 16px rgba(0, 0, 0, 0.24);

  .name {
    font-size: 14px;
    font-weight: 700;
    width: 200px;
    color: #d8f4ff;
  }

  .count-time {
    margin-top: 10px;
    font-size: 10px;
    color: rgba(177, 221, 255, 0.76);
    display: flex;
    justify-content: space-between;
  }

  &.active {
    transition: all 0.2s ease;
    border: 1px solid rgba(106, 247, 255, 0.72);
    box-shadow:
      inset 0 0 22px rgba(101, 246, 255, 0.22),
      0 0 24px rgba(255, 90, 212, 0.24),
      0 0 12px rgba(88, 232, 255, 0.24);
    background: linear-gradient(135deg, rgba(47, 73, 136, 0.66), rgba(17, 28, 62, 0.94)),
      radial-gradient(circle at 86% 12%, rgba(255, 92, 223, 0.2), transparent 42%);
  }

  &:hover {
    .mask {
      opacity: 1;
      left: 0;
    }

    .btn-wrapper {
      &:hover {
        cursor: pointer;
      }

      opacity: 1;
      right: 20px;
    }
  }

  .mask {
    transition: all 0.2s ease-out;
    position: absolute;
    background: linear-gradient(120deg, rgba(106, 241, 255, 0.15), rgba(255, 100, 219, 0.15));
    width: 100%;
    height: 100%;
    top: 0;
    left: -100%;
    opacity: 0;
  }

  .btn-wrapper {
    color: rgba(178, 227, 255, 0.9);
    transition: all 0.2s ease-out;
    position: absolute;
    top: 10px;
    right: -20px;
    z-index: 10;
    opacity: 0;

    .edit {
      margin-right: 5px;
    }
    :deep(.close:hover) {
      color: #71fcff;
      text-shadow:
        0 0 6px rgba(113, 252, 255, 0.8),
        0 0 14px rgba(255, 96, 212, 0.46);
    }
  }
}
</style>
