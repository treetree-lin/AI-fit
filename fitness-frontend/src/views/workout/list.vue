<template>
  <div class="workout-list">
    <div class="page-banner">
      <div class="page-banner-content">
        <h1 class="page-banner-title">健身方案商城</h1>
        <p class="page-banner-desc">精选专业训练计划，助你达成健身目标</p>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-group">
        <span class="filter-label">难度：</span>
        <el-radio-group v-model="filterDifficulty" size="default">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="初级">初级</el-radio-button>
          <el-radio-button label="中级">中级</el-radio-button>
          <el-radio-button label="高级">高级</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-group">
        <span class="filter-label">部位：</span>
        <el-radio-group v-model="filterTarget" size="default">
          <el-radio-button label="">全部部位</el-radio-button>
          <el-radio-button label="胸">胸</el-radio-button>
          <el-radio-button label="背">背</el-radio-button>
          <el-radio-button label="腿">腿</el-radio-button>
          <el-radio-button label="肩">肩</el-radio-button>
          <el-radio-button label="手臂">手臂</el-radio-button>
        </el-radio-group>
      </div>
      <el-input
        v-model="searchQuery"
        placeholder="搜索方案名称..."
        class="search-input"
        :prefix-icon="Search"
        clearable
      />
    </div>

    <el-row :gutter="20">
      <el-col v-for="item in filteredWorkouts" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <div class="product-card" @click="goDetail(item.id)">
          <div class="product-cover">
            <el-image
              :src="item.coverImageUrl || '/default-workout.jpg'"
              fit="cover"
              style="width: 100%; height: 200px"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon size="48" color="#ddd"><Basketball /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="product-badge" :class="difficultyClass(item.difficulty)">
              {{ item.difficulty }}
            </div>
            <div class="product-favorite" @click.stop="toggleFavorite(item.id)">
              <el-icon size="20" :color="isFavorited(item.id) ? '#ff6b35' : '#fff'"><Star /></el-icon>
            </div>
          </div>
          <div class="product-body">
            <h3 class="product-title">{{ item.title }}</h3>
            <p class="product-desc">{{ item.description }}</p>
            <div class="product-meta">
              <div class="meta-item">
                <el-icon size="14"><Timer /></el-icon>
                <span>{{ item.durationMinutes }}分钟</span>
              </div>
              <div class="meta-item">
                <el-icon size="14"><FirstAidKit /></el-icon>
                <span>{{ item.caloriesBurned }}kcal</span>
              </div>
            </div>
            <div class="product-footer">
              <div class="product-stats">
                <span><el-icon size="14"><View /></el-icon> {{ item.viewCount || 0 }}</span>
                <span><el-icon size="14"><Star /></el-icon> {{ item.favoriteCount || 0 }}</span>
              </div>
              <el-button type="primary" size="small" class="product-btn">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-empty v-if="filteredWorkouts.length === 0" description="暂无相关方案" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Star, Timer, View, Basketball, FirstAidKit } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getWorkouts, getWorkoutsByDifficulty, getWorkoutsByTarget, addFavorite, removeFavorite, getUserFavorites } from '@/api/workout'
import type { Workout, Favorite } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const workouts = ref<Workout[]>([])
const favorites = ref<Favorite[]>([])
const searchQuery = ref('')
const filterDifficulty = ref('')
const filterTarget = ref('')

const filteredWorkouts = computed(() => {
  let list = workouts.value
  if (filterDifficulty.value) {
    list = list.filter(w => w.difficulty === filterDifficulty.value)
  }
  if (filterTarget.value) {
    list = list.filter(w => w.targetMuscle && w.targetMuscle.includes(filterTarget.value))
  }
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(w => w.title.toLowerCase().includes(q) || w.description.toLowerCase().includes(q))
  }
  return list
})

const difficultyClass = (d: string) => {
  if (d === '初级' || d === 'BEGINNER') return 'badge-beginner'
  if (d === '中级' || d === 'INTERMEDIATE') return 'badge-intermediate'
  return 'badge-advanced'
}

const isFavorited = (workoutId: number) => {
  return favorites.value.some(f => f.workoutId === workoutId)
}

const loadWorkouts = async () => {
  try {
    const res = await getWorkouts()
    workouts.value = res.data || []
  } catch (e) {
    // ignore
  }
}

const loadFavorites = async () => {
  if (!userStore.user) return
  try {
    const res = await getUserFavorites(userStore.user.id)
    favorites.value = res.data || []
  } catch (e) {
    // ignore
  }
}

const toggleFavorite = async (workoutId: number) => {
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    return
  }
  const userId = userStore.user.id
  try {
    if (isFavorited(workoutId)) {
      await removeFavorite(workoutId, userId)
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(workoutId, userId)
      ElMessage.success('收藏成功')
    }
    await loadFavorites()
  } catch (e) {
    // ignore
  }
}

const goDetail = (id: number) => {
  router.push(`/workouts/${id}`)
}

watch([filterDifficulty, filterTarget], () => {
  loadWorkouts()
})

onMounted(() => {
  loadWorkouts()
  loadFavorites()
})
</script>

<style scoped>
.workout-list {
  padding-bottom: 40px;
}

.page-banner {
  background: var(--bg-banner);
  border-radius: 20px;
  padding: 36px 40px;
  margin-bottom: 28px;
  color: #fff;
}

.page-banner-title {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px;
}

.page-banner-desc {
  font-size: 15px;
  color: var(--text-banner-desc);
  margin: 0;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  background: var(--bg-card);
  padding: 20px 24px;
  border-radius: 16px;
  border: 1px solid var(--border-color);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.search-input {
  width: 260px;
  margin-left: auto;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: var(--bg-input);
  box-shadow: none;
}

.product-card {
  background: var(--bg-card);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  cursor: pointer;
  margin-bottom: 20px;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 48px rgba(0,0,0,0.1);
}

.product-cover {
  position: relative;
}

.image-placeholder {
  width: 100%;
  height: 200px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}

.badge-beginner {
  background: linear-gradient(135deg, #52c41a, #73d13d);
}

.badge-intermediate {
  background: linear-gradient(135deg, #faad14, #ffc53d);
}

.badge-advanced {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
}

.product-favorite {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  background: rgba(0,0,0,0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(4px);
}

.product-favorite:hover {
  background: rgba(255,107,53,0.8);
}

.product-body {
  padding: 16px;
}

.product-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--text-tertiary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 34px;
  line-height: 1.5;
}

.product-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.product-stats {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--text-muted);
}

.product-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.product-btn {
  border-radius: 8px;
  background: linear-gradient(135deg, #ff6b35, #ff8f5a);
  border: none;
  font-weight: 600;
}
</style>
