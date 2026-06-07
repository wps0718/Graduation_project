<template>
  <view class="dispute-submit">
    <view class="dispute-submit__content">
      <view class="dispute-submit__card">
        <text class="dispute-submit__label">纠纷类型</text>
        <view class="dispute-submit__types">
          <view
            v-for="type in disputeTypes"
            :key="type.value"
            class="dispute-submit__type-item"
            :class="{ 'dispute-submit__type-item--active': form.disputeType === type.value }"
            @click="form.disputeType = type.value"
          >
            <text class="dispute-submit__type-text">{{ type.label }}</text>
          </view>
        </view>
      </view>

      <view class="dispute-submit__card">
        <text class="dispute-submit__label">申诉描述</text>
        <textarea
          class="dispute-submit__textarea"
          :value="form.description"
          placeholder="请详细描述纠纷情况..."
          placeholder-class="dispute-submit__placeholder"
          maxlength="500"
          @input="onDescriptionInput"
        />
        <text class="dispute-submit__counter">{{ form.description.length }}/500</text>
      </view>

      <view class="dispute-submit__card">
        <text class="dispute-submit__label">证据材料（至少1项）</text>
        <EvidenceUpload
          type="both"
          :max-images="6"
          :max-texts="3"
          :show-description="true"
          @change="onEvidenceChange"
        />
      </view>
    </view>

    <view class="dispute-submit__footer safe-area-bottom">
      <view
        class="dispute-submit__submit"
        :class="{ 'dispute-submit__submit--disabled': submitting }"
        @click="handleSubmit"
      >
        <text>{{ submitting ? '提交中...' : '提交纠纷' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { post } from '@/utils/request'
import { PICKUP_DISPUTE_TYPES } from '@/utils/constant'
import EvidenceUpload from '@/components/evidence-upload/evidence-upload.vue'
import { showToast } from '@/utils/nav'

const disputeTypes = PICKUP_DISPUTE_TYPES
const orderId = ref(null)
const submitting = ref(false)

const form = ref({
  disputeType: null,
  description: ''
})

const evidenceData = ref({ images: [], texts: [] })

function onDescriptionInput(e) {
  form.value.description = String(e?.detail?.value || '')
}

function onEvidenceChange(data) {
  evidenceData.value = data
}

function validateForm() {
  if (!form.value.disputeType) return '请选择纠纷类型'
  if (!form.value.description.trim()) return '请填写申诉描述'
  const images = evidenceData.value.images || []
  const texts = evidenceData.value.texts || []
  if (images.length === 0 && texts.length === 0) return '请至少提供一项证据'
  return ''
}

async function handleSubmit() {
  if (submitting.value) return
  const error = validateForm()
  if (error) { showToast(error); return }
  submitting.value = true
  try {
    const images = evidenceData.value.images || []
    const texts = evidenceData.value.texts || []
    const evidenceMaterials = []
    images.forEach((item) => {
      evidenceMaterials.push({
        type: '1',
        url: item.url,
        description: item.description || ''
      })
    })
    texts.forEach((item) => {
      if (item.content && item.content.trim()) {
        evidenceMaterials.push({
          type: '2',
          content: item.content.trim()
        })
      }
    })
    await post('/mini/pickup/dispute/submit', {
      orderId: orderId.value,
      disputeType: form.value.disputeType,
      description: form.value.description.trim(),
      evidenceMaterials
    }, { showLoading: true })
    showToast('纠纷已提交')
    setTimeout(() => {
      uni.navigateBack()
    }, 500)
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

onLoad((options) => {
  orderId.value = options?.orderId ? Number(options.orderId) : null
  if (!orderId.value) showToast('订单ID无效')
})
</script>

<style lang="scss" scoped>
.dispute-submit {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.dispute-submit__content {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.dispute-submit__card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
}

.dispute-submit__label {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.dispute-submit__types {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.dispute-submit__type-item {
  padding: 12rpx 28rpx;
  border-radius: 32rpx;
  background: #f5f5f5;
  border: 2rpx solid transparent;
}

.dispute-submit__type-item--active {
  background: #fff0f0;
  border-color: #ff4d4f;
}

.dispute-submit__type-text {
  font-size: 26rpx;
  color: #666;
}

.dispute-submit__type-item--active .dispute-submit__type-text {
  color: #ff4d4f;
  font-weight: 500;
}

.dispute-submit__textarea {
  width: 100%;
  height: 240rpx;
  border: 2rpx solid #e8e8e8;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.dispute-submit__placeholder {
  color: #bbb;
}

.dispute-submit__counter {
  font-size: 22rpx;
  color: #999;
  text-align: right;
  margin-top: 8rpx;
  display: block;
}

.dispute-submit__footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
}

.dispute-submit__submit {
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

.dispute-submit__submit--disabled {
  opacity: 0.5;
}
</style>
