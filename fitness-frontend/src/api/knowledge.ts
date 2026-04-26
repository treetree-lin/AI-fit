import request from '@/utils/request'
import type { FileUpload, SearchResult } from '@/types'

export const getAccessibleFiles = () => {
  return request.get<FileUpload[]>('/v1/documents/accessible')
}

export const getUploadedFiles = () => {
  return request.get<FileUpload[]>('/v1/documents/uploads')
}

export const deleteDocument = (fileMd5: string) => {
  return request.delete<string>(`/v1/documents/${fileMd5}`)
}

export const downloadFile = (fileName: string) => {
  return request.get('/v1/documents/download', {
    params: { fileName }
  })
}

export const previewFile = (fileName: string) => {
  return request.get<string>('/v1/documents/preview', {
    params: { fileName }
  })
}

export const uploadChunk = (fileMd5: string, chunkIndex: number, totalSize: number, fileName: string, totalChunks: number, isPublic: boolean, file: Blob) => {
  const formData = new FormData()
  formData.append('fileMd5', fileMd5)
  formData.append('chunkIndex', String(chunkIndex))
  formData.append('totalSize', String(totalSize))
  formData.append('fileName', fileName)
  formData.append('totalChunks', String(totalChunks))
  formData.append('isPublic', String(isPublic))
  formData.append('file', file)
  return request.post<any>('/upload/chunk', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const mergeChunks = (fileMd5: string, fileName: string) => {
  return request.post<any>('/upload/merge', { fileMd5, fileName })
}

export const getUploadStatus = (fileMd5: string) => {
  return request.get<any>('/upload/status', {
    params: { file_md5: fileMd5 }
  })
}

export const getSupportedTypes = () => {
  return request.get<Record<string, string[]>>('/upload/supported-types')
}
