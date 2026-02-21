import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/LoginView.vue'),
    hidden: true
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        name: 'Dashboard',
        meta: { title: 'Dashboard', icon: 'dashboard' }
      },
      {
        path: 'product/review',
        component: () => import('@/views/ProductReview.vue'),
        name: 'ProductReview',
        meta: { title: 'Product Review', icon: 'product' }
      },
      {
        path: 'product/list',
        component: () => import('@/views/ProductList.vue'),
        name: 'ProductList',
        meta: { title: 'Product List', icon: 'product' }
      },
      {
        path: 'auth-review',
        component: () => import('@/views/AuthReview.vue'),
        name: 'AuthReview',
        meta: { title: 'Auth Review', icon: 'auth' }
      },
      {
        path: 'user',
        component: () => import('@/views/UserList.vue'),
        name: 'UserList',
        meta: { title: 'User List', icon: 'user' }
      },
      {
        path: 'order',
        component: () => import('@/views/OrderList.vue'),
        name: 'OrderList',
        meta: { title: 'Order List', icon: 'order' }
      },
      {
        path: 'report',
        component: () => import('@/views/ReportList.vue'),
        name: 'ReportList',
        meta: { title: 'Report List', icon: 'report' }
      },
      {
        path: 'category',
        component: () => import('@/views/CategoryList.vue'),
        name: 'CategoryList',
        meta: { title: 'Category List', icon: 'category' }
      },
      {
        path: 'campus',
        component: () => import('@/views/CampusList.vue'),
        name: 'CampusList',
        meta: { title: 'Campus List', icon: 'campus' }
      },
      {
        path: 'college',
        component: () => import('@/views/CollegeList.vue'),
        name: 'CollegeList',
        meta: { title: 'College List', icon: 'college' }
      },
      {
        path: 'banner',
        component: () => import('@/views/BannerList.vue'),
        name: 'BannerList',
        meta: { title: 'Banner List', icon: 'banner' }
      },
      {
        path: 'notice',
        component: () => import('@/views/NoticeList.vue'),
        name: 'NoticeList',
        meta: { title: 'Notice List', icon: 'notice' }
      },
      {
        path: 'employee',
        component: () => import('@/views/EmployeeList.vue'),
        name: 'EmployeeList',
        meta: { title: 'Employee List', icon: 'employee' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const hasToken = getToken()
  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    if (to.path === '/login') {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
