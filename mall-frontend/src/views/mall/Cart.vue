<template>
  <div class="mall-container cart-page">
    <div class="page-title">购物车</div>
    <div class="mall-card cart-card" v-loading="loading">
      <div class="cart-head">
        <el-checkbox :model-value="allChecked" @change="toggleAll">全选</el-checkbox>
        <span class="col-name">商品</span>
        <span class="col-price">单价</span>
        <span class="col-qty">数量</span>
        <span class="col-sub">小计</span>
        <span class="col-op">操作</span>
      </div>
      <div v-for="item in items" :key="item.id" class="cart-row">
        <el-checkbox :model-value="selectedIds.includes(item.id)" @change="(v) => toggleOne(item.id, v)" />
        <div class="col-name product-info">
          <img :src="item.pic" :alt="item.productName" />
          <div>
            <router-link :to="`/product/${item.productId}`" class="p-name">{{ item.productName }}</router-link>
            <div class="muted">{{ skuText(item.spData) }}</div>
            <div class="promo" v-if="item.promotionAmount > 0">已优惠 ¥{{ item.promotionAmount }}</div>
          </div>
        </div>
        <div class="col-price">
          <div class="price">{{ item.price }}</div>
          <div class="origin" v-if="item.originalPrice > item.price">¥{{ item.originalPrice }}</div>
        </div>
        <div class="col-qty">
          <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" @change="(v) => updateQty(item, v)" />
        </div>
        <div class="col-sub price">{{ (item.price * item.quantity).toFixed(2) }}</div>
        <div class="col-op">
          <el-button text type="danger" @click="remove([item.id])">删除</el-button>
        </div>
      </div>
      <div v-if="!items.length" class="empty-block">购物车还是空的，去挑几件好物吧~</div>
    </div>

    <div class="cart-footer mall-card" v-if="items.length">
      <div class="left">
        <el-button text @click="remove(selectedIds)" :disabled="!selectedIds.length">删除选中</el-button>
        <el-button text @click="clear">清空购物车</el-button>
      </div>
      <div class="right">
        <span>已选 <b>{{ selectedItems.length }}</b> 件</span>
        <span class="total-label">合计：<span class="price total">{{ totalAmount }}</span></span>
        <el-button type="danger" size="large" :disabled="!selectedItems.length" @click="checkout">去结算</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const items = ref([])
const selectedIds = ref([])
const loading = ref(true)
const router = useRouter()

const selectedItems = computed(() => items.value.filter((i) => selectedIds.value.includes(i.id)))
const allChecked = computed(() => items.value.length > 0 && selectedIds.value.length === items.value.length)
const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0).toFixed(2)
)

function skuText(spData) {
  try {
    return Object.values(JSON.parse(spData || '{}')).join(' / ')
  } catch {
    return ''
  }
}

function toggleAll(value) {
  selectedIds.value = value ? items.value.map((i) => i.id) : []
}

function toggleOne(id, value) {
  if (value) {
    if (!selectedIds.value.includes(id)) selectedIds.value.push(id)
  } else {
    selectedIds.value = selectedIds.value.filter((i) => i !== id)
  }
}

async function load() {
  loading.value = true
  try {
    items.value = await http.get('/portal/cart/list')
    selectedIds.value = items.value.map((i) => i.id)
  } finally {
    loading.value = false
  }
}

async function updateQty(item, value) {
  await http.post('/portal/cart/updateQuantity', null, { params: { id: item.id, quantity: value } })
}

async function remove(ids) {
  if (!ids.length) return
  await http.post('/portal/cart/delete', ids)
  ElMessage.success('已删除')
  load()
}

async function clear() {
  await ElMessageBox.confirm('确定清空购物车吗？', '提示', { type: 'warning' })
  await http.post('/portal/cart/clear')
  load()
}

function checkout() {
  router.push({ path: '/checkout', query: { ids: selectedIds.value.join(',') } })
}

onMounted(load)
</script>

<style scoped>
.cart-page {
  padding-top: 6px;
}
.cart-card {
  overflow: hidden;
}
.cart-head {
  display: grid;
  grid-template-columns: 44px 1fr 130px 150px 120px 90px;
  padding: 16px 20px;
  background: #fafbfc;
  font-size: 13px;
  color: #8b8f99;
  align-items: center;
}
.cart-row {
  display: grid;
  grid-template-columns: 44px 1fr 130px 150px 120px 90px;
  align-items: center;
  padding: 18px 20px;
  border-top: 1px solid #f2f3f6;
}
.product-info {
  display: flex;
  gap: 14px;
  min-width: 0;
}
.product-info img {
  width: 84px;
  height: 84px;
  object-fit: cover;
  border-radius: 10px;
  background: #f4f5f7;
}
.p-name {
  font-weight: 600;
  font-size: 14px;
  display: block;
  margin-bottom: 6px;
}
.promo {
  color: #e1251b;
  font-size: 12px;
  margin-top: 4px;
}
.origin {
  color: #b0b4bd;
  text-decoration: line-through;
  font-size: 12px;
}
.cart-footer {
  margin-top: 16px;
  padding: 14px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.total-label {
  margin: 0 20px;
  font-size: 14px;
}
.total {
  font-size: 26px;
}
@media (max-width: 900px) {
  .cart-head {
    display: none;
  }
  .cart-row {
    grid-template-columns: 30px 1fr;
    gap: 10px;
  }
  .col-price, .col-qty, .col-sub, .col-op {
    grid-column: 2;
    margin-top: 6px;
  }
}
</style>
