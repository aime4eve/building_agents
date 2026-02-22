<template>
  <div class="telemetry-chart">
    <a-card :bordered="false" :title="title">
      <template #extra>
        <a-space>
          <a-select
            v-model:value="timeRange"
            style="width: 100px"
            size="small"
            @change="handleTimeRangeChange"
          >
            <a-select-option value="1h">1小时</a-select-option>
            <a-select-option value="6h">6小时</a-select-option>
            <a-select-option value="24h">24小时</a-select-option>
            <a-select-option value="7d">7天</a-select-option>
            <a-select-option value="30d">30天</a-select-option>
          </a-select>
          <a-button size="small" @click="handleRefresh">
            <ReloadOutlined />
          </a-button>
        </a-space>
      </template>

      <div ref="chartRef" :style="{ height: `${height}px` }" v-loading="loading"></div>

      <!-- 空状态 -->
      <a-empty
        v-if="!loading && isEmpty"
        description="暂无数据"
        :image="Empty.PRESENTED_IMAGE_SIMPLE"
      />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Empty } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

interface TelemetryData {
  timestamp: number
  value: number
}

interface Props {
  deviceId: string
  property: string
  title?: string
  height?: number
  unit?: string
  color?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '遥测数据',
  height: 300,
  unit: '',
  color: '#1890ff',
})

const loading = ref(false)
const timeRange = ref('24h')
const isEmpty = ref(false)
const chartRef = ref<HTMLElement>()
let chart: ECharts | null = null
let resizeObserver: ResizeObserver | null = null

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // TODO: 调用实际API获取遥测数据
    // const response = await telemetryApi.getHistory({
    //   deviceId: props.deviceId,
    //   property: props.property,
    //   timeRange: timeRange.value,
    // })

    // 模拟数据
    const mockData = generateMockData()
    isEmpty.value = mockData.length === 0

    if (mockData.length > 0) {
      renderChart(mockData)
    }
  } finally {
    loading.value = false
  }
}

// 生成模拟数据
const generateMockData = (): TelemetryData[] => {
  const data: TelemetryData[] = []
  const now = Date.now()
  const interval = getTimeInterval()

  for (let i = 0; i < 100; i++) {
    const timestamp = now - (100 - i) * interval
    const value = 20 + Math.random() * 15
    data.push({ timestamp, value })
  }

  return data
}

// 获取时间间隔
const getTimeInterval = (): number => {
  const rangeMap: Record<string, number> = {
    '1h': 36000,
    '6h': 216000,
    '24h': 864000,
    '7d': 6048000,
    '30d': 25920000,
  }
  return rangeMap[timeRange.value] / 100
}

// 渲染图表
const renderChart = (data: TelemetryData[]) => {
  if (!chartRef.value) return

  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const param = params[0]
        const time = new Date(param.name).toLocaleString('zh-CN')
        return `${time}<br/>${param.seriesName}: ${param.value}${props.unit}`
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.timestamp),
      axisLabel: {
        formatter: (value: number) => {
          const date = new Date(value)
          if (timeRange.value === '1h' || timeRange.value === '6h') {
            return `${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
          }
          return `${date.getMonth() + 1}/${date.getDate()}`
        },
      },
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: `{value}${props.unit}`,
      },
    },
    series: [
      {
        name: props.property,
        type: 'line',
        smooth: true,
        data: data.map((d) => d.value),
        itemStyle: {
          color: props.color,
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${props.color}40` },
            { offset: 1, color: `${props.color}05` },
          ]),
        },
      },
    ],
  }

  chart.setOption(option)
}

// 刷新
const handleRefresh = () => {
  loadData()
}

// 时间范围变化
const handleTimeRangeChange = () => {
  loadData()
}

// 窗口大小变化
const handleResize = () => {
  chart?.resize()
}

// 监听属性变化
watch(() => [props.deviceId, props.property], () => {
  loadData()
})

onMounted(() => {
  loadData()

  // 监听容器大小变化
  if (chartRef.value) {
    resizeObserver = new ResizeObserver(handleResize)
    resizeObserver.observe(chartRef.value)
  }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  chart?.dispose()
  resizeObserver?.disconnect()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.telemetry-chart {
  width: 100%;
}
</style>
