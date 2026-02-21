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
          <image src="/static/svg/定位.svg" class="location-icon" />
          <text>{{ appStore.currentCampusName ? appStore.currentCampusName + '校区' : '定位中...' }}</text>
        </view>
      </view>
      
      <!-- 第二行：搜索框 -->
      <view class="search-bar" @click="goSearch">
        <image src="/static/svg/搜索.svg" class="search-icon" />
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
          <image :src="item.image" mode="aspectFill" class="banner-image" />
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
            <image v-if="item.iconPath" :src="item.iconPath" class="category-icon-img" />
            <text v-else>{{ item.icon }}</text>
          </view>
          <text class="category-text">{{ item.name }}</text>
        </view>
        <!-- 更多分类 -->
        <view class="category-item" @click="goSearch">
          <view class="category-icon">
             <image src="/static/svg/全部.svg" class="category-icon-img" />
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
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useAppStore } from '@/store/app'
import { get } from '@/utils/request'
import ProductCard from '@/components/product-card/product-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const appStore = useAppStore()

// 状态定义
const bannerList = ref([])
const categoryList = ref([])
const productList = ref([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

// 生命周期
onLoad(async () => {
  try {
    await appStore.loadCampusList()
  } catch (e) {
    console.error('加载校区失败', e)
  }
  await loadBanners()
  await loadCategories()
  await loadProducts(true)
})

onShow(() => {
  // 每次显示页面时刷新商品列表（保持最新）
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

// 数据加载方法
async function loadBanners() {
  if (!appStore.currentCampusId) return
  try {
    const res = await get('/mini/banner/list', { campusId: appStore.currentCampusId }, { showLoading: false })
    bannerList.value = res || []
  } catch (e) {
    console.error('加载Banner失败', e)
  }
}

async function loadCategories() {
  try {
    // 优先从store获取，如果store没有再请求
    if (appStore.categoryList && appStore.categoryList.length > 0) {
      categoryList.value = appStore.categoryList
    } else {
      await appStore.loadCategoryList()
      categoryList.value = appStore.categoryList
    }
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

    const list = res?.records || []
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

// 交互处理
function handleCampusChange() {
  const itemList = appStore.campusList.map(item => item.name)
  uni.showActionSheet({
    itemList: itemList,
    success: (res) => {
      const selectedIndex = res.tapIndex
      const selectedCampus = appStore.campusList[selectedIndex]
      if (selectedCampus.id !== appStore.currentCampusId) {
        appStore.setCampus(selectedCampus.id, selectedCampus.name)
        // 切换校区后重新加载数据
        loadBanners()
        loadProducts(true)
      }
    }
  })
}

function goSearch() {
  uni.navigateTo({
    url: '/pages/search/search'
  })
}

function handleBannerClick(item) {
  if (!item.linkUrl) return
  
  // 根据linkType跳转，此处简化处理，假设内部链接跳转
  if (item.linkType === 1) { // 商品详情
     uni.navigateTo({ url: `/pages/product/detail/detail?id=${item.linkUrl}` })
  } else if (item.linkType === 2) { // 页面跳转
     uni.navigateTo({ url: item.linkUrl })
  } else { // 外链（小程序需webview）
     // 暂不处理
  }
}

function handleCategoryClick(item) {
  uni.navigateTo({
    url: `/pages/search/search?categoryId=${item.id}`
  })
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
</style>
