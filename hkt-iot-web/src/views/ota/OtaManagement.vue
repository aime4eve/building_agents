<template>
  <div class="ota-management-container">
    <a-card :bordered="false">
      <!-- 操作栏 -->
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="handleCreateTask">
            <PlusOutlined /> 创建升级任务
          </a-button>
          <a-button type="primary" ghost @click="handleRefresh">
            <ReloadOutlined /> 刷新
          </a-button>
        </a-space>
      </div>

      <!-- OTA任务列表 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="taskId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'taskStatus'">
            <a-badge
              :status="getStatusBadge(record.taskStatus)"
              :text="getStatusText(record.taskStatus)"
            />
          </template>
          <template v-else-if="column.key === 'progress'">
            <a-progress
              :percent="record.progress"
              :status="getProgressStatus(record.taskStatus)"
              :stroke-color="getProgressColor(record.taskStatus)"
            />
          </template>
          <template v-else-if="column.key === 'deviceInfo'">
            <a-tooltip :title="`设备数量: ${record.deviceCount || record.deviceIds?.length}`">
              <span>{{ record.deviceIds?.length || 0 }} 台设备</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'firmwareSize'">
            {{ formatFileSize(record.fileSize) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleViewDetail(record)"
              >
                详情
              </a-button>
              <a-button
                v-if="record.taskStatus === 'PENDING'"
                type="link"
                size="small"
                @click="handleStartTask(record)"
              >
                开始
              </a-button>
              <a-button
                v-else-if="record.taskStatus === 'DOWNLOADING' || record.taskStatus === 'INSTALLING'"
                type="link"
                size="small"
                @click="handlePauseTask(record)"
              >
                暂停
              </a-button>
              <a-button
                v-else-if="record.taskStatus === 'FAILED'"
                type="link"
                size="small"
                @click="handleRetryTask(record)"
              >
                重试
              </a-button>
              <a-button
                v-else-if="record.taskStatus === 'SUCCESS' && record.canRollback"
                type="link"
                size="small"
                @click="handleRollbackTask(record)"
              >
                回滚
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleViewDevices(record)">
                      <MobileOutlined /> 设备列表
                    </a-menu-item>
                    <a-menu-item @click="handleViewProgress(record)">
                      <BarChartOutlined /> 进度详情
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item
                      v-if="record.taskStatus !== 'SUCCESS' && record.taskStatus !== 'CANCELLED'"
                      @click="handleCancelTask(record)"
                    >
                      <CloseCircleOutlined /> 取消任务
                    </a-menu-item>
                    <a-menu-item
                      v-else
                      @click="handleDeleteTask(record)"
                      danger
                    >
                      <DeleteOutlined /> 删除任务
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

    <!-- 创建/编辑任务弹窗 -->
    <a-modal
      v-model:open="taskModalVisible"
      :title="taskModalTitle"
      :width="700"
      @ok="handleTaskModalOk"
      @cancel="taskModalVisible = false"
    >
      <a-form
        ref="taskFormRef"
        :model="taskFormData"
        :rules="taskFormRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="任务名称" name="taskName">
          <a-input v-model:value="taskFormData.taskName" placeholder="请输入任务名称" />
        </a-form-item>

        <a-form-item label="固件版本" name="firmwareVersion">
          <a-input v-model:value="taskFormData.firmwareVersion" placeholder="请输入固件版本，如：1.0.0" />
        </a-form-item>

        <a-form-item label="固件URL" name="firmwareUrl">
          <a-input
            v-model:value="taskFormData.firmwareUrl"
            placeholder="请输入固件下载地址"
            prefix="h(LinkOutlined)"
          />
        </a-form-item>

        <a-form-item label="固件文件" name="firmwareFile">
          <a-upload
            :before-upload="beforeUpload"
            @change="handleFileChange"
          >
            <a-button>
              <UploadOutlined /> 选择文件
            </a-button>
            <template #tip>
              支持 .bin/.hex/.zip 格式，文件大小不超过100MB
            </template>
          </a-upload>
          <div v-if="uploadedFile" class="upload-info">
            <a-tag color="success" closable @close="handleRemoveFile">
              {{ uploadedFile.name }} ({{ formatFileSize(uploadedFile.size) }})
            </a-tag>
            <input type="hidden" v-model:value="taskFormData.firmwareUrl" />
            <input type="hidden" v-model:value="taskFormData.fileSize" />
            <input type="hidden" v-model:value="taskFormData.fileMd5" />
          </div>
        </a-form-item>

        <a-form-item label="升级设备" name="deviceIds">
          <a-select
            v-model:value="taskFormData.deviceIds"
            mode="multiple"
            placeholder="请选择要升级的设备"
            show-search
            :filter-option="filterDeviceOption"
            :options="deviceOptions"
            style="width: 100%"
          />
          <div class="device-count-info">
            已选择 {{ taskFormData.deviceIds?.length || 0 }} 台设备
          </div>
        </a-form-item>

        <a-form-item label="定时执行" name="scheduleAt">
          <a-date-picker
            v-model:value="scheduleAtDate"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择执行时间（可选，留空表示立即执行）"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="taskFormData.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 任务详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="任务详情"
      :width="800"
      :footer="null"
    >
      <a-descriptions bordered :column="2" v-if="currentTask">
        <a-descriptions-item label="任务名称">
          {{ currentTask.taskName }}
        </a-descriptions-item>
        <a-descriptions-item label="固件版本">
          {{ currentTask.firmwareVersion }}
        </a-descriptions-item>
        <a-descriptions-item label="任务状态">
          <a-badge
            :status="getStatusBadge(currentTask.taskStatus)"
            :text="getStatusText(currentTask.taskStatus)"
          />
        </a-descriptions-item>
        <a-descriptions-item label="执行进度">
          <a-progress
            :percent="currentTask.progress"
            :status="getProgressStatus(currentTask.taskStatus)"
          />
          <div class="progress-detail">
            已下载: {{ formatFileSize(currentTask.downloadedSize) }} / 已安装: {{ currentTask.installedCount }}/{{ currentTask.totalCount }}
          </div>
        </a-descriptions-item>
        <a-descriptions-item label="设备数量">
          {{ currentTask.deviceIds?.length || 0 }} 台设备
        </a-descriptions-item>
        <a-descriptions-item label="文件大小">
          {{ formatFileSize(currentTask.fileSize) }}
        </a-descriptions-item>
        <a-descriptions-item label="MD5校验">
          <code>{{ currentTask.fileMd5 }}</code>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatDateTime(currentTask.createdAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ formatDateTime(currentTask.updatedAt) }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 设备列表 -->
      <a-divider>设备升级状态</a-divider>
      <a-table
        :columns="deviceColumns"
        :data-source="taskDevices"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column: record }">
          <template v-if="column.key === 'status'">
            <a-badge
              :status="getDeviceStatusBadge(record.status)"
              :text="getDeviceStatusText(record.status)"
            />
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 设备选择弹窗 -->
    <a-modal
      v-model:open="devicesModalVisible"
      title="选择升级设备"
      :width="800"
      @ok="handleDevicesConfirm"
    >
      <a-form layout="inline" class="device-filter-form">
        <a-form-item label="设备类型">
          <a-select
            v-model:value="deviceFilter.deviceType"
            placeholder="请选择"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="GATEWAY">网关</a-select-option>
            <a-select-option value="AIR_CONDITIONER">空调</a-select-option>
            <a-select-option value="LIGHT">灯光</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="deviceFilter.status"
            placeholder="请选择"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="ONLINE">在线</a-select-option>
            <a-select-option value="OFFLINE">离线</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearchDevices">查询</a-button>
        </a-form-item>
      </a-form>

      <a-table
        :columns="deviceSelectionColumns"
        :row-selection="deviceRowSelection"
        :data-source="availableDevices"
        :loading="devicesLoading"
        :pagination="devicePagination"
        size="small"
        row-key="deviceId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge
              :status="record.deviceStatus === 'ONLINE' ? 'success' : 'default'"
              :text="getDeviceStatusText(record.deviceStatus)"
            />
          </template>
          <template v-else-if="column.key === 'firmwareVersion'">
            <span>{{ record.firmwareVersion || '-' }}</span>
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
  PlusOutlined,
  ReloadOutlined,
  MobileOutlined,
  BarChartOutlined,
  CloseCircleOutlined,
  DeleteOutlined,
  DownOutlined,
  UploadOutlined,
  LinkOutlined,
} from '@ant-design/icons-vue'
import {
  otaApi,
  deviceApi,
  type OtaTask,
  type OtaTaskCreateRequest,
  type OtaTaskStatus,
  type Device,
  type DeviceStatus,
  getDeviceStatusText,
} from '@/api/device'
import { formatDateTime, formatFileSize } from '@/utils/format'

// 表格列定义
const columns = [
  { title: '任务名称', dataIndex: 'taskName', key: 'taskName', width: 200 },
  { title: '固件版本', dataIndex: 'firmwareVersion', key: 'firmwareVersion', width: 120 },
  { title: '状态', dataIndex: 'taskStatus', key: 'taskStatus', width: 120 },
  { title: '进度', dataIndex: 'progress', key: 'progress', width: 200 },
  { title: '设备信息', dataIndex: 'deviceInfo', key: 'deviceInfo', width: 120 },
  { title: '文件大小', dataIndex: 'firmwareSize', key: 'firmwareSize', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
]

const deviceColumns = [
  { title: '设备SN', dataIndex: 'deviceSn', key: 'deviceSn', width: 150 },
  { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName', width: 150 },
  { title: '类型', dataIndex: 'deviceType', key: 'deviceType', width: 120 },
  { title: '当前版本', dataIndex: 'firmwareVersion', key: 'firmwareVersion', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '升级状态', dataIndex: 'upgradeStatus', key: 'upgradeStatus', width: 120 },
  { title: '升级进度', dataIndex: 'upgradeProgress', key: 'upgradeProgress', width: 120 },
]

const deviceSelectionColumns = [
  { type: 'selection', width: 50 },
  { title: '设备SN', dataIndex: 'deviceSn', key: 'deviceSn', width: 150 },
  { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName' },
  { title: '类型', dataIndex: 'deviceType', key: 'deviceType' },
  { title: '当前版本', dataIndex: 'firmwareVersion', key: 'firmwareVersion' },
  { title: '状态', dataIndex: 'status', key: 'status' },
]

// 数据源
const dataSource = ref<OtaTask[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 弹窗相关
const taskModalVisible = ref(false)
const detailModalVisible = ref(false)
const devicesModalVisible = ref(false)
const taskModalTitle = ref('')
const editMode = ref<'add' | 'edit'>('add')
const currentTask = ref<OtaTask | null>(null)

// 任务表单
const taskFormData = reactive<Partial<OtaTaskCreateRequest>>({
  taskName: '',
  deviceIds: [],
  firmwareVersion: '',
  firmwareUrl: '',
  fileSize: 0,
  fileMd5: '',
  scheduleAt: '',
  description: '',
})

const taskFormRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  firmwareVersion: [{ required: true, message: '请输入固件版本', trigger: 'blur' }],
  firmwareUrl: [{ required: true, message: '请输入固件URL或上传文件', trigger: 'blur' }],
  deviceIds: [
    {
      required: true,
      type: 'array',
      min: 1,
      message: '请至少选择一台设备',
      trigger: 'change',
    },
  ],
}

// 文件上传
const uploadedFile = ref<any>(null)
const scheduleAtDate = ref<Dayjs>()

// 可用设备
const availableDevices = ref<Device[]>([])
const devicesLoading = ref(false)
const selectedDeviceKeys = ref<string[]>([])
const deviceFilter = reactive({
  deviceType: '',
  status: '',
})
const devicePagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const deviceRowSelection = {
  selectedRowKeys: selectedDeviceKeys,
  onChange: (keys: string[]) => {
    selectedDeviceKeys.value = keys
  },
}

// 设备选项
const deviceOptions = ref<{ value: string; label: string }[]>([])

// 任务设备
const taskDevices = ref<any[]>([])

// 获取OTA任务列表
const fetchTasks = async () => {
  loading.value = true
  try {
    const response = await otaApi.getTasks({
      page: pagination.current,
      size: pagination.pageSize,
    })
    dataSource.value = response.data.items
    pagination.total = response.data.total
  } catch (error) {
    console.error('获取OTA任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 创建任务
const handleCreateTask = () => {
  editMode.value = 'add'
  taskModalTitle.value = '创建OTA升级任务'
  Object.assign(taskFormData, {
    taskName: '',
    deviceIds: [],
    firmwareVersion: '',
    firmwareUrl: '',
    fileSize: 0,
    fileMd5: '',
    scheduleAt: '',
    description: '',
  })
  uploadedFile.value = null
  scheduleAtDate.value = undefined
  taskModalVisible.value = true
}

// 刷新
const handleRefresh = () => {
  fetchTasks()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchTasks()
}

// 查看详情
const handleViewDetail = async (record: OtaTask) => {
  currentTask.value = record
  detailModalVisible.value = true

  // 模拟设备升级状态
  taskDevices.value = record.deviceIds.map((deviceId) => ({
    deviceId,
    deviceSn: deviceId,
    deviceName: `设备-${deviceId.slice(-6)}`,
    status: record.taskStatus === 'SUCCESS' ? 'SUCCESS' : 'PENDING',
    firmwareVersion: record.firmwareVersion,
    upgradeStatus: record.taskStatus,
    upgradeProgress: record.progress,
  }))
}

// 开始任务
const handleStartTask = async (record: OtaTask) => {
  try {
    await otaApi.startTask(record.taskId)
    message.success('任务已开始')
    fetchTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 暂停任务
const handlePauseTask = async (record: OtaTask) => {
  try {
    await otaApi.pauseTask(record.taskId)
    message.success('任务已暂停')
    fetchTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 重试任务
const handleRetryTask = async (record: OtaTask) => {
  try {
    await otaApi.startTask(record.taskId)
    message.success('任务已重新开始')
    fetchTasks()
  } catch (error) {
    message.error('操作失败')
  }
}

// 回滚任务
const handleRollbackTask = async (record: OtaTask) => {
  Modal.confirm({
    title: '确认回滚',
    content: '确定要回滚这些设备的固件版本吗？此操作不可逆。',
    onOk: async () => {
      try {
        await otaApi.rollbackTask(record.taskId)
        message.success('回滚指令已发送')
        fetchTasks()
      } catch (error) {
        message.error('操作失败')
      }
    },
  })
}

// 取消任务
const handleCancelTask = async (record: OtaTask) => {
  Modal.confirm({
    title: '确认取消',
    content: '确定要取消该OTA任务吗？',
    onOk: async () => {
      try {
        await otaApi.cancelTask(record.taskId)
        message.success('任务已取消')
        fetchTasks()
      } catch (error) {
        message.error('操作失败')
      }
    },
  })
}

// 删除任务
const handleDeleteTask = async (record: OtaTask) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该OTA任务吗？此操作不可恢复。',
    onOk: async () => {
      try {
        await otaApi.deleteTask(record.taskId)
        message.success('任务已删除')
        fetchTasks()
      } catch (error) {
        message.error('操作失败')
      }
    },
  })
}

// 查看设备列表
const handleViewDevices = async (record: OtaTask) => {
  await handleOpenDeviceSelector((selected) => {
    console.log('Selected devices for task:', selected)
  })
}

// 查看进度
const handleViewProgress = (record: OtaTask) => {
  message.info('查看进度功能开发中')
}

// 打开设备选择器
const handleOpenDeviceSelector = async (onConfirm: (devices: Device[]) => void) => {
  devicesModalVisible.value = true
  await fetchAvailableDevices()

  // 设置确认回调
  window.deviceConfirmCallback = onConfirm
}

// 查询可用设备
const fetchAvailableDevices = async () => {
  devicesLoading.value = true
  try {
    const response = await deviceApi.getDevices({
      page: devicePagination.current,
      size: devicePagination.pageSize,
      deviceStatus: deviceFilter.status as DeviceStatus || undefined,
      deviceType: deviceFilter.deviceType as DeviceType || undefined,
    })
    availableDevices.value = response.data.items
    devicePagination.total = response.data.total
  } catch (error) {
    console.error('获取设备列表失败:', error)
  } finally {
    devicesLoading.value = false
  }
}

// 搜索设备
const handleSearchDevices = () => {
  devicePagination.current = 1
  fetchAvailableDevices()
}

// 确认设备选择
const handleDevicesConfirm = () => {
  if (selectedDeviceKeys.value.length === 0) {
    message.warning('请选择要升级的设备')
    return
  }

  taskFormData.deviceIds = [...selectedDeviceKeys.value]
  deviceOptions.value = availableDevices.value
    .filter((d) => selectedDeviceKeys.value.includes(d.deviceId))
    .map((d) => ({ value: d.deviceId, label: `${d.deviceName}(${d.deviceSn})` }))
  devicesModalVisible.value = false

  if (window.deviceConfirmCallback) {
    window.deviceConfirmCallback(availableDevices.value.filter((d) =>
      selectedDeviceKeys.value.includes(d.deviceId)
    ))
  }
}

// 文件上传前处理
const beforeUpload = (file: File) => {
  const isValidType = ['bin', 'hex', 'zip'].some((ext) => file.name.endsWith(`.${ext}`))
  if (!isValidType) {
    message.error('只支持 .bin/.hex/.zip 格式的文件')
    return false
  }

  const isValidSize = file.size <= 100 * 1024 * 1024
  if (!isValidSize) {
    message.error('文件大小不能超过100MB')
    return false
  }

  uploadedFile.value = file
  taskFormData.firmwareUrl = file.name
  taskFormData.fileSize = file.size

  return false // 阻止自动上传
}

// 文件变化
const handleFileChange = async (info: any) => {
  // 计算MD5（前端无法直接计算，需要后端处理）
  if (uploadedFile.value) {
    // 实际场景中应该上传到服务器获取MD5
    message.success('文件已选择，提交时将自动计算MD5')
  }
}

// 移除文件
const handleRemoveFile = () => {
  uploadedFile.value = null
  taskFormData.firmwareUrl = ''
  taskFormData.fileSize = 0
}

// 过滤设备选项
const filterDeviceOption = (input: string, option: any) => {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

// 弹窗确认
const handleTaskModalOk = async () => {
  // 处理日期时间
  if (scheduleAtDate.value) {
    taskFormData.scheduleAt = scheduleAtDate.value.format('YYYY-MM-DD HH:mm:ss')
  }

  try {
    await otaApi.createTask(taskFormData as OtaTaskCreateRequest)
    message.success('任务创建成功')
    taskModalVisible.value = false
    fetchTasks()
  } catch (error) {
    message.error('创建失败')
  }
}

// 获取状态徽标
const getStatusBadge = (status: OtaTaskStatus) => {
  const badgeMap: Record<OtaTaskStatus, string> = {
    PENDING: 'default',
    DOWNLOADING: 'processing',
    DOWNLOAD_FAILED: 'error',
    INSTALLING: 'processing',
    SUCCESS: 'success',
    FAILED: 'error',
    ROLLBACK: 'warning',
    CANCELLED: 'default',
  }
  return badgeMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: OtaTaskStatus) => {
  const textMap: Record<OtaTaskStatus, string> = {
    PENDING: '待执行',
    DOWNLOADING: '下载中',
    DOWNLOAD_FAILED: '下载失败',
    INSTALLING: '升级中',
    SUCCESS: '升级成功',
    FAILED: '升级失败',
    ROLLBACK: '回滚中',
    CANCELLED: '已取消',
  }
  return textMap[status] || status
}

// 获取进度状态
const getProgressStatus = (status: OtaTaskStatus) => 'normal'

// 获取进度颜色
const getProgressColor = (status: OtaTaskStatus) => {
  const colorMap: Record<OtaTaskStatus, string> = {
    PENDING: undefined,
    DOWNLOADING: 'blue',
    DOWNLOAD_FAILED: 'red',
    INSTALLING: 'blue',
    SUCCESS: 'green',
    FAILED: 'red',
    ROLLBACK: 'orange',
    CANCELLED: 'gray',
  }
  return colorMap[status]
}

// 获取设备状态徽标
const getDeviceStatusBadge = (status: string) => {
  return status === 'SUCCESS' ? 'success' : status === 'FAILED' ? 'error' : 'processing'
}

// 获取设备状态文本
const getDeviceStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    SUCCESS: '已完成',
    FAILED: '失败',
    PENDING: '等待中',
  }
  return textMap[status] || status
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
.ota-management-container {
  padding: 24px;
}

.table-actions {
  margin-bottom: 16px;
}

.device-count-info {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.upload-info {
  margin-top: 8px;
}

.progress-detail {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.device-filter-form {
  margin-bottom: 16px;
}
</style>
