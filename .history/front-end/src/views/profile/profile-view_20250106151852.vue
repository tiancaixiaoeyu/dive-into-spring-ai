<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/utils/api-instance'
import { useRouter } from 'vue-router'
import type { UserDTO } from '@/apis/__generated/model/user'
import { Plus, ChatLineRound, Service, User, UserFilled, Camera } from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref<UserDTO>()
const avatarUrl = ref('')
const username = ref('')
const nickname = ref('')
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const defaultAvatar = '/logo.jpg'

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

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  try {
    const res = await api.userController.userInfo()
    userInfo.value = res
    username.value = res.username || ''
    nickname.value = res.nickname || ''
    avatarUrl.value = res.avatar || ''
    console.log('获取到的用户信息:', res)
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
})

const handleAvatarSuccess = async (response: any) => {
  console.log('上传响应:', response)
  try {
    // 准备更新的用户数据
    const updateData = {
      id: userInfo.value?.id,
      phone: userInfo.value?.phone,
      nickname: nickname.value,
      avatar: response.url,
      gender: userInfo.value?.gender
    }
    console.log('更新数据:', updateData)

    await api.userController.updateUser({
      body: updateData
    })

    // // 重新获取用户信息
    // const res = await api.userController.userInfo()
    userInfo.value = res
    avatarUrl.value = response.url
    ElMessage.success('头像更新成功')
  } catch (error: any) {
    console.error('更新失败:', error)
    ElMessage.error(error.response?.data?.message || '头像更新失败')
  }
}

const updateNickname = async () => {
  try {
    if (!userInfo.value?.id) {
      ElMessage.error('用户信息获取失败')
      return
    }

    console.log('准备更新昵称:', nickname.value)

    const updateData = {
      id: userInfo.value.id,
      nickname: nickname.value,
      phone: userInfo.value.phone,
      avatar: avatarUrl.value || userInfo.value?.avatar,
      gender: userInfo.value?.gender || null
    }

    console.log('发送的更新数据:', updateData)

    await api.userController.updateUser({
      body: updateData
    })

    // 重新获取用户信息
    const res = await api.userController.userInfo()
    userInfo.value = res
    nickname.value = res.nickname || ''
    avatarUrl.value = res.avatar || ''
    ElMessage.success('昵称更新成功')
  } catch (error: any) {
    console.error('更新失败:', {
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
      ElMessage.warning('账号注销成功')
      router.push('/login')
    }
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  router.push('/login')
  ElMessage.success('已退出登录')
}
</script>

<template>
  <div class="profile-container">
    <div class="nav-bar">
      <el-menu mode="horizontal" :router="true" class="custom-menu">
        <el-menu-item index="/">
          <el-icon><ChatLineRound /></el-icon>AI 助手
        </el-menu-item>
        <el-menu-item index="/chatroom">
          <el-icon><Service /></el-icon>公共聊天室
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>个人中心
        </el-menu-item>
      </el-menu>
    </div>

    <div class="content-wrapper">
      <el-card class="avatar-card">
        <div class="avatar-wrapper">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :headers="uploadHeaders"
            name="file"
          >
            <div class="avatar-container">
              <el-avatar
                :size="120"
                :src="avatarUrl || userInfo?.avatar || defaultAvatar"
                @error="() => defaultAvatar"
              >
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="avatar-hover">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
          </el-upload>
          <h2 class="username">{{ nickname || '未设置昵称' }}</h2>
          <p class="user-info">{{ userInfo?.phone }}</p>
          <el-button type="danger" @click="handleLogout" class="logout-btn">退出登录</el-button>
        </div>
      </el-card>

      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <el-tabs>
              <el-tab-pane label="修改昵称" name="basic">
                <el-form label-width="100px" class="profile-form">
                  <el-form-item label="昵称">
                    <el-input v-model="nickname" placeholder="请输入昵称">
                      <template #append>
                        <el-button type="primary" @click="updateNickname">更新</el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-form>
              </el-tab-pane>

              <el-tab-pane label="账户设置" name="security">
                <el-form label-width="100px" class="profile-form">
                  <el-form-item label="原密码">
                    <el-input v-model="oldPassword" type="password" placeholder="请输入原密码" />
                  </el-form-item>
                  <el-form-item label="新密码">
                    <el-input v-model="newPassword" type="password" placeholder="请输入新密码" />
                  </el-form-item>
                  <el-form-item label="确认密码">
                    <el-input
                      v-model="confirmPassword"
                      type="password"
                      placeholder="请再次输入新密码"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="updatePassword">修改密码</el-button>
                  </el-form-item>

                  <el-divider content-position="left">
                    <el-tag type="danger" effect="dark">危险区域</el-tag>
                  </el-divider>

                  <el-form-item>
                    <el-popconfirm
                      title="确定要注销账号吗？此操作不可恢复"
                      confirm-button-text="确定注销"
                      cancel-button-text="取消"
                      @confirm="deleteAccount"
                    >
                      <template #reference>
                        <el-button type="danger" plain>注销账号</el-button>
                      </template>
                    </el-popconfirm>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
            </el-tabs>
          </div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.profile-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #e6f0ff 0%, #ffffff 100%);

  .custom-menu {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    padding: 0 20px;

    :deep(.el-menu-item) {
      font-size: 16px;
      height: 60px;
      line-height: 60px;

      .el-icon {
        font-size: 20px;
        margin-right: 8px;
      }
    }
  }

  .content-wrapper {
    max-width: 1400px;
    margin: 40px auto;
    padding: 0 40px;
    display: grid;
    grid-template-columns: 380px 1fr;
    gap: 32px;
  }

  .avatar-card {
    height: fit-content;

    .avatar-wrapper {
      text-align: center;
      padding: 30px 0;

      .avatar-container {
        position: relative;
        display: inline-block;
        cursor: pointer;

        .el-avatar {
          width: 160px !important;
          height: 160px !important;
          font-size: 160px;
        }

        .avatar-hover {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: rgba(0, 0, 0, 0.5);
          border-radius: 50%;
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          color: white;
          opacity: 0;
          transition: opacity 0.3s;

          .el-icon {
            font-size: 32px;
            margin-bottom: 12px;
          }

          span {
            font-size: 16px;
          }
        }

        &:hover .avatar-hover {
          opacity: 1;
        }
      }

      .username {
        margin: 24px 0 12px;
        font-size: 26px;
        font-weight: 600;
      }

      .user-info {
        color: #909399;
        font-size: 16px;
      }

      .logout-btn {
        margin-top: 20px;
        width: 120px;
        font-size: 16px;
        height: 40px;
        background: linear-gradient(to right, #ff4b2b, #ff416c);
        border: none;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 5px 15px rgba(255, 75, 43, 0.2);
        }
      }
    }
  }

  .info-card {
    .el-tabs {
      margin-top: -20px;

      :deep(.el-tabs__item) {
        font-size: 16px;
        padding: 0 24px;
      }
    }

    .profile-form {
      max-width: 600px;
      margin: 30px auto;

      :deep(.el-form-item) {
        margin-bottom: 24px;

        .el-form-item__label {
          font-size: 16px;
          padding-right: 20px;
        }

        .el-input {
          .el-input__inner {
            font-size: 16px;
            height: 44px;
            line-height: 44px;
          }
        }

        .el-button {
          font-size: 16px;
          padding: 12px 24px;
          height: 44px;
        }
      }
    }
  }

  :deep(.el-card) {
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

    .el-card__header {
      padding: 20px 24px;
      border-bottom: 1px solid #ebeef5;
    }

    .el-card__body {
      padding: 24px;
    }
  }

  :deep(.el-input-group__append) {
    padding: 0;
    .el-button {
      margin: 0;
      border: none;
      padding: 0 24px;
      height: 44px;
      font-size: 16px;
    }
  }

  :deep(.el-divider) {
    margin: 32px 0;

    .el-tag {
      font-size: 14px;
      padding: 8px 16px;
    }
  }

  :deep(.el-dialog) {
    .el-dialog__title {
      font-size: 20px;
    }

    .el-dialog__body {
      font-size: 16px;
      padding: 24px;
    }
  }
}
</style>
