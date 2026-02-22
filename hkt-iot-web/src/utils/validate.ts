/**
 * 验证工具函数
 */

/**
 * 验证邮箱
 */
export function isEmail(value: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(value)
}

/**
 * 验证手机号（中国大陆）
 */
export function isPhone(value: string): boolean {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(value)
}

/**
 * 验证身份证号
 */
export function isIdCard(value: string): boolean {
  const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  return idCardRegex.test(value)
}

/**
 * 验证URL
 */
export function isUrl(value: string): boolean {
  try {
    new URL(value)
    return true
  } catch {
    return false
  }
}

/**
 * 验证IP地址
 */
export function isIP(value: string): boolean {
  const ipRegex = /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
  return ipRegex.test(value)
}

/**
 * 验证MAC地址
 */
export function isMac(value: string): boolean {
  const macRegex = /^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/
  return macRegex.test(value)
}

/**
 * 验证数字
 */
export function isNumber(value: any): boolean {
  return !isNaN(parseFloat(value)) && isFinite(value)
}

/**
 * 验证整数
 */
export function isInteger(value: any): boolean {
  return Number.isInteger(Number(value))
}

/**
 * 验证正数
 */
export function isPositive(value: number): boolean {
  return value > 0
}

/**
 * 验证非负数
 */
export function isNonNegative(value: number): boolean {
  return value >= 0
}

/**
 * 验证范围
 */
export function isInRange(value: number, min: number, max: number): boolean {
  return value >= min && value <= max
}

/**
 * 验证字符串长度
 */
export function isLength(value: string, min: number, max: number): boolean {
  return value.length >= min && value.length <= max
}

/**
 * 验证是否为空
 */
export function isEmpty(value: any): boolean {
  if (value === null || value === undefined) return true
  if (typeof value === 'string') return value.trim().length === 0
  if (Array.isArray(value)) return value.length === 0
  if (typeof value === 'object') return Object.keys(value).length === 0
  return false
}

/**
 * 验证JSON字符串
 */
export function isJSON(value: string): boolean {
  try {
    JSON.parse(value)
    return true
  } catch {
    return false
  }
}

/**
 * 验证密码强度
 */
export function checkPasswordStrength(password: string): 'weak' | 'medium' | 'strong' {
  let score = 0

  if (password.length >= 8) score++
  if (/[a-z]/.test(password)) score++
  if (/[A-Z]/.test(password)) score++
  if (/[0-9]/.test(password)) score++
  if (/[^a-zA-Z0-9]/.test(password)) score++

  if (score <= 2) return 'weak'
  if (score <= 3) return 'medium'
  return 'strong'
}

/**
 * Ant Design Vue 表单验证规则
 */
export const FormRules = {
  // 必填
  required: (message = '该字段为必填项') => ({
    required: true,
    message,
  }),

  // 邮箱
  email: (message = '请输入有效的邮箱地址') => ({
    type: 'email' as const,
    message,
  }),

  // 手机号
  phone: (message = '请输入有效的手机号') => ({
    pattern: /^1[3-9]\d{9}$/,
    message,
  }),

  // URL
  url: (message = '请输入有效的URL') => ({
    type: 'url' as const,
    message,
  }),

  // 数字范围
  range: (min: number, max: number, message?: string) => ({
    type: 'number' as const,
    min,
    max,
    message: message || `请输入 ${min} 到 ${max} 之间的数字`,
  }),

  // 字符串长度
  length: (min: number, max: number, message?: string) => ({
    min,
    max,
    message: message || `请输入 ${min} 到 ${max} 个字符`,
  }),

  // 自定义正则
  pattern: (regex: RegExp, message: string) => ({
    pattern: regex,
    message,
  }),
}
