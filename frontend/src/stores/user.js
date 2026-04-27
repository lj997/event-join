import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token'))
  const unreadCount = ref(0)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(credentials) {
    const res = await request.post('/auth/login', credentials)
    token.value = res.data.token
    user.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data))
    await fetchUnreadCount()
    return res
  }

  async function register(userData) {
    const res = await request.post('/auth/register', userData)
    return res
  }

  async function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function fetchUserInfo() {
    try {
      const res = await request.get('/auth/me')
      user.value = res.data
      await fetchUnreadCount()
    } catch (error) {
      logout()
    }
  }

  async function fetchUnreadCount() {
    if (!isLoggedIn.value) return
    try {
      const res = await request.get('/notifications/unread/count')
      unreadCount.value = res.data.count
    } catch (error) {
      console.error('获取未读消息数量失败:', error)
    }
  }

  function restoreUser() {
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }

  restoreUser()

  return {
    user,
    token,
    unreadCount,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    fetchUserInfo,
    fetchUnreadCount
  }
})
