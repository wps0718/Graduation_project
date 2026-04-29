<template>
  <view class="page">
    <!-- 状态筛选Tab -->
    <view class="status-tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="status-tab"
        :class="{ active: currentStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >
        <text class="status-tab__text">{{ tab.label }}</text>
        <view v-if="currentStatus === tab.value" class="status-tab__line"></view>
      </view>
    </view>

    <!-- 搜索栏（单独一行） -->
    <view class="search-bar">
      <view class="search-box">
        <text class="search-box__icon">🔍</text>
        <input
          type="text"
          :value="searchKeyword"
          placeholder="搜索商品标题"
          placeholder-style="color: #999999; font-size: 28rpx;"
          @input="onSearchInput"
          @confirm="loadList(true)"
          confirm-type="search"
          class="search-box__input"
        />
        <text v-if="searchKeyword" class="search-box__clear" @click="onSearchClear">✕</text>
      </view>
    </view>

    <!-- 排序栏 + 管理按钮 -->
    <view class="sort-bar">
      <view class="sort-bar__left" @click="showSortMenu = !showSortMenu">
        <text class="sort-bar__label">排序：{{ sortOptions[currentSortIndex].label }}</text>
        <text class="sort-bar__arrow" :class="{ up: showSortMenu }">▾</text>
      </view>
      <text class="sort-bar__count">共 {{ totalCount }} 件</text>
      <text class="sort-bar__manage" @click="toggleBatchMode">
        {{ batchMode ? '取消' : '管理' }}
      </text>
    </view>

    <!-- 批量操作栏（批量模式下替换排序栏） -->
    <view v-if="batchMode" class="batch-top-bar">
      <text class="batch-top-bar__text">已选 {{ selectedIds.size }} 件</text>
    </view>

    <!-- 主滚动区域 -->
    <scroll-view
      scroll-y
      class="scroll-view"
      :style="{ height: scrollViewHeight }"
      @scrolltolower="onReachBottom"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 骨架屏（初次加载） -->
      <template v-if="loading && productList.length === 0 && !error">
        <view v-for="n in 3" :key="n" class="skeleton-card">
          <view class="skeleton-img" />
          <view class="skeleton-info">
            <view class="skeleton-line w-60" />
            <view class="skeleton-line w-40" />
            <view class="skeleton-line w-30" />
          </view>
        </view>
      </template>

      <!-- 商品列表 -->
      <template v-if="productList.length > 0">
        <view
          v-for="item in productList"
          :key="item.id"
          class="product-card"
          :class="{ 'is-batch': batchMode }"
          @click="batchMode ? toggleSelect(item.id) : goDetail(item.id)"
        >
          <!-- 批量选择复选框 -->
          <view v-if="batchMode" class="card-checkbox" @click.stop="toggleSelect(item.id)">
            <view class="checkbox-circle" :class="{ checked: selectedIds.has(item.id) }">
              <text v-if="selectedIds.has(item.id)" class="checkbox-check">✓</text>
            </view>
          </view>

          <!-- 缩略图 -->
          <view class="card-thumb">
            <image
              :src="item.coverImage || '/static/pic/placeholder.png'"
              mode="aspectFill"
              class="card-thumb__img"
            />
            <!-- 状态角标（左上角） -->
            <view class="badge" :class="`badge--${item.status}`">
              {{ getStatusText(item.status) }}
            </view>
          </view>

          <!-- 商品信息 -->
          <view class="card-info">
            <text class="card-info__title">{{ item.title }}</text>
            <view class="card-info__meta">
              <text class="card-info__price">¥{{ item.price }}</text>
              <text class="card-info__condition">· {{ getConditionText(item.conditionLevel) }}</text>
            </view>
            <view class="card-info__stats">
              <text class="stat-icon">👁</text>
              <text class="stat-num">{{ item.viewCount || 0 }}</text>
              <text class="stat-icon">❤</text>
              <text class="stat-num">{{ item.favoriteCount || 0 }}</text>
              <text class="stat-icon">💬</text>
              <text class="stat-num">{{ item.chatCount || 0 }}</text>
            </view>
            <text class="card-info__time">{{ formatTime(item.createTime) }}</text>
          </view>

          <!-- 操作按钮（非批量模式） -->
          <view v-if="!batchMode" class="card-actions" @click.stop>
            <view class="more-btn" @click="showActionSheet(item)">
              <text class="more-btn__dots">⋯</text>
            </view>
            <view
              v-if="item.status === 1"
              class="sold-btn"
              @click="handleMarkSold(item)"
            >
              标记售出
            </view>
          </view>
        </view>
      </template>

      <!-- 空状态 -->
      <view v-if="!loading && productList.length === 0 && !error" class="empty-state">
        <view class="empty-icon">
          <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
            <rect x="20" y="30" width="80" height="60" rx="8" stroke="#D9D9D9" stroke-width="2" fill="none"/>
            <rect x="35" y="45" width="50" height="6" rx="3" fill="#D9D9D9"/>
            <rect x="35" y="58" width="35" height="6" rx="3" fill="#D9D9D9"/>
            <rect x="35" y="71" width="20" height="6" rx="3" fill="#D9D9D9"/>
            <circle cx="88" cy="85" r="18" stroke="#D9D9D9" stroke-width="2" fill="none"/>
            <line x1="100" y1="97" x2="112" y2="109" stroke="#D9D9D9" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </view>
        <text class="empty-main">{{ emptyMainText }}</text>
        <text v-if="emptySubText" class="empty-sub">{{ emptySubText }}</text>
        <view v-if="currentStatus === 1" class="empty-btn" @click="goPublish">
          <text>去发布</text>
        </view>
      </view>

      <!-- 网络异常状态 -->
      <view v-if="error && productList.length === 0" class="error-state">
        <view class="error-icon">
          <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
            <circle cx="60" cy="55" r="35" stroke="#D9D9D9" stroke-width="2" fill="none"/>
            <line x1="45" y1="45" x2="75" y2="75" stroke="#D9D9D9" stroke-width="2" stroke-linecap="round"/>
            <line x1="75" y1="45" x2="45" y2="75" stroke="#D9D9D9" stroke-width="2" stroke-linecap="round"/>
            <path d="M55 100 L65 100 L60 108 Z" fill="#D9D9D9"/>
          </svg>
        </view>
        <text class="error-text">网络开小差了</text>
        <view class="error-btn" @click="loadList(true)">
          <text>点击重试</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="productList.length > 0" class="load-more">
        <text v-if="loading" class="load-more__text">加载中⋯</text>
        <text v-else-if="hasMore" class="load-more__text load-more__text--hidden"> </text>
        <text v-else class="load-more__text">没有更多了</text>
      </view>
    </scroll-view>

    <!-- 批量操作底部栏 -->
    <view v-if="batchMode" class="batch-bar">
      <view class="batch-bar__select" @click="toggleSelectAll">
        <text>{{ isAllSelected ? '取消全选' : '全选' }}</text>
      </view>
      <view class="batch-bar__actions">
        <view class="batch-btn batch-btn--outline" @click="batchOffShelf">
          <text>批量下架</text>
        </view>
        <view class="batch-btn batch-btn--danger" @click="batchDelete">
          <text>批量删除</text>
        </view>
      </view>
    </view>

    <!-- 排序下拉菜单 -->
    <view v-if="showSortMenu" class="sort-overlay" @click="showSortMenu = false">
      <view class="sort-panel" @click.stop>
        <view class="sort-panel__title">选择排序方式</view>
        <view
          v-for="(opt, idx) in sortOptions"
          :key="idx"
          class="sort-option"
          :class="{ active: currentSortIndex === idx }"
          @click="selectSort(idx)"
        >
          <text class="sort-option__label">{{ opt.label }}</text>
          <text v-if="currentSortIndex === idx" class="sort-option__check">✓</text>
        </view>
      </view>
    </view>

    <!-- 自定义 ActionSheet（底部弹出） -->
    <view v-if="showSheet" class="sheet-overlay" @click="closeSheet">
      <view class="sheet-panel" @click.stop>
        <view class="sheet-list">
          <view
            v-for="(act, idx) in sheetActions"
            :key="idx"
            class="sheet-item"
            :class="{ 'sheet-item--danger': act === '删除' }"
            @click="onSheetAction(act)"
          >
            <text>{{ act }}</text>
          </view>
        </view>
        <view class="sheet-cancel" @click="closeSheet">
          <text>取消</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { isLoggedIn } from '@/utils/auth'

// ====== 状态 Tab ======
const statusTabs = [
  { label: '全部', value: null },
  { label: '在售', value: 1 },
  { label: '待审核', value: 0 },
  { label: '已下架', value: 2 },
  { label: '已售出', value: 3 },
]
const currentStatus = ref(null)
function switchStatus(status) {
  if (currentStatus.value === status) return
  currentStatus.value = status
  totalCount.value = 0
  loadList(true)
}

// ====== 数据 ======
const productList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const error = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10
const totalCount = ref(0)

// 获取滚动区域高度
const scrollViewHeight = computed(() => {
  const batchBar = batchMode.value ? 56 : 0
  return `calc(100vh - 88rpx - 96rpx - 80rpx - ${batchBar}px)`
})

// ====== 搜索 ======
const searchKeyword = ref('')
let searchTimer = null
function onSearchInput(e) {
  searchKeyword.value = e.detail.value
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadList(true)
  }, 300)
}
function onSearchClear() {
  searchKeyword.value = ''
  loadList(true)
}

// ====== 排序 ======
const sortOptions = [
  { label: '最新发布', sortBy: 'time', order: '' },
  { label: '价格从低到高', sortBy: 'price', order: 'asc' },
  { label: '价格从高到低', sortBy: 'price', order: 'desc' },
  { label: '浏览量最多', sortBy: 'time', order: '' },
]
const currentSortIndex = ref(0)
const showSortMenu = ref(false)
function selectSort(idx) {
  currentSortIndex.value = idx
  showSortMenu.value = false
  loadList(true)
}

// ====== 批量模式 ======
const batchMode = ref(false)
const selectedIds = ref(new Set())
const allIds = computed(() => productList.value.map(i => i.id))
const isAllSelected = computed(() => {
  if (productList.value.length === 0) return false
  return allIds.value.every(id => selectedIds.value.has(id))
})
function toggleBatchMode() {
  batchMode.value = !batchMode.value
  if (!batchMode.value) {
    selectedIds.value = new Set()
  }
}
function toggleSelect(id) {
  const s = new Set(selectedIds.value)
  if (s.has(id)) {
    s.delete(id)
  } else {
    s.add(id)
  }
  selectedIds.value = s
}
function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(allIds.value)
  }
}
async function batchOffShelf() {
  if (selectedIds.value.size === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  uni.showLoading({ title: '操作中...' })
  try {
    const promises = [...selectedIds.value].map(id =>
      post('/mini/product/off-shelf', { productId: id }).catch(() => {})
    )
    await Promise.all(promises)
    uni.hideLoading()
    uni.showToast({ title: '已批量下架', icon: 'success' })
    selectedIds.value = new Set()
    loadList(true)
  } catch (e) {
    uni.hideLoading()
  }
}
async function batchDelete() {
  if (selectedIds.value.size === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  uni.showModal({
    title: '确认批量删除',
    content: `确认删除选中的 ${selectedIds.value.size} 件商品？删除后不可恢复`,
    confirmText: '确认删除',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (!res.confirm) return
      uni.showLoading({ title: '删除中...' })
      try {
        const promises = [...selectedIds.value].map(id =>
          post('/mini/product/delete', { productId: id }).catch(() => {})
        )
        await Promise.all(promises)
        uni.hideLoading()
        uni.showToast({ title: '已批量删除', icon: 'success' })
        selectedIds.value = new Set()
        loadList(true)
      } catch (e) {
        uni.hideLoading()
      }
    }
  })
}

// ====== 操作面板 ======
const showSheet = ref(false)
const sheetTarget = ref(null)
const sheetActions = ref([])
function showActionSheet(item) {
  sheetTarget.value = item
  const acts = ['编辑']
  if (item.status === 1) acts.push('下架')
  if (item.status === 2) acts.push('上架')
  if (item.status !== 3) acts.push('删除')
  sheetActions.value = acts
  showSheet.value = true
}
function closeSheet() {
  showSheet.value = false
  sheetTarget.value = null
}
function onSheetAction(action) {
  showSheet.value = false
  const item = sheetTarget.value
  if (!item) return
  if (action === '编辑') {
    uni.navigateTo({ url: `/pages/product/edit/edit?id=${item.id}` })
  } else if (action === '下架') {
    handleOffShelf(item)
  } else if (action === '上架') {
    handleOnShelf(item)
  } else if (action === '删除') {
    handleDelete(item)
  }
  sheetTarget.value = null
}

// ====== 操作函数 ======
async function loadList(refresh = false) {
  if (loading.value) return
  if (refresh) {
    page.value = 1
    hasMore.value = true
    error.value = false
  }
  loading.value = true

  try {
    const params = {
      page: page.value,
      pageSize,
    }
    if (currentStatus.value !== null) {
      params.status = currentStatus.value
    }
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    const opt = sortOptions[currentSortIndex.value]
    if (opt.sortBy === 'price') {
      params.sortBy = 'price'
      params.order = opt.order
    } else {
      params.sortBy = 'time'
    }

    const res = await get('/mini/product/my-list', params)
    const list = res?.records || []
    totalCount.value = res?.total || 0

    if (refresh) {
      productList.value = list
    } else {
      productList.value = [...productList.value, ...list]
    }

    if (list.length < pageSize) {
      hasMore.value = false
    } else {
      page.value++
    }
  } catch (e) {
    console.error('❌ 加载失败', e)
    error.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function handleOffShelf(item) {
  uni.showModal({
    title: '确认下架',
    content: `确定要下架「${item.title}」吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/product/off-shelf', { productId: item.id })
        uni.showToast({ title: '已下架', icon: 'success' })
        loadList(true)
      } catch (e) {}
    }
  })
}

async function handleOnShelf(item) {
  try {
    await post('/mini/product/on-shelf', { productId: item.id })
    uni.showToast({ title: '已重新上架', icon: 'success' })
    loadList(true)
  } catch (e) {}
}

async function handleDelete(item) {
  uni.showModal({
    title: '确认删除',
    content: '确认删除该商品？删除后不可恢复',
    confirmText: '确认删除',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/product/delete', { productId: item.id })
        uni.showToast({ title: '已删除', icon: 'success' })
        loadList(true)
      } catch (e) {}
    }
  })
}

async function handleMarkSold(item) {
  uni.showModal({
    title: '确认标记售出',
    content: '确认将该商品标记为已售出？标记后将自动下架',
    confirmText: '确认售出',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/product/mark-sold', { productId: item.id })
        uni.showToast({ title: '已标记为售出', icon: 'success' })
        loadList(true)
      } catch (e) {
        console.error('标记售出失败', e)
      }
    }
  })
}

// ====== 工具函数 ======
function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace(/-/g, '/'))
  const now = new Date()
  const diff = now - date
  const day = 24 * 60 * 60 * 1000
  if (diff < day) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    if (hours === 0) {
      const minutes = Math.floor(diff / (60 * 1000))
      return minutes === 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  }
  const days = Math.floor(diff / day)
  if (days < 7) return `${days}天前`
  return timeStr.substring(0, 10)
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/product/detail/detail?id=${id}` })
}

function goPublish() {
  uni.switchTab({ url: '/pages/publish/publish' })
}

function getStatusText(status) {
  const map = { 0: '待审核', 1: '在售', 2: '已下架', 3: '已售出', 4: '已驳回' }
  return map[status] || '未知'
}

function getConditionText(level) {
  const map = { 1: '全新', 2: '几乎全新', 3: '九成新', 4: '八成新', 5: '七成新及以下' }
  return map[level] || '未知'
}

// ====== 空状态文案 ======
const emptyMainText = computed(() => {
  const map = {
    null: '还没有发布任何商品',
    1: '还没有在售商品',
    0: '暂无待审核的商品',
    2: '暂无已下架的商品',
    3: '暂无已售出的商品',
  }
  return map[currentStatus.value] || '还没有相关商品'
})
const emptySubText = computed(() => {
  if (currentStatus.value === 1) return '去发布你的第一件商品吧'
  return ''
})

// ====== 分页 ======
function onReachBottom() {
  if (hasMore.value && !loading.value) {
    loadList(false)
  }
}
function onRefresh() {
  refreshing.value = true
  loadList(true)
}

// ====== 生命周期 ======
onLoad((options) => {
  if (!isLoggedIn()) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      showCancel: false,
      success: () => {
        uni.reLaunch({ url: '/pages/login-sub/login/login' })
      }
    })
    return
  }
  if (options && options.status !== undefined) {
    currentStatus.value = Number(options.status)
  }
  loadList(true)
})

onShow(() => {
  if (isLoggedIn() && productList.value.length === 0) {
    loadList(true)
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* ====== 状态 Tab ====== */
.status-tabs {
  display: flex;
  background: #fff;
  padding: 0 32rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
.status-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0;
  position: relative;
}
.status-tab__text {
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;
}
.status-tab.active .status-tab__text {
  color: #1677ff;
  font-weight: 600;
  font-size: 30rpx;
}
.status-tab__line {
  width: 40rpx;
  height: 6rpx;
  background: #1677ff;
  border-radius: 3rpx;
  margin-top: 8rpx;
}

/* ====== 搜索栏 ====== */
.search-bar {
  padding: 16rpx 32rpx;
  background: #fff;
}
.search-box {
  position: relative;
  display: flex;
  align-items: center;
  height: 72rpx;
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 0 32rpx;
}
.search-box__icon {
  font-size: 28rpx;
  margin-right: 12rpx;
  flex-shrink: 0;
}
.search-box__input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  background: transparent;
  border: none;
  outline: none;
}
.search-box__clear {
  flex-shrink: 0;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #bbb;
  margin-left: 8rpx;
}

/* ====== 排序栏 ====== */
.sort-bar {
  display: flex;
  align-items: center;
  height: 80rpx;
  padding: 0 32rpx;
  background: #fafafa;
  border-bottom: 1rpx solid #f0f0f0;
}
.sort-bar__left {
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.sort-bar__label {
  font-size: 26rpx;
  color: #666;
}
.sort-bar__arrow {
  font-size: 20rpx;
  color: #666;
  transition: transform 0.2s;
}
.sort-bar__arrow.up {
  transform: rotate(180deg);
}
.sort-bar__count {
  margin-left: auto;
  font-size: 24rpx;
  color: #999;
  margin-right: 24rpx;
}
.sort-bar__manage {
  font-size: 26rpx;
  color: #1677ff;
  flex-shrink: 0;
}

/* ====== 批量操作栏 ====== */
.batch-top-bar {
  height: 80rpx;
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  background: #e6f7ff;
  border-bottom: 1rpx solid #bae7ff;
}
.batch-top-bar__text {
  font-size: 26rpx;
  color: #1677ff;
  font-weight: 500;
}

/* ====== 主滚动区域 ====== */
.scroll-view {
  flex: 1;
}

/* ====== 骨架屏 ====== */
.skeleton-card {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
  margin: 12rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}
.skeleton-img {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  background: #eee;
  flex-shrink: 0;
  animation: skeleton 1.2s ease-in-out infinite;
}
.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 8rpx 0;
}
.skeleton-line {
  height: 24rpx;
  background: #eee;
  border-radius: 12rpx;
  animation: skeleton 1.2s ease-in-out infinite;
}
.skeleton-line.w-60 { width: 60%; }
.skeleton-line.w-40 { width: 40%; }
.skeleton-line.w-30 { width: 30%; }
@keyframes skeleton {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

/* ====== 商品卡片 ====== */
.product-card {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  margin: 12rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  align-items: flex-start;
  position: relative;
}
.product-card.is-batch {
  padding-left: 16rpx;
}

/* 复选框 */
.card-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12rpx 0 4rpx;
  flex-shrink: 0;
}
.checkbox-circle {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.checkbox-circle.checked {
  border-color: #1890ff;
  background: #1890ff;
}
.checkbox-check {
  color: #fff;
  font-size: 24rpx;
  font-weight: bold;
}

/* 缩略图 */
.card-thumb {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  flex-shrink: 0;
  border-radius: 16rpx;
  overflow: hidden;
  background: #f5f5f5;
}
.card-thumb__img {
  width: 100%;
  height: 100%;
}

/* 状态角标（左上） */
.badge {
  position: absolute;
  top: 8rpx;
  left: 8rpx;
  height: 44rpx;
  line-height: 44rpx;
  padding: 0 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #fff;
  font-weight: 500;
}
.badge--0 { background: #faad14; }
.badge--1 { background: #52c41a; }
.badge--2 { background: #bfbfbf; }
.badge--3 { background: #1890ff; }
.badge--4 { background: #ff4d4f; }

/* 商品信息 */
.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  min-width: 0;
  padding: 4rpx 0;
}
.card-info__title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-info__meta {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.card-info__price {
  font-size: 36rpx;
  color: #ff4d4f;
  font-weight: 700;
}
.card-info__condition {
  font-size: 24rpx;
  color: #999;
}
.card-info__stats {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.stat-icon {
  font-size: 24rpx;
  color: #bfbfbf;
}
.stat-num {
  font-size: 24rpx;
  color: #999;
}
.card-info__time {
  font-size: 24rpx;
  color: #ccc;
}

/* 操作按钮区 */
.card-actions {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  align-items: center;
  flex-shrink: 0;
  padding-left: 8rpx;
}
.more-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 2rpx solid #e5e5e5;
  background: #fff;
}
.more-btn__dots {
  font-size: 32rpx;
  color: #666;
  line-height: 1;
  letter-spacing: 2rpx;
}
.sold-btn {
  height: 56rpx;
  line-height: 56rpx;
  padding: 0 20rpx;
  border-radius: 28rpx;
  background: #1890ff;
  color: #fff;
  font-size: 24rpx;
  text-align: center;
  white-space: nowrap;
}

/* ====== 空状态 ====== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 32rpx 80rpx;
}
.empty-icon {
  margin-bottom: 32rpx;
}
.empty-main {
  font-size: 32rpx;
  color: #999;
  margin-bottom: 12rpx;
}
.empty-sub {
  font-size: 28rpx;
  color: #ccc;
  margin-bottom: 32rpx;
}
.empty-btn {
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 48rpx;
  border-radius: 36rpx;
  border: 2rpx solid #1890ff;
  color: #1890ff;
  font-size: 28rpx;
}

/* ====== 网络异常 ====== */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 32rpx 80rpx;
}
.error-icon {
  margin-bottom: 32rpx;
}
.error-text {
  font-size: 32rpx;
  color: #999;
  margin-bottom: 32rpx;
}
.error-btn {
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 48rpx;
  border-radius: 36rpx;
  border: 2rpx solid #1890ff;
  color: #1890ff;
  font-size: 28rpx;
}

/* ====== 加载更多 ====== */
.load-more {
  padding: 32rpx;
  text-align: center;
}
.load-more__text {
  font-size: 24rpx;
  color: #ccc;
}
.load-more__text--hidden {
  visibility: hidden;
}

/* ====== 批量操作底部栏 ====== */
.batch-bar {
  display: flex;
  align-items: center;
  height: 112rpx;
  padding: 0 32rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.04);
  position: sticky;
  bottom: 0;
}
.batch-bar__select {
  padding: 16rpx 24rpx;
  font-size: 28rpx;
  color: #666;
  flex-shrink: 0;
}
.batch-bar__actions {
  display: flex;
  gap: 16rpx;
  margin-left: auto;
}
.batch-btn {
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 32rpx;
  border-radius: 36rpx;
  font-size: 26rpx;
  text-align: center;
}
.batch-btn--outline {
  border: 2rpx solid #d9d9d9;
  color: #666;
  background: #fff;
}
.batch-btn--danger {
  border: 2rpx solid #ff4d4f;
  color: #ff4d4f;
  background: #fff;
}

/* ====== 排序下拉 ====== */
.sort-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 100;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.sort-panel {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx 0 48rpx;
}
.sort-panel__title {
  font-size: 28rpx;
  color: #999;
  text-align: center;
  margin-bottom: 16rpx;
}
.sort-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 48rpx;
}
.sort-option.active {
  background: #f0f8ff;
}
.sort-option__label {
  font-size: 30rpx;
  color: #333;
}
.sort-option.active .sort-option__label {
  color: #1890ff;
  font-weight: 600;
}
.sort-option__check {
  font-size: 28rpx;
  color: #1890ff;
  font-weight: bold;
}

/* ====== 自定义 ActionSheet ====== */
.sheet-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.sheet-panel {
  width: 100%;
  padding: 0 32rpx 40rpx;
}
.sheet-list {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
}
.sheet-item {
  height: 100rpx;
  line-height: 100rpx;
  text-align: center;
  font-size: 30rpx;
  color: #333;
  border-bottom: 1rpx solid #f0f0f0;
}
.sheet-item:last-child {
  border-bottom: none;
}
.sheet-item--danger text {
  color: #ff4d4f;
}
.sheet-cancel {
  height: 100rpx;
  line-height: 100rpx;
  text-align: center;
  font-size: 30rpx;
  color: #333;
  background: #fff;
  border-radius: 24rpx;
}
</style>