import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)

  // 全局loading状态
  const globalLoading = ref(false)

  // 页面loading状态
  const pageLoading = ref(false)

  // 设备类型列表
  const deviceTypes = ref<string[]>([])

  // 侧边栏菜单列表
  const menuList = ref<any[]>([])

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 设置侧边栏状态
  const setSidebarCollapsed = (collapsed: boolean) => {
    sidebarCollapsed.value = collapsed
  }

  // 设置全局loading
  const setGlobalLoading = (loading: boolean) => {
    globalLoading.value = loading
  }

  // 设置页面loading
  const setPageLoading = (loading: boolean) => {
    pageLoading.value = loading
  }

  // 设置设备类型列表
  const setDeviceTypes = (types: string[]) => {
    deviceTypes.value = types
  }

  // 设置菜单列表
  const setMenuList = (menus: any[]) => {
    menuList.value = menus
  }

  return {
    sidebarCollapsed,
    globalLoading,
    pageLoading,
    deviceTypes,
    menuList,
    toggleSidebar,
    setSidebarCollapsed,
    setGlobalLoading,
    setPageLoading,
    setDeviceTypes,
    setMenuList,
  }
})
