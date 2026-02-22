/**
 * 设备相关工具函数
 */

import type { DeviceType, DeviceStatus } from '@/types'

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
  TRASH_FULL_DETECTOR: '垃圾桶满溢检测器',
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
 * 设备状态映射
 */
export const DeviceStatusMap: Record<DeviceStatus, string> = {
  ONLINE: '在线',
  OFFLINE: '离线',
  FAULT: '故障',
  MAINTENANCE: '维护中',
}

/**
 * 获取设备类型文本
 */
export function getDeviceTypeText(type: DeviceType): string {
  return DeviceTypeMap[type] || type
}

/**
 * 获取设备状态文本
 */
export function getDeviceStatusText(status: DeviceStatus): string {
  return DeviceStatusMap[status] || status
}

/**
 * 获取设备状态颜色
 */
export function getDeviceStatusColor(status: DeviceStatus): string {
  const colorMap: Record<DeviceStatus, string> = {
    ONLINE: 'success',
    OFFLINE: 'default',
    FAULT: 'error',
    MAINTENANCE: 'warning',
  }
  return colorMap[status] || 'default'
}

/**
 * 获取设备类型图标
 */
export function getDeviceTypeIcon(type: DeviceType): string {
  const iconMap: Partial<Record<DeviceType, string>> = {
    WATER_METER: '💧',
    ELECTRIC_METER: '⚡',
    GAS_METER: '🔥',
    SMOKE_DETECTOR: '🔥',
    TEMPERATURE_SENSOR: '🌡️',
    HUMIDITY_SENSOR: '💧',
    TRASH_FULL_DETECTOR: '🗑️',
    SOLENOID_VALVE: '🔧',
    ANIMAL_TRACKER: '🐄',
    RUMEN_CAPSULE: '💊',
    LIGHT_SENSOR: '☀️',
    DOOR_LOCK: '🔒',
    GEOMAGNETIC_DETECTOR: '🧲',
    PARKING_LOCK: '🅿️',
    AIR_CONDITIONER: '❄️',
    LIGHT: '💡',
    GATEWAY: '📡',
    DOOR_CONTACT: '🚪',
  }
  return iconMap[type] || '📱'
}

/**
 * 是否为网关设备
 */
export function isGateway(type: DeviceType): boolean {
  return type === 'GATEWAY'
}

/**
 * 是否为传感器设备
 */
export function isSensor(type: DeviceType): boolean {
  return type.endsWith('_SENSOR') || type === 'SMOKE_DETECTOR' || type === 'TRASH_FULL_DETECTOR'
}

/**
 * 是否为控制器设备
 */
export function isController(type: DeviceType): boolean {
  return ['SOLENOID_VALVE', 'DOOR_LOCK', 'PARKING_LOCK', 'AIR_CONDITIONER', 'LIGHT'].includes(type)
}

/**
 * 解析设备属性
 */
export interface DeviceProperty {
  name: string
  value: any
  unit?: string
  type: 'string' | 'number' | 'boolean' | 'enum'
  enumOptions?: string[]
}

/**
 * 获取设备属性定义
 */
export function getDeviceProperties(type: DeviceType): DeviceProperty[] {
  const commonProps: DeviceProperty[] = [
    { name: 'battery', value: null, unit: '%', type: 'number' },
    { name: 'rssi', value: null, unit: 'dBm', type: 'number' },
  ]

  const typeProps: Record<DeviceType, DeviceProperty[]> = {
    TEMPERATURE_SENSOR: [
      { name: 'temperature', value: null, unit: '°C', type: 'number' },
      ...commonProps,
    ],
    HUMIDITY_SENSOR: [
      { name: 'humidity', value: null, unit: '%', type: 'number' },
      ...commonProps,
    ],
    SMOKE_DETECTOR: [
      { name: 'smoke concentration', value: null, unit: 'ppm', type: 'number' },
      { name: 'alarm', value: null, type: 'boolean' },
      ...commonProps,
    ],
    WATER_METER: [
      { name: 'total flow', value: null, unit: 'm³', type: 'number' },
      { name: 'flow rate', value: null, unit: 'm³/h', type: 'number' },
      ...commonProps,
    ],
    ELECTRIC_METER: [
      { name: 'voltage', value: null, unit: 'V', type: 'number' },
      { name: 'current', value: null, unit: 'A', type: 'number' },
      { name: 'power', value: null, unit: 'kW', type: 'number' },
      { name: 'energy', value: null, unit: 'kWh', type: 'number' },
      ...commonProps,
    ],
    AIR_CONDITIONER: [
      { name: 'power', value: null, type: 'boolean' },
      { name: 'mode', value: null, type: 'enum', enumOptions: ['cool', 'heat', 'dry', 'fan', 'auto'] },
      { name: 'temperature', value: null, unit: '°C', type: 'number' },
      { name: 'fan speed', value: null, type: 'enum', enumOptions: ['low', 'medium', 'high', 'auto'] },
      ...commonProps,
    ],
    DOOR_LOCK: [
      { name: 'lock state', value: null, type: 'boolean' },
      { name: 'battery low', value: null, type: 'boolean' },
      ...commonProps,
    ],
    LIGHT: [
      { name: 'power', value: null, type: 'boolean' },
      { name: 'brightness', value: null, unit: '%', type: 'number' },
      { name: 'color temperature', value: null, unit: 'K', type: 'number' },
      ...commonProps,
    ],
  }

  return typeProps[type] || commonProps
}

/**
 * 格式化设备属性值
 */
export function formatDeviceValue(value: any, unit?: string): string {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'boolean') return value ? '是' : '否'
  if (unit) return `${value} ${unit}`
  return String(value)
}

/**
 * 获取设备遥测数据单位
 */
export function getTelemetryUnit(key: string): string {
  const unitMap: Record<string, string> = {
    temperature: '°C',
    humidity: '%',
    voltage: 'V',
    current: 'A',
    power: 'W',
    energy: 'kWh',
    flow_rate: 'm³/h',
    total_flow: 'm³',
    pressure: 'Pa',
    speed: 'rpm',
    brightness: '%',
    battery: '%',
    rssi: 'dBm',
  }
  return unitMap[key] || ''
}
