// 通用类型定义

// 分页请求参数
export interface PageRequest {
  page: number
  size: number
  sortBy?: string
  direction?: 'ASC' | 'DESC'
}

// 分页响应结果
export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

// API响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 用户相关类型
export interface User {
  id: string
  username: string
  email: string
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  roles: Role[]
  tenantId: string
  lastLoginAt?: string
  createdAt: string
}

export interface Role {
  id: string
  name: string
  code: string
  permissions: Permission[]
}

export interface Permission {
  id: string
  code: string
  name: string
  resource: string
  action: string
}

// 租户配额信息
export interface TenantQuota {
  maxUsers: number
  maxDevices: number
  maxSpaces: number
  maxRules: number
  maxStorageGB: number
}

// 租户类型
export interface Tenant {
  id: string
  tenantId: string
  name: string
  tenantName: string
  code: string
  tenantCode: string
  type: 'OPERATOR' | 'GROUP' | 'SUBSIDIARY' | 'ENTERPRISE'
  tenantType: 'OPERATOR' | 'GROUP' | 'SUBSIDIARY' | 'ENTERPRISE'
  status: 'ACTIVE' | 'SUSPENDED' | 'TERMINATED'
  tenantStatus: 'ACTIVE' | 'SUSPENDED' | 'TERMINATED'
  parentId?: string
  quota?: TenantQuota
  createdAt: string
  updatedAt: string
}

// 设备物模型信息
export interface DeviceThingModel {
  modelId: string
  modelName: string
  manufacturer: string
  model: string
  firmwareVersion: string
  properties: PropertyDefinition[]
  services: ServiceDefinition[]
  events: EventDefinition[]
}

export interface PropertyDefinition {
  identifier: string
  name: string
  dataType: 'INT' | 'FLOAT' | 'STRING' | 'BOOLEAN' | 'ENUM' | 'ARRAY'
  unit?: string
  min?: number
  max?: number
  enum?: { value: number; name: string }[]
}

export interface ServiceDefinition {
  identifier: string
  name: string
  description?: string
  inputParams?: Record<string, any>
  outputParams?: Record<string, any>
}

export interface EventDefinition {
  identifier: string
  name: string
  description?: string
  eventType: 'INFO' | 'WARNING' | 'ERROR'
  outputParams?: Record<string, any>
}

// 设备类型
export interface Device {
  id: string
  deviceId: string
  deviceCode: string
  sn: string
  deviceSn: string
  name: string
  deviceName: string
  type: DeviceType
  deviceType: DeviceType
  status: DeviceStatus
  deviceStatus: DeviceStatus
  model: DeviceThingModel
  thingModel: DeviceThingModel
  thingModelId?: string
  spaceId?: string
  tenantId: string
  licenseId?: string
  activatedAt?: string
  lastOnlineAt?: string
  createdAt: string
  updatedAt: string
}

export type DeviceType =
  | 'WATER_METER'
  | 'ELECTRIC_METER'
  | 'GAS_METER'
  | 'SMOKE_DETECTOR'
  | 'TEMPERATURE_SENSOR'
  | 'HUMIDITY_SENSOR'
  | 'TRASH_FULL_DETECTOR'
  | 'SOLENOID_VALVE'
  | 'ANIMAL_TRACKER'
  | 'RUMEN_CAPSULE'
  | 'LIGHT_SENSOR'
  | 'DOOR_LOCK'
  | 'GEOMAGNETIC_DETECTOR'
  | 'PARKING_LOCK'
  | 'AIR_CONDITIONER'
  | 'LIGHT'
  | 'GATEWAY'
  | 'DOOR_CONTACT'

export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'FAULT' | 'LOCKED' | 'MAINTENANCE'

// 设备命令相关类型
export interface CommandRequest {
  deviceId: string
  serviceIdentifier: string
  method: string
  params?: Record<string, any>
  timeout?: number
  retry?: number
  retryInterval?: number
}

export interface DeviceCommand {
  commandId: string
  deviceId: string
  serviceIdentifier: string
  method: string
  params: Record<string, any>
  status: 'PENDING' | 'SENT' | 'DELIVERED' | 'EXECUTING' | 'SUCCESS' | 'FAILED' | 'TIMEOUT' | 'CANCELLED'
  result?: any
  error?: string
  createdAt: string
  sentAt?: string
  completedAt?: string
  retryCount?: number
}

// 遥测数据相关类型
export interface TelemetryQueryRequest {
  deviceId: string
  properties?: string[]
  startTime?: number
  endTime?: number
  aggregation?: 'RAW' | 'AVG' | 'MAX' | 'MIN' | 'SUM' | 'COUNT'
  interval?: string
}

export interface MultiQueryRequest {
  deviceIds: string[]
  properties?: string[]
  startTime?: number
  endTime?: number
}

export interface TelemetryData {
  deviceId: string
  property: string
  value: any
  unit?: string
  timestamp: number
}

// OTA相关类型
export interface OtaTaskCreateRequest {
  taskName: string
  deviceIds: string[]
  firmwareVersion: string
  firmwareUrl: string
  fileSize: number
  fileMd5: string
  scheduleAt?: string
}

export interface OtaTask {
  taskId: string
  taskName: string
  deviceIds: string[]
  firmwareVersion: string
  firmwareUrl: string
  fileSize: number
  fileMd5: string
  status: 'PENDING' | 'DOWNLOADING' | 'DOWNLOAD_FAILED' | 'INSTALLING' | 'SUCCESS' | 'FAILED' | 'ROLLBACK' | 'CANCELLED'
  progress: number
  downloadedSize: number
  installedCount: number
  totalCount: number
  createdAt: string
  updatedAt: string
}

// 设备查询参数
export interface DeviceQuery extends PageRequest {
  tenantId?: string
  spaceId?: string
  type?: string
  status?: string
  keyword?: string
}

// 空间类型
export interface Space {
  id: string
  code: string
  name: string
  type: 'CAMPUS' | 'BUILDING' | 'FLOOR' | 'ROOM'
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'
  parentId?: string
  tenantId: string
  createdAt: string
  updatedAt: string
}

// 规则类型
export interface Rule {
  id: string
  code: string
  name: string
  type: 'ALARM' | 'LINKAGE' | 'BILLING'
  status: 'DRAFT' | 'ACTIVE' | 'SUSPENDED' | 'ARCHIVED'
  priority: number
  tenantId: string
  effectiveFrom: string
  effectiveTo: string
  createdAt: string
  updatedAt: string
}

// 路由菜单项
export interface MenuItem {
  key: string
  label: string
  icon?: string
  path?: string
  children?: MenuItem[]
}
