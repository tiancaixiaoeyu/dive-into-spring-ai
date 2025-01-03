<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/utils/api-instance'
import { useRouter } from 'vue-router'
import type { User } from '@/apis/__generated/model/static'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref<User>()
const avatarUrl = ref('')
const nickname = ref('')
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

// 定义上传地址
const uploadUrl = `${import.meta.env.VITE_API_PREFIX}/oss/upload`

// ... 其他代码保持不变 ...
</script>

<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <!-- ... 其他代码保持不变 ... -->
      
      <div class="avatar-section">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
        >
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">点击上传头像</div>
      </div>

      <!-- ... 其他代码保持不变 ... -->
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
// ... 样式代码保持不变 ...
</style> 