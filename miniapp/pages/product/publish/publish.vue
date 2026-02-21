<template>
  <view class="publish">
    <view :style="{ height: `${statusBarHeight}px` }"></view>
    <view class="publish__nav" :style="{ height: `${navBarHeight}px` }">
      <view class="publish__nav-left" @click="goBack">
        <text class="publish__nav-close">×</text>
      </view>
      <text class="publish__nav-title">发布闲置</text>
      <view class="publish__nav-right" :class="{ 'is-disabled': submitDisabled }" @click="submitPublish">
        <text class="publish__nav-action">发布</text>
      </view>
    </view>

    <view class="publish__content">
      <view class="publish__card">
        <view class="publish__upload">
          <view class="publish__upload-grid">
            <view
              v-for="(item, index) in imageList"
              :key="item.id"
              class="publish__upload-item"
              @longpress="setCover(index)"
            >
              <image class="publish__upload-img" :src="item.url" mode="aspectFill" />
              <view v-if="index === 0" class="publish__upload-cover">封面</view>
              <view class="publish__upload-remove" @click.stop="removeImage(index)">
                <text class="publish__upload-remove-text">×</text>
              </view>
            </view>
            <view v-if="imageList.length < maxImages" class="publish__upload-add" @click="chooseImages">
              <text class="publish__upload-plus">+</text>
              <text class="publish__upload-hint">最多上传{{ maxImages }}张</text>
            </view>
          </view>
          <text class="publish__upload-tip">长按可设置封面，最多{{ maxImages }}张</text>
        </view>
      </view>

      <view class="publish__card">
        <view class="publish__field">
          <input
            class="publish__input"
            :value="form.title"
            placeholder="设备名称"
            placeholder-class="publish__placeholder"
            maxlength="50"
            @input="onInput('title', $event)"
          />
        </view>
        <view class="publish__row">
          <view class="publish__field publish__field--half">
            <view class="publish__price-prefix">¥</view>
            <input
              class="publish__input publish__input--price"
              :value="form.price"
              type="digit"
              placeholder="二手价格"
              placeholder-class="publish__placeholder"
              @input="onInput('price', $event)"
            />
          </view>
          <view class="publish__field publish__field--half">
            <view class="publish__price-prefix">¥</view>
            <input
              class="publish__input publish__input--price"
              :value="form.originalPrice"
              type="digit"
              placeholder="原价"
              placeholder-class="publish__placeholder"
              @input="onInput('originalPrice', $event)"
            />
          </view>
        </view>

        <picker :range="categoryList" range-key="name" @change="onCategoryChange">
          <view class="publish__field publish__field--select">
            <text class="publish__select-text">{{ categoryLabel }}</text>
            <text class="publish__select-icon">›</text>
          </view>
        </picker>

        <picker :range="conditionList" range-key="label" @change="onConditionChange">
          <view class="publish__field publish__field--select">
            <text class="publish__select-text">{{ conditionLabel }}</text>
            <text class="publish__select-icon">›</text>
          </view>
        </picker>

        <view class="publish__field publish__field--textarea">
          <textarea
            class="publish__textarea"
            :value="form.description"
            placeholder="介绍一下你的闲置物品..."
            placeholder-class="publish__placeholder"
            maxlength="500"
            @input="onInput('description', $event)"
          />
          <text class="publish__counter">{{ descriptionCount }}/500</text>
        </view>
      </view>

      <view class="publish__card">
        <picker :range="campusList" range-key="name" @change="onCampusChange">
          <view class="publish__field publish__field--select">
            <text class="publish__select-text">{{ campusLabel }}</text>
            <text class="publish__select-icon">›</text>
          </view>
        </picker>

        <view class="publish__meeting">
          <view class="publish__meeting-tabs">
            <view
              class="publish__meeting-tab"
              :class="{ 'is-active': !customMeeting }"
              @click="customMeeting = false"
            >
              预设地点
            </view>
            <view
              class="publish__meeting-tab"
              :class="{ 'is-active': customMeeting }"
              @click="customMeeting = true"
            >
              自定义
            </view>
          </view>
          <view v-if="!customMeeting">
            <picker :range="meetingPointList" range-key="name" @change="onMeetingChange">
              <view class="publish__field publish__field--select">
                <text class="publish__select-text">{{ meetingLabel }}</text>
                <text class="publish__select-icon">›</text>
              </view>
            </picker>
          </view>
          <view v-else>
            <view class="publish__field">
              <input
                class="publish__input"
                :value="form.meetingPointName"
                placeholder="请输入面交地点"
                placeholder-class="publish__placeholder"
                maxlength="30"
                @input="onInput('meetingPointName', $event)"
              />
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="publish__footer safe-area-bottom">
      <view class="publish__footer-btn" :class="{ 'is-disabled': submitDisabled }" @click="submitPublish">
        发布
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { useAppStore } from '@/store/app'
import { CONDITION_LEVELS } from '@/utils/constant'

const appStore = useAppStore()
const statusBarHeight = ref(0)
const navBarHeight = ref(44)
const maxImages = 9

const imageList = ref([])
const uploadLoading = ref(false)
const customMeeting = ref(false)
const meetingPointList = ref([])
const submitting = ref(false)

const form = ref({
  title: '',
  price: '',
  originalPrice: '',
  categoryId: null,
  conditionLevel: null,
  description: '',
  campusId: null,
  meetingPointId: null,
  meetingPointName: ''
})

const campusList = computed(() => appStore.campusList || [])
const categoryList = computed(() => appStore.categoryList || [])
const conditionList = CONDITION_LEVELS

const descriptionCount = computed(() => form.value.description.length)

const categoryLabel = computed(() => {
  const found = categoryList.value.find((item) => item.id === form.value.categoryId)
  return found ? found.name : '产品类型'
})

const conditionLabel = computed(() => {
  const found = conditionList.find((item) => item.value === form.value.conditionLevel)
  return found ? found.label : '产品磨损程度'
})

const campusLabel = computed(() => {
  const found = campusList.value.find((item) => item.id === form.value.campusId)
  return found ? found.name : '交易校区'
})

const meetingLabel = computed(() => {
  const found = meetingPointList.value.find((item) => item.id === form.value.meetingPointId)
  return found ? found.name : '面交地点'
})

const submitDisabled = computed(() => submitting.value)

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function goBack() {
  const pages = getCurrentPages()
  if (!pages || pages.length <= 1) {
    uni.switchTab({ url: '/pages/index/index' })
    return
  }
  uni.navigateBack()
}

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
  form.value.meetingPointName = ''
  customMeeting.value = false
  await loadMeetingPoints()
}

function onMeetingChange(e) {
  const index = Number((e && e.detail && e.detail.value) || 0)
  const item = meetingPointList.value[index]
  form.value.meetingPointId = item ? item.id : null
  form.value.meetingPointName = item ? item.name : ''
}

function setCover(index) {
  if (index === 0) return
  const list = [...imageList.value]
  const [target] = list.splice(index, 1)
  list.unshift(target)
  imageList.value = list
}

function removeImage(index) {
  const list = [...imageList.value]
  list.splice(index, 1)
  imageList.value = list
}

async function chooseImages() {
  if (uploadLoading.value) return
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
  uploadLoading.value = true
  uni.showLoading({ title: '上传中', mask: true })
  for (const filePath of files) {
    try {
      const url = await uploadImage(filePath)
      if (url) {
        imageList.value.push({ id: `${Date.now()}-${Math.random()}`, url })
      }
    } catch (e) {
      showToast('上传失败，请重试')
    }
  }
  uni.hideLoading()
  uploadLoading.value = false
}

async function uploadImage(filePath) {
  const data = await post('/common/upload', { filePath }, { showLoading: false })
  return data && data.url ? data.url : ''
}

function validateForm() {
  if (!imageList.value.length) return '请至少上传一张图片'
  if (!form.value.title || form.value.title.length < 1) return '请输入商品名称'
  if (form.value.title.length > 50) return '商品名称最多50字'
  if (!Number(form.value.price) || Number(form.value.price) <= 0) return '请输入有效的二手价格'
  if (form.value.originalPrice && Number(form.value.originalPrice) <= 0) return '原价需大于0'
  if (!form.value.categoryId) return '请选择产品类型'
  if (!form.value.conditionLevel) return '请选择磨损程度'
  if (!form.value.description || form.value.description.length < 1) return '请输入商品描述'
  if (form.value.description.length > 500) return '商品描述最多500字'
  if (!form.value.campusId) return '请选择交易校区'
  if (customMeeting.value) {
    if (!form.value.meetingPointName) return '请输入面交地点'
  } else if (!form.value.meetingPointId) {
    return '请选择面交地点'
  }
  return ''
}

async function submitPublish() {
  if (submitting.value) return
  const message = validateForm()
  if (message) {
    showToast(message)
    return
  }
  submitting.value = true
  const payload = {
    title: form.value.title.trim(),
    price: Number(form.value.price),
    originalPrice: form.value.originalPrice ? Number(form.value.originalPrice) : 0,
    categoryId: form.value.categoryId,
    conditionLevel: form.value.conditionLevel,
    description: form.value.description.trim(),
    campusId: form.value.campusId,
    meetingPointId: customMeeting.value ? null : form.value.meetingPointId,
    meetingPointName: customMeeting.value ? form.value.meetingPointName : form.value.meetingPointName,
    coverImage: imageList.value[0].url,
    images: imageList.value.map((item) => item.url)
  }
  try {
    await post('/mini/product/publish', payload, { showLoading: true })
    showToast('发布成功，等待审核')
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 300)
  } catch (e) {
    showToast('发布失败，请稍后重试')
  } finally {
    submitting.value = false
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

onMounted(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = (info && info.statusBarHeight) || 0
  const menuButton = typeof uni.getMenuButtonBoundingClientRect === 'function'
    ? uni.getMenuButtonBoundingClientRect()
    : null
  if (menuButton && menuButton.top) {
    const padding = menuButton.top - statusBarHeight.value
    navBarHeight.value = menuButton.height + padding * 2
  } else {
    navBarHeight.value = 44
  }
})

onLoad(async () => {
  await Promise.all([appStore.loadCampusList(), appStore.loadCategoryList()])
  if (!form.value.campusId && appStore.currentCampusId) {
    form.value.campusId = appStore.currentCampusId
  }
  await loadMeetingPoints()
})
</script>

<style lang="scss" scoped>
.publish {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: calc(140rpx + var(--spacing-lg));
}

.publish__nav {
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-md);
  background-color: var(--bg-page);
}

.publish__nav-left,
.publish__nav-right {
  width: 120rpx;
  display: flex;
  align-items: center;
}

.publish__nav-right {
  justify-content: flex-end;
}

.publish__nav-title {
  font-size: var(--font-lg);
  color: var(--text-primary);
  font-weight: 600;
}

.publish__nav-close {
  font-size: 48rpx;
  color: var(--text-secondary);
}

.publish__nav-action {
  font-size: var(--font-md);
  color: var(--success-color);
  font-weight: 600;
}

.publish__nav-right.is-disabled .publish__nav-action {
  color: var(--text-placeholder);
}

.publish__content {
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.publish__card {
  background-color: var(--bg-white);
  border-radius: 24rpx;
  padding: var(--spacing-md);
}

.publish__upload-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-sm);
}

.publish__upload-item,
.publish__upload-add {
  position: relative;
  width: 100%;
  height: 200rpx;
  border-radius: 20rpx;
  overflow: hidden;
  background-color: var(--bg-grey);
}

.publish__upload-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.publish__upload-cover {
  position: absolute;
  left: 12rpx;
  bottom: 12rpx;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background-color: rgba(0, 0, 0, 0.55);
  color: var(--text-white);
  font-size: 20rpx;
}

.publish__upload-remove {
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

.publish__upload-remove-text {
  color: var(--text-white);
  font-size: 24rpx;
  line-height: 1;
}

.publish__upload-add {
  border: 2rpx dashed var(--border-color);
  background-color: var(--bg-white);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  text-align: center;
}

.publish__upload-plus {
  font-size: 48rpx;
  color: var(--text-placeholder);
  line-height: 1;
}

.publish__upload-hint {
  font-size: 22rpx;
  color: var(--text-placeholder);
  text-align: center;
  width: 100%;
}

.publish__upload-tip {
  margin-top: var(--spacing-sm);
  font-size: 22rpx;
  color: var(--text-secondary);
}

.publish__field {
  display: flex;
  align-items: center;
  background-color: var(--bg-white);
  border: 2rpx solid var(--border-light);
  border-radius: 20rpx;
  padding: 0 var(--spacing-md);
  height: 90rpx;
  margin-bottom: var(--spacing-md);
}

.publish__field--half {
  flex: 1;
  margin-bottom: 0;
}

.publish__row {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.publish__price-prefix {
  font-size: 28rpx;
  color: var(--text-secondary);
  margin-right: 8rpx;
}

.publish__input {
  flex: 1;
  font-size: 28rpx;
  color: var(--text-primary);
}

.publish__input--price {
  text-align: left;
}

.publish__placeholder {
  color: var(--text-placeholder);
}

.publish__field--select {
  justify-content: space-between;
}

.publish__select-text {
  font-size: 28rpx;
  color: var(--text-primary);
}

.publish__select-icon {
  font-size: 32rpx;
  color: var(--text-secondary);
}

.publish__field--textarea {
  flex-direction: column;
  align-items: flex-start;
  height: 260rpx;
  padding: var(--spacing-md);
}

.publish__textarea {
  width: 100%;
  flex: 1;
  font-size: 28rpx;
  color: var(--text-primary);
}

.publish__counter {
  align-self: flex-end;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.publish__meeting {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.publish__meeting-tabs {
  display: flex;
  gap: var(--spacing-sm);
}

.publish__meeting-tab {
  flex: 1;
  text-align: center;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 999rpx;
  background-color: var(--bg-grey);
  color: var(--text-secondary);
  font-size: 24rpx;
}

.publish__meeting-tab.is-active {
  background-color: var(--primary-bg);
  color: var(--primary-color);
  font-weight: 600;
}

.publish__footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  border-top: 2rpx solid var(--border-light);
}

.publish__footer-btn {
  height: 88rpx;
  border-radius: 999rpx;
  background-color: var(--primary-color);
  color: var(--text-white);
  font-size: var(--font-md);
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.publish__footer-btn.is-disabled {
  background-color: var(--border-light);
  color: var(--text-placeholder);
}
</style>
