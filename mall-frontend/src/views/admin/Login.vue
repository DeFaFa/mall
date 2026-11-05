<template>
  <div class="admin-login">
    <div class="login-card">
      <div class="brand">
        <span class="mark">m</span>
        <div>
          <h1>mall 管理后台</h1>
          <p>商品 · 订单 · 营销 · 权限</p>
        </div>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password size="large" placeholder="请输入密码" />
        </el-form-item>
        <el-button type="danger" size="large" class="submit" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
      <div class="tips">
        <span>演示账号：admin / 123456</span>
        <router-link to="/">返回商城</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'
import { setAdminAuth } from '../../utils/auth'

const form = reactive({ username: 'admin', password: '123456' })
const loading = ref(false)
const router = useRouter()

async function submit() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const data = await http.post('/admin/login', form)
    setAdminAuth(data.token, data.info)
    ElMessage.success('登录成功')
    router.replace('/admin/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: radial-gradient(circle at 15% 10%, #2a1a18, #0d0f13 65%);
  padding: 30px;
}
.login-card {
  width: min(420px, 100%);
  background: #fff;
  border-radius: 18px;
  padding: 34px;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.35);
}
.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 24px;
}
.mark {
  width: 44px;
  height: 44px;
  background: #e1251b;
  color: #fff;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 26px;
  font-weight: 800;
}
h1 {
  margin: 0;
  font-size: 20px;
}
p {
  margin: 3px 0 0;
  color: #8b8f99;
  font-size: 12px;
}
.submit {
  width: 100%;
  margin-top: 6px;
}
.tips {
  display: flex;
  justify-content: space-between;
  color: #8b8f99;
  font-size: 12px;
  margin-top: 18px;
}
</style>
