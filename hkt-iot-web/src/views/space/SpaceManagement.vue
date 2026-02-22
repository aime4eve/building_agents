<template>
  <div class="space-management-container">
    <a-row :gutter="16">
      <!-- 左侧空间树 -->
      <a-col :xs="24" :lg="8">
        <a-card title="空间结构" :bordered="false" class="space-tree-card">
          <template #extra>
            <a-space>
              <a-button type="primary" size="small" @click="handleAddSpace(null)">
                <PlusOutlined /> 新建
              </a-button>
              <a-button size="small" @click="handleRefresh">
                <ReloadOutlined />
              </a-button>
            </a-space>
          </template>
          <a-tree
            v-model:selectedKeys="selectedKeys"
            v-model:expandedKeys="expandedKeys"
            :tree-data="spaceTree"
            :field-names="{ title: 'name', key: 'id' }"
            show-line
            @select="handleSelectSpace"
          >
            <template #title="{ name, type, status }">
              <span class="tree-node-title">
                <a-tag :color="getTypeColor(type)" size="small">{{ type }}</a-tag>
                {{ name }}
                <a-badge
                  v-if="status === 'INACTIVE'"
                  status="error"
                  text="停用"
                />
              </span>
            </template>
            <template #icon="{ type }">
              <component :is="getTypeIcon(type)" />
            </template>
          </a-tree>
        </a-card>
      </a-col>

      <!-- 右侧空间详情 -->
      <a-col :xs="24" :lg="16">
        <a-card v-if="selectedSpace" :title="`空间详情 - ${selectedSpace.name}`" :bordered="false">
          <template #extra>
            <a-space>
              <a-button size="small" @click="handleAddSpace(selectedSpace.id)">
                <PlusOutlined /> 子空间
              </a-button>
              <a-button size="small" @click="handleEditSpace">
                <EditOutlined /> 编辑
              </a-button>
              <a-button size="small" danger @click="handleDeleteSpace">
                <DeleteOutlined /> 删除
              </a-button>
            </a-space>
          </template>

          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="空间名称">
              {{ selectedSpace.name }}
            </a-descriptions-item>
            <a-descriptions-item label="空间编码">
              {{ selectedSpace.code }}
            </a-descriptions-item>
            <a-descriptions-item label="空间类型">
              <a-tag :color="getTypeColor(selectedSpace.type)">
                {{ getTypeText(selectedSpace.type) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-badge
                :status="selectedSpace.status === 'ACTIVE' ? 'success' : 'error'"
                :text="selectedSpace.status === 'ACTIVE' ? '正常' : '停用'"
              />
            </a-descriptions-item>
            <a-descriptions-item label="设备数量">
              {{ selectedSpace.deviceCount || 0 }}
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              {{ selectedSpace.createdAt }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- 设备列表 -->
          <a-divider>空间设备</a-divider>
          <a-table
            :columns="deviceColumns"
            :data-source="spaceDevices"
            :loading="deviceLoading"
            :pagination="{ pageSize: 5 }"
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
        </a-card>

        <!-- 空统计卡片 -->
        <a-row v-else :gutter="16" class="stats-cards">
          <a-col :xs="24" :sm="12" :md="6">
            <a-card>
              <a-statistic title="园区数" :value="stats.campus" prefix="🏢" />
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-card>
              <a-statistic title="楼栋数" :value="stats.building" prefix="🏢" />
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-card>
              <a-statistic title="楼层数" :value="stats.floor" prefix="🏢" />
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-card>
              <a-statistic title="房间数" :value="stats.room" prefix="🏢" />
            </a-card>
          </a-col>
        </a-row>
      </a-col>
    </a-row>

    <!-- 新增/编辑空间弹窗 -->
    <a-modal
      v-model:open="spaceModalVisible"
      :title="spaceModalTitle"
      :width="600"
      @ok="handleSpaceModalOk"
      @cancel="spaceModalVisible = false"
    >
      <a-form
        ref="spaceFormRef"
        :model="spaceFormData"
        :rules="spaceFormRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="空间名称" name="name">
          <a-input v-model:value="spaceFormData.name" placeholder="请输入空间名称" />
        </a-form-item>
        <a-form-item label="空间编码" name="code">
          <a-input v-model:value="spaceFormData.code" placeholder="请输入空间编码" />
        </a-form-item>
        <a-form-item label="空间类型" name="type">
          <a-select v-model:value="spaceFormData.type" placeholder="请选择空间类型">
            <a-select-option value="CAMPUS">园区</a-select-option>
            <a-select-option value="BUILDING">楼栋</a-select-option>
            <a-select-option value="FLOOR">楼层</a-select-option>
            <a-select-option value="ROOM">房间</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="上级空间" name="parentId">
          <a-tree-select
            v-model:value="spaceFormData.parentId"
            :tree-data="spaceTreeForSelect"
            placeholder="请选择上级空间"
            allow-clear
            tree-default-expand-all
          />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="spaceFormData.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
  HomeOutlined,
  BuildingOutlined,
  ApartmentOutlined,
} from '@ant-design/icons-vue'
import { spaceApi, type SpaceRequest, type SpaceTreeNode } from '@/api/space'
import type { Space, Device } from '@/types'

// 选中的空间
const selectedKeys = ref<string[]>([])
const expandedKeys = ref<string[]>([])

// 空间树数据
const spaceTree = ref<SpaceTreeNode[]>([])
const selectedSpace = ref<Space | null>(null)

// 设备列表
const spaceDevices = ref<Device[]>([])
const deviceLoading = ref(false)

// 设备表格列
const deviceColumns = [
  { title: '设备名称', dataIndex: 'name', key: 'name' },
  { title: '设备SN', dataIndex: 'sn', key: 'sn' },
  { title: '类型', dataIndex: 'type', key: 'type' },
  { title: '状态', dataIndex: 'status', key: 'status' },
]

// 统计数据
const stats = reactive({
  campus: 0,
  building: 0,
  floor: 0,
  room: 0,
})

// 空间弹窗
const spaceModalVisible = ref(false)
const spaceModalTitle = ref('')
const spaceEditMode = ref<'add' | 'edit'>('add')
const currentSpaceId = ref<string | null>(null)

const spaceFormData = reactive<Partial<SpaceRequest>>({
  name: '',
  code: '',
  type: 'ROOM',
  parentId: undefined,
  description: '',
})

const spaceFormRules = {
  name: [{ required: true, message: '请输入空间名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入空间编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择空间类型', trigger: 'change' }],
}

// 用于下拉选择的树数据
const spaceTreeForSelect = computed(() => {
  const convertToTreeSelect = (nodes: SpaceTreeNode[]): any[] => {
    return nodes.map((node) => ({
      title: node.name,
      value: node.id,
      children: node.children ? convertToTreeSelect(node.children) : undefined,
    }))
  }
  return convertToTreeSelect(spaceTree.value)
})

// 获取空间树
const fetchSpaceTree = async () => {
  try {
    const response = await spaceApi.getSpaceTree('')
    spaceTree.value = response.data

    // 计算统计数据
    stats.campus = response.data.filter((n) => n.type === 'CAMPUS').length
    stats.building = countByType(response.data, 'BUILDING')
    stats.floor = countByType(response.data, 'FLOOR')
    stats.room = countByType(response.data, 'ROOM')
  } catch (error) {
    console.error('获取空间树失败:', error)
  }
}

// 递归统计
const countByType = (nodes: SpaceTreeNode[], type: string): number => {
  let count = 0
  nodes.forEach((node) => {
    if (node.type === type) count++
    if (node.children) count += countByType(node.children, type)
  })
  return count
}

// 选择空间
const handleSelectSpace = (keys: string[]) => {
  if (keys.length > 0) {
    const findSpace = (nodes: SpaceTreeNode[], id: string): Space | null => {
      for (const node of nodes) {
        if (node.id === id) return node
        if (node.children) {
          const found = findSpace(node.children, id)
          if (found) return found
        }
      }
      return null
    }
    selectedSpace.value = findSpace(spaceTree.value, keys[0])
    if (selectedSpace.value) {
      fetchSpaceDevices(selectedSpace.value.id)
    }
  } else {
    selectedSpace.value = null
    spaceDevices.value = []
  }
}

// 获取空间设备
const fetchSpaceDevices = async (spaceId: string) => {
  deviceLoading.value = true
  try {
    const response = await spaceApi.getSpaceDevices(spaceId, { page: 1, size: 100 })
    spaceDevices.value = response.data.items
  } catch (error) {
    console.error('获取空间设备失败:', error)
  } finally {
    deviceLoading.value = false
  }
}

// 新增空间
const handleAddSpace = (parentId: string | null) => {
  spaceEditMode.value = 'add'
  spaceModalTitle.value = parentId ? '新增子空间' : '新增空间'
  currentSpaceId.value = null
  Object.assign(spaceFormData, {
    name: '',
    code: '',
    type: 'ROOM',
    parentId: parentId || undefined,
    description: '',
  })
  spaceModalVisible.value = true
}

// 编辑空间
const handleEditSpace = () => {
  if (!selectedSpace.value) return
  spaceEditMode.value = 'edit'
  spaceModalTitle.value = '编辑空间'
  currentSpaceId.value = selectedSpace.value.id
  Object.assign(spaceFormData, {
    name: selectedSpace.value.name,
    code: selectedSpace.value.code,
    type: selectedSpace.value.type,
    parentId: selectedSpace.value.parentId,
    description: '',
  })
  spaceModalVisible.value = true
}

// 删除空间
const handleDeleteSpace = () => {
  if (!selectedSpace.value) return
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该空间吗？此操作不可恢复。',
    onOk: async () => {
      try {
        await spaceApi.deleteSpace(selectedSpace.value!.id)
        message.success('删除成功')
        fetchSpaceTree()
        selectedSpace.value = null
        selectedKeys.value = []
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 空间弹窗确认
const handleSpaceModalOk = async () => {
  try {
    if (spaceEditMode.value === 'add') {
      await spaceApi.createSpace(spaceFormData as SpaceRequest)
      message.success('创建成功')
    } else {
      await spaceApi.updateSpace(currentSpaceId.value!, spaceFormData)
      message.success('更新成功')
    }
    spaceModalVisible.value = false
    fetchSpaceTree()
  } catch (error) {
    message.error(spaceEditMode.value === 'add' ? '创建失败' : '更新失败')
  }
}

// 刷新
const handleRefresh = () => {
  fetchSpaceTree()
}

// 获取类型颜色
const getTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    CAMPUS: 'blue',
    BUILDING: 'green',
    FLOOR: 'orange',
    ROOM: 'purple',
  }
  return colorMap[type] || 'default'
}

// 获取类型文本
const getTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    CAMPUS: '园区',
    BUILDING: '楼栋',
    FLOOR: '楼层',
    ROOM: '房间',
  }
  return textMap[type] || type
}

// 获取类型图标
const getTypeIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    CAMPUS: HomeOutlined,
    BUILDING: BuildingOutlined,
    FLOOR: ApartmentOutlined,
    ROOM: ApartmentOutlined,
  }
  return iconMap[type] || HomeOutlined
}

onMounted(() => {
  fetchSpaceTree()
})
</script>

<style scoped>
.space-management-container {
  padding: 24px;
}

.space-tree-card {
  height: calc(100vh - 150px);
  overflow-y: auto;
}

.tree-node-title {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stats-cards {
  padding: 24px 0;
}

.stats-cards .ant-card {
  margin-bottom: 16px;
}
</style>
