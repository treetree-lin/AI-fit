import request from '@/utils/request'
import type { LoginRequest, RegisterRequest, UserProfile, ProfileMeResponse } from '@/types'

export const login = (data: LoginRequest) => {
  return request.post<{ token: string; userId: number }>('/users/login', data)
}

export const register = (data: RegisterRequest) => {
  return request.post<{ userId: number }>('/users/register', data)
}

export const registerAdmin = (data: RegisterRequest) => {
  return request.post<{ userId: number }>('/users/register-admin', data)
}

export const getCurrentProfile = () => {
  return request.get<ProfileMeResponse>('/user/profile/me')
}

export const getProfile = async (userId: number) => {
  // 如果是查看当前用户，使用普通接口；否则使用管理员接口
  const userStore = (await import('@/stores/user')).useUserStore()
  if (userStore.user?.id === userId) {
    // 查看自己的资料，使用普通接口
    const res = await request.get<ProfileMeResponse>('/user/profile/me')
    return { ...res, data: res.data?.profile }
  }
  // 查看他人资料，需要管理员权限
  return request.get<UserProfile>(`/admin/profile/${userId}`)
}

export const updateProfile = (data: RegisterRequest) => {
  return request.put<string>('/user/profile/update', data)
}

export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ url: string }>('/user/avatar/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const updateAvatar = (avatarUrl: string) => {
  return request.put<string>('/user/avatar/update', null, {
    params: { avatarUrl }
  })
}
