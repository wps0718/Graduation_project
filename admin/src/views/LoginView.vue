<template>
  <div class="login-container">
    <el-form :model="loginForm" class="login-form">
      <h3>Login</h3>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" placeholder="Username"></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" placeholder="Password"></el-input>
      </el-form-item>
      <el-button type="primary" @click="handleLogin">Login</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = () => {
  userStore.login(loginForm).then(() => {
    router.push('/')
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
.login-form {
  width: 300px;
}
</style>
