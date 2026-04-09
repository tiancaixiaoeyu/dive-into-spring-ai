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
  border-radius: 10px;
}

:deep(.md-editor-preview-wrapper) {
  padding: 0 10px 2px;
  color: #3D2B1F;

  .smart-blue-theme p {
    line-height: unset;
  }
}

:deep(.md-editor) {
  background: transparent;
  color: #3D2B1F;
}

:deep(.md-editor-preview-wrapper a) {
  color: #D4804A;
  text-decoration: underline;
}
</style>
