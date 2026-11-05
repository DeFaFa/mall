<template>
  <div class="product-card" @click="$router.push(`/product/${product.id}`)">
    <img class="thumb" :src="product.pic" :alt="product.name" />
    <div class="body">
      <div class="name">{{ product.name }}</div>
      <div class="sub">{{ product.subTitle || product.keywords || '正品保障 · 极速发货' }}</div>
      <div class="foot">
        <span class="price">{{ product.price }}</span>
        <span class="muted">已售 {{ product.sale || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  product: { type: Object, required: true }
})
</script>

<style scoped>
.foot {
  margin-top: 12px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.price {
  font-size: 20px;
}
</style>
