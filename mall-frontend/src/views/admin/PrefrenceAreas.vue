<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <span class="muted">首页优选专区，一个专区可关联多个商品</span>
      <el-button type="danger" @click="openArea()">新增专区</el-button>
    </div>
    <el-table :data="list">
      <el-table-column label="图片" width="150">
        <template #default="{ row }"><img :src="row.pic" class="cover" /></template>
      </el-table-column>
      <el-table-column prop="name" label="专区名称" min-width="160" />
      <el-table-column prop="subTitle" label="副标题" min-width="180" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="显示" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.showStatus === 1 ? 'success' : 'info'">{{ row.showStatus === 1 ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button text type="primary" @click="openArea(row)">编辑</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑专区' : '新增专区'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subTitle" /></el-form-item>
        <el-form-item label="图片"><el-input v-model="form.pic" placeholder="图片 URL" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="显示"><el-switch v-model="form.showStatus" :active-value="1" :inactive-value="0" /></el-form-item>
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
const form = reactive({ id: null, name: '', subTitle: '', pic: '', sort: 0, showStatus: 1 })

async function load() {
  loading.value = true
  try {
    list.value = await http.get('/admin/prefrenceArea/list')
  } finally {
    loading.value = false
  }
}

function openArea(row) {
  Object.assign(form, { id: null, name: '', subTitle: '', pic: '', sort: 0, showStatus: 1 })
  if (row) Object.assign(form, row)
  dialog.value = true
}

async function save() {
  if (!form.name) {
    ElMessage.warning('请填写专区名称')
    return
  }
  await http.post(form.id ? '/admin/prefrenceArea/update' : '/admin/prefrenceArea/create', form)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除专区「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/prefrenceArea/delete/${row.id}`)
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
.cover {
  width: 110px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}
</style>
