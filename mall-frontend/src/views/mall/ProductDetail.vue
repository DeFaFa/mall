<template>
  <div class="mall-container detail-page" v-loading="loading">
    <div class="detail-main mall-card" v-if="detail">
      <div class="gallery">
        <img :src="mainPic" :alt="detail.product.name" class="main-img" />
        <div class="thumbs">
          <img
            v-for="(pic, idx) in album"
            :key="idx"
            :src="pic"
            class="thumb"
            :class="{ active: mainPic === pic }"
            @click="mainPic = pic"
          />
        </div>
      </div>
      <div class="info">
        <div class="brand-line">
          <router-link
            v-if="detail.brandName"
            :to="{ path: '/search', query: { brandId: detail.product.brandId } }"
            class="brand-name"
          >{{ detail.brandName }}</router-link>
          <span class="sub-title">{{ detail.product.subTitle }}</span>
        </div>
        <h1>{{ detail.product.name }}</h1>
        <div class="price-box">
          <div class="price-line">
            <span class="price">{{ displayPrice }}</span>
            <span class="origin" v-if="detail.product.originalPrice">¥{{ detail.product.originalPrice }}</span>
            <span class="flash-tag" v-if="detail.flashPrice">秒杀价</span>
          </div>
          <div class="muted">会员权益价以下单结算为准 · 已售 {{ detail.product.sale }} 件</div>
        </div>

        <div class="promo-box" v-if="detail.fullReductionList?.length || detail.ladderList?.length">
          <div v-if="detail.fullReductionList?.length" class="promo-line">
            <span class="tag">满减</span>
            <span v-for="rule in detail.fullReductionList" :key="rule.id" class="promo-item">
              满 ¥{{ rule.fullPrice }} 减 ¥{{ rule.reducePrice }}
            </span>
          </div>
          <div v-if="detail.ladderList?.length" class="promo-line">
            <span class="tag">阶梯价</span>
            <span v-for="rule in detail.ladderList" :key="rule.id" class="promo-item">
              满 {{ rule.count }} 件 ¥{{ rule.price }}/件
            </span>
          </div>
        </div>

        <div class="sku-row" v-if="detail.skuStockList?.length">
          <span class="row-label">规格</span>
          <div class="sku-options">
            <button
              v-for="sku in detail.skuStockList"
              :key="sku.id"
              class="sku-btn"
              :class="{ active: selectedSku?.id === sku.id }"
              @click="selectedSku = sku"
            >
              {{ skuLabel(sku) }}
              <small v-if="sku.stock <= 0">缺货</small>
            </button>
          </div>
        </div>

        <div class="sku-row">
          <span class="row-label">数量</span>
          <el-input-number v-model="quantity" :min="1" :max="selectedSku?.stock || 1" />
          <span class="muted stock">库存 {{ selectedSku?.stock || 0 }}</span>
        </div>

        <div class="actions">
          <el-button size="large" @click="addCart">加入购物车</el-button>
          <el-button size="large" type="danger" @click="buyNow">立即购买</el-button>
          <el-button size="large" text :icon="Star" @click="collect">收藏</el-button>
        </div>

        <div class="coupon-tip" v-if="coupons.length">
          <span class="tag">优惠券</span>
          <span v-for="c in coupons.slice(0, 3)" :key="c.id" class="coupon">
            {{ c.name }}
          </span>
          <router-link to="/member/coupons" class="muted">去领取 →</router-link>
        </div>
      </div>
    </div>

    <div class="detail-bottom mall-card" v-if="detail">
      <el-tabs v-model="tab">
        <el-tab-pane label="商品详情" name="desc">
          <div class="desc">
            <p>{{ detail.product.detailDesc || detail.product.description || '暂无详细描述' }}</p>
            <div class="attr-table">
              <div v-for="param in detail.paramList" :key="param.id" class="attr-row">
                <span class="attr-name">{{ param.name }}</span>
                <span class="attr-value">{{ paramValue(param) }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="规格参数" name="params">
          <div class="attr-table">
            <div v-for="param in detail.paramList" :key="param.id" class="attr-row">
              <span class="attr-name">{{ param.name }}</span>
              <span class="attr-value">{{ paramValue(param) }}</span>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="售后保障" name="service">
          <div class="desc">7 天无理由退货 · 正品保障 · 极速退款 · 30 分钟未支付自动取消订单</div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import http from '../../utils/request'
import { getMemberToken } from '../../utils/auth'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const coupons = ref([])
const selectedSku = ref(null)
const quantity = ref(1)
const loading = ref(true)
const tab = ref('desc')
const mainPic = ref('')

const displayPrice = computed(() => {
  if (!detail.value) return ''
  if (detail.value.flashPrice) return detail.value.flashPrice
  if (selectedSku.value) return selectedSku.value.price
  return detail.value.currentPrice
})

const album = computed(() => {
  if (!detail.value) return []
  const pics = [detail.value.product.pic]
  if (detail.value.product.albumPics) {
    pics.push(...detail.value.product.albumPics.split(',').filter(Boolean))
  }
  return [...new Set(pics)]
})

function skuLabel(sku) {
  try {
    const obj = JSON.parse(sku.spData || '{}')
    return Object.values(obj).join(' / ') || sku.skuCode
  } catch {
    return sku.skuCode
  }
}

function paramValue(param) {
  const value = detail.value?.attributeValueList?.find((v) => v.productAttributeId === param.id)
  return value?.value || ''
}

async function load() {
  loading.value = true
  try {
    detail.value = await http.get(`/portal/product/${route.params.id}`)
    selectedSku.value = detail.value.skuStockList?.[0] || null
    mainPic.value = detail.value.product.pic
    coupons.value = await http.get(`/portal/product/${route.params.id}/coupons`)
  } finally {
    loading.value = false
  }
}

function requireLogin() {
  if (!getMemberToken()) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function addCart() {
  if (!requireLogin()) return
  if (!selectedSku.value || selectedSku.value.stock < 1) {
    ElMessage.warning('该规格暂时缺货')
    return
  }
  await http.post('/portal/cart/add', {
    productId: detail.value.product.id,
    skuId: selectedSku.value.id,
    quantity: quantity.value
  })
  ElMessage.success('已加入购物车')
}

async function buyNow() {
  if (!requireLogin()) return
  await http.post('/portal/cart/add', {
    productId: detail.value.product.id,
    skuId: selectedSku.value.id,
    quantity: quantity.value
  })
  router.push('/cart')
}

async function collect() {
  if (!requireLogin()) return
  await http.post(`/portal/collection/add/${detail.value.product.id}`)
  ElMessage.success('收藏成功')
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.detail-page {
  padding-top: 20px;
}
.detail-main {
  display: grid;
  grid-template-columns: 440px 1fr;
  gap: 32px;
  padding: 26px;
}
.gallery .main-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 12px;
  background: #f4f5f7;
}
.thumbs {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
.thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
}
.thumb.active {
  border-color: #e1251b;
}
.brand-line {
  display: flex;
  gap: 10px;
  align-items: center;
  font-size: 13px;
  color: #8b8f99;
}
.brand-name {
  color: #e1251b;
  font-weight: 600;
}
h1 {
  font-size: 25px;
  line-height: 1.35;
  margin: 10px 0 18px;
}
.price-box {
  background: linear-gradient(90deg, #fff6f5, #fff);
  padding: 16px 18px;
  border-radius: 12px;
}
.price-line {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.price-line .price {
  font-size: 30px;
}
.origin {
  color: #a7abb4;
  text-decoration: line-through;
  font-size: 13px;
}
.flash-tag {
  background: #e1251b;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 5px;
}
.promo-box {
  margin-top: 14px;
  font-size: 13px;
}
.promo-line {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 8px;
  flex-wrap: wrap;
}
.tag {
  background: #fff1f0;
  color: #e1251b;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 12px;
  flex-shrink: 0;
}
.promo-item {
  color: #555;
}
.sku-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 20px;
  flex-wrap: wrap;
}
.row-label {
  width: 44px;
  color: #8b8f99;
  font-size: 13px;
}
.sku-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.sku-btn {
  border: 1px solid #dcdfe6;
  background: #fff;
  padding: 7px 13px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
}
.sku-btn.active {
  border-color: #e1251b;
  color: #e1251b;
  background: #fff5f4;
}
.sku-btn small {
  margin-left: 4px;
  color: #bbb;
}
.stock {
  margin-left: 4px;
}
.actions {
  display: flex;
  gap: 10px;
  margin-top: 28px;
  flex-wrap: wrap;
}
.coupon-tip {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 20px;
  font-size: 12px;
}
.coupon {
  border: 1px dashed #f0a09b;
  color: #e1251b;
  border-radius: 5px;
  padding: 2px 8px;
  background: #fffafa;
}
.detail-bottom {
  margin-top: 18px;
  padding: 10px 24px 24px;
}
.desc {
  line-height: 1.9;
  color: #444;
}
.attr-table {
  max-width: 600px;
  margin-top: 16px;
  border-top: 1px solid #f0f1f4;
}
.attr-row {
  display: flex;
  border-bottom: 1px solid #f0f1f4;
  padding: 10px 0;
  font-size: 13px;
}
.attr-name {
  width: 130px;
  color: #8b8f99;
}
.attr-value {
  flex: 1;
}
@media (max-width: 900px) {
  .detail-main {
    grid-template-columns: 1fr;
    padding: 16px;
  }
}
</style>
