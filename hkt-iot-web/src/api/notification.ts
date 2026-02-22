import { http } from '@/utils/request'
import type { ApiResponse, PageRequest, PageResult } from '@/types'

// ==================== 通知相关类型 ====================

/**
 * 通知渠道类型
 */
export type NotificationChannel = 'APP_PUSH' | 'EMAIL' | 'SMS' | 'IN_APP' | 'WEBHOOK'

/**
 * 通知优先级
 */
export type NotificationPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'

/**
 * 通知状态
 */
export type NotificationStatus = 'PENDING' | 'SENDING' | 'SENT' | 'FAILED' | 'CANCELLED'

/**
 * 通知请求
 */
export interface NotificationRequest {
  requestId: string
  tenantId: string
  channel: NotificationChannel
  templateId?: string
  priority: NotificationPriority
  status: NotificationStatus
  recipients: NotificationRecipient[]
  title?: string
  content: string
  variables?: Record<string, any>
  scheduledAt?: string
  sentAt?: string
  completedAt?: string
  error?: string
  retryCount: number
  createdAt: string
  updatedAt: string
}

/**
 * 通知接收者
 */
export interface NotificationRecipient {
  type: 'USER' | 'DEVICE' | 'EMAIL' | 'PHONE' | 'WEBHOOK'
  id: string
  name?: string
}

/**
 * 发送通知请求
 */
export interface SendNotificationRequest {
  tenantId: string
  channel: NotificationChannel
  recipients: NotificationRecipient[]
  templateId?: string
  title?: string
  content: string
  priority?: NotificationPriority
  variables?: Record<string, any>
  scheduledAt?: string
  dedupeKey?: string // 幂等键，用于去重
  timeout?: number // 超时时间（秒）
}

/**
 * 批量发送通知请求
 */
export interface BatchSendNotificationRequest {
  tenantId: string
  channel: NotificationChannel
  notifications: {
    recipients: NotificationRecipient[]
    templateId?: string
    title?: string
    content: string
    variables?: Record<string, any>
  }[]
  priority?: NotificationPriority
  dedupeKey?: string
}

/**
 * 通知日志查询参数
 */
export interface NotificationLogQuery extends PageRequest {
  tenantId?: string
  channel?: NotificationChannel
  status?: NotificationStatus
  priority?: NotificationPriority
  requestId?: string
  startTime?: string
  endTime?: string
}

/**
 * 通知日志
 */
export interface NotificationLog {
  logId: string
  requestId: string
  tenantId: string
  channel: NotificationChannel
  recipient: NotificationRecipient
  status: NotificationStatus
  title?: string
  content: string
  error?: string
  sentAt?: string
  createdAt: string
}

/**
 * 通知统计
 */
export interface NotificationStatistics {
  tenantId: string
  totalSent: number
  totalFailed: number
  totalPending: number
  successRate: number
  channelStats: {
    channel: NotificationChannel
    sent: number
    failed: number
    successRate: number
  }[]
  dailyStats: {
    date: string
    sent: number
    failed: number
  }[]
}

// ==================== 通知模板相关类型 ====================

/**
 * 通知模板
 */
export interface NotificationTemplate {
  templateId: string
  tenantId: string
  templateName: string
  templateCode: string
  channel: NotificationChannel
  title: string
  content: string
  variables: TemplateVariable[]
  status: 'ACTIVE' | 'INACTIVE'
  createdAt: string
  updatedAt: string
}

/**
 * 模板变量定义
 */
export interface TemplateVariable {
  name: string
  type: 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN'
  description?: string
  required: boolean
  defaultValue?: any
}

/**
 * 模板查询参数
 */
export interface TemplateQuery extends PageRequest {
  tenantId?: string
  channel?: NotificationChannel
  status?: 'ACTIVE' | 'INACTIVE'
  keyword?: string
}

/**
 * 创建/更新模板请求
 */
export interface TemplateRequest {
  tenantId: string
  templateName: string
  templateCode: string
  channel: NotificationChannel
  title: string
  content: string
  variables?: TemplateVariable[]
}

/**
 * 模板预览请求
 */
export interface TemplatePreviewRequest {
  templateId: string
  variables: Record<string, any>
}

// ==================== 通知API ====================

/**
 * 通知API（与后端 /api/v1/notifications 对接）
 */
export const notificationApi = {
  // 发送单条通知
  sendNotification(request: SendNotificationRequest): Promise<ApiResponse<NotificationRequest>> {
    return http.post('/v1/notifications/send', request)
  },

  // 批量发送通知
  batchSendNotification(request: BatchSendNotificationRequest): Promise<ApiResponse<NotificationRequest[]>> {
    return http.post('/v1/notifications/batch-send', request)
  },

  // 获取通知请求状态
  getNotificationRequest(requestId: string): Promise<ApiResponse<NotificationRequest>> {
    return http.get(`/v1/notifications/requests/${requestId}`)
  },

  // 取消通知
  cancelNotification(requestId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/notifications/requests/${requestId}/cancel`)
  },

  // 重试发送失败的通知
  retryNotification(requestId: string): Promise<ApiResponse<NotificationRequest>> {
    return http.post(`/v1/notifications/requests/${requestId}/retry`)
  },

  // 查询通知日志
  queryNotificationLogs(query: NotificationLogQuery): Promise<ApiResponse<PageResult<NotificationLog>>> {
    return http.post('/v1/notifications/logs/query', query)
  },

  // 获取通知统计
  getNotificationStatistics(tenantId: string, params?: { startDate?: string; endDate?: string }): Promise<ApiResponse<NotificationStatistics>> {
    return http.get(`/v1/notifications/statistics`, { params: { tenantId, ...params } })
  },
}

// ==================== 通知模板API ====================

/**
 * 通知模板API（与后端 /api/v1/notifications/templates 对接）
 */
export const notificationTemplateApi = {
  // 获取模板列表
  getTemplates(params: TemplateQuery): Promise<ApiResponse<PageResult<NotificationTemplate>>> {
    return http.get('/v1/notifications/templates', { params })
  },

  // 获取模板详情
  getTemplate(templateId: string): Promise<ApiResponse<NotificationTemplate>> {
    return http.get(`/v1/notifications/templates/${templateId}`)
  },

  // 创建模板
  createTemplate(data: TemplateRequest): Promise<ApiResponse<NotificationTemplate>> {
    return http.post('/v1/notifications/templates', data)
  },

  // 更新模板
  updateTemplate(templateId: string, data: Partial<TemplateRequest>): Promise<ApiResponse<NotificationTemplate>> {
    return http.put(`/v1/notifications/templates/${templateId}`, data)
  },

  // 删除模板
  deleteTemplate(templateId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/notifications/templates/${templateId}`)
  },

  // 启用模板
  activateTemplate(templateId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/notifications/templates/${templateId}/activate`)
  },

  // 禁用模板
  deactivateTemplate(templateId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/notifications/templates/${templateId}/deactivate`)
  },

  // 预览模板
  previewTemplate(request: TemplatePreviewRequest): Promise<ApiResponse<{ title: string; content: string }>> {
    return http.post('/v1/notifications/templates/preview', request)
  },
}

// ==================== 通知工具函数 ====================

/**
 * 通知渠道映射
 */
export const NotificationChannelMap: Record<NotificationChannel, string> = {
  APP_PUSH: 'APP推送',
  EMAIL: '邮件',
  SMS: '短信',
  IN_APP: '站内信',
  WEBHOOK: 'Webhook',
}

/**
 * 通知优先级映射
 */
export const NotificationPriorityMap: Record<NotificationPriority, string> = {
  LOW: '低',
  NORMAL: '普通',
  HIGH: '高',
  URGENT: '紧急',
}

/**
 * 通知状态映射
 */
export const NotificationStatusMap: Record<NotificationStatus, string> = {
  PENDING: '待发送',
  SENDING: '发送中',
  SENT: '已发送',
  FAILED: '发送失败',
  CANCELLED: '已取消',
}

/**
 * 获取通知渠道文本
 */
export function getNotificationChannelText(channel: NotificationChannel): string {
  return NotificationChannelMap[channel] || channel
}

/**
 * 获取通知优先级文本
 */
export function getNotificationPriorityText(priority: NotificationPriority): string {
  return NotificationPriorityMap[priority] || priority
}

/**
 * 获取通知状态文本
 */
export function getNotificationStatusText(status: NotificationStatus): string {
  return NotificationStatusMap[status] || status
}

/**
 * 获取通知状态颜色
 */
export function getNotificationStatusColor(status: NotificationStatus): string {
  const colorMap: Record<NotificationStatus, string> = {
    PENDING: 'default',
    SENDING: 'processing',
    SENT: 'success',
    FAILED: 'error',
    CANCELLED: 'warning',
  }
  return colorMap[status] || 'default'
}

/**
 * 获取通知优先级颜色
 */
export function getNotificationPriorityColor(priority: NotificationPriority): string {
  const colorMap: Record<NotificationPriority, string> = {
    LOW: 'default',
    NORMAL: 'processing',
    HIGH: 'warning',
    URGENT: 'error',
  }
  return colorMap[priority] || 'default'
}

/**
 * 获取通知渠道图标
 */
export function getNotificationChannelIcon(channel: NotificationChannel): string {
  const iconMap: Record<NotificationChannel, string> = {
    APP_PUSH: 'mobile',
    EMAIL: 'mail',
    SMS: 'message',
    IN_APP: 'bell',
    WEBHOOK: 'api',
  }
  return iconMap[channel] || 'notification'
}
