# 华宽通智能体平台 - 前端管理后台

基于 Vue 3 + TypeScript + Ant Design Vue 的物联网智能管理平台前端项目。

## 项目简介

华宽通智能体平台前端管理后台，提供设备管理、空间管理、规则引擎、场景联动、智能应用等核心功能的Web界面。

## 技术栈

| 类别 | 技术选型 | 版本 |
|------|----------|------|
| 前端框架 | Vue | 3.5.25 |
| 开发语言 | TypeScript | ~5.9.3 |
| 构建工具 | Vite | 7.3.1 |
| UI组件库 | Ant Design Vue | 4.2.6 |
| 状态管理 | Pinia | 3.0.4 |
| 路由管理 | Vue Router | 4.6.4 |
| HTTP客户端 | Axios | 1.13.5 |
| 图表库 | ECharts | 5.6.0 |
| 日期处理 | Day.js | 1.11.19 |
| 图标库 | Ant Design Icons | 7.0.1 |

## 功能模块

### 核心页面
- **首页仪表板** - 数据统计、图表展示、告警列表
- **设备管理** - 设备列表、设备详情、状态监控、控制面板
- **空间管理** - 空间树形结构、平面图、设备关联
- **规则引擎** - 规则配置、表达式验证、测试调试
- **场景联动** - 场景配置、执行记录、手动触发
- **租户管理** - 租户CRUD、配额管理、状态控制
- **用户管理** - 用户CRUD、角色分配、密码重置
- **防霉管控** - 环境监控、风险评估、设备控制
- **智慧畜牧** - 牲畜健康、位置追踪、异常告警

### 核心功能
- 多租户管理界面
- RBAC权限管理
- 实时设备状态监控（WebSocket）
- 数据可视化（ECharts）
- 响应式设计（移动端适配）

## 项目结构

```
hkt-iot-web/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口模块
│   │   ├── auth.ts        # 认证API
│   │   ├── device.ts      # 设备API
│   │   ├── space.ts       # 空间API
│   │   ├── rule.ts        # 规则API
│   │   ├── scenario.ts    # 场景API
│   │   └── tenant.ts      # 租户/用户API
│   ├── assets/            # 资源文件
│   │   ├── images/        # 图片
│   │   └── styles/        # 全局样式
│   ├── components/        # 组件
│   │   ├── business/      # 业务组件
│   │   │   ├── DataTable.vue
│   │   │   ├── DeviceStatusCard.vue
│   │   │   └── TelemetryChart.vue
│   │   └── common/        # 通用组件
│   │       └── Layout.vue
│   ├── directives/        # 自定义指令
│   │   ├── index.ts       # 指令入口
│   │   └── permission.ts  # 权限指令
│   ├── router/            # 路由配置
│   │   └── index.ts
│   ├── stores/            # 状态管理
│   │   ├── app.ts         # 应用状态
│   │   ├── user.ts        # 用户状态
│   │   └── websocket.ts   # WebSocket状态
│   ├── types/             # 类型定义
│   │   └── index.ts
│   ├── utils/             # 工具函数
│   │   ├── device.ts      # 设备工具
│   │   ├── format.ts      # 格式化函数
│   │   ├── request.ts     # HTTP请求
│   │   ├── validate.ts    # 验证函数
│   │   └── websocket.ts   # WebSocket客户端
│   ├── views/             # 页面组件
│   │   ├── device/        # 设备管理
│   │   ├── rule/          # 规则引擎
│   │   ├── scenario/      # 场景联动
│   │   ├── smart-apps/    # 智能应用
│   │   ├── space/         # 空间管理
│   │   ├── tenant/        # 租户管理
│   │   └── user/          # 用户管理
│   ├── App.vue
│   └── main.ts
├── .env.development       # 开发环境配置
├── .env.production        # 生产环境配置
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## 安装步骤

### 环境要求
- Node.js >= 18.x
- npm >= 9.x 或 pnpm >= 8.x

### 安装依赖
```bash
cd hkt-iot-web
npm install
```

### 配置环境变量
编辑 `.env.development` 文件，配置后端API地址：
```bash
VITE_API_BASE_URL=/api
VITE_WS_URL=ws://localhost:8080/ws
```

### 启动开发服务器
```bash
npm run dev
```
访问 http://localhost:3000

### 构建生产版本
```bash
npm run build
```

## 开发指南

### 添加新页面
1. 在 `src/views/` 对应模块下创建页面组件
2. 在 `src/router/index.ts` 中添加路由配置
3. 在 `src/components/common/Layout.vue` 中添加菜单项

### 添加新API
1. 在 `src/api/` 下创建API模块文件
2. 定义请求/响应类型
3. 导出API函数

### 使用状态管理
```typescript
import { useUserStore } from '@/stores'

const userStore = useUserStore()
await userStore.login({ username, password })
```

### 使用WebSocket
```typescript
import { useWebSocketStore } from '@/stores'

const wsStore = useWebSocketStore()
wsStore.connect()

// 订阅设备事件
wsStore.onDeviceOnline((data) => {
  console.log('设备上线', data)
})
```

### 使用自定义指令
```vue
<!-- 权限控制 -->
<a-button v-permission="{ role: 'admin' }">管理员操作</a-button>

<!-- 防抖 -->
<a-button v-debounce:300="handleClick">点击</a-button>

<!-- 复制 -->
<span v-copy="text">复制文本</span>
```

## API对接

### API基础路径配置
- 开发环境: 通过 Vite proxy 代理到后端
- 生产环境: 需配置 Nginx 反向代理

### 代理配置 (vite.config.ts)
```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

### 请求拦截器
- 自动添加 JWT Token
- 处理请求超时
- 错误统一处理

### 响应拦截器
- 统一响应格式处理
- 自动刷新Token
- 错误提示

## WebSocket实时通信

### 连接配置
```typescript
const wsStore = useWebSocketStore()
wsStore.connect()
```

### 事件订阅
- 设备上线/离线: `device.online` / `device.offline`
- 设备状态变化: `device.status_change`
- 设备遥测数据: `device.telemetry`
- 告警创建: `alarm.created`
- 规则触发: `rule.triggered`
- 场景触发: `scenario.triggered`

### 自动重连
- 连接断开自动重连
- 最多重连10次
- 重连间隔5秒
- 心跳保持30秒

## 样式规范

### 全局样式
- 使用 Ant Design Vue 主题变量
- 遵循 BEM 命名规范
- 响应式断点: xs(<576px), sm(≥576px), md(≥768px), lg(≥992px), xl(≥1200px)

### 组件样式
- 使用 `scoped` 样式
- 避免使用行内样式
- 优先使用预定义的CSS类

## 浏览器支持

| 浏览器 | 版本 |
|--------|------|
| Chrome | >= 90 |
| Firefox | >= 88 |
| Safari | >= 14 |
| Edge | >= 90 |

## 开发团队

华宽通智能体平台 - 前端开发团队

## 许可证

Copyright © 2026 HKT IoT Team
