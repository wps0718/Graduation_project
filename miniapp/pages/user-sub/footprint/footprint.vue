<template>
  <view class="footprint-page">
    <view v-if="footprintList.length" class="footprint-list">
      <view v-for="item in footprintList" :key="item.id" class="footprint-item">
        <ProductCard :product="item" />
      </view>
    </view>
    <view v-else class="footprint-empty">
      <EmptyState type="no-data" text="暂无浏览足迹" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import ProductCard from '@/components/product-card/product-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

const FOOTPRINT_KEY = 'footprint_list'
const FOOTPRINT_MAX = 50
const FOOTPRINT_EXPIRE = 7 * 24 * 60 * 60 * 1000

const footprintList = ref([])

function normalizeFootprints(list) {
  const now = Date.now()
  const items = Array.isArray(list) ? list : []
  const filtered = items.filter(item => item && item.id && item.viewedAt && now - item.viewedAt <= FOOTPRINT_EXPIRE)
  filtered.sort((a, b) => (b.viewedAt || 0) - (a.viewedAt || 0))
  if (filtered.length > FOOTPRINT_MAX) {
    return filtered.slice(0, FOOTPRINT_MAX)
  }
  return filtered
}

function loadFootprints() {
  const list = normalizeFootprints(uni.getStorageSync(FOOTPRINT_KEY))
  footprintList.value = list
  uni.setStorageSync(FOOTPRINT_KEY, list)
  uni.stopPullDownRefresh()
}

onShow(() => {
  loadFootprints()
})

onPullDownRefresh(() => {
  loadFootprints()
})
</script>

<style lang="scss" scoped>
.footprint-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  box-sizing: border-box;
}

.footprint-item {
  position: relative;
}
</style>
