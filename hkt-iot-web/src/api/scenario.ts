import { http } from '@/utils/request'
import type { ApiResponse, PageRequest, PageResult } from '@/types'

// ==================== 场景管理 ====================

// 场景相关类型定义
export interface Scenario {
  sceneId: string
  sceneName: string
  sceneCode: string
  sceneType: string
  sceneStatus: 'ACTIVE' | 'INACTIVE'
  tenantId: string
  description?: string
  priority: number
  triggers: SceneTrigger[]
  actions: SceneAction[]
  createdAt: string
  updatedAt: string
}

export interface SceneTrigger {
  triggerId: string
  triggerType: 'DEVICE_STATE' | 'TELEMETRY' | 'TIME' | 'EXPRESSION' | 'MANUAL'
  deviceId?: string
  property?: string
  operator: 'EQ' | 'NEQ' | 'GT' | 'GTE' | 'LT' | 'LTE' | 'IN' | 'CONTAINS'
  value: any
  logicOperator?: 'AND' | 'OR'
}

export interface SceneAction {
  actionId: string
  actionType: 'DEVICE_CONTROL' | 'NOTIFICATION' | 'DELAY' | 'SCENE_TRIGGER' | 'SCHEDULE'
  deviceId?: string
  serviceId?: string
  serviceIdentifier?: string
  params?: Record<string, any>
  delaySeconds?: number
  targetSceneId?: string
  notificationConfig?: NotificationConfig
}

export interface NotificationConfig {
  channels: ('APP' | 'SMS' | 'EMAIL' | 'WECHAT')[]
  templateId?: string
  title?: string
  content?: string
  recipients?: string[]
}

// 向后端兼容的类型别名
export type ScenarioTrigger = SceneTrigger
export type ScenarioAction = SceneAction

// 场景查询参数
export interface ScenarioQuery extends PageRequest {
  tenantId?: string
  sceneType?: string
  sceneStatus?: string
  keyword?: string
}

// 创建场景请求
export interface SceneCreateRequest {
  tenantId: string
  sceneName: string
  sceneCode: string
  sceneType: string
  description?: string
  priority?: number
  triggers: Omit<SceneTrigger, 'triggerId'>[]
  actions: Omit<SceneAction, 'actionId'>[]
}

// 更新场景请求
export interface SceneUpdateRequest {
  sceneName?: string
  description?: string
  priority?: number
  triggers?: Omit<SceneTrigger, 'triggerId'>[]
  actions?: Omit<SceneAction, 'actionId'>[]
}

// 场景执行记录
export interface SceneExecutionRecord {
  executionId: string
  sceneId: string
  sceneName: string
  executionStatus: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'PARTIAL'
  triggerType: 'MANUAL' | 'AUTO' | 'SCHEDULE'
  triggeredBy?: string
  triggeredAt: string
  completedAt?: string
  results: ActionExecutionResult[]
  error?: string
}

export interface ActionExecutionResult {
  actionId: string
  executionStatus: 'PENDING' | 'SUCCESS' | 'FAILED'
  result?: any
  error?: string
  executedAt?: string
}

// 场景管理API（与后端 /api/v1/scenes 对接）
export const sceneApi = {
  // 获取场景列表
  getScenes(params: ScenarioQuery): Promise<ApiResponse<PageResult<Scenario>>> {
    return http.get('/v1/scenes', { params })
  },

  // 获取场景详情
  getScene(sceneId: string): Promise<ApiResponse<Scenario>> {
    return http.get(`/v1/scenes/${sceneId}`)
  },

  // 创建场景
  createScene(data: SceneCreateRequest): Promise<ApiResponse<Scenario>> {
    return http.post('/v1/scenes', data)
  },

  // 更新场景
  updateScene(sceneId: string, data: SceneUpdateRequest): Promise<ApiResponse<Scenario>> {
    return http.put(`/v1/scenes/${sceneId}`, data)
  },

  // 删除场景
  deleteScene(sceneId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/scenes/${sceneId}`)
  },

  // 激活场景
  activateScene(sceneId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/scenes/${sceneId}/activate`)
  },

  // 停用场景
  deactivateScene(sceneId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/scenes/${sceneId}/deactivate`)
  },

  // 手动触发场景
  executeScene(sceneId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/scenes/${sceneId}/execute`)
  },

  // 添加触发条件
  addTrigger(sceneId: string, trigger: Omit<SceneTrigger, 'triggerId'>): Promise<ApiResponse<SceneTrigger>> {
    return http.post(`/v1/scenes/${sceneId}/triggers`, trigger)
  },

  // 移除触发条件
  removeTrigger(sceneId: string, triggerId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/scenes/${sceneId}/triggers/${triggerId}`)
  },

  // 添加执行动作
  addAction(sceneId: string, action: Omit<SceneAction, 'actionId'>): Promise<ApiResponse<SceneAction>> {
    return http.post(`/v1/scenes/${sceneId}/actions`, action)
  },

  // 移除执行动作
  removeAction(sceneId: string, actionId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/scenes/${sceneId}/actions/${actionId}`)
  },

  // 获取场景执行记录
  getExecutionRecords(
    sceneId: string,
    params: PageRequest
  ): Promise<ApiResponse<PageResult<SceneExecutionRecord>>> {
    return http.get(`/v1/scenes/${sceneId}/executions`, { params })
  },

  // 获取最近执行记录
  getRecentExecutions(tenantId: string, limit = 10): Promise<ApiResponse<SceneExecutionRecord[]>> {
    return http.get('/v1/scenes/executions/recent', { params: { tenantId, limit } })
  },
}

// ==================== 兼容旧API ====================

// 向后兼容的别名
export const scenarioApi = sceneApi

// 向后兼容的类型
export interface Scenario {
  sceneId: string
  sceneName: string
  sceneCode: string
  sceneType: string
  sceneStatus: 'ACTIVE' | 'INACTIVE'
  tenantId: string
  description?: string
  priority: number
  triggers: SceneTrigger[]
  actions: SceneAction[]
  createdAt: string
  updatedAt: string
}

export type ScenarioCondition = SceneTrigger
export type ScenarioAction = SceneAction
