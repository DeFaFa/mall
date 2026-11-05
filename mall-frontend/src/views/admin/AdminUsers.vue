<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="用户名" clearable style="width: 200px" @keyup.enter="load(1)" />
      <div>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button type="danger" @click="openCreate">新增管理员</el-button>
      </div>
    </div>

    <el-table :data="list">
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="nickName" label="昵称" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="note" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" :disabled="row.id === 1" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑管理员' : '新增管理员'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickName" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password :placeholder="form.id ? '留空则不修改' : '至少 6 位'" />
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="form.roleIds">
            <el-checkbox v-for="r in roles" :key="r.id" :value="r.id">{{ r.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.note" type="textarea" :rows="2" /></el-form-item>
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
const roles = ref([])
const form = reactive({
  id: null, username: '', nickName: '', email: '', password: '', note: '', status: 1, roleIds: []
})

async function load(page = pageNum.value) {
  loading.value = true
  try {
    const data = await http.get('/admin/admin/list', {
      params: { pageNum: page, pageSize: pageSize.value, keyword: keyword.value || undefined }
    })
    list.value = data.list
    total.value = data.total
    pageNum.value = page
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: null, username: '', nickName: '', email: '', password: '', note: '', status: 1, roleIds: [] })
  dialog.value = true
}

async function openEdit(row) {
  const data = await http.get(`/admin/admin/${row.id}`)
  Object.assign(form, data.admin, { roleIds: data.roleIds || [] })
  form.password = ''
  dialog.value = true
}

async function save() {
  if (!form.username) {
    ElMessage.warning('请填写用户名')
    return
  }
  await http.post(form.id ? `/admin/admin/update/${form.id}` : '/admin/admin/create', form)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除管理员「${row.username}」吗？`, '提示', { type: 'warning' })
  await http.post('/admin/admin/delete', [row.id])
  load()
}

onMounted(async () => {
  roles.value = await http.get('/admin/role/list')
  load(1)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
