<template>
  <view class="page">
    <view class="page__header">
      <text class="page__title">客服与帮助</text>
      <text class="page__subtitle">常见问题与客服入口</text>
    </view>
    <view class="page__card">
      <view class="faq">
        <view v-for="(item, index) in faqList" :key="item.title" class="faq-item">
          <view class="faq-item__header" @click="toggleFaq(index)">
            <text class="faq-item__title">{{ item.title }}</text>
            <text class="faq-item__icon">{{ openIndex === index ? '－' : '＋' }}</text>
          </view>
          <view v-if="openIndex === index" class="faq-item__body">
            <text class="faq-item__text">{{ item.content }}</text>
          </view>
        </view>
      </view>
    </view>
    <view class="contact-card">
      <text class="contact-card__title">联系客服</text>
      <text class="contact-card__text">客服邮箱：service@qingyuan-secondhand.com</text>
      <text class="contact-card__text">客服微信：QY-Secondhand</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const openIndex = ref(-1)
const faqList = [
  {
    title: '如何发布商品？',
    content: '进入“发布”页填写商品信息并提交，审核通过后即可展示。'
  },
  {
    title: '如何购买商品？',
    content: '在商品详情点击“确认购买”，与卖家沟通面交时间地点。'
  },
  {
    title: '如何进行校园认证？',
    content: '在“个人中心-校园认证”提交证件信息，审核通过后展示认证标识。'
  },
  {
    title: '商品审核需要多久？',
    content: '一般在24小时内完成审核，遇高峰期可能略有延迟。'
  },
  {
    title: '如何取消交易？',
    content: '在交易记录中选择订单并取消，双方确认后交易关闭。'
  },
  {
    title: '发现违规商品怎么举报？',
    content: '进入商品详情或聊天页“更多”选择举报并提交原因。'
  },
  {
    title: '如何修改个人信息？',
    content: '在“设置-编辑资料”页面修改头像、昵称等信息。'
  },
  {
    title: '如何注销账号？',
    content: '在“设置”页面发起注销申请，按提示完成确认。'
  }
]

function toggleFaq(index) {
  openIndex.value = openIndex.value === index ? -1 : index
}
</script>

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

.faq {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.faq-item {
  padding: var(--spacing-sm);
  border-radius: var(--radius-md);
  background-color: var(--bg-grey);
}

.faq-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-sm);
}

.faq-item__title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
  flex: 1;
}

.faq-item__icon {
  font-size: var(--font-md);
  color: var(--text-secondary);
}

.faq-item__body {
  margin-top: 12rpx;
}

.faq-item__text {
  font-size: var(--font-sm);
  color: var(--text-regular);
  line-height: 1.6;
}

.contact-card {
  margin-top: var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.contact-card__title {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 600;
}

.contact-card__text {
  font-size: var(--font-sm);
  color: var(--text-regular);
}
</style>
