<template>
  <section class="card">
    <h1 class="title">登录</h1>
    <p class="desc">这是最小骨架占位页。</p>
    <div class="form">
      <input placeholder="账号" />
      <input placeholder="密码" type="password" />
      <button type="button">登录</button>
    </div>
  </section>
</template>

<style>
.card {
  max-width: 360px;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 10px;
}
.title {
  margin: 0;
  font-size: 18px;
}
.desc {
  margin: 8px 0 16px;
  color: #666;
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
</style>
