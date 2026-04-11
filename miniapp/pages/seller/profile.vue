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
              <view class="seller-profile__name-left">
                <text class="seller-profile__name">{{ profile.nickName || '未命名卖家' }}</text>
                <StatusTag type="auth" :value="profile.authStatus || 0" />
              </view>
              <view v-if="canChat" class="seller-profile__actions">
                <view
                  class="seller-profile__chat-btn"
                  @click.stop="onChat"
                >
                  <text class="seller-profile__chat-text">私信</text>
                </view>
                <view
                  class="seller-profile__follow-btn"
                  :class="{ 'is-following': isFollowing }"
                  @click.stop="toggleFollow"
                >
                  <text class="seller-profile__follow-text">{{ isFollowing ? '已关注' : '关注' }}</text>
                </view>
              </view>
              <view v-else-if="!isSelf && (isBanned || isDeactivated)" class="seller-profile__status-tag">
                <text class="seller-profile__status-text">{{ isBanned ? '该用户已封禁' : '该用户已注销' }}</text>
              </view>
            </view>
            <text class="seller-profile__score">综合评分 {{ profile.score || 0 }}</text>
            <view class="seller-profile__meta">
              <text class="seller-profile__meta-text">IP属地 {{ profile.ipRegion || '未知' }}</text>
              <text class="seller-profile__meta-dot">·</text>
              <text class="seller-profile__meta-text">{{ profile.lastActiveText || '最近活跃未知' }}</text>
            </view>
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
              <view class="seller-profile__divider"></view>
              <view class="seller-profile__stat">
                <text class="seller-profile__stat-value">{{ profile.followerCount || 0 }}</text>
                <text class="seller-profile__stat-label">粉丝</text>
              </view>
              <view class="seller-profile__divider"></view>
              <view class="seller-profile__stat">
                <text class="seller-profile__stat-value">{{ profile.followingCount || 0 }}</text>
                <text class="seller-profile__stat-label">关注</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="profile.bio" class="seller-profile__bio">
        <text class="seller-profile__bio-label">简介</text>
        <text class="seller-profile__bio-text">{{ bioText }}</text>
        <text v-if="showBioToggle" class="seller-profile__bio-toggle" @click="toggleBio">
          {{ bioExpanded ? '收起' : '展开' }}
        </text>
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
import { computed, ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { useUserStore } from '@/store/user'
import { CONDITION_LEVELS } from '@/utils/constant'
import Navbar from '@/components/navbar/navbar.vue'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import StatusTag from '@/components/status-tag/status-tag.vue'
import ProductCard from '@/components/product-card/product-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const userStore = useUserStore()

const profile = ref({})
const productList = ref([])
const page = ref(1)
const pageSize = 8
const total = ref(0)
const hasMore = computed(() => productList.value.length < total.value)
const loading = ref(false)
const loadingMore = ref(false)
const sellerId = ref(null)
const isFollowing = ref(false)
const followLoading = ref(false)
const bioExpanded = ref(false)

const isSelf = computed(() => {
  return !!(userStore.userInfo && sellerId.value && userStore.userInfo.id === sellerId.value)
})

const isBanned = computed(() => profile.value && profile.value.status === 0)
const isDeactivated = computed(() => profile.value && profile.value.status === 2)
const canChat = computed(() => !isSelf.value && !isBanned.value && !isDeactivated.value)

const showBioToggle = computed(() => {
  const bio = String((profile.value && profile.value.bio) || '')
  return bio.length > 36
})

const bioText = computed(() => {
  const bio = String((profile.value && profile.value.bio) || '')
  if (bioExpanded.value || bio.length <= 36) {
    return bio
  }
  return `${bio.slice(0, 36)}...`
})

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

function ensureLogin() {
  if (!userStore.isLogin) {
    showToast('请先登录')
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 300)
    return false
  }
  return true
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

function getConditionText(value) {
  const found = CONDITION_LEVELS.find((item) => item.value === value)
  return found ? found.label : ''
}

function normalizeProducts(records) {
  const list = Array.isArray(records) ? records : []
  return list.map((item) => {
    const conditionLevel = item && typeof item.conditionLevel === 'number' ? item.conditionLevel : null
    return {
      ...item,
      conditionText: getConditionText(conditionLevel)
    }
  })
}

async function onChat() {
  if (!ensureLogin()) return
  if (!sellerId.value) return
  
  try {
    const data = await post('/mini/chat/session/create', {
      peerId: sellerId.value
    }, { showLoading: true })
    
    if (data && data.sessionKey) {
      uni.navigateTo({
        url: `/pages/chat/detail/detail?sessionKey=${data.sessionKey}&peerId=${sellerId.value}`
      })
    }
  } catch (error) {
    showToast('发起私信失败，请重试')
  }
}

async function fetchFollowState() {
  if (!userStore.isLogin || !sellerId.value) {
    isFollowing.value = false
    return
  }
  try {
    const data = await get(`/mini/follow/check/${sellerId.value}`, {}, { showLoading: false })
    isFollowing.value = !!data
  } catch (e) {
    isFollowing.value = false
  }
}

async function fetchProfile(id, reset = true) {
  if (loading.value) return
  loading.value = true
  try {
    const params = { page: page.value, pageSize }
    const data = await get(`/mini/user/profile/${id}`, params, { showLoading: reset })
    if (reset) {
      profile.value = data || {}
      bioExpanded.value = false
      await fetchFollowState()
    }
    const products = (data && data.products) || {}
    const records = (products && products.records) || []
    total.value = Number((products && products.total) || 0)
    const mapped = normalizeProducts(records)
    if (reset) {
      productList.value = mapped
    } else {
      productList.value = [...productList.value, ...mapped]
    }
  } catch (e) {
    showToast('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    page.value += 1
    await fetchProfile(sellerId.value, false)
  } finally {
    loadingMore.value = false
  }
}

function toggleBio() {
  bioExpanded.value = !bioExpanded.value
}

async function toggleFollow() {
  if (followLoading.value) return
  if (!ensureLogin()) return
  if (!sellerId.value) return
  followLoading.value = true
  try {
    if (isFollowing.value) {
      await post('/mini/follow/unfollow', { userId: sellerId.value }, { showLoading: true })
    } else {
      await post('/mini/follow/follow', { userId: sellerId.value }, { showLoading: true })
    }
    isFollowing.value = !isFollowing.value
    try {
      const stats = await get(`/mini/follow/stats/${sellerId.value}`, {}, { showLoading: false })
      profile.value = {
        ...profile.value,
        followerCount: stats && typeof stats.followerCount === 'number' ? stats.followerCount : profile.value.followerCount,
        followingCount: stats && typeof stats.followingCount === 'number' ? stats.followingCount : profile.value.followingCount
      }
    } catch (e) {
      const followerCount = Number(profile.value.followerCount || 0)
      profile.value = {
        ...profile.value,
        followerCount: Math.max(0, followerCount + (isFollowing.value ? 1 : -1))
      }
    }
  } catch (e) {
    showToast('操作失败，请稍后重试')
  } finally {
    followLoading.value = false
  }
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
  page.value = 1
  productList.value = []
  total.value = 0
  await fetchProfile(id, true)
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
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-light) 100%);
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
  justify-content: space-between;
}

.seller-profile__name-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.seller-profile__name {
  font-size: var(--font-lg);
  font-weight: 600;
}

.seller-profile__actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.seller-profile__chat-btn {
  padding: 10rpx 22rpx;
  border-radius: var(--radius-round);
  background-color: rgba(255, 255, 255, 0.2);
  border: 1rpx solid rgba(255, 255, 255, 0.5);
}

.seller-profile__chat-text {
  font-size: var(--font-sm);
  color: var(--text-white);
}

.seller-profile__status-tag {
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  background-color: rgba(0, 0, 0, 0.2);
}

.seller-profile__status-text {
  font-size: var(--font-xs);
  color: var(--text-white-85);
}

.seller-profile__follow-btn {
  padding: 10rpx 22rpx;
  border-radius: var(--radius-round);
  background-color: var(--text-white);
  opacity: 0.95;
}

.seller-profile__follow-btn.is-following {
  background-color: var(--primary-bg);
}

.seller-profile__follow-text {
  font-size: var(--font-sm);
  color: var(--primary-color);
}

.seller-profile__score {
  font-size: var(--font-sm);
  color: var(--text-white-85);
}

.seller-profile__meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.seller-profile__meta-text {
  font-size: var(--font-xs);
  color: var(--text-white-85);
}

.seller-profile__meta-dot {
  font-size: var(--font-xs);
  color: var(--text-white-85);
}

.seller-profile__stats {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  flex-wrap: wrap;
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
  background-color: var(--text-white-30);
}

.seller-profile__bio {
  margin-top: calc(var(--spacing-lg) * -0.5);
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-md);
  border-radius: 24rpx;
  background-color: var(--bg-white);
}

.seller-profile__bio-label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  display: block;
  margin-bottom: var(--spacing-xs);
}

.seller-profile__bio-text {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.seller-profile__bio-toggle {
  margin-top: var(--spacing-xs);
  font-size: var(--font-sm);
  color: var(--primary-color);
  display: inline-block;
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
