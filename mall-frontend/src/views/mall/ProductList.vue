<template>
  <div class="mall-container list-page">
    <div class="filter-bar mall-card">
      <div class="filter-row">
        <span class="label">分类</span>
        <el-select v-model="query.categoryId" clearable placeholder="全部分类" class="w180" @change="search(1)">
          <el-option v-for="c in flatCategories" :key="c.id" :label="c.label" :value="c.id" />
        </el-select>
        <span class="label">品牌</span>
        <el-select v-model="query.brandId" clearable placeholder="全部品牌" class="w160" @change="search(1)">
          <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
        <span class="label">价格</span>
        <el-input-number v-model="query.priceMin" :min="0" :controls="false" placeholder="最低" class="w100" />
        <span class="dash">-</span>
        <el-input-number v-model="query.priceMax" :min="0" :controls="false" placeholder="最高" class="w100" />
        <span class="label">排序</span>
        <el-select v-model="query.sort" class="w150" @change="search(1)">
          <el-option label="综合" value="" />
          <el-option label="销量优先" value="sale_desc" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
        </el-select>
        <el-button type="danger" @click="search(1)">筛选</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <div class="result-head">
      <span v-if="query.keyword">“{{ query.keyword }}” 的搜索结果</span>
      <span v-else>全部商品</span>
      <span class="muted">共 {{ total }} 件</span>
    </div>

    <div class="product-grid" v-loading="loading">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>
    <div v-if="!loading && !products.length" class="empty-block">没有找到相关商品</div>

    <div class="pager">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../../utils/request'
import ProductCard from '../../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()
const products = ref([])
const brands = ref([])
const categoryTree = ref([])
const flatCategories = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const loading = ref(false)
const query = ref({
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null,
  brandId: route.query.brandId ? Number(route.query.brandId) : null,
  sort: route.query.sort || '',
  priceMin: null,
  priceMax: null
})

function flatten(tree, level = 0, result = []) {
  for (const c of tree) {
    result.push({ id: c.id, label: (level ? '　'.repeat(level) : '') + c.name })
    if (c.children?.length) flatten(c.children, level + 1, result)
  }
  return result
}

async function load() {
  loading.value = true
  try {
    const page = await http.get('/portal/product/search', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: query.value.keyword || undefined,
        categoryId: query.value.categoryId || undefined,
        brandId: query.value.brandId || undefined,
        sort: query.value.sort || undefined,
        priceMin: query.value.priceMin || undefined,
        priceMax: query.value.priceMax || undefined
      }
    })
    products.value = page.list
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function search(page = 1) {
  pageNum.value = page
  router.replace({
    path: '/search',
    query: {
      keyword: query.value.keyword || undefined,
      categoryId: query.value.categoryId || undefined,
      brandId: query.value.brandId || undefined,
      sort: query.value.sort || undefined
    }
  })
  load()
}

function reset() {
  query.value = { keyword: '', categoryId: null, brandId: null, sort: '', priceMin: null, priceMax: null }
  search(1)
}

watch(
  () => route.query,
  (q) => {
    query.value.keyword = q.keyword || ''
    query.value.categoryId = q.categoryId ? Number(q.categoryId) : null
    query.value.brandId = q.brandId ? Number(q.brandId) : null
    query.value.sort = q.sort || ''
    pageNum.value = 1
    load()
  }
)

onMounted(async () => {
  categoryTree.value = await http.get('/portal/product/category/tree')
  flatCategories.value = flatten(categoryTree.value)
  brands.value = await http.get('/portal/product/brand/list')
  load()
})
</script>

<style scoped>
.list-page {
  padding-top: 18px;
}
.filter-bar {
  padding: 16px 18px;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.label {
  color: #6b7280;
  font-size: 13px;
  margin-left: 6px;
}
.w180 { width: 180px; }
.w160 { width: 150px; }
.w150 { width: 150px; }
.w100 { width: 100px; }
.dash { color: #bbb; }
.result-head {
  display: flex;
  justify-content: space-between;
  margin: 22px 4px 14px;
  font-size: 14px;
}
.product-grid {
  min-height: 200px;
}
.pager {
  display: flex;
  justify-content: center;
  margin: 26px 0;
}
</style>
