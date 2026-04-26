import type { MockMethod } from 'vite-plugin-mock'

const mockUserProfile = {
  userId: 1,
  username: 'fitness_user',
  age: 25,
  gender: 'male',
  height: 175,
  weight: 70,
  bodyFat: 15,
  goal: '增肌',
  level: '中级',
  equipment: ['哑铃', '瑜伽垫', '引体向上杆'],
  injuryHistory: ['膝盖旧伤'],
  avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=fitness'
}

const mockToken = 'mock-jwt-token-for-development-only'

export default [
  {
    url: '/api/users/login',
    method: 'post',
    response: () => {
      return {
        code: 200,
        message: '登录成功',
        data: { token: mockToken, userId: 1 },
        traceId: 'mock-trace-' + Date.now()
      }
    }
  },
  {
    url: '/api/users/register',
    method: 'post',
    response: () => {
      return {
        code: 200,
        message: '注册成功',
        data: { userId: 2 },
        traceId: 'mock-trace-' + Date.now()
      }
    }
  },
  {
    url: '/api/users/register-admin',
    method: 'post',
    response: () => {
      return {
        code: 200,
        message: '注册成功',
        data: { userId: 3 },
        traceId: 'mock-trace-' + Date.now()
      }
    }
  },
  {
    url: '/api/user/profile/me',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: mockUserProfile,
        traceId: 'mock-trace-' + Date.now()
      }
    }
  },
  {
    url: '/api/admin/profile/:userId',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: mockUserProfile,
        traceId: 'mock-trace-' + Date.now()
      }
    }
  },
  {
    url: '/api/user/profile/update',
    method: 'put',
    response: () => {
      return {
        code: 200,
        message: '更新成功',
        data: 'success',
        traceId: 'mock-trace-' + Date.now()
      }
    }
  },
  {
    url: '/api/user/avatar/upload',
    method: 'post',
    response: () => {
      return {
        code: 200,
        message: '上传成功',
        data: { url: 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + Date.now() },
        traceId: 'mock-trace-' + Date.now()
      }
    }
  },
  {
    url: '/api/user/avatar/update',
    method: 'put',
    response: () => {
      return {
        code: 200,
        message: '更新成功',
        data: 'success',
        traceId: 'mock-trace-' + Date.now()
      }
    }
  }
] as MockMethod[]
