<template>
  <view class="auth-page">
    <view class="auth-header">
      <view class="auth-header__logo">
        <image src="/static/pic/校徽.png" mode="aspectFit" />
      </view>
      <view class="auth-header__text">
        <text class="auth-header__title">校园二手交易</text>
        <text class="auth-header__sub">让闲置物品流转起来</text>
      </view>
    </view>

    <view class="auth-hero">
      <image class="auth-hero__logo" src="/static/pic/图标.png" mode="aspectFit" />
      <text class="auth-hero__name">轻院二手</text>
    </view>

    <view v-if="showPending" class="auth-state">
      <text class="auth-state__title">认证审核中</text>
      <text class="auth-state__desc">认证审核中，请耐心等待</text>
    </view>

    <view v-else-if="showVerified" class="auth-state">
      <text class="auth-state__title">已认证</text>
      <view class="auth-state__info">
        <view class="auth-state__row">
          <text class="auth-state__label">学院</text>
          <text class="auth-state__value">{{ savedInfo.collegeName || '-' }}</text>
        </view>
        <view class="auth-state__row">
          <text class="auth-state__label">学号</text>
          <text class="auth-state__value">{{ savedInfo.studentId || '-' }}</text>
        </view>
        <view class="auth-state__row">
          <text class="auth-state__label">班级</text>
          <text class="auth-state__value">{{ savedInfo.className || '-' }}</text>
        </view>
      </view>
    </view>

    <view v-else class="auth-card">
      <text class="auth-card__title">{{ formTitle }}</text>
      <view v-if="rejectRemark" class="auth-reject">
        <text class="auth-reject__text">驳回原因：{{ rejectRemark }}</text>
      </view>
      <view class="auth-field">
        <text class="auth-field__label">学院</text>
        <picker mode="selector" range-key="name" :range="colleges" :value="collegeIndex" @change="onCollegeChange">
          <view class="auth-field__picker">
            <text class="auth-field__value">{{ collegeName || '请选择学院' }}</text>
            <text class="auth-field__arrow">›</text>
          </view>
        </picker>
      </view>
      <view class="auth-field">
        <text class="auth-field__label">学号</text>
        <input
          class="auth-field__input"
          type="number"
          :value="form.studentId"
          placeholder="请输入学号"
          placeholder-class="auth-field__placeholder"
          @input="onStudentIdInput"
        />
      </view>
      <view class="auth-field">
        <text class="auth-field__label">班级</text>
        <input
          class="auth-field__input"
          :value="form.className"
          placeholder="请输入班级"
          placeholder-class="auth-field__placeholder"
          @input="onClassInput"
        />
      </view>
      <view class="auth-upload">
        <text class="auth-upload__label">请上传一卡通或者3.0截图</text>
        <view class="auth-upload__box" @click="chooseMaterial">
          <image v-if="form.materialUrl" :src="form.materialUrl" class="auth-upload__image" mode="aspectFill" />
          <view v-else class="auth-upload__placeholder">
            <text class="auth-upload__icon">↑</text>
            <text class="auth-upload__text">点击上传</text>
          </view>
        </view>
      </view>
      <view class="auth-submit" :class="{ 'is-disabled': !canSubmit }" @click="submitAuth">确定</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { get, post, uploadFile } from '@/utils/request'
import { resolveImageUrl } from '@/utils/image'
import { AUTH_STATUS } from '@/utils/constant'
import { useUserStore } from '@/store'

const userStore = useUserStore()

const colleges = ref([])
const authStatus = ref(AUTH_STATUS.NONE)
const rejectRemark = ref('')

const form = ref({
  collegeId: null,
  collegeName: '',
  studentId: '',
  className: '',
  materialUrl: ''
})

const savedInfo = ref({
  collegeName: '',
  studentId: '',
  className: ''
})

const collegeIndex = computed(() => {
  if (!form.value.collegeId) return 0
  const idx = colleges.value.findIndex((item) => item.id === form.value.collegeId)
  return idx === -1 ? 0 : idx
})

const collegeName = computed(() => form.value.collegeName)

const formTitle = computed(() => (userStore.isLogin ? '校园认证' : '登录并认证'))

const showPending = computed(() => authStatus.value === AUTH_STATUS.PENDING)
const showVerified = computed(() => authStatus.value === AUTH_STATUS.VERIFIED)

const canSubmit = computed(() => {
  return (
    !!form.value.collegeId &&
    !!form.value.studentId &&
    !!form.value.className &&
    !!form.value.materialUrl
  )
})

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

function onCollegeChange(e) {
  const index = Number((e && e.detail && e.detail.value) || 0)
  const target = colleges.value[index]
  if (!target) return
  form.value.collegeId = target.id
  form.value.collegeName = target.name
}

function onStudentIdInput(e) {
  const value = String((e && e.detail && e.detail.value) || '').replace(/\D/g, '')
  form.value.studentId = value
}

function onClassInput(e) {
  form.value.className = String((e && e.detail && e.detail.value) || '').trim()
}

async function loadColleges() {
  try {
    const data = await get('/mini/college/list', {}, { showLoading: false })
    colleges.value = Array.isArray(data) ? data : []
  } catch (error) {
    colleges.value = []
  }
}

async function loadAuthStatus() {
  if (!ensureLogin()) return
  try {
    const data = await get('/mini/auth/status', {}, { showLoading: true })
    authStatus.value = typeof data.status === 'number' ? data.status : AUTH_STATUS.NONE
    rejectRemark.value = data.remark || ''
  } catch (error) {
    authStatus.value = AUTH_STATUS.NONE
    rejectRemark.value = ''
  }
}

function validateImage(file) {
  if (!file) return false
  const sizeOk = file.size <= 5 * 1024 * 1024
  const path = file.path || ''
  const extOk = /\.(jpe?g|png)$/i.test(path)
  if (!sizeOk) {
    showToast('图片大小不能超过5MB')
    return false
  }
  if (!extOk) {
    showToast('仅支持JPG/PNG/JPEG')
    return false
  }
  return true
}

async function chooseMaterial() {
  if (!ensureLogin()) return
  try {
    const res = await new Promise((resolve, reject) => {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (data) => resolve(data),
        fail: (err) => reject(err)
      })
    })
    const file = res && res.tempFiles && res.tempFiles[0]
    if (!validateImage(file)) return
    form.value.materialUrl = file.path
    const data = await uploadFile('/common/upload', file.path, { showLoading: true })
    if (data && data.url) {
      form.value.materialUrl = resolveImageUrl(data.url)
    }
  } catch (error) {
    showToast('上传失败，请重试')
  }
}

async function submitAuth() {
  if (!ensureLogin()) return
  if (!form.value.collegeId) {
    showToast('请选择学院')
    return
  }
  if (!form.value.studentId) {
    showToast('请输入学号')
    return
  }
  if (!/^\d+$/.test(form.value.studentId)) {
    showToast('学号需为纯数字')
    return
  }
  if (!form.value.className) {
    showToast('请输入班级')
    return
  }
  if (!form.value.materialUrl) {
    showToast('请上传认证材料')
    return
  }
  try {
    await post('/mini/auth/submit', {
      collegeId: form.value.collegeId,
      studentId: form.value.studentId,
      className: form.value.className,
      materialUrl: form.value.materialUrl
    })
    savedInfo.value = {
      collegeName: form.value.collegeName,
      studentId: form.value.studentId,
      className: form.value.className
    }
    uni.setStorageSync('authInfo', savedInfo.value)
    authStatus.value = AUTH_STATUS.PENDING
    rejectRemark.value = ''
    showToast('提交成功，请等待审核')
  } catch (error) {
    showToast('提交失败，请稍后重试')
  }
}

function loadSavedInfo() {
  const info = uni.getStorageSync('authInfo')
  if (!info) return
  savedInfo.value = {
    collegeName: info.collegeName || '',
    studentId: info.studentId || '',
    className: info.className || ''
  }
}

onLoad(() => {
  loadColleges()
  loadSavedInfo()
})

onShow(() => {
  loadAuthStatus()
})
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-lg);
  box-sizing: border-box;
}

.auth-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.auth-header__logo {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  overflow: hidden;
  background-color: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-header__logo image {
  width: 100%;
  height: 100%;
}

.auth-header__text {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.auth-header__title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
}

.auth-header__sub {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.auth-hero {
  margin-top: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
}

.auth-hero__logo {
  width: 150rpx;
  height: 150rpx;
  border-radius: 24rpx;
  background-color: var(--bg-white);
}

.auth-hero__name {
  font-size: var(--font-lg);
  color: var(--text-primary);
  font-weight: 600;
}

.auth-card {
  margin-top: var(--spacing-lg);
  padding: var(--spacing-lg);
  background-color: var(--bg-white);
  border-radius: 24rpx;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}

.auth-card__title {
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.auth-reject {
  background-color: rgba(255, 77, 79, 0.08);
  color: var(--danger-color);
  padding: 12rpx 16rpx;
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-md);
}

.auth-reject__text {
  font-size: var(--font-sm);
}

.auth-field {
  margin-bottom: var(--spacing-md);
}

.auth-field__label {
  display: block;
  margin-bottom: 8rpx;
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

.auth-field__picker {
  height: 88rpx;
  padding: 0 var(--spacing-md);
  border-radius: var(--radius-lg);
  border: 1rpx solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--bg-white);
}

.auth-field__value {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.auth-field__arrow {
  font-size: 34rpx;
  color: var(--text-secondary);
}

.auth-field__input {
  height: 88rpx;
  padding: 0 var(--spacing-md);
  border-radius: var(--radius-lg);
  border: 1rpx solid var(--border-color);
  font-size: var(--font-md);
  color: var(--text-primary);
  background-color: var(--bg-white);
}

.auth-field__placeholder {
  color: var(--text-placeholder);
}

.auth-upload {
  margin-top: var(--spacing-md);
}

.auth-upload__label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.auth-upload__box {
  margin-top: var(--spacing-sm);
  height: 280rpx;
  border-radius: var(--radius-lg);
  border: 2rpx dashed var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-grey);
  overflow: hidden;
}

.auth-upload__image {
  width: 100%;
  height: 100%;
}

.auth-upload__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  color: var(--text-secondary);
}

.auth-upload__icon {
  font-size: 44rpx;
}

.auth-upload__text {
  font-size: var(--font-sm);
}

.auth-submit {
  margin-top: var(--spacing-lg);
  height: 88rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-md);
  color: var(--text-white);
  background-color: var(--primary-color);
}

.auth-submit.is-disabled {
  opacity: 0.5;
}

.auth-state {
  margin-top: var(--spacing-lg);
  padding: var(--spacing-lg);
  background-color: var(--bg-white);
  border-radius: 24rpx;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.auth-state__title {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.auth-state__desc {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.auth-state__info {
  margin-top: var(--spacing-sm);
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.auth-state__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.auth-state__label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.auth-state__value {
  font-size: var(--font-md);
  color: var(--text-primary);
}
</style>
