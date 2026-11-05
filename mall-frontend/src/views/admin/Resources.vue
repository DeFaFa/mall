<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="资源名称" clearable style="width: 200px" @keyup.enter="load(1)" />
        <el-button type="primary" @click="load(1)">查询</el-button>
      </div>
      <el-button type="danger" @click="openResource()">新增资源</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="name" label="资源名称" min-width="170" />
      <el-table-column prop="url" label="URL 模式" min-width="220" />
      <el-table-column prop="method" label="请求方式" width="110" />
      <el-table-column prop="description" label="描述" min-width="180" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button text type="primary" @click="openResource(row)">编辑</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑资源' : '新增资源'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="资源名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="URL 模式"><el-input v-model="form.url" placeholder="/admin/product/**" /></el-form-item>
        <el-form-item label="请求方式">
          <el-select v-model="form.method" style="width: 100%">
            <el-option label="全部 (*)" value="*" />
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
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
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const loading = ref(true)
const dialog = ref(false)
const form = reactive({ id: null, name: '', url: '', method: '*', description: '' })

async function load(page = pageNum.value) {
  loading.value = true
  try {
    const data = await http.get('/admin/resource/list', {
      params: { pageNum: page, pageSize: pageSize.value, keyword: keyword.value || undefined }
    })
    list.value = data.list
    total.value = data.total
    pageNum.value = page
  } finally {
    loading.value = false
  }
}

function openResource(row) {
  Object.assign(form, { id: null, name: '', url: '', method: '*', description: '' })
  if (row) Object.assign(form, row)
  dialog.value = true
}

async function save() {
  if (!form.name || !form.url) {
    ElMessage.warning('请填写资源名称和 URL')
    return
  }
  await http.post(form.id ? '/admin/resource/update' : '/admin/resource/create', form)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除资源「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/resource/delete/${row.id}`)
  load()
}

onMounted(() => load(1))
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}
.filters {
  display: flex;
  gap: 8px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
