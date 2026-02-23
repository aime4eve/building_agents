<template>
  <div class="order-list-page">
    <!-- 搜索表单 -->
    <a-card class="search-card" hoverable>
      <a-form
        ref="searchFormRef"
        :model="searchForm"
        :inline="true"
        @keyup.enter="handleSearch"
      >
        <a-form-item label="订单号">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="请输入订单号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="订单类型">
          <a-select
            v-model:value="searchForm.orderType"
            placeholder="请选择订单类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="PRODUCT">商品订单</a-select-option>
            <a-select-option value="SERVICE">服务订单</a-select-option>
            <a-select-option value="SUBSCRIPTION">订阅订单</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="订单状态">
          <a-select
            v-model:value="searchForm.orderStatus"
            placeholder="请选择订单状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="PENDING">待支付</a-select-option>
            <a-select-option value="PAID">已支付</a-select-option>
            <a-select-option value="SHIPPED">已发货</a-select-option>
            <a-select-option value="COMPLETED">已完成</a-select-option>
            <a-select-option value="CANCELLED">已取消</a-select-option>
            <a-select-option value="REFUNDED">已退款</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="dateRange"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" @click="handleSearch">
            <template #icon><SearchOutlined /></template>
            搜索
          </a-button>
          <a-button @click="handleReset">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作按钮区 -->
    <a-card class="toolbar-card" hoverable>
      <div class="toolbar">
        <div class="toolbar-left">
          <a-button type="primary" @click="handleCreate">
            <template #icon><PlusOutlined /></template>
            创建订单
          </a-button>
        </div>
        <div class="toolbar-right">
          <a-button @click="handleExport">
            <template #icon><DownloadOutlined /></template>
            导出
          </a-button>
        </div>
      </div>
    </a-card>

    <!-- 数据表格 -->
    <a-card class="table-card" hoverable>
      <a-table
        :loading="loading"
        :data-source="tableData"
        :pagination="false"
        :scroll="{ x: 1200 }"
        row-key="orderId"
      >
        <a-table-column title="订单号" data-index="orderNo" :ellipsis="{ showTitle: false }">
          <template #customRender="{ text }">
            <a-typography-text copyable>{{ text }}</a-typography-text>
          </template>
        </a-table-column>
        <a-table-column title="订单类型" data-index="orderType" width="120" align="center">
          <template #customRender="{ text }">
            <a-tag :color="getOrderTypeTag(text)">
              {{ getOrderTypeText(text) }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="订单状态" data-index="orderStatus" width="120" align="center">
          <template #customRender="{ text }">
            <a-tag :color="getOrderStatusTagType(text)">
              {{ getOrderStatusText(text) }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="订单金额" data-index="totalAmount" width="120" align="center">
          <template #customRender="{ text }">
            <span class="amount">¥{{ Number(text).toFixed(2) }}</span>
          </template>
        </a-table-column>
        <a-table-column title="支付金额" data-index="paidAmount" width="120" align="center">
          <template #customRender="{ text }">
            <span class="amount">¥{{ Number(text).toFixed(2) }}</span>
          </template>
        </a-table-column>
        <a-table-column title="用户 ID" data-index="userId" :ellipsis="{ showTitle: false }" />
        <a-table-column title="创建时间" data-index="createdAt" width="180" align="center" />
        <a-table-column title="操作" width="280" align="center" fixed="right">
          <template #customRender="{ record }">
            <a-button type="link" size="small" @click="handleView(record)">
              详情
            </a-button>
            <a-button
              v-if="record.orderStatus === 'PENDING'"
              type="link"
              size="small"
              @click="handlePay(record)"
            >
              支付
            </a-button>
            <a-button
              v-if="record.orderStatus === 'PENDING'"
              type="link"
              size="small"
              @click="handleCancel(record)"
            >
              取消
            </a-button>
            <a-button type="link" size="small" @click="handleRefund(record)">
              退款
            </a-button>
          </template>
        </a-table-column>
      </a-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <a-pagination
          v-model:current="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :show-size-changer="true"
          :show-quick-jumper="true"
          :page-size-options="['10', '20', '50', '100']"
          show-total="(共 {total} 条)"
          @show-size-change="handleSizeChange"
          @change="handlePageChange"
        />
      </div>
    </a-card>

    <!-- 订单详情对话框 -->
    <a-modal
      v-model:open="detailDialogVisible"
      title="订单详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions v-if="currentOrder" :column="2" bordered>
        <a-descriptions-item label="订单号">{{ currentOrder.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="订单状态">
          <a-tag :color="getOrderStatusTagType(currentOrder.orderStatus)">
            {{ getOrderStatusText(currentOrder.orderStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="订单类型">
          <a-tag :color="getOrderTypeTag(currentOrder.orderType)">
            {{ getOrderTypeText(currentOrder.orderType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount.toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="支付金额">¥{{ currentOrder.paidAmount.toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentOrder.createdAt }}</a-descriptions-item>
        <a-descriptions-item label="用户 ID" :span="2">{{ currentOrder.userId }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</a-descriptions-item>
      </a-descriptions>
      
      <a-divider>订单商品</a-divider>
      <a-table :data-source="currentOrder?.items || []" :pagination="false" size="small" bordered>
        <a-table-column title="商品名称" data-index="productName" :ellipsis="{ showTitle: false }" />
        <a-table-column title="商品类型" data-index="productType" width="120" />
        <a-table-column title="数量" data-index="quantity" width="80" align="center" />
        <a-table-column title="单价" data-index="unitPrice" width="120" align="center">
          <template #customRender="{ text }">¥{{ Number(text).toFixed(2) }}</template>
        </a-table-column>
        <a-table-column title="总价" data-index="totalPrice" width="120" align="center">
          <template #customRender="{ text }">¥{{ Number(text).toFixed(2) }}</template>
        </a-table-column>
      </a-table>
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
} from '@ant-design/icons-vue'
import { useOrderStore } from '@/stores/order'
import type { Order, OrderQuery, OrderStatus, PageResponse } from '@/api/order'
import { getOrderStatusText, getOrderStatusColor } from '@/api/order'
import { useRouter } from 'vue-router'

// 路由
const router = useRouter()

// Store
const orderStore = useOrderStore()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchFormRef = ref()
const searchForm = reactive<OrderQuery>({
  keyword: '',
  orderType: undefined,
  orderStatus: undefined,
  page: 1,
  size: 10,
})

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 表格数据
const tableData = ref<Order[]>([])

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

// 当前订单
const currentOrder = ref<Order | null>(null)

// 详情对话框
const detailDialogVisible = ref(false)

// 获取订单类型标签
const getOrderTypeTag = (type: string) => {
  const typeMap: Record<string, string> = {
    PRODUCT: 'blue',
    SERVICE: 'green',
    SUBSCRIPTION: 'orange',
  }
  return typeMap[type] || 'default'
}

// 获取订单类型文本
const getOrderTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    PRODUCT: '商品订单',
    SERVICE: '服务订单',
    SUBSCRIPTION: '订阅订单',
  }
  return typeMap[type] || type
}

// 获取订单状态标签类型
const getOrderStatusTagType = (status: OrderStatus) => {
  const statusMap: Record<OrderStatus, string> = {
    PENDING: 'orange',
    PAID: 'green',
    SHIPPED: 'blue',
    COMPLETED: 'green',
    CANCELLED: 'default',
    REFUNDED: 'default',
    REFUNDING: 'orange',
  }
  return statusMap[status] || 'default'
}

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const params: OrderQuery = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1],
    }
    const response: PageResponse<Order> = await orderStore.fetchOrders(params)
    tableData.value = response.items
    pagination.total = response.total
  } catch (error: any) {
    message.error(error.message || '加载订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadOrders()
}

// 重置
const handleReset = () => {
  searchFormRef.value?.resetFields()
  dateRange.value = null
  handleSearch()
}

// 创建订单
const handleCreate = () => {
  message.info('创建订单功能开发中')
}

// 查看详情
const handleView = async (row: Order) => {
  try {
    const order = await orderStore.fetchOrder(row.orderId)
    currentOrder.value = order
    detailDialogVisible.value = true
  } catch (error: any) {
    message.error(error.message || '加载订单详情失败')
  }
}

// 支付订单
const handlePay = (row: Order) => {
  router.push({ path: '/order/payment', query: { orderId: row.orderId, amount: row.totalAmount } })
}

// 取消订单
const handleCancel = async (row: Order) => {
  try {
    Modal.confirm({
      title: '提示',
      content: '确定要取消该订单吗？',
      okText: '确定',
      cancelText: '取消',
      okType: 'warning',
      onOk: async () => {
        await orderStore.cancelOrder(row.orderId, '用户主动取消')
        message.success('订单已取消')
        loadOrders()
      },
    })
  } catch (error: any) {
    if (error !== 'cancel') {
      message.error(error.message || '取消订单失败')
    }
  }
}

// 申请退款
const handleRefund = async (row: Order) => {
  try {
    Modal.confirm({
      title: '提示',
      content: '确定要申请退款吗？',
      okText: '确定',
      cancelText: '取消',
      okType: 'warning',
      onOk: () => {
        message.info('退款功能开发中')
      },
    })
  } catch (error: any) {
    if (error !== 'cancel') {
      message.error(error.message || '退款申请失败')
    }
  }
}

// 导出
const handleExport = () => {
  message.info('导出功能开发中')
}

// 页大小变化
const handleSizeChange = () => {
  loadOrders()
}

// 页码变化
const handlePageChange = () => {
  loadOrders()
}

// 生命周期
onMounted(() => {
  loadOrders()
})
</script>

<style lang="scss" scoped>
.order-list-page {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.search-card {
  margin-bottom: 16px;
}

.toolbar-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .toolbar-left {
    display: flex;
    gap: 8px;
  }
}

.table-card {
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}

.amount {
  color: #f5222d;
  font-weight: 500;
}
</style>
