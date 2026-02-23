import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Rule, RuleQuery, CreateRuleRequest, UpdateRuleRequest, RuleTestContext } from '@/api/rule'
import { ruleApi } from '@/api/rule'
import type { PageResult } from '@/types'

/**
 * 规则状态管理
 */
export const useRuleStore = defineStore('rule', () => {
  // 状态
  const ruleList = ref<Rule[]>([])
  const currentRule = ref<Rule | null>(null)
  const loading = ref(false)
  const activeRules = ref<Rule[]>([])

  // 获取规则列表
  async function fetchRules(params: RuleQuery) {
    loading.value = true
    try {
      const response = await ruleApi.getRules(params)
      ruleList.value = response.data.items
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 获取规则详情
  async function fetchRule(id: string) {
    loading.value = true
    try {
      const response = await ruleApi.getRule(id)
      currentRule.value = response.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 创建规则
  async function createRule(data: CreateRuleRequest) {
    const response = await ruleApi.createRule(data)
    return response.data
  }

  // 更新规则
  async function updateRule(id: string, data: UpdateRuleRequest) {
    await ruleApi.updateRule(id, data)
    // 更新后刷新当前规则
    if (currentRule.value?.id === id) {
      currentRule.value = { ...currentRule.value, ...data }
    }
  }

  // 删除规则
  async function deleteRule(id: string, deletedBy: string) {
    await ruleApi.deleteRule(id, deletedBy)
    // 从列表中移除
    ruleList.value = ruleList.value.filter((r) => r.id !== id)
  }

  // 启用规则
  async function enableRule(id: string, operatorId: string) {
    await ruleApi.enableRule(id, operatorId)
  }

  // 禁用规则
  async function disableRule(id: string, operatorId: string) {
    await ruleApi.disableRule(id, operatorId)
  }

  // 归档规则
  async function archiveRule(id: string, operatorId: string) {
    await ruleApi.archiveRule(id, operatorId)
  }

  // 获取激活的规则
  async function fetchActiveRules(tenantId: string) {
    const response = await ruleApi.getActiveRules(tenantId)
    activeRules.value = response.data
    return response.data
  }

  // 执行规则
  async function executeRule(id: string, context: RuleTestContext) {
    const response = await ruleApi.executeRule(id, context)
    return response.data
  }

  // 测试规则
  async function testRule(id: string, context: RuleTestContext) {
    const response = await ruleApi.testRule(id, context)
    return response.data
  }

  // 验证规则表达式
  async function validateExpression(expression: string) {
    const response = await ruleApi.validateExpression(expression)
    return response.data
  }

  // 提取表达式变量
  async function extractVariables(expression: string) {
    const response = await ruleApi.extractVariables(expression)
    return response.data
  }

  // 清空当前规则
  function clearCurrentRule() {
    currentRule.value = null
  }

  // 清空规则列表
  function clearRuleList() {
    ruleList.value = []
  }

  return {
    // 状态
    ruleList,
    currentRule,
    loading,
    activeRules,
    // 方法
    fetchRules,
    fetchRule,
    createRule,
    updateRule,
    deleteRule,
    enableRule,
    disableRule,
    archiveRule,
    fetchActiveRules,
    executeRule,
    testRule,
    validateExpression,
    extractVariables,
    clearCurrentRule,
    clearRuleList,
  }
})
