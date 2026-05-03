<template>
  <div class="product-review-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>商品审核</span>
          <div class="toolbar">
            <!-- 批量通过按钮 -->
            <el-button
              type="primary"
              plain
              :disabled="!canBatchApprove"
              @click="onBatchApprove"
            >
              {{ selectedRows.length > 0 ? `批量通过（${selectedRows.length}）` : '批量通过' }}
            </el-button>
            
            <!-- 搜索框 -->
            <div class="search-box">
              <svg class="search-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                <path d="M966.4 924.8l-230.4-227.2c60.8-67.2 96-156.8 96-256 0-217.6-176-390.4-390.4-390.4-217.6 0-390.4 176-390.4 390.4 0 217.6 176 390.4 390.4 390.4 99.2 0 188.8-35.2 256-96l230.4 227.2c9.6 9.6 28.8 9.6 38.4 0C979.2 950.4 979.2 934.4 966.4 924.8zM102.4 441.6c0-185.6 150.4-339.2 339.2-339.2s339.2 150.4 339.2 339.2c0 89.6-35.2 172.8-92.8 233.6-3.2 0-3.2 3.2-6.4 3.2-3.2 3.2-3.2 3.2-3.2 6.4-60.8 57.6-144 92.8-233.6 92.8C256 780.8 102.4 627.2 102.4 441.6z"/>
              </svg>
              <el-input
                v-model="query.keyword"
                placeholder="搜索商品标题/发布者昵称"
                clearable
                class="search-input"
                @keyup.enter="onSearch"
              />
            </div>
            
            <!-- 状态筛选 -->
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="onStatusChange">
              <el-option label="全部" value="" />
              <el-option label="待审核" :value="0" />
              <el-option label="在售" :value="1" />
              <el-option label="已下架" :value="2" />
              <el-option label="已售出" :value="3" />
              <el-option label="审核驳回" :value="4" />
            </el-select>
            
            <!-- 查询按钮 -->
            <el-button type="primary" @click="onQuery">查询</el-button>
          </div>
        </div>
      </template>

      <!-- 商品表格 -->
      <el-table
        :data="sortedList"
        border
        @selection-change="onSelectionChange"
      >
        <!-- 多选列 -->
        <el-table-column type="selection" width="55" align="center" />
        
        <!-- 商品ID -->
        <el-table-column prop="id" label="ID" width="90" align="center" />
        
        <!-- 封面图 -->
        <el-table-column label="封面图" width="90" align="center">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.coverImage)"
              :preview-src-list="[getImageUrl(row.coverImage)]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px;"
              hide-on-click-modal
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        
        <!-- 商品标题 -->
        <el-table-column prop="title" label="商品标题" min-width="180" show-overflow-tooltip />
        
        <!-- 价格 -->
        <el-table-column label="价格" width="140" align="center">
          <template #default="{ row }">
            <div class="price-column">
              <div class="price-current">¥{{ row.price }}</div>
              <div class="price-original">原¥{{ row.originalPrice }}</div>
            </div>
          </template>
        </el-table-column>
        
        <!-- 分类 -->
        <el-table-column prop="categoryName" label="分类" width="100" align="center" />
        
        <!-- 成色 -->
        <el-table-column label="成色" width="110" align="center">
          <template #default="{ row }">
            {{ getConditionText(row.conditionLevel) }}
          </template>
        </el-table-column>
        
        <!-- 发布者 -->
        <el-table-column label="发布者" width="140" align="center">
          <template #default="{ row }">
            <div class="publisher-column">
              <div class="publisher-name">{{ row.publisherNickName }}</div>
              <el-tag :type="getAuthStatusType(row.publisherAuthStatus)" size="small">
                {{ getAuthStatusText(row.publisherAuthStatus) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <!-- 发布时间 -->
        <el-table-column label="发布时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <!-- 状态 -->
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 操作 -->
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" plain @click="openDetail(row)">详情</el-button>
            <el-button
              type="success"
              size="small"
              plain
              :disabled="row.status !== 0"
              @click="onApprove(row.id)"
            >
              通过
            </el-button>
            <el-button
              type="danger"
              size="small"
              plain
              :disabled="row.status !== 0"
              @click="openReject(row)"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 商品详情弹窗 -->
    <el-dialog v-model="detailVisible" title="商品详情" width="860px">
      <template v-if="detail">
        <!-- 图片展示区 -->
        <div class="detail-images">
          <div
            v-for="(img, index) in detail.images || []"
            :key="index"
            class="image-item"
          >
            <el-image
              :src="getImageUrl(img)"
              :preview-src-list="getPreviewImages(detail.images)"
              fit="cover"
              style="width: 120px; height: 120px; border-radius: 6px;"
              hide-on-click-modal
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <!-- 封面标签 -->
            <span v-if="img === detail.coverImage" class="cover-tag">封面</span>
          </div>
        </div>

        <!-- 信息展示区 -->
        <el-descriptions border :column="2">
          <el-descriptions-item label="商品标题">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="商品状态">
            <el-tag :type="getStatusType(detail.status)">
              {{ getStatusText(detail.status) }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="售价">
            <span class="price-red">¥{{ detail.price }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="原价">
            <span class="price-gray">¥{{ detail.originalPrice }}</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="商品分类">{{ detail.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="商品成色">{{ getConditionText(detail.conditionLevel) }}</el-descriptions-item>
          
          <el-descriptions-item label="交易校区">{{ detail.campusName }}</el-descriptions-item>
          <el-descriptions-item label="面交地点">{{ detail.meetingPoint || '-' }}</el-descriptions-item>
          
          <el-descriptions-item label="发布者">
            <span>{{ detail.publisherNickName }}</span>
            <el-tag :type="getAuthStatusType(detail.publisherAuthStatus)" size="small" style="margin-left: 8px;">
              {{ getAuthStatusText(detail.publisherAuthStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
          
          <el-descriptions-item label="驳回原因">
            <span :class="detail.status === 4 ? 'reject-reason' : ''">
              {{ detail.status === 4 ? detail.rejectReason : '-' }}
            </span>
          </el-descriptions-item>
          
          <el-descriptions-item label="商品描述" :span="2">
            <div class="description-box">{{ detail.description || '暂无描述' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </template>

      <!-- Tabs: 关联订单 + 发布者信息 -->
      <el-tabs v-model="activeTab" type="border-card" style="margin-top: 16px;" @tab-change="onTabChange">
        <!-- 关联订单 -->
        <el-tab-pane label="关联订单" name="orders">
          <el-table :data="relatedOrders" v-loading="relatedOrdersLoading" stripe>
            <el-table-column prop="orderNo" label="订单ID" width="200" />
            <el-table-column prop="buyerNickName" label="买家" width="120" />
            <el-table-column label="交易价格" width="100">
              <template #default="{ row }">
                <span style="color: #f56c6c">¥{{ Number(row.price).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="订单状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusType(row.status)">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="成交时间" width="170">
              <template #default="{ row }">
                {{ row.completeTime || row.createTime }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openOrderDetail(row.id)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!relatedOrdersLoading && relatedOrders.length === 0" description="暂无关联订单" />
          <el-pagination
            v-if="relatedOrdersTotal > 10"
            v-model:current-page="relatedOrdersPage"
            :page-size="10"
            :total="relatedOrdersTotal"
            layout="total, prev, pager, next"
            @current-change="onRelatedOrdersPageChange"
            style="margin-top: 16px; justify-content: flex-end"
          />
        </el-tab-pane>

        <!-- 发布者信息 -->
        <el-tab-pane label="发布者信息" name="publisher">
          <div v-loading="publisherInfoLoading">
            <template v-if="publisherInfo">
              <!-- 顶部头像+昵称区域 -->
              <div style="display: flex; align-items: center; margin-bottom: 20px;">
                <el-avatar :size="64" :src="publisherInfo.avatarUrl" />
                <div style="margin-left: 16px;">
                  <div style="font-size: 18px; font-weight: bold;">{{ publisherInfo.nickName }}</div>
                  <div style="margin-top: 4px;">
                    <el-tag size="small" :type="publisherInfo.authStatus === 2 ? 'success' : 'info'">
                      {{ publisherInfo.authStatusText }}
                    </el-tag>
                    <el-tag size="small" :type="publisherInfo.accountStatus === 1 ? 'success' : 'danger'" style="margin-left: 8px;">
                      {{ publisherInfo.accountStatusText }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <!-- 详细信息 -->
              <el-descriptions :column="2" border>
                <el-descriptions-item label="手机号">{{ publisherInfo.phone }}</el-descriptions-item>
                <el-descriptions-item label="综合评分">{{ publisherInfo.score }} 分</el-descriptions-item>
                <el-descriptions-item label="个人简介">{{ publisherInfo.bio || '暂无' }}</el-descriptions-item>
                <el-descriptions-item label="IP属地">{{ publisherInfo.ipRegion || '暂无' }}</el-descriptions-item>
                <el-descriptions-item label="注册时间">{{ publisherInfo.createTime }}</el-descriptions-item>
                <el-descriptions-item label="发布商品数">{{ publisherInfo.productCount }}</el-descriptions-item>
                <el-descriptions-item label="成交订单数">{{ publisherInfo.dealOrderCount }}</el-descriptions-item>
              </el-descriptions>
              <!-- 校园认证信息（如果存在） -->
              <template v-if="publisherInfo.realName">
                <div style="margin: 16px 0 8px; font-weight: bold; color: #303133;">校园认证信息</div>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="真实姓名">{{ publisherInfo.realName }}</el-descriptions-item>
                  <el-descriptions-item label="学院">{{ publisherInfo.collegeName || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="学号">{{ publisherInfo.studentNo || '-' }}</el-descriptions-item>
                </el-descriptions>
              </template>
            </template>
            <el-empty v-if="!publisherInfoLoading && !publisherInfo" description="暂无发布者信息" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 订单详情弹窗（关联订单查看） -->
    <el-dialog v-model="orderDetailVisible" title="订单详情" width="720px">
      <div v-if="orderDetail" class="detail-content">
        <el-descriptions border :column="2">
          <el-descriptions-item label="订单号" :span="2">
            <span class="mono-text">{{ orderDetail.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusType(orderDetail.status)" size="small">
              {{ orderDetail.status === 1 ? '待面交' : orderDetail.status === 2 ? '预留' : orderDetail.status === 3 ? '已完成' : orderDetail.status === 4 ? '已评价' : orderDetail.status === 5 ? '已取消' : '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="成交价格">
            <span class="price-red">¥{{ Number(orderDetail.price).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="面交校区">{{ orderDetail.campusName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="面交地点">{{ orderDetail.meetingPoint || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(orderDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ orderDetail.completeTime ? formatTime(orderDetail.completeTime) : '-' }}</el-descriptions-item>
        </el-descriptions>
        <template v-if="orderDetail.status === 5">
          <el-divider />
          <el-descriptions border :column="1">
            <el-descriptions-item label="取消原因">{{ orderDetail.cancelReason || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回商品" width="420px">
      <el-input
        v-model="rejectForm.rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入驳回原因（将通知发布者）"
        maxlength="200"
        show-word-limit
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import {
  getProductPage,
  getProductDetail,
  approveProduct,
  rejectProduct,
  batchApproveProduct,
  getRelatedOrders,
  getPublisherInfo
} from '@/api/product'
import { getOrderDetail } from '@/api/order'

// ========== 查询参数 ==========
const query = ref({
  page: 1,
  pageSize: 10,
  status: '',
  keyword: ''
})

// ========== 列表数据 ==========
const list = ref([])
const total = ref(0)
const selectedRows = ref([])

// ========== 弹窗控制 ==========
const detailVisible = ref(false)
const rejectVisible = ref(false)

// ========== 详情数据 ==========
const detail = ref(null)

// ========== Tabs 状态 ==========
const activeTab = ref('orders')
const tabsLoaded = ref({ orders: false, publisher: false })
const relatedOrdersLoading = ref(false)
const relatedOrders = ref([])
const relatedOrdersTotal = ref(0)
const relatedOrdersPage = ref(1)
const publisherInfoLoading = ref(false)
const publisherInfo = ref(null)

// ========== 订单详情弹窗（关联订单查看） ==========
const orderDetailVisible = ref(false)
const orderDetail = ref(null)

// ========== 驳回表单 ==========
const rejectForm = ref({
  productId: null,
  rejectReason: ''
})

// ========== 排序后的列表（按ID升序） ==========
const sortedList = computed(() => {
  return [...list.value].sort((a, b) => a.id - b.id)
})

// ========== 批量通过按钮可用性判断 ==========
const canBatchApprove = computed(() => {
  // 有勾选且所有勾选项都是待审核状态
  return selectedRows.value.length > 0 && 
         selectedRows.value.every(row => row.status === 0)
})

// ========== 商品状态映射 ==========
const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '在售', 2: '已下架', 3: '已售出', 4: '审核驳回' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: '', 4: 'danger' }
  return map[status]
}

// ========== 成色映射 ==========
const getConditionText = (level) => {
  const map = { 1: '全新', 2: '几乎全新', 3: '9成新', 4: '8成新', 5: '7成新及以下' }
  return map[level] || '未知'
}

// ========== 认证状态映射 ==========
const getAuthStatusText = (status) => {
  const map = { 0: '未认证', 1: '审核中', 2: '已认证', 3: '已驳回' }
  return map[status] || '未知'
}

const getAuthStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status]
}

// ========== 图片URL处理 ==========
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  const baseUrl = 'http://localhost:8080'
  return `${baseUrl}${path.startsWith('/') ? '' : '/'}${path}`
}

// ========== 获取预览图片列表 ==========
const getPreviewImages = (images) => {
  if (!images || !Array.isArray(images)) return []
  return images.map(img => getImageUrl(img))
}

// ========== 时间格式化 ==========
const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

// ========== 加载列表 ==========
const loadList = async () => {
  try {
    // 构造请求参数，过滤空值
    const params = {
      page: query.value.page,
      pageSize: query.value.pageSize
    }
    // status 只有在有实际值时才传递（null、undefined、'' 均不传）
    if (query.value.status !== null && query.value.status !== '' && query.value.status !== undefined) {
      params.status = query.value.status
    }
    // keyword 只有非空字符串时才传递
    if (query.value.keyword && query.value.keyword.trim() !== '') {
      params.keyword = query.value.keyword.trim()
    }

    const res = await getProductPage(params)
    const page = res.data || {}
    list.value = page.records || []
    total.value = Number(page.total || 0)
  } catch (error) {
    // request.js 已处理错误提示
    console.error('加载商品列表失败:', error)
  }
}

// ========== 表格多选变化 ==========
const onSelectionChange = (selection) => {
  selectedRows.value = selection
}

// ========== 状态筛选变化时重置页码 ==========
const onStatusChange = () => {
  query.value.page = 1
  loadList()
}

// ========== 搜索框回车时重置页码 ==========
const onSearch = () => {
  query.value.page = 1
  loadList()
}

// ========== 查询按钮点击时重置页码 ==========
const onQuery = () => {
  query.value.page = 1
  loadList()
}

// ========== 打开详情弹窗 ==========
const openDetail = async (row) => {
  try {
    const res = await getProductDetail(row.id)
    detail.value = res.data || null
    detailVisible.value = true
    // 重置 tab 状态
    activeTab.value = 'orders'
    tabsLoaded.value = { orders: false, publisher: false }
    relatedOrders.value = []
    relatedOrdersTotal.value = 0
    relatedOrdersPage.value = 1
    publisherInfo.value = null
    // 加载关联订单和发布者信息
    loadRelatedOrders(row.id)
    loadPublisherInfo(row.id)
  } catch (error) {
    console.error('加载商品详情失败:', error)
  }
}

// ========== Tab 切换时懒加载 ==========
const onTabChange = (tabName) => {
  if (tabsLoaded.value[tabName] || !detail.value) return
  if (tabName === 'orders') {
    loadRelatedOrders(detail.value.id)
  } else if (tabName === 'publisher') {
    loadPublisherInfo(detail.value.id)
  }
}

// ========== 加载关联订单 ==========
const loadRelatedOrders = async (productId, page) => {
  if (!productId) return
  relatedOrdersLoading.value = true
  const p = page || relatedOrdersPage.value
  try {
    const res = await getRelatedOrders(productId, p, 10)
    const pageData = res.data || {}
    relatedOrders.value = pageData.records || []
    relatedOrdersTotal.value = Number(pageData.total || 0)
    tabsLoaded.value.orders = true
  } catch (error) {
    console.error('加载关联订单失败:', error)
  } finally {
    relatedOrdersLoading.value = false
  }
}

// ========== 加载发布者信息 ==========
const loadPublisherInfo = async (productId) => {
  if (!productId) return
  publisherInfoLoading.value = true
  try {
    const res = await getPublisherInfo(productId)
    publisherInfo.value = res.data || null
    tabsLoaded.value.publisher = true
  } catch (error) {
    console.error('加载发布者信息失败:', error)
  } finally {
    publisherInfoLoading.value = false
  }
}

// ========== 打开订单详情弹窗（关联订单查看） ==========
const openOrderDetail = async (orderId) => {
  try {
    const res = await getOrderDetail(orderId)
    orderDetail.value = res.data || null
    orderDetailVisible.value = true
  } catch (error) {
    console.error('加载订单详情失败:', error)
  }
}

// ========== 订单时间格式化 ==========
const formatOrderTime = (row) => {
  if (row.status === 3 || row.status === 4) {
    return formatTime(row.completeTime)
  } else if (row.status === 5) {
    return formatTime(row.createTime)
  }
  return formatTime(row.createTime)
}

// ========== 订单状态 tag 类型 ==========
const getOrderStatusType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'success', 5: 'info' }
  return map[status] || 'info'
}

// ========== 单个通过 ==========
const onApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定审核通过该商品？', '提示', {
      type: 'warning',
      confirmButtonText: '确认通过',
      cancelButtonText: '取消'
    })
    await approveProduct(id)
    ElMessage.success('审核通过')
    await loadList()
  } catch (error) {
    // 用户取消不处理
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
    }
  }
}

// ========== 批量通过 ==========
const onBatchApprove = async () => {
  const ids = selectedRows.value.map(row => row.id)
  try {
    await ElMessageBox.confirm(`确定批量通过选中的 ${ids.length} 个商品？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认通过',
      cancelButtonText: '取消'
    })
    await batchApproveProduct(ids)
    ElMessage.success(`已通过 ${ids.length} 个商品`)
    await loadList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量通过失败:', error)
    }
  }
}

// ========== 打开驳回弹窗 ==========
const openReject = (row) => {
  rejectForm.value = {
    productId: row.id,
    rejectReason: ''
  }
  rejectVisible.value = true
}

// ========== 确认驳回 ==========
const confirmReject = async () => {
  // 校验驳回原因
  if (!rejectForm.value.rejectReason || !rejectForm.value.rejectReason.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await rejectProduct(rejectForm.value.productId, rejectForm.value.rejectReason.trim())
    ElMessage.success('已驳回')
    rejectVisible.value = false
    await loadList()
  } catch (error) {
    console.error('驳回失败:', error)
  }
}

// ========== 关联订单分页切换 ==========
const onRelatedOrdersPageChange = (page) => {
  relatedOrdersPage.value = page
  loadRelatedOrders(detail.value.id, page)
}

// ========== 页面加载 ==========
onMounted(() => {
  loadList()
})
</script>

<style scoped>
.product-review-page {
  padding: 20px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  width: 220px;
}

.search-icon {
  position: absolute;
  left: 10px;
  width: 16px;
  height: 16px;
  fill: #909399;
  z-index: 1;
  pointer-events: none;
}

.search-input :deep(.el-input__wrapper) {
  padding-left: 32px;
}

/* 价格列样式 */
.price-column {
  line-height: 1.4;
}

.price-current {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}

.price-original {
  color: #909399;
  font-size: 12px;
  text-decoration: line-through;
}

/* 发布者列样式 */
.publisher-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.publisher-name {
  font-size: 13px;
}

/* 图片占位样式 */
.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 20px;
}

/* 分页样式 */
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 详情弹窗样式 */
.detail-images {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.image-item {
  position: relative;
  flex-shrink: 0;
}

.cover-tag {
  position: absolute;
  top: 4px;
  left: 4px;
  background-color: #4A90D9;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.price-red {
  color: #f56c6c;
  font-weight: bold;
}

.price-gray {
  color: #909399;
  text-decoration: line-through;
}

.reject-reason {
  color: #f56c6c;
}

.description-box {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
  color: #606266;
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}

.mono-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}

.detail-content {
  padding: 10px 0;
}
</style>
