<script setup lang="ts">
import { RouterView } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
</script>

<template>
  <el-config-provider :locale="zhCn">
    <router-view v-slot="{ Component }">
      <transition name="slide">
        <component :is="Component" />
      </transition>
    </router-view>
  </el-config-provider>
</template>

<style>
:root {
  --el-color-primary: #D4804A;
  --el-color-primary-light-3: #E0A070;
  --el-color-primary-light-5: #E9BF9E;
  --el-color-primary-light-7: #F2D8C3;
  --el-color-primary-light-8: #F6E5D5;
  --el-color-primary-light-9: #FBF2EB;
  --el-color-primary-dark-2: #AA663B;
  --el-border-color: rgba(139, 90, 43, 0.15);
  --el-border-color-light: rgba(139, 90, 43, 0.1);
  --el-text-color-primary: #3D2B1F;
  --el-text-color-regular: #5D4037;
  --el-text-color-secondary: #8D6E63;
  --el-text-color-placeholder: #BCAAA4;
}

body {
  margin: 0;
  min-height: 100vh;
  color: #3D2B1F;
  background: #FDF8F3;
  font-family:
    Noto Sans SC,
    SF Pro SC,
    SF Pro Text,
    SF Pro Icons,
    PingFang SC,
    Helvetica Neue,
    Helvetica,
    Arial,
    sans-serif;
}

.el-date-editor--datetime {
  width: 160px !important;
}

::-webkit-scrollbar {
  --bar-width: 5px;
  width: 5px;
  height: 5px;
}

::-webkit-scrollbar-track {
  background-color: transparent;
}

::-webkit-scrollbar-thumb {
  background-color: rgba(139, 90, 43, 0.18);
  border-radius: 20px;
  background-clip: content-box;
  border: 1px solid transparent;
}

.slide-enter-active {
  transition: all 0.8s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-leave-active {
  transition: all 0.3s ease-out;
}

.slide-enter-from {
  transform: translateX(-100vw);
  opacity: 0;
}

.slide-enter-to {
  transform: translateX(0);
  opacity: 1;
}

.slide-leave-from {
  transform: translateX(0);
  opacity: 1;
}

.slide-leave-to {
  transform: translateX(100vw);
  opacity: 0;
}
</style>
