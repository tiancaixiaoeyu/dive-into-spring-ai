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
  background: #FFFCF8;
  border-radius: 12px;
  width: 250px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(139, 90, 43, 0.08);
  box-shadow: 0 2px 8px rgba(139, 90, 43, 0.04);
  transition: all 0.2s ease;

  .name {
    font-size: 14px;
    font-weight: 700;
    width: 200px;
    color: rgba(61, 43, 31, 0.85);
  }

  .count-time {
    margin-top: 10px;
    font-size: 10px;
    color: rgba(61, 43, 31, 0.55);
    display: flex;
    justify-content: space-between;
  }

  &.active {
    transition: all 0.15s linear;
    border: 2px solid #D4804A;
    box-shadow: 0 4px 12px rgba(212, 128, 74, 0.12);
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
    background: rgba(139, 90, 43, 0.04);
    width: 100%;
    height: 100%;
    top: 0;
    left: -100%;
    opacity: 0;
  }

  .btn-wrapper {
    color: rgba(61, 43, 31, 0.5);
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
      color: #C62828;
    }
  }
}
</style>
