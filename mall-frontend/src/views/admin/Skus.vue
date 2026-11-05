<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="SKU 编码" clearable style="width: 200px" @keyup.enter="load(1)" />
        <el-select v-model="productId" placeholder="选择商品" clearable filterable style="width: 240px">
          <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-button type="primary" @click="load(1)">查询</el-button>
      </div>
      <el-button type="primary" @click="openBatch">批量改库存</el-button>
    </div>

    <el-table :data="list" @selection-change="(rows) => (selectedRows = rows)">
      <el-table-column type="selection" width="44" />
      <el-table-column prop="skuCode" label="SKU 编码" width="160" />
      <el-table-column prop="productName" label="商品" min-width="200" />
      <el-table-column prop="spData" label="规格" min-width="180" show-overflow-tooltip />
      <el-table-column label="价格" width="110">
        <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
      </el-table-column>
      <el-table-column prop="stock" label="可用库存" width="100" />
      <el-table-column prop="lockStock" label="锁定库存" width="100" />
      <el-table-column prop="sale" label="销量" width="80" />
      <el-table-column prop="lowStock" label="预警值" width="90" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" title="编辑 SKU" width="460px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="SKU 编码"><el-input v-model="form.skuCode" disabled /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="可用库存"><el-input-number v-model="form.stock" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="预警库存"><el-input-number v-model="form.lowStock" :min="0" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="danger" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialog" title="批量调整库存" width="460px">
      <el-form label-width="100px">
        <el-form-item label="库存值"><el-input-number v-model="batchStock" :min="0" style="width: 100%" /></el-form-item>
      </el-form>
      <div class="muted">将选中的 SKU 可用库存统一设置为该值</div>
      <template #footer>
        <el-button @click="batchDialog = false">取消</el-button>
        <el-button type="danger" :disabled="!selectedRows.length" @click="saveBatch">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const productId = ref(null)
const products = ref([])
const loading = ref(true)
const dialog = ref(false)
const batchDialog = ref(false)
const batchStock = ref(0)
const selectedRows = ref([])
const form = reactive({ id: null, skuCode: '', price: 0, stock: 0, lowStock: 0, productId: null })

async function load(page = pageNum.value) {
  loading.value = true
  try {
    const data = await http.get('/admin/sku/list', {
      params: {
        pageNum: page, pageSize: pageSize.value,
        skuCode: keyword.value || undefined,
        productId: productId.value || undefined
      }
    })
    list.value = data.list
    total.value = data.total
    pageNum.value = page
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.assign(form, row)
  dialog.value = true
}

async function save() {
  await http.post('/admin/sku/update', form)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

function openBatch() {
  batchStock.value = 0
  batchDialog.value = true
}

async function saveBatch() {
  if (!selectedRows.value.length) return
  const payload = selectedRows.value.map((r) => ({ ...r, stock: batchStock.value }))
  await http.post('/admin/sku/batchStock', payload)
  batchDialog.value = false
  ElMessage.success('库存已更新')
  load()
}

onMounted(async () => {
  products.value = await http.get('/admin/product/simpleList')
  load(1)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.filters {
  display: flex;
  gap: 8px;
}
.price {
  color: #e1251b;
  font-weight: 600;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.muted {
  color: #9aa0aa;
  font-size: 12px;
}
</style>
