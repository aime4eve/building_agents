<template>
  <div class="scenario-management-container">
    <a-card :bordered="false">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="queryForm" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="queryForm.keyword" placeholder="场景名称/编码" allow-clear />
        </a-form-item>
        <a-form-item label="场景类型">
          <a-select v-model:value="queryForm.type" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="MANUAL">手动场景</a-select-option>
            <a-select-option value="AUTO">自动场景</a-select-option>
            <a-select-option value="SCHEDULE">定时场景</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryForm.status" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="ACTIVE">启用</a-select-option>
            <a-select-option value="SUSPENDED">暂停</a-select-option>
            <a-select-option value="DRAFT">草稿</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <SearchOutlined /> 查询
            </a-button>
            <a-button @click="handleReset">
              <ReloadOutlined /> 重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 操作栏 -->
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined /> 新增场景
          </a-button>
          <a-button type="primary" ghost @click="handleShowExecutions">
            <HistoryOutlined /> 执行记录
          </a-button>
        </a-space>
      </div>

      <!-- 场景卡片列表 -->
      <div class="scenario-cards">
        <a-row :gutter="16">
          <a-col
            v-for="scenario in dataSource"
            :key="scenario.id"
            :xs="24"
            :sm="12"
            :lg="8"
            :xl="6"
          >
            <a-card class="scenario-card" :hoverable="true">
              <template #title>
                <div class="card-title">
                  <a-badge
                    :status="scenario.status === 'ACTIVE' ? 'success' : 'default'"
                    :text="scenario.name"
                  />
                </div>
              </template>
              <template #extra>
                <a-dropdown>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="handleEdit(scenario)">
                        <EditOutlined /> 编辑
                      </a-menu-item>
                      <a-menu-item
                        v-if="scenario.status !== 'ACTIVE'"
                        @click="handleToggleStatus(scenario, 'ACTIVE')"
                      >
                        <PlayCircleOutlined /> 启用
                      </a-menu-item>
                      <a-menu-item
                        v-else
                        @click="handleToggleStatus(scenario, 'SUSPENDED')"
                      >
                        <PauseCircleOutlined /> 暂停
                      </a-menu-item>
                      <a-menu-item
                        v-if="scenario.type === 'MANUAL'"
                        @click="handleTrigger(scenario)"
                      >
                        <ThunderboltOutlined /> 立即执行
                      </a-menu-item>
                      <a-menu-item @click="handleViewExecutions(scenario)">
                        <HistoryOutlined /> 执行记录
                      </a-menu-item>
                      <a-menu-divider />
                      <a-menu-item @click="handleDelete(scenario)" danger>
                        <DeleteOutlined /> 删除
                      </a-menu-item>
                    </a-menu>
                  </template>
                  <a-button type="text" size="small">
                    <MoreOutlined />
                  </a-button>
                </a-dropdown>
              </template>

              <div class="card-content">
                <div class="card-info">
                  <a-tag :color="getTypeColor(scenario.type)">
                    {{ getTypeText(scenario.type) }}
                  </a-tag>
                  <a-tag v-if="scenario.priority >= 80" color="red">高优先级</a-tag>
                </div>

                <div class="card-conditions">
                  <div class="info-title">
                    <ConditionCountOutlined /> 触发条件
                  </div>
                  <div class="info-content">
                    {{ scenario.conditions?.length || 0 }} 个条件
                  </div>
                </div>

                <div class="card-actions">
                  <div class="info-title">
                    <ApiOutlined /> 执行动作
                  </div>
                  <div class="info-content">
                    {{ scenario.actions?.length || 0 }} 个动作
                  </div>
                </div>

                <div class="card-time" v-if="scenario.scheduleConfig">
                  <div class="info-title">
                    <ClockCircleOutlined /> 定时配置
                  </div>
                  <div class="info-content">
                    {{ getScheduleText(scenario.scheduleConfig) }}
                  </div>
                </div>
              </div>

              <template #actions>
                <a-button
                  v-if="scenario.type === 'MANUAL' && scenario.status === 'ACTIVE'"
                  type="primary"
                  size="small"
                  @click.stop="handleTrigger(scenario)"
                >
                  <ThunderboltOutlined /> 执行
                </a-button>
                <a-button
                  v-else-if="scenario.status !== 'ACTIVE'"
                  size="small"
                  @click.stop="handleEdit(scenario)"
                >
                  <EditOutlined /> 编辑
                </a-button>
                <a-button
                  v-else
                  size="small"
                  @click.stop="handleViewExecutions(scenario)"
                >
                  <HistoryOutlined /> 记录
                </a-button>
              </template>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <a-pagination
          v-model:current="pagination.current"
          v-model:pageSize="pagination.pageSize"
          :total="pagination.total"
          :show-size-changer="true"
          :show-quick-jumper="true"
          :show-total="(total: number) => `共 ${total} 条`"
          @change="handlePageChange"
        />
      </div>
    </a-card>

    <!-- 新增/编辑场景弹窗 -->
    <a-modal
      v-model:open="scenarioModalVisible"
      :title="scenarioModalTitle"
      :width="900"
      @ok="handleScenarioModalOk"
      @cancel="scenarioModalVisible = false"
    >
      <a-form
        ref="scenarioFormRef"
        :model="scenarioFormData"
        :rules="scenarioFormRules"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="场景名称" name="name">
              <a-input v-model:value="scenarioFormData.name" placeholder="请输入场景名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="场景编码" name="code">
              <a-input v-model:value="scenarioFormData.code" placeholder="请输入场景编码" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="场景类型" name="type">
              <a-select v-model:value="scenarioFormData.type" placeholder="请选择场景类型">
                <a-select-option value="MANUAL">手动场景</a-select-option>
                <a-select-option value="AUTO">自动场景</a-select-option>
                <a-select-option value="SCHEDULE">定时场景</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级" name="priority">
              <a-input-number v-model:value="scenarioFormData.priority" :min="1" :max="100" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="scenarioFormData.description" :rows="2" />
        </a-form-item>

        <!-- 触发条件 -->
        <a-divider>触发条件</a-divider>
        <div v-if="scenarioFormData.type !== 'MANUAL'">
          <div
            v-for="(condition, index) in scenarioFormData.conditions"
            :key="index"
            class="condition-item"
          >
            <a-row :gutter="8">
              <a-col :span="6">
                <a-select v-model:value="condition.type" placeholder="条件类型">
                  <a-select-option value="DEVICE_STATE">设备状态</a-select-option>
                  <a-select-option value="TELEMETRY">遥测数据</a-select-option>
                  <a-select-option value="TIME">时间条件</a-select-option>
                  <a-select-option value="EXPRESSION">表达式</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="6">
                <a-select v-model:value="condition.operator" placeholder="操作符">
                  <a-select-option value="EQ">等于</a-select-option>
                  <a-select-option value="NEQ">不等于</a-select-option>
                  <a-select-option value="GT">大于</a-select-option>
                  <a-select-option value="GTE">大于等于</a-select-option>
                  <a-select-option value="LT">小于</a-select-option>
                  <a-select-option value="LTE">小于等于</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="6">
                <a-input v-model:value="condition.value" placeholder="条件值" />
              </a-col>
              <a-col :span="4">
                <a-select v-model:value="condition.logicOperator" placeholder="逻辑关系">
                  <a-select-option value="AND">且</a-select-option>
                  <a-select-option value="OR">或</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="2">
                <a-button type="text" danger @click="removeCondition(index)">
                  <DeleteOutlined />
                </a-button>
              </a-col>
            </a-row>
          </div>
          <a-button type="dashed" block @click="addCondition">
            <PlusOutlined /> 添加条件
          </a-button>
        </div>
        <a-alert v-else message="手动场景无需设置触发条件" type="info" show-icon />

        <!-- 执行动作 -->
        <a-divider>执行动作</a-divider>
        <div>
          <div
            v-for="(action, index) in scenarioFormData.actions"
            :key="index"
            class="action-item"
          >
            <a-row :gutter="8">
              <a-col :span="6">
                <a-select v-model:value="action.type" placeholder="动作类型">
                  <a-select-option value="DEVICE_CONTROL">设备控制</a-select-option>
                  <a-select-option value="NOTIFICATION">发送通知</a-select-option>
                  <a-select-option value="DELAY">延迟</a-select-option>
                  <a-select-option value="SCENE_TRIGGER">触发场景</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="6">
                <a-input v-model:value="action.deviceId" placeholder="设备ID" />
              </a-col>
              <a-col :span="6">
                <a-input v-model:value="action.serviceId" placeholder="服务ID" />
              </a-col>
              <a-col :span="2">
                <a-button type="text" danger @click="removeAction(index)">
                  <DeleteOutlined />
                </a-button>
              </a-col>
            </a-row>
          </div>
          <a-button type="dashed" block @click="addAction">
            <PlusOutlined /> 添加动作
          </a-button>
        </div>

        <!-- 定时配置 -->
        <template v-if="scenarioFormData.type === 'SCHEDULE'">
          <a-divider>定时配置</a-divider>
          <a-form-item label="执行方式" name="scheduleType">
            <a-select v-model:value="scheduleType" placeholder="请选择执行方式">
              <a-select-option value="ONCE">单次执行</a-select-option>
              <a-select-option value="DAILY">每天执行</a-select-option>
              <a-select-option value="WEEKLY">每周执行</a-select-option>
              <a-select-option value="MONTHLY">每月执行</a-select-option>
              <a-select-option value="CRON">Cron表达式</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item v-if="scheduleType === 'CRON'" label="Cron表达式">
            <a-input v-model:value="scenarioFormData.cronExpression" placeholder="请输入Cron表达式" />
          </a-form-item>
        </template>
      </a-form>
    </a-modal>

    <!-- 执行记录弹窗 -->
    <a-modal
      v-model:open="executionModalVisible"
      title="执行记录"
      :width="1000"
      :footer="null"
    >
      <a-table
        :columns="executionColumns"
        :data-source="executions"
        :loading="executionLoading"
        :pagination="{ pageSize: 10 }"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge
              :status="getExecutionStatusBadge(record.status)"
              :text="getExecutionStatusText(record.status)"
            />
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  HistoryOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  ThunderboltOutlined,
  MoreOutlined,
  ConditionCountOutlined,
  ApiOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { scenarioApi, type CreateScenarioRequest, type ScenarioQuery } from '@/api/scenario'
import type { Scenario, ScenarioExecutionRecord } from '@/api/scenario'

// 查询表单
const queryForm = reactive<ScenarioQuery>({
  keyword: '',
  type: undefined,
  status: undefined,
  page: 1,
  size: 12,
})

// 数据源
const dataSource = ref<Scenario[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 12,
  total: 0,
})

// 场景弹窗
const scenarioModalVisible = ref(false)
const scenarioModalTitle = ref('')
const scenarioEditMode = ref<'add' | 'edit'>('add')
const currentScenario = ref<Scenario | null>(null)

const scenarioFormData = reactive<CreateScenarioRequest>({
  tenantId: '',
  code: '',
  name: '',
  type: 'MANUAL',
  description: '',
  conditions: [],
  actions: [],
  scheduleConfig: undefined,
  priority: 50,
})

const scenarioFormRules = {
  name: [{ required: true, message: '请输入场景名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入场景编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择场景类型', trigger: 'change' }],
}

const scheduleType = ref('ONCE')

// 执行记录
const executionModalVisible = ref(false)
const executions = ref<ScenarioExecutionRecord[]>([])
const executionLoading = ref(false)

const executionColumns = [
  { title: '场景名称', dataIndex: 'scenarioName', key: 'scenarioName' },
  { title: '触发方式', dataIndex: 'triggerType', key: 'triggerType' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '触发时间', dataIndex: 'triggeredAt', key: 'triggeredAt' },
  { title: '完成时间', dataIndex: 'completedAt', key: 'completedAt' },
]

// 获取场景列表
const fetchScenarios = async () => {
  loading.value = true
  try {
    const response = await scenarioApi.getScenarios(queryForm)
    dataSource.value = response.data.items
    pagination.total = response.data.total
  } catch (error) {
    console.error('获取场景列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchScenarios()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.type = undefined
  queryForm.status = undefined
  handleSearch()
}

// 分页变化
const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  queryForm.page = page
  queryForm.size = pageSize
  fetchScenarios()
}

// 新增场景
const handleAdd = () => {
  scenarioEditMode.value = 'add'
  scenarioModalTitle.value = '新增场景'
  currentScenario.value = null
  Object.assign(scenarioFormData, {
    tenantId: '',
    code: '',
    name: '',
    type: 'MANUAL',
    description: '',
    conditions: [],
    actions: [],
    scheduleConfig: undefined,
    priority: 50,
  })
  scenarioModalVisible.value = true
}

// 编辑场景
const handleEdit = (record: Scenario) => {
  scenarioEditMode.value = 'edit'
  scenarioModalTitle.value = '编辑场景'
  currentScenario.value = record
  Object.assign(scenarioFormData, {
    code: record.code,
    name: record.name,
    type: record.type,
    description: record.description,
    conditions: JSON.parse(JSON.stringify(record.conditions || [])),
    actions: JSON.parse(JSON.stringify(record.actions || [])),
    scheduleConfig: record.scheduleConfig,
    priority: record.priority,
  })
  scenarioModalVisible.value = true
}

// 删除场景
const handleDelete = (record: Scenario) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该场景吗？此操作不可恢复。',
    onOk: async () => {
      try {
        await scenarioApi.deleteScenario(record.id)
        message.success('删除成功')
        fetchScenarios()
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 切换状态
const handleToggleStatus = (record: Scenario, status: string) => {
  Modal.confirm({
    title: '确认操作',
    content: `确定要${status === 'ACTIVE' ? '启用' : '暂停'}该场景吗？`,
    onOk: async () => {
      try {
        if (status === 'ACTIVE') {
          await scenarioApi.enableScenario(record.id)
        } else {
          await scenarioApi.disableScenario(record.id)
        }
        message.success('操作成功')
        fetchScenarios()
      } catch (error) {
        message.error('操作失败')
      }
    },
  })
}

// 触发场景
const handleTrigger = async (record: Scenario) => {
  try {
    await scenarioApi.triggerScenario(record.id)
    message.success('场景已触发')
  } catch (error) {
    message.error('触发失败')
  }
}

// 查看执行记录
const handleViewExecutions = async (record: Scenario) => {
  executionModalVisible.value = true
  executionLoading.value = true
  try {
    const response = await scenarioApi.getExecutionRecords(record.id, { page: 1, size: 50 })
    executions.value = response.data.items
  } catch (error) {
    console.error('获取执行记录失败:', error)
  } finally {
    executionLoading.value = false
  }
}

// 显示所有执行记录
const handleShowExecutions = async () => {
  executionModalVisible.value = true
  executionLoading.value = true
  try {
    const response = await scenarioApi.getRecentExecutions('', 50)
    executions.value = response.data
  } catch (error) {
    console.error('获取执行记录失败:', error)
  } finally {
    executionLoading.value = false
  }
}

// 添加条件
const addCondition = () => {
  scenarioFormData.conditions.push({
    type: 'TELEMETRY',
    operator: 'GT',
    value: '',
    logicOperator: 'AND',
  })
}

// 移除条件
const removeCondition = (index: number) => {
  scenarioFormData.conditions.splice(index, 1)
}

// 添加动作
const addAction = () => {
  scenarioFormData.actions.push({
    type: 'DEVICE_CONTROL',
    deviceId: '',
    serviceId: '',
    params: {},
  })
}

// 移除动作
const removeAction = (index: number) => {
  scenarioFormData.actions.splice(index, 1)
}

// 场景弹窗确认
const handleScenarioModalOk = async () => {
  try {
    if (scenarioEditMode.value === 'add') {
      await scenarioApi.createScenario(scenarioFormData)
      message.success('创建成功')
    } else {
      await scenarioApi.updateScenario(currentScenario.value!.id, scenarioFormData)
      message.success('更新成功')
    }
    scenarioModalVisible.value = false
    fetchScenarios()
  } catch (error) {
    message.error(scenarioEditMode.value === 'add' ? '创建失败' : '更新失败')
  }
}

// 获取类型颜色
const getTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    MANUAL: 'blue',
    AUTO: 'green',
    SCHEDULE: 'orange',
  }
  return colorMap[type] || 'default'
}

// 获取类型文本
const getTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    MANUAL: '手动场景',
    AUTO: '自动场景',
    SCHEDULE: '定时场景',
  }
  return textMap[type] || type
}

// 获取定时配置文本
const getScheduleText = (config: any) => {
  if (!config) return ''
  if (config.cronExpression) return `Cron: ${config.cronExpression}`
  if (config.executeAt) return `执行时间: ${config.executeAt}`
  return config.type || ''
}

// 获取执行状态徽标
const getExecutionStatusBadge = (status: string) => {
  const badgeMap: Record<string, string> = {
    SUCCESS: 'success',
    FAILED: 'error',
    RUNNING: 'processing',
    PENDING: 'default',
    PARTIAL: 'warning',
  }
  return badgeMap[status] || 'default'
}

// 获取执行状态文本
const getExecutionStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    SUCCESS: '成功',
    FAILED: '失败',
    RUNNING: '执行中',
    PENDING: '待执行',
    PARTIAL: '部分成功',
  }
  return textMap[status] || status
}

onMounted(() => {
  fetchScenarios()
})
</script>

<style scoped>
.scenario-management-container {
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
}

.table-actions {
  margin-bottom: 16px;
}

.scenario-cards {
  margin-bottom: 16px;
}

.scenario-card {
  margin-bottom: 16px;
  height: 280px;
}

.card-title {
  font-weight: 600;
}

.card-content {
  min-height: 160px;
}

.card-info {
  margin-bottom: 12px;
}

.card-conditions,
.card-actions,
.card-time {
  margin-bottom: 8px;
}

.info-title {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.info-content {
  font-size: 14px;
  color: #262626;
}

.condition-item,
.action-item {
  margin-bottom: 8px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
</style>
