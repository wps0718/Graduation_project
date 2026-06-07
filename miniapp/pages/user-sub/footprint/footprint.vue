<template>
  <view class="footprint-page">
    <!-- 分类Tab栏 -->
    <scroll-view
      class="footprint-categories"
      scroll-x
      enhanced
      show-scrollbar="false"
    >
      <view
        v-for="item in categories"
        :key="item.value"
        class="footprint-category"
        :class="{ 'is-active': activeCategory === item.value }"
        @click="switchCategory(item.value)"
      >
        <text class="footprint-category__text">{{ item.label }}</text>
        <view v-if="activeCategory === item.value" class="footprint-category__line"></view>
      </view>
    </scroll-view>

    <!-- 时间筛选 -->
    <view class="footprint-filter">
      <view class="footprint-filter__picker" @click="showTimePicker = true">
        <text class="footprint-filter__text">{{ timeOptions.find(t => t.value === activeTime).label }}</text>
        <text class="footprint-filter__arrow">▼</text>
      </view>
      <view class="footprint-filter__actions">
        <text
          v-if="!isManageMode"
          class="footprint-filter__btn"
          @click="enterManageMode"
        >管理</text>
        <text
          v-else
          class="footprint-filter__btn is-cancel"
          @click="exitManageMode"
        >取消</text>
      </view>
    </view>

    <!-- 时间选择器弹出层 -->
    <view v-if="showTimePicker" class="footprint-mask" @click="showTimePicker = false"></view>
    <view v-if="showTimePicker" class="footprint-picker">
      <view
        v-for="item in timeOptions"
        :key="item.value"
        class="footprint-picker__item"
        :class="{ 'is-selected': activeTime === item.value }"
        @click="selectTime(item.value)"
      >
        <text class="footprint-picker__text">{{ item.label }}</text>
        <text v-if="activeTime === item.value" class="footprint-picker__check">✓</text>
      </view>
    </view>

    <!-- 足迹列表（按日期分组） -->
    <view class="footprint-content">
      <view v-if="groupList.length" class="footprint-groups">
        <view v-for="group in groupList" :key="group.date" class="footprint-group">
          <view class="footprint-group__header">
            <text class="footprint-group__date">{{ group.date }}</text>
          </view>
          <view class="footprint-group__grid">
            <view
              v-for="item in group.items"
              :key="item.id"
              class="footprint-card"
              @click="onItemClick(item)"
            >
              <!-- 管理模式选择框 -->
              <view
                v-if="isManageMode"
                class="footprint-card__checkbox"
                :class="{ 'is-checked': selectedIds.includes(item.id) }"
                @click.stop="toggleSelect(item.id)"
              >
                <text v-if="selectedIds.includes(item.id)" class="footprint-card__checkmark">✓</text>
              </view>
              <view class="footprint-card__image-wrap">
                <image
                  class="footprint-card__image"
                  :src="resolveImageUrl(item.coverImage)"
                  mode="aspectFill"
                />
                <view
                  v-if="item.status === 1"
                  class="footprint-card__tag is-selling"
                >
                  <text class="footprint-card__tag-text">在售</text>
                </view>
                <view
                  v-else-if="item.status === 2 || item.status === 3"
                  class="footprint-card__tag"
                  :class="item.status === 2 ? 'is-off' : 'is-sold'"
                >
                  <text class="footprint-card__tag-text">{{ item.status === 2 ? '已下架' : '已售出' }}</text>
                </view>
              </view>
              <view class="footprint-card__info">
                <text class="footprint-card__title">{{ item.title }}</text>
                <view class="footprint-card__meta">
                  <text class="footprint-card__price">¥{{ item.price }}</text>
                  <text v-if="item.originalPrice" class="footprint-card__original">¥{{ item.originalPrice }}</text>
                </view>
                <view class="footprint-card__footer">
                  <text class="footprint-card__campus">{{ item.campusName || '' }}</text>
                  <text class="footprint-card__time">{{ formatBrowseTime(item.browseTime) }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view v-if="loadingMore" class="footprint-loading">加载中...</view>
        <view v-else-if="!hasMore && groupList.length" class="footprint-loading">没有更多了</view>
      </view>

      <!-- 空状态 -->
      <view v-else class="footprint-empty">
        <EmptyState type="no-data" text="还没有浏览记录" />
        <view class="footprint-empty__btn" @click="goHome">
          <text class="footprint-empty__btn-text">去逛逛</text>
        </view>
      </view>
    </view>

    <!-- 管理模式底部操作栏 -->
    <view v-if="isManageMode" class="footprint-bottom-bar">
      <view class="footprint-bottom-bar__left" @click="toggleSelectAll">
        <view
          class="footprint-bottom-bar__checkbox"
          :class="{ 'is-checked': isAllSelected }"
        >
          <text v-if="isAllSelected" class="footprint-bottom-bar__checkmark">✓</text>
        </view>
        <text class="footprint-bottom-bar__label">全选</text>
      </view>
      <view
        class="footprint-bottom-bar__delete"
        :class="{ 'is-disabled': !selectedIds.length }"
        @click="confirmDelete"
      >
        <text class="footprint-bottom-bar__delete-text">删除选中 ({{ selectedIds.length }})</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { resolveImageUrl } from '@/utils/image'
import EmptyState from '@/components/empty-state/empty-state.vue'
import { showToast, ensureLogin } from '@/utils/nav'

const categories = ref([
  { label: '全部', value: 0 }
])
const activeCategory = ref(0)

const timeOptions = [
  { label: '全部时间', value: 4 },
  { label: '最近一周', value: 1 },
  { label: '最近一月', value: 2 },
  { label: '最近三月', value: 3 }
]
const activeTime = ref(4)
const showTimePicker = ref(false)

const groupList = ref([])
const page = ref(1)
const pageSize = 20
const hasMore = ref(true)
const loading = ref(false)
const loadingMore = ref(false)
const totalCount = ref(0)

// 管理模式
const isManageMode = ref(false)
const selectedIds = ref([])

// 是否全选
const allRecordIds = computed(() => {
  const ids = []
  for (const group of groupList.value) {
    for (const item of group.items) {
      ids.push(item.id)
    }
  }
  return ids
})

const isAllSelected = computed(() => {
  if (!allRecordIds.value.length) return false
  return selectedIds.value.length === allRecordIds.value.length
})

onLoad(async () => {
  await loadCategories()
  fetchFootprints(1, true)
})

onPullDownRefresh(() => {
  page.value = 1
  hasMore.value = true
  fetchFootprints(1, true)
})

onReachBottom(() => {
  if (!hasMore.value || loading.value || isManageMode.value) return
  loadingMore.value = true
  fetchFootprints(page.value + 1, false)
})

async function loadCategories() {
  try {
    const data = await get('/mini/category/list', {}, { showLoading: false })
    if (Array.isArray(data)) {
      categories.value = [
        { label: '全部', value: 0 },
        ...data.map(c => ({ label: c.name, value: c.id }))
      ]
    }
  } catch (e) {
    // 使用默认分类
  }
}

function switchCategory(value) {
  if (activeCategory.value === value) return
  activeCategory.value = value
  page.value = 1
  hasMore.value = true
  groupList.value = []
  fetchFootprints(1, true)
}

function selectTime(value) {
  activeTime.value = value
  showTimePicker.value = false
  page.value = 1
  hasMore.value = true
  groupList.value = []
  fetchFootprints(1, true)
}

function getTimeRange(value) {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const today = `${y}-${m}-${d}`

  switch (value) {
    case 1: { // 最近一周
      const start = new Date(now)
      start.setDate(start.getDate() - 7)
      const sy = start.getFullYear()
      const sm = String(start.getMonth() + 1).padStart(2, '0')
      const sd = String(start.getDate()).padStart(2, '0')
      return { beginTime: `${sy}-${sm}-${sd}`, endTime: today }
    }
    case 2: { // 最近一月
      const start = new Date(now)
      start.setMonth(start.getMonth() - 1)
      const sy = start.getFullYear()
      const sm = String(start.getMonth() + 1).padStart(2, '0')
      const sd = String(start.getDate()).padStart(2, '0')
      return { beginTime: `${sy}-${sm}-${sd}`, endTime: today }
    }
    case 3: { // 最近三月
      const start = new Date(now)
      start.setMonth(start.getMonth() - 3)
      const sy = start.getFullYear()
      const sm = String(start.getMonth() + 1).padStart(2, '0')
      const sd = String(start.getDate()).padStart(2, '0')
      return { beginTime: `${sy}-${sm}-${sd}`, endTime: today }
    }
    default:
      return {}
  }
}

async function fetchFootprints(targetPage, refresh = false) {
  if (!ensureLogin()) return
  if (loading.value) return
  loading.value = true

  try {
    const params = {
      page: targetPage,
      pageSize,
      categoryId: activeCategory.value > 0 ? activeCategory.value : undefined
    }
    const timeRange = getTimeRange(activeTime.value)
    if (timeRange.beginTime) params.beginTime = timeRange.beginTime
    if (timeRange.endTime) params.endTime = timeRange.endTime

    const data = await get('/mini/footprint/list', params, { showLoading: refresh })
    const records = (data && data.records) || []
    totalCount.value = (data && data.total) || 0

    uni.setNavigationBarTitle({ title: `我的足迹 (${totalCount.value})` })

    if (refresh) {
      groupList.value = records
    } else {
      // 合并分组
      for (const group of records) {
        const existing = groupList.value.find(g => g.date === group.date)
        if (existing) {
          existing.items.push(...group.items)
        } else {
          groupList.value.push(group)
        }
      }
    }
    // 修复：用已加载条目数 vs 总条目数判断是否还有更多
    // 之前用 records.length >= pageSize 比较的是"分组数 vs pageSize"，语义不准确
    const loadedItemCount = groupList.value.reduce((sum, g) => sum + g.items.length, 0)
    hasMore.value = loadedItemCount < totalCount.value
    page.value = targetPage
  } catch (error) {
    showToast('加载失败')
  } finally {
    loading.value = false
    loadingMore.value = false
    if (refresh) {
      uni.stopPullDownRefresh()
    }
  }
}

function formatBrowseTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

function onItemClick(item) {
  if (isManageMode.value) {
    // 修复：必须用 item.id（browse_history 主键），与 toggleSelectAll / confirmDelete 保持一致
    // 之前误用 item.productId，导致选中 ID 与全选/删除逻辑不匹配
    toggleSelect(item.id)
    return
  }
  if (item.productId) {
    uni.navigateTo({ url: `/pages/product-sub/detail/detail?id=${item.productId}` })
  }
}

// 管理模式
function enterManageMode() {
  isManageMode.value = true
  selectedIds.value = []
}

function exitManageMode() {
  isManageMode.value = false
  selectedIds.value = []
}

function toggleSelect(id) {
  const idx = selectedIds.value.indexOf(id)
  if (idx > -1) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = [...allRecordIds.value]
  }
}

function confirmDelete() {
  if (!selectedIds.value.length) {
    showToast('请选择要删除的足迹')
    return
  }
  uni.showModal({
    title: '提示',
    content: `确定删除选中的 ${selectedIds.value.length} 条足迹吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/footprint/delete', { ids: selectedIds.value }, { showLoading: true })
        showToast('删除成功')
        exitManageMode()
        page.value = 1
        hasMore.value = true
        await fetchFootprints(1, true)
      } catch (error) {
        showToast('删除失败')
      }
    }
  })
}

function goHome() {
  uni.switchTab({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.footprint-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

// 分类Tab
.footprint-categories {
  display: flex;
  flex-wrap: nowrap;
  white-space: nowrap;
  padding: 0 var(--spacing-md);
  background-color: var(--bg-white);
  border-bottom: 1rpx solid var(--border-light);
}

.footprint-category {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 20rpx;
  margin-right: 8rpx;
  position: relative;
}

.footprint-category__text {
  font-size: var(--font-md);
  color: var(--text-secondary);
}

.footprint-category.is-active .footprint-category__text {
  color: var(--primary-color);
  font-weight: 600;
}

.footprint-category__line {
  position: absolute;
  bottom: 0;
  width: 40rpx;
  height: 4rpx;
  border-radius: 2rpx;
  background-color: var(--primary-color);
}

// 筛选栏
.footprint-filter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: var(--bg-white);
  border-bottom: 1rpx solid var(--border-light);
}

.footprint-filter__picker {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  border-radius: var(--radius-sm);
  background-color: var(--bg-grey);
}

.footprint-filter__text {
  font-size: var(--font-sm);
  color: var(--text-primary);
}

.footprint-filter__arrow {
  font-size: 20rpx;
  color: var(--text-secondary);
}

.footprint-filter__btn {
  font-size: var(--font-sm);
  color: var(--primary-color);
  font-weight: 500;
  padding: 10rpx 16rpx;
}

.footprint-filter__btn.is-cancel {
  color: var(--text-secondary);
}

// 时间选择器弹出层
.footprint-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 99;
}

.footprint-picker {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: var(--bg-white);
  padding: var(--spacing-sm) 0;
  border-radius: 0 0 20rpx 20rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
}

.footprint-picker__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx var(--spacing-md);
}

.footprint-picker__item.is-selected {
  background-color: var(--primary-bg);
}

.footprint-picker__text {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.footprint-picker__item.is-selected .footprint-picker__text {
  color: var(--primary-color);
  font-weight: 600;
}

.footprint-picker__check {
  color: var(--primary-color);
  font-weight: bold;
}

// 足迹内容
.footprint-content {
  padding: var(--spacing-sm) var(--spacing-sm) 120rpx;
}

// 日期分组
.footprint-group__header {
  padding: var(--spacing-sm) var(--spacing-sm);
}

.footprint-group__date {
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-primary);
}

// 双列网格
.footprint-group__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-sm);
}

// 商品卡片
.footprint-card {
  background: var(--bg-white);
  border-radius: 16rpx;
  overflow: hidden;
  position: relative;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.footprint-card__image-wrap {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  background-color: var(--bg-grey);
}

.footprint-card__image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.footprint-card__tag {
  position: absolute;
  top: 8rpx;
  left: 8rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  font-size: 20rpx;
}

.footprint-card__tag.is-selling {
  background-color: rgba(103, 194, 58, 0.85);
  color: #fff;
}

.footprint-card__tag.is-sold {
  background-color: rgba(230, 162, 60, 0.85);
  color: #fff;
}

.footprint-card__tag.is-off {
  background-color: rgba(153, 153, 153, 0.85);
  color: #fff;
}

.footprint-card__tag-text {
  font-size: 20rpx;
  font-weight: 500;
}

.footprint-card__info {
  padding: 12rpx 12rpx 16rpx;
}

.footprint-card__title {
  font-size: var(--font-sm);
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.footprint-card__meta {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-top: 8rpx;
}

.footprint-card__price {
  font-size: 34rpx;
  color: var(--danger-color);
  font-weight: 600;
}

.footprint-card__original {
  font-size: 22rpx;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.footprint-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6rpx;
}

.footprint-card__campus {
  font-size: 20rpx;
  color: var(--primary-color);
  background-color: var(--primary-bg);
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
}

.footprint-card__time {
  font-size: 20rpx;
  color: var(--text-secondary);
}

// 管理模式勾选框
.footprint-card__checkbox {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  z-index: 10;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.8);
  background-color: rgba(0, 0, 0, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
}

.footprint-card__checkbox.is-checked {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.footprint-card__checkmark {
  color: #fff;
  font-size: 24rpx;
  font-weight: bold;
}

// 底部操作栏
.footprint-bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100rpx;
  background-color: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-md);
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
  z-index: 50;
}

.footprint-bottom-bar__left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.footprint-bottom-bar__checkbox {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.footprint-bottom-bar__checkbox.is-checked {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.footprint-bottom-bar__checkmark {
  color: #fff;
  font-size: 24rpx;
  font-weight: bold;
}

.footprint-bottom-bar__label {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.footprint-bottom-bar__delete {
  padding: 16rpx 40rpx;
  border-radius: 40rpx;
  background-color: var(--danger-color);
}

.footprint-bottom-bar__delete.is-disabled {
  opacity: 0.4;
}

.footprint-bottom-bar__delete-text {
  color: #fff;
  font-size: var(--font-md);
  font-weight: 500;
}

// 加载状态
.footprint-loading {
  text-align: center;
  padding: var(--spacing-md) 0;
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

// 空状态
.footprint-empty {
  padding-top: 160rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.footprint-empty__btn {
  margin-top: var(--spacing-lg);
  padding: 18rpx 60rpx;
  border-radius: 40rpx;
  background-color: var(--primary-color);
}

.footprint-empty__btn-text {
  color: #fff;
  font-size: var(--font-md);
  font-weight: 500;
}
</style>
