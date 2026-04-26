import request from '@/utils/request'

export interface RecommendItem {
  workoutId: number
  title: string
  score: number
  reason: string
  type?: string
  difficulty?: string
  duration?: number
}

export interface RecommendationDTO {
  userId: number
  source: string
  items: RecommendItem[]
}

export const getRecommendations = (userId: number, topN = 10) => {
  return request.get<RecommendationDTO>(`/v1/recommendations`, { params: { userId, topN } })
}

export const getRuleRecommendations = (userId: number, topN = 10) => {
  return request.get<RecommendItem[]>(`/v1/recommendations/rule`, { params: { userId, topN } })
}

export const getCfRecommendations = (userId: number, topN = 10) => {
  return request.get<RecommendItem[]>(`/v1/recommendations/cf`, { params: { userId, topN } })
}
