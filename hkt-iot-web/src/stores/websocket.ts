import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { createWebSocket, type WebSocketClient } from '@/utils/websocket'
import { useUserStore } from './user'

export const useWebSocketStore = defineStore('websocket', () => {
  const userStore = useUserStore()
  const wsClient = ref<WebSocketClient | null>(null)
  const isConnected = ref(false)
  const reconnectAttempts = ref(0)

  // 连接WebSocket
  const connect = () => {
    if (!userStore.token) {
      console.warn('Cannot connect WebSocket: no token')
      return
    }

    if (wsClient.value?.isConnected) {
      console.warn('WebSocket already connected')
      return
    }

    // 创建WebSocket客户端
    // TODO: 从配置文件读取WebSocket URL
    const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'

    wsClient.value = createWebSocket({
      url: wsUrl,
      token: userStore.token,
      reconnectInterval: 5000,
      maxReconnectAttempts: 10,
      heartbeatInterval: 30000,
    })

    // 监听连接状态
    wsClient.value.on('connected', () => {
      isConnected.value = true
      reconnectAttempts.value = 0
      console.log('WebSocket connected')
    })

    wsClient.value.on('disconnected', () => {
      isConnected.value = false
      console.log('WebSocket disconnected')
    })

    wsClient.value.on('error', (data) => {
      console.error('WebSocket error:', data)
    })

    // 连接
    wsClient.value.connect()
  }

  // 断开连接
  const disconnect = () => {
    if (wsClient.value) {
      wsClient.value.disconnect()
      wsClient.value = null
      isConnected.value = false
    }
  }

  // 发送消息
  const send = (data: any) => {
    wsClient.value?.send(data)
  }

  // 订阅事件
  const on = (event: string, handler: (data: any) => void) => {
    return wsClient.value?.on(event, handler)
  }

  // 取消订阅
  const off = (event: string, handler: (data: any) => void) => {
    wsClient.value?.off(event, handler)
  }

  // 订阅设备事件
  const onDeviceOnline = (handler: (data: any) => void) => {
    return on('device.online', handler)
  }

  const onDeviceOffline = (handler: (data: any) => void) => {
    return on('device.offline', handler)
  }

  const onDeviceTelemetry = (handler: (data: any) => void) => {
    return on('device.telemetry', handler)
  }

  // 订阅告警事件
  const onAlarmCreated = (handler: (data: any) => void) => {
    return on('alarm.created', handler)
  }

  // 订阅规则事件
  const onRuleTriggered = (handler: (data: any) => void) => {
    return on('rule.triggered', handler)
  }

  // 订阅场景事件
  const onScenarioTriggered = (handler: (data: any) => void) => {
    return on('scenario.triggered', handler)
  }

  return {
    // 状态
    isConnected,
    reconnectAttempts,

    // 方法
    connect,
    disconnect,
    send,
    on,
    off,

    // 设备事件
    onDeviceOnline,
    onDeviceOffline,
    onDeviceTelemetry,

    // 告警事件
    onAlarmCreated,

    // 规则事件
    onRuleTriggered,

    // 场景事件
    onScenarioTriggered,
  }
})
