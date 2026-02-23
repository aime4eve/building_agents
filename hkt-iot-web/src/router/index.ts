import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// 布局组件
const Layout = () => import('@/components/common/Layout.vue')

// 页面组件
const Login = () => import('@/views/Login.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const DeviceList = () => import('@/views/device/DeviceList.vue')
const DeviceDetail = () => import('@/views/device/DeviceDetail.vue')
const SpaceManagement = () => import('@/views/space/SpaceManagement.vue')
const SpaceDetail = () => import('@/views/space/detail.vue')
const SpaceForm = () => import('@/views/space/form.vue')
const RuleManagement = () => import('@/views/rule/RuleManagement.vue')
const ScenarioManagement = () => import('@/views/scenario/ScenarioManagement.vue')
const ScheduleManagement = () => import('@/views/schedule/ScheduleManagement.vue')
const OtaManagement = () => import('@/views/ota/OtaManagement.vue')
const TenantList = () => import('@/views/tenant/TenantList.vue')
const UserList = () => import('@/views/user/UserList.vue')
const MoldControl = () => import('@/views/smart-apps/MoldControl.vue')
const SmartLivestock = () => import('@/views/smart-apps/SmartLivestock.vue')
const NotificationManagement = () => import('@/views/notification/NotificationManagement.vue')
const OrderList = () => import('@/views/order/OrderList.vue')
const Payment = () => import('@/views/order/Payment.vue')
const NotFound = () => import('@/views/NotFound.vue')

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录',
      requiresAuth: false,
    },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: {
      requiresAuth: true,
    },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: {
          title: '首页',
          icon: 'DashboardOutlined',
        },
      },
      {
        path: '/device',
        name: 'Device',
        redirect: '/device/list',
        meta: {
          title: '设备管理',
          icon: 'AppstoreOutlined',
        },
        children: [
          {
            path: '/device/list',
            name: 'DeviceList',
            component: DeviceList,
            meta: {
              title: '设备列表',
              icon: 'UnorderedListOutlined',
            },
          },
          {
            path: '/device/ota',
            name: 'OtaManagement',
            component: OtaManagement,
            meta: {
              title: 'OTA升级',
              icon: 'CloudUploadOutlined',
            },
          },
          {
            path: '/device/:id',
            name: 'DeviceDetail',
            component: DeviceDetail,
            meta: {
              title: '设备详情',
              hidden: true,
            },
          },
        ],
      },
      {
        path: '/space',
        name: 'Space',
        redirect: '/space/management',
        meta: {
          title: '空间管理',
          icon: 'ApartmentOutlined',
        },
        children: [
          {
            path: '/space/management',
            name: 'SpaceManagement',
            component: SpaceManagement,
            meta: {
              title: '空间管理',
              icon: 'UnorderedListOutlined',
            },
          },
        ],
      },
      {
        path: '/spaces',
        name: 'SpaceManagement',
        redirect: '/space/management',
        meta: {
          title: '空间管理',
          requiresAuth: true,
          hidden: true,
        },
      },
      {
        path: '/spaces/create',
        name: 'SpaceCreate',
        component: SpaceForm,
        meta: {
          title: '创建空间',
          requiresAuth: true,
          hidden: true,
        },
      },
      {
        path: '/spaces/:id',
        name: 'SpaceDetail',
        component: SpaceDetail,
        meta: {
          title: '空间详情',
          requiresAuth: true,
          hidden: true,
        },
      },
      {
        path: '/spaces/:id/edit',
        name: 'SpaceEdit',
        component: SpaceForm,
        meta: {
          title: '编辑空间',
          requiresAuth: true,
          hidden: true,
        },
      },
      {
        path: '/rule',
        name: 'Rule',
        component: RuleManagement,
        meta: {
          title: '规则引擎',
          icon: 'SettingOutlined',
        },
      },
      {
        path: '/scenario',
        name: 'Scenario',
        redirect: '/scenario/scenes',
        meta: {
          title: '场景联动',
          icon: 'NodeIndexOutlined',
        },
        children: [
          {
            path: '/scenario/scenes',
            name: 'Scenes',
            component: ScenarioManagement,
            meta: {
              title: '场景管理',
              icon: 'UnorderedListOutlined',
            },
          },
          {
            path: '/scenario/schedules',
            name: 'Schedules',
            component: ScheduleManagement,
            meta: {
              title: '定时计划',
              icon: 'ClockCircleOutlined',
            },
          },
        ],
      },
      {
        path: '/tenant',
        name: 'Tenant',
        redirect: '/tenant/list',
        meta: {
          title: '租户管理',
          icon: 'TeamOutlined',
        },
        children: [
          {
            path: '/tenant/list',
            name: 'TenantList',
            component: TenantList,
            meta: {
              title: '租户列表',
              icon: 'UnorderedListOutlined',
            },
          },
          {
            path: '/tenant/users',
            name: 'UserList',
            component: UserList,
            meta: {
              title: '用户管理',
              icon: 'UserOutlined',
            },
          },
        ],
      },
      {
        path: '/smart-apps',
        name: 'SmartApps',
        redirect: '/smart-apps/mold-control',
        meta: {
          title: '智能应用',
          icon: 'AppstoreOutlined',
        },
        children: [
          {
            path: '/smart-apps/mold-control',
            name: 'MoldControl',
            component: MoldControl,
            meta: {
              title: '防霉管控',
              icon: 'ExperimentOutlined',
            },
          },
          {
            path: '/smart-apps/smart-livestock',
            name: 'SmartLivestock',
            component: SmartLivestock,
            meta: {
              title: '智慧畜牧',
              icon: 'BgColorsOutlined',
            },
          },
        ],
      },
      {
        path: '/notification',
        name: 'Notification',
        component: NotificationManagement,
        meta: {
          title: '通知中心',
          icon: 'BellOutlined',
        },
      },
      {
        path: '/order',
        name: 'Order',
        redirect: '/order/list',
        meta: {
          title: '订单中心',
          icon: 'ShoppingOutlined',
        },
        children: [
          {
            path: '/order/list',
            name: 'OrderList',
            component: OrderList,
            meta: {
              title: '订单列表',
              icon: 'UnorderedListOutlined',
            },
          },
          {
            path: '/order/payment',
            name: 'Payment',
            component: Payment,
            meta: {
              title: '支付页面',
              hidden: true,
            },
          },
        ],
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: {
      title: '404',
      requiresAuth: false,
    },
  },
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} - 华宽通智能体平台`
  }

  // 检查是否需要登录
  const requiresAuth = to.meta.requiresAuth !== false
  const token = localStorage.getItem('access_token')

  if (requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录用户访问登录页，跳转到首页
    next('/dashboard')
  } else {
    next()
  }
})

export default router
