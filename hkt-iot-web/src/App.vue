<template>
  <a-config-provider :locale="zhCN">
    <router-view />
  </a-config-provider>
</template>

<script setup lang="ts">
import { provide } from 'vue'
import { useUserStore } from '@/stores'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import 'dayjs/locale/zh-cn'

const userStore = useUserStore()

// 初始化用户状态
userStore.initFromStorage()

// 提供全局状态
provide('userStore', userStore)
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>
