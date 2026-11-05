<template>
  <div class="admin-shell">
    <aside class="admin-aside">
      <div class="admin-brand">
        <span class="mark">m</span>
        <div>
          <div class="title">mall 管理后台</div>
          <div class="sub">ADMIN CONSOLE</div>
        </div>
      </div>
      <el-menu :default-active="activePath" router class="admin-menu" background-color="#141821" text-color="#c3c8d1" active-text-color="#ffffff">
        <el-menu-item index="/admin/dashboard"><el-icon><HomeFilled /></el-icon><span>首页</span></el-menu-item>
        <el-sub-menu index="pms">
          <template #title><el-icon><Goods /></el-icon><span>商品管理</span></template>
          <el-menu-item index="/admin/products">商品列表</el-menu-item>
          <el-menu-item index="/admin/categories">商品分类</el-menu-item>
          <el-menu-item index="/admin/brands">品牌管理</el-menu-item>
          <el-menu-item index="/admin/attributes">商品属性</el-menu-item>
          <el-menu-item index="/admin/skus">SKU 库存</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="oms">
          <template #title><el-icon><List /></el-icon><span>订单管理</span></template>
          <el-menu-item index="/admin/orders">订单列表</el-menu-item>
          <el-menu-item index="/admin/returns">退货售后</el-menu-item>
          <el-menu-item index="/admin/order-setting">订单设置</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="sms">
          <template #title><el-icon><Present /></el-icon><span>营销管理</span></template>
          <el-menu-item index="/admin/coupons">优惠券</el-menu-item>
          <el-menu-item index="/admin/flash">秒杀活动</el-menu-item>
          <el-menu-item index="/admin/home">首页推荐位</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="ums">
          <template #title><el-icon><Lock /></el-icon><span>权限管理</span></template>
          <el-menu-item index="/admin/admins">管理员</el-menu-item>
          <el-menu-item index="/admin/roles">角色</el-menu-item>
          <el-menu-item index="/admin/menus">菜单</el-menu-item>
          <el-menu-item index="/admin/resources">资源</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="cms">
          <template #title><el-icon><User /></el-icon><span>内容与会员</span></template>
          <el-menu-item index="/admin/subjects">专题管理</el-menu-item>
          <el-menu-item index="/admin/prefrence-areas">优选专区</el-menu-item>
          <el-menu-item index="/admin/member-levels">会员等级</el-menu-item>
          <el-menu-item index="/admin/members">会员列表</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>
    <div class="admin-main">
      <header class="admin-header">
        <div class="crumb">mall 商城 / 管理后台</div>
        <div class="right">
          <router-link to="/" class="to-mall">返回商城</router-link>
          <el-dropdown trigger="hover">
            <span class="admin-user">
              <el-icon><UserFilled /></el-icon>
              {{ info?.admin?.nickName || info?.admin?.username || '管理员' }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <section class="admin-content">
        <router-view />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getAdminInfo, clearAdminAuth } from '../utils/auth'

const route = useRoute()
const info = computed(() => getAdminInfo())
const activePath = computed(() => route.path)

function logout() {
  clearAdminAuth()
  location.href = '/admin/login'
}
</script>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: #f2f4f8;
}

.admin-aside {
  width: 230px;
  background: #141821;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}

.admin-brand {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 20px 18px 18px;
  color: #fff;
}

.admin-brand .mark {
  width: 34px;
  height: 34px;
  background: #e1251b;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 22px;
}

.admin-brand .title {
  font-weight: 700;
  font-size: 15px;
}

.admin-brand .sub {
  color: #6b7280;
  font-size: 10px;
  letter-spacing: 1px;
}

.admin-menu {
  border-right: 0;
}

.admin-menu :deep(.el-menu-item),
.admin-menu :deep(.el-sub-menu__title) {
  height: 42px;
  line-height: 42px;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: #e1251b !important;
  border-radius: 8px;
  margin: 2px 10px;
}

.admin-menu :deep(.el-sub-menu .el-menu-item) {
  height: 38px;
  line-height: 38px;
}

.admin-main {
  flex: 1;
  min-width: 0;
}

.admin-header {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  position: sticky;
  top: 0;
  z-index: 20;
  box-shadow: 0 1px 8px rgba(20, 20, 30, 0.05);
}

.crumb {
  color: #8b8f99;
  font-size: 13px;
}

.right {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 13px;
}

.to-mall {
  color: #8b8f99;
}

.admin-user {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #333;
}

.admin-content {
  padding: 20px 22px 40px;
}

.admin-content :deep(.el-card) {
  border-radius: 12px;
  border: 0;
}

.admin-content :deep(.el-table) {
  --el-table-header-bg-color: #f7f8fa;
}
</style>
