import { createRouter, createWebHashHistory } from 'vue-router'
import RegisterView from '@/views/login/register-view.vue'
import LoginView from '@/views/login/login-view.vue'
import ChatView from '@/views/chat/chat-view.vue'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/',
      name: 'chat',
      component: () => import('@/views/chat/chat-view.vue') // 使用异步导入
    }
  ]
})

export default router
