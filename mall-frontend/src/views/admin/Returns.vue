<template>
  <el-card shadow="never" v-loading="loading">
    <el-tabs v-model="tab">
      <el-tab-pane label="退货申请" name="apply">
        <div class="toolbar">
          <div class="filters">
            <el-select v-model="status" placeholder="审核状态" clearable style="width: 130px">
              <el-option label="待处理" :value="0" />
              <el-option label="已同意" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
            <el-input v-model="keyword" placeholder="订单号 / 收货人" clearable style="width: 200px" />
            <el-button type="primary" @click="loadApply(1)">查询</el-button>
          </div>
        </div>

        <el-table :data="applies">
          <el-table-column prop="orderSn" label="订单号" min-width="190" />
          <el-table-column prop="returnName" label="申请人" width="110" />
          <el-table-column prop="returnPhone" label="手机号" width="130" />
          <el-table-column prop="returnReasonName" label="退货原因" width="140" />
          <el-table-column label="退款金额" width="110">
            <template #default="{ row }"><span class="price">¥{{ row.refundAmount }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="['warning', 'success', 'danger', 'info'][row.status]">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="170" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button text type="primary" @click="openHandle(row)">{{ row.status === 0 ? '审核' : '查看' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager">
          <el-pagination v-model:current-page="applyPage" :page-size="pageSize" :total="applyTotal"
            layout="total, prev, pager, next" @current-change="loadApply" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="退货原因" name="reason">
        <div class="toolbar">
          <span class="muted">供前台申请退货时选择</span>
          <el-button type="danger" @click="openReason()">新增原因</el-button>
        </div>
        <el-table :data="reasons">
          <el-table-column prop="name" label="原因名称" min-width="200" />
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column label="启用" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button text type="primary" @click="openReason(row)">编辑</el-button>
              <el-button text type="danger" @click="removeReason(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="handleDialog" title="退货审核" width="500px">
      <el-descriptions :column="1" border size="small" v-if="current">
        <el-descriptions-item label="订单号">{{ current.orderSn }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ current.returnName }} · {{ current.returnPhone }}</el-descriptions-item>
        <el-descriptions-item label="退货原因">{{ current.returnReasonName }}</el-descriptions-item>
        <el-descriptions-item label="问题描述">{{ current.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="申请退款金额">¥{{ current.refundAmount }}</el-descriptions-item>
        <el-descriptions-item label="处理意见" v-if="current.handleNote">{{ current.handleNote }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-if="current?.status === 0" label-width="90px" style="margin-top: 14px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="handleForm.status">
            <el-radio :value="1">同意</el-radio>
            <el-radio :value="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退款金额" v-if="handleForm.status === 1">
          <el-input-number v-model="handleForm.refundAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="处理备注"><el-input v-model="handleForm.note" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialog = false">关闭</el-button>
        <el-button v-if="current?.status === 0" type="danger" @click="submitHandle">提交审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reasonDialog" :title="reasonForm.id ? '编辑退货原因' : '新增退货原因'" width="420px">
      <el-form :model="reasonForm" label-width="90px">
        <el-form-item label="原因名称"><el-input v-model="reasonForm.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="reasonForm.sort" :min="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="reasonForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reasonDialog = false">取消</el-button>
        <el-button type="danger" @click="saveReason">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const tab = ref('apply')
const loading = ref(true)
const applies = ref([])
const applyTotal = ref(0)
const applyPage = ref(1)
const pageSize = ref(10)
const status = ref(null)
const keyword = ref('')
const reasons = ref([])
const handleDialog = ref(false)
const current = ref(null)
const handleForm = reactive({ id: null, status: 1, note: '', refundAmount: 0 })
const reasonDialog = ref(false)
const reasonForm = reactive({ id: null, name: '', sort: 0, status: 1 })
const statusText = (s) => ['待处理', '已同意', '已拒绝', '已完成'][s] || '未知'

async function loadApply(page = applyPage.value) {
  loading.value = true
  try {
    const data = await http.get('/admin/returnApply/list', {
      params: {
        pageNum: page, pageSize: pageSize.value, status: status.value ?? undefined,
        keyword: keyword.value || undefined
      }
    })
    applies.value = data.list
    applyTotal.value = data.total
    applyPage.value = page
  } finally {
    loading.value = false
  }
}

async function loadReasons() {
  reasons.value = await http.get('/admin/returnReason/list')
}

async function openHandle(row) {
  current.value = row
  Object.assign(handleForm, { id: row.id, status: 1, note: '', refundAmount: Number(row.refundAmount) })
  handleDialog.value = true
}

async function submitHandle() {
  await http.post('/admin/returnApply/handle', handleForm)
  ElMessage.success('审核完成')
  handleDialog.value = false
  loadApply()
}

function openReason(row) {
  Object.assign(reasonForm, { id: null, name: '', sort: 0, status: 1 })
  if (row) Object.assign(reasonForm, row)
  reasonDialog.value = true
}

async function saveReason() {
  if (!reasonForm.name) {
    ElMessage.warning('请填写原因名称')
    return
  }
  await http.post(reasonForm.id ? '/admin/returnReason/update' : '/admin/returnReason/create', reasonForm)
  ElMessage.success('保存成功')
  reasonDialog.value = false
  loadReasons()
}

async function removeReason(row) {
  await ElMessageBox.confirm(`确定删除原因「${row.name}」吗？`, '提示', { type: 'warning' })
  await http.post(`/admin/returnReason/delete/${row.id}`)
  loadReasons()
}

onMounted(async () => {
  await Promise.all([loadApply(1), loadReasons()])
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 10px;
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
