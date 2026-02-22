/**
 * 自定义指令
 */
import type { App, Directive } from 'vue'
import { setupPermissionDirective } from './permission'

/**
 * 防抖指令
 */
export const debounce: Directive = {
  mounted(el: HTMLElement, binding) {
    let timer: number | null = null
    const delay = Number(binding.arg) || 300

    el.addEventListener('click', () => {
      if (timer) clearTimeout(timer)
      timer = window.setTimeout(() => {
        binding.value()
      }, delay)
    })
  },
}

/**
 * 节流指令
 */
export const throttle: Directive = {
  mounted(el: HTMLElement, binding) {
    let timer: number | null = null
    const delay = Number(binding.arg) || 300

    el.addEventListener('click', () => {
      if (timer) return
      timer = window.setTimeout(() => {
        binding.value()
        timer = null
      }, delay)
    })
  },
}

/**
 * 长按指令
 */
export const longpress: Directive = {
  mounted(el: HTMLElement, binding) {
    if (typeof binding.value !== 'function') return

    let timer: number | null = null

    const start = () => {
      timer = window.setTimeout(() => {
        binding.value()
      }, 500)
    }

    const cancel = () => {
      if (timer) clearTimeout(timer)
    }

    el.addEventListener('mousedown', start)
    el.addEventListener('touchstart', start)
    el.addEventListener('mouseup', cancel)
    el.addEventListener('mouseleave', cancel)
    el.addEventListener('touchend', cancel)
    el.addEventListener('touchcancel', cancel)
  },
}

/**
 * 复制到剪贴板指令
 */
export const copy: Directive = {
  mounted(el: HTMLElement, binding) {
    el.addEventListener('click', async () => {
      try {
        await navigator.clipboard.writeText(binding.value)
        // TODO: 显示成功提示
      } catch {
        // 降级方案
        const textarea = document.createElement('textarea')
        textarea.value = binding.value
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
      }
    })
  },
}

/**
 * 无限滚动指令
 */
export const infiniteScroll: Directive = {
  mounted(el: HTMLElement, binding) {
    const callback = binding.value
    const options = binding.arg || {}

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          callback()
        }
      })
    }, options)

    observer.observe(el)

    // 保存 observer 以便在 unmounted 时清理
    ;(el as any)._infiniteScrollObserver = observer
  },
  unmounted(el: HTMLElement) {
    const observer = (el as any)._infiniteScrollObserver
    if (observer) {
      observer.disconnect()
    }
  },
}

/**
 * 加载指令
 */
export const loading: Directive = {
  mounted(el: HTMLElement, binding) {
    if (binding.value) {
      el.classList.add('is-loading')
      const spinner = document.createElement('div')
      spinner.className = 'loading-spinner'
      spinner.innerHTML = '<a-spin />'
      el.appendChild(spinner)
      ;(el as any)._loadingSpinner = spinner
    }
  },
  updated(el: HTMLElement, binding) {
    const spinner = (el as any)._loadingSpinner
    if (binding.value) {
      el.classList.add('is-loading')
      if (!spinner) {
        const newSpinner = document.createElement('div')
        newSpinner.className = 'loading-spinner'
        newSpinner.innerHTML = '<a-spin />'
        el.appendChild(newSpinner)
        ;(el as any)._loadingSpinner = newSpinner
      }
    } else {
      el.classList.remove('is-loading')
      if (spinner) {
        spinner.remove()
        ;(el as any)._loadingSpinner = null
      }
    }
  },
}

/**
 * 注册所有指令
 */
export function setupDirectives(app: App) {
  setupPermissionDirective(app)

  app.directive('debounce', debounce)
  app.directive('throttle', throttle)
  app.directive('longpress', longpress)
  app.directive('copy', copy)
  app.directive('infinite-scroll', infiniteScroll)
  app.directive('loading', loading)
}
