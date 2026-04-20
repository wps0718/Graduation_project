<template>
  <div class="report-manage-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>举报处理</span>
          <div class="toolbar">
            <!-- 搜索框 -->
            <div class="search-box">
              <svg class="search-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                <path d="M966.4 924.8l-230.4-227.2c60.8-67.2 96-156.8 96-256 0-217.6-176-390.4-390.4-390.4-217.6 0-390.4 176-390.4 390.4 0 217.6 176 390.4 390.4 390.4 99.2 0 188.8-35.2 256-96l230.4 227.2c9.6 9.6 28.8 9.6 38.4 0C979.2 950.4 979.2 934.4 966.4 924.8zM102.4 441.6c0-185.6 150.4-339.2 339.2-339.2s339.2 150.4 339.2 339.2c0 89.6-35.2 172.8-92.8 233.6-3.2 0-3.2 3.2-6.4 3.2-3.2 3.2-3.2 3.2-3.2 6.4-60.8 57.6-144 92.8-233.6 92.8C256 780.8 102.4 627.2 102.4 441.6z"/>
              </svg>
              <el-input
                v-model="query.keyword"
                placeholder="搜索举报人昵称/被举报目标"
                clearable
                class="search-input"
                @keyup.enter="onSearch"
              />
            </div>
            <!-- 处理状态筛选 -->
            <el-select
              v-model="query.status"
              placeholder="处理状态"
              clearable
              style="width: 120px"
              @change="onFilterChange"
            >
              <el-option label="待处理" :value="0" />
              <el-option label="已处理" :value="1" />
              <el-option label="已忽略" :value="2" />
            </el-select>
            <!-- 查询按钮 -->
            <el-button type="primary" @click="onQuery">查询</el-button>
          </div>
        </div>
      </template>

      <!-- Tabs 切换 -->
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="商品举报" name="product" />
        <el-tab-pane label="用户举报" name="user" />
      </el-tabs>

      <!-- 商品举报表格 -->
      <el-table v-if="activeTab === 'product'" :data="list" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="reporterNickName" label="举报人" width="120" />
        <el-table-column label="被举报商品" width="200">
          <template #default="{ row }">
            <div class="target-cell">
              <el-image
                :src="getImageUrl(row.targetCoverImage)"
                fit="cover"
                class="target-image"
              />
              <span class="target-title" :title="row.targetTitle">{{ row.targetTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="举报原因" width="120">
          <template #default="{ row }">
            <el-tag type="warning">{{ reasonTypeText(row.reasonType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="补充说明" width="180" show-overflow-tooltip />
        <el-table-column label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="举报时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="primary"
              @click="openDetail(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 用户举报表格 -->
      <el-table v-else :data="list" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="reporterNickName" label="举报人" width="120" />
        <el-table-column prop="targetTitle" label="被举报用户" width="140" />
        <el-table-column label="举报原因" width="120">
          <template #default="{ row }">
            <el-tag type="warning">{{ reasonTypeText(row.reasonType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="补充说明" width="180" show-overflow-tooltip />
        <el-table-column label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="举报时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="primary"
              @click="openDetail(row)"
            >
              处理
            </el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="举报详情" width="720px">
      <div v-if="detail">
        <!-- 区域一：举报基本信息 -->
        <div class="section-title">举报信息</div>
        <el-descriptions border :column="2">
          <el-descriptions-item label="举报人">{{ detail.reporterNickName }}</el-descriptions-item>
          <el-descriptions-item label="举报人认证">
            <el-tag :type="authStatusTagType(detail.reporterAuthStatus)">
              {{ authStatusText(detail.reporterAuthStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="举报原因">
            <el-tag type="warning">{{ reasonTypeText(detail.reasonType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="补充说明" :span="2">
            {{ detail.description || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="处理结果" :span="2">
            {{ detail.handleResult || '暂未处理' }}
          </el-descriptions-item>
          <el-descriptions-item label="处理人">{{ detail.handlerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">
            {{ formatTime(detail.handleTime) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="举报时间" :span="2">
            {{ formatTime(detail.createTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <!-- 区域二：被举报目标信息 -->
        <template v-if="detail.targetType === 1 && detail.targetProduct">
          <div class="section-title">被举报商品</div>
          <el-descriptions border :column="2">
            <el-descriptions-item label="商品封面" :span="2">
              <el-image
                :src="getImageUrl(detail.targetProduct.coverImage)"
                fit="cover"
                style="width: 80px; height: 80px; border-radius: 6px"
              />
            </el-descriptions-item>
            <el-descriptions-item label="商品标题">{{ detail.targetProduct.title }}</el-descriptions-item>
            <el-descriptions-item label="商品状态">
              <el-tag :type="productStatusTagType(detail.targetProduct.status)">
                {{ productStatusText(detail.targetProduct.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="分类">{{ detail.targetProduct.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="成色">
              {{ conditionLevelText(detail.targetProduct.conditionLevel) }}
            </el-descriptions-item>
            <el-descriptions-item label="价格" class-name="price-cell">
              <span style="color: #f56c6c; font-weight: 600">¥{{ detail.targetProduct.price }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="发布者">{{ detail.targetProduct.publisherNickName }}</el-descriptions-item>
            <el-descriptions-item label="发布者认证">
              <el-tag :type="authStatusTagType(detail.targetProduct.publisherAuthStatus)">
                {{ authStatusText(detail.targetProduct.publisherAuthStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发布时间">
              {{ formatTime(detail.targetProduct.createTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </template>

        <template v-else-if="detail.targetType === 2 && detail.targetUser">
          <div class="section-title">被举报用户</div>
          <el-descriptions border :column="2">
            <el-descriptions-item label="头像" :span="2">
              <el-avatar :size="60" :src="getImageUrl(detail.targetUser.avatarUrl)" />
            </el-descriptions-item>
            <el-descriptions-item label="昵称">{{ detail.targetUser.nickName }}</el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag :type="userStatusTagType(detail.targetUser.status)">
                {{ userStatusText(detail.targetUser.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detail.targetUser.phone }}</el-descriptions-item>
            <el-descriptions-item label="认证状态">
              <el-tag :type="authStatusTagType(detail.targetUser.authStatus)">
                {{ authStatusText(detail.targetUser.authStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="所在校区">
              {{ detail.targetUser.campusName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="综合评分" :span="2">
              ⭐ {{ detail.targetUser.score ?? '5.0' }}
            </el-descriptions-item>
          </el-descriptions>
        </template>

        <!-- 区域三：操作区（仅待处理时显示） -->
        <div v-if="detail.status === 0" class="action-area">
          <el-divider>处理操作</el-divider>
          <p class="action-tip">请选择处理结果并填写处理说明：</p>
          <el-input
            v-model="handleResult"
            type="textarea"
            :rows="3"
            placeholder="请输入处理结果说明（必填）"
          />
          <div class="action-buttons">
            <el-button type="success" :loading="handling" @click="onHandle(1)">
              标记已处理
            </el-button>
            <el-button type="info" :loading="handling" @click="onHandle(2)">
              标记已忽略
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportPage, getReportDetail, handleReport } from '@/api/report'

// Tab 状态
const activeTab = ref('product') // 'product' | 'user'

// 查询参数（两个tab共用，切换时重置）
const query = ref({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: null
})
const total = ref(0)
const list = ref([])

// 详情弹窗
const detailVisible = ref(false)
const detail = ref(null)

// 处理说明（在详情弹窗内填写）
const handleResult = ref('')
const handling = ref(false) // 处理中loading状态

/**
 * 加载举报列表
 */
const loadList = async () => {
  const params = {
    page: query.value.page,
    pageSize: query.value.pageSize,
    // targetType 根据当前 tab 自动决定
    targetType: activeTab.value === 'product' ? 1 : 2
  }
  if (query.value.keyword?.trim()) params.keyword = query.value.keyword.trim()
  if (query.value.status !== null && query.value.status !== '') {
    params.status = query.value.status
  }

  const res = await getReportPage(params)
  const page = res.data || {}
  list.value = page.records || []
  total.value = Number(page.total || 0)
}

/**
 * Tab 切换处理
 */
const onTabChange = () => {
  // 切换tab时重置查询条件和列表
  query.value = { page: 1, pageSize: query.value.pageSize, keyword: '', status: null }
  list.value = []
  total.value = 0
  loadList()
}

/**
 * 筛选条件变化时触发
 */
const onSearch = () => { query.value.page = 1; loadList() }
const onFilterChange = () => { query.value.page = 1; loadList() }
const onQuery = () => { query.value.page = 1; loadList() }

/**
 * 打开详情弹窗
 */
const openDetail = async (row) => {
  const res = await getReportDetail(row.id)
  detail.value = res.data || null
  handleResult.value = '' // 每次打开重置处理说明
  detailVisible.value = true
}

/**
 * 处理举报
 * @param {number} status - 1-已处理 / 2-已忽略
 */
const onHandle = async (status) => {
  if (!handleResult.value.trim()) {
    ElMessage.warning('请输入处理结果说明')
    return
  }
  const statusText = status === 1 ? '已处理' : '已忽略'
  await ElMessageBox.confirm(
    `确定将该举报标记为「${statusText}」？`,
    '提示',
    { type: 'warning' }
  )
  handling.value = true
  try {
    await handleReport({
      id: detail.value.id,
      status,
      handleResult: handleResult.value.trim()
    })
    ElMessage.success(`已标记为${statusText}`)
    detailVisible.value = false
    await loadList()
  } finally {
    handling.value = false
  }
}

/**
 * 图片URL处理
 */
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path.startsWith('/') ? '' : '/'}${path}`
}

/**
 * 时间格式化
 */
const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

/**
 * 举报原因映射
 */
const reasonTypeText = (type) => {
  const map = { 1: '虚假商品', 2: '违禁物品', 3: '价格异常', 4: '骚扰信息', 5: '其他' }
  return map[type] || '未知'
}

/**
 * 举报状态映射
 */
const statusText = (status) => {
  const map = { 0: '待处理', 1: '已处理', 2: '已忽略' }
  return map[status] ?? '未知'
}
const statusTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] ?? 'info'
}

/**
 * 认证状态映射
 */
const authStatusText = (s) => {
  const map = { 0: '未认证', 1: '审核中', 2: '已认证', 3: '已驳回' }
  return map[s] ?? '未知'
}
const authStatusTagType = (s) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[s] ?? 'info'
}

/**
 * 商品状态映射
 */
const productStatusText = (s) => {
  const map = { 0: '待审核', 1: '在售', 2: '已下架', 3: '已售出', 4: '审核驳回' }
  return map[s] ?? '未知'
}
const productStatusTagType = (s) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: '', 4: 'danger' }
  return map[s] ?? 'info'
}

/**
 * 用户账号状态映射
 */
const userStatusText = (s) => {
  const map = { 0: '封禁', 1: '正常', 2: '注销中' }
  return map[s] ?? '未知'
}
const userStatusTagType = (s) => {
  const map = { 0: 'danger', 1: 'success', 2: 'warning' }
  return map[s] ?? 'info'
}

/**
 * 成色映射
 */
const conditionLevelText = (level) => {
  const map = { 1: '全新', 2: '几乎全新', 3: '9成新', 4: '8成新', 5: '7成新及以下' }
  return map[level] || '-'
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
/* 头部布局 */
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

/* 搜索框统一样式 */
.search-box {
  position: relative;
  display: flex;
  align-items: center;
  width: 240px;
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

/* 分页 */
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 被举报目标：图片+标题 flex 布局 */
.target-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.target-image {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  flex-shrink: 0;
}
.target-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

/* 区域标题 */
.section-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
}

/* 详情弹窗操作区 */
.action-area {
  margin-top: 8px;
}
.action-tip {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}
.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}

/* 价格单元格样式 */
:deep(.price-cell) {
  color: #f56c6c;
  font-weight: 600;
}
</style>
