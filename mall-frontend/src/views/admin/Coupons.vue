<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="优惠券名称" clearable style="width: 200px" @keyup.enter="load(1)" />
        <el-button type="primary" @click="load(1)">查询</el-button>
      </div>
      <el-button type="danger" @click="openCreate">新增优惠券</el-button>
    </div>

    <el-table :data="list">
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.type === 0 ? 'danger' : 'warning'">{{ row.type === 0 ? '满减' : '折扣' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="面额" width="100">
        <template #default="{ row }">{{ row.type === 0 ? '¥' + row.amount : row.amount + ' 折' }}</template>
      </el-table-column>
      <el-table-column label="门槛" width="100">
        <template #default="{ row }">满 ¥{{ row.minPoint }}</template>
      </el-table-column>
      <el-table-column label="发行/限领" width="120">
        <template #default="{ row }">{{ row.count }} / 每人 {{ row.perLimit }}</template>
      </el-table-column>
      <el-table-column label="有效期" min-width="260">
        <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
      </el-table-column>
      <el-table-column label="适用范围" width="110">
        <template #default="{ row }">{{ ['全场', '指定分类', '指定商品'][row.useType] }}</template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button text @click="openHistory(row)">领取记录</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑优惠券' : '新增优惠券'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">满减</el-radio>
            <el-radio :value="1">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.type === 0 ? '减免金额' : '折扣(如8=8折)'">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="使用门槛"><el-input-number v-model="form.minPoint" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="发行数量"><el-input-number v-model="form.count" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="每人限领"><el-input-number v-model="form.perLimit" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="适用范围">
          <el-radio-group v-model="form.useType">
            <el-radio :value="0">全场</el-radio>
            <el-radio :value="1">指定分类</el-radio>
            <el-radio :value="2">指定商品</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="指定分类" v-if="form.useType === 1">
          <el-select v-model="form.categoryIds" multiple style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.label" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="指定商品" v-if="form.useType === 2">
          <el-select v-model="form.productIds" multiple filterable style="width: 100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.note" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="danger" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="historyDialog" title="领取记录" width="680px">
      <el-table :data="history" size="small">
        <el-table-column prop="memberNickname" label="会员" width="130" />
        <el-table-column prop="couponCode" label="券码" min-width="170" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.useStatus === 1 ? 'success' : 'warning'">{{ row.useStatus === 1 ? '已使用' : '未使用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderSn" label="使用订单" min-width="170" />
        <el-table-column prop="getTime" label="领取时间" width="170" />
      </el-table>
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
const historyDialog = ref(false)
const history = ref([])
const products = ref([])
const categories = ref([])
const form = reactive({
  id: null, name: '', type: 0, amount: 0, minPoint: 0, count: 100, perLimit: 1,
  startTime: '', endTime: '', useType: 0, productIds: [], categoryIds: [], note: ''
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
    const data = await http.get('/admin/coupon/list', {
      params: { pageNum: page, pageSize: pageSize.value, keyword: keyword.value || undefined }
    })
    list.value = data.list
    total.value = data.total
    pageNum.value = page
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null, name: '', type: 0, amount: 0, minPoint: 0, count: 100, perLimit: 1,
    startTime: '', endTime: '', useType: 0, productIds: [], categoryIds: [], note: ''
  })
}

function openCreate() {
  resetForm()
  dialog.value = true
}

async function openEdit(row) {
  const detail = await http.get(`/admin/coupon/${row.id}`)
  Object.assign(form, detail)
  form.productIds = detail.productIds || []
  form.categoryIds = detail.categoryIds || []
  dialog.value = true
}

async function save() {
  if (!form.name || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写名称和有效期')
    return
  }
  await http.post(form.id ? `/admin/coupon/update/${form.id}` : '/admin/coupon/create', form)
  dialog.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除优惠券「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post('/admin/coupon/delete', [row.id])
  load()
}

async function openHistory(row) {
  history.value = await http.get('/admin/coupon/history', { params: { couponId: row.id } })
  historyDialog.value = true
}

onMounted(async () => {
  const tree = await http.get('/admin/productCategory/tree')
  categories.value = flatten(tree)
  products.value = await http.get('/admin/product/simpleList')
  load(1)
})
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
