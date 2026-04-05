<template>
  <view class="search-page">
    <!-- 顶部搜索栏 -->
    <view class="search-header" :style="headerStyle">
      <view class="search-header__back" @click="goBack">
        <text class="search-header__back-icon">←</text>
      </view>
      <view class="search-header__input-wrap">
        <view class="search-header__search-icon" @click="handleSearch">
          <image class="search-header__search-icon-img" src="/static/svg/search.svg" mode="aspectFit" />
        </view>
        <input
          ref="searchInputRef"
          v-model="keyword"
          class="search-header__input"
          type="text"
          :focus="inputFocused"
          placeholder="请输入搜索内容"
          confirm-type="search"
          @confirm="handleSearch"
          @input="handleInput"
        />
        <view v-if="keyword" class="search-header__clear" @click="clearKeyword">
          <text class="search-header__clear-icon">×</text>
        </view>
      </view>
    </view>

    <!-- 搜索前：历史记录 + 热门搜索 -->
    <view v-if="!hasSearched" class="search-content">
      <!-- 搜索历史 -->
      <view v-if="searchHistory.length > 0" class="search-card">
        <view class="search-card__header">
          <text class="search-card__title">搜索历史</text>
          <view class="search-card__action" @click="showClearHistoryModal">
            <text class="search-card__action-text">清空</text>
          </view>
        </view>
        <view class="search-tags search-tags--history">
          <view
            v-for="(item, index) in searchHistory"
            :key="index"
            class="search-tag"
            @click="searchByTag(item)"
          >
            <text>{{ item }}</text>
          </view>
        </view>
      </view>

      <!-- 热门搜索 -->
      <view class="search-card">
        <view class="search-card__header">
          <text class="search-card__title">热门搜索</text>
        </view>
        <view class="search-tags search-tags--hot">
          <view
            v-for="(item, index) in hotKeywords"
            :key="index"
            class="search-tag"
            @click="searchByTag(item.keyword)"
          >
            <text>{{ item.keyword }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 搜索后：筛选 + 结果 -->
    <view v-else class="search-result">
      <!-- 筛选条件栏 -->
      <view class="filter-section">
        <text class="filter-section__title">筛选条件</text>
        <view class="filter-grid">
          <picker
            class="filter-cell"
            range-key="name"
            :value="selectedCategoryIndex"
            :range="categoryOptions"
            @change="onCategoryChange"
          >
            <view class="filter-cell__inner">
              <text class="filter-cell__label">{{ selectedCategoryName }}</text>
              <text class="filter-cell__arrow">›</text>
            </view>
          </picker>

          <picker
            class="filter-cell"
            range-key="name"
            :value="selectedCampusIndex"
            :range="campusOptions"
            @change="onCampusChange"
          >
            <view class="filter-cell__inner">
              <text class="filter-cell__label">{{ selectedCampusName }}</text>
              <text class="filter-cell__arrow">›</text>
            </view>
          </picker>

          <picker
            class="filter-cell"
            mode="selector"
            :range="priceRangeOptions"
            :value="selectedPriceIndex"
            @change="onPriceRangeChange"
          >
            <view class="filter-cell__inner">
              <text class="filter-cell__label">{{ selectedPriceRangeText }}</text>
              <text class="filter-cell__arrow">›</text>
            </view>
          </picker>

          <picker
            class="filter-cell"
            mode="selector"
            :range="sortOptions"
            :value="selectedSortIndex"
            range-key="label"
            @change="onSortChange"
          >
            <view class="filter-cell__inner">
              <text class="filter-cell__label">{{ selectedSortLabel }}</text>
              <text class="filter-cell__arrow">›</text>
            </view>
          </picker>
        </view>
      </view>

      <view class="result-header">
        <text class="result-header__title">推荐内容</text>
        <text class="result-header__count">搜索结果 {{ total }}件</text>
      </view>

      <!-- 商品列表 -->
      <view v-if="productList.length > 0" class="result-list">
        <product-card
          v-for="item in productList"
          :key="item.id"
          :product="item"
        />
        <!-- 加载更多 -->
        <view v-if="loading" class="result-loading">
          <text>加载中...</text>
        </view>
        <view v-else-if="noMore" class="result-no-more">
          <text>没有更多了</text>
        </view>
      </view>

      <!-- 无结果 -->
      <empty-state v-else type="no-result" />
    </view>

    <!-- 清空历史确认弹窗 -->
    <uni-popup ref="clearHistoryPopupRef" type="dialog">
      <uni-popup-dialog
        mode="dialog"
        title="提示"
        content="确定清空所有搜索历史吗？"
        :before-close="true"
        @close="closeClearHistoryModal"
        @confirm="clearSearchHistory"
      />
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import ProductCard from '@/components/product-card/product-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

// 常量定义
const SEARCH_HISTORY_KEY = 'searchHistory'
const MAX_HISTORY_COUNT = 20

// 响应式状态
const keyword = ref('')
const hasSearched = ref(false)
const searchHistory = ref([])
const hotKeywords = ref([])
const productList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const loading = ref(false)
const noMore = ref(false)
const searchInputRef = ref(null)
const inputFocused = ref(false)
const lastSearchedKeyword = ref('')
const statusBarHeight = ref(0)
const navBarHeight = ref(44)

// 筛选条件
const categories = ref([])
const campuses = ref([])
const selectedCategoryId = ref(null)
const selectedCampusId = ref(null)
const selectedCategoryIndex = ref(0)
const selectedCampusIndex = ref(0)

// 价格区间
const priceRangeOptions = ['价格不限', '0-50', '50-100', '100-200', '200-500']
const selectedPriceIndex = ref(0)
const priceRange = ref({ min: '', max: '' })

// 排序
const sortOptions = [
  { value: 'latest', label: '最新发布' },
  { value: 'price_asc', label: '价格↑' },
  { value: 'price_desc', label: '价格↓' }
]
const selectedSortIndex = ref(0)
const currentSort = ref('latest')

// 弹窗引用
const clearHistoryPopupRef = ref(null)

// 计算属性
const categoryOptions = computed(() => {
  return [{ id: null, name: '全部分类' }, ...categories.value]
})

const campusOptions = computed(() => {
  return [{ id: null, name: '不限校区' }, ...campuses.value]
})

const selectedCategoryName = computed(() => {
  return categoryOptions.value[selectedCategoryIndex.value]?.name || '全部分类'
})

const selectedCampusName = computed(() => {
  return campusOptions.value[selectedCampusIndex.value]?.name || '不限校区'
})

const selectedPriceRangeText = computed(() => {
  return priceRangeOptions[selectedPriceIndex.value]
})

const selectedSortLabel = computed(() => {
  return sortOptions[selectedSortIndex.value]?.label || '最新发布'
})

const headerStyle = computed(() => ({
  height: `${navBarHeight.value + statusBarHeight.value}px`,
  paddingTop: `${statusBarHeight.value}px`
}))

// 方法
function goBack() {
  uni.navigateBack()
}

function handleInput() {
  const current = keyword.value.trim()
  if (current !== lastSearchedKeyword.value) {
    hasSearched.value = false
  }
}

function clearKeyword() {
  keyword.value = ''
  hasSearched.value = false
  lastSearchedKeyword.value = ''
  productList.value = []
  total.value = 0
}

function handleSearch() {
  if (!keyword.value.trim()) {
    uni.showToast({
      title: '请输入搜索关键词',
      icon: 'none'
    })
    return
  }
  executeSearch({ allowEmptyKeyword: false, recordHistory: true })
}

function searchByTag(text) {
  keyword.value = text
  handleSearch()
}

function focusInput() {
  inputFocused.value = false
  nextTick(() => {
    inputFocused.value = true
  })
}

function executeSearch({ allowEmptyKeyword, recordHistory }) {
  const current = keyword.value.trim()
  if (!allowEmptyKeyword && !current) {
    return
  }
  if (keyword.value !== current) {
    keyword.value = current
  }
  hasSearched.value = true
  lastSearchedKeyword.value = current
  if (recordHistory && current) {
    addToSearchHistory(current)
  }
  page.value = 1
  noMore.value = false
  fetchProductList(true)
}

function addToSearchHistory(key) {
  let history = uni.getStorageSync(SEARCH_HISTORY_KEY) || []
  // 移除已存在的相同关键词
  history = history.filter(item => item !== key)
  // 添加到最前面
  history.unshift(key)
  // 限制最多20条
  if (history.length > MAX_HISTORY_COUNT) {
    history = history.slice(0, MAX_HISTORY_COUNT)
  }
  uni.setStorageSync(SEARCH_HISTORY_KEY, history)
  searchHistory.value = history
}

function loadSearchHistory() {
  const history = uni.getStorageSync(SEARCH_HISTORY_KEY) || []
  searchHistory.value = history
}

function showClearHistoryModal() {
  clearHistoryPopupRef.value?.open()
}

function closeClearHistoryModal() {
  clearHistoryPopupRef.value?.close()
}

function clearSearchHistory() {
  uni.removeStorageSync(SEARCH_HISTORY_KEY)
  searchHistory.value = []
  closeClearHistoryModal()
  uni.showToast({
    title: '已清空',
    icon: 'success'
  })
}

// 筛选条件变化处理
function onCategoryChange(e) {
  selectedCategoryIndex.value = e.detail.value
  selectedCategoryId.value = categoryOptions.value[e.detail.value]?.id
  page.value = 1
  noMore.value = false
  fetchProductList(true)
}

function onCampusChange(e) {
  selectedCampusIndex.value = e.detail.value
  selectedCampusId.value = campusOptions.value[e.detail.value]?.id
  page.value = 1
  noMore.value = false
  fetchProductList(true)
}

function onPriceRangeChange(e) {
  selectedPriceIndex.value = e.detail.value
  const selectedText = priceRangeOptions[e.detail.value]

  if (selectedText === '价格不限') {
    priceRange.value = { min: '', max: '' }
    page.value = 1
    noMore.value = false
    fetchProductList(true)
  } else {
    const [min, max] = selectedText.split('-').map(Number)
    priceRange.value = { min: String(min), max: String(max) }
    page.value = 1
    noMore.value = false
    fetchProductList(true)
  }
}

function onSortChange(e) {
  selectedSortIndex.value = e.detail.value
  currentSort.value = sortOptions[e.detail.value].value
  page.value = 1
  noMore.value = false
  fetchProductList(true)
}

// 获取热门关键词
async function fetchHotKeywords() {
  try {
    const data = await get('/mini/search/hot-keywords', {}, { showLoading: false })
    hotKeywords.value = data || []
  } catch (err) {
    console.error('获取热门搜索失败', err)
  }
}

// 获取分类列表
async function fetchCategories() {
  try {
    const data = await get('/mini/category/list', {}, { showLoading: false })
    categories.value = data || []
  } catch (err) {
    console.error('获取分类列表失败', err)
  }
}

// 获取校区列表
async function fetchCampuses() {
  try {
    const data = await get('/mini/campus/list', {}, { showLoading: false })
    campuses.value = data || []
  } catch (err) {
    console.error('获取校区列表失败', err)
  }
}

// 获取商品列表
async function fetchProductList(reset = false) {
  if (loading.value) return

  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize,
      keyword: keyword.value,
      campusId: selectedCampusId.value || undefined,
      categoryId: selectedCategoryId.value || undefined,
      sortBy: currentSort.value,
      minPrice: priceRange.value.min || undefined,
      maxPrice: priceRange.value.max || undefined
    }

    const data = await get('/mini/product/list', params)

    if (reset) {
      productList.value = data.records || []
    } else {
      productList.value = [...productList.value, ...(data.records || [])]
    }
    total.value = data.total || 0

    if (productList.value.length >= total.value) {
      noMore.value = true
    }
  } catch (err) {
    console.error('获取商品列表失败', err)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 上拉加载更多
function loadMore() {
  if (noMore.value || loading.value) return
  page.value++
  fetchProductList(false)
}

// 监听页面滚动到底部
onLoad(async (options = {}) => {
  loadSearchHistory()

  const categoryId = options.categoryId ? Number(options.categoryId) : null
  const initialKeyword = options.keyword ? decodeURIComponent(options.keyword) : ''

  if (initialKeyword) {
    keyword.value = initialKeyword
  }
  if (categoryId) {
    selectedCategoryId.value = categoryId
  }

  await Promise.all([fetchHotKeywords(), fetchCategories(), fetchCampuses()])

  if (categoryId) {
    const index = categories.value.findIndex(c => c.id === categoryId)
    if (index >= 0) {
      selectedCategoryIndex.value = index + 1
    } else {
      selectedCategoryId.value = null
    }
  }

  if (initialKeyword || categoryId) {
    executeSearch({ allowEmptyKeyword: !!categoryId, recordHistory: !!initialKeyword })
  }

  focusInput()
})

// 页面滚动处理
onReachBottom(() => {
  if (hasSearched.value) {
    loadMore()
  }
})

// 下拉刷新
onPullDownRefresh(() => {
  if (hasSearched.value) {
    page.value = 1
    noMore.value = false
    fetchProductList(true).then(() => {
      uni.stopPullDownRefresh()
    })
  } else {
    uni.stopPullDownRefresh()
  }
})

onMounted(() => {
  const info = uni.getSystemInfoSync()
  if (info && info.statusBarHeight) {
    statusBarHeight.value = info.statusBarHeight
  }
  const menuButton = typeof uni.getMenuButtonBoundingClientRect === 'function'
    ? uni.getMenuButtonBoundingClientRect()
    : null
  if (menuButton && menuButton.top) {
    const padding = menuButton.top - statusBarHeight.value
    navBarHeight.value = menuButton.height + padding * 2
  } else {
    navBarHeight.value = 44
  }
})
</script>

<style lang="scss" scoped>
.search-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

.search-page,
.search-page view,
.search-page text,
.search-page image,
.search-page input,
.search-page picker {
  box-sizing: border-box;
}

/* 顶部搜索栏 */
.search-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background-color: var(--bg-white);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.06);
}

.search-header__back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-header__back-icon {
  font-size: 36rpx;
  color: var(--text-regular);
}

.search-header__input-wrap {
  flex: 1;
  position: relative;
  height: 72rpx;
}

.search-header__search-icon {
  position: absolute;
  left: 18rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-header__search-icon-img {
  width: 32rpx;
  height: 32rpx;
  opacity: 0.55;
}

.search-header__input {
  width: 100%;
  height: 100%;
  padding: 0 72rpx 0 72rpx;
  background-color: var(--bg-grey);
  border: 2rpx solid transparent;
  border-radius: 999rpx;
  font-size: var(--font-md);
  color: var(--text-primary);
}

.search-header__input::placeholder {
  color: var(--text-placeholder);
}

.search-header__clear {
  position: absolute;
  right: 16rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-header__clear-icon {
  font-size: 34rpx;
  line-height: 1;
  color: var(--text-placeholder);
}

/* 搜索内容区域 */
.search-content {
  padding: 24rpx;
}

.search-card {
  background-color: var(--bg-white);
  border-radius: 24rpx;
  padding: 24rpx 24rpx 28rpx;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}

.search-card + .search-card {
  margin-top: 24rpx;
}

.search-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.search-card__title {
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-regular);
}

.search-card__action {
  padding: 8rpx 6rpx;
}

.search-card__action-text {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

/* 标签云 */
.search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.search-tag {
  padding: 14rpx 24rpx;
  border-radius: 999rpx;
  font-size: var(--font-sm);
  font-weight: 600;
  color: var(--primary-color);
  background-color: var(--primary-bg);
}

.search-tags--history .search-tag {
  font-weight: 500;
  color: var(--text-regular);
  background-color: var(--bg-grey);
}

.search-tags--hot .search-tag:nth-child(4n + 2) {
  color: #1f9d55;
  background-color: rgba(82, 196, 26, 0.12);
}

.search-tags--hot .search-tag:nth-child(4n + 3) {
  color: #2f80ed;
  background-color: rgba(74, 144, 217, 0.14);
}

.search-tags--hot .search-tag:nth-child(4n) {
  color: #4a4ff0;
  background-color: rgba(79, 70, 229, 0.12);
}

/* 搜索结果 */
.search-result {
  padding: 24rpx;
}

/* 筛选栏 */
.filter-section {
  padding: 24rpx;
  margin-top: 0;
  background-color: var(--bg-white);
  border-radius: 28rpx;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}

.filter-section__title {
  font-size: var(--font-sm);
  font-weight: 600;
  color: var(--text-secondary);
}

.filter-grid {
  margin-top: 18rpx;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
}

.filter-cell {
  height: 84rpx;
  border-radius: 22rpx;
  border: 2rpx solid rgba(148, 163, 184, 0.35);
  background-color: var(--bg-white);
}

.filter-cell__inner {
  height: 84rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-cell__label {
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-primary);
}

.filter-cell__arrow {
  font-size: 26rpx;
  color: var(--text-placeholder);
}

/* 结果头部 */
.result-header {
  margin-top: 22rpx;
  padding: 0;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.result-header__title {
  font-size: var(--font-sm);
  font-weight: 600;
  color: var(--text-secondary);
}

.result-header__count {
  font-size: var(--font-xs);
  color: var(--text-placeholder);
}

/* 结果列表 */
.result-list {
  margin-top: 18rpx;
  padding-bottom: 24rpx;
}

.result-loading,
.result-no-more {
  text-align: center;
  padding: 32rpx;
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

</style>
