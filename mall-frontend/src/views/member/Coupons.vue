<template>
  <div class="mall-card panel" v-loading="loading">
    <div class="panel-head">
      <div class="panel-title">我的优惠券</div>
      <el-radio-group v-model="useStatus" size="small" @change="loadMy">
        <el-radio-button :value="0">未使用</el-radio-button>
        <el-radio-button :value="1">已使用</el-radio-button>
      </el-radio-group>
    </div>

    <div class="coupon-grid">
      <div v-for="coupon in mine" :key="coupon.id" class="coupon-card" :class="{ used: useStatus === 1 || !inTime(coupon) }">
        <div class="left">
          <div class="amount">
            <span v-if="coupon.type === 0">¥{{ coupon.amount }}</span>
            <span v-else>{{ coupon.amount }}<small>折</small></span>
          </div>
          <div class="threshold">满 {{ coupon.minPoint }} 可用</div>
        </div>
        <div class="right">
          <div class="name">{{ coupon.name }}</div>
          <div class="muted">{{ coupon.note }}</div>
          <div class="muted">有效期至 {{ coupon.endTime }}</div>
          <div class="code muted">券码 {{ coupon.couponCode }}</div>
          <div class="status" v-if="useStatus === 1 || !inTime(coupon)">
            {{ useStatus === 1 ? '已使用' : '已过期' }}
          </div>
        </div>
      </div>
      <div v-if="!mine.length" class="empty-block">暂无优惠券</div>
    </div>

    <div class="section-title">领券中心</div>
    <div class="coupon-grid">
      <div v-for="coupon in available" :key="coupon.id" class="coupon-card">
        <div class="left">
          <div class="amount">
            <span v-if="coupon.type === 0">¥{{ coupon.amount }}</span>
            <span v-else>{{ coupon.amount }}<small>折</small></span>
          </div>
          <div class="threshold">满 {{ coupon.minPoint }} 可用</div>
        </div>
        <div class="right">
          <div class="name">{{ coupon.name }}</div>
          <div class="muted">{{ coupon.note }}</div>
          <div class="muted">{{ scopeText(coupon.useType) }} · 有效期至 {{ coupon.endTime }}</div>
          <el-button size="small" type="danger" @click="receive(coupon)">立即领取</el-button>
        </div>
      </div>
      <div v-if="!available.length" class="empty-block">暂无可领取优惠券</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const mine = ref([])
const available = ref([])
const useStatus = ref(0)
const loading = ref(true)

function inTime(coupon) {
  const now = new Date()
  return new Date(coupon.startTime) <= now && new Date(coupon.endTime) >= now
}

function scopeText(type) {
  return ['全场通用', '指定分类', '指定商品'][type] || '全场通用'
}

async function loadMy() {
  loading.value = true
  try {
    mine.value = await http.get('/portal/coupon/my', { params: { useStatus: useStatus.value } })
  } finally {
    loading.value = false
  }
}

async function loadAvailable() {
  available.value = await http.get('/portal/coupon/list')
}

async function receive(coupon) {
  await http.post(`/portal/coupon/receive/${coupon.id}`)
  ElMessage.success('领取成功')
  loadMy()
}

onMounted(() => {
  loadMy()
  loadAvailable()
})
</script>

<style scoped>
.panel {
  padding: 24px;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.panel-title {
  font-weight: 700;
  font-size: 16px;
}
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.coupon-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(90deg, #fff4f2 0%, #fff 45%);
  border: 1px solid #ffe3df;
}
.coupon-card.used {
  filter: grayscale(0.7);
  opacity: 0.7;
}
.left {
  width: 132px;
  padding: 18px 12px;
  text-align: center;
  border-right: 1px dashed #f3c9c5;
  color: #e1251b;
}
.amount {
  font-size: 26px;
  font-weight: 800;
}
.amount small {
  font-size: 13px;
  margin-left: 2px;
}
.threshold {
  font-size: 12px;
  margin-top: 4px;
}
.right {
  flex: 1;
  padding: 14px 16px;
  font-size: 12px;
  line-height: 1.7;
}
.name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
}
.code {
  font-size: 11px;
}
.status {
  color: #e1251b;
  font-weight: 600;
  margin-top: 4px;
}
.section-title {
  font-weight: 700;
  font-size: 16px;
  margin: 28px 0 14px;
}
@media (max-width: 800px) {
  .coupon-grid {
    grid-template-columns: 1fr;
  }
}
</style>
