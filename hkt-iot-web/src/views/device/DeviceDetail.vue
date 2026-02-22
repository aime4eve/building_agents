<template>
  <div class="device-detail-container">
    <a-page-header
      title="设备详情"
      @back="handleBack"
      class="page-header"
    >
      <template #extra>
        <a-space>
          <a-button @click="handleEdit">编辑</a-button>
          <a-button
            type="primary"
            @click="handleControl"
            :disabled="deviceInfo?.status !== 'ONLINE'"
          >
            <ApiOutlined />
            控制设备
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
              <a-descriptions-item label="设备SN">
                {{ deviceInfo?.sn }}
              </a-descriptions-item>
              <a-descriptions-item label="设备名称">
                {{ deviceInfo?.name }}
              </a-descriptions-item>
              <a-descriptions-item label="设备类型">
                <a-tag>{{ getDeviceTypeText(deviceInfo?.type) }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="设备状态">
                <a-tag :color="getStatusColor(deviceInfo?.status)">
                  {{ getStatusText(deviceInfo?.status) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="所属空间">
                {{ deviceInfo?.spaceId || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="租户ID">
                {{ deviceInfo?.tenantId }}
              </a-descriptions-item>
              <a-descriptions-item label="激活时间">
                {{ formatTime(deviceInfo?.activatedAt) }}
              </a-descriptions-item>
              <a-descriptions-item label="最后在线">
                {{ formatTime(deviceInfo?.lastOnlineAt) }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>

        <!-- 设备模型 -->
        <a-col :xs="24" :lg="12">
          <a-card title="设备模型" class="info-card">
            <a-descriptions bordered :column="1">
              <a-descriptions-item label="厂商">
                {{ deviceInfo?.model.manufacturer }}
              </a-descriptions-item>
              <a-descriptions-item label="型号">
                {{ deviceInfo?.model.model }}
              </a-descriptions-item>
              <a-descriptions-item label="固件版本">
                {{ deviceInfo?.model.firmwareVersion }}
              </a-descriptions-item>
              <a-descriptions-item label="License ID">
                {{ deviceInfo?.licenseId || '-' }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>
      </a-row>

      <!-- 设备数据 -->
      <a-row :gutter="16" class="mt-16">
        <a-col :span="24">
          <a-card title="实时数据" class="data-card">
            <div ref="telemetryChartRef" style="height: 300px"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 设备事件 -->
      <a-row :gutter="16" class="mt-16">
        <a-col :span="24">
          <a-card title="设备事件" class="event-card">
            <a-timeline>
              <a-timeline-item v-for="event in deviceEvents" :key="event.id">
                <p>{{ event.time }} - {{ event.message }}</p>
                <a-tag :color="getEventColor(event.type)">{{ event.type }}</a-tag>
              </a-timeline-item>
            </a-timeline>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { ApiOutlined } from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import dayjs from 'dayjs'
import type { Device } from '@/types'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const deviceInfo = ref<Device | null>(null)
const telemetryChartRef = ref<HTMLElement>()
let telemetryChart: ECharts | null = null

// 设备事件
const deviceEvents = ref([
  {
    id: '1',
    time: '2024-02-20 10:30:00',
    message: '设备上线',
    type: 'INFO',
  },
  {
    id: '2',
    time: '2024-02-20 09:15:00',
    message: '数据上报成功',
    type: 'SUCCESS',
  },
  {
    id: '3',
    time: '2024-02-20 08:00:00',
    message: '固件升级完成',
    type: 'INFO',
  },
])

// 获取设备详情
const fetchDeviceDetail = async () => {
  const deviceId = route.params.id as string
  loading.value = true

  try {
    // TODO: 调用API获取设备详情
    // 模拟数据
    await new Promise((resolve) => setTimeout(resolve, 500))

    deviceInfo.value = {
      id: deviceId,
      sn: 'SN000001',
      name: '1号楼电表',
      type: 'ELECTRIC_METER',
      status: 'ONLINE',
      model: {
        manufacturer: '华宽通',
        model: 'HK-ELECTRIC-001',
        firmwareVersion: '1.2.0',
      },
      spaceId: 'space-1-1',
      tenantId: 'tenant-1',
      licenseId: 'license-1',
      activatedAt: '2024-01-01T00:00:00Z',
      lastOnlineAt: new Date().toISOString(),
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: new Date().toISOString(),
    }

    // 初始化图表
    initTelemetryChart()
  } finally {
    loading.value = false
  }
}

// 初始化遥测数据图表
const initTelemetryChart = () => {
  if (!telemetryChartRef.value) return

  telemetryChart = echarts.init(telemetryChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 24 }, (_, i) => `${i}:00`),
    },
    yAxis: {
      type: 'value',
      name: '功率 (kW)',
    },
    series: [
      {
        name: '功率',
        type: 'line',
        smooth: true,
        data: Array.from({ length: 24 }, () => Math.random() * 50 + 50),
        itemStyle: {
          color: '#1890ff',
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' },
          ]),
        },
      },
    ],
  }

  telemetryChart.setOption(option)
}

// 返回
const handleBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  message.info('打开编辑对话框')
}

// 控制设备
const handleControl = () => {
  message.info('打开控制面板')
}

// 格式化时间
const formatTime = (time?: string) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 获取状态颜色
const getStatusColor = (status?: string) => {
  if (!status) return 'default'
  const colorMap: Record<string, string> = {
    ONLINE: 'success',
    OFFLINE: 'default',
    FAULT: 'error',
    MAINTENANCE: 'warning',
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status?: string) => {
  if (!status) return '-'
  const textMap: Record<string, string> = {
    ONLINE: '在线',
    OFFLINE: '离线',
    FAULT: '故障',
    MAINTENANCE: '维护中',
  }
  return textMap[status] || status
}

// 获取设备类型文本
const getDeviceTypeText = (type?: string) => {
  if (!type) return '-'
  const typeMap: Record<string, string> = {
    WATER_METER: '水表',
    ELECTRIC_METER: '电表',
    TEMPERATURE_SENSOR: '温度传感器',
    HUMIDITY_SENSOR: '湿度传感器',
    SMOKE_DETECTOR: '烟雾探测器',
  }
  return typeMap[type] || type
}

// 获取事件颜色
const getEventColor = (type: string) => {
  const colorMap: Record<string, string> = {
    INFO: 'blue',
    SUCCESS: 'green',
    WARNING: 'orange',
    ERROR: 'red',
  }
  return colorMap[type] || 'default'
}

const handleResize = () => {
  telemetryChart?.resize()
}

onMounted(() => {
  fetchDeviceDetail()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  telemetryChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.device-detail-container {
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
.data-card,
.event-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.mt-16 {
  margin-top: 16px;
}
</style>
