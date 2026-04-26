import request from '@/utils/request'


export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ url: string }>('/common/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const uploadVideo = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ url: string }>('/common/upload/video', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
