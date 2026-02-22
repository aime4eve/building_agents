<template>
  <div class="schedule-management-container">
    <a-card :bordered="false">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="queryForm" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="queryForm.keyword" placeholder="计划名称/编码" allow-clear />
        </a-form-item>
        <a-form-item label="计划类型">
          <a-select v-model:value="queryForm.scheduleType" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="ONCE">单次执行</a-select-option>
            <a-select-option value="DAILY">每天执行</a-select-option>
            <a-select-option value="WEEKLY">每周执行</a-select-option>
            <a-select-option value="MONTHLY">每月执行</a-select-option>
            <a-select-option value="CRON">Cron表达式</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryForm.scheduleStatus" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="ACTIVE">启用</a-select-option>
            <a-select-option value="INACTIVE">停用</a-select-option>
            <a-select-option value="PAUSED">暂停</a-select-option>
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
            <PlusOutlined /> 新增计划
          </a-button>
          <a-button type="primary" ghost @click="handleBatchExecute">
              <ThunderboltOutlined /> 批量执行
            </a-button>
        </a-space>
      </div>

      <!-- 定时计划列表 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="scheduleId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'scheduleType'">
            <a-tag :color="getTypeColor(record.scheduleType)">
              {{ getTypeText(record.scheduleType) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'scheduleStatus'">
            <a-badge
              :status="getStatusBadge(record.scheduleStatus)"
              :text="getStatusText(record.scheduleStatus)"
            />
          </template>
          <template v-else-if="column.key === 'nextExecution'">
            <span v-if="record.nextExecutionAt">
              {{ formatDateTime(record.nextExecutionAt) }}
            </span>
            <span v-else class="text-gray">-</span>
          </template>
          <template v-else-if="column.key === 'cronExpression'">
            <a-tooltip v-if="record.cronExpression" :title="record.cronExpression">
              <span class="cron-text">{{ record.cronExpression }}</span>
            </a-tooltip>
            <span v-else class="text-gray">-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button
                v-if="record.scheduleStatus === 'ACTIVE'"
                type="link"
                size="small"
                @click="handlePause(record)"
              >
                暂停
              </a-button>
              <a-button
                v-else-if="record.scheduleStatus === 'PAUSED'"
                type="link"
                size="small"
                @click="handleResume(record)"
              >
                恢复
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleExecuteNow(record)">
                      <ThunderboltOutlined /> 立即执行
                    </a-menu-item>
                    <a-menu-item @click="handleViewExecutions(record)">
                      <HistoryOutlined /> 执行记录
                    </a-menu-item>
                    <a-menu-divider />
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

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :width="700"
      @ok="handleModalOk"
      @cancel="modalVisible = false"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="计划名称" name="scheduleName">
          <a-input v-model:value="formData.scheduleName" placeholder="请输入计划名称" />
        </a-form-item>
        <a-form-item label="计划编码" name="scheduleCode">
          <a-input v-model:value="formData.scheduleCode" placeholder="请输入计划编码" />
        </a-form-item>
        <a-form-item label="计划类型" name="scheduleType">
          <a-select v-model:value="formData.scheduleType" placeholder="请选择计划类型">
            <a-select-option value="ONCE">单次执行</a-select-option>
            <a-select-option value="DAILY">每天执行</a-select-option>
            <a-select-option value="WEEKLY">每周执行</a-select-option>
            <a-select-option value="MONTHLY">每月执行</a-select-option>
            <a-select-option value="CRON">Cron表达式</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 单次执行 -->
        <template v-if="formData.scheduleType === 'ONCE'">
          <a-form-item label="执行时间" name="executeAt">
            <a-date-picker
              v-model:value="executeAtDate"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择执行时间"
              style="width: 100%"
            />
          </a-form-item>
        </template>

        <!-- 每周执行 -->
        <template v-if="formData.scheduleType === 'WEEKLY'">
          <a-form-item label="执行时间" name="executeAt">
            <a-time-picker
              v-model:value="executeAtTime"
              format="HH:mm:ss"
              placeholder="请选择时间"
              style="width: 100%"
            />
          </a-form-item>
          <a-form-item label="星期" name="weekDays">
            <a-checkbox-group v-model:value="formData.weekDays">
              <a-checkbox :value="1">周一</a-checkbox>
              <a-checkbox :value="2">周二</a-checkbox>
              <a-checkbox :value="3">周三</a-checkbox>
              <a-checkbox :value="4">周四</a-checkbox>
              <a-checkbox :value="5">周五</a-checkbox>
              <a-checkbox :value="6">周六</a-checkbox>
              <a-checkbox :value="0">周日</a-checkbox>
            </a-checkbox-group>
          </a-form-item>
        </template>

        <!-- 每月执行 -->
        <template v-if="formData.scheduleType === 'MONTHLY'">
          <a-form-item label="执行时间" name="executeAt">
            <a-time-picker
              v-model:value="executeAtTime"
              format="HH:mm:ss"
              placeholder="请选择时间"
              style="width: 100%"
            />
          </a-form-item>
          <a-form-item label="日期" name="monthDays">
            <a-checkbox-group v-model:value="formData.monthDays">
              <a-row>
                <a-col v-for="day in 31" :key="day" :span="4">
                  <a-checkbox :value="day">{{ day }}</a-checkbox>
                </a-col>
              </a-row>
            </a-checkbox-group>
          </a-form-item>
        </template>

        <!-- Cron表达式 -->
        <template v-if="formData.scheduleType === 'CRON'">
          <a-form-item label="Cron表达式" name="cronExpression">
            <a-space direction="vertical" style="width: 100%">
              <a-input
                v-model:value="formData.cronExpression"
                placeholder="请输入Cron表达式，如: 0 8 * * *"
              />
              <a-space>
                <a-button size="small" @click="handleValidateCron">验证</a-button>
                <a-select
                  v-model:value="selectedPreset"
                  placeholder="选择预设"
                  style="width: 200px"
                  @change="handleSelectPreset"
                >
                  <a-select-option value="* * * * *">每分钟</a-select-option>
                  <a-select-option value="0 * * * *">每小时</a-select-option>
                  <a-select-option value="0 0 * * *">每天凌晨</a-select-option>
                  <a-select-option value="0 8 * * *">每天早上8点</a-select-option>
                  <a-select-option value="0 8 * * 1">每周一早上8点</a-select-option>
                </a-select>
              </a-space>
              <div v-if="cronValidation.valid !== null" class="cron-validation">
                <a-alert
                  :type="cronValidation.valid ? 'success' : 'error'"
                  :message="cronValidation.valid ? 'Cron表达式有效' : cronValidation.error"
                  show-icon
                />
              </div>
            </a-space>
          </a-form-item>
        </template>

        <a-form-item label="关联场景" name="targetSceneId">
          <a-select
            v-model:value="formData.targetSceneId"
            placeholder="请选择要执行的场景"
            allow-clear
            show-search
            :filter-option="filterSceneOption"
          >
            <a-select-option v-for="scene in sceneList" :key="scene.sceneId" :value="scene.sceneId">
              {{ scene.sceneName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 执行记录弹窗 -->
    <a-modal
      v-model:open="executionModalVisible"
      title="执行记录"
      :width="900"
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
          <template v-if="column.key === 'executionStatus'">
            <a-badge
              :status="getExecutionStatusBadge(record.executionStatus)"
              :text="getExecutionStatusText(record.executionStatus)"
            />
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  ThunderboltOutlined,
  DownOutlined,
  HistoryOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import {
  scheduleApi,
  type ScheduleQuery,
  type Schedule,
  type ScheduleCreateRequest,
  type ScheduleExecutionRecord,
  isValidCronExpression,
} from '@/api/schedule'
import { sceneApi, type Scenario } from '@/api/scenario'
import { formatDateTime } from '@/utils/format'

// 查询表单
const queryForm = reactive<ScheduleQuery>({
  keyword: '',
  scheduleType: undefined,
  scheduleStatus: undefined,
  page: 1,
  size: 10,
})

// 数据源
const dataSource = ref<Schedule[]>([])
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
  { title: '计划名称', dataIndex: 'scheduleName', key: 'scheduleName', width: 180 },
  { title: '计划编码', dataIndex: 'scheduleCode', key: 'scheduleCode', width: 150 },
  { title: '类型', dataIndex: 'scheduleType', key: 'scheduleType', width: 120 },
  { title: 'Cron表达式', dataIndex: 'cronExpression', key: 'cronExpression', width: 150 },
  { title: '下次执行', dataIndex: 'nextExecution', key: 'nextExecution', width: 180 },
  { title: '状态', dataIndex: 'scheduleStatus', key: 'scheduleStatus', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
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

// 弹窗相关
const modalVisible = ref(false)
const modalTitle = ref('')
const editMode = ref<'add' | 'edit'>('add')
const currentSchedule = ref<Schedule | null>(null)

const formData = reactive<Partial<ScheduleCreateRequest>>({
  scheduleName: '',
  scheduleCode: '',
  scheduleType: 'DAILY',
  cronExpression: '',
  executeAt: '',
  weekDays: [],
  monthDays: [],
  targetSceneId: '',
  description: '',
})

const formRules = {
  scheduleName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  scheduleCode: [{ required: true, message: '请输入计划编码', trigger: 'blur' }],
  scheduleType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' },
    { validator: (_rule: any, value: string) => isValidCronExpression(value) ? Promise.resolve() : Promise.reject('Cron表达式格式不正确') }
  ],
}

// 日期时间处理
const executeAtDate = ref<Dayjs>()
const executeAtTime = ref<Dayjs>()

// Cron预设
const selectedPreset = ref<string>()
const cronValidation = reactive<{ valid: boolean | null; error?: string }>({
  valid: null,
})

// 场景列表
const sceneList = ref<Scenario[]>([])

// 执行记录
const executionModalVisible = ref(false)
const executions = ref<ScheduleExecutionRecord[]>([])
const executionLoading = ref(false)

const executionColumns = [
  { title: '执行时间', dataIndex: 'scheduledAt', key: 'scheduledAt' },
  { title: '状态', dataIndex: 'executionStatus', key: 'executionStatus' },
  { title: '完成时间', dataIndex: 'completedAt', key: 'completedAt' },
  { title: '结果', dataIndex: 'result', key: 'result' },
  { title: '错误', dataIndex: 'error', key: 'error' },
]

// 获取定时计划列表
const fetchSchedules = async () => {
  loading.value = true
  try {
    const response = await scheduleApi.getSchedules(queryForm)
    dataSource.value = response.data.items
    pagination.total = response.data.total
  } catch (error) {
    console.error('获取定时计划列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取场景列表
const fetchScenes = async () => {
  try {
    const response = await sceneApi.getScenes({ page: 1, size: 1000 })
    sceneList.value = response.data.items.filter(s => s.sceneStatus === 'ACTIVE')
  } catch (error) {
    console.error('获取场景列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchSchedules()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.scheduleType = undefined
  queryForm.scheduleStatus = undefined
  handleSearch()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchSchedules()
}

// 新增
const handleAdd = () => {
  editMode.value = 'add'
  modalTitle.value = '新增定时计划'
  currentSchedule.value = null
  Object.assign(formData, {
    scheduleName: '',
    scheduleCode: '',
    scheduleType: 'DAILY',
    cronExpression: '',
    executeAt: '',
    weekDays: [],
    monthDays: [],
    targetSceneId: '',
    description: '',
  })
  executeAtDate.value = undefined
  executeAtTime.value = undefined
  selectedPreset.value = undefined
  cronValidation.valid = null
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: Schedule) => {
  editMode.value = 'edit'
  modalTitle.value = '编辑定时计划'
  currentSchedule.value = record
  Object.assign(formData, {
    scheduleName: record.scheduleName,
    scheduleCode: record.scheduleCode,
    scheduleType: record.scheduleType,
    cronExpression: record.cronExpression,
    executeAt: record.executeAt,
    weekDays: record.weekDays,
    monthDays: record.monthDays,
    targetSceneId: record.targetSceneId,
    description: record.description,
  })

  if (record.executeAt) {
    const dateTime = dayjs(record.executeAt)
    executeAtDate.value = dateTime
    executeAtTime.value = dateTime
  }

  cronValidation.valid = null
  modalVisible.value = true
}

// 查看
const handleView = (record: Schedule) => {
  message.info('查看详情功能开发中')
}

// 暂停
const handlePause = async (record: Schedule) => {
  try {
    await scheduleApi.pauseSchedule(record.scheduleId)
    message.success('已暂停')
    fetchSchedules()
  } catch (error) {
    message.error('操作失败')
  }
}

// 恢复
const handleResume = async (record: Schedule) => {
  try {
    await scheduleApi.resumeSchedule(record.scheduleId)
    message.success('已恢复')
    fetchSchedules()
  } catch (error) {
    message.error('操作失败')
  }
}

// 立即执行
const handleExecuteNow = async (record: Schedule) => {
  Modal.confirm({
    title: '确认执行',
    content: '确定要立即执行该定时计划吗？',
    onOk: async () => {
      try {
        await scheduleApi.executeSchedule(record.scheduleId)
        message.success('已发送执行请求')
      } catch (error) {
        message.error('执行失败')
      }
    },
  })
}

// 查看执行记录
const handleViewExecutions = async (record: Schedule) => {
  executionModalVisible.value = true
  executionLoading.value = true
  try {
    const response = await scheduleApi.getExecutionRecords(record.scheduleId, { page: 1, size: 50 })
    executions.value = response.data.items
  } catch (error) {
    console.error('获取执行记录失败:', error)
  } finally {
    executionLoading.value = false
  }
}

// 批量执行
const handleBatchExecute = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要执行的定时计划')
    return
  }
  Modal.confirm({
    title: '确认批量执行',
    content: `确定要立即执行选中的 ${selectedRowKeys.value.length} 个定时计划吗？`,
    onOk: async () => {
      message.info('批量执行功能开发中')
    },
  })
}

// 删除
const handleDelete = (record: Schedule) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该定时计划吗？此操作不可恢复。',
    onOk: async () => {
      try {
        await scheduleApi.deleteSchedule(record.scheduleId)
        message.success('删除成功')
        fetchSchedules()
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 验证Cron表达式
const handleValidateCron = async () => {
  if (!formData.cronExpression) {
    message.warning('请先输入Cron表达式')
    return
  }

  try {
    const response = await scheduleApi.validateCronExpression(formData.cronExpression!)
    cronValidation.valid = response.data.valid
    cronValidation.error = response.data.error

    if (response.data.valid) {
      message.success('Cron表达式有效')
    }
  } catch (error) {
    message.error('验证失败')
  }
}

// 选择预设Cron
const handleSelectPreset = (value: string) => {
  formData.cronExpression = value
  handleValidateCron()
}

// 过滤场景
const filterSceneOption = (input: string, option: any) => {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

// 弹窗确认
const handleModalOk = async () => {
  // 处理日期时间
  if (executeAtDate.value && executeAtTime.value) {
    const date = executeAtDate.value.format('YYYY-MM-DD')
    const time = executeAtTime.value.format('HH:mm:ss')
    formData.executeAt = `${date} ${time}`
  }

  try {
    if (editMode.value === 'add') {
      await scheduleApi.createSchedule(formData as ScheduleCreateRequest)
      message.success('创建成功')
    } else {
      await scheduleApi.updateSchedule(currentSchedule.value!.scheduleId, formData)
      message.success('更新成功')
    }
    modalVisible.value = false
    fetchSchedules()
  } catch (error) {
    message.error(editMode.value === 'add' ? '创建失败' : '更新失败')
  }
}

// 获取类型颜色
const getTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    ONCE: 'blue',
    DAILY: 'green',
    WEEKLY: 'orange',
    MONTHLY: 'purple',
    CRON: 'red',
  }
  return colorMap[type] || 'default'
}

// 获取类型文本
const getTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    ONCE: '单次执行',
    DAILY: '每天执行',
    WEEKLY: '每周执行',
    MONTHLY: '每月执行',
    CRON: 'Cron表达式',
  }
  return textMap[type] || type
}

// 获取状态徽标
const getStatusBadge = (status: string) => {
  const badgeMap: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'default',
    PAUSED: 'warning',
  }
  return badgeMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    ACTIVE: '启用',
    INACTIVE: '停用',
    PAUSED: '暂停',
  }
  return textMap[status] || status
}

// 获取执行状态徽标
const getExecutionStatusBadge = (status: string) => {
  const badgeMap: Record<string, string> = {
    SUCCESS: 'success',
    FAILED: 'error',
    RUNNING: 'processing',
    PENDING: 'default',
    SKIPPED: 'warning',
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
    SKIPPED: '已跳过',
  }
  return textMap[status] || status
}

onMounted(() => {
  fetchSchedules()
  fetchScenes()
})
</script>

<style scoped>
.schedule-management-container {
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
}

.table-actions {
  margin-bottom: 16px;
}

.cron-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.text-gray {
  color: #8c8c8c;
}

.cron-validation {
  margin-top: 8px;
}
</style>
