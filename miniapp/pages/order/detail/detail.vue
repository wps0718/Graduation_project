<template>
  <view class="order-detail" v-if="order">
    <view class="order-header">
      <view class="order-header__status">
        <StatusTag type="order" :value="order.status" />
        <text class="order-header__status-text">{{ statusText }}</text>
      </view>
      <text class="order-header__no">订单号：{{ order.orderNo }}</text>
    </view>

    <view class="order-section">
      <text class="order-section__title">面交信息</text>
      <view class="order-info-grid">
        <view class="order-info-item">
          <text class="order-info-item__label">面交校区</text>
          <text class="order-info-item__value">{{ order.campusName || '未指定' }}</text>
        </view>
        <view class="order-info-item">
          <text class="order-info-item__label">面交地点</text>
          <text class="order-info-item__value">{{ order.meetingPoint || '未指定' }}</text>
        </view>
        <view class="order-info-item">
          <text class="order-info-item__label">成交价格</text>
          <text class="order-info-item__value order-info-item__value--price">¥{{ order.price }}</text>
        </view>
      </view>
    </view>

    <view class="order-section">
      <text class="order-section__title">时间信息</text>
      <view class="order-info-grid">
        <view class="order-info-item">
          <text class="order-info-item__label">创建时间</text>
          <text class="order-info-item__value">{{ formatTime(order.createTime) }}</text>
        </view>
        <view v-if="order.status === ORDER_STATUS.PENDING || order.status === ORDER_STATUS.PENDING_MEET" class="order-info-item">
          <text class="order-info-item__label">超时时间</text>
          <text class="order-info-item__value order-info-item__value--warn">{{ expireText }}</text>
        </view>
        <view v-if="order.completeTime" class="order-info-item">
          <text class="order-info-item__label">完成时间</text>
          <text class="order-info-item__value">{{ formatTime(order.completeTime) }}</text>
        </view>
      </view>
    </view>

    <view v-if="order.status === ORDER_STATUS.PENDING_MEET" class="order-section">
      <text class="order-section__title">确认状态</text>
      <view class="order-confirm-status">
        <view class="order-confirm-item">
          <text class="order-confirm-item__label">卖家确认交付</text>
          <text class="order-confirm-item__value" :class="{ 'is-done': order.sellerConfirmed }">{{ order.sellerConfirmed ? '✓ 已确认' : '未确认' }}</text>
        </view>
        <view class="order-confirm-item">
          <text class="order-confirm-item__label">买家确认交易</text>
          <text class="order-confirm-item__value" :class="{ 'is-done': order.buyerConfirmed }">{{ order.buyerConfirmed ? '✓ 已确认' : '未确认' }}</text>
        </view>
      </view>
    </view>

    <view class="order-section">
      <text class="order-section__title">商品信息</text>
      <view class="order-product" @click="goProductDetail">
        <image class="order-product__image" :src="productImage" mode="aspectFill" />
        <view class="order-product__info">
          <text class="order-product__title">{{ order.productTitle }}</text>
          <text class="order-product__desc">{{ order.productDescription }}</text>
        </view>
        <text class="order-product__arrow">▶</text>
      </view>
    </view>

    <view class="order-section">
      <text class="order-section__title">用户信息</text>
      <view class="order-user-card" @click="goOtherProfile">
        <UserAvatar
          :avatar-url="otherUser.avatar"
          :nick-name="otherUser.nickName"
          :auth-status="2"
          size="md"
        />
        <view class="order-user-card__info">
          <text class="order-user-card__name">{{ otherUser.nickName }}</text>
          <text class="order-user-card__role">{{ otherUser.roleLabel }}</text>
        </view>
        <text class="order-user-card__arrow">▶</text>
      </view>
    </view>

    <view v-if="order.status === ORDER_STATUS.CANCELLED && order.cancelReason" class="order-section">
      <text class="order-section__title">取消信息</text>
      <view class="order-cancel">
        <text class="order-cancel__reason">{{ order.cancelReason }}</text>
        <text v-if="order.cancelByText" class="order-cancel__by">{{ order.cancelByText }}</text>
      </view>
    </view>

    <view class="order-actions safe-area-bottom">
      <view
        v-if="showConfirmBtn"
        class="order-btn order-btn--primary"
        @click="confirmReceive"
      >
        <text>{{ confirmBtnText }}</text>
      </view>
      <view
        v-if="showCancelBtn"
        class="order-btn order-btn--danger"
        @click="cancelOrder"
      >
        <text>取消交易</text>
      </view>
      <view
        v-if="showReviewBtn"
        class="order-btn order-btn--primary"
        @click="goReview"
      >
        <text>去评价</text>
      </view>
      <view
        v-if="showDeleteBtn"
        class="order-btn order-btn--default"
        @click="deleteOrder"
      >
        <text>删除订单</text>
      </view>
      <view
        v-if="showContactBtn"
        class="order-btn order-btn--default"
        @click="contactOther"
      >
        <text>联系{{ otherUser.roleLabel }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { ORDER_STATUS } from '@/utils/constant'
import { useUserStore } from '@/store'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import StatusTag from '@/components/status-tag/status-tag.vue'

const userStore = useUserStore()

const order = ref(null)

const statusText = computed(() => {
  if (!order.value) return ''
  const map = { 1: '等待卖家接单', 2: '待面交', 3: '交易已完成，待评价', 4: '已评价', 5: '已取消' }
  return map[order.value.status] || ''
})

const productImage = computed(() => {
  const images = order.value?.productImages
  if (images && images.length) return images[0]
  return ''
})

const selfId = computed(() => {
  const info = userStore.userInfo
  if (info && info.id) return Number(info.id)
  return 0
})

const myRole = computed(() => {
  if (!order.value) return ''
  const buyerId = order.value.buyerId
  const sellerId = order.value.sellerId
  if (selfId.value === buyerId) return 'buyer'
  if (selfId.value === sellerId) return 'seller'
  return ''
})

const otherUser = computed(() => {
  if (!order.value) return {}
  const isBuyer = myRole.value === 'buyer'
  return {
    avatar: isBuyer ? order.value.sellerAvatar : order.value.buyerAvatar,
    nickName: isBuyer ? order.value.sellerNickName : order.value.buyerNickName,
    id: isBuyer ? order.value.sellerId : order.value.buyerId,
    roleLabel: isBuyer ? '卖家' : '买家'
  }
})

const expireText = computed(() => {
  if (!order.value || !order.value.expireTime) return ''
  const expire = new Date(order.value.expireTime.replace(/-/g, '/'))
  const now = Date.now()
  const diff = expire.getTime() - now
  if (diff <= 0) return '已超时'
  const hours = Math.floor(diff / 3600000)
  const minutes = Math.floor((diff % 3600000) / 60000)
  if (hours > 0) return `${hours}小时${minutes}分后超时`
  return `${minutes}分后超时`
})

const showConfirmBtn = computed(() => {
  if (!order.value) return false
  if (order.value.status === ORDER_STATUS.PENDING && myRole.value === 'seller') return true
  if (order.value.status === ORDER_STATUS.PENDING_MEET) {
    if (myRole.value === 'seller' && !order.value.sellerConfirmed) return true
    if (myRole.value === 'buyer' && !order.value.buyerConfirmed) return true
  }
  return false
})

const confirmBtnText = computed(() => {
  if (!order.value) return ''
  if (order.value.status === ORDER_STATUS.PENDING && myRole.value === 'seller') return '确认发货'
  if (order.value.status === ORDER_STATUS.PENDING_MEET && myRole.value === 'seller') return '已交付'
  if (order.value.status === ORDER_STATUS.PENDING_MEET && myRole.value === 'buyer') return '完成交易'
  return '确认'
})

const showCancelBtn = computed(() => {
  return order.value && (order.value.status === ORDER_STATUS.PENDING || order.value.status === ORDER_STATUS.PENDING_MEET)
})

const showReviewBtn = computed(() => {
  return order.value && order.value.status === ORDER_STATUS.COMPLETED
})

const showDeleteBtn = computed(() => {
  return order.value && (order.value.status === ORDER_STATUS.REVIEWED || order.value.status === ORDER_STATUS.CANCELLED)
})

const showContactBtn = computed(() => {
  return order.value && order.value.status === ORDER_STATUS.PENDING
})

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function formatTime(value) {
  if (!value) return ''
  const d = new Date(String(value).replace(/-/g, '/'))
  if (isNaN(d.getTime())) return value
  const y = d.getFullYear()
  const M = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  const h = `${d.getHours()}`.padStart(2, '0')
  const m = `${d.getMinutes()}`.padStart(2, '0')
  return `${y}-${M}-${day} ${h}:${m}`
}

function goProductDetail() {
  if (!order.value || !order.value.productId) return
  uni.navigateTo({ url: `/pages/product/detail/detail?id=${order.value.productId}` })
}

function goOtherProfile() {
  if (!otherUser.value.id) return
  uni.navigateTo({ url: `/pages/user-sub/seller/profile?id=${otherUser.value.id}` })
}

function contactOther() {
  if (!otherUser.value.id) return
  uni.navigateTo({ url: `/pages/chat/detail/detail?peerId=${otherUser.value.id}` })
}

function confirmReceive() {
  const status = order.value.status
  const role = myRole.value
  let title = '确认'
  let content = ''
  let url = ''

  if (status === ORDER_STATUS.PENDING && role === 'seller') {
    title = '确认发货'
    content = '确认该订单可以面交？'
    url = '/mini/order/confirm-ship'
  } else if (status === ORDER_STATUS.PENDING_MEET && role === 'seller') {
    title = '确认交付'
    content = '确认已完成面交交付？双方都确认后交易完成。'
    url = '/mini/order/seller-confirm-receive'
  } else if (status === ORDER_STATUS.PENDING_MEET && role === 'buyer') {
    title = '完成交易'
    content = '确认已完成交易？双方都确认后交易完成。'
    url = '/mini/order/confirm'
  } else {
    return
  }

  uni.showModal({
    title,
    content,
    confirmText: title,
    cancelText: '取消',
    success: async (res) => {
      if (!res || !res.confirm) return
      try {
        await post(url, { orderId: order.value.id }, { showLoading: true })
        showToast(title + '成功')
        setTimeout(() => fetchOrder(), 500)
      } catch (error) {
        showToast('操作失败，请稍后重试')
      }
    }
  })
}

function cancelOrder() {
  const reasons = myRole.value === 'buyer' ? ['不想买了', '对方无响应', '双方协商取消', '其他'] : ['不想卖了', '对方无响应', '双方协商取消', '其他']
  uni.showActionSheet({
    itemList: reasons,
    success: async (res) => {
      if (!res) return
      const reason = reasons[res.tapIndex]
      try {
        await post('/mini/order/cancel', { orderId: order.value.id, cancelReason: reason }, { showLoading: true })
        showToast('已取消交易')
        setTimeout(() => fetchOrder(), 500)
      } catch (error) {
        showToast('取消失败，请稍后重试')
      }
    }
  })
}

function goReview() {
  uni.navigateTo({ url: `/pages/user-sub/review/review?orderId=${order.value.id}` })
}

function deleteOrder() {
  uni.showModal({
    title: '删除订单',
    content: '确认删除该订单吗？',
    confirmText: '删除',
    cancelText: '取消',
    success: async (res) => {
      if (!res || !res.confirm) return
      try {
        await post('/mini/order/delete', { orderId: order.value.id }, { showLoading: true })
        showToast('已删除')
        setTimeout(() => uni.navigateBack(), 500)
      } catch (error) {
        showToast('删除失败，请稍后重试')
      }
    }
  })
}

async function fetchOrder() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const id = (currentPage && currentPage.options && currentPage.options.id) ? Number(currentPage.options.id) : null
  if (!id) {
    showToast('订单ID无效')
    return
  }
  try {
    const data = await get(`/mini/order/detail/${id}`, {}, { showLoading: true })
    if (data) {
      const cancelByText = data.cancelBy === 0 ? '系统自动取消'
        : data.cancelBy === data.buyerId ? '买家取消'
        : data.cancelBy === data.sellerId ? '卖家取消'
        : ''
      order.value = {
        ...data,
        cancelByText
      }
    }
  } catch (error) {
    showToast('加载失败')
  }
}

onLoad(() => {
  fetchOrder()
})
</script>

<style lang="scss" scoped>
.order-detail {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: 140rpx;
}

.order-header {
  background-color: var(--bg-white);
  padding: var(--spacing-lg) var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.order-header__status {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.order-header__status-text {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.order-header__no {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.order-section {
  background-color: var(--bg-white);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.order-section__title {
  font-size: var(--font-sm);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.order-info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.order-info-item {
  flex: 1;
  min-width: 180rpx;
}

.order-info-item__label {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  margin-bottom: 4rpx;
  display: block;
}

.order-info-item__value {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.order-info-item__value--price {
  color: var(--primary-color);
  font-weight: 600;
}

.order-info-item__value--warn {
  color: #ff9800;
}

.order-product {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.order-product__image {
  width: 100rpx;
  height: 100rpx;
  border-radius: var(--radius-sm);
  background-color: var(--bg-grey);
  flex-shrink: 0;
}

.order-product__info {
  flex: 1;
  min-width: 0;
}

.order-product__title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 500;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.order-product__desc {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  margin-top: 4rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.order-product__arrow {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.order-user-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.order-user-card__info {
  flex: 1;
}

.order-user-card__name {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 500;
}

.order-user-card__role {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  margin-top: 4rpx;
}

.order-user-card__arrow {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.order-cancel {
  padding: var(--spacing-sm);
  background-color: var(--bg-grey);
  border-radius: var(--radius-sm);
}

.order-cancel__reason {
  font-size: var(--font-sm);
  color: var(--text-primary);
}

.order-cancel__by {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  margin-top: 4rpx;
  display: block;
}

.order-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: var(--bg-white);
  border-top: 1rpx solid var(--border-light);
}

.order-btn {
  padding: 16rpx 32rpx;
  border-radius: var(--radius-round);
  font-size: var(--font-md);
}

.order-btn--primary {
  background-color: var(--primary-color);
  color: #fff;
}

.order-btn--danger {
  background-color: #fff;
  color: var(--danger-color);
  border: 1rpx solid var(--danger-color);
}

.order-btn--default {
  background-color: #fff;
  color: var(--text-primary);
  border: 1rpx solid var(--border-light);
}

.order-confirm-status {
  display: flex;
  gap: var(--spacing-md);
}

.order-confirm-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.order-confirm-item__label {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.order-confirm-item__value {
  font-size: var(--font-sm);
  color: #ff9800;
  font-weight: 500;
}

.order-confirm-item__value.is-done {
  color: #52c41a;
}
</style>
