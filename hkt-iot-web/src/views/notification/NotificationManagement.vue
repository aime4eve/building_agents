<template>
  <div class="notification-management">
    <a-card title="通知管理" :bordered="false">
      <!-- 搜索区域 -->
      <div class="search-section">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="通知渠道">
            <a-select
              v-model:value="searchForm.channel"
              placeholder="请选择通知渠道"
              style="width: 150px"
              allow-clear
            >
              <a-select-option
                v-for="channel in channelOptions"
                :key="channel.value"
                :value="channel.value"
              >
                {{ channel.label }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="通知状态">
            <a-select
              v-model:value="searchForm.status"
              placeholder="请选择通知状态"
              style="width: 120px"
              allow-clear
            >
              <a-select-option
                v-for="status in statusOptions"
                :key="status.value"
                :value="status.value"
              >
                {{ status.label }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="请求ID">
            <a-input
              v-model:value="searchForm.requestId"
              placeholder="请输入请求ID"
              style="width: 200px"
              allow-clear
            />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="handleReset">重置</a-button>
              <a-button type="primary" @click="showSendModal = true">
                <template #icon><PlusOutlined /></template>
                发送通知
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 统计卡片 -->
      <a-row :gutter="16" class="stats-row">
        <a-col :xs="24" :sm="12" :md="6">
          <a-statistic
            title="总发送数"
            :value="statistics.totalSent"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix><SendOutlined /></template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-statistic
            title="发送成功"
            :value="statistics.totalSent - statistics.totalFailed"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix><CheckCircleOutlined /></template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-statistic
            title="发送失败"
            :value="statistics.totalFailed"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix><CloseCircleOutlined /></template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-statistic
            title="成功率"
            :value="statistics.successRate"
            suffix="%"
            :precision="2"
          >
            <template #prefix><RiseOutlined /></template>
          </a-statistic>
        </a-col>
      </a-row>

      <!-- 通知列表 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'channel'">
            <a-tag :color="getChannelColor(record.channel)">
              <component :is="getChannelIcon(record.channel)" />
              {{ getNotificationChannelText(record.channel) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'priority'">
            <a-tag :color="getPriorityColor(record.priority)">
              {{ getNotificationPriorityText(record.priority) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="getNotificationStatusColor(record.status) as any"
              :text="getNotificationStatusText(record.status)"
            />
          </template>
          <template v-else-if="column.key === 'recipients'">
            <a-tag v-for="(recipient, index) in record.recipients?.slice(0, 2)" :key="index">
              {{ recipient.name || recipient.id }}
            </a-tag>
            <a-tag v-if="record.recipients?.length > 2">
              +{{ record.recipients.length - 2 }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'content'">
            <a-tooltip :title="record.content">
              <span class="content-text">{{ record.content }}</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'createdAt'">
            {{ formatDateTime(record.createdAt) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="viewDetail(record)"
              >
                详情
              </a-button>
              <a-button
                v-if="record.status === 'FAILED'"
                type="link"
                size="small"
                @click="retryNotification(record)"
              >
                重试
              </a-button>
              <a-button
                v-if="record.status === 'PENDING'"
                type="link"
                size="small"
                danger
                @click="cancelNotification(record)"
              >
                取消
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 发送通知弹窗 -->
    <a-modal
      v-model:open="showSendModal"
      title="发送通知"
      width="700px"
      @ok="handleSendNotification"
      @cancel="resetSendForm"
    >
      <a-form
        ref="sendFormRef"
        :model="sendForm"
        :rules="sendRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="通知渠道" name="channel">
          <a-select v-model:value="sendForm.channel" placeholder="请选择通知渠道">
            <a-select-option
              v-for="channel in channelOptions"
              :key="channel.value"
              :value="channel.value"
            >
              {{ channel.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="优先级" name="priority">
          <a-select v-model:value="sendForm.priority" placeholder="请选择优先级">
            <a-select-option
              v-for="priority in priorityOptions"
              :key="priority.value"
              :value="priority.value"
            >
              {{ priority.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="接收者类型" name="recipientType">
          <a-radio-group v-model:value="sendForm.recipientType">
            <a-radio-button value="USER">用户</a-radio-button>
            <a-radio-button value="EMAIL">邮箱</a-radio-button>
            <a-radio-button value="PHONE">手机</a-radio-button>
            <a-radio-button value="WEBHOOK">Webhook</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="接收者" name="recipients">
          <a-select
            v-model:value="sendForm.recipients"
            mode="tags"
            placeholder="请输入或选择接收者"
            :options="recipientOptions"
          />
        </a-form-item>
        <a-form-item label="使用模板">
          <a-space>
            <a-switch v-model:checked="sendForm.useTemplate" />
            <span v-if="sendForm.useTemplate">选择模板：</span>
            <a-select
              v-if="sendForm.useTemplate"
              v-model:value="sendForm.templateId"
              placeholder="请选择模板"
              style="width: 200px"
              @change="loadTemplate"
            >
              <a-select-option
                v-for="template in templateList"
                :key="template.templateId"
                :value="template.templateId"
              >
                {{ template.templateName }}
              </a-select-option>
            </a-select>
          </a-space>
        </a-form-item>
        <a-form-item label="标题" name="title">
          <a-input v-model:value="sendForm.title" placeholder="请输入通知标题" />
        </a-form-item>
        <a-form-item label="内容" name="content">
          <a-textarea
            v-model:value="sendForm.content"
            :rows="4"
            placeholder="请输入通知内容，支持变量如 {userName}, {deviceName} 等"
          />
        </a-form-item>
        <a-form-item label="定时发送">
          <a-space>
            <a-switch v-model:checked="sendForm.scheduled" />
            <a-date-picker
              v-if="sendForm.scheduled"
              v-model:value="sendForm.scheduledAt"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
            />
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 通知详情弹窗 -->
    <a-modal
      v-model:open="showDetailModal"
      title="通知详情"
      width="700px"
      :footer="null"
    >
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="请求ID" :span="2">
          {{ currentDetail.requestId }}
        </a-descriptions-item>
        <a-descriptions-item label="通知渠道">
          <a-tag :color="getChannelColor(currentDetail.channel)">
            {{ getNotificationChannelText(currentDetail.channel) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="优先级">
          <a-tag :color="getPriorityColor(currentDetail.priority)">
            {{ getNotificationPriorityText(currentDetail.priority) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-badge
            :status="getNotificationStatusColor(currentDetail.status) as any"
            :text="getNotificationStatusText(currentDetail.status)"
          />
        </a-descriptions-item>
        <a-descriptions-item label="重试次数">
          {{ currentDetail.retryCount || 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="标题" :span="2">
          {{ currentDetail.title || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="内容" :span="2">
          <div class="detail-content">{{ currentDetail.content }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="接收者" :span="2">
          <a-tag v-for="(recipient, index) in currentDetail.recipients" :key="index">
            {{ recipient.name || recipient.id }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatDateTime(currentDetail.createdAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="发送时间">
          {{ currentDetail.sentAt ? formatDateTime(currentDetail.sentAt) : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="完成时间">
          {{ currentDetail.completedAt ? formatDateTime(currentDetail.completedAt) : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="定时发送">
          {{ currentDetail.scheduledAt ? formatDateTime(currentDetail.scheduledAt) : '-' }}
        </a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.error" label="错误信息" :span="2">
          <a-alert :message="currentDetail.error" type="error" show-icon />
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 模板管理弹窗 -->
    <a-modal
      v-model:open="showTemplateModal"
      title="模板管理"
      width="900px"
      :footer="null"
    >
      <a-space style="margin-bottom: 16px">
        <a-button type="primary" @click="showAddTemplateModal = true">
          <template #icon><PlusOutlined /></template>
          新建模板
        </a-button>
        <a-button @click="loadTemplates">刷新</a-button>
      </a-space>
      <a-table
        :columns="templateColumns"
        :data-source="templateList"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'channel'">
            <a-tag>{{ getNotificationChannelText(record.channel) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 'ACTIVE' ? 'success' : 'default'"
              :text="record.status === 'ACTIVE' ? '已启用' : '已禁用'"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editTemplate(record)">
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                :disabled="record.status === 'ACTIVE'"
                @click="activateTemplate(record)"
              >
                启用
              </a-button>
              <a-button
                type="link"
                size="small"
                :danger="record.status === 'ACTIVE'"
                @click="deactivateTemplate(record)"
              >
                禁用
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  PlusOutlined,
  SendOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  RiseOutlined,
} from '@ant-design/icons-vue'
import {
  notificationApi,
  notificationTemplateApi,
  getNotificationChannelText,
  getNotificationStatusText,
  getNotificationPriorityText,
  getNotificationStatusColor,
  getNotificationPriorityColor,
  type NotificationChannel,
  type NotificationStatus,
  type NotificationPriority,
  type NotificationLog,
  type NotificationLogQuery,
  type NotificationRequest,
  type NotificationStatistics,
  type NotificationTemplate,
  type SendNotificationRequest,
} from '@/api/notification'
import { formatDateTime } from '@/utils/format'

// ==================== 数据定义 ====================

const loading = ref(false)
const dataSource = ref<NotificationLog[]>([])
const statistics = ref<NotificationStatistics>({
  tenantId: '',
  totalSent: 0,
  totalFailed: 0,
  totalPending: 0,
  successRate: 0,
  channelStats: [],
  dailyStats: [],
})

const searchForm = reactive<NotificationLogQuery>({
  page: 1,
  size: 10,
  channel: undefined,
  status: undefined,
  requestId: undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const columns = [
  { title: '请求ID', dataIndex: 'requestId', key: 'requestId', width: 180 },
  { title: '通知渠道', dataIndex: 'channel', key: 'channel', width: 120 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '接收者', dataIndex: 'recipients', key: 'recipients', width: 150 },
  { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]

const channelOptions = [
  { label: 'APP推送', value: 'APP_PUSH' },
  { label: '邮件', value: 'EMAIL' },
  { label: '短信', value: 'SMS' },
  { label: '站内信', value: 'IN_APP' },
  { label: 'Webhook', value: 'WEBHOOK' },
]

const statusOptions = [
  { label: '待发送', value: 'PENDING' },
  { label: '发送中', value: 'SENDING' },
  { label: '已发送', value: 'SENT' },
  { label: '发送失败', value: 'FAILED' },
  { label: '已取消', value: 'CANCELLED' },
]

const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '普通', value: 'NORMAL' },
  { label: '高', value: 'HIGH' },
  { label: '紧急', value: 'URGENT' },
]

// 发送通知表单
const showSendModal = ref(false)
const sendFormRef = ref()
const sendForm = reactive<SendNotificationRequest>({
  tenantId: '',
  channel: 'IN_APP' as NotificationChannel,
  recipients: [],
  priority: 'NORMAL' as NotificationPriority,
  title: '',
  content: '',
  useTemplate: false,
  templateId: undefined,
  recipientType: 'USER',
  scheduled: false,
  scheduledAt: undefined,
})

const sendRules = {
  channel: [{ required: true, message: '请选择通知渠道' }],
  recipients: [{ required: true, message: '请选择接收者' }],
  content: [{ required: true, message: '请输入通知内容' }],
}

const recipientOptions = ref([
  { label: '用户1', value: 'user-001' },
  { label: '用户2', value: 'user-002' },
])

// 通知详情
const showDetailModal = ref(false)
const currentDetail = ref<Partial<NotificationRequest>>({})

// 模板管理
const showTemplateModal = ref(false)
const showAddTemplateModal = ref(false)
const templateList = ref<NotificationTemplate[]>([])

const templateColumns = [
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName' },
  { title: '模板代码', dataIndex: 'templateCode', key: 'templateCode' },
  { title: '渠道', dataIndex: 'channel', key: 'channel', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 150 },
]

// ==================== 计算属性 ====================

const getChannelColor = (channel: NotificationChannel) => {
  const colorMap: Record<NotificationChannel, string> = {
    APP_PUSH: 'blue',
    EMAIL: 'green',
    SMS: 'orange',
    IN_APP: 'purple',
    WEBHOOK: 'cyan',
  }
  return colorMap[channel] || 'default'
}

const getPriorityColor = (priority: NotificationPriority) => {
  const colorMap: Record<NotificationPriority, string> = {
    LOW: 'default',
    NORMAL: 'blue',
    HIGH: 'orange',
    URGENT: 'red',
  }
  return colorMap[priority] || 'default'
}

const getChannelIcon = (channel: NotificationChannel) => {
  // 图标映射
  return SendOutlined
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadData()
  loadStatistics()
  loadTemplates()
})

// ==================== 方法 ====================

const loadData = async () => {
  loading.value = true
  try {
    const response = await notificationApi.queryNotificationLogs(searchForm)
    if (response.code === 200) {
      dataSource.value = response.data.items
      pagination.total = response.data.total
    }
  } catch (error) {
    message.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await notificationApi.getNotificationStatistics('')
    if (response.code === 200) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const loadTemplates = async () => {
  try {
    const response = await notificationTemplateApi.getTemplates({
      page: 1,
      size: 100,
      status: 'ACTIVE',
    })
    if (response.code === 200) {
      templateList.value = response.data.items
    }
  } catch (error) {
    message.error('加载模板列表失败')
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.channel = undefined
  searchForm.status = undefined
  searchForm.requestId = undefined
  handleSearch()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  searchForm.page = pag.current
  searchForm.size = pag.pageSize
  loadData()
}

const handleSendNotification = async () => {
  try {
    await sendFormRef.value?.validate()
    const request: SendNotificationRequest = {
      tenantId: sendForm.tenantId || '',
      channel: sendForm.channel,
      recipients: sendForm.recipients.map(r => ({
        type: sendForm.recipientType as any,
        id: typeof r === 'string' ? r : r.value,
      })),
      priority: sendForm.priority,
      title: sendForm.title,
      content: sendForm.content,
      templateId: sendForm.templateId,
      scheduledAt: sendForm.scheduled ? sendForm.scheduledAt?.format('YYYY-MM-DD HH:mm:ss') : undefined,
    }
    const response = await notificationApi.sendNotification(request)
    if (response.code === 200) {
      message.success('通知发送成功')
      showSendModal.value = false
      resetSendForm()
      loadData()
      loadStatistics()
    }
  } catch (error: any) {
    if (error.errorFields) {
      message.warning('请填写完整信息')
    } else {
      message.error('发送失败')
    }
  }
}

const resetSendForm = () => {
  sendFormRef.value?.resetFields()
  sendForm.recipients = []
  sendForm.useTemplate = false
  sendForm.templateId = undefined
  sendForm.scheduled = false
  sendForm.scheduledAt = undefined
}

const loadTemplate = async (templateId: string) => {
  try {
    const response = await notificationTemplateApi.getTemplate(templateId)
    if (response.code === 200) {
      const template = response.data
      sendForm.title = template.title
      sendForm.content = template.content
    }
  } catch (error) {
    message.error('加载模板失败')
  }
}

const viewDetail = (record: NotificationLog) => {
  currentDetail.value = { ...record }
  showDetailModal.value = true
}

const retryNotification = async (record: NotificationLog) => {
  try {
    const response = await notificationApi.retryNotification(record.requestId)
    if (response.code === 200) {
      message.success('重试成功')
      loadData()
    }
  } catch (error) {
    message.error('重试失败')
  }
}

const cancelNotification = async (record: NotificationLog) => {
  try {
    const response = await notificationApi.cancelNotification(record.requestId)
    if (response.code === 200) {
      message.success('取消成功')
      loadData()
    }
  } catch (error) {
    message.error('取消失败')
  }
}

const editTemplate = (template: NotificationTemplate) => {
  // 编辑模板逻辑
  console.log('编辑模板', template)
}

const activateTemplate = async (template: NotificationTemplate) => {
  try {
    const response = await notificationTemplateApi.activateTemplate(template.templateId)
    if (response.code === 200) {
      message.success('启用成功')
      loadTemplates()
    }
  } catch (error) {
    message.error('启用失败')
  }
}

const deactivateTemplate = async (template: NotificationTemplate) => {
  try {
    const response = await notificationTemplateApi.deactivateTemplate(template.templateId)
    if (response.code === 200) {
      message.success('禁用成功')
      loadTemplates()
    }
  } catch (error) {
    message.error('禁用失败')
  }
}
</script>

<style scoped>
.notification-management {
  padding: 0;
}

.search-section {
  margin-bottom: 16px;
}

.stats-row {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.content-text {
  display: inline-block;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.detail-content {
  max-height: 100px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
