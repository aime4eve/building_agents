import { http } from '@/utils/request'
import type { ApiResponse, PageRequest, PageResult, Tenant, User } from '@/types'

// 租户配额信息
export interface TenantQuota {
  maxUsers: number
  maxDevices: number
  maxSpaces: number
  maxRules: number
  maxStorageGB: number
}

// 租户查询参数
export interface TenantQuery extends PageRequest {
  keyword?: string
  type?: string
  status?: string
  parentId?: string
}

// 租户创建/更新请求
export interface TenantRequest {
  name: string
  code: string
  type: 'OPERATOR' | 'GROUP' | 'SUBSIDIARY' | 'ENTERPRISE'
  parentId?: string
  description?: string
  metadata?: Record<string, any>
  licenseQuota?: number
  userQuota?: number
  deviceQuota?: number
}

// 用户查询参数
export interface UserQuery extends PageRequest {
  tenantId?: string
  keyword?: string
  status?: string
  roleId?: string
}

// 用户创建/更新请求
export interface UserRequest {
  tenantId: string
  username: string
  email: string
  phone?: string
  roleId?: string
  roleIds?: string[]
  status?: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
}

// 角色相关类型
export interface Role {
  id: string
  name: string
  code: string
  description?: string
  tenantId: string
  permissions: Permission[]
  createdAt: string
  updatedAt: string
}

export interface Permission {
  id: string
  code: string
  name: string
  resource: string
  action: string
  description?: string
}

// 角色查询参数
export interface RoleQuery extends PageRequest {
  tenantId?: string
  keyword?: string
}

// 角色创建/更新请求
export interface RoleRequest {
  tenantId: string
  name: string
  code: string
  description?: string
  permissionIds: string[]
}

// 租户API
export const tenantApi = {
  // 获取租户列表
  getTenants(params: TenantQuery): Promise<ApiResponse<PageResult<Tenant>>> {
    return http.get('/v1/tenants', { params })
  },

  // 获取租户详情
  getTenant(id: string): Promise<ApiResponse<Tenant>> {
    return http.get(`/v1/tenants/${id}`)
  },

  // 创建租户
  createTenant(data: TenantRequest): Promise<ApiResponse<Tenant>> {
    return http.post('/v1/tenants', data)
  },

  // 更新租户
  updateTenant(id: string, data: Partial<TenantRequest>): Promise<ApiResponse<Tenant>> {
    return http.put(`/v1/tenants/${id}`, data)
  },

  // 删除租户
  deleteTenant(id: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/tenants/${id}`)
  },

  // 启用租户
  activateTenant(id: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/tenants/${id}/activate`)
  },

  // 暂停租户
  suspendTenant(id: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/tenants/${id}/suspend`)
  },

  // 启用/禁用租户（向后兼容）
  toggleTenantStatus(id: string, status: 'ACTIVE' | 'SUSPENDED'): Promise<ApiResponse<void>> {
    return status === 'ACTIVE'
      ? http.post(`/v1/tenants/${id}/activate`)
      : http.post(`/v1/tenants/${id}/suspend`)
  },

  // 获取租户子级列表
  getTenantChildren(id: string, params?: PageRequest): Promise<ApiResponse<PageResult<Tenant>>> {
    return http.get(`/v1/tenants/${id}/children`, { params })
  },

  // 获取租户配额
  getTenantQuota(id: string): Promise<ApiResponse<TenantQuota>> {
    return http.get(`/v1/tenants/${id}/quota`)
  },

  // 更新租户配额
  updateTenantQuota(id: string, quota: Partial<TenantQuota>): Promise<ApiResponse<void>> {
    return http.put(`/v1/tenants/${id}/quota`, quota)
  },

  // 获取租户统计
  getTenantStats(id: string): Promise<ApiResponse<any>> {
    return http.get(`/v1/tenants/${id}/stats`)
  },
}

// 用户API
export const userApi = {
  // 获取用户列表
  getUsers(params: UserQuery): Promise<ApiResponse<PageResult<User>>> {
    return http.get('/v1/users', { params })
  },

  // 获取用户详情
  getUser(id: string): Promise<ApiResponse<User>> {
    return http.get(`/v1/users/${id}`)
  },

  // 创建用户
  createUser(data: UserRequest): Promise<ApiResponse<User>> {
    return http.post('/v1/users', data)
  },

  // 更新用户
  updateUser(id: string, data: Partial<UserRequest>): Promise<ApiResponse<User>> {
    return http.put(`/v1/users/${id}`, data)
  },

  // 删除用户
  deleteUser(id: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/users/${id}`)
  },

  // 重置用户密码
  resetPassword(id: string, newPassword: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/users/${id}/reset-password`, { newPassword })
  },

  // 启用/禁用用户
  toggleUserStatus(id: string, status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'): Promise<ApiResponse<void>> {
    return http.patch(`/v1/users/${id}/status`, { status })
  },

  // 分配角色
  assignRoles(id: string, roleIds: string[]): Promise<ApiResponse<void>> {
    return http.post(`/v1/users/${id}/roles`, { roleIds })
  },
}

// 角色API
export const roleApi = {
  // 获取角色列表
  getRoles(params: RoleQuery): Promise<ApiResponse<PageResult<Role>>> {
    return http.get('/v1/roles', { params })
  },

  // 获取所有角色（不分页，用于下拉选择）
  getAllRoles(tenantId: string): Promise<ApiResponse<Role[]>> {
    return http.get('/v1/roles/all', { params: { tenantId } })
  },

  // 获取角色详情
  getRole(id: string): Promise<ApiResponse<Role>> {
    return http.get(`/v1/roles/${id}`)
  },

  // 创建角色
  createRole(data: RoleRequest): Promise<ApiResponse<Role>> {
    return http.post('/v1/roles', data)
  },

  // 更新角色
  updateRole(id: string, data: Partial<RoleRequest>): Promise<ApiResponse<Role>> {
    return http.put(`/v1/roles/${id}`, data)
  },

  // 删除角色
  deleteRole(id: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/roles/${id}`)
  },

  // 获取所有权限
  getAllPermissions(): Promise<ApiResponse<Permission[]>> {
    return http.get('/v1/permissions/all')
  },
}
