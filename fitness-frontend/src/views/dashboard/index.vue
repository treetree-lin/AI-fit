<template>
  <div class="dashboard">
    <div class="hero-banner">
      <div class="hero-content">
        <div class="hero-greeting">
          <h1 class="hero-title"><span class="greeting-icon">{{ greetingIcon }}</span>{{ greeting }}，{{ userStore.user?.username || '健身达人' }}</h1>
          <p class="hero-date">{{ currentDate }}</p>
        </div>
        <div class="hero-streak">
          <div class="streak-icon">🔥</div>
          <div class="streak-info">
            <div class="streak-num">连续打卡 {{ streakDays }} 天</div>
            <div class="streak-label">继续保持！</div>
          </div>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <div class="stat-value">{{ weekStats.totalMinutes }}</div>
          <div class="stat-label">本周训练(分)</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ weekStats.totalCalories }}</div>
          <div class="stat-label">本周消耗(kcal)</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ weekStats.workoutCount }}</div>
          <div class="stat-label">本周打卡</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ streakDays }}</div>
          <div class="stat-label">连续打卡(天)</div>
        </div>
      </div>
    </div>

    <div class="section recommend-section">
      <div class="section-header">
        <h2 class="section-title"><el-icon size="22" color="#3b82f6"><StarFilled /></el-icon>AI 推荐方案</h2>
        <el-radio-group v-model="recommendType" size="small" @change="loadRecommendations">
          <el-radio-button label="hybrid">智能混合</el-radio-button>
          <el-radio-button label="rule">规则匹配</el-radio-button>
          <el-radio-button label="cf">协同过滤</el-radio-button>
        </el-radio-group>
      </div>
      <el-row :gutter="16">
        <el-col v-for="(item, index) in recommendations.slice(0, 6)" :key="item.workoutId" :xs="24" :sm="12" :md="8">
          <div class="recommend-card glow-card" @click="goToWorkout(item.workoutId)">
            <div class="recommend-rank">#{{ index + 1 }}</div>
            <div v-if="item.type" class="recommend-type">{{ getTypeLabel(item.type) }}</div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.reason }}</p>
            <div class="recommend-meta">
              <el-tag v-if="item.difficulty" size="small" effect="plain">{{ item.difficulty }}</el-tag>
              <span v-if="item.duration">{{ item.duration }} 分钟</span>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-if="recommendations.length === 0" description="暂无推荐方案" />
    </div>

    <div class="section chart-section">
      <div class="section-header">
        <h2 class="section-title"><el-icon size="22" color="#3b82f6"><TrendCharts /></el-icon>运动趋势</h2>
      </div>
      <div class="chart-card glow-card">
        <v-chart class="chart" :option="chartOption" autoresize />
      </div>
    </div>

    <div class="section quick-section">
      <div class="section-header">
        <h2 class="section-title"><el-icon size="22" color="#3b82f6"><Compass /></el-icon>快捷入口</h2>
      </div>
      <div class="quick-grid">
        <div class="quick-card" @click="router.push('/workouts')">
          <div class="quick-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);"><el-icon size="28" color="#fff"><FirstAidKit /></el-icon></div>
          <div class="quick-title">浏览方案</div>
          <div class="quick-desc">发现适合你的训练计划</div>
        </div>
        <div class="quick-card" @click="router.push('/records')">
          <div class="quick-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);"><el-icon size="28" color="#fff"><Calendar /></el-icon></div>
          <div class="quick-title">打卡记录</div>
          <div class="quick-desc">记录每一次运动</div>
        </div>
        <div class="quick-card" @click="router.push('/chat')">
          <div class="quick-icon" style="background: linear-gradient(135deg, #10b981, #34d399);"><el-icon size="28" color="#fff"><ChatDotRound /></el-icon></div>
          <div class="quick-title">AI 教练</div>
          <div class="quick-desc">获取专业建议</div>
        </div>
        <div class="quick-card" @click="router.push('/plan')">
          <div class="quick-icon" style="background: linear-gradient(135deg, #8b5cf6, #a78bfa);"><el-icon size="28" color="#fff"><Document /></el-icon></div>
          <div class="quick-title">健身计划</div>
          <div class="quick-desc">定制你的周计划</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getRecommendations, getRuleRecommendations, getCfRecommendations } from '@/api/recommendation'
import { getOverviewStats, getLast7DaysStats } from '@/api/record'
import { StarFilled, TrendCharts, Compass, FirstAidKit, Calendar, ChatDotRound, Document } from '@element-plus/icons-vue'
import type { RecommendItem } from '@/api/recommendation'

const router = useRouter()
const userStore = useUserStore()

const recommendations = ref<RecommendItem[]>([])
const recommendType = ref('hybrid')

const weekStats = ref({ totalMinutes: 0, totalCalories: 0, workoutCount: 0 })
const streakDays = ref(0)

const greetingIcon = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '🌙'
  if (hour < 9) return '🌅'
  if (hour < 12) return '☀️'
  if (hour < 14) return '🌞'
  if (hour < 18) return '🌤️'
  return '🌙'
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const d = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${weekDays[d.getDay()]}`
})

const loadRecommendations = async () => {
  if (!userStore.user) return
  try {
    let res
    if (recommendType.value === 'rule') res = await getRuleRecommendations(userStore.user.id, 6)
    else if (recommendType.value === 'cf') res = await getCfRecommendations(userStore.user.id, 6)
    else res = await getRecommendations(userStore.user.id, 6)
    if (recommendType.value === 'hybrid') {
      recommendations.value = (res.data as any)?.items || []
    } else {
      recommendations.value = (res.data as any) || []
    }
  } catch (e) { recommendations.value = [] }
}

const getTypeLabel = (type?: string) => {
  if (type === 'rule') return '规则推荐'
  if (type === 'cf') return '协同过滤'
  return '智能推荐'
}

const goToWorkout = (id: number) => router.push(`/workouts/${id}`)

const chartOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['训练时长(分)', '消耗热量(kcal)'], textStyle: { color: 'var(--text-secondary)' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: ['周一','周二','周三','周四','周五','周六','周日'], axisLabel: { color: 'var(--text-secondary)' }, axisLine: { lineStyle: { color: 'var(--border-color)' } } },
  yAxis: [
    { type: 'value', name: '分钟', axisLabel: { color: 'var(--text-secondary)' }, splitLine: { lineStyle: { color: 'var(--border-color)' } } },
    { type: 'value', name: 'kcal', axisLabel: { color: 'var(--text-secondary)' }, splitLine: { show: false } }
  ],
  series: [
    { name: '训练时长(分)', type: 'line', data: [30, 45, 0, 60, 30, 90, 45], smooth: true, itemStyle: { color: '#3b82f6' }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(59,130,246,0.3)' }, { offset: 1, color: 'rgba(59,130,246,0.02)' }] } } },
    { name: '消耗热量(kcal)', type: 'line', yAxisIndex: 1, data: [200, 350, 0, 450, 220, 600, 300], smooth: true, itemStyle: { color: '#f59e0b' } }
  ]
})

const loadStats = async () => {
  if (!userStore.user) return
  try {
    const stats = (await getOverviewStats(userStore.user.id)).data
    if (stats) {
      weekStats.value.totalMinutes = stats.totalMinutes || 0
      weekStats.value.totalCalories = stats.totalCalories || 0
      weekStats.value.workoutCount = stats.totalWorkouts || 0
      streakDays.value = stats.streakDays || 0
    }
    const weekly = (await getLast7DaysStats(userStore.user.id)).data || []
    if (weekly.length > 0) {
      chartOption.value.xAxis.data = weekly.map((d: any) => d.date?.slice(5) || '')
      chartOption.value.series[0].data = weekly.map((d: any) => d.durationMinutes || 0)
      chartOption.value.series[1].data = weekly.map((d: any) => d.caloriesBurned || 0)
    }
  } catch (e) {}
}

onMounted(() => {
  loadRecommendations()
  loadStats()
  
  // 监听打卡成功事件，自动刷新主页数据
  window.addEventListener('checkin-success', loadStats)
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('checkin-success', loadStats)
})
</script>

<style scoped>
.dashboard { padding-bottom: 40px; }
.hero-banner { background: var(--bg-banner); border-radius: 20px; padding: 32px 40px; margin-bottom: 28px; color: #fff; }
.hero-content { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.hero-title { font-size: 28px; font-weight: 800; margin: 0 0 6px; color: #fff; }
.greeting-icon { margin-right: 8px; font-size: 26px; vertical-align: middle; line-height: 1; }
.hero-date { font-size: 14px; color: rgba(255,255,255,0.75); margin: 0; }
.hero-streak { display: flex; align-items: center; gap: 12px; background: rgba(255,255,255,0.12); padding: 14px 20px; border-radius: 14px; border: 1px solid rgba(255,255,255,0.15); backdrop-filter: blur(8px); }
.streak-icon { font-size: 28px; }
.streak-num { font-size: 15px; font-weight: 700; color: #fff; }
.streak-label { font-size: 12px; color: rgba(255,255,255,0.7); }
.hero-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card { background: var(--bg-card); border-radius: 14px; padding: 16px; text-align: center; border: 1px solid var(--border-color); box-shadow: var(--shadow-sm); }
.stat-value { font-size: 28px; font-weight: 800; color: var(--color-primary); }
.stat-label { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
.section { margin-bottom: 28px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-title { font-size: 20px; font-weight: 700; color: var(--text-primary); display: flex; align-items: center; gap: 8px; margin: 0; }
.recommend-card { background: var(--bg-card); border-radius: 16px; border: 1px solid var(--border-color); padding: 20px; cursor: pointer; transition: all 0.3s ease; position: relative; overflow: hidden; margin-bottom: 16px; }
.glow-card:hover { box-shadow: 0 8px 40px rgba(59,130,246,0.1); border-color: rgba(59,130,246,0.15); transform: translateY(-2px); }
.recommend-rank { position: absolute; top: 12px; right: 12px; font-size: 20px; font-weight: 800; color: var(--color-primary); opacity: 0.15; }
.recommend-type { font-size: 12px; color: var(--color-primary); font-weight: 600; margin-bottom: 8px; }
.recommend-card h3 { font-size: 16px; font-weight: 700; color: var(--text-primary); margin: 0 0 8px; }
.recommend-card p { font-size: 13px; color: var(--text-secondary); margin: 0 0 12px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.recommend-meta { display: flex; align-items: center; gap: 10px; }
.chart-card { background: var(--bg-card); border-radius: 16px; border: 1px solid var(--border-color); padding: 20px; }
.chart { height: 320px; }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.quick-card { background: var(--bg-card); border-radius: 16px; border: 1px solid var(--border-color); padding: 24px; cursor: pointer; transition: all 0.3s ease; }
.quick-card:hover { box-shadow: 0 8px 32px rgba(59,130,246,0.1); transform: translateY(-2px); border-color: rgba(59,130,246,0.15); }
.quick-icon { width: 52px; height: 52px; border-radius: 14px; display: flex; align-items: center; justify-content: center; margin-bottom: 14px; }
.quick-title { font-size: 16px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; }
.quick-desc { font-size: 13px; color: var(--text-muted); }
@media (max-width: 900px) { .hero-stats { grid-template-columns: repeat(2, 1fr); } .quick-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .hero-content { flex-direction: column; align-items: flex-start; gap: 16px; } .hero-stats { grid-template-columns: 1fr 1fr; } .quick-grid { grid-template-columns: 1fr; } }
</style>
