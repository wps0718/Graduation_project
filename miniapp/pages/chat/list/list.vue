<template>
  <view class="chat-list-page">
    <view :style="{ height: `${statusBarHeight}px` }"></view>
    <view class="chat-nav" :style="{ height: `${navBarHeight}px` }">
      <view class="chat-nav__left" @click="goBack">
        <image class="chat-nav__back" src="/static/svg/back.svg" mode="aspectFit" />
      </view>
      <text class="chat-nav__title">消息</text>
      <view class="chat-nav__right">
        <view class="chat-nav__icon" @click="clearAllRead">
          <image class="chat-nav__icon-img" src="/static/svg/cleaning.svg" mode="aspectFit" />
        </view>
        <view class="chat-nav__icon" @click="showMoreMenu">
          <image class="chat-nav__icon-img" src="/static/svg/more.svg" mode="aspectFit" />
        </view>
      </view>
    </view>

    <view class="chat-entries">
      <view class="chat-entry" @click="goToReplyInbox">
        <view class="chat-entry__icon-wrap">
          <image class="chat-entry__icon" src="/static/svg/New Message.svg" mode="aspectFit" />
          <view v-if="unreadMessageCount > 0" class="chat-entry__badge">
            <text class="chat-entry__badge-text">{{ unreadMessageCount > 99 ? '99+' : unreadMessageCount }}</text>
          </view>
        </view>
        <text class="chat-entry__label">收到回复</text>
      </view>
      <view class="chat-entry" @click="goToReceivedFavorites">
        <view class="chat-entry__icon-wrap">
          <image class="chat-entry__icon" src="/static/svg/Received Favorite.svg" mode="aspectFit" />
          <view v-if="unreadFavoriteCount > 0" class="chat-entry__badge">
            <text class="chat-entry__badge-text">{{ unreadFavoriteCount > 99 ? '99+' : unreadFavoriteCount }}</text>
          </view>
        </view>
        <text class="chat-entry__label">收到收藏</text>
      </view>
      <view class="chat-entry" @click="goToNotifications('follow')">
        <view class="chat-entry__icon-wrap">
          <image class="chat-entry__icon" src="/static/svg/New Follower.svg" mode="aspectFit" />
          <view v-if="unreadFollowCount > 0" class="chat-entry__badge">
            <text class="chat-entry__badge-text">{{ unreadFollowCount > 99 ? '99+' : unreadFollowCount }}</text>
          </view>
        </view>
        <text class="chat-entry__label">新增关注</text>
      </view>
      <view class="chat-entry" @click="goToMessageSettings">
        <view class="chat-entry__icon-wrap">
          <image class="chat-entry__icon" src="/static/svg/Message Settings.svg" mode="aspectFit" />
        </view>
        <text class="chat-entry__label">消息设置</text>
      </view>
    </view>

    <scroll-view
      class="chat-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      lower-threshold="80"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="displayItems.length" class="chat-list__content">
        <view v-for="item in displayItems" :key="item.key" class="list-item">
          <view v-if="item.source === 'notification'" class="notification-item" @click="openNotification(item)">
            <view class="notification-item__avatar">
              <image class="notification-item__avatar-img" :src="notificationIcon(item)" mode="aspectFit" />
            </view>
            <view class="notification-item__body">
              <view class="notification-item__row">
                <text class="notification-item__title">{{ item.title }}</text>
                <text class="notification-item__time">{{ formatListTimeByTimestamp(item.timestamp) }}</text>
              </view>
              <view class="notification-item__row">
                <text class="notification-item__preview">{{ item.content }}</text>
                <view v-if="!item.isRead" class="notification-item__dot"></view>
              </view>
            </view>
          </view>

          <view v-else class="chat-item-wrap">
            <view
              class="chat-item"
              :class="{ 'is-dragging': isDragging && activeId === item.raw.id }"
              :style="itemStyle(item.raw)"
              @touchstart="onTouchStart(item.raw, $event)"
              @touchmove="onTouchMove(item.raw, $event)"
              @touchend="onTouchEnd(item.raw)"
              @longpress="onLongpress(item.raw)"
              @click="openSession(item.raw)"
            >
              <view class="chat-item__avatar">
                <image v-if="item.raw.productId && item.raw.productImage" 
                       class="chat-item__product-img" 
                       :src="item.raw.productImage" 
                       mode="aspectFill" />
                <UserAvatar
                  v-else
                  :avatar-url="item.raw.avatarUrl"
                  :nick-name="item.raw.nickName"
                  :auth-status="item.raw.authStatus"
                  size="md"
                />
              </view>
              <view class="chat-item__body">
                <view class="chat-item__row">
                  <view class="chat-item__name-row">
                    <text class="chat-item__name">{{ item.raw.nickName }}</text>
                    <view class="chat-item__tags">
                      <text v-if="item.raw.isStarred" class="chat-item__tag is-star">特别关注</text>
                      <text v-if="item.raw.isPinned" class="chat-item__tag is-pin">置顶</text>
                    </view>
                  </view>
                  <text class="chat-item__time">{{ formatListTimeByTimestamp(item.timestamp) }}</text>
                </view>
                <view class="chat-item__row">
                  <text class="chat-item__preview">{{ item.raw.lastMessage }}</text>
                  <view v-if="item.raw.unread" class="chat-item__badge">
                    <text class="chat-item__badge-text">{{ item.raw.unread > 99 ? '99+' : item.raw.unread }}</text>
                  </view>
                </view>
              </view>
            </view>
            <view class="chat-item__actions" :class="{ 'is-visible': isActionsVisible(item.raw) }">
              <view class="chat-item__action is-muted" @click.stop="markUnread(item.raw)">
                <view class="chat-item__action-text">
                  <text>设为</text>
                  <text>未读</text>
                </view>
              </view>
              <view class="chat-item__action is-primary" @click.stop="markRead(item.raw)">
                <view class="chat-item__action-text">
                  <text>设为</text>
                  <text>已读</text>
                </view>
              </view>
              <view class="chat-item__action is-danger" @click.stop="confirmDelete(item.raw)">
                <text class="chat-item__action-text">删除</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="notificationLoadingMore" class="chat-list__loading">加载中...</view>
        <view v-else-if="!notificationHasMore" class="chat-list__loading">没有更多了</view>
      </view>

      <view v-else class="chat-list__empty">
        <EmptyState type="no-message" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { useUserStore } from '@/store'
import EmptyState from '@/components/empty-state/empty-state.vue'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const navBarHeight = ref(44)

const sessions = ref([])
const notifications = ref([])

const loading = ref(false)
const notificationLoading = ref(false)
const notificationLoadingMore = ref(false)
const notificationPage = ref(1)
const notificationHasMore = ref(true)
const isRefreshing = ref(false)

const unreadMessageCount = ref(0)
const unreadFavoriteCount = ref(0)
const unreadFollowCount = ref(0)

const activeId = ref(null)
const isDragging = ref(false)
const deletedIds = ref([])
const blockedIds = ref([])
const starredIds = ref([])
const pinnedIds = ref([])
const touchInfo = ref({
  startX: 0,
  startY: 0,
  startOffset: 0
})

const maxSwipe = 240
const threshold = 90
const dragThreshold = 8
const pageSize = 20

const favoriteTypes = [6]
const followTypes = [11]

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

const displayItems = computed(() => {
  const chatItems = sessions.value.map((s) => ({
    key: `chat-${s.id}`,
    source: 'chat',
    id: s.id,
    timestamp: parseTime(s.lastTime),
    isPinned: !!s.isPinned,
    raw: s
  }))
  const notificationItems = notifications.value.map((n) => ({
    key: `notification-${n.id}`,
    source: 'notification',
    id: n.id,
    title: n.title || '系统通知',
    content: n.content || '',
    timestamp: parseTime(n.createTime),
    isRead: Number(n.isRead) === 1,
    raw: n
  }))
  return chatItems
    .concat(notificationItems)
    .sort((a, b) => {
      const pinA = a.source === 'chat' && a.isPinned
      const pinB = b.source === 'chat' && b.isPinned
      if (pinA && !pinB) return -1
      if (!pinA && pinB) return 1
      return (b.timestamp || 0) - (a.timestamp || 0)
    })
})

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function parseTime(value) {
  if (!value) return 0
  if (typeof value === 'number') return value
  // 兼容 iOS 端的日期解析
  const normalized = String(value).replace('T', ' ').replace(/-/g, '/')
  const date = new Date(normalized)
  const timestamp = date.getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

function formatListTimeByTimestamp(timestamp) {
  if (!timestamp) return ''
  const time = new Date(timestamp)
  const now = new Date()
  const sameDay =
    time.getFullYear() === now.getFullYear() &&
    time.getMonth() === now.getMonth() &&
    time.getDate() === now.getDate()
  if (sameDay) {
    const hour = `${time.getHours()}`.padStart(2, '0')
    const minute = `${time.getMinutes()}`.padStart(2, '0')
    return `${hour}:${minute}`
  }
  const month = `${time.getMonth() + 1}`.padStart(2, '0')
  const day = `${time.getDate()}`.padStart(2, '0')
  return `${month}-${day}`
}

function notificationIcon(item) {
  const type = Number(item && item.raw && item.raw.type)
  if (favoriteTypes.includes(type)) return '/static/svg/favorite.svg'
  if (type === 2) return '/static/svg/message.svg'
  return '/static/svg/email.svg'
}

function ensureLoginSilent() {
  return !!userStore.isLogin
}

function goBack() {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: '/pages/index/index' })
    return
  }
  uni.navigateBack()
}

function goToNotifications(entry) {
  if (!ensureLoginSilent()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  if (entry === 'favorite') {
    uni.navigateTo({ url: '/pages/notification/notification?type=6' })
    return
  }
  if (entry === 'follow') {
    uni.navigateTo({ url: '/pages/notification/follower' })
    return
  }
  uni.navigateTo({ url: '/pages/notification/notification' })
}

function goToReplyInbox() {
  if (!ensureLoginSilent()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: '/pages/notification/received-replies' })
}

function goToReceivedFavorites() {
  if (!ensureLoginSilent()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: '/pages/notification/received-favorites' })
}

function goToMessageSettings() {
  uni.navigateTo({ url: '/pages/chat/settings/settings' })
}

function showMoreMenu() {
  uni.showActionSheet({
    itemList: ['通讯录', '消息通知设置'],
    success: (res) => {
      if (!res) return
      if (res.tapIndex === 0) {
        showToast('通讯录开发中')
      } else if (res.tapIndex === 1) {
        showToast('消息通知设置开发中')
      }
    }
  })
}

function openNotification(item) {
  const raw = item && item.raw
  if (!raw) return
  uni.navigateTo({ url: '/pages/notification/notification' })
}

function itemStyle(item) {
  const offset = item.offsetX || 0
  return {
    transform: `translate3d(${offset}px, 0, 0)`
  }
}

function isActionsVisible(item) {
  return (item.offsetX || 0) <= -threshold
}

function onTouchStart(item, event) {
  const touch = event.touches && event.touches[0]
  if (!touch) return
  activeId.value = item.id
  isDragging.value = false
  touchInfo.value = {
    startX: touch.clientX,
    startY: touch.clientY,
    startOffset: item.offsetX || 0
  }
  sessions.value.forEach((session) => {
    if (session.id !== item.id && session.offsetX) {
      session.offsetX = 0
    }
  })
}

function onTouchMove(item, event) {
  const touch = event.touches && event.touches[0]
  if (!touch) return
  const dx = touch.clientX - touchInfo.value.startX
  const dy = touch.clientY - touchInfo.value.startY
  if (Math.abs(dx) < dragThreshold && Math.abs(dy) < dragThreshold) return
  if (Math.abs(dx) < Math.abs(dy)) return
  if (activeId.value !== item.id) return
  isDragging.value = true
  const nextOffset = clamp(touchInfo.value.startOffset + dx, -maxSwipe, 0)
  item.offsetX = nextOffset
}

function onTouchEnd(item) {
  if (activeId.value !== item.id) return
  if (item.offsetX <= -threshold) {
    item.offsetX = -maxSwipe
  } else {
    item.offsetX = 0
  }
  isDragging.value = false
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value))
}

function openSession(item) {
  if (item.offsetX) {
    item.offsetX = 0
    return
  }
  if (item.unread) {
    item.unread = 0
  }
  const params = [
    `sessionKey=${item.sessionKey}`,
    `peerId=${item.userId}`,
    item.productId ? `productId=${item.productId}` : ''
  ]
    .filter(Boolean)
    .join('&')
  uni.navigateTo({ url: `/pages/chat/detail/detail?${params}` })
}

function onLongpress(item) {
  if (isDragging.value) return
  item.offsetX = 0
  const starLabel = item.isStarred ? '取消特别关注' : '设为特别关注'
  const pinLabel = item.isPinned ? '取消置顶' : '置顶'
  uni.showActionSheet({
    itemList: [starLabel, pinLabel],
    success: (res) => {
      if (!res) return
      if (res.tapIndex === 0) {
        toggleStar(item)
      } else if (res.tapIndex === 1) {
        togglePin(item)
      }
    }
  })
}

function toggleStar(item) {
  item.isStarred = !item.isStarred
  if (item.isStarred) {
    if (!starredIds.value.includes(item.userId)) {
      starredIds.value.push(item.userId)
    }
  } else {
    starredIds.value = starredIds.value.filter((id) => id !== item.userId)
  }
  saveIds('chat_starred_ids', starredIds.value)
}

function togglePin(item) {
  item.isPinned = !item.isPinned
  if (item.isPinned) {
    if (!pinnedIds.value.includes(item.userId)) {
      pinnedIds.value.push(item.userId)
    }
  } else {
    pinnedIds.value = pinnedIds.value.filter((id) => id !== item.userId)
  }
  saveIds('chat_pinned_ids', pinnedIds.value)
  sessions.value = sortSessions(sessions.value)
}

function closeSwipe(item) {
  item.offsetX = 0
}

function markUnread(item) {
  item.unread = item.unread > 0 ? item.unread : 1
  closeSwipe(item)
  showToast('已设为未读')
}

function markRead(item) {
  item.unread = 0
  closeSwipe(item)
  showToast('已设为已读')
}

async function confirmDelete(item) {
  const confirm = await showConfirm('确认删除该会话？')
  if (!confirm) return
  try {
    await post('/mini/chat/delete', { sessionId: item.id }, { showLoading: true })
    if (!deletedIds.value.includes(item.userId)) {
      deletedIds.value.push(item.userId)
      saveIds('chat_deleted_ids', deletedIds.value)
    }
    sessions.value = sessions.value.filter((session) => session.id !== item.id)
    showToast('已删除')
  } catch (error) {
    showToast('删除失败，请重试')
  }
}

function getIds(key) {
  const list = uni.getStorageSync(key)
  return Array.isArray(list) ? list : []
}

function saveIds(key, list) {
  uni.setStorageSync(key, list)
}

function sortSessions(list) {
  return list.slice().sort((a, b) => {
    if (a.isPinned && !b.isPinned) return -1
    if (!a.isPinned && b.isPinned) return 1
    return parseTime(b.lastTime) - parseTime(a.lastTime)
  })
}

function showConfirm(content) {
  return new Promise((resolve) => {
    uni.showModal({
      title: '提示',
      content,
      confirmText: '确定',
      cancelText: '取消',
      success: (res) => resolve(res && res.confirm)
    })
  })
}

async function loadSessions() {
  if (!ensureLoginSilent()) {
    sessions.value = []
    return
  }
  if (loading.value) return
  loading.value = true
  try {
    const data = await get('/mini/chat/list', {}, { showLoading: false })
    const list = (data && data.records) || []
    deletedIds.value = getIds('chat_deleted_ids')
    blockedIds.value = getIds('chat_blocked_ids')
    starredIds.value = getIds('chat_starred_ids')
    pinnedIds.value = getIds('chat_pinned_ids')
    const selfId = (userStore.userInfo && userStore.userInfo.id) || 0
    const filtered = list.filter((item) => {
      if (!item) return false
      if (item.userId === selfId) return false
      if (blockedIds.value.includes(item.userId)) return false
      if (deletedIds.value.includes(item.userId)) return false
      return true
    })
    const normalized = filtered.map((item) => ({
      ...item,
      isStarred: starredIds.value.includes(item.userId),
      isPinned: pinnedIds.value.includes(item.userId),
      offsetX: 0
    }))
    sessions.value = sortSessions(normalized)
  } catch (error) {
    showToast('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

async function loadNotifications(targetPage, refresh = false) {
  if (!ensureLoginSilent()) {
    notifications.value = []
    notificationHasMore.value = true
    notificationPage.value = 1
    return
  }
  if (notificationLoading.value) return
  notificationLoading.value = true
  try {
    const data = await get(
      '/mini/notification/list',
      {
        page: targetPage,
        pageSize
      },
      { showLoading: refresh && targetPage === 1 }
    )
    const records = Array.isArray(data) ? data : (data.records || [])
    const total = Array.isArray(data) ? records.length : (data.total ?? records.length)
    if (refresh) {
      notifications.value = records
    } else {
      notifications.value = notifications.value.concat(records)
    }
    notificationPage.value = targetPage
    if (records.length < pageSize) {
      notificationHasMore.value = false
    } else {
      notificationHasMore.value = notifications.value.length < total
    }
    recalcBadges()
  } catch (error) {
    showToast('加载失败，请稍后重试')
  } finally {
    notificationLoading.value = false
    notificationLoadingMore.value = false
    if (refresh) {
      isRefreshing.value = false
      uni.stopPullDownRefresh()
    }
  }
}

async function loadUnreadCount() {
  if (!ensureLoginSilent()) {
    unreadMessageCount.value = 0
    return
  }
  try {
    const data = await get('/mini/product/comment/unread-reply-count', {}, { showLoading: false })
    unreadMessageCount.value = Number(data || 0)
  } catch (error) {
    unreadMessageCount.value = 0
  }
}

function recalcBadges() {
  const list = notifications.value || []
  unreadFavoriteCount.value = list.filter((n) => Number(n.isRead) !== 1 && favoriteTypes.includes(Number(n.type))).length
  unreadFollowCount.value = list.filter((n) => Number(n.isRead) !== 1 && followTypes.includes(Number(n.type))).length
}

async function refreshAll(showLoading = false) {
  if (!ensureLoginSilent()) {
    sessions.value = []
    notifications.value = []
    unreadMessageCount.value = 0
    unreadFavoriteCount.value = 0
    unreadFollowCount.value = 0
    return
  }
  if (showLoading) {
    uni.showLoading({ title: '加载中', mask: true })
  }
  try {
    await Promise.all([loadUnreadCount(), loadSessions(), loadNotifications(1, true)])
  } finally {
    if (showLoading) {
      uni.hideLoading()
    }
  }
}

function onRefresh() {
  isRefreshing.value = true
  notificationHasMore.value = true
  refreshAll(false)
}

function loadMore() {
  if (!notificationHasMore.value || notificationLoading.value) return
  notificationLoadingMore.value = true
  loadNotifications(notificationPage.value + 1, false)
}

async function clearAllRead() {
  if (!ensureLoginSilent()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  const confirm = await showConfirm('确定将所有消息标为已读吗？')
  if (!confirm) return
  try {
    await post('/mini/product/comment/mark-read', {}, { showLoading: false })
    await post('/mini/notification/read-all', {}, { showLoading: true })
    const unreadSessions = sessions.value.filter((s) => s && s.unread && s.sessionKey)
    const tasks = unreadSessions.map((s) =>
      post('/mini/chat/read', { sessionKey: s.sessionKey }, { showLoading: false })
    )
    await Promise.allSettled(tasks)
    sessions.value.forEach((s) => {
      if (s && s.unread) s.unread = 0
    })
    await refreshAll(false)
    showToast('已全部标为已读')
  } catch (error) {
    showToast('操作失败，请稍后重试')
  }
}

onShow(() => {
  refreshAll(false)
})

onPullDownRefresh(() => {
  notificationHasMore.value = true
  refreshAll(false)
})

onReachBottom(() => {
  loadMore()
})
</script>

<style lang="scss" scoped>
.chat-list-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  display: flex;
  flex-direction: column;
}

.chat-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-md);
  background-color: var(--bg-white);
  box-shadow: 0 6rpx 20rpx rgba(15, 23, 42, 0.05);
}

.chat-nav__left,
.chat-nav__right {
  width: 140rpx;
  display: flex;
  align-items: center;
}

.chat-nav__right {
  justify-content: flex-end;
  gap: 14rpx;
}

.chat-nav__back {
  width: 44rpx;
  height: 44rpx;
}

.chat-nav__title {
  font-size: var(--font-lg);
  color: var(--text-primary);
  font-weight: 600;
}

.chat-nav__icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 18rpx;
  background-color: var(--bg-grey);
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-nav__icon-img {
  width: 34rpx;
  height: 34rpx;
}

.chat-entries {
  display: flex;
  justify-content: space-between;
  padding: var(--spacing-md);
  gap: 20rpx;
}

.chat-entry {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14rpx;
}

.chat-entry__icon-wrap {
  width: 92rpx;
  height: 92rpx;
  border-radius: 999rpx;
  background-color: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.06);
}

.chat-entry__icon {
  width: 52rpx;
  height: 52rpx;
}

.chat-entry__label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.chat-entry__badge {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  background-color: var(--danger-color);
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: -10rpx;
  right: -10rpx;
}

.chat-entry__badge-text {
  font-size: 20rpx;
  color: var(--text-white);
}

.chat-scroll {
  flex: 1;
  padding: 0 var(--spacing-md) var(--spacing-lg);
}

.chat-list__content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.chat-list__empty {
  padding-top: var(--spacing-xl);
}

.chat-list__loading {
  padding: var(--spacing-md) 0;
  text-align: center;
  color: var(--text-placeholder);
  font-size: var(--font-sm);
}

.notification-item {
  display: flex;
  align-items: center;
  padding: 22rpx var(--spacing-md);
  border-radius: var(--radius-lg);
  background-color: var(--bg-white);
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.06);
  gap: var(--spacing-md);
}

.notification-item__avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 999rpx;
  background-color: var(--bg-grey);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-item__avatar-img {
  width: 44rpx;
  height: 44rpx;
}

.notification-item__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  overflow: hidden;
}

.notification-item__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-sm);
}

.notification-item__title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.notification-item__time {
  font-size: 22rpx;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.notification-item__preview {
  flex: 1;
  font-size: 24rpx;
  color: var(--text-secondary);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.notification-item__dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 999rpx;
  background-color: var(--danger-color);
  flex-shrink: 0;
}

.chat-item-wrap {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg);
  background-color: var(--bg-white);
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.06);
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 22rpx var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  transition: transform 0.2s ease;
  will-change: transform;
}

.chat-item.is-dragging {
  transition: none;
}

.chat-item__actions {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 240rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 0 16rpx;
  background-color: var(--surface-muted);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.chat-item__actions.is-visible {
  opacity: 1;
  pointer-events: auto;
}

.chat-item__action {
  flex: 1;
  height: 86rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-item__action.is-muted {
  background-color: var(--bg-white);
  border: 1rpx solid var(--border-strong);
}

.chat-item__action.is-primary {
  background-color: var(--primary-tint);
  border: 1rpx solid var(--primary-border);
}

.chat-item__action.is-danger {
  background-color: var(--danger-tint);
  border: 1rpx solid var(--danger-border);
}

.chat-item__action-text {
  font-size: 22rpx;
  color: var(--text-primary);
  font-weight: 600;
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.1;
}

.chat-item__avatar {
  width: 96rpx;
  height: 96rpx;
  flex-shrink: 0;
  margin-right: var(--spacing-md);
}

.chat-item__product-img {
  width: 96rpx;
  height: 96rpx;
  border-radius: var(--radius-sm);
  background-color: var(--bg-grey);
}

.chat-item__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.chat-item__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-sm);
}

.chat-item__name-row {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  overflow: hidden;
}

.chat-item__tags {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.chat-item__tag {
  padding: 4rpx 10rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  line-height: 1;
}

.chat-item__tag.is-star {
  background-color: var(--primary-bg);
  color: var(--primary-color);
}

.chat-item__tag.is-pin {
  background-color: var(--bg-grey);
  color: var(--text-regular);
}

.chat-item__name {
  flex: 1;
  font-size: 30rpx;
  color: var(--text-primary);
  font-weight: 600;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.chat-item__time {
  font-size: 22rpx;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.chat-item__preview {
  flex: 1;
  font-size: 24rpx;
  color: var(--text-secondary);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.chat-item__badge {
  min-width: 34rpx;
  height: 34rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  background-color: var(--danger-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-item__badge-text {
  font-size: 20rpx;
  color: var(--text-white);
}
</style>
