<template>
  <div class="space-detail-container">
    <a-page-header
      title="空间详情"
      @back="handleBack"
      class="page-header"
    >
      <template #extra>
        <a-space>
          <a-button @click="handleEdit">
            <EditOutlined />
            编辑
          </a-button>
          <a-button @click="handleAddChild">
            <PlusOutlined />
            添加子空间
          </a-button>
          <a-button danger @click="handleDelete">
            <DeleteOutlined />
            删除
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <a-spin :spinning="loading">
      <a-row :gutter="16">
        <!-- 基本信息 -->
        <a-col :xs="24" :lg="12">
          <a-card title="基本信息" class="info-card">
            <a-descriptions bordered :column="1">
              <a-descriptions-item label="空间名称">
                {{ spaceInfo?.name }}
              </a-descriptions-item>
              <a-descriptions-item label="空间编码">
                {{ spaceInfo?.code }}
              </a-descriptions-item>
              <a-descriptions-item label="空间类型">
                <a-tag :color="getTypeColor(spaceInfo?.type)">
                  {{ getTypeText(spaceInfo?.type) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-badge
                  :status="getStatusBadge(spaceInfo?.status)"
                  :text="getStatusText(spaceInfo?.status)"
                />
              </a-descriptions-item>
              <a-descriptions-item label="上级空间">
                {{ parentSpaceName || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="租户ID">
                {{ spaceInfo?.tenantId }}
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                {{ formatTime(spaceInfo?.createdAt) }}
              </a-descriptions-item>
              <a-descriptions-item label="更新时间">
                {{ formatTime(spaceInfo?.updatedAt) }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>

        <!-- 统计信息 -->
        <a-col :xs="24" :lg="12">
          <a-card title="统计信息" class="info-card">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-statistic
                  title="直接子空间"
                  :value="stats.childCount"
                  :loading="statsLoading"
                >
                  <template #prefix>
                    <ApartmentOutlined />
                  </template>
                </a-statistic>
              </a-col>
              <a-col :span="12">
                <a-statistic
                  title="设备数量"
                  :value="stats.deviceCount"
                  :loading="statsLoading"
                >
                  <template #prefix>
                    <AppstoreOutlined />
                  </template>
                </a-statistic>
              </a-col>
            </a-row>
            <a-divider />
            <a-row :gutter="16">
              <a-col :span="12">
                <a-statistic
                  title="在线设备"
                  :value="stats.onlineDeviceCount"
                  :value-style="{ color: '#3f8600' }"
                  :loading="statsLoading"
                />
              </a-col>
              <a-col :span="12">
                <a-statistic
                  title="离线设备"
                  :value="stats.deviceCount - stats.onlineDeviceCount"
                  :value-style="{ color: '#cf1322' }"
                  :loading="statsLoading"
                />
              </a-col>
            </a-row>
          </a-card>
        </a-col>
      </a-row>

      <!-- 空间设备 -->
      <a-row :gutter="16" class="mt-16">
        <a-col :span="24">
          <a-card title="空间设备" class="device-card">
            <template #extra>
              <a-space>
                <a-tag color="blue">共 {{ devicePagination.total }} 台设备</a-tag>
                <a-button type="primary" size="small" @click="handleBindDevice">
                  <LinkOutlined />
                  绑定设备
                </a-button>
              </a-space>
            </template>

            <a-table
              :columns="deviceColumns"
              :data-source="deviceList"
              :loading="deviceLoading"
              :pagination="devicePagination"
              row-key="id"
              @change="handleDeviceTableChange"
            >
              <template #bodyCell="{ column, record }">
                <!-- 设备状态 -->
                <template v-if="column.key === 'status'">
                  <a-badge
                    :status="record.status === 'ONLINE' ? 'success' : 'default'"
                    :text="record.status === 'ONLINE' ? '在线' : '离线'"
                  />
                </template>

                <!-- 设备类型 -->
                <template v-else-if="column.key === 'type'">
                  <a-tag>{{ getDeviceTypeText(record.type) }}</a-tag>
                </template>

                <!-- 操作 -->
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="handleViewDevice(record)">
                      查看
                    </a-button>
                    <a-popconfirm
                      title="确定要解绑该设备吗？"
                      @confirm="handleUnbindDevice(record)"
                    >
                      <a-button type="link" size="small" danger>解绑</a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>

      <!-- 子空间列表 -->
      <a-row :gutter="16" class="mt-16">
        <a-col :span="24">
          <a-card title="子空间列表" class="child-card">
            <template #extra>
              <a-tag color="green">共 {{ childSpaceList.length }} 个子空间</a-tag>
            </template>

            <a-table
              :columns="childSpaceColumns"
              :data-source="childSpaceList"
              :loading="childLoading"
              :pagination="false"
              row-key="id"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <!-- 空间类型 -->
                <template v-if="column.key === 'type'">
                  <a-tag :color="getTypeColor(record.type)">
                    {{ getTypeText(record.type) }}
                  </a-tag>
                </template>

                <!-- 状态 -->
                <template v-else-if="column.key === 'status'">
                  <a-badge
                    :status="getStatusBadge(record.status)"
                    :text="getStatusText(record.status)"
                  />
                </template>

                <!-- 操作 -->
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="router.push(`/spaces/${record.id}`)">
                      查看
                    </a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>

    <!-- 绑定设备弹窗 -->
    <a-modal
      v-model:open="bindDeviceModalVisible"
      title="绑定设备"
      :width="800"
      @ok="handleBindDeviceOk"
    >
      <a-form layout="inline" class="mb-16">
        <a-form-item label="设备名称">
          <a-input
            v-model:value="deviceQueryParams.keyword"
            placeholder="请输入设备名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="fetchUnbindDevices">
            <SearchOutlined />
            查询
          </a-button>
        </a-form-item>
      </a-form>

      <a-table
        :columns="unbindDeviceColumns"
        :data-source="unbindDeviceList"
        :loading="unbindDeviceLoading"
        :pagination="{ pageSize: 5 }"
        :row-selection="{
          selectedRowKeys: selectedDeviceKeys,
          onChange: handleDeviceSelectionChange,
        }"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge
              :status="record.status === 'ONLINE' ? 'success' : 'default'"
              :text="record.status === 'ONLINE' ? '在线' : '离线'"
            />
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  ApartmentOutlined,
  AppstoreOutlined,
  LinkOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { spaceApi } from '@/api/space'
import type { Space, Device } from '@/types'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const spaceInfo = ref<Space | null>(null)
const parentSpaceName = ref('')

// 统计信息
const statsLoading = ref(false)
const stats = reactive({
  childCount: 0,
  deviceCount: 0,
  onlineDeviceCount: 0,
})

// 设备列表
const deviceList = ref<Device[]>([])
const deviceLoading = ref(false)
const devicePagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`,
})

const deviceColumns = [
  { title: '设备SN', dataIndex: 'sn', key: 'sn', width: 150 },
  { title: '设备名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '设备类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '最后在线', dataIndex: 'lastOnlineAt', key: 'lastOnlineAt', width: 150 },
  { title: '操作', key: 'action', fixed: 'right' as const, width: 150 },
]

// 子空间列表
const childSpaceList = ref<Space[]>([])
const childLoading = ref(false)

const childSpaceColumns = [
  { title: '空间编码', dataIndex: 'code', key: 'code', width: 150 },
  { title: '空间名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '空间类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', fixed: 'right' as const, width: 100 },
]

// 绑定设备弹窗
const bindDeviceModalVisible = ref(false)
const unbindDeviceList = ref<Device[]>([])
const unbindDeviceLoading = ref(false)
const selectedDeviceKeys = ref<string[]>([])
const deviceQueryParams = reactive({ keyword: '' })

const unbindDeviceColumns = [
  { title: '设备SN', dataIndex: 'sn', key: 'sn', width: 150 },
  { title: '设备名称', dataIndex: 'name', key: 'name' },
  { title: '设备类型', dataIndex: 'type', key: 'type' },
  { title: '状态', dataIndex: 'status', key: 'status' },
]

// 获取空间详情
const fetchSpaceDetail = async () => {
  const spaceId = route.params.id as string
  loading.value = true

  try {
    const response = await spaceApi.getSpace(spaceId)
    spaceInfo.value = response.data

    // 并行获取统计信息、设备和子空间
    await Promise.all([
      fetchSpaceStats(spaceId),
      fetchSpaceDevices(spaceId),
      fetchChildSpaces(spaceId),
    ])
  } catch (error) {
    message.error('获取空间详情失败')
  } finally {
    loading.value = false
  }
}

// 获取空间统计信息
const fetchSpaceStats = async (spaceId: string) => {
  statsLoading.value = true
  try {
    const response = await spaceApi.getSpaceStats(spaceId)
    Object.assign(stats, response.data)
  } catch (error) {
    console.error('获取统计信息失败:', error)
  } finally {
    statsLoading.value = false
  }
}

// 获取空间设备
const fetchSpaceDevices = async (spaceId: string) => {
  deviceLoading.value = true
  try {
    const response = await spaceApi.getSpaceDevices(spaceId, {
      page: devicePagination.current,
      size: devicePagination.pageSize,
    })
    deviceList.value = response.data.items
    devicePagination.total = response.data.total
  } catch (error) {
    console.error('获取空间设备失败:', error)
  } finally {
    deviceLoading.value = false
  }
}

// 获取子空间列表
const fetchChildSpaces = async (spaceId: string) => {
  childLoading.value = true
  try {
    const response = await spaceApi.getSpaces({
      page: 1,
      size: 100,
      parentId: spaceId,
    })
    childSpaceList.value = response.data.items
    stats.childCount = response.data.total
  } catch (error) {
    console.error('获取子空间失败:', error)
  } finally {
    childLoading.value = false
  }
}

// 设备表格变化
const handleDeviceTableChange = (pag: any) => {
  devicePagination.current = pag.current
  devicePagination.pageSize = pag.pageSize
  if (spaceInfo.value) {
    fetchSpaceDevices(spaceInfo.value.id)
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  if (spaceInfo.value) {
    router.push(`/spaces/${spaceInfo.value.id}/edit`)
  }
}

// 添加子空间
const handleAddChild = () => {
  if (spaceInfo.value) {
    router.push(`/spaces/create?parentId=${spaceInfo.value.id}`)
  }
}

// 删除空间
const handleDelete = () => {
  if (!spaceInfo.value) return

  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该空间吗？此操作不可恢复。',
    onOk: async () => {
      try {
        await spaceApi.deleteSpace(spaceInfo.value!.id)
        message.success('删除成功')
        router.push('/spaces')
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 绑定设备
const handleBindDevice = () => {
  bindDeviceModalVisible.value = true
  fetchUnbindDevices()
}

// 获取未绑定设备列表
const fetchUnbindDevices = async () => {
  unbindDeviceLoading.value = true
  try {
    // TODO: 调用API获取未绑定设备列表
    // 模拟数据
    await new Promise((resolve) => setTimeout(resolve, 500))
    unbindDeviceList.value = []
  } catch (error) {
    console.error('获取设备列表失败:', error)
  } finally {
    unbindDeviceLoading.value = false
  }
}

// 设备选择变化
const handleDeviceSelectionChange = (keys: (string | number)[]) => {
  selectedDeviceKeys.value = keys as string[]
}

// 绑定设备确认
const handleBindDeviceOk = async () => {
  if (selectedDeviceKeys.value.length === 0) {
    message.warning('请选择要绑定的设备')
    return
  }

  try {
    // TODO: 调用API绑定设备
    message.success('绑定成功')
    bindDeviceModalVisible.value = false
    selectedDeviceKeys.value = []
    if (spaceInfo.value) {
      await Promise.all([
        fetchSpaceDevices(spaceInfo.value.id),
        fetchSpaceStats(spaceInfo.value.id),
      ])
    }
  } catch (error) {
    message.error('绑定失败')
  }
}

// 查看设备
const handleViewDevice = (device: Device) => {
  router.push(`/device/${device.id}`)
}

// 解绑设备
const handleUnbindDevice = async (device: Device) => {
  try {
    // TODO: 调用API解绑设备
    message.success('解绑成功')
    if (spaceInfo.value) {
      await Promise.all([
        fetchSpaceDevices(spaceInfo.value.id),
        fetchSpaceStats(spaceInfo.value.id),
      ])
    }
  } catch (error) {
    message.error('解绑失败')
  }
}

// 格式化时间
const formatTime = (time?: string) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 获取类型颜色
const getTypeColor = (type?: string) => {
  if (!type) return 'default'
  const colorMap: Record<string, string> = {
    CAMPUS: 'blue',
    BUILDING: 'green',
    FLOOR: 'orange',
    ROOM: 'purple',
  }
  return colorMap[type] || 'default'
}

// 获取类型文本
const getTypeText = (type?: string) => {
  if (!type) return '-'
  const textMap: Record<string, string> = {
    CAMPUS: '园区',
    BUILDING: '楼栋',
    FLOOR: '楼层',
    ROOM: '房间',
  }
  return textMap[type] || type
}

// 获取状态徽标
const getStatusBadge = (status?: string) => {
  if (!status) return 'default'
  const badgeMap: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'default',
    MAINTENANCE: 'warning',
  }
  return badgeMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status?: string) => {
  if (!status) return '-'
  const textMap: Record<string, string> = {
    ACTIVE: '正常',
    INACTIVE: '停用',
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
  fetchSpaceDetail()
})
</script>

<style scoped>
.space-detail-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  background: #fff;
  border-radius: 8px;
  padding: 16px 24px;
  margin-bottom: 16px;
}

.info-card,
.device-card,
.child-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.mt-16 {
  margin-top: 16px;
}

.mb-16 {
  margin-bottom: 16px;
}
</style>
