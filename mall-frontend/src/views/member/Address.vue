<template>
  <div class="mall-card panel">
    <div class="panel-head">
      <div class="panel-title">收货地址</div>
      <el-button type="danger" :icon="Plus" @click="openCreate">新增地址</el-button>
    </div>
    <div class="address-list" v-loading="loading">
      <div v-for="addr in list" :key="addr.id" class="address-card">
        <div class="top">
          <strong>{{ addr.name }}</strong>
          <span>{{ addr.phone }}</span>
          <el-tag v-if="addr.defaultStatus === 1" size="small" type="danger">默认</el-tag>
        </div>
        <div class="muted address-text">{{ addr.province }} {{ addr.city }} {{ addr.region }} {{ addr.detailAddress }}</div>
        <div class="ops">
          <el-button text type="primary" @click="openEdit(addr)">编辑</el-button>
          <el-button v-if="addr.defaultStatus !== 1" text @click="setDefault(addr)">设为默认</el-button>
          <el-button text type="danger" @click="remove(addr)">删除</el-button>
        </div>
      </div>
      <div v-if="!list.length" class="empty-block">还没有收货地址</div>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑地址' : '新增地址'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="form.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="form.region" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.detailAddress" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="form.defaultStatus" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="danger" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import http from '../../utils/request'

const list = ref([])
const loading = ref(true)
const dialog = ref(false)
const form = reactive({
  id: null, name: '', phone: '', province: '', city: '', region: '', detailAddress: '', defaultStatus: 0
})

async function load() {
  loading.value = true
  try {
    list.value = await http.get('/portal/address/list')
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(form, { id: null, name: '', phone: '', province: '', city: '', region: '', detailAddress: '', defaultStatus: 0 })
}

function openCreate() {
  reset()
  dialog.value = true
}

function openEdit(addr) {
  Object.assign(form, addr)
  dialog.value = true
}

async function save() {
  if (!form.name || !form.phone || !form.detailAddress) {
    ElMessage.warning('请完整填写收货信息')
    return
  }
  if (form.id) {
    await http.post('/portal/address/update', form)
  } else {
    await http.post('/portal/address/create', form)
  }
  dialog.value = false
  ElMessage.success('已保存')
  load()
}

async function setDefault(addr) {
  await http.post('/portal/address/update', { ...addr, defaultStatus: 1 })
  load()
}

async function remove(addr) {
  await ElMessageBox.confirm('确定删除该地址吗？', '提示', { type: 'warning' })
  await http.post(`/portal/address/delete/${addr.id}`)
  load()
}

onMounted(load)
</script>

<style scoped>
.panel {
  padding: 24px;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.panel-title {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 0;
}
.address-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.address-card {
  border: 1px solid #eef0f4;
  border-radius: 12px;
  padding: 16px;
  background: #fff;
}
.address-card .top {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 8px;
}
.address-text {
  min-height: 36px;
}
.ops {
  margin-top: 10px;
  display: flex;
  gap: 4px;
}
@media (max-width: 800px) {
  .address-list {
    grid-template-columns: 1fr;
  }
}
</style>
