/**
 * 权限指令
 */
import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores'

export interface PermissionBinding {
  permission?: string | string[]
  role?: string | string[]
  has?: (user: any) => boolean
}

/**
 * 权限检查
 */
function checkPermission(binding: DirectiveBinding): boolean {
  const { value } = binding
  const userStore = useUserStore()
  const { roles, permissions } = userStore

  if (!value) return true

  // 角色检查
  if (value.role) {
    const requiredRoles = Array.isArray(value.role) ? value.role : [value.role]
    return requiredRoles.some((role: string) => roles.includes(role))
  }

  // 权限检查
  if (value.permission) {
    const requiredPermissions = Array.isArray(value.permission) ? value.permission : [value.permission]
    // TODO: 实现权限检查逻辑
    // return requiredPermissions.some((permission: string) => permissions.includes(permission))
    return true
  }

  // 自定义检查函数
  if (value.has && typeof value.has === 'function') {
    return value.has(userStore.userInfo)
  }

  return true
}

/**
 * v-permission 指令
 * 用法：v-permission="{ role: 'admin' }" 或 v-permission="{ permission: 'user:create' }"
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const hasPermission = checkPermission(binding)
    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  },
}

/**
 * v-role 指令
 * 用法：v-role="'admin'" 或 v-role="['admin', 'editor']"
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const userStore = useUserStore()
    const { roles } = userStore

    if (!value) return

    const requiredRoles = Array.isArray(value) ? value : [value]
    const hasRole = requiredRoles.some((r: string) => roles.includes(r))

    if (!hasRole) {
      el.parentNode?.removeChild(el)
    }
  },
}

/**
 * 注册所有指令
 */
export function setupPermissionDirective(app: any) {
  app.directive('permission', permission)
  app.directive('role', role)
}
