<template>
  <el-card shadow="never" v-loading="loading" class="setting-card">
    <div class="title">订单设置</div>
    <div class="muted">超时策略同时用于 RabbitMQ 延迟消息（TTL）与业务提示</div>
    <el-form :model="form" label-width="180px" class="form" style="margin-top: 20px">
      <el-form-item label="秒杀订单未支付超时">
        <el-input-number v-model="form.flashOrderOvertime" :min="1" :step="60" />
        <span class="unit">秒</span>
      </el-form-item>
      <el-form-item label="普通订单未支付超时">
        <el-input-number v-model="form.normalOrderOvertime" :min="1" :step="60" />
        <span class="unit">秒（默认 1800 = 30 分钟）</span>
      </el-form-item>
      <el-form-item label="发货后自动收货">
        <el-input-number v-model="form.confirmOvertime" :min="1" />
        <span class="unit">天</span>
      </el-form-item>
      <el-form-item label="完成后自动好评">
        <el-input-number v-model="form.commentOvertime" :min="1" />
        <span class="unit">天</span>
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="save">保存设置</el-button>
      </el-form-item>
    </el-form>
    <div class="note">
      <strong>订单超时取消链路：</strong>
      下单 → 发送 TTL 消息到 <code>mall.order.delay.queue</code> → 过期进入死信交换机
      <code>mall.order.dlx.exchange</code> → <code>mall.order.cancel.queue</code> →
      消费端执行 <code>UPDATE oms_order SET status=4 WHERE id=? AND status=0</code>，
      更新成功才回滚库存/优惠券/积分，重复消息自动忽略。
    </div>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const loading = ref(true)
const form = reactive({
  flashOrderOvertime: 1800, normalOrderOvertime: 1800, confirmOvertime: 15, commentOvertime: 7
})

async function load() {
  loading.value = true
  try {
    Object.assign(form, await http.get('/admin/orderSetting'))
  } finally {
    loading.value = false
  }
}

async function save() {
  await http.post('/admin/orderSetting/update', form)
  ElMessage.success('设置已保存')
}

onMounted(load)
</script>

<style scoped>
.setting-card {
  max-width: 900px;
}
.title {
  font-size: 16px;
  font-weight: 700;
}
.muted {
  color: #9aa0aa;
  font-size: 12px;
  margin-top: 4px;
}
.form {
  max-width: 620px;
}
.unit {
  margin-left: 10px;
  color: #8b8f99;
  font-size: 12px;
}
.note {
  margin-top: 20px;
  background: #f7f8fa;
  padding: 16px;
  border-radius: 10px;
  color: #555;
  font-size: 13px;
  line-height: 1.9;
}
code {
  background: #eceef2;
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 12px;
}
</style>
