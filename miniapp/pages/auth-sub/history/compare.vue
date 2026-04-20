<template>
  <view class="compare-page">
    <view v-if="loading" class="compare-loading">加载中...</view>
    <view v-else class="compare-content">
      <view class="compare-section">
        <view class="compare-section__header">
          <text class="compare-section__title">待审核内容</text>
          <text class="compare-section__status is-pending">{{ pendingRecord ? '待审核' : '暂无' }}</text>
        </view>
        <view v-if="pendingRecord" class="compare-card" @click="goDetail(pendingRecord.id)">
          <view class="compare-row"><text>姓名</text><text>{{ pendingRecord.realName || '-' }}</text></view>
          <view class="compare-row"><text>学院</text><text>{{ pendingRecord.collegeName || '-' }}</text></view>
          <view class="compare-row"><text>学号</text><text>{{ pendingRecord.studentNo || '-' }}</text></view>
          <view class="compare-row"><text>班级</text><text>{{ pendingRecord.className || '-' }}</text></view>
          <text class="compare-link">点击查看完整详情 ›</text>
        </view>
        <view v-else class="compare-empty">暂无待审核记录</view>
      </view>

      <view class="compare-section">
        <view class="compare-section__header">
          <text class="compare-section__title">已通过内容</text>
          <text class="compare-section__status is-verified">{{ verifiedRecord ? '已通过' : '暂无' }}</text>
        </view>
        <view v-if="verifiedRecord" class="compare-card" @click="goDetail(verifiedRecord.id)">
          <view class="compare-row"><text>姓名</text><text>{{ verifiedRecord.realName || '-' }}</text></view>
          <view class="compare-row"><text>学院</text><text>{{ verifiedRecord.collegeName || '-' }}</text></view>
          <view class="compare-row"><text>学号</text><text>{{ verifiedRecord.studentNo || '-' }}</text></view>
          <view class="compare-row"><text>班级</text><text>{{ verifiedRecord.className || '-' }}</text></view>
          <text class="compare-link">点击查看完整详情 ›</text>
        </view>
        <view v-else class="compare-empty">暂无已通过记录</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get } from '@/utils/request'

const loading = ref(false)
const pendingRecord = ref(null)
const verifiedRecord = ref(null)

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function pickLatest(list, status) {
  return list.find((item) => item.status === status) || null
}

function goDetail(id) {
  if (!id) return
  uni.navigateTo({ url: `/pages/auth-sub/history/detail?id=${id}` })
}

async function loadCompareData() {
  loading.value = true
  try {
    const data = await get('/mini/auth/history', {}, { showLoading: false })
    const records = Array.isArray(data) ? data : []
    pendingRecord.value = pickLatest(records, 1)
    verifiedRecord.value = pickLatest(records, 2)
  } catch (error) {
    pendingRecord.value = null
    verifiedRecord.value = null
    showToast((error && error.msg) || '加载失败')
  } finally {
    loading.value = false
  }
}

onShow(() => {
  loadCompareData()
})
</script>

<style lang="scss" scoped>
.compare-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  box-sizing: border-box;
}

.compare-loading {
  text-align: center;
  color: var(--text-secondary);
  padding-top: 120rpx;
}

.compare-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.compare-section {
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
}

.compare-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.compare-section__title {
  color: var(--text-primary);
  font-size: var(--font-md);
  font-weight: 600;
}

.compare-section__status {
  color: var(--text-white);
  font-size: var(--font-xs);
  border-radius: 999rpx;
  padding: 4rpx 14rpx;
}

.compare-section__status.is-pending {
  background-color: var(--warning-color);
}

.compare-section__status.is-verified {
  background-color: var(--success-color);
}

.compare-card {
  margin-top: var(--spacing-sm);
}

.compare-row {
  display: flex;
  justify-content: space-between;
  margin-top: 10rpx;
  color: var(--text-primary);
  font-size: var(--font-sm);
}

.compare-link {
  margin-top: var(--spacing-sm);
  display: block;
  color: var(--primary-color);
  font-size: var(--font-sm);
}

.compare-empty {
  margin-top: var(--spacing-sm);
  color: var(--text-placeholder);
  font-size: var(--font-sm);
}
</style>

