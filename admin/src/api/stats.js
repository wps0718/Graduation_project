import request from '@/utils/request'

/**
 * 统计数据相关接口
 * Dashboard 数据概览页面使用
 */

/**
 * 获取数据总览
 * @returns {Promise<{totalUsers: number, totalProducts: number, totalOrders: number, totalReviews: number, pendingAuthCount: number, pendingProductCount: number, todayNewUsers: number, todayNewProducts: number, todayNewOrders: number, totalAmount: number}>}
 */
export const getOverview = () => {
  return request.get('/admin/stats/overview')
}

/**
 * 获取趋势数据（折线图）
 * @param {number} days - 天数（7或30，默认7）
 * @returns {Promise<Array<{date: string, newUsers: number, newProducts: number, newOrders: number}>>}
 */
export const getTrend = (days = 7) => {
  return request.get('/admin/stats/trend', { params: { days } })
}

/**
 * 获取校区维度统计（柱状图）
 * @returns {Promise<Array<{campusName: string, productCount: number, orderCount: number, userCount: number}>>}
 */
export const getCampusStats = () => {
  return request.get('/admin/stats/campus')
}

/**
 * 获取分类维度统计（饼图）
 * @returns {Promise<Array<{categoryName: string, productCount: number, percentage: number}>>}
 */
export const getCategoryStats = () => {
  return request.get('/admin/stats/category')
}
