import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Device, DeviceQuery, OtaTask, OtaTaskCreateRequest } from '@/api/device'
import { deviceApi, otaApi } from '@/api/device'
import type { PageResult } from '@/types'

/**
 * 设备状态管理
 */
export const useDeviceStore = defineStore('device', () => {
  // 状态
  const deviceList = ref<Device[]>([])
  const currentDevice = ref<Device | null>(null)
  const loading = ref(false)
  const otaTaskList = ref<OtaTask[]>([])

  // 获取设备列表
  async function fetchDevices(params: DeviceQuery) {
    loading.value = true
    try {
      const response = await deviceApi.getDevices(params)
      deviceList.value = response.data.items
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 获取设备详情
  async function fetchDevice(deviceId: string) {
    loading.value = true
    try {
      const response = await deviceApi.getDevice(deviceId)
      currentDevice.value = response.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 创建设备
  async function createDevice(data: Partial<Device>) {
    const response = await deviceApi.createDevice(data)
    return response.data
  }

  // 更新设备
  async function updateDevice(deviceId: string, data: Partial<Device>) {
    const response = await deviceApi.updateDevice(deviceId, data)
    // 更新后刷新当前设备
    if (currentDevice.value?.deviceId === deviceId) {
      currentDevice.value = { ...currentDevice.value, ...data }
    }
    return response.data
  }

  // 删除设备
  async function deleteDevice(deviceId: string) {
    await deviceApi.deleteDevice(deviceId)
    // 从列表中移除
    deviceList.value = deviceList.value.filter((d) => d.deviceId !== deviceId)
  }

  // 激活设备
  async function activateDevice(deviceId: string) {
    await deviceApi.activateDevice(deviceId)
  }

  // 停用设备
  async function deactivateDevice(deviceId: string) {
    await deviceApi.deactivateDevice(deviceId)
  }

  // 锁定设备
  async function lockDevice(deviceId: string) {
    await deviceApi.lockDevice(deviceId)
  }

  // 解锁设备
  async function unlockDevice(deviceId: string) {
    await deviceApi.unlockDevice(deviceId)
  }

  // 批量控制设备
  async function batchControl(request: {
    deviceIds: string[]
    serviceIdentifier: string
    method: string
    params?: Record<string, any>
    timeout?: number
  }) {
    const response = await deviceApi.batchControl(request)
    return response.data
  }

  // 获取设备统计
  async function fetchDeviceStats(tenantId: string) {
    const response = await deviceApi.getDeviceStats(tenantId)
    return response.data
  }

  // OTA 任务相关
  async function fetchOtaTasks(params: any) {
    loading.value = true
    try {
      const response = await otaApi.getTasks(params)
      otaTaskList.value = response.data.items
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function createOtaTask(request: OtaTaskCreateRequest) {
    const response = await otaApi.createTask(request)
    return response.data
  }

  async function startOtaTask(taskId: string) {
    await otaApi.startTask(taskId)
  }

  async function pauseOtaTask(taskId: string) {
    await otaApi.pauseTask(taskId)
  }

  async function resumeOtaTask(taskId: string) {
    await otaApi.resumeTask(taskId)
  }

  async function cancelOtaTask(taskId: string) {
    await otaApi.cancelTask(taskId)
  }

  // 清空当前设备
  function clearCurrentDevice() {
    currentDevice.value = null
  }

  // 清空设备列表
  function clearDeviceList() {
    deviceList.value = []
  }

  return {
    // 状态
    deviceList,
    currentDevice,
    loading,
    otaTaskList,
    // 方法
    fetchDevices,
    fetchDevice,
    createDevice,
    updateDevice,
    deleteDevice,
    activateDevice,
    deactivateDevice,
    lockDevice,
    unlockDevice,
    batchControl,
    fetchDeviceStats,
    fetchOtaTasks,
    createOtaTask,
    startOtaTask,
    pauseOtaTask,
    resumeOtaTask,
    cancelOtaTask,
    clearCurrentDevice,
    clearDeviceList,
  }
})
