import { defineStore } from 'pinia'
import { ref } from 'vue'
import type {
  Scenario,
  ScenarioQuery,
  SceneCreateRequest,
  SceneUpdateRequest,
  SceneExecutionRecord,
} from '@/api/scenario'
import { scenarioApi } from '@/api/scenario'
import type { PageResult } from '@/types'

/**
 * 场景状态管理
 */
export const useScenarioStore = defineStore('scenario', () => {
  // 状态
  const scenarioList = ref<Scenario[]>([])
  const currentScenario = ref<Scenario | null>(null)
  const loading = ref(false)
  const executionRecords = ref<SceneExecutionRecord[]>([])

  // 获取场景列表
  async function fetchScenarios(params: ScenarioQuery) {
    loading.value = true
    try {
      const response = await scenarioApi.getScenes(params)
      scenarioList.value = response.data.items
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 获取场景详情
  async function fetchScenario(sceneId: string) {
    loading.value = true
    try {
      const response = await scenarioApi.getScene(sceneId)
      currentScenario.value = response.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 创建场景
  async function createScenario(data: SceneCreateRequest) {
    const response = await scenarioApi.createScene(data)
    return response.data
  }

  // 更新场景
  async function updateScenario(sceneId: string, data: SceneUpdateRequest) {
    await scenarioApi.updateScene(sceneId, data)
    // 更新后刷新当前场景
    if (currentScenario.value?.sceneId === sceneId) {
      currentScenario.value = { ...currentScenario.value, ...data }
    }
  }

  // 删除场景
  async function deleteScenario(sceneId: string) {
    await scenarioApi.deleteScene(sceneId)
    // 从列表中移除
    scenarioList.value = scenarioList.value.filter((s) => s.sceneId !== sceneId)
  }

  // 激活场景
  async function activateScenario(sceneId: string) {
    await scenarioApi.activateScene(sceneId)
  }

  // 停用场景
  async function deactivateScenario(sceneId: string) {
    await scenarioApi.deactivateScene(sceneId)
  }

  // 执行场景
  async function executeScenario(sceneId: string) {
    await scenarioApi.executeScene(sceneId)
  }

  // 添加触发条件
  async function addTrigger(sceneId: string, trigger: any) {
    await scenarioApi.addTrigger(sceneId, trigger)
  }

  // 移除触发条件
  async function removeTrigger(sceneId: string, triggerId: string) {
    await scenarioApi.removeTrigger(sceneId, triggerId)
  }

  // 添加执行动作
  async function addAction(sceneId: string, action: any) {
    await scenarioApi.addAction(sceneId, action)
  }

  // 移除执行动作
  async function removeAction(sceneId: string, actionId: string) {
    await scenarioApi.removeAction(sceneId, actionId)
  }

  // 获取执行记录
  async function fetchExecutionRecords(sceneId: string, params: any) {
    loading.value = true
    try {
      const response = await scenarioApi.getExecutionRecords(sceneId, params)
      executionRecords.value = response.data.items
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 获取最近执行记录
  async function fetchRecentExecutions(tenantId: string, limit = 10) {
    const response = await scenarioApi.getRecentExecutions(tenantId, limit)
    executionRecords.value = response.data
    return response.data
  }

  // 清空当前场景
  function clearCurrentScenario() {
    currentScenario.value = null
  }

  // 清空场景列表
  function clearScenarioList() {
    scenarioList.value = []
  }

  return {
    // 状态
    scenarioList,
    currentScenario,
    loading,
    executionRecords,
    // 方法
    fetchScenarios,
    fetchScenario,
    createScenario,
    updateScenario,
    deleteScenario,
    activateScenario,
    deactivateScenario,
    executeScenario,
    addTrigger,
    removeTrigger,
    addAction,
    removeAction,
    fetchExecutionRecords,
    fetchRecentExecutions,
    clearCurrentScenario,
    clearScenarioList,
  }
})
