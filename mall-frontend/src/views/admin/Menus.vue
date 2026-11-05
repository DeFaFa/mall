<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <span class="muted">菜单树控制后台左侧导航显示</span>
      <el-button type="danger" @click="openMenu(null, 0)">新增一级菜单</el-button>
    </div>
    <el-table :data="tree" row-key="id" :tree-props="{ children: 'children' }" default-expand-all>
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="name" label="路由标识" width="160" />
      <el-table-column prop="icon" label="图标" width="120" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="隐藏" width="80">
        <template #default="{ row }">{{ row.hidden === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button text type="primary" @click="openMenu(row)">编辑</el-button>
          <el-button text @click="openMenu(null, row.id)">新增子菜单</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑菜单' : '新增菜单'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级菜单">
          <el-select v-model="form.parentId" style="width: 100%">
            <el-option label="顶级菜单" :value="0" />
            <el-option v-for="m in parents" :key="m.id" :label="m.label" :value="m.id" :disabled="m.id === form.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="路由标识"><el-input v-model="form.name" placeholder="如 product" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="隐藏"><el-switch v-model="form.hidden" :active-value="1" :inactive-value="0" /></el-form-item>
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

const tree = ref([])
const parents = ref([])
const loading = ref(true)
const dialog = ref(false)
const form = reactive({ id: null, parentId: 0, title: '', name: '', icon: '', sort: 0, hidden: 0 })

function flatten(tree, level = 0, result = []) {
  for (const c of tree) {
    result.push({ id: c.id, label: (level ? '　'.repeat(level) : '') + c.title })
    if (c.children?.length) flatten(c.children, level + 1, result)
  }
  return result
}

async function load() {
  loading.value = true
  try {
    tree.value = await http.get('/admin/menu/tree')
    parents.value = flatten(tree.value)
  } finally {
    loading.value = false
  }
}

function openMenu(row, parentId = 0) {
  Object.assign(form, { id: null, parentId, title: '', name: '', icon: '', sort: 0, hidden: 0 })
  if (row) Object.assign(form, row)
  dialog.value = true
}

async function save() {
  if (!form.title) {
    ElMessage.warning('请填写菜单标题')
    return
  }
  await http.post(form.id ? '/admin/menu/update' : '/admin/menu/create', form)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除菜单「${row.title}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/menu/delete/${row.id}`)
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
