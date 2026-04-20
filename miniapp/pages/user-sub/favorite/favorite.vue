<template>
  <view class="favorite-page">
    <view v-if="favoriteList.length" class="favorite-list">
      <view
        v-for="item in favoriteList"
        :key="item.id"
        class="favorite-item"
        :class="{ 'is-disabled': isDisabled(item) }"
        @longpress="openCancel(item)"
      >
        <ProductCard :product="item" />
        <view
          v-if="isDisabled(item)"
          class="favorite-item__mask"
          @click.stop="onDisabledTap(item)"
        >
          <view class="favorite-item__tag">{{ statusText(item) }}</view>
        </view>
      </view>
      <view v-if="loadingMore" class="favorite-loading">加载中...</view>
      <view v-else-if="!hasMore" class="favorite-loading">没有更多了</view>
    </view>
    <view v-else class="favorite-empty">
      <EmptyState type="no-favorite" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { PRODUCT_STATUS } from '@/utils/constant'
import { useUserStore } from '@/store'
import ProductCard from '@/components/product-card/product-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const userStore = useUserStore()

const favoriteList = ref([])
const page = ref(1)
const pageSize = 20
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function ensureLogin() {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login-sub/login/login' })
    return false
  }
  return true
}

function isDisabled(item) {
  return item && (item.status === PRODUCT_STATUS.OFF_SHELF || item.status === PRODUCT_STATUS.SOLD)
}

function statusText(item) {
  if (!item) return ''
  if (item.status === PRODUCT_STATUS.SOLD) return '已售出'
  if (item.status === PRODUCT_STATUS.OFF_SHELF) return '已下架'
  return ''
}

function onDisabledTap(item) {
  if (!item) return
  if (item.status === PRODUCT_STATUS.SOLD) {
    showToast('该商品已售出')
    return
  }
  if (item.status === PRODUCT_STATUS.OFF_SHELF) {
    showToast('该商品已下架')
  }
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
    const total = isArray ? records.length : (data.total ?? records.length)
    if (refresh) {
      favoriteList.value = records
    } else {
      favoriteList.value = favoriteList.value.concat(records)
    }
    if (isArray) {
      hasMore.value = false
    } else {
      hasMore.value = favoriteList.value.length < total
      if (records.length < pageSize) {
        hasMore.value = false
      }
    }
    page.value = targetPage
  } catch (error) {
    showToast('加载失败，请稍后重试')
  } finally {
    loading.value = false
    if (refresh) {
      uni.stopPullDownRefresh()
    }
  }
}

function loadMore() {
  if (!hasMore.value || loading.value) return
  loadingMore.value = true
  fetchFavorites(page.value + 1, false).finally(() => {
    loadingMore.value = false
  })
}

function openCancel(item) {
  if (!item || !item.id) return
  uni.showActionSheet({
    itemList: ['取消收藏'],
    success: async (res) => {
      if (res.tapIndex !== 0) return
      try {
        await post('/mini/favorite/cancel', { productId: item.id }, { showLoading: true })
        favoriteList.value = favoriteList.value.filter((target) => target.id !== item.id)
        if (!favoriteList.value.length) {
          hasMore.value = false
        }
        showToast('已取消收藏')
      } catch (error) {
        showToast('取消失败，请稍后重试')
      }
    }
  })
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
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  box-sizing: border-box;
}

.favorite-item {
  position: relative;
}

.favorite-item.is-disabled :deep(.product-card) {
  opacity: 0.5;
  pointer-events: none;
}

.favorite-item__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: var(--spacing-md);
  background-color: rgba(255, 255, 255, 0.55);
}

.favorite-item__tag {
  padding: 6rpx 16rpx;
  border-radius: var(--radius-round);
  background-color: var(--bg-white);
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

.favorite-loading {
  text-align: center;
  padding: var(--spacing-md) 0;
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

.favorite-empty {
  padding-top: var(--spacing-xl);
}
</style>
