import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Dynamic_User } from '@/apis/__generated/model/dynamic/Dynamic_User'
import { api } from '@/utils/api-instance'

export const useHomeStore = defineStore('home', () => {
  const userInfo = ref<Dynamic_User>()
  const getUserInfo = async () => {
    userInfo.value = await api.userController.userInfo()
    return userInfo.value
  }
  const init = async () => {
    await getUserInfo()
  }
  const logout = () => {
    userInfo.value = undefined
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
  }
  return { userInfo, getUserInfo, init, logout }
})
