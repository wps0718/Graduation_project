<template>
  <view class="evidence-upload">
    <view class="evidence-upload__images" v-if="type === 'image' || type === 'both'">
      <view
        v-for="(item, index) in imageList"
        :key="item.id"
        class="evidence-upload__img-item"
      >
        <image
          class="evidence-upload__img"
          :src="item.url"
          mode="aspectFill"
          @click="previewImage(index)"
        />
        <view class="evidence-upload__img-remove" @click.stop="removeImage(index)">
          <text class="evidence-upload__remove-text">×</text>
        </view>
        <input
          v-if="showDescription"
          class="evidence-upload__desc-input"
          :value="item.description"
          placeholder="图片说明（选填）"
          placeholder-class="evidence-upload__placeholder"
          maxlength="50"
          @input="onDescChange(index, $event)"
        />
      </view>
      <view
        v-if="imageList.length < maxImages"
        class="evidence-upload__add"
        @click="chooseImage"
      >
        <text class="evidence-upload__add-icon">+</text>
        <text class="evidence-upload__add-text">添加图片</text>
      </view>
    </view>

    <view v-if="type === 'text' || type === 'both'" class="evidence-upload__text-section">
      <view v-for="(item, index) in textList" :key="item.id" class="evidence-upload__text-item">
        <textarea
          class="evidence-upload__textarea"
          :value="item.content"
          placeholder="输入文字证据..."
          placeholder-class="evidence-upload__placeholder"
          maxlength="500"
          @input="onTextChange(index, $event)"
        />
        <view class="evidence-upload__text-remove" @click="removeText(index)">
          <text class="evidence-upload__text-remove-text">删除</text>
        </view>
      </view>
      <view v-if="textList.length < maxTexts" class="evidence-upload__add-text" @click="addText">
        <text class="evidence-upload__add-text-icon">+</text>
        <text>添加文字证据</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { uploadFile } from '@/utils/request'

const props = defineProps({
  type: { type: String, default: 'both' }, // 'image' | 'text' | 'both'
  maxImages: { type: Number, default: 6 },
  maxTexts: { type: Number, default: 3 },
  showDescription: { type: Boolean, default: false }
})

const emit = defineEmits(['change'])

const imageList = ref([])
const textList = ref([])

function emitChange() {
  emit('change', {
    images: imageList.value,
    texts: textList.value
  })
}

async function chooseImage() {
  const count = props.maxImages - imageList.value.length
  if (count <= 0) return
  const res = await new Promise((resolve) => {
    uni.chooseImage({
      count,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: resolve,
      fail: () => resolve(null)
    })
  })
  if (!res) return
  const files = res.tempFilePaths || []
  if (!files.length) return
  uni.showLoading({ title: '上传中', mask: true })
  for (const filePath of files) {
    try {
      const data = await uploadFile('/mini/common/upload', filePath, { showLoading: false, formData: { type: 'dispute' } })
      if (data && data.url) {
        imageList.value.push({ id: `${Date.now()}-${Math.random()}`, url: data.url, description: '' })
      }
    } catch (e) {
      // skip failed upload
    }
  }
  uni.hideLoading()
  emitChange()
}

function removeImage(index) {
  imageList.value.splice(index, 1)
  emitChange()
}

function previewImage(index) {
  uni.previewImage({
    urls: imageList.value.map((i) => i.url),
    current: imageList.value[index].url
  })
}

function onDescChange(index, e) {
  imageList.value[index].description = String(e?.detail?.value || '')
  emitChange()
}

function addText() {
  textList.value.push({ id: `${Date.now()}-${Math.random()}`, content: '' })
}

function removeText(index) {
  textList.value.splice(index, 1)
  emitChange()
}

function onTextChange(index, e) {
  textList.value[index].content = String(e?.detail?.value || '')
  emitChange()
}
</script>

<style lang="scss" scoped>
.evidence-upload__images {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.evidence-upload__img-item {
  width: 200rpx;
}

.evidence-upload__img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
}

.evidence-upload__img-remove {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 18rpx;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.evidence-upload__remove-text {
  color: #fff;
  font-size: 24rpx;
}

.evidence-upload__desc-input {
  width: 100%;
  height: 56rpx;
  border: 1rpx solid #e8e8e8;
  border-radius: 8rpx;
  padding: 0 12rpx;
  margin-top: 8rpx;
  font-size: 22rpx;
  box-sizing: border-box;
}

.evidence-upload__placeholder {
  color: #bbb;
}

.evidence-upload__add {
  width: 200rpx;
  height: 200rpx;
  border: 2rpx dashed #ddd;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.evidence-upload__add-icon {
  font-size: 48rpx;
  color: #ccc;
  line-height: 1;
}

.evidence-upload__add-text {
  font-size: 22rpx;
  color: #999;
}

.evidence-upload__text-section {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.evidence-upload__text-item {
  position: relative;
}

.evidence-upload__textarea {
  width: 100%;
  height: 160rpx;
  border: 2rpx solid #e8e8e8;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.evidence-upload__text-remove {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
}

.evidence-upload__text-remove-text {
  font-size: 24rpx;
  color: #ff4d4f;
}

.evidence-upload__add-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 72rpx;
  border: 2rpx dashed #ddd;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #4A90D9;
}

.evidence-upload__add-text-icon {
  font-size: 32rpx;
  line-height: 1;
}
</style>
