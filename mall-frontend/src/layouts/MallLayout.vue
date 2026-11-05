<template>
  <div class="mall-shell">
    <header class="mall-header">
      <div class="mall-container head-inner">
        <router-link to="/" class="logo">
          <span class="logo-mark">m</span>
          <span class="logo-text">mall 商城</span>
        </router-link>
        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="搜索商品、品牌"
            clearable
            @keyup.enter="goSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="goSearch" />
            </template>
          </el-input>
        </div>
        <div class="head-actions">
          <router-link v-if="!member" to="/login" class="head-link">登录</router-link>
          <router-link v-if="!member" to="/register" class="head-link">注册</router-link>
          <el-dropdown v-else trigger="hover">
            <span class="head-link">{{ member.nickname || member.username }} <el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/member/orders')">会员中心</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <router-link to="/cart" class="cart-link">
            <el-icon><ShoppingCart /></el-icon>
            购物车
          </router-link>
          <router-link to="/admin" class="admin-link">后台管理</router-link>
        </div>
      </div>
      <div class="mall-container nav-row">
        <router-link to="/" class="nav-home">首页</router-link>
        <router-link
          v-for="cat in categories"
          :key="cat.id"
          :to="{ path: '/search', query: { categoryId: cat.id } }"
          class="nav-item"
        >{{ cat.name }}</router-link>
      </div>
    </header>
    <main>
      <router-view />
    </main>
    <footer class="mall-footer">
      <div class="mall-container">
        <div class="footer-line">mall 商城 · Spring Boot + Vue + MySQL + Redis + RabbitMQ</div>
        <div class="footer-sub">演示项目 · 订单 30 分钟未支付自动取消（TTL + 死信队列）</div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import http from '../utils/request'
import { getMemberInfo, clearMemberAuth } from '../utils/auth'

const router = useRouter()
const keyword = ref('')
const categories = ref([])
const member = computed(() => getMemberInfo())

onMounted(async () => {
  try {
    const tree = await http.get('/portal/product/category/tree')
    categories.value = tree.slice(0, 7)
  } catch (e) {
    categories.value = []
  }
})

function goSearch() {
  router.push({ path: '/search', query: { keyword: keyword.value || undefined } })
}

function logout() {
  clearMemberAuth()
  location.reload()
}
</script>

<style scoped>
.mall-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6f8;
}

.mall-header {
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 30;
  box-shadow: 0 2px 12px rgba(20, 20, 30, 0.05);
}

.head-inner {
  display: flex;
  align-items: center;
  gap: 26px;
  padding: 14px 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.logo-mark {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: #e1251b;
  color: #fff;
  font-size: 24px;
  font-weight: 800;
  display: grid;
  place-items: center;
  line-height: 1;
}

.logo-text {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.3px;
}

.search-box {
  flex: 1;
  max-width: 520px;
}

.search-box :deep(.el-input-group__append) {
  background: #e1251b;
  border-color: #e1251b;
  color: #fff;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 14px;
  color: #555;
}

.head-link {
  cursor: pointer;
}

.cart-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #e1251b;
  font-weight: 600;
}

.admin-link {
  color: #8b8f99;
  font-size: 12px;
}

.nav-row {
  display: flex;
  gap: 26px;
  padding-bottom: 12px;
  font-size: 14px;
  color: #444;
  overflow-x: auto;
}

.nav-home {
  color: #e1251b;
  font-weight: 600;
}

.nav-item:hover {
  color: #e1251b;
}

main {
  flex: 1;
  padding-bottom: 40px;
}

.mall-footer {
  background: #16181d;
  color: #9ca3af;
  padding: 28px 0;
  font-size: 13px;
}

.footer-line {
  color: #fff;
  font-weight: 600;
  margin-bottom: 6px;
}

@media (max-width: 900px) {
  .head-inner {
    flex-wrap: wrap;
    gap: 12px;
  }
  .search-box {
    order: 3;
    flex-basis: 100%;
    max-width: none;
  }
  .admin-link {
    display: none;
  }
}
</style>
