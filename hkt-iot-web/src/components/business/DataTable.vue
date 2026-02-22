<template>
  <div class="data-table">
    <a-card :bordered="false">
      <!-- 搜索表单 -->
      <div v-if="showSearch" class="search-form">
        <a-form layout="inline" :model="searchForm" class="form">
          <slot name="search-form" :form="searchForm">
            <!-- 默认搜索表单 -->
          </slot>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <SearchOutlined /> 查询
              </a-button>
              <a-button @click="handleReset">
                <ReloadOutlined /> 重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 操作栏 -->
      <div v-if="$slots['table-actions'] || showTableActions" class="table-actions">
        <slot name="table-actions">
          <a-space>
            <a-button type="primary" @click="$emit('add')">
              <PlusOutlined /> 新增
            </a-button>
            <a-button type="primary" ghost @click="$emit('export')">
              <DownloadOutlined /> 导出
            </a-button>
          </a-space>
        </slot>
      </div>

      <!-- 表格 -->
      <a-table
        v-bind="$attrs"
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="paginationConfig"
        :row-selection="rowSelectionConfig"
        :row-key="rowKey"
        :scroll="{ x: scrollX }"
        @change="handleTableChange"
      >
        <!-- 默认插槽 -->
        <template v-for="slot in Object.keys($slots)" #[slot]="slotProps">
          <slot :name="slot" v-bind="slotProps"></slot>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TableProps, ColumnProps } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'

interface Props {
  // 列定义
  columns: ColumnProps[]
  // 数据加载函数
  loadData: (params: any) => Promise<{ items: any[]; total: number }>
  // 搜索表单默认值
  searchForm?: Record<string, any>
  // 是否显示搜索表单
  showSearch?: boolean
  // 是否显示表格操作栏
  showTableActions?: boolean
  // 行key
  rowKey?: string | ((record: any) => string)
  // 是否支持行选择
  showRowSelection?: boolean
  // 表格横向滚动
  scrollX?: number | string
  // 分页配置
  pageSizeOptions?: number[]
}

const props = withDefaults(defineProps<Props>(), {
  searchForm: () => ({}),
  showSearch: true,
  showTableActions: true,
  rowKey: 'id',
  showRowSelection: false,
  scrollX: 1200,
  pageSizeOptions: () => [10, 20, 50, 100],
})

const emit = defineEmits<{
  (e: 'add'): void
  (e: 'export'): void
  (e: 'search', params: any): void
  (e: 'reset'): void
}>()

// 数据源
const dataSource = ref<any[]>([])
const loading = ref(false)

// 分页
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: props.pageSizeOptions,
})

// 分页配置
const paginationConfig = computed(() => ({
  ...pagination.value,
  pageSizeOptions: props.pageSizeOptions,
}))

// 行选择
const selectedRowKeys = ref<any[]>([])
const rowSelectionConfig = computed(() => {
  if (!props.showRowSelection) return undefined
  return {
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys: any[]) => {
      selectedRowKeys.value = keys
      emit('selection-change', keys)
    },
  }
})

// 内部搜索表单
const internalSearchForm = ref<Record<string, any>>({ ...props.searchForm })

// 加载数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.current,
      size: pagination.value.pageSize,
      ...internalSearchForm.value,
    }
    const response = await props.loadData(params)
    dataSource.value = response.items
    pagination.value.total = response.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  emit('search', internalSearchForm.value)
  fetchData()
}

// 重置
const handleReset = () => {
  internalSearchForm.value = { ...props.searchForm }
  emit('reset')
  handleSearch()
}

// 表格变化
const handleTableChange: TableProps['onChange'] = (pag, filters, sorter) => {
  pagination.value.current = pag.current || 1
  pagination.value.pageSize = pag.pageSize || 10
  fetchData()
}

// 刷新
const refresh = () => {
  fetchData()
}

// 获取选中的行
const getSelectedRows = () => {
  return dataSource.value.filter((item) =>
    selectedRowKeys.value.includes(typeof props.rowKey === 'function' ? props.rowKey(item) : item[props.rowKey])
  )
}

// 暴露方法
defineExpose({
  refresh,
  getSelectedRows,
})

// 监听搜索表单变化
watch(
  () => props.searchForm,
  (newVal) => {
    internalSearchForm.value = { ...newVal }
  },
  { deep: true }
)

// 初始加载
fetchData()
</script>

<style scoped>
.data-table {
  padding: 0;
}

.search-form {
  margin-bottom: 16px;
}

.search-form .form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.table-actions {
  margin-bottom: 16px;
}
</style>
