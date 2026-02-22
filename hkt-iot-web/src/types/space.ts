/**
 * 空间类型定义
 */

/**
 * 空间类型枚举
 */
export type SpaceType = 'PARK' | 'BUILDING' | 'FLOOR' | 'ROOM'

/**
 * 空间状态枚举
 */
export type SpaceStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'

/**
 * 使用状态枚举
 */
export type UsageStatus = 'OCCUPIED' | 'VACANT' | 'RESERVED'

/**
 * 空间实体
 */
export interface Space {
  id: string
  spaceCode: string
  spaceName: string
  spaceType: SpaceType
  spaceLevel: number
  parentSpaceId?: string
  spacePath: string
  province?: string
  city?: string
  district?: string
  address?: string
  longitude?: number
  latitude?: number
  altitude?: number
  area?: number
  boundary?: string
  extProperties?: Record<string, any>
  spaceStatus: SpaceStatus
  usageStatus: UsageStatus
  tenantId: string
  createdAt: string
  updatedAt: string
}

/**
 * 资源类型枚举
 */
export type ResourceType = 'DEVICE' | 'USER' | 'ASSET' | 'EQUIPMENT'

/**
 * 关联类型枚举
 */
export type RelationType = 'OWNER' | 'OCCUPANT' | 'MANAGER' | 'TEMPORARY'

/**
 * 资源状态枚举
 */
export type ResourceStatus = 'ACTIVE' | 'INACTIVE'

/**
 * 空间资源关联
 */
export interface SpaceResource {
  id: string
  spaceId: string
  spaceCode: string
  resourceType: ResourceType
  resourceId: string
  relationType: RelationType
  primaryRelation: boolean
  locationDetail?: string
  floorNumber?: number
  roomNumber?: string
  startDate?: string
  endDate?: string
  extProperties?: Record<string, any>
  resourceStatus: ResourceStatus
  createdAt: string
}

/**
 * 分组类型枚举
 */
export type GroupType = 'APPLICATION' | 'TENANT' | 'BUSINESS'

/**
 * 逻辑空间分组
 */
export interface LogicalSpaceGroup {
  id: string
  groupCode: string
  groupName: string
  groupType: GroupType
  groupRule?: Record<string, any>
  groupColor?: string
  groupIcon?: string
  displayOrder: number
  tenantId: string
  createdAt: string
  updatedAt: string
}

/**
 * 空间查询参数
 */
export interface SpaceQuery {
  tenantId?: string
  spaceType?: SpaceType
  spaceStatus?: SpaceStatus
  usageStatus?: UsageStatus
  parentSpaceId?: string
  spaceLevel?: number
  page: number
  size: number
  sortBy?: string
  direction?: 'ASC' | 'DESC'
}

/**
 * 创建空间请求
 */
export interface CreateSpaceRequest {
  spaceCode: string
  spaceName: string
  spaceType: SpaceType
  spaceLevel: number
  parentSpaceId?: string
  province?: string
  city?: string
  district?: string
  address?: string
  longitude?: number
  latitude?: number
  altitude?: number
  area?: number
  boundary?: string
  extProperties?: Record<string, any>
  spaceStatus?: SpaceStatus
  usageStatus?: UsageStatus
}

/**
 * 更新空间请求
 */
export interface UpdateSpaceRequest {
  spaceName?: string
  spaceType?: SpaceType
  province?: string
  city?: string
  district?: string
  address?: string
  longitude?: number
  latitude?: number
  altitude?: number
  area?: number
  boundary?: string
  extProperties?: Record<string, any>
  spaceStatus?: SpaceStatus
  usageStatus?: UsageStatus
}

/**
 * 绑定资源请求
 */
export interface BindResourceRequest {
  spaceId: string
  resourceType: ResourceType
  resourceId: string
  relationType: RelationType
  primaryRelation?: boolean
  locationDetail?: string
  floorNumber?: number
  roomNumber?: string
  startDate?: string
  endDate?: string
  extProperties?: Record<string, any>
}

/**
 * 创建空间分组请求
 */
export interface CreateSpaceGroupRequest {
  groupCode: string
  groupName: string
  groupType: GroupType
  groupRule?: Record<string, any>
  groupColor?: string
  groupIcon?: string
  displayOrder?: number
}

/**
 * 更新空间分组请求
 */
export interface UpdateSpaceGroupRequest {
  groupName?: string
  groupRule?: Record<string, any>
  groupColor?: string
  groupIcon?: string
  displayOrder?: number
}

/**
 * 空间树节点（用于前端展示）
 */
export interface SpaceTreeNode extends Space {
  key: string
  title: string
  children?: SpaceTreeNode[]
}

/**
 * 空间统计信息
 */
export interface SpaceStats {
  totalSpaces: number
  parkCount: number
  buildingCount: number
  floorCount: number
  roomCount: number
  occupiedCount: number
  vacantCount: number
  reservedCount: number
}

/**
 * 空间转移请求
 */
export interface TransferSpaceRequest {
  fromSpaceId: string
  toSpaceId: string
  resourceIds?: string[]
  transferChildren?: boolean
}

/**
 * 批量操作请求
 */
export interface BatchOperationRequest {
  spaceIds: string[]
  operation: 'ACTIVATE' | 'DEACTIVATE' | 'DELETE'
}
