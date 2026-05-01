<template>
  <view class="web-view-page">
    <web-view :src="url" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const url = ref('')

onLoad((query) => {
  if (query?.url) {
    url.value = decodeURIComponent(query.url)
  }
})
</script>

<style scoped>
.web-view-page {
  width: 100%;
  height: 100vh;
}
</style>
