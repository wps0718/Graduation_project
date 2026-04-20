<template>
  <view class="login safe-area-bottom">
    <view :style="{ height: `${statusBarHeight}px` }"></view>

    <view class="login__header" :style="{ height: `${navBarHeight}px` }">
      <image class="login__badge" src="/static/pic/校徽.png" mode="aspectFit" />
      <view class="login__header-text">
        <text class="login__header-title">校园二手交易</text>
        <text class="login__header-sub">让闲置物品流转起来</text>
      </view>
    </view>

    <view class="login__card">
      <view class="login__hero">
        <view class="login__hero-bg">
          <image class="login__hero-img" src="/static/pic/图标.png" mode="aspectFit" />
        </view>
      </view>

      <view class="login__brand-wrap">
        <text class="login__brand">轻院二手</text>
      </view>

      <view class="login__section">
        <text class="login__section-title">账号登录</text>

        <view class="login__field">
          <image class="login__field-icon" src="/static/svg/phone.svg" mode="aspectFit" />
          <input
            class="login__input"
            :value="accountPhone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="login__placeholder"
            @input="onAccountPhoneInput"
          />
        </view>

        <view class="login__field">
          <image class="login__field-icon" src="/static/svg/password.svg" mode="aspectFit" />
          <input
            class="login__input"
            :value="password"
            password
            placeholder="请输入密码"
            placeholder-class="login__placeholder"
            @input="onPasswordInput"
          />
        </view>

        <view class="login__submit" :class="{ 'is-disabled': !canAccountSubmit }" @click="onAccountLogin">
          <text class="login__submit-text">登录</text>
        </view>

        <text class="login__hint">未注册的手机号验证通过后会自动创建账号</text>
      </view>

      <view class="login__other">
        <view class="login__other-item" @click="onWeChatLogin">
          <view class="login__other-icon-wrap">
            <image class="login__other-icon" src="/static/svg/wechat.svg" mode="aspectFit" />
          </view>
          <text class="login__other-text">微信登录</text>
        </view>
        <view class="login__other-item" @click="goSmsLogin">
          <view class="login__other-icon-wrap">
            <image class="login__other-icon" src="/static/svg/sms-login.svg" mode="aspectFit" />
          </view>
          <text class="login__other-text">短信验证码登录</text>
        </view>
      </view>

      <view class="login__agreement">
        <checkbox-group @change="onAgreeChange">
          <label class="login__agree-row">
            <checkbox value="agree" :checked="agreed" color="var(--login-primary)" />
            <text class="login__agree-text">登录即代表同意</text>
            <text class="login__agree-link" @click.stop="goAgreement">《用户协议》</text>
            <text class="login__agree-text">和</text>
            <text class="login__agree-link" @click.stop="goPrivacy">《隐私政策》</text>
          </label>
        </checkbox-group>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { post } from '@/utils/request'
import { useUserStore } from '@/store'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const navBarHeight = ref(44)

const accountPhone = ref('')
const password = ref('')

const agreed = ref(false)

function sanitizeDigits(value, maxLen) {
  const digits = String(value || '').replace(/\D/g, '')
  return digits.slice(0, maxLen)
}

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function isValidPhone(phone) {
  return /^\d{11}$/.test(phone)
}

function onAccountPhoneInput(e) {
  accountPhone.value = sanitizeDigits(e.detail.value, 11)
}

function onPasswordInput(e) {
  password.value = String((e && e.detail && e.detail.value) || '')
}

function onAgreeChange(e) {
  const values = (e && e.detail && e.detail.value) || []
  agreed.value = Array.isArray(values) && values.includes('agree')
}

function ensureAgreed() {
  if (!agreed.value) {
    showToast('请先同意用户协议和隐私政策')
    return false
  }
  return true
}

const canAccountSubmit = computed(() => {
  return isValidPhone(accountPhone.value) && !!password.value
})

async function finishLogin(data) {
  await userStore.login(data)
  if (data && data.agreementAccepted !== 1) {
    try {
      await post('/mini/user/accept-agreement', {}, { showLoading: false })
    } catch (error) {
    }
  }
  uni.switchTab({ url: '/pages/index/index' })
}

async function onAccountLogin() {
  if (!ensureAgreed()) {
    return
  }
  if (!canAccountSubmit.value) {
    showToast('请输入正确的手机号和密码')
    return
  }
  const data = await post(
    '/mini/user/login',
    { phone: accountPhone.value, password: password.value },
    { showLoading: true }
  )
  await finishLogin(data)
}

async function onWeChatLogin() {
  if (!ensureAgreed()) {
    return
  }
  const loginRes = await new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: resolve,
      fail: reject
    })
  })
  const code = loginRes && loginRes.code
  if (!code) {
    showToast('微信登录失败，请重试')
    return
  }
  const data = await post('/mini/user/wx-login', { code }, { showLoading: true })
  await finishLogin(data)
}

function goAgreement() {
  uni.navigateTo({ url: '/pages/login-sub/agreement/agreement' })
}

function goPrivacy() {
  uni.navigateTo({ url: '/pages/login-sub/privacy/privacy' })
}

function goSmsLogin() {
  uni.navigateTo({ url: '/pages/login-sub/login/sms-login' })
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
</script>

<style lang="scss" scoped>
.login {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: 5rpx var(--spacing-md) var(--spacing-lg);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.login__header {
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-top: 14rpx;
}

.login__badge {
  width: 95rpx;
  height: 95rpx;
  position: absolute;
  left: 75rpx;
}

.login__header-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login__header-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--login-text-strong);
}

.login__header-sub {
  margin-top: 6rpx;
  font-size: 28rpx;
  color: var(--login-text-muted);
}

.login__card {
  background-color: var(--bg-white);
  border-radius: 24rpx;
  padding: 44rpx 40rpx 32rpx;
  width: 100%;
  max-width: 580rpx;
  margin: 20rpx auto;
}

.login__brand-wrap {
  text-align: center;
  margin-top: 20rpx;
}

.login__hero {
  display: flex;
  justify-content: center;
}

.login__hero-bg {
  width: 203rpx;
  height: 203rpx;
  border-radius: 20rpx;
  background-color: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--spacing-md);
}

.login__hero-img {
  width: 200rpx;
  height: 200rpx;
}

.login__brand {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--login-text-strong);
}


.login__section {
  margin-top: 26rpx;
}

.login__section-title {
  font-size: 22rpx;
  font-weight: 700;
  color: var(--login-text-muted);
  letter-spacing: 1rpx;
}

.login__field {
  height: 94rpx;
  border-radius: 24rpx;
  border: 2rpx solid var(--login-border);
  display: flex;
  align-items: center;
  padding: 0 26rpx;
  margin-top: 18rpx;
}

.login__field-icon {
  width: 34rpx;
  height: 34rpx;
  margin-right: 16rpx;
}

.login__input {
  flex: 1;
  font-size: 28rpx;
  color: var(--login-text-strong);
}

.login__placeholder {
  color: var(--login-text-placeholder);
}

.login__submit {
  height: 94rpx;
  border-radius: 24rpx;
  background-color: var(--login-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 24rpx;
}

.login__submit.is-disabled {
  background-color: var(--login-muted-bg);
}

.login__submit-text {
  color: var(--text-white);
  font-size: 30rpx;
  font-weight: 600;
}

.login__submit.is-disabled .login__submit-text {
  color: var(--login-text-placeholder);
}

.login__hint {
  margin-top: 16rpx;
  font-size: 22rpx;
  color: var(--login-text-hint);
  text-align: center;
}

.login__other {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 34rpx;
}

.login__other-item {
  width: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login__other-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 48rpx;
  background-color: var(--login-surface);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login__other-icon {
  width: 44rpx;
  height: 44rpx;
}

.login__other-text {
  margin-top: 14rpx;
  font-size: 24rpx;
  color: var(--login-text-muted);
}

.login__agreement {
  margin-top: 26rpx;
}

.login__agree-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.login__agree-text {
  font-size: 22rpx;
  color: var(--login-text-muted);
  margin-left: 8rpx;
}

.login__agree-link {
  font-size: 22rpx;
  color: var(--login-primary);
  margin-left: 8rpx;
}
</style>
