<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>注册会员</h1>
      <p class="muted">注册后即可收藏商品、领取优惠券、下单购买</p>
      <el-form :model="form" label-position="top">
        <el-form-item label="账号 / 手机号">
          <el-input v-model="form.username" size="large" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" size="large" placeholder="选填" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" size="large" placeholder="选填" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" size="large" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="短信验证码（演示）">
          <div class="code-row">
            <el-input v-model="code" size="large" placeholder="点击获取验证码" />
            <el-button @click="getCode">获取验证码</el-button>
          </div>
        </el-form-item>
        <el-button type="danger" size="large" class="submit" :loading="loading" @click="submit">注册</el-button>
      </el-form>
      <div class="tips">
        <span>已有账号？<router-link to="/login">去登录</router-link></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const form = reactive({ username: '', nickname: '', phone: '', password: '' })
const code = ref('')
const loading = ref(false)
const router = useRouter()

async function getCode() {
  if (!form.phone) {
    ElMessage.warning('请先填写手机号')
    return
  }
  const result = await http.get('/portal/auth/smsCode', { params: { phone: form.phone } })
  code.value = result
  ElMessage.success('验证码已生成：' + result)
}

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写账号和密码')
    return
  }
  loading.value = true
  try {
    await http.post('/portal/auth/register', form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
}
h1 {
  margin: 0;
}
.muted {
  margin: 6px 0 22px;
}
.submit {
  width: 100%;
}
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.tips {
  margin-top: 16px;
  font-size: 13px;
  color: #8b8f99;
}
.tips a {
  color: #e1251b;
}
</style>
