import { http } from '@/utils/request'
import type { ApiResponse, PageRequest, PageResult, Space } from '@/types'
import type {
  SpaceResource,
  LogicalSpaceGroup,
  SpaceTreeNode,
} from '@/types/space'

// ==================== 类型导入和重导出 ====================

// 从 types/space.ts 导入类型
export type {
  SpaceType,
  SpaceStatus,
  UsageStatus,
  ResourceType,
  RelationType,
  ResourceStatus,
  GroupType,
  SpaceResource,
  LogicalSpaceGroup,
  SpaceQuery,
  CreateSpaceRequest,
  UpdateSpaceRequest,
  BindResourceRequest,
  CreateSpaceGroupRequest,
  UpdateSpaceGroupRequest,
  SpaceStats,
  TransferSpaceRequest,
  BatchOperationRequest,
} from '@/types/space'

// ==================== 本地接口定义（保持向后兼容） ====================

/**
 * 空间查询参数（兼容旧版本）
 * @deprecated 使用 SpaceQuery 替代
 */
export interface LegacySpaceQuery extends PageRequest {
  tenantId?: string
  parentId?: string
  type?: string
  status?: string
  keyword?: string
}

/**
 * 空间树节点（兼容旧版本）
 * @deprecated 使用 types/space.ts 中的 SpaceTreeNode 替代
 */
export interface LegacySpaceTreeNode extends Space {
  children?: LegacySpaceTreeNode[]
  deviceCount?: number
}

/**
 * 空间创建/更新请求（兼容旧版本）
 * @deprecated 使用 CreateSpaceRequest/UpdateSpaceRequest 替代
 */
export interface SpaceRequest {
  tenantId: string
  code: string
  name: string
  type: 'CAMPUS' | 'BUILDING' | 'FLOOR' | 'ROOM'
  parentId?: string
  description?: string
  metadata?: Record<string, any>
}

// ==================== 空间管理 API ====================

/**
 * 空间管理API（与后端 /api/v1/spaces 对接）
 */
export const spaceApi = {
  // ==================== 空间 CRUD ====================

  /**
   * 获取空间列表（分页）
   * @param params 查询参数
   */
  getSpaces(params: LegacySpaceQuery): Promise<ApiResponse<PageResult<Space>>> {
    return http.get('/v1/spaces', { params })
  },

  /**
   * 获取空间详情
   * @param id 空间ID
   */
  getSpace(id: string): Promise<ApiResponse<Space>> {
    return http.get(`/v1/spaces/${id}`)
  },

  /**
   * 创建空间
   * @param data 空间创建请求
   */
  createSpace(data: SpaceRequest): Promise<ApiResponse<Space>> {
    return http.post('/v1/spaces', data)
  },

  /**
   * 更新空间
   * @param id 空间ID
   * @param data 更新请求数据
   */
  updateSpace(id: string, data: Partial<SpaceRequest>): Promise<ApiResponse<Space>> {
    return http.put(`/v1/spaces/${id}`, data)
  },

  /**
   * 删除空间
   * @param id 空间ID
   */
  deleteSpace(id: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/spaces/${id}`)
  },

  // ==================== 空间层级 ====================

  /**
   * 获取子空间列表
   * @param spaceId 父空间ID
   */
  getChildSpaces(spaceId: string): Promise<ApiResponse<Space[]>> {
    return http.get(`/v1/spaces/${spaceId}/children`)
  },

  /**
   * 获取空间树
   * @param tenantId 租户ID
   * @param rootType 根节点类型（可选）
   */
  getSpaceTree(tenantId: string, rootType?: string): Promise<ApiResponse<SpaceTreeNode[]>> {
    return http.get('/v1/spaces/tree', { params: { tenantId, rootType } })
  },

  /**
   * 获取空间路径（从根到当前空间的完整路径）
   * @param spaceId 空间ID
   */
  getSpacePath(spaceId: string): Promise<ApiResponse<Space[]>> {
    return http.get(`/v1/spaces/${spaceId}/path`)
  },

  /**
   * 移动空间到新的父空间下
   * @param id 空间ID
   * @param targetParentId 目标父空间ID
   */
  moveSpace(id: string, targetParentId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/spaces/${id}/move`, { targetParentId })
  },

  // ==================== 空间设备关联 ====================

  /**
   * 获取空间的设备列表
   * @param id 空间ID
   * @param params 分页参数
   */
  getSpaceDevices(
    id: string,
    params: PageRequest
  ): Promise<ApiResponse<PageResult<any>>> {
    return http.get(`/v1/spaces/${id}/devices`, { params })
  },

  // ==================== 空间统计 ====================

  /**
   * 获取空间统计信息
   * @param id 空间ID
   */
  getSpaceStats(id: string): Promise<ApiResponse<any>> {
    return http.get(`/v1/spaces/${id}/stats`)
  },
}

// ==================== 空间资源关联 API ====================

/**
 * 空间资源关联API
 */
export const spaceResourceApi = {
  /**
   * 绑定资源到空间
   * @param spaceId 空间ID
   * @param data 绑定请求
   */
  bindResource(spaceId: string, data: {
    resourceType: string
    resourceId: string
    relationType: string
    primaryRelation?: boolean
    locationDetail?: string
  }): Promise<ApiResponse<string>> {
    return http.post(`/v1/spaces/${spaceId}/resources`, data)
  },

  /**
   * 解绑资源
   * @param resourceLinkId 资源关联ID
   */
  unbindResource(resourceLinkId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/spaces/resources/${resourceLinkId}`)
  },

  /**
   * 获取空间的资源列表
   * @param spaceId 空间ID
   */
  getSpaceResources(spaceId: string): Promise<ApiResponse<SpaceResource[]>> {
    return http.get(`/v1/spaces/${spaceId}/resources`)
  },

  /**
   * 获取资源所在空间
   * @param resourceType 资源类型
   * @param resourceId 资源ID
   */
  getResourceSpace(resourceType: string, resourceId: string): Promise<ApiResponse<Space>> {
    return http.get(`/v1/spaces/by-resource`, {
      params: { resourceType, resourceId }
    })
  },
}

// ==================== 逻辑空间分组 API ====================

/**
 * 逻辑空间分组API
 */
export const spaceGroupApi = {
  /**
   * 创建空间分组
   * @param data 创建请求
   */
  createSpaceGroup(data: {
    groupCode: string
    groupName: string
    groupType: string
    groupColor?: string
    groupIcon?: string
  }): Promise<ApiResponse<string>> {
    return http.post('/v1/space-groups', data)
  },

  /**
   * 获取租户的空间分组列表
   * @param tenantId 租户ID
   */
  getSpaceGroups(tenantId: string): Promise<ApiResponse<LogicalSpaceGroup[]>> {
    return http.get('/v1/space-groups', {
      params: { tenantId }
    })
  },

  /**
   * 获取分组详情
   * @param groupId 分组ID
   */
  getSpaceGroup(groupId: string): Promise<ApiResponse<LogicalSpaceGroup>> {
    return http.get(`/v1/space-groups/${groupId}`)
  },

  /**
   * 更新空间分组
   * @param groupId 分组ID
   * @param data 更新请求
   */
  updateSpaceGroup(groupId: string, data: Partial<{
    groupName: string
    groupColor: string
    groupIcon: string
  }>): Promise<ApiResponse<void>> {
    return http.put(`/v1/space-groups/${groupId}`, data)
  },

  /**
   * 删除空间分组
   * @param groupId 分组ID
   */
  deleteSpaceGroup(groupId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/space-groups/${groupId}`)
  },

  /**
   * 添加空间到分组
   * @param groupId 分组ID
   * @param spaceId 空间ID
   */
  addSpaceToGroup(groupId: string, spaceId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/space-groups/${groupId}/spaces/${spaceId}`)
  },

  /**
   * 从分组移除空间
   * @param groupId 分组ID
   * @param spaceId 空间ID
   */
  removeSpaceFromGroup(groupId: string, spaceId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/space-groups/${groupId}/spaces/${spaceId}`)
  },

  /**
   * 获取分组下的空间列表
   * @param groupId 分组ID
   * @param params 分页参数
   */
  getGroupSpaces(groupId: string, params?: PageRequest): Promise<ApiResponse<PageResult<Space>>> {
    return http.get(`/v1/space-groups/${groupId}/spaces`, { params })
  },
}
