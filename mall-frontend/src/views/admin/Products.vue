<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="filters.keyword" placeholder="商品名称 / 货号" clearable style="width: 200px" @keyup.enter="load(1)" />
        <el-select v-model="filters.productCategoryId" placeholder="商品分类" clearable style="width: 160px">
          <el-option v-for="c in categories" :key="c.id" :label="c.label" :value="c.id" />
        </el-select>
        <el-select v-model="filters.brandId" placeholder="品牌" clearable style="width: 130px">
          <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
        <el-select v-model="filters.publishStatus" placeholder="上架状态" clearable style="width: 120px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <el-button type="danger" @click="$router.push('/admin/product/edit')">新增商品</el-button>
    </div>

    <div class="batch-bar">
      <el-button size="small" :disabled="!selection.length" @click="batch('verifyStatus', 1)">批量审核通过</el-button>
      <el-button size="small" :disabled="!selection.length" @click="batch('publishStatus', 1)">批量上架</el-button>
      <el-button size="small" :disabled="!selection.length" @click="batch('publishStatus', 0)">批量下架</el-button>
      <el-button size="small" :disabled="!selection.length" @click="batch('recommandStatus', 1)">批量推荐</el-button>
      <el-button size="small" :disabled="!selection.length" @click="batch('newStatus', 1)">批量设为新品</el-button>
      <el-button size="small" type="danger" plain :disabled="!selection.length" @click="removeSelected">批量删除</el-button>
    </div>

    <el-table :data="list" @selection-change="(rows) => (selection = rows)">
      <el-table-column type="selection" width="44" />
      <el-table-column label="商品" min-width="280">
        <template #default="{ row }">
          <div class="product-cell">
            <img :src="row.pic" />
            <div>
              <div class="p-name">{{ row.name }}</div>
              <div class="muted">{{ row.productSn }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="110">
        <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sale" label="销量" width="80" />
      <el-table-column label="审核" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.verifyStatus === 1 ? 'success' : 'warning'">
            {{ row.verifyStatus === 1 ? '已审核' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上架" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.publishStatus === 1" @change="(v) => togglePublish(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="推荐/新品" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.recommandStatus === 1" size="small" type="danger" class="tag-gap">推荐</el-tag>
          <el-tag v-if="row.newStatus === 1" size="small" type="success">新品</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="$router.push(`/admin/product/edit/${row.id}`)">编辑</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>
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
const loading = ref(true)
const selection = ref([])
const brands = ref([])
const categories = ref([])
const filters = reactive({
  keyword: '', productCategoryId: null, brandId: null, publishStatus: null
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
    const data = await http.get('/admin/product/list', {
      params: {
        pageNum: page, pageSize: pageSize.value, keyword: filters.keyword || undefined,
        productCategoryId: filters.productCategoryId || undefined,
        brandId: filters.brandId || undefined,
        publishStatus: filters.publishStatus ?? undefined,
        deleteStatus: 0
      }
    })
    list.value = data.list
    total.value = data.total
    pageNum.value = page
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(filters, { keyword: '', productCategoryId: null, brandId: null, publishStatus: null })
  load(1)
}

async function batch(field, value) {
  const ids = selection.value.map((r) => r.id)
  await http.post('/admin/product/batchStatus', null, { params: { ids: ids.join(','), field, value } })
  ElMessage.success('操作成功')
  load()
}

async function togglePublish(row, value) {
  await http.post('/admin/product/batchStatus', null, {
    params: { ids: row.id, field: 'publishStatus', value: value ? 1 : 0 }
  })
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post('/admin/product/delete', [row.id])
  ElMessage.success('已删除')
  load()
}

async function removeSelected() {
  await ElMessageBox.confirm('确定批量删除选中的商品吗？', '提示', { type: 'warning' })
  await http.post('/admin/product/delete', selection.value.map((r) => r.id))
  load()
}

onMounted(async () => {
  const tree = await http.get('/admin/productCategory/tree')
  categories.value = flatten(tree)
  brands.value = await http.get('/admin/brand/all')
  load(1)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.batch-bar {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.product-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}
.product-cell img {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  object-fit: cover;
}
.p-name {
  font-weight: 600;
  font-size: 13px;
}
.muted {
  color: #9aa0aa;
  font-size: 12px;
}
.price {
  color: #e1251b;
  font-weight: 600;
}
.tag-gap {
  margin-right: 4px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
