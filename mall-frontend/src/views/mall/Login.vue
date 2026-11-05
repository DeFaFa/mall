<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand">
        <span class="mark">m</span>
        <div>
          <h1>mall 商城</h1>
          <p>登录会员账号</p>
        </div>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="账号 / 手机号">
          <el-input v-model="form.username" size="large" placeholder="请输入账号或手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" size="large" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="danger" size="large" class="submit" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <div class="tips">
        <span>演示账号：13800138000 / 123456</span>
        <router-link to="/register">注册新账号</router-link>
      </div>
      <div class="admin-link">
        <router-link to="/admin/login">进入后台管理 →</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'
import { setMemberAuth } from '../../utils/auth'

const form = reactive({ username: '13800138000', password: '123456' })
const loading = ref(false)
const router = useRouter()
const route = useRoute()

async function submit() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const data = await http.post('/portal/auth/login', form)
    setMemberAuth(data.token, data.member)
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: radial-gradient(circle at 20% 10%, #3b1f1c, #101114 60%);
  display: grid;
  place-items: center;
  padding: 40px 20px;
}
.auth-card {
  width: min(440px, 100%);
  background: #fff;
  border-radius: 18px;
  padding: 32px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.3);
}
.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 22px;
}
.mark {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #e1251b;
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 26px;
  font-weight: 800;
}
h1 {
  margin: 0;
  font-size: 21px;
}
p {
  margin: 2px 0 0;
  color: #8b8f99;
  font-size: 13px;
}
.submit {
  width: 100%;
  margin-top: 8px;
}
.tips {
  display: flex;
  justify-content: space-between;
  color: #8b8f99;
  font-size: 12px;
  margin-top: 16px;
}
.tips a {
  color: #e1251b;
}
.admin-link {
  text-align: center;
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid #f0f1f4;
  font-size: 13px;
  color: #8b8f99;
}
</style>
