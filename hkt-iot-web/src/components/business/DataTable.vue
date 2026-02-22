<template>
  <div class="data-table-component">
    <a-card :bordered="false" class="table-card" :body-style="{ padding: '20px' }">
      <!-- 搜索表单 -->
      <div v-if="showSearch" class="search-form-wrapper">
        <a-form layout="inline" :model="searchForm" class="search-form">
          <slot name="search-form" :form="searchForm">
            <!-- 默认搜索表单 -->
          </slot>
          <a-form-item class="search-actions">
            <a-space>
              <a-button type="primary" @click="handleSearch" class="btn-search">
                <SearchOutlined /> 查询
              </a-button>
              <a-button @click="handleReset" class="btn-reset">
                <ReloadOutlined /> 重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 操作栏 -->
      <div v-if="$slots['table-actions'] || showTableActions" class="table-toolbar">
        <slot name="table-actions">
          <a-space class="toolbar-left">
            <a-button type="primary" @click="$emit('add')" class="btn-add">
              <PlusOutlined /> 新增
            </a-button>
            <a-button type="primary" ghost @click="$emit('export')" class="btn-export">
              <DownloadOutlined /> 导出
            </a-button>
          </a-space>
          <slot name="toolbar-right"></slot>
        </slot>
      </div>

      <!-- 表格 -->
      <div class="table-wrapper">
        <a-table
          v-bind="$attrs"
          :columns="columns"
          :data-source="dataSource"
          :loading="loading"
          :pagination="paginationConfig"
          :row-selection="rowSelectionConfig"
          :row-key="rowKey"
          :scroll="{ x: scrollX }"
          :class="'styled-table'"
          @change="handleTableChange"
        >
          <!-- 默认插槽 -->
          <template v-for="slot in Object.keys($slots)" #[slot]="slotProps">
            <slot :name="slot" v-bind="slotProps"></slot>
          </template>
        </a-table>
      </div>
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

<style scoped lang="less">
@primary-color: #1890ff;
@success-color: #52c41a;
@border-radius: 12px;
@shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
@transition-base: all 0.2s ease;

.data-table-component {
  .table-card {
    border-radius: @border-radius;
    box-shadow: @shadow-md;
    transition: @transition-base;

    &:hover {
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
    }
  }

  // 搜索表单
  .search-form-wrapper {
    background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
    padding: 20px;
    border-radius: @border-radius;
    margin-bottom: 20px;
    border: 1px solid #f0f0f0;

    .search-form {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: flex-end;

      :deep(.ant-input),
      :deep(.ant-select-selector) {
        border-radius: 8px;
        transition: @transition-base;

        &:hover {
          border-color: @primary-color;
        }
      }
    }

    .search-actions {
      margin-left: auto;

      .ant-btn {
        border-radius: 8px;
        padding: 6px 20px;
        font-weight: 500;

        &.btn-search {
          background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
          border: none;
          box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);

          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
          }
        }

        &.btn-reset {
          &:hover {
            border-color: @primary-color;
            color: @primary-color;
          }
        }
      }
    }
  }

  // 表格工具栏
  .table-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 12px;

    .toolbar-left {
      .ant-btn {
        border-radius: 8px;
        padding: 6px 16px;
        font-weight: 500;
        transition: @transition-base;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
        }
      }
    }
  }

  // 表格样式
  .table-wrapper {
    :deep(.ant-table) {
      font-size: 14px;
      border-radius: @border-radius;
      overflow: hidden;

      thead > tr > th {
        background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
        color: #262626;
        font-weight: 600;
        padding: 14px 16px;
        border: none;
        white-space: nowrap;

        &:first-child {
          border-top-left-radius: @border-radius;
        }

        &:last-child {
          border-top-right-radius: @border-radius;
        }
      }

      tbody > tr {
        transition: @transition-base;

        &:hover {
          background: #fafafa;
        }

        > td {
          padding: 14px 16px;
          border-color: #f0f0f0;
        }
      }
    }

    // 分页器样式
    :deep(.ant-pagination) {
      margin-top: 16px;

      .ant-pagination-item {
        border-radius: 8px;
        transition: @transition-base;

        &:hover {
          border-color: @primary-color;
          transform: translateY(-1px);
        }

        &.ant-pagination-item-active {
          background: @primary-color;
          border-color: @primary-color;

          a {
            color: #fff;
          }
        }
      }
    }
  }
}
</style>
