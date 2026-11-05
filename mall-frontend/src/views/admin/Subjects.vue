<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="专题名称" clearable style="width: 200px" @keyup.enter="load(1)" />
      <div>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button type="danger" @click="openSubject()">新增专题</el-button>
      </div>
    </div>

    <el-table :data="list">
      <el-table-column label="封面" width="130">
        <template #default="{ row }"><img :src="row.pic" class="cover" /></template>
      </el-table-column>
      <el-table-column prop="name" label="专题名称" min-width="160" />
      <el-table-column prop="mainTitle" label="主标题" min-width="160" />
      <el-table-column prop="subTitle" label="副标题" min-width="160" />
      <el-table-column prop="views" label="浏览量" width="90" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="显示" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.showStatus === 1 ? 'success' : 'info'">{{ row.showStatus === 1 ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button text type="primary" @click="openSubject(row)">编辑</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑专题' : '新增专题'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="专题名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="主标题"><el-input v-model="form.mainTitle" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subTitle" /></el-form-item>
        <el-form-item label="关联分类">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.label" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图"><el-input v-model="form.pic" placeholder="图片 URL" /></el-form-item>
        <el-form-item label="关联商品">
          <el-select v-model="form.productIds" multiple filterable style="width: 100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="显示"><el-switch v-model="form.showStatus" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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
const products = ref([])
const categories = ref([])
const form = reactive({
  id: null, name: '', mainTitle: '', subTitle: '', categoryId: 0, pic: '',
  productIds: [], sort: 0, showStatus: 1, description: ''
})

function flatten(tree, level = 0, result = []) {
  for (const c of tree) {
    result.push({ id: c.id, label: (level ? '　'.repeat(level) : '') + c.name })
    if (c.children?.length) flatten(c.children, level + 1, result)
  }
  return result
}

async function load(page = pageNum.value) {
  loading.value = true
  try {
    const data = await http.get('/admin/subject/list', {
      params: { pageNum: page, pageSize: pageSize.value, keyword: keyword.value || undefined }
    })
    list.value = data.list
    total.value = data.total
    pageNum.value = page
  } finally {
    loading.value = false
  }
}

async function openSubject(row) {
  Object.assign(form, {
    id: null, name: '', mainTitle: '', subTitle: '', categoryId: 0, pic: '',
    productIds: [], sort: 0, showStatus: 1, description: ''
  })
  if (row) {
    const detail = await http.get(`/admin/subject/${row.id}`)
    Object.assign(form, detail, { productIds: detail.productIds || [] })
  }
  dialog.value = true
}

async function save() {
  if (!form.name) {
    ElMessage.warning('请填写专题名称')
    return
  }
  await http.post(form.id ? `/admin/subject/update/${form.id}` : '/admin/subject/create', form)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除专题「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post('/admin/subject/delete', [row.id])
  load()
}

onMounted(async () => {
  products.value = await http.get('/admin/product/simpleList')
  const tree = await http.get('/admin/productCategory/tree')
  categories.value = flatten(tree)
  load(1)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}
.cover {
  width: 96px;
  height: 54px;
  object-fit: cover;
  border-radius: 6px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
