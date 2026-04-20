<template>
  <div class="category-list-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>分类管理</span>
        </div>
      </template>
      <div class="placeholder">
        <el-empty description="分类管理功能开发中..." />
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 分类管理页面 - 待开发
</script>

<style scoped>
.category-list-page {
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
