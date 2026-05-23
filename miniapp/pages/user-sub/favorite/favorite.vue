<template>
  <view class="favorite-page">
    <!-- 筛选Tab栏 -->
    <view class="favorite-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="favorite-tab"
        :class="{ 'is-active': activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        <text class="favorite-tab__text">{{ tab.label }}</text>
        <view v-if="activeTab === tab.value" class="favorite-tab__line"></view>
      </view>
    </view>

    <!-- 收藏列表 -->
    <view v-if="filteredList.length" class="favorite-list">
      <view
        v-for="item in filteredList"
        :key="item.favoriteId"
        class="favorite-card"
        @click="goDetail(item)"
      >
        <!-- 左侧封面图 -->
        <view class="favorite-card__cover">
          <image
            class="favorite-card__image"
            :src="item.coverImage"
            mode="aspectFill"
          />
          <!-- 已售出/已下架角标 -->
          <view v-if="item.status === 3" class="favorite-card__badge favorite-card__badge--sold">
            <text class="favorite-card__badge-text">已售出</text>
          </view>
          <view v-else-if="item.status === 2" class="favorite-card__badge favorite-card__badge--off">
            <text class="favorite-card__badge-text">已下架</text>
          </view>
        </view>

        <!-- 右侧信息区 -->
        <view class="favorite-card__info">
          <text class="favorite-card__title">{{ item.title }}</text>
          <view class="favorite-card__meta">
            <text v-if="conditionText(item)" class="favorite-card__condition">{{ conditionText(item) }}</text>
            <text v-if="item.campusName" class="favorite-card__campus">{{ item.campusName }}</text>
          </view>
          <view class="favorite-card__price-row">
            <text class="favorite-card__price">¥{{ item.price }}</text>
          </view>
          <view class="favorite-card__bottom">
            <text class="favorite-card__time">收藏于 {{ formatTime(item.favoriteTime) }}</text>
            <view class="favorite-card__heart" @click.stop="cancelFavorite(item)">
              <text class="favorite-card__heart-icon">♥</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部加载状态 -->
      <view v-if="loading" class="favorite-loading">加载中...</view>
      <view v-else-if="!hasMore" class="favorite-no-more">
        <text class="favorite-no-more__text">没有更多了</text>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!loading" class="favorite-empty">
      <empty-state type="no-favorite" />
      <view class="favorite-empty__action" @click="goDiscover">
        <text class="favorite-empty__btn">去发现好物</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { PRODUCT_STATUS } from '@/utils/constant'
import { useUserStore } from '@/store'
import EmptyState from '@/components/empty-state/empty-state.vue'
import { showToast, ensureLogin } from '@/utils/nav'

const CONDITION_TEXT = { 1: '全新', 2: '99新', 3: '9成新', 4: '8成新', 5: '7成新及以下' }
const userStore = useUserStore()

const tabs = [
  { label: '全部', value: 'all' },
  { label: '在售', value: PRODUCT_STATUS.ON_SALE },
  { label: '已售出', value: PRODUCT_STATUS.SOLD },
  { label: '已下架', value: PRODUCT_STATUS.OFF_SHELF }
]

const favoriteList = ref([])
const activeTab = ref('all')
const page = ref(1)
const pageSize = 20
const loading = ref(false)
const hasMore = ref(true)

const filteredList = computed(() => {
  if (activeTab.value === 'all') return favoriteList.value
  return favoriteList.value.filter(item => item.status === activeTab.value)
})

function conditionText(item) {
  return item.conditionLevel ? CONDITION_TEXT[item.conditionLevel] : ''
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${mm}-${dd}`
}

function switchTab(value) {
  activeTab.value = value
}

async function fetchFavorites(targetPage, refresh = false) {
  if (!ensureLogin()) return
  if (loading.value) return
  loading.value = true
  try {
    const data = await get(
      '/mini/favorite/list',
      { page: targetPage, pageSize },
      { showLoading: refresh || targetPage === 1 }
    )
    const isArray = Array.isArray(data)
    const records = isArray ? data : (data.records || [])
    const total = isArray ? records.length : (data.total != null ? data.total : records.length)
    if (refresh) {
      favoriteList.value = records
    } else {
      favoriteList.value = [...favoriteList.value, ...records]
    }
    hasMore.value = isArray ? false : favoriteList.value.length < total
    if (records.length < pageSize) hasMore.value = false
    page.value = targetPage
  } catch {
    showToast('加载失败，请稍后重试')
  } finally {
    loading.value = false
    if (refresh) uni.stopPullDownRefresh()
  }
}

function loadMore() {
  if (!hasMore.value || loading.value) return
  fetchFavorites(page.value + 1)
}

function cancelFavorite(item) {
  if (!item || !item.productId) return
  uni.showModal({
    title: '提示',
    content: '确定取消收藏该商品吗？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await post('/mini/favorite/cancel', { productId: item.productId }, { showLoading: true })
        favoriteList.value = favoriteList.value.filter(t => t.favoriteId !== item.favoriteId)
        userStore.updateStats()
        showToast('已取消收藏')
      } catch {
        showToast('取消失败，请稍后重试')
      }
    }
  })
}

function goDetail(item) {
  if (!item || !item.productId) return
  if (item.status === PRODUCT_STATUS.SOLD) {
    showToast('该商品已售出')
    return
  }
  if (item.status === PRODUCT_STATUS.OFF_SHELF) {
    showToast('该商品已下架')
    return
  }
  uni.navigateTo({ url: `/pages/product-sub/detail/detail?id=${item.productId}` })
}

function goDiscover() {
  uni.switchTab({ url: '/pages/index/index' })
}

onLoad(() => {
  fetchFavorites(1, true)
})

onPullDownRefresh(() => {
  fetchFavorites(1, true)
})

onReachBottom(() => {
  loadMore()
})
</script>

<style lang="scss" scoped>
.favorite-page {
  min-height: 100vh;
  background-color: var(--bg-page, #f5f5f5);
}

/* 筛选Tab栏 */
.favorite-tabs {
  display: flex;
  background-color: var(--bg-white, #fff);
  border-bottom: 1rpx solid var(--border-light, #f0f0f0);
  position: sticky;
  top: 0;
  z-index: 10;
}

.favorite-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 22rpx 0;
  position: relative;
}

.favorite-tab__text {
  font-size: 28rpx;
  color: var(--text-placeholder, #999);
}

.favorite-tab.is-active .favorite-tab__text {
  color: var(--primary-color, #4A90D9);
  font-weight: 600;
}

.favorite-tab__line {
  position: absolute;
  bottom: 0;
  width: 40rpx;
  height: 4rpx;
  border-radius: 2rpx;
  background-color: var(--primary-color, #4A90D9);
}

/* 收藏列表 */
.favorite-list {
  padding: 24rpx 24rpx;
}

/* 商品卡片 */
.favorite-card {
  display: flex;
  background-color: var(--bg-white, #fff);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.favorite-card__cover {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  border-radius: 14rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.favorite-card__image {
  width: 100%;
  height: 100%;
}

/* 右上角状态角标 */
.favorite-card__badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 4rpx 14rpx;
  border-radius: 0 14rpx 0 10rpx;
}

.favorite-card__badge--sold {
  background-color: #FF9800;
}

.favorite-card__badge--off {
  background-color: #999;
}

.favorite-card__badge-text {
  color: #fff;
  font-size: 20rpx;
  font-weight: 600;
}

/* 右侧信息 */
.favorite-card__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-left: 20rpx;
  min-width: 0;
}

.favorite-card__title {
  font-size: 28rpx;
  color: var(--text-primary, #333);
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.favorite-card__meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 12rpx;
}

.favorite-card__condition {
  font-size: 22rpx;
  color: #666;
  background-color: #f0f0f0;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
}

.favorite-card__campus {
  font-size: 22rpx;
  color: #1890FF;
  background-color: #E6F7FF;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
}

.favorite-card__price-row {
  margin-top: auto;
}

.favorite-card__price {
  font-size: 34rpx;
  color: #FF4D4F;
  font-weight: 600;
}

.favorite-card__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}

.favorite-card__time {
  font-size: 22rpx;
  color: #bbb;
}

.favorite-card__heart {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s ease;
}

.favorite-card__heart:active {
  transform: scale(0.75);
}

.favorite-card__heart-icon {
  font-size: 36rpx;
  color: #FF4D4F;
}

/* 底部加载 */
.favorite-loading {
  text-align: center;
  padding: 32rpx 0;
  color: var(--text-placeholder, #bbb);
  font-size: 24rpx;
}

/* 没有更多了 */
.favorite-no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 32rpx 0;
}

.favorite-no-more::before,
.favorite-no-more::after {
  content: '';
  width: 80rpx;
  height: 1rpx;
  background-color: var(--border-light, #eee);
}

.favorite-no-more__text {
  font-size: 24rpx;
  color: var(--text-placeholder, #bbb);
}

/* 空状态 */
.favorite-empty {
  padding-top: 120rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.favorite-empty__action {
  margin-top: 40rpx;
}

.favorite-empty__btn {
  font-size: 28rpx;
  color: #fff;
  background-color: var(--primary-color, #4A90D9);
  padding: 16rpx 48rpx;
  border-radius: 999rpx;
}
</style>
