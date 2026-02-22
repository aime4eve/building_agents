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

<style scoped lang="less">
@primary-color: #1890ff;
@success-color: #52c41a;
@warning-color: #faad14;
@error-color: #ff4d4f;
@border-radius: 16px;
@transition-base: all 0.3s ease;

.device-status-card {
  height: 100%;

  :deep(.ant-card) {
    border-radius: @border-radius;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: @transition-base;
    overflow: hidden;

    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      transform: translateY(-4px);
    }
  }

  // 不同状态的卡片左边框
  &.status-online :deep(.ant-card-body) {
    border-left: 4px solid @success-color;
  }

  &.status-offline :deep(.ant-card-body) {
    border-left: 4px solid #8c8c8c;
  }

  &.status-fault :deep(.ant-card-body) {
    border-left: 4px solid @error-color;
  }

  &.status-maintenance :deep(.ant-card-body) {
    border-left: 4px solid @warning-color;
  }
}

// 设备图标区域
.device-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    animation: shimmer 3s infinite;
  }

  .icon {
    font-size: 56px;
    margin-bottom: 12px;
    color: #fff;
    filter: drop-shadow(0 2px 8px rgba(0,0,0,0.2));
    z-index: 1;
  }

  .status-badge {
    z-index: 1;

    :deep(.ant-badge-status-text) {
      color: #fff;
      font-weight: 500;
      font-size: 13px;
    }
  }
}

@keyframes shimmer {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(180deg); }
}

// 设备名称
.device-name {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;

  :deep(.ant-tag) {
    border-radius: 6px;
    padding: 2px 10px;
    font-weight: 500;
  }
}

// 设备信息
.device-info {
  margin-top: 12px;

  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #f5f5f5;
    transition: @transition-base;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #fafafa;
      padding-left: 8px;
      padding-right: 8px;
      margin: 0 -8px;
      border-radius: 6px;
    }

    .label {
      color: #8c8c8c;
      font-size: 13px;
    }

    .value {
      color: #262626;
      font-weight: 500;
      font-size: 13px;
    }
  }
}

// 设备属性
.device-properties {
  margin-top: 16px;
  padding-top: 16px;

  :deep(.ant-divider) {
    margin: 0 0 12px 0;
    border-color: #e8e8e8;
    font-size: 13px;
    color: #8c8c8c;
  }

  .property-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
    border-radius: 8px;
    margin-bottom: 8px;
    transition: @transition-base;

    &:hover {
      background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
      transform: translateX(4px);
    }

    .property-label {
      color: #8c8c8c;
      font-size: 12px;
    }

    .property-value {
      font-weight: 600;
      font-size: 14px;
      color: #262626;

      &.warning {
        color: @error-color;
      }
    }
  }
}

// 操作按钮
:deep(.ant-card-actions) {
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
  padding: 12px 0;

  .ant-btn {
    border-radius: 8px;
    font-weight: 500;
    transition: @transition-base;

    &:hover {
      background: #f0f5ff;
      color: @primary-color;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .device-icon {
    padding: 24px 16px;

    .icon {
      font-size: 42px;
    }
  }

  .device-name {
    font-size: 14px;
    flex-direction: column;
    align-items: flex-start;
  }

  .property-item {
    padding: 6px 10px;
  }
}
</style>
