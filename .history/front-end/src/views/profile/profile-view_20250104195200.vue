<script lang="ts" setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/utils/api-instance'
import { useRouter } from 'vue-router'
import type { UserDTO } from '@/apis/__generated/model/user'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref<UserDTO>()
const avatarUrl = ref('')
const nickname = ref('')
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

// 使用 computed 属性来处理 headers
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return {}
  }
  return {
    Authorization: token
  }
})

// 定义上传地址
const uploadUrl = `${import.meta.env.VITE_API_PREFIX}/oss/upload`

// 在组件初始化时获取用户信息
const initUserInfo = async () => {
  try {
    const res = await api.userController.userInfo()
    userInfo.value = res
    nickname.value = res.nickname || ''
    avatarUrl.value = res.avatar || ''
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 在组件挂载时获取用户信息
onMounted(() => {
  initUserInfo()
})

const handleAvatarSuccess = async (response: any) => {
  try {
    await api.userController.updateAvatar({
      body: response.url
    })
    
    // 更新成功后重新获取用户信息
    const res = await api.userController.userInfo()
    userInfo.value = res
    avatarUrl.value = response.url
    
    // 强制更新组件
    if (typeof nextTick === 'function') {
      await nextTick()
    }
    
    ElMessage.success('头像更新成功')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '头像更新失败')
  }
}

const updateNickname = async () => {
  try {
    if (!userInfo.value?.id) {
      ElMessage.error('用户信息获取失败')
      return
    }

    const updateData = {
      id: userInfo.value.id,
      nickname: nickname.value,
      phone: userInfo.value.phone,
      avatar: userInfo.value.avatar || null,
      gender: userInfo.value.gender || null
    }

    console.log('准备更新的数据:', updateData)

    await api.userController.updateUser({
      body: updateData
    })

    const res = await api.userController.userInfo()
    userInfo.value = res
    ElMessage.success('昵称更新成功')
  } catch (error: any) {
    console.error('更新失败详细信息:', {
      error,
      response: error.response,
      data: error.response?.data,
      message: error.response?.data?.message
    })
    ElMessage.error(error.response?.data?.message || '昵称更新失败')
  }
}

const updatePassword = async () => {
  if (newPassword.value !== confirmPassword.value) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  try {
    await api.userController.updatePassword({
      body: {
        oldPassword: oldPassword.value,
        newPassword: newPassword.value
      }
    })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.removeItem('token')
    router.push('/login')
  } catch (error) {
    ElMessage.error('密码修改失败')
  }
}

const deleteAccount = async () => {
  try {
    await ElMessageBox.confirm('此操作将永久删除您的账号，是否继续？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.userController.deleteAccount()
    ElMessage.success('账号已注销')
    localStorage.removeItem('token')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('账号注销失败')
    }
  }
}
</script>

<template>
  <div class="profile-container">
    <div class="nav-bar">
      <el-menu mode="horizontal" :router="true" style="width: 100%">
        <el-menu-item index="/">AI 助手</el-menu-item>
        <el-menu-item index="/chatroom">公共聊天室</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
      </el-menu>
    </div>
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>

      <div class="avatar-section">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :headers="uploadHeaders"
          name="file"
        >
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
          
          
          <el-icon v-else class="avatar-uploader-icon"><Plus />
            </el-icon>
        </el-upload>
        <div class="upload-tip">点击上传头像</div>
      </div>

      <el-form label-width="100px" class="profile-form">
        <el-form-item label="昵称">
          <el-input v-model="nickname" placeholder="请输入昵称">
            <template #append>
              <el-button @click="updateNickname">更新</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-divider>修改密码</el-divider>

        <el-form-item label="原密码">
          <el-input v-model="oldPassword" type="password" placeholder="请输入原密码" />
        </el-form-item>

        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input v-model="confirmPassword" type="password" placeholder="请再次输入新密码" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="updatePassword">修改密码</el-button>
        </el-form-item>

        <el-divider>危险操作</el-divider>

        <el-form-item>
          <el-button type="danger" @click="deleteAccount">注销账号</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.profile-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;

  .profile-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 15px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

    .card-header {
      font-size: 20px;
      font-weight: bold;
    }

    .avatar-section {
      text-align: center;
      margin-bottom: 30px;

      .avatar-uploader {
        .avatar {
          width: 120px;
          height: 120px;
          border-radius: 50%;
          object-fit: cover;
        }

        .avatar-uploader-icon {
          font-size: 28px;
          color: #8c939d;
          width: 120px;
          height: 120px;
          line-height: 120px;
          text-align: center;
          border: 1px dashed #d9d9d9;
          border-radius: 50%;
        }
      }

      .upload-tip {
        color: #666;
        margin-top: 10px;
      }
    }

    .profile-form {
      max-width: 500px;
      margin: 0 auto;
    }
  }
}
</style>
