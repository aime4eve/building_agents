<template>
  <div class="space-form-container">
    <a-page-header
      :title="isEdit ? '编辑空间' : '创建空间'"
      @back="handleBack"
      class="page-header"
    />

    <a-card class="form-card">
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 14 }"
        @finish="handleSubmit"
      >
        <a-form-item label="空间名称" name="name">
          <a-input
            v-model:value="formData.name"
            placeholder="请输入空间名称"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="空间编码" name="code">
          <a-input
            v-model:value="formData.code"
            placeholder="请输入空间编码"
            :disabled="isEdit"
            allow-clear
          />
          <div v-if="!isEdit" class="form-item-tip">
            编码创建后不可修改，建议使用有意义的标识
          </div>
        </a-form-item>

        <a-form-item label="空间类型" name="type">
          <a-select
            v-model:value="formData.type"
            placeholder="请选择空间类型"
            :disabled="isEdit"
          >
            <a-select-option value="CAMPUS">
              <a-tag color="blue">园区</a-tag>
            </a-select-option>
            <a-select-option value="BUILDING">
              <a-tag color="green">楼栋</a-tag>
            </a-select-option>
            <a-select-option value="FLOOR">
              <a-tag color="orange">楼层</a-tag>
            </a-select-option>
            <a-select-option value="ROOM">
              <a-tag color="purple">房间</a-tag>
            </a-select-option>
          </a-select>
          <div class="form-item-tip">
            {{ getTypeDescription(formData.type) }}
          </div>
        </a-form-item>

        <a-form-item label="上级空间" name="parentId">
          <a-tree-select
            v-model:value="formData.parentId"
            :tree-data="spaceTreeForSelect"
            placeholder="请选择上级空间（可选）"
            allow-clear
            tree-default-expand-all
            :disabled="isEdit && !!initialParentId"
            :field-names="{ label: 'title', value: 'value', children: 'children' }"
            show-search
            tree-node-filter-prop="title"
          />
          <div v-if="isEdit && initialParentId" class="form-item-tip warning">
            编辑模式下不能修改上级空间
          </div>
        </a-form-item>

        <a-form-item label="租户ID" name="tenantId">
          <a-input
            v-model:value="formData.tenantId"
            placeholder="请输入租户ID"
            :disabled="isEdit"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="描述" name="description">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入空间描述"
            :rows="4"
            allow-clear
            show-count
            :maxlength="500"
          />
        </a-form-item>

        <a-form-item label="元数据" name="metadata">
          <div class="metadata-container">
            <a-button
              v-for="(item, index) in metadataItems"
              :key="index"
              size="small"
              @click="editMetadata(index)"
            >
              {{ item.key }}: {{ item.value }}
            </a-button>
            <a-button size="small" type="dashed" @click="addMetadata">
              <PlusOutlined />
              添加
            </a-button>
          </div>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 6, span: 14 }">
          <a-space>
            <a-button type="primary" html-type="submit" :loading="submitting">
              <SaveOutlined />
              {{ isEdit ? '保存' : '创建' }}
            </a-button>
            <a-button @click="handleBack">
              <RollbackOutlined />
              取消
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 元数据编辑弹窗 -->
    <a-modal
      v-model:open="metadataModalVisible"
      :title="editingMetadataIndex >= 0 ? '编辑元数据' : '添加元数据'"
      @ok="handleMetadataModalOk"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="键名" required>
          <a-input
            v-model:value="metadataForm.key"
            placeholder="请输入键名"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="值" required>
          <a-input
            v-model:value="metadataForm.value"
            placeholder="请输入值"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  SaveOutlined,
  RollbackOutlined,
  PlusOutlined,
} from '@ant-design/icons-vue'
import { spaceApi, type SpaceRequest, type SpaceTreeNode } from '@/api/space'

const router = useRouter()
const route = useRoute()

// 是否为编辑模式
const isEdit = computed(() => !!route.params.id)
const spaceId = computed(() => route.params.id as string)

// 初始父空间ID（编辑时不能修改）
const initialParentId = ref<string>()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive<Partial<SpaceRequest>>({
  name: '',
  code: '',
  type: 'ROOM',
  parentId: undefined,
  tenantId: '',
  description: '',
  metadata: {},
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入空间名称', trigger: 'blur' },
    { min: 2, max: 50, message: '空间名称长度为2-50个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入空间编码', trigger: 'blur' },
    {
      pattern: /^[A-Z0-9_-]+$/,
      message: '编码只能包含大写字母、数字、下划线和连字符',
      trigger: 'blur',
    },
    { min: 2, max: 30, message: '空间编码长度为2-30个字符', trigger: 'blur' },
  ],
  type: [{ required: true, message: '请选择空间类型', trigger: 'change' }],
  tenantId: [{ required: true, message: '请输入租户ID', trigger: 'blur' }],
}

// 提交状态
const submitting = ref(false)

// 空间树数据（用于选择上级空间）
const spaceTree = ref<SpaceTreeNode[]>([])

// 用于下拉选择的树数据
const spaceTreeForSelect = computed(() => {
  const convertToTreeSelect = (nodes: SpaceTreeNode[]): any[] => {
    return nodes
      .filter((node) => node.id !== spaceId.value) // 排除自己
      .map((node) => ({
        title: `${node.name} (${getTypeText(node.type)})`,
        value: node.id,
        children: node.children ? convertToTreeSelect(node.children) : undefined,
      }))
  }
  return convertToTreeSelect(spaceTree.value)
})

// 元数据
const metadataItems = ref<Array<{ key: string; value: string }>>([])
const metadataModalVisible = ref(false)
const editingMetadataIndex = ref(-1)
const metadataForm = reactive({ key: '', value: '' })

// 获取空间树
const fetchSpaceTree = async () => {
  try {
    const response = await spaceApi.getSpaceTree('')
    spaceTree.value = response.data
  } catch (error) {
    console.error('获取空间树失败:', error)
  }
}

// 获取空间详情（编辑模式）
const fetchSpaceDetail = async () => {
  if (!isEdit.value) return

  try {
    const response = await spaceApi.getSpace(spaceId.value)
    const space = response.data

    formData.name = space.name
    formData.code = space.code
    formData.type = space.type
    formData.parentId = space.parentId
    formData.tenantId = space.tenantId
    formData.description = (space as any).description || ''

    // 保存初始父空间ID
    initialParentId.value = space.parentId

    // 处理元数据
    if ((space as any).metadata) {
      metadataItems.value = Object.entries((space as any).metadata).map(
        ([key, value]) => ({ key, value: String(value) })
      )
    }
  } catch (error) {
    message.error('获取空间详情失败')
    router.push('/spaces')
  }
}

// 检查空间编码是否重复
const checkCodeDuplicate = async (code: string) => {
  try {
    const response = await spaceApi.getSpaces({
      page: 1,
      size: 1,
      keyword: code,
    })
    return response.data.total > 0
  } catch {
    return false
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()

    // 构建元数据对象
    const metadata: Record<string, any> = {}
    metadataItems.value.forEach((item) => {
      metadata[item.key] = item.value
    })
    formData.metadata = metadata

    submitting.value = true

    if (isEdit.value) {
      await spaceApi.updateSpace(spaceId.value, formData)
      message.success('更新成功')
    } else {
      // 新建时检查编码重复
      const exists = await checkCodeDuplicate(formData.code!)
      if (exists) {
        message.warning('空间编码已存在，请使用其他编码')
        return
      }

      await spaceApi.createSpace(formData as SpaceRequest)
      message.success('创建成功')
    }

    router.push('/spaces')
  } catch (error) {
    message.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
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

// 获取类型描述
const getTypeDescription = (type?: string) => {
  const descMap: Record<string, string> = {
    CAMPUS: '园区级别的顶级空间，如：科技园、工业园',
    BUILDING: '建筑物空间，如：A栋、综合楼',
    FLOOR: '楼层空间，如：1层、2层',
    ROOM: '房间空间，如：101室、会议室',
  }
  return descMap[type || ''] || ''
}

// 元数据操作
const addMetadata = () => {
  editingMetadataIndex.value = -1
  metadataForm.key = ''
  metadataForm.value = ''
  metadataModalVisible.value = true
}

const editMetadata = (index: number) => {
  editingMetadataIndex.value = index
  const item = metadataItems.value[index]
  metadataForm.key = item.key
  metadataForm.value = item.value
  metadataModalVisible.value = true
}

const handleMetadataModalOk = () => {
  if (!metadataForm.key || !metadataForm.value) {
    message.warning('请输入完整的键值对')
    return
  }

  if (editingMetadataIndex.value >= 0) {
    metadataItems.value[editingMetadataIndex.value] = {
      key: metadataForm.key,
      value: metadataForm.value,
    }
  } else {
    metadataItems.value.push({
      key: metadataForm.key,
      value: metadataForm.value,
    })
  }

  metadataModalVisible.value = false
}

// 初始化
onMounted(async () => {
  await fetchSpaceTree()

  // 处理URL参数中的parentId
  const parentId = route.query.parentId as string
  if (parentId) {
    formData.parentId = parentId
  }

  // 设置默认租户ID（可以从用户信息中获取）
  formData.tenantId = 'default-tenant'

  if (isEdit.value) {
    await fetchSpaceDetail()
  }
})
</script>

<style scoped>
.space-form-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  background: #fff;
  border-radius: 8px;
  padding: 16px 24px;
  margin-bottom: 16px;
}

.form-card {
  border-radius: 8px;
  max-width: 800px;
}

.form-item-tip {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  margin-top: 4px;
}

.form-item-tip.warning {
  color: #faad14;
}

.metadata-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.metadata-container .ant-btn {
  margin: 0;
}
</style>
