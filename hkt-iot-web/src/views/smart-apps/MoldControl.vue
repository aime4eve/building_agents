<template>
  <div class="mold-control-container">
    <a-row :gutter="16">
      <!-- 左侧：空间列表 -->
      <a-col :xs="24" :lg="8">
        <a-card title="空间列表" :bordered="false" class="space-list-card">
          <template #extra>
            <a-select v-model:value="filterType" style="width: 120px" @change="fetchSpaces">
              <a-select-option value="all">全部</a-select-option>
              <a-select-option value="ROOM">房间</a-select-option>
              <a-select-option value="FLOOR">楼层</a-select-option>
            </a-select>
          </template>
          <a-list
            :data-source="spaceList"
            :loading="spaceLoading"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item
                :class="{ 'active-item': selectedSpaceId === item.id }"
                @click="selectSpace(item)"
                class="space-item"
              >
                <a-list-item-meta>
                  <template #title>
                    <a-space>
                      <span>{{ item.name }}</span>
                      <a-tag
                        v-if="getMoldRisk(item.riskLevel)"
                        :color="getRiskColor(item.riskLevel)"
                      >
                        {{ getRiskText(item.riskLevel) }}
                      </a-tag>
                    </a-space>
                  </template>
                  <template #description>
                    温度: {{ item.temperature }}°C | 湿度: {{ item.humidity }}%
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="link" size="small" @click.stop="viewSpaceDetail(item)">
                    查看详情
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 右侧：监控详情 -->
      <a-col :xs="24" :lg="16">
        <a-card v-if="selectedSpace" :bordered="false">
          <!-- 顶部信息 -->
          <template #title>
            <a-space>
              <span>{{ selectedSpace.name }}</span>
              <a-tag :color="getRiskColor(selectedSpace.riskLevel)">
                {{ getRiskText(selectedSpace.riskLevel) }}
              </a-tag>
            </a-space>
          </template>

          <!-- 实时数据 -->
          <a-row :gutter="16" class="data-cards">
            <a-col :xs="12" :sm="6">
              <a-card class="data-card">
                <a-statistic
                  title="当前温度"
                  :value="selectedSpace.temperature"
                  suffix="°C"
                  :value-style="{ color: getTempColor(selectedSpace.temperature) }"
                />
              </a-card>
            </a-col>
            <a-col :xs="12" :sm="6">
              <a-card class="data-card">
                <a-statistic
                  title="当前湿度"
                  :value="selectedSpace.humidity"
                  suffix="%"
                  :value-style="{ color: getHumidityColor(selectedSpace.humidity) }"
                />
              </a-card>
            </a-col>
            <a-col :xs="12" :sm="6">
              <a-card class="data-card">
                <a-statistic
                  title="霉变风险"
                  :value="selectedSpace.riskScore"
                  suffix="分"
                  :value-style="{ color: getRiskColor(selectedSpace.riskLevel) }"
                />
              </a-card>
            </a-col>
            <a-col :xs="12" :sm="6">
              <a-card class="data-card">
                <a-statistic
                  title="设备状态"
                  :value="selectedSpace.deviceStatus"
                  :value-style="{ color: selectedSpace.deviceStatus === '运行中' ? '#52c41a' : '#8c8c8c' }"
                />
              </a-card>
            </a-col>
          </a-row>

          <!-- 温湿度趋势图 -->
          <a-card title="24小时温湿度趋势" class="chart-card">
            <div ref="trendChartRef" style="height: 280px"></div>
          </a-card>

          <!-- 控制面板 -->
          <a-card title="设备控制" class="control-card">
            <a-row :gutter="16">
              <a-col :xs="24" :sm="12" :md="6">
                <div class="control-item">
                  <div class="control-label">除湿机</div>
                  <a-switch
                    v-model:checked="deviceControls.dehumidifier"
                    checked-children="开"
                    un-checked-children="关"
                    @change="handleDeviceControl('dehumidifier', $event)"
                  />
                </div>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6">
                <div class="control-item">
                  <div class="control-label">空调</div>
                  <a-switch
                    v-model:checked="deviceControls.airConditioner"
                    checked-children="开"
                    un-checked-children="关"
                    @change="handleDeviceControl('airConditioner', $event)"
                  />
                </div>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6">
                <div class="control-item">
                  <div class="control-label">通风</div>
                  <a-switch
                    v-model:checked="deviceControls.ventilation"
                    checked-children="开"
                    un-checked-children="关"
                    @change="handleDeviceControl('ventilation', $event)"
                  />
                </div>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6">
                <div class="control-item">
                  <div class="control-label">智能联动</div>
                  <a-switch
                    v-model:checked="deviceControls.autoMode"
                    checked-children="开"
                    un-checked-children="关"
                    @change="handleDeviceControl('autoMode', $event)"
                  />
                </div>
              </a-col>
            </a-row>

            <!-- 目标设置 -->
            <a-divider>目标设置</a-divider>
            <a-row :gutter="16">
              <a-col :xs="24" :sm="12">
                <a-form-item label="目标温度">
                  <a-slider
                    v-model:value="targetSettings.temperature"
                    :min="16"
                    :max="30"
                    :marks="{ 18: '18°C', 22: '22°C', 26: '26°C' }"
                    @change="handleTargetChange"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="12">
                <a-form-item label="目标湿度">
                  <a-slider
                    v-model:value="targetSettings.humidity"
                    :min="30"
                    :max="80"
                    :marks="{ 40: '40%', 55: '55%', 70: '70%' }"
                    @change="handleTargetChange"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-card>

          <!-- 预警记录 -->
          <a-card title="预警记录" class="alert-card">
            <a-timeline>
              <a-timeline-item
                v-for="alert in alerts"
                :key="alert.id"
                :color="getAlertColor(alert.level)"
              >
                <template #dot>
                  <ClockCircleOutlined v-if="alert.level === 'info'" style="font-size: 16px" />
                  <ExclamationCircleOutlined v-else-if="alert.level === 'warning'" style="font-size: 16px" />
                  <CloseCircleOutlined v-else style="font-size: 16px" />
                </template>
                <div>
                  <div class="alert-title">{{ alert.title }}</div>
                  <div class="alert-time">{{ alert.time }}</div>
                </div>
              </a-timeline-item>
            </a-timeline>
          </a-card>
        </a-card>

        <!-- 空选择时显示 -->
        <a-empty v-else description="请选择空间查看详情" />
      </a-col>
    </a-row>

    <!-- 风险评估弹窗 -->
    <a-modal
      v-model:open="riskModalVisible"
      title="霉变风险评估"
      :width="600"
      :footer="null"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="评估空间">
          {{ riskData.spaceName }}
        </a-descriptions-item>
        <a-descriptions-item label="当前温度">
          {{ riskData.temperature }}°C
        </a-descriptions-item>
        <a-descriptions-item label="当前湿度">
          {{ riskData.humidity }}%
        </a-descriptions-item>
        <a-descriptions-item label="风险等级">
          <a-tag :color="getRiskColor(riskData.riskLevel)">
            {{ getRiskText(riskData.riskLevel) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="风险评分">
          <a-progress
            :percent="riskData.riskScore"
            :stroke-color="getProgressColor(riskData.riskScore)"
          />
        </a-descriptions-item>
        <a-descriptions-item label="建议措施">
          <ul class="suggestions">
            <li v-for="(suggestion, index) in riskData.suggestions" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

// 过滤类型
const filterType = ref('all')

// 空间列表
const spaceList = ref<any[]>([])
const spaceLoading = ref(false)
const selectedSpaceId = ref('')
const selectedSpace = ref<any>(null)

// 设备控制
const deviceControls = reactive({
  dehumidifier: false,
  airConditioner: false,
  ventilation: false,
  autoMode: true,
})

// 目标设置
const targetSettings = reactive({
  temperature: 22,
  humidity: 55,
})

// 预警记录
const alerts = ref<any[]>([])

// 图表
const trendChartRef = ref<HTMLElement>()
let trendChart: ECharts | null = null

// 风险评估弹窗
const riskModalVisible = ref(false)
const riskData = reactive({
  spaceName: '',
  temperature: 0,
  humidity: 0,
  riskLevel: 'low',
  riskScore: 0,
  suggestions: [] as string[],
})

// 获取空间列表
const fetchSpaces = async () => {
  spaceLoading.value = true
  try {
    // 模拟数据
    spaceList.value = [
      {
        id: '1',
        name: '1号楼-3F-301',
        type: 'ROOM',
        temperature: 26,
        humidity: 68,
        riskLevel: 'high',
        riskScore: 85,
        deviceStatus: '运行中',
      },
      {
        id: '2',
        name: '1号楼-3F-302',
        type: 'ROOM',
        temperature: 23,
        humidity: 55,
        riskLevel: 'low',
        riskScore: 20,
        deviceStatus: '待机',
      },
      {
        id: '3',
        name: '1号楼-3F-303',
        type: 'ROOM',
        temperature: 25,
        humidity: 62,
        riskLevel: 'medium',
        riskScore: 55,
        deviceStatus: '运行中',
      },
      {
        id: '4',
        name: '2号楼-2F-201',
        type: 'ROOM',
        temperature: 22,
        humidity: 48,
        riskLevel: 'low',
        riskScore: 15,
        deviceStatus: '待机',
      },
    ]
  } finally {
    spaceLoading.value = false
  }
}

// 选择空间
const selectSpace = async (space: any) => {
  selectedSpaceId.value = space.id
  selectedSpace.value = space

  // 更新设备控制状态
  deviceControls.dehumidifier = space.deviceStatus === '运行中'
  deviceControls.autoMode = true

  // 获取预警记录
  alerts.value = [
    { id: '1', level: 'error', title: '霉变风险高，建议开启除湿', time: '10分钟前' },
    { id: '2', level: 'warning', title: '湿度超过阈值(65%)', time: '30分钟前' },
    { id: '3', level: 'info', title: '设备自动控制已启用', time: '1小时前' },
  ]

  // 初始化图表
  await nextTick()
  initTrendChart()
}

// 查看空间详情
const viewSpaceDetail = (space: any) => {
  // 计算风险等级
  let riskLevel = 'low'
  let riskScore = 0
  const suggestions: string[] = []

  if (space.temperature > 26 || space.humidity > 65) {
    riskLevel = 'high'
    riskScore = 85
    suggestions.push('立即开启除湿机降低湿度')
    suggestions.push('开启空调降温至22-24°C')
    suggestions.push('加强通风换气')
  } else if (space.temperature > 24 || space.humidity > 60) {
    riskLevel = 'medium'
    riskScore = 55
    suggestions.push('建议开启除湿机')
    suggestions.push('适当降低空调温度')
  } else {
    riskLevel = 'low'
    riskScore = 20
    suggestions.push('当前环境适宜，保持监控')
  }

  Object.assign(riskData, {
    spaceName: space.name,
    temperature: space.temperature,
    humidity: space.humidity,
    riskLevel,
    riskScore,
    suggestions,
  })

  riskModalVisible.value = true
}

// 设备控制
const handleDeviceControl = (device: string, checked: boolean) => {
  const deviceNames: Record<string, string> = {
    dehumidifier: '除湿机',
    airConditioner: '空调',
    ventilation: '通风',
    autoMode: '智能联动',
  }
  message.success(`${deviceNames[device]}已${checked ? '开启' : '关闭'}`)
}

// 目标设置变化
const handleTargetChange = () => {
  message.success(`目标设置已更新：温度${targetSettings.temperature}°C，湿度${targetSettings.humidity}%`)
}

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return

  if (trendChart) {
    trendChart.dispose()
  }

  trendChart = echarts.init(trendChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['温度', '湿度'],
    },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 24 }, (_, i) => `${i}:00`),
    },
    yAxis: [
      {
        type: 'value',
        name: '温度(°C)',
        position: 'left',
      },
      {
        type: 'value',
        name: '湿度(%)',
        position: 'right',
      },
    ],
    series: [
      {
        name: '温度',
        type: 'line',
        smooth: true,
        data: [22, 22, 21, 21, 20, 20, 21, 22, 23, 24, 25, 26, 27, 27, 26, 26, 25, 24, 24, 23, 23, 22, 22, 22],
        itemStyle: { color: '#ff7300' },
      },
      {
        name: '湿度',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: [55, 54, 54, 53, 52, 52, 53, 55, 58, 62, 65, 68, 70, 70, 68, 67, 65, 62, 60, 58, 57, 56, 55, 55],
        itemStyle: { color: '#13c2c2' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(19, 194, 194, 0.3)' },
            { offset: 1, color: 'rgba(19, 194, 194, 0.05)' },
          ]),
        },
      },
    ],
  }

  trendChart.setOption(option)
}

// 获取温度颜色
const getTempColor = (temp: number) => {
  if (temp > 26) return '#ff4d4f'
  if (temp > 24) return '#faad14'
  return '#52c41a'
}

// 获取湿度颜色
const getHumidityColor = (humidity: number) => {
  if (humidity > 65) return '#ff4d4f'
  if (humidity > 60) return '#faad14'
  return '#52c41a'
}

// 获取风险等级颜色
const getRiskColor = (level: string) => {
  const colorMap: Record<string, string> = {
    high: 'error',
    medium: 'warning',
    low: 'success',
  }
  return colorMap[level] || 'default'
}

// 获取风险等级文本
const getRiskText = (level: string) => {
  const textMap: Record<string, string> = {
    high: '高风险',
    medium: '中风险',
    low: '低风险',
  }
  return textMap[level] || level
}

// 获取霉变风险
const getMoldRisk = (level: string) => {
  return level !== 'low'
}

// 获取预警颜色
const getAlertColor = (level: string) => {
  const colorMap: Record<string, string> = {
    error: 'red',
    warning: 'orange',
    info: 'blue',
  }
  return colorMap[level] || 'gray'
}

// 获取进度条颜色
const getProgressColor = (score: number) => {
  if (score >= 80) return '#ff4d4f'
  if (score >= 50) return '#faad14'
  return '#52c41a'
}

onMounted(() => {
  fetchSpaces()
})
</script>

<style scoped>
.mold-control-container {
  padding: 24px;
}

.space-list-card {
  height: calc(100vh - 150px);
  overflow-y: auto;
}

.space-item {
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.space-item:hover {
  background-color: #f5f5f5;
}

.space-item.active-item {
  background-color: #e6f7ff;
}

.data-cards {
  margin-bottom: 16px;
}

.data-card {
  text-align: center;
  margin-bottom: 16px;
}

.chart-card {
  margin-bottom: 16px;
}

.control-card {
  margin-bottom: 16px;
}

.control-item {
  padding: 16px;
  text-align: center;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.control-label {
  font-size: 14px;
  color: #595959;
  margin-bottom: 8px;
}

.alert-card {
  margin-bottom: 16px;
}

.alert-title {
  font-size: 14px;
  color: #262626;
}

.alert-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.suggestions {
  margin: 0;
  padding-left: 20px;
}

.suggestions li {
  margin-bottom: 8px;
}
</style>
