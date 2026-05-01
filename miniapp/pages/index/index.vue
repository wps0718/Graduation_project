<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <!-- 第一行：校区 + 定位 -->
      <view class="location-row">
        <view class="campus-selector" @click="handleCampusChange">
          <text class="campus-name">{{ appStore.currentCampusName || '选择校区' }}</text>
          <view class="campus-arrow">▼</view>
        </view>
        <view class="campus-location">
          <image src="/static/svg/location.svg" class="location-icon" />
          <text>{{ appStore.currentCampusName ? appStore.currentCampusName + '校区' : '定位中...' }}</text>
        </view>
      </view>
      
      <!-- 第二行：搜索框 -->
      <view class="search-bar" @click="goSearch">
        <image src="/static/svg/search.svg" class="search-icon" />
        <text class="search-placeholder">搜索闲置物品</text>
      </view>
    </view>

    <!-- 轮播图 -->
    <view class="banner-section">
      <swiper
        class="banner-swiper"
        circular
        autoplay
        interval="5000"
        indicator-dots
        indicator-active-color="#fff"
      >
        <swiper-item
          v-for="item in bannerList"
          :key="item.id"
          @click="handleBannerClick(item)"
        >
          <image :src="resolveImageUrl(item.image)" mode="aspectFill" class="banner-image" />
        </swiper-item>
      </swiper>
    </view>

    <!-- 分类入口 -->
    <view class="category-section">
      <view class="category-list">
        <view
          v-for="item in categoryList"
          :key="item.id"
          class="category-item"
          @click="handleCategoryClick(item)"
        >
          <view class="category-icon">
            <image
              :src="getCategoryIconSrc(item)"
              class="category-icon-img"
              @error="handleCategoryIconError(item)"
            />
          </view>
          <text class="category-text">{{ item.name }}</text>
        </view>
        <!-- 更多分类 -->
        <view class="category-item" @click="goSearch">
          <view class="category-icon">
             <image src="/static/svg/all.svg" class="category-icon-img" />
          </view>
          <text class="category-text">更多</text>
        </view>
      </view>
    </view>

    <!-- 最新发布 -->
    <view class="section-title">
      <text class="title-text">最新发布</text>
    </view>

    <!-- 商品列表 -->
    <view class="product-list">
      <block v-if="productList.length > 0">
        <product-card
          v-for="item in productList"
          :key="item.id"
          :product="item"
        />
      </block>
      <empty-state
        v-else-if="!loading"
        type="no-data"
        text="暂无相关商品"
      />
    </view>

    <!-- 加载状态 -->
    <view v-if="productList.length > 0" class="load-more">
      <text>{{ hasMore ? '加载中...' : '没有更多了' }}</text>
    </view>

    <!-- 系统公告弹窗 -->
    <view v-if="noticeVisible" class="notice-mask" @click.stop>
      <view class="notice-modal">
        <view class="notice-modal__header">
          <text class="notice-modal__title">📢 {{ currentNotice?.title || '系统公告' }}</text>
        </view>
        <scroll-view scroll-y class="notice-modal__body">
          <text class="notice-modal__content">{{ currentNotice?.content }}</text>
        </scroll-view>
        <view class="notice-modal__footer" @click="closeNotice">
          <text class="notice-modal__btn">我知道了</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useAppStore } from '@/store/app'
import { get } from '@/utils/request'
import { getToken } from '@/utils/auth'
import { normalizeProductCardData, resolveImageUrl } from '@/utils/image'
import ProductCard from '@/components/product-card/product-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const appStore = useAppStore()

const bannerList = ref([])
const categoryList = ref([])
const productList = ref([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20
const categoryIconErrorMap = ref({})
const CATEGORY_ICON_PLACEHOLDER = '/static/svg/all.svg'
const CATEGORY_ICON_BY_NAME = {
  书籍: '/static/svg/book.svg',
  服饰: '/static/svg/clothing.svg',
  服饰鞋包: '/static/svg/clothing-bags.svg',
  生活: '/static/svg/water-bottle.svg',
  生活设备: '/static/svg/water-bottle.svg',
  电子设备: '/static/svg/electronics.svg',
  运动设备: '/static/svg/sports.svg',
  潮玩娱乐: '/static/svg/hobby.svg',
  快递: '/static/svg/bicycle.svg',
  代拿快递: '/static/svg/Pick_up_Package.svg'
}
const CATEGORY_ICON_BY_ID = {
  1: '/static/svg/book.svg',
  2: '/static/svg/clothing.svg',
  3: '/static/svg/water-bottle.svg',
  4: '/static/svg/electronics.svg',
  5: '/static/svg/sports.svg',
  6: '/static/svg/hobby.svg',
  7: '/static/svg/bicycle.svg'
}
const LOCAL_BANNERS = [
  { id: 'local-nhb', image: '/static/pic/推广（南海北）.webp' },
  { id: 'local-gz', image: '/static/pic/推广（广州）.webp' }
]
const remoteBannerList = ref([])
const useRemoteBanners = ref(false)
const noticeVisible = ref(false)
const currentNotice = ref(null)

onLoad(async () => {
  try {
    await appStore.loadCampusList()
  } catch (e) {
    console.error('加载校区失败', e)
  }
  await loadBanners()
  await loadCategories()
  await loadProducts(true)
  await loadLatestNotice()
})

onShow(() => {
  if (productList.value.length > 0) {
    loadProducts(true)
  }
})

onPullDownRefresh(async () => {
  await Promise.all([
    loadBanners(),
    loadCategories(),
    loadProducts(true)
  ])
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    loadProducts(false)
  }
})

function getNormalizedCategoryIconPath(item) {
  const rawIcon = (item?.iconPath || item?.icon || '').trim()
  if (!rawIcon) {
    return ''
  }
  if (/^https?:\/\//i.test(rawIcon)) {
    return rawIcon
  }
  if (rawIcon.startsWith('/')) {
    return rawIcon
  }
  if (rawIcon.endsWith('.svg')) {
    return `/static/svg/${rawIcon}`
  }
  return ''
}

function normalizeCategoryList(list) {
  return (list || []).map((item) => {
    return {
      ...item,
      _iconPath: getNormalizedCategoryIconPath(item)
    }
  })
}

function getCategoryMappedIconPath(item) {
  const name = String(item?.name || '').trim()
  const id = String(item?.id || '').trim()
  if (name && CATEGORY_ICON_BY_NAME[name]) {
    return CATEGORY_ICON_BY_NAME[name]
  }
  if (id && CATEGORY_ICON_BY_ID[id]) {
    return CATEGORY_ICON_BY_ID[id]
  }
  return CATEGORY_ICON_PLACEHOLDER
}

function getCategoryIconSrc(item) {
  const key = String(item?.id || item?.name || '')
  if (key && categoryIconErrorMap.value[key]) {
    return getCategoryMappedIconPath(item)
  }
  return item?._iconPath || getCategoryMappedIconPath(item)
}

function handleCategoryIconError(item) {
  const key = String(item?.id || item?.name || '')
  if (!key) {
    return
  }
  categoryIconErrorMap.value = {
    ...categoryIconErrorMap.value,
    [key]: true
  }
}

async function loadBanners() {
  if (!appStore.currentCampusId) {
    bannerList.value = LOCAL_BANNERS
    return
  }
  try {
    const res = await get('/mini/banner/list',
      { campusId: appStore.currentCampusId },
      { showLoading: false }
    )
    const remote = Array.isArray(res) ? res : []
    if (remote.length > 0) {
      bannerList.value = remote
      useRemoteBanners.value = true
    } else {
      bannerList.value = LOCAL_BANNERS
      useRemoteBanners.value = false
    }
  } catch (e) {
    console.error('加载Banner失败', e)
    bannerList.value = LOCAL_BANNERS
    useRemoteBanners.value = false
  }
}


async function loadCategories() {
  try {
    if (appStore.categoryList && appStore.categoryList.length > 0) {
      categoryList.value = normalizeCategoryList(appStore.categoryList)
    } else {
      await appStore.loadCategoryList()
      categoryList.value = normalizeCategoryList(appStore.categoryList)
    }
    categoryIconErrorMap.value = {}
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

async function loadProducts(refresh = false) {
  if (loading.value) return
  
  if (refresh) {
    page.value = 1
    hasMore.value = true
  }

  loading.value = true
  try {
    const res = await get('/mini/product/list', {
      page: page.value,
      pageSize: pageSize,
      campusId: appStore.currentCampusId,
      sortBy: 'latest'
    })

    const list = (res?.records || []).map((item) => {
      const version = item && (item.updateTime || item.createTime || item.id)
      return normalizeProductCardData(item, { version })
    })
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
    console.error('加载商品列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleCampusChange() {
  const itemList = appStore.campusList.map(item => item.name)
  uni.showActionSheet({
    itemList: itemList,
    success: (res) => {
      const selectedIndex = res.tapIndex
      const selectedCampus = appStore.campusList[selectedIndex]
      if (selectedCampus.id !== appStore.currentCampusId) {
        appStore.setCampus(selectedCampus.id, selectedCampus.name)
        loadBanners()
        loadProducts(true)
      }
    }
  })
}

function goSearch() {
  uni.navigateTo({
    url: '/pages/login-sub/search/search'
  })
}


function handleBannerClick(item) {
  if (!item.linkUrl) return

  if (item.linkType === 1) {
    uni.navigateTo({ url: `/pages/product/detail/detail?id=${item.linkUrl}` })
  } else if (item.linkType === 2) {
    // 活动页：如果是外部链接则用 web-view，否则内部跳转
    if (/^https?:\/\//i.test(item.linkUrl)) {
      uni.navigateTo({ url: `/pages/common/web-view/web-view?url=${encodeURIComponent(item.linkUrl)}` })
    } else {
      uni.navigateTo({ url: item.linkUrl })
    }
  } else if (item.linkType === 3) {
    // 外部链接：使用 web-view 打开
    uni.navigateTo({ url: `/pages/common/web-view/web-view?url=${encodeURIComponent(item.linkUrl)}` })
  }
}

function handleCategoryClick(item) {
  if (item.name === '代拿快递') {
    const keyword = encodeURIComponent('代拿快递')
    uni.navigateTo({
      url: `/pages/login-sub/search/search?keyword=${keyword}`
    })
  } else {
    uni.navigateTo({
      url: `/pages/login-sub/search/search?categoryId=${item.id}`
    })
  }
}

async function loadLatestNotice() {
  try {
    const token = getToken()
    if (!token) {
      console.log('用户未登录，跳过加载公告')
      return
    }

    const res = await get('/mini/notification/list',
      { category: 2, page: 1, pageSize: 5 },
      { showLoading: false }
    )
    const records = Array.isArray(res) ? res : (res?.records || [])
    const notices = records.filter(item => item.type === 5)
    if (!notices.length) return

    const notice = notices[0]
    const shownId = uni.getStorageSync('lastShownNoticeId')
    if (String(shownId) === String(notice.id)) return

    currentNotice.value = notice
    noticeVisible.value = true
  } catch(e) {
    console.error('加载公告失败', e)
  }
}

function closeNotice() {
  if (currentNotice.value) {
    uni.setStorageSync('lastShownNoticeId', String(currentNotice.value.id))
  }
  noticeVisible.value = false
  currentNotice.value = null
}
</script>

<style lang="scss">
/* 页面容器 */
.container {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: var(--spacing-lg);
}

/* 顶部导航栏 */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  flex-direction: column; /* 改为纵向布局 */
  padding: var(--spacing-md);
  background-color: var(--bg-page); /* 整个页面背景色 */
  /* padding-top: calc(var(--status-bar-height) + var(--spacing-sm)); */
}

/* 顶部第一行：校区定位 */
.location-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.campus-selector {
  display: flex;
  align-items: center;
  font-size: 34rpx; /* 标题大字 */
  color: var(--text-primary);
  font-weight: bold;
}

.campus-name {
  margin-right: var(--spacing-xs);
}

.campus-location {
  display: flex;
  align-items: center;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.location-icon {
  width: 24rpx;
  height: 24rpx;
  margin-right: 4rpx;
}

/* 搜索框 */
.search-bar {
  display: flex;
  align-items: center;
  height: 80rpx; /* 加高搜索框 */
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 0 var(--spacing-md);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
}

.search-icon {
  margin-right: var(--spacing-sm);
  width: 32rpx;
  height: 32rpx;
}

.search-placeholder {
  font-size: var(--font-md);
  color: var(--text-placeholder);
}

/* 轮播图 */
.banner-section {
  padding: 0 var(--spacing-md) var(--spacing-md);
  background-color: var(--bg-page);
}

.banner-swiper {
  height: 320rpx; /* 稍微加高 */
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transform: translateY(0);
}

.banner-image {
  width: 100%;
  height: 100%;
}

/* 分类入口 */
.category-section {
  background-color: var(--bg-page); /* 背景透明 */
  padding-bottom: var(--spacing-lg);
  margin-bottom: 0;
}

.category-list {
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
  padding: 0 var(--spacing-md);
  justify-content: space-between; /* 两端对齐 */
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 25%; /* 一行4个 */
  margin-right: 0;
  margin-bottom: var(--spacing-lg);
  flex-shrink: 0;
}

.category-icon {
  width: 96rpx; /* 加大图标区域 */
  height: 96rpx;
  background-color: var(--bg-white); /* 白色背景 */
  border-radius: 32rpx; /* 方形圆角 */
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  margin-bottom: var(--spacing-sm);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
}

.category-icon-img {
  width: 56rpx;
  height: 56rpx;
}

.category-text {
  font-size: var(--font-sm);
  color: var(--text-regular);
}

/* 最新发布标题 */
.section-title {
  padding: var(--spacing-md);
  background-color: transparent;
}

.title-text {
  font-size: var(--font-lg);
  font-weight: bold;
  color: var(--text-primary);
  position: relative;
  padding-left: var(--spacing-sm);
}

.title-text::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8rpx;
  height: 32rpx;
  background-color: var(--primary-color);
  border-radius: var(--radius-sm);
}

/* 商品列表 */
.product-list {
  padding: 0 var(--spacing-md);
}

/* 加载更多 */
.load-more {
  padding: var(--spacing-md);
  text-align: center;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

/* 系统公告弹窗 */
.notice-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-modal {
  background: #fff;
  border-radius: 24rpx;
  width: 600rpx;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
}

.notice-modal__header {
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid var(--border-light);
}

.notice-modal__title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
  text-align: left;
}

.notice-modal__body {
  padding: 24rpx 32rpx;
  max-height: 400rpx;
  flex: 1;
}

.notice-modal__content {
  font-size: 25rpx;
  color: #666;
  line-height: 1.6;
  text-align: center;
  word-break: break-all;
  white-space: pre-wrap;
}

.notice-modal__footer {
  padding: 25rpx;
  border-top: 1rpx solid var(--border-light);
  text-align: center;
}

.notice-modal__btn {
  color: var(--primary-color);
  font-size: var(--font-md);
  font-weight: 600;
}
</style>
