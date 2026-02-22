<template>
  <div class="space-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">空间管理</h2>
    </div>

    <!-- 搜索表单 -->
    <a-card class="search-form-card">
      <a-form layout="inline" :model="queryParams" class="search-form">
        <a-form-item label="空间名称">
          <a-input
            v-model:value="queryParams.keyword"
            placeholder="请输入空间名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="空间类型">
          <a-select
            v-model:value="queryParams.type"
            placeholder="请选择空间类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="CAMPUS">园区</a-select-option>
            <a-select-option value="BUILDING">楼栋</a-select-option>
            <a-select-option value="FLOOR">楼层</a-select-option>
            <a-select-option value="ROOM">房间</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="状态">
          <a-select
            v-model:value="queryParams.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="ACTIVE">正常</a-select-option>
            <a-select-option value="INACTIVE">停用</a-select-option>
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

    <!-- 空间树形表格 -->
    <a-card class="table-card">
      <template #title>
        <a-space>
          <span>空间列表</span>
          <a-tag v-if="spaceList.length > 0" color="blue">
            共 {{ totalCount }} 个空间
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button @click="handleTreeView">
            <ApartmentOutlined />
            {{ isTreeView ? '列表视图' : '树形视图' }}
          </a-button>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined />
            新增空间
          </a-button>
        </a-space>
      </template>

      <!-- 树形视图 -->
      <a-tree
        v-if="isTreeView"
        v-model:selectedKeys="selectedKeys"
        v-model:expandedKeys="expandedKeys"
        :tree-data="spaceTree"
        :field-names="{ title: 'name', key: 'id' }"
        show-line
        class="space-tree"
      >
        <template #title="{ name, type, status, code }">
          <span class="tree-node-title">
            <a-tag :color="getTypeColor(type)" size="small">
              {{ getTypeText(type) }}
            </a-tag>
            {{ name }}
            <a-tag v-if="status !== 'ACTIVE'" :color="getStatusColor(status)" size="small">
              {{ getStatusText(status) }}
            </a-tag>
            <a-button type="link" size="small" @click.stop="handleViewDetail($event)">
              详情
            </a-button>
          </span>
        </template>
      </a-tree>

      <!-- 列表视图 -->
      <a-table
        v-else
        :columns="columns"
        :data-source="spaceList"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
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
              <a-button type="link" size="small" @click="handleView(record as Space)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="handleAddChild(record as Space)">
                子空间
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record as Space)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该空间吗？"
                @confirm="handleDelete(record as Space)"
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
  ApartmentOutlined,
} from '@ant-design/icons-vue'
import { spaceApi, type SpaceQuery, type SpaceTreeNode } from '@/api/space'
import type { Space } from '@/types'

const router = useRouter()

// 查询参数
const queryParams = reactive<SpaceQuery>({
  page: 1,
  size: 10,
  keyword: '',
  type: undefined,
  status: undefined,
})

// 空间列表
const spaceList = ref<Space[]>([])
const spaceTree = ref<SpaceTreeNode[]>([])
const loading = ref(false)
const totalCount = ref(0)

// 视图模式
const isTreeView = ref(false)

// 树选中状态
const selectedKeys = ref<string[]>([])
const expandedKeys = ref<string[]>([])

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 表格列
const columns: TableProps['columns'] = [
  {
    title: '空间编码',
    dataIndex: 'code',
    key: 'code',
    width: 150,
  },
  {
    title: '空间名称',
    dataIndex: 'name',
    key: 'name',
    width: 200,
  },
  {
    title: '空间类型',
    dataIndex: 'type',
    key: 'type',
    width: 120,
  },
  {
    title: '上级空间',
    dataIndex: 'parentId',
    key: 'parentId',
    width: 150,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right' as const,
    width: 200,
  },
]

// 获取空间列表
const fetchSpaceList = async () => {
  loading.value = true
  try {
    const response = await spaceApi.getSpaces(queryParams)
    spaceList.value = response.data.items
    pagination.total = response.data.total
    totalCount.value = response.data.total
  } catch (error) {
    message.error('获取空间列表失败')
  } finally {
    loading.value = false
  }
}

// 获取空间树
const fetchSpaceTree = async () => {
  loading.value = true
  try {
    const response = await spaceApi.getSpaceTree('')
    spaceTree.value = response.data
    totalCount.value = countSpaces(response.data)
  } catch (error) {
    message.error('获取空间树失败')
  } finally {
    loading.value = false
  }
}

// 递归统计空间数量
const countSpaces = (nodes: SpaceTreeNode[]): number => {
  let count = nodes.length
  nodes.forEach((node) => {
    if (node.children) {
      count += countSpaces(node.children)
    }
  })
  return count
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  if (isTreeView.value) {
    fetchSpaceTree()
  } else {
    fetchSpaceList()
  }
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.type = undefined
  queryParams.status = undefined
  handleSearch()
}

// 切换视图
const handleTreeView = () => {
  isTreeView.value = !isTreeView.value
  if (isTreeView.value) {
    fetchSpaceTree()
  } else {
    fetchSpaceList()
  }
}

// 表格变化
const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  queryParams.page = pagination.current
  queryParams.size = pagination.pageSize
  fetchSpaceList()
}

// 添加空间
const handleAdd = () => {
  router.push('/spaces/create')
}

// 添加子空间
const handleAddChild = (record: Space) => {
  router.push(`/spaces/create?parentId=${record.id}`)
}

// 查看详情
const handleView = (record: Space) => {
  router.push(`/spaces/${record.id}`)
}

// 树形视图查看详情
const handleViewDetail = (event: Event) => {
  event.preventDefault()
  if (selectedKeys.value.length > 0) {
    router.push(`/spaces/${selectedKeys.value[0]}`)
  }
}

// 编辑空间
const handleEdit = (record: Space) => {
  router.push(`/spaces/${record.id}/edit`)
}

// 删除空间
const handleDelete = async (record: Space) => {
  try {
    await spaceApi.deleteSpace(record.id)
    message.success('删除成功')
    if (isTreeView.value) {
      fetchSpaceTree()
    } else {
      fetchSpaceList()
    }
  } catch (error) {
    message.error('删除失败')
  }
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

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'default',
    MAINTENANCE: 'warning',
  }
  return colorMap[status] || 'default'
}

// 获取状态徽标
const getStatusBadge = (status: string) => {
  const badgeMap: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'default',
    MAINTENANCE: 'warning',
  }
  return badgeMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    ACTIVE: '正常',
    INACTIVE: '停用',
    MAINTENANCE: '维护中',
  }
  return textMap[status] || status
}

onMounted(() => {
  fetchSpaceList()
})
</script>

<style scoped>
.space-list-container {
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

.space-tree {
  padding: 16px 0;
}

.tree-node-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}
</style>
