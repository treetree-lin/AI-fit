import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { ApiResponse } from '@/types'

export interface TypedAxiosInstance {
  get<T = any>(url: string, config?: any): Promise<ApiResponse<T>>
  post<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>>
  put<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>>
  delete<T = any>(url: string, config?: any): Promise<ApiResponse<T>>
  patch<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>>
  request<T = any>(config: any): Promise<ApiResponse<T>>
  head<T = any>(url: string, config?: any): Promise<ApiResponse<T>>
  options<T = any>(url: string, config?: any): Promise<ApiResponse<T>>
}

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    // 对 blob / arraybuffer 类型响应不做 JSON code 校验，直接放行
    const responseType = response.config?.responseType
    if (responseType === 'blob' || responseType === 'arraybuffer') {
      return response.data
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error: AxiosError) => {
    // 添加更详细的调试信息，方便定位 403/401 问题
    const status = error.response?.status
    const respData = error.response?.data
    console.error('[API Error] status=', status, 'url=', error.config?.url, 'response=', respData)

    const message = (respData as any)?.message || error.message || '网络错误'
    ElMessage.error(message)
    
    // 只有在 401 时才自动跳转，403 由业务代码处理
    if (status === 401) {
      console.warn('[Auth] Token 无效或过期，跳转到登录页')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default request as unknown as TypedAxiosInstance
