<template>
  <a-layout class="layout-container">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      class="layout-sider"
      :theme="siderTheme"
    >
      <div class="logo" @click="router.push('/')">
        <div class="logo-icon">
          <span class="logo-text">HK</span>
        </div>
        <transition name="slide-fade" mode="out-in">
          <span v-if="!collapsed" class="logo-title">华宽通智能体</span>
        </transition>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        :theme="siderTheme"
        :inline-collapsed="collapsed"
        class="side-menu"
      >
        <template v-for="item in menuList" :key="item.key">
          <a-sub-menu v-if="item.children?.length" :key="item.key">
            <template #icon>
              <component :is="item.icon" class="menu-icon" />
            </template>
            <template #title>{{ item.label }}</template>
            <a-menu-item
              v-for="child in item.children"
              :key="child.key"
              @click="handleMenuClick(child)"
            >
              <component :is="child.icon" class="menu-icon submenu-icon" />
              {{ child.label }}
            </a-menu-item>
          </a-sub-menu>

          <a-menu-item v-else :key="item.key" @click="handleMenuClick(item)">
            <template #icon>
              <component :is="item.icon" class="menu-icon" />
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
          <a-space :size="16">
            <!-- 通知 -->
            <a-popover placement="bottomRight" trigger="click" overlay-class-name="notification-popover">
              <template #content>
                <div class="notification-panel">
                  <div class="notification-header">
                    <span>通知中心</span>
                    <a-badge :count="notificationCount" show-zero v-if="notificationCount > 0" />
                  </div>
                  <div class="notification-list">
                    <a-empty v-if="notifications.length === 0" description="暂无通知" />
                    <div
                      v-else
                      v-for="notif in notifications"
                      :key="notif.id"
                      class="notification-item"
                      :class="{ unread: !notif.read }"
                    >
                      <div class="notification-dot" :class="notif.type"></div>
                      <div class="notification-content">
                        <div class="notification-title">{{ notif.title }}</div>
                        <div class="notification-time">{{ notif.time }}</div>
                      </div>
                    </div>
                  </div>
                  <div class="notification-footer">
                    <a-button type="link" block>查看全部</a-button>
                  </div>
                </div>
              </template>
              <a-badge :count="notificationCount" :offset="[-5, 5]" class="notification-btn">
                <a-button type="text" shape="circle" class="header-icon-btn">
                  <BellOutlined />
                </a-button>
              </a-badge>
            </a-popover>

            <!-- 全屏切换 -->
            <a-button type="text" class="header-icon-btn" @click="toggleFullscreen">
              <FullScreenOutlined v-if="!isFullscreen" />
              <FullscreenExitOutlined v-else />
            </a-button>

            <!-- 主题切换 -->
            <a-button type="text" class="header-icon-btn" @click="toggleTheme">
              <MoonOutlined v-if="darkMode" />
              <SunOutlined v-else />
            </a-button>

            <!-- 用户菜单 -->
            <a-dropdown :overlay-class-name="user-dropdown">
              <a-space class="user-info">
                <a-avatar :size="36" class="user-avatar">
                  <template #icon>
                    <UserOutlined />
                  </template>
                </a-avatar>
                <span class="username" v-show="!collapsed">{{ username }}</span>
                <DownOutlined class="user-arrow" />
              </a-space>
              <template #overlay>
                <a-menu class="user-menu">
                  <a-menu-item key="profile" class="user-menu-item">
                    <UserOutlined class="menu-item-icon" />
                    <span>个人中心</span>
                  </a-menu-item>
                  <a-menu-item key="settings" class="user-menu-item">
                    <SettingOutlined class="menu-item-icon" />
                    <span>系统设置</span>
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout" class="user-menu-item logout" @click="handleLogout">
                    <LogoutOutlined class="menu-item-icon" />
                    <span>退出登录</span>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </div>
      </a-layout-header>

      <!-- 页面内容 -->
      <a-layout-content class="layout-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
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
  DownOutlined,
  FullScreenOutlined,
  FullscreenExitOutlined,
  MoonOutlined,
  SunOutlined,
  ShoppingOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

// 侧边栏折叠状态
const collapsed = computed(() => appStore.sidebarCollapsed)

// 侧边栏主题
const siderTheme = ref<'dark' | 'light'>('dark')

// 暗色模式
const darkMode = ref(false)

// 全屏状态
const isFullscreen = ref(false)

// 选中的菜单
const selectedKeys = ref<string[]>([])
// 展开的子菜单
const openKeys = ref<string[]>([])

// 用户名
const username = computed(() => userStore.username)

// 通知相关
const notificationCount = ref(0)
const notifications = ref<any[]>([])

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
  {
    key: '/notification',
    label: '通知中心',
    icon: BellOutlined,
  },
  {
    key: '/order',
    label: '订单中心',
    icon: ShoppingOutlined,
    children: [
      {
        key: '/order/list',
        label: '订单列表',
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

// 切换全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

// 切换主题
const toggleTheme = () => {
  darkMode.value = !darkMode.value
  document.documentElement.setAttribute('data-theme', darkMode.value ? 'dark' : 'light')
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

// 监听路由变化
watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]

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

// 监听全屏状态变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => {
  selectedKeys.value = [route.path]
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style scoped lang="less">
// 变量定义
@primary-color: #1890ff;
@sidebar-width: 256px;
@sidebar-collapsed-width: 80px;
@header-height: 64px;
@transition-base: all 0.2s ease;

.layout-container {
  width: 100%;
  height: 100vh;
}

// 侧边栏样式
.layout-sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 10;
  overflow: hidden;
  background: linear-gradient(180deg, #001529 0%, #002140 100%);

  &:deep(.ant-layout-sider-children) {
    display: flex;
    flex-direction: column;
  }
}

// Logo 区域
.logo {
  height: @header-height;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.05);
  margin: 0;
  cursor: pointer;
  transition: @transition-base;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }

  .logo-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    transition: @transition-base;

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 6px 16px rgba(102, 126, 234, 0.5);
    }

    .logo-text {
      color: #fff;
      font-size: 16px;
      font-weight: 700;
      letter-spacing: 0.5px;
    }
  }

  .logo-title {
    margin-left: 12px;
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;
    opacity: 1;
    transition: @transition-base;
  }
}

// 菜单样式
.side-menu {
  flex: 1;
  overflow-y: auto;
  border-right: none;
  background: transparent;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }

  :deep(.ant-menu-item) {
    margin: 4px 8px;
    border-radius: 8px;
    transition: @transition-base;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    &.ant-menu-item-selected {
      background: linear-gradient(90deg, rgba(24, 144, 255, 0.2) 0%, transparent 100%);

      .menu-icon {
        color: @primary-color;
      }
    }
  }

  :deep(.ant-menu-submenu-title) {
    margin: 4px 8px;
    border-radius: 8px;
    transition: @transition-base;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }
  }
}

.menu-icon {
  font-size: 16px;
  transition: @transition-base;
}

.submenu-icon {
  font-size: 14px;
  opacity: 0.8;
}

// 顶部导航
.layout-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
  position: sticky;
  top: 0;
  z-index: 9;
  height: @header-height;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.trigger-btn {
  font-size: 18px;
  margin-right: 12px;
  border-radius: 8px;
  transition: @transition-base;

  &:hover {
    background: #f5f5f5;
  }
}

.breadcrumb {
  :deep(.ant-breadcrumb-separator) {
    margin: 0 8px;
    color: rgba(0, 0, 0, 0.3);
  }

  :deep(.ant-breadcrumb-link) {
    color: rgba(0, 0, 0, 0.45);
    transition: @transition-base;

    &:hover {
      color: @primary-color;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.header-icon-btn {
  font-size: 18px;
  border-radius: 8px;
  transition: @transition-base;

  &:hover {
    background: #f5f5f5;
    color: @primary-color;
  }
}

// 通知面板
.notification-popover {
  :deep(.ant-popover-inner-content) {
    padding: 0;
    width: 360px;
  }
}

.notification-panel {
  .notification-header {
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: 600;
  }

  .notification-list {
    max-height: 400px;
    overflow-y: auto;
  }

  .notification-item {
    display: flex;
    align-items: flex-start;
    padding: 12px 16px;
    cursor: pointer;
    transition: @transition-base;

    &:hover {
      background: #fafafa;
    }

    &.unread {
      background: #e6f7ff;

      &:hover {
        background: #bae7ff;
      }
    }

    .notification-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      margin-top: 8px;
      margin-right: 12px;
      flex-shrink: 0;

      &.info { background: @primary-color; }
      &.success { background: #52c41a; }
      &.warning { background: #faad14; }
      &.error { background: #ff4d4f; }
    }

    .notification-content {
      flex: 1;
      min-width: 0;
    }

    .notification-title {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.85);
      margin-bottom: 4px;
    }

    .notification-time {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.45);
    }
  }

  .notification-footer {
    padding: 12px 16px;
    border-top: 1px solid #f0f0f0;
    text-align: center;
  }
}

// 用户菜单
.user-info {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: @transition-base;

  &:hover {
    background: #f5f5f5;
  }

  .user-avatar {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .username {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.85);
    font-weight: 500;
  }

  .user-arrow {
    font-size: 12px;
    opacity: 0.7;
  }
}

.user-menu {
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);

  .user-menu-item {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    transition: @transition-base;

    &:hover {
      background: #f5f5f5;
    }

    &.logout {
      color: #ff4d4f;

      &:hover {
        background: #fff1f0;
      }
    }

    .menu-item-icon {
      margin-right: 8px;
      font-size: 16px;
    }
  }
}

// 内容区域
.layout-content {
  margin-left: @sidebar-width;
  min-height: calc(100vh - @header-height);
  background: var(--hkt-bg-layout);
  transition: @transition-base;

  .content-wrapper {
    padding: 24px;
    min-height: calc(100vh - @header-height);
  }
}

// 折叠状态
:deep(.ant-layout-sider-collapsed) {
  + .ant-layout .layout-content {
    margin-left: @sidebar-collapsed-width;
  }

  .logo {
    .logo-title {
      opacity: 0;
      width: 0;
      margin-left: 0;
    }
  }

  .side-menu {
    :deep(.ant-menu-item) {
      .ant-menu-item-icon {
        margin-right: 0;
      }
    }
  }
}

// 动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

// 响应式
@media (max-width: 768px) {
  .layout-header {
    padding: 0 16px;
  }

  .breadcrumb {
    display: none;
  }

  .username {
    display: none !important;
  }

  .layout-content {
    margin-left: 0;
  }

  :deep(.ant-layout-sider-collapsed) {
    transform: translateX(-@sidebar-width);
  }
}
</style>
