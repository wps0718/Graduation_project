<template>
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside width="210px" class="aside">
      <div class="logo">
        <img :src="logo" alt="轻院二手" class="logo-img" />
        <span class="logo-text">轻院二手</span>
      </div>
      
      <el-menu
        :default-active="$route.path"
        router
        class="el-menu-vertical-demo"
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <el-sub-menu index="2">
          <template #title>
            <el-icon><Stamp /></el-icon>
            <span>审核管理</span>
          </template>
          <el-menu-item index="/product/review">商品审核</el-menu-item>
          <el-menu-item index="/auth-review">认证审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="3">
          <template #title>
            <el-icon><Briefcase /></el-icon>
            <span>业务管理</span>
          </template>
          <el-menu-item index="/product/list">商品列表</el-menu-item>
          <el-menu-item index="/order">订单管理</el-menu-item>
          <el-menu-item index="/user">用户管理</el-menu-item>
          <el-menu-item index="/report">举报管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="4">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/banner">Banner管理</el-menu-item>
          <el-menu-item index="/notice">公告管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="5">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/category">分类管理</el-menu-item>
          <el-menu-item index="/campus">校区管理</el-menu-item>
          <el-menu-item index="/college">学院管理</el-menu-item>
          <el-menu-item index="/employee">员工管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 右侧区域 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <span class="system-name">轻院二手后台管理系统</span>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              {{ userStore.userInfo?.name || '管理员' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
// 修改了这里：正确引入你的校徽图片
import logo from '@/static/校徽.png'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import { DataLine, Stamp, Briefcase, Document, Setting, ArrowDown } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  userStore.logout().then(() => {
    router.push('/login')
  })
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.aside { background-color: #545c64; }

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #3f4756;
  color: white;
  font-size: 18px;
  font-weight: 600;
}
.logo-img { width: 36px; height: 36px; }

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.system-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 14px;
}
</style>