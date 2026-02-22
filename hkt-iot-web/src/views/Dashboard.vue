<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :xs="24" :sm="12" :lg="6" v-for="stat in stats" :key="stat.key">
        <a-card class="stat-card">
          <a-statistic
            :title="stat.title"
            :value="stat.value"
            :prefix="stat.prefix"
            :suffix="stat.suffix"
            :value-style="{ color: stat.color }"
          />
          <div class="stat-footer">
            <span :class="stat.trend > 0 ? 'text-success' : 'text-error'">
              {{ stat.trend > 0 ? '↑' : '↓' }} {{ Math.abs(stat.trend) }}%
            </span>
            <span class="stat-label">较上周</span>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="charts-row">
      <!-- 设备状态分布 -->
      <a-col :xs="24" :lg="12">
        <a-card title="设备状态分布" class="chart-card">
          <div ref="deviceStatusChartRef" style="height: 300px"></div>
        </a-card>
      </a-col>

      <!-- 告警趋势 -->
      <a-col :xs="24" :lg="12">
        <a-card title="告警趋势" class="chart-card">
          <div ref="alarmTrendChartRef" style="height: 300px"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="charts-row">
      <!-- 能耗统计 -->
      <a-col :xs="24" :lg="16">
        <a-card title="能耗统计" class="chart-card">
          <div ref="energyChartRef" style="height: 300px"></div>
        </a-card>
      </a-col>

      <!-- 最近告警 -->
      <a-col :xs="24" :lg="8">
        <a-card title="最近告警" class="chart-card">
          <a-list
            :data-source="recentAlarms"
            :loading="alarmLoading"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a-tag :color="getAlarmColor(item.level)">{{ item.level }}</a-tag>
                    {{ item.message }}
                  </template>
                  <template #description>{{ item.time }}</template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

// 统计卡片数据
const stats = ref([
  {
    key: 'devices',
    title: '设备总数',
    value: 1248,
    prefix: '📱',
    trend: 12.5,
    color: '#1890ff',
  },
  {
    key: 'online',
    title: '在线设备',
    value: 1198,
    prefix: '🟢',
    trend: 8.2,
    color: '#52c41a',
  },
  {
    key: 'alarms',
    title: '今日告警',
    value: 23,
    prefix: '🔔',
    trend: -15.3,
    color: '#ff4d4f',
  },
  {
    key: 'energy',
    title: '今日能耗',
    value: 3428,
    prefix: '⚡',
    suffix: 'kWh',
    trend: -5.8,
    color: '#faad14',
  },
])

// 图表引用
const deviceStatusChartRef = ref<HTMLElement>()
const alarmTrendChartRef = ref<HTMLElement>()
const energyChartRef = ref<HTMLElement>()

let deviceStatusChart: ECharts | null = null
let alarmTrendChart: ECharts | null = null
let energyChart: ECharts | null = null

// 最近告警
const recentAlarms = ref([
  { level: '高', message: '1号楼温度过高', time: '10:25' },
  { level: '中', message: '地下车库烟雾报警', time: '09:45' },
  { level: '低', message: '3F-01除湿机离线', time: '08:30' },
  { level: '高', message: '配电室电压异常', time: '07:15' },
  { level: '中', message: '园区入口门禁异常', time: '06:00' },
])

const alarmLoading = ref(false)

// 告警颜色
const getAlarmColor = (level: string) => {
  const colorMap: Record<string, string> = {
    高: 'error',
    中: 'warning',
    低: 'default',
  }
  return colorMap[level] || 'default'
}

// 初始化设备状态图表
const initDeviceStatusChart = () => {
  if (!deviceStatusChartRef.value) return

  deviceStatusChart = echarts.init(deviceStatusChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
    },
    series: [
      {
        name: '设备状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: [
          { value: 1198, name: '在线', itemStyle: { color: '#52c41a' } },
          { value: 35, name: '离线', itemStyle: { color: '#8c8c8c' } },
          { value: 15, name: '故障', itemStyle: { color: '#ff4d4f' } },
        ],
      },
    ],
  }

  deviceStatusChart.setOption(option)
}

// 初始化告警趋势图表
const initAlarmTrendChart = () => {
  if (!alarmTrendChartRef.value) return

  alarmTrendChart = echarts.init(alarmTrendChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '告警数',
        type: 'line',
        smooth: true,
        data: [12, 18, 15, 23, 20, 25, 23],
        itemStyle: {
          color: '#ff4d4f',
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 77, 79, 0.3)' },
            { offset: 1, color: 'rgba(255, 77, 79, 0.05)' },
          ]),
        },
      },
    ],
  }

  alarmTrendChart.setOption(option)
}

// 初始化能耗图表
const initEnergyChart = () => {
  if (!energyChartRef.value) return

  energyChart = echarts.init(energyChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['电耗', '水耗'],
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '电耗',
        type: 'bar',
        data: [120, 180, 350, 420, 380, 290],
        itemStyle: {
          color: '#1890ff',
        },
      },
      {
        name: '水耗',
        type: 'line',
        smooth: true,
        data: [45, 52, 78, 85, 72, 58],
        itemStyle: {
          color: '#13c2c2',
        },
      },
    ],
  }

  energyChart.setOption(option)
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  deviceStatusChart?.resize()
  alarmTrendChart?.resize()
  energyChart?.resize()
}

onMounted(() => {
  initDeviceStatusChart()
  initAlarmTrendChart()
  initEnergyChart()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  deviceStatusChart?.dispose()
  alarmTrendChart?.dispose()
  energyChart?.dispose()

  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="less">
@primary-color: #1890ff;
@success-color: #52c41a;
@warning-color: #faad14;
@error-color: #ff4d4f;
@border-radius: 16px;
@shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
@shadow-md: 0 4px 16px rgba(0, 0, 0, 0.1);
@shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);
@transition-base: all 0.3s ease;

.dashboard-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

// 统计卡片行
.stats-row {
  margin-bottom: 24px;

  :deep(.ant-col) {
    transition: transform 0.3s ease;

    &:hover {
      transform: translateY(-4px);
    }
  }
}

.stat-card {
  border-radius: @border-radius;
  box-shadow: @shadow-sm;
  transition: @transition-base;
  overflow: hidden;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, @primary-color 0%, @success-color 100%);
  }

  &:hover {
    box-shadow: @shadow-lg;
    transform: translateY(-4px);
  }

  :deep(.ant-statistic) {
    .ant-statistic-title {
      font-size: 14px;
      color: #8c8c8c;
      margin-bottom: 12px;
      font-weight: 500;
    }

    .ant-statistic-content {
      font-size: 28px;
      font-weight: 700;
      color: #262626;

      .ant-statistic-content-prefix {
        font-size: 20px;
        margin-right: 8px;
      }

      .ant-statistic-content-suffix {
        font-size: 14px;
        margin-left: 4px;
        color: #8c8c8c;
      }
    }
  }

  .stat-footer {
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    align-items: center;
    gap: 8px;

    .text-success {
      color: @success-color;
      font-weight: 600;
    }

    .text-error {
      color: @error-color;
      font-weight: 600;
    }

    .stat-label {
      color: #bfbfbf;
      font-size: 12px;
      margin-left: auto;
    }
  }
}

// 图表行
.charts-row {
  margin-bottom: 24px;

  :deep(.ant-col) {
    transition: transform 0.3s ease;
  }
}

.chart-card {
  border-radius: @border-radius;
  box-shadow: @shadow-sm;
  transition: @transition-base;
  height: 100%;

  &:hover {
    box-shadow: @shadow-md;
  }

  :deep(.ant-card-head) {
    background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
    border-bottom: 1px solid #e8e8e8;
    padding: 16px 20px;
    font-weight: 600;
    font-size: 16px;
    border-radius: @border-radius @border-radius 0 0 !important;
  }

  :deep(.ant-card-body) {
    padding: 20px;
  }
}

// 告警列表
:deep(.ant-list) {
  .ant-list-item {
    padding: 12px 0;
    border-color: #f0f0f0 !important;
    transition: @transition-base;

    &:hover {
      background: #fafafa;
      padding-left: 12px;
      padding-right: 12px;
      margin: 0 -12px;
      border-radius: 8px;
    }

    .ant-list-item-meta {
      align-items: center;

      .ant-list-item-meta-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 500;
      }

      .ant-list-item-meta-description {
        color: #bfbfbf;
        font-size: 12px;
        margin-top: 4px;
      }
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }

  .stats-row {
    :deep(.ant-col) {
      margin-bottom: 16px;
    }
  }

  .chart-card {
    margin-bottom: 16px;
  }
}
</style>
