<template>
  <view class="page">
    <view class="page__header">
      <text class="page__title">轻院二手隐私政策</text>
      <text class="page__subtitle">更新日期：2025-02-05</text>
    </view>
    <view class="page__card">
      <view class="section">
        <text class="section__title">1. 我们收集的信息</text>
        <text class="section__text">
          为提供服务所需，我们可能收集账号信息、设备信息、交易信息以及用户主动提交的内容。
        </text>
      </view>
      <view class="section">
        <text class="section__title">2. 信息的使用方式</text>
        <text class="section__text">
          我们会用于账号登录、交易撮合、客服支持与安全风控等目的，确保服务稳定与体验提升。
        </text>
      </view>
      <view class="section">
        <text class="section__title">3. 信息的共享</text>
        <text class="section__text">
          未经用户授权不会向第三方共享个人信息，法律法规另有规定的除外。
        </text>
      </view>
      <view class="section">
        <text class="section__title">4. 信息的存储与安全</text>
        <text class="section__text">
          我们采取合理的安全措施保护信息安全，并在必要时按规定期限存储。
        </text>
      </view>
      <view class="section">
        <text class="section__title">5. 您的权利</text>
        <text class="section__text">
          您有权访问、更正或删除个人信息，并可申请注销账号或撤回授权。
        </text>
      </view>
      <view class="section">
        <text class="section__title">6. 未成年人保护</text>
        <text class="section__text">
          未成年人使用平台需在监护人指导下进行，我们将采取合理措施保护未成年人权益。
        </text>
      </view>
      <view class="section">
        <text class="section__title">7. 隐私政策的更新</text>
        <text class="section__text">
          隐私政策更新后将以适当方式提示，继续使用即表示同意更新内容。
        </text>
      </view>
      <view class="section">
        <text class="section__title">8. 联系我们</text>
        <text class="section__text">
          如对隐私政策有疑问，可通过客服邮箱或平台反馈渠道联系我们。
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
