/**
 * WebSocket 实时通信工具
 */
import { message } from 'ant-design-vue'

export type WebSocketEventType = string
export type WebSocketEventHandler = (data: any) => void

export interface WebSocketOptions {
  url: string
  token?: string
  reconnectInterval?: number
  maxReconnectAttempts?: number
  heartbeatInterval?: number
}

export class WebSocketClient {
  private ws: WebSocket | null = null
  private url: string
  private token: string | undefined
  private reconnectTimer: number | null = null
  private heartbeatTimer: number | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts: number
  private reconnectInterval: number
  private heartbeatInterval: number
  private isManualClose = false
  private eventHandlers = new Map<WebSocketEventType, Set<WebSocketEventHandler>>()

  constructor(options: WebSocketOptions) {
    this.url = options.url
    this.token = options.token
    this.reconnectInterval = options.reconnectInterval || 5000
    this.maxReconnectAttempts = options.maxReconnectAttempts || 10
    this.heartbeatInterval = options.heartbeatInterval || 30000
  }

  /**
   * 连接WebSocket
   */
  connect(): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.warn('WebSocket already connected')
      return
    }

    this.isManualClose = false

    try {
      const wsUrl = this.token
        ? `${this.url}?token=${encodeURIComponent(this.token)}`
        : this.url

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = this.handleOpen.bind(this)
      this.ws.onmessage = this.handleMessage.bind(this)
      this.ws.onerror = this.handleError.bind(this)
      this.ws.onclose = this.handleClose.bind(this)

      console.log('WebSocket connecting...', wsUrl)
    } catch (error) {
      console.error('WebSocket connection error:', error)
      this.scheduleReconnect()
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.isManualClose = true
    this.clearTimers()

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  /**
   * 发送消息
   */
  send(data: any): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      const message = typeof data === 'string' ? data : JSON.stringify(data)
      this.ws.send(message)
    } else {
      console.error('WebSocket not connected, cannot send message')
    }
  }

  /**
   * 订阅事件
   */
  on(event: WebSocketEventType, handler: WebSocketEventHandler): () => void {
    if (!this.eventHandlers.has(event)) {
      this.eventHandlers.set(event, new Set())
    }
    this.eventHandlers.get(event)!.add(handler)

    // 返回取消订阅函数
    return () => {
      this.off(event, handler)
    }
  }

  /**
   * 取消订阅事件
   */
  off(event: WebSocketEventType, handler: WebSocketEventHandler): void {
    const handlers = this.eventHandlers.get(event)
    if (handlers) {
      handlers.delete(handler)
      if (handlers.size === 0) {
        this.eventHandlers.delete(event)
      }
    }
  }

  /**
   * 触发事件
   */
  private emit(event: WebSocketEventType, data: any): void {
    const handlers = this.eventHandlers.get(event)
    if (handlers) {
      handlers.forEach((handler) => {
        try {
          handler(data)
        } catch (error) {
          console.error(`Error in event handler for ${event}:`, error)
        }
      })
    }
  }

  /**
   * 连接成功处理
   */
  private handleOpen(): void {
    console.log('WebSocket connected')
    this.reconnectAttempts = 0

    // 启动心跳
    this.startHeartbeat()

    // 触发连接成功事件
    this.emit('connected', {})
  }

  /**
   * 消息处理
   */
  private handleMessage(event: MessageEvent): void {
    try {
      const data = JSON.parse(event.data)

      // 处理心跳响应
      if (data.type === 'pong') {
        return
      }

      // 根据消息类型触发对应事件
      if (data.type) {
        this.emit(data.type, data.payload || data)
      }

      // 触发通用消息事件
      this.emit('message', data)
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error)
    }
  }

  /**
   * 错误处理
   */
  private handleError(error: Event): void {
    console.error('WebSocket error:', error)
    this.emit('error', { error })
  }

  /**
   * 连接关闭处理
   */
  private handleClose(event: CloseEvent): void {
    console.log('WebSocket disconnected:', event.code, event.reason)
    this.clearTimers()

    // 触发断开连接事件
    this.emit('disconnected', { code: event.code, reason: event.reason })

    // 非手动关闭则尝试重连
    if (!this.isManualClose) {
      this.scheduleReconnect()
    }
  }

  /**
   * 安排重连
   */
  private scheduleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnect attempts reached')
      message.error('连接已断开，请刷新页面重试')
      return
    }

    this.reconnectAttempts++

    console.log(
      `Scheduling reconnect in ${this.reconnectInterval}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`
    )

    this.reconnectTimer = window.setTimeout(() => {
      console.log('Reconnecting...')
      this.connect()
    }, this.reconnectInterval)
  }

  /**
   * 启动心跳
   */
  private startHeartbeat(): void {
    this.heartbeatTimer = window.setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.send({ type: 'ping', timestamp: Date.now() })
      }
    }, this.heartbeatInterval)
  }

  /**
   * 清除定时器
   */
  private clearTimers(): void {
    if (this.reconnectTimer !== null) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.heartbeatTimer !== null) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 获取连接状态
   */
  get readyState(): number {
    return this.ws?.readyState ?? WebSocket.CLOSED
  }

  /**
   * 是否已连接
   */
  get isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN
  }
}

/**
 * 创建WebSocket客户端实例
 */
export function createWebSocket(options: WebSocketOptions): WebSocketClient {
  return new WebSocketClient(options)
}

/**
 * 设备状态更新事件类型
 */
export const DeviceEventType = {
  ONLINE: 'device.online',
  OFFLINE: 'device.offline',
  STATUS_CHANGE: 'device.status_change',
  TELEMETRY: 'device.telemetry',
  ATTRIBUTE_CHANGE: 'device.attribute_change',
} as const

/**
 * 告警事件类型
 */
export const AlarmEventType = {
  CREATED: 'alarm.created',
  UPDATED: 'alarm.updated',
  ACKED: 'alarm.acked',
  CLEARED: 'alarm.cleared',
} as const

/**
 * 规则事件类型
 */
export const RuleEventType = {
  TRIGGERED: 'rule.triggered',
  EXECUTED: 'rule.executed',
  FAILED: 'rule.failed',
} as const

/**
 * 场景事件类型
 */
export const ScenarioEventType = {
  TRIGGERED: 'scenario.triggered',
  EXECUTED: 'scenario.executed',
  FAILED: 'scenario.failed',
} as const
