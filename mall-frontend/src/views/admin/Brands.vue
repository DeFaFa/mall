<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="品牌名称" clearable style="width: 180px" @keyup.enter="load(1)" />
        <el-button type="primary" @click="load(1)">查询</el-button>
      </div>
      <el-button type="danger" @click="openCreate">新增品牌</el-button>
    </div>

    <div class="batch-bar">
      <el-button size="small" :disabled="!selection.length" @click="batch('showStatus', 1)">批量显示</el-button>
      <el-button size="small" :disabled="!selection.length" @click="batch('showStatus', 0)">批量隐藏</el-button>
      <el-button size="small" :disabled="!selection.length" @click="batch('factoryStatus', 1)">批量设为制造商</el-button>
    </div>

    <el-table :data="list" @selection-change="(rows) => (selection = rows)">
      <el-table-column type="selection" width="44" />
      <el-table-column label="品牌" min-width="220">
        <template #default="{ row }">
          <div class="brand-cell">
            <img :src="row.logo" />
            <div>
              <div class="name">{{ row.name }}</div>
              <div class="muted">首字母 {{ row.firstLetter || '-' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="显示" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.showStatus === 1" @change="(v) => toggle(row, 'showStatus', v)" />
        </template>
      </el-table-column>
      <el-table-column label="制造商" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.factoryStatus === 1 ? 'success' : 'info'">{{ row.factoryStatus === 1 ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="brandStory" label="品牌故事" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑品牌' : '新增品牌'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="品牌名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="首字母"><el-input v-model="form.firstLetter" maxlength="1" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="显示"><el-switch v-model="form.showStatus" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="制造商"><el-switch v-model="form.factoryStatus" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="Logo URL"><el-input v-model="form.logo" /></el-form-item>
        <el-form-item label="品牌大图"><el-input v-model="form.bigPic" /></el-form-item>
        <el-form-item label="品牌故事"><el-input v-model="form.brandStory" type="textarea" :rows="3" /></el-form-item>
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
const selection = ref([])
const dialog = ref(false)
const form = reactive({
  id: null, name: '', firstLetter: '', sort: 0, showStatus: 1, factoryStatus: 0,
  logo: '', bigPic: '', brandStory: ''
})

async function load(page = pageNum.value) {
  loading.value = true
  try {
    const data = await http.get('/admin/brand/list', {
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
  Object.assign(form, {
    id: null, name: '', firstLetter: '', sort: 0, showStatus: 1, factoryStatus: 0,
    logo: '', bigPic: '', brandStory: ''
  })
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialog.value = true
}

async function save() {
  if (!form.name) {
    ElMessage.warning('请填写品牌名称')
    return
  }
  await http.post(form.id ? '/admin/brand/update' : '/admin/brand/create', form)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function toggle(row, field, value) {
  await http.post('/admin/brand/batchStatus', null, {
    params: { ids: row.id, field, value: value ? 1 : 0 }
  })
  load()
}

async function batch(field, value) {
  await http.post('/admin/brand/batchStatus', null, {
    params: { ids: selection.value.map((r) => r.id).join(','), field, value }
  })
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除品牌「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post('/admin/brand/delete', [row.id])
  ElMessage.success('已删除')
  load()
}

onMounted(() => load(1))
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.filters {
  display: flex;
  gap: 8px;
}
.batch-bar {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}
.brand-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}
.brand-cell img {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}
.name {
  font-weight: 600;
  font-size: 13px;
}
.muted {
  color: #9aa0aa;
  font-size: 12px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
