<template>
  <view class="notification-page">
    <view :style="{ height: `${statusBarHeight}px` }"></view>
    <view class="notification-nav" :style="{ height: `${navBarHeight}px` }">
      <view class="notification-nav__left" @click="goBack">
        <text class="notification-nav__close">×</text>
      </view>
      <text class="notification-nav__title">消息中心</text>
      <view class="notification-nav__right" :class="{ 'is-disabled': !canClear }" @click="confirmClear">
        <text class="notification-nav__action">清空</text>
      </view>
    </view>

    <view class="notification-tabs">
      <view
        v-for="item in tabs"
        :key="item.value"
        class="notification-tab"
        :class="{ 'is-active': activeTab === item.value }"
        @click="switchTab(item.value)"
      >
        <text class="notification-tab__text">{{ item.label }}</text>
      </view>
    </view>

    <view class="notification-content">
      <view v-if="messageList.length" class="notification-list">
        <view
          v-for="item in messageList"
          :key="item.id"
          class="notification-item"
          @click="onMessageClick(item)"
        >
          <view v-if="!item.isRead" class="notification-item__dot"></view>
          <view class="notification-item__icon-wrap">
            <view class="notification-item__icon" :class="iconClass(item)">
              <text class="notification-item__icon-text">{{ iconText(item) }}</text>
            </view>
          </view>
          <view class="notification-item__content">
            <view class="notification-item__header">
              <text class="notification-item__title">{{ item.title }}</text>
              <text class="notification-item__time">{{ item.createTime }}</text>
            </view>
            <text class="notification-item__desc">{{ item.content }}</text>
          </view>
        </view>
        <view v-if="loadingMore" class="notification-loading">加载中...</view>
        <view v-else-if="!hasMore" class="notification-loading">没有更多了</view>
      </view>
      <view v-else class="notification-empty">
        <EmptyState type="no-message" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { useUserStore } from '@/store'
import EmptyState from '@/components/empty-state/empty-state.vue'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const navBarHeight = ref(44)

const tabs = [
  { label: '全部', value: 0 },
  { label: '交易', value: 1 },
  { label: '系统', value: 2 }
]

const activeTab = ref(0)
const filterType = ref(null)
const messageList = ref([])
const page = ref(1)
const pageSize = 20
const hasMore = ref(true)
const loading = ref(false)
const loadingMore = ref(false)

const canClear = computed(() => messageList.value.length > 0)

const typeMap = {
  1: { icon: '✓', colorClass: 'is-success' },
  2: { icon: '💬', colorClass: 'is-primary' },
  3: { icon: '⚠', colorClass: 'is-warning' },
  4: { icon: '📢', colorClass: 'is-primary' },
  5: { icon: '❤', colorClass: 'is-danger' },
  6: { icon: '✕', colorClass: 'is-secondary' },
  11: { icon: '👤', colorClass: 'is-primary' }
}

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function ensureLogin() {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login/login' })
    return false
  }
  return true
}

function iconText(item) {
  return (item && typeMap[item.type] && typeMap[item.type].icon) || '💬'
}

function iconClass(item) {
  return (item && typeMap[item.type] && typeMap[item.type].colorClass) || 'is-primary'
}

function goBack() {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: '/pages/index/index' })
    return
  }
  uni.navigateBack()
}

function switchTab(value) {
  if (activeTab.value === value) return
  activeTab.value = value
  resetPagination()
  fetchNotifications(1, true)
}

function resetPagination() {
  page.value = 1
  hasMore.value = true
  loadingMore.value = false
}

function applyCategoryFilter(list, category) {
  if (!category) return list
  const hasCategory = list.some((item) => item && item.category !== undefined)
  if (!hasCategory) return list
  return list.filter((item) => item && Number(item.category) === Number(category))
}

function applyTypeFilter(list) {
  if (!filterType.value) return list
  const raw = String(filterType.value || '')
  const parts = raw
    .split(',')
    .map((v) => Number(v))
    .filter((v) => Number.isFinite(v) && v > 0)
  if (!parts.length) return list
  return list.filter((item) => item && parts.includes(Number(item.type)))
}

async function fetchNotifications(targetPage, refresh = false) {
  if (!ensureLogin()) return
  if (loading.value) return
  loading.value = true
  try {
    const data = await get(
      '/mini/notification/list',
      {
        page: targetPage,
        pageSize,
        category: activeTab.value > 0 ? activeTab.value : undefined
      },
      { showLoading: refresh || targetPage === 1 }
    )
    const records = Array.isArray(data) ? data : (data.records || [])
    const total = Array.isArray(data) ? records.length : (data.total ?? records.length)
    const filtered = applyTypeFilter(applyCategoryFilter(records, activeTab.value))
    const isServerPaged = !Array.isArray(data) && typeof data.total === 'number' && data.total > records.length
    if (isServerPaged) {
      if (refresh) {
        messageList.value = filtered
      } else {
        messageList.value = messageList.value.concat(filtered)
      }
      if (filtered.length < pageSize) {
        hasMore.value = false
      } else {
        hasMore.value = messageList.value.length < total
      }
    } else {
      const start = (targetPage - 1) * pageSize
      const end = start + pageSize
      const pageRecords = filtered.slice(start, end)
      if (refresh) {
        messageList.value = pageRecords
      } else {
        messageList.value = messageList.value.concat(pageRecords)
      }
      hasMore.value = end < filtered.length
    }
    page.value = targetPage
  } catch (error) {
    showToast('加载失败，请稍后重试')
  } finally {
    loading.value = false
    loadingMore.value = false
    if (refresh) {
      uni.stopPullDownRefresh()
    }
  }
}

function loadMore() {
  if (!hasMore.value || loading.value) return
  loadingMore.value = true
  fetchNotifications(page.value + 1)
}

async function markAsRead(item) {
  if (!item || item.isRead) return
  try {
    await post('/mini/notification/read', { id: item.id }, { showLoading: false })
    item.isRead = true
  } catch (error) {
    showToast('标记失败，请稍后重试')
  }
}

function showSystemNotice(item) {
  if (!item) return
  uni.showModal({
    title: item.title || '系统公告',
    content: item.content || '',
    showCancel: false
  })
}

async function onMessageClick(item) {
  if (!item) return
  await markAsRead(item)
  const relatedType = Number(item.relatedType || 0)
  if (relatedType === 1 && item.relatedId) {
    uni.navigateTo({ url: `/pages/product/detail/detail?id=${item.relatedId}` })
    return
  }
  if (relatedType === 2 && item.relatedId) {
    uni.navigateTo({ url: `/pages/order/detail/detail?id=${item.relatedId}` })
    return
  }
  if (relatedType === 3) {
    uni.navigateTo({ url: '/pages/auth/auth' })
    return
  }
  if (item.type === 11) {
    uni.navigateTo({ url: '/pages/notification/follower' })
    return
  }
  if (relatedType === 4) {
    showSystemNotice(item)
  }
}

function confirmClear() {
  if (!messageList.value.length) {
    showToast('暂无可清空')
    return
  }
  uni.showModal({
    title: '提示',
    content: '确定将所有消息标为已读吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/notification/read-all', {}, { showLoading: false })
        resetPagination()
        await fetchNotifications(1, true)
        showToast('已清空')
      } catch (error) {
        showToast('操作失败，请稍后重试')
      }
    }
  })
}

onMounted(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = (info && info.statusBarHeight) || 0
  const menuButton = typeof uni.getMenuButtonBoundingClientRect === 'function'
    ? uni.getMenuButtonBoundingClientRect()
    : null
  if (menuButton && menuButton.top) {
    const padding = menuButton.top - statusBarHeight.value
    navBarHeight.value = menuButton.height + padding * 2
  } else {
    navBarHeight.value = 44
  }
})

onLoad((options) => {
  if (options && options.category !== undefined && options.category !== null && options.category !== '') {
    const next = Number(options.category)
    if (Number.isFinite(next)) {
      activeTab.value = next
    }
  }
  if (options && options.type !== undefined && options.type !== null && options.type !== '') {
    filterType.value = options.type
  } else {
    filterType.value = null
  }
  resetPagination()
  fetchNotifications(1, true)
})

onPullDownRefresh(() => {
  resetPagination()
  fetchNotifications(1, true)
})

onReachBottom(() => {
  loadMore()
})
</script>

<style lang="scss" scoped>
.notification-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

.notification-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-md);
  background-color: var(--bg-page);
}

.notification-nav__left,
.notification-nav__right {
  width: 120rpx;
  display: flex;
  align-items: center;
}

.notification-nav__right {
  justify-content: flex-end;
}

.notification-nav__right.is-disabled {
  opacity: 0.5;
}

.notification-nav__right.is-disabled .notification-nav__action {
  color: var(--text-placeholder);
}

.notification-nav__close {
  font-size: 48rpx;
  color: var(--text-secondary);
}

.notification-nav__title {
  font-size: var(--font-lg);
  color: var(--text-primary);
  font-weight: 600;
}

.notification-nav__action {
  font-size: var(--font-md);
  color: var(--primary-color);
  font-weight: 600;
}

.notification-tabs {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-md) var(--spacing-sm);
}

.notification-tab {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: var(--radius-round);
  background-color: var(--bg-grey);
}

.notification-tab.is-active {
  background-color: var(--primary-bg);
}

.notification-tab__text {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.notification-tab.is-active .notification-tab__text {
  color: var(--primary-color);
  font-weight: 600;
}

.notification-content {
  padding: 0 var(--spacing-md) var(--spacing-lg);
}

.notification-item {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: 20rpx;
  margin-bottom: var(--spacing-md);
  position: relative;
  box-shadow: 0 10rpx 28rpx rgba(0, 0, 0, 0.04);
}

.notification-item__icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  background-color: var(--bg-grey);
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-item__icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 30rpx;
  background-color: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
}

.notification-item__icon.is-success {
  color: var(--success-color);
}

.notification-item__icon.is-primary {
  color: var(--primary-color);
}

.notification-item__icon.is-warning {
  color: var(--warning-color);
}

.notification-item__icon.is-danger {
  color: var(--danger-color);
}

.notification-item__icon.is-secondary {
  color: var(--text-secondary);
}

.notification-item__dot {
  position: absolute;
  left: 12rpx;
  top: 24rpx;
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: var(--primary-color);
}

.notification-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.notification-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-sm);
}

.notification-item__title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
}

.notification-item__time {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.notification-item__desc {
  font-size: var(--font-sm);
  color: var(--text-regular);
  line-height: 1.4;
}

.notification-loading {
  text-align: center;
  padding: var(--spacing-md) 0;
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

.notification-empty {
  padding-top: var(--spacing-xl);
}
</style>
