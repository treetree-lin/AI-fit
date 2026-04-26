import request from '@/utils/request'
import type { WorkoutPlan, PlanItem } from '@/types'

export interface PlanCreateRequest {
  name: string
  description?: string
  startDate?: string
  endDate?: string
  items?: PlanItemRequest[]
}

export interface PlanItemRequest {
  dayOfWeek: number
  workoutId?: number
  scheduledDate?: string
  status?: string
}

export const createPlan = (data: PlanCreateRequest, userId: number) => {
  return request.post<WorkoutPlan>('/plans', data, { params: { userId } })
}

export const getPlanById = (id: number) => {
  return request.get<WorkoutPlan>(`/plans/${id}`)
}

export const getUserPlans = (userId: number) => {
  return request.get<WorkoutPlan[]>(`/plans/user/${userId}`)
}

export const getActivePlan = (userId: number) => {
  return request.get<WorkoutPlan>(`/plans/user/${userId}/active`)
}

export const updatePlan = (id: number, data: PlanCreateRequest) => {
  return request.put<WorkoutPlan>(`/plans/${id}`, data)
}

export const deletePlan = (id: number) => {
  return request.delete<string>(`/plans/${id}`)
}

export const updateItemStatus = (itemId: number, status: string) => {
  return request.patch<WorkoutPlan>(`/plans/items/${itemId}/status`, null, { params: { status } })
}
