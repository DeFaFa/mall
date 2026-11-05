<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div>
        <div class="title">商品分类</div>
        <div class="muted">树形结构通过 parent_id 维护，控制导航栏显示</div>
      </div>
      <el-button type="danger" @click="openCreate(0)">新增一级分类</el-button>
    </div>

    <el-table :data="tree" row-key="id" :tree-props="{ children: 'children' }" default-expand-all>
      <el-table-column prop="name" label="分类名称" min-width="220" />
      <el-table-column prop="level" label="层级" width="70">
        <template #default="{ row }">L{{ row.level }}</template>
      </el-table-column>
      <el-table-column prop="productCount" label="商品数" width="90" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="导航栏" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.navStatus === 1 ? 'success' : 'info'">{{ row.navStatus === 1 ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.showStatus === 1 ? 'success' : 'info'">{{ row.showStatus === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.level === 0" text @click="openCreate(row.id)">新增子类</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑分类' : '新增分类'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" style="width: 100%">
            <el-option label="顶级分类" :value="0" />
            <el-option v-for="c in parents" :key="c.id" :label="c.label" :value="c.id" :disabled="c.id === form.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="计量单位"><el-input v-model="form.productUnit" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="导航栏显示"><el-switch v-model="form.navStatus" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.showStatus" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="图标 URL"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="关键词"><el-input v-model="form.keywords" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
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
const form = reactive({
  id: null, parentId: 0, name: '', productUnit: '件', sort: 0, navStatus: 1, showStatus: 1,
  icon: '', keywords: '', description: ''
})

function flatten(tree, level = 0, result = []) {
  for (const c of tree) {
    result.push({ id: c.id, label: (level ? '　'.repeat(level) : '') + c.name })
    if (c.children?.length) flatten(c.children, level + 1, result)
  }
  return result
}

async function load() {
  loading.value = true
  try {
    tree.value = await http.get('/admin/productCategory/tree')
    parents.value = flatten(tree.value).filter((c) => !c.label.startsWith('　　'))
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(form, {
    id: null, parentId: 0, name: '', productUnit: '件', sort: 0, navStatus: 1, showStatus: 1,
    icon: '', keywords: '', description: ''
  })
}

function openCreate(parentId) {
  reset()
  form.parentId = parentId
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialog.value = true
}

async function save() {
  if (!form.name) {
    ElMessage.warning('请填写分类名称')
    return
  }
  if (form.id) {
    await http.post('/admin/productCategory/update', form)
  } else {
    await http.post('/admin/productCategory/create', form)
  }
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/productCategory/delete/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.title {
  font-weight: 700;
  font-size: 16px;
}
.muted {
  color: #9aa0aa;
  font-size: 12px;
  margin-top: 4px;
}
</style>
