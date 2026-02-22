<template>
  <a-layout class="layout-container">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      class="layout-sider"
    >
      <div class="logo">
        <div class="logo-icon" v-if="!collapsed">HK</div>
        <span v-if="!collapsed">华宽通智能体</span>
        <span v-else>HK</span>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        theme="dark"
        :inline-collapsed="collapsed"
      >
        <template v-for="item in menuList" :key="item.key">
          <!-- 有子菜单 -->
          <a-sub-menu v-if="item.children?.length" :key="item.key">
            <template #icon>
              <component :is="item.icon" />
            </template>
            <template #title>{{ item.label }}</template>
            <a-menu-item
              v-for="child in item.children"
              :key="child.key"
              @click="handleMenuClick(child)"
            >
              {{ child.label }}
            </a-menu-item>
          </a-sub-menu>

          <!-- 无子菜单 -->
          <a-menu-item v-else :key="item.key" @click="handleMenuClick(item)">
            <template #icon>
              <component :is="item.icon" />
            </template>
            {{ item.label }}
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>

    <!-- 主体内容 -->
    <a-layout>
      <!-- 顶部导航 -->
      <a-layout-header class="layout-header">
        <div class="header-left">
          <a-button
            type="text"
            @click="toggleSidebar"
            class="trigger-btn"
          >
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </a-button>
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="item in breadcrumbList" :key="item.path">
              {{ item.label }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-right">
          <a-space>
            <!-- 通知 -->
            <a-badge :count="notificationCount" :offset="[-5, 5]">
              <a-button type="text" shape="circle">
                <BellOutlined />
              </a-button>
            </a-badge>

            <!-- 用户菜单 -->
            <a-dropdown>
              <a-space class="user-info">
                <a-avatar>
                  <UserOutlined />
                </a-avatar>
                <span class="username">{{ username }}</span>
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="profile">
                    <UserOutlined />
                    个人中心
                  </a-menu-item>
                  <a-menu-item key="settings">
                    <SettingOutlined />
                    系统设置
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout" @click="handleLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </div>
      </a-layout-header>

      <!-- 页面内容 -->
      <a-layout-content class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore, useAppStore } from '@/stores'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  BellOutlined,
  DashboardOutlined,
  AppstoreOutlined,
  ApartmentOutlined,
  SettingOutlined as SettingIconOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

// 侧边栏折叠状态
const collapsed = computed(() => appStore.sidebarCollapsed)

// 选中的菜单
const selectedKeys = ref<string[]>([])
// 展开的子菜单
const openKeys = ref<string[]>([])

// 用户名
const username = computed(() => userStore.username)

// 通知数量
const notificationCount = ref(0)

// 菜单列表
const menuList = computed(() => [
  {
    key: '/dashboard',
    label: '首页',
    icon: DashboardOutlined,
  },
  {
    key: '/device',
    label: '设备管理',
    icon: AppstoreOutlined,
    children: [
      {
        key: '/device/list',
        label: '设备列表',
        icon: UnorderedListOutlined,
      },
    ],
  },
  {
    key: '/space',
    label: '空间管理',
    icon: ApartmentOutlined,
  },
  {
    key: '/rule',
    label: '规则引擎',
    icon: SettingIconOutlined,
  },
  {
    key: '/scenario',
    label: '场景联动',
    icon: SettingIconOutlined,
  },
  {
    key: '/tenant',
    label: '租户管理',
    icon: AppstoreOutlined,
    children: [
      {
        key: '/tenant/list',
        label: '租户列表',
        icon: UnorderedListOutlined,
      },
      {
        key: '/tenant/users',
        label: '用户管理',
        icon: UnorderedListOutlined,
      },
    ],
  },
  {
    key: '/smart-apps',
    label: '智能应用',
    icon: AppstoreOutlined,
    children: [
      {
        key: '/smart-apps/mold-control',
        label: '防霉管控',
        icon: UnorderedListOutlined,
      },
      {
        key: '/smart-apps/smart-livestock',
        label: '智慧畜牧',
        icon: UnorderedListOutlined,
      },
    ],
  },
])

// 面包屑列表
const breadcrumbList = computed(() => {
  const list: Array<{ path: string; label: string }> = []
  const matched = route.matched.filter((r) => r.meta?.title)

  matched.forEach((r) => {
    list.push({
      path: r.path,
      label: r.meta.title as string,
    })
  })

  return list
})

// 切换侧边栏
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

// 菜单点击
const handleMenuClick = (item: any) => {
  router.push(item.key)
}

// 退出登录
const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

// 监听路由变化，更新选中菜单
watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]

    // 更新展开的子菜单
    const pathParts = path.split('/').filter(Boolean)
    if (pathParts.length > 1) {
      const parentPath = `/${pathParts[0]}`
      if (!openKeys.value.includes(parentPath)) {
        openKeys.value.push(parentPath)
      }
    }
  },
  { immediate: true }
)

onMounted(() => {
  selectedKeys.value = [route.path]
})
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
}

.layout-sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 10;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  margin: 16px;
  border-radius: 8px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  margin-right: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.layout-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: sticky;
  top: 0;
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
}

.trigger-btn {
  font-size: 18px;
  margin-right: 16px;
}

.breadcrumb {
  margin-left: 8px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  padding: 0 8px;
}

.user-info .username {
  margin-left: 8px;
}

.layout-content {
  margin-left: 200px;
  min-height: calc(100vh - 64px);
  background: #f0f2f5;
  transition: margin-left 0.2s;
}

.layout-content.ant-layout-has-sider {
  margin-left: 80px;
}

/* 侧边栏折叠时调整内容边距 */
.layout-sider.ant-layout-sider-collapsed + .ant-layout .layout-content {
  margin-left: 80px;
}
</style>
