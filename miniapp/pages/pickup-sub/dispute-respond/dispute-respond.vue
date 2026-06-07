<template>
  <view class="dispute-respond" v-if="dispute">
    <!-- 纠纷状态 -->
    <view class="dispute-respond__header">
      <StatusTag type="dispute" :value="dispute.status" />
      <text class="dispute-respond__header-text">{{ statusText }}</text>
    </view>

    <!-- 申诉方信息 -->
    <view class="dispute-respond__section">
      <text class="dispute-respond__section-title">申诉方</text>
      <view class="dispute-respond__user">
        <image class="dispute-respond__avatar" :src="dispute.initiatorAvatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="dispute-respond__user-info">
          <text class="dispute-respond__name">{{ dispute.initiatorNickName }}</text>
          <text class="dispute-respond__role">{{ dispute.initiatorRole === 1 ? '需求者' : '代拿者' }}</text>
        </view>
      </view>
      <view class="dispute-respond__type">
        <text class="dispute-respond__type-label">纠纷类型：</text>
        <text class="dispute-respond__type-value">{{ disputeTypeText }}</text>
      </view>
      <view class="dispute-respond__desc">
        <text class="dispute-respond__desc-label">申诉描述：</text>
        <text>{{ dispute.description }}</text>
      </view>
      <text class="dispute-respond__time">提交时间：{{ formatTime(dispute.submitTime) }}</text>
    </view>

    <!-- 申诉方证据 -->
    <view v-if="dispute.initiatorEvidence && dispute.initiatorEvidence.length" class="dispute-respond__section">
      <text class="dispute-respond__section-title">申诉方证据</text>
      <view class="dispute-respond__evidence">
        <view v-for="(item, i) in dispute.initiatorEvidence" :key="i" class="dispute-respond__evidence-item">
          <image
            v-if="item.type === 1 && item.url"
            class="dispute-respond__evidence-img"
            :src="item.url"
            mode="aspectFill"
            @click="previewImage(dispute.initiatorEvidence, i)"
          />
          <view v-else-if="item.type === 2" class="dispute-respond__evidence-text">
            <text>{{ item.content }}</text>
          </view>
          <text v-if="item.type === 1 && item.description" class="dispute-respond__evidence-desc">{{ item.description }}</text>
        </view>
      </view>
    </view>

    <!-- 被申诉方回应 -->
    <view v-if="dispute.responseDescription" class="dispute-respond__section">
      <text class="dispute-respond__section-title">回应方</text>
      <view class="dispute-respond__user">
        <image class="dispute-respond__avatar" :src="dispute.responderAvatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="dispute-respond__user-info">
          <text class="dispute-respond__name">{{ dispute.responderNickName }}</text>
        </view>
      </view>
      <view class="dispute-respond__desc">
        <text class="dispute-respond__desc-label">回应描述：</text>
        <text>{{ dispute.responseDescription }}</text>
      </view>
      <text class="dispute-respond__time">回应时间：{{ formatTime(dispute.responseTime) }}</text>
    </view>

    <!-- 被申诉方证据 -->
    <view v-if="dispute.responderEvidence && dispute.responderEvidence.length" class="dispute-respond__section">
      <text class="dispute-respond__section-title">回应方证据</text>
      <view class="dispute-respond__evidence">
        <view v-for="(item, i) in dispute.responderEvidence" :key="i" class="dispute-respond__evidence-item">
          <image
            v-if="item.type === 1 && item.url"
            class="dispute-respond__evidence-img"
            :src="item.url"
            mode="aspectFill"
            @click="previewImage(dispute.responderEvidence, i)"
          />
          <view v-else-if="item.type === 2" class="dispute-respond__evidence-text">
            <text>{{ item.content }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 平台裁决结果 -->
    <view v-if="dispute.status === 3 && dispute.judgmentResult" class="dispute-respond__section">
      <text class="dispute-respond__section-title">平台裁决</text>
      <view class="dispute-respond__judgment">
        <text class="dispute-respond__judgment-result">{{ dispute.judgmentResult === 1 ? '申诉方胜诉' : '被申诉方胜诉' }}</text>
        <text class="dispute-respond__judgment-detail">{{ dispute.judgmentDetail || '' }}</text>
        <text v-if="dispute.penaltyScore" class="dispute-respond__judgment-penalty">信用分扣除：{{ dispute.penaltyScore }}</text>
      </view>
    </view>

    <!-- 截止时间提示 -->
    <view v-if="isResponder && dispute.status === 0 && deadlineText" class="dispute-respond__deadline">
      <text class="dispute-respond__deadline-icon">⏰</text>
      <text>回应截止：{{ deadlineText }}</text>
    </view>

    <!-- 回应表单（仅被申诉方可填写） -->
    <view v-if="isResponder && dispute.status === 0" class="dispute-respond__section">
      <text class="dispute-respond__section-title">提交回应</text>
      <textarea
        class="dispute-respond__input"
        :value="responseForm.description"
        placeholder="请描述您的回应..."
        placeholder-class="dispute-respond__placeholder"
        maxlength="500"
        @input="onDescriptionInput"
      />
      <text class="dispute-respond__counter">{{ responseForm.description.length }}/500</text>
      <text class="dispute-respond__evidence-label" style="margin-top: 20rpx; display: block;">证据材料（至少1项）</text>
      <EvidenceUpload
        type="both"
        :max-images="6"
        :max-texts="3"
        :show-description="true"
        @change="onEvidenceChange"
      />
    </view>

    <!-- 撤回按钮 -->
    <view v-if="isInitiator && dispute.status === 0" class="dispute-respond__section">
      <view class="dispute-respond__withdraw" @click="handleWithdraw">
        <text>撤回纠纷</text>
      </view>
    </view>

    <!-- 底部操作 -->
    <view v-if="isResponder && dispute.status === 0" class="dispute-respond__footer safe-area-bottom">
      <view
        class="dispute-respond__submit"
        :class="{ 'dispute-respond__submit--disabled': submitting }"
        @click="handleSubmit"
      >
        <text>{{ submitting ? '提交中...' : '提交回应' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { PICKUP_DISPUTE_TYPES } from '@/utils/constant'
import { useUserStore } from '@/store'
import StatusTag from '@/components/status-tag/status-tag.vue'
import EvidenceUpload from '@/components/evidence-upload/evidence-upload.vue'
import { showToast } from '@/utils/nav'

const userStore = useUserStore()
const dispute = ref(null)
const submitting = ref(false)
const responseForm = ref({ description: '' })
const evidenceData = ref({ images: [], texts: [] })

const selfId = computed(() => {
  const info = userStore.userInfo
  return info && info.id ? Number(info.id) : 0
})

const isInitiator = computed(() => dispute.value && selfId.value === dispute.value.initiatorId)
const isResponder = computed(() => dispute.value && selfId.value === dispute.value.responderId)

const statusText = computed(() => {
  if (!dispute.value) return ''
  const map = { 0: '待回应', 1: '已回应，等待裁决', 2: '对方未回应，自动胜诉', 3: '已裁决', 4: '已撤回' }
  return map[dispute.value.status] || ''
})

const disputeTypeText = computed(() => {
  if (!dispute.value) return ''
  const found = PICKUP_DISPUTE_TYPES.find((t) => t.value === dispute.value.disputeType)
  return found ? found.label : '未知'
})

const deadlineText = computed(() => {
  if (!dispute.value || !dispute.value.responseDeadline) return ''
  const deadline = new Date(dispute.value.responseDeadline.replace('T', ' ').replace(/-/g, '/'))
  const now = new Date()
  const diff = deadline.getTime() - now.getTime()
  if (diff <= 0) return '已超时'
  const days = Math.floor(diff / 86400000)
  const hours = Math.floor((diff % 86400000) / 3600000)
  if (days > 0) return `${days}天${hours}小时`
  return `${hours}小时`
})

function formatTime(value) {
  if (!value) return ''
  const d = new Date(String(value).replace(/-/g, '/'))
  if (isNaN(d.getTime())) return value
  const M = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  const h = `${d.getHours()}`.padStart(2, '0')
  const m = `${d.getMinutes()}`.padStart(2, '0')
  return `${M}-${day} ${h}:${m}`
}

function previewImage(evidenceList, index) {
  const images = evidenceList.filter((e) => e.type === 1 && e.url).map((e) => e.url)
  if (images.length) {
    const imgItems = evidenceList.filter((e) => e.type === 1 && e.url)
    const currentImg = imgItems[index]
    if (currentImg) {
      uni.previewImage({ urls: images, current: currentImg.url })
    }
  }
}

function onDescriptionInput(e) {
  responseForm.value.description = String(e?.detail?.value || '')
}

function onEvidenceChange(data) {
  evidenceData.value = data
}

async function fetchDispute(orderId) {
  try {
    const data = await get(`/mini/pickup/dispute/detail/${orderId}`, {}, { showLoading: true })
    if (data) dispute.value = data
  } catch (e) { showToast('加载失败') }
}

async function handleSubmit() {
  if (submitting.value) return
  if (!responseForm.value.description.trim()) { showToast('请填写回应描述'); return }
  const images = evidenceData.value.images || []
  const texts = evidenceData.value.texts || []
  if (images.length === 0 && texts.length === 0) { showToast('请至少提供一项证据'); return }
  submitting.value = true
  try {
    const responseMaterials = []
    images.forEach((item) => {
      responseMaterials.push({ type: '1', url: item.url, description: item.description || '' })
    })
    texts.forEach((item) => {
      if (item.content && item.content.trim()) {
        responseMaterials.push({ type: '2', content: item.content.trim() })
      }
    })
    await post('/mini/pickup/dispute/respond', {
      disputeId: dispute.value.id,
      responseDescription: responseForm.value.description.trim(),
      responseMaterials
    }, { showLoading: true })
    showToast('回应已提交')
    setTimeout(() => {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      fetchDispute(currentPage?.options?.orderId)
    }, 500)
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

async function handleWithdraw() {
  uni.showModal({
    title: '撤回纠纷',
    content: '确定撤回该纠纷？撤回后不可重新提交。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/pickup/dispute/withdraw', { disputeId: dispute.value.id })
        showToast('已撤回')
        setTimeout(() => uni.navigateBack(), 500)
      } catch (e) { /* handled */ }
    }
  })
}

onLoad((options) => {
  const orderId = options?.orderId ? Number(options.orderId) : null
  if (orderId) {
    fetchDispute(orderId)
  } else {
    showToast('订单ID无效')
  }
})
</script>

<style lang="scss" scoped>
.dispute-respond {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 180rpx;
}

.dispute-respond__header {
  background: linear-gradient(135deg, #ff6b6b, #d9363e);
  padding: 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.dispute-respond__header-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: 600;
}

.dispute-respond__section {
  background: #fff;
  margin: 20rpx 24rpx 0;
  border-radius: 20rpx;
  padding: 24rpx;
}

.dispute-respond__section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
}

.dispute-respond__user {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.dispute-respond__avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.dispute-respond__user-info {
  flex: 1;
}

.dispute-respond__name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.dispute-respond__role {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.dispute-respond__type {
  margin-bottom: 12rpx;
}

.dispute-respond__type-label,
.dispute-respond__desc-label {
  font-size: 26rpx;
  color: #999;
}

.dispute-respond__type-value {
  font-size: 26rpx;
  color: #ff4d4f;
  font-weight: 500;
}

.dispute-respond__desc {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12rpx;
}

.dispute-respond__time {
  font-size: 22rpx;
  color: #999;
  display: block;
}

.dispute-respond__evidence {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.dispute-respond__evidence-item {
  width: 200rpx;
}

.dispute-respond__evidence-img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  background: #f0f0f0;
}

.dispute-respond__evidence-text {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 24rpx;
  color: #333;
  line-height: 1.5;
}

.dispute-respond__evidence-desc {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.dispute-respond__judgment {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 24rpx;
}

.dispute-respond__judgment-result {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.dispute-respond__judgment-detail {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  display: block;
  margin-bottom: 12rpx;
}

.dispute-respond__judgment-penalty {
  font-size: 24rpx;
  color: #ff4d4f;
  display: block;
}

.dispute-respond__deadline {
  margin: 20rpx 24rpx 0;
  background: #fff7e6;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 26rpx;
  color: #d46b08;
}

.dispute-respond__deadline-icon {
  font-size: 24rpx;
}

.dispute-respond__input {
  width: 100%;
  height: 200rpx;
  border: 2rpx solid #e8e8e8;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.dispute-respond__placeholder {
  color: #bbb;
}

.dispute-respond__counter {
  font-size: 22rpx;
  color: #999;
  text-align: right;
  margin-top: 8rpx;
  display: block;
}

.dispute-respond__evidence-label {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.dispute-respond__withdraw {
  background: #fff;
  border: 1rpx solid #ff4d4f;
  border-radius: 999rpx;
  padding: 20rpx 0;
  text-align: center;
  color: #ff4d4f;
  font-size: 28rpx;
  font-weight: 500;
}

.dispute-respond__footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
}

.dispute-respond__submit {
  height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ff4d4f, #d9363e);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.3);
}

.dispute-respond__submit--disabled {
  opacity: 0.5;
}
</style>
