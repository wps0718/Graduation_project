<template>
  <div class="product-list-page">
    <el-card>
        <template #header>
          <div class="header-row">
            <span>商品列表</span>
            <div class="toolbar">
              <el-form
                :model="queryParams"
                inline
                label-position="right"
                label-width="56px"
                class="filter-form"
              >
                <el-form-item label="状态">
                  <el-select
                    v-model="queryParams.status"
                    clearable
                    placeholder="全部"
                    style="width: 140px"
                    @change="onFilterChange"
                    @clear="onFilterChange"
                  >
                    <el-option label="全部" value="" />
                    <el-option label="待审核" :value="0" />
                    <el-option label="在售" :value="1" />
                    <el-option label="已下架" :value="2" />
                    <el-option label="已售出" :value="3" />
                    <el-option label="审核驳回" :value="4" />
                  </el-select>
                </el-form-item>

                <el-form-item label="分类">
                  <el-select
                    v-model="queryParams.categoryId"
                    clearable
                    placeholder="全部"
                    style="width: 160px"
                    @change="onFilterChange"
                    @clear="onFilterChange"
                  >
                    <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>

                <el-form-item label="价格">
                  <div class="price-range-inline">
                    <el-input-number
                      v-model="queryParams.minPrice"
                      :min="0"
                      :precision="2"
                      controls-position="right"
                      style="width: 120px"
                      placeholder="最低价"
                      @change="onFilterChange"
                      @input="onFilterChange"
                    />
                    <span class="range-sep">-</span>
                    <el-input-number
                      v-model="queryParams.maxPrice"
                      :min="0"
                      :precision="2"
                      controls-position="right"
                      style="width: 120px"
                      placeholder="最高价"
                      @change="onFilterChange"
                      @input="onFilterChange"
                    />
                  </div>
                </el-form-item>

                <el-form-item label="发布时间">
                  <el-date-picker
                    v-model="queryParams.dateRange"
                    type="daterange"
                    unlink-panels
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    style="width: 260px"
                    @change="onFilterChange"
                    @clear="onFilterChange"
                  />
                </el-form-item>

                <el-form-item label="排序">
                  <el-select v-model="queryParams.sortBy" style="width: 160px" @change="onFilterChange">
                    <el-option label="按ID倒序" value="default" />
                    <el-option label="发布时间最新" value="createTime-desc" />
                    <el-option label="浏览量最高" value="viewCount-desc" />
                    <el-option label="价格高→低" value="price-desc" />
                    <el-option label="价格低→高" value="price-asc" />
                  </el-select>
                </el-form-item>

                <el-form-item>
                  <div class="search-box">
                    <svg class="search-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                      <path
                        d="M966.4 924.8l-230.4-227.2c60.8-67.2 96-156.8 96-256 0-217.6-176-390.4-390.4-390.4-217.6 0-390.4 176-390.4 390.4 0 217.6 176 390.4 390.4 390.4 99.2 0 188.8-35.2 256-96l230.4 227.2c9.6 9.6 28.8 9.6 38.4 0C979.2 950.4 979.2 934.4 966.4 924.8zM102.4 441.6c0-185.6 150.4-339.2 339.2-339.2s339.2 150.4 339.2 339.2c0 89.6-35.2 172.8-92.8 233.6-3.2 0-3.2 3.2-6.4 3.2-3.2 3.2-3.2 3.2-3.2 6.4-60.8 57.6-144 92.8-233.6 92.8C256 780.8 102.4 627.2 102.4 441.6z"
                      />
                    </svg>
                    <el-input
                      v-model="queryParams.keyword"
                      placeholder="搜索商品标题/发布者昵称"
                      clearable
                      class="search-input"
                      @keyup.enter="onSearch"
                    />
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="onQuery">查询</el-button>
                  <el-button @click="onReset">重置</el-button>
                </el-form-item>

                <el-form-item>
                  <el-button type="danger" :disabled="!canBatchForceOff" @click="onBatchForceOff">
                    {{ selectedRows.length > 0 ? `批量强制下架（${selectedRows.length}）` : '批量强制下架' }}
                  </el-button>
                  <el-button type="primary" plain @click="onExport">导出</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </template>

        <el-table :data="list" border stripe @selection-change="onSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="id" label="ID" width="90" align="center" sortable />

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
          <el-descriptions-item label="面交地点">{{ currentProductDetail.meetingPoint || '-' }}</el-descriptions-item>

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

        <el-tabs v-model="detailActiveTab" style="margin-top: 16px;">
          <el-tab-pane label="关联订单" name="orders">
            <el-table :data="orderList" border stripe size="small">
              <el-table-column prop="id" label="订单ID" width="100" align="center" />
              <el-table-column prop="buyerNickName" label="买家" width="140" align="center" show-overflow-tooltip />
              <el-table-column label="交易价格" width="120" align="center">
                <template #default="{ row }">
                  <span class="price-red">¥{{ row.amount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="statusText" label="订单状态" width="120" align="center" />
              <el-table-column label="成交时间" width="160" align="center">
                <template #default="{ row }">
                  {{ formatTime(row.finishTime || row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" plain @click="openOrderDetail(row)">查看</el-button>
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
            <div v-if="currentProductDetail.publisher" class="publisher-detail">
              <div class="publisher-header">
                <el-avatar :size="80" shape="circle" :src="getImageUrl(currentProductDetail.publisher.avatarUrl)">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <div class="publisher-meta">
                  <div class="publisher-nickname">{{ currentProductDetail.publisher.nickName || '-' }}</div>
                  <div class="publisher-score" v-if="currentProductDetail.publisher.score !== null && currentProductDetail.publisher.score !== undefined">
                    ⭐ {{ currentProductDetail.publisher.score }} 分
                  </div>
                  <div class="publisher-score" v-else>暂无评分</div>
                </div>
              </div>

              <el-descriptions border :column="2">
                <el-descriptions-item label="用户ID">{{ currentProductDetail.publisher.id }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ currentProductDetail.publisher.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="认证状态">
                  <el-tag :type="getAuthStatusType(currentProductDetail.publisher.authStatus)" size="small">
                    {{ getAuthStatusText(currentProductDetail.publisher.authStatus) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="所在校区">{{ currentProductDetail.publisher.campusName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="发布商品">{{ currentProductDetail.publisher.publishCount || 0 }} 件</el-descriptions-item>
                <el-descriptions-item label="订单数量">{{ currentProductDetail.publisher.orderCount || 0 }} 单</el-descriptions-item>
              </el-descriptions>
            </div>
            <div v-else style="padding: 16px 0;">
              <el-empty description="暂无发布者信息" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </template>
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
import { Picture, User } from '@element-plus/icons-vue'
import { getProductDetail, getProductPage, batchForceOffShelf, exportProduct, forceOffShelf } from '@/api/product'
import { getCategoryList } from '@/api/category'
import { getOrderDetail, getOrderPage } from '@/api/order'

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

const canBatchForceOff = computed(() => {
  return selectedRows.value.length > 0 && selectedRows.value.every(row => row.status === 1)
})

const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path.startsWith('/') ? '' : '/'}${path}`
}

const getPreviewImages = (images) => {
  if (!images || !Array.isArray(images)) return []
  return images.map(img => getImageUrl(img))
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
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: '', 4: 'danger' }
  return map[status] || ''
}

const getAuthStatusText = (status) => {
  const map = { 0: '未认证', 1: '审核中', 2: '已认证', 3: '已驳回' }
  return map[status] || '未知'
}

const getAuthStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || ''
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
    await loadOrderList()
  } catch (error) {
    console.error('加载商品详情失败:', error)
  }
}

const loadOrderList = async () => {
  try {
    if (!currentProductDetail.value?.id) return
    const res = await getOrderPage({
      page: orderQuery.value.page,
      pageSize: orderQuery.value.pageSize,
      productId: currentProductDetail.value.id
    })
    const page = res.data || {}
    orderList.value = page.records || []
    orderTotal.value = Number(page.total || 0)
  } catch (error) {
    console.error('加载关联订单失败:', error)
  }
}

const openOrderDetail = async (row) => {
  try {
    const res = await getOrderDetail(row.id)
    const detail = res.data
    ElMessageBox.alert(JSON.stringify(detail || {}, null, 2), '订单详情', {
      confirmButtonText: '关闭',
      customClass: 'order-detail-alert'
    })
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

.el-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-form :deep(.el-form-item__label) {
  line-height: 32px;
  height: 32px;
  padding-right: 6px;
  font-weight: 500;
  color: #606266;
}

.filter-form :deep(.el-form-item__content) {
  line-height: 32px;
}

.filter-form :deep(.el-select),
.filter-form :deep(.el-date-editor),
.filter-form :deep(.el-input),
.filter-form :deep(.el-input-number) {
  vertical-align: middle;
}

.filter-form :deep(.el-input__wrapper),
.filter-form :deep(.el-select__wrapper),
.filter-form :deep(.el-date-editor) {
  height: 32px;
}

.filter-form :deep(.el-input-number .el-input__wrapper) {
  height: 32px;
}

.price-range-inline {
  display: flex;
  align-items: center;
}

.range-sep {
  margin: 0 8px;
  color: #909399;
}

.search-box {
  position: relative;
  width: 240px;
  display: flex;
  align-items: center;
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

.el-table {
  margin-top: 16px;
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
</style>
