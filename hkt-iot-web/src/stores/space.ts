import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { spaceApi, type SpaceQuery, type SpaceTreeNode } from '@/api/space'
import type { Space } from '@/types'
import { message } from 'ant-design-vue'

export const useSpaceStore = defineStore('space', () => {
  // 状态
  const spaceList = ref<Space[]>([])
  const spaceTree = ref<SpaceTreeNode[]>([])
  const currentSpace = ref<Space | null>(null)
  const selectedSpaceId = ref<string | null>(null)
  const loading = ref(false)
  const total = ref(0)

  // 查询参数
  const queryParams = ref<SpaceQuery>({
    page: 1,
    size: 10,
    keyword: '',
    type: undefined,
    status: undefined,
  })

  // 计算属性
  const spaceTreeFlat = computed(() => {
    const flatten = (nodes: SpaceTreeNode[]): Space[] => {
      const result: Space[] = []
      nodes.forEach((node) => {
        result.push({
          id: node.id,
          code: node.code,
          name: node.name,
          type: node.type,
          status: node.status,
          parentId: node.parentId,
          tenantId: node.tenantId,
          createdAt: node.createdAt,
          updatedAt: node.updatedAt,
        })
        if (node.children) {
          result.push(...flatten(node.children))
        }
      })
      return result
    }
    return flatten(spaceTree.value)
  })

  // 根据ID查找空间
  const findSpaceById = computed(() => {
    return (id: string): Space | undefined => {
      return spaceList.value.find((s) => s.id === id) ||
        spaceTreeFlat.value.find((s) => s.id === id)
    }
  })

  // 获取子空间
  const getChildSpaces = computed(() => {
    return (parentId: string): Space[] => {
      return spaceTreeFlat.value.filter((s) => s.parentId === parentId)
    }
  })

  // 获取空间路径（从根到当前空间）
  const getSpacePath = computed(() => {
    return (spaceId: string): Space[] => {
      const path: Space[] = []
      let current = findSpaceById.value(spaceId)

      while (current) {
        path.unshift(current)
        if (current.parentId) {
          current = findSpaceById.value(current.parentId)
        } else {
          break
        }
      }

      return path
    }
  })

  // 获取空间列表
  const fetchSpaceList = async (params?: Partial<SpaceQuery>) => {
    loading.value = true
    try {
      const mergedParams = { ...queryParams.value, ...params }
      const response = await spaceApi.getSpaces(mergedParams)
      spaceList.value = response.data.items
      total.value = response.data.total
      queryParams.value = mergedParams
      return response.data
    } catch (error) {
      message.error('获取空间列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取空间树
  const fetchSpaceTree = async (tenantId?: string, rootType?: string) => {
    loading.value = true
    try {
      const response = await spaceApi.getSpaceTree(tenantId || '', rootType)
      spaceTree.value = response.data
      return response.data
    } catch (error) {
      message.error('获取空间树失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取空间详情
  const fetchSpaceDetail = async (id: string) => {
    loading.value = true
    try {
      const response = await spaceApi.getSpace(id)
      currentSpace.value = response.data
      return response.data
    } catch (error) {
      message.error('获取空间详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 创建空间
  const createSpace = async (data: Parameters<typeof spaceApi.createSpace>[0]) => {
    loading.value = true
    try {
      const response = await spaceApi.createSpace(data)
      message.success('创建空间成功')

      // 刷新列表
      await fetchSpaceList()
      await fetchSpaceTree(data.tenantId)

      return response.data
    } catch (error) {
      message.error('创建空间失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新空间
  const updateSpace = async (id: string, data: Parameters<typeof spaceApi.updateSpace>[1]) => {
    loading.value = true
    try {
      const response = await spaceApi.updateSpace(id, data)
      message.success('更新空间成功')

      // 更新当前空间
      if (currentSpace.value?.id === id) {
        await fetchSpaceDetail(id)
      }

      // 刷新列表
      await fetchSpaceList()

      return response.data
    } catch (error) {
      message.error('更新空间失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 删除空间
  const deleteSpace = async (id: string) => {
    loading.value = true
    try {
      await spaceApi.deleteSpace(id)
      message.success('删除空间成功')

      // 清除当前空间
      if (currentSpace.value?.id === id) {
        currentSpace.value = null
      }

      // 刷新列表
      await fetchSpaceList()
      await fetchSpaceTree()

      return true
    } catch (error) {
      message.error('删除空间失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 移动空间
  const moveSpace = async (id: string, targetParentId: string) => {
    loading.value = true
    try {
      await spaceApi.moveSpace(id, targetParentId)
      message.success('移动空间成功')

      // 刷新列表
      await fetchSpaceList()
      await fetchSpaceTree()

      return true
    } catch (error) {
      message.error('移动空间失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 设置查询参数
  const setQueryParams = (params: Partial<SpaceQuery>) => {
    queryParams.value = { ...queryParams.value, ...params }
  }

  // 重置查询参数
  const resetQueryParams = () => {
    queryParams.value = {
      page: 1,
      size: 10,
      keyword: '',
      type: undefined,
      status: undefined,
    }
  }

  // 选择空间
  const selectSpace = (id: string | null) => {
    selectedSpaceId.value = id
    if (id) {
      fetchSpaceDetail(id)
    } else {
      currentSpace.value = null
    }
  }

  // 清空状态
  const clearState = () => {
    spaceList.value = []
    spaceTree.value = []
    currentSpace.value = null
    selectedSpaceId.value = null
    total.value = 0
    resetQueryParams()
  }

  return {
    // 状态
    spaceList,
    spaceTree,
    currentSpace,
    selectedSpaceId,
    loading,
    total,
    queryParams,

    // 计算属性
    spaceTreeFlat,
    findSpaceById,
    getChildSpaces,
    getSpacePath,

    // 方法
    fetchSpaceList,
    fetchSpaceTree,
    fetchSpaceDetail,
    createSpace,
    updateSpace,
    deleteSpace,
    moveSpace,
    setQueryParams,
    resetQueryParams,
    selectSpace,
    clearState,
  }
})
