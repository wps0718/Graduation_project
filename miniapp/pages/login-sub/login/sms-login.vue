<template>
  <view class="sms safe-area-bottom">
    <view :style="{ height: `${statusBarHeight}px` }"></view>

    <view class="sms__header" :style="{ height: `${navBarHeight}px` }">
      <view class="sms__back" @click="goBack">
        <text class="sms__back-text">‹</text>
      </view>
      <image class="sms__badge" src="/static/pic/校徽.png" mode="aspectFit" />
      <view class="sms__header-text">
        <text class="sms__header-title">校园二手交易</text>
        <text class="sms__header-sub">让闲置物品流转起来</text>
      </view>
    </view>

    <view class="sms__card">
      <view class="sms__hero">
        <view class="sms__hero-bg">
          <image class="sms__hero-img" src="/static/pic/图标.png" mode="aspectFit" />
        </view>
      </view>

    <view class="sms__brand-wrap">
      <text class="sms__brand">轻院二手</text>
    </view>

      <view class="sms__section">
        <text class="sms__section-title">短信验证</text>

        <view class="sms__field">
          <image class="sms__field-icon" src="/static/svg/phone.svg" mode="aspectFit" />
          <input
            class="sms__input"
            :value="phone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="sms__placeholder"
            @input="onPhoneInput"
          />
        </view>

        <view class="sms__field sms__field--code">
          <image class="sms__field-icon" src="/static/svg/verify-code.svg" mode="aspectFit" />
          <input
            class="sms__input sms__input--code"
            :value="code"
            type="number"
            maxlength="6"
            placeholder="请输入验证码"
            placeholder-class="sms__placeholder"
            @input="onCodeInput"
          />
          <view class="sms__code-btn" :class="{ 'is-disabled': countdown > 0 || !canSendCode }" @click="onSendCode">
            <text class="sms__code-text">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</text>
          </view>
        </view>

        <view class="sms__submit" :class="{ 'is-disabled': !canSubmit }" @click="onSmsLogin">
          <text class="sms__submit-text">登录</text>
        </view>

        <text class="sms__hint">未注册的手机号验证通过后会自动创建账号</text>
      </view>

      <view class="sms__other">
        <view class="sms__other-item" @click="onWeChatLogin">
          <view class="sms__other-icon-wrap">
            <image class="sms__other-icon" src="/static/svg/wechat.svg" mode="aspectFit" />
          </view>
          <text class="sms__other-text">微信登录</text>
        </view>
        <view class="sms__other-item" @click="goAccountLogin">
          <view class="sms__other-icon-wrap">
            <image class="sms__other-icon" src="/static/svg/password.svg" mode="aspectFit" />
          </view>
          <text class="sms__other-text">账号密码登录</text>
        </view>
      </view>

      <view class="sms__agreement">
        <checkbox-group @change="onAgreeChange">
          <label class="sms__agree-row">
            <checkbox value="agree" :checked="agreed" color="var(--login-primary)" />
            <text class="sms__agree-text">登录即代表同意</text>
            <text class="sms__agree-link" @click.stop="goAgreement">《用户协议》</text>
            <text class="sms__agree-text">和</text>
            <text class="sms__agree-link" @click.stop="goPrivacy">《隐私政策》</text>
          </label>
        </checkbox-group>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { post } from '@/utils/request'
import { useUserStore } from '@/store'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const navBarHeight = ref(44)

const phone = ref('')
const code = ref('')

const countdown = ref(0)
let countdownTimer = null

const agreed = ref(false)

function sanitizeDigits(value, maxLen) {
  const digits = String(value || '').replace(/\D/g, '')
  return digits.slice(0, maxLen)
}

function showToast(title) {
  uni.showToast({ title, icon: 'none' })
}

function isValidPhone(value) {
  return /^\d{11}$/.test(value)
}

const canSendCode = computed(() => isValidPhone(phone.value))
const canSubmit = computed(() => isValidPhone(phone.value) && /^\d{6}$/.test(code.value))

function onPhoneInput(e) {
  phone.value = sanitizeDigits(e.detail.value, 11)
}

function onCodeInput(e) {
  code.value = sanitizeDigits(e.detail.value, 6)
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

function startCountdown() {
  stopCountdown()
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      stopCountdown()
    }
  }, 1000)
}

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  countdown.value = 0
}

async function onSendCode() {
  if (countdown.value > 0) {
    return
  }
  if (!canSendCode.value) {
    showToast('请输入正确的手机号')
    return
  }
  await post('/mini/user/sms/send', { phone: phone.value }, { showLoading: true })
  startCountdown()
}

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

async function onSmsLogin() {
  if (!ensureAgreed()) {
    return
  }
  if (!canSubmit.value) {
    showToast('请输入正确的手机号和验证码')
    return
  }
  const data = await post('/mini/user/sms-login', { phone: phone.value, code: code.value }, { showLoading: true })
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
  const wxCode = loginRes && loginRes.code
  if (!wxCode) {
    showToast('微信登录失败，请重试')
    return
  }
  const data = await post('/mini/user/wx-login', { code: wxCode }, { showLoading: true })
  await finishLogin(data)
}

function goBack() {
  uni.navigateBack()
}

function goAccountLogin() {
  uni.redirectTo({ url: '/pages/login-sub/login/login' })
}

function goAgreement() {
  uni.navigateTo({ url: '/pages/login-sub/agreement/agreement' })
}

function goPrivacy() {
  uni.navigateTo({ url: '/pages/login-sub/privacy/privacy' })
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

onBeforeUnmount(() => {
  stopCountdown()
})
</script>

<style lang="scss" scoped>
.sms {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: 0 var(--spacing-md) var(--spacing-lg);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.sms__header {
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-top: 14rpx;
}

.sms__back {
  position: absolute;
  left: 0;
  height: 96rpx;
  width: 96rpx;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.sms__back-text {
  font-size: 50rpx;
  line-height: 1;
  color: var(--login-text-strong);
}

.sms__badge {
  width: 95rpx;
  height: 95rpx;
  position: absolute;
  left: 75rpx;
}

.sms__header-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.sms__header-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--login-text-strong);
}

.sms__header-sub {
  margin-top: 6rpx;
  font-size: 28rpx;
  color: var(--login-text-muted);
}

.sms__card {
  background-color: var(--bg-white);
  border-radius: 24rpx;
  padding: 44rpx 40rpx 32rpx;
  width: 100%;
  max-width: 580rpx;
  margin: 20rpx auto;
}

.sms__hero {
  display: flex;
  justify-content: center;
}

.sms__hero-bg {
  width: 203rpx;
  height: 203rpx;
  border-radius: 20rpx;
  background-color: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sms__hero-img {
  width: 200rpx;
  height: 200rpx;
}

.sms__brand-wrap {
  text-align: center;
   margin-top: 20rpx;
}

.sms__brand {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--login-text-strong);
}

.sms__section {
  margin-top: 26rpx;
}

.sms__section-title {
  font-size: 22rpx;
  font-weight: 700;
  color: var(--login-text-muted);
  letter-spacing: 1rpx;
}

.sms__field {
  height: 94rpx;
  border-radius: 24rpx;
  border: 2rpx solid var(--login-border);
  display: flex;
  align-items: center;
  padding: 0 26rpx;
  margin-top: 18rpx;
}

.sms__field--code {
  padding-right: 12rpx;
}

.sms__field-icon {
  width: 34rpx;
  height: 34rpx;
  margin-right: 16rpx;
}

.sms__input {
  flex: 1;
  font-size: 28rpx;
  color: var(--login-text-strong);
}

.sms__input--code {
  padding-right: 12rpx;
}

.sms__placeholder {
  color: var(--login-text-placeholder);
}

.sms__code-btn {
  height: 66rpx;
  padding: 0 20rpx;
  border-radius: 18rpx;
  background-color: var(--login-muted-bg);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sms__code-btn.is-disabled {
  opacity: 0.6;
}

.sms__code-text {
  font-size: 24rpx;
  color: var(--login-primary);
  font-weight: 500;
}

.sms__submit {
  height: 94rpx;
  border-radius: 24rpx;
  background-color: var(--login-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 24rpx;
}

.sms__submit.is-disabled {
  background-color: var(--login-muted-bg);
}

.sms__submit-text {
  color: var(--text-white);
  font-size: 30rpx;
  font-weight: 600;
}

.sms__submit.is-disabled .sms__submit-text {
  color: var(--login-text-placeholder);
}

.sms__hint {
  margin-top: 16rpx;
  font-size: 22rpx;
  color: var(--login-text-hint);
  text-align: center;
}

.sms__other {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 34rpx;
}

.sms__other-item {
  width: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.sms__other-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 48rpx;
  background-color: var(--login-surface);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sms__other-icon {
  width: 44rpx;
  height: 44rpx;
}

.sms__other-text {
  margin-top: 14rpx;
  font-size: 24rpx;
  color: var(--login-text-muted);
}

.sms__agreement {
  margin-top: 26rpx;
}

.sms__agree-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.sms__agree-text {
  font-size: 22rpx;
  color: var(--login-text-muted);
  margin-left: 8rpx;
}

.sms__agree-link {
  font-size: 22rpx;
  color: var(--login-primary);
  margin-left: 8rpx;
}
</style>
