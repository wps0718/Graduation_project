<template>
  <view class="report-page">
    <view class="report-card">
      <text class="section-title">举报原因</text>
      <radio-group class="reason-group" @change="onReasonChange">
        <label v-for="item in reasonOptions" :key="item.value" class="reason-item">
          <radio class="reason-radio" :value="String(item.value)" :checked="selectedReason === item.value" color="var(--primary-color)" />
          <text class="reason-text">{{ item.label }}</text>
        </label>
      </radio-group>
    </view>

    <view class="report-card">
      <text class="section-title">补充说明</text>
      <view class="textarea-wrap">
        <textarea
          v-model="description"
          class="textarea"
          maxlength="255"
          placeholder="请描述具体情况..."
        />
        <view class="textarea-count">
          <text>{{ descriptionCount }}/255</text>
        </view>
      </view>
    </view>

    <view class="report-footer safe-area-bottom">
      <button class="submit-btn" :loading="submitting" :disabled="submitting" @click="submitReport">
        提交举报
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { post } from '@/utils/request'
import { useUserStore } from '@/store'

const userStore = useUserStore()

const targetType = ref(null)
const targetId = ref(null)
const selectedReason = ref(null)
const description = ref('')
const submitting = ref(false)

const reasonOptions = [
  { value: 1, label: '虚假商品' },
  { value: 2, label: '违禁物品' },
  { value: 3, label: '诈骗信息' },
  { value: 4, label: '不当内容' },
  { value: 5, label: '其他' }
]

const descriptionCount = computed(() => description.value.length)

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

function onReasonChange(event) {
  const value = Number((event && event.detail && event.detail.value) || 0)
  selectedReason.value = value || null
}

function resolveTarget(options) {
  if (options && options.targetType && options.targetId) {
    targetType.value = Number(options.targetType)
    targetId.value = Number(options.targetId)
    return
  }
  if (options && options.productId) {
    targetType.value = 1
    targetId.value = Number(options.productId)
    return
  }
  if (options && options.userId) {
    targetType.value = 2
    targetId.value = Number(options.userId)
  }
}

async function submitReport() {
  if (submitting.value) return
  if (!targetType.value || !targetId.value) {
    showToast('举报对象不存在')
    return
  }
  if (!selectedReason.value) {
    showToast('请选择举报原因')
    return
  }
  submitting.value = true
  try {
    await post(
      '/mini/report/submit',
      {
        targetType: targetType.value,
        targetId: targetId.value,
        reasonType: selectedReason.value,
        description: description.value.trim()
      },
      { showLoading: true }
    )
    showToast('举报已提交，我们会尽快处理')
    setTimeout(() => {
      uni.navigateBack()
    }, 400)
  } catch (error) {
    showToast('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onLoad((options = {}) => {
  if (!ensureLogin()) return
  resolveTarget(options)
})
</script>

<style lang="scss" scoped>
.report-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  padding-bottom: 140rpx;
  box-sizing: border-box;
}

.report-card {
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.section-title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
}

.reason-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.reason-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 12rpx 0;
  border-bottom: 1rpx solid var(--border-light);
}

.reason-item:last-child {
  border-bottom: none;
}

.reason-radio {
  transform: scale(0.9);
}

.reason-text {
  font-size: var(--font-sm);
  color: var(--text-regular);
}

.textarea-wrap {
  margin-top: var(--spacing-sm);
  background-color: var(--bg-grey);
  border-radius: var(--radius-md);
  padding: var(--spacing-sm);
}

.textarea {
  width: 100%;
  min-height: 200rpx;
  font-size: var(--font-sm);
  color: var(--text-primary);
}

.textarea-count {
  display: flex;
  justify-content: flex-end;
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.report-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  box-shadow: 0 -6rpx 20rpx rgba(15, 23, 42, 0.08);
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, var(--primary-light), var(--primary-color));
  color: var(--text-white);
  font-size: var(--font-md);
  font-weight: 600;
}

.submit-btn[disabled] {
  background: var(--border-color);
  color: var(--text-secondary);
}
</style>
