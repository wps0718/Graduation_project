<template>
  <view class="chat-detail">
    <!-- ====== 顶部商品信息栏 ====== -->
      <view v-if="product" class="chat-product">
        <image class="chat-product__image" :src="resolveImageUrl(product.coverImage)" mode="aspectFill" @click="goProductDetail" />
        <view class="chat-product__info" @click="goProductDetail">
          <text class="chat-product__title">{{ product.title }}</text>
          <view class="chat-product__meta">
            <Price :price="product.price" />
            <text v-if="product.conditionText" class="chat-product__condition">{{ product.conditionText }}</text>
            <text v-if="product.statusText" class="chat-product__status">{{ product.statusText }}</text>
          </view>
        </view>
        <view class="chat-product__actions">
          <view class="chat-product__arrow" @click="goProductDetail">
            <text class="chat-product__arrow-text">查看 ▶</text>
          </view>
          <view
            v-if="showBuyButton"
            class="chat-product__buy-btn"
            :class="{ 'is-disabled': buyBtnDisabled }"
            @click="openBuyModal"
          >
            <text class="chat-product__buy-text">{{ buyBtnText }}</text>
          </view>
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
          'is-card': item.type === 'product-card' || item.type === 'pickup-card'
        }"
      >
        <view v-if="item.showTime" class="chat-time">
          <text class="chat-time__text">{{ item.showTime }}</text>
        </view>
        <view v-if="item.type === 'system'" class="chat-system">
          <text class="chat-system__text">{{ item.content }}</text>
        </view>

        <OrderCard
          v-else-if="item.type === 'order-card'"
          :item="item"
          :self-id="selfId"
          @confirm-ship="handleConfirmShip"
          @seller-confirm-receive="handleSellerConfirmReceive"
          @buyer-confirm-receive="handleBuyerConfirmReceive"
          @cancel-order="handleCancelOrder"
        />

        <!-- 代拿价格协商卡片 -->
        <view v-else-if="item.type === 'pickup-card'" class="chat-bubble" :class="{ 'is-self': item.isSelf }">
          <view v-if="!item.isSelf" class="chat-bubble__avatar-col" @click="goPeerProfile">
            <UserAvatar
              v-if="!item.compact"
              :avatar-url="peer.avatarUrl"
              :nick-name="peer.nickName"
              :auth-status="peer.authStatus"
              size="sm"
            />
          </view>
          <view class="pickup-card">
            <view class="pickup-card__header">
              <text class="pickup-card__title">💰 价格协商</text>
            </view>
            <view class="pickup-card__body">
              <view class="pickup-card__row">
                <text class="pickup-card__label">提议报酬</text>
                <text class="pickup-card__price">¥{{ Number(item.pickupPrice || 0).toFixed(2) }}</text>
              </view>
              <view class="pickup-card__row">
                <text class="pickup-card__label">期望送达</text>
                <text class="pickup-card__time">{{ item.pickupExpectedTime || '待协商' }}</text>
              </view>
            </view>
            <view v-if="!item.isSelf && !item.pickupConfirmed" class="pickup-card__footer">
              <view class="pickup-card__agree-btn" @click="agreePickupPrice(item)">
                <text class="pickup-card__agree-text">同意该价格</text>
              </view>
            </view>
            <view v-else-if="item.pickupConfirmed" class="pickup-card__footer pickup-card__footer--done">
              <text class="pickup-card__confirmed">✅ 已确认</text>
            </view>
          </view>
          <view v-if="item.isSelf" class="chat-bubble__avatar-col">
            <UserAvatar
              v-if="item.selfShowAvatar"
              :avatar-url="selfUser.avatarUrl"
              :nick-name="selfUser.nickName"
              size="sm"
              :show-auth="false"
            />
          </view>
        </view>

        <view v-else class="chat-bubble" :class="{ 'is-self': item.isSelf }">
          <!-- 对方头像列（compact 时隐藏头像但保留占位） -->
          <view v-if="!item.isSelf" class="chat-bubble__avatar-col" @click="goPeerProfile">
            <UserAvatar
              v-if="!item.compact"
              :avatar-url="peer.avatarUrl"
              :nick-name="peer.nickName"
              :auth-status="peer.authStatus"
              size="sm"
            />
          </view>

          <view v-if="item.type === 'product-card'" class="chat-bubble__content" @click="goProductDetail">
            <view class="chat-bubble__card">
              <image class="chat-bubble__card-image" :src="resolveImageUrl(item.productImage)" mode="aspectFill" />
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

          <!-- 自己头像列（仅连续消息的最后一条显示） -->
          <view v-if="item.isSelf" class="chat-bubble__avatar-col">
            <UserAvatar
              v-if="item.selfShowAvatar"
              :avatar-url="selfUser.avatarUrl"
              :nick-name="selfUser.nickName"
              size="sm"
              :show-auth="false"
            />
          </view>
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

    <BuyModal
      :visible="showBuyModal"
      :product="product"
      :meeting-points="meetingPoints"
      :form="buyForm"
      :submitting="submitting"
      @close="closeBuyModal"
      @submit="submitBuy"
      @meeting-point-change="onMeetingPointChange"
      @update:form="buyForm = $event"
    />
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { resolveImageUrl } from '@/utils/image'
import { QUICK_REPLIES } from '@/utils/constant'
import { useUserStore } from '@/store'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import Price from '@/components/price/price.vue'
import StatusTag from '@/components/status-tag/status-tag.vue'
import { showToast, ensureLogin } from '@/utils/nav'
import { connect as wsConnect, disconnect as wsDisconnect } from '@/utils/websocket'
import { shouldShowTime, formatMessageTime, formatLastActive, parseActiveTime } from '@/utils/chat-time'
import BuyModal from './buy-modal.vue'
import OrderCard from './order-card.vue'

const userStore = useUserStore()

const scrollHeight = ref(400)

const peer = ref({})
const peerProfile = ref(null)
const product = ref(null)
const sessionKey = ref('')
const messages = ref([])
const scrollIntoView = ref('')
const inputValue = ref('')
const orderCreated = ref(false)
const quickVisible = ref(true)

// 确认购买弹窗
const showBuyModal = ref(false)
const submitting = ref(false)
const meetingPoints = ref([])
const buyForm = ref({
  price: '',
  meetingPointIdx: -1,
  meetingPointText: '',
  remark: ''
})

const isBuyer = computed(() => {
  if (!product.value || !selfId.value) return false
  return selfId.value !== product.value.sellerId
})

const showBuyButton = computed(() => {
  if (!product.value) return false
  if (!isBuyer.value) return false
  if (product.value.status !== 1) return false
  return true
})

const buyBtnDisabled = computed(() => {
  return orderCreated.value || product.value.hasActiveOrder
})

const buyBtnText = computed(() => {
  if (orderCreated.value) return '已下单'
  if (product.value && product.value.hasActiveOrder) return '已有订单'
  return '确认购买'
})

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
  const raw = messages.value
  raw.forEach((item, index) => {
    const showTime = shouldShowTime(item, prev) ? formatMessageTime(item.time) : ''
    const showReadStatus = item.isSelf && item.id === lastSelfMsgId.value
    const compact = prev && !showTime && item.from === prev.from
    const next = raw[index + 1]
    const selfShowAvatar = item.isSelf ? (!next || next.from !== item.from) : false
    list.push({
      ...item,
      showTime,
      showReadStatus,
      compact,
      selfShowAvatar
    })
    prev = item
  })
  return list
})

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
  uni.navigateTo({ url: `/pages/product-sub/detail/detail?id=${product.value.id}` })
}

function openBuyModal() {
  if (buyBtnDisabled.value) {
    showToast(orderCreated.value ? '已发起交易，请耐心等待' : '该商品已有进行中的订单')
    return
  }
  showBuyModal.value = true
}

function closeBuyModal() {
  if (submitting.value) return
  showBuyModal.value = false
}

function onMeetingPointChange(e) {
  const idx = e.detail.value
  buyForm.value.meetingPointIdx = idx
  buyForm.value.meetingPointText = meetingPoints.value[idx] ? meetingPoints.value[idx].name : ''
}

async function submitBuy() {
  if (!product.value) return
  if (submitting.value) return

  const price = buyForm.value.price
  if (!price || isNaN(Number(price)) || Number(price) <= 0) {
    showToast('请输入有效的成交价格')
    return
  }
  if (!buyForm.value.meetingPointText) {
    showToast('请选择面交地点')
    return
  }

  submitting.value = true
  try {
    const orderData = {
      productId: product.value.id,
      price: Number(price),
      meetingPointText: buyForm.value.meetingPointText,
      remark: buyForm.value.remark || undefined
    }
    const data = await post('/mini/order/create', orderData, { showLoading: true })
    orderCreated.value = true
    showBuyModal.value = false

    // 发送订单卡片消息（type=3）
    const sellerId = product.value ? product.value.userId || product.value.sellerId : 0
    const orderMsg = JSON.stringify({
      orderId: data.orderId,
      orderNo: data.orderNo,
      price: Number(price),
      meetingPointText: buyForm.value.meetingPointText,
      status: 1,
      buyerId: selfId.value,
      sellerId: sellerId,
      sellerConfirmed: 0,
      buyerConfirmed: 0
    })
    try {
      const msgId = await post('/mini/chat/message/send', {
        sessionKey: sessionKey.value,
        type: 3,
        content: orderMsg
      }, { showLoading: false })
      // 本地插入订单卡片消息
      messages.value.push({
        id: typeof msgId === 'number' ? msgId : Date.now(),
        time: Date.now(),
        from: selfId.value,
        isSelf: true,
        type: 'order-card',
        orderId: data.orderId,
        orderNo: data.orderNo,
        orderPrice: Number(price),
        orderMeetingPoint: buyForm.value.meetingPointText,
        orderStatus: 1,
        buyerId: selfId.value,
        sellerId: sellerId,
        sellerConfirmed: false,
        buyerConfirmed: false
      })
      scrollToBottom()
    } catch (e) {
      // 订单创建成功但消息发送失败，仍显示系统提示
      appendSystemMessage(`订单已创建：${data.orderNo}`)
    }

    uni.showToast({ title: '订单创建成功', icon: 'success' })
  } catch (error) {
    showToast('下单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function handleConfirmShip(item) {
  if (!item || !item.orderId) return
  uni.showModal({
    title: '确认发货',
    content: '确认该订单可以面交？确认后买家将看到确认收货按钮。',
    confirmText: '确认发货',
    cancelText: '取消',
    success: async (res) => {
      if (!res || !res.confirm) return
      try {
        await post('/mini/order/confirm-ship', { orderId: item.orderId }, { showLoading: true })
        uni.showToast({ title: '已确认发货', icon: 'success' })
        fetchMessages()
      } catch (error) {
        showToast('操作失败，请稍后重试')
      }
    }
  })
}

async function handleSellerConfirmReceive(item) {
  if (!item || !item.orderId) return
  uni.showModal({
    title: '确认交付',
    content: '确认已完成面交交付？双方都确认后交易完成。',
    confirmText: '确认交付',
    cancelText: '取消',
    success: async (res) => {
      if (!res || !res.confirm) return
      try {
        await post('/mini/order/seller-confirm-receive', { orderId: item.orderId }, { showLoading: true })
        uni.showToast({ title: '已确认交付', icon: 'success' })
        fetchMessages()
      } catch (error) {
        showToast(error?.message || '操作失败，请稍后重试')
      }
    }
  })
}

async function handleBuyerConfirmReceive(item) {
  if (!item || !item.orderId) return
  uni.showModal({
    title: '完成交易',
    content: '确认已完成交易？双方都确认后交易完成。',
    confirmText: '确认完成',
    cancelText: '取消',
    success: async (res) => {
      if (!res || !res.confirm) return
      try {
        await post('/mini/order/confirm', { orderId: item.orderId }, { showLoading: true })
        uni.showToast({ title: '已确认完成', icon: 'success' })
        fetchMessages()
      } catch (error) {
        showToast(error?.message || '操作失败，请稍后重试')
      }
    }
  })
}

async function handleCancelOrder(item) {
  if (!item || !item.orderId) return
  uni.showModal({
    title: '取消订单',
    editable: true,
    placeholderText: '请输入取消原因（必填）',
    confirmText: '确认取消',
    cancelText: '再想想',
    success: async (res) => {
      if (!res || !res.confirm) return
      const reason = (res.content || '').trim()
      if (!reason) {
        showToast('请填写取消原因')
        return
      }
      try {
        await post('/mini/order/cancel', {
          orderId: item.orderId,
          cancelReason: reason
        }, { showLoading: true })
        uni.showToast({ title: '订单已取消', icon: 'success' })
        appendSystemMessage('订单已取消')
        // 发送系统消息到聊天，触发对方 WebSocket 推送刷新
        try {
          await post('/mini/chat/message/send', {
            sessionKey: sessionKey.value,
            type: 4,
            content: '订单已取消'
          }, { showLoading: false })
        } catch (e) {
          // 系统消息发送失败不影响取消结果
        }
        fetchMessages()
      } catch (error) {
        showToast(error?.message || '取消失败，请稍后重试')
      }
    }
  })
}

async function agreePickupPrice(item) {
  if (!item || !item.pickupOrderId) return
  uni.showModal({
    title: '确认价格',
    content: `确认同意报酬 ¥${Number(item.pickupPrice || 0).toFixed(2)}，\n期望送达 ${item.pickupExpectedTime || ''}？`,
    confirmText: '同意',
    cancelText: '再想想',
    success: async (res) => {
      if (!res || !res.confirm) return
      try {
        await post('/mini/pickup/confirm-price', { orderId: item.pickupOrderId }, { showLoading: true })
        showToast('价格已确认')

        // 发送确认系统消息
        try {
          await post('/mini/chat/message/send', {
            sessionKey: sessionKey.value,
            type: 1,
            content: `✅ 价格已确认：报酬 ¥${Number(item.pickupPrice || 0).toFixed(2)}，送达 ${item.pickupExpectedTime || ''}`
          }, { showLoading: false })
        } catch (e) {
          // 消息发送失败不影响确认结果
        }
        fetchMessages()
      } catch (error) {
        showToast(error?.message || '确认失败，请稍后重试')
      }
    }
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
      uni.setNavigationBarTitle({ title: data.nickName || '聊天' })
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
      uni.setNavigationBarTitle({ title: data.nickName || '聊天' })
      product.value = {
        ...data,
        coverImage: data.coverImage || (data.images && data.images[0]),
        conditionText: getConditionText(data.conditionLevel),
        statusText: data.status === 1 ? '在售' : '已售'
      }
      // 预填成交价格为商品价格
      buyForm.value.price = data.price || ''
      // 获取面交地点列表
      if (data.campusId) {
        fetchMeetingPoints(data.campusId)
      }
    }
  } catch (error) {
    product.value = null
  }
}

async function fetchMeetingPoints(campusId) {
  if (!campusId) return
  try {
    const data = await get(`/mini/campus/meeting-points/${campusId}`, {}, { showLoading: false })
    if (data && Array.isArray(data)) {
      meetingPoints.value = data
      // 自动选中商品预设的面交地点
      if (product.value && product.value.meetingPointId) {
        const idx = data.findIndex(m => m.id === product.value.meetingPointId)
        if (idx >= 0) {
          buyForm.value.meetingPointIdx = idx
          buyForm.value.meetingPointText = data[idx].name
        }
      }
    }
  } catch (error) {
    console.error('Fetch meeting points error:', error)
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
      const list = data.records.map(m => {
        let type = 'text'
        if (m.msgType === 2) type = 'product-card'
        else if (m.msgType === 3) type = 'order-card'
        else if (m.msgType === 10) type = 'pickup-card'
        const parsed = {
          id: m.msgId,
          from: m.senderId,
          isSelf: !!m.isSelf,
          type,
          content: m.content,
          time: new Date(m.createTime.replace(/-/g, '/')).getTime(),
          isRead: m.isRead,
          ...parseProductCardContent(m.content, m.msgType),
          ...parseOrderCardContent(m.content, m.msgType),
          ...parsePickupCardContent(m.content, m.msgType)
        }
        return parsed
      })
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

function parseOrderCardContent(content, type) {
  if (type !== 3) return {}
  try {
    const data = JSON.parse(content)
    return {
      orderId: data.orderId,
      orderNo: data.orderNo,
      orderPrice: data.price,
      orderMeetingPoint: data.meetingPointText,
      orderStatus: data.status,
      buyerId: data.buyerId,
      sellerId: data.sellerId,
      sellerConfirmed: data.sellerConfirmed === 1,
      buyerConfirmed: data.buyerConfirmed === 1
    }
  } catch (e) {
    return {}
  }
}

function parsePickupCardContent(content, type) {
  if (type !== 10) return {}
  try {
    const data = JSON.parse(content)
    return {
      pickupOrderId: data.orderId,
      pickupPrice: data.proposedPrice,
      pickupExpectedTime: data.expectedTime,
      pickupConfirmed: !!data.confirmed
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
  let topExtra = 0
  if (product.value) {
    topExtra = 110
  } else if (peerProfile.value) {
    topExtra = 120
  }
  const topPx = topExtra * info.windowWidth / 750
  const bottomPx = 120 + (info.safeAreaInsets?.bottom || 0)
  scrollHeight.value = info.windowHeight - topPx - bottomPx
}

onLoad(async (options = {}) => {
  if (!ensureLogin()) return

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
  }

  if (productId && messages.value.length === 0) {
    await sendProductCard()
  }

  // 通过 WebSocket 接收新消息，替代轮询
  wsConnect((msg) => {
    if (msg.type === 'chat' || msg.type === 'read_ack') {
      fetchMessages()
    }
    if (msg.type === 'force_offline') {
      wsDisconnect()
    }
  })
})

import { onUnload, onHide, onShow } from '@dcloudio/uni-app'

onShow(() => {
  if (sessionKey.value) {
    markRead()
  }
})

onHide(() => {
  wsDisconnect()
})

onUnload(() => {
  wsDisconnect()
})

onShareAppMessage(() => {
  const name = (peer.value && peer.value.nickName) || '对方'
  const userId = (peer.value && peer.value.id) || ''
  return {
    title: `与${name}的聊天`,
    path: `/pages/chat-sub/detail/detail?userId=${userId}`
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

/* ====== 商品栏 ====== */
.chat-product {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 16rpx var(--spacing-md);
  border-bottom: 2rpx solid #f0f0f0;
  background-color: var(--bg-white);
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

.chat-product__actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
  flex-shrink: 0;
}

.chat-product__arrow { padding-left: var(--spacing-sm); }
.chat-product__arrow-text {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.chat-product__buy-btn {
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  background-color: var(--primary-color);
}
.chat-product__buy-btn.is-disabled {
  background-color: #ccc;
}
.chat-product__buy-text {
  font-size: 22rpx;
  color: #fff;
  font-weight: 600;
  white-space: nowrap;
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
  gap: 16rpx;
}
.chat-bubble.is-self {
  justify-content: flex-end;
}

/* 头像列 —— 固定宽度撑开布局，无头像时保留占位 */
.chat-bubble__avatar-col {
  width: 60rpx;
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
}

.chat-bubble__content {
  max-width: 480rpx;
  padding: 20rpx 24rpx;
  border-radius: 16rpx 16rpx 16rpx 4rpx;
  background-color: #fff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  position: relative;
  word-break: break-word;
}
.chat-bubble.is-self .chat-bubble__content {
  background-color: var(--primary-color);
  border-radius: 16rpx 16rpx 4rpx 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(74, 144, 217, 0.15);
}

.chat-bubble__text {
  font-size: var(--font-md);
  color: var(--text-primary);
  line-height: 1.6;
}
.chat-bubble.is-self .chat-bubble__text {
  color: #fff;
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
  gap: 20rpx;
  padding: 8rpx 0;
}
.chat-quick__item {
  padding: 14rpx 30rpx;
  border-radius: 999rpx;
  background-color: var(--primary-bg);
  flex-shrink: 0;
}
.chat-quick__text {
  font-size: 26rpx;
  color: var(--text-primary);
  white-space: nowrap;
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
  padding: 0 28rpx;
  border-radius: 999rpx;
  background-color: #f0f0f0;
  font-size: 28rpx;
  color: var(--text-primary);
}
.chat-input__placeholder {
  color: #bbb;
  font-size: 28rpx;
}
.chat-input__send {
  min-width: 124rpx;
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

/* ====== 代拿协商卡片 ====== */
.pickup-card {
  max-width: 480rpx;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid #e5e7eb;
}
.chat-bubble.is-self .pickup-card {
  border-color: transparent;
}

.pickup-card__header {
  padding: 16rpx 24rpx;
  background: #f0f9ff;
  border-bottom: 1rpx solid #e0f2fe;
}

.pickup-card__title {
  font-size: 26rpx;
  color: #0369a1;
  font-weight: 600;
}

.pickup-card__body {
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.pickup-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pickup-card__label {
  font-size: 24rpx;
  color: #9ca3af;
}

.pickup-card__price {
  font-size: 32rpx;
  color: #ef4444;
  font-weight: 700;
}

.pickup-card__time {
  font-size: 26rpx;
  color: #f97316;
}

.pickup-card__footer {
  padding: 16rpx 24rpx;
  border-top: 1rpx solid #f3f4f6;
  display: flex;
  justify-content: flex-end;
}

.pickup-card__footer--done {
  justify-content: center;
}

.pickup-card__agree-btn {
  padding: 12rpx 32rpx;
  background: linear-gradient(135deg, #10b981, #059669);
  border-radius: 12rpx;
}

.pickup-card__agree-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 600;
}

.pickup-card__confirmed {
  font-size: 24rpx;
  color: #059669;
  font-weight: 600;
}
</style>
