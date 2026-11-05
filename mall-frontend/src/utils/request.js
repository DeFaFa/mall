import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  getAdminToken,
  getMemberToken,
  clearAdminAuth,
  clearMemberAuth
} from '../utils/auth'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 20000
})

http.interceptors.request.use((config) => {
  const token = config.url && config.url.startsWith('/admin') ? getAdminToken() : getMemberToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data
      }
      if (body.code === 401) {
        if (response.config.url && response.config.url.startsWith('/admin')) {
          clearAdminAuth()
          if (location.pathname.startsWith('/admin')) location.href = '/admin/login'
        } else {
          clearMemberAuth()
          if (location.pathname !== '/login') location.href = '/login'
        }
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http
