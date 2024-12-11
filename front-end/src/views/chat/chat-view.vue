<script setup lang="ts">

</script>

<template>

  <!-- 最外层页面于窗口同宽，使聊天面板居中 -->
  <div class="home-view">
    <!-- 整个聊天面板 -->
    <div class="chat-panel" v-loading="loading">
      <!-- 左侧的会话列表 -->
      <div class="session-panel">
        <div class="title">AI助手</div>
        <div class="session-list" v-if="activeSession">
          <!-- for循环遍历会话列表用会话组件显示，并监听点击事件和删除事件。点击时切换到被点击的会话，删除时从会话列表中提出被删除的会话。 -->
          <session-item
            v-for="session in sessionList"
            :key="session.id"
            :active="session.id === activeSession.id"
            :session="session"
            class="session"
            @click="activeSession = session"
            @delete="handleDeleteSession"
          ></session-item>
        </div>
        <div class="button-wrapper">
          <el-button
            style="margin-right: 20px"
            :icon="ChatRound"
            size="small"
            @click="handleSessionCreate"
            >创建会话
          </el-button>
        </div>
      </div>
      <!-- 右侧的消息记录 -->
      <div class="message-panel">
        <!-- 会话名称 -->
        <div class="header" v-if="activeSession">
          <div class="front">
            <!-- 如果处于编辑状态则显示输入框让用户去修改 -->
            <div v-if="isEdit" class="title">
              <!-- 按回车代表确认修改 -->
              <el-input
                v-model="activeSession.name"
                @keydown.enter="handleUpdateSession"
              ></el-input>
            </div>
            <!-- 否则正常显示标题 -->
            <div v-else class="title">{{ activeSession.name }}</div>
            <div class="description">{{ activeSession.messages.length }}条对话</div>
          </div>
          <!-- 尾部的编辑按钮 -->
          <div class="rear">
            <el-icon :size="20" style="margin-right: 10px">
              <Delete @click="handleClearMessage(activeSession.id)" />
            </el-icon>
            <el-icon :size="20">
              <!-- 不处于编辑状态显示编辑按钮 -->
              <EditPen v-if="!isEdit" @click="isEdit = true" />
              <!-- 处于编辑状态显示取消编辑按钮 -->
              <Close v-else @click="isEdit = false"></Close>
            </el-icon>
          </div>
        </div>
        <el-divider :border-style="'solid'" />
        <div ref="messageListRef" class="message-list">
          <!-- 过渡效果 -->
          <transition-group name="list" v-if="activeSession">
            <message-row
              v-for="message in activeSession.messages"
              :key="message.id"
              :message="message"
            ></message-row>
          </transition-group>
        </div>
        <!-- 监听发送事件 -->
        <message-input @send="handleSendMessage" v-if="activeSession"></message-input>
      </div>
      <div class="option-panel">
        <el-form size="small">
          <el-form-item>
            <el-upload
              v-loading="embeddingLoading"
              :action="`${API_PREFIX}/document/embedding`"
              :show-file-list="false"
              :on-success="onUploadSuccess"
              :before-upload="beforeUpload"
            >
              <el-button type="primary">上传文档</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="知识库">
            <el-switch v-model="options.enableVectorStore"></el-switch>
          </el-form-item>
          <el-form-item label="agent（智能体）">
            <el-switch v-model="options.enableAgent"></el-switch>
          </el-form-item>
          <el-form-item label="文件">
            <div class="upload">
              <el-upload v-model:file-list="fileList" :auto-upload="false" :limit="1">
                <el-button type="primary">上传文本文件</el-button>
              </el-upload>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">

</style>