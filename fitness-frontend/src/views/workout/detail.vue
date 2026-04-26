<template>
  <div v-if="workout" class="workout-detail">
    <div class="page-banner">
      <div class="page-banner-content">
        <el-button text :icon="ArrowLeft" class="back-btn" @click="router.back()">返回列表</el-button>
        <h1 class="page-banner-title">{{ workout.title }}</h1>
        <p class="page-banner-desc">{{ workout.description }}</p>
      </div>
    </div>

    <el-row :gutter="24" class="detail-body">
      <el-col :span="16">
        <div class="detail-main">
          <!-- 视频区 -->
          <div v-if="workout.videoUrl" class="detail-section video-section">
            <div class="video-wrapper">
              <iframe
                v-if="isBilibili"
                :src="bilibiliSrc"
                style="width: 100%; height: 450px; border: none; border-radius: 16px"
                scrolling="no"
                frameborder="0"
                allowfullscreen
              />
              <video v-else :src="workout.videoUrl" controls style="width: 100%; max-height: 450px; border-radius: 16px" />
            </div>
          </div>

          <div class="detail-cover">
            <el-image
              :src="workout.coverImageUrl || '/default-workout.jpg'"
              fit="cover"
              style="width: 100%; height: 280px; border-radius: 16px"
            >
              <template #error>
                <div class="image-placeholder-large">
                  <el-icon size="64" color="#ddd"><Basketball /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="detail-badges">
              <div class="detail-badge" :class="difficultyClass(workout.difficulty)">{{ workout.difficulty }}</div>
              <div v-for="(m, idx) in targetMuscles" :key="idx" class="detail-badge outline">{{ m }}</div>
              <div class="detail-badge outline"><el-icon><Timer /></el-icon> {{ workout.durationMinutes }} 分钟</div>
            </div>
          </div>

          <div class="detail-section">
            <h3 class="section-title">训练动作</h3>
            <div class="steps-table">
              <div v-for="(step, idx) in workout.steps" :key="step.id" class="step-row">
                <div class="step-num">{{ idx + 1 }}</div>
                <div class="step-content">
                  <div class="step-name">{{ step.exerciseName }}</div>
                  <div class="step-meta">
                    <span class="step-tag">{{ step.sets }} 组</span>
                    <span class="step-tag">{{ step.reps }}</span>
                    <span class="step-tag">休息 {{ step.restSeconds }}s</span>
                  </div>
                  <div v-if="step.tips" class="step-tips">💡 {{ step.tips }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="detail-section comments-section">
            <h3 class="section-title">用户评论 ({{ comments.length }})</h3>
            <div v-if="userStore.isLoggedIn" class="comment-form">
              <el-input v-model="commentContent" type="textarea" :rows="2" placeholder="发表你的评论..." class="comment-input" />
              <el-button type="primary" class="comment-submit" @click="submitComment">发表评论</el-button>
            </div>
            <div class="comment-list">
              <div v-for="c in comments" :key="c.id" class="comment-item">
                <div class="comment-user">
                  <el-avatar :size="36" class="comment-avatar">{{ c.username?.[0] }}</el-avatar>
                  <div class="comment-info">
                    <div class="comment-username">{{ c.username }}</div>
                    <div class="comment-time">{{ c.createdAt }}</div>
                  </div>
                </div>
                <div class="comment-content">{{ c.content }}</div>
                <div class="comment-actions">
                  <el-button text size="small" :type="c.isLiked ? 'primary' : 'default'" :icon="Pointer" @click="likeComment(c.id)">{{ c.likeCount || 0 }}</el-button>
                  <el-button v-if="canDelete(c)" text size="small" type="danger" @click="deleteComment(c.id)">删除</el-button>
                </div>
              </div>
              <el-empty v-if="comments.length === 0" description="暂无评论，来抢沙发吧" />
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="detail-sidebar">
          <div class="side-card">
            <el-button :type="favorited ? 'warning' : 'primary'" size="large" class="side-btn favorite-btn" :icon="Star" @click="toggleFavorite">{{ favorited ? '已收藏' : '收藏方案' }}</el-button>
            <el-button type="success" size="large" class="side-btn start-btn" :icon="CircleCheck" @click="goCheckIn">开始打卡</el-button>
          </div>

          <div class="side-card info-card">
            <h4 class="info-title">方案信息</h4>
            <div class="info-list">
              <div class="info-row"><span class="info-label">目标肌群</span><span class="info-value">{{ workout.targetMuscle }}</span></div>
              <div class="info-row"><span class="info-label">预计时长</span><span class="info-value">{{ workout.durationMinutes }} 分钟</span></div>
              <div class="info-row"><span class="info-label">消耗热量</span><span class="info-value">{{ workout.caloriesBurned || '-' }} kcal</span></div>
              <div class="info-row"><span class="info-label">收藏人数</span><span class="info-value">{{ workout.favoriteCount || 0 }}</span></div>
              <div class="info-row"><span class="info-label">评论人数</span><span class="info-value">{{ workout.commentCount || 0 }}</span></div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, CircleCheck, Pointer, Basketball, Timer, ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getWorkoutById, getComments, addComment, deleteComment as apiDeleteComment, toggleLike, addFavorite, removeFavorite, checkFavorite } from '@/api/workout'
import type { Workout, Comment } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const workout = ref<Workout | null>(null)
const comments = ref<Comment[]>([])
const commentContent = ref('')
const favorited = ref(false)

const targetMuscles = computed(() => {
  if (!workout.value?.targetMuscle) return []
  return workout.value.targetMuscle.split(',').map((s: string) => s.trim()).filter(Boolean)
})

const isBilibili = computed(() => {
  if (!workout.value?.videoUrl) return false
  return workout.value.videoUrl.includes('bilibili.com') || workout.value.videoUrl.includes('BV')
})

const bilibiliSrc = computed(() => {
  if (!workout.value?.videoUrl) return ''
  const match = workout.value.videoUrl.match(/BV[a-zA-Z0-9]+/)
  if (match) return `//player.bilibili.com/player.html?bvid=${match[0]}&autoplay=0`
  return workout.value.videoUrl
})

const difficultyClass = (d: string) => {
  if (d === '初级' || d === 'BEGINNER') return 'badge-beginner'
  if (d === '中级' || d === 'INTERMEDIATE') return 'badge-intermediate'
  return 'badge-advanced'
}

const canDelete = (c: Comment) => userStore.user?.id === c.userId || userStore.isAdmin

const loadData = async () => {
  const id = Number(route.params.id)
  try {
    const [wRes, cRes] = await Promise.all([getWorkoutById(id), getComments(id)])
    workout.value = wRes.data
    comments.value = cRes.data || []
    if (userStore.user) {
      const fRes = await checkFavorite(id, userStore.user.id)
      favorited.value = fRes.data?.favorited || false
    }
  } catch (e) {}
}

const toggleFavorite = async () => {
  if (!userStore.user) { ElMessage.warning('请先登录'); return }
  const id = Number(route.params.id)
  const userId = userStore.user.id
  try {
    if (favorited.value) { await removeFavorite(id, userId); ElMessage.success('已取消收藏') }
    else { await addFavorite(id, userId); ElMessage.success('收藏成功') }
    favorited.value = !favorited.value
  } catch (e) {}
}

const submitComment = async () => {
  if (!commentContent.value.trim()) { ElMessage.warning('请输入评论内容'); return }
  if (!userStore.user) return
  const id = Number(route.params.id)
  try {
    await addComment(id, userStore.user.id, userStore.user.username, commentContent.value)
    ElMessage.success('评论成功')
    commentContent.value = ''
    const cRes = await getComments(id)
    comments.value = cRes.data || []
  } catch (e) {}
}

const likeComment = async (commentId: number) => {
  if (!userStore.user) { ElMessage.warning('请先登录'); return }
  try {
    await toggleLike(commentId, userStore.user.id)
    const cRes = await getComments(Number(route.params.id))
    comments.value = cRes.data || []
  } catch (e) {}
}

const deleteComment = async (commentId: number) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '提示', { type: 'warning' })
    await apiDeleteComment(commentId)
    ElMessage.success('已删除')
    const cRes = await getComments(Number(route.params.id))
    comments.value = cRes.data || []
  } catch (e) {}
}

const goCheckIn = () => {
  if (!workout.value) return
  router.push({ path: '/records', query: { workoutId: workout.value.id, duration: workout.value.durationMinutes, calories: workout.value.caloriesBurned } })
}

onMounted(loadData)
</script>

<style scoped>
.workout-detail { padding-bottom: 40px; }
.page-banner { background: var(--bg-banner); border-radius: 20px; padding: 28px 40px; margin-bottom: 24px; color: var(--text-primary); }
.back-btn { color: var(--text-secondary); margin-bottom: 12px; }
.page-banner-title { font-size: 28px; font-weight: 800; margin: 0 0 8px; }
.page-banner-desc { font-size: 15px; color: var(--text-banner-desc); margin: 0; }
.detail-main { background: var(--bg-card); border-radius: 16px; border: 1px solid var(--border-color); overflow: hidden; }
.video-section { padding: 20px 24px; border-top: none; }
.video-wrapper { border-radius: 16px; overflow: hidden; }
.detail-cover { position: relative; padding: 16px; }
.image-placeholder-large { width: 100%; height: 280px; background: var(--bg-tag); display: flex; align-items: center; justify-content: center; border-radius: 16px; }
.detail-badges { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 16px; }
.detail-badge { padding: 6px 14px; border-radius: 8px; font-size: 13px; font-weight: 700; color: #fff; display: flex; align-items: center; gap: 4px; }
.detail-badge.outline { background: var(--bg-tag); color: var(--text-secondary); font-weight: 500; }
.badge-beginner { background: linear-gradient(135deg, #52c41a, #73d13d); }
.badge-intermediate { background: linear-gradient(135deg, #faad14, #ffc53d); }
.badge-advanced { background: linear-gradient(135deg, #ff4d4f, #ff7875); }
.detail-section { padding: 20px 24px; border-top: 1px solid var(--border-color); }
.section-title { font-size: 18px; font-weight: 700; color: var(--text-primary); margin: 0 0 16px; }
.detail-desc { color: var(--text-secondary); line-height: 1.8; margin: 0; font-size: 14px; }
.steps-table { display: flex; flex-direction: column; gap: 12px; }
.step-row { display: flex; gap: 16px; padding: 16px; background: var(--bg-tag); border-radius: 12px; align-items: flex-start; }
.step-num { width: 32px; height: 32px; background: linear-gradient(135deg, #3b82f6, #60a5fa); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; flex-shrink: 0; }
.step-name { font-size: 15px; font-weight: 700; color: var(--text-primary); margin-bottom: 8px; }
.step-meta { display: flex; gap: 10px; margin-bottom: 6px; }
.step-tag { background: var(--bg-card); padding: 2px 10px; border-radius: 6px; font-size: 12px; color: var(--text-secondary); }
.step-tips { font-size: 12px; color: var(--text-muted); }
.comments-section { background: var(--bg-page); }
.comment-form { display: flex; gap: 12px; margin-bottom: 20px; }
.comment-input { flex: 1; }
.comment-submit { background: var(--btn-primary-gradient); border: none; border-radius: 10px; font-weight: 600; }
.comment-item { padding: 16px; background: var(--bg-card); border-radius: 12px; margin-bottom: 12px; }
.comment-user { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.comment-avatar { background: linear-gradient(135deg, #3b82f6, #60a5fa); color: #fff; font-weight: 700; }
.comment-username { font-weight: 700; font-size: 14px; color: var(--text-primary); }
.comment-time { font-size: 12px; color: var(--text-muted); }
.comment-content { color: var(--text-secondary); line-height: 1.6; margin-left: 46px; }
.comment-actions { margin-top: 10px; margin-left: 46px; }
.detail-sidebar { display: flex; flex-direction: column; gap: 20px; }
.side-card { background: var(--bg-card); border-radius: 16px; padding: 20px; border: 1px solid var(--border-color); }
.side-btn { width: 100%; margin-bottom: 12px; border-radius: 10px; font-weight: 600; height: 44px; }
.favorite-btn { background: linear-gradient(135deg, #f59e0b, #fbbf24); border: none; color: #fff; }
.start-btn { background: linear-gradient(135deg, #52c41a, #73d13d); border: none; margin-bottom: 0; }
.info-title { font-size: 16px; font-weight: 700; color: var(--text-primary); margin: 0 0 16px; }
.info-list { display: flex; flex-direction: column; gap: 14px; }
.info-row { display: flex; justify-content: space-between; font-size: 14px; }
.info-label { color: var(--text-muted); }
.info-value { color: var(--text-primary); font-weight: 600; }
</style>
