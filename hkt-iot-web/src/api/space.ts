import { http } from '@/utils/request'
import type { ApiResponse, PageRequest, PageResult, Space } from '@/types'

// 空间查询参数
export interface SpaceQuery extends PageRequest {
  tenantId?: string
  parentId?: string
  type?: string
  status?: string
  keyword?: string
}

// 空间树节点
export interface SpaceTreeNode extends Space {
  children?: SpaceTreeNode[]
  deviceCount?: number
}

// 空间创建/更新请求
export interface SpaceRequest {
  tenantId: string
  code: string
  name: string
  type: 'CAMPUS' | 'BUILDING' | 'FLOOR' | 'ROOM'
  parentId?: string
  description?: string
  metadata?: Record<string, any>
}

// 空间API
export const spaceApi = {
  // 获取空间列表
  getSpaces(params: SpaceQuery): Promise<ApiResponse<PageResult<Space>>> {
    return http.get('/v1/spaces', { params })
  },

  // 获取空间树
  getSpaceTree(tenantId: string, rootType?: string): Promise<ApiResponse<SpaceTreeNode[]>> {
    return http.get('/v1/spaces/tree', { params: { tenantId, rootType } })
  },

  // 获取空间详情
  getSpace(id: string): Promise<ApiResponse<Space>> {
    return http.get(`/v1/spaces/${id}`)
  },

  // 创建空间
  createSpace(data: SpaceRequest): Promise<ApiResponse<Space>> {
    return http.post('/v1/spaces', data)
  },

  // 更新空间
  updateSpace(id: string, data: Partial<SpaceRequest>): Promise<ApiResponse<Space>> {
    return http.put(`/v1/spaces/${id}`, data)
  },

  // 删除空间
  deleteSpace(id: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/spaces/${id}`)
  },

  // 获取空间的设备列表
  getSpaceDevices(
    id: string,
    params: PageRequest
  ): Promise<ApiResponse<PageResult<any>>> {
    return http.get(`/v1/spaces/${id}/devices`, { params })
  },

  // 获取空间统计信息
  getSpaceStats(id: string): Promise<ApiResponse<any>> {
    return http.get(`/v1/spaces/${id}/stats`)
  },

  // 移动空间
  moveSpace(id: string, targetParentId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/spaces/${id}/move`, { targetParentId })
  },
}
