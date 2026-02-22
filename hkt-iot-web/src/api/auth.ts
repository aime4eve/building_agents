import { http } from '@/utils/request'
import type { ApiResponse } from '@/types'

// ==================== 认证相关类型 ====================

/**
 * 登录请求参数
 */
export interface LoginRequest {
  username: string
  password: string
  tenantCode?: string
  mfaCode?: string // MFA验证码
  mfaTrustToken?: string // MFA信任令牌（记住设备）
}

/**
 * 登录响应
 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  expiresAt: number // 令牌过期时间戳
  tokenType: string
  requiresMfa: boolean // 是否需要MFA验证
  mfaType?: 'TOTP' | 'SMS' | 'EMAIL' // MFA类型
  mfaTrustToken?: string // MFA信任令牌
  user: {
    id: string
    username: string
    email: string
    phone?: string
    roles: string[]
    permissions?: string[]
    tenantId: string
    mfaEnabled: boolean
  }
}

/**
 * MFA设备类型
 */
export type MfaDeviceType = 'TOTP' | 'SMS' | 'EMAIL'

/**
 * MFA设备信息
 */
export interface MfaDevice {
  deviceId: string
  deviceType: MfaDeviceType
  deviceName: string
  isVerified: boolean
  isDefault: boolean
  createdAt: string
  lastUsedAt?: string
}

/**
 * TOTP设置响应
 */
export interface TotpSetupResponse {
  secret: string
  qrCodeUrl: string
  backupCodes: string[]
}

/**
 * MFA验证请求
 */
export interface MfaVerifyRequest {
  code: string
  deviceType?: MfaDeviceType
  trustDevice?: boolean // 是否信任此设备（30天免验证）
}

/**
 * MFA验证响应
 */
export interface MfaVerifyResponse {
  success: boolean
  trustToken?: string
  accessToken?: string
}

// ==================== 认证API ====================

/**
 * 认证API（与后端 /api/v1/auth 对接）
 */
export const authApi = {
  // 用户登录
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return http.post('/v1/auth/login', data)
  },

  // MFA验证
  mfaVerify(data: MfaVerifyRequest): Promise<ApiResponse<MfaVerifyResponse>> {
    return http.post('/v1/auth/mfa/verify', data)
  },

  // 刷新令牌
  refreshToken(refreshToken: string): Promise<ApiResponse<LoginResponse>> {
    return http.post('/v1/auth/refresh', { refreshToken })
  },

  // 用户登出
  logout(): Promise<ApiResponse<void>> {
    return http.post('/v1/auth/logout')
  },

  // 获取当前用户信息
  getCurrentUser(): Promise<ApiResponse<LoginResponse['user']>> {
    return http.get('/v1/auth/user')
  },

  // 修改密码
  changePassword(data: {
    oldPassword: string
    newPassword: string
    confirmPassword: string
  }): Promise<ApiResponse<void>> {
    return http.post('/v1/auth/change-password', data)
  },
}

// ==================== MFA多因素认证API ====================

/**
 * MFA管理API（与后端 /api/v1/mfa 对接）
 */
export const mfaApi = {
  // 获取用户的MFA设备列表
  getMfaDevices(): Promise<ApiResponse<MfaDevice[]>> {
    return http.get('/v1/mfa/devices')
  },

  // 启用TOTP（生成密钥和二维码）
  enableTotp(): Promise<ApiResponse<TotpSetupResponse>> {
    return http.post('/v1/mfa/totp/enable')
  },

  // 验证并确认TOTP设置
  confirmTotp(data: { code: string }): Promise<ApiResponse<void>> {
    return http.post('/v1/mfa/totp/confirm', data)
  },

  // 禁用TOTP
  disableTotp(): Promise<ApiResponse<void>> {
    return http.post('/v1/mfa/totp/disable')
  },

  // 启用短信MFA
  enableSms(): Promise<ApiResponse<void>> {
    return http.post('/v1/mfa/sms/enable')
  },

  // 验证短信验证码
  verifySms(data: { code: string }): Promise<ApiResponse<void>> {
    return http.post('/v1/mfa/sms/verify', data)
  },

  // 禁用短信MFA
  disableSms(): Promise<ApiResponse<void>> {
    return http.post('/v1/mfa/sms/disable')
  },

  // 启用邮箱MFA
  enableEmail(): Promise<ApiResponse<void>> {
    return http.post('/v1/mfa/email/enable')
  },

  // 验证邮箱验证码
  verifyEmail(data: { code: string }): Promise<ApiResponse<void>> {
    return http.post('/v1/mfa/email/verify', data)
  },

  // 禁用邮箱MFA
  disableEmail(): Promise<ApiResponse<void>> {
    return http.post('/v1/mfa/email/disable')
  },

  // 生成备用恢复码
  generateBackupCodes(): Promise<ApiResponse<string[]>> {
    return http.post('/v1/mfa/backup-codes/generate')
  },

  // 获取备用恢复码（仅显示一次）
  getBackupCodes(): Promise<ApiResponse<string[]>> {
    return http.get('/v1/mfa/backup-codes')
  },

  // 设置默认MFA设备
  setDefaultDevice(deviceId: string): Promise<ApiResponse<void>> {
    return http.post(`/v1/mfa/devices/${deviceId}/set-default`)
  },

  // 删除MFA设备
  removeDevice(deviceId: string): Promise<ApiResponse<void>> {
    return http.delete(`/v1/mfa/devices/${deviceId}`)
  },

  // 验证MFA代码
  verifyCode(data: MfaVerifyRequest): Promise<ApiResponse<MfaVerifyResponse>> {
    return http.post('/v1/mfa/verify', data)
  },
}

// ==================== 向后兼容 ====================

/**
 * @deprecated 使用 authApi.login 代替
 */
export const userApi = {
  login: authApi.login,
  logout: authApi.logout,
  getCurrentUser: authApi.getCurrentUser,
  refreshToken: authApi.refreshToken,
}

// ==================== 菜单API ====================

/**
 * 菜单API
 */
export const menuApi = {
  // 获取用户菜单
  getUserMenus(): Promise<ApiResponse<any[]>> {
    return http.get('/v1/auth/menus')
  },
}

// ==================== MFA工具函数 ====================

/**
 * 生成TOTP二维码URL（用于前端展示）
 */
export function generateTotpQrCodeUrl(secret: string, accountName: string, issuer = 'HKT-IoT'): string {
  return `otpauth://totp/${encodeURIComponent(accountName)}?secret=${secret}&issuer=${encodeURIComponent(issuer)}`
}

/**
 * 验证TOTP码格式（6位数字）
 */
export function isValidTotpCode(code: string): boolean {
  return /^\d{6}$/.test(code)
}

/**
 * 验证短信/邮箱验证码格式（6位数字）
 */
export function isValidVerificationCode(code: string): boolean {
  return /^\d{6}$/.test(code)
}

/**
 * MFA设备类型显示名称
 */
export const MfaDeviceTypeNames: Record<MfaDeviceType, string> = {
  TOTP: '身份验证器应用',
  SMS: '短信验证码',
  EMAIL: '邮箱验证码',
}

/**
 * 获取MFA设备类型显示名称
 */
export function getMfaDeviceTypeName(type: MfaDeviceType): string {
  return MfaDeviceTypeNames[type] || type
}
