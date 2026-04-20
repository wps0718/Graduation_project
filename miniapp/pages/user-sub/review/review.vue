<template>
  <view class="review-page">
    <view v-if="productInfo" class="review-card">
      <view class="product">
        <image class="product__image" :src="productImage" mode="aspectFill" />
        <view class="product__info">
          <text class="product__title">{{ productInfo.title || '商品' }}</text>
          <Price v-if="priceValue !== null" :price="priceValue" size="sm" />
        </view>
      </view>
    </view>

    <view v-if="!readonly" class="review-card">
      <text class="section-title">填写评价</text>
      <view
        v-for="item in ratingItems"
        :key="item.key"
        class="rating-row"
      >
        <text class="rating-label">{{ item.label }}</text>
        <view class="rating-stars">
          <text
            v-for="value in 5"
            :key="value"
            class="star"
            :class="{ 'is-active': value <= scores[item.key] }"
            @click="selectStar(item.key, value)"
          >
            ★
          </text>
        </view>
        <text class="rating-score">{{ scores[item.key] }}</text>
      </view>

      <view class="textarea-wrap">
        <textarea
          v-model="content"
          class="textarea"
          maxlength="200"
          placeholder="分享你的交易体验..."
        />
        <view class="textarea-count">
          <text>{{ contentCount }}/200</text>
        </view>
      </view>
    </view>

    <view v-else class="review-card">
      <text class="section-title">评价内容</text>
      <view v-if="reviewList.length" class="review-list">
        <view v-for="item in reviewList" :key="item.id" class="review-item">
          <view class="review-header">
            <text class="review-label">{{ item.label }}</text>
            <text v-if="item.isAuto" class="review-auto">系统默认好评</text>
          </view>
          <view
            v-for="row in ratingItems"
            :key="row.key"
            class="rating-row rating-row--readonly"
          >
            <text class="rating-label">{{ row.label }}</text>
            <view class="rating-stars">
              <text
                v-for="value in 5"
                :key="value"
                class="star"
                :class="{ 'is-active': value <= item[row.key] }"
              >
                ★
              </text>
            </view>
            <text class="rating-score">{{ item[row.key] }}</text>
          </view>
          <view class="review-content">
            <text>{{ item.content || '未填写评价' }}</text>
          </view>
        </view>
      </view>
      <view v-else class="review-empty">暂无评价</view>
    </view>

    <view v-if="!readonly" class="review-footer safe-area-bottom">
      <button class="submit-btn" :loading="submitting" :disabled="submitting" @click="submitReview">
        提交评价
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { useUserStore } from '@/store'
import Price from '@/components/price/price.vue'

const userStore = useUserStore()

const orderId = ref(null)
const readonly = ref(false)
const orderDetail = ref(null)
const reviewDetail = ref(null)
const loading = ref(false)
const submitting = ref(false)

const scores = reactive({
  scoreDesc: 5,
  scoreAttitude: 5,
  scoreExperience: 5
})

const content = ref('')

const ratingItems = [
  { key: 'scoreDesc', label: '商品描述相符' },
  { key: 'scoreAttitude', label: '沟通态度' },
  { key: 'scoreExperience', label: '交易体验' }
]

const productInfo = computed(() => {
  if (reviewDetail.value && reviewDetail.value.product) {
    return reviewDetail.value.product
  }
  if (orderDetail.value && orderDetail.value.product) {
    return orderDetail.value.product
  }
  return null
})

const productImage = computed(() => {
  if (!productInfo.value) return ''
  if (productInfo.value.coverImage) return productInfo.value.coverImage
  if (Array.isArray(productInfo.value.images) && productInfo.value.images.length > 0) {
    return productInfo.value.images[0]
  }
  return ''
})

const priceValue = computed(() => {
  if (orderDetail.value && typeof orderDetail.value.price === 'number') {
    return orderDetail.value.price
  }
  if (productInfo.value && typeof productInfo.value.price === 'number') {
    return productInfo.value.price
  }
  return null
})

const contentCount = computed(() => content.value.length)

const reviewList = computed(() => {
  const data = reviewDetail.value
  if (!data) return []
  const currentId = userStore.userInfo && userStore.userInfo.id
  if (Array.isArray(data.reviews)) {
    return data.reviews.map((item, index) => normalizeReview(item, currentId, index))
  }
  const list = []
  if (data.myReview || data.otherReview) {
    if (data.myReview) {
      list.push(normalizeReview({ ...data.myReview, label: '我的评价' }, currentId, 0))
    }
    if (data.otherReview) {
      list.push(normalizeReview({ ...data.otherReview, label: '对方评价' }, currentId, 1))
    }
    return list
  }
  return [normalizeReview(data, currentId, 0)]
})

function normalizeReview(raw, currentId, index) {
  const fromId = raw && raw.fromUser && raw.fromUser.id
  const toId = raw && raw.toUser && raw.toUser.id
  let label = raw && raw.label
  if (!label) {
    if (currentId && fromId && currentId === fromId) {
      label = '我的评价'
    } else if (currentId && toId && currentId === toId) {
      label = '对方评价'
    } else {
      label = '评价'
    }
  }
  return {
    id: raw && raw.id ? raw.id : `${label}-${index}`,
    label,
    scoreDesc: (raw && (raw.scoreDesc ?? raw.score_desc)) || 0,
    scoreAttitude: (raw && (raw.scoreAttitude ?? raw.score_attitude)) || 0,
    scoreExperience: (raw && (raw.scoreExperience ?? raw.score_experience)) || 0,
    content: (raw && raw.content) || '',
    isAuto: (raw && (raw.isAuto ?? raw.is_auto)) || false
  }
}

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

function selectStar(key, value) {
  if (readonly.value) return
  if (!key) return
  scores[key] = value
}

async function fetchOrderDetail() {
  if (!orderId.value || loading.value) return
  loading.value = true
  try {
    const data = await get(`/mini/order/detail/${orderId.value}`, {}, { showLoading: true })
    orderDetail.value = data || null
  } catch (error) {
    showToast('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function fetchReviewDetail() {
  if (!orderId.value || loading.value) return
  loading.value = true
  try {
    const data = await get(`/mini/review/detail/${orderId.value}`, {}, { showLoading: true })
    reviewDetail.value = data || null
  } catch (error) {
    showToast('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function submitReview() {
  if (readonly.value || submitting.value) return
  if (!orderId.value) {
    showToast('订单不存在')
    return
  }
  if (!scores.scoreDesc || !scores.scoreAttitude || !scores.scoreExperience) {
    showToast('请完成评分')
    return
  }
  submitting.value = true
  try {
    await post(
      '/mini/review/submit',
      {
        orderId: orderId.value,
        scoreDesc: scores.scoreDesc,
        scoreAttitude: scores.scoreAttitude,
        scoreExperience: scores.scoreExperience,
        content: content.value.trim()
      },
      { showLoading: true }
    )
    showToast('评价成功')
    setTimeout(() => {
      uni.navigateBack()
    }, 400)
  } catch (error) {
    showToast('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onLoad((options = {}) => {
  if (!ensureLogin()) return
  const id = options.orderId ? Number(options.orderId) : null
  if (!id) {
    showToast('订单不存在')
    uni.navigateBack()
    return
  }
  orderId.value = id
  readonly.value = options.readonly === '1' || options.readonly === 'true'
  if (readonly.value) {
    fetchReviewDetail()
  } else {
    fetchOrderDetail()
  }
})
</script>

<style lang="scss" scoped>
.review-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  padding-bottom: 140rpx;
  box-sizing: border-box;
}

.review-card {
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.product {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.product__image {
  width: 120rpx;
  height: 120rpx;
  border-radius: var(--radius-md);
  background-color: var(--bg-grey);
}

.product__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.product__title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
}

.section-title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
}

.rating-row {
  display: flex;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid var(--border-light);
}

.rating-row:last-child {
  border-bottom: none;
}

.rating-row--readonly {
  padding: 8rpx 0;
}

.rating-label {
  width: 200rpx;
  font-size: var(--font-sm);
  color: var(--text-regular);
}

.rating-stars {
  flex: 1;
  display: flex;
  gap: 8rpx;
}

.star {
  font-size: 32rpx;
  color: var(--border-color);
}

.star.is-active {
  color: var(--warning-color);
}

.rating-score {
  width: 40rpx;
  text-align: right;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.textarea-wrap {
  margin-top: var(--spacing-md);
  background-color: var(--bg-grey);
  border-radius: var(--radius-md);
  padding: var(--spacing-sm);
}

.textarea {
  width: 100%;
  min-height: 180rpx;
  font-size: var(--font-sm);
  color: var(--text-primary);
}

.textarea-count {
  display: flex;
  justify-content: flex-end;
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.review-item {
  background-color: var(--bg-grey);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xs);
}

.review-label {
  font-size: var(--font-sm);
  color: var(--text-primary);
  font-weight: 600;
}

.review-auto {
  font-size: var(--font-xs);
  color: var(--success-color);
  background-color: var(--primary-bg);
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
}

.review-content {
  margin-top: var(--spacing-sm);
  font-size: var(--font-sm);
  color: var(--text-regular);
  line-height: 1.6;
}

.review-empty {
  text-align: center;
  font-size: var(--font-sm);
  color: var(--text-secondary);
  padding: var(--spacing-lg) 0;
}

.review-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  box-shadow: 0 -6rpx 20rpx rgba(15, 23, 42, 0.08);
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, var(--primary-light), var(--primary-color));
  color: var(--text-white);
  font-size: var(--font-md);
  font-weight: 600;
}

.submit-btn[disabled] {
  background: var(--border-color);
  color: var(--text-secondary);
}
</style>
