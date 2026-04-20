<template>
  <div class="order-manage-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>订单管理</span>
          <div class="toolbar">
            <!-- 搜索框 -->
            <div class="search-box">
              <svg class="search-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                <path d="M966.4 924.8l-230.4-227.2c60.8-67.2 96-156.8 96-256 0-217.6-176-390.4-390.4-390.4-217.6 0-390.4 176-390.4 390.4 0 217.6 176 390.4 390.4 390.4 99.2 0 188.8-35.2 256-96l230.4 227.2c9.6 9.6 28.8 9.6 38.4 0C979.2 950.4 979.2 934.4 966.4 924.8zM102.4 441.6c0-185.6 150.4-339.2 339.2-339.2s339.2 150.4 339.2 339.2c0 89.6-35.2 172.8-92.8 233.6-3.2 0-3.2 3.2-6.4 3.2-3.2 3.2-3.2 3.2-3.2 6.4-60.8 57.6-144 92.8-233.6 92.8C256 780.8 102.4 627.2 102.4 441.6z"/>
              </svg>
              <el-input
                v-model="query.keyword"
                placeholder="搜索订单号/买家昵称/商品标题"
                clearable
                class="search-input"
                @keyup.enter="onSearch"
              />
            </div>

            <!-- 订单状态筛选 -->
            <el-select
              v-model="query.status"
              placeholder="订单状态"
              clearable
              style="width: 120px"
              @change="onFilterChange"
            >
              <el-option label="待面交" :value="1" />
              <el-option label="预留" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已评价" :value="4" />
              <el-option label="已取消" :value="5" />
            </el-select>

            <!-- 时间范围选择 -->
            <el-date-picker
              v-model="query.dateRange"
              type="daterange"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              placeholder="开始日期 / 结束日期"
              clearable
              style="width: 240px"
              @change="onDateChange"
            />

            <!-- 查询按钮 -->
            <el-button type="primary" @click="onQuery">查询</el-button>
          </div>
        </div>
      </template>

      <!-- 订单表格 -->
      <el-table :data="list" border stripe>
        <!-- ID列 -->
        <el-table-column prop="id" label="ID" width="70" align="center" />

        <!-- 订单号列 -->
        <el-table-column label="订单号" width="200">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>

        <!-- 商品列 -->
        <el-table-column label="商品" width="200">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                :src="getImageUrl(row.productCoverImage)"
                :preview-src-list="[getImageUrl(row.productCoverImage)]"
                fit="cover"
                style="width: 40px; height: 40px; border-radius: 4px; flex-shrink: 0"
              />
              <span class="product-title" :title="row.productTitle">
                {{ row.productTitle || '-' }}
              </span>
            </div>
          </template>
        </el-table-column>

        <!-- 买家列 -->
        <el-table-column prop="buyerNickName" label="买家" width="110" align="center" />

        <!-- 卖家列 -->
        <el-table-column prop="sellerNickName" label="卖家" width="110" align="center" />

        <!-- 成交价列 -->
        <el-table-column label="成交价" width="100" align="center">
          <template #default="{ row }">
            <span class="price-text">¥ {{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>

        <!-- 面交校区列 -->
        <el-table-column label="面交校区" width="100" align="center">
          <template #default="{ row }">
            {{ row.campusName || '-' }}
          </template>
        </el-table-column>

        <!-- 订单状态列 -->
        <el-table-column label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 过期时间列 -->
        <el-table-column label="过期时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.status === 1 ? formatTime(row.expireTime) : '-' }}
          </template>
        </el-table-column>

        <!-- 创建时间列 -->
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="720px">
      <div v-if="detail" class="detail-content">
        <!-- 区域一：订单基本信息 -->
        <div class="section-title">订单基本信息</div>
        <el-descriptions border :column="2">
          <el-descriptions-item label="订单号" :span="2">
            <span class="order-no">{{ detail.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusTagType(detail.status)" size="small">
              {{ statusText(detail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="成交价格">
            <span class="price-text">¥ {{ formatPrice(detail.price) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="面交校区">
            {{ detail.campusName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="面交地点">
            {{ detail.meetingPoint || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(detail.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="过期时间">
            {{ detail.status === 1 ? formatTime(detail.expireTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="完成时间">
            {{ detail.completeTime ? formatTime(detail.completeTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="自动确认截止" :span="2">
            {{ formatTime(detail.confirmDeadline) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 区域二：取消信息（仅status=5时显示） -->
        <template v-if="detail.status === 5">
          <el-divider />
          <div class="section-title">取消信息</div>
          <el-descriptions border :column="1">
            <el-descriptions-item label="取消原因">
              {{ detail.cancelReason || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="取消操作人">
              {{ cancelByText(detail.cancelBy, detail.buyerId, detail.sellerId) }}
            </el-descriptions-item>
          </el-descriptions>
        </template>

        <!-- 区域三：商品信息 -->
        <el-divider />
        <div class="section-title">商品信息</div>
        <el-descriptions border :column="2">
          <el-descriptions-item label="商品封面" :span="2">
            <el-image
              :src="getImageUrl(detail.productCoverImage)"
              :preview-src-list="[getImageUrl(detail.productCoverImage)]"
              fit="cover"
              style="width: 80px; height: 80px; border-radius: 6px"
            />
          </el-descriptions-item>
          <el-descriptions-item label="商品标题">
            {{ detail.productTitle || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品分类">
            {{ detail.productCategoryName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品成色">
            {{ conditionLevelText(detail.productConditionLevel) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 区域四：买卖双方信息 -->
        <el-divider />
        <div class="buyer-seller-grid">
          <!-- 买家信息 -->
          <div>
            <div class="section-title">买家信息</div>
            <el-descriptions border :column="1">
              <el-descriptions-item label="昵称">
                {{ detail.buyerNickName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="手机号">
                {{ detail.buyerPhone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="认证状态">
                <el-tag :type="authStatusTagType(detail.buyerAuthStatus)" size="small">
                  {{ authStatusText(detail.buyerAuthStatus) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 卖家信息 -->
          <div>
            <div class="section-title">卖家信息</div>
            <el-descriptions border :column="1">
              <el-descriptions-item label="昵称">
                {{ detail.sellerNickName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="手机号">
                {{ detail.sellerPhone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="认证状态">
                <el-tag :type="authStatusTagType(detail.sellerAuthStatus)" size="small">
                  {{ authStatusText(detail.sellerAuthStatus) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getOrderPage, getOrderDetail } from '@/api/order'

// ==================== 数据定义 ====================

// 查询参数
const query = ref({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: null,
  dateRange: []   // [startTime, endTime]
})
const total = ref(0)
const list = ref([])

// 详情弹窗
const detailVisible = ref(false)
const detail = ref(null)

// ==================== 加载函数 ====================

/**
 * 加载订单列表
 */
const loadList = async () => {
  const params = {
    page: query.value.page,
    pageSize: query.value.pageSize
  }

  // 过滤空值，避免传空字符串给后端
  if (query.value.keyword?.trim()) {
    params.keyword = query.value.keyword.trim()
  }
  if (query.value.status !== null && query.value.status !== '') {
    params.status = query.value.status
  }

  // 时间范围处理
  if (query.value.dateRange && query.value.dateRange.length === 2) {
    params.startTime = query.value.dateRange[0]
    params.endTime = query.value.dateRange[1]
  }

  const res = await getOrderPage(params)
  const page = res.data || {}
  list.value = page.records || []
  total.value = Number(page.total || 0)
}

// ==================== 筛选触发函数 ====================

/**
 * 搜索（回车触发）
 */
const onSearch = () => {
  query.value.page = 1
  loadList()
}

/**
 * 筛选条件变化（状态筛选）
 */
const onFilterChange = () => {
  query.value.page = 1
  loadList()
}

/**
 * 时间范围变化
 */
const onDateChange = () => {
  query.value.page = 1
  loadList()
}

/**
 * 查询按钮点击
 */
const onQuery = () => {
  query.value.page = 1
  loadList()
}

// ==================== 详情函数 ====================

/**
 * 打开订单详情弹窗
 */
const openDetail = async (row) => {
  const res = await getOrderDetail(row.id)
  detail.value = res.data || null
  detailVisible.value = true
}

// ==================== 工具函数 ====================

/**
 * 图片URL处理
 * @param {string} path - 图片路径
 * @returns {string} 完整URL
 */
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path.startsWith('/') ? '' : '/'}${path}`
}

/**
 * 时间格式化
 * @param {string} value - 时间字符串
 * @returns {string} 格式化后的时间
 */
const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

/**
 * 价格格式化（保留2位小数）
 * @param {number} value - 价格
 * @returns {string} 格式化后的价格
 */
const formatPrice = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

/**
 * 订单状态文字映射
 * @param {number} status - 状态值
 * @returns {string} 状态文字
 */
const statusText = (status) => {
  const map = { 1: '待面交', 2: '预留', 3: '已完成', 4: '已评价', 5: '已取消' }
  return map[status] || '未知'
}

/**
 * 订单状态tag类型映射
 * @param {number} status - 状态值
 * @returns {string} tag类型
 */
const statusTagType = (status) => {
  const map = { 1: 'warning', 2: 'info', 3: 'success', 4: '', 5: 'danger' }
  return map[status] ?? 'info'
}

/**
 * 成色文字映射
 * @param {number} level - 成色等级
 * @returns {string} 成色文字
 */
const conditionLevelText = (level) => {
  const map = { 1: '全新', 2: '几乎全新', 3: '9成新', 4: '8成新', 5: '7成新及以下' }
  return map[level] || '-'
}

/**
 * 认证状态文字映射
 * @param {number} authStatus - 认证状态值
 * @returns {string} 认证状态文字
 */
const authStatusText = (authStatus) => {
  const map = { 0: '未认证', 1: '审核中', 2: '已认证', 3: '已驳回' }
  return map[authStatus] ?? '未知'
}

/**
 * 认证状态tag类型映射
 * @param {number} authStatus - 认证状态值
 * @returns {string} tag类型
 */
const authStatusTagType = (authStatus) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[authStatus] ?? 'info'
}

/**
 * 取消操作人文字映射
 * @param {number} cancelBy - 取消操作人ID
 * @param {number} buyerId - 买家ID
 * @param {number} sellerId - 卖家ID
 * @returns {string} 取消操作人文字
 */
const cancelByText = (cancelBy, buyerId, sellerId) => {
  if (cancelBy === 0 || cancelBy === null) return '系统自动取消（超时）'
  if (cancelBy === buyerId) return '买家主动取消'
  if (cancelBy === sellerId) return '卖家主动取消'
  return '未知'
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadList()
})
</script>

<style scoped>
/* 头部行 */
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 工具栏 */
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 搜索框容器 */
.search-box {
  position: relative;
  display: flex;
  align-items: center;
  width: 260px;
}

/* SVG搜索图标 - 融入输入框内部左侧 */
.search-icon {
  position: absolute;
  left: 10px;
  width: 16px;
  height: 16px;
  fill: #909399;
  z-index: 1;
  pointer-events: none;
}

/* 搜索输入框 */
.search-input :deep(.el-input__wrapper) {
  padding-left: 32px;
}

/* 分页 */
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 商品信息列：图片+标题 flex 布局 */
.product-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

/* 订单号等宽字体 */
.order-no {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}

/* 成交价红色加粗 */
.price-text {
  color: #f56c6c;
  font-weight: 600;
}

/* 详情弹窗买卖双方两列布局 */
.buyer-seller-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* 详情区域标题 */
.section-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
}

/* 详情弹窗内容 */
.detail-content {
  padding: 10px 0;
}
</style>
