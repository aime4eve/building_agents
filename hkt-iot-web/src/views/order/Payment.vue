<template>
  <div class="payment-page">
    <a-card class="payment-card" hoverable>
      <template #title>
        <div class="card-header">
          <span class="title">订单支付</span>
        </div>
      </template>

      <a-steps :current="activeStep" class="payment-steps">
        <a-step title="确认订单" />
        <a-step title="选择支付方式" />
        <a-step title="完成支付" />
      </a-steps>

      <!-- 步骤 1：确认订单 -->
      <div v-show="activeStep === 0" class="step-content">
        <a-descriptions :column="1" bordered>
          <a-descriptions-item label="订单号">{{ orderInfo.orderNo }}</a-descriptions-item>
          <a-descriptions-item label="订单金额">
            <span class="amount">¥{{ orderInfo.totalAmount?.toFixed(2) || amount }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="商品明细">
            <a-table :data-source="orderInfo.items || []" :pagination="false" size="small" bordered>
              <a-table-column title="商品名称" data-index="productName" :ellipsis="{ showTitle: false }" />
              <a-table-column title="数量" data-index="quantity" width="80" align="center" />
              <a-table-column title="单价" data-index="unitPrice" width="100" align="center">
                <template #customRender="{ text }">¥{{ Number(text).toFixed(2) }}</template>
              </a-table-column>
              <a-table-column title="小计" data-index="totalPrice" width="100" align="center">
                <template #customRender="{ text }">¥{{ Number(text).toFixed(2) }}</template>
              </a-table-column>
            </a-table>
          </a-descriptions-item>
        </a-descriptions>

        <div class="step-actions">
          <a-button @click="handleBack">返回</a-button>
          <a-button type="primary" @click="activeStep = 1">下一步</a-button>
        </div>
      </div>

      <!-- 步骤 2：选择支付方式 -->
      <div v-show="activeStep === 1" class="step-content">
        <div class="payment-methods">
          <a-radio-group v-model:value="selectedPaymentMethod" class="payment-method-group">
            <a-radio-button value="WECHAT" class="payment-method-item">
              <div class="method-content">
                <i class="method-icon wechat-icon" />
                <span class="method-name">微信支付</span>
              </div>
            </a-radio-button>

            <a-radio-button value="ALIPAY" class="payment-method-item">
              <div class="method-content">
                <i class="method-icon alipay-icon" />
                <span class="method-name">支付宝</span>
              </div>
            </a-radio-button>

            <a-radio-button value="UNIONPAY" class="payment-method-item">
              <div class="method-content">
                <i class="method-icon unionpay-icon" />
                <span class="method-name">银联支付</span>
              </div>
            </a-radio-button>
          </a-radio-group>
        </div>

        <div class="step-actions">
          <a-button @click="activeStep = 0">上一步</a-button>
          <a-button type="primary" @click="handleConfirmPayment">确认支付</a-button>
        </div>
      </div>

      <!-- 步骤 3：完成支付 -->
      <div v-show="activeStep === 2" class="step-content">
        <div class="payment-result">
          <div v-if="paymentSuccess" class="success">
            <a-result
              status="success"
              title="支付成功"
              sub-title="支付金额：¥{{ paymentAmount.toFixed(2) }} | 支付时间：{{ paymentTime }} | 交易号：{{ transactionId }}"
            >
              <template #extra>
                <a-button type="primary" @click="handleBackToOrder">返回订单列表</a-button>
                <a-button @click="handleDownloadReceipt">下载电子回单</a-button>
              </template>
            </a-result>
          </div>

          <div v-else-if="paymentFailed" class="failed">
            <a-result
              status="error"
              title="支付失败"
              :sub-title="paymentError"
            >
              <template #extra>
                <a-button type="primary" @click="activeStep = 1">重新支付</a-button>
                <a-button @click="handleBackToOrder">返回订单列表</a-button>
              </template>
            </a-result>
          </div>

          <div v-else class="processing">
            <a-result
              status="info"
              title="支付处理中"
              sub-title="正在等待支付结果... 请勿关闭此页面"
            />
          </div>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useOrderStore } from '@/stores/order'
import type { Order, Payment, CreatePaymentRequest } from '@/api/order'

// 路由
const route = useRoute()
const router = useRouter()

// Store
const orderStore = useOrderStore()

// 当前步骤
const activeStep = ref(0)

// 订单信息
const orderInfo = ref<Partial<Order>>({})

// 支付金额
const amount = ref(0)

// 选中的支付方式
const selectedPaymentMethod = ref<'WECHAT' | 'ALIPAY' | 'UNIONPAY'>('WECHAT')

// 支付状态
const paymentSuccess = ref(false)
const paymentFailed = ref(false)
const paymentAmount = ref(0)
const paymentTime = ref('')
const transactionId = ref('')
const paymentError = ref('')

// 轮询定时器
let pollTimer: number | null = null

// 加载订单信息
const loadOrderInfo = async () => {
  const orderId = route.query.orderId as string
  const payAmount = parseFloat(route.query.amount as string)

  if (!orderId) {
    message.error('订单 ID 不能为空')
    router.push('/order/list')
    return
  }

  try {
    const order = await orderStore.fetchOrder(orderId)
    orderInfo.value = order
    amount.value = order.totalAmount || payAmount || 0
  } catch (error: any) {
    message.error(error.message || '加载订单信息失败')
    router.push('/order/list')
  }
}

// 确认支付
const handleConfirmPayment = async () => {
  const orderId = route.query.orderId as string

  try {
    const paymentRequest: CreatePaymentRequest = {
      orderId,
      paymentMethod: selectedPaymentMethod.value,
    }

    const payment = await orderStore.createPayment(paymentRequest)

    // 模拟支付流程（实际应该跳转到支付平台或显示二维码）
    message.success('支付请求已提交，正在处理...')
    activeStep.value = 2

    // 开始轮询支付状态
    startPollingPaymentStatus(payment.paymentId)
  } catch (error: any) {
    message.error(error.message || '创建支付失败')
  }
}

// 轮询支付状态
const startPollingPaymentStatus = async (paymentId: string) => {
  const pollPaymentStatus = async () => {
    try {
      const status = await orderStore.fetchPaymentStatus(paymentId)

      if (status === 'SUCCESS') {
        paymentSuccess.value = true
        paymentAmount.value = orderInfo.value.totalAmount || amount.value
        paymentTime.value = new Date().toLocaleString('zh-CN')
        transactionId.value = `TXN${Date.now()}`
        stopPolling()
      } else if (status === 'FAILED') {
        paymentFailed.value = true
        paymentError.value = '支付失败，请重试'
        stopPolling()
      }
    } catch (error: any) {
      console.error('轮询支付状态失败:', error)
    }
  }

  // 立即执行一次
  pollPaymentStatus()

  // 每 3 秒轮询一次
  pollTimer = window.setInterval(pollPaymentStatus, 3000)

  // 30 秒后停止
  setTimeout(() => {
    stopPolling()
    if (!paymentSuccess.value && !paymentFailed.value) {
      paymentFailed.value = true
      paymentError.value = '支付超时，请重试'
    }
  }, 30000)
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 返回订单列表
const handleBackToOrder = () => {
  router.push('/order/list')
}

// 返回上一步
const handleBack = () => {
  router.push('/order/list')
}

// 下载电子回单
const handleDownloadReceipt = () => {
  message.info('电子回单下载功能开发中')
}

// 生命周期
onMounted(() => {
  loadOrderInfo()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style lang="scss" scoped>
.payment-page {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.payment-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  .title {
    font-size: 20px;
    font-weight: 500;
    color: #262626;
  }
}

.payment-steps {
  margin-bottom: 32px;
}

.step-content {
  padding: 24px 0;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.amount {
  color: #f5222d;
  font-size: 20px;
  font-weight: 600;
}

.payment-methods {
  padding: 24px 0;
}

.payment-method-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.payment-method-item {
  width: 100%;

  :deep(.ant-radio-button-wrapper) {
    width: 100%;
    padding: 16px;
    text-align: left;
  }
}

.method-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.method-icon {
  font-size: 24px;
  display: inline-block;
  width: 24px;
  height: 24px;

  &.wechat-icon {
    background-color: #07c160;
    mask: url('data:image/svg+xml;utf8,<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M8.5 10a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zm7 0a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z"/></svg>') no-repeat center;
    -webkit-mask: url('data:image/svg+xml;utf8,<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M8.5 10a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zm7 0a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z"/></svg>') no-repeat center;
  }

  &.alipay-icon {
    background-color: #1677ff;
    mask: url('data:image/svg+xml;utf8,<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/></svg>') no-repeat center;
    -webkit-mask: url('data:image/svg+xml;utf8,<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/></svg>') no-repeat center;
  }

  &.unionpay-icon {
    background-color: #d42323;
    mask: url('data:image/svg+xml;utf8,<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/></svg>') no-repeat center;
    -webkit-mask: url('data:image/svg+xml;utf8,<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/></svg>') no-repeat center;
  }
}

.method-name {
  font-size: 16px;
  font-weight: 500;
}

.payment-result {
  padding: 48px 0;
}
</style>
