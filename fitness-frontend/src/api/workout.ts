import request from '@/utils/request'
import type { Workout, Comment, Favorite } from '@/types'

export const getWorkouts = () => {
  return request.get<Workout[]>('/workouts')
}

export const getWorkoutById = (id: number) => {
  return request.get<Workout>(`/workouts/${id}`)
}

export const getWorkoutsByDifficulty = (difficulty: string) => {
  return request.get<Workout[]>(`/workouts/difficulty/${difficulty}`)
}

export const getWorkoutsByTarget = (targetMuscle: string) => {
  return request.get<Workout[]>(`/workouts/target/${targetMuscle}`)
}

export const createWorkout = (data: Partial<Workout>, adminId: number) => {
  return request.post<Workout>('/workouts', data, { params: { adminId } })
}

export const updateWorkout = (id: number, data: Partial<Workout>) => {
  return request.put<Workout>(`/workouts/${id}`, data)
}

export const deleteWorkout = (id: number) => {
  return request.delete<string>(`/workouts/${id}`)
}

export const toggleWorkoutStatus = (id: number) => {
  return request.patch<Workout>(`/workouts/${id}/toggle-status`)
}

export const uploadCover = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ url: string }>('/workouts/cover/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const uploadVideo = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ url: string }>('/workouts/video/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// Comments
export const getComments = (workoutId: number) => {
  return request.get<Comment[]>(`/workouts/comments/${workoutId}`)
}

export const addComment = (workoutId: number, userId: number, username: string, content: string, parentId?: number) => {
  return request.post<Comment>('/workouts/comments', { content }, {
    params: { userId, username, workoutId, parentId }
  })
}

export const deleteComment = (commentId: number) => {
  return request.delete<string>(`/workouts/comments/${commentId}`)
}

export const toggleLike = (commentId: number, userId: number) => {
  return request.post<Comment>(`/workouts/comments/${commentId}/like`, null, {
    params: { userId }
  })
}

// Favorites
export const addFavorite = (workoutId: number, userId: number) => {
  return request.post<{ success: boolean }>(`/workouts/favorites/${workoutId}`, null, {
    params: { userId }
  })
}

export const removeFavorite = (workoutId: number, userId: number) => {
  return request.delete<string>(`/workouts/favorites/${workoutId}`, {
    params: { userId }
  })
}

export const checkFavorite = (workoutId: number, userId: number) => {
  return request.get<{ favorited: boolean }>(`/workouts/favorites/${workoutId}/check`, {
    params: { userId }
  })
}

export const getUserFavorites = (userId: number) => {
  return request.get<Favorite[]>(`/workouts/favorites/user/${userId}`)
}
