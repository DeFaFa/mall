<template>
  <div class="member-shell">
    <div class="mall-container">
      <div class="member-head">
        <div>
          <div class="hello">你好，{{ info?.member?.nickname || '会员' }}</div>
          <div class="muted">
            {{ info?.level?.name || '普通会员' }} · 成长值 {{ info?.member?.growth ?? 0 }} · 积分 {{ info?.member?.integration ?? 0 }}
          </div>
        </div>
        <router-link to="/" class="back">继续逛商城 →</router-link>
      </div>
      <div class="member-body">
        <aside class="member-nav mall-card">
          <router-link
            v-for="item in navs"
            :key="item.path"
            :to="item.path"
            class="nav-link"
            :class="{ active: route.path === item.path }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            {{ item.label }}
          </router-link>
        </aside>
        <section class="member-content">
          <router-view />
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import http from '../utils/request'

const route = useRoute()
const info = ref(null)
const navs = [
  { path: '/member/orders', label: '我的订单', icon: 'List' },
  { path: '/member/address', label: '收货地址', icon: 'Location' },
  { path: '/member/coupons', label: '我的优惠券', icon: 'Ticket' },
  { path: '/member/collections', label: '收藏与足迹', icon: 'Star' },
  { path: '/member/returns', label: '退货记录', icon: 'RefreshLeft' },
  { path: '/member/profile', label: '个人资料', icon: 'User' }
]

async function load() {
  try {
    info.value = await http.get('/portal/member/info')
  } catch (e) {
    info.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.member-shell {
  padding-top: 26px;
}
.member-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(120deg, #1d2129, #313842);
  color: #fff;
  border-radius: 16px;
  padding: 24px 28px;
}
.hello {
  font-size: 22px;
  font-weight: 700;
}
.member-head .muted {
  color: #9aa3b2;
  margin-top: 6px;
}
.back {
  color: #ffb4ae;
  font-size: 14px;
}
.member-body {
  display: grid;
  grid-template-columns: 210px 1fr;
  gap: 18px;
  margin-top: 18px;
}
.member-nav {
  padding: 12px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 14px;
  color: #555;
}
.nav-link.active {
  background: #fff1f0;
  color: #e1251b;
  font-weight: 600;
}
.member-content {
  min-width: 0;
}
@media (max-width: 800px) {
  .member-body {
    grid-template-columns: 1fr;
  }
  .member-nav {
    flex-direction: row;
    overflow-x: auto;
  }
}
</style>
