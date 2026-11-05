<template>
  <div class="home">
    <div class="mall-container">
      <div class="hero" v-loading="loading">
        <el-carousel height="360px" class="banner" indicator-position="outside" arrow="always">
          <el-carousel-item v-for="banner in data.banners" :key="banner.id">
            <router-link :to="banner.url || '/'">
              <img :src="banner.pic" class="banner-img" :alt="banner.name" />
              <div class="banner-caption">
                <span>{{ banner.name }}</span>
              </div>
            </router-link>
          </el-carousel-item>
        </el-carousel>
        <div class="side-ad" v-for="ad in data.recommends" :key="ad.id">
          <router-link :to="ad.url || '/'">
            <img :src="ad.pic" :alt="ad.name" />
            <div class="side-name">{{ ad.name }}</div>
          </router-link>
        </div>
      </div>

      <div class="category-strip mall-card">
        <router-link
          v-for="cat in data.categories"
          :key="cat.id"
          :to="{ path: '/search', query: { categoryId: cat.id } }"
          class="cat-item"
        >
          <span class="cat-name">{{ cat.name }}</span>
          <span class="cat-children">{{ childrenNames(cat) }}</span>
        </router-link>
      </div>

      <div v-if="data.flashProducts?.length" class="section-head">
        <h2>
          限时秒杀
          <span class="session" v-if="data.flashSession">{{ data.flashSession.name }}</span>
        </h2>
        <span class="muted">{{ data.flashPromotion?.title }}</span>
      </div>
      <div v-if="data.flashProducts?.length" class="flash-grid">
        <router-link
          v-for="item in data.flashProducts"
          :key="item.product.id"
          :to="`/product/${item.product.id}`"
          class="flash-card"
        >
          <img :src="item.product.pic" :alt="item.product.name" />
          <div class="flash-body">
            <div class="flash-name">{{ item.product.name }}</div>
            <div class="flash-price">
              <span class="price">{{ item.flashPrice }}</span>
              <span class="origin">¥{{ item.product.price }}</span>
            </div>
            <div class="muted">仅剩 {{ item.flashCount }} 件 · 每人限 {{ item.flashLimit }} 件</div>
          </div>
        </router-link>
      </div>

      <div class="section-head">
        <h2>新品首发</h2>
        <router-link to="/search" class="more">查看全部 →</router-link>
      </div>
      <div class="product-grid">
        <ProductCard v-for="p in data.newProducts" :key="p.id" :product="p" />
      </div>
      <div v-if="!data.newProducts?.length" class="empty-block">暂无新品</div>

      <div class="section-head">
        <h2>人气推荐</h2>
        <router-link to="/search?sort=sale_desc" class="more">查看更多 →</router-link>
      </div>
      <div class="product-grid">
        <ProductCard v-for="p in data.hotProducts" :key="p.id" :product="p" />
      </div>

      <div class="section-head"><h2>品牌精选</h2></div>
      <div class="brand-strip mall-card">
        <router-link
          v-for="brand in data.brands"
          :key="brand.id"
          :to="{ path: '/search', query: { brandId: brand.id } }"
          class="brand-tile"
        >
          <img :src="brand.logo" :alt="brand.name" />
          <span>{{ brand.name }}</span>
        </router-link>
      </div>

      <div class="subject-list" v-if="data.subjects?.length">
        <router-link
          v-for="subject in data.subjects"
          :key="subject.id"
          to="/search"
          class="subject-card"
        >
          <img :src="subject.pic" :alt="subject.name" />
          <div class="subject-meta">
            <div class="subject-title">{{ subject.mainTitle || subject.name }}</div>
            <div class="subject-sub">{{ subject.subTitle }}</div>
          </div>
        </router-link>
      </div>

      <div v-for="area in data.prefrenceAreas" :key="area.area.id" class="pref-area">
        <div class="section-head">
          <h2>{{ area.area.name }}</h2>
          <span class="muted">{{ area.area.subTitle }}</span>
        </div>
        <div class="pref-inner">
          <router-link to="/search" class="pref-cover">
            <img :src="area.area.pic" :alt="area.area.name" />
            <span class="pref-label">{{ area.area.name }}</span>
          </router-link>
          <div class="pref-products">
            <router-link
              v-for="p in area.products.slice(0, 4)"
              :key="p.id"
              :to="`/product/${p.id}`"
              class="mini-product"
            >
              <img :src="p.pic" :alt="p.name" />
              <div class="mini-name">{{ p.name }}</div>
              <div class="price">{{ p.price }}</div>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../../utils/request'
import ProductCard from '../../components/ProductCard.vue'

const loading = ref(true)
const data = ref({
  banners: [],
  recommends: [],
  categories: [],
  brands: [],
  newProducts: [],
  hotProducts: [],
  flashProducts: [],
  subjects: [],
  prefrenceAreas: []
})

function childrenNames(cat) {
  return (cat.children || []).map((c) => c.name).join(' · ')
}

onMounted(async () => {
  try {
    data.value = await http.get('/portal/home/index')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home {
  padding-top: 20px;
}
.hero {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 16px;
}
.banner {
  border-radius: 16px;
  overflow: hidden;
}
.banner-img {
  width: 100%;
  height: 360px;
  object-fit: cover;
}
.banner-caption {
  position: absolute;
  left: 28px;
  bottom: 26px;
  color: #fff;
  font-size: 26px;
  font-weight: 800;
  text-shadow: 0 4px 18px rgba(0, 0, 0, 0.4);
}
.side-ad {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
}
.side-ad img {
  width: 100%;
  height: 172px;
  object-fit: cover;
}
.side-name {
  padding: 10px 14px;
  font-weight: 600;
  font-size: 14px;
}
.category-strip {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 4px;
  padding: 16px 20px;
  margin-top: 18px;
}
.cat-item {
  padding: 10px 12px;
  border-radius: 10px;
  transition: background 0.2s;
}
.cat-item:hover {
  background: #fff5f4;
}
.cat-name {
  display: block;
  font-weight: 600;
  font-size: 15px;
}
.cat-children {
  display: block;
  color: #9aa0aa;
  font-size: 12px;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.session {
  font-size: 12px;
  color: #e1251b;
  background: #fff1f0;
  padding: 3px 8px;
  border-radius: 6px;
  margin-left: 8px;
  font-weight: 500;
}
.flash-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.flash-card {
  display: flex;
  gap: 14px;
  background: #fff;
  border-radius: 14px;
  padding: 14px;
}
.flash-card img {
  width: 110px;
  height: 110px;
  border-radius: 10px;
  object-fit: cover;
}
.flash-body {
  min-width: 0;
}
.flash-name {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8px;
}
.flash-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}
.flash-price .price {
  font-size: 20px;
}
.origin {
  color: #a7abb4;
  text-decoration: line-through;
  font-size: 12px;
}
.brand-strip {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  padding: 18px;
  gap: 14px;
}
.brand-tile {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border-radius: 10px;
  border: 1px solid #f1f2f5;
  font-size: 13px;
}
.brand-tile img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}
.subject-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 32px;
}
.subject-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
}
.subject-card img {
  width: 100%;
  height: 190px;
  object-fit: cover;
}
.subject-meta {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0.55), transparent);
  color: #fff;
  padding: 24px;
} 
.subject-title {
  font-size: 22px;
  font-weight: 800;
}
.subject-sub {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
}
.pref-area {
  margin-top: 8px;
}
.pref-inner {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
}
.pref-cover {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
}
.pref-cover img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}
.pref-label {
  position: absolute;
  left: 18px;
  bottom: 16px;
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
}
.pref-products {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.mini-product {
  background: #fff;
  border-radius: 12px;
  padding: 10px;
}
.mini-product img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
}
.mini-name {
  font-size: 13px;
  margin-top: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }
  .side-ad {
    display: none;
  }
  .category-strip {
    grid-template-columns: repeat(2, 1fr);
  }
  .flash-grid {
    grid-template-columns: 1fr;
  }
  .brand-strip {
    grid-template-columns: repeat(2, 1fr);
  }
  .subject-list {
    grid-template-columns: 1fr;
  }
  .pref-inner {
    grid-template-columns: 1fr;
  }
  .pref-products {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
