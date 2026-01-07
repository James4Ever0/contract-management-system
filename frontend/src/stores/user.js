import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || '')
  const roles = computed(() => userInfo.value.roles || [])

  async function login(credentials) {
    try {
      const res = await loginApi(credentials)
      token.value = res.data.token
      userInfo.value = res.data.user || {}
      localStorage.setItem('token', token.value)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      if (credentials.remember) {
        localStorage.setItem('rememberedUser', JSON.stringify({
          username: credentials.username,
          loginType: credentials.loginType || 'local'
        }))
      } else {
        localStorage.removeItem('rememberedUser')
      }
      return res
    } catch (error) {
      throw error
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      console.error(e)
    } finally {
      token.value = ''
      userInfo.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data || {}
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      return res
    } catch (error) {
      throw error
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    roles,
    login,
    logout,
    fetchUserInfo
  }
})
