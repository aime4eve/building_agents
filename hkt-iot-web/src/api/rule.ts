import { http } from '@/utils/request'
import type { ApiResponse, PageRequest, PageResult } from '@/types'

// 规则相关类型定义
export interface Rule {
  id: string
  code: string
  name: string
  type: 'ALARM' | 'LINKAGE' | 'BILLING'
  status: 'DRAFT' | 'ACTIVE' | 'SUSPENDED' | 'ARCHIVED'
  priority: number
  tenantId: string
  triggerType: 'DEVICE_STATE_CHANGE' | 'TELEMETRY' | 'SCHEDULE' | 'MANUAL'
  triggerExpression?: string
  ruleConfig?: Record<string, any>
  deviceIds?: string[]
  effectiveFrom?: string
  effectiveTo?: string
  cronExpression?: string
  createdAt: string
  updatedAt: string
}

export interface RuleQuery extends PageRequest {
  tenantId?: string
  type?: string
  status?: string
  keyword?: string
}

export interface CreateRuleRequest {
  tenantId: string
  ruleCode: string
  ruleName: string
  ruleType: string
  ruleCategory?: string
  description?: string
  triggerType: string
  triggerExpression?: string
  ruleConfig?: Record<string, any>
  deviceIds?: string[]
  effectiveTime?: string
  expireTime?: string
  cronExpression?: string
  createdBy?: string
}

export interface UpdateRuleRequest {
  ruleName?: string
  description?: string
  triggerExpression?: string
  ruleConfig?: Record<string, any>
  deviceIds?: string[]
  updatedBy?: string
}

export interface RuleValidationResult {
  valid: boolean
  errors?: string[]
  variables?: string[]
}

export interface RuleExecutionResult {
  success: boolean
  matched: boolean
  result?: any
  error?: string
}

export interface RuleTestContext {
  deviceId?: string
  telemetry?: Record<string, any>
  deviceState?: Record<string, any>
  timestamp?: string
}

// 规则API
export const ruleApi = {
  // 获取规则列表
  getRules(params: RuleQuery): Promise<ApiResponse<PageResult<Rule>>> {
    return http.get('/v1/rules', { params })
  },

  // 获取规则详情
  getRule(id: string): Promise<ApiResponse<Rule>> {
    return http.get(`/v1/rules/${id}`)
  },

  // 创建规则
  createRule(data: CreateRuleRequest): Promise<ApiResponse<Rule>> {
    return http.post('/v1/rules', data)
  },

  // 更新规则
  updateRule(id: string, data: UpdateRuleRequest): Promise<ApiResponse<void>> {
    return http.put(`/v1/rules/${id}`, data)
  },

  // 删除规则
  deleteRule(id: string, deletedBy: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/rules/${id}`, { params: { deletedBy } })
  },

  // 启用规则
  enableRule(id: string, operatorId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/rules/${id}/enable`, null, { params: { operatorId } })
  },

  // 禁用规则
  disableRule(id: string, operatorId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/rules/${id}/disable`, null, { params: { operatorId } })
  },

  // 归档规则
  archiveRule(id: string, operatorId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/rules/${id}/archive`, null, { params: { operatorId } })
  },

  // 获取激活的规则列表
  getActiveRules(tenantId: string): Promise<ApiResponse<Rule[]>> {
    return http.get('/v1/rules/active', { params: { tenantId } })
  },

  // 执行规则
  executeRule(id: string, context: RuleTestContext): Promise<ApiResponse<RuleExecutionResult>> {
    return http.post(`/v1/rules/${id}/execute`, context)
  },

  // 测试规则
  testRule(id: string, context: RuleTestContext): Promise<ApiResponse<RuleExecutionResult>> {
    return http.post(`/v1/rules/${id}/test`, context)
  },

  // 验证规则表达式
  validateExpression(expression: string): Promise<ApiResponse<RuleValidationResult>> {
    return http.post('/v1/rules/validate', { expression })
  },

  // 提取表达式变量
  extractVariables(expression: string): Promise<ApiResponse<string[]>> {
    return http.post('/v1/rules/extract-variables', { expression })
  },
}
