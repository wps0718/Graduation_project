<template>
  <div class="order-list-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>订单管理</span>
        </div>
      </template>
      <div class="placeholder">
        <el-empty description="订单管理功能开发中..." />
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 订单管理页面 - 待开发
</script>

<style scoped>
.order-list-page {
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
