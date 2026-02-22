import { http } from '@/utils/request'
import type { ApiResponse, PageRequest, PageResult } from '@/types'

// ==================== 定时计划 ====================

/**
 * 定时计划类型
 */
export interface Schedule {
  scheduleId: string
  scheduleName: string
  scheduleCode: string
  scheduleType: 'ONCE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CRON'
  scheduleStatus: 'ACTIVE' | 'INACTIVE' | 'PAUSED'
  tenantId: string
  cronExpression?: string
  executeAt?: string
  weekDays?: number[]
  monthDays?: number[]
  description?: string
  targetSceneId?: string
  targetActionId?: string
  createdAt: string
  updatedAt: string
}

/**
 * 查询参数
 */
export interface ScheduleQuery extends PageRequest {
  tenantId?: string
  scheduleType?: string
  scheduleStatus?: string
  keyword?: string
  targetSceneId?: string
}

/**
 * 创建定时计划请求
 */
export interface ScheduleCreateRequest {
  tenantId: string
  scheduleName: string
  scheduleCode: string
  scheduleType: 'ONCE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CRON'
  cronExpression?: string
  executeAt?: string
  weekDays?: number[]
  monthDays?: number[]
  description?: string
  targetSceneId?: string
  targetActionId?: string
}

/**
 * 更新定时计划请求
 */
export interface ScheduleUpdateRequest {
  scheduleName?: string
  description?: string
  cronExpression?: string
  executeAt?: string
  weekDays?: number[]
  monthDays?: number[]
  targetSceneId?: string
  targetActionId?: string
}

/**
 * 执行记录
 */
export interface ScheduleExecutionRecord {
  executionId: string
  scheduleId: string
  scheduleName: string
  executionStatus: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'SKIPPED'
  scheduledAt: string
  executedAt?: string
  completedAt?: string
  result?: any
  error?: string
}

// 定时计划管理API（与后端 /api/v1/schedules 对接）
export const scheduleApi = {
  // 获取定时计划列表
  getSchedules(params: ScheduleQuery): Promise<ApiResponse<PageResult<Schedule>>> {
    return http.get('/v1/schedules', { params })
  },

  // 获取定时计划详情
  getSchedule(scheduleId: string): Promise<ApiResponse<Schedule>> {
    return http.get(`/v1/schedules/${scheduleId}`)
  },

  // 创建定时计划
  createSchedule(data: ScheduleCreateRequest): Promise<ApiResponse<Schedule>> {
    return http.post('/v1/schedules', data)
  },

  // 更新定时计划
  updateSchedule(scheduleId: string, data: ScheduleUpdateRequest): Promise<ApiResponse<Schedule>> {
    return http.put(`/v1/schedules/${scheduleId}`, data)
  },

  // 删除定时计划
  deleteSchedule(scheduleId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/schedules/${scheduleId}`)
  },

  // 激活定时计划
  activateSchedule(scheduleId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/schedules/${scheduleId}/activate`)
  },

  // 停用定时计划
  deactivateSchedule(scheduleId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/schedules/${scheduleId}/deactivate`)
  },

  // 暂停定时计划
  pauseSchedule(scheduleId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/schedules/${scheduleId}/pause`)
  },

  // 恢复定时计划
  resumeSchedule(scheduleId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/schedules/${scheduleId}/resume`)
  },

  // 立即执行定时计划
  executeSchedule(scheduleId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/schedules/${scheduleId}/execute`)
  },

  // 获取定时计划执行记录
  getExecutionRecords(
    scheduleId: string,
    params: PageRequest
  ): Promise<ApiResponse<PageResult<ScheduleExecutionRecord>>> {
    return http.get(`/v1/schedules/${scheduleId}/executions`, { params })
  },

  // 获取最近执行记录
  getRecentExecutions(tenantId: string, limit = 10): Promise<ApiResponse<ScheduleExecutionRecord[]>> {
    return http.get('/v1/schedules/executions/recent', { params: { tenantId, limit } })
  },

  // 验证Cron表达式
  validateCronExpression(expression: string): Promise<ApiResponse<{ valid: boolean; error?: string }>> {
    return http.post('/v1/schedules/validate-cron', { expression })
  },

  // 获取Cron表达式下次执行时间
  getNextExecutionTime(expression: string): Promise<ApiResponse<string[]>> {
    return http.post('/v1/schedules/next-execution', { expression })
  },
}

// ==================== Cron表达式工具 ====================

/**
 * Cron表达式预设
 */
export const CronPresets = {
  // 每分钟
  EVERY_MINUTE: '* * * * *',
  // 每小时
  EVERY_HOUR: '0 * * * *',
  // 每天凌晨
  EVERY_DAY_MIDNIGHT: '0 0 * * *',
  // 每天早上8点
  EVERY_DAY_8AM: '0 8 * * *',
  // 每周一早上8点
  EVERY_MONDAY_8AM: '0 8 * * 1',
  // 每月1号凌晨
  EVERY_MONTH_1ST: '0 0 1 * *',
  // 工作日早上8点
  EVERY_WEEKDAY_8AM: '0 8 * * MON-FRI',
}

/**
 * Cron表达式描述
 */
export const CronDescriptions: Record<string, string> = {
  '* * * * *': '每分钟',
  '0 * * * *': '每小时',
  '0 0 * * *': '每天凌晨',
  '0 8 * * *': '每天早上8点',
  '0 0 * * 1': '每周一凌晨',
  '0 0 1 * *': '每月1号凌晨',
  '0 8 * * MON-FRI': '工作日早上8点',
}

/**
 * 获取Cron表达式描述
 */
export function getCronDescription(expression: string): string {
  return CronDescriptions[expression] || '自定义Cron表达式'
}

/**
 * 验证Cron表达式格式
 */
export function isValidCronExpression(expression: string): boolean {
  // 简单验证：5部分或6部分，用空格分隔
  const parts = expression.trim().split(/\s+/)
  if (parts.length < 5 || parts.length > 6) return false

  // 每部分应该是有效的cron格式
  const cronPartRegex = /^(\*|(\d+|\d+-\d+)(\/\d+)?(,\d+)*|\?|[A-Z]+(-[A-Z]+)?)$/

  // 前五部分：分钟 小时 日期 月份 星期
  for (let i = 0; i < 5; i++) {
    if (!cronPartRegex.test(parts[i])) return false
  }

  // 第六部分（年份）可选
  if (parts.length === 6 && !cronPartRegex.test(parts[5])) {
    return false
  }

  return true
}
