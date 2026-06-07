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

/**
 * 获取登录方式统计（饼图）
 * @param {string} startDate - 开始日期 YYYY-MM-DD
 * @param {string} endDate - 结束日期 YYYY-MM-DD
 * @returns {Promise<Array<{methodName: string, count: number}>>}
 */
export const getLoginMethodStats = (startDate, endDate) => {
  return request.get('/admin/stats/login-method', { params: { startDate, endDate } })
}

/**
 * 获取登录时段统计（柱状图）
 * @param {string} startDate - 开始日期 YYYY-MM-DD
 * @param {string} endDate - 结束日期 YYYY-MM-DD
 * @returns {Promise<Array<{timeSlot: string, pcCount: number, miniCount: number}>>}
 */
export const getLoginTimeStats = (startDate, endDate) => {
  return request.get('/admin/stats/login-time', { params: { startDate, endDate } })
}

/**
 * 获取登录与注册趋势（折线图）
 * @param {number} days - 天数（7或30，默认7）
 * @returns {Promise<Array<{date: string, loginCount: number, registerCount: number}>>}
 */
export const getLoginTrend = (days = 7) => {
  return request.get('/admin/stats/login-trend', { params: { days } })
}
