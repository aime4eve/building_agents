import { defineStore } from 'pinia'
import { ref } from 'vue'
import type {
  Order,
  OrderQuery,
  CreateOrderRequest,
  Payment,
  CreatePaymentRequest,
  EnergyBill,
  EnergyBillQuery,
} from '@/api/order'
import { orderApi, paymentApi, energyBillApi } from '@/api/order'
import type { PageResult } from '@/types'

/**
 * 订单状态管理
 */
export const useOrderStore = defineStore('order', () => {
  // 状态
  const orderList = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const currentPayment = ref<Payment | null>(null)
  const loading = ref(false)
  const energyBillList = ref<EnergyBill[]>([])

  // 获取订单列表
  async function fetchOrders(params: OrderQuery) {
    loading.value = true
    try {
      const response = await orderApi.getOrders(params)
      orderList.value = response.data.items
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 获取订单详情
  async function fetchOrder(orderId: string) {
    loading.value = true
    try {
      const response = await orderApi.getOrder(orderId)
      currentOrder.value = response.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 创建订单
  async function createOrder(data: CreateOrderRequest) {
    const response = await orderApi.createOrder(data)
    return response.data
  }

  // 取消订单
  async function cancelOrder(orderId: string, reason: string) {
    await orderApi.cancelOrder(orderId, reason)
  }

  // 获取用户订单
  async function fetchUserOrders(userId: string, params: any) {
    loading.value = true
    try {
      const response = await orderApi.getUserOrders(userId, params)
      orderList.value = response.data.items
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 获取租户订单
  async function fetchTenantOrders(tenantId: string, params: any) {
    loading.value = true
    try {
      const response = await orderApi.getTenantOrders(tenantId, params)
      orderList.value = response.data.items
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 创建支付
  async function createPayment(data: CreatePaymentRequest) {
    const response = await paymentApi.createPayment(data)
    currentPayment.value = response.data
    return response.data
  }

  // 获取支付详情
  async function fetchPayment(paymentId: string) {
    const response = await paymentApi.getPayment(paymentId)
    currentPayment.value = response.data
    return response.data
  }

  // 获取订单支付
  async function fetchOrderPayment(orderId: string) {
    const response = await paymentApi.getOrderPayment(orderId)
    currentPayment.value = response.data
    return response.data
  }

  // 查询支付状态
  async function fetchPaymentStatus(paymentId: string) {
    const response = await paymentApi.getPaymentStatus(paymentId)
    return response.data
  }

  // 申请退款
  async function requestRefund(paymentId: string, reason: string) {
    await paymentApi.requestRefund(paymentId, reason)
  }

  // 获取账单列表
  async function fetchEnergyBills(params: EnergyBillQuery) {
    loading.value = true
    try {
      const response = await energyBillApi.getEnergyBills(params)
      energyBillList.value = response.data.items
      return response.data
    } finally {
      loading.value = false
    }
  }

  // 获取账单详情
  async function fetchEnergyBill(billId: string) {
    const response = await energyBillApi.getEnergyBill(billId)
    return response.data
  }

  // 支付账单
  async function payEnergyBill(billId: string) {
    await energyBillApi.payEnergyBill(billId)
  }

  // 清空当前订单
  function clearCurrentOrder() {
    currentOrder.value = null
  }

  // 清空当前支付
  function clearCurrentPayment() {
    currentPayment.value = null
  }

  // 清空订单列表
  function clearOrderList() {
    orderList.value = []
  }

  return {
    // 状态
    orderList,
    currentOrder,
    currentPayment,
    loading,
    energyBillList,
    // 方法
    fetchOrders,
    fetchOrder,
    createOrder,
    cancelOrder,
    fetchUserOrders,
    fetchTenantOrders,
    createPayment,
    fetchPayment,
    fetchOrderPayment,
    fetchPaymentStatus,
    requestRefund,
    fetchEnergyBills,
    fetchEnergyBill,
    payEnergyBill,
    clearCurrentOrder,
    clearCurrentPayment,
    clearOrderList,
  }
})
