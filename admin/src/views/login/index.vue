<template>
  <div class="login-page">
    <!-- 左侧装饰区 -->
    <div class="login-left">
      <div class="left-content">
        <!-- Logo 和系统名称 -->
        <div class="brand-section">
          <div class="logo-wrapper">
            <img v-if="logoExists" src="/src/static/校徽.png" class="logo-img" alt="logo" @error="logoExists = false" />
            <div v-else class="logo-placeholder">轻</div>
          </div>
          <h1 class="system-name">轻院二手</h1>
        </div>
        <p class="system-subtitle">后台管理系统</p>

        <!-- 系统描述卡片 -->
        <div class="description-card">
          <h3 class="school-name">广东轻工职业技术大学</h3>
          <p class="platform-desc">校园二手交易平台 · 让闲置物品高效流转</p>
        </div>
      </div>

      <!-- 底部版权信息 -->
      <div class="copyright">
        © 2025 轻院二手 · 毕业设计项目
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-right">
      <div class="login-card">
        <!-- 欢迎标题 -->
        <h2 class="welcome-title">欢迎回来</h2>
        <p class="welcome-subtitle">请登录您的管理员账号</p>

        <!-- 登录表单 -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <!-- 账号输入框 -->
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入管理员账号"
              :prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>

          <!-- 密码输入框 -->
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入登录密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              clearable
            />
          </el-form-item>

          <!-- 登录按钮 -->
          <el-form-item class="login-btn-wrapper">
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部提示 -->
        <p class="bottom-hint">
          轻院二手后台管理系统 · 仅限授权人员登录
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { adminLogin } from '@/api/auth'

// 路由实例
const router = useRouter()
// 用户状态管理
const userStore = useUserStore()
// 表单引用
const formRef = ref(null)
// 加载状态
const loading = ref(false)
// Logo是否存在
const logoExists = ref(true)

// 表单数据
const form = reactive({
  username: '',
  password: ''
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  if (!formRef.value) return

  try {
    // 1. 触发表单校验
    await formRef.value.validate()

    // 2. 设置加载状态
    loading.value = true

    // 3. 调用登录接口
    const res = await adminLogin({
      username: form.username,
      password: form.password
    })

    // 4. 响应处理
    if (res.code === 1) {
      // 登录成功
      const { token, id, name, username, role } = res.data

      // a. 存储token和用户信息
      userStore.setToken(token)
      userStore.setUserInfo({ id, name, username, role })

      // b. 显示成功提示
      ElMessage.success(`登录成功，欢迎回来 ${name}！`)

      // c. 跳转至仪表盘
      router.push('/dashboard')
    } else {
      // 登录失败
      ElMessage.error(res.msg || '登录失败')
      // 清空密码输入框
      form.password = ''
    }
  } catch (error) {
    // 请求异常处理
    if (error?.msg) {
      ElMessage.error(error.msg)
    } else if (error?.message && error.message !== '表单校验失败') {
      ElMessage.error('网络异常，请稍后重试')
    }
    // 清空密码输入框
    form.password = ''
  } finally {
    // 关闭加载状态
    loading.value = false
  }
}

/**
 * 检查是否已登录
 */
const checkAlreadyLogin = () => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    // 已有token，直接跳转仪表盘
    router.push('/dashboard')
  }
}

// 页面加载时检查登录状态
onMounted(() => {
  checkAlreadyLogin()
})
</script>

<style scoped>
/* 页面整体布局 */
.login-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

/* ==================== 左侧装饰区 ==================== */
.login-left {
  position: relative;
  width: 50%;
  height: 100%;
  background: linear-gradient(135deg, #569CD6 0%, #3a7bd5 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.left-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
}

/* 品牌区域 */
.brand-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.logo-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-placeholder {
  width: 48px;
  height: 48px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  color: #569CD6;
}

.system-name {
  font-size: 28px;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
}

.system-subtitle {
  font-size: 16px;
  color: #ffffff;
  opacity: 0.85;
  margin: 0 0 40px 0;
}

/* 描述卡片 */
.description-card {
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  max-width: 320px;
}

.school-name {
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  margin: 0 0 8px 0;
}

.platform-desc {
  font-size: 13px;
  color: #ffffff;
  opacity: 0.8;
  margin: 0;
}

/* 版权信息 */
.copyright {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #ffffff;
  opacity: 0.6;
  white-space: nowrap;
}

/* ==================== 右侧登录区 ==================== */
.login-right {
  width: 50%;
  height: 100%;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

/* 登录卡片 */
.login-card {
  width: 400px;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 48px 40px;
  text-align: center; 
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px 0;
}

/* 登录表单 */
.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #1a1a2e;
}

.login-form :deep(.el-input__inner) {
  height: 44px;
}

.login-btn-wrapper {
  margin-top: 24px;
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.login-btn :deep(.el-icon) {
  font-size: 18px;
}

/* 底部提示 */
.bottom-hint {
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
  margin: 0;
}

/* ==================== 响应式设计 ==================== */
@media screen and (max-width: 768px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
    padding: 16px;
  }

  .login-card {
    width: 100%;
    max-width: 400px;
    padding: 32px 24px;
  }
}
</style>
