import { createRouter, createWebHistory } from 'vue-router'
import { getAdminToken, getMemberToken } from '../utils/auth'

import MallLayout from '../layouts/MallLayout.vue'
import Home from '../views/mall/Home.vue'
import ProductList from '../views/mall/ProductList.vue'
import ProductDetail from '../views/mall/ProductDetail.vue'
import Cart from '../views/mall/Cart.vue'
import Checkout from '../views/mall/Checkout.vue'
import Login from '../views/mall/Login.vue'
import Register from '../views/mall/Register.vue'
import MemberLayout from '../layouts/MemberLayout.vue'
import MemberProfile from '../views/member/Profile.vue'
import MemberOrders from '../views/member/Orders.vue'
import MemberAddress from '../views/member/Address.vue'
import MemberCoupons from '../views/member/Coupons.vue'
import MemberCollections from '../views/member/Collections.vue'
import MemberReturns from '../views/member/Returns.vue'

import AdminLayout from '../layouts/AdminLayout.vue'
import AdminLogin from '../views/admin/Login.vue'
import Dashboard from '../views/admin/Dashboard.vue'
import AdminProducts from '../views/admin/Products.vue'
import ProductEdit from '../views/admin/ProductEdit.vue'
import Categories from '../views/admin/Categories.vue'
import Brands from '../views/admin/Brands.vue'
import Attributes from '../views/admin/Attributes.vue'
import Skus from '../views/admin/Skus.vue'
import AdminOrders from '../views/admin/Orders.vue'
import AdminReturns from '../views/admin/Returns.vue'
import OrderSetting from '../views/admin/OrderSetting.vue'
import Coupons from '../views/admin/Coupons.vue'
import Flash from '../views/admin/Flash.vue'
import HomeContent from '../views/admin/HomeContent.vue'
import AdminUsers from '../views/admin/AdminUsers.vue'
import Roles from '../views/admin/Roles.vue'
import Menus from '../views/admin/Menus.vue'
import Resources from '../views/admin/Resources.vue'
import Subjects from '../views/admin/Subjects.vue'
import PrefrenceAreas from '../views/admin/PrefrenceAreas.vue'
import MemberLevels from '../views/admin/MemberLevels.vue'
import Members from '../views/admin/Members.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: MallLayout,
      children: [
        { path: '', component: Home },
        { path: 'search', component: ProductList },
        { path: 'product/:id', component: ProductDetail },
        { path: 'cart', component: Cart },
        { path: 'checkout', component: Checkout },
        {
          path: 'member',
          component: MemberLayout,
          redirect: '/member/orders',
          children: [
            { path: 'profile', component: MemberProfile },
            { path: 'orders', component: MemberOrders },
            { path: 'address', component: MemberAddress },
            { path: 'coupons', component: MemberCoupons },
            { path: 'collections', component: MemberCollections },
            { path: 'returns', component: MemberReturns }
          ]
        }
      ]
    },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/admin/login', component: AdminLogin },
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard', component: Dashboard },
        { path: 'products', component: AdminProducts },
        { path: 'product/edit/:id?', component: ProductEdit },
        { path: 'categories', component: Categories },
        { path: 'brands', component: Brands },
        { path: 'attributes', component: Attributes },
        { path: 'skus', component: Skus },
        { path: 'orders', component: AdminOrders },
        { path: 'returns', component: AdminReturns },
        { path: 'order-setting', component: OrderSetting },
        { path: 'coupons', component: Coupons },
        { path: 'flash', component: Flash },
        { path: 'home', component: HomeContent },
        { path: 'admins', component: AdminUsers },
        { path: 'roles', component: Roles },
        { path: 'menus', component: Menus },
        { path: 'resources', component: Resources },
        { path: 'subjects', component: Subjects },
        { path: 'prefrence-areas', component: PrefrenceAreas },
        { path: 'member-levels', component: MemberLevels },
        { path: 'members', component: Members }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.path.startsWith('/admin') && to.path !== '/admin/login' && !getAdminToken()) {
    return '/admin/login'
  }
  if (['/cart', '/checkout', '/member'].some((p) => to.path.startsWith(p)) && !getMemberToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
