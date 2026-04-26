<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="top-header">
      <div class="header-left">
        <div class="logo" @click="router.push('/dashboard')">
          <div class="logo-icon">
            <img src="/logo.png" alt="Logo" class="logo-img" />
          </div>
          <span class="logo-title">AI-Fit <span class="gradient-text">智动健身</span></span>
        </div>
      </div>

      <nav class="header-nav">
        <span
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path || route.path.startsWith(item.path + '/') }"
          @click="router.push(item.path)"
        >
          {{ item.title }}
        </span>
      </nav>

      <div class="header-right">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
          <el-button class="icon-btn" circle @click="showNotifications = true">
            <el-icon size="18"><Bell /></el-icon>
          </el-button>
        </el-badge>

        <el-button class="icon-btn" circle @click="toggleTheme">
          <el-icon size="18">
            <component :is="isDark ? Sunny : Moon" />
          </el-icon>
        </el-button>

        <el-dropdown @command="handleCommand" trigger="click">
          <div class="user-chip">
            <el-avatar
              :size="32"
              :src="userStore.profile?.avatarUrl"
              class="user-avatar"
            >
              <el-icon size="16"><UserFilled /></el-icon>
            </el-avatar>
            <span class="user-name">{{ userStore.user?.username || '用户' }}</span>
            <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><UserFilled /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 消息抽屉 -->
    <el-drawer v-model="showNotifications" title="消息中心" size="360px" direction="rtl">
      <div class="notification-list">
        <div
          v-for="(msg, idx) in notifications"
          :key="idx"
          class="notification-item"
          :class="{ unread: !msg.read }"
          @click="markRead(idx)"
        >
          <div class="notification-icon" :style="{ background: msg.iconBg }">
            <el-icon size="18" color="#fff"><component :is="msg.icon" /></el-icon>
          </div>
          <div class="notification-body">
            <div class="notification-title">{{ msg.title }}</div>
            <div class="notification-desc">{{ msg.desc }}</div>
            <div class="notification-time">{{ msg.time }}</div>
          </div>
          <el-badge v-if="!msg.read" is-dot type="primary" />
        </div>
      </div>
      <template #footer>
        <el-button text type="primary" @click="markAllRead">全部已读</el-button>
        <el-button text @click="clearNotifications">清空消息</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell, ArrowDown, SwitchButton, Sunny, Moon, UserFilled,
  Trophy, Warning, InfoFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const navItems = [
  { path: '/dashboard', title: '推荐首页' },
  { path: '/chat', title: 'AI 智能教练' },
  { path: '/knowledge', title: '知识库' },
  { path: '/profile', title: '个人中心' },
  { path: '/workouts', title: '训练营' },
  { path: '/records', title: '运动记录' }
]

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
  }
}

const isDark = computed(() => document.documentElement.classList.contains('dark'))
const toggleTheme = () => {
  const next = !document.documentElement.classList.contains('dark')
  if (next) {
    document.documentElement.classList.add('dark')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.classList.remove('dark')
    localStorage.setItem('theme', 'light')
  }
}

const showNotifications = ref(false)
const notifications = ref([
  { title: '恭喜完成本周目标', desc: '你已完成本周 3 次训练，继续保持！', time: '10分钟前', read: false, icon: 'Trophy', iconBg: 'linear-gradient(135deg, #faad14, #ffc53d)' },
  { title: '新方案推荐', desc: '为你推荐了「核心力量进阶」训练方案', time: '2小时前', read: false, icon: 'InfoFilled', iconBg: 'linear-gradient(135deg, #3b82f6, #60a5fa)' },
  { title: '系统维护通知', desc: '今晚 02:00-04:00 进行系统升级', time: '1天前', read: true, icon: 'Warning', iconBg: 'linear-gradient(135deg, #ff4d4f, #ff7875)' }
])
const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)
const markRead = (idx: number) => { notifications.value[idx].read = true }
const markAllRead = () => { notifications.value.forEach(n => n.read = true) }
const clearNotifications = () => { notifications.value = [] }
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: var(--bg-page);
}

/* 顶部导航栏 */
.top-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  height: 64px;
  background: var(--bg-header);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: var(--shadow-sm);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.02);
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.logo-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.gradient-text {
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 导航 */
.header-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.nav-item {
  padding: 8px 18px;
  border-radius: 10px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  font-weight: 500;
}

.nav-item:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

.nav-item.active {
  color: var(--color-primary);
  background: var(--color-primary-bg);
  font-weight: 600;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 20%;
  right: 20%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #3b82f6, transparent);
  border-radius: 2px;
}

/* 右侧 */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-badge :deep(.el-badge__content) {
  background-color: #3b82f6;
  border: none;
  font-size: 10px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
}

.icon-btn {
  border: 1px solid var(--border-color);
  background: var(--bg-page);
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.icon-btn:hover {
  background: var(--bg-hover);
  border-color: var(--color-primary-light);
  color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.15);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  border: 1px solid var(--border-color);
  background: var(--bg-page);
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-chip:hover {
  background: var(--bg-hover);
  border-color: var(--color-primary-light);
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.1);
}

.user-avatar {
  border: 2px solid #3b82f6;
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.3);
}

.user-name {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
}

.dropdown-arrow {
  color: var(--text-muted);
  font-size: 11px;
  transition: transform 0.3s;
}

.user-chip:hover .dropdown-arrow {
  transform: rotate(180deg);
  color: var(--color-primary);
}

/* 主内容 */
.main-content {
  padding: 24px 32px;
  max-width: 1440px;
  margin: 0 auto;
}

/* 消息面板 */
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: var(--bg-page);
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;
}

.notification-item:hover {
  background: var(--bg-hover);
  transform: translateX(4px);
  border-color: rgba(59, 130, 246, 0.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notification-item.unread {
  background: rgba(59, 130, 246, 0.04);
  border-color: rgba(59, 130, 246, 0.08);
}

.notification-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(0,0,0,0.15);
}

.notification-body {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.notification-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 4px;
}

.notification-time {
  font-size: 11px;
  color: var(--text-muted);
}

/* 暗色模式覆盖 */
html.dark .top-header {
  background: #0f172a;
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.3);
}

html.dark .logo-title {
  color: #fff;
}

html.dark .nav-item {
  color: #94a3b8;
}

html.dark .nav-item:hover {
  color: #e2e8f0;
  background: rgba(59, 130, 246, 0.08);
}

html.dark .nav-item.active {
  color: #60a5fa;
  background: rgba(59, 130, 246, 0.12);
}

html.dark .icon-btn {
  border: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(30, 41, 59, 0.5);
  color: #94a3b8;
}

html.dark .icon-btn:hover {
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.3);
  color: #60a5fa;
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.2);
}

html.dark .user-chip {
  border: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(30, 41, 59, 0.5);
}

html.dark .user-chip:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.15);
}

html.dark .user-name {
  color: #e2e8f0;
}

html.dark .dropdown-arrow {
  color: #64748b;
}

html.dark .user-chip:hover .dropdown-arrow {
  color: #60a5fa;
}
</style>
