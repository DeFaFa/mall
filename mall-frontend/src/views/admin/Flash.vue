<template>
  <el-card shadow="never" v-loading="loading">
    <el-tabs v-model="tab">
      <el-tab-pane label="秒杀活动" name="promotion">
        <div class="toolbar">
          <span class="muted">活动定义起止日期与上下线状态</span>
          <el-button type="danger" @click="openPromotion()">新增活动</el-button>
        </div>
        <el-table :data="promotions">
          <el-table-column prop="title" label="活动名称" min-width="200" />
          <el-table-column prop="startDate" label="开始日期" width="130" />
          <el-table-column prop="endDate" label="结束日期" width="130" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上线' : '下线' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button text type="primary" @click="openPromotion(row)">编辑</el-button>
              <el-button text type="danger" @click="removePromotion(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="秒杀场次" name="session">
        <div class="toolbar">
          <span class="muted">一天拆分为多个时段，前台按当前时间命中场次</span>
          <el-button type="danger" @click="openSession()">新增场次</el-button>
        </div>
        <el-table :data="sessions">
          <el-table-column prop="name" label="场次名称" min-width="180" />
          <el-table-column prop="startTime" label="开始时间" width="140" />
          <el-table-column prop="endTime" label="结束时间" width="140" />
          <el-table-column label="启用" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button text type="primary" @click="openSession(row)">编辑</el-button>
              <el-button text type="danger" @click="removeSession(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="秒杀商品" name="product">
        <div class="toolbar">
          <div class="filters">
            <el-select v-model="filterPromotionId" placeholder="活动" clearable style="width: 180px" @change="loadRelations(1)">
              <el-option v-for="p in promotions" :key="p.id" :label="p.title" :value="p.id" />
            </el-select>
            <el-select v-model="filterSessionId" placeholder="场次" clearable style="width: 160px" @change="loadRelations(1)">
              <el-option v-for="s in sessions" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-button type="primary" @click="loadRelations(1)">查询</el-button>
          </div>
          <el-button type="danger" @click="openRelation">新增秒杀商品</el-button>
        </div>
        <el-table :data="relations">
          <el-table-column prop="productName" label="商品" min-width="200" />
          <el-table-column prop="flashPromotionPrice" label="秒杀价" width="110" />
          <el-table-column prop="flashPromotionCount" label="秒杀数量" width="100" />
          <el-table-column prop="flashPromotionLimit" label="限购" width="80" />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <el-button text type="primary" @click="openRelation(row)">编辑</el-button>
              <el-button text type="danger" @click="removeRelation(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager">
          <el-pagination v-model:current-page="relationPage" :page-size="pageSize" :total="relationTotal"
            layout="total, prev, pager, next" @current-change="loadRelations" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="promotionDialog" :title="promotionForm.id ? '编辑活动' : '新增活动'" width="460px">
      <el-form :model="promotionForm" label-width="90px">
        <el-form-item label="活动名称"><el-input v-model="promotionForm.title" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="promotionForm.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="promotionForm.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="上线"><el-switch v-model="promotionForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="promotionDialog = false">取消</el-button>
        <el-button type="danger" @click="savePromotion">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="sessionDialog" :title="sessionForm.id ? '编辑场次' : '新增场次'" width="460px">
      <el-form :model="sessionForm" label-width="90px">
        <el-form-item label="场次名称"><el-input v-model="sessionForm.name" placeholder="如 08:00-12:00" /></el-form-item>
        <el-form-item label="开始时间"><el-time-picker v-model="sessionForm.startTime" value-format="HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="结束时间"><el-time-picker v-model="sessionForm.endTime" value-format="HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="sessionForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sessionDialog = false">取消</el-button>
        <el-button type="danger" @click="saveSession">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="relationDialog" :title="relationForm.id ? '编辑秒杀商品' : '新增秒杀商品'" width="500px">
      <el-form :model="relationForm" label-width="110px">
        <el-form-item label="活动">
          <el-select v-model="relationForm.flashPromotionId" style="width: 100%">
            <el-option v-for="p in promotions" :key="p.id" :label="p.title" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="场次">
          <el-select v-model="relationForm.flashPromotionSessionId" style="width: 100%">
            <el-option v-for="s in sessions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品">
          <el-select v-model="relationForm.productId" filterable style="width: 100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="秒杀价"><el-input-number v-model="relationForm.flashPromotionPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="秒杀数量"><el-input-number v-model="relationForm.flashPromotionCount" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="每人限购"><el-input-number v-model="relationForm.flashPromotionLimit" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="relationForm.sort" :min="0" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="relationDialog = false">取消</el-button>
        <el-button type="danger" @click="saveRelation">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const tab = ref('promotion')
const loading = ref(true)
const promotions = ref([])
const sessions = ref([])
const relations = ref([])
const relationTotal = ref(0)
const relationPage = ref(1)
const pageSize = ref(10)
const filterPromotionId = ref(null)
const filterSessionId = ref(null)
const products = ref([])
const promotionDialog = ref(false)
const sessionDialog = ref(false)
const relationDialog = ref(false)
const promotionForm = reactive({ id: null, title: '', startDate: '', endDate: '', status: 1 })
const sessionForm = reactive({ id: null, name: '', startTime: '', endTime: '', status: 1 })
const relationForm = reactive({
  id: null, flashPromotionId: null, flashPromotionSessionId: null, productId: null,
  flashPromotionPrice: 0, flashPromotionCount: 10, flashPromotionLimit: 1, sort: 0
})

async function loadBase() {
  promotions.value = await http.get('/admin/flashPromotion/list')
  sessions.value = await http.get('/admin/flashPromotion/session/list')
  if (!filterPromotionId.value && promotions.value.length) filterPromotionId.value = promotions.value[0].id
  await loadRelations(1)
}

async function loadRelations(page = relationPage.value) {
  const data = await http.get('/admin/flashPromotion/product/list', {
    params: {
      pageNum: page, pageSize: pageSize.value,
      flashPromotionId: filterPromotionId.value || undefined,
      flashPromotionSessionId: filterSessionId.value || undefined
    }
  })
  relations.value = data.list
  relationTotal.value = data.total
  relationPage.value = page
}

function openPromotion(row) {
  Object.assign(promotionForm, { id: null, title: '', startDate: '', endDate: '', status: 1 })
  if (row) Object.assign(promotionForm, row)
  promotionDialog.value = true
}

async function savePromotion() {
  await http.post(promotionForm.id ? '/admin/flashPromotion/update' : '/admin/flashPromotion/create', promotionForm)
  promotionDialog.value = false
  ElMessage.success('保存成功')
  loadBase()
}

async function removePromotion(row) {
  await ElMessageBox.confirm(`确定删除活动「${row.title}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/flashPromotion/delete/${row.id}`)
  loadBase()
}

function openSession(row) {
  Object.assign(sessionForm, { id: null, name: '', startTime: '', endTime: '', status: 1 })
  if (row) Object.assign(sessionForm, row)
  sessionDialog.value = true
}

async function saveSession() {
  await http.post(sessionForm.id ? '/admin/flashPromotion/session/update' : '/admin/flashPromotion/session/create', sessionForm)
  sessionDialog.value = false
  ElMessage.success('保存成功')
  loadBase()
}

async function removeSession(row) {
  await ElMessageBox.confirm(`确定删除场次「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/flashPromotion/session/delete/${row.id}`)
  loadBase()
}

function openRelation(row) {
  Object.assign(relationForm, {
    id: null, flashPromotionId: filterPromotionId.value, flashPromotionSessionId: filterSessionId.value,
    productId: null, flashPromotionPrice: 0, flashPromotionCount: 10, flashPromotionLimit: 1, sort: 0
  })
  if (row) Object.assign(relationForm, row)
  relationDialog.value = true
}

async function saveRelation() {
  if (!relationForm.flashPromotionId || !relationForm.flashPromotionSessionId || !relationForm.productId) {
    ElMessage.warning('请选择活动、场次与商品')
    return
  }
  await http.post(relationForm.id ? '/admin/flashPromotion/product/update' : '/admin/flashPromotion/product/create', relationForm)
  relationDialog.value = false
  ElMessage.success('保存成功')
  loadRelations()
}

async function removeRelation(row) {
  await ElMessageBox.confirm('确定删除该秒杀商品吗？', '提示', { type: 'warning' })
  await http.post('/admin/flashPromotion/product/delete', [row.id])
  loadRelations()
}

onMounted(async () => {
  try {
    products.value = await http.get('/admin/product/simpleList')
    await loadBase()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 10px;
  flex-wrap: wrap;
}
.filters {
  display: flex;
  gap: 8px;
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
