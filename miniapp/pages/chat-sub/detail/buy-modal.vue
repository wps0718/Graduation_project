<template>
  <view v-if="visible" class="modal-mask" @click="$emit('close')">
    <view class="modal-content" @click.stop>
      <view class="modal-header">
        <text class="modal-title">确认购买</text>
        <text class="modal-close" @click="$emit('close')">✕</text>
      </view>

      <view class="modal-body">
        <view class="modal-product">
          <image class="modal-product__image" :src="product.coverImage" mode="aspectFill" />
          <view class="modal-product__info">
            <text class="modal-product__title">{{ product.title }}</text>
            <text class="modal-product__price">原价：¥{{ product.price }}</text>
          </view>
        </view>

        <view class="modal-field">
          <text class="modal-field__label">成交价格</text>
          <view class="modal-field__input-wrap">
            <text class="modal-field__prefix">¥</text>
            <input
              class="modal-field__input"
              :value="form.price"
              type="digit"
              placeholder="请输入协商后的价格"
              placeholder-style="color: #ccc"
              @input="$emit('update:form', { ...form, price: $event.detail.value })"
            />
          </view>
        </view>

        <view class="modal-field">
          <text class="modal-field__label">面交地点</text>
          <picker
            :value="form.meetingPointIdx"
            :range="meetingPointNames"
            @change="$emit('meeting-point-change', $event)"
          >
            <view class="modal-field__picker">
              <text :class="{ 'is-placeholder': !form.meetingPointText }">
                {{ form.meetingPointText || '请选择面交地点' }}
              </text>
              <text class="modal-field__picker-arrow">▼</text>
            </view>
          </picker>
        </view>

        <view class="modal-field">
          <text class="modal-field__label">备注说明</text>
          <textarea
            class="modal-field__textarea"
            :value="form.remark"
            placeholder="如约定的面交时间等"
            placeholder-style="color: #ccc"
            :maxlength="200"
            @input="$emit('update:form', { ...form, remark: $event.detail.value })"
          />
        </view>
      </view>

      <view class="modal-footer">
        <view class="modal-btn modal-btn--cancel" @click="$emit('close')">
          <text>取消</text>
        </view>
        <view class="modal-btn modal-btn--confirm" :class="{ 'is-loading': submitting }" @click="$emit('submit')">
          <text>{{ submitting ? '提交中...' : '确认购买' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  product: Object,
  meetingPoints: { type: Array, default: () => [] },
  form: { type: Object, default: () => ({ price: '', meetingPointIdx: -1, meetingPointText: '', remark: '' }) },
  submitting: Boolean
})

defineEmits(['close', 'submit', 'meeting-point-change', 'update:form'])

const meetingPointNames = computed(() => props.meetingPoints.map(m => m.name))
</script>

<style lang="scss" scoped>
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 620rpx;
  max-height: 80vh;
  background-color: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  border-bottom: 1rpx solid var(--border-light);
}

.modal-title {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.modal-close {
  font-size: 32rpx;
  color: var(--text-secondary);
  padding: 4rpx 12rpx;
}

.modal-body {
  padding: var(--spacing-md);
  overflow-y: auto;
  flex: 1;
}

.modal-product {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  background-color: var(--bg-grey);
  border-radius: var(--radius-sm);
  margin-bottom: var(--spacing-md);
}

.modal-product__image {
  width: 80rpx;
  height: 80rpx;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.modal-product__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.modal-product__title {
  font-size: var(--font-sm);
  color: var(--text-primary);
  font-weight: 500;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.modal-product__price {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.modal-field {
  margin-bottom: var(--spacing-md);
}

.modal-field__label {
  font-size: var(--font-sm);
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 8rpx;
  display: block;
}

.modal-field__input-wrap {
  display: flex;
  align-items: center;
  border: 1rpx solid var(--border-light);
  border-radius: var(--radius-sm);
  padding: 0 var(--spacing-sm);
}

.modal-field__prefix {
  font-size: var(--font-md);
  color: var(--primary-color);
  font-weight: 600;
  margin-right: 4rpx;
}

.modal-field__input {
  flex: 1;
  height: 80rpx;
  font-size: var(--font-md);
  color: var(--text-primary);
}

.modal-field__picker {
  height: 80rpx;
  border: 1rpx solid var(--border-light);
  border-radius: var(--radius-sm);
  padding: 0 var(--spacing-sm);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--font-sm);
  color: var(--text-primary);
}

.modal-field__picker .is-placeholder {
  color: #ccc;
}

.modal-field__picker-arrow {
  font-size: 20rpx;
  color: var(--text-secondary);
}

.modal-field__textarea {
  width: 100%;
  height: 120rpx;
  border: 1rpx solid var(--border-light);
  border-radius: var(--radius-sm);
  padding: var(--spacing-sm);
  font-size: var(--font-sm);
  color: var(--text-primary);
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  border-top: 1rpx solid var(--border-light);
}

.modal-btn {
  flex: 1;
  height: 80rpx;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-md);
  font-weight: 600;
}

.modal-btn--cancel {
  background-color: var(--bg-grey);
  color: var(--text-regular);
}

.modal-btn--confirm {
  background-color: var(--primary-color);
  color: #fff;
}

.modal-btn--confirm.is-loading {
  opacity: 0.6;
}
</style>
