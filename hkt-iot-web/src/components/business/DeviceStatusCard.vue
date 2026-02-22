<template>
  <div class="device-status-card">
    <a-card :bordered="false" :hoverable="true" :class="`status-${device.status.toLowerCase()}`">
      <template #cover>
        <div class="device-icon">
          <span class="icon">{{ deviceIcon }}</span>
          <a-badge
            :status="statusDot"
            :text="statusText"
            class="status-badge"
          />
        </div>
      </template>

      <a-card-meta>
        <template #title>
          <div class="device-name">
            {{ device.name || device.sn }}
            <a-tag v-if="isOnline" color="success" size="small">在线</a-tag>
            <a-tag v-else color="default" size="small">离线</a-tag>
          </div>
        </template>
        <template #description>
          <div class="device-info">
            <div class="info-item">
              <span class="label">设备SN:</span>
              <span class="value">{{ device.sn }}</span>
            </div>
            <div class="info-item">
              <span class="label">类型:</span>
              <span class="value">{{ deviceTypeText }}</span>
            </div>
            <div class="info-item" v-if="device.spaceId">
              <span class="label">位置:</span>
              <span class="value">{{ device.spaceName || '-' }}</span>
            </div>
          </div>
        </template>
      </a-card-meta>

      <!-- 设备属性 -->
      <div v-if="showProperties && properties.length > 0" class="device-properties">
        <a-divider size="small">实时数据</a-divider>
        <a-row :gutter="8">
          <a-col
            v-for="prop in displayedProperties"
            :key="prop.name"
            :span="12"
          >
            <div class="property-item">
              <span class="property-label">{{ prop.label }}:</span>
              <span class="property-value" :class="getPropertyClass(prop)">
                {{ formatPropertyValue(prop) }}
              </span>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 操作按钮 -->
      <template #actions v-if="showActions">
        <a-button type="text" size="small" @click="$emit('detail', device)">
          <EyeOutlined /> 详情
        </a-button>
        <a-button
          v-if="isController"
          type="text"
          size="small"
          @click="$emit('control', device)"
        >
          <ControlOutlined /> 控制
        </a-button>
        <a-button type="text" size="small" @click="$emit('chart', device)">
          <LineChartOutlined /> 曲线
        </a-button>
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  EyeOutlined,
  ControlOutlined,
  LineChartOutlined,
} from '@ant-design/icons-vue'
import {
  getDeviceTypeText,
  getDeviceStatusText,
  getDeviceStatusColor,
  getDeviceTypeIcon,
  isController,
  formatDeviceValue,
  getTelemetryUnit,
} from '@/utils/device'
import type { Device } from '@/types'

interface Props {
  device: Device & {
    spaceName?: string
    properties?: Record<string, any>
  }
  showProperties?: boolean
  showActions?: boolean
  maxProperties?: number
}

const props = withDefaults(defineProps<Props>(), {
  showProperties: true,
  showActions: true,
  maxProperties: 4,
})

defineEmits<{
  (e: 'detail', device: Device): void
  (e: 'control', device: Device): void
  (e: 'chart', device: Device): void
}>()

const deviceTypeText = computed(() => getDeviceTypeText(props.device.type))
const statusText = computed(() => getDeviceStatusText(props.device.status))
const deviceIcon = computed(() => getDeviceTypeIcon(props.device.type))
const isOnline = computed(() => props.device.status === 'ONLINE')
const statusDot = computed(() => {
  const colorMap: Record<string, string> = {
    ONLINE: 'success',
    OFFLINE: 'default',
    FAULT: 'error',
    MAINTENANCE: 'warning',
  }
  return colorMap[props.device.status] || 'default'
})

// 设备属性
const properties = computed(() => {
  const props = props.device.properties || {}
  return Object.entries(props).map(([key, value]) => ({
    key,
    label: getPropertyLabel(key),
    value,
    unit: getTelemetryUnit(key),
  }))
})

// 显示的属性
const displayedProperties = computed(() => {
  return properties.value.slice(0, props.maxProperties)
})

// 获取属性标签
function getPropertyLabel(key: string): string {
  const labelMap: Record<string, string> = {
    temperature: '温度',
    humidity: '湿度',
    voltage: '电压',
    current: '电流',
    power: '功率',
    energy: '能耗',
    battery: '电池',
    rssi: '信号',
  }
  return labelMap[key] || key
}

// 格式化属性值
function formatPropertyValue(prop: any): string {
  return formatDeviceValue(prop.value, prop.unit)
}

// 获取属性值样式类
function getPropertyClass(prop: any): string {
  const { key, value } = prop
  if (key === 'battery' && value < 20) return 'warning'
  if (key === 'temperature' && (value > 35 || value < 15)) return 'warning'
  return ''
}
</script>

<style scoped>
.device-status-card {
  height: 100%;
}

.device-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.device-icon .icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.status-badge {
  margin-top: 8px;
}

.device-name {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.device-info {
  margin-top: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 12px;
}

.info-item .label {
  color: #8c8c8c;
}

.info-item .value {
  color: #262626;
  font-weight: 500;
}

.device-properties {
  margin-top: 12px;
}

.property-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 12px;
}

.property-label {
  color: #8c8c8c;
}

.property-value {
  font-weight: 500;
}

.property-value.warning {
  color: #ff4d4f;
}

/* 状态样式 */
.status-online :deep(.ant-card-body) {
  border-left: 3px solid #52c41a;
}

.status-offline :deep(.ant-card-body) {
  border-left: 3px solid #8c8c8c;
}

.status-fault :deep(.ant-card-body) {
  border-left: 3px solid #ff4d4f;
}

.status-maintenance :deep(.ant-card-body) {
  border-left: 3px solid #faad14;
}
</style>
