import { http } from '@/utils/request'
import type { ApiResponse, PageRequest, PageResult } from '@/types'

// ==================== 设备类型定义 ====================

/**
 * 设备状态
 */
export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'FAULT' | 'LOCKED' | 'MAINTENANCE'

/**
 * 设备类型
 */
export type DeviceType =
  | 'WATER_METER'
  | 'ELECTRIC_METER'
  | 'GAS_METER'
  | 'SMOKE_DETECTOR'
  | 'TEMPERATURE_SENSOR'
  | 'HUMIDITY_SENSOR'
  | 'TRASH_FULL_DETETECTOR'
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

/**
 * 设备信息
 */
export interface Device {
  deviceId: string
  deviceSn: string
  deviceName: string
  deviceType: DeviceType
  deviceStatus: DeviceStatus
  tenantId: string
  spaceId?: string
  licenseId?: string
  thingModelId?: string
  firmwareVersion?: string
  activatedAt?: string
  lastOnlineAt?: string
  createdAt: string
  updatedAt: string
}

/**
 * 设备查询参数
 */
export interface DeviceQuery extends PageRequest {
  tenantId?: string
  spaceId?: string
  deviceType?: DeviceType
  deviceStatus?: DeviceStatus
  keyword?: string
}

// ==================== 设备控制 ====================

/**
 * 命令状态
 */
export type CommandStatus = 'PENDING' | 'SENT' | 'DELIVERED' | 'EXECUTING' | 'SUCCESS' | 'FAILED' | 'TIMEOUT' | 'CANCELLED'

/**
 * 设备命令
 */
export interface DeviceCommand {
  commandId: string
  deviceId: string
  serviceIdentifier: string
  method: string
  params: Record<string, any>
  commandStatus: CommandStatus
  result?: any
  error?: string
  createdAt: string
  sentAt?: string
  completedAt?: string
  retryCount?: number
}

/**
 * 命令请求
 */
export interface CommandRequest {
  deviceId: string
  serviceIdentifier: string
  method: string
  params?: Record<string, any>
  timeout?: number
  retry?: number
  retryInterval?: number
}

/**
 * 批量命令请求
 */
export interface BatchCommandRequest {
  deviceIds: string[]
  serviceIdentifier: string
  method: string
  params?: Record<string, any>
  timeout?: number
}

// ==================== 遥测数据 ====================

/**
 * 遥测数据点
 */
export interface TelemetryData {
  deviceId: string
  property: string
  value: any
  unit?: string
  timestamp: number
}

/**
 * 遥测查询参数
 */
export interface TelemetryQuery {
  deviceId: string
  properties?: string[]
  startTime?: number
  endTime?: number
  aggregation?: 'RAW' | 'AVG' | 'MAX' | 'MIN' | 'SUM' | 'COUNT'
  interval?: string // 1m, 5m, 1h, 1d
}

/**
 * 历史遥测响应
 */
export interface TelemetryHistoryResponse {
  deviceId: string
  property: string
  dataType: 'STRING' | 'NUMERIC' | 'BOOLEAN'
  data: {
    timestamp: number
    value: any
  }[]
}

// ==================== OTA升级 ====================

/**
 * OTA任务状态
 */
export type OtaTaskStatus = 'PENDING' | 'DOWNLOADING' | 'DOWNLOAD_FAILED' | 'INSTALLING' | 'SUCCESS' | 'FAILED' | 'ROLLBACK' | 'CANCELLED'

/**
 * OTA任务
 */
export interface OtaTask {
  taskId: string
  taskName: string
  deviceIds: string[]
  firmwareVersion: string
  firmwareUrl: string
  fileSize: number
  fileMd5: string
  taskStatus: OtaTaskStatus
  progress: number
  downloadedSize: number
  installedCount: number
  totalCount: number
  createdAt: string
  updatedAt: string
}

/**
 * 创建OTA任务请求
 */
export interface OtaTaskCreateRequest {
  taskName: string
  deviceIds: string[]
  firmwareVersion: string
  firmwareUrl: string
  fileSize: number
  fileMd5: string
  scheduleAt?: string
}

// ==================== 设备API ====================

/**
 * 设备管理API（与后端 /api/v1/devices 对接）
 */
export const deviceApi = {
  // 获取设备列表
  getDevices(params: DeviceQuery): Promise<ApiResponse<PageResult<Device>>> {
    return http.get('/v1/devices', { params })
  },

  // 获取设备详情
  getDevice(deviceId: string): Promise<ApiResponse<Device>> {
    return http.get(`/v1/devices/${deviceId}`)
  },

  // 创建设备
  createDevice(data: Partial<Device>): Promise<ApiResponse<Device>> {
    return http.post('/v1/devices', data)
  },

  // 更新设备
  updateDevice(deviceId: string, data: Partial<Device>): Promise<ApiResponse<Device>> {
    return http.put(`/v1/devices/${deviceId}`, data)
  },

  // 删除设备
  deleteDevice(deviceId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/devices/${deviceId}`)
  },

  // 激活设备
  activateDevice(deviceId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/devices/${deviceId}/activate`)
  },

  // 停用设备
  deactivateDevice(deviceId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/devices/${deviceId}/deactivate`)
  },

  // 锁定设备
  lockDevice(deviceId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/devices/${deviceId}/lock`)
  },

  // 解锁设备
  unlockDevice(deviceId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/devices/${deviceId}/unlock`)
  },

  // 获取租户设备列表
  getTenantDevices(tenantId: string, params: PageRequest): Promise<ApiResponse<PageResult<Device>>> {
    return http.get(`/v1/tenants/${tenantId}/devices`, { params })
  },

  // 获取空间设备列表
  getSpaceDevices(spaceId: string, params: PageRequest): Promise<ApiResponse<PageResult<Device>>> {
    return http.get(`/v1/spaces/${spaceId}/devices`, { params })
  },

  // 获取设备状态统计
  getDeviceStats(tenantId: string): Promise<ApiResponse<any>> {
    return http.get(`/v1/tenants/${tenantId}/devices/stats`)
  },

  // 获取设备类型列表
  getDeviceTypes(): Promise<ApiResponse<DeviceType[]>> {
    return http.get('/v1/devices/types')
  },

  // 批量控制设备
  batchControl(request: BatchCommandRequest): Promise<ApiResponse<DeviceCommand[]>> {
    return http.post('/v1/devices/batch-control', request)
  },
}

// ==================== 设备命令API ====================

/**
 * 设备命令API（与后端 /api/v1/commands 对接）
 */
export const commandApi = {
  // 发送命令
  sendCommand(request: CommandRequest): Promise<ApiResponse<DeviceCommand>> {
    return http.post('/v1/commands', request)
  },

  // 获取命令详情
  getCommand(commandId: string): Promise<ApiResponse<DeviceCommand>> {
    return http.get(`/v1/commands/${commandId}`)
  },

  // 取消命令
  cancelCommand(commandId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/commands/${commandId}/cancel`)
  },

  // 重试命令
  retryCommand(commandId: string): Promise<ApiResponse<DeviceCommand>> {
    return http.post(`/v1/commands/${commandId}/retry`)
  },

  // 获取设备命令历史
  getDeviceCommands(
    deviceId: string,
    params: PageRequest
  ): Promise<ApiResponse<PageResult<DeviceCommand>>> {
    return http.get(`/v1/devices/${deviceId}/commands`, { params })
  },

  // 获取命令执行结果
  getCommandResult(commandId: string): Promise<ApiResponse<any>> {
    return http.get(`/v1/commands/${commandId}/result`)
  },
}

// ==================== 遥测数据API ====================

/**
 * 遥测数据API（与后端 /api/v1/telemetry 对接）
 */
export const telemetryApi = {
  // 获取设备最新遥测数据
  getLatestTelemetry(deviceId: string, properties?: string[]): Promise<ApiResponse<Record<string, TelemetryData>>> {
    return http.get(`/v1/telemetry/${deviceId}/latest`, {
      params: { properties: properties?.join(',') }
    })
  },

  // 获取多设备最新遥测
  getMultiLatestTelemetry(deviceIds: string[], properties?: string[]): Promise<ApiResponse<Record<string, Record<string, TelemetryData>>>> {
    return http.post('/v1/telemetry/multi-latest', {
      deviceIds,
      properties,
    })
  },

  // 获取历史遥测数据
  getHistoryTelemetry(query: TelemetryQuery): Promise<ApiResponse<TelemetryHistoryResponse[]>> {
    return http.post('/v1/telemetry/history', query)
  },

  // 查询聚合数据
  queryAggregatedData(query: TelemetryQuery): Promise<ApiResponse<any[]>> {
    return http.post('/v1/telemetry/query', query)
  },

  // 订阅设备遥测（WebSocket）
  // 需要建立WebSocket连接后发送订阅消息
}

// ==================== OTA升级API ====================

/**
 * OTA升级API（与后端 /api/v1/ota 对接）
 */
export const otaApi = {
  // 创建OTA任务
  createTask(request: OtaTaskCreateRequest): Promise<ApiResponse<OtaTask>> {
    return http.post('/v1/ota/tasks', request)
  },

  // 获取OTA任务详情
  getTask(taskId: string): Promise<ApiResponse<OtaTask>> {
    return http.get(`/v1/ota/tasks/${taskId}`)
  },

  // 获取OTA任务列表
  getTasks(params: PageRequest & { taskStatus?: OtaStatus }): Promise<ApiResponse<PageResult<OtaTask>>> {
    return http.get('/v1/ota/tasks', { params })
  },

  // 获取设备的OTA任务
  getDeviceTasks(deviceId: string, params: PageRequest): Promise<ApiResponse<PageResult<OtaTask>>> {
    return http.get(`/v1/devices/${deviceId}/ota-tasks`, { params })
  },

  // 开始OTA任务
  startTask(taskId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/ota/tasks/${taskId}/start`)
  },

  // 暂停OTA任务
  pauseTask(taskId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/ota/tasks/${taskId}/pause`)
  },

  // 恢复OTA任务
  resumeTask(taskId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/ota/tasks/${taskId}/resume`)
  },

  // 取消OTA任务
  cancelTask(taskId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/ota/tasks/${taskId}/cancel`)
  },

  // 删除OTA任务
  deleteTask(taskId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/ota/tasks/${taskId}`)
  },

  // 获取OTA任务进度
  getTaskProgress(taskId: string): Promise<ApiResponse<any>> {
    return http.get(`/v1/ota/tasks/${taskId}/progress`)
  },

  // 回滚OTA任务
  rollbackTask(taskId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/ota/tasks/${taskId}/rollback`)
  },
}

// ==================== 物模型API ====================

/**
 * 物模型API（与后端 /api/v1/thing-models 对接）
 */
export const thingModelApi = {
  // 获取物模型列表
  getThingModels(params: PageRequest): Promise<ApiResponse<PageResult<any>>> {
    return http.get('/v1/thing-models', { params })
  },

  // 获取物模型详情
  getThingModel(modelId: string): Promise<ApiResponse<any>> {
    return http.get(`/v1/thing-models/${modelId}`)
  },

  // 创建物模型
  createThingModel(data: any): Promise<ApiResponse<any>> {
    return http.post('/v1/thing-models', data)
  },

  // 更新物模型
  updateThingModel(modelId: string, data: any): Promise<ApiResponse<any>> {
    return http.put(`/v1/thing-models/${modelId}`, data)
  },

  // 删除物模型
  deleteThingModel(modelId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/thing-models/${modelId}`)
  },
}

// ==================== 设备License API ====================

/**
 * 设备License API（与后端 /api/v1/device-licenses 对接）
 */
export const deviceLicenseApi = {
  // 获取License详情
  getLicense(licenseId: string): Promise<ApiResponse<any>> {
    return http.get(`/v1/device-licenses/${licenseId}`)
  },

  // 获取设备License
  getDeviceLicense(deviceId: string): Promise<ApiResponse<any>> {
    return http.get(`/v1/devices/${deviceId}/license`)
  },

  // 续费License
  renewLicense(licenseId: string): Promise<ApiResponse<any>> {
    return http.post(`/v1/device-licenses/${licenseId}/renew`)
  },

  // 检查License过期
  checkLicenseExpiry(deviceId: string): Promise<ApiResponse<boolean>> {
    return http.get(`/v1/devices/${deviceId}/license/expiry`)
  },
}

// ==================== 设备工具函数 ====================

import type { DeviceStatus, DeviceType } from '@/types'

/**
 * 设备状态映射
 */
export const DeviceStatusMap: Record<DeviceStatus, string> = {
  ONLINE: '在线',
  OFFLINE: '离线',
  FAULT: '故障',
  LOCKED: '锁定',
  MAINTENANCE: '维护中',
}

/**
 * 设备类型映射
 */
export const DeviceTypeMap: Record<DeviceType, string> = {
  WATER_METER: '水表',
  ELECTRIC_METER: '电表',
  GAS_METER: '燃气表',
  SMOKE_DETECTOR: '烟雾探测器',
  TEMPERATURE_SENSOR: '温度传感器',
  HUMIDITY_SENSOR: '湿度传感器',
  TRASH_FULL_DETETECTOR: '垃圾桶满溢检测器',
  SOLENOID_VALVE: '电磁阀',
  ANIMAL_TRACKER: '动物追踪器',
  RUMEN_CAPSULE: '瘤胃胶囊',
  LIGHT_SENSOR: '光照传感器',
  DOOR_LOCK: '智能门锁',
  GEOMAGNETIC_DETECTOR: '门磁探测器',
  PARKING_LOCK: '地锁',
  AIR_CONDITIONER: '空调',
  LIGHT: '灯光',
  GATEWAY: '网关',
  DOOR_CONTACT: '门磁',
}

/**
 * 获取设备状态文本
 */
export function getDeviceStatusText(status: DeviceStatus): string {
  return DeviceStatusMap[status] || status
}

/**
 * 获取设备类型文本
 */
export function getDeviceTypeText(type: DeviceType): string {
  return DeviceTypeMap[type] || type
}

/**
 * 获取设备状态颜色
 */
export function getDeviceStatusColor(status: DeviceStatus): string {
  const colorMap: Record<DeviceStatus, string> = {
    ONLINE: 'success',
    OFFLINE: 'default',
    FAULT: 'error',
    LOCKED: 'warning',
    MAINTENANCE: 'processing',
  }
  return colorMap[status] || 'default'
}

/**
 * 获取设备状态图标
 */
export function getDeviceStatusIcon(status: DeviceStatus): string {
  const iconMap: Record<DeviceStatus, string> = {
    ONLINE: 'check-circle',
    OFFLINE: 'stop',
    FAULT: 'exclamation-circle',
    LOCKED: 'lock',
    MAINTENANCE: 'tool',
  }
  return iconMap[status] || 'question-circle'
}

/**
 * 获取设备类型图标
 */
export function getDeviceTypeIcon(type: DeviceType): string {
  const iconMap: Partial<Record<DeviceType, string>> = {
    WATER_METER: 'droplet',
    ELECTRIC_METER: 'thunderbolt',
    GAS_METER: 'fire',
    SMOKE_DETECTOR: 'alert',
    TEMPERATURE_SENSOR: 'temperature-high',
    HUMIDITY_SENSOR: 'dashboard',
    TRASH_FULL_DETECTOR: 'delete',
    SOLENOID_VALVE: 'control',
    AIR_CONDITIONER: 'minus',
    LIGHT: 'bulb',
    DOOR_LOCK: 'lock',
    GATEWAY: 'api',
  }
  return iconMap[type] || 'customer'
}

/**
 * 是否在线
 */
export function isOnline(status: DeviceStatus): boolean {
  return status === 'ONLINE'
}

/**
 * 是否离线
 */
export function isOffline(status: DeviceStatus): boolean {
  return status === 'OFFLINE'
}

/**
 * 是否故障
 */
export function isFault(status: DeviceStatus): boolean {
  return status === 'FAULT'
}
