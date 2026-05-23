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

      <view class="login__divider">
        <view class="login__divider-line"></view>
      </view>

      <view class="login__section">
        <text class="login__section-title">账号登录</text>

        <view class="login__field" :class="{ 'is-focus': focusField === 'phone' }">
          <image class="login__field-icon" src="/static/svg/phone.svg" mode="aspectFit" />
          <input
            class="login__input"
            :value="accountPhone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="login__placeholder"
            @input="onAccountPhoneInput"
            @focus="focusField = 'phone'"
            @blur="focusField = ''"
          />
        </view>

        <view class="login__field" :class="{ 'is-focus': focusField === 'password' }">
          <image class="login__field-icon" src="/static/svg/password.svg" mode="aspectFit" />
          <input
            class="login__input"
            :value="password"
            :password="!showPassword"
            placeholder="请输入密码"
            placeholder-class="login__placeholder"
            @input="onPasswordInput"
            @focus="focusField = 'password'"
            @blur="focusField = ''"
          />
          <view class="login__eye" @click="showPassword = !showPassword">
            <text class="login__eye-icon">{{ showPassword ? '' : '' }}</text>
          </view>
        </view>

        <view class="login__submit" :class="{ 'is-disabled': !canAccountSubmit }" @click="onAccountLogin">
          <text class="login__submit-text">登录</text>
        </view>

        <text class="login__hint">未注册的手机号验证通过后会自动创建账号</text>
      </view>

      <view class="login__or">
        <view class="login__or-line"></view>
        <text class="login__or-text">或</text>
        <view class="login__or-line"></view>
      </view>

      <view class="login__other">
        <view class="login__other-card" @click="onWeChatLogin">
          <image class="login__other-icon" src="/static/svg/wechat.svg" mode="aspectFit" />
          <text class="login__other-text">微信登录</text>
        </view>
        <view class="login__other-card" @click="goSmsLogin">
          <image class="login__other-icon" src="/static/svg/sms-login.svg" mode="aspectFit" />
          <text class="login__other-text">短信登录</text>
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
import { ref, computed } from 'vue'
import { post } from '@/utils/request'
import { useUserStore } from '@/store'
import { showToast } from '@/utils/nav'
import { useNavBar } from '@/utils/useNavBar'

const userStore = useUserStore()

const { statusBarHeight, navBarHeight } = useNavBar()

const accountPhone = ref('')
const password = ref('')
const showPassword = ref(false)
const focusField = ref('')

const agreed = ref(false)

function sanitizeDigits(value, maxLen) {
  const digits = String(value || '').replace(/\D/g, '')
  return digits.slice(0, maxLen)
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

</script>

<style lang="scss" scoped>
/* 1. 页面渐变背景 */
.login {
  min-height: 100vh;
  background: linear-gradient(180deg, #3B82F6 0%, #3B82F6 12%, #f0f5ff 28%, #f5f7fa 100%);
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
  color: #fff;
}

.login__header-sub {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 4. 品牌卡片阴影加深 + 10. 入场动画 */
.login__card {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 48rpx 40rpx 36rpx;
  width: 100%;
  max-width: 580rpx;
  margin: 20rpx auto;
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.08);
  animation: slideUp 0.4s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 5. Logo 增加质感 */
.login__hero {
  display: flex;
  justify-content: center;
}

.login__hero-bg {
  width: 120rpx;
  height: 120rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 24rpx rgba(59, 130, 246, 0.25);
}

.login__hero-img {
  width: 88rpx;
  height: 88rpx;
}

.login__brand-wrap {
  text-align: center;
  margin-top: 16rpx;
  margin-bottom: 16rpx;
}

.login__brand {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--login-text-strong);
  letter-spacing: 2rpx;
}

/* 品牌与表单之间装饰分隔线 */
.login__divider {
  display: flex;
  justify-content: center;
  margin-bottom: 32rpx;
}

.login__divider-line {
  width: 48rpx;
  height: 4rpx;
  background: linear-gradient(90deg, #3B82F6, #93C5FD);
  border-radius: 2rpx;
}

.login__section {
  margin-top: 0;
}

.login__section-title {
  font-size: 26rpx;
  font-weight: 500;
  color: var(--login-text-placeholder);
  text-align: center;
  margin-bottom: 24rpx;
}

/* 2. 输入框圆角卡片式 + 聚焦态 */
.login__field {
  height: 94rpx;
  border-radius: 16rpx;
  background-color: #f5f5f5;
  border: 2rpx solid transparent;
  display: flex;
  align-items: center;
  padding: 0 28rpx;
  margin-bottom: 20rpx;
  transition: border-color 0.3s, background-color 0.3s;
}

.login__field.is-focus {
  border-color: #3B82F6;
  background-color: #fff;
}

.login__field-icon {
  width: 36rpx;
  height: 36rpx;
  margin-right: 18rpx;
}

.login__input {
  flex: 1;
  font-size: 30rpx;
  color: var(--login-text-strong);
}

.login__placeholder {
  color: var(--login-text-placeholder);
}

/* 9. 密码显示/隐藏 */
.login__eye {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 4rpx;
}

.login__eye-icon {
  font-size: 36rpx;
  color: #999;
}

/* 3. 登录按钮颜色强化 */
.login__submit {
  height: 88rpx;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8rpx;
  width: 100%;
  box-shadow: 0 6rpx 20rpx rgba(59, 130, 246, 0.35);
  transition: transform 0.15s, opacity 0.15s;
}

.login__submit:active {
  transform: scale(0.98);
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
}

.login__submit.is-disabled {
  background: #F1F5F9;
  box-shadow: none;
}

.login__submit.is-disabled:active {
  transform: none;
}

.login__submit-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.login__submit.is-disabled .login__submit-text {
  color: var(--login-text-placeholder);
}

.login__hint {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #999;
  text-align: center;
}

/* "或"分隔线 */
.login__or {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 24rpx 0;
}

.login__or-line {
  width: 80rpx;
  height: 1rpx;
  background-color: #e0e0e0;
}

.login__or-text {
  font-size: 24rpx;
  color: #bbb;
  margin: 0 24rpx;
}

/* 6. 社交登录：圆角卡片式 */
.login__other {
  display: flex;
  gap: 24rpx;
}

.login__other-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  background-color: #f5f5f5;
  border-radius: 16rpx;
  padding: 28rpx 0;
  transition: background-color 0.15s;
}

.login__other-card:active {
  background-color: #e8e8e8;
}

.login__other-icon {
  width: 40rpx;
  height: 40rpx;
}

.login__other-text {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

/* 8. 协议区域 */
.login__agreement {
  margin-top: 32rpx;
}

.login__agree-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  line-height: 1.6;
}

.login__agree-text {
  font-size: 24rpx;
  color: #999;
  margin-left: 8rpx;
}

.login__agree-link {
  font-size: 24rpx;
  color: #3B82F6;
  margin-left: 8rpx;
  text-decoration: underline;
}
</style>
