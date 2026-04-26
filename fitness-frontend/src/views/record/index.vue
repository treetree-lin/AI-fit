<template>
  <div class="record-page">
    <div class="page-banner">
      <div class="page-banner-content">
        <h1 class="page-banner-title">运动记录</h1>
        <p class="page-banner-desc">记录每一次汗水，见证你的成长</p>
      </div>
    </div>

    <div class="record-layout">
      <!-- 左侧打卡 -->
      <div class="left-panel">
        <div class="panel-card glow-card">
          <div class="card-header"><el-icon size="20" color="#3b82f6"><CircleCheck /></el-icon><span>运动打卡</span></div>
          <el-form :model="checkInForm" label-position="top">
            <el-form-item label="选择方案">
              <el-select v-model="checkInForm.workoutId" placeholder="搜索并选择训练方案" filterable clearable style="width: 100%" @change="onWorkoutChange">
                <el-option v-for="w in workouts" :key="w.id" :label="w.title" :value="w.id">
                  <div class="workout-option">
                    <span class="workout-dot" :class="difficultyClass(w.difficulty)" />
                    <span class="workout-title">{{ w.title }}</span>
                    <span class="workout-meta">{{ w.durationMinutes }}分钟</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="运动日期">
              <el-date-picker v-model="checkInForm.date" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item label="运动时长 (分钟)">
              <el-slider v-model="checkInForm.durationMinutes" :min="1" :max="180" show-stops :step="5" />
              <div class="slider-value">{{ checkInForm.durationMinutes }} 分钟</div>
            </el-form-item>
            <el-form-item label="步数（训练动作数）">
              <div class="step-count-display">
                <el-tag size="large" type="primary" effect="dark">{{ checkInForm.stepCount ?? 0 }} 个动作</el-tag>
                <span class="step-count-tip">自动统计所选方案的动作数量</span>
              </div>
            </el-form-item>
            <el-form-item label="预计消耗热量 (kcal)">
              <div class="calories-display">
                <el-tag size="large" type="warning" effect="dark">{{ calculatedCalories }} kcal</el-tag>
                <span class="calories-tip">根据勾选动作自动累加</span>
              </div>
            </el-form-item>
            <el-form-item v-if="workoutSteps.length > 0" label="训练动作">
              <div class="step-list">
                <div v-for="(step, idx) in workoutSteps" :key="step.id" class="workout-step-item">
                  <el-checkbox v-model="step.completed" :label="step.exerciseName" size="large" />
                  <div v-if="step.completed" class="step-inputs">
                    <el-input-number v-model="step.actualSets" :min="1" :max="50" placeholder="组数" size="small" style="width: 80px" />
                    <el-input v-model="step.actualReps" placeholder="次数" size="small" style="width: 80px" />
                    <el-input-number v-model="step.weightUsed" :min="0" :precision="1" placeholder="重量kg" size="small" style="width: 90px" />
                  </div>
                  <div class="step-target">目标: {{ step.sets }}组 × {{ step.reps }} · 休息{{ step.restSeconds }}s</div>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="checkInForm.notes" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="checkin-btn" @click="handleCheckIn">
                <el-icon><CircleCheck /></el-icon>打卡
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 右侧记录列表 -->
      <div class="right-panel">
        <div class="panel-card glow-card">
          <div class="list-header">
            <div class="card-header"><el-icon size="20" color="#3b82f6"><Calendar /></el-icon><span>运动记录</span></div>
            <div class="record-filters">
              <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" size="small" />
              <el-button size="small" type="primary" class="filter-btn" @click="loadRecordsByRange">筛选</el-button>
              <el-button size="small" @click="loadRecords">重置</el-button>
            </div>
          </div>
          <el-timeline>
            <el-timeline-item v-for="r in records" :key="r.id" :timestamp="r.recordDate" placement="top">
              <div class="record-card">
                <div class="record-main">
                  <div class="record-title">{{ getWorkoutTitle(r.workoutId) }}</div>
                  <div class="record-meta">
                    <span class="record-tag">⏱ {{ r.durationMinutes }} 分钟</span>
                    <span class="record-tag">🔥 {{ r.caloriesBurned }} kcal</span>
                    <span v-if="r.stepCount" class="record-tag">👣 {{ r.stepCount }} 步</span>
                  </div>
                  <p v-if="r.notes" class="record-notes">{{ r.notes }}</p>
                </div>
                <div class="record-actions">
                  <el-button text type="primary" size="small" @click="editRecord(r)">编辑</el-button>
                  <el-button text type="danger" size="small" @click="removeRecord(r.id)">删除</el-button>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="records.length === 0" description="暂无运动记录" />
        </div>
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" title="编辑记录" width="520px">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="运动时长 (分钟)">
          <el-slider v-model="editForm.durationMinutes" :min="1" :max="180" show-stops :step="5" />
          <div class="slider-value">{{ editForm.durationMinutes }} 分钟</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.notes" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" class="save-btn" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, Calendar } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getWorkouts, getWorkoutById } from '@/api/workout'
import { checkIn, getUserRecords, getRecordsByDateRange, deleteRecord, updateRecord } from '@/api/record'
import type { Workout, WorkoutRecord, RecordUpdateRequest, WorkoutStep } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const workouts = ref<Workout[]>([])
const records = ref<WorkoutRecord[]>([])
const dateRange = ref<[string, string] | null>(null)

const checkInForm = ref({
  workoutId: undefined as number | undefined,
  date: new Date().toISOString().split('T')[0],
  durationMinutes: 30,
  calories: 200,
  stepCount: undefined as number | undefined,
  notes: ''
})

const workoutSteps = ref<(WorkoutStep & { completed?: boolean; actualSets?: number; actualReps?: string; weightUsed?: number })[]>([])
const calculatedCalories = computed(() => workoutSteps.value.filter(s => s.completed).reduce((sum, s) => sum + (s.caloriesBurned || 0), 0))

const editDialogVisible = ref(false)
const editForm = ref<RecordUpdateRequest>({})
const editingId = ref<number>(0)

const loadWorkouts = async () => {
  try { workouts.value = (await getWorkouts()).data || [] } catch (e) {}
}

const getWorkoutTitle = (workoutId?: number) => {
  if (!workoutId) return '自由训练'
  const w = workouts.value.find(x => x.id === workoutId)
  return w ? w.title : `方案 #${workoutId}`
}

const difficultyClass = (d: string) => {
  if (d === '初级' || d === 'BEGINNER') return 'dot-beginner'
  if (d === '中级' || d === 'INTERMEDIATE') return 'dot-intermediate'
  return 'dot-advanced'
}

const onWorkoutChange = async (id: number | undefined) => {
  if (!id) { workoutSteps.value = []; checkInForm.value.stepCount = 0; checkInForm.value.calories = 0; return }
  try {
    const workout = (await getWorkoutById(id)).data as Workout
    if (workout?.steps) {
      workoutSteps.value = workout.steps.map(s => ({ ...s, completed: false, actualSets: s.sets, actualReps: s.reps, weightUsed: undefined }))
      checkInForm.value.stepCount = workout.steps.length
      checkInForm.value.calories = workout.steps.reduce((sum, s) => sum + (s.caloriesBurned || 0), 0)
    }
  } catch (e) { workoutSteps.value = []; checkInForm.value.stepCount = 0; checkInForm.value.calories = 0 }
}

const loadRecords = async () => {
  if (!userStore.user) return
  try { records.value = (await getUserRecords(userStore.user.id)).data || []; dateRange.value = null } catch (e) {}
}

const loadRecordsByRange = async () => {
  if (!userStore.user || !dateRange.value) return
  try { records.value = (await getRecordsByDateRange(userStore.user.id, dateRange.value[0], dateRange.value[1])).data || [] } catch (e) {}
}

const handleCheckIn = async () => {
  if (!userStore.user) { ElMessage.error('请先登录'); return }
  if (!checkInForm.value.workoutId) { ElMessage.warning('请选择训练方案'); return }

  const completedSteps = workoutSteps.value.filter(s => s.completed).map(s => ({
    stepId: s.id, exerciseName: s.exerciseName, actualSets: s.actualSets || 1,
    actualReps: s.actualReps || '', weightUsed: s.weightUsed, caloriesBurned: s.caloriesBurned
  }))

  try {
    await checkIn({
      userId: userStore.user.id, workoutId: checkInForm.value.workoutId,
      durationMinutes: checkInForm.value.durationMinutes,
      caloriesBurned: calculatedCalories.value > 0 ? calculatedCalories.value : checkInForm.value.calories,
      recordDate: checkInForm.value.date, stepCount: checkInForm.value.stepCount,
      notes: checkInForm.value.notes, steps: completedSteps.length > 0 ? completedSteps : undefined
    })
    ElMessage.success('打卡成功')
    checkInForm.value = { workoutId: undefined, date: new Date().toISOString().split('T')[0], durationMinutes: 30, calories: 200, stepCount: undefined, notes: '' }
    workoutSteps.value = []
    loadRecords()
    
    // 触发刷新事件，通知个人主页更新数据
    window.dispatchEvent(new CustomEvent('checkin-success'))
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '打卡失败，请重试')
  }
}

const editRecord = (r: WorkoutRecord) => {
  editingId.value = r.id
  editForm.value = { durationMinutes: r.durationMinutes, caloriesBurned: r.caloriesBurned, notes: r.notes }
  editDialogVisible.value = true
}

const saveEdit = async () => {
  try { await updateRecord(editingId.value, editForm.value); ElMessage.success('更新成功'); editDialogVisible.value = false; loadRecords() }
  catch (e) { ElMessage.error('更新失败') }
}

const removeRecord = async (id: number) => {
  try { await ElMessageBox.confirm('确定删除该记录吗？', '提示', { type: 'warning' }); await deleteRecord(id); ElMessage.success('已删除'); loadRecords() }
  catch (e) {}
}

onMounted(() => {
  loadWorkouts().then(() => {
    const q = route.query as Record<string, any>
    if (q.workoutId) {
      checkInForm.value.workoutId = Number(q.workoutId)
      onWorkoutChange(checkInForm.value.workoutId)
      if (q.duration) checkInForm.value.durationMinutes = Number(q.duration)
      if (q.calories) checkInForm.value.calories = Number(q.calories)
    }
  })
  loadRecords()
})
</script>

<style scoped>
.record-page { padding-bottom: 40px; }
.page-banner { background: var(--bg-banner); border-radius: 20px; padding: 32px 40px; margin-bottom: 28px; color: #fff; }
.page-banner-title { font-size: 28px; font-weight: 800; margin: 0 0 8px; }
.page-banner-desc { font-size: 15px; color: var(--text-banner-desc); margin: 0; }
.record-layout { display: grid; grid-template-columns: 380px 1fr; gap: 20px; }
.panel-card { background: var(--bg-card); border-radius: 20px; border: 1px solid var(--border-color); padding: 24px; transition: all 0.4s ease; }
.glow-card:hover { box-shadow: 0 8px 40px rgba(59, 130, 246, 0.1); border-color: rgba(59, 130, 246, 0.15); transform: translateY(-2px); }
.card-header { display: flex; align-items: center; gap: 10px; font-size: 16px; font-weight: 700; color: var(--text-primary); margin-bottom: 20px; }
.checkin-btn { width: 100%; background: var(--btn-primary-gradient); border: none; border-radius: 12px; font-weight: 600; height: 44px; }
.checkin-btn:hover { background: var(--btn-primary-gradient-hover); box-shadow: 0 4px 16px rgba(59,130,246,0.25); }
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.record-filters { display: flex; gap: 8px; align-items: center; }
.filter-btn { background: var(--btn-primary-gradient); border: none; }
.record-card { display: flex; justify-content: space-between; align-items: flex-start; background: var(--bg-card); padding: 16px; border-radius: 14px; border: 1px solid var(--border-color); transition: all 0.3s ease; }
.record-card:hover { box-shadow: 0 4px 16px rgba(59,130,246,0.08); border-color: rgba(59,130,246,0.15); }
.record-title { font-size: 15px; font-weight: 700; color: var(--text-primary); margin-bottom: 8px; }
.record-meta { display: flex; gap: 10px; margin-bottom: 6px; flex-wrap: wrap; }
.record-tag { background: var(--bg-tag); padding: 2px 10px; border-radius: 6px; font-size: 12px; color: var(--text-secondary); }
.record-notes { color: var(--text-tertiary); font-size: 13px; margin: 4px 0 0; }
.record-actions { display: flex; gap: 8px; }
.save-btn { background: var(--btn-primary-gradient); border: none; }
.slider-value { text-align: center; color: var(--color-primary); font-weight: 600; font-size: 14px; margin-top: 4px; }
.step-list { display: flex; flex-direction: column; gap: 10px; }
.workout-step-item { padding: 12px; background: var(--bg-tag); border-radius: 10px; }
.step-inputs { display: flex; gap: 8px; margin: 8px 0 0 24px; flex-wrap: wrap; }
.step-target { margin: 4px 0 0 24px; font-size: 12px; color: var(--text-muted); }
.step-count-display, .calories-display { display: flex; align-items: center; gap: 12px; }
.step-count-tip, .calories-tip { font-size: 12px; color: var(--text-tertiary); }
.workout-option { display: flex; align-items: center; gap: 8px; }
.workout-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-beginner { background: #52c41a; }
.dot-intermediate { background: #faad14; }
.dot-advanced { background: #ff4d4f; }
.workout-title { flex: 1; font-weight: 500; }
.workout-meta { color: var(--text-muted); font-size: 12px; }
@media (max-width: 1100px) { .record-layout { grid-template-columns: 1fr; } }
</style>
