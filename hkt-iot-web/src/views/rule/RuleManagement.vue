<template>
  <div class="rule-management-container">
    <a-card :bordered="false">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="queryForm" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="queryForm.keyword" placeholder="规则名称/编码" allow-clear />
        </a-form-item>
        <a-form-item label="规则类型">
          <a-select v-model:value="queryForm.type" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="ALARM">告警规则</a-select-option>
            <a-select-option value="LINKAGE">联动规则</a-select-option>
            <a-select-option value="BILLING">计费规则</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryForm.status" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="ACTIVE">启用</a-select-option>
            <a-select-option value="SUSPENDED">暂停</a-select-option>
            <a-select-option value="DRAFT">草稿</a-select-option>
            <a-select-option value="ARCHIVED">归档</a-select-option>
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
            <PlusOutlined /> 新增规则
          </a-button>
          <a-button type="primary" ghost @click="handleBatchAction">
            <ThunderboltOutlined /> 批量执行
          </a-button>
        </a-space>
      </div>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="getTypeColor(record.type)">
              {{ getTypeText(record.type) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="getStatusBadge(record.status)"
              :text="getStatusText(record.status)"
            />
          </template>
          <template v-else-if="column.key === 'priority'">
            <a-tag :color="getPriorityColor(record.priority)">
              {{ record.priority }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleTest(record)">测试</a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item
                      v-if="record.status !== 'ACTIVE'"
                      @click="handleToggleStatus(record, 'ACTIVE')"
                    >
                      <PlayCircleOutlined /> 启用
                    </a-menu-item>
                    <a-menu-item
                      v-else
                      @click="handleToggleStatus(record, 'SUSPENDED')"
                    >
                      <PauseCircleOutlined /> 暂停
                    </a-menu-item>
                    <a-menu-item @click="handleCopy(record)">
                      <CopyOutlined /> 复制
                    </a-menu-item>
                    <a-menu-item @click="handleDelete(record)" danger>
                      <DeleteOutlined /> 删除
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">更多 <DownOutlined /></a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑规则弹窗 -->
    <a-modal
      v-model:open="ruleModalVisible"
      :title="ruleModalTitle"
      :width="900"
      @ok="handleRuleModalOk"
      @cancel="ruleModalVisible = false"
    >
      <a-form
        ref="ruleFormRef"
        :model="ruleFormData"
        :rules="ruleFormRules"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="规则名称" name="ruleName">
              <a-input v-model:value="ruleFormData.ruleName" placeholder="请输入规则名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="规则编码" name="ruleCode">
              <a-input v-model:value="ruleFormData.ruleCode" placeholder="请输入规则编码" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="规则类型" name="ruleType">
              <a-select v-model:value="ruleFormData.ruleType" placeholder="请选择规则类型">
                <a-select-option value="ALARM">告警规则</a-select-option>
                <a-select-option value="LINKAGE">联动规则</a-select-option>
                <a-select-option value="BILLING">计费规则</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级" name="priority">
              <a-input-number v-model:value="ruleFormData.priority" :min="1" :max="100" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="触发类型" name="triggerType">
              <a-select v-model:value="ruleFormData.triggerType" placeholder="请选择触发类型">
                <a-select-option value="DEVICE_STATE_CHANGE">设备状态变化</a-select-option>
                <a-select-option value="TELEMETRY">遥测数据</a-select-option>
                <a-select-option value="SCHEDULE">定时触发</a-select-option>
                <a-select-option value="MANUAL">手动触发</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12" v-if="ruleFormData.triggerType === 'SCHEDULE'">
            <a-form-item label="Cron表达式" name="cronExpression">
              <a-input v-model:value="ruleFormData.cronExpression" placeholder="请输入Cron表达式" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="触发条件" name="triggerExpression">
          <div class="expression-editor">
            <a-textarea
              v-model:value="ruleFormData.triggerExpression"
              placeholder="请输入触发表达式，例如：device.temperature > 30"
              :rows="4"
            />
            <a-button type="link" size="small" @click="handleValidateExpression">
              <CheckCircleOutlined /> 验证表达式
            </a-button>
          </div>
        </a-formitem>

        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="ruleFormData.description" :rows="2" />
        </a-form-item>

        <a-divider>关联设备</a-divider>
        <a-form-item label="选择设备">
          <a-select
            v-model:value="ruleFormData.deviceIds"
            mode="multiple"
            placeholder="请选择关联设备"
            :options="deviceOptions"
            :filter-option="filterDeviceOption"
            show-search
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 测试规则弹窗 -->
    <a-modal
      v-model:open="testModalVisible"
      title="测试规则"
      :width="700"
      @ok="handleTestExecute"
      @cancel="testModalVisible = false"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="设备ID">
          <a-input v-model:value="testContext.deviceId" placeholder="请输入设备ID" />
        </a-form-item>
        <a-form-item label="测试数据">
          <a-textarea
            v-model:value="testDataJson"
            placeholder='请输入测试数据JSON，例如：{"temperature": 35, "humidity": 60}'
            :rows="6"
          />
        </a-form-item>
        <a-form-item label="测试结果">
          <a-alert
            v-if="testResult"
            :type="testResult.success ? 'success' : 'error'"
            :message="testResult.success ? '规则匹配成功' : '规则测试失败'"
            :description="testResult.error || `匹配结果: ${JSON.stringify(testResult.result)}`"
            show-icon
          />
        </a-form-item>
      </a-form>
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
  ThunderboltOutlined,
  DownOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  CopyOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons-vue'
import { ruleApi, type CreateRuleRequest, type RuleQuery } from '@/api/rule'
import type { Rule, RuleExecutionResult, RuleTestContext } from '@/api/rule'

// 查询表单
const queryForm = reactive<RuleQuery>({
  keyword: '',
  type: undefined,
  status: undefined,
  page: 1,
  size: 10,
})

// 数据源
const dataSource = ref<Rule[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 表格列定义
const columns = [
  { title: '规则名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '规则编码', dataIndex: 'code', key: 'code', width: 150 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 100 },
  { title: '触发类型', dataIndex: 'triggerType', key: 'triggerType', width: 150 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '生效时间', dataIndex: 'effectiveFrom', key: 'effectiveFrom', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
]

// 行选择
const selectedRowKeys = ref<string[]>([])
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}

// 规则弹窗
const ruleModalVisible = ref(false)
const ruleModalTitle = ref('')
const ruleEditMode = ref<'add' | 'edit' | 'copy'>('add')
const currentRule = ref<Rule | null>(null)

const ruleFormData = reactive<CreateRuleRequest>({
  tenantId: '',
  ruleCode: '',
  ruleName: '',
  ruleType: 'ALARM',
  ruleCategory: '',
  description: '',
  triggerType: 'TELEMETRY',
  triggerExpression: '',
  ruleConfig: {},
  deviceIds: [],
  priority: 50,
  cronExpression: '',
})

const ruleFormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  triggerType: [{ required: true, message: '请选择触发类型', trigger: 'change' }],
}

// 设备选项
const deviceOptions = ref<Array<{ value: string; label: string }>>([])

// 测试相关
const testModalVisible = ref(false)
const testRuleId = ref('')
const testContext = reactive<RuleTestContext>({
  deviceId: '',
  telemetry: {},
})
const testDataJson = ref('')
const testResult = ref<RuleExecutionResult | null>(null)

// 获取规则列表
const fetchRules = async () => {
  loading.value = true
  try {
    const response = await ruleApi.getRules(queryForm)
    dataSource.value = response.data.items
    pagination.total = response.data.total
  } catch (error) {
    console.error('获取规则列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchRules()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.type = undefined
  queryForm.status = undefined
  handleSearch()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchRules()
}

// 新增规则
const handleAdd = () => {
  ruleEditMode.value = 'add'
  ruleModalTitle.value = '新增规则'
  currentRule.value = null
  Object.assign(ruleFormData, {
    tenantId: '',
    ruleCode: '',
    ruleName: '',
    ruleType: 'ALARM',
    ruleCategory: '',
    description: '',
    triggerType: 'TELEMETRY',
    triggerExpression: '',
    ruleConfig: {},
    deviceIds: [],
    priority: 50,
    cronExpression: '',
  })
  ruleModalVisible.value = true
}

// 编辑规则
const handleEdit = (record: Rule) => {
  ruleEditMode.value = 'edit'
  ruleModalTitle.value = '编辑规则'
  currentRule.value = record
  Object.assign(ruleFormData, {
    ruleName: record.name,
    ruleCode: record.code,
    ruleType: record.type,
    triggerType: record.triggerType,
    triggerExpression: record.triggerExpression,
    deviceIds: record.deviceIds,
    priority: record.priority,
    cronExpression: record.cronExpression,
  })
  ruleModalVisible.value = true
}

// 复制规则
const handleCopy = (record: Rule) => {
  ruleEditMode.value = 'copy'
  ruleModalTitle.value = '复制规则'
  currentRule.value = null
  Object.assign(ruleFormData, {
    ruleCode: `${record.code}_copy`,
    ruleName: `${record.name} - 副本`,
    ruleType: record.type,
    triggerType: record.triggerType,
    triggerExpression: record.triggerExpression,
    deviceIds: record.deviceIds,
    priority: record.priority,
    cronExpression: record.cronExpression,
  })
  ruleModalVisible.value = true
}

// 查看规则
const handleView = (record: Rule) => {
  message.info('查看详情功能开发中')
}

// 测试规则
const handleTest = (record: Rule) => {
  testRuleId.value = record.id
  testContext.deviceId = ''
  testDataJson.value = ''
  testResult.value = null
  testModalVisible.value = true
}

// 执行测试
const handleTestExecute = async () => {
  try {
    testContext.telemetry = JSON.parse(testDataJson.value || '{}')
    const response = await ruleApi.testRule(testRuleId.value, testContext)
    testResult.value = response.data
  } catch (error) {
    message.error('测试失败')
  }
}

// 切换状态
const handleToggleStatus = (record: Rule, status: string) => {
  Modal.confirm({
    title: '确认操作',
    content: `确定要${status === 'ACTIVE' ? '启用' : '暂停'}该规则吗？`,
    onOk: async () => {
      try {
        if (status === 'ACTIVE') {
          await ruleApi.enableRule(record.id, '1')
        } else {
          await ruleApi.disableRule(record.id, '1')
        }
        message.success('操作成功')
        fetchRules()
      } catch (error) {
        message.error('操作失败')
      }
    },
  })
}

// 删除规则
const handleDelete = (record: Rule) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该规则吗？此操作不可恢复。',
    onOk: async () => {
      try {
        await ruleApi.deleteRule(record.id, '1')
        message.success('删除成功')
        fetchRules()
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 批量操作
const handleBatchAction = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要操作的规则')
    return
  }
  message.info(`批量执行 ${selectedRowKeys.value.length} 条规则`)
}

// 验证表达式
const handleValidateExpression = async () => {
  if (!ruleFormData.triggerExpression) {
    message.warning('请先输入表达式')
    return
  }
  try {
    const response = await ruleApi.validateExpression(ruleFormData.triggerExpression)
    if (response.data.valid) {
      message.success('表达式验证通过')
    } else {
      message.error(`表达式验证失败: ${response.data.errors?.join(', ')}`)
    }
  } catch (error) {
    message.error('验证失败')
  }
}

// 规则弹窗确认
const handleRuleModalOk = async () => {
  try {
    if (ruleEditMode.value === 'add' || ruleEditMode.value === 'copy') {
      await ruleApi.createRule(ruleFormData)
      message.success('创建成功')
    } else {
      await ruleApi.updateRule(currentRule.value!.id, ruleFormData)
      message.success('更新成功')
    }
    ruleModalVisible.value = false
    fetchRules()
  } catch (error) {
    message.error(ruleEditMode.value === 'add' ? '创建失败' : '更新失败')
  }
}

// 设备过滤
const filterDeviceOption = (input: string, option: any) => {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

// 获取类型颜色
const getTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    ALARM: 'error',
    LINKAGE: 'warning',
    BILLING: 'blue',
  }
  return colorMap[type] || 'default'
}

// 获取类型文本
const getTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    ALARM: '告警规则',
    LINKAGE: '联动规则',
    BILLING: '计费规则',
  }
  return textMap[type] || type
}

// 获取状态徽标
const getStatusBadge = (status: string) => {
  const badgeMap: Record<string, string> = {
    ACTIVE: 'success',
    SUSPENDED: 'warning',
    DRAFT: 'default',
    ARCHIVED: 'error',
  }
  return badgeMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    ACTIVE: '启用',
    SUSPENDED: '暂停',
    DRAFT: '草稿',
    ARCHIVED: '归档',
  }
  return textMap[status] || status
}

// 获取优先级颜色
const getPriorityColor = (priority: number) => {
  if (priority >= 80) return 'error'
  if (priority >= 50) return 'warning'
  return 'default'
}

onMounted(() => {
  fetchRules()
  // 模拟设备数据
  deviceOptions.value = [
    { value: 'device-001', label: '温度传感器-001' },
    { value: 'device-002', label: '湿度传感器-002' },
    { value: 'device-003', label: '烟雾探测器-003' },
  ]
})
</script>

<style scoped>
.rule-management-container {
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
}

.table-actions {
  margin-bottom: 16px;
}

.expression-editor {
  width: 100%;
}

.expression-editor .ant-btn {
  margin-top: 8px;
}
</style>
