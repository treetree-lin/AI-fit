import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCurrentProfile } from '@/api/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/register/index.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '首页', icon: 'HomeFilled' }
        },
        {
          path: 'workouts',
          name: 'Workouts',
          component: () => import('@/views/workout/list.vue'),
          meta: { title: '健身方案', icon: 'Basketball' }
        },
        {
          path: 'workouts/:id',
          name: 'WorkoutDetail',
          component: () => import('@/views/workout/detail.vue'),
          meta: { title: '方案详情', hidden: true }
        },
        {
          path: 'records',
          name: 'Records',
          component: () => import('@/views/record/index.vue'),
          meta: { title: '运动记录', icon: 'Calendar' }
        },
        {
          path: 'chat',
          name: 'Chat',
          component: () => import('@/views/chat/index.vue'),
          meta: { title: 'AI助手', icon: 'ChatDotRound' }
        },
        {
          path: 'knowledge',
          name: 'Knowledge',
          component: () => import('@/views/knowledge/index.vue'),
          meta: { title: '知识库', icon: 'Collection' }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/profile/index.vue'),
          meta: { title: '个人中心', icon: 'UserFilled' }
        },
        {
          path: 'plans',
          name: 'Plans',
          component: () => import('@/views/plan/index.vue'),
          meta: { title: '健身计划', icon: 'Calendar' }
        }
      ]
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  userStore.loadFromStorage()

  const hasToken = !!userStore.token

  // 如果访问的是公开页面（登录/注册）
  if (to.meta.public) {
    // 已登录且 token 已验证的用户访问登录页，重定向到首页
    if (hasToken && userStore.isTokenValidated && to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    // 访问需要认证的页面
    if (!hasToken) {
      // 没有 token，重定向到登录页
      next('/login')
    } else if (userStore.isTokenValidated) {
      // token 已经验证过，直接放行
      next()
    } else {
      // 有 token 但未验证，尝试验证 token 有效性
      try {
        // 调用后端接口验证 token，同时获取用户信息
        const res = await getCurrentProfile()
        
        // 如果成功获取到用户信息，说明 token 有效
        if (res.data?.profile) {
          const profileData = res.data.profile
          
          // 构建用户对象
          const userInfo: import('@/types').User = {
            id: profileData.userId!,
            username: profileData.username!,
            role: (res.data as any).adminInfo?.role === 'ADMIN' ? 'ADMIN' : 'USER',
            profileCompleted: true,
            avatarUrl: profileData.avatarUrl,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          }
          
          userStore.setUser(userInfo)
          userStore.setProfile(profileData)
          // 标记 token 已验证
          userStore.isTokenValidated = true
          next()
        } else {
          throw new Error('Invalid response')
        }
      } catch (error) {
        // token 无效或过期，清除并跳转到登录页
        console.error('Token validation failed:', error)
        userStore.logout()
        next('/login')
      }
    }
  }
})

export default router
