<template>
  <view class="edit-page">
    <view class="edit-card">
      <view class="edit-item" @click="changeAvatar">
        <text class="edit-label">头像</text>
        <view class="edit-right">
          <image class="edit-avatar" :src="form.avatarUrl || '/static/pic/校徽.png'" mode="aspectFill" />
          <text class="edit-arrow">›</text>
        </view>
      </view>
      <view class="edit-divider"></view>
      <view class="edit-item">
        <text class="edit-label">昵称</text>
        <input
          class="edit-input"
          :value="form.nickName"
          placeholder="请输入昵称"
          placeholder-class="edit-placeholder"
          @input="onNameInput"
        />
      </view>
      <view class="edit-divider"></view>
      <picker mode="selector" :range="genderOptions" :value="genderIndex" @change="onGenderChange">
        <view class="edit-item">
          <text class="edit-label">性别</text>
          <view class="edit-right">
            <text class="edit-value">{{ genderLabel }}</text>
            <text class="edit-arrow">›</text>
          </view>
        </view>
      </picker>
      <view class="edit-divider"></view>
      <view class="edit-item">
        <text class="edit-label">手机号</text>
        <text class="edit-value edit-value--muted">{{ form.phone || '未绑定' }}</text>
      </view>
      <view class="edit-divider"></view>
      <view class="edit-item edit-item--bio">
        <text class="edit-label">个人简介</text>
        <textarea
          class="edit-textarea"
          :value="form.bio"
          placeholder="简单介绍一下自己吧"
          placeholder-class="edit-placeholder"
          maxlength="200"
          @input="onBioInput"
        />
      </view>
    </view>

    <view class="edit-footer safe-area-bottom">
      <view class="edit-save" :class="{ 'is-disabled': !canSave }" @click="saveProfile">保存</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get, post, uploadFile } from '@/utils/request'
import { resolveImageUrl } from '@/utils/image'
import { useUserStore } from '@/store'

const userStore = useUserStore()

const form = ref({
  avatarUrl: '',
  nickName: '',
  gender: 0,
  phone: '',
  bio: ''
})

const genderOptions = ['男', '女', '保密']

const genderIndex = computed(() => {
  if (form.value.gender === 1) return 0
  if (form.value.gender === 2) return 1
  return 2
})

const genderLabel = computed(() => genderOptions[genderIndex.value] || '保密')

const canSave = computed(() => {
  return !!form.value.nickName
})

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

function onNameInput(e) {
  form.value.nickName = String((e && e.detail && e.detail.value) || '').trim()
}

function onGenderChange(e) {
  const index = Number((e && e.detail && e.detail.value) || 0)
  if (index === 0) {
    form.value.gender = 1
  } else if (index === 1) {
    form.value.gender = 2
  } else {
    form.value.gender = 0
  }
}

function onBioInput(e) {
  form.value.bio = String((e && e.detail && e.detail.value) || '').trim()
}

async function loadProfile() {
  if (!ensureLogin()) return
  try {
    const data = await get('/mini/user/info', {}, { showLoading: false })
    form.value = {
      avatarUrl: data.avatarUrl || '',
      nickName: data.nickName || '',
      gender: typeof data.gender === 'number' ? data.gender : 0,
      phone: data.phone || '',
      bio: data.bio || ''
    }
    userStore.setUserInfo(data)
  } catch (error) {
    showToast('加载失败，请稍后重试')
  }
}

function chooseImage() {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => resolve(res),
      fail: (err) => reject(err)
    })
  })
}

async function changeAvatar() {
  if (!ensureLogin()) return
  try {
    const res = await chooseImage()
    const path = res && res.tempFilePaths && res.tempFilePaths[0]
    if (!path) return
    form.value.avatarUrl = path
    const data = await uploadFile('/common/upload', path, { showLoading: true, formData: { type: 'avatar' } })
    if (data && data.url) {
      form.value.avatarUrl = resolveImageUrl(data.url)
    }
  } catch (error) {
    showToast('头像更新失败')
  }
}

async function saveProfile() {
  if (!ensureLogin()) return
  if (!form.value.nickName) {
    showToast('请输入昵称')
    return
  }
  const payload = {
    avatarUrl: form.value.avatarUrl,
    nickName: form.value.nickName,
    gender: form.value.gender,
    bio: form.value.bio
  }
  try {
    await post('/mini/user/update', payload, { showLoading: true })
    const next = {
      ...(userStore.userInfo || {}),
      ...payload,
      phone: form.value.phone
    }
    userStore.setUserInfo(next)
    showToast('保存成功')
  } catch (error) {
    showToast('保存失败，请稍后重试')
  }
}

onShow(() => {
  loadProfile()
})
</script>

<style lang="scss" scoped>
.edit-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.edit-card {
  background-color: var(--bg-white);
  border-radius: 24rpx;
  padding: 6rpx 0;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}

.edit-item {
  padding: 22rpx var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.edit-label {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.edit-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.edit-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background-color: var(--bg-grey);
}

.edit-input {
  flex: 1;
  text-align: right;
  font-size: var(--font-md);
  color: var(--text-primary);
}

.edit-placeholder {
  color: var(--text-placeholder);
}

.edit-value {
  font-size: var(--font-md);
  color: var(--text-primary);
}

.edit-value--muted {
  color: var(--text-secondary);
}

.edit-arrow {
  font-size: 34rpx;
  color: var(--text-secondary);
}

.edit-divider {
  height: 1rpx;
  background-color: var(--border-light);
  margin: 0 var(--spacing-md);
}

.edit-item--bio {
  align-items: flex-start;
  gap: var(--spacing-sm);
}

.edit-textarea {
  flex: 1;
  min-height: 160rpx;
  text-align: right;
  font-size: var(--font-md);
  color: var(--text-primary);
  line-height: 1.5;
}

.edit-footer {
  margin-top: auto;
}

.edit-save {
  width: 100%;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 999rpx;
  background-color: var(--primary-color);
  color: var(--text-white);
  font-size: var(--font-md);
}

.edit-save.is-disabled {
  opacity: 0.5;
}
</style>
