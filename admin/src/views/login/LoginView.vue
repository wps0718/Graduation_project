<template>
  <section class="card">
    <h1 class="title">登录</h1>
    <form class="form" @submit.prevent="handleLogin">
      <input v-model="form.username" placeholder="账号" />
      <input v-model="form.password" type="password" placeholder="密码" />
      <button type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
    </form>
    <p v-if="error" class="error">{{ error }}</p>
  </section>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  if (!form.username || !form.password) {
    error.value = '请输入账号和密码'
    return
  }
  error.value = ''
  loading.value = true
  try {
    await userStore.login(form)
    router.push(route.query.redirect || '/')
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style>
.card {
  max-width: 360px;
  margin: 100px auto;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 10px;
}
.title {
  margin: 0 0 16px;
  font-size: 18px;
}
.form {
  display: grid;
  gap: 10px;
}
.form input {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
.form button {
  padding: 10px;
  border: 1px solid #111;
  background: #111;
  color: #fff;
  border-radius: 8px;
  cursor: pointer;
}
.form button:disabled {
  background: #999;
}
.error {
  color: red;
  margin-top: 10px;
}
</style>