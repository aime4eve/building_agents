<template>
  <div class="smart-livestock-container">
    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.key">
        <a-card class="stat-card">
          <a-statistic
            :title="stat.title"
            :value="stat.value"
            :suffix="stat.suffix"
            :prefix="stat.prefix"
            :value-style="{ color: stat.color }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 主要内容 -->
    <a-row :gutter="16">
      <!-- 左侧：牲畜列表 -->
      <a-col :xs="24" :lg="10">
        <a-card title="牲畜列表" :bordered="false">
          <template #extra>
            <a-space>
              <a-select v-model:value="filterStatus" style="width: 100px" placeholder="状态">
                <a-select-option value="all">全部</a-select-option>
                <a-select-option value="healthy">健康</a-select-option>
                <a-select-option value="warning">异常</a-select-option>
                <a-select-option value="sick">生病</a-select-option>
              </a-select>
              <a-button type="primary" size="small" @click="handleAddAnimal">
                <PlusOutlined /> 新增
              </a-button>
            </a-space>
          </template>

          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索耳标号/名称"
            style="margin-bottom: 16px"
            @search="handleSearch"
          />

          <a-list
            :data-source="filteredAnimals"
            :loading="loading"
            size="small"
            class="animal-list"
          >
            <template #renderItem="{ item }">
              <a-list-item
                :class="{ 'active-item': selectedAnimalId === item.id }"
                @click="selectAnimal(item)"
                class="animal-item"
              >
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar :style="{ backgroundColor: getAvatarColor(item.status) }">
                      <template #icon>
                        <span style="font-size: 20px">{{ getAnimalIcon(item.type) }}</span>
                      </template>
                    </a-avatar>
                  </template>
                  <template #title>
                    <a-space>
                      <span>{{ item.name }}</span>
                      <a-tag :color="getStatusColor(item.status)">
                        {{ getStatusText(item.status) }}
                      </a-tag>
                    </a-space>
                  </template>
                  <template #description>
                    耳标: {{ item.earTag }} | {{ item.type }}
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 右侧：详情 -->
      <a-col :xs="24" :lg="14">
        <a-card v-if="selectedAnimal" :bordered="false">
          <template #title>
            <a-space>
              <span>{{ selectedAnimal.name }}</span>
              <a-tag :color="getStatusColor(selectedAnimal.status)">
                {{ getStatusText(selectedAnimal.status) }}
              </a-tag>
            </a-space>
          </template>

          <!-- 基本信息 -->
          <a-descriptions :column="3" bordered size="small">
            <a-descriptions-item label="耳标号" :span="2">
              {{ selectedAnimal.earTag }}
            </a-descriptions-item>
            <a-descriptions-item label="牲畜类型">
              {{ selectedAnimal.type }}
            </a-descriptions-item>
            <a-descriptions-item label="性别">
              {{ selectedAnimal.gender === 'male' ? '公' : '母' }}
            </a-descriptions-item>
            <a-descriptions-item label="年龄">
              {{ selectedAnimal.age }}岁
            </a-descriptions-item>
            <a-descriptions-item label="体重">
              {{ selectedAnimal.weight }}kg
            </a-descriptions-item>
          </a-descriptions>

          <!-- 健康指标 -->
          <a-divider>健康指标</a-divider>
          <a-row :gutter="16" class="health-cards">
            <a-col :xs="12" :sm="8">
              <a-card class="health-card">
                <div class="health-value" :style="{ color: getHealthColor(selectedAnimal.bodyTemp, 38, 39.5) }">
                  {{ selectedAnimal.bodyTemp }}°C
                </div>
                <div class="health-label">体温</div>
                <div class="health-normal">正常: 38-39.5°C</div>
              </a-card>
            </a-col>
            <a-col :xs="12" :sm="8">
              <a-card class="health-card">
                <div class="health-value" :style="{ color: getHealthColor(selectedAnimal.heartRate, 60, 80, true) }">
                  {{ selectedAnimal.heartRate }}次/分
                </div>
                <div class="health-label">心率</div>
                <div class="health-normal">正常: 60-80</div>
              </a-card>
            </a-col>
            <a-col :xs="12" :sm="8">
              <a-card class="health-card">
                <div class="health-value" :style="{ color: getHealthColor(selectedAnimal.respiratory, 15, 25, true) }">
                  {{ selectedAnimal.respiratory }}次/分
                </div>
                <div class="health-label">呼吸频率</div>
                <div class="health-normal">正常: 15-25</div>
              </a-card>
            </a-col>
            <a-col :xs="12" :sm="8">
              <a-card class="health-card">
                <div class="health-value" :style="{ color: getHealthColor(selectedAnimal.steps, 3000, 10000) }">
                  {{ selectedAnimal.steps }}
                </div>
                <div class="health-label">今日步数</div>
                <div class="health-normal">正常: 3000-10000</div>
              </a-card>
            </a-col>
            <a-col :xs="12" :sm="8">
              <a-card class="health-card">
                <div class="health-value">{{ selectedAnimal.feedIntake }}kg</div>
                <div class="health-label">今日采食量</div>
              </a-card>
            </a-col>
            <a-col :xs="12" :sm="8">
              <a-card class="health-card">
                <div class="health-value">{{ selectedAnimal.waterIntake }}L</div>
                <div class="health-label">今日饮水量</div>
              </a-card>
            </a-col>
          </a-row>

          <!-- 位置追踪 -->
          <a-divider>位置追踪</a-divider>
          <div class="location-map">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-statistic title="当前位置" :value="selectedAnimal.currentLocation" />
              </a-col>
              <a-col :span="12">
                <a-statistic title="最后更新" :value="selectedAnimal.lastUpdate" />
              </a-col>
            </a-row>
            <a-button type="primary" block class="location-btn">
              <EnvironmentOutlined /> 查看实时位置
            </a-button>
          </div>

          <!-- 健康记录 -->
          <a-divider>健康记录</a-divider>
          <a-timeline>
            <a-timeline-item
              v-for="record in healthRecords"
              :key="record.id"
              :color="record.type === 'normal' ? 'green' : 'red'"
            >
              <div class="record-title">{{ record.title }}</div>
              <div class="record-time">{{ record.time }}</div>
              <div v-if="record.description" class="record-desc">{{ record.description }}</div>
            </a-timeline-item>
          </a-timeline>
        </a-card>

        <a-empty v-else description="请选择牲畜查看详情" />
      </a-col>
    </a-row>

    <!-- 异常告警弹窗 -->
    <a-modal
      v-model:open="alertModalVisible"
      title="异常告警"
      :width="800"
    >
      <a-alert
        type="warning"
        message="检测到以下牲畜存在异常情况"
        show-icon
        style="margin-bottom: 16px"
      />
      <a-table
        :columns="alertColumns"
        :data-source="alertAnimals"
        :pagination="{ pageSize: 5 }"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleAlertAction(record)">
              处理
            </a-button>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, EnvironmentOutlined } from '@ant-design/icons-vue'

// 统计数据
const stats = ref([
  { key: 'total', title: '存栏总数', value: 1250, prefix: '🐄', color: '#1890ff' },
  { key: 'healthy', title: '健康', value: 1180, prefix: '✅', color: '#52c41a' },
  { key: 'warning', title: '异常', value: 52, prefix: '⚠️', color: '#faad14' },
  { key: 'sick', title: '生病', value: 18, prefix: '🏥', color: '#ff4d4f' },
])

// 过滤条件
const filterStatus = ref('all')
const searchKeyword = ref('')

// 加载状态
const loading = ref(false)

// 牲畜列表
const animals = ref<any[]>([])

// 选中的牲畜
const selectedAnimalId = ref('')
const selectedAnimal = ref<any>(null)

// 健康记录
const healthRecords = ref<any[]>([])

// 告警弹窗
const alertModalVisible = ref(false)
const alertAnimals = ref<any[]>([])

const alertColumns = [
  { title: '耳标号', dataIndex: 'earTag', key: 'earTag' },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '异常类型', dataIndex: 'alertType', key: 'alertType' },
  { title: '检测时间', dataIndex: 'detectedAt', key: 'detectedAt' },
  { title: '操作', key: 'action', width: 80 },
]

// 过滤后的列表
const filteredAnimals = computed(() => {
  let result = animals.value

  if (filterStatus.value !== 'all') {
    result = result.filter((a) => a.status === filterStatus.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (a) =>
        a.earTag.toLowerCase().includes(keyword) ||
        a.name.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 获取牲畜列表
const fetchAnimals = async () => {
  loading.value = true
  try {
    // 模拟数据
    animals.value = [
      {
        id: '1',
        name: '奶牛-001',
        earTag: 'CN202401001',
        type: '奶牛',
        gender: 'female',
        age: 3,
        weight: 520,
        status: 'healthy',
        bodyTemp: 38.6,
        heartRate: 72,
        respiratory: 22,
        steps: 5600,
        feedIntake: 25.5,
        waterIntake: 65,
        currentLocation: '1号牛舍-A区',
        lastUpdate: '5分钟前',
      },
      {
        id: '2',
        name: '奶牛-002',
        earTag: 'CN202401002',
        type: '奶牛',
        gender: 'female',
        age: 4,
        weight: 580,
        status: 'warning',
        bodyTemp: 39.8,
        heartRate: 95,
        respiratory: 35,
        steps: 1200,
        feedIntake: 15.2,
        waterIntake: 40,
        currentLocation: '1号牛舍-B区',
        lastUpdate: '3分钟前',
      },
      {
        id: '3',
        name: '奶牛-003',
        earTag: 'CN202401003',
        type: '奶牛',
        gender: 'female',
        age: 2,
        weight: 450,
        status: 'healthy',
        bodyTemp: 38.5,
        heartRate: 68,
        respiratory: 20,
        steps: 7200,
        feedIntake: 22.8,
        waterIntake: 58,
        currentLocation: '2号牛舍-A区',
        lastUpdate: '8分钟前',
      },
      {
        id: '4',
        name: '肉牛-001',
        earTag: 'CN202401004',
        type: '肉牛',
        gender: 'male',
        age: 2,
        weight: 680,
        status: 'sick',
        bodyTemp: 40.2,
        heartRate: 105,
        respiratory: 42,
        steps: 500,
        feedIntake: 8.5,
        waterIntake: 25,
        currentLocation: '隔离舍',
        lastUpdate: '1分钟前',
      },
    ]

    // 检查异常数量
    const warningCount = animals.value.filter((a) => a.status === 'warning').length
    const sickCount = animals.value.filter((a) => a.status === 'sick').length
    stats.value[2].value = warningCount
    stats.value[3].value = sickCount

    // 准备告警数据
    alertAnimals.value = animals.value
      .filter((a) => a.status !== 'healthy')
      .map((a) => ({
        ...a,
        alertType: a.status === 'warning' ? '体温偏高/活动量少' : '疑似生病',
        detectedAt: a.lastUpdate,
      }))

    // 如果有异常，显示告警
    if (alertAnimals.value.length > 0) {
      setTimeout(() => {
        alertModalVisible.value = true
      }, 1000)
    }
  } finally {
    loading.value = false
  }
}

// 选择牲畜
const selectAnimal = (animal: any) => {
  selectedAnimalId.value = animal.id
  selectedAnimal.value = animal

  // 生成健康记录
  healthRecords.value = [
    {
      id: '1',
      type: 'normal',
      title: '健康检查正常',
      time: '今天 08:00',
      description: '体温、心率、呼吸频率均正常',
    },
    {
      id: '2',
      type: animal.status === 'healthy' ? 'normal' : 'warning',
      title: animal.status === 'healthy' ? '采食记录正常' : '采食量异常',
      time: '今天 12:30',
      description: `采食量: ${animal.feedIntake}kg`,
    },
    {
      id: '3',
      type: 'normal',
      title: '疫苗接种',
      time: '2024-01-15',
      description: '口蹄疫疫苗',
    },
  ]
}

// 搜索
const handleSearch = () => {
  // 搜索逻辑已在computed中实现
}

// 新增牲畜
const handleAddAnimal = () => {
  message.info('新增功能开发中')
}

// 处理告警
const handleAlertAction = (record: any) => {
  Modal.confirm({
    title: '处理异常',
    content: `确定要将 ${record.name} 移至隔离区并通知兽医吗？`,
    onOk: () => {
      message.success('已安排处理')
      alertModalVisible.value = false
    },
  })
}

// 获取头像颜色
const getAvatarColor = (status: string) => {
  const colorMap: Record<string, string> = {
    healthy: '#52c41a',
    warning: '#faad14',
    sick: '#ff4d4f',
  }
  return colorMap[status] || '#d9d9d9'
}

// 获取牲畜图标
const getAnimalIcon = (type: string) => {
  return type === '奶牛' ? '🐄' : type === '肉牛' ? '🐂' : '🐄'
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    healthy: 'success',
    warning: 'warning',
    sick: 'error',
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    healthy: '健康',
    warning: '异常',
    sick: '生病',
  }
  return textMap[status] || status
}

// 获取健康指标颜色
const getHealthColor = (
  value: number,
  min: number,
  max: number,
  reverse = false
) => {
  if (reverse) {
    if (value < min || value > max) return '#ff4d4f'
    return '#52c41a'
  }
  if (value < min || value > max) return '#ff4d4f'
  if (value > max * 0.9 || value < min * 1.1) return '#faad14'
  return '#52c41a'
}

onMounted(() => {
  fetchAnimals()
})
</script>

<style scoped>
.smart-livestock-container {
  padding: 24px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  margin-bottom: 16px;
}

.animal-list {
  max-height: calc(100vh - 350px);
  overflow-y: auto;
}

.animal-item {
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.animal-item:hover {
  background-color: #f5f5f5;
}

.animal-item.active-item {
  background-color: #e6f7ff;
}

.health-cards {
  margin-bottom: 16px;
}

.health-card {
  text-align: center;
  margin-bottom: 16px;
}

.health-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.health-label {
  font-size: 14px;
  color: #595959;
  margin-bottom: 4px;
}

.health-normal {
  font-size: 12px;
  color: #8c8c8c;
}

.location-map {
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.location-btn {
  margin-top: 16px;
}

.record-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.record-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.record-desc {
  font-size: 13px;
  color: #595959;
}
</style>
