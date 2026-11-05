<template>
  <div class="mall-container checkout-page" v-loading="loading">
    <div class="page-title">确认订单</div>

    <div class="mall-card block" v-if="result">
      <div class="block-title">收货地址</div>
      <div class="address-list">
        <div
          v-for="addr in result.addressList"
          :key="addr.id"
          class="address-card"
          :class="{ active: form.addressId === addr.id }"
          @click="selectAddress(addr.id)"
        >
          <div class="addr-top">
            <strong>{{ addr.name }}</strong>
            <span>{{ addr.phone }}</span>
            <el-tag v-if="addr.defaultStatus === 1" size="small" type="danger">默认</el-tag>
          </div>
          <div class="muted">{{ addr.province }} {{ addr.city }} {{ addr.region }} {{ addr.detailAddress }}</div>
        </div>
        <button class="address-card add" @click="addressDialog = true">
          <el-icon><Plus /></el-icon>
          新增收货地址
        </button>
      </div>
    </div>

    <div class="mall-card block" v-if="result">
      <div class="block-title">商品清单</div>
      <div v-for="item in result.items" :key="item.id" class="item-row">
        <img :src="item.pic" :alt="item.productName" />
        <div class="item-name">
          <div>{{ item.productName }}</div>
          <div class="muted">{{ skuText(item.spData) }}</div>
        </div>
        <div class="muted">x{{ item.quantity }}</div>
        <div class="price">{{ item.price }}</div>
      </div>
    </div>

    <div class="mall-card block" v-if="result">
      <div class="block-title">优惠与积分</div>
      <div class="form-row">
        <span class="label">优惠券</span>
        <el-select
          v-model="form.couponHistoryId"
          clearable
          placeholder="选择可用优惠券"
          style="width: 320px"
          @change="recalc"
        >
          <el-option
            v-for="coupon in result.couponHistoryList"
            :key="coupon.id"
            :label="`${coupon.name}（${coupon.type === 1 ? coupon.amount + '折' : '减¥' + coupon.amount}）`"
            :value="coupon.id"
          />
        </el-select>
        <span class="muted">共有 {{ result.couponHistoryList.length }} 张可用</span>
      </div>
      <div class="form-row">
        <span class="label">使用积分</span>
        <el-input-number
          v-model="form.useIntegration"
          :min="0"
          :max="result.memberIntegration"
          :step="100"
          @change="recalc"
        />
        <span class="muted">当前积分 {{ result.memberIntegration }}，100 积分抵 ¥1，最高可抵商品金额的 10%</span>
      </div>
      <div class="form-row">
        <span class="label">订单备注</span>
        <el-input v-model="form.note" placeholder="选填，例如：请尽快发货" style="max-width: 420px" />
      </div>
    </div>

    <div class="mall-card block amount-block" v-if="result">
      <div class="amount-row"><span>商品总额</span><span>¥{{ result.totalAmount }}</span></div>
      <div class="amount-row"><span>会员/活动优惠</span><span class="minus">-¥{{ result.discountAmount }}</span></div>
      <div class="amount-row"><span>促销优惠</span><span class="minus">-¥{{ result.promotionAmount }}</span></div>
      <div class="amount-row"><span>优惠券</span><span class="minus">-¥{{ result.couponAmount }}</span></div>
      <div class="amount-row"><span>积分抵扣</span><span class="minus">-¥{{ result.integrationAmount }}</span></div>
      <div class="amount-row"><span>运费</span><span>{{ Number(result.freightAmount) === 0 ? '免运费' : '¥' + result.freightAmount }}</span></div>
      <div class="pay-row">
        应付金额：<span class="price">{{ result.payAmount }}</span>
        <el-button type="danger" size="large" :loading="submitting" @click="submit">提交订单</el-button>
      </div>
    </div>

    <el-dialog v-model="addressDialog" title="新增收货地址" width="480px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="addressForm.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addressForm.phone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="addressForm.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="addressForm.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="addressForm.region" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detailAddress" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="addressForm.defaultStatus" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialog = false">取消</el-button>
        <el-button type="danger" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="payDialog" title="支付宝收银台（演示）" width="420px" :close-on-click-modal="false">
      <div class="pay-dialog" v-if="payInfo">
        <el-icon class="pay-icon"><Wallet /></el-icon>
        <div class="pay-amount price">{{ payInfo.payAmount }}</div>
        <div class="muted">订单号 {{ payInfo.orderSn }}</div>
        <div class="muted">演示环境不跳转真实支付宝，点击下方按钮模拟支付回调</div>
      </div>
      <template #footer>
        <el-button @click="laterPay">稍后支付</el-button>
        <el-button type="danger" :loading="paying" @click="mockPay">模拟支付成功</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Wallet } from '@element-plus/icons-vue'
import http from '../../utils/request'

const route = useRoute()
const router = useRouter()
const cartIds = (route.query.ids || '').split(',').filter(Boolean).map(Number)
const loading = ref(true)
const submitting = ref(false)
const result = ref(null)
const addressDialog = ref(false)
const payDialog = ref(false)
const payInfo = ref(null)
const paying = ref(false)
const form = reactive({ addressId: null, couponHistoryId: null, useIntegration: 0, note: '' })
const addressForm = reactive({
  name: '', phone: '', province: '', city: '', region: '', detailAddress: '', defaultStatus: 1
})

function skuText(spData) {
  try {
    return Object.values(JSON.parse(spData || '{}')).join(' / ')
  } catch {
    return ''
  }
}

async function load(keepSelection = false) {
  loading.value = true
  try {
    const data = await http.post('/portal/order/confirm', {
      cartIds,
      addressId: keepSelection ? form.addressId : null,
      couponHistoryId: keepSelection ? form.couponHistoryId : null,
      useIntegration: keepSelection ? form.useIntegration : 0,
      note: form.note
    })
    result.value = data
    if (!keepSelection && data.address) {
      form.addressId = data.address.id
    }
    form.useIntegration = data.useIntegration || 0
  } finally {
    loading.value = false
  }
}

function selectAddress(id) {
  form.addressId = id
  recalc()
}

async function recalc() {
  if (!result.value) return
  try {
    const data = await http.post('/portal/order/confirm', {
      cartIds,
      addressId: form.addressId,
      couponHistoryId: form.couponHistoryId,
      useIntegration: form.useIntegration,
      note: form.note
    })
    result.value = data
    form.useIntegration = data.useIntegration || 0
  } catch (e) {
    form.couponHistoryId = null
    await recalc()
  }
}

async function saveAddress() {
  await http.post('/portal/address/create', addressForm)
  addressDialog.value = false
  ElMessage.success('地址已保存')
  await load(true)
}

async function submit() {
  if (!form.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }
  submitting.value = true
  try {
    const orderId = await http.post('/portal/order/generate', {
      cartIds,
      addressId: form.addressId,
      couponHistoryId: form.couponHistoryId,
      useIntegration: form.useIntegration,
      note: form.note
    })
    payInfo.value = await http.get(`/portal/alipay/pay/${orderId}`)
    payDialog.value = true
  } finally {
    submitting.value = false
  }
}

async function mockPay() {
  paying.value = true
  try {
    await http.post('/portal/alipay/notify', null, {
      params: { orderId: payInfo.value.orderId, tradeNo: 'MOCK' + Date.now() }
    })
    ElMessage.success('支付成功')
    payDialog.value = false
    router.push('/member/orders')
  } finally {
    paying.value = false
  }
}

function laterPay() {
  payDialog.value = false
  ElMessage.info('订单已创建，30 分钟内未支付将自动取消')
  router.push('/member/orders')
}

onMounted(() => load(false))
</script>

<style scoped>
.checkout-page {
  padding-top: 6px;
}
.block {
  padding: 20px 24px;
  margin-bottom: 16px;
}
.block-title {
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 14px;
}
.address-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.address-card {
  width: 300px;
  border: 1px solid #e7e8ec;
  border-radius: 10px;
  padding: 12px 14px;
  cursor: pointer;
  background: #fff;
  text-align: left;
}
.address-card.active {
  border-color: #e1251b;
  background: #fff8f7;
}
.address-card.add {
  border-style: dashed;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #8b8f99;
  min-height: 76px;
}
.addr-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
  font-size: 14px;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-top: 1px solid #f3f4f6;
}
.item-row img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
}
.item-name {
  flex: 1;
  min-width: 0;
  font-size: 14px;
}
.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 12px 0;
  flex-wrap: wrap;
}
.label {
  width: 64px;
  color: #6b7280;
  font-size: 13px;
}
.amount-block {
  padding-bottom: 8px;
}
.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  color: #555;
  font-size: 14px;
  max-width: 420px;
  margin-left: auto;
}
.minus {
  color: #e1251b;
}
.pay-row {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 12px;
  border-top: 1px solid #f3f4f6;
  padding-top: 16px;
  margin-top: 8px;
}
.pay-row .price {
  font-size: 28px;
  margin-right: 8px;
}
.pay-dialog {
  text-align: center;
  padding: 10px 0;
}
.pay-icon {
  font-size: 40px;
  color: #1677ff;
}
.pay-amount {
  font-size: 30px;
  margin: 12px 0;
}
</style>
