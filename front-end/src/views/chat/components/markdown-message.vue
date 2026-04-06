<script setup lang="ts">
import { MdPreview } from 'md-editor-v3'
import { ref } from 'vue'

defineProps<{ message: string }>()

const flash = ref(false)

const handleClick = (event: MouseEvent) => {
  const target = event.target as HTMLElement | null
  if (!target?.closest('a')) {
    return
  }
  flash.value = false
  requestAnimationFrame(() => {
    flash.value = true
    setTimeout(() => {
      flash.value = false
    }, 520)
  })
}
</script>

<template>
  <div :class="['markdown-wrap', flash ? 'flash' : '']" @click="handleClick">
    <md-preview id="preview-only" :model-value="message" preview-theme="smart-blue"></md-preview>
  </div>
</template>

<style scoped lang="scss">
.markdown-wrap {
  position: relative;
  overflow: hidden;
  border-radius: 12px;

  &::after {
    content: '';
    position: absolute;
    inset: -60%;
    background: radial-gradient(
      circle,
      rgba(116, 248, 255, 0.6),
      rgba(255, 111, 220, 0.28),
      transparent 64%
    );
    opacity: 0;
    pointer-events: none;
  }
}

.markdown-wrap.flash::after {
  animation: causticPulse 0.55s ease-out;
}

:deep(.md-editor-preview-wrapper) {
  padding: 0 10px 2px;
  color: #dcf6ff;

  .smart-blue-theme p {
    line-height: unset;
  }
}

:deep(.md-editor) {
  background: transparent;
  color: #dcf6ff;
}

:deep(.md-editor-preview-wrapper a) {
  color: #72f4ff;
  text-decoration: underline;
  text-shadow: 0 0 8px rgba(113, 246, 255, 0.55);
}

@keyframes causticPulse {
  0% {
    opacity: 0.75;
    transform: scale(0.5);
  }
  100% {
    opacity: 0;
    transform: scale(1.22);
  }
}
</style>
