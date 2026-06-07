<template>
  <view class="pickup-detail" v-if="order">
    <!-- 头部状态 -->
    <view class="pickup-detail__header">
      <view class="pickup-detail__status">
        <StatusTag type="pickup" :value="order.status" />
        <text class="pickup-detail__status-text">{{ statusDesc }}</text>
      </view>
      <text class="pickup-detail__order-no">订单号：{{ order.orderNo }}</text>
      <view v-if="isPickingUp && countdownText" class="pickup-detail__countdown">
        <text class="pickup-detail__countdown-icon">⏰</text>
        <text class="pickup-detail__countdown-text">剩余 {{ countdownText }}</text>
      </view>
    </view>

    <!-- 路线信息 -->
    <view class="pickup-detail__section">
      <text class="pickup-detail__section-title">取送路线</text>
      <view class="pickup-detail__route">
        <view class="pickup-detail__route-item">
          <view class="pickup-detail__dot pickup-detail__dot--green"></view>
          <view class="pickup-detail__route-info">
            <text class="pickup-detail__route-label">取件地点</text>
            <text class="pickup-detail__route-value">{{ order.pickupLocation }}</text>
          </view>
        </view>
        <view class="pickup-detail__route-line"></view>
        <view class="pickup-detail__route-item">
          <view class="pickup-detail__dot pickup-detail__dot--red"></view>
          <view class="pickup-detail__route-info">
            <text class="pickup-detail__route-label">送达地点</text>
            <text class="pickup-detail__route-value">{{ order.deliveryLocation }}</text>
          </view>
        </view>
      </view>
      <view v-if="order.pickupDetail" class="pickup-detail__detail-text">
        <text class="pickup-detail__detail-label">补充说明：</text>
        <text>{{ order.pickupDetail }}</text>
      </view>
    </view>

    <!-- 取件码（条件展示） -->
    <view v-if="showPickupCode" class="pickup-detail__section">
      <text class="pickup-detail__section-title">取件码</text>
      <view class="pickup-detail__code-box" @click="copyCode">
        <text class="pickup-detail__code">{{ order.pickupCode || '未提供' }}</text>
        <text v-if="order.pickupCode" class="pickup-detail__code-copy">复制</text>
      </view>
    </view>

    <!-- 价格信息：status=1 需求者视角 -->
    <view v-if="order.status === 1 && isRequester" class="pickup-detail__section">
      <text class="pickup-detail__section-title">价格信息</text>
      <view class="pickup-detail__price-row">
        <view class="pickup-detail__price-col">
          <text class="pickup-detail__info-label">提议报酬</text>
          <view class="pickup-detail__price-amount">
            <text class="pickup-detail__price-symbol">¥</text>
            <text class="pickup-detail__price-value">{{ Number(order.proposedPrice).toFixed(2) }}</text>
          </view>
        </view>
      </view>
      <view class="pickup-detail__price-hint pickup-detail__price-hint--blue">
        <text class="pickup-detail__hint-icon">💡</text>
        <text class="pickup-detail__hint-desc">修改报酬后可发送给代拿者确认</text>
      </view>
    </view>

    <!-- 价格信息：status=1 代拿者视角 -->
    <view v-if="order.status === 1 && isPicker" class="pickup-detail__section">
      <text class="pickup-detail__section-title">价格信息</text>
      <view class="pickup-detail__price-row">
        <view class="pickup-detail__price-col">
          <text class="pickup-detail__info-label">提议报酬</text>
          <view class="pickup-detail__price-amount">
            <text class="pickup-detail__price-hidden">待确认</text>
          </view>
        </view>
      </view>
      <view class="pickup-detail__price-hint pickup-detail__price-hint--orange">
        <text class="pickup-detail__hint-icon">⏳</text>
        <text class="pickup-detail__hint-desc">等待需求者发送报价</text>
      </view>
      <view class="pickup-detail__price-hint pickup-detail__price-hint--blue">
        <text class="pickup-detail__hint-icon">💡</text>
        <text class="pickup-detail__hint-desc">请在聊天中与需求者协商价格和送达时间</text>
      </view>
    </view>

    <!-- 价格信息：status>=2 已确认 -->
    <view v-if="order.status >= 2" class="pickup-detail__section">
      <text class="pickup-detail__section-title">价格信息</text>
      <view class="pickup-detail__price-row">
        <view class="pickup-detail__price-col">
          <text class="pickup-detail__info-label">确认报酬</text>
          <view class="pickup-detail__price-amount">
            <text class="pickup-detail__price-symbol">¥</text>
            <text class="pickup-detail__price-value pickup-detail__price-value--confirmed">{{ Number(order.agreedPrice || order.proposedPrice).toFixed(2) }}</text>
          </view>
        </view>
        <view class="pickup-detail__confirmed-tag">
          <text class="pickup-detail__confirmed-text">✅ 已确认</text>
        </view>
      </view>
    </view>

    <!-- 时间信息 -->
    <view class="pickup-detail__section">
      <text class="pickup-detail__section-title">时间信息</text>
      <view class="pickup-detail__info-grid">
        <view class="pickup-detail__info-item">
          <text class="pickup-detail__info-label">期望送达</text>
          <text class="pickup-detail__info-value">{{ formatTime(order.expectedDeliveryTime) }}</text>
        </view>
        <view class="pickup-detail__info-item">
          <text class="pickup-detail__info-label">创建时间</text>
          <text class="pickup-detail__info-value">{{ formatTime(order.createTime) }}</text>
        </view>
        <view v-if="order.completeTime" class="pickup-detail__info-item">
          <text class="pickup-detail__info-label">完成时间</text>
          <text class="pickup-detail__info-value">{{ formatTime(order.completeTime) }}</text>
        </view>
      </view>
    </view>

    <!-- 对方信息 -->
    <view v-if="otherUser" class="pickup-detail__section">
      <text class="pickup-detail__section-title">{{ isRequester ? '代拿者' : '需求者' }}</text>
      <view class="pickup-detail__user-card" @click="goOtherProfile">
        <image class="pickup-detail__avatar" :src="otherUser.avatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="pickup-detail__user-info">
          <text class="pickup-detail__user-name">{{ otherUser.nickName }}</text>
          <text class="pickup-detail__user-role">{{ isRequester ? '代拿者' : '需求者' }}</text>
        </view>
        <text class="pickup-detail__arrow">›</text>
      </view>
    </view>

    <!-- 送达证据（status>=4时对双方可见） -->
    <view v-if="order.status >= 4" class="pickup-detail__section">
      <text class="pickup-detail__section-title">代拿证据</text>
      <view v-if="evidenceImages.length" class="pickup-detail__evidence">
        <image
          v-for="(img, i) in evidenceImages"
          :key="i"
          class="pickup-detail__evidence-img"
          :src="img"
          mode="aspectFill"
          @click="previewEvidence(i)"
        />
      </view>
      <view v-else class="pickup-detail__empty-evidence">
        <text class="pickup-detail__empty-evidence-text">代拿者暂未上传送达证据</text>
      </view>
      <view v-if="order.status === 4 && isRequester && evidenceImages.length" class="pickup-detail__evidence-hint">
        <text class="pickup-detail__hint-text">如有疑问可发起纠纷</text>
      </view>
    </view>

    <!-- 提交证据区域 -->
    <view v-if="showEvidenceForm" class="pickup-detail__section">
      <text class="pickup-detail__section-title">上传送达证据</text>
      <EvidenceUpload
        type="image"
        :max-images="4"
        :show-description="false"
        @change="onEvidenceChange"
      />
      <view class="pickup-detail__evidence-submit" @click="submitEvidence">
        <text class="pickup-detail__evidence-submit-text">{{ submitting ? '提交中...' : '确认送达' }}</text>
      </view>
    </view>

    <!-- 取消信息 -->
    <view v-if="order.status === 7 && order.cancelReason" class="pickup-detail__section">
      <text class="pickup-detail__section-title">取消原因</text>
      <text class="pickup-detail__cancel-reason">{{ order.cancelReason }}</text>
    </view>

    <!-- 底部操作栏 -->
    <view class="pickup-detail__actions safe-area-bottom">
      <!-- status=0 待接单 -->
      <view v-if="order.status === 0 && !isRequester" class="pickup-detail__btn pickup-detail__btn--primary" @click="handleAccept">
        <text>立即接单</text>
      </view>
      <view v-if="order.status === 0 && isRequester" class="pickup-detail__btn pickup-detail__btn--danger" @click="handleCancel">
        <text>取消订单</text>
      </view>

      <!-- status=1 已接单/待确认价格 -->
      <view v-if="order.status === 1 && isRequester" class="pickup-detail__btn pickup-detail__btn--primary" @click="goModifyPrice">
        <text>修改报酬</text>
      </view>
      <view v-if="order.status === 1" class="pickup-detail__btn pickup-detail__btn--default" @click="contactOther">
        <text>联系对方</text>
      </view>
      <view v-if="order.status === 1 && isRequester" class="pickup-detail__btn pickup-detail__btn--danger" @click="handleCancel">
        <text>取消订单</text>
      </view>

      <!-- status=2 价格已确认/待代拿 -->
      <view v-if="order.status === 2 && isPicker" class="pickup-detail__btn pickup-detail__btn--primary" @click="handleStartPickup">
        <text>开始代拿</text>
      </view>
      <view v-if="order.status === 2" class="pickup-detail__btn pickup-detail__btn--default" @click="contactOther">
        <text>联系对方</text>
      </view>

      <!-- status=3 代拿中 -->
      <view v-if="order.status === 3 && isPicker && !showEvidenceForm" class="pickup-detail__btn pickup-detail__btn--primary" @click="toggleEvidenceForm">
        <text>确认送达</text>
      </view>
      <view v-if="order.status === 3" class="pickup-detail__btn pickup-detail__btn--default" @click="contactOther">
        <text>联系对方</text>
      </view>

      <!-- status=4 已送达 -->
      <view v-if="order.status === 4 && isRequester" class="pickup-detail__btn pickup-detail__btn--primary" @click="handleConfirmReceive">
        <text>确认收货</text>
      </view>
      <view v-if="order.status === 4 && isRequester" class="pickup-detail__btn pickup-detail__btn--danger" @click="goDispute">
        <text>发起纠纷</text>
      </view>
      <view v-if="order.status === 4" class="pickup-detail__btn pickup-detail__btn--default" @click="contactOther">
        <text>联系对方</text>
      </view>

      <!-- status=5/6 已完成/已评价 -->
      <view v-if="(order.status === 5 || order.status === 6) && !hasRated" class="pickup-detail__btn pickup-detail__btn--primary" @click="goReview">
        <text>去评价</text>
      </view>

      <!-- status=8 纠纷中 -->
      <view v-if="order.status === 8" class="pickup-detail__btn pickup-detail__btn--default" @click="goDisputeDetail">
        <text>查看纠纷</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { PICKUP_STATUS } from '@/utils/constant'
import { getCountdown } from '@/utils/pickup-helpers'
import { resolveImageUrl } from '@/utils/image'
import { useUserStore } from '@/store'
import StatusTag from '@/components/status-tag/status-tag.vue'
import EvidenceUpload from '@/components/evidence-upload/evidence-upload.vue'
import { showToast } from '@/utils/nav'

const userStore = useUserStore()
const order = ref(null)
const showEvidenceForm = ref(false)
const evidenceData = ref({ images: [], texts: [] })
const submitting = ref(false)

const selfId = computed(() => {
  const info = userStore.userInfo
  return info && info.id ? Number(info.id) : 0
})

const isRequester = computed(() => order.value && selfId.value === order.value.requesterId)
const isPicker = computed(() => order.value && selfId.value === order.value.pickerId)

const showPickupCode = computed(() => {
  if (!order.value) return false
  return isRequester.value || isPicker.value
})

const isPickingUp = computed(() => order.value && order.value.status === PICKUP_STATUS.PICKING_UP)

const countdownText = computed(() => {
  if (!order.value || !order.value.deliveryDeadline) return ''
  return getCountdown(order.value.deliveryDeadline)
})

const otherUser = computed(() => {
  if (!order.value) return null
  if (isRequester.value) {
    return order.value.pickerId ? {
      id: order.value.pickerId,
      avatar: resolveImageUrl(order.value.pickerAvatar),
      nickName: order.value.pickerNickName
    } : null
  }
  return {
    id: order.value.requesterId,
    avatar: resolveImageUrl(order.value.requesterAvatar),
    nickName: order.value.requesterNickName
  }
})

const evidenceImages = computed(() => {
  if (!order.value || !order.value.evidenceImages) return []
  try {
    const raw = order.value.evidenceImages
    const images = Array.isArray(raw) ? raw : JSON.parse(raw)
    if (!Array.isArray(images)) return []
    return images.filter(url => url && typeof url === 'string' && url.trim()).map(url => resolveImageUrl(url))
  } catch (e) {
    console.error('解析evidenceImages失败:', e, order.value.evidenceImages)
    return []
  }
})

const hasRated = computed(() => order.value && order.value.status === PICKUP_STATUS.RATED)

const statusDesc = computed(() => {
  if (!order.value) return ''
  const map = {
    0: '等待代拿者接单',
    1: '待确认价格',
    2: '等待代拿者取件',
    3: '代拿者正在配送中',
    4: '已送达，请确认收货',
    5: '订单已完成',
    6: '订单已评价',
    7: '订单已取消',
    8: '纠纷处理中'
  }
  return map[order.value.status] || ''
})

function formatTime(value) {
  if (!value) return ''
  const d = new Date(String(value).replace('T', ' ').replace(/-/g, '/'))
  if (isNaN(d.getTime())) return value
  const M = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  const h = `${d.getHours()}`.padStart(2, '0')
  const m = `${d.getMinutes()}`.padStart(2, '0')
  return `${M}-${day} ${h}:${m}`
}

function copyCode() {
  if (!order.value || !order.value.pickupCode) return
  uni.setClipboardData({
    data: order.value.pickupCode,
    success: () => showToast('已复制取件码')
  })
}

function previewEvidence(index) {
  uni.previewImage({ urls: evidenceImages.value, current: evidenceImages.value[index] })
}

function goOtherProfile() {
  if (!otherUser.value || !otherUser.value.id) return
  uni.navigateTo({ url: `/pages/user-sub/seller/profile?id=${otherUser.value.id}` })
}

async function contactOther() {
  if (!otherUser.value || !otherUser.value.id) return
  try {
    const data = await post('/mini/chat/session/create', {
      peerId: otherUser.value.id
    }, { showLoading: true })
    if (data && data.sessionKey) {
      uni.navigateTo({
        url: `/pages/chat-sub/detail/detail?sessionKey=${data.sessionKey}&peerId=${otherUser.value.id}`
      })
    }
  } catch (e) {
    showToast('发起私信失败，请重试')
  }
}

async function fetchOrder() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const id = currentPage?.options?.id ? Number(currentPage.options.id) : null
  if (!id) { showToast('订单ID无效'); return }
  try {
    const data = await get(`/mini/pickup/detail/${id}`, {}, { showLoading: true })
    if (data) {
      order.value = data
      console.log('[PickupDetail] evidenceImages:', data.evidenceImages)
      console.log('[PickupDetail] requesterAvatar:', data.requesterAvatar)
      console.log('[PickupDetail] pickerAvatar:', data.pickerAvatar)
    }
  } catch (e) { showToast('加载失败') }
}

async function handleAccept() {
  uni.showModal({
    title: '确认接单',
    content: `确定要接下这单吗？报酬 ¥${Number(order.value.proposedPrice).toFixed(2)}`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/pickup/accept', { orderId: order.value.id })
        showToast('接单成功')
        setTimeout(() => fetchOrder(), 500)
      } catch (e) { /* handled by request util */ }
    }
  })
}

function goModifyPrice() {
  const o = order.value
  const params = [
    `orderId=${o.id}`,
    `price=${o.proposedPrice}`,
    `pickerUserId=${o.pickerId || ''}`
  ]
  if (o.expectedDeliveryTime) {
    params.push(`expectedTime=${encodeURIComponent(o.expectedDeliveryTime)}`)
  }
  uni.navigateTo({ url: `/pages/pickup-sub/price-confirm/price-confirm?${params.join('&')}` })
}

async function handleStartPickup() {
  uni.showModal({
    title: '开始代拿',
    content: '确认已前往取件地点？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/pickup/start', { orderId: order.value.id })
        showToast('已开始代拿')
        setTimeout(() => fetchOrder(), 500)
      } catch (e) { /* handled */ }
    }
  })
}

function toggleEvidenceForm() {
  showEvidenceForm.value = true
}

function onEvidenceChange(data) {
  evidenceData.value = data
}

async function submitEvidence() {
  if (submitting.value) return
  const images = evidenceData.value.images || []
  if (images.length === 0) { showToast('请至少上传一张送达证据图片'); return }
  submitting.value = true
  try {
    const imageUrls = images.map((item) => item.url)
    await post('/mini/pickup/submit-evidence', {
      orderId: order.value.id,
      evidenceImages: imageUrls
    })
    showToast('已提交送达证据')
    showEvidenceForm.value = false
    setTimeout(() => fetchOrder(), 500)
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

async function handleConfirmReceive() {
  uni.showModal({
    title: '确认收货',
    content: '确认已收到快递？确认后将完成订单。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/pickup/confirm-receive', { orderId: order.value.id })
        showToast('已确认收货')
        setTimeout(() => fetchOrder(), 500)
      } catch (e) { /* handled */ }
    }
  })
}

function handleCancel() {
  uni.showActionSheet({
    itemList: ['不想代拿了', '对方无响应', '双方协商取消', '其他'],
    success: async (res) => {
      const reasons = ['不想代拿了', '对方无响应', '双方协商取消', '其他']
      const reason = reasons[res.tapIndex]
      try {
        await post('/mini/pickup/cancel', { orderId: order.value.id, cancelReason: reason })
        showToast('已取消')
        setTimeout(() => fetchOrder(), 500)
      } catch (e) { /* handled */ }
    }
  })
}

function goDispute() {
  uni.navigateTo({ url: `/pages/pickup-sub/dispute-submit/dispute-submit?orderId=${order.value.id}` })
}

function goDisputeDetail() {
  uni.navigateTo({ url: `/pages/pickup-sub/dispute-respond/dispute-respond?orderId=${order.value.id}` })
}

function goReview() {
  showToast('评价功能开发中')
}

onLoad(() => {
  fetchOrder()
})
</script>

<style lang="scss" scoped>
.pickup-detail {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 180rpx;
}

.pickup-detail__header {
  background: linear-gradient(135deg, #4A90D9, #3b7dd8);
  padding: 32rpx 32rpx 40rpx;
}

.pickup-detail__status {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.pickup-detail__status-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: 600;
}

.pickup-detail__order-no {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.pickup-detail__countdown {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12rpx;
  padding: 12rpx 20rpx;
  width: fit-content;
}

.pickup-detail__countdown-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.pickup-detail__countdown-text {
  font-size: 26rpx;
  color: #fff;
}

.pickup-detail__section {
  background: #fff;
  margin: 20rpx 24rpx 0;
  border-radius: 20rpx;
  padding: 24rpx;
}

.pickup-detail__section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.pickup-detail__route {
  display: flex;
  flex-direction: column;
}

.pickup-detail__route-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.pickup-detail__dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.pickup-detail__dot--green { background: #52c41a; }
.pickup-detail__dot--red { background: #ff4d4f; }

.pickup-detail__route-info {
  flex: 1;
}

.pickup-detail__route-label {
  font-size: 22rpx;
  color: #999;
  display: block;
}

.pickup-detail__route-value {
  font-size: 28rpx;
  color: #333;
}

.pickup-detail__route-line {
  width: 2rpx;
  height: 32rpx;
  background: #e0e0e0;
  margin-left: 7rpx;
}

.pickup-detail__detail-text {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  font-size: 26rpx;
  color: #666;
}

.pickup-detail__detail-label {
  color: #999;
}

.pickup-detail__code-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 24rpx;
}

.pickup-detail__code {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
  letter-spacing: 4rpx;
  font-family: 'Courier New', monospace;
}

.pickup-detail__code-copy {
  font-size: 26rpx;
  color: #4A90D9;
}

.pickup-detail__info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.pickup-detail__info-item {
  flex: 1;
  min-width: 180rpx;
}

.pickup-detail__info-label {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-bottom: 4rpx;
}

.pickup-detail__info-value {
  font-size: 28rpx;
  color: #333;
}

.pickup-detail__info-value--price {
  color: #ff6b35;
  font-weight: 700;
  font-size: 32rpx;
}

.pickup-detail__price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pickup-detail__price-col {
  flex: 1;
}

.pickup-detail__price-amount {
  display: flex;
  align-items: baseline;
  margin-top: 8rpx;
}

.pickup-detail__price-symbol {
  font-size: 28rpx;
  color: #ff6b35;
  font-weight: 600;
}

.pickup-detail__price-value {
  font-size: 48rpx;
  color: #ff6b35;
  font-weight: 700;
}

.pickup-detail__price-value--confirmed {
  color: #333;
}

.pickup-detail__price-hidden {
  font-size: 32rpx;
  color: #999;
  font-style: italic;
  margin-top: 8rpx;
}

.pickup-detail__confirmed-tag {
  padding: 8rpx 20rpx;
  background: #f0fdf4;
  border-radius: 999rpx;
  border: 1rpx solid #bbf7d0;
}

.pickup-detail__confirmed-text {
  font-size: 24rpx;
  color: #16a34a;
  font-weight: 500;
}

.pickup-detail__price-hint {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
  margin-top: 16rpx;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
}

.pickup-detail__price-hint--blue {
  background: #eff6ff;
}

.pickup-detail__price-hint--orange {
  background: #fff7ed;
}

.pickup-detail__hint-icon {
  font-size: 24rpx;
  flex-shrink: 0;
}

.pickup-detail__hint-desc {
  font-size: 24rpx;
  color: #3b82f6;
}

.pickup-detail__price-hint--orange .pickup-detail__hint-desc {
  color: #d97706;
}

.pickup-detail__user-card {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.pickup-detail__avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.pickup-detail__user-info {
  flex: 1;
}

.pickup-detail__user-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.pickup-detail__user-role {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.pickup-detail__arrow {
  font-size: 32rpx;
  color: #ccc;
}

.pickup-detail__evidence {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.pickup-detail__evidence-img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  background: #f0f0f0;
}

.pickup-detail__empty-evidence {
  padding: 40rpx 0;
  text-align: center;
}

.pickup-detail__empty-evidence-text {
  font-size: 26rpx;
  color: #9ca3af;
}

.pickup-detail__evidence-hint {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f3f4f6;
}

.pickup-detail__hint-text {
  font-size: 24rpx;
  color: #ef4444;
}

.pickup-detail__evidence-submit {
  margin-top: 24rpx;
  height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #4A90D9, #3b7dd8);
  display: flex;
  align-items: center;
  justify-content: center;
}

.pickup-detail__evidence-submit-text {
  font-size: 30rpx;
  color: #fff;
  font-weight: 600;
}

.pickup-detail__cancel-reason {
  font-size: 26rpx;
  color: #666;
}

.pickup-detail__actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
}

.pickup-detail__btn {
  padding: 16rpx 36rpx;
  border-radius: 999rpx;
  font-size: 28rpx;
}

.pickup-detail__btn--primary {
  background: linear-gradient(135deg, #4A90D9, #3b7dd8);
  color: #fff;
}

.pickup-detail__btn--danger {
  background: #fff;
  color: #ff4d4f;
  border: 1rpx solid #ff4d4f;
}

.pickup-detail__btn--default {
  background: #fff;
  color: #333;
  border: 1rpx solid #ddd;
}
</style>
