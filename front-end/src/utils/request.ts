import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const BASE_URL = import.meta.env.VITE_API_PREFIX
export const request = axios.create({
  baseURL: BASE_URL,
  timeout: 600000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.token = token
    config.headers.Authorization = token
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    if (res.data.code === 1) {
      return res.data.result
    }
    return Promise.reject(res.data)
  },
  (error) => {
    const response = error.response
    if (response?.data) {
      ElMessage.warning({ message: response.data.msg || '请求失败' })
      if (response.data.code === 10012) {
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        router.push('/login')
      }
    } else {
      ElMessage.error({ message: '网络错误' })
    }
    return Promise.reject(error)
  }
)
