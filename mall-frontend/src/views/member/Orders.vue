<template>
  <div class="mall-card panel">
    <div class="panel-head">
      <div class="panel-title">我的订单</div>
      <el-radio-group v-model="status" size="small" @change="load(1)">
        <el-radio-button :value="''">全部</el-radio-button>
        <el-radio-button :value="0">待付款</el-radio-button>
        <el-radio-button :value="1">待发货</el-radio-button>
        <el-radio-button :value="2">已发货</el-radio-button>
        <el-radio-button :value="3">已完成</el-radio-button>
        <el-radio-button :value="4">已关闭</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading">
      <div v-for="order in list" :key="order.id" class="order-card">
        <div class="order-head">
          <div class="order-meta">
            <span class="sn">订单号 {{ order.orderSn }}</span>
            <span class="muted">{{ order.createTime }}</span>
          </div>
          <el-tag :type="statusType(order.status)" effect="plain">{{ statusText(order.status) }}</el-tag>
        </div>
        <div class="order-body" @click="openDetail(order)">
          <div class="amount">
            <div class="price">{{ order.payAmount }}</div>
            <div class="muted">实付金额</div>
          </div>
          <div class="addr muted">
            {{ order.receiverName }} · {{ order.receiverPhone }}<br />
            {{ order.receiverProvince }} {{ order.receiverCity }} {{ order.receiverRegion }} {{ order.receiverDetailAddress }}
          </div>
        </div>
        <div class="order-foot">
          <span class="muted">点击查看详情</span>
          <div class="ops">
            <el-button v-if="order.status === 0" size="small" type="danger" @click="pay(order)">去支付</el-button>
            <el-button v-if="order.status === 0" size="small" @click="cancel(order)">取消订单</el-button>
            <el-button v-if="order.status === 2" size="small" type="danger" @click="confirmReceive(order)">确认收货</el-button>
            <el-button v-if="[2, 3].includes(order.status)" size="small" @click="openReturn(order)">申请退货</el-button>
            <el-button v-if="[3, 4].includes(order.status)" size="small" @click="remove(order)">删除订单</el-button>
            <el-button size="small" @click="openDetail(order)">查看详情</el-button>
          </div>
        </div>
      </div>
      <div v-if="!list.length && !loading" class="empty-block">暂无订单</div>
    </div>
    <div class="pager">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="payDialog" title="支付宝收银台（演示）" width="400px">
      <div class="pay-box" v-if="payInfo">
        <div class="muted">订单号 {{ payInfo.orderSn }}</div>
        <div class="price pay-amount">{{ payInfo.payAmount }}</div>
        <div class="muted">演示环境点击下方按钮模拟支付成功回调</div>
      </div>
      <template #footer>
        <el-button @click="payDialog = false">稍后支付</el-button>
        <el-button type="danger" :loading="paying" @click="mockPay">模拟支付成功</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="returnDialog" title="申请退货" width="460px">
      <el-form :model="returnForm" label-width="90px">
        <el-form-item label="退货原因">
          <el-select v-model="returnForm.returnReasonId" placeholder="请选择">
            <el-option v-for="r in reasons" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="returnForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="凭证图片">
          <el-input v-model="returnForm.productImg" placeholder="图片 URL，可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialog = false">取消</el-button>
        <el-button type="danger" @click="submitReturn">提交申请</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailDrawer" title="订单详情" size="520px">
      <div v-if="detail">
        <div class="detail-head">
          <el-tag :type="statusType(detail.order.status)">{{ statusText(detail.order.status) }}</el-tag>
          <span class="muted">{{ detail.order.orderSn }}</span>
        </div>
        <div class="detail-items">
          <div v-for="item in detail.orderItemList" :key="item.id" class="detail-item">
            <img :src="item.productPic" />
            <div class="info">
              <div>{{ item.productName }}</div>
              <div class="muted">{{ skuText(item.productAttr) }}</div>
            </div>
            <div class="right">
              <div>¥{{ item.productPrice }} × {{ item.productQuantity }}</div>
              <div class="price">{{ item.realAmount }}</div>
            </div>
          </div>
        </div>
        <div class="amount-lines">
          <div><span>商品总额</span><span>¥{{ detail.order.totalAmount }}</span></div>
          <div><span>会员优惠</span><span>-¥{{ detail.order.discountAmount }}</span></div>
          <div><span>促销优惠</span><span>-¥{{ detail.order.promotionAmount }}</span></div>
          <div><span>优惠券</span><span>-¥{{ detail.order.couponAmount }}</span></div>
          <div><span>积分抵扣</span><span>-¥{{ detail.order.integrationAmount }}</span></div>
          <div><span>运费</span><span>¥{{ detail.order.freightAmount }}</span></div>
          <div class="final"><span>实付</span><span class="price">{{ detail.order.payAmount }}</span></div>
        </div>
        <div class="detail-block">
          <div class="block-title">收货信息</div>
          <div class="muted">{{ detail.order.receiverName }} {{ detail.order.receiverPhone }}</div>
          <div class="muted">{{ detail.order.receiverProvince }} {{ detail.order.receiverCity }} {{ detail.order.receiverRegion }} {{ detail.order.receiverDetailAddress }}</div>
        </div>
        <div class="detail-block" v-if="detail.order.deliverySn">
          <div class="block-title">物流信息</div>
          <div class="muted">{{ detail.order.deliveryCompany }}：{{ detail.order.deliverySn }}</div>
        </div>
        <div class="detail-block">
          <div class="block-title">操作记录</div>
          <el-timeline>
            <el-timeline-item v-for="h in detail.historyList" :key="h.id" :timestamp="h.createTime">
              {{ h.note }} <span class="muted">({{ h.operateMan }})</span>
            </el-timeline-item>
          </el-timeline>
        </div>
        <div class="detail-block" v-if="detail.returnApply">
          <div class="block-title">退货申请</div>
          <div class="muted">状态：{{ returnStatusText(detail.returnApply.status) }} · {{ detail.returnApply.returnReasonName }}</div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(5)
const status = ref('')
const loading = ref(true)
const detailDrawer = ref(false)
const detail = ref(null)
const payDialog = ref(false)
const payInfo = ref(null)
const paying = ref(false)
const returnDialog = ref(false)
const reasons = ref([])
const currentOrder = ref(null)
const returnForm = reactive({ orderId: null, returnReasonId: null, description: '', productImg: '' })

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '待发货', type: 'info' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已关闭', type: 'info' },
  5: { text: '无效', type: 'info' }
}

const statusText = (s) => statusMap[s]?.text || '未知'
const statusType = (s) => statusMap[s]?.type || 'info'
const returnStatusText = (s) => ['待处理', '已同意', '已拒绝', '已完成'][s] || ''

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
    const data = await http.get('/portal/order/list', {
      params: { pageNum: page, pageSize: pageSize.value, status: status.value === '' ? undefined : status.value }
    })
    list.value = data.list
    total.value = data.total
    pageNum.value = page
  } finally {
    loading.value = false
  }
}

async function openDetail(order) {
  detail.value = await http.get(`/portal/order/${order.id}`)
  detailDrawer.value = true
}

async function pay(order) {
  payInfo.value = await http.get(`/portal/alipay/pay/${order.id}`)
  payDialog.value = true
}

async function mockPay() {
  paying.value = true
  try {
    await http.post('/portal/alipay/notify', null, {
      params: { orderId: payInfo.value.orderId, tradeNo: 'MOCK' + Date.now() }
    })
    ElMessage.success('支付成功')
    payDialog.value = false
    load()
  } finally {
    paying.value = false
  }
}

async function cancel(order) {
  await ElMessageBox.confirm('确定取消该订单吗？库存与优惠券将退回。', '提示', { type: 'warning' })
  await http.post(`/portal/order/cancel/${order.id}`)
  ElMessage.success('订单已取消')
  load()
}

async function confirmReceive(order) {
  await ElMessageBox.confirm('确认已经收到商品？', '提示', { type: 'warning' })
  await http.post(`/portal/order/confirmReceive/${order.id}`)
  ElMessage.success('已确认收货')
  load()
}

async function remove(order) {
  await ElMessageBox.confirm('确定删除该订单吗？', '提示', { type: 'warning' })
  await http.post(`/portal/order/delete/${order.id}`)
  load()
}

async function openReturn(order) {
  currentOrder.value = order
  if (!reasons.value.length) reasons.value = await http.get('/portal/return/reasons')
  Object.assign(returnForm, { orderId: order.id, returnReasonId: reasons.value[0]?.id, description: '', productImg: '' })
  returnDialog.value = true
}

async function submitReturn() {
  await http.post('/portal/return/apply', returnForm)
  ElMessage.success('退货申请已提交')
  returnDialog.value = false
  load()
}

onMounted(async () => {
  await load(1)
})
</script>

<style scoped>
.panel {
  padding: 22px 24px 24px;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 10px;
}
.panel-title {
  font-weight: 700;
  font-size: 16px;
}
.order-card {
  border: 1px solid #f0f1f4;
  border-radius: 12px;
  margin-bottom: 14px;
  overflow: hidden;
}
.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafbfc;
  font-size: 13px;
}
.order-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.sn {
  font-weight: 600;
}
.order-body {
  display: flex;
  justify-content: space-between;
  padding: 16px;
  cursor: pointer;
}
.amount .price {
  font-size: 22px;
}
.addr {
  text-align: right;
  line-height: 1.6;
}
.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-top: 1px solid #f5f6f8;
}
.ops {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
.pay-box {
  text-align: center;
  padding: 10px;
}
.pay-amount {
  font-size: 32px;
  margin: 14px 0;
}
.detail-head {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}
.detail-items {
  border-top: 1px solid #f3f4f6;
}
.detail-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}
.detail-item img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}
.detail-item .info {
  flex: 1;
  font-size: 13px;
}
.detail-item .right {
  text-align: right;
  font-size: 13px;
}
.amount-lines {
  margin: 16px 0;
  font-size: 13px;
}
.amount-lines > div {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
}
.amount-lines .final {
  border-top: 1px solid #f3f4f6;
  font-weight: 600;
  padding-top: 10px;
}
.detail-block {
  margin-top: 18px;
}
.block-title {
  font-weight: 600;
  margin-bottom: 8px;
}
</style>
