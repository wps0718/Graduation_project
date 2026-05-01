<template>
  <view class="chat-detail">
    <!-- ====== 固定顶部区域 ====== -->
    <view class="chat-header">
      <view :style="{ height: `${statusBarHeight}px` }"></view>
      <view class="chat-nav" :style="{ height: `${navBarHeight}px` }">
        <view class="chat-nav__left" @click="goBack">
          <text class="chat-nav__back">‹</text>
        </view>
        <view
          class="chat-nav__center"
          :style="{ paddingRight: `${navRightGap}px`, '--nav-right-gap': `${navRightGap}px` }"
        >
          <view
            class="chat-nav__capsule"
            @click="goPeerProfile"
            @longpress="openMore"
            @longtap="openMore"
          >
            <UserAvatar
              :avatar-url="peer.avatarUrl"
              :nick-name="peer.nickName"
              :auth-status="peer.authStatus"
              size="sm"
            />
            <view class="chat-nav__info">
              <text class="chat-nav__name">{{ peer.nickName || '对方' }}</text>
              <text class="chat-nav__status">
                <text class="status-dot" :class="peerOnline ? 'status-dot--on' : ''"></text>
                {{ peerStatusText }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 商品信息栏 -->
      <view v-if="product" class="chat-product" @click="goProductDetail">
        <image class="chat-product__image" :src="product.coverImage" mode="aspectFill" />
        <view class="chat-product__info">
          <text class="chat-product__title">{{ product.title }}</text>
          <view class="chat-product__meta">
            <Price :price="product.price" />
            <text v-if="product.conditionText" class="chat-product__condition">{{ product.conditionText }}</text>
            <text v-if="product.statusText" class="chat-product__status">{{ product.statusText }}</text>
          </view>
        </view>
        <view class="chat-product__arrow">
          <text class="chat-product__arrow-text">查看 ▶</text>
        </view>
      </view>

      <view v-else-if="peerProfile" class="chat-user-card" @click="goPeerProfile">
        <view class="chat-user-card__header">
          <text class="chat-user-card__title">正在与「{{ peerProfile.nickName }}」对话</text>
        </view>
        <view class="chat-user-card__content">
          <UserAvatar
            :avatar-url="peerProfile.avatarUrl"
            :nick-name="peerProfile.nickName"
            :auth-status="peerProfile.authStatus"
            size="md"
          />
          <view class="chat-user-card__info">
            <text class="chat-user-card__name">{{ peerProfile.nickName }}</text>
            <view class="chat-user-card__meta">
              <text class="chat-user-card__score">★{{ peerProfile.score || '0.0' }}</text>
              <StatusTag type="auth" :value="peerProfile.authStatus || 0" />
              <text class="chat-user-card__onsale">在售{{ peerProfile.onSaleCount || 0 }}件</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- ====== 消息列表（独立滚动） ====== -->
    <scroll-view
      class="chat-scroll"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
      :style="{ height: `${scrollHeight}px` }"
    >
      <view
        v-for="item in displayMessages"
        :key="item.id"
        :id="`msg-${item.id}`"
        class="chat-message"
        :class="{
          'is-compact': item.compact,
          'is-card': item.type === 'product-card'
        }"
      >
        <view v-if="item.showTime" class="chat-time">
          <text class="chat-time__text">{{ item.showTime }}</text>
        </view>
        <view v-if="item.type === 'system'" class="chat-system">
          <text class="chat-system__text">{{ item.content }}</text>
        </view>
        <view v-else class="chat-bubble" :class="{ 'is-self': item.isSelf }">
          <view v-if="!item.isSelf" class="chat-bubble__avatar" @click="goPeerProfile">
            <UserAvatar
              :avatar-url="peer.avatarUrl"
              :nick-name="peer.nickName"
              :auth-status="peer.authStatus"
              size="sm"
            />
          </view>

          <view v-if="item.type === 'product-card'" class="chat-bubble__content" @click="goProductDetail">
            <view class="chat-bubble__card">
              <image class="chat-bubble__card-image" :src="item.productImage" mode="aspectFill" />
              <view class="chat-bubble__card-info">
                <text class="chat-bubble__card-title">{{ item.productTitle }}</text>
                <view class="chat-bubble__card-price-row">
                  <text class="chat-bubble__card-price">¥{{ item.productPrice }}</text>
                  <text v-if="item.productCondition" class="chat-bubble__card-tag">{{ item.productCondition }}</text>
                </view>
              </view>
            </view>
            <view v-if="item.isSelf && item.showReadStatus" class="chat-bubble__read">
              <text class="chat-bubble__read-text" :class="{ 'is-read': item.isRead }">
                {{ item.isRead ? '✓✓' : '✓' }}
              </text>
            </view>
          </view>

          <view v-else class="chat-bubble__content" @longpress="onMessageLongpress(item)" @longtap="onMessageLongpress(item)">
            <text class="chat-bubble__text">{{ item.content }}</text>
            <view v-if="item.isSelf && item.showReadStatus" class="chat-bubble__read">
              <text class="chat-bubble__read-text" :class="{ 'is-read': item.isRead }">
                {{ item.isRead ? '✓✓' : '✓' }}
              </text>
            </view>
          </view>

          <UserAvatar
            v-if="item.isSelf"
            :avatar-url="selfUser.avatarUrl"
            :nick-name="selfUser.nickName"
            size="sm"
            :show-auth="false"
          />
        </view>
      </view>
    </scroll-view>

    <!-- ====== 固定底部区域 ====== -->
    <view class="chat-footer">
      <!-- 快捷回复 -->
      <view v-if="quickVisible" class="chat-quick">
        <scroll-view class="chat-quick__scroll" scroll-x>
          <view class="chat-quick__list">
            <view
              v-for="item in quickReplies"
              :key="item"
              class="chat-quick__item"
              @click="sendQuick(item)"
            >
              <text class="chat-quick__text">{{ item }}</text>
            </view>
          </view>
        </scroll-view>
        <view class="chat-quick__toggle" @click="quickVisible = false">
          <text class="chat-quick__toggle-icon">▼</text>
        </view>
      </view>
      <view v-else class="chat-quick-collapsed" @click="quickVisible = true">
        <text class="chat-quick-collapsed__text">快捷回复</text>
        <text class="chat-quick-collapsed__icon">▲</text>
      </view>

      <!-- 输入栏 -->
      <view class="chat-input safe-area-bottom">
        <input
          class="chat-input__field"
          :value="inputValue"
          placeholder="输入消息"
          placeholder-class="chat-input__placeholder"
          confirm-type="send"
          @input="onInput"
          @confirm="onSend"
          @focus="quickVisible = false"
        />
        <view class="chat-input__send" :class="{ 'is-disabled': !canSend }" @click="onSend">
          <text class="chat-input__send-text">发送</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { QUICK_REPLIES } from '@/utils/constant'
import { useUserStore } from '@/store'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import Price from '@/components/price/price.vue'
import StatusTag from '@/components/status-tag/status-tag.vue'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const navBarHeight = ref(44)
const navRightGap = ref(0)
const scrollHeight = ref(400)

const peer = ref({})
const peerProfile = ref(null)
const product = ref(null)
const sessionKey = ref('')
const messages = ref([])
const scrollIntoView = ref('')
const inputValue = ref('')
const orderCreated = ref(false)
const pollingTimer = ref(null)
const quickVisible = ref(true)

const quickReplies = QUICK_REPLIES

const selfId = computed(() => {
  const info = userStore.userInfo
  if (info && info.id) return Number(info.id)
  try {
    const raw = uni.getStorageSync('userInfo')
    if (raw) {
      const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
      if (parsed && parsed.id) return Number(parsed.id)
    }
  } catch (_) {}
  return 0
})
const selfUser = computed(() => userStore.userInfo || {})

const peerOnline = computed(() => Number(peer.value?.onlineStatus) === 1)

const peerStatusText = computed(() => {
  if (peerOnline.value) return '在线'
  if (peer.value && peer.value.lastActiveTime) return formatLastActive(peer.value.lastActiveTime)
  if (peer.value && Number(peer.value.authStatus) === 2) return '已认证'
  return '未认证'
})

const canSend = computed(() => inputValue.value.trim().length > 0)

// 自己发送的最后一条消息 ID（用于已读状态显示）
const lastSelfMsgId = computed(() => {
  for (let i = messages.value.length - 1; i >= 0; i--) {
    if (messages.value[i].isSelf) return messages.value[i].id
  }
  return null
})

const displayMessages = computed(() => {
  const list = []
  let prev = null
  messages.value.forEach((item) => {
    const showTime = shouldShowTime(item, prev) ? formatMessageTime(item.time) : ''
    const showReadStatus = item.isSelf && item.id === lastSelfMsgId.value
    // 紧凑模式：同一发送者且无时间标签
    const compact = prev && !showTime && item.from === prev.from
    list.push({
      ...item,
      showTime,
      showReadStatus,
      compact
    })
    prev = item
  })
  return list
})

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function ensureLogin() {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login-sub/login/login' })
    return false
  }
  return true
}

// 时间显示 + 发送者变化判断
function shouldShowTime(current, prev) {
  if (!prev) return true
  if (current.from !== prev.from) return true
  return current.time - prev.time > 5 * 60 * 1000
}

function formatMessageTime(timestamp) {
  if (!timestamp) return ''
  const time = new Date(timestamp)
  const now = new Date()
  const hour = `${time.getHours()}`.padStart(2, '0')
  const minute = `${time.getMinutes()}`.padStart(2, '0')
  const hm = `${hour}:${minute}`

  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)
  const msgDay = new Date(time.getFullYear(), time.getMonth(), time.getDate())

  if (msgDay >= today) return hm
  if (msgDay >= yesterday) return `昨天 ${hm}`
  if (time.getFullYear() === now.getFullYear()) {
    return `${time.getMonth() + 1}月${time.getDate()}日 ${hm}`
  }
  return `${time.getFullYear()}年${time.getMonth() + 1}月${time.getDate()}日 ${hm}`
}

function formatLastActive(value) {
  const time = typeof value === 'number' ? value : parseActiveTime(value)
  if (!time) return '最近在线'
  const diff = Date.now() - time
  if (diff < 2 * 60 * 1000) return '刚刚在线'
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return `最近在线 ${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `最近在线 ${hours}小时`
  const days = Math.floor(hours / 24)
  return `最近在线 ${days}天`
}

function parseActiveTime(value) {
  if (!value) return 0
  if (typeof value === 'number') return value
  return new Date(String(value).replace(/-/g, '/')).getTime()
}

function goBack() {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: '/pages/chat/list/list' })
    return
  }
  uni.navigateBack()
}

function openMore() {
  uni.showActionSheet({
    itemList: ['举报用户', '分享会话'],
    success: (res) => {
      if (!res) return
      if (res.tapIndex === 0 && peer.value && peer.value.id) {
        uni.navigateTo({ url: `/pages/login-sub/report/report?targetType=2&targetId=${peer.value.id}` })
        return
      }
      if (res.tapIndex === 1) shareChat()
    }
  })
}

function shareChat() {
  if (typeof uni.showShareMenu === 'function') {
    uni.showShareMenu({ withShareTicket: true })
    showToast('请使用系统分享')
    return
  }
  showToast('当前环境不支持分享')
}

function goPeerProfile() {
  if (!peer.value || !peer.value.id) return
  if (selfId.value && peer.value.id === selfId.value) {
    uni.switchTab({ url: '/pages/user/user' })
    return
  }
  uni.navigateTo({ url: `/pages/user-sub/seller/profile?id=${peer.value.id}` })
}

function onMessageLongpress(item) {
  if (!item || item.type === 'system') return
  const isSelf = !!item.isSelf
  const actionList = isSelf ? ['复制', '删除'] : ['复制']
  uni.showActionSheet({
    itemList: actionList,
    success: (res) => {
      if (!res) return
      if (res.tapIndex === 0) {
        uni.setClipboardData({
          data: item.content || '',
          success: () => showToast('已复制')
        })
        return
      }
      if (isSelf && res.tapIndex === 1) {
        messages.value = messages.value.filter((msg) => msg.id !== item.id)
      }
    }
  })
}

function goProductDetail() {
  if (!product.value) return
  uni.navigateTo({ url: `/pages/product/detail/detail?id=${product.value.id}` })
}

async function confirmBuy() {
  if (!product.value) return
  if (orderCreated.value || product.value.hasActiveOrder) {
    showToast('已发起交易，请耐心等待')
    return
  }
  const confirm = await showConfirm(`确认购买"${product.value.title}"？`)
  if (!confirm) return
  try {
    const data = await post('/mini/order/create', { productId: product.value.id }, { showLoading: true })
    orderCreated.value = true
    const orderText = data && data.orderNo ? `订单已创建：${data.orderNo}` : '订单已创建'
    appendSystemMessage(orderText)
  } catch (error) {
    showToast('下单失败，请稍后重试')
  }
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

function onInput(event) {
  inputValue.value = event && event.detail ? event.detail.value : ''
}

function sendQuick(text) {
  inputValue.value = text
  quickVisible.value = false
  onSend()
}

async function onSend() {
  const content = inputValue.value.trim()
  if (!content) return

  try {
    const data = await post('/mini/chat/message/send', {
      sessionKey: sessionKey.value,
      type: 1,
      content
    })

    const newMessage = createMessage({
      id: data,
      from: selfId.value,
      isSelf: true,
      type: 'text',
      content,
      isRead: false
    })
    messages.value.push(newMessage)
    inputValue.value = ''
    scrollToBottom()
  } catch (error) {
    showToast('发送失败')
  }
}

function createMessage(payload) {
  return {
    id: payload.id || Date.now() + Math.floor(Math.random() * 1000),
    time: Date.now(),
    ...payload
  }
}

function appendSystemMessage(content) {
  messages.value.push({
    id: Date.now() + Math.floor(Math.random() * 1000),
    time: Date.now(),
    type: 'system',
    content
  })
  scrollToBottom()
}

async function markRead() {
  if (!sessionKey.value) return
  try {
    await post('/mini/chat/read', { sessionKey: sessionKey.value })
  } catch (error) {
    console.error('Mark read error:', error)
  }
}

function scrollToBottom() {
  nextTick(() => {
    const last = messages.value[messages.value.length - 1]
    if (last) {
      scrollIntoView.value = `msg-${last.id}`
    }
  })
}

async function fetchPeer(id) {
  if (!id) return
  try {
    const data = await get(`/mini/user/profile/${id}`, {}, { showLoading: false })
    if (data) {
      peerProfile.value = data
      peer.value = {
        id: data.id,
        nickName: data.nickName,
        avatarUrl: data.avatarUrl,
        authStatus: data.authStatus,
        onlineStatus: data.onlineStatus || 0,
        lastActiveTime: parseActiveTime(data.lastActiveTime) || Date.now() - 15 * 60 * 1000
      }
    }
  } catch (error) {
    console.error('Fetch peer error:', error)
  }
}

async function fetchProduct(id) {
  if (!id) return
  try {
    const data = await get(`/mini/product/detail/${id}`, {}, { showLoading: false })
    if (data) {
      product.value = {
        ...data,
        coverImage: data.coverImage || (data.images && data.images[0]),
        conditionText: getConditionText(data.conditionLevel),
        statusText: data.status === 1 ? '在售' : '已售'
      }
    }
  } catch (error) {
    product.value = null
  }
}

function getConditionText(level) {
  const map = {
    100: '全新',
    95: '95新',
    90: '9成新',
    80: '8成新',
    70: '7成新'
  }
  return map[level] || ''
}

async function fetchMessages() {
  if (!sessionKey.value) return
  try {
    const data = await get('/mini/chat/messages', {
      sessionKey: sessionKey.value,
      page: 1,
      pageSize: 50
    })
    if (data && data.records) {
      const list = data.records.map(m => ({
        id: m.msgId,
        from: m.senderId,
        isSelf: !!m.isSelf,
        type: m.msgType === 2 ? 'product-card' : 'text',
        content: m.content,
        time: new Date(m.createTime.replace(/-/g, '/')).getTime(),
        isRead: m.isRead,
        ...parseProductCardContent(m.content, m.msgType)
      }))
      messages.value = list.reverse()
      scrollToBottom()
    }
  } catch (error) {
    console.error('Fetch messages error:', error)
  }
}

function parseProductCardContent(content, type) {
  if (type !== 2) return {}
  try {
    const data = JSON.parse(content)
    return {
      productTitle: data.title,
      productImage: data.image,
      productPrice: data.price,
      productCondition: data.condition,
      isRead: false
    }
  } catch (e) {
    return {}
  }
}

async function sendProductCard() {
  if (!product.value || !sessionKey.value) return

  const cardData = {
    productId: product.value.id,
    title: product.value.title,
    image: product.value.coverImage,
    price: product.value.price,
    condition: product.value.conditionText
  }

  try {
    const data = await post('/mini/chat/message/send', {
      sessionKey: sessionKey.value,
      type: 2,
      content: JSON.stringify(cardData)
    })

    const newMessage = createMessage({
      id: data,
      from: selfId.value,
      isSelf: true,
      type: 'product-card',
      productTitle: cardData.title,
      productImage: cardData.image,
      productPrice: cardData.price,
      productCondition: cardData.condition,
      isRead: false
    })
    messages.value.push(newMessage)
    scrollToBottom()
  } catch (error) {
    console.error('Send product card error:', error)
  }
}

// 计算滚动区域高度
function calcScrollHeight() {
  const info = uni.getSystemInfoSync()
  const statusH = info.statusBarHeight || 0
  const navH = 44
  // 商品栏或用户卡片高度
  let topExtra = 0
  if (product.value) {
    topExtra = 110 // 商品栏大约 110rpx → 约 55px
  } else if (peerProfile.value) {
    topExtra = 120
  }
  const topPx = statusH + navH + topExtra * info.windowWidth / 750
  // 底部：快捷回复约 60px + 输入栏约 60px + 安全区域
  const bottomPx = 120 + (info.safeAreaInsets?.bottom || 0)
  scrollHeight.value = info.windowHeight - topPx - bottomPx
}

onLoad(async (options = {}) => {
  if (!ensureLogin()) return
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = info.statusBarHeight || 0
  navBarHeight.value = 44
  if (typeof uni.getMenuButtonBoundingClientRect === 'function') {
    const rect = uni.getMenuButtonBoundingClientRect()
    const gap = rect && rect.left ? info.windowWidth - rect.left + 8 : 0
    navRightGap.value = Math.max(0, gap)
  }

  sessionKey.value = options.sessionKey || ''
  const peerId = options.peerId ? Number(options.peerId) : null
  const productId = options.productId ? Number(options.productId) : null

  if (peerId) {
    await fetchPeer(peerId)
  }

  if (productId) {
    await fetchProduct(productId)
  }

  calcScrollHeight()

  if (sessionKey.value) {
    await fetchMessages()
    await markRead()
  }

  if (productId && messages.value.length === 0) {
    await sendProductCard()
  }

  pollingTimer.value = setInterval(() => {
    fetchMessages()
    markRead()
  }, 5000)
})

import { onUnload, onHide, onShow } from '@dcloudio/uni-app'

onShow(() => {
  if (sessionKey.value) {
    markRead()
  }
})

onHide(() => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
})

onUnload(() => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
})

onShareAppMessage(() => {
  const name = (peer.value && peer.value.nickName) || '对方'
  const userId = (peer.value && peer.value.id) || ''
  return {
    title: `与${name}的聊天`,
    path: `/pages/chat/detail/detail?userId=${userId}`
  }
})
</script>

<style lang="scss" scoped>
.chat-detail {
  height: 100vh;
  background-color: var(--bg-page);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ====== 固定顶部 ====== */
.chat-header {
  flex-shrink: 0;
  background-color: var(--bg-white);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.chat-nav {
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-md);
}

.chat-nav__left,
.chat-nav__right {
  width: 120rpx;
  display: flex;
  align-items: center;
}
.chat-nav__right { justify-content: flex-end; }

.chat-nav__back {
  font-size: 44rpx;
  color: var(--text-primary);
}

.chat-nav__center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.chat-nav__capsule {
  position: relative;
  left: calc((var(--nav-right-gap, 0px)) / -2);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 8rpx 18rpx 8rpx 10rpx;
  border-radius: 999rpx;
  background-color: var(--bg-grey);
  border: 1rpx solid var(--border-light);
}

.chat-nav__info { display: flex; flex-direction: column; }
.chat-nav__name {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
}
.chat-nav__status {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  margin-top: 2rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.status-dot {
  display: inline-block;
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: #ccc;
}
.status-dot--on { background-color: #52c41a; }

/* ====== 商品栏（优化后） ====== */
.chat-product {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 12rpx var(--spacing-md);
  border-bottom: 1rpx solid #e5e5e5;
}

.chat-product__image {
  width: 80rpx;
  height: 80rpx;
  border-radius: var(--radius-sm);
  background-color: var(--bg-grey);
  flex-shrink: 0;
}

.chat-product__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.chat-product__title {
  font-size: var(--font-sm);
  color: var(--text-primary);
  font-weight: 500;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.chat-product__meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.chat-product__condition {
  font-size: 20rpx;
  color: var(--text-secondary);
  background-color: var(--bg-grey);
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
}

.chat-product__status {
  font-size: 20rpx;
  color: var(--primary-color);
  background-color: var(--primary-bg);
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
}

.chat-product__arrow { padding-left: var(--spacing-sm); }
.chat-product__arrow-text {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

/* ====== 用户卡片 ====== */
.chat-user-card {
  padding: var(--spacing-sm) var(--spacing-md);
  border-bottom: 1rpx solid #e5e5e5;
}
.chat-user-card__header { margin-bottom: 8rpx; }
.chat-user-card__title {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}
.chat-user-card__content {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
.chat-user-card__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.chat-user-card__name {
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-primary);
}
.chat-user-card__meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
.chat-user-card__score {
  font-size: var(--font-sm);
  color: #ff9800;
  font-weight: 600;
}
.chat-user-card__onsale {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

/* ====== 消息滚动区 ====== */
.chat-scroll {
  padding: 0 var(--spacing-md);
  box-sizing: border-box;
}

/* ====== 消息间距 ====== */
.chat-message {
  margin-bottom: 16rpx;
}
.chat-message.is-compact {
  margin-bottom: 8rpx;
}
.chat-message.is-card {
  margin-bottom: 16rpx;
}

/* ====== 时间标签 ====== */
.chat-time {
  display: flex;
  justify-content: center;
  padding: 24rpx 0 16rpx;
}
.chat-time__text {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  background-color: var(--bg-grey);
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
}

/* ====== 系统消息 ====== */
.chat-system {
  display: flex;
  justify-content: center;
  margin-bottom: 8rpx;
}
.chat-system__text {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  background-color: var(--bg-grey);
  padding: 8rpx 24rpx;
  border-radius: 999rpx;
}

/* ====== 气泡 ====== */
.chat-bubble {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-sm);
}
.chat-bubble.is-self {
  justify-content: flex-end;
}

.chat-bubble__content {
  max-width: 480rpx;
  padding: 18rpx 24rpx;
  border-radius: 20rpx;
  background-color: var(--bg-white);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  position: relative;
}
.chat-bubble.is-self .chat-bubble__content {
  background-color: var(--primary-bg);
}

/* ====== 商品卡片消息 ====== */
.chat-bubble__card {
  width: 420rpx;
  background-color: var(--bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1rpx solid var(--border-light);
}
.chat-bubble.is-self .chat-bubble__card {
  border-color: transparent;
}
.chat-bubble__card-image {
  width: 100%;
  height: 200rpx;
  background-color: var(--bg-grey);
}
.chat-bubble__card-info {
  padding: var(--spacing-sm);
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.chat-bubble__card-title {
  font-size: var(--font-sm);
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.chat-bubble__card-price-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
.chat-bubble__card-price {
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--primary-color);
}
.chat-bubble__card-tag {
  font-size: 20rpx;
  color: var(--text-secondary);
  background-color: var(--bg-grey);
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
}

/* ====== 头像 ====== */
.chat-bubble__avatar {
  display: flex;
  align-items: center;
}

/* ====== 已读状态（仅最后一条自己的消息） ====== */
.chat-bubble__read {
  display: flex;
  justify-content: flex-end;
  margin-top: 6rpx;
}
.chat-bubble__read-text {
  font-size: 20rpx;
  color: #aaa;
  &.is-read {
    color: var(--primary-color);
  }
}

/* ====== 快捷回复 ====== */
.chat-footer {
  flex-shrink: 0;
  background-color: var(--bg-page);
}

.chat-quick {
  padding: 0 var(--spacing-md);
  display: flex;
  align-items: center;
}
.chat-quick__scroll {
  flex: 1;
  overflow: hidden;
}
.chat-quick__list {
  display: flex;
  gap: var(--spacing-sm);
  padding: 8rpx 0;
}
.chat-quick__item {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background-color: var(--bg-white);
  border: 1rpx solid var(--border-light);
  flex-shrink: 0;
}
.chat-quick__text {
  font-size: var(--font-sm);
  color: var(--text-regular);
}
.chat-quick__toggle {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.chat-quick__toggle-icon {
  font-size: 20rpx;
  color: var(--text-secondary);
}

.chat-quick-collapsed {
  padding: 8rpx var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}
.chat-quick-collapsed__text {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}
.chat-quick-collapsed__icon {
  font-size: 18rpx;
  color: var(--text-secondary);
}

/* ====== 输入栏 ====== */
.chat-input {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: var(--bg-white);
  border-top: 1rpx solid var(--border-light);
}
.chat-input__field {
  flex: 1;
  height: 72rpx;
  padding: 0 var(--spacing-md);
  border-radius: 999rpx;
  background-color: var(--bg-grey);
  font-size: var(--font-sm);
  color: var(--text-primary);
}
.chat-input__send {
  min-width: 120rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background-color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
}
.chat-input__send.is-disabled {
  opacity: 0.5;
}
.chat-input__send-text {
  font-size: var(--font-sm);
  color: var(--text-white);
  font-weight: 600;
}
</style>
