<template>
  <el-card shadow="never" v-loading="loading">
    <div class="split">
      <div class="left">
        <div class="head">
          <strong>属性分类</strong>
          <el-button size="small" type="primary" @click="openCategory()">新增</el-button>
        </div>
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="cat-item"
          :class="{ active: cat.id === currentCategoryId }"
          @click="selectCategory(cat)"
        >
          <div>
            <div class="cat-name">{{ cat.name }}</div>
            <div class="muted">商品分类 ID：{{ cat.productCategoryId }}</div>
          </div>
          <div class="ops">
            <el-button text type="primary" @click.stop="openCategory(cat)">编辑</el-button>
            <el-button text type="danger" @click.stop="removeCategory(cat)">删除</el-button>
          </div>
        </div>
        <div v-if="!categories.length" class="empty-small">暂无属性分类</div>
      </div>

      <div class="right">
        <div class="head">
          <strong>属性列表</strong>
          <el-button size="small" type="primary" :disabled="!currentCategoryId" @click="openAttribute()">新增属性</el-button>
        </div>
        <el-table :data="attributes" size="small">
          <el-table-column prop="name" label="属性名" min-width="140" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.type === 0 ? 'danger' : 'info'">{{ row.type === 0 ? '规格' : '参数' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="inputList" label="可选值" min-width="200" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="70" />
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <el-button text type="primary" @click="openAttribute(row)">编辑</el-button>
              <el-button text type="danger" @click="removeAttribute(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="categoryDialog" :title="categoryForm.id ? '编辑属性分类' : '新增属性分类'" width="480px">
      <el-form :model="categoryForm" label-width="110px">
        <el-form-item label="名称"><el-input v-model="categoryForm.name" /></el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="categoryForm.productCategoryId" style="width: 100%">
            <el-option v-for="c in productCategories" :key="c.id" :label="c.label" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialog = false">取消</el-button>
        <el-button type="danger" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="attrDialog" :title="attrForm.id ? '编辑属性' : '新增属性'" width="500px">
      <el-form :model="attrForm" label-width="110px">
        <el-form-item label="属性名称"><el-input v-model="attrForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="attrForm.type">
            <el-radio :value="0">规格（影响 SKU）</el-radio>
            <el-radio :value="1">参数（详情展示）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="可选值"><el-input v-model="attrForm.inputList" placeholder="逗号分隔，例如：黑色,白色" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="attrForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="attrDialog = false">取消</el-button>
        <el-button type="danger" @click="saveAttribute">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const categories = ref([])
const attributes = ref([])
const productCategories = ref([])
const currentCategoryId = ref(null)
const loading = ref(true)
const categoryDialog = ref(false)
const attrDialog = ref(false)
const categoryForm = reactive({ id: null, name: '', productCategoryId: null })
const attrForm = reactive({ id: null, name: '', type: 0, inputList: '', sort: 0, productAttributeCategoryId: null })

function flatten(tree, level = 0, result = []) {
  for (const c of tree) {
    result.push({ id: c.id, label: (level ? '　'.repeat(level) : '') + c.name })
    if (c.children?.length) flatten(c.children, level + 1, result)
  }
  return result
}

async function loadCategories() {
  categories.value = await http.get('/admin/productAttribute/category/list')
  if (!currentCategoryId.value && categories.value.length) {
    currentCategoryId.value = categories.value[0].id
  }
  await loadAttributes()
}

async function loadAttributes() {
  if (!currentCategoryId.value) {
    attributes.value = []
    return
  }
  attributes.value = await http.get('/admin/productAttribute/list', {
    params: { categoryId: currentCategoryId.value }
  })
}

function selectCategory(cat) {
  currentCategoryId.value = cat.id
  loadAttributes()
}

function openCategory(cat) {
  Object.assign(categoryForm, { id: null, name: '', productCategoryId: null })
  if (cat) Object.assign(categoryForm, cat)
  categoryDialog.value = true
}

async function saveCategory() {
  await http.post(categoryForm.id ? '/admin/productAttribute/category/update' : '/admin/productAttribute/category/create', categoryForm)
  categoryDialog.value = false
  ElMessage.success('保存成功')
  loadCategories()
}

async function removeCategory(cat) {
  await ElMessageBox.confirm(`确定删除属性分类「${cat.name}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/productAttribute/category/delete/${cat.id}`)
  if (currentCategoryId.value === cat.id) currentCategoryId.value = null
  loadCategories()
}

function openAttribute(row) {
  Object.assign(attrForm, {
    id: null, name: '', type: 0, inputList: '', sort: 0, productAttributeCategoryId: currentCategoryId.value
  })
  if (row) Object.assign(attrForm, row)
  attrDialog.value = true
}

async function saveAttribute() {
  await http.post(attrForm.id ? '/admin/productAttribute/update' : '/admin/productAttribute/create', attrForm)
  attrDialog.value = false
  ElMessage.success('保存成功')
  loadAttributes()
}

async function removeAttribute(row) {
  await ElMessageBox.confirm(`确定删除属性「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/productAttribute/delete/${row.id}`)
  loadAttributes()
}

onMounted(async () => {
  const tree = await http.get('/admin/productCategory/tree')
  productCategories.value = flatten(tree)
  try {
    await loadCategories()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.split {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
}
.left {
  border-right: 1px solid #f0f1f4;
  padding-right: 20px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.cat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
}
.cat-item.active {
  background: #fff1f0;
}
.cat-name {
  font-weight: 600;
  font-size: 13px;
}
.ops {
  display: flex;
  gap: 2px;
}
.muted {
  color: #9aa0aa;
  font-size: 12px;
}
.empty-small {
  color: #9aa0aa;
  padding: 20px 0;
  font-size: 13px;
}
@media (max-width: 900px) {
  .split {
    grid-template-columns: 1fr;
  }
  .left {
    border-right: 0;
    padding-right: 0;
  }
}
</style>
