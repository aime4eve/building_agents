<template>
  <div class="user-list-container">
    <a-card :bordered="false">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="queryForm" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="queryForm.keyword" placeholder="用户名/邮箱/手机号" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryForm.status" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option value="ACTIVE">正常</a-select-option>
            <a-select-option value="INACTIVE">未激活</a-select-option>
            <a-select-option value="LOCKED">锁定</a-select-option>
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
            <PlusOutlined /> 新增用户
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
          <template v-else-if="column.key === 'roles'">
            <a-tag v-for="role in record.roles" :key="role.id" color="blue">
              {{ role.name }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleResetPwd(record)">重置密码</a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleToggleStatus(record)">
                      {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
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
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="formData.username" placeholder="请输入用户名" :disabled="editMode === 'edit'" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="角色" name="roleIds">
          <a-select v-model:value="formData.roleIds" mode="multiple" placeholder="请选择角色">
            <a-select-option v-for="role in roleList" :key="role.id" :value="role.id">
              {{ role.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="editMode === 'add'" label="初始密码" name="password">
          <a-input-password v-model:value="formData.password" placeholder="请输入初始密码" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="formData.status" placeholder="请选择状态">
            <a-select-option value="ACTIVE">正常</a-select-option>
            <a-select-option value="INACTIVE">未激活</a-select-option>
            <a-select-option value="LOCKED">锁定</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 重置密码弹窗 -->
    <a-modal
      v-model:open="resetPwdVisible"
      title="重置密码"
      :width="400"
      @ok="handleResetPwdOk"
      @cancel="resetPwdVisible = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="新密码">
          <a-input-password v-model:value="newPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item label="确认密码">
          <a-input-password v-model:value="confirmPassword" placeholder="请再次输入新密码" />
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
import { userApi, roleApi, type UserRequest } from '@/api/tenant'
import type { User, Role } from '@/types'

// 查询表单
const queryForm = reactive({
  keyword: '',
  status: undefined as string | undefined,
})

// 数据源
const dataSource = ref<User[]>([])
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
  { title: '用户名', dataIndex: 'username', key: 'username', width: 150 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 200 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 150 },
  { title: '角色', dataIndex: 'roles', key: 'roles', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '最后登录', dataIndex: 'lastLoginAt', key: 'lastLoginAt', width: 180 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
]

// 弹窗相关
const modalVisible = ref(false)
const modalTitle = ref('')
const editMode = ref<'add' | 'edit'>('add')
const currentRecord = ref<User | null>(null)

// 表单数据
const formData = reactive<Partial<UserRequest> & { password?: string }>({
  username: '',
  email: '',
  phone: '',
  roleIds: [],
  status: 'ACTIVE',
  password: '',
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  roleIds: [{ required: true, message: '请选择角色', trigger: 'change', type: 'array' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
}

// 角色列表
const roleList = ref<Role[]>([])

// 重置密码相关
const resetPwdVisible = ref(false)
const newPassword = ref('')
const confirmPassword = ref('')
const resetPwdUser = ref<User | null>(null)

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await userApi.getUsers({
      page: pagination.current,
      size: pagination.pageSize,
      ...queryForm,
    })
    dataSource.value = response.data.items
    pagination.total = response.data.total
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const fetchRoles = async () => {
  try {
    const response = await roleApi.getAllRoles('')
    roleList.value = response.data
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUsers()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.status = undefined
  handleSearch()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUsers()
}

// 新增
const handleAdd = () => {
  editMode.value = 'add'
  modalTitle.value = '新增用户'
  modalVisible.value = true
  Object.assign(formData, {
    username: '',
    email: '',
    phone: '',
    roleIds: [],
    status: 'ACTIVE',
    password: '',
  })
}

// 编辑
const handleEdit = (record: User) => {
  editMode.value = 'edit'
  modalTitle.value = '编辑用户'
  currentRecord.value = record
  modalVisible.value = true
  Object.assign(formData, {
    username: record.username,
    email: record.email,
    phone: record.phone || '',
    roleIds: record.roles.map((r) => r.id),
    status: record.status,
  })
}

// 查看
const handleView = (record: User) => {
  message.info('查看功能开发中')
}

// 重置密码
const handleResetPwd = (record: User) => {
  resetPwdUser.value = record
  newPassword.value = ''
  confirmPassword.value = ''
  resetPwdVisible.value = true
}

// 确认重置密码
const handleResetPwdOk = async () => {
  if (!newPassword.value) {
    message.error('请输入新密码')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    message.error('两次密码输入不一致')
    return
  }
  try {
    await userApi.resetPassword(resetPwdUser.value!.id, newPassword.value)
    message.success('密码重置成功')
    resetPwdVisible.value = false
  } catch (error) {
    message.error('密码重置失败')
  }
}

// 切换状态
const handleToggleStatus = (record: User) => {
  const newStatus = record.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  Modal.confirm({
    title: '确认操作',
    content: `确定要${newStatus === 'ACTIVE' ? '启用' : '禁用'}该用户吗？`,
    onOk: async () => {
      try {
        await userApi.toggleUserStatus(record.id, newStatus)
        message.success('操作成功')
        fetchUsers()
      } catch (error) {
        message.error('操作失败')
      }
    },
  })
}

// 删除
const handleDelete = (record: User) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该用户吗？此操作不可恢复。',
    onOk: async () => {
      try {
        await userApi.deleteUser(record.id)
        message.success('删除成功')
        fetchUsers()
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
      await userApi.createUser(formData as UserRequest)
      message.success('创建成功')
    } else {
      await userApi.updateUser(currentRecord.value!.id, formData)
      message.success('更新成功')
    }
    modalVisible.value = false
    fetchUsers()
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
    INACTIVE: 'default',
    LOCKED: 'error',
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    ACTIVE: '正常',
    INACTIVE: '未激活',
    LOCKED: '锁定',
  }
  return textMap[status] || status
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
.user-list-container {
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
}

.table-actions {
  margin-bottom: 16px;
}
</style>
