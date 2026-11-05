<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <span class="muted">菜单控制“看不看得见”，资源控制“调不调得动”</span>
      <el-button type="danger" @click="openRole()">新增角色</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="name" label="角色名称" width="170" />
      <el-table-column prop="description" label="描述" min-width="240" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text type="primary" @click="openRole(row)">编辑</el-button>
          <el-button text type="danger" :disabled="row.id === 1" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑角色' : '新增角色'" width="680px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="菜单权限">
          <div class="tree-box">
            <el-tree
              ref="menuTreeRef"
              :data="menuTree"
              node-key="id"
              show-checkbox
              :props="{ label: 'title', children: 'children' }"
              default-expand-all
            />
          </div>
        </el-form-item>
        <el-form-item label="接口资源">
          <div class="tree-box resource-box">
            <el-checkbox-group v-model="form.resourceIds">
              <el-checkbox v-for="r in resources" :key="r.id" :value="r.id" class="resource-item">
                {{ r.name }} <span class="muted">{{ r.method }} {{ r.url }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="danger" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const list = ref([])
const loading = ref(true)
const dialog = ref(false)
const menuTree = ref([])
const resources = ref([])
const menuTreeRef = ref(null)
const form = reactive({ id: null, name: '', description: '', status: 1, sort: 0, menuIds: [], resourceIds: [], menuTree: [] })

async function load() {
  loading.value = true
  try {
    list.value = await http.get('/admin/role/list')
  } finally {
    loading.value = false
  }
}

function openRole(row) {
  Object.assign(form, { id: null, name: '', description: '', status: 1, sort: 0, menuIds: [], resourceIds: [] })
  dialog.value = true
  nextTick(() => menuTreeRef.value?.setCheckedKeys([]))
  if (row) {
    http.get(`/admin/role/${row.id}`).then((data) => {
      Object.assign(form, { id: data.id, name: data.name, description: data.description, status: data.status, sort: data.sort, resourceIds: data.resourceIds || [] })
      form.menuIds = data.menuIds || []
      nextTick(() => menuTreeRef.value?.setCheckedKeys(form.menuIds))
    })
  }
}

async function save() {
  if (!form.name) {
    ElMessage.warning('请填写角色名称')
    return
  }
  const menuIds = [
    ...(menuTreeRef.value?.getCheckedKeys() || []),
    ...(menuTreeRef.value?.getHalfCheckedKeys() || [])
  ]
  const payload = { ...form, menuIds, resourceIds: form.resourceIds }
  await http.post(form.id ? `/admin/role/update/${form.id}` : '/admin/role/create', payload)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除角色「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post('/admin/role/delete', [row.id])
  load()
}

onMounted(async () => {
  menuTree.value = await http.get('/admin/menu/tree')
  resources.value = await http.get('/admin/resource/all')
  load()
})
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
.tree-box {
  width: 100%;
  max-height: 240px;
  overflow: auto;
  border: 1px solid #eef0f4;
  border-radius: 8px;
  padding: 10px;
}
.resource-box {
  max-height: 220px;
}
.resource-item {
  display: flex;
  width: 100%;
  margin-right: 0;
  font-size: 13px;
}
.resource-item .muted {
  margin-left: 8px;
}
</style>
