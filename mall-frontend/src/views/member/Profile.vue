<template>
  <div class="mall-card panel" v-loading="loading">
    <div class="panel-title">个人资料</div>
    <div class="profile-head">
      <div class="avatar">
        <img v-if="form.icon" :src="form.icon" />
        <el-icon v-else><UserFilled /></el-icon>
      </div>
      <div>
        <div class="nick">{{ form.nickname || form.username }}</div>
        <div class="muted">{{ info?.level?.name }} · 成长值 {{ info?.member?.growth }} · 积分 {{ info?.member?.integration }}</div>
      </div>
    </div>
    <el-form :model="form" label-width="90px" class="profile-form">
      <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :value="0">保密</el-radio>
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="生日">
        <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
      </el-form-item>
      <el-form-item label="所在城市"><el-input v-model="form.city" /></el-form-item>
      <el-form-item label="头像地址"><el-input v-model="form.icon" placeholder="图片 URL" /></el-form-item>
      <el-form-item>
        <el-button type="danger" @click="save">保存修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const info = ref(null)
const loading = ref(true)
const form = reactive({
  username: '', nickname: '', phone: '', gender: 0, birthday: null, city: '', icon: ''
})

async function load() {
  loading.value = true
  try {
    info.value = await http.get('/portal/member/info')
    Object.assign(form, {
      username: info.value.member.username,
      nickname: info.value.member.nickname,
      phone: info.value.member.phone,
      gender: info.value.member.gender || 0,
      birthday: info.value.member.birthday,
      city: info.value.member.city,
      icon: info.value.member.icon
    })
  } finally {
    loading.value = false
  }
}

async function save() {
  await http.post('/portal/member/update', form)
  ElMessage.success('资料已更新')
  load()
}

onMounted(load)
</script>

<style scoped>
.panel {
  padding: 24px;
}
.panel-title {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 18px;
}
.profile-head {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 18px;
  background: #fafbfc;
  border-radius: 12px;
  margin-bottom: 22px;
}
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #eef0f4;
  display: grid;
  place-items: center;
  overflow: hidden;
  font-size: 28px;
  color: #9aa0aa;
}
.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.nick {
  font-size: 18px;
  font-weight: 700;
}
.profile-form {
  max-width: 520px;
}
</style>
