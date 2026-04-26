<template>
  <div class="login-page">
    <div class="login-left">
      <div class="login-brand">
        <div class="brand-icon">
          <img src="/logo.png" alt="FIT商城 Logo" class="brand-logo" />
        </div>
        <div class="brand-text">
          <div class="brand-title">AI-FIT智动健身</div>
          <div class="brand-slogan">智能健身运动平台</div>
        </div>
      </div>
      <div class="login-hero">
        <h1>发现你的<br><span class="highlight">专属训练方案</span></h1>
        <p>基于 AI 智能推荐，为你匹配最适合的健身计划与运动装备</p>
        <div class="hero-features">
          <div class="feature-item">
            <el-icon size="20" color="#ff6b35"><Star /></el-icon>
            <span>智能推荐</span>
          </div>
          <div class="feature-item">
            <el-icon size="20" color="#ff6b35"><FirstAidKit /></el-icon>
            <span>专业方案</span>
          </div>
          <div class="feature-item">
            <el-icon size="20" color="#ff6b35"><ChatDotRound /></el-icon>
            <span>AI 助手</span>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <el-card class="login-card" shadow="never">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>登录以继续你的健身之旅</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              立即登录
            </el-button>
          </el-form-item>

          <div class="login-footer">
            <span>还没有账号？</span>
            <el-link type="primary" @click="router.push('/register')">立即注册</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Basketball, Star, FirstAidKit, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    const { token, userId } = res.data
    userStore.setToken(token)
    userStore.setUser({ id: userId, username: form.username, role: 'USER', profileCompleted: false, createdAt: '', updatedAt: '' })
    // 标记 token 已验证（因为刚登录成功）
    userStore.isTokenValidated = true
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
}

.login-left {
  flex: 1;
  background: var(--bg-banner);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -30%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255,107,53,0.12) 0%, transparent 70%);
  pointer-events: none;
}

.login-brand {
  position: absolute;
  top: 40px;
  left: 60px;
  display: flex;
  align-items: center;
  gap: 14px;
  z-index: 1;
}

.brand-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: transparent;
}

.brand-logo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.brand-title {
  font-size: 22px;
  font-weight: 800;
  color: #fff;
}

.brand-slogan {
  font-size: 13px;
  color: #888;
  margin-top: 2px;
}

.login-hero {
  position: relative;
  z-index: 1;
  max-width: 480px;
}

.login-hero h1 {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.15;
  margin-bottom: 20px;
}

.login-hero .highlight {
  background: var(--btn-primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.login-hero p {
  font-size: 16px;
  color: #aaa;
  line-height: 1.6;
  margin-bottom: 36px;
}

.hero-features {
  display: flex;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.06);
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 14px;
  color: #ddd;
}

.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 40px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border: none;
}

.login-header {
  margin-bottom: 32px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.login-header p {
  font-size: 14px;
  color: #888;
  margin: 0;
}

.login-btn {
  width: 100%;
  border-radius: var(--radius-lg);
  background: var(--btn-primary-gradient);
  border: none;
  font-size: 16px;
  font-weight: 600;
  height: 48px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: var(--btn-primary-gradient-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.3);
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

@media (max-width: 900px) {
  .login-left {
    display: none;
  }
  .login-right {
    width: 100%;
  }
}
</style>
