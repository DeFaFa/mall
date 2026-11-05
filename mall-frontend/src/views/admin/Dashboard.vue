<template>
  <div class="dashboard" v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <div class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-sub">{{ card.sub }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span>最新订单</span>
              <router-link to="/admin/orders" class="link">查看全部</router-link>
            </div>
          </template>
          <el-table :data="data.latestOrders || []" size="small">
            <el-table-column prop="orderSn" label="订单号" min-width="200" />
            <el-table-column prop="memberUsername" label="会员" width="120" />
            <el-table-column label="金额" width="100">
              <template #default="{ row }"><span class="price">¥{{ row.payAmount }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="170" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="side-card">
          <template #header><span>运营提示</span></template>
          <div class="tip-item">
            <el-icon color="#e1251b"><Bell /></el-icon>
            <div>
              <strong>30 分钟未支付自动取消</strong>
              <div class="muted">由 RabbitMQ TTL + 死信队列驱动，消费端按订单状态条件更新保证幂等。</div>
            </div>
          </div>
          <div class="tip-item">
            <el-icon color="#e1251b"><Goods /></el-icon>
            <div>
              <strong>待发货 {{ data.unshipped || 0 }} 单</strong>
              <div class="muted">进入订单列表可批量发货。</div>
            </div>
          </div>
          <div class="tip-item">
            <el-icon color="#e1251b"><RefreshLeft /></el-icon>
            <div>
              <strong>待处理退货 {{ data.returnPending || 0 }} 单</strong>
              <div class="muted">在退货售后中审核退款金额。</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import http from '../../utils/request'

const data = ref({})
const loading = ref(true)
const cards = computed(() => [
  { label: '今日订单', value: data.value.todayOrders ?? 0, sub: '累计 ' + (data.value.totalOrders ?? 0) + ' 单' },
  { label: '累计销售额', value: '¥' + Number(data.value.totalSales ?? 0).toFixed(2), sub: '已支付订单' },
  { label: '待付款', value: data.value.unpaid ?? 0, sub: '30 分钟超时自动关闭' },
  { label: '待发货', value: data.value.unshipped ?? 0, sub: '已发货 ' + (data.value.shipped ?? 0) + ' 单' }
])
const statusText = (s) => ['待付款', '待发货', '已发货', '已完成', '已关闭', '无效'][s] || '未知'
const statusType = (s) => ['warning', 'info', 'primary', 'success', 'info', 'info'][s] || 'info'

onMounted(async () => {
  try {
    data.value = await http.get('/admin/dashboard')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 6px 18px rgba(20, 20, 30, 0.05);
}
.stat-label {
  color: #8b8f99;
  font-size: 13px;
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  margin: 8px 0 4px;
}
.stat-sub {
  color: #a7abb4;
  font-size: 12px;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.link {
  color: #e1251b;
  font-size: 13px;
}
.price {
  color: #e1251b;
  font-weight: 600;
}
.tip-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f4f5f7;
  font-size: 13px;
}
.tip-item:last-child {
  border-bottom: 0;
}
.muted {
  color: #9aa0aa;
  margin-top: 4px;
  line-height: 1.6;
}
</style>
