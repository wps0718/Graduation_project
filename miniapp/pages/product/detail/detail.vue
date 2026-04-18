<template>
  <view class="detail">
    <view v-if="detail" class="detail__content">
      <view class="detail__swiper">
        <swiper
          class="detail__swiper-inner"
          :indicator-dots="imageCount > 1"
          :autoplay="imageCount > 1"
          circular
          indicator-active-color="var(--primary-color)"
          @change="onSwiperChange"
        >
          <swiper-item
            v-for="(img, index) in imageList"
            :key="img"
          >
            <image
              class="detail__swiper-image"
              :src="img"
              mode="aspectFill"
              @click="previewImage(index)"
            />
          </swiper-item>
        </swiper>
        <view v-if="imageCount > 0" class="detail__swiper-indicator">
          <text class="detail__swiper-indicator-text">{{ displayIndex }}/{{ imageCount }}</text>
        </view>
        <view class="detail__menu" @click="openMenu">
          <text class="detail__menu-text">···</text>
        </view>
      </view>

      <view class="detail__card">
        <view class="detail__title-row">
          <text class="detail__title">{{ detail.title }}</text>
          <StatusTag v-if="detail.status" type="product" :value="detail.status" />
        </view>
        <view class="detail__price-row">
          <Price :price="detail.price" :original-price="detail.originalPrice" />
          <text class="detail__views">浏览 {{ detail.viewCount || 0 }}</text>
        </view>
        <view v-if="tagList.length" class="detail__tags">
          <view v-for="tag in tagList" :key="tag" class="detail__tag">
            <text class="detail__tag-text">{{ tag }}</text>
          </view>
        </view>
        <view class="detail__meta">
          <view class="detail__meta-item">
            <text class="detail__meta-label">成色</text>
            <StatusTag type="condition" :value="detail.conditionLevel || 0" />
          </view>
          <view class="detail__meta-item">
            <text class="detail__meta-label">发布时间</text>
            <text class="detail__meta-value">{{ detail.createTime || '-' }}</text>
          </view>
        </view>
      </view>

      <view class="detail__section">
        <text class="detail__section-title">商品描述</text>
        <text class="detail__desc">{{ productDescription || '暂无描述' }}</text>
      </view>

      <view class="detail__section">
        <text class="detail__section-title">面交信息</text>
        <view class="detail__meeting">
          <image class="detail__meeting-icon" src="/static/svg/location.svg" mode="aspectFit" />
          <view class="detail__meeting-content">
            <text class="detail__meeting-title">{{ detail.campusName || '未设置校区' }}</text>
            <text class="detail__meeting-sub">{{ detail.meetingPointName || '未设置面交点' }}</text>
          </view>
        </view>
      </view>

      <view class="detail__section">
        <text class="detail__section-title">卖家信息</text>
        <view class="detail__seller" @click="goSellerProfile">
          <UserAvatar :src="seller.avatarUrl" :size="96" :show-auth="true" :auth-status="seller.authStatus" />
          <view class="detail__seller-info">
            <text class="detail__seller-name">{{ seller.nickName || '未命名卖家' }}</text>
            <view class="detail__seller-extra">
              <StatusTag type="auth" :value="seller.authStatus || 0" />
              <text class="detail__seller-score">信用 {{ seller.score || 0 }}</text>
            </view>
          </view>
          <text class="detail__seller-arrow">›</text>
        </view>
      </view>

      <view class="detail__section comment-section">
        <view class="comment-section__header">
          <text class="detail__section-title">全部留言</text>
          <text class="comment-section__count">{{ commentCount }}</text>
        </view>
        
        <view v-if="comments.length > 0" class="comment-list">
          <view v-for="comment in comments" :key="comment.id" class="comment-item">
            <view class="comment-item__main">
              <view class="comment-item__avatar">
                <UserAvatar :src="comment.avatarUrl" :size="64" />
              </view>
              <view class="comment-item__content-wrap">
                <view class="comment-item__user-row">
                  <text class="comment-item__user-name">{{ comment.nickName }}</text>
                  <text v-if="comment.userId === seller.id" class="comment-item__seller-tag">卖家</text>
                </view>
                <text class="comment-item__text">{{ comment.content }}</text>
                <view class="comment-item__footer">
                  <text class="comment-item__time">{{ formatCommentTime(comment.createTime) }}</text>
                  <view class="comment-item__actions">
                    <text class="comment-item__action" @click="onReply(comment)">回复</text>
                    <text v-if="comment.userId === userStore.userInfo?.id" class="comment-item__action is-danger" @click="onDeleteComment(comment.id)">删除</text>
                  </view>
                </view>
              </view>
            </view>

            <view v-if="comment.replies && comment.replies.length > 0" class="comment-replies">
              <view v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <view class="reply-item__avatar">
                  <UserAvatar :src="reply.avatarUrl" :size="48" />
                </view>
                <view class="reply-item__content-wrap">
                  <view class="reply-item__user-row">
                    <text class="reply-item__user-name">{{ reply.nickName }}</text>
                    <text v-if="reply.userId === seller.id" class="reply-item__seller-tag">卖家</text>
                    <text v-if="reply.replyToNickName" class="reply-item__to-user">回复 {{ reply.replyToNickName }}</text>
                  </view>
                  <text class="reply-item__text">{{ reply.content }}</text>
                  <view class="reply-item__footer">
                    <text class="reply-item__time">{{ formatCommentTime(reply.createTime) }}</text>
                    <view class="reply-item__actions">
                      <text class="reply-item__action" @click="onReply(reply)">回复</text>
                      <text v-if="reply.userId === userStore.userInfo?.id" class="reply-item__action is-danger" @click="onDeleteComment(reply.id)">删除</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="comment-empty">
          <text class="comment-empty__text">暂无留言，快来抢沙发吧~</text>
        </view>

        <view class="comment-bar" @click="onOpenComment">
          <view class="comment-bar__input-placeholder">
            <text class="comment-bar__text">问问更多细节...</text>
          </view>
          <button class="comment-bar__btn">留言</button>
        </view>
      </view>
    </view>

    <!-- 留言输入弹窗 -->
    <view v-if="showCommentInput" class="comment-popup" @touchmove.stop.prevent>
      <view class="comment-popup__mask" @click="onCloseComment"></view>
      <view class="comment-popup__content">
        <view class="comment-popup__header">
          <text class="comment-popup__title">{{ replyTo ? `回复 ${replyTo.nickName}` : '发表留言' }}</text>
          <text class="comment-popup__cancel" @click="onCloseComment">取消</text>
        </view>
        <textarea
          class="comment-popup__textarea"
          v-model="commentContent"
          :placeholder="replyTo ? '回复内容...' : '问问更多细节...'"
          :focus="showCommentInput"
          maxlength="300"
          fixed
          show-confirm-bar
        ></textarea>
        <view class="comment-popup__footer">
          <text class="comment-popup__count">{{ commentContent.length }}/300</text>
          <button class="comment-popup__submit" :disabled="!commentContent.trim()" @click="submitComment">发送</button>
        </view>
      </view>
    </view>

    <view v-else class="detail__empty">
      <EmptyState title="暂无商品信息" description="请稍后重试" />
    </view>

    <view class="detail__bottom safe-area-bottom">
      <view class="detail__bottom-left">
        <view class="detail__bottom-item" @click="toggleFavorite">
          <view class="detail__bottom-icon" :class="{ 'is-active': isFavorited }">
            <image
              class="detail__bottom-icon-img"
              :src="isFavorited ? '/static/svg/favorited.svg' : '/static/svg/favorite.svg'"
              mode="aspectFit"
            />
          </view>
          <text class="detail__bottom-label">{{ isFavorited ? '已收藏' : '收藏' }}</text>
        </view>
        <view class="detail__bottom-item" @click="onReport">
          <view class="detail__bottom-icon">
            <image class="detail__bottom-icon-img" src="/static/svg/report.svg" mode="aspectFit" />
          </view>
          <text class="detail__bottom-label">举报</text>
        </view>
      </view>
      <view
        class="detail__bottom-action"
        :class="{ 'is-disabled': actionDisabled }"
        @click="onWant"
      >
        <text class="detail__bottom-action-text">{{ actionButtonText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { isLogin } from '@/utils/auth'
import { PRODUCT_STATUS } from '@/utils/constant'
import { useUserStore } from '@/store'
import Price from '@/components/price/price.vue'
import UserAvatar from '@/components/user-avatar/user-avatar.vue'
import StatusTag from '@/components/status-tag/status-tag.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'
import { buildMenuOptions, buildSharePayload } from '@/utils/product-detail-helpers'

const FOOTPRINT_KEY = 'footprint_list'
const FOOTPRINT_MAX = 50
const FOOTPRINT_EXPIRE = 7 * 24 * 60 * 60 * 1000

const detail = ref(null)
const loading = ref(false)
const currentIndex = ref(0)
const isFavorited = ref(false)

// 留言相关
const comments = ref([])
const showCommentInput = ref(false)
const commentContent = ref('')
const replyTo = ref(null)

const commentCount = computed(() => {
  let count = comments.value.length
  comments.value.forEach(c => {
    if (c.replies) count += c.replies.length
  })
  return count
})

const userStore = useUserStore()

const imageList = computed(() => {
  if (!detail.value) return []
  const images = detail.value.images || []
  if (Array.isArray(images) && images.length > 0) {
    return images
  }
  if (detail.value.coverImage) {
    return [detail.value.coverImage]
  }
  return []
})

const imageCount = computed(() => imageList.value.length)
const displayIndex = computed(() => {
  if (!imageCount.value) return 0
  return currentIndex.value + 1
})

const tagList = computed(() => {
  const tags = (detail.value && detail.value.tags) || []
  if (Array.isArray(tags) && tags.length > 0) {
    return tags
  }
  const categoryName = detail.value && detail.value.categoryName
  return categoryName ? [categoryName] : []
})

const seller = computed(() => {
  if (!detail.value) return {}
  const fromObject = detail.value.seller
  if (fromObject && typeof fromObject === 'object') {
    return {
      ...fromObject,
      status: fromObject.status ?? fromObject.userStatus
    }
  }
  const id = detail.value.sellerId
  if (!id) return {}
  return {
    id,
    nickName: detail.value.sellerNickName,
    avatarUrl: detail.value.sellerAvatarUrl,
    score: detail.value.sellerScore,
    authStatus: detail.value.sellerAuthStatus,
    status: detail.value.sellerStatus
  }
});

const productDescription = computed(() => {
  if (!detail.value) return ''
  return detail.value.description || detail.value.desc || ''
})

const isOwner = computed(() => !!(detail.value && detail.value.isOwner))
const isOffShelf = computed(() => detail.value && detail.value.status === PRODUCT_STATUS.OFF_SHELF)
const isSold = computed(() => detail.value && detail.value.status === PRODUCT_STATUS.SOLD)
const isSellerBanned = computed(() => seller.value && seller.value.status === 0)

const actionDisabled = computed(() => {
  if (!detail.value) return true
  if (isOwner.value) return false
  if (isSellerBanned.value) return true
  return isOffShelf.value || isSold.value
})

const actionButtonText = computed(() => {
  if (isOwner.value) return '管理商品'
  if (isSellerBanned.value) return '卖家已封禁'
  if (isSold.value) return '已售出'
  if (isOffShelf.value) return '已下架'
  return '我想要'
})

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

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

function saveFootprint(product) {
  if (!product || !product.id) return
  const list = normalizeFootprints(uni.getStorageSync(FOOTPRINT_KEY))
  const next = list.filter(item => item.id !== product.id)
  next.unshift({ ...product, viewedAt: Date.now() })
  if (next.length > FOOTPRINT_MAX) {
    next.length = FOOTPRINT_MAX
  }
  uni.setStorageSync(FOOTPRINT_KEY, next)
}

function promptLogin() {
  showToast('请先登录')
  setTimeout(() => {
    uni.navigateTo({ url: '/pages/login/login' })
  }, 300)
}

async function fetchDetail(id) {
  if (!id) return
  loading.value = true
  try {
    const data = await get(`/mini/product/detail/${id}`, {}, { showLoading: true })
    detail.value = data
    isFavorited.value = !!(data && data.isFavorited)
    currentIndex.value = 0
    saveFootprint(data)
    if (isLogin()) {
      await checkFavorite(id)
    }
    fetchComments(id)
  } catch (error) {
    showToast('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

async function fetchComments(productId) {
  try {
    const data = await get(`/mini/product/comment/list/${productId}`)
    comments.value = data || []
  } catch (error) {
    console.error('Fetch comments error:', error)
  }
}

function onOpenComment() {
  if (!isLogin()) {
    promptLogin()
    return
  }
  replyTo.value = null
  showCommentInput.value = true
}

function onCloseComment() {
  showCommentInput.value = false
  commentContent.value = ''
  replyTo.value = null
}

function onReply(comment) {
  if (!isLogin()) {
    promptLogin()
    return
  }
  replyTo.value = {
    id: comment.id,
    nickName: comment.nickName
  }
  showCommentInput.value = true
}

async function submitComment() {
  if (!commentContent.value.trim()) return
  try {
    await post('/mini/product/comment/add', {
      productId: detail.value.id,
      parentId: replyTo.value ? replyTo.value.id : null,
      content: commentContent.value
    })
    showToast('留言成功')
    onCloseComment()
    fetchComments(detail.value.id)
  } catch (error) {
    showToast('留言失败，请重试')
  }
}

async function onDeleteComment(commentId) {
  const confirm = await showConfirm('确认删除这条留言吗？')
  if (!confirm) return
  try {
    await post(`/mini/product/comment/delete/${commentId}`)
    showToast('已删除')
    fetchComments(detail.value.id)
  } catch (error) {
    showToast('删除失败')
  }
}

function formatCommentTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace('T', ' ').replace(/-/g, '/'))
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) return '刚刚'
  if (diff < hour) return Math.floor(diff / minute) + '分钟前'
  if (diff < day) return Math.floor(diff / hour) + '小时前'
  if (diff < 2 * day) return '昨天'
  
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  return `${m}-${d}`
}

async function checkFavorite(id) {
  try {
    const data = await get(`/mini/favorite/check/${id}`, {}, { showLoading: false })
    isFavorited.value = !!(data && data.isFavorited)
  } catch (error) {
    isFavorited.value = !!(detail.value && detail.value.isFavorited)
  }
}

function onSwiperChange(e) {
  currentIndex.value = (e && e.detail && e.detail.current) || 0
}

function previewImage(index) {
  if (!imageCount.value) return
  const current = imageList.value[index] || imageList.value[0]
  uni.previewImage({ urls: imageList.value, current })
}

async function toggleFavorite() {
  if (!detail.value || !detail.value.id) return
  if (!isLogin()) {
    promptLogin()
    return
  }
  const id = detail.value.id
  const next = !isFavorited.value
  const url = next ? '/mini/favorite/add' : '/mini/favorite/cancel'
  try {
    await post(url, { productId: id }, { showLoading: true })
    isFavorited.value = next
    const count = detail.value.favoriteCount || 0
    detail.value.favoriteCount = Math.max(0, count + (next ? 1 : -1))
  } catch (error) {
    showToast('操作失败，请稍后重试')
  }
}

function onReport() {
  if (!detail.value || !detail.value.id) return
  uni.navigateTo({ url: `/pages/report/report?productId=${detail.value.id}` })
}

function openMenu() {
  const menu = buildMenuOptions({
    isOwner: isOwner.value,
    isOffShelf: isOffShelf.value
  })
  if (!menu.items.length) return
  uni.showActionSheet({
    itemList: menu.items,
    success: (res) => {
      const action = menu.actions[res.tapIndex]
      handleMenuAction(action)
    }
  })
}

async function handleMenuAction(action) {
  if (!detail.value || !detail.value.id) return
  const id = detail.value.id
  if (action === 'edit') {
    uni.navigateTo({ url: `/pages/product/edit/edit?id=${id}` })
    return
  }
  if (action === 'share') {
    uni.showShareMenu({ withShareTicket: true })
    return
  }
  if (action === 'report') {
    uni.navigateTo({ url: `/pages/report/report?productId=${id}` })
    return
  }
  if (action === 'offShelf') {
    const confirm = await showConfirm('确认下架该商品吗')
    if (!confirm) return
    await post('/mini/product/off-shelf', { productId: id }, { showLoading: true })
    detail.value.status = PRODUCT_STATUS.OFF_SHELF
    showToast('已下架')
    return
  }
  if (action === 'onShelf') {
    const confirm = await showConfirm('确认上架该商品吗')
    if (!confirm) return
    await post('/mini/product/on-shelf', { productId: id }, { showLoading: true })
    detail.value.status = PRODUCT_STATUS.ON_SALE
    showToast('已上架')
    return
  }
  if (action === 'delete') {
    const confirm = await showConfirm('删除后不可恢复，是否继续')
    if (!confirm) return
    await post('/mini/product/delete', { productId: id }, { showLoading: true })
    showToast('已删除')
    setTimeout(() => {
      uni.navigateBack()
    }, 300)
  }
}

function showConfirm(content) {
  return new Promise((resolve) => {
    uni.showModal({
      title: '提示',
      content,
      confirmText: '确定',
      cancelText: '取消',
      success: (res) => resolve(res && res.confirm)
    })
  })
}

async function onWant() {
  if (actionDisabled.value) {
    if (!detail.value) return
    if (!isOwner.value) {
      showToast(actionButtonText.value)
    }
    return
  }
  if (isOwner.value) {
    openMenu()
    return
  }
  if (!isLogin()) {
    promptLogin()
    return
  }
  
  try {
    // 创建或获取聊天会话
    const data = await post('/mini/chat/session/create', {
      peerId: seller.value.id,
      productId: detail.value.id
    }, { showLoading: true })
    
    if (data && data.sessionKey) {
      uni.navigateTo({
        url: `/pages/chat/detail/detail?sessionKey=${data.sessionKey}&productId=${detail.value.id}&peerId=${seller.value.id}`
      })
    }
  } catch (error) {
    showToast('发起私信失败，请重试')
  }
}

function goSellerProfile() {
  if (!seller.value || !seller.value.id) return
  const userId = userStore.userInfo && userStore.userInfo.id
  if (userId && seller.value.id === userId) {
    uni.switchTab({ url: '/pages/user/user' })
    return
  }
  uni.navigateTo({ url: `/pages/seller/profile?id=${seller.value.id}` })
}

onLoad((options = {}) => {
  const id = options.id ? Number(options.id) : null
  if (!id) {
    showToast('商品不存在')
    return
  }
  fetchDetail(id)
})

onShareAppMessage(() => {
  return buildSharePayload(detail.value)
})
</script>

<style lang="scss" scoped>
.detail {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: 160rpx;
}

.detail__content {
  padding-bottom: 40rpx;
}

.detail__swiper {
  position: relative;
  width: 100%;
  height: 520rpx;
  background-color: var(--bg-grey);
}

.detail__swiper-inner {
  width: 100%;
  height: 100%;
}

.detail__swiper-image {
  width: 100%;
  height: 100%;
}

.detail__swiper-indicator {
  position: absolute;
  right: var(--spacing-md);
  bottom: var(--spacing-md);
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background-color: rgba(0, 0, 0, 0.45);
}

.detail__swiper-indicator-text {
  font-size: var(--font-sm);
  color: var(--text-white);
}

.detail__menu {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background-color: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail__menu-text {
  color: var(--text-white);
  font-size: 36rpx;
  line-height: 1;
}

.detail__card {
  margin: var(--spacing-md);
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
}

.detail__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.detail__title {
  flex: 1;
  font-size: var(--font-lg);
  color: var(--text-primary);
  margin-right: var(--spacing-sm);
}

.detail__price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.detail__views {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.detail__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: var(--spacing-sm);
}

.detail__tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background-color: var(--bg-light);
}

.detail__tag-text {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.detail__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--spacing-md);
}

.detail__meta-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.detail__meta-label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.detail__meta-value {
  font-size: var(--font-sm);
  color: var(--text-primary);
}

.detail__section {
  margin: 0 var(--spacing-md) var(--spacing-md);
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
}

.detail__section-title {
  display: block;
  margin-bottom: var(--spacing-sm);
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
}

.detail__desc {
  font-size: var(--font-md);
  color: var(--text-regular);
  line-height: 1.6;
}

.detail__meeting {
  display: flex;
  align-items: center;
}

.detail__meeting-icon {
  width: 40rpx;
  height: 40rpx;
  margin-right: var(--spacing-sm);
}

.detail__meeting-content {
  display: flex;
  flex-direction: column;
}

.detail__meeting-title {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.detail__meeting-sub {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  margin-top: 6rpx;
}

.detail__seller {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail__seller-info {
  flex: 1;
  margin-left: var(--spacing-md);
}

.detail__seller-name {
  font-size: var(--font-md);
  color: var(--text-primary);
  margin-bottom: 8rpx;
}

.detail__seller-extra {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.detail__seller-score {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.detail__seller-arrow {
  font-size: 36rpx;
  color: var(--text-secondary);
}

.detail__bottom {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx var(--spacing-md);
  background-color: var(--bg-white);
  box-shadow: 0 -6rpx 20rpx rgba(0, 0, 0, 0.06);
  z-index: 20;
}

.detail__bottom-left {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.detail__bottom-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  min-width: 84rpx;
}

.detail__bottom-icon {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail__bottom-icon-img {
  width: 40rpx;
  height: 40rpx;
}

.detail__bottom-label {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.detail__bottom-action {
  flex: 1;
  margin-left: var(--spacing-md);
  height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, var(--primary-light), var(--primary-color));
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 16rpx rgba(74, 144, 217, 0.3);
}

.detail__bottom-action.is-disabled {
  background: var(--border-color);
  box-shadow: none;
}

.detail__bottom-action-text {
  font-size: var(--font-md);
  color: var(--text-white);
  font-weight: bold;
}

// 留言板块样式
.comment-section {
  padding-bottom: 30rpx;

  &__header {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
  }

  &__count {
    font-size: var(--font-sm);
    color: var(--text-secondary);
    margin-left: 12rpx;
  }
}

.comment-list {
  margin-bottom: 30rpx;
}

.comment-item {
  margin-bottom: 32rpx;

  &__main {
    display: flex;
  }

  &__avatar {
    flex-shrink: 0;
    margin-right: 20rpx;
  }

  &__content-wrap {
    flex: 1;
    border-bottom: 1rpx solid var(--border-light);
    padding-bottom: 24rpx;
  }

  &__user-row {
    display: flex;
    align-items: center;
    margin-bottom: 8rpx;
  }

  &__user-name {
    font-size: var(--font-sm);
    font-weight: 500;
    color: var(--text-primary);
  }

  &__seller-tag {
    font-size: 20rpx;
    color: var(--primary-color);
    background: rgba(74, 144, 226, 0.1);
    padding: 2rpx 8rpx;
    border-radius: 4rpx;
    margin-left: 12rpx;
  }

  &__text {
    font-size: var(--font-md);
    color: var(--text-primary);
    line-height: 1.5;
    word-break: break-all;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12rpx;
  }

  &__time {
    font-size: 22rpx;
    color: var(--text-placeholder);
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 24rpx;
  }

  &__action {
    font-size: var(--font-sm);
    color: var(--text-secondary);

    &.is-danger {
      color: var(--error-color);
    }
  }
}

.comment-replies {
  margin-top: 20rpx;
  padding-left: 84rpx;
}

.reply-item {
  display: flex;
  margin-bottom: 24rpx;

  &:last-child {
    margin-bottom: 0;
  }

  &__avatar {
    flex-shrink: 0;
    margin-right: 16rpx;
  }

  &__content-wrap {
    flex: 1;
  }

  &__user-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    margin-bottom: 4rpx;
  }

  &__user-name {
    font-size: 24rpx;
    font-weight: 500;
    color: var(--text-primary);
  }

  &__seller-tag {
    font-size: 18rpx;
    color: var(--primary-color);
    background: rgba(74, 144, 226, 0.1);
    padding: 2rpx 6rpx;
    border-radius: 4rpx;
    margin-left: 8rpx;
  }

  &__to-user {
    font-size: 24rpx;
    color: var(--text-secondary);
    margin-left: 12rpx;

    &::before {
      content: '';
      display: inline-block;
    }
  }

  &__text {
    font-size: 26rpx;
    color: var(--text-primary);
    line-height: 1.4;
    word-break: break-all;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 8rpx;
  }

  &__time {
    font-size: 20rpx;
    color: var(--text-placeholder);
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }

  &__action {
    font-size: 24rpx;
    color: var(--text-secondary);

    &.is-danger {
      color: var(--error-color);
    }
  }
}

.comment-empty {
  padding: 40rpx 0;
  text-align: center;

  &__text {
    font-size: var(--font-sm);
    color: var(--text-placeholder);
  }
}

.comment-bar {
  display: flex;
  align-items: center;
  background: var(--bg-grey);
  border-radius: 999rpx;
  padding: 12rpx 12rpx 12rpx 30rpx;
  margin-top: 20rpx;

  &__input-placeholder {
    flex: 1;
  }

  &__text {
    font-size: var(--font-sm);
    color: var(--text-placeholder);
  }

  &__btn {
    min-width: 120rpx;
    height: 64rpx;
    line-height: 64rpx;
    background: var(--primary-color);
    color: var(--text-white);
    font-size: var(--font-sm);
    border-radius: 999rpx;
    padding: 0 30rpx;
    margin: 0;

    &::after {
      border: none;
    }
  }
}

// 留言弹窗
.comment-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;

  &__mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
  }

  &__content {
    position: relative;
    background: var(--bg-white);
    border-radius: 32rpx 32rpx 0 0;
    padding: 30rpx 30rpx calc(30rpx + var(--safe-area-inset-bottom));
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30rpx;
  }

  &__title {
    font-size: var(--font-md);
    font-weight: bold;
    color: var(--text-primary);
  }

  &__cancel {
    font-size: var(--font-md);
    color: var(--text-secondary);
  }

  &__textarea {
    width: 100%;
    height: 240rpx;
    background: var(--bg-grey);
    border-radius: 16rpx;
    padding: 20rpx;
    box-sizing: border-box;
    font-size: var(--font-md);
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 20rpx;
  }

  &__count {
    font-size: var(--font-sm);
    color: var(--text-placeholder);
  }

  &__submit {
    min-width: 160rpx;
    height: 72rpx;
    line-height: 72rpx;
    background: var(--primary-color);
    color: var(--text-white);
    font-size: var(--font-md);
    border-radius: 999rpx;
    margin: 0;

    &[disabled] {
      background: var(--border-color);
      color: var(--text-placeholder);
    }

    &::after {
      border: none;
    }
  }
}

.detail__empty {
  padding: 160rpx var(--spacing-md) 0;
}
</style>
