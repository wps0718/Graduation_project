<template>
  <view class="order-page">
    <Navbar title="交易记录" />
    <view class="order-tabs">
      <view
        v-for="item in tabs"
        :key="item.value"
        class="order-tab"
        :class="{ 'is-active': activeTab === item.value }"
        @click="switchTab(item.value)"
      >
        <text class="order-tab__text">{{ item.label }}</text>
      </view>
    </view>
    <view class="order-content">
      <view v-if="orderList.length">
        <OrderCard
          v-for="item in orderList"
          :key="item.id"
          :order="item"
          :role="activeTab"
          @action="onOrderAction"
        />
        <view v-if="loadingMore" class="order-loading">加载中...</view>
        <view v-else-if="!hasMore" class="order-loading">没有更多了</view>
      </view>
      <view v-else class="order-empty">
        <EmptyState type="no-order" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { useUserStore } from '@/store'
import Navbar from '@/components/navbar/navbar.vue'
import OrderCard from '@/components/order-card/order-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const userStore = useUserStore()

const tabs = [
  { label: '我买到的', value: 'buyer' },
  { label: '我卖出的', value: 'seller' }
]

const activeTab = ref('buyer')
const orderList = ref([])
const page = ref(1)
const pageSize = 20
const hasMore = ref(true)
const loading = ref(false)
const loadingMore = ref(false)

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function ensureLogin() {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login/login' })
    return false
  }
  return true
}

function buildRoleOrders(records, role, targetPage) {
  const currentId = userStore.userInfo && userStore.userInfo.id
  if (!currentId) {
    return { list: records, total: records.length }
  }
  const filtered = records.filter((item) => {
    if (role === 'buyer') {
      return item.buyerId === currentId
    }
    if (role === 'seller') {
      return item.sellerId === currentId
    }
    return true
  })
  const start = (targetPage - 1) * pageSize
  const sliced = filtered.slice(start, start + pageSize).map((item) => {
    const otherUser = role === 'buyer' ? item.seller : item.buyer
    return {
      ...item,
      otherUser: otherUser || item.otherUser
    }
  })
  return { list: sliced, total: filtered.length }
}

async function fetchOrders(targetPage, refresh = false) {
  if (!ensureLogin()) return
  if (loading.value) return
  loading.value = true
  try {
    const data = await get(
      '/mini/order/list',
      { page: targetPage, pageSize, role: activeTab.value, status: '' },
      { showLoading: refresh || targetPage === 1 }
    )
    const isArray = Array.isArray(data)
    const records = isArray ? data : (data.records || [])
    const isRoleAware = records.some((item) => item && item.buyerId && item.sellerId)
    let total = isArray ? records.length : (data.total ?? records.length)
    let nextList = records
    if (isRoleAware) {
      const roleData = buildRoleOrders(records, activeTab.value, targetPage)
      total = roleData.total
      nextList = roleData.list
    }
    if (refresh) {
      orderList.value = nextList
    } else {
      orderList.value = orderList.value.concat(nextList)
    }
    if (isRoleAware) {
      hasMore.value = orderList.value.length < total
    } else if (isArray) {
      hasMore.value = false
    } else {
      hasMore.value = orderList.value.length < total
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

function refreshList() {
  page.value = 1
  hasMore.value = true
  fetchOrders(1, true)
}

function loadMore() {
  if (loading.value || !hasMore.value) return
  loadingMore.value = true
  fetchOrders(page.value + 1, false).finally(() => {
    loadingMore.value = false
  })
}

function switchTab(value) {
  if (activeTab.value === value) return
  activeTab.value = value
  refreshList()
}

function onOrderAction(payload) {
  if (!payload) return
  const { action, order } = payload
  if (!order) return
  if (action === 'contact') {
    const userId = order.otherUser && order.otherUser.id
    uni.navigateTo({ url: `/pages/chat/detail/detail?userId=${userId || ''}` })
    return
  }
  if (action === 'cancel') {
    openCancel(order)
    return
  }
  if (action === 'confirm') {
    confirmReceive(order)
    return
  }
  if (action === 'review') {
    uni.navigateTo({ url: `/pages/review/review?orderId=${order.id}` })
    return
  }
  if (action === 'viewReview') {
    uni.navigateTo({ url: `/pages/review/review?orderId=${order.id}&readonly=1` })
    return
  }
  if (action === 'reorder') {
    const sellerId = order.otherUser && order.otherUser.id
    uni.navigateTo({ url: `/pages/seller/profile?id=${sellerId || ''}` })
    return
  }
  if (action === 'delete') {
    deleteOrder(order)
    return
  }
}

function openCancel(order) {
  const reasons = ['双方协商取消', '对方无响应', '不想买了', '其他']
  uni.showActionSheet({
    itemList: reasons,
    success: async (res) => {
      const reason = reasons[res.tapIndex]
      try {
        await post('/mini/order/cancel', { orderId: order.id, reason }, { showLoading: true })
        showToast('已取消交易')
        refreshList()
      } catch (error) {
        showToast('取消失败，请稍后重试')
      }
    }
  })
}

function confirmReceive(order) {
  uni.showModal({
    title: '确认收货',
    content: '确认已收到商品吗？',
    confirmText: '确认',
    cancelText: '取消',
    success: async (res) => {
      if (!res || !res.confirm) return
      try {
        await post('/mini/order/confirm', { orderId: order.id }, { showLoading: true })
        showToast('已确认收货')
        refreshList()
      } catch (error) {
        showToast('操作失败，请稍后重试')
      }
    }
  })
}

function deleteOrder(order) {
  uni.showModal({
    title: '删除订单',
    content: '确认删除该订单吗？',
    confirmText: '删除',
    cancelText: '取消',
    success: async (res) => {
      if (!res || !res.confirm) return
      try {
        await post('/mini/order/delete', { orderId: order.id }, { showLoading: true })
        orderList.value = orderList.value.filter((item) => item.id !== order.id)
        showToast('已删除')
      } catch (error) {
        showToast('删除失败，请稍后重试')
      }
    }
  })
}

onLoad(() => {
  fetchOrders(1, true)
})

onPullDownRefresh(() => {
  refreshList()
})

onReachBottom(() => {
  loadMore()
})
</script>

<style lang="scss" scoped>
.order-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

.order-tabs {
  margin: var(--spacing-md);
  padding: 8rpx;
  background-color: var(--bg-white);
  border-radius: 999rpx;
  display: flex;
  gap: 8rpx;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}

.order-tab {
  flex: 1;
  padding: 16rpx 0;
  border-radius: 999rpx;
  text-align: center;
  background-color: var(--bg-white);
}

.order-tab.is-active {
  background-color: var(--success-color);
}

.order-tab__text {
  font-size: var(--font-md);
  color: var(--text-secondary);
}

.order-tab.is-active .order-tab__text {
  color: var(--text-white);
  font-weight: 600;
}

.order-content {
  padding: 0 var(--spacing-md) var(--spacing-lg);
}

.order-loading {
  text-align: center;
  padding: var(--spacing-md) 0;
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

.order-empty {
  padding-top: var(--spacing-xl);
}
</style>
