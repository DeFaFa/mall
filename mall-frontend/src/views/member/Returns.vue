<template>
  <div class="mall-card panel" v-loading="loading">
    <div class="panel-title">退货记录</div>
    <div v-for="item in list" :key="item.id" class="return-card">
      <div class="top">
        <div>
          <div class="sn">订单号 {{ item.orderSn }}</div>
          <div class="muted">申请时间 {{ item.createTime }}</div>
        </div>
        <el-tag :type="statusType(item.status)">{{ statusText(item.status) }}</el-tag>
      </div>
      <div class="body">
        <div>退货原因：{{ item.returnReasonName }}</div>
        <div class="muted">问题描述：{{ item.description || '无' }}</div>
        <div class="muted" v-if="item.handleNote">处理意见：{{ item.handleNote }}</div>
        <div class="muted" v-if="item.handleTime">处理时间：{{ item.handleTime }}</div>
      </div>
      <div class="foot">
        <span>退款金额：<span class="price">{{ item.refundAmount }}</span></span>
      </div>
    </div>
    <div v-if="!list.length" class="empty-block">暂无退货记录</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../../utils/request'

const list = ref([])
const loading = ref(true)
const statusText = (s) => ['待处理', '已同意', '已拒绝', '已完成'][s] || '未知'
const statusType = (s) => ['warning', 'success', 'danger', 'info'][s] || 'info'

onMounted(async () => {
  try {
    list.value = await http.get('/portal/return/list')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.panel {
  padding: 24px;
}
.panel-title {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 16px;
}
.return-card {
  border: 1px solid #f0f1f4;
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
}
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafbfc;
}
.sn {
  font-weight: 600;
  font-size: 13px;
}
.body {
  padding: 14px 16px;
  font-size: 13px;
  line-height: 1.9;
}
.foot {
  padding: 10px 16px;
  border-top: 1px solid #f5f6f8;
  text-align: right;
  font-size: 13px;
}
</style>
