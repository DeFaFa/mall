<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="filters.orderSn" placeholder="订单号" clearable style="width: 190px" />
        <el-select v-model="filters.status" placeholder="订单状态" clearable style="width: 120px">
          <el-option v-for="(v, k) in statusMap" :key="k" :label="v.text" :value="Number(k)" />
        </el-select>
        <el-input v-model="filters.receiverKeyword" placeholder="收货人 / 手机号" clearable style="width: 160px" />
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 250px" />
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <div class="actions">
        <el-button :disabled="!selection.length" @click="openDelivery">批量发货</el-button>
        <el-button :disabled="!selection.length" @click="closeOrders">批量关闭</el-button>
        <el-button type="danger" plain :disabled="!selection.length" @click="deleteOrders">批量删除</el-button>
      </div>
    </div>

    <el-table :data="list" @selection-change="(rows) => (selection = rows)">
      <el-table-column type="selection" width="44" />
      <el-table-column prop="orderSn" label="订单号" min-width="200" />
      <el-table-column prop="memberUsername" label="会员" width="120" />
      <el-table-column label="金额" width="110">
        <template #default="{ row }"><span class="price">¥{{ row.payAmount }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收货人" min-width="170">
        <template #default="{ row }">{{ row.receiverName }} · {{ row.receiverPhone }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status === 1" text @click="openDeliveryFor(row)">发货</el-button>
          <el-button v-if="[1, 2, 3].includes(row.status)" text type="danger" @click="openReturnFor(row)">退货</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="load" />
    </div>

    <el-dialog v-model="deliveryDialog" title="订单发货" width="460px">
      <el-form label-width="90px">
        <el-form-item label="物流公司"><el-input v-model="deliveryForm.deliveryCompany" placeholder="如 顺丰速运" /></el-form-item>
        <el-form-item label="物流单号"><el-input v-model="deliveryForm.deliverySn" /></el-form-item>
      </el-form>
      <div class="muted">仅待发货订单会被处理，共 {{ deliveryForm.ids.length }} 单</div>
      <template #footer>
        <el-button @click="deliveryDialog = false">取消</el-button>
        <el-button type="danger" @click="submitDelivery">确认发货</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailDrawer" title="订单详情" size="560px">
      <div v-if="detail">
        <div class="detail-head">
          <el-tag :type="statusMap[detail.order.status]?.type">{{ statusMap[detail.order.status]?.text }}</el-tag>
          <span class="muted">{{ detail.order.orderSn }}</span>
        </div>
        <div class="item-list">
          <div v-for="item in detail.orderItemList" :key="item.id" class="item">
            <img :src="item.productPic" />
            <div class="info">
              <div>{{ item.productName }}</div>
              <div class="muted">{{ skuText(item.productAttr) }}</div>
            </div>
            <div class="right">
              <div>¥{{ item.productPrice }} × {{ item.productQuantity }}</div>
              <div class="price">¥{{ item.realAmount }}</div>
            </div>
          </div>
        </div>
        <div class="money-lines">
          <div><span>商品总额</span><span>¥{{ detail.order.totalAmount }}</span></div>
          <div><span>会员优惠</span><span>-¥{{ detail.order.discountAmount }}</span></div>
          <div><span>促销优惠</span><span>-¥{{ detail.order.promotionAmount }}</span></div>
          <div><span>优惠券</span><span>-¥{{ detail.order.couponAmount }}</span></div>
          <div><span>积分抵扣</span><span>-¥{{ detail.order.integrationAmount }}</span></div>
          <div><span>运费</span><span>¥{{ detail.order.freightAmount }}</span></div>
          <div class="final"><span>实付</span><span class="price">¥{{ detail.order.payAmount }}</span></div>
        </div>
        <div class="block">
          <div class="block-title">收货信息 <el-button text type="primary" @click="openReceiver">修改</el-button></div>
          <div class="muted">{{ detail.order.receiverName }} {{ detail.order.receiverPhone }}</div>
          <div class="muted">{{ detail.order.receiverProvince }} {{ detail.order.receiverCity }} {{ detail.order.receiverRegion }} {{ detail.order.receiverDetailAddress }}</div>
        </div>
        <div class="block" v-if="detail.order.deliverySn">
          <div class="block-title">物流信息</div>
          <div class="muted">{{ detail.order.deliveryCompany }}：{{ detail.order.deliverySn }}</div>
        </div>
        <div class="block">
          <div class="block-title">订单备注 <el-button text type="primary" @click="openNote">修改</el-button></div>
          <div class="muted">{{ detail.order.note || '无' }}</div>
        </div>
        <div class="block">
          <div class="block-title">费用调整 <el-button text type="primary" @click="openMoney">修改</el-button></div>
          <div class="muted">运费 ¥{{ detail.order.freightAmount }} · 客单价调整 ¥{{ detail.order.discountAmount }}</div>
        </div>
        <div class="block" v-if="detail.returnApply">
          <div class="block-title">退货申请</div>
          <div class="muted">{{ ['待处理', '已同意', '已拒绝'][detail.returnApply.status] }} · {{ detail.returnApply.returnReasonName }} · 退款 ¥{{ detail.returnApply.refundAmount }}</div>
        </div>
        <div class="block">
          <div class="block-title">操作流水</div>
          <el-timeline>
            <el-timeline-item v-for="h in detail.historyList" :key="h.id" :timestamp="h.createTime">
              {{ h.note }} <span class="muted">({{ h.operateMan }})</span>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="receiverDialog" title="修改收货人" width="480px">
      <el-form :model="receiverForm" label-width="90px">
        <el-form-item label="收货人"><el-input v-model="receiverForm.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="receiverForm.receiverPhone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="receiverForm.receiverProvince" /></el-form-item>
        <el-form-item label="市"><el-input v-model="receiverForm.receiverCity" /></el-form-item>
        <el-form-item label="区"><el-input v-model="receiverForm.receiverRegion" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="receiverForm.receiverDetailAddress" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiverDialog = false">取消</el-button>
        <el-button type="danger" @click="saveReceiver">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="moneyDialog" title="调整订单费用" width="440px">
      <el-form label-width="100px">
        <el-form-item label="运费"><el-input-number v-model="moneyForm.freightAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="优惠金额"><el-input-number v-model="moneyForm.discountAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moneyDialog = false">取消</el-button>
        <el-button type="danger" @click="saveMoney">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="noteDialog" title="修改备注" width="440px">
      <el-input v-model="noteForm.note" type="textarea" :rows="3" />
      <template #footer>
        <el-button @click="noteDialog = false">取消</el-button>
        <el-button type="danger" @click="saveNote">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '待发货', type: 'info' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已关闭', type: 'info' },
  5: { text: '无效', type: 'info' }
}

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(true)
const selection = ref([])
const dateRange = ref(null)
const filters = reactive({ orderSn: '', status: null, receiverKeyword: '' })
const detailDrawer = ref(false)
const detail = ref(null)
const deliveryDialog = ref(false)
const deliveryForm = reactive({ ids: [], deliveryCompany: '顺丰速运', deliverySn: '' })
const receiverDialog = ref(false)
const receiverForm = reactive({})
const moneyDialog = ref(false)
const moneyForm = reactive({ freightAmount: 0, discountAmount: 0 })
const noteDialog = ref(false)
const noteForm = reactive({ note: '' })

function skuText(value) {
  try {
    return Object.values(JSON.parse(value || '{}')).join(' / ')
  } catch {
    return value || ''
  }
}

async function load(page = pageNum.value) {
  loading.value = true
  try {
    const data = await http.get('/admin/order/list', {
      params: {
        pageNum: page, pageSize: pageSize.value,
        orderSn: filters.orderSn || undefined,
        status: filters.status ?? undefined,
        receiverKeyword: filters.receiverKeyword || undefined,
        startDate: dateRange.value?.[0],
        endDate: dateRange.value?.[1]
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
  filters.orderSn = ''
  filters.status = null
  filters.receiverKeyword = ''
  dateRange.value = null
  load(1)
}

async function openDetail(row) {
  detail.value = await http.get(`/admin/order/${row.id}`)
  detailDrawer.value = true
}

function openDelivery() {
  deliveryForm.ids = selection.value.map((r) => r.id)
  deliveryForm.deliverySn = ''
  deliveryDialog.value = true
}

function openDeliveryFor(row) {
  deliveryForm.ids = [row.id]
  deliveryForm.deliverySn = ''
  deliveryDialog.value = true
}

async function submitDelivery() {
  if (!deliveryForm.deliverySn) {
    ElMessage.warning('请填写物流单号')
    return
  }
  await http.post('/admin/order/delivery', deliveryForm)
  ElMessage.success('发货完成')
  deliveryDialog.value = false
  load()
}

async function closeOrders() {
  await ElMessageBox.confirm('确定关闭选中的订单吗？已支付订单会退回库存。', '提示', { type: 'warning' })
  await http.post('/admin/order/close', selection.value.map((r) => r.id))
  ElMessage.success('已关闭')
  load()
}

async function deleteOrders() {
  await ElMessageBox.confirm('删除为逻辑删除，确定继续？', '提示', { type: 'warning' })
  await http.post('/admin/order/delete', selection.value.map((r) => r.id))
  load()
}

function openReceiver() {
  Object.assign(receiverForm, {
    orderId: detail.value.order.id,
    receiverName: detail.value.order.receiverName,
    receiverPhone: detail.value.order.receiverPhone,
    receiverProvince: detail.value.order.receiverProvince,
    receiverCity: detail.value.order.receiverCity,
    receiverRegion: detail.value.order.receiverRegion,
    receiverDetailAddress: detail.value.order.receiverDetailAddress
  })
  receiverDialog.value = true
}

async function saveReceiver() {
  await http.post('/admin/order/updateReceiver', receiverForm)
  ElMessage.success('已更新')
  receiverDialog.value = false
  openDetail({ id: receiverForm.orderId })
  load()
}

function openMoney() {
  moneyForm.orderId = detail.value.order.id
  moneyForm.freightAmount = Number(detail.value.order.freightAmount)
  moneyForm.discountAmount = Number(detail.value.order.discountAmount)
  moneyDialog.value = true
}

async function saveMoney() {
  await http.post('/admin/order/updateMoney', {
    orderId: detail.value.order.id,
    freightAmount: moneyForm.freightAmount,
    discountAmount: moneyForm.discountAmount
  })
  ElMessage.success('已更新')
  moneyDialog.value = false
  openDetail({ id: detail.value.order.id })
  load()
}

function openNote() {
  noteForm.orderId = detail.value.order.id
  noteForm.note = detail.value.order.note
  noteDialog.value = true
}

async function saveNote() {
  await http.post('/admin/order/updateNote', { orderId: detail.value.order.id, note: noteForm.note })
  ElMessage.success('已更新')
  noteDialog.value = false
  openDetail({ id: detail.value.order.id })
  load()
}

async function openReturnFor(row) {
  await ElMessageBox.alert('退货审核请前往「退货售后」页面处理', '提示')
}

onMounted(() => load(1))
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
.actions {
  display: flex;
  gap: 6px;
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
.detail-head {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;
}
.item-list {
  border-top: 1px solid #f3f4f6;
}
.item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}
.item img {
  width: 58px;
  height: 58px;
  object-fit: cover;
  border-radius: 8px;
}
.item .info {
  flex: 1;
  font-size: 13px;
}
.item .right {
  text-align: right;
  font-size: 13px;
}
.money-lines {
  margin: 16px 0;
  font-size: 13px;
}
.money-lines > div {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
}
.money-lines .final {
  border-top: 1px solid #f3f4f6;
  padding-top: 10px;
  font-weight: 600;
}
.block {
  margin-top: 18px;
}
.block-title {
  font-weight: 600;
  margin-bottom: 8px;
}
</style>
