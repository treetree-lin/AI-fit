<template>
  <div class="plan-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">健身计划</h1>
        <p class="page-desc">简单安排每日运动，轻松养成习惯</p>
      </div>
      <el-button type="primary" class="create-btn" @click="openCreate">
        <el-icon><Plus /></el-icon>
        创建计划
      </el-button>
    </div>

    <!-- 当前活跃计划 -->
    <div v-if="activePlan" class="plan-card glow-card">
      <div class="plan-card-header">
        <div>
          <h2 class="plan-name">{{ activePlan.name }}</h2>
          <p class="plan-desc">{{ activePlan.description || '暂无描述' }}</p>
        </div>
        <div class="plan-actions">
          <el-button text type="primary" @click="editPlan">编辑</el-button>
          <el-button text type="danger" @click="deletePlan">删除</el-button>
        </div>
      </div>

      <div class="week-grid">
        <div
          v-for="day in weekDays"
          :key="day.value"
          class="day-card"
          :class="{ 'has-workout': getDayItem(day.value), 'completed': getDayItem(day.value)?.status === 'COMPLETED' }"
        >
          <div class="day-header">
            <span class="day-name">{{ day.label }}</span>
            <el-tag v-if="getDayItem(day.value)?.status === 'COMPLETED'" type="success" size="small" effect="dark">已完成</el-tag>
            <el-tag v-else-if="getDayItem(day.value)?.status === 'PENDING'" type="info" size="small" effect="dark">待训练</el-tag>
          </div>
          <div v-if="getDayItem(day.value)?.exerciseType" class="day-workout">
            <div class="exercise-icon">{{ getExerciseIcon(getDayItem(day.value)?.exerciseType) }}</div>
            <div class="workout-info">
              <span class="workout-title">{{ exerciseTypes.find(t => t.value === getDayItem(day.value)?.exerciseType)?.label || getDayItem(day.value)?.exerciseType }}</span>
              <span class="workout-meta">{{ getDayItem(day.value)?.durationMinutes || 30 }} 分钟 · {{ getDayItem(day.value)?.caloriesBurned || 200 }} kcal</span>
            </div>
          </div>
          <div v-else class="day-empty">
            <el-icon size="24" color="var(--text-tertiary)"><Plus /></el-icon>
            <span>休息</span>
          </div>
          <div v-if="getDayItem(day.value)" class="day-footer">
            <el-button
              v-if="getDayItem(day.value)?.status !== 'COMPLETED'"
              type="primary"
              size="small"
              round
              @click="markComplete(getDayItem(day.value)!.id)"
            >
              标记完成
            </el-button>
            <el-button
              v-else
              text
              type="success"
              size="small"
              @click="markPending(getDayItem(day.value)!.id)"
            >
              已完成
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无活跃计划，创建一个开始训练吧">
      <el-button type="primary" @click="openCreate">创建计划</el-button>
    </el-empty>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="showCreate" :title="isEdit ? '编辑计划' : '创建计划'" width="640px" destroy-on-close>
      <el-form :model="planForm" label-width="80px">
        <el-form-item label="计划名称">
          <el-input v-model="planForm.name" placeholder="例如：3月增肌计划" />
        </el-form-item>
        <el-form-item label="计划描述">
          <el-input v-model="planForm.description" type="textarea" :rows="2" />
        </el-form-item>

        <div class="plan-items-section">
          <div class="section-label">周训练安排</div>
          <div v-for="(item, idx) in planForm.items" :key="idx" class="plan-item-row">
            <span class="day-label">{{ weekDays[item.dayOfWeek - 1]?.label }}</span>
            <el-select v-model="item.exerciseType" placeholder="选择运动" clearable style="width:160px">
              <el-option v-for="t in exerciseTypes" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
            <el-input-number v-model="item.durationMinutes" :min="5" :max="180" :step="5" style="width:120px" />
            <span class="unit-label">分钟</span>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getActivePlan, createPlan, updatePlan, deletePlan as apiDeletePlan, updateItemStatus } from '@/api/plan'
import type { WorkoutPlan } from '@/types'

const userStore = useUserStore()
const activePlan = ref<WorkoutPlan | null>(null)
const showCreate = ref(false)
const isEdit = ref(false)

const weekDays = [
  { label: '周一', value: 1 }, { label: '周二', value: 2 }, { label: '周三', value: 3 },
  { label: '周四', value: 4 }, { label: '周五', value: 5 }, { label: '周六', value: 6 }, { label: '周日', value: 7 }
]

const exerciseTypes = [
  { label: '跑步', value: 'RUNNING' }, { label: '跳绳', value: 'JUMP_ROPE' },
  { label: '力量训练', value: 'STRENGTH' }, { label: '瑜伽', value: 'YOGA' },
  { label: '骑行', value: 'CYCLING' }, { label: '游泳', value: 'SWIMMING' },
  { label: 'HIIT', value: 'HIIT' }, { label: '步行', value: 'WALKING' }, { label: '其他', value: 'OTHER' }
]

const exerciseIconMap: Record<string, string> = {
  RUNNING: '🏃', JUMP_ROPE: '⛹', STRENGTH: '💪', YOGA: '🧘',
  CYCLING: '🚴', SWIMMING: '🏊', HIIT: '🔥', WALKING: '🚶', OTHER: '🏋'
}

const getExerciseIcon = (type?: string) => exerciseIconMap[type || ''] || '🏋'

const planForm = ref<any>({
  name: '', description: '',
  items: weekDays.map(d => ({ dayOfWeek: d.value, exerciseType: '', durationMinutes: 30, caloriesBurned: 200 }))
})

const getDayItem = (dayOfWeek: number): any => {
  return activePlan.value?.items.find(i => i.dayOfWeek === dayOfWeek)
}

const openCreate = () => {
  isEdit.value = false
  planForm.value = {
    name: '', description: '',
    items: weekDays.map(d => ({ dayOfWeek: d.value, exerciseType: undefined as any, durationMinutes: 30, caloriesBurned: 200 }))
  }
  showCreate.value = true
}

const loadActivePlan = async () => {
  if (!userStore.user?.id) return
  try {
    const res = await getActivePlan(userStore.user.id)
    activePlan.value = res.data || null
  } catch (e) { activePlan.value = null }
}

const editPlan = () => {
  if (!activePlan.value) return
  isEdit.value = true
  planForm.value = {
    name: activePlan.value.name, description: activePlan.value.description || '',
    items: weekDays.map(d => {
      const item = activePlan.value?.items.find(i => i.dayOfWeek === d.value)
      return {
        dayOfWeek: d.value,
        exerciseType: item?.exerciseType || '',
        durationMinutes: item?.durationMinutes || 30,
        caloriesBurned: item?.caloriesBurned || 200
      }
    })
  }
  showCreate.value = true
}

const savePlan = async () => {
  if (!planForm.value.name) { ElMessage.warning('请输入计划名称'); return }
  if (!userStore.user?.id) return
  const payload = {
    name: planForm.value.name, description: planForm.value.description,
    items: planForm.value.items
      .filter((i: any) => i.exerciseType)
      .map((i: any) => ({
        dayOfWeek: i.dayOfWeek, exerciseType: i.exerciseType,
        durationMinutes: i.durationMinutes, caloriesBurned: i.caloriesBurned
      }))
  }
  try {
    if (isEdit.value && activePlan.value) {
      await updatePlan(activePlan.value.id, payload)
      ElMessage.success('计划更新成功')
    } else {
      await createPlan(payload, userStore.user.id)
      ElMessage.success('计划创建成功')
    }
    showCreate.value = false
    await loadActivePlan()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const deletePlan = async () => {
  if (!activePlan.value) return
  try {
    await ElMessageBox.confirm('确定要删除当前计划吗？', '提示', { type: 'warning' })
    await apiDeletePlan(activePlan.value.id)
    ElMessage.success('删除成功'); activePlan.value = null
  } catch (e) {}
}

const markComplete = async (itemId: number) => {
  try { await updateItemStatus(itemId, 'COMPLETED'); ElMessage.success('已完成训练'); await loadActivePlan() }
  catch (e) { ElMessage.error('更新失败') }
}

const markPending = async (itemId: number) => {
  try { await updateItemStatus(itemId, 'PENDING'); await loadActivePlan() }
  catch (e) { ElMessage.error('更新失败') }
}

onMounted(() => { loadActivePlan() })
</script>

<style scoped>
.plan-page { padding-bottom: 40px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 28px; }
.page-title { font-size: 28px; font-weight: 800; color: var(--text-primary); margin: 0 0 8px; }
.page-desc { font-size: 14px; color: var(--text-secondary); margin: 0; }
.create-btn { background: var(--btn-primary-gradient); border: none; border-radius: 12px; padding: 12px 24px; font-weight: 600; }
.create-btn:hover { background: var(--btn-primary-gradient-hover); box-shadow: 0 0 20px rgba(59, 130, 246, 0.3); }
.plan-card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 24px; padding: 28px; transition: all 0.4s ease; }
.glow-card { position: relative; }
.glow-card:hover { box-shadow: 0 8px 40px rgba(59, 130, 246, 0.12), 0 0 0 1px rgba(59, 130, 246, 0.08); transform: translateY(-2px); }
.plan-card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.plan-name { font-size: 20px; font-weight: 700; color: var(--text-primary); margin: 0 0 6px; }
.plan-desc { font-size: 13px; color: var(--text-secondary); margin: 0; }
.plan-actions { display: flex; gap: 8px; }
.week-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 14px; }
.day-card { background: var(--bg-page); border: 1px solid var(--border-color); border-radius: 18px; padding: 16px; min-height: 180px; display: flex; flex-direction: column; transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1); position: relative; overflow: hidden; }
.day-card::after { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px; background: linear-gradient(90deg, #3b82f6, #a78bfa); opacity: 0; transition: opacity 0.35s ease; }
.day-card:hover { transform: translateY(-6px); box-shadow: 0 12px 32px rgba(59, 130, 246, 0.15); border-color: rgba(59, 130, 246, 0.25); }
.day-card:hover::after { opacity: 1; }
.day-card.completed { border-color: rgba(34, 197, 94, 0.2); }
.day-card.completed::after { background: linear-gradient(90deg, #22c55e, #4ade80); opacity: 1; }
.day-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.day-name { font-size: 14px; font-weight: 700; color: var(--text-primary); }
.day-workout { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; text-align: center; }
.exercise-icon { font-size: 36px; }
.workout-info { display: flex; flex-direction: column; gap: 2px; }
.workout-title { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.workout-meta { font-size: 11px; color: var(--text-tertiary); }
.day-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; color: var(--text-tertiary); font-size: 12px; }
.day-footer { margin-top: 10px; }
.plan-items-section { margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--border-color); }
.section-label { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 12px; }
.plan-item-row { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.day-label { font-size: 13px; color: var(--text-secondary); width: 50px; flex-shrink: 0; }
.unit-label { font-size: 12px; color: var(--text-tertiary); }
@media (max-width: 1200px) { .week-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 768px) { .week-grid { grid-template-columns: repeat(2, 1fr); } .page-header { flex-direction: column; align-items: flex-start; gap: 12px; } }
</style>
