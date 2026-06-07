<template>
  <view class="page">
    <view class="section">
      <text class="section-title">修改报酬</text>

      <!-- 当前价格 -->
      <view class="current-price">
        <text class="current-label">当前提议报酬</text>
        <text class="current-value">¥{{ Number(currentPrice).toFixed(2) }}</text>
      </view>

      <!-- 新价格输入 -->
      <view class="input-group">
        <text class="input-label">新的报酬金额</text>
        <view class="price-input">
          <text class="input-prefix">¥</text>
          <input
            class="input-field"
            type="digit"
            :value="newPrice"
            @input="onPriceInput"
            placeholder="请输入金额"
          />
        </view>
      </view>

      <!-- 期望送达时间 -->
      <view class="input-group">
        <text class="input-label">期望送达时间</text>
        <picker mode="date" :value="newDate" @change="onDateChange" :start="today">
          <view class="time-picker">
            <text class="picker-value" :class="{ 'picker-placeholder': !newDate }">{{ newDate || '选择日期' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
        <picker mode="time" :value="newTime" @change="onTimeChange">
          <view class="time-picker">
            <text class="picker-value" :class="{ 'picker-placeholder': !newTime }">{{ newTime || '选择时间' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 提示 -->
      <view class="tip-box">
        <text class="tip-text">💡 修改后可在聊天中发送给代拿者确认</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-area">
      <button class="btn-preview" :disabled="saving" @click="previewAndSend">
        {{ saving ? '保存中...' : '预览并发送给代拿者' }}
      </button>
      <button class="btn-save" :disabled="saving" @click="saveOnly">
        {{ saving ? '保存中...' : '仅保存修改' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { post } from '@/utils/request'
import { showToast } from '@/utils/nav'

const orderId = ref(null)
const currentPrice = ref(0)
const newPrice = ref('')
const newDate = ref('')
const newTime = ref('')
const pickerUserId = ref(null)
const saving = ref(false)

const today = (() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})()

onLoad((options) => {
  orderId.value = Number(options.orderId)
  currentPrice.value = Number(options.price) || 0
  newPrice.value = options.price || ''
  pickerUserId.value = options.pickerUserId ? Number(options.pickerUserId) : null

  if (options.expectedTime) {
    const d = new Date(decodeURIComponent(options.expectedTime).replace(/-/g, '/'))
    if (!isNaN(d.getTime())) {
      newDate.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      newTime.value = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    }
  }
})

function onPriceInput(e) {
  newPrice.value = e.detail.value
}

function onDateChange(e) {
  newDate.value = e.detail.value
}

function onTimeChange(e) {
  newTime.value = e.detail.value
}

function validate() {
  const price = parseFloat(newPrice.value)
  if (!price || price <= 0 || price > 100) {
    showToast('请输入有效金额（0.01-100）')
    return false
  }
  if (!newDate.value || !newTime.value) {
    showToast('请选择期望送达时间')
    return false
  }
  return true
}

async function callModifyApi() {
  const data = {
    orderId: orderId.value,
    proposedPrice: parseFloat(newPrice.value),
    expectedDeliveryTime: `${newDate.value} ${newTime.value}:00`
  }
  return await post('/mini/pickup/modify-price', data, { showLoading: true })
}

async function sendNegotiationCard() {
  if (!pickerUserId.value) return
  try {
    // 获取/创建聊天会话
    const sessionData = await post('/mini/chat/session/create', { peerId: pickerUserId.value }, { showLoading: false })
    if (!sessionData || !sessionData.sessionKey) return

    const cardData = JSON.stringify({
      orderId: orderId.value,
      proposedPrice: parseFloat(newPrice.value),
      expectedTime: `${newDate.value} ${newTime.value}`,
      confirmed: false
    })
    await post('/mini/chat/message/send', {
      sessionKey: sessionData.sessionKey,
      type: 10,
      content: cardData
    }, { showLoading: false })
  } catch (e) {
    console.error('发送协商卡片失败:', e)
  }
}

async function previewAndSend() {
  if (!validate() || saving.value) return
  saving.value = true
  try {
    await callModifyApi()

    // 发送协商卡片到聊天
    await sendNegotiationCard()

    showToast('已保存并发送')

    // 跳转到聊天页面
    if (pickerUserId.value) {
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/chat-sub/detail/detail?peerId=${pickerUserId.value}&pickupOrderId=${orderId.value}`
        })
      }, 800)
    } else {
      setTimeout(() => uni.navigateBack(), 800)
    }
  } catch (e) {
    /* handled by request util */
  } finally {
    saving.value = false
  }
}

async function saveOnly() {
  if (!validate() || saving.value) return
  saving.value = true
  try {
    await callModifyApi()
    showToast('已保存')
    setTimeout(() => uni.navigateBack(), 800)
  } catch (e) {
    /* handled by request util */
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 24rpx 32rpx;
}

.section {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 32rpx;
  color: #1f2937;
  font-weight: 600;
  margin-bottom: 32rpx;
  display: block;
}

.current-price {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background: #f9fafb;
  border-radius: 12rpx;
  margin-bottom: 32rpx;
}

.current-label {
  font-size: 26rpx;
  color: #6b7280;
}

.current-value {
  font-size: 36rpx;
  color: #ef4444;
  font-weight: 700;
}

.input-group {
  margin-bottom: 32rpx;
}

.input-label {
  font-size: 26rpx;
  color: #374151;
  font-weight: 500;
  margin-bottom: 12rpx;
  display: block;
}

.price-input {
  display: flex;
  align-items: center;
  border: 2rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
}

.input-prefix {
  font-size: 32rpx;
  color: #ef4444;
  font-weight: 600;
  margin-right: 12rpx;
}

.input-field {
  flex: 1;
  font-size: 32rpx;
  color: #1f2937;
}

.time-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 2rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  margin-top: 12rpx;
}

.picker-value {
  font-size: 28rpx;
  color: #1f2937;
}

.picker-placeholder {
  color: #9ca3af;
}

.picker-arrow {
  font-size: 28rpx;
  color: #d1d5db;
}

.tip-box {
  background: #eff6ff;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  margin-top: 8rpx;
}

.tip-text {
  font-size: 24rpx;
  color: #3b82f6;
}

.action-area {
  margin-top: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.btn-preview {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #4A90D9, #3b7dd8);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 44rpx;
  text-align: center;
  border: none;
}

.btn-preview::after {
  border: none;
}

.btn-preview[disabled] {
  opacity: 0.5;
}

.btn-save {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #ffffff;
  color: #6b7280;
  font-size: 30rpx;
  border-radius: 44rpx;
  text-align: center;
  border: 2rpx solid #e5e7eb;
}

.btn-save::after {
  border: none;
}

.btn-save[disabled] {
  opacity: 0.5;
}
</style>
