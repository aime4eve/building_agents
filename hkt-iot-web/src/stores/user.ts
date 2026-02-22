import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api/auth'
import type { LoginRequest, LoginResponse } from '@/api/auth'
import { message } from 'ant-design-vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const userInfo = ref<LoginResponse['user'] | null>(null)
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const roles = computed(() => userInfo.value?.roles || [])

  // 初始化：从localStorage恢复状态
  const initFromStorage = () => {
    const savedToken = localStorage.getItem('access_token')
    const savedUserInfo = localStorage.getItem('user_info')

    if (savedToken) {
      token.value = savedToken
    }

    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch (e) {
        console.error('Failed to parse user info:', e)
      }
    }
  }

  // 登录
  const login = async (credentials: LoginRequest) => {
    loading.value = true
    try {
      const response = await userApi.login(credentials)
      const { accessToken, user } = response.data

      token.value = accessToken
      userInfo.value = user

      // 保存到localStorage
      localStorage.setItem('access_token', accessToken)
      localStorage.setItem('user_info', JSON.stringify(user))

      message.success('登录成功')
      return true
    } catch (error) {
      console.error('Login failed:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 登出
  const logout = async () => {
    try {
      await userApi.logout()
    } catch (error) {
      console.error('Logout API failed:', error)
    } finally {
      // 清除状态
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('access_token')
      localStorage.removeItem('user_info')

      message.success('已退出登录')
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) {
      return false
    }

    try {
      const response = await userApi.getCurrentUser()
      userInfo.value = response.data
      localStorage.setItem('user_info', JSON.stringify(response.data))
      return true
    } catch (error) {
      console.error('Fetch user info failed:', error)
      // Token可能已过期，清除状态
      await logout()
      return false
    }
  }

  // 检查权限
  const hasRole = (role: string) => {
    return roles.value.includes(role)
  }

  const hasAnyRole = (roleList: string[]) => {
    return roleList.some((role) => roles.value.includes(role))
  }

  return {
    // 状态
    token,
    userInfo,
    loading,

    // 计算属性
    isLoggedIn,
    username,
    roles,

    // 方法
    initFromStorage,
    login,
    logout,
    fetchUserInfo,
    hasRole,
    hasAnyRole,
  }
})
