<template>
  <view class="chat-list-page">
    <view class="chat-list">
      <view v-if="displaySessions.length" class="chat-list__content">
        <view
          v-for="item in displaySessions"
          :key="item.id"
          class="chat-item-wrap"
        >
          <view
            class="chat-item"
            :class="{ 'is-dragging': isDragging && activeId === item.id }"
            :style="itemStyle(item)"
            @touchstart="onTouchStart(item, $event)"
            @touchmove="onTouchMove(item, $event)"
            @touchend="onTouchEnd(item)"
            @longpress="onLongpress(item)"
            @click="openSession(item)"
          >
            <view class="chat-item__avatar">
              <UserAvatar
                :avatar-url="item.avatarUrl"
                :nick-name="item.nickName"
                :auth-status="item.authStatus"
                size="md"
              />
            </view>
            <view class="chat-item__body">
            <view class="chat-item__row">
              <view class="chat-item__name-row">
                <text class="chat-item__name">{{ item.nickName }}</text>
                <view class="chat-item__tags">
                  <text v-if="item.isStarred" class="chat-item__tag is-star">特别关注</text>
                  <text v-if="item.isPinned" class="chat-item__tag is-pin">置顶</text>
                </view>
              </view>
              <text class="chat-item__time">{{ formatListTime(item.lastTime) }}</text>
            </view>
              <view class="chat-item__row">
                <text class="chat-item__preview">{{ item.lastMessage }}</text>
                <view v-if="item.unread" class="chat-item__badge">
                  <text class="chat-item__badge-text">{{ item.unread > 99 ? '99+' : item.unread }}</text>
                </view>
              </view>
            </view>
          </view>
          <view class="chat-item__actions" :class="{ 'is-visible': isActionsVisible(item) }">
            <view class="chat-item__action is-muted" @click.stop="markUnread(item)">
              <view class="chat-item__action-text">
                <text>设为</text>
                <text>未读</text>
              </view>
            </view>
            <view class="chat-item__action is-primary" @click.stop="markRead(item)">
              <view class="chat-item__action-text">
                <text>设为</text>
                <text>已读</text>
              </view>
            </view>
            <view class="chat-item__action is-danger" @click.stop="confirmDelete(item)">
              <text class="chat-item__action-text">删除</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="chat-list__empty">
        <EmptyState type="no-message" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { useUserStore } from '@/store'
import EmptyState from '@/components/empty-state/empty-state.vue'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'

const userStore = useUserStore()

const sessions = ref([])
const loading = ref(false)
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

const displaySessions = computed(() => sessions.value)

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function parseTime(value) {
  if (!value) return 0
  return new Date(value.replace(/-/g, '/')).getTime()
}

function formatListTime(value) {
  if (!value) return ''
  const time = new Date(value.replace(/-/g, '/'))
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
    `userId=${item.userId}`,
    `nickName=${encodeURIComponent(item.nickName || '')}`,
    `avatarUrl=${encodeURIComponent(item.avatarUrl || '')}`,
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
  if (!userStore.isLogin) {
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

onShow(() => {
  loadSessions()
})
</script>

<style lang="scss" scoped>
.chat-list-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

.chat-list {
  padding: var(--spacing-md);
}

.chat-list__content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.chat-list__empty {
  padding-top: var(--spacing-xl);
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
  margin-right: var(--spacing-md);
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
