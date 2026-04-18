<template>
  <view class="page">
    <view class="page__header">
      <text class="page__title">轻院二手用户协议</text>
      <text class="page__subtitle">更新日期：2025-02-05</text>
    </view>
    <view class="page__card">
      <view class="section">
        <text class="section__title">1. 总则</text>
        <text class="section__text">
          本协议用于说明平台服务内容、双方权利义务及使用规则。用户注册或使用即视为已阅读并同意本协议。
        </text>
      </view>
      <view class="section">
        <text class="section__title">2. 用户注册与账号管理</text>
        <text class="section__text">
          用户需提供真实、准确的注册信息，并妥善保管账号。因账号保管不善造成的损失由用户自行承担。
        </text>
      </view>
      <view class="section">
        <text class="section__title">3. 用户行为规范</text>
        <text class="section__text">
          用户不得发布违法违规内容，不得进行骚扰、欺诈或侵害他人权益的行为。平台有权对违规行为进行处理。
        </text>
      </view>
      <view class="section">
        <text class="section__title">4. 商品发布规范</text>
        <text class="section__text">
          商品信息需真实完整，图片清晰且与商品一致。禁止发布违禁品、虚假信息或侵权内容。
        </text>
      </view>
      <view class="section">
        <text class="section__title">5. 交易规则</text>
        <text class="section__text">
          交易以双方协商为准。用户需遵守平台交易流程，合理约定面交时间与地点，并确保交易安全。
        </text>
      </view>
      <view class="section">
        <text class="section__title">6. 违规处理</text>
        <text class="section__text">
          平台可根据违规程度采取警告、限权、封禁等措施，并保留追究责任的权利。
        </text>
      </view>
      <view class="section">
        <text class="section__title">7. 免责声明</text>
        <text class="section__text">
          平台仅提供信息发布与交易撮合服务，对用户发布内容及交易结果不承担担保责任。
        </text>
      </view>
      <view class="section">
        <text class="section__title">8. 其他条款</text>
        <text class="section__text">
          平台有权在必要时更新本协议内容，并通过合理方式告知用户。更新后继续使用即视为接受。
        </text>
      </view>
    </view>
  </view>
</template>

<script setup></script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--spacing-md);
  box-sizing: border-box;
}

.page__header {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: var(--spacing-md);
}

.page__title {
  font-size: var(--font-lg);
  color: var(--text-primary);
  font-weight: 600;
}

.page__subtitle {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.page__card {
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.06);
}

.section {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  padding-bottom: var(--spacing-md);
}

.section:last-child {
  padding-bottom: 0;
}

.section__title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
}

.section__text {
  font-size: var(--font-sm);
  color: var(--text-regular);
  line-height: 1.7;
}
</style>
