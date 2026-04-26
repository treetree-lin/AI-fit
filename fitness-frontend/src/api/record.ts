import request from '@/utils/request'
import type { WorkoutRecord, CheckInRequest, RecordUpdateRequest } from '@/types'

export const checkIn = (data: CheckInRequest) => {
  return request.post<WorkoutRecord>('/records/checkin', data)
}

export const getUserRecords = (userId: number) => {
  return request.get<WorkoutRecord[]>(`/records/user/${userId}`)
}

export const getRecordsByDateRange = (userId: number, startDate: string, endDate: string) => {
  return request.get<WorkoutRecord[]>(`/records/user/${userId}/date-range`, {
    params: { startDate, endDate }
  })
}

export const getRecordById = (id: number) => {
  return request.get<WorkoutRecord>(`/records/${id}`)
}

export const updateRecord = (id: number, data: RecordUpdateRequest) => {
  return request.put<WorkoutRecord>(`/records/${id}`, data)
}

export const deleteRecord = (id: number) => {
  return request.delete<string>(`/records/${id}`)
}

export const getMonthlyStats = (userId: number, year: number, month: number) => {
  return request.get<Record<string, any>>(`/records/stats/monthly/${userId}`, {
    params: { year, month }
  })
}

export const getOverviewStats = (userId: number) => {
  return request.get<Record<string, any>>(`/records/stats/overview/${userId}`)
}

export const getLast7DaysStats = (userId: number) => {
  return request.get<Record<string, any>[]>(`/records/stats/last-7days/${userId}`)
}

export const getHeatmapStats = (userId: number, days = 30) => {
  return request.get<Record<string, any>[]>(`/records/stats/heatmap/${userId}`, { params: { days } })
}

export const getRadarStats = (userId: number) => {
  return request.get<Record<string, any>>(`/records/stats/radar/${userId}`)
}
