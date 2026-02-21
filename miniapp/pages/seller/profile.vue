<template>
  <view class="seller-profile">
    <Navbar title="卖家主页" />
    <view class="seller-profile__content">
      <view class="seller-profile__header">
        <view class="seller-profile__card" @longpress="confirmBlock">
          <UserAvatar
            :avatar-url="profile.avatarUrl"
            :nick-name="profile.nickName"
            :auth-status="profile.authStatus"
            size="lg"
            :show-auth="true"
          />
          <view class="seller-profile__info">
            <view class="seller-profile__name-row">
              <text class="seller-profile__name">{{ profile.nickName || '未命名卖家' }}</text>
              <StatusTag type="auth" :value="profile.authStatus || 0" />
            </view>
            <text class="seller-profile__score">综合评分 {{ profile.score || 0 }}</text>
            <view class="seller-profile__stats">
              <view class="seller-profile__stat">
                <text class="seller-profile__stat-value">{{ profile.onSaleCount || 0 }}</text>
                <text class="seller-profile__stat-label">在售商品</text>
              </view>
              <view class="seller-profile__divider"></view>
              <view class="seller-profile__stat">
                <text class="seller-profile__stat-value">{{ profile.soldCount || 0 }}</text>
                <text class="seller-profile__stat-label">已成交</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="seller-profile__list">
        <view v-if="productList.length">
          <ProductCard v-for="item in productList" :key="item.id" :product="item" />
          <view v-if="loadingMore" class="seller-profile__loading">加载中...</view>
          <view v-else-if="!hasMore" class="seller-profile__loading">没有更多了</view>
        </view>
        <view v-else class="seller-profile__empty">
          <EmptyState type="no-result" text="暂无在售商品" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import { useUserStore } from '@/store/user'
import Navbar from '@/components/navbar/navbar.vue'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import StatusTag from '@/components/status-tag/status-tag.vue'
import ProductCard from '@/components/product-card/product-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const userStore = useUserStore()

const profile = ref({})
const productList = ref([])
const allProducts = ref([])
const page = ref(1)
const pageSize = 6
const hasMore = ref(true)
const loading = ref(false)
const loadingMore = ref(false)
const sellerId = ref(null)

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function safeBack() {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: '/pages/index/index' })
    return
  }
  uni.navigateBack()
}

function getIds(key) {
  const list = uni.getStorageSync(key)
  return Array.isArray(list) ? list : []
}

function saveIds(key, list) {
  uni.setStorageSync(key, list)
}

async function confirmBlock() {
  if (!sellerId.value || !profile.value) return
  if (userStore.userInfo && userStore.userInfo.id === sellerId.value) return
  const blocked = getIds('chat_blocked_ids')
  if (blocked.includes(sellerId.value)) {
    showToast('该用户已拉黑')
    return
  }
  const confirm = await new Promise((resolve) => {
    uni.showModal({
      title: '拉黑用户',
      content: '确认拉黑该用户？拉黑后将不再收到对方消息。',
      confirmText: '确认',
      cancelText: '取消',
      success: (res) => resolve(res && res.confirm)
    })
  })
  if (!confirm) return
  blocked.push(sellerId.value)
  saveIds('chat_blocked_ids', blocked)
  showToast('已拉黑')
}

function buildList(reset = false) {
  const start = (page.value - 1) * pageSize
  const next = allProducts.value.slice(start, start + pageSize)
  if (reset) {
    productList.value = next
  } else {
    productList.value = [...productList.value, ...next]
  }
  hasMore.value = productList.value.length < allProducts.value.length
}

async function fetchProfile(id) {
  if (loading.value) return
  loading.value = true
  try {
    const data = await get(`/mini/user/profile/${id}`, {}, { showLoading: true })
    profile.value = data || {}
    const products = (data && data.products && data.products.records) || []
    const filtered = Array.isArray(products)
      ? products.filter((item) => item.seller && item.seller.id === sellerId.value)
      : []
    allProducts.value = filtered
    page.value = 1
    buildList(true)
  } catch (e) {
    showToast('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  page.value += 1
  buildList(false)
  loadingMore.value = false
}

onLoad(async (options = {}) => {
  const id = options.id ? Number(options.id) : null
  if (!id) {
    showToast('卖家不存在')
    safeBack()
    return
  }
  sellerId.value = id
  if (userStore.userInfo && userStore.userInfo.id === id) {
    showToast('已为你打开个人中心')
    uni.switchTab({ url: '/pages/user/user' })
    return
  }
  await fetchProfile(id)
})

onReachBottom(() => {
  loadMore()
})
</script>

<style lang="scss" scoped>
.seller-profile {
  min-height: 100vh;
  background-color: var(--bg-page);
}

.seller-profile__content {
  padding: var(--spacing-md);
}

.seller-profile__header {
  margin-bottom: var(--spacing-lg);
}

.seller-profile__card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  border-radius: 24rpx;
  background: linear-gradient(135deg, #4a90d9 0%, #6ba3e0 100%);
  color: var(--text-white);
}

.seller-profile__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.seller-profile__name-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.seller-profile__name {
  font-size: var(--font-lg);
  font-weight: 600;
}

.seller-profile__score {
  font-size: var(--font-sm);
  color: rgba(255, 255, 255, 0.85);
}

.seller-profile__stats {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.seller-profile__stat {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.seller-profile__stat-value {
  font-size: var(--font-lg);
  font-weight: 600;
}

.seller-profile__stat-label {
  font-size: var(--font-xs);
  color: rgba(255, 255, 255, 0.85);
}

.seller-profile__divider {
  width: 2rpx;
  height: 48rpx;
  background-color: rgba(255, 255, 255, 0.3);
}

.seller-profile__list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.seller-profile__loading {
  text-align: center;
  font-size: var(--font-sm);
  color: var(--text-secondary);
  padding: var(--spacing-md) 0;
}

.seller-profile__empty {
  padding-top: var(--spacing-xl);
}
</style>
