<template>
  <div class="product-list-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>商品列表</span>
        </div>
      </template>
      <div class="placeholder">
        <el-empty description="商品列表功能开发中..." />
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 商品列表页面 - 待开发
</script>

<style scoped>
.product-list-page {
  padding: 20px;
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.placeholder {
  padding: 60px 0;
}
</style>
