<template>
  <div class="register-page">
    <div class="register-left">
      <div class="register-brand">
        <div class="brand-icon"><img src="/logo.png" alt="Logo" class="brand-logo" /></div>
        <div class="brand-text">
          <div class="brand-title">AI-FIT 智动健身</div>
          <div class="brand-slogan">智能健身运动平台</div>
        </div>
      </div>
      <div class="register-hero">
        <h1>开启你的<br><span class="highlight">智能健身之旅</span></h1>
        <p>注册即享 AI 个性化训练方案推荐，让每一次训练都更高效</p>
        <div class="hero-features">
          <div class="feature-item"><el-icon size="20" color="#60a5fa"><Star /></el-icon><span>智能推荐</span></div>
          <div class="feature-item"><el-icon size="20" color="#60a5fa"><FirstAidKit /></el-icon><span>专业方案</span></div>
          <div class="feature-item"><el-icon size="20" color="#60a5fa"><ChatDotRound /></el-icon><span>AI 助手</span></div>
        </div>
      </div>
    </div>

    <div class="register-right">
      <div class="register-card">
        <div class="register-header">
          <h2>创建账号</h2>
          <p>填写以下信息完成注册</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username"><el-input v-model="form.username" placeholder="3-30 字符" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" placeholder="建议最少 8 位" show-password /></el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="年龄">
                <div class="slider-input"><el-slider v-model="form.age" :min="10" :max="100" show-stops :step="1" /><el-input-number v-model="form.age" :min="10" :max="100" controls-position="right" class="slider-number" /></div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
                  <el-option label="男" value="male" /><el-option label="女" value="female" /><el-option label="其他" value="other" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="身高 (cm)"><el-input :model-value="form.height ? form.height + ' cm' : '点击选择'" readonly placeholder="点击选择" @click="openWheel('height')" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="体重 (kg)"><el-input :model-value="form.weight ? form.weight + ' kg' : '点击选择'" readonly placeholder="点击选择" @click="openWheel('weight')" /></el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="健身目标">
            <el-select v-model="form.goal" placeholder="请选择健身目标" style="width: 100%">
              <el-option label="减脂" value="减脂" /><el-option label="增肌" value="增肌" /><el-option label="维持" value="维持" />
            </el-select>
          </el-form-item>
          <el-form-item label="健身水平">
            <el-select v-model="form.level" placeholder="请选择水平" style="width: 100%">
              <el-option label="初级" value="初级" /><el-option label="中级" value="中级" /><el-option label="高级" value="高级" />
            </el-select>
          </el-form-item>
          <el-form-item label="可用设备">
            <div class="equipment-tags">
              <el-check-tag v-for="item in equipmentOptions" :key="item" :checked="selectedEquipment.includes(item)" class="equipment-tag" @change="toggleEquipment(item)">{{ item }}</el-check-tag>
            </div>
            <el-input v-model="otherEquipment" placeholder="其他设备，逗号分隔" style="margin-top: 8px" />
          </el-form-item>
          <el-form-item label="既往伤病">
            <el-input v-model="form.injuries" type="textarea" :rows="2" placeholder="如有伤病请说明，方便推荐合适方案" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">立即注册</el-button>
          </el-form-item>
          <div class="register-footer"><span>已有账号？</span><el-link type="primary" @click="router.push('/login')">立即登录</el-link></div>
        </el-form>
      </div>
    </div>
    <el-dialog v-model="wheelVisible" title="选择数值" width="320px" align-center>
      <div class="wheel-dialog-body">
        <WheelPicker v-if="pickerType === 'height'" v-model="wheelValue" :min="100" :max="250" :step="1" unit=" cm" />
        <WheelPicker v-else-if="pickerType === 'weight'" v-model="wheelValue" :min="30" :max="200" :step="1" :precision="1" unit=" kg" />
      </div>
      <template #footer><el-button @click="wheelVisible = false">取消</el-button><el-button type="primary" @click="confirmWheel">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import WheelPicker from '@/components/WheelPicker.vue'
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, FirstAidKit, ChatDotRound } from '@element-plus/icons-vue'
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '', password: '', age: undefined as number | undefined,
  gender: '', height: undefined as number | undefined, weight: undefined as number | undefined,
  goal: '', level: '', injuries: ''
})

const equipmentOptions = ['哑铃','杠铃','瑜伽垫','跑步机','动感单车','健身球','弹力带','卧推架','引体向上杆','壶铃','跳绳','TRX悬挂带','划船机','史密斯机','腿举机','龙门架']
const selectedEquipment = ref<string[]>([])
const otherEquipment = ref('')

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 30, message: '长度在 3 到 30 个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 8, message: '密码至少 8 位', trigger: 'blur' }]
}

const pickerType = ref<'height' | 'weight' | null>(null)
const wheelVisible = ref(false)
const wheelValue = ref<number | undefined>(undefined)

const openWheel = (type: 'height' | 'weight') => {
  pickerType.value = type
  wheelValue.value = type === 'height' ? (form.height || 170) : (form.weight || 65)
  wheelVisible.value = true
}

const confirmWheel = () => {
  if (pickerType.value === 'height') form.height = wheelValue.value
  else if (pickerType.value === 'weight') form.weight = wheelValue.value
  wheelVisible.value = false
}

const toggleEquipment = (item: string) => {
  const idx = selectedEquipment.value.indexOf(item)
  if (idx > -1) selectedEquipment.value.splice(idx, 1)
  else selectedEquipment.value.push(item)
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const payload: any = { username: form.username, password: form.password }
    if (form.age !== undefined) payload.age = form.age
    if (form.gender) payload.gender = form.gender
    if (form.height !== undefined) payload.height = form.height
    if (form.weight !== undefined) payload.weight = form.weight
    if (form.goal) payload.goal = form.goal
    if (form.level) payload.level = form.level
    const equipList = [...selectedEquipment.value]
    if (otherEquipment.value) {
      otherEquipment.value.split(',').map(s => s.trim()).filter(Boolean).forEach(e => { if (!equipList.includes(e)) equipList.push(e) })
    }
    if (equipList.length) payload.equipment = equipList
    if (form.injuries) payload.injuryHistory = [form.injuries]
    await register(payload)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally { loading.value = false }
}
</script>

<style scoped>
.register-page { min-height: 100vh; display: flex; }
.register-left { flex: 1; background: var(--bg-banner); display: flex; flex-direction: column; justify-content: center; padding: 60px; color: #fff; position: relative; overflow: hidden; }
.register-left::before { content: ''; position: absolute; top: -30%; right: -20%; width: 600px; height: 600px; background: radial-gradient(circle, rgba(59,130,246,0.12) 0%, transparent 70%); pointer-events: none; }
.register-brand { position: absolute; top: 40px; left: 60px; display: flex; align-items: center; gap: 14px; z-index: 1; }
.brand-icon { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; overflow: hidden; background: transparent; }
.brand-logo { width: 100%; height: 100%; object-fit: cover; }
.brand-title { font-size: 22px; font-weight: 800; color: #fff; }
.brand-slogan { font-size: 13px; color: #888; margin-top: 2px; }
.register-hero { position: relative; z-index: 1; max-width: 480px; }
.register-hero h1 { font-size: 48px; font-weight: 800; line-height: 1.15; margin-bottom: 20px; }
.register-hero .highlight { background: linear-gradient(135deg, #60a5fa, #a78bfa); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.register-hero p { font-size: 16px; color: #aaa; line-height: 1.6; margin-bottom: 36px; }
.hero-features { display: flex; gap: 24px; }
.feature-item { display: flex; align-items: center; gap: 8px; background: rgba(255,255,255,0.06); padding: 10px 18px; border-radius: 10px; font-size: 14px; color: #ddd; }
.register-right { width: 560px; display: flex; align-items: center; justify-content: center; background: var(--bg-page); padding: 40px; overflow-y: auto; }
.register-card { width: 100%; max-width: 520px; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 20px; padding: 32px; }
.slider-input { display: flex; align-items: center; gap: 12px; }
.slider-input .el-slider { flex: 1; }
.slider-number { width: 90px; flex-shrink: 0; }
.equipment-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.equipment-tag { border-radius: 6px; padding: 6px 12px; font-size: 13px; transition: all 0.2s; }
.equipment-tag.is-checked { background: linear-gradient(135deg, #3b82f6, #60a5fa); color: #fff; border-color: #3b82f6; box-shadow: 0 2px 8px rgba(59,130,246,0.25); }
.register-header { margin-bottom: 24px; }
.register-header h2 { font-size: 28px; font-weight: 700; color: var(--text-primary); margin: 0 0 8px; }
.register-header p { font-size: 14px; color: var(--text-secondary); margin: 0; }
.register-btn { width: 100%; border-radius: 12px; background: var(--btn-primary-gradient); border: none; font-size: 16px; font-weight: 600; height: 48px; transition: all 0.3s ease; }
.register-btn:hover { background: var(--btn-primary-gradient-hover); transform: translateY(-2px); box-shadow: 0 6px 16px rgba(59,130,246,0.3); }
.register-footer { text-align: center; margin-top: 20px; color: var(--text-secondary); font-size: 14px; }
.wheel-dialog-body { padding: 20px 0; }
@media (max-width: 900px) { .register-left { display: none; } .register-right { width: 100%; } }
</style>
