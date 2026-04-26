<template>
  <div class="profile-page">
    <!-- 顶部用户信息横幅 -->
    <div class="profile-hero">
      <div class="profile-hero-left">
        <el-upload class="avatar-uploader" action="" :auto-upload="false" :show-file-list="false" :on-change="handleAvatarChange">
          <el-avatar :size="80" :src="userStore.user?.avatarUrl" icon="UserFilled" class="user-avatar" />
          <div class="avatar-overlay"><el-icon size="16"><Camera /></el-icon></div>
        </el-upload>
        <div class="hero-info">
          <h2 class="user-name">{{ userStore.user?.username || '未登录' }}</h2>
          <p v-if="profile.bio" class="user-bio">{{ profile.bio }}</p>
          <div class="user-meta">
            <el-tag v-if="profile.level" size="small" effect="dark" type="warning">{{ profile.level }}</el-tag>
            <span v-if="profile.location" class="location-text"><el-icon size="12"><Location /></el-icon>{{ profile.location }}</span>
          </div>
        </div>
      </div>
      <div class="profile-hero-right">
        <div class="date-info">
          <div class="greeting">{{ greeting }}</div>
          <div class="date">{{ currentDate }}</div>
        </div>
        <div class="streak-badge">
          <div class="streak-icon">🔥</div>
          <div class="streak-num">连续打卡 {{ streakDays }} 天</div>
        </div>
        <el-button type="primary" class="edit-btn" @click="showEdit = true">
          <el-icon><EditPen /></el-icon>编辑档案
        </el-button>
      </div>
    </div>

    <!-- 中间区域：左上柱形图 | 右上雷达图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <div class="panel-card glow-card">
          <div class="card-header">
            <el-icon size="18" color="#3b82f6"><Histogram /></el-icon>
            <span>本周训练</span>
          </div>
          <v-chart class="bar-chart" :option="barOption" autoresize />
          <div class="week-summary">
            <div class="summary-item">
              <div class="summary-label">本周总计时长</div>
              <div class="summary-value">{{ weekTotalMinutes }} min</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">本周总计消耗</div>
              <div class="summary-value">{{ weekTotalCalories.toLocaleString() }} kcal</div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="panel-card glow-card radar-card">
          <div class="card-header">
            <el-icon size="18" color="#3b82f6"><DataLine /></el-icon>
            <span>体能雷达图</span>
          </div>
          <v-chart class="radar-chart" :option="radarOption" autoresize />
        </div>
      </el-col>
    </el-row>

    <!-- 底部区域：左下勋章墙 | 右下身体数据 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="16">
        <div class="panel-card glow-card badges-panel">
          <div class="card-header">
            <el-icon size="18" color="#3b82f6"><Trophy /></el-icon>
            <span>我的勋章</span>
          </div>
          <div class="badges-grid">
            <div v-for="badge in badges" :key="badge.id" class="badge-item" :class="{ locked: !badge.unlocked }" :title="badge.description">
              <div class="badge-icon">{{ badge.icon }}</div>
              <div class="badge-name">{{ badge.name }}</div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="panel-card glow-card stats-card">
          <div class="card-header">
            <el-icon size="18" color="#3b82f6"><FirstAidKit /></el-icon>
            <span>身体数据</span>
          </div>
          <div class="stats-list">
            <div class="stats-row">
              <span class="stats-label">BMI</span>
              <span class="stats-value">{{ bmiValue }}</span>
            </div>
            <div class="stats-row">
              <span class="stats-label">体脂率</span>
              <span class="stats-value">{{ profile.bodyFat ? profile.bodyFat + '%' : '--' }}</span>
            </div>
            <div class="stats-row">
              <span class="stats-label">身高</span>
              <span class="stats-value">{{ profile.height ? profile.height + ' cm' : '--' }}</span>
            </div>
            <div class="stats-row">
              <span class="stats-label">体重</span>
              <span class="stats-value">{{ profile.weight ? profile.weight + ' kg' : '--' }}</span>
            </div>
            <div class="stats-row">
              <span class="stats-label">健身目标</span>
              <span class="stats-value">{{ profile.goal || '--' }}</span>
            </div>
            <div class="stats-row">
              <span class="stats-label">健身水平</span>
              <span class="stats-value">{{ profile.level || '--' }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showEdit" title="编辑档案" width="560px" destroy-on-close>
      <el-form :model="profile" label-position="top">
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="年龄"><el-input-number v-model="profile.age" :min="1" :max="120" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="性别"><el-select v-model="profile.gender" style="width: 100%"><el-option label="男" value="MALE" /><el-option label="女" value="FEMALE" /><el-option label="其他" value="OTHER" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="身高 (cm)"><el-input-number v-model="profile.height" :min="50" :max="300" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="体重 (kg)"><el-input-number v-model="profile.weight" :min="20" :max="300" :precision="1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="体脂率 (%)"><el-input-number v-model="profile.bodyFat" :min="0" :max="100" :precision="1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="健身目标"><el-select v-model="profile.goal" style="width: 100%"><el-option label="减脂" value="减脂" /><el-option label="增肌" value="增肌" /><el-option label="维持" value="维持" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="签名"><el-input v-model="profile.bio" maxlength="100" show-word-limit placeholder="写一段简介..." /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所在地"><el-input v-model="profile.location" placeholder="城市/地区" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="健身水平">
          <el-radio-group v-model="profile.level">
            <el-radio-button label="初级" value="初级" />
            <el-radio-button label="中级" value="中级" />
            <el-radio-button label="高级" value="高级" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="可用设备">
          <div class="equipment-tags">
            <el-check-tag v-for="item in equipmentOptions" :key="item" :checked="selectedEquipment.includes(item)" class="equipment-tag" @change="toggleEquipment(item)">{{ item }}</el-check-tag>
          </div>
          <el-input v-model="otherEquipment" placeholder="其他设备，逗号分隔" style="margin-top: 8px" />
        </el-form-item>
        <el-form-item label="伤病情况">
          <el-input v-model="injuryInput" type="textarea" :rows="2" placeholder="如有伤病请说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, EditPen, Trophy, Location, DataLine, Histogram, FirstAidKit } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile, uploadAvatar } from '@/api/user'
import { getOverviewStats, getLast7DaysStats, getRadarStats } from '@/api/record'
import type { UserProfile } from '@/types'

const userStore = useUserStore()

const profile = reactive<Partial<UserProfile>>({
  age: undefined, gender: undefined, height: undefined, weight: undefined,
  bodyFat: undefined, goal: '', level: '初级', bio: '', location: ''
})

const badges = ref([
  { id: '1', name: '初出茅庐', icon: '🏃', description: '完成首次训练', unlocked: true },
  { id: '2', name: '持之以恒', icon: '🔥', description: '连续打卡7天', unlocked: true },
  { id: '3', name: '铁人', icon: '💪', description: '累计训练100次', unlocked: false },
  { id: '4', name: '减脂达人', icon: '🥗', description: '消耗10000kcal', unlocked: false },
  { id: '5', name: '力量之王', icon: '🏋️', description: '完成力量训练50次', unlocked: false },
  { id: '6', name: '百炼成钢', icon: '⚡', description: '累计训练500次', unlocked: false },
  { id: '7', name: '晨练达人', icon: '🌅', description: '连续7天早晨训练', unlocked: false },
  { id: '8', name: '耐力先锋', icon: '🏔️', description: '单次训练超过90分钟', unlocked: false },
  { id: '9', name: '完美主义', icon: '⭐', description: '10次训练评分5星', unlocked: false },
  { id: '10', name: '坚持不懈', icon: '🎯', description: '连续打卡30天', unlocked: false },
  { id: '11', name: '热量杀手', icon: '🔥', description: '累计消耗50000kcal', unlocked: false },
  { id: '12', name: '健身达人', icon: '🏆', description: '累计训练200次', unlocked: false },
])

const showEdit = ref(false)
const saving = ref(false)
const streakDays = ref(0)
const weekTotalMinutes = ref(0)
const weekTotalCalories = ref(0)

const equipmentOptions = ['哑铃','杠铃','瑜伽垫','跑步机','动感单车','健身球','弹力带','卧推架','引体向上杆','壶铃','跳绳','TRX悬挂带','划船机','史密斯机','腿举机','龙门架']
const selectedEquipment = ref<string[]>([])
const otherEquipment = ref('')
const injuryInput = ref('')

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

const barData = ref<{ date: string; value: number }[]>([])

const barOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
  xAxis: {
    type: 'category',
    data: barData.value.map(d => d.date),
    axisLabel: { color: 'var(--text-secondary)', fontSize: 11 },
    axisLine: { show: false },
    axisTick: { show: false }
  },
  yAxis: {
    type: 'value',
    show: false
  },
  series: [{
    type: 'bar',
    data: barData.value.map(d => d.value),
    barWidth: '40%',
    itemStyle: {
      borderRadius: [8, 8, 0, 0],
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: '#6366f1' },
          { offset: 1, color: '#3b82f6' }
        ]
      }
    }
  }]
}))

const radarValues = ref([70, 65, 50, 60, 55])
const bmiValue = computed(() => {
  if (!profile.height || !profile.weight) return '--'
  const h = profile.height / 100
  const bmi = profile.weight / (h * h)
  return bmi.toFixed(1)
})

const radarOption = computed(() => ({
  radar: {
    indicator: [
      { name: '力量', max: 100 }, { name: '耐力', max: 100 }, { name: '柔韧', max: 100 },
      { name: '速度', max: 100 }, { name: '爆发', max: 100 }
    ],
    axisName: { color: 'var(--text-secondary)', fontSize: 11 },
    splitArea: { areaStyle: { color: ['rgba(59,130,246,0.02)', 'rgba(59,130,246,0.05)'] } },
    axisLine: { lineStyle: { color: 'var(--border-color)' } },
    splitLine: { lineStyle: { color: 'var(--border-color)' } }
  },
  series: [{
    type: 'radar', data: [{
      value: radarValues.value,
      name: '当前能力',
      areaStyle: { color: 'rgba(99, 102, 241, 0.2)' },
      lineStyle: { color: '#6366f1', width: 2 },
      itemStyle: { color: '#6366f1' }
    }]
  }],
  tooltip: { trigger: 'item' }
}))

const toggleEquipment = (item: string) => {
  const idx = selectedEquipment.value.indexOf(item)
  if (idx > -1) selectedEquipment.value.splice(idx, 1)
  else selectedEquipment.value.push(item)
}

const handleAvatarChange = async (file: any) => {
  try {
    const res = await uploadAvatar(file.raw)
    if (userStore.user && res.data?.url) {
      userStore.user.avatarUrl = res.data.url
      ElMessage.success('头像上传成功')
    }
  } catch { ElMessage.error('上传失败') }
}

const loadProfile = async () => {
  if (!userStore.user) return
  try {
    const data = (await getProfile(userStore.user.id)).data
    Object.assign(profile, data)
    
    // 同步头像 URL 到 userStore
    if (data.avatarUrl && userStore.user) {
      userStore.user.avatarUrl = data.avatarUrl
    }
    
    if (data.equipment) selectedEquipment.value = [...data.equipment]
    if (data.injuryHistory && data.injuryHistory.length > 0) injuryInput.value = data.injuryHistory.join(', ')

    const stats = (await getOverviewStats(userStore.user.id)).data
    if (stats) {
      streakDays.value = stats.streakDays || 0
    }

    const weekly = (await getLast7DaysStats(userStore.user.id)).data || []
    barData.value = weekly.map((d: any) => ({
      date: d.date?.slice(5)?.replace('-', '.') || '',
      value: d.durationMinutes || 0
    }))
    weekTotalMinutes.value = weekly.reduce((sum: number, d: any) => sum + (d.durationMinutes || 0), 0)
    // 优先使用 overview 接口返回的 totalCalories（与 dashboard 保持一致），
    // 若接口无返回再从 daily 数据累加兜底
    weekTotalCalories.value = stats?.totalCalories ?? weekly.reduce((sum: number, d: any) => sum + (d.caloriesBurned || 0), 0)

    const radar = (await getRadarStats(userStore.user.id)).data
    if (radar && typeof radar === 'object') {
      radarValues.value = [
        radar.strength || 70, radar.endurance || 65, radar.flexibility || 50,
        radar.speed || 60, radar.coordination || 55
      ]
    } else {
      // 如果没有数据，使用默认值
      radarValues.value = [70, 65, 50, 60, 55]
    }
    console.log('雷达图数据:', radarValues.value)
  } catch (e) {
    console.error('加载个人主页失败:', e)
  }
}

const saveProfile = async () => {
  if (!userStore.user) return
  saving.value = true
  try {
    const payload: any = { ...profile }
    delete payload.userId
    delete payload.username
    const equipList = [...selectedEquipment.value]
    if (otherEquipment.value) {
      otherEquipment.value.split(',').map((s: string) => s.trim()).filter(Boolean).forEach((e: string) => { if (!equipList.includes(e)) equipList.push(e) })
    }
    payload.equipment = equipList
    if (injuryInput.value) payload.injuryHistory = injuryInput.value.split(',').map((s: string) => s.trim()).filter(Boolean)
    await updateProfile(payload)
    ElMessage.success('保存成功')
    showEdit.value = false
  } catch (e) { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

onMounted(() => {
  loadProfile()
  
  // 监听打卡成功事件，自动刷新主页数据
  window.addEventListener('checkin-success', loadProfile)
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('checkin-success', loadProfile)
})
</script>

<style scoped>
.profile-page { padding-bottom: 40px; }

/* Hero */
.profile-hero {
  background: var(--bg-banner);
  border-radius: 20px;
  padding: 28px 36px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  color: #fff;
}
.profile-hero-left { display: flex; align-items: center; gap: 20px; }
.avatar-uploader { position: relative; cursor: pointer; }
.user-avatar { border: 3px solid var(--border-color); }
.avatar-overlay {
  position: absolute; bottom: 0; right: 0; width: 28px; height: 28px;
  background: var(--color-primary); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; opacity: 0; transition: opacity 0.3s;
}
.avatar-uploader:hover .avatar-overlay { opacity: 1; }
.hero-info { display: flex; flex-direction: column; gap: 6px; }
.user-name { font-size: 22px; font-weight: 800; color: #fff; margin: 0; }
.user-bio { font-size: 13px; color: rgba(255,255,255,0.8); margin: 0; }
.user-meta { display: flex; align-items: center; gap: 10px; }
.location-text { font-size: 12px; color: rgba(255,255,255,0.7); display: flex; align-items: center; gap: 4px; }

.profile-hero-right { display: flex; align-items: center; gap: 20px; }
.date-info { text-align: right; }
.greeting { font-size: 14px; color: rgba(255,255,255,0.8); }
.date { font-size: 13px; color: rgba(255,255,255,0.65); }
.streak-badge {
  display: flex; align-items: center; gap: 8px;
  background: var(--bg-card); padding: 10px 16px;
  border-radius: 12px; border: 1px solid var(--border-color);
}
.streak-icon { font-size: 22px; }
.streak-num { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.edit-btn { border-radius: 10px; background: var(--btn-primary-gradient); border: none; font-weight: 600; }

/* Charts */
.chart-row { margin-bottom: 20px; }
.panel-card { background: var(--bg-card); border-radius: 20px; border: 1px solid var(--border-color); padding: 24px; transition: all 0.4s ease; }
.glow-card:hover { box-shadow: 0 8px 40px rgba(59, 130, 246, 0.1); border-color: rgba(59, 130, 246, 0.15); transform: translateY(-2px); }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 700; color: var(--text-primary); margin-bottom: 16px; }
.bar-chart { height: 220px; }
.week-summary { display: flex; gap: 24px; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--border-color); }
.summary-item { flex: 1; }
.summary-label { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; }
.summary-value { font-size: 22px; font-weight: 800; color: var(--text-primary); }
.radar-card { height: 100%; display: flex; flex-direction: column; min-height: 360px; }
.radar-chart { flex: 1; min-height: 280px; width: 100%; }

/* Badges */
.badges-panel { margin-bottom: 0; }
.badges-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr)); 
  gap: 16px; 
}
.badge-item { 
  text-align: center; 
  padding: 16px 8px; 
  border-radius: 12px; 
  background: var(--bg-tag); 
  transition: all 0.2s; 
  cursor: default;
}
.badge-item:hover:not(.locked) { 
  transform: translateY(-4px); 
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15); 
}
.badge-item.locked { opacity: 0.35; filter: grayscale(1); }
.badge-icon { font-size: 32px; margin-bottom: 8px; }
.badge-name { font-size: 12px; color: var(--text-secondary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.equipment-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.equipment-tag { border-radius: 6px; padding: 6px 12px; font-size: 13px; transition: all 0.2s; }
.equipment-tag.is-checked { background: linear-gradient(135deg, #3b82f6, #60a5fa); color: #fff; border-color: #3b82f6; box-shadow: 0 2px 8px rgba(59,130,246,0.25); }

.stats-card { height: 100%; }
.stats-list { display: flex; flex-direction: column; gap: 14px; }
.stats-row { display: flex; justify-content: space-between; align-items: center; font-size: 14px; }
.stats-label { color: var(--text-muted); }
.stats-value { color: var(--text-primary); font-weight: 700; }

.bottom-row { margin-bottom: 20px; }

@media (max-width: 900px) {
  .profile-hero { flex-direction: column; align-items: flex-start; }
  .profile-hero-right { width: 100%; justify-content: space-between; }
  .chart-row :deep(.el-col-16), .chart-row :deep(.el-col-8) { flex: 0 0 100%; max-width: 100%; margin-bottom: 16px; }
  .bottom-row :deep(.el-col-16), .bottom-row :deep(.el-col-8) { flex: 0 0 100%; max-width: 100%; margin-bottom: 16px; }
}
</style>
