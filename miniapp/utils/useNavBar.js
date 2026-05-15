import { ref } from 'vue'

/**
 * 获取状态栏和导航栏高度
 * @returns {{ statusBarHeight: import('vue').Ref<number>, navBarHeight: import('vue').Ref<number> }}
 */
export function useNavBar() {
  const statusBarHeight = ref(0)
  const navBarHeight = ref(44)

  const info = uni.getSystemInfoSync()
  statusBarHeight.value = (info && info.statusBarHeight) || 0

  const menuButton = typeof uni.getMenuButtonBoundingClientRect === 'function'
    ? uni.getMenuButtonBoundingClientRect()
    : null

  if (menuButton && menuButton.top) {
    const padding = menuButton.top - statusBarHeight.value
    navBarHeight.value = menuButton.height + padding * 2
  }

  return { statusBarHeight, navBarHeight }
}
