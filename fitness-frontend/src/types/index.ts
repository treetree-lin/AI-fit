export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  traceId: string
}

export interface User {
  id: number
  username: string
  role: 'USER' | 'ADMIN'
  profileCompleted: boolean
  avatarUrl?: string
  createdAt: string
  updatedAt: string
}

export interface UserProfile {
  userId?: number
  username?: string
  age?: number
  gender?: '男' | '女' | '其他' | string
  height?: number
  weight?: number
  bodyFat?: number
  goal?: string
  level?: string
  bio?: string
  location?: string
  checkInTime?: string
  exp?: number
  equipment?: string[]
  injuryHistory?: string[]
  injuries?: string
  avatarUrl?: string
  totalWorkouts?: number
  totalMinutes?: number
  totalCalories?: number
}

// /api/user/profile/me 接口返回的数据结构
export interface ProfileMeResponse {
  profile: UserProfile
  message: string
  adminInfo?: {
    role: string
    permissions: string[]
    adminLevel: string
    canManageOtherUsers: boolean
  }
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  age?: number
  gender?: '男' | '女' | '其他' | string
  height?: number
  weight?: number
  bodyFat?: number
  goal?: string
  level?: string
  equipment?: string[]
  injuryHistory?: string[]
  avatarUrl?: string
}

export interface Workout {
  id: number
  title: string
  description: string
  difficulty: string
  durationMinutes: number
  targetMuscle: string
  equipmentNeeded: string[]
  caloriesBurned: number
  isActive: boolean
  createdBy: number
  coverImageUrl: string
  videoUrl: string
  favoriteCount: number
  commentCount: number
  viewCount: number
  createdAt: string
  updatedAt: string
  steps: WorkoutStep[]
}

export interface WorkoutStep {
  id: number
  stepOrder: number
  exerciseName: string
  sets: number
  reps: string
  restSeconds: number
  tips: string
  videoUrl: string
  caloriesBurned?: number
}

export interface Comment {
  id: number
  userId: number
  username: string
  workoutId: number
  parentId: number | null
  content: string
  likeCount: number
  replyCount: number
  createdAt: string
  updatedAt: string
  isLiked: boolean
  replies: Comment[]
}

export interface Favorite {
  id: number
  userId: number
  workoutId: number
  createdAt: string
}

export interface WorkoutRecord {
  id: number
  userId: number
  workoutId: number
  recordDate: string
  durationMinutes: number
  caloriesBurned: number
  completed: boolean
  rating: number
  notes: string
  stepCount?: number
  createdAt: string
  updatedAt: string
  steps: RecordStep[]
}

export interface RecordStep {
  id?: number
  stepId: number
  exerciseName: string
  actualSets: number
  actualReps: string
  weightUsed?: number
  caloriesBurned?: number
}

export interface CheckInRequest {
  userId?: number
  workoutId?: number
  durationMinutes?: number
  caloriesBurned?: number
  recordDate?: string
  stepCount?: number
  notes?: string
  steps?: RecordStep[]
}

export interface RecordUpdateRequest {
  durationMinutes?: number
  caloriesBurned?: number
  recordDate?: string
  stepCount?: number
  notes?: string
  steps?: RecordStep[]
}

export interface Conversation {
  id: number
  question: string
  answer: string
  timestamp: string
}

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  status?: 'pending' | 'loading' | 'finished' | 'error'
  timestamp?: number
}

export interface FileUpload {
  id: number
  fileMd5: string
  fileName: string
  totalSize: number
  status: number
  userId: string
  isPublic: boolean
  createdAt: string
  mergedAt: string
}

export interface SearchResult {
  fileMd5: string
  chunkId: number
  textContent: string
  score: number
  fileName: string
  userId: string
  isPublic: boolean
}

export interface WorkoutPlan {
  id: number
  userId: number
  name: string
  description?: string
  startDate?: string
  endDate?: string
  status: string
  createdAt: string
  updatedAt: string
  items: PlanItem[]
}

export interface PlanItem {
  id: number
  dayOfWeek: number
  workoutId?: number
  workoutTitle?: string
  workoutCover?: string
  durationMinutes?: number
  status: string
  scheduledDate?: string
  exerciseType?: string
  exerciseTypeLabel?: string
  caloriesBurned?: number
}
