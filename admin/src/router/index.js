import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      // ✅ 已存在
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        name: 'Dashboard',
        meta: { title: '工作台' }
      },
      // ✅ 已存在
      {
        path: 'auth-review',
        component: () => import('@/views/AuthReview.vue'),
        name: 'AuthReview',
        meta: { title: '认证审核' }
      },

      // ⏳ 待开发 - 开发完成后逐步取消注释
      // {
      //   path: 'product/review',
      //   component: () => import('@/views/product/Review.vue'),
      //   name: 'ProductReview',
      //   meta: { title: '商品审核' }
      // },
      // {
      //   path: 'product/list',
      //   component: () => import('@/views/product/List.vue'),
      //   name: 'ProductList',
      //   meta: { title: '商品列表' }
      // },
      // {
      //   path: 'order',
      //   component: () => import('@/views/order/index.vue'),
      //   name: 'OrderList',
      //   meta: { title: '订单管理' }
      // },
      // {
      //   path: 'user',
      //   component: () => import('@/views/user/index.vue'),
      //   name: 'UserList',
      //   meta: { title: '用户管理' }
      // },
      // {
      //   path: 'report',
      //   component: () => import('@/views/report/index.vue'),
      //   name: 'ReportList',
      //   meta: { title: '举报管理' }
      // },
      // {
      //   path: 'category',
      //   component: () => import('@/views/category/index.vue'),
      //   name: 'CategoryList',
      //   meta: { title: '分类管理' }
      // },
      // {
      //   path: 'campus',
      //   component: () => import('@/views/campus/index.vue'),
      //   name: 'CampusList',
      //   meta: { title: '校区管理' }
      // },
      // {
      //   path: 'college',
      //   component: () => import('@/views/college/index.vue'),
      //   name: 'CollegeList',
      //   meta: { title: '学院管理' }
      // },
      // {
      //   path: 'banner',
      //   component: () => import('@/views/banner/index.vue'),
      //   name: 'BannerList',
      //   meta: { title: 'Banner管理' }
      // },
      // {
      //   path: 'notice',
      //   component: () => import('@/views/notice/index.vue'),
      //   name: 'NoticeList',
      //   meta: { title: '公告管理' }
      // },
      // {
      //   path: 'employee',
      //   component: () => import('@/views/employee/index.vue'),
      //   name: 'EmployeeList',
      //   meta: { title: '员工管理' }
      // }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
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