<template>
  <view class="pickup-publish">
    <view class="pickup-publish__content">
      <view class="pickup-publish__card">
        <view class="pickup-publish__field">
          <text class="pickup-publish__label">取件信息</text>
          <input
            class="pickup-publish__input"
            :value="form.pickupCode"
            placeholder="取件码/快递单号（选填）"
            placeholder-class="pickup-publish__placeholder"
            maxlength="50"
            @input="onInput('pickupCode', $event)"
          />
        </view>

        <view class="pickup-publish__field">
          <text class="pickup-publish__label">取件地点</text>
          <input
            class="pickup-publish__input"
            :value="form.pickupLocation"
            placeholder="如：菜鸟驿站、西门快递柜"
            placeholder-class="pickup-publish__placeholder"
            maxlength="50"
            @input="onInput('pickupLocation', $event)"
          />
        </view>

        <view class="pickup-publish__field">
          <text class="pickup-publish__label">送达地点</text>
          <input
            class="pickup-publish__input"
            :value="form.deliveryLocation"
            placeholder="如：宿舍楼栋、教学楼"
            placeholder-class="pickup-publish__placeholder"
            maxlength="50"
            @input="onInput('deliveryLocation', $event)"
          />
        </view>

        <view class="pickup-publish__field pickup-publish__field--textarea">
          <text class="pickup-publish__label">补充说明（选填）</text>
          <textarea
            class="pickup-publish__textarea"
            :value="form.pickupDetail"
            placeholder="快递大小、注意事项等"
            placeholder-class="pickup-publish__placeholder"
            maxlength="200"
            @input="onInput('pickupDetail', $event)"
          />
          <text class="pickup-publish__counter">{{ detailCount }}/200</text>
        </view>
      </view>

      <view class="pickup-publish__card">
        <view class="pickup-publish__row">
          <view class="pickup-publish__field pickup-publish__field--half">
            <text class="pickup-publish__label">期望送达时间</text>
            <picker mode="time" @change="onTimeChange">
              <view class="pickup-publish__picker">
                <text class="pickup-publish__picker-text">{{ timeLabel }}</text>
                <text class="pickup-publish__picker-icon">›</text>
              </view>
            </picker>
          </view>
          <view class="pickup-publish__field pickup-publish__field--half">
            <text class="pickup-publish__label">期望送达日期</text>
            <picker mode="date" @change="onDateChange">
              <view class="pickup-publish__picker">
                <text class="pickup-publish__picker-text">{{ dateLabel }}</text>
                <text class="pickup-publish__picker-icon">›</text>
              </view>
            </picker>
          </view>
        </view>

        <view class="pickup-publish__field">
          <text class="pickup-publish__label">提议报酬</text>
          <view class="pickup-publish__price-row">
            <text class="pickup-publish__price-symbol">¥</text>
            <input
              class="pickup-publish__input pickup-publish__input--price"
              :value="form.proposedPrice"
              type="digit"
              placeholder="建议 2-10 元"
              placeholder-class="pickup-publish__placeholder"
              @input="onInput('proposedPrice', $event)"
            />
          </view>
        </view>
      </view>
    </view>

    <view class="pickup-publish__footer safe-area-bottom">
      <view
        class="pickup-publish__submit"
        :class="{ 'pickup-publish__submit--disabled': submitting }"
        @click="handleSubmit"
      >
        <text class="pickup-publish__submit-text">{{ submitting ? '发布中...' : '发布需求' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { post } from '@/utils/request'
import { useAppStore } from '@/store/app'
import { showToast } from '@/utils/nav'

const appStore = useAppStore()

function getInitialForm() {
  return {
    pickupCode: '',
    pickupLocation: '',
    deliveryLocation: '',
    pickupDetail: '',
    expectedDeliveryTime: '',
    proposedPrice: ''
  }
}

const form = ref(getInitialForm())
const selectedDate = ref('')
const selectedTime = ref('')
const submitting = ref(false)

const detailCount = computed(() => form.value.pickupDetail.length)

const dateLabel = computed(() => selectedDate.value || '请选择日期')
const timeLabel = computed(() => selectedTime.value || '请选择时间')

function onInput(field, e) {
  form.value[field] = String((e && e.detail && e.detail.value) || '')
}

function onDateChange(e) {
  selectedDate.value = e.detail.value
  buildExpectedTime()
}

function onTimeChange(e) {
  selectedTime.value = e.detail.value
  buildExpectedTime()
}

function buildExpectedTime() {
  if (selectedDate.value && selectedTime.value) {
    form.value.expectedDeliveryTime = `${selectedDate.value} ${selectedTime.value}:00`
  }
}

function validateForm() {
  if (!form.value.pickupLocation.trim()) return '请输入取件地点'
  if (!form.value.deliveryLocation.trim()) return '请输入送达地点'
  if (!form.value.expectedDeliveryTime) return '请选择期望送达时间'
  const price = Number(form.value.proposedPrice)
  if (!price || price <= 0) return '请输入有效的报酬金额'
  if (price > 100) return '报酬金额不能超过100元'
  return ''
}

async function handleSubmit() {
  if (submitting.value) return
  const error = validateForm()
  if (error) {
    showToast(error)
    return
  }
  submitting.value = true
  try {
    const campusId = appStore.currentCampusId
    const payload = {
      pickupCode: form.value.pickupCode.trim(),
      pickupLocation: form.value.pickupLocation.trim(),
      deliveryLocation: form.value.deliveryLocation.trim(),
      pickupDetail: form.value.pickupDetail.trim(),
      expectedDeliveryTime: form.value.expectedDeliveryTime,
      proposedPrice: Number(form.value.proposedPrice),
      campusId
    }
    await post('/mini/pickup/create', payload, { showLoading: true })
    showToast('发布成功')
    setTimeout(() => {
      uni.navigateBack()
    }, 500)
  } catch (e) {
    showToast('发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.pickup-publish {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.pickup-publish__content {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.pickup-publish__card {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
}

.pickup-publish__field {
  margin-bottom: 24rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.pickup-publish__label {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 12rpx;
  display: block;
}

.pickup-publish__input {
  width: 100%;
  height: 88rpx;
  border: 2rpx solid #e8e8e8;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.pickup-publish__placeholder {
  color: #bbb;
}

.pickup-publish__field--textarea {
  .pickup-publish__input {
    display: none;
  }
}

.pickup-publish__textarea {
  width: 100%;
  height: 180rpx;
  border: 2rpx solid #e8e8e8;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.pickup-publish__counter {
  font-size: 22rpx;
  color: #999;
  text-align: right;
  margin-top: 8rpx;
  display: block;
}

.pickup-publish__row {
  display: flex;
  gap: 24rpx;
}

.pickup-publish__field--half {
  flex: 1;
}

.pickup-publish__picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  border: 2rpx solid #e8e8e8;
  border-radius: 16rpx;
  padding: 0 24rpx;
}

.pickup-publish__picker-text {
  font-size: 28rpx;
  color: #333;
}

.pickup-publish__picker-icon {
  font-size: 32rpx;
  color: #999;
}

.pickup-publish__price-row {
  display: flex;
  align-items: center;
  border: 2rpx solid #e8e8e8;
  border-radius: 16rpx;
  padding: 0 24rpx;
  height: 88rpx;
}

.pickup-publish__price-symbol {
  font-size: 32rpx;
  color: #ff6b35;
  font-weight: 600;
  margin-right: 8rpx;
}

.pickup-publish__input--price {
  border: none;
  height: 100%;
  padding: 0;
  font-size: 36rpx;
  font-weight: 600;
}

.pickup-publish__footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx;
  background-color: #fff;
  border-top: 1rpx solid #eee;
}

.pickup-publish__submit {
  height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #4A90D9, #3b7dd8);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 217, 0.3);
}

.pickup-publish__submit--disabled {
  opacity: 0.5;
}

.pickup-publish__submit-text {
  font-size: 30rpx;
  color: #fff;
  font-weight: 600;
}
</style>
