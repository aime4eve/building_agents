<template>
  <div class="device-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">设备管理</h2>
    </div>

    <!-- 搜索表单 -->
    <a-card class="search-form-card">
      <a-form layout="inline" :model="queryParams" class="search-form">
        <a-form-item label="设备名称">
          <a-input
            v-model:value="queryParams.keyword"
            placeholder="请输入设备名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="设备类型">
          <a-select
            v-model:value="queryParams.type"
            placeholder="请选择设备类型"
            allow-clear
            style="width: 200px"
          >
            <a-select-option value="WATER_METER">水表</a-select-option>
            <a-select-option value="ELECTRIC_METER">电表</a-select-option>
            <a-select-option value="TEMPERATURE_SENSOR">温度传感器</a-select-option>
            <a-select-option value="HUMIDITY_SENSOR">湿度传感器</a-select-option>
            <a-select-option value="SMOKE_DETECTOR">烟雾探测器</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="设备状态">
          <a-select
            v-model:value="queryParams.status"
            placeholder="请选择设备状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="ONLINE">在线</a-select-option>
            <a-select-option value="OFFLINE">离线</a-select-option>
            <a-select-option value="FAULT">故障</a-select-option>
            <a-select-option value="MAINTENANCE">维护中</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch" :loading="loading">
              <SearchOutlined />
              查询
            </a-button>
            <a-button @click="handleReset">
              <ReloadOutlined />
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 设备列表 -->
    <a-card class="table-card">
      <template #title>
        <a-space>
          <span>设备列表</span>
          <a-tag v-if="pagination.total > 0" color="blue">
            共 {{ pagination.total }} 台设备
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined />
            新增设备
          </a-button>
          <a-button @click="handleBatchControl" :disabled="selectedRowKeys.length === 0">
            <ApiOutlined />
            批量控制
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="deviceList"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: handleSelectionChange,
        }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 设备状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <!-- 设备类型 -->
          <template v-else-if="column.key === 'type'">
            <a-tag>{{ getDeviceTypeText(record.type) }}</a-tag>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record as Device)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record as Device)">
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleControl(record as Device)"
                :disabled="record.status !== 'ONLINE'"
              >
                控制
              </a-button>
              <a-popconfirm
                title="确定要删除该设备吗？"
                @confirm="handleDelete(record as Device)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { TableProps } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  ApiOutlined,
} from '@ant-design/icons-vue'
import type { Device, DeviceQuery } from '@/types'

const router = useRouter()

// 查询参数
const queryParams = reactive<DeviceQuery>({
  page: 1,
  size: 10,
  keyword: '',
  type: undefined,
  status: undefined,
})

// 设备列表
const deviceList = ref<Device[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 选中行
const selectedRowKeys = ref<string[]>([])

// 表格列
const columns: TableProps['columns'] = [
  {
    title: '设备SN',
    dataIndex: 'sn',
    key: 'sn',
    width: 150,
  },
  {
    title: '设备名称',
    dataIndex: 'name',
    key: 'name',
    width: 150,
  },
  {
    title: '设备类型',
    dataIndex: 'type',
    key: 'type',
    width: 120,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '所属空间',
    dataIndex: 'spaceId',
    key: 'spaceId',
    width: 120,
  },
  {
    title: '厂商',
    dataIndex: ['model', 'manufacturer'],
    key: 'manufacturer',
    width: 120,
  },
  {
    title: '型号',
    dataIndex: ['model', 'model'],
    key: 'model',
    width: 120,
  },
  {
    title: '最后在线',
    dataIndex: 'lastOnlineAt',
    key: 'lastOnlineAt',
    width: 150,
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right' as const,
    width: 200,
  },
]

// 获取设备列表
const fetchDeviceList = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取设备列表
    // 模拟数据
    await new Promise((resolve) => setTimeout(resolve, 500))

    const mockData: Device[] = Array.from({ length: 10 }, (_, i) => ({
      id: `device-${i + 1}`,
      sn: `SN${String(i + 1).padStart(6, '0')}`,
      name: `设备${i + 1}`,
      type: ['ELECTRIC_METER', 'TEMPERATURE_SENSOR', 'HUMIDITY_SENSOR'][i % 3] as any,
      status: ['ONLINE', 'OFFLINE', 'FAULT'][i % 3] as any,
      model: {
        manufacturer: '华宽通',
        model: 'HK-001',
        firmwareVersion: '1.0.0',
      },
      spaceId: i % 2 === 0 ? 'space-1' : undefined,
      tenantId: 'tenant-1',
      lastOnlineAt: new Date(Date.now() - i * 60000).toISOString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }))

    deviceList.value = mockData
    pagination.total = 100
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchDeviceList()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.type = undefined
  queryParams.status = undefined
  handleSearch()
}

// 表格变化
const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  fetchDeviceList()
}

// 选中行变化
const handleSelectionChange = (keys: (string | number)[]) => {
  selectedRowKeys.value = keys as string[]
}

// 添加设备
const handleAdd = () => {
  message.info('打开新增设备对话框')
}

// 查看设备
const handleView = (record: Device) => {
  router.push(`/device/${record.id}`)
}

// 编辑设备
const handleEdit = (record: Device) => {
  message.info(`编辑设备: ${record.name}`)
}

// 控制设备
const handleControl = (record: Device) => {
  message.info(`控制设备: ${record.name}`)
}

// 删除设备
const handleDelete = async (record: Device) => {
  message.success(`删除设备: ${record.name}`)
  await fetchDeviceList()
}

// 批量控制
const handleBatchControl = () => {
  message.info(`批量控制 ${selectedRowKeys.value.length} 台设备`)
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    ONLINE: 'success',
    OFFLINE: 'default',
    FAULT: 'error',
    MAINTENANCE: 'warning',
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    ONLINE: '在线',
    OFFLINE: '离线',
    FAULT: '故障',
    MAINTENANCE: '维护中',
  }
  return textMap[status] || status
}

// 获取设备类型文本
const getDeviceTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    WATER_METER: '水表',
    ELECTRIC_METER: '电表',
    TEMPERATURE_SENSOR: '温度传感器',
    HUMIDITY_SENSOR: '湿度传感器',
    SMOKE_DETECTOR: '烟雾探测器',
  }
  return typeMap[type] || type
}

onMounted(() => {
  fetchDeviceList()
})
</script>

<style scoped>
.device-list-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}

.search-form-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.search-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.table-card {
  border-radius: 8px;
}
</style>
