import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserProfile } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)
  const profile = ref<UserProfile | null>(null)
  const isTokenValidated = ref<boolean>(false) // 标记 token 是否已验证

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUser = (newUser: User) => {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  const setProfile = (newProfile: UserProfile) => {
    profile.value = newProfile
  }

  const loadFromStorage = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    if (storedToken) token.value = storedToken
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (e) {
        localStorage.removeItem('user')
      }
    }
  }

  const logout = () => {
    token.value = ''
    user.value = null
    profile.value = null
    isTokenValidated.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    token,
    user,
    profile,
    isLoggedIn,
    isAdmin,
    isTokenValidated,
    setToken,
    setUser,
    setProfile,
    loadFromStorage,
    logout
  }
})
