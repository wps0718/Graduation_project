<template>
  <view class="edit">
    <!-- 重新审核提示条 -->
    <view class="edit__banner">
      <text class="edit__banner-icon">⚠️</text>
      <text class="edit__banner-text">修改商品信息后需重新审核通过后上架</text>
    </view>

    <view class="edit__content">
      <!-- 图片管理 -->
      <view class="edit__card">
        <view class="edit__card-title">商品图片</view>
        <view class="edit__upload-grid">
          <view
            v-for="(item, index) in imageList"
            :key="item.id"
            class="edit__upload-item"
            @click="previewImage(index)"
            @longpress="setCover(index)"
          >
            <image class="edit__upload-img" :src="item.url" mode="aspectFill" />
            <view v-if="index === 0" class="edit__upload-cover">封面</view>
            <view class="edit__upload-remove" @click.stop="removeImage(index)">
              <text class="edit__upload-remove-text">×</text>
            </view>
          </view>
          <view v-if="imageList.length < maxImages" class="edit__upload-add" @click="chooseImages">
            <text class="edit__upload-plus">+</text>
            <text class="edit__upload-hint">上传图片</text>
          </view>
        </view>
        <text class="edit__upload-tip">长按可设置封面，最多{{ maxImages }}张</text>
      </view>

      <!-- 基本信息 -->
      <view class="edit__card">
        <view class="edit__card-title">基本信息</view>
        <view class="edit__field">
          <input
            class="edit__input"
            :value="form.title"
            placeholder="商品标题"
            placeholder-class="edit__placeholder"
            maxlength="30"
            @input="onInput('title', $event)"
          />
        </view>
        <view class="edit__row">
          <view class="edit__field edit__field--half">
            <view class="edit__price-prefix">¥</view>
            <input
              class="edit__input edit__input--price"
              :value="form.price"
              type="digit"
              placeholder="二手价格"
              placeholder-class="edit__placeholder"
              @input="onInput('price', $event)"
            />
          </view>
          <view class="edit__field edit__field--half">
            <view class="edit__price-prefix">¥</view>
            <input
              class="edit__input edit__input--price"
              :value="form.originalPrice"
              type="digit"
              placeholder="原价"
              placeholder-class="edit__placeholder"
              @input="onInput('originalPrice', $event)"
            />
          </view>
        </view>

        <picker :range="categoryList" range-key="name" @change="onCategoryChange">
          <view class="edit__field edit__field--select">
            <text class="edit__select-text" :class="{ 'is-placeholder': !form.categoryId }">{{ categoryLabel }}</text>
            <text class="edit__select-icon">›</text>
          </view>
        </picker>

        <picker :range="conditionList" range-key="label" @change="onConditionChange">
          <view class="edit__field edit__field--select">
            <text class="edit__select-text" :class="{ 'is-placeholder': !form.conditionLevel }">{{ conditionLabel }}</text>
            <text class="edit__select-icon">›</text>
          </view>
        </picker>

        <view class="edit__field edit__field--textarea">
          <textarea
            class="edit__textarea"
            :value="form.description"
            placeholder="请详细描述商品信息..."
            placeholder-class="edit__placeholder"
            maxlength="500"
            @input="onInput('description', $event)"
          />
          <text class="edit__counter">{{ descriptionCount }}/500</text>
        </view>
      </view>

      <!-- 交易信息 -->
      <view class="edit__card">
        <view class="edit__card-title">交易信息</view>
        <picker :range="campusList" range-key="name" @change="onCampusChange">
          <view class="edit__field edit__field--select">
            <text class="edit__select-text" :class="{ 'is-placeholder': !form.campusId }">{{ campusLabel }}</text>
            <text class="edit__select-icon">›</text>
          </view>
        </picker>

        <picker :range="meetingPointList" range-key="name" @change="onMeetingChange">
          <view class="edit__field edit__field--select">
            <text class="edit__select-text" :class="{ 'is-placeholder': !form.meetingPointId }">{{ meetingLabel }}</text>
            <text class="edit__select-icon">›</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 底部提交按钮 -->
    <view class="edit__footer safe-area-bottom">
      <view class="edit__footer-btn" :class="{ 'is-disabled': submitting }" @click="submitEdit">
        <text>提交修改</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { get, post, uploadFile } from '@/utils/request'
import { resolveImageUrl } from '@/utils/image'
import { useAppStore } from '@/store/app'
import { CONDITION_LEVELS } from '@/utils/constant'

const appStore = useAppStore()
const maxImages = 9

const productId = ref(null)
const imageList = ref([])
const meetingPointList = ref([])
const submitting = ref(false)
const loadingDetail = ref(true)

const form = ref({
  title: '',
  price: '',
  originalPrice: '',
  categoryId: null,
  conditionLevel: null,
  description: '',
  campusId: null,
  meetingPointId: null
})

const campusList = computed(() => appStore.campusList || [])
const categoryList = computed(() => appStore.categoryList || [])
const conditionList = CONDITION_LEVELS

const descriptionCount = computed(() => form.value.description.length)

const categoryLabel = computed(() => {
  if (!form.value.categoryId) return '商品分类'
  const found = categoryList.value.find(item => item.id === form.value.categoryId)
  return found ? found.name : '商品分类'
})

const conditionLabel = computed(() => {
  if (!form.value.conditionLevel) return '成色'
  const found = conditionList.find(item => item.value === form.value.conditionLevel)
  return found ? found.label : '成色'
})

const campusLabel = computed(() => {
  if (!form.value.campusId) return '交易校区'
  const found = campusList.value.find(item => item.id === form.value.campusId)
  return found ? found.name : '交易校区'
})

const meetingLabel = computed(() => {
  if (!form.value.meetingPointId) return '面交地点'
  const found = meetingPointList.value.find(item => item.id === form.value.meetingPointId)
  return found ? found.name : '面交地点'
})

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

async function loadProductDetail() {
  if (!productId.value) {
    showToast('商品ID不存在')
    return
  }
  loadingDetail.value = true
  try {
    const data = await get(`/mini/product/detail/${productId.value}`, {}, { showLoading: true })

    form.value.title = data.title || ''
    form.value.price = data.price ? String(data.price) : ''
    form.value.originalPrice = data.originalPrice ? String(data.originalPrice) : ''
    form.value.categoryId = data.categoryId || null
    form.value.conditionLevel = data.conditionLevel || null
    form.value.description = data.description || ''
    form.value.campusId = data.campusId || null
    form.value.meetingPointId = data.meetingPointId || null

    // 加载已有图片
    if (data.images && data.images.length > 0) {
      imageList.value = data.images.map((url, index) => ({
        id: `exist-${index}`,
        url: resolveImageUrl(url),
        isNew: false
      }))
    }

    // 加载该校区面交地点
    if (form.value.campusId) {
      await loadMeetingPoints()
    }
  } catch (e) {
    showToast('加载商品信息失败')
  } finally {
    loadingDetail.value = false
  }
}

async function loadMeetingPoints() {
  if (!form.value.campusId) {
    meetingPointList.value = []
    return
  }
  try {
    const data = await get(`/mini/campus/meeting-points/${form.value.campusId}`, {}, { showLoading: false })
    meetingPointList.value = Array.isArray(data) ? data : []
  } catch (e) {
    meetingPointList.value = []
  }
}

// 图片管理
function previewImage(index) {
  const urls = imageList.value.map(item => item.url)
  uni.previewImage({ current: index, urls })
}

function removeImage(index) {
  const list = [...imageList.value]
  list.splice(index, 1)
  imageList.value = list
}

function setCover(index) {
  if (index === 0) return
  const list = [...imageList.value]
  const [target] = list.splice(index, 1)
  list.unshift(target)
  imageList.value = list
}

async function chooseImages() {
  const count = maxImages - imageList.value.length
  if (count <= 0) return
  const res = await new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: resolve,
      fail: reject
    })
  }).catch(() => null)
  if (!res) return
  const files = res.tempFilePaths || []
  if (!files.length) return

  uni.showLoading({ title: '上传中', mask: true })
  for (const filePath of files) {
    try {
      const data = await uploadFile('/common/upload', filePath, { showLoading: false, formData: { type: 'product' } })
      const url = data && data.url ? resolveImageUrl(data.url) : ''
      if (url) {
        imageList.value.push({ id: `new-${Date.now()}-${Math.random()}`, url, isNew: true })
      }
    } catch (e) {
      showToast('上传失败，请重试')
    }
  }
  uni.hideLoading()
}

// 表单操作
function onInput(field, e) {
  form.value[field] = String((e && e.detail && e.detail.value) || '')
}

function onCategoryChange(e) {
  const index = Number((e && e.detail && e.detail.value) || 0)
  const item = categoryList.value[index]
  form.value.categoryId = item ? item.id : null
}

function onConditionChange(e) {
  const index = Number((e && e.detail && e.detail.value) || 0)
  const item = conditionList[index]
  form.value.conditionLevel = item ? item.value : null
}

async function onCampusChange(e) {
  const index = Number((e && e.detail && e.detail.value) || 0)
  const item = campusList.value[index]
  form.value.campusId = item ? item.id : null
  form.value.meetingPointId = null
  await loadMeetingPoints()
}

function onMeetingChange(e) {
  const index = Number((e && e.detail && e.detail.value) || 0)
  const item = meetingPointList.value[index]
  form.value.meetingPointId = item ? item.id : null
}

// 验证与提交
function validateForm() {
  if (!imageList.value.length) return '请至少上传一张图片'
  if (!form.value.title || form.value.title.trim().length < 1) return '请输入商品标题'
  if (form.value.title.length > 30) return '商品标题最多30字'
  if (!Number(form.value.price) || Number(form.value.price) <= 0) return '请输入有效的二手价格'
  if (form.value.originalPrice && Number(form.value.originalPrice) <= 0) return '原价需大于0'
  if (!form.value.categoryId) return '请选择商品分类'
  if (!form.value.conditionLevel) return '请选择成色'
  if (!form.value.campusId) return '请选择交易校区'
  if (!form.value.meetingPointId) return '请选择面交地点'
  return ''
}

async function submitEdit() {
  if (submitting.value) return
  const message = validateForm()
  if (message) {
    showToast(message)
    return
  }
  submitting.value = true

  const payload = {
    productId: Number(productId.value),
    title: form.value.title.trim(),
    price: Number(form.value.price),
    originalPrice: form.value.originalPrice ? Number(form.value.originalPrice) : 0,
    categoryId: form.value.categoryId,
    conditionLevel: form.value.conditionLevel,
    description: form.value.description.trim(),
    campusId: form.value.campusId,
    meetingPointId: form.value.meetingPointId,
    images: imageList.value.map(item => item.url)
  }

  try {
    await post('/mini/product/update', payload, { showLoading: true })
    showToast('提交成功，等待审核')
    setTimeout(() => {
      uni.navigateBack()
    }, 300)
  } catch (e) {
    showToast('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onLoad(async (options) => {
  productId.value = options?.id

  await Promise.all([
    appStore.loadCampusList(),
    appStore.loadCategoryList()
  ])

  await loadProductDetail()
})
</script>

<style lang="scss" scoped>
.edit {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: calc(140rpx + var(--spacing-lg));
}

/* ====== 重新审核提示条 ====== */
.edit__banner {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  margin: var(--spacing-md);
  background-color: #FFFBE6;
  border: 2rpx solid #FFE58F;
  border-radius: var(--radius-md);
}

.edit__banner-icon {
  font-size: 28rpx;
  flex-shrink: 0;
}

.edit__banner-text {
  font-size: var(--font-sm);
  color: #AD8B00;
  line-height: 1.5;
}

/* ====== 内容区域 ====== */
.edit__content {
  padding: 0 var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.edit__card {
  background-color: var(--bg-white);
  border-radius: 24rpx;
  padding: var(--spacing-md);
}

.edit__card-title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: var(--spacing-md);
}

/* ====== 图片管理 ====== */
.edit__upload-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-sm);
}

.edit__upload-item,
.edit__upload-add {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  background-color: var(--bg-grey);
}

.edit__upload-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.edit__upload-cover {
  position: absolute;
  left: 12rpx;
  bottom: 12rpx;
  padding: 6rpx 16rpx;
  border-radius: var(--radius-round);
  background-color: rgba(0, 0, 0, 0.55);
  color: var(--text-white);
  font-size: var(--font-xs);
}

.edit__upload-remove {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 18rpx;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit__upload-remove-text {
  color: var(--text-white);
  font-size: var(--font-sm);
  line-height: 1;
}

.edit__upload-add {
  border: 2rpx dashed var(--border-color);
  background-color: var(--bg-white);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.edit__upload-plus {
  font-size: 48rpx;
  color: var(--text-placeholder);
  line-height: 1;
}

.edit__upload-hint {
  font-size: 22rpx;
  color: var(--text-placeholder);
}

.edit__upload-tip {
  margin-top: var(--spacing-sm);
  font-size: 22rpx;
  color: var(--text-secondary);
}

/* ====== 表单字段 ====== */
.edit__field {
  display: flex;
  align-items: center;
  border-bottom: 2rpx solid var(--border-light);
  padding: 0 var(--spacing-sm);
  height: 90rpx;
  margin-bottom: var(--spacing-sm);
}

.edit__field:last-child {
  margin-bottom: 0;
}

.edit__field--half {
  flex: 1;
  margin-bottom: 0;
}

.edit__row {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.edit__price-prefix {
  font-size: 28rpx;
  color: var(--text-secondary);
  margin-right: 8rpx;
}

.edit__input {
  flex: 1;
  font-size: 28rpx;
  color: var(--text-primary);
  height: 100%;
}

.edit__input--price {
  text-align: left;
}

.edit__placeholder {
  color: var(--text-placeholder);
}

.edit__field--select {
  justify-content: space-between;
}

.edit__select-text {
  font-size: 28rpx;
  color: var(--text-primary);
}

.edit__select-text.is-placeholder {
  color: var(--text-placeholder);
}

.edit__select-icon {
  font-size: 32rpx;
  color: var(--text-secondary);
}

.edit__field--textarea {
  flex-direction: column;
  align-items: flex-start;
  height: 260rpx;
  padding: var(--spacing-md) var(--spacing-sm);
}

.edit__textarea {
  width: 100%;
  flex: 1;
  font-size: 28rpx;
  color: var(--text-primary);
  line-height: 1.6;
}

.edit__counter {
  align-self: flex-end;
  font-size: 22rpx;
  color: var(--text-secondary);
}

/* ====== 底部提交按钮 ====== */
.edit__footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  border-top: 2rpx solid var(--border-light);
}

.edit__footer-btn {
  height: 96rpx;
  border-radius: var(--radius-round);
  background: linear-gradient(135deg, #4A90D9, #357ABD);
  color: var(--text-white);
  font-size: var(--font-lg);
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 217, 0.4);
}

.edit__footer-btn.is-disabled {
  background: var(--border-light);
  color: var(--text-placeholder);
  box-shadow: none;
}

.edit__footer-btn:active {
  opacity: 0.9;
}
</style>
