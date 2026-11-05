<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <span class="muted">会员等级按成长值划分，决定免邮门槛与商品折扣</span>
      <el-button type="danger" @click="openLevel()">新增等级</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="name" label="等级名称" width="160" />
      <el-table-column prop="growthPoint" label="成长值门槛" width="120" />
      <el-table-column label="默认等级" width="100">
        <template #default="{ row }">{{ row.defaultStatus === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="免邮门槛" width="110">
        <template #default="{ row }">¥{{ row.freeFreightPoint }}</template>
      </el-table-column>
      <el-table-column label="商品折扣" width="110">
        <template #default="{ row }">
          <span>{{ Number(row.productDiscount) >= 10 ? '无折扣' : row.productDiscount + ' 折' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button text type="primary" @click="openLevel(row)">编辑</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑会员等级' : '新增会员等级'" width="500px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="成长值门槛"><el-input-number v-model="form.growthPoint" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="默认等级"><el-switch v-model="form.defaultStatus" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="免邮门槛"><el-input-number v-model="form.freeFreightPoint" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="商品折扣"><el-input-number v-model="form.productDiscount" :min="1" :max="10" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="评价成长值"><el-input-number v-model="form.commentGrowthPoint" :min="0" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="danger" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const list = ref([])
const loading = ref(true)
const dialog = ref(false)
const form = reactive({
  id: null, name: '', growthPoint: 0, defaultStatus: 0, freeFreightPoint: 99,
  productDiscount: 10, commentGrowthPoint: 5
})

async function load() {
  loading.value = true
  try {
    list.value = await http.get('/admin/memberLevel/list')
  } finally {
    loading.value = false
  }
}

function openLevel(row) {
  Object.assign(form, {
    id: null, name: '', growthPoint: 0, defaultStatus: 0, freeFreightPoint: 99,
    productDiscount: 10, commentGrowthPoint: 5
  })
  if (row) Object.assign(form, row)
  dialog.value = true
}

async function save() {
  if (!form.name) {
    ElMessage.warning('请填写等级名称')
    return
  }
  await http.post(form.id ? '/admin/memberLevel/update' : '/admin/memberLevel/create', form)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除等级「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/memberLevel/delete/${row.id}`)
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}
.muted {
  color: #9aa0aa;
  font-size: 12px;
}
</style>
