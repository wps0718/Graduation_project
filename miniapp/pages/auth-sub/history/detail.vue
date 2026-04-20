<template>
  <view class="history-detail-page">
    <view v-if="loading" class="history-detail-loading">加载中...</view>
    <view v-else-if="detail" class="history-detail-card">
      <view class="history-detail__header">
        <text class="history-detail__title">认证详情</text>
        <text class="history-detail__status" :class="statusClass(detail.status)">{{ statusText(detail.status) }}</text>
      </view>

      <view class="history-detail__row">
        <text class="history-detail__label">提交时间</text>
        <text class="history-detail__value">{{ formatTime(detail.createTime) || '-' }}</text>
      </view>
      <view class="history-detail__row">
        <text class="history-detail__label">审核时间</text>
        <text class="history-detail__value">{{ formatTime(detail.reviewTime) || '-' }}</text>
      </view>
      <view class="history-detail__row">
        <text class="history-detail__label">姓名</text>
        <text class="history-detail__value">{{ detail.realName || '-' }}</text>
      </view>
      <view class="history-detail__row">
        <text class="history-detail__label">学院</text>
        <text class="history-detail__value">{{ detail.collegeName || '-' }}</text>
      </view>
      <view class="history-detail__row">
        <text class="history-detail__label">学号</text>
        <text class="history-detail__value">{{ detail.studentNo || '-' }}</text>
      </view>
      <view class="history-detail__row">
        <text class="history-detail__label">班级</text>
        <text class="history-detail__value">{{ detail.className || '-' }}</text>
      </view>

      <view class="history-detail__material">
        <text class="history-detail__label">认证材料</text>
        <view class="history-detail__material-box">
          <image v-if="detail.certImage" :src="detail.certImage" class="history-detail__material-image" mode="aspectFill" />
          <view v-else class="history-detail__material-empty">暂无图片</view>
        </view>
      </view>

      <view v-if="detail.status === 3 && detail.rejectReason" class="history-detail__reject">
        <text class="history-detail__reject-title">审核失败原因</text>
        <text class="history-detail__reject-text">{{ detail.rejectReason }}</text>
      </view>

      <view class="history-detail__submit" @click="goEdit">修改资料</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import { AUTH_STATUS_TEXT } from '@/utils/constant'
import { resolveImageUrl } from '@/utils/image'

const loading = ref(false)
const detail = ref(null)
const historyId = ref(null)

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function formatTime(value) {
  if (!value) return ''
  const text = String(value)
  return text.replace('T', ' ').slice(0, 19)
}

function statusText(status) {
  return AUTH_STATUS_TEXT[status] || '未知状态'
}

function statusClass(status) {
  if (status === 1) return 'is-pending'
  if (status === 2) return 'is-verified'
  if (status === 3) return 'is-rejected'
  return ''
}

function goEdit() {
  if (!detail.value) return
  uni.setStorageSync('authEditDraft', {
    id: detail.value.id,
    collegeId: detail.value.collegeId,
    collegeName: detail.value.collegeName,
    realName: detail.value.realName,
    studentNo: detail.value.studentNo,
    className: detail.value.className,
    certImage: detail.value.certImage
  })
  uni.navigateTo({ url: '/pages/auth-sub/auth/auth?edit=1' })
}

async function loadDetail() {
  if (!historyId.value) return
  loading.value = true
  try {
    const data = await get(`/mini/auth/history/${historyId.value}`, {}, { showLoading: false })
    detail.value = {
      ...(data || {}),
      certImage: resolveImageUrl((data && data.certImage) || '')
    }
  } catch (error) {
    showToast((error && error.msg) || '加载失败')
    detail.value = null
  } finally {
    loading.value = false
  }
}

onLoad((options) => {
  historyId.value = options && options.id ? Number(options.id) : null
  loadDetail()
})
</script>

<style lang="scss" scoped>
.history-detail-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  box-sizing: border-box;
}

.history-detail-loading {
  color: var(--text-secondary);
  font-size: var(--font-sm);
  text-align: center;
  padding-top: 120rpx;
}

.history-detail-card {
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
}

.history-detail__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.history-detail__title {
  color: var(--text-primary);
  font-size: var(--font-md);
  font-weight: 600;
}

.history-detail__status {
  font-size: var(--font-xs);
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  color: var(--text-white);
}

.history-detail__status.is-pending {
  background-color: var(--warning-color);
}

.history-detail__status.is-verified {
  background-color: var(--success-color);
}

.history-detail__status.is-rejected {
  background-color: var(--danger-color);
}

.history-detail__row {
  display: flex;
  justify-content: space-between;
  margin-top: 10rpx;
  gap: var(--spacing-sm);
}

.history-detail__label {
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

.history-detail__value {
  color: var(--text-primary);
  font-size: var(--font-sm);
}

.history-detail__material {
  margin-top: var(--spacing-md);
}

.history-detail__material-box {
  margin-top: var(--spacing-sm);
  width: 100%;
  height: 320rpx;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background-color: var(--bg-grey);
}

.history-detail__material-image {
  width: 100%;
  height: 100%;
}

.history-detail__material-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-placeholder);
  font-size: var(--font-sm);
}

.history-detail__reject {
  margin-top: var(--spacing-md);
  background-color: rgba(255, 77, 79, 0.08);
  border-radius: var(--radius-md);
  padding: 12rpx 16rpx;
}

.history-detail__reject-title {
  color: var(--danger-color);
  font-size: var(--font-sm);
  font-weight: 600;
}

.history-detail__reject-text {
  margin-top: 6rpx;
  color: var(--danger-color);
  font-size: var(--font-sm);
}

.history-detail__submit {
  margin-top: var(--spacing-lg);
  height: 88rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-md);
  color: var(--text-white);
  background-color: var(--primary-color);
}
</style>

