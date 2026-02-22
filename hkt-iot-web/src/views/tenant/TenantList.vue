<template>
  <div class="tenant-list-container">
    <a-card :bordered="false">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="queryForm" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="queryForm.keyword" placeholder="名称/编码" allow-clear />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="queryForm.type" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="OPERATOR">运营商</a-select-option>
            <a-select-option value="GROUP">集团</a-select-option>
            <a-select-option value="SUBSIDIARY">子公司</a-select-option>
            <a-select-option value="ENTERPRISE">企业</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryForm.status" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="ACTIVE">正常</a-select-option>
            <a-select-option value="SUSPENDED">暂停</a-select-option>
            <a-select-option value="TERMINATED">终止</a-select-option>
          </a-select>
        </a-form-item>
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

      <!-- 操作栏 -->
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined /> 新增租户
          </a-button>
          <a-button type="primary" ghost @click="handleExport">
            <DownloadOutlined /> 导出
          </a-button>
        </a-space>
      </div>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'type'">
            <a-tag>{{ getTypeText(record.type) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleToggleStatus(record)">
                      {{ record.status === 'ACTIVE' ? '暂停' : '启用' }}
                    </a-menu-item>
                    <a-menu-item @click="handleDelete(record)" danger>删除</a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">更多 <DownOutlined /></a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :width="600"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="租户名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入租户名称" />
        </a-form-item>
        <a-form-item label="租户编码" name="code">
          <a-input v-model:value="formData.code" placeholder="请输入租户编码" />
        </a-form-item>
        <a-form-item label="租户类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择租户类型">
            <a-select-option value="OPERATOR">运营商</a-select-option>
            <a-select-option value="GROUP">集团</a-select-option>
            <a-select-option value="SUBSIDIARY">子公司</a-select-option>
            <a-select-option value="ENTERPRISE">企业</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="上级租户" name="parentId">
          <a-select v-model:value="formData.parentId" placeholder="请选择上级租户" allow-clear>
            <a-select-option v-for="item in tenantList" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="许可证配额" name="licenseQuota">
          <a-input-number v-model:value="formData.licenseQuota" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="用户配额" name="userQuota">
          <a-input-number v-model:value="formData.userQuota" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="设备配额" name="deviceQuota">
          <a-input-number v-model:value="formData.deviceQuota" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DownloadOutlined,
  DownOutlined,
} from '@ant-design/icons-vue'
import { tenantApi, type TenantRequest } from '@/api/tenant'
import type { Tenant } from '@/types'

// 查询表单
const queryForm = reactive({
  keyword: '',
  type: undefined as string | undefined,
  status: undefined as string | undefined,
})

// 数据源
const dataSource = ref<Tenant[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 表格列定义
const columns = [
  { title: '租户名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '租户编码', dataIndex: 'code', key: 'code', width: 150 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '许可证配额', dataIndex: 'licenseQuota', key: 'licenseQuota', width: 120 },
  { title: '用户配额', dataIndex: 'userQuota', key: 'userQuota', width: 100 },
  { title: '设备配额', dataIndex: 'deviceQuota', key: 'deviceQuota', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' },
]

// 弹窗相关
const modalVisible = ref(false)
const modalTitle = ref('')
const editMode = ref<'add' | 'edit'>('add')
const currentRecord = ref<Tenant | null>(null)

// 表单数据
const formData = reactive<Partial<TenantRequest>>({
  name: '',
  code: '',
  type: 'ENTERPRISE',
  licenseQuota: 100,
  userQuota: 100,
  deviceQuota: 1000,
})

const formRules = {
  name: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择租户类型', trigger: 'change' }],
}

// 租户列表（用于上级租户选择）
const tenantList = ref<Tenant[]>([])

// 获取租户列表
const fetchTenants = async () => {
  loading.value = true
  try {
    const response = await tenantApi.getTenants({
      page: pagination.current,
      size: pagination.pageSize,
      ...queryForm,
    })
    dataSource.value = response.data.items
    pagination.total = response.data.total
  } catch (error) {
    console.error('获取租户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取所有租户（用于下拉选择）
const fetchAllTenants = async () => {
  try {
    const response = await tenantApi.getTenants({ page: 1, size: 1000 })
    tenantList.value = response.data.items
  } catch (error) {
    console.error('获取租户列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchTenants()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.type = undefined
  queryForm.status = undefined
  handleSearch()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchTenants()
}

// 新增
const handleAdd = () => {
  editMode.value = 'add'
  modalTitle.value = '新增租户'
  modalVisible.value = true
  Object.assign(formData, {
    name: '',
    code: '',
    type: 'ENTERPRISE',
    parentId: undefined,
    licenseQuota: 100,
    userQuota: 100,
    deviceQuota: 1000,
    description: '',
  })
}

// 编辑
const handleEdit = (record: Tenant) => {
  editMode.value = 'edit'
  modalTitle.value = '编辑租户'
  currentRecord.value = record
  modalVisible.value = true
  Object.assign(formData, {
    name: record.name,
    code: record.code,
    type: record.type,
    parentId: record.parentId,
    licenseQuota: record.licenseQuota,
    userQuota: record.userQuota,
    deviceQuota: record.deviceQuota,
  })
}

// 查看
const handleView = (record: Tenant) => {
  message.info('查看功能开发中')
}

// 切换状态
const handleToggleStatus = (record: Tenant) => {
  const newStatus = record.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'
  Modal.confirm({
    title: '确认操作',
    content: `确定要${newStatus === 'ACTIVE' ? '启用' : '暂停'}该租户吗？`,
    onOk: async () => {
      try {
        await tenantApi.toggleTenantStatus(record.id, newStatus)
        message.success('操作成功')
        fetchTenants()
      } catch (error) {
        message.error('操作失败')
      }
    },
  })
}

// 删除
const handleDelete = (record: Tenant) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该租户吗？此操作不可恢复。',
    onOk: async () => {
      try {
        await tenantApi.deleteTenant(record.id)
        message.success('删除成功')
        fetchTenants()
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 导出
const handleExport = () => {
  message.info('导出功能开发中')
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    if (editMode.value === 'add') {
      await tenantApi.createTenant(formData as TenantRequest)
      message.success('创建成功')
    } else {
      await tenantApi.updateTenant(currentRecord.value!.id, formData)
      message.success('更新成功')
    }
    modalVisible.value = false
    fetchTenants()
  } catch (error) {
    message.error(editMode.value === 'add' ? '创建失败' : '更新失败')
  }
}

// 弹窗取消
const handleModalCancel = () => {
  modalVisible.value = false
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    ACTIVE: 'success',
    SUSPENDED: 'warning',
    TERMINATED: 'error',
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    ACTIVE: '正常',
    SUSPENDED: '暂停',
    TERMINATED: '终止',
  }
  return textMap[status] || status
}

// 获取类型文本
const getTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    OPERATOR: '运营商',
    GROUP: '集团',
    SUBSIDIARY: '子公司',
    ENTERPRISE: '企业',
  }
  return textMap[type] || type
}

onMounted(() => {
  fetchTenants()
  fetchAllTenants()
})
</script>

<style scoped>
.tenant-list-container {
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
}

.table-actions {
  margin-bottom: 16px;
}
</style>
