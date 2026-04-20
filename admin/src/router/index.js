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
      // 工作台
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        name: 'Dashboard',
        meta: { title: '工作台' }
      },
      // 审核管理 - 认证审核
      {
        path: 'auth-review',
        component: () => import('@/views/AuthReview.vue'),
        name: 'AuthReview',
        meta: { title: '认证审核' }
      },
      // 审核管理 - 商品审核
      {
        path: 'product',
        children: [
          {
            path: 'review',
            component: () => import('@/views/product/ProductReview.vue'),
            name: 'ProductReview',
            meta: { title: '商品审核' }
          },
          {
            path: 'list',
            component: () => import('@/views/product/ProductList.vue'),
            name: 'ProductList',
            meta: { title: '商品列表' }
          }
        ]
      },
      // 业务管理 - 订单管理
      {
        path: 'order',
        component: () => import('@/views/order/OrderManage.vue'),
        name: 'OrderManage',
        meta: { title: '订单管理' }
      },
      // 业务管理 - 用户管理
      {
        path: 'user',
        component: () => import('@/views/user/UserManage.vue'),
        name: 'UserManage',
        meta: { title: '用户管理' }
      },
      // 业务管理 - 举报管理
      {
        path: 'report',
        component: () => import('@/views/report/ReportManage.vue'),
        name: 'ReportManage',
        meta: { title: '举报处理' }
      },
      // 内容管理 - Banner管理
      {
        path: 'banner',
        component: () => import('@/views/banner/BannerList.vue'),
        name: 'BannerList',
        meta: { title: 'Banner管理' }
      },
      // 内容管理 - 公告管理
      {
        path: 'notice',
        component: () => import('@/views/notice/NoticeList.vue'),
        name: 'NoticeList',
        meta: { title: '公告管理' }
      },
      // 系统管理 - 分类管理
      {
        path: 'category',
        component: () => import('@/views/category/CategoryList.vue'),
        name: 'CategoryList',
        meta: { title: '分类管理' }
      },
      // 系统管理 - 校区管理
      {
        path: 'campus',
        component: () => import('@/views/campus/CampusList.vue'),
        name: 'CampusList',
        meta: { title: '校区管理' }
      },
      // 系统管理 - 学院管理
      {
        path: 'college',
        component: () => import('@/views/college/CollegeList.vue'),
        name: 'CollegeList',
        meta: { title: '学院管理' }
      },
      // 系统管理 - 员工管理
      {
        path: 'employee',
        component: () => import('@/views/employee/EmployeeList.vue'),
        name: 'EmployeeList',
        meta: { title: '员工管理' }
      }
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