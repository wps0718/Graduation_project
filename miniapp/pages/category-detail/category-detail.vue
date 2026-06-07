<template>
  <view class="page">
    <view class="sticky-header">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="search-wrapper" @tap="goSearch">
        <image src="/static/svg/search.svg" class="search-icon" />
        <text class="search-placeholder">搜索{{ categoryName || '商品' }}</text>
      </view>
    </view>

    <!-- 筛选按钮栏 -->
    <scroll-view class="filter-bar" scroll-x :show-scrollbar="false" :style="filterBarStyle">
      <view class="filter-tabs">
        <view
          class="filter-tab"
          :class="{ active: currentCategoryFilter }"
          @tap="openFilter('category')"
        >
          <text class="tab-text">{{ currentCategoryName || categoryName || '全部分类' }}</text>
          <text class="tab-arrow" :class="{ rotated: showFilterPopup && filterType === 'category' }">▾</text>
        </view>
        <view
          class="filter-tab"
          :class="{ 'has-value': filters.campusId }"
          @tap="openFilter('campus')"
        >
          <text class="tab-text">{{ currentCampusLabel || '校区' }}</text>
          <text class="tab-arrow">▾</text>
        </view>
        <view
          class="filter-tab"
          :class="{ 'has-value': filters.minPrice || filters.maxPrice }"
          @tap="openFilter('price')"
        >
          <text class="tab-text">{{ priceLabel || '价格' }}</text>
          <text class="tab-arrow">▾</text>
        </view>
        <view
          class="filter-tab"
          :class="{ active: filters.sortBy !== 'latest' }"
          @tap="openFilter('sort')"
        >
          <text class="tab-text">{{ sortLabel }}</text>
          <text class="tab-arrow" :class="{ rotated: showFilterPopup && filterType === 'sort' }">▾</text>
        </view>
      </view>
    </scroll-view>
    </view>

    <!-- 结果统计 + 筛选标签 -->
    <view v-if="!loading || hasFilter" class="result-header">
      <view v-if="!loading && productList.length > 0" class="result-info">
        <text class="result-count">找到 </text>
        <text class="result-num">{{ total }}</text>
        <text class="result-count"> 件商品</text>
        <text v-if="hasFilter" class="result-tip">已筛选</text>
      </view>

      <scroll-view v-if="hasFilter" class="filter-tags" scroll-x :show-scrollbar="false">
        <view class="tag-list">
          <view v-if="filters.campusId" class="filter-tag" @tap="clearFilter('campusId')">
            <text>{{ currentCampusLabel }}</text>
            <text class="tag-close">✕</text>
          </view>
          <view v-if="filters.minPrice || filters.maxPrice" class="filter-tag" @tap="clearFilter('price')">
            <text>{{ priceLabel }}</text>
            <text class="tag-close">✕</text>
          </view>
          <view v-if="filters.sortBy !== 'latest'" class="filter-tag" @tap="clearFilter('sortBy')">
            <text>{{ sortLabel }}</text>
            <text class="tag-close">✕</text>
          </view>
          <view class="filter-tag clear-all" @tap="resetFilters">
            <text class="clear-icon">↻</text>
            <text>清空</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading && productList.length === 0" class="skeleton-wrapper">
      <view class="skeleton-grid">
        <view v-for="i in 6" :key="i" class="skeleton-card">
          <view class="skeleton-image shimmer" />
          <view class="skeleton-body">
            <view class="skeleton-line w80 shimmer" />
            <view class="skeleton-line w50 shimmer" />
            <view class="skeleton-line w30 shimmer" />
          </view>
        </view>
      </view>
    </view>

    <!-- 商品列表（双列瀑布流） -->
    <view v-else-if="productList.length > 0" class="waterfall">
      <view class="waterfall-col">
        <view
          v-for="item in leftList"
          :key="item.id"
          class="product-card"
          @tap="goDetail(item)"
        >
          <view class="card-img-wrap">
            <image
              :src="item.coverImage"
              class="card-img"
              mode="aspectFill"
              lazy-load
              @tap.stop="previewImage(item)"
            />
            <view v-if="item.sellerAuthStatus === 2" class="verified-badge">认</view>
            <view class="favorite-btn" @tap.stop="toggleFavorite(item)">
              <image :src="item.isFavorited ? '/static/svg/favorited.svg' : '/static/svg/favorite.svg'" class="fav-icon" />
            </view>
          </view>
          <view class="card-body">
            <text class="card-title">{{ item.title }}</text>
            <view v-if="getConditionText(item.conditionLevel)" class="card-condition">
              <text class="condition-tag">{{ getConditionText(item.conditionLevel) }}</text>
            </view>
            <view class="card-price">
              <text class="price-sym">¥</text>
              <text class="price-val">{{ formatPrice(item.price) }}</text>
              <text v-if="item.originalPrice" class="price-orig">¥{{ formatPrice(item.originalPrice) }}</text>
            </view>
            <view class="card-meta">
              <text class="meta-campus">{{ item.campusName || '' }}</text>
              <view class="meta-stats">
                <text class="stat-item">{{ item.viewCount || 0 }}浏览</text>
              </view>
            </view>
            <view class="card-seller" v-if="item.sellerNickName">
              <image v-if="item.sellerAvatarUrl" :src="resolveImageUrl(item.sellerAvatarUrl)" class="seller-avatar" mode="aspectFill" />
              <view v-else class="seller-avatar avatar-placeholder">{{ item.sellerNickName[0] }}</view>
              <text class="seller-name">{{ item.sellerNickName }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="waterfall-col">
        <view
          v-for="item in rightList"
          :key="item.id"
          class="product-card"
          @tap="goDetail(item)"
        >
          <view class="card-img-wrap">
            <image
              :src="item.coverImage"
              class="card-img"
              mode="aspectFill"
              lazy-load
              @tap.stop="previewImage(item)"
            />
            <view v-if="item.sellerAuthStatus === 2" class="verified-badge">认</view>
            <view class="favorite-btn" @tap.stop="toggleFavorite(item)">
              <image :src="item.isFavorited ? '/static/svg/favorited.svg' : '/static/svg/favorite.svg'" class="fav-icon" />
            </view>
          </view>
          <view class="card-body">
            <text class="card-title">{{ item.title }}</text>
            <view v-if="getConditionText(item.conditionLevel)" class="card-condition">
              <text class="condition-tag">{{ getConditionText(item.conditionLevel) }}</text>
            </view>
            <view class="card-price">
              <text class="price-sym">¥</text>
              <text class="price-val">{{ formatPrice(item.price) }}</text>
              <text v-if="item.originalPrice" class="price-orig">¥{{ formatPrice(item.originalPrice) }}</text>
            </view>
            <view class="card-meta">
              <text class="meta-campus">{{ item.campusName || '' }}</text>
              <view class="meta-stats">
                <text class="stat-item">{{ item.viewCount || 0 }}浏览</text>
              </view>
            </view>
            <view class="card-seller" v-if="item.sellerNickName">
              <image v-if="item.sellerAvatarUrl" :src="resolveImageUrl(item.sellerAvatarUrl)" class="seller-avatar" mode="aspectFill" />
              <view v-else class="seller-avatar avatar-placeholder">{{ item.sellerNickName[0] }}</view>
              <text class="seller-name">{{ item.sellerNickName }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!loading && !loadError" class="empty-wrapper">
      <view class="empty-content">
        <text class="empty-emoji"> </text>
        <text class="empty-title">{{ hasFilter ? '没有找到相关商品' : '暂无商品' }}</text>
        <text class="empty-desc">{{ hasFilter ? '换个筛选条件试试' : '换个分类试试吧' }}</text>
        <view v-if="hasFilter" class="empty-btn" @tap="resetFilters">
          <text>清空筛选条件</text>
        </view>
        <view v-else-if="otherCategories.length > 0" class="recommend-section">
          <text class="recommend-title">试试其他分类</text>
          <scroll-view class="recommend-scroll" scroll-x :show-scrollbar="false">
            <view class="recommend-list">
              <view
                v-for="cat in otherCategories"
                :key="cat.id"
                class="recommend-item"
                @tap="switchCategory(cat)"
              >
                <text class="recommend-icon">{{ getCategoryEmoji(cat.name) }}</text>
                <text class="recommend-name">{{ cat.name }}</text>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>

    <!-- 错误状态 -->
    <view v-else-if="loadError" class="error-wrapper">
      <view class="error-content">
        <text class="error-emoji">⚡</text>
        <text class="error-title">加载失败</text>
        <text class="error-desc">网络开小了，请稍后重试</text>
        <view class="error-btn" @tap="retryLoad">
          <text>点击重试</text>
        </view>
      </view>
    </view>

    <!-- 加载更多 -->
    <view v-if="productList.length > 0" class="loadmore-wrapper">
      <view v-if="loading" class="loadmore-loading">
        <view class="loading-dots">
          <view class="dot" />
          <view class="dot" />
          <view class="dot" />
        </view>
        <text class="loadmore-text">加载中...</text>
      </view>
      <view v-else-if="!hasMore" class="loadmore-nomore">
        <view class="nomore-line" />
        <text class="nomore-text">— 没有更多了 —</text>
        <view class="nomore-line" />
      </view>
    </view>

    <!-- 筛选弹窗 -->
    <view v-if="showFilterPopup" class="filter-mask" @tap="onMaskClick">
      <view class="filter-drawer" @tap.stop>
        <view class="drawer-header">
          <view class="drawer-handle" />
          <text class="drawer-title">{{ filterTitle }}</text>
          <view class="drawer-close" @tap="showFilterPopup = false">
            <text class="close-icon">✕</text>
          </view>
        </view>

        <scroll-view scroll-y class="drawer-body" :show-scrollbar="false">
          <!-- 分类选择 -->
          <view v-if="filterType === 'category'" class="filter-grid">
            <view
              class="grid-item"
              :class="{ active: tempFilters.categoryId === null }"
              @tap="tempFilters.categoryId = null"
            >
              <text class="grid-emoji">📋</text>
              <text class="grid-text">全部</text>
            </view>
            <view
              v-for="item in categoryList"
              :key="item.id"
              class="grid-item"
              :class="{ active: tempFilters.categoryId === item.id }"
              @tap="tempFilters.categoryId = item.id"
            >
              <text class="grid-emoji">{{ getCategoryEmoji(item.name) }}</text>
              <text class="grid-text">{{ item.name }}</text>
              <view v-if="tempFilters.categoryId === item.id" class="grid-check">✓</view>
            </view>
          </view>

          <!-- 校区筛选 -->
          <view v-if="filterType === 'campus'" class="filter-list">
            <view
              class="list-item"
              :class="{ active: tempFilters.campusId === null }"
              @tap="tempFilters.campusId = null"
            >
              <text class="list-label">不限校区</text>
              <view class="list-radio" :class="{ checked: tempFilters.campusId === null }" />
            </view>
            <view
              v-for="item in campusList"
              :key="item.id"
              class="list-item"
              :class="{ active: tempFilters.campusId === item.id }"
              @tap="tempFilters.campusId = item.id"
            >
              <text class="list-label">{{ item.name }}</text>
              <view class="list-radio" :class="{ checked: tempFilters.campusId === item.id }" />
            </view>
          </view>

          <!-- 价格筛选 -->
          <view v-if="filterType === 'price'" class="filter-price">
            <text class="section-label">快捷选择</text>
            <view class="price-tags">
              <view
                v-for="range in priceRanges"
                :key="range.label"
                class="price-tag"
                :class="{ active: isPriceRangeActive(range) }"
                @tap="selectPriceRange(range)"
              >
                <text>{{ range.label }}</text>
              </view>
            </view>
            <text class="section-label">自定义价格</text>
            <view class="price-input-row">
              <input
                v-model="tempFilters.minPrice"
                class="price-input"
                type="number"
                placeholder="最低价"
              />
              <text class="price-sep">—</text>
              <input
                v-model="tempFilters.maxPrice"
                class="price-input"
                type="number"
                placeholder="最高价"
              />
            </view>
          </view>

          <!-- 排序 -->
          <view v-if="filterType === 'sort'" class="filter-list">
            <view
              v-for="item in sortOptions"
              :key="item.value"
              class="list-item"
              :class="{ active: tempFilters.sortBy === item.value }"
              @tap="tempFilters.sortBy = item.value"
            >
              <text class="list-label">{{ item.label }}</text>
              <view class="list-radio" :class="{ checked: tempFilters.sortBy === item.value }" />
            </view>
          </view>
        </scroll-view>

        <view class="drawer-footer">
          <view class="btn-reset" @tap="resetTempFilters">
            <text>重置</text>
          </view>
          <view class="btn-confirm" @tap="confirmFilter">
            <text>确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useAppStore } from '@/store/app'
import { get, post } from '@/utils/request'
import { normalizeProductCardData, resolveImageUrl } from '@/utils/image'

const appStore = useAppStore()
const categoryId = ref(null)
const categoryName = ref('')

// 数据
const categoryList = ref([])
const campusList = ref([])
const productList = ref([])
const page = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const loadError = ref(false)
const capsuleRightPadding = ref(0)

// 筛选
const filters = ref({
  categoryId: null,
  campusId: null,
  minPrice: null,
  maxPrice: null,
  sortBy: 'latest'
})

const showFilterPopup = ref(false)
const filterType = ref('category')
const tempFilters = ref({
  categoryId: null,
  campusId: null,
  minPrice: null,
  maxPrice: null,
  sortBy: 'latest'
})

const priceRanges = [
  { label: '0-50', min: 0, max: 50 },
  { label: '50-100', min: 50, max: 100 },
  { label: '100-500', min: 100, max: 500 },
  { label: '500以上', min: 500, max: null }
]

const sortOptions = [
  { label: '综合排序', value: 'latest' },
  { label: '价格从低到高', value: 'price_asc' },
  { label: '价格从高到低', value: 'price_desc' }
]

const CATEGORY_EMOJI = {
  书籍: '📚',
  服饰: '👗',
  服饰鞋包: '👗',
  生活: '☕',
  生活设备: '☕',
  电子设备: '💻',
  运动设备: '⚽',
  潮玩娱乐: '🎮',
  代拿快递: '📦',
  快递: '📦'
}

const CONDITION_MAP = {
  1: '全新',
  2: '几乎全新',
  3: '轻微使用痕迹',
  4: '明显使用痕迹',
  5: '有缺陷'
}

// 双列分配
const leftList = computed(() => {
  return productList.value.filter((_, i) => i % 2 === 0)
})

const rightList = computed(() => {
  return productList.value.filter((_, i) => i % 2 === 1)
})

const hasMore = computed(() => productList.value.length < total.value)

const hasFilter = computed(() => {
  return filters.value.campusId ||
    filters.value.minPrice ||
    filters.value.maxPrice ||
    filters.value.sortBy !== 'latest'
})

const currentCategoryFilter = computed(() => {
  return filters.value.categoryId !== categoryId.value
})

const currentCategoryName = computed(() => {
  if (!filters.value.categoryId) return ''
  if (filters.value.categoryId === categoryId.value) return categoryName.value
  const cat = categoryList.value.find(c => c.id === filters.value.categoryId)
  return cat?.name || ''
})

const currentCampusLabel = computed(() => {
  if (!filters.value.campusId) return ''
  const campus = campusList.value.find(c => c.id === filters.value.campusId)
  return campus?.name || ''
})

const priceLabel = computed(() => {
  const min = filters.value.minPrice
  const max = filters.value.maxPrice
  if (!min && !max) return ''
  if (min && max) return `${min}-${max}元`
  if (min) return `${min}元以上`
  return `${max}元以下`
})

const sortLabel = computed(() => {
  const opt = sortOptions.find(s => s.value === filters.value.sortBy)
  return opt?.label || '综合排序'
})

const filterTitle = computed(() => {
  const map = { category: '选择分类', campus: '校区筛选', price: '价格区间', sort: '排序方式' }
  return map[filterType.value] || '筛选'
})

const filterBarStyle = computed(() => {
  if (capsuleRightPadding.value > 0) {
    return { paddingRight: `${capsuleRightPadding.value}px` }
  }
  return {}
})

const otherCategories = computed(() => {
  return categoryList.value.filter(c => c.id !== categoryId.value).slice(0, 6)
})

// 工具
function getCategoryEmoji(name) {
  return CATEGORY_EMOJI[name] || ' '
}

function getConditionText(level) {
  return CONDITION_MAP[level] || ''
}

function formatPrice(price) {
  if (price == null) return '0'
  const num = Number(price)
  if (num === Math.floor(num)) return String(Math.floor(num))
  return num.toFixed(2).replace(/\.?0+$/, '')
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

// 页面
onLoad(async (query) => {
  categoryId.value = query.categoryId ? Number(query.categoryId) : null
  categoryName.value = decodeURIComponent(query.name || '') || '分类'
  filters.value.categoryId = categoryId.value

  // 适配微信胶囊按钮，避免筛选栏被遮挡
  try {
    const menu = uni.getMenuButtonBoundingClientRect()
    const sys = uni.getSystemInfoSync()
    if (menu && sys) {
      capsuleRightPadding.value = sys.windowWidth - menu.right + 20
    }
  } catch(e) {}

  await Promise.all([loadCategoryList(), loadCampusList()])
  await loadProducts(true)
})

onPullDownRefresh(async () => {
  await loadProducts(true)
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    loadProducts(false)
  }
})

async function loadCategoryList() {
  try {
    if (appStore.categoryList && appStore.categoryList.length > 0) {
      categoryList.value = appStore.categoryList
    } else {
      await appStore.loadCategoryList()
      categoryList.value = appStore.categoryList
    }
  } catch (e) {
    console.error('加载分类列表失败', e)
  }
}

async function loadCampusList() {
  try {
    if (appStore.campusList && appStore.campusList.length > 0) {
      campusList.value = appStore.campusList
    } else {
      await appStore.loadCampusList()
      campusList.value = appStore.campusList
    }
  } catch (e) {
    console.error('加载校区列表失败', e)
  }
}

async function loadProducts(refresh = false) {
  if (loading.value) return

  if (refresh) {
    page.value = 1
  }

  loading.value = true
  loadError.value = false

  try {
    const params = {
      page: page.value,
      pageSize,
      categoryId: filters.value.categoryId
    }
    if (filters.value.campusId) params.campusId = filters.value.campusId
    if (filters.value.minPrice) params.minPrice = filters.value.minPrice
    if (filters.value.maxPrice) params.maxPrice = filters.value.maxPrice
    if (filters.value.sortBy && filters.value.sortBy !== 'latest') {
      params.sortBy = filters.value.sortBy
    }

    const res = await get('/mini/product/list', params, { showLoading: false })

    const list = (res?.records || []).map((item) => {
      const version = item && (item.updateTime || item.createTime || item.id)
      const normalized = normalizeProductCardData(item, { version })
      normalized.isFavorited = false
      return normalized
    })

    total.value = res?.total || 0

    if (refresh) {
      productList.value = list
    } else {
      productList.value = [...productList.value, ...list]
    }

    if (list.length >= pageSize) {
      page.value++
    }
  } catch (e) {
    console.error('加载商品列表失败', e)
    loadError.value = true
  } finally {
    loading.value = false
  }
}

function retryLoad() {
  loadError.value = false
  loadProducts(true)
}

function openFilter(type) {
  filterType.value = type
  tempFilters.value = { ...filters.value }
  showFilterPopup.value = true
}

function onMaskClick(e) {
  // 防止抽屉内部事件冒泡导致误关闭
  if (e && e.target && e.currentTarget && e.target !== e.currentTarget) return
  showFilterPopup.value = false
}

function resetTempFilters() {
  if (filterType.value === 'category') {
    tempFilters.value.categoryId = categoryId.value
  } else if (filterType.value === 'campus') {
    tempFilters.value.campusId = null
  } else if (filterType.value === 'price') {
    tempFilters.value.minPrice = null
    tempFilters.value.maxPrice = null
  } else if (filterType.value === 'sort') {
    tempFilters.value.sortBy = 'latest'
  }
}

function confirmFilter() {
  filters.value = { ...tempFilters.value }
  showFilterPopup.value = false
  loadProducts(true)
}

function selectPriceRange(range) {
  tempFilters.value.minPrice = range.min
  tempFilters.value.maxPrice = range.max === null ? null : range.max
}

function isPriceRangeActive(range) {
  const min = Number(tempFilters.value.minPrice)
  const max = tempFilters.value.maxPrice == null ? null : Number(tempFilters.value.maxPrice)
  return min === range.min && (max === range.max || (range.max == null && max == null))
}

function clearFilter(key) {
  if (key === 'campusId') {
    filters.value.campusId = null
  } else if (key === 'price') {
    filters.value.minPrice = null
    filters.value.maxPrice = null
  } else if (key === 'sortBy') {
    filters.value.sortBy = 'latest'
  }
  loadProducts(true)
}

function resetFilters() {
  filters.value = {
    categoryId: categoryId.value,
    campusId: null,
    minPrice: null,
    maxPrice: null,
    sortBy: 'latest'
  }
  tempFilters.value = { ...filters.value }
  loadProducts(true)
}


function goSearch() {
  uni.navigateTo({
    url: `/pages/login-sub/search/search?categoryId=${categoryId.value || ''}`
  })
}

async function toggleFavorite(item) {
  if (!item || !item.id) return
  try {
    if (item.isFavorited) {
      await post('/mini/favorite/cancel', { productId: item.id })
      item.isFavorited = false
      item.favoriteCount = Math.max(0, (item.favoriteCount || 0) - 1)
    } else {
      await post('/mini/favorite/add', { productId: item.id })
      item.isFavorited = true
      item.favoriteCount = (item.favoriteCount || 0) + 1
    }
  } catch (e) {
    console.error('收藏操作失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function goDetail(item) {
  if (!item || !item.id) return
  uni.navigateTo({
    url: `/pages/product-sub/detail/detail?id=${item.id}`
  })
}

function previewImage(item) {
  if (!item || !item.images || item.images.length === 0) return
  const urls = item.images.map(url => resolveImageUrl(url))
  uni.previewImage({
    urls,
    current: urls[0]
  })
}

function switchCategory(cat) {
  uni.redirectTo({
    url: `/pages/category-detail/category-detail?categoryId=${cat.id}&name=${encodeURIComponent(cat.name || '')}`
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

/* ========== 吸顶容器 ========== */
.sticky-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #4A90D9 0%, #3A7BC8 100%);
}

/* ========== 导航 ========== */
.navbar {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 24rpx;
  background: linear-gradient(135deg, #4A90D9 0%, #3A7BC8 100%);
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 217, 0.25);
}


.search-wrapper {
  flex: 1;
  height: 64rpx;
  margin: 0;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.search-icon {
  width: 30rpx;
  height: 30rpx;
  margin-right: 12rpx;
  opacity: 0.5;
}

.search-placeholder {
  font-size: 26rpx;
  color: #999;
}

/* ========== 筛选栏 ========== */
.filter-bar {
  background: #fff;
  white-space: nowrap;
  border-bottom: 1rpx solid #F0F0F0;
}

.filter-tabs {
  display: inline-flex;
  padding: 16rpx 24rpx;
  gap: 24rpx;
}

.filter-tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  padding: 12rpx 24rpx;
  background: #F7F8FA;
  border-radius: 40rpx;
  border: 2rpx solid transparent;
  transition: all 0.25s ease;

  .tab-text {
    font-size: 26rpx;
    color: var(--text-regular);
    margin-right: 6rpx;
    max-width: 160rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .tab-arrow {
    font-size: 18rpx;
    color: var(--text-secondary);
    transition: transform 0.25s ease;
    &.rotated { transform: rotate(180deg); }
  }

  &.active {
    background: #fff;
    .tab-text { color: #4A90D9; font-weight: 600; }
    .tab-arrow { color: #4A90D9; }
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60%;
      height: 4rpx;
      background: #4A90D9;
      border-radius: 2rpx;
    }
  }

  &.has-value {
    background: #E8F4FF;
    border-color: #4A90D9;
    .tab-text { color: #4A90D9; font-weight: 500; }
    .tab-arrow { color: #4A90D9; }
  }
}

/* ========== 结果统计 ========== */
.result-header {
  background: #fff;
  padding: 12rpx 24rpx;
}

.result-info {
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
}

.result-count {
  font-size: 26rpx;
  color: var(--text-secondary);
}

.result-num {
  font-size: 32rpx;
  font-weight: bold;
  color: #4A90D9;
  margin: 0 4rpx;
}

.result-tip {
  font-size: 22rpx;
  color: #FF9800;
  padding: 2rpx 12rpx;
  background: #FFF8E1;
  border-radius: 6rpx;
  margin-left: 12rpx;
}

.filter-tags {
  white-space: nowrap;
}

.tag-list {
  display: inline-flex;
  gap: 12rpx;
}

.filter-tag {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 16rpx;
  background: #E8F4FF;
  border-radius: 24rpx;
  font-size: 22rpx;
  color: #4A90D9;
  transition: all 0.2s ease;

  .tag-close {
    margin-left: 8rpx;
    font-size: 18rpx;
    opacity: 0.6;
  }

  &.clear-all {
    background: #FFF0F0;
    color: #FF6B6B;
    .clear-icon { margin-right: 4rpx; font-size: 22rpx; }
  }

  &:active { opacity: 0.7; transform: scale(0.95); }
}

/* ========== 骨架屏 ========== */
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.shimmer {
  background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 37%, #F0F0F0 63%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

.skeleton-wrapper {
  padding: 24rpx;
}

.skeleton-grid {
  display: flex;
  gap: 16rpx;
}

.skeleton-card {
  flex: 1;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
}

.skeleton-image {
  width: 100%;
  height: 340rpx;
}

.skeleton-body {
  padding: 16rpx;
}

.skeleton-line {
  height: 28rpx;
  border-radius: 6rpx;
  margin-bottom: 12rpx;

  &.w80 { width: 80%; }
  &.w50 { width: 50%; height: 32rpx; }
  &.w30 { width: 30%; height: 24rpx; margin-bottom: 0; }
}

/* ========== 瀑布流商品列表 ========== */
.waterfall {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 24rpx;
}

.waterfall-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.product-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.15s ease, box-shadow 0.15s ease;

  &:active {
    transform: scale(0.97);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  }
}

.card-img-wrap {
  position: relative;
  width: 100%;
  height: 340rpx;
  background: #F5F5F5;
}

.card-img {
  width: 100%;
  height: 100%;
}

.favorite-btn {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;

  .fav-icon {
    width: 28rpx;
    height: 28rpx;
  }

  &:active {
    transform: scale(0.9);
  }
}

.verified-badge {
  position: absolute;
  bottom: 12rpx;
  right: 12rpx;
  width: 36rpx;
  height: 36rpx;
  background: rgba(74, 144, 217, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  color: #fff;
  font-weight: bold;
}

.card-condition {
  margin-bottom: 8rpx;

  .condition-tag {
    display: inline-block;
    padding: 2rpx 12rpx;
    background: #FFF3E0;
    color: #FF8F00;
    font-size: 20rpx;
    border-radius: 4rpx;
  }
}

.card-body {
  padding: 16rpx;
}

.card-title {
  font-size: 26rpx;
  color: var(--text-primary);
  line-height: 36rpx;
  font-weight: 500;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin-bottom: 10rpx;
}

.card-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 10rpx;
}

.price-sym {
  font-size: 22rpx;
  color: #FF6B6B;
  font-weight: bold;
  margin-right: 2rpx;
}

.price-val {
  font-size: 34rpx;
  color: #FF6B6B;
  font-weight: bold;
  font-family: 'DIN Alternate', 'Helvetica Neue', Arial, sans-serif;
  letter-spacing: -1rpx;
}

.price-orig {
  font-size: 20rpx;
  color: var(--text-placeholder);
  text-decoration: line-through;
  margin-left: 8rpx;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.meta-campus {
  display: inline-block;
  padding: 2rpx 10rpx;
  background: #E8F4FF;
  color: #4A90D9;
  font-size: 20rpx;
  border-radius: 4rpx;
  max-width: 140rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-stats {
  display: flex;
  align-items: center;
}

.stat-item {
  font-size: 20rpx;
  color: var(--text-regular);
}

.card-seller {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid #F5F5F5;

  .seller-avatar {
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    overflow: hidden;
    flex-shrink: 0;

    &.avatar-placeholder {
      background: #E8F4FF;
      color: #4A90D9;
      font-size: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
    }
  }

  .seller-name {
    font-size: 22rpx;
    color: var(--text-secondary);
    margin-left: 8rpx;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* ========== 空状态 ========== */
.empty-wrapper {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-content {
  text-align: center;
  padding: 48rpx 24rpx;
}

.empty-emoji {
  display: block;
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.empty-title {
  display: block;
  font-size: 32rpx;
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 12rpx;
}

.empty-desc {
  display: block;
  font-size: 26rpx;
  color: var(--text-secondary);
  margin-bottom: 48rpx;
}

.empty-btn {
  display: inline-flex;
  align-items: center;
  padding: 16rpx 48rpx;
  background: linear-gradient(135deg, #4A90D9 0%, #3A7BC8 100%);
  border-radius: 48rpx;
  box-shadow: 0 8rpx 20rpx rgba(74, 144, 217, 0.3);

  text {
    font-size: 28rpx;
    color: #fff;
    font-weight: 600;
  }

  &:active { transform: scale(0.96); }
}

.recommend-section {
  margin-top: 16rpx;
}

.recommend-title {
  display: block;
  font-size: 26rpx;
  color: var(--text-secondary);
  margin-bottom: 24rpx;
}

.recommend-scroll {
  white-space: nowrap;
}

.recommend-list {
  display: inline-flex;
  gap: 16rpx;
}

.recommend-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  min-width: 140rpx;
  transition: all 0.2s ease;

  &:active { transform: scale(0.95); background: #EDEDF0; }
}

.recommend-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.recommend-name {
  font-size: 22rpx;
  color: var(--text-regular);
}

/* ========== 错误状态 ========== */
.error-wrapper {
  min-height: 50vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-content {
  text-align: center;
  padding: 48rpx 24rpx;
}

.error-emoji {
  display: block;
  font-size: 100rpx;
  margin-bottom: 24rpx;
}

.error-title {
  display: block;
  font-size: 32rpx;
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 12rpx;
}

.error-desc {
  display: block;
  font-size: 26rpx;
  color: var(--text-secondary);
  margin-bottom: 48rpx;
}

.error-btn {
  display: inline-flex;
  align-items: center;
  padding: 16rpx 48rpx;
  background: #F7F8FA;
  border: 2rpx solid #E0E0E0;
  border-radius: 48rpx;
  transition: all 0.2s ease;

  text {
    font-size: 28rpx;
    color: var(--text-regular);
    font-weight: 500;
  }

  &:active {
    transform: scale(0.96);
    background: #EDEDF0;
  }
}

/* ========== 加载更多 ========== */
.loadmore-wrapper {
  padding: 40rpx 24rpx 60rpx;
}

.loadmore-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.loading-dots {
  display: flex;
  gap: 8rpx;

  .dot {
    width: 12rpx;
    height: 12rpx;
    background: #4A90D9;
    border-radius: 50%;
    animation: bounce 1.4s ease-in-out infinite both;
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.loadmore-text {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.loadmore-nomore {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
}

.nomore-line {
  flex: 1;
  height: 1rpx;
  background: linear-gradient(90deg, transparent, #E0E0E0, transparent);
}

.nomore-text {
  font-size: 24rpx;
  color: var(--text-placeholder);
  flex-shrink: 0;
}

/* ========== 筛选弹窗 ========== */
.filter-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.42);
  z-index: 200;
  animation: filterMaskFadeIn 0.18s ease-out;
  @supports (backdrop-filter: blur(6px)) {
    backdrop-filter: blur(6px);
  }
}

.filter-drawer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 72vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 -12rpx 32rpx rgba(15, 23, 42, 0.18);
  animation: filterDrawerSlideUp 0.22s ease-out;
}

.drawer-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  flex-shrink: 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.drawer-handle {
  position: absolute;
  top: 12rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 72rpx;
  height: 8rpx;
  background: rgba(148, 163, 184, 0.55);
  border-radius: 999rpx;
}

.drawer-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-top: 12rpx;
}

.drawer-close {
  position: absolute;
  right: 24rpx;
  top: 28rpx;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  background: rgba(148, 163, 184, 0.16);
  transition: all 0.18s ease;

  &:active {
    transform: scale(0.96);
    background: rgba(148, 163, 184, 0.24);
  }
}

.close-icon {
  font-size: 26rpx;
  color: rgba(15, 23, 42, 0.65);
}

@keyframes filterDrawerSlideUp {
  from {
    transform: translateY(18rpx);
    opacity: 0.98;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes filterMaskFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.drawer-body {
  flex: 1;
  width: 100%;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: 24rpx;
  box-sizing: border-box;
}

/* 分类网格 */
.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.grid-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 12rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.25s ease;

  .grid-emoji {
    font-size: 44rpx;
    margin-bottom: 10rpx;
  }

  .grid-text {
    font-size: 22rpx;
    color: var(--text-regular);
  }

  .grid-check {
    position: absolute;
    top: 6rpx;
    right: 6rpx;
    width: 28rpx;
    height: 28rpx;
    background: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18rpx;
    color: #4A90D9;
    font-weight: bold;
  }

  &.active {
    background: linear-gradient(135deg, #4A90D9 0%, #3A7BC8 100%);
    box-shadow: 0 6rpx 16rpx rgba(74, 144, 217, 0.3);
    transform: translateY(-2rpx);
    .grid-text { color: #fff; font-weight: 600; }
    .grid-check { background: rgba(255, 255, 255, 0.9); }
  }

  &:active { transform: scale(0.96); }
}

/* 列表选项 */
.filter-list {
  .list-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 28rpx 24rpx;
    background: #F7F8FA;
    border-radius: 16rpx;
    margin-bottom: 12rpx;
    border: 2rpx solid transparent;
    transition: all 0.25s ease;

    .list-label {
      font-size: 28rpx;
      color: var(--text-primary);
    }

    .list-radio {
      width: 40rpx;
      height: 40rpx;
      border-radius: 50%;
      border: 3rpx solid #DDD;
      position: relative;
      transition: all 0.25s ease;

      &.checked {
        border-color: #4A90D9;
        background: #4A90D9;
        &::after {
          content: '';
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 14rpx;
          height: 14rpx;
          border-radius: 50%;
          background: #fff;
        }
      }
    }

    &.active {
      background: #E8F4FF;
      border-color: #4A90D9;
      .list-label { color: #4A90D9; font-weight: 600; }
    }

    &:active { transform: scale(0.98); }
  }
}

/* 价格筛选 */
.filter-price {
  .section-label {
    display: block;
    font-size: 26rpx;
    color: var(--text-secondary);
    margin-bottom: 16rpx;
    font-weight: 500;
  }

  .section-label + .price-input-row {
    margin-top: 12rpx;
  }
}

.price-tags {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.price-tag {
  width: 100%;
  padding: 24rpx 0;
  text-align: center;
  background: rgba(248, 250, 252, 0.95);
  border-radius: 16rpx;
  border: 2rpx solid rgba(148, 163, 184, 0.24);
  transition: all 0.2s ease;

  text {
    font-size: 26rpx;
    color: var(--text-primary);
  }

  &.active {
    background: rgba(232, 244, 255, 0.95);
    border-color: rgba(74, 144, 217, 0.8);
    box-shadow: 0 8rpx 20rpx rgba(74, 144, 217, 0.12);
    text { color: #2f7ac6; font-weight: 700; }
  }

  &:active {
    transform: scale(0.985);
    background: rgba(148, 163, 184, 0.12);
  }
}

.price-input-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 8rpx;
}

.price-input {
  flex: 1;
  height: 88rpx;
  padding: 0 24rpx;
  background: rgba(248, 250, 252, 0.95);
  border-radius: 16rpx;
  font-size: 28rpx;
  text-align: center;
  border: 2rpx solid rgba(148, 163, 184, 0.24);
  transition: all 0.2s ease;
}

.price-input:focus {
  border-color: rgba(74, 144, 217, 0.7);
  background: #fff;
  box-shadow: 0 8rpx 20rpx rgba(74, 144, 217, 0.12);
}

.price-input::placeholder {
  color: rgba(15, 23, 42, 0.35);
}

.price-sep {
  font-size: 28rpx;
  color: var(--text-placeholder);
}

/* 底部按钮 */
.drawer-footer {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #F0F0F0;
  flex-shrink: 0;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.btn-reset {
  flex: 0 0 160rpx;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: #fff;
  border: 2rpx solid rgba(148, 163, 184, 0.45);
  border-radius: 12rpx;
  transition: all 0.2s ease;

  text { font-size: 28rpx; color: var(--text-regular); }
  &:active {
    transform: scale(0.98);
    background: rgba(148, 163, 184, 0.12);
  }
}

.btn-confirm {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: linear-gradient(135deg, #4A90D9 0%, #3A7BC8 100%);
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 217, 0.35);
  transition: all 0.2s ease;

  text { font-size: 30rpx; color: #fff; font-weight: 600; }
  &:active {
    transform: scale(0.985);
    filter: saturate(1.05);
    box-shadow: 0 2rpx 8rpx rgba(74, 144, 217, 0.25);
  }
}
</style>
