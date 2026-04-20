<template>
  <view class="history-page">
    <view v-if="loading" class="history-loading">加载中...</view>
    <view v-else-if="!historyList.length" class="history-empty">
      <empty-state title="暂无认证历史" description="提交认证后可在这里查看历史记录" />
    </view>
    <view v-else class="history-list">
      <view
        v-for="(item, index) in historyList"
        :key="item.id"
        class="history-card"
        :class="{ 'is-expired': index > 0 }"
        @click="goDetail(item.id)"
      >
        <view class="history-card__header">
          <text class="history-card__title">认证记录</text>
          <view class="history-card__status-group">
            <text v-if="index > 0" class="history-card__expired">已失效</text>
            <text class="history-card__status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
          </view>
        </view>
        <view class="history-card__row">
          <text class="history-card__label">姓名</text>
          <text class="history-card__value">{{ item.realName || '-' }}</text>
        </view>
        <view class="history-card__row">
          <text class="history-card__label">学院</text>
          <text class="history-card__value">{{ item.collegeName || '-' }}</text>
        </view>
        <view class="history-card__row">
          <text class="history-card__label">学号</text>
          <text class="history-card__value">{{ item.studentNo || '-' }}</text>
        </view>
        <view class="history-card__footer">
          <text class="history-card__time">{{ formatTime(item.createTime) }}</text>
          <text class="history-card__arrow">查看详情 ›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import { AUTH_STATUS_TEXT } from '@/utils/constant'
import EmptyState from '@/components/empty-state/empty-state.vue'

const loading = ref(false)
const historyList = ref([])

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

function goDetail(id) {
  if (!id) return
  uni.navigateTo({ url: `/pages/auth-sub/history/detail?id=${id}` })
}

async function loadHistory() {
  loading.value = true
  try {
    const data = await get('/mini/auth/history', {}, { showLoading: false })
    historyList.value = Array.isArray(data) ? data : []
  } catch (error) {
    historyList.value = []
    showToast((error && error.msg) || '加载失败')
  } finally {
    loading.value = false
  }
}

onShow(() => {
  loadHistory()
})
</script>

<style lang="scss" scoped>
.history-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  box-sizing: border-box;
}

.history-loading {
  color: var(--text-secondary);
  font-size: var(--font-sm);
  text-align: center;
  padding-top: 120rpx;
}

.history-empty {
  padding-top: 120rpx;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.history-card {
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
}

.history-card.is-expired {
  opacity: 0.78;
}

.history-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.history-card__title {
  color: var(--text-primary);
  font-size: var(--font-md);
  font-weight: 600;
}

.history-card__status {
  font-size: var(--font-xs);
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  color: var(--text-white);
}

.history-card__status-group {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.history-card__expired {
  font-size: var(--font-xs);
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  color: var(--text-secondary);
  background-color: var(--bg-grey);
}

.history-card__status.is-pending {
  background-color: var(--warning-color);
}

.history-card__status.is-verified {
  background-color: var(--success-color);
}

.history-card__status.is-rejected {
  background-color: var(--danger-color);
}

.history-card__row {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-sm);
  margin-top: 8rpx;
}

.history-card__label {
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

.history-card__value {
  color: var(--text-primary);
  font-size: var(--font-sm);
}

.history-card__footer {
  margin-top: var(--spacing-sm);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.history-card__time {
  color: var(--text-placeholder);
  font-size: var(--font-xs);
}

.history-card__arrow {
  color: var(--primary-color);
  font-size: var(--font-sm);
}
</style>
