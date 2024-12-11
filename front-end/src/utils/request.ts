import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const BASE_URL = import.meta.env.VITE_API_PREFIX
export const request = axios.create({
  baseURL: BASE_URL,
  timeout: 600000
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
        router.push('/login')
      }
    } else {
      ElMessage.error({ message: '网络错误' })
    }
    return Promise.reject(error)
  }
)
