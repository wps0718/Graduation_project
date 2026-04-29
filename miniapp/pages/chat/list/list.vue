<template>
  <view class="page">
    <!-- 状态栏 -->
    <view :style="{ height: `${statusBarHeight}px` }"></view>

    <!-- 导航栏 -->
    <view class="nav" :style="{ height: `${navBarHeight}px` }">
      <view class="nav__left" @click="goBack">
        <image class="nav__back" src="/static/svg/back.svg" mode="aspectFit" />
      </view>
      <text class="nav__title">消息</text>
      <view class="nav__right">
        <text
          class="nav__read-all"
          :class="{ disabled: totalUnread === 0 }"
          @click="clearAllRead"
        >全部已读</text>
        <text class="nav__gear" @click="goToMessageSettings">⚙</text>
        <view class="nav__more" @click="showMoreMenu">
          <image class="nav__more-img" src="/static/svg/more.svg" mode="aspectFit" />
        </view>
      </view>
    </view>

    <!-- 顶部快捷入口（三个） -->
    <view class="quick-entry">
      <view class="quick-entry__item" @click="goToReplyInbox">
        <view class="quick-icon quick-icon--blue">
          <text class="quick-icon__inner">💬</text>
          <text v-if="unreadMessageCount > 0" class="quick-badge">{{ unreadMessageCount > 99 ? '99+' : unreadMessageCount }}</text>
        </view>
        <text class="quick-label">收到回复</text>
      </view>
      <view class="quick-entry__item" @click="goToReceivedFavorites">
        <view class="quick-icon quick-icon--red">
          <text class="quick-icon__inner">❤</text>
          <text v-if="unreadFavoriteCount > 0" class="quick-badge">{{ unreadFavoriteCount > 99 ? '99+' : unreadFavoriteCount }}</text>
        </view>
        <text class="quick-label">收到收藏</text>
      </view>
      <view class="quick-entry__item" @click="goToNotifications('follow')">
        <view class="quick-icon quick-icon--green">
          <text class="quick-icon__inner">👤</text>
          <text v-if="unreadFollowCount > 0" class="quick-badge">{{ unreadFollowCount > 99 ? '99+' : unreadFollowCount }}</text>
        </view>
        <text class="quick-label">新增关注</text>
      </view>
    </view>

    <!-- 间隔 -->
    <view class="divider"></view>

    <!-- 主滚动区域 -->
    <scroll-view
      class="scroll-view"
      scroll-y
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 骨架屏 -->
      <template v-if="loading && !loadedOnce">
        <view v-for="n in 4" :key="n" class="skeleton-item">
          <view class="skeleton-avatar" />
          <view class="skeleton-lines">
            <view class="skeleton-line w-50" />
            <view class="skeleton-line w-70" />
          </view>
        </view>
      </template>

      <!-- 网络异常 -->
      <view v-if="error && !loadedOnce" class="state-box">
        <svg width="100" height="100" viewBox="0 0 100 100" fill="none">
          <circle cx="50" cy="45" r="28" stroke="#D9D9D9" stroke-width="2" fill="none"/>
          <line x1="37" y1="37" x2="63" y2="63" stroke="#D9D9D9" stroke-width="2" stroke-linecap="round"/>
          <line x1="63" y1="37" x2="37" y2="63" stroke="#D9D9D9" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <text class="state-text">网络开小差了</text>
        <view class="state-btn" @click="refreshAll(true)">点击重试</view>
      </view>

      <!-- 数据列表 -->
      <template v-if="loadedOnce && !error">
        <!-- ====== 分区一：聊天消息 ====== -->
        <template v-if="chatItems.length > 0">
          <view class="section-header">
            <view class="section-header__bar"></view>
            <text class="section-header__title">💬 聊天消息</text>
          </view>
          <view class="card-list">
            <view
              v-for="item in chatItems"
              :key="'chat-' + item.id"
              class="swipe-wrap"
            >
              <view
                class="card card--chat"
                :class="{ 'card--unread': item.raw.unread > 0 }"
                :style="{ transform: `translateX(${item.raw.offsetX || 0}px)` }"
                @touchstart="onTouchStart(item.raw, $event)"
                @touchmove="onTouchMove(item.raw, $event)"
                @touchend="onTouchEnd(item.raw)"
                @longpress="onLongpress(item.raw)"
                @click="openSession(item.raw)"
              >
                <view class="chat-avatar-wrap">
                  <image
                    v-if="item.raw.productId && item.raw.productImage"
                    class="chat-avatar-img"
                    :src="item.raw.productImage"
                    mode="aspectFill"
                  />
                  <image
                    v-else
                    class="chat-avatar-img"
                    :src="item.raw.avatarUrl || '/static/pic/default-avatar.png'"
                    mode="aspectFill"
                  />
                </view>
                <view class="chat-info">
                  <view class="chat-top">
                    <text class="chat-name">{{ item.raw.nickName }}</text>
                    <text v-if="item.raw.productTitle" class="chat-product">· {{ item.raw.productTitle }}</text>
                  </view>
                  <view class="chat-bottom">
                    <text class="chat-msg">{{ item.raw.lastMessage || '暂无消息' }}</text>
                  </view>
                </view>
                <view class="chat-side">
                  <text class="chat-time">{{ formatListTimeByTimestamp(item.timestamp) }}</text>
                  <text v-if="item.raw.unread > 0" class="chat-badge">{{ item.raw.unread > 99 ? '99+' : item.raw.unread }}</text>
                </view>
              </view>
              <!-- 左滑操作 -->
              <view class="swipe-actions">
                <view class="swipe-btn swipe-btn--read" @click.stop="markRead(item.raw)">标为已读</view>
                <view class="swipe-btn swipe-btn--delete" @click.stop="confirmDelete(item.raw)">删除</view>
              </view>
            </view>
          </view>
        </template>

        <!-- 分区间隔 -->
        <view v-if="chatItems.length > 0 && (systemItems.length > 0 || interactItems.length > 0)" class="section-gap"></view>

        <!-- ====== 分区二：系统通知 ====== -->
        <template v-if="systemItems.length > 0">
          <view class="section-header">
            <view class="section-header__bar"></view>
            <text class="section-header__title">📢 系统通知</text>
          </view>
          <view class="card-list">
            <view
              v-for="item in systemItems"
              :key="'sys-' + item.id"
              class="swipe-wrap"
            >
              <view
                class="card card--system"
                :class="{ 'card--unread': !item.raw.isRead }"
                @click="onSystemClick(item.raw)"
              >
                <view class="sys-icon" :class="sysIconClass(item.raw.type)">
                  <text class="sys-icon__text">{{ sysIconText(item.raw.type) }}</text>
                </view>
                <view class="sys-info">
                  <view class="sys-top">
                    <text class="sys-title" :class="{ bold: !item.raw.isRead }">{{ item.title }}</text>
                    <text class="sys-time">{{ formatListTimeByTimestamp(item.timestamp) }}</text>
                  </view>
                  <text class="sys-desc">{{ item.content }}</text>
                </view>
                <view v-if="!item.raw.isRead" class="sys-dot"></view>
              </view>
            </view>
          </view>
        </template>

        <!-- 分区间隔 -->
        <view v-if="systemItems.length > 0 && interactItems.length > 0" class="section-gap"></view>

        <!-- ====== 分区三：互动通知 ====== -->
        <template v-if="interactItems.length > 0">
          <view class="section-header">
            <view class="section-header__bar"></view>
            <text class="section-header__title">👥 互动通知</text>
          </view>
          <view class="card-list">
            <view
              v-for="item in interactItems"
              :key="'int-' + item.id"
              class="swipe-wrap"
            >
              <view
                class="card card--interact"
                :class="{ 'card--unread': !item.raw.isRead }"
                @click="onInteractClick(item.raw)"
              >
                <image
                  class="interact-avatar"
                  :src="item.raw.fromAvatarUrl || '/static/pic/default-avatar.png'"
                  mode="aspectFill"
                />
                <view class="interact-info">
                  <view class="interact-top">
                    <text class="interact-name">{{ item.raw.fromNickName || item.title }}</text>
                    <text class="interact-action">{{ getInteractAction(item.raw) }}</text>
                    <text class="interact-time">{{ formatListTimeByTimestamp(item.timestamp) }}</text>
                  </view>
                  <text v-if="item.content" class="interact-hint">{{ item.content }}</text>
                </view>
                <view v-if="!item.raw.isRead" class="interact-dot"></view>
              </view>
            </view>
          </view>
        </template>

        <!-- 完全空状态 -->
        <view
          v-if="chatItems.length === 0 && systemItems.length === 0 && interactItems.length === 0"
          class="state-box"
        >
          <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
            <rect x="25" y="30" width="70" height="50" rx="6" stroke="#D9D9D9" stroke-width="2" fill="none"/>
            <polyline points="25,42 60,62 95,42" stroke="#D9D9D9" stroke-width="2" fill="none"/>
            <circle cx="32" cy="70" r="4" fill="#D9D9D9"/>
            <circle cx="60" cy="70" r="4" fill="#D9D9D9"/>
            <circle cx="88" cy="70" r="4" fill="#D9D9D9"/>
          </svg>
          <text class="state-text state-text--main">暂无消息</text>
          <text class="state-text state-text--sub">去看看首页有什么好货吧</text>
          <view class="state-btn" @click="goHome">去首页看看</view>
        </view>

        <!-- 加载更多 -->
        <view v-if="loadedOnce && totalCount > 0" class="load-more">
          <text v-if="notificationLoadingMore" class="load-more__text">加载中⋯</text>
          <text v-else-if="!notificationHasMore" class="load-more__text">— 以上是全部消息 —</text>
        </view>
      </template>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { useUserStore } from '@/store'

const userStore = useUserStore()

// ====== 导航栏 ======
const statusBarHeight = ref(0)
const navBarHeight = ref(44)

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

// ====== 数据状态 ======
const sessions = ref([])
const notifications = ref([])
const loading = ref(false)
const notificationLoading = ref(false)
const notificationLoadingMore = ref(false)
const notificationPage = ref(1)
const notificationHasMore = ref(true)
const isRefreshing = ref(false)
const loadedOnce = ref(false)
const error = ref(false)

const unreadMessageCount = ref(0)
const unreadFavoriteCount = ref(0)
const unreadFollowCount = ref(0)
const pageSize = 20

// 本地存储的状态
const deletedIds = ref([])
const blockedIds = ref([])
const starredIds = ref([])
const pinnedIds = ref([])

// 触摸滑动状态
const activeId = ref(null)
const isDragging = ref(false)
const touchInfo = ref({ startX: 0, startY: 0, startOffset: 0 })
const maxSwipe = 160
const threshold = 80
const dragThreshold = 8

// ====== 类型常量 ======
const favoriteTypes = [5, 6]
const followTypes = [11]
const systemTypes = [1, 3, 4, 7, 8, 9, 10]

// ====== 未读总数 ======
const totalUnread = computed(() => {
  let n = 0
  sessions.value.forEach(s => { n += s.unread || 0 })
  notifications.value.forEach(s => { if (!s.isRead) n++ })
  return n
})

// ====== 分区计算 ======
const chatItems = computed(() =>
  sessions.value.map(s => ({
    key: `chat-${s.id}`,
    source: 'chat',
    id: s.id,
    timestamp: parseTime(s.lastTime),
    raw: s
  })).sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0))
)

const systemItems = computed(() =>
  notifications.value
    .filter(n => systemTypes.includes(Number(n.type)))
    .map(n => ({
      key: `sys-${n.id}`,
      source: 'system',
      id: n.id,
      title: n.title || '系统通知',
      content: n.content || '',
      timestamp: parseTime(n.createTime),
      raw: n
    }))
    .sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0))
)

const interactItems = computed(() =>
  notifications.value
    .filter(n => [...favoriteTypes, ...followTypes].includes(Number(n.type)))
    .map(n => ({
      key: `int-${n.id}`,
      source: 'interact',
      id: n.id,
      title: n.title || '',
      content: n.content || '',
      timestamp: parseTime(n.createTime),
      raw: n
    }))
    .sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0))
)

const totalCount = computed(() =>
  chatItems.value.length + systemItems.value.length + interactItems.value.length
)

// ====== 系统通知图标 ======
function sysIconClass(type) {
  const map = {
    1: 'sys-icon--reject',
    3: 'sys-icon--reject',
    4: 'sys-icon--warn',
    7: 'sys-icon--success',
    8: 'sys-icon--success',
    9: 'sys-icon--primary',
    10: 'sys-icon--primary',
  }
  return map[type] || 'sys-icon--primary'
}
function sysIconText(type) {
  const map = {
    1: '✕', 3: '✕', 4: '📢',
    7: '✓', 8: '✓',
    9: '🛡', 10: '🛡',
  }
  return map[type] || '📢'
}

// ====== 互动行为 ======
function getInteractAction(item) {
  const t = Number(item.type)
  if (favoriteTypes.includes(t)) return '收藏了你的商品'
  if (followTypes.includes(t)) return '关注了你'
  return item.title || ''
}

// ====== 工具函数 ======
function parseTime(value) {
  if (!value) return 0
  if (typeof value === 'number') return value
  const normalized = String(value).replace('T', ' ').replace(/-/g, '/')
  const date = new Date(normalized)
  return Number.isFinite(date.getTime()) ? date.getTime() : 0
}

function formatListTimeByTimestamp(timestamp) {
  if (!timestamp) return ''
  const time = new Date(timestamp)
  const now = new Date()
  const sameDay = time.getFullYear() === now.getFullYear() &&
    time.getMonth() === now.getMonth() &&
    time.getDate() === now.getDate()
  if (sameDay) {
    const h = `${time.getHours()}`.padStart(2, '0')
    const m = `${time.getMinutes()}`.padStart(2, '0')
    return `${h}:${m}`
  }
  const mm = `${time.getMonth() + 1}`.padStart(2, '0')
  const dd = `${time.getDate()}`.padStart(2, '0')
  if (time.getFullYear() === now.getFullYear()) return `${mm}-${dd}`
  return `${time.getFullYear()}-${mm}-${dd}`
}

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function ensureLoginSilent() {
  return !!userStore.isLogin
}

// ====== 导航 ======
function goBack() {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: '/pages/index/index' })
    return
  }
  uni.navigateBack()
}

function goHome() {
  uni.switchTab({ url: '/pages/index/index' })
}

function goToMessageSettings() {
  uni.navigateTo({ url: '/pages/chat/settings/settings' })
}

function goToNotifications(entry) {
  if (!ensureLoginSilent()) { uni.navigateTo({ url: '/pages/login-sub/login/login' }); return }
  if (entry === 'favorite') { uni.navigateTo({ url: '/pages/notification-sub/notification/notification?type=6' }); return }
  if (entry === 'follow') { uni.navigateTo({ url: '/pages/notification-sub/notification/follower' }); return }
  uni.navigateTo({ url: '/pages/notification-sub/notification/notification' })
}

function goToReplyInbox() {
  if (!ensureLoginSilent()) { uni.navigateTo({ url: '/pages/login-sub/login/login' }); return }
  uni.navigateTo({ url: '/pages/notification-sub/notification/received-replies' })
}

function goToReceivedFavorites() {
  if (!ensureLoginSilent()) { uni.navigateTo({ url: '/pages/login-sub/login/login' }); return }
  uni.navigateTo({ url: '/pages/notification-sub/notification/received-favorites' })
}

function showMoreMenu() {
  uni.showActionSheet({
    itemList: ['通讯录', '消息通知设置'],
    success: (res) => {
      if (!res) return
      if (res.tapIndex === 0) showToast('通讯录开发中')
      else if (res.tapIndex === 1) showToast('消息通知设置开发中')
    }
  })
}

// ====== 点击事件 ======
function openSession(item) {
  if (item.offsetX) { item.offsetX = 0; return }
  if (item.unread) item.unread = 0
  const params = [`sessionKey=${item.sessionKey}`, `peerId=${item.userId}`, item.productId ? `productId=${item.productId}` : ''].filter(Boolean).join('&')
  uni.navigateTo({ url: `/pages/chat/detail/detail?${params}` })
}

function onSystemClick(raw) {
  if (!raw) return
  markNotifRead(raw)
  const rt = Number(raw.relatedType || 0)
  const rid = raw.relatedId
  if ((rt === 1 || rt === 2) && rid) {
    uni.navigateTo({ url: `/pages/product/detail/detail?id=${rid}` })
  } else if (rt === 3) {
    uni.navigateTo({ url: '/pages/auth-sub/auth/auth' })
  } else if (rt === 4) {
    uni.showModal({ title: raw.title || '公告', content: raw.content || '', showCancel: false })
  } else {
    uni.navigateTo({ url: '/pages/notification-sub/notification/notification' })
  }
}

function onInteractClick(raw) {
  if (!raw) return
  markNotifRead(raw)
  const t = Number(raw.type)
  if (followTypes.includes(t)) {
    uni.navigateTo({ url: '/pages/notification-sub/notification/follower' })
  } else if (raw.relatedId) {
    uni.navigateTo({ url: `/pages/product/detail/detail?id=${raw.relatedId}` })
  }
}

async function markNotifRead(item) {
  if (!item || item.isRead) return
  try {
    await post('/mini/notification/read', { id: item.id }, { showLoading: false })
    item.isRead = true
  } catch (_) {}
}

// ====== 触摸滑动 ======
function onTouchStart(item, event) {
  const touch = event.touches && event.touches[0]
  if (!touch) return
  activeId.value = item.id
  isDragging.value = false
  touchInfo.value = { startX: touch.clientX, startY: touch.clientY, startOffset: item.offsetX || 0 }
  sessions.value.forEach(s => { if (s.id !== item.id && s.offsetX) s.offsetX = 0 })
}

function onTouchMove(item, event) {
  const touch = event.touches && event.touches[0]
  if (!touch || activeId.value !== item.id) return
  const dx = touch.clientX - touchInfo.value.startX
  const dy = touch.clientY - touchInfo.value.startY
  if (Math.abs(dx) < dragThreshold && Math.abs(dy) < dragThreshold) return
  if (Math.abs(dx) < Math.abs(dy)) return
  isDragging.value = true
  item.offsetX = clamp(touchInfo.value.startOffset + dx, -maxSwipe, 0)
}

function onTouchEnd(item) {
  if (activeId.value !== item.id) return
  if (item.offsetX <= -threshold) item.offsetX = -maxSwipe
  else item.offsetX = 0
  isDragging.value = false
}

function clamp(val, min, max) {
  return Math.min(max, Math.max(min, val))
}

// ====== 长按菜单 ======
function onLongpress(item) {
  if (isDragging.value) return
  item.offsetX = 0
  uni.showActionSheet({
    itemList: ['标为已读', '删除'],
    success: (res) => {
      if (!res) return
      if (res.tapIndex === 0) markRead(item)
      else if (res.tapIndex === 1) confirmDelete(item)
    }
  })
}

// ====== 操作函数 ======
function markRead(item) {
  item.unread = 0
  item.offsetX = 0
  showToast('已标为已读')
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
    sessions.value = sessions.value.filter(s => s.id !== item.id)
    showToast('已删除')
  } catch (_) {
    showToast('删除失败，请重试')
  }
}

function showConfirm(content) {
  return new Promise(resolve => {
    uni.showModal({
      title: '提示', content,
      confirmText: '确定', cancelText: '取消',
      success: res => resolve(res && res.confirm)
    })
  })
}

function getIds(key) {
  const list = uni.getStorageSync(key)
  return Array.isArray(list) ? list : []
}
function saveIds(key, list) {
  uni.setStorageSync(key, list)
}

// ====== 数据加载 ======
async function loadSessions() {
  if (!ensureLoginSilent()) { sessions.value = []; return }
  try {
    const data = await get('/mini/chat/list', {}, { showLoading: false })
    const list = (data && data.records) || []
    deletedIds.value = getIds('chat_deleted_ids')
    blockedIds.value = getIds('chat_blocked_ids')
    starredIds.value = getIds('chat_starred_ids')
    pinnedIds.value = getIds('chat_pinned_ids')
    const selfId = (userStore.userInfo && userStore.userInfo.id) || 0
    const filtered = list.filter(item => {
      if (!item) return false
      if (item.userId === selfId) return false
      if (blockedIds.value.includes(item.userId)) return false
      if (deletedIds.value.includes(item.userId)) return false
      return true
    })
    sessions.value = filtered.map(item => ({
      ...item,
      offsetX: 0
    }))
  } catch (_) {
    showToast('加载聊天列表失败')
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
    const data = await get('/mini/notification/list', { page: targetPage, pageSize }, { showLoading: refresh && targetPage === 1 })
    const records = Array.isArray(data) ? data : (data.records || [])
    const total = Array.isArray(data) ? records.length : (data.total ?? records.length)
    if (refresh) notifications.value = records
    else notifications.value = notifications.value.concat(records)
    notificationPage.value = targetPage
    notificationHasMore.value = records.length >= pageSize && notifications.value.length < total
    recalcBadges()
  } catch (_) {
    showToast('加载通知失败')
  } finally {
    notificationLoading.value = false
    notificationLoadingMore.value = false
    if (refresh) { isRefreshing.value = false; uni.stopPullDownRefresh() }
  }
}

async function loadUnreadCount() {
  if (!ensureLoginSilent()) { unreadMessageCount.value = 0; return }
  try {
    const data = await get('/mini/product/comment/unread-reply-count', {}, { showLoading: false })
    unreadMessageCount.value = Number(data || 0)
  } catch (_) { unreadMessageCount.value = 0 }
}

function recalcBadges() {
  const list = notifications.value || []
  unreadFavoriteCount.value = list.filter(n => Number(n.isRead) !== 1 && favoriteTypes.includes(Number(n.type))).length
  unreadFollowCount.value = list.filter(n => Number(n.isRead) !== 1 && followTypes.includes(Number(n.type))).length
}

async function refreshAll(showLoading = false) {
  if (!ensureLoginSilent()) {
    sessions.value = []; notifications.value = []
    unreadMessageCount.value = 0; unreadFavoriteCount.value = 0; unreadFollowCount.value = 0
    return
  }
  error.value = false
  if (showLoading) uni.showLoading({ title: '加载中', mask: true })
  try {
    await Promise.all([loadUnreadCount(), loadSessions(), loadNotifications(1, true)])
    loadedOnce.value = true
  } catch (_) {
    error.value = true
  } finally {
    loading.value = false
    if (showLoading) uni.hideLoading()
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
  if (totalUnread.value === 0) return
  const confirm = await showConfirm('确认将所有消息标为已读？')
  if (!confirm) return
  try {
    await post('/mini/product/comment/mark-read', {}, { showLoading: false })
    await post('/mini/notification/read-all', {}, { showLoading: true })
    const tasks = sessions.value.filter(s => s && s.unread && s.sessionKey).map(s =>
      post('/mini/chat/read', { sessionKey: s.sessionKey }, { showLoading: false })
    )
    await Promise.allSettled(tasks)
    sessions.value.forEach(s => { if (s) s.unread = 0 })
    await refreshAll(false)
    showToast('已全部标为已读')
  } catch (_) {
    showToast('操作失败，请稍后重试')
  }
}

// ====== 生命周期 ======
onShow(() => { refreshAll(false) })
onPullDownRefresh(() => { notificationHasMore.value = true; refreshAll(false) })
onReachBottom(() => { loadMore() })
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* ====== 导航栏 ====== */
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  background-color: #fff;
}
.nav__left {
  width: 140rpx;
  display: flex;
  align-items: center;
}
.nav__back {
  width: 44rpx;
  height: 44rpx;
}
.nav__title {
  font-size: 34rpx;
  color: #333;
  font-weight: 600;
}
.nav__right {
  width: 280rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16rpx;
}
.nav__read-all {
  font-size: 26rpx;
  color: #1890ff;
  font-weight: 500;
  flex-shrink: 0;
}
.nav__read-all.disabled {
  color: #ccc;
}
.nav__gear {
  font-size: 36rpx;
  flex-shrink: 0;
}
.nav__more {
  width: 56rpx;
  height: 56rpx;
  border-radius: 18rpx;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.nav__more-img {
  width: 34rpx;
  height: 34rpx;
}

/* ====== 快捷入口 ====== */
.quick-entry {
  display: flex;
  padding: 32rpx 32rpx;
  background: #fff;
  justify-content: space-around;
}
.quick-entry__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  flex: 1;
}
.quick-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.quick-icon--blue { background: #ebf5ff; }
.quick-icon--red { background: #fff1f0; }
.quick-icon--green { background: #f0fff0; }
.quick-icon__inner { font-size: 40rpx; }
.quick-badge {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  padding: 0 6rpx;
  border-radius: 16rpx;
  background: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  font-weight: 600;
  text-align: center;
}
.quick-label {
  font-size: 24rpx;
  color: #666;
}

/* ====== 间隔 ====== */
.divider { height: 16rpx; background: #f5f5f5; }
.section-gap { height: 16rpx; background: #f5f5f5; }

/* ====== 分区标题 ====== */
.section-header {
  display: flex;
  align-items: center;
  height: 80rpx;
  padding-left: 32rpx;
  gap: 12rpx;
}
.section-header__bar {
  width: 6rpx;
  height: 28rpx;
  border-radius: 3rpx;
  background: #1890ff;
  flex-shrink: 0;
}
.section-header__title {
  font-size: 26rpx;
  color: #333;
  font-weight: 600;
}

/* ====== 卡片列表 ====== */
.card-list { padding: 0 32rpx; }
.swipe-wrap {
  position: relative;
  overflow: hidden;
  margin-bottom: 16rpx;
  border-radius: 24rpx;
}

/* ====== 卡片 ====== */
.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 1rpx 8rpx rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 24rpx;
  position: relative;
  z-index: 2;
  transition: transform 0.25s ease;
}
.card--unread {
  border-left: 6rpx solid #1890ff;
  border-top-left-radius: 24rpx;
  border-bottom-left-radius: 24rpx;
}

/* ====== 左滑按钮 ====== */
.swipe-actions {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  z-index: 1;
}
.swipe-btn {
  width: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #fff;
  font-weight: 500;
}
.swipe-btn--read { background: #bfbfbf; }
.swipe-btn--delete { background: #ff4d4f; }

/* ====== 聊天消息卡片 ====== */
.chat-avatar-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f0f0;
}
.chat-avatar-img {
  width: 100%;
  height: 100%;
}
.chat-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.chat-top {
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.chat-name {
  font-size: 30rpx;
  color: #333;
  font-weight: 600;
}
.chat-product {
  font-size: 26rpx;
  color: #1890ff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200rpx;
}
.chat-bottom { display: flex; align-items: center; }
.chat-msg {
  font-size: 28rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
.chat-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
  flex-shrink: 0;
}
.chat-time {
  font-size: 24rpx;
  color: #ccc;
  white-space: nowrap;
}
.chat-badge {
  min-width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  padding: 0 12rpx;
  border-radius: 18rpx;
  background: #ff4d4f;
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
  text-align: center;
}

/* ====== 系统通知卡片 ====== */
.sys-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.sys-icon--reject { background: #fff1f0; }
.sys-icon--success { background: #f0fff0; }
.sys-icon--primary { background: #ebf5ff; }
.sys-icon--warn { background: #fff7e6; }
.sys-icon__text { font-size: 36rpx; }
.sys-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.sys-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}
.sys-title {
  font-size: 30rpx;
  color: #333;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sys-title.bold { font-weight: 600; }
.sys-time {
  font-size: 24rpx;
  color: #ccc;
  white-space: nowrap;
  flex-shrink: 0;
}
.sys-desc {
  font-size: 26rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sys-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #ff4d4f;
  flex-shrink: 0;
}

/* ====== 互动通知卡片 ====== */
.interact-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: #f0f0f0;
}
.interact-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.interact-top {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
}
.interact-name {
  font-size: 28rpx;
  color: #1890ff;
  font-weight: 600;
}
.interact-action {
  font-size: 28rpx;
  color: #333;
}
.interact-time {
  font-size: 24rpx;
  color: #ccc;
  margin-left: auto;
  white-space: nowrap;
  flex-shrink: 0;
}
.interact-hint {
  font-size: 24rpx;
  color: #ccc;
}
.interact-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #ff4d4f;
  flex-shrink: 0;
}

/* ====== 骨架屏 ====== */
.skeleton-item {
  display: flex;
  gap: 24rpx;
  padding: 32rpx;
  margin: 12rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  align-items: center;
}
.skeleton-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  background: #e8e8e8;
  flex-shrink: 0;
  animation: skeleton 1.5s ease-in-out infinite;
}
.skeleton-lines {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.skeleton-line {
  height: 28rpx;
  background: #e8e8e8;
  border-radius: 14rpx;
  animation: skeleton 1.5s ease-in-out infinite;
}
.skeleton-line.w-50 { width: 50%; }
.skeleton-line.w-70 { width: 70%; }
@keyframes skeleton {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 0.8; }
}

/* ====== 状态 ====== */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 32rpx 80rpx;
}
.state-text {
  font-size: 32rpx;
  color: #999;
  margin-bottom: 8rpx;
}
.state-text--main { margin-top: 24rpx; }
.state-text--sub {
  font-size: 28rpx;
  color: #ccc;
  margin-bottom: 32rpx;
}
.state-btn {
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 48rpx;
  border-radius: 36rpx;
  border: 2rpx solid #1890ff;
  color: #1890ff;
  font-size: 28rpx;
}

/* ====== 加载更多 ====== */
.load-more {
  padding: 48rpx 0;
  text-align: center;
}
.load-more__text {
  font-size: 26rpx;
  color: #ccc;
}
</style>
