<template>
  <div class="dashboard">
    <!-- 区域1：顶部统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" v-loading="loadingOverview">
          <div class="card-content">
            <div class="card-icon" style="background-color: rgba(74, 144, 217, 0.1);">
              <el-icon :size="32" color="#4A90D9"><User /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-number">{{ formatNumber(overview.totalUsers) }}</div>
              <div class="card-title">用户总数</div>
              <div class="card-sub">
                今日新增 <span class="up">+{{ overview.todayNewUsers || 0 }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" v-loading="loadingOverview">
          <div class="card-content">
            <div class="card-icon" style="background-color: rgba(103, 194, 58, 0.1);">
              <el-icon :size="32" color="#67C23A"><Goods /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-number">{{ formatNumber(overview.totalProducts) }}</div>
              <div class="card-title">商品总数</div>
              <div class="card-sub">
                今日新增 <span class="up">+{{ overview.todayNewProducts || 0 }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" v-loading="loadingOverview">
          <div class="card-content">
            <div class="card-icon" style="background-color: rgba(230, 162, 60, 0.1);">
              <el-icon :size="32" color="#E6A23C"><ShoppingCart /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-number">{{ formatNumber(overview.totalOrders) }}</div>
              <div class="card-title">订单总数</div>
              <div class="card-sub">
                今日新增 <span class="up">+{{ overview.todayNewOrders || 0 }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" v-loading="loadingOverview">
          <div class="card-content">
            <div class="card-icon" style="background-color: rgba(155, 89, 182, 0.1);">
              <el-icon :size="32" color="#9B59B6"><Money /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-number">{{ formatAmount(overview.totalAmount) }}</div>
              <div class="card-title">交易总额</div>
              <div class="card-sub">
                累计评价 {{ formatNumber(overview.totalReviews) }} 条
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 区域2：待处理事项提醒 -->
    <el-row :gutter="20" class="alert-row" v-if="!loadingOverview">
      <el-col :span="12">
        <el-alert
          v-if="overview.pendingAuthCount > 0"
          :title="`有 ${overview.pendingAuthCount} 条认证申请待审核`"
          type="warning"
          :closable="false"
          show-icon
          @click="goToAuthReview"
          class="clickable-alert"
        >
          <template #default>
            <el-button link type="warning" @click.stop="goToAuthReview">去审核 →</el-button>
          </template>
        </el-alert>
        <el-alert
          v-else
          title="暂无待审核认证申请"
          type="success"
          :closable="false"
          show-icon
        />
      </el-col>
      <el-col :span="12">
        <el-alert
          v-if="overview.pendingProductCount > 0"
          :title="`有 ${overview.pendingProductCount} 个商品待审核`"
          type="warning"
          :closable="false"
          show-icon
          @click="goToProductReview"
          class="clickable-alert"
        >
          <template #default>
            <el-button link type="warning" @click.stop="goToProductReview">去审核 →</el-button>
          </template>
        </el-alert>
        <el-alert
          v-else
          title="暂无待审核商品"
          type="success"
          :closable="false"
          show-icon
        />
      </el-col>
    </el-row>

    <!-- 区域3：趋势折线图 -->
    <el-row class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card" v-loading="loadingTrend">
          <template #header>
            <div class="card-header">
              <span class="header-title">近期数据趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="handleTrendDaysChange">
                <el-radio-button :label="7">近7天</el-radio-button>
                <el-radio-button :label="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 区域4：底部两列图表 -->
    <el-row :gutter="20" class="bottom-charts">
      <el-col :span="12">
        <el-card class="chart-card" v-loading="loadingCampus">
          <template #header>
            <div class="card-header">
              <span class="header-title">各校区数据分布</span>
            </div>
          </template>
          <div ref="campusChartRef" class="chart-container" style="height: 280px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" v-loading="loadingCategory">
          <template #header>
            <div class="card-header">
              <span class="header-title">商品分类占比</span>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart-container" style="height: 280px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Goods, ShoppingCart, Money } from '@element-plus/icons-vue'
import { getOverview, getTrend, getCampusStats, getCategoryStats } from '@/api/stats'

// ECharts 按需引入
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { LabelLayout } from 'echarts/features'

// 注册 ECharts 组件
echarts.use([
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent,
  CanvasRenderer,
  LabelLayout
])

const router = useRouter()

// ==================== 数据状态 ====================
const loadingOverview = ref(false)
const loadingTrend = ref(false)
const loadingCampus = ref(false)
const loadingCategory = ref(false)

// 概览数据
const overview = ref({
  totalUsers: 0,
  totalProducts: 0,
  totalOrders: 0,
  totalReviews: 0,
  pendingAuthCount: 0,
  pendingProductCount: 0,
  todayNewUsers: 0,
  todayNewProducts: 0,
  todayNewOrders: 0,
  totalAmount: 0
})

// 趋势数据
const trendDays = ref(7)
const trendData = ref([])

// 校区数据
const campusData = ref([])

// 分类数据
const categoryData = ref([])

// ==================== 图表实例 ====================
const trendChartRef = ref(null)
const campusChartRef = ref(null)
const categoryChartRef = ref(null)

let trendChart = null
let campusChart = null
let categoryChart = null

// ==================== 工具函数 ====================

/**
 * 数字千位分隔符格式化
 */
const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 金额格式化（保留2位小数，加¥符号）
 */
const formatAmount = (amount) => {
  if (amount === undefined || amount === null) return '¥0.00'
  return '¥' + parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 跳转认证审核页面
 */
const goToAuthReview = () => {
  router.push('/auth-review')
}

/**
 * 跳转商品审核页面
 */
const goToProductReview = () => {
  router.push('/product/review')
}

// ==================== 数据加载 ====================

/**
 * 加载概览数据
 */
const loadOverview = async () => {
  loadingOverview.value = true
  try {
    const res = await getOverview()
    if (res.code === 1) {
      overview.value = { ...overview.value, ...res.data }
    }
  } catch (error) {
    ElMessage.error('加载概览数据失败')
    console.error('加载概览数据失败:', error)
  } finally {
    loadingOverview.value = false
  }
}

/**
 * 加载趋势数据
 */
const loadTrend = async () => {
  loadingTrend.value = true
  try {
    const res = await getTrend(trendDays.value)
    if (res.code === 1) {
      trendData.value = res.data || []
      initTrendChart()
    }
  } catch (error) {
    ElMessage.error('加载趋势数据失败')
    console.error('加载趋势数据失败:', error)
  } finally {
    loadingTrend.value = false
  }
}

/**
 * 加载校区数据
 */
const loadCampus = async () => {
  loadingCampus.value = true
  try {
    const res = await getCampusStats()
    if (res.code === 1) {
      campusData.value = res.data || []
      initCampusChart()
    }
  } catch (error) {
    ElMessage.error('加载校区数据失败')
    console.error('加载校区数据失败:', error)
  } finally {
    loadingCampus.value = false
  }
}

/**
 * 加载分类数据
 */
const loadCategory = async () => {
  loadingCategory.value = true
  try {
    const res = await getCategoryStats()
    if (res.code === 1) {
      categoryData.value = res.data || []
      initCategoryChart()
    }
  } catch (error) {
    ElMessage.error('加载分类数据失败')
    console.error('加载分类数据失败:', error)
  } finally {
    loadingCategory.value = false
  }
}

/**
 * 趋势天数切换
 */
const handleTrendDaysChange = () => {
  loadTrend()
}

// ==================== 图表初始化 ====================

/**
 * 初始化趋势折线图
 */
const initTrendChart = () => {
  if (!trendChartRef.value) return

  // 如果图表已存在，先销毁
  if (trendChart) {
    trendChart.dispose()
  }

  trendChart = echarts.init(trendChartRef.value)

  const dates = trendData.value.map(item => item.date)
  const newUsers = trendData.value.map(item => item.newUsers)
  const newProducts = trendData.value.map(item => item.newProducts)
  const newOrders = trendData.value.map(item => item.newOrders)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: { color: '#999' }
      }
    },
    legend: {
      data: ['新增用户', '新增商品', '新增订单'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisPointer: { type: 'shadow' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        data: newUsers,
        smooth: true,
        itemStyle: { color: '#4A90D9' },
        lineStyle: { width: 3 }
      },
      {
        name: '新增商品',
        type: 'line',
        data: newProducts,
        smooth: true,
        itemStyle: { color: '#67C23A' },
        lineStyle: { width: 3 }
      },
      {
        name: '新增订单',
        type: 'line',
        data: newOrders,
        smooth: true,
        itemStyle: { color: '#E6A23C' },
        lineStyle: { width: 3 }
      }
    ]
  }

  trendChart.setOption(option)
}

/**
 * 初始化校区柱状图
 */
const initCampusChart = () => {
  if (!campusChartRef.value) return

  if (campusChart) {
    campusChart.dispose()
  }

  campusChart = echarts.init(campusChartRef.value)

  const campusNames = campusData.value.map(item => item.campusName)
  const userCounts = campusData.value.map(item => item.userCount)
  const productCounts = campusData.value.map(item => item.productCount)
  const orderCounts = campusData.value.map(item => item.orderCount)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['用户数', '商品数', '订单数'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: campusNames
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '用户数',
        type: 'bar',
        data: userCounts,
        itemStyle: { color: '#4A90D9' }
      },
      {
        name: '商品数',
        type: 'bar',
        data: productCounts,
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '订单数',
        type: 'bar',
        data: orderCounts,
        itemStyle: { color: '#E6A23C' }
      }
    ]
  }

  campusChart.setOption(option)
}

/**
 * 初始化分类饼图
 */
const initCategoryChart = () => {
  if (!categoryChartRef.value) return

  if (categoryChart) {
    categoryChart.dispose()
  }

  categoryChart = echarts.init(categoryChartRef.value)

  const pieData = categoryData.value.map(item => ({
    name: item.categoryName,
    value: item.productCount
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      itemGap: 15
    },
    series: [
      {
        name: '商品分类',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: pieData
      }
    ]
  }

  categoryChart.setOption(option)
}

/**
 * 窗口大小改变时重绘图表
 */
const handleResize = () => {
  trendChart?.resize()
  campusChart?.resize()
  categoryChart?.resize()
}

// ==================== 生命周期 ====================

onMounted(async () => {
  // 并行加载所有数据
  await Promise.all([
    loadOverview(),
    loadTrend(),
    loadCampus(),
    loadCategory()
  ])

  // 等待 DOM 更新后初始化图表
  await nextTick()
  initTrendChart()
  initCampusChart()
  initCategoryChart()

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  trendChart?.dispose()
  campusChart?.dispose()
  categoryChart?.dispose()

  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

/* 统计卡片样式 */
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.card-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.card-info {
  flex: 1;
}

.card-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 4px;
}

.card-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.card-sub {
  font-size: 12px;
  color: #909399;
}

.card-sub .up {
  color: #67C23A;
  font-weight: 500;
}

/* 提醒条样式 */
.alert-row {
  margin-bottom: 20px;
}

.clickable-alert {
  cursor: pointer;
  transition: opacity 0.2s;
}

.clickable-alert:hover {
  opacity: 0.9;
}

/* 图表区域样式 */
.chart-row {
  margin-bottom: 20px;
}

.bottom-charts {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.chart-container {
  width: 100%;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .stat-cards .el-col {
    margin-bottom: 20px;
  }
}
</style>
