<template>
  <div class="product-list-page">
    <!-- 筛选栏 -->
    <div class="filter-container">
      <el-form :model="queryParams" inline class="filter-form">
        <!-- 第一行：筛选条件 -->
        <el-row :gutter="20" style="width: 100%; margin-bottom: 20px;">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" clearable placeholder="全部" style="width: 140px" @change="onFilterChange">
                <el-option label="全部" value="" />
                <el-option label="待审核" :value="0" />
                <el-option label="在售" :value="1" />
                <el-option label="已下架" :value="2" />
                <el-option label="已售出" :value="3" />
                <el-option label="审核驳回" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="分类">
              <el-select v-model="queryParams.categoryId" clearable placeholder="全部" style="width: 140px" @change="onFilterChange">
                <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="价格">
              <div class="price-range">
                <el-input-number v-model="queryParams.minPrice" :min="0" :precision="2" controls-position="right" placeholder="最低价" style="width: 110px" @change="onFilterChange" />
                <span>-</span>
                <el-input-number v-model="queryParams.maxPrice" :min="0" :precision="2" controls-position="right" placeholder="最高价" style="width: 110px" @change="onFilterChange" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="发布时间">
              <el-date-picker v-model="queryParams.dateRange" type="daterange" unlink-panels value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px" @change="onFilterChange" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第二行：排序 + 搜索 + 操作按钮 -->
        <el-row :gutter="16" style="width: 100%; align-items: center;">
          <el-col :span="18" class="left-actions">
            <el-form-item label="排序">
              <el-select v-model="queryParams.sortBy" style="width: 160px" @change="onFilterChange">
                <el-option label="发布时间倒序" value="default" />
                <el-option label="发布时间最新" value="createTime-desc" />
                <el-option label="浏览量最高" value="viewCount-desc" />
                <el-option label="价格高→低" value="price-desc" />
                <el-option label="价格低→高" value="price-asc" />
              </el-select>
            </el-form-item>

            <el-input v-model="queryParams.keyword" placeholder="搜索商品标题/发布者昵称" clearable style="width: 280px" @keyup.enter="onSearch">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>

            <el-button type="primary" @click="onQuery">查询</el-button>
            <el-button @click="onReset">重置</el-button>
          </el-col>

          <el-col :span="6" class="right-actions">
            <el-button type="danger" :disabled="!canBatchForceOff" @click="onBatchForceOff">
              {{ selectedRows.length > 0 ? `批量强制下架（${selectedRows.length}）` : '批量强制下架' }}
            </el-button>
            <el-button type="primary" plain @click="onExport">导出</el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <el-card>
        <template #header>
          <span class="page-title">商品列表</span>
        </template>

        <el-table :data="list" border stripe @selection-change="onSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="ID" width="90" align="center" :index="1" />

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

          <el-table-column prop="title" label="商品标题" width="200" show-overflow-tooltip />

          <el-table-column label="价格" width="140" align="center">
            <template #default="{ row }">
              <div class="price-column">
                <div class="price-current">¥{{ row.price }}</div>
                <div class="price-original">原¥{{ row.originalPrice }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="categoryName" label="分类" width="100" align="center" />

          <el-table-column label="成色" width="110" align="center">
            <template #default="{ row }">
              {{ getConditionText(row.conditionLevel) }}
            </template>
          </el-table-column>

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

          <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />

          <el-table-column label="发布时间" width="160" align="center" sortable>
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>

          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" plain @click="openDetail(row)">详情</el-button>
              <el-button type="info" size="small" plain :disabled="row.status !== 0" @click="openReview(row)">
                审核
              </el-button>
              <el-button type="danger" size="small" plain :disabled="row.status !== 1" @click="openForceOff(row)">
                强制下架
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadList"
            @current-change="loadList"
          />
        </div>
    </el-card>

    <!-- 商品详情弹窗 -->
    <el-dialog v-model="detailVisible" title="商品详情" width="920px">
      <template v-if="currentProductDetail">
        <div class="detail-images">
          <div v-for="(img, index) in currentProductDetail.images || []" :key="index" class="image-item">
            <el-image
              :src="getImageUrl(img)"
              :preview-src-list="getPreviewImages(currentProductDetail.images)"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 6px;"
              hide-on-click-modal
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <span v-if="img === currentProductDetail.coverImage" class="cover-tag">封面</span>
          </div>
        </div>

        <el-descriptions border :column="2">
          <el-descriptions-item label="商品ID">{{ currentProductDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="商品状态">
            <el-tag :type="getStatusType(currentProductDetail.status)">
              {{ getStatusText(currentProductDetail.status) }}
            </el-tag>
          </el-descriptions-item>

          <el-descriptions-item label="商品标题" :span="2">{{ currentProductDetail.title }}</el-descriptions-item>

          <el-descriptions-item label="售价">
            <span class="price-red">¥{{ currentProductDetail.price }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="原价">
            <span class="price-gray">¥{{ currentProductDetail.originalPrice }}</span>
          </el-descriptions-item>

          <el-descriptions-item label="商品分类">{{ currentProductDetail.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品成色">{{ getConditionText(currentProductDetail.conditionLevel) }}</el-descriptions-item>

          <el-descriptions-item label="交易校区">{{ currentProductDetail.campusName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="面交地点">{{ currentProductDetail.meetingPointName || currentProductDetail.meetingPointText || '-' }}</el-descriptions-item>

          <el-descriptions-item label="发布者">
            <span>{{ currentProductDetail.publisherNickName }}</span>
            <el-tag
              :type="getAuthStatusType(currentProductDetail.publisherAuthStatus)"
              size="small"
              style="margin-left: 8px;"
            >
              {{ getAuthStatusText(currentProductDetail.publisherAuthStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatTime(currentProductDetail.createTime) }}</el-descriptions-item>

          <el-descriptions-item label="驳回原因">
            <span :class="currentProductDetail.status === 4 ? 'reject-reason' : ''">
              {{ currentProductDetail.status === 4 ? currentProductDetail.rejectReason : '-' }}
            </span>
          </el-descriptions-item>

          <el-descriptions-item label="商品描述" :span="2">
            <div class="description-box">{{ currentProductDetail.description || '暂无描述' }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <el-tabs v-model="detailActiveTab" style="margin-top: 16px;" @tab-change="onDetailTabChange">
          <el-tab-pane label="关联订单" name="orders">
            <el-table :data="orderList" border stripe size="small">
              <el-table-column prop="orderNo" label="订单ID" width="200" />
              <el-table-column prop="buyerNickName" label="买家" width="100" />
              <el-table-column prop="sellerName" label="卖家" width="100" />
              <el-table-column label="交易价格" width="100">
                <template #default="{ row }">
                  <span class="price-red">¥{{ Number(row.price).toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="订单状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getOrderStatusType(row.status)">{{ row.statusText }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="成交时间" width="170">
                <template #default="{ row }">
                  {{ formatTime(row.completeTime || row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="openOrderDetail(row.orderId)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="orderTotal === 0" style="padding: 16px 0;">
              <el-empty description="暂无订单" />
            </div>

            <div class="pager">
              <el-pagination
                v-model:current-page="orderQuery.page"
                v-model:page-size="orderQuery.pageSize"
                :total="orderTotal"
                :page-sizes="[5]"
                layout="total, prev, pager, next"
                @current-change="loadOrderList"
              />
            </div>
          </el-tab-pane>

          <el-tab-pane label="发布者信息" name="publisher">
            <div v-loading="publisherInfoLoading">
              <template v-if="publisherInfo">
                <!-- 顶部头像+昵称区域 -->
                <div style="display: flex; align-items: center; margin-bottom: 20px;">
                  <el-avatar :size="64" :src="getImageUrl(publisherInfo.avatarUrl)" />
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
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="orderDetailVisible" title="订单详情" width="720px">
      <div v-if="orderDetail" class="detail-content">
        <!-- 区域一：订单基本信息 -->
        <div class="section-title">订单基本信息</div>
        <el-descriptions border :column="2">
          <el-descriptions-item label="订单号" :span="2">
            <span class="mono-text">{{ orderDetail.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusType(orderDetail.status)" size="small">
              {{ getOrderStatusText(orderDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="成交价格">
            <span class="price-red">¥{{ Number(orderDetail.price).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="面交校区">{{ orderDetail.campusName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="面交地点">{{ orderDetail.meetingPoint || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(orderDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="过期时间">{{ orderDetail.status === 1 ? formatTime(orderDetail.expireTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ orderDetail.completeTime ? formatTime(orderDetail.completeTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="自动确认截止">{{ formatTime(orderDetail.confirmDeadline) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 区域二：取消信息（仅status=5时显示） -->
        <template v-if="orderDetail.status === 5">
          <el-divider />
          <div class="section-title">取消信息</div>
          <el-descriptions border :column="1">
            <el-descriptions-item label="取消原因">{{ orderDetail.cancelReason || '-' }}</el-descriptions-item>
            <el-descriptions-item label="取消操作人">{{ cancelByText(orderDetail.cancelBy, orderDetail.buyerId, orderDetail.sellerId) }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <!-- 区域三：商品信息 -->
        <el-divider />
        <div class="section-title">商品信息</div>
        <el-descriptions border :column="2">
          <el-descriptions-item label="商品封面" :span="2">
            <el-image
              v-if="orderDetail.productCoverImage"
              :src="getImageUrl(orderDetail.productCoverImage)"
              :preview-src-list="[getImageUrl(orderDetail.productCoverImage)]"
              fit="cover"
              style="width: 80px; height: 80px; border-radius: 6px"
            />
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="商品标题">{{ orderDetail.productTitle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品分类">{{ orderDetail.productCategoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品成色">{{ getConditionText(orderDetail.productConditionLevel) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 区域四：买卖双方信息 -->
        <el-divider />
        <div class="buyer-seller-grid">
          <div>
            <div class="section-title">买家信息</div>
            <el-descriptions border :column="1">
              <el-descriptions-item label="昵称">{{ orderDetail.buyerNickName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ maskPhone(orderDetail.buyerPhone) }}</el-descriptions-item>
              <el-descriptions-item label="认证状态">
                <el-tag :type="getAuthStatusType(orderDetail.buyerAuthStatus)" size="small">
                  {{ getAuthStatusText(orderDetail.buyerAuthStatus) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div>
            <div class="section-title">卖家信息</div>
            <el-descriptions border :column="1">
              <el-descriptions-item label="昵称">{{ orderDetail.sellerNickName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ maskPhone(orderDetail.sellerPhone) }}</el-descriptions-item>
              <el-descriptions-item label="认证状态">
                <el-tag :type="getAuthStatusType(orderDetail.sellerAuthStatus)" size="small">
                  {{ getAuthStatusText(orderDetail.sellerAuthStatus) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 强制下架弹窗 -->
    <el-dialog v-model="forceOffVisible" title="强制下架商品" width="480px">
      <div v-if="currentProduct" class="ban-content">
        <p class="force-off-tip">
          确定强制下架商品「{{ currentProduct.title }}」？该商品将立即下架。
        </p>
        <el-input
          v-model="forceOffReason"
          type="textarea"
          :rows="4"
          placeholder="请输入强制下架原因（将记录在案）"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="forceOffVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmForceOff">确认下架</el-button>
      </template>
    </el-dialog>

    <!-- 批量强制下架弹窗 -->
    <el-dialog v-model="batchForceOffVisible" title="批量强制下架" width="480px">
      <div class="ban-content">
        <p class="force-off-tip">
          确定强制下架选中的 {{ selectedRows.length }} 个商品？
        </p>
        <el-input
          v-model="batchForceOffReason"
          type="textarea"
          :rows="4"
          placeholder="请输入强制下架原因（将记录在案）"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="batchForceOffVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmBatchForceOff">确认下架</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, User, Search } from '@element-plus/icons-vue'
import { getProductDetail, getProductPage, batchForceOffShelf, exportProduct, forceOffShelf, getRelatedOrders, getPublisherInfo } from '@/api/product'
import { getCategoryList } from '@/api/category'
import { getOrderDetail } from '@/api/order'
import { getImageUrl, getPreviewImages } from '@/utils/baseUrl'

const router = useRouter()

// 查询参数
const queryParams = ref({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: null,
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  dateRange: null,
  sortBy: 'default'
})

const list = ref([])
const total = ref(0)
const selectedRows = ref([])

const categoryList = ref([])

const detailVisible = ref(false)
const forceOffVisible = ref(false)
const batchForceOffVisible = ref(false)

const currentProduct = ref(null)
const currentProductDetail = ref(null)

const forceOffReason = ref('')
const batchForceOffReason = ref('')

const detailActiveTab = ref('orders')

// 关联订单分页
const orderQuery = ref({
  page: 1,
  pageSize: 5
})
const orderList = ref([])
const orderTotal = ref(0)
const publisherInfo = ref(null)
const publisherInfoLoading = ref(false)

const canBatchForceOff = computed(() => {
  return selectedRows.value.length > 0 && selectedRows.value.every(row => row.status === 1)
})

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 19)
  return date.toLocaleString('zh-CN', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

const getOrderStatusType = (status) => {
  const map = { 1: 'warning', 2: 'info', 3: 'success', 4: 'success', 5: 'danger' }
  return map[status] ?? 'info'
}

const getOrderStatusText = (status) => {
  const map = { 1: '待接单', 2: '待面交', 3: '已完成', 4: '已评价', 5: '已取消' }
  return map[status] || '未知'
}

const maskPhone = (phone) => {
  if (!phone || phone.length < 7) return phone || '-'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

const cancelByText = (cancelBy, buyerId, sellerId) => {
  if (cancelBy === 0 || cancelBy === null) return '系统自动取消（超时）'
  if (cancelBy === buyerId) return '买家主动取消'
  if (cancelBy === sellerId) return '卖家主动取消'
  return '未知'
}

const getConditionText = (level) => {
  const map = { 1: '全新', 2: '几乎全新', 3: '9成新', 4: '8成新', 5: '7成新及以下' }
  return map[level] || '未知'
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '在售', 2: '已下架', 3: '已售出', 4: '审核驳回' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

const getAuthStatusText = (status) => {
  const map = { 0: '未认证', 1: '审核中', 2: '已认证', 3: '已驳回' }
  return map[status] || '未知'
}

const getAuthStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const buildListParams = () => {
  const params = {
    page: queryParams.value.page,
    pageSize: queryParams.value.pageSize
  }

  if (queryParams.value.keyword?.trim()) params.keyword = queryParams.value.keyword.trim()
  if (queryParams.value.status !== null && queryParams.value.status !== '') params.status = queryParams.value.status
  if (queryParams.value.categoryId !== null && queryParams.value.categoryId !== '') params.categoryId = queryParams.value.categoryId
  if (queryParams.value.minPrice !== null && queryParams.value.minPrice !== undefined) params.minPrice = queryParams.value.minPrice
  if (queryParams.value.maxPrice !== null && queryParams.value.maxPrice !== undefined) params.maxPrice = queryParams.value.maxPrice
  if (queryParams.value.dateRange?.[0]) {
    // 后端常见接收 beginTime/endTime，这里先按该命名组装
    params.beginTime = queryParams.value.dateRange[0]
    params.endTime = queryParams.value.dateRange[1]
  }
  if (queryParams.value.sortBy !== 'default') params.sortBy = queryParams.value.sortBy

  return params
}

const loadList = async () => {
  try {
    const res = await getProductPage(buildListParams())
    const page = res.data || {}
    list.value = page.records || []
    total.value = Number(page.total || 0)
  } catch (error) {
    console.error('加载商品列表失败:', error)
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

const onSearch = () => {
  queryParams.value.page = 1
  loadList()
}

const onFilterChange = () => {
  queryParams.value.page = 1
  loadList()
}

const onQuery = () => {
  queryParams.value.page = 1
  loadList()
}

const onReset = () => {
  queryParams.value = {
    page: 1,
    pageSize: 10,
    keyword: '',
    status: null,
    categoryId: null,
    minPrice: null,
    maxPrice: null,
    dateRange: null,
    sortBy: 'default'
  }
  loadList()
}

const onSelectionChange = (selection) => {
  selectedRows.value = selection
}

const openDetail = async (row) => {
  try {
    const res = await getProductDetail(row.id)
    currentProductDetail.value = res.data || null
    detailVisible.value = true
    detailActiveTab.value = 'orders'

    orderQuery.value.page = 1
    publisherInfo.value = null
    await loadOrderList()
    loadPublisherInfo(row.id)
  } catch (error) {
    console.error('加载商品详情失败:', error)
  }
}

const loadOrderList = async () => {
  try {
    if (!currentProductDetail.value?.id) return
    const res = await getRelatedOrders(currentProductDetail.value.id, orderQuery.value.page, orderQuery.value.pageSize)
    const pageData = res.data || {}
    orderList.value = pageData.records || []
    orderTotal.value = Number(pageData.total || 0)
  } catch (error) {
    console.error('加载关联订单失败:', error)
  }
}

const loadPublisherInfo = async (productId) => {
  if (!productId) return
  publisherInfoLoading.value = true
  try {
    const res = await getPublisherInfo(productId)
    publisherInfo.value = res.data || null
  } catch (error) {
    console.error('加载发布者信息失败:', error)
  } finally {
    publisherInfoLoading.value = false
  }
}

const onDetailTabChange = (tabName) => {
  if (tabName === 'publisher' && !publisherInfo.value && currentProductDetail.value) {
    loadPublisherInfo(currentProductDetail.value.id)
  }
}

const orderDetailVisible = ref(false)
const orderDetail = ref(null)

const openOrderDetail = async (orderId) => {
  try {
    const res = await getOrderDetail(orderId)
    orderDetail.value = res.data || null
    orderDetailVisible.value = true
  } catch (error) {
    console.error('加载订单详情失败:', error)
  }
}

const openReview = (row) => {
  router.push({ name: 'ProductReview', query: { id: row.id } })
}

const openForceOff = (row) => {
  currentProduct.value = row
  forceOffReason.value = ''
  forceOffVisible.value = true
}

const confirmForceOff = async () => {
  if (!forceOffReason.value.trim()) {
    ElMessage.warning('请输入强制下架原因')
    return
  }

  try {
    await ElMessageBox.confirm(`确定强制下架商品「${currentProduct.value.title}」？`, '提示', { type: 'warning' })
    await forceOffShelf(currentProduct.value.id, forceOffReason.value.trim())
    ElMessage.success('商品已下架')
    forceOffVisible.value = false
    await loadList()
  } catch (error) {
    if (error !== 'cancel') console.error('强制下架失败:', error)
  }
}

const onBatchForceOff = () => {
  batchForceOffReason.value = ''
  batchForceOffVisible.value = true
}

const confirmBatchForceOff = async () => {
  if (!batchForceOffReason.value.trim()) {
    ElMessage.warning('请输入强制下架原因')
    return
  }

  try {
    const ids = selectedRows.value.map(row => row.id)
    await ElMessageBox.confirm(`确定批量强制下架选中的 ${ids.length} 个商品？`, '提示', { type: 'warning' })
    await batchForceOffShelf(ids, batchForceOffReason.value.trim())
    ElMessage.success(`已下架 ${ids.length} 个商品`)
    batchForceOffVisible.value = false
    await loadList()
  } catch (error) {
    if (error !== 'cancel') console.error('批量下架失败:', error)
  }
}

const onExport = async () => {
  try {
    const params = buildListParams()
    const blob = await exportProduct(params)

    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const d = new Date()
    const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    a.download = `商品列表_${dateStr}.csv`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
    console.error('导出失败:', error)
  }
}

watch(detailVisible, (v) => {
  if (!v) {
    currentProductDetail.value = null
    orderList.value = []
    orderTotal.value = 0
    orderQuery.value.page = 1
    orderDetail.value = null
    publisherInfo.value = null
  }
})

// 筛选项（除 keyword 外）统一监听，避免部分组件 change 触发不稳定
watch(
  () => [
    queryParams.value.status,
    queryParams.value.categoryId,
    queryParams.value.minPrice,
    queryParams.value.maxPrice,
    queryParams.value.dateRange,
    queryParams.value.sortBy
  ],
  () => {
    queryParams.value.page = 1
  }
)

onMounted(async () => {
  await Promise.all([loadList(), loadCategories()])
})
</script>

<style scoped>
.product-list-page {
  padding: 20px;
}

.filter-container {
  background: #fff;
  padding: 24px 28px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 20px;
}

.filter-form :deep(.el-form-item__label) {
  color: #606266;
  font-size: 14px;
  padding-right: 10px;
  font-weight: 500;
  white-space: nowrap;
}

.el-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.page-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-range span {
  color: #c0c4cc;
  font-size: 14px;
}

.left-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.right-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.el-table :deep(.el-table__row:hover > td) {
  background-color: #f5f7fa;
}

.price-column {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: center;
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

.publisher-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.publisher-name {
  font-size: 13px;
  color: #606266;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

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
  background-color: #4a90d9;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
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

.force-off-tip {
  margin-bottom: 16px;
  color: #f56c6c;
  line-height: 1.5;
}

.ban-content {
  padding: 10px 0;
}

.publisher-detail {
  padding: 10px 0;
}

.publisher-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.publisher-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.publisher-nickname {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.publisher-score {
  color: #909399;
  font-size: 13px;
}

.detail-content {
  padding: 10px 0;
}

.section-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
}

.buyer-seller-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.mono-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}
</style>
