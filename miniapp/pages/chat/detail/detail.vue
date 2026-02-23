<template>
  <view class="chat-detail">
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
            <text class="chat-nav__status">{{ peerStatusText }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="product" class="chat-product" @click="goProductDetail">
      <image class="chat-product__image" :src="product.coverImage" mode="aspectFill" />
      <view class="chat-product__info">
        <text class="chat-product__title">{{ product.title }}</text>
        <Price :price="product.price" />
      </view>
      <view class="chat-product__action" @click.stop="confirmBuy">
        <text class="chat-product__action-text">确认购买</text>
      </view>
    </view>

    <scroll-view
      class="chat-scroll"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
    >
      <view
        v-for="item in displayMessages"
        :key="item.id"
        :id="`msg-${item.id}`"
        class="chat-message"
      >
        <view v-if="item.showTime" class="chat-time">
          <text class="chat-time__text">{{ item.showTime }}</text>
        </view>
        <view v-if="item.type === 'system'" class="chat-system">
          <text class="chat-system__text">{{ item.content }}</text>
        </view>
        <view v-else class="chat-bubble" :class="{ 'is-self': item.from === selfId }">
          <view v-if="item.from !== selfId" class="chat-bubble__avatar" @click="goPeerProfile">
            <UserAvatar
              :avatar-url="peer.avatarUrl"
              :nick-name="peer.nickName"
              :auth-status="peer.authStatus"
              size="sm"
            />
          </view>
          <view class="chat-bubble__content" @longpress="onMessageLongpress(item)" @longtap="onMessageLongpress(item)">
            <text class="chat-bubble__text">{{ item.content }}</text>
          </view>
          <UserAvatar
            v-if="item.from === selfId"
            :avatar-url="selfUser.avatarUrl"
            :nick-name="selfUser.nickName"
            size="sm"
            :show-auth="false"
          />
        </view>
      </view>
    </scroll-view>

    <view class="chat-quick">
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
    </view>

    <view class="chat-input safe-area-bottom">
      <input
        class="chat-input__field"
        :value="inputValue"
        placeholder="输入消息"
        placeholder-class="chat-input__placeholder"
        confirm-type="send"
        @input="onInput"
        @confirm="onSend"
      />
      <view class="chat-input__send" :class="{ 'is-disabled': !canSend }" @click="onSend">
        <text class="chat-input__send-text">发送</text>
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

const userStore = useUserStore()

const statusBarHeight = ref(0)
const navBarHeight = ref(44)
const navRightGap = ref(0)

const peer = ref({})
const product = ref(null)
const messages = ref([])
const scrollIntoView = ref('')
const inputValue = ref('')
const orderCreated = ref(false)

const quickReplies = QUICK_REPLIES

const selfId = computed(() => (userStore.userInfo && userStore.userInfo.id) || 0)
const selfUser = computed(() => userStore.userInfo || {})

const peerStatusText = computed(() => {
  if (peer.value && Number(peer.value.onlineStatus) === 1) return '在线'
  if (peer.value && peer.value.lastActiveTime) return formatLastActive(peer.value.lastActiveTime)
  if (peer.value && Number(peer.value.authStatus) === 2) return '已认证'
  return '未认证'
})

const canSend = computed(() => inputValue.value.trim().length > 0)

const displayMessages = computed(() => {
  const list = []
  let lastTime = 0
  messages.value.forEach((item) => {
    const showTime = shouldShowTime(lastTime, item.time) ? formatMessageTime(item.time) : ''
    if (item.time) lastTime = item.time
    list.push({
      ...item,
      showTime
    })
  })
  return list
})

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

function shouldShowTime(prev, current) {
  if (!prev) return true
  return current - prev > 5 * 60 * 1000
}

function formatMessageTime(timestamp) {
  if (!timestamp) return ''
  const time = new Date(timestamp)
  const now = new Date()
  const sameDay =
    time.getFullYear() === now.getFullYear() &&
    time.getMonth() === now.getMonth() &&
    time.getDate() === now.getDate()
  const hour = `${time.getHours()}`.padStart(2, '0')
  const minute = `${time.getMinutes()}`.padStart(2, '0')
  if (sameDay) {
    return `${hour}:${minute}`
  }
  const month = `${time.getMonth() + 1}`.padStart(2, '0')
  const day = `${time.getDate()}`.padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
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
        uni.navigateTo({ url: `/pages/report/report?targetType=2&targetId=${peer.value.id}` })
        return
      }
      if (res.tapIndex === 1) {
        shareChat()
      }
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
  uni.navigateTo({ url: `/pages/seller/profile?id=${peer.value.id}` })
}

function onMessageLongpress(item) {
  if (!item || item.type === 'system') return
  const isSelf = item.from === selfId.value
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
  const confirm = await showConfirm(`确认购买“${product.value.title}”？`)
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
  onSend()
}

function onSend() {
  const content = inputValue.value.trim()
  if (!content) return
  const newMessage = createMessage({
    from: selfId.value,
    type: 'text',
    content
  })
  messages.value.push(newMessage)
  inputValue.value = ''
  scrollToBottom()
  simulateReply()
}

function createMessage(payload) {
  const id = Date.now() + Math.floor(Math.random() * 1000)
  return {
    id,
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

function simulateReply() {
  const replyList = [
    '好的，我们约个时间面交。',
    '可以的，我这边时间比较灵活。',
    '如果需要我可以带上发票。',
    '校内哪个位置方便？',
    '谢谢理解～'
  ]
  const reply = replyList[Math.floor(Math.random() * replyList.length)]
  setTimeout(() => {
    messages.value.push({
      id: Date.now() + Math.floor(Math.random() * 1000),
      time: Date.now(),
      type: 'text',
      from: peer.value.id,
      content: reply
    })
    scrollToBottom()
  }, 1200)
}

function scrollToBottom() {
  nextTick(() => {
    const last = messages.value[messages.value.length - 1]
    if (last) {
      scrollIntoView.value = `msg-${last.id}`
    }
  })
}

function buildMockMessages(peerId) {
  const now = Date.now()
  return [
    {
      id: 1,
      time: now - 36 * 60 * 1000,
      type: 'text',
      from: peerId,
      content: '你好，我对这个商品还挺感兴趣的。'
    },
    {
      id: 2,
      time: now - 34 * 60 * 1000,
      type: 'text',
      from: selfId.value,
      content: '你好，可以聊聊具体需求吗？'
    },
    {
      id: 3,
      time: now - 30 * 60 * 1000,
      type: 'text',
      from: peerId,
      content: '我今晚在南海北，能当面看看吗？'
    },
    {
      id: 4,
      time: now - 10 * 60 * 1000,
      type: 'text',
      from: selfId.value,
      content: '可以，给你留着。'
    }
  ]
}

async function fetchPeer(id) {
  if (!id) {
    peer.value = { nickName: '对方' }
    return
  }
  try {
    const data = await get(`/mini/user/profile/${id}`, {}, { showLoading: false })
    if (data) {
      peer.value = {
        id: data.id || id,
        nickName: data.nickName,
        avatarUrl: data.avatarUrl,
      authStatus: data.authStatus,
      onlineStatus: data.onlineStatus || 0,
      lastActiveTime: parseActiveTime(data.lastActiveTime) || Date.now() - 15 * 60 * 1000
      }
      return
    }
  } catch (error) {
    peer.value = { id, nickName: '对方', onlineStatus: 0, lastActiveTime: Date.now() - 30 * 60 * 1000 }
  }
}

async function fetchProduct(id) {
  if (!id) return
  try {
    const data = await get(`/mini/product/detail/${id}`, {}, { showLoading: false })
    if (data) {
      product.value = data
    }
  } catch (error) {
    product.value = null
  }
}

onLoad((options = {}) => {
  if (!ensureLogin()) return
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = info.statusBarHeight || 0
  navBarHeight.value = 44
  if (typeof uni.getMenuButtonBoundingClientRect === 'function') {
    const rect = uni.getMenuButtonBoundingClientRect()
    const gap = rect && rect.left ? info.windowWidth - rect.left + 8 : 0
    navRightGap.value = Math.max(0, gap)
  }

  const peerId = options.userId ? Number(options.userId) : null
  const productId = options.productId ? Number(options.productId) : null
  if (options.nickName || options.avatarUrl) {
    peer.value = {
      id: peerId,
      nickName: decodeURIComponent(options.nickName || '') || '对方',
      avatarUrl: decodeURIComponent(options.avatarUrl || ''),
      authStatus: 0
    }
  }
  fetchPeer(peerId)
  fetchProduct(productId)
  messages.value = buildMockMessages(peerId || 0)
  scrollToBottom()
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
  min-height: 100vh;
  background-color: var(--bg-page);
  display: flex;
  flex-direction: column;
}

.chat-nav {
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-md);
  background-color: var(--bg-white);
  box-shadow: 0 6rpx 20rpx rgba(15, 23, 42, 0.05);
}

.chat-nav__left,
.chat-nav__right {
  width: 120rpx;
  display: flex;
  align-items: center;
}

.chat-nav__right {
  justify-content: flex-end;
}

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
}

.chat-nav__capsule {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 8rpx 18rpx 8rpx 10rpx;
  border-radius: 999rpx;
  background-color: var(--bg-grey);
  border: 1rpx solid var(--border-light);
}

.chat-nav__info {
  display: flex;
  flex-direction: column;
}

.chat-nav__name {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
}

.chat-nav__status {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  margin-top: 2rpx;
}

.chat-nav__more {
  font-size: 40rpx;
  color: var(--text-primary);
}

.chat-product {
  margin: var(--spacing-md);
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.chat-product__image {
  width: 120rpx;
  height: 120rpx;
  border-radius: var(--radius-md);
  background-color: var(--bg-grey);
}

.chat-product__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.chat-product__title {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.chat-product__action {
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background-color: var(--primary-color);
}

.chat-product__action-text {
  font-size: var(--font-sm);
  color: var(--text-white);
}

.chat-scroll {
  flex: 1;
  padding: 0 var(--spacing-md) var(--spacing-md);
  box-sizing: border-box;
}

.chat-message {
  margin-bottom: var(--spacing-sm);
}

.chat-time {
  display: flex;
  justify-content: center;
  margin: var(--spacing-md) 0 var(--spacing-sm);
}

.chat-time__text {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  background-color: var(--bg-grey);
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
}

.chat-system {
  display: flex;
  justify-content: center;
  margin-bottom: var(--spacing-sm);
}

.chat-system__text {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  background-color: var(--bg-grey);
  padding: 8rpx 24rpx;
  border-radius: 999rpx;
}

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
  padding: 16rpx 20rpx;
  border-radius: 20rpx;
  background-color: var(--bg-white);
  box-shadow: 0 8rpx 18rpx rgba(15, 23, 42, 0.04);
}

.chat-bubble__avatar {
  display: flex;
  align-items: center;
}

.chat-bubble.is-self .chat-bubble__content {
  background-color: var(--primary-bg);
}

.chat-bubble__text {
  font-size: var(--font-sm);
  color: var(--text-primary);
  line-height: 1.5;
  word-break: break-all;
}

.chat-quick {
  padding: 0 var(--spacing-md) var(--spacing-sm);
  background-color: var(--bg-page);
}

.chat-quick__scroll {
  width: 100%;
}

.chat-quick__list {
  display: flex;
  gap: var(--spacing-sm);
}

.chat-quick__item {
  padding: 12rpx 22rpx;
  border-radius: 999rpx;
  background-color: var(--bg-white);
  border: 1rpx solid var(--border-light);
  flex-shrink: 0;
}

.chat-quick__text {
  font-size: var(--font-sm);
  color: var(--text-regular);
}

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
