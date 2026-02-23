import { http } from '@/utils/request'
import type { ApiResponse, PageRequest, PageResult } from '@/types'

// ==================== 订单相关类型定义 ====================

/**
 * 订单状态
 */
export type OrderStatus =
  | 'PENDING'
  | 'PAID'
  | 'SHIPPED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'REFUNDED'
  | 'REFUNDING'

/**
 * 订单信息
 */
export interface Order {
  orderId: string
  orderNo: string
  tenantId: string
  userId: string
  orderType: 'PRODUCT' | 'SERVICE' | 'SUBSCRIPTION'
  orderStatus: OrderStatus
  totalAmount: number
  paidAmount: number
  currency: string
  items: OrderItem[]
  paymentId?: string
  invoiceId?: string
  remark?: string
  createdAt: string
  updatedAt: string
  paidAt?: string
  completedAt?: string
  cancelledAt?: string
}

/**
 * 订单商品项
 */
export interface OrderItem {
  itemId: string
  orderId: string
  productId: string
  productName: string
  productType: string
  quantity: number
  unitPrice: number
  totalPrice: number
  metadata?: Record<string, any>
}

/**
 * 订单查询参数
 */
export interface OrderQuery extends PageRequest {
  tenantId?: string
  userId?: string
  orderStatus?: OrderStatus
  orderType?: string
  keyword?: string
  startTime?: string
  endTime?: string
}

/**
 * 创建订单请求
 */
export interface CreateOrderRequest {
  orderType: 'PRODUCT' | 'SERVICE' | 'SUBSCRIPTION'
  items: {
    productId: string
    productName: string
    productType: string
    quantity: number
    unitPrice: number
  }[]
  remark?: string
}

/**
 * 支付状态
 */
export type PaymentStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'SUCCESS'
  | 'FAILED'
  | 'REFUNDED'
  | 'REFUNDING'

/**
 * 支付方式
 */
export type PaymentMethod = 'WECHAT' | 'ALIPAY' | 'UNIONPAY' | 'BALANCE'

/**
 * 支付信息
 */
export interface Payment {
  paymentId: string
  orderId: string
  orderNo: string
  paymentNo: string
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  amount: number
  currency: string
  transactionId?: string
  qrCodeUrl?: string
  paidAt?: string
  createdAt: string
  updatedAt: string
}

/**
 * 创建支付请求
 */
export interface CreatePaymentRequest {
  orderId: string
  paymentMethod: PaymentMethod
  notifyUrl?: string
  returnUrl?: string
}

/**
 * 账单状态
 */
export type BillStatus = 'UNPAID' | 'PAID' | 'OVERDUE' | 'CANCELLED'

/**
 * 能耗账单
 */
export interface EnergyBill {
  billId: string
  billNo: string
  tenantId: string
  spaceId?: string
  billType: 'ELECTRICITY' | 'WATER' | 'GAS'
  billStatus: BillStatus
  amount: number
  usage: number
  unit: string
  unitPrice: number
  billingPeriod: string
  dueDate: string
  paidAt?: string
  createdAt: string
}

/**
 * 账单查询参数
 */
export interface EnergyBillQuery extends PageRequest {
  tenantId?: string
  spaceId?: string
  billType?: string
  billStatus?: BillStatus
  startTime?: string
  endTime?: string
}

// ==================== 订单 API ====================

/**
 * 订单管理 API（与后端 /api/v1/orders 对接）
 */
export const orderApi = {
  // 获取订单列表
  getOrders(params: OrderQuery): Promise<ApiResponse<PageResult<Order>>> {
    return http.get('/v1/orders', { params })
  },

  // 获取订单详情
  getOrder(orderId: string): Promise<ApiResponse<Order>> {
    return http.get(`/v1/orders/${orderId}`)
  },

  // 创建订单
  createOrder(data: CreateOrderRequest): Promise<ApiResponse<Order>> {
    return http.post('/v1/orders', data)
  },

  // 取消订单
  cancelOrder(orderId: string, reason: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/orders/${orderId}/cancel`, { reason })
  },

  // 获取用户订单列表
  getUserOrders(userId: string, params: PageRequest): Promise<ApiResponse<PageResult<Order>>> {
    return http.get(`/v1/users/${userId}/orders`, { params })
  },

  // 获取租户订单列表
  getTenantOrders(tenantId: string, params: PageRequest): Promise<ApiResponse<PageResult<Order>>> {
    return http.get(`/v1/tenants/${tenantId}/orders`, { params })
  },
}

// ==================== 支付 API ====================

/**
 * 支付管理 API（与后端 /api/v1/payments 对接）
 */
export const paymentApi = {
  // 创建支付
  createPayment(data: CreatePaymentRequest): Promise<ApiResponse<Payment>> {
    return http.post('/v1/payments', data)
  },

  // 获取支付详情
  getPayment(paymentId: string): Promise<ApiResponse<Payment>> {
    return http.get(`/v1/payments/${paymentId}`)
  },

  // 获取订单支付
  getOrderPayment(orderId: string): Promise<ApiResponse<Payment>> {
    return http.get(`/v1/orders/${orderId}/payment`)
  },

  // 查询支付状态
  getPaymentStatus(paymentId: string): Promise<ApiResponse<PaymentStatus>> {
    return http.get(`/v1/payments/${paymentId}/status`)
  },

  // 申请退款
  requestRefund(paymentId: string, reason: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/payments/${paymentId}/refund`, { reason })
  },
}

// ==================== 账单 API ====================

/**
 * 账单管理 API（与后端 /api/v1/energy-bills 对接）
 */
export const energyBillApi = {
  // 获取账单列表
  getEnergyBills(params: EnergyBillQuery): Promise<ApiResponse<PageResult<EnergyBill>>> {
    return http.get('/v1/energy-bills', { params })
  },

  // 获取账单详情
  getEnergyBill(billId: string): Promise<ApiResponse<EnergyBill>> {
    return http.get(`/v1/energy-bills/${billId}`)
  },

  // 支付账单
  payEnergyBill(billId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/energy-bills/${billId}/pay`)
  },

  // 获取租户账单
  getTenantBills(tenantId: string, params: PageRequest): Promise<ApiResponse<PageResult<EnergyBill>>> {
    return http.get(`/v1/tenants/${tenantId}/energy-bills`, { params })
  },

  // 获取空间账单
  getSpaceBills(spaceId: string, params: PageRequest): Promise<ApiResponse<PageResult<EnergyBill>>> {
    return http.get(`/v1/spaces/${spaceId}/energy-bills`, { params })
  },
}

// ==================== 工具函数 ====================

/**
 * 订单状态映射
 */
export const OrderStatusMap: Record<OrderStatus, string> = {
  PENDING: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUNDED: '已退款',
  REFUNDING: '退款中',
}

/**
 * 支付状态映射
 */
export const PaymentStatusMap: Record<PaymentStatus, string> = {
  PENDING: '待支付',
  PROCESSING: '支付中',
  SUCCESS: '支付成功',
  FAILED: '支付失败',
  REFUNDED: '已退款',
  REFUNDING: '退款中',
}

/**
 * 支付方式映射
 */
export const PaymentMethodMap: Record<PaymentMethod, string> = {
  WECHAT: '微信支付',
  ALIPAY: '支付宝',
  UNIONPAY: '银联支付',
  BALANCE: '余额支付',
}

/**
 * 获取订单状态文本
 */
export function getOrderStatusText(status: OrderStatus): string {
  return OrderStatusMap[status] || status
}

/**
 * 获取支付状态文本
 */
export function getPaymentStatusText(status: PaymentStatus): string {
  return PaymentStatusMap[status] || status
}

/**
 * 获取支付方式文本
 */
export function getPaymentMethodText(method: PaymentMethod): string {
  return PaymentMethodMap[method] || method
}

/**
 * 获取订单状态颜色
 */
export function getOrderStatusColor(status: OrderStatus): string {
  const colorMap: Record<OrderStatus, string> = {
    PENDING: 'warning',
    PAID: 'success',
    SHIPPED: 'processing',
    COMPLETED: 'success',
    CANCELLED: 'default',
    REFUNDED: 'default',
    REFUNDING: 'processing',
  }
  return colorMap[status] || 'default'
}
