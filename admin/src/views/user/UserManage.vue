<template>
  <div class="user-manage-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>用户管理</span>
          <div class="toolbar">
            <!-- 搜索框 -->
            <div class="search-box">
              <svg class="search-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                <path d="M966.4 924.8l-230.4-227.2c60.8-67.2 96-156.8 96-256 0-217.6-176-390.4-390.4-390.4-217.6 0-390.4 176-390.4 390.4 0 217.6 176 390.4 390.4 390.4 99.2 0 188.8-35.2 256-96l230.4 227.2c9.6 9.6 28.8 9.6 38.4 0C979.2 950.4 979.2 934.4 966.4 924.8zM102.4 441.6c0-185.6 150.4-339.2 339.2-339.2s339.2 150.4 339.2 339.2c0 89.6-35.2 172.8-92.8 233.6-3.2 0-3.2 3.2-6.4 3.2-3.2 3.2-3.2 3.2-3.2 6.4-60.8 57.6-144 92.8-233.6 92.8C256 780.8 102.4 627.2 102.4 441.6z"/>
              </svg>
              <el-input
                v-model="query.keyword"
                placeholder="搜索昵称/手机号"
                clearable
                class="search-input"
                @keyup.enter="onSearch"
              />
            </div>

            <!-- 账号状态筛选 -->
            <el-select
              v-model="query.status"
              placeholder="账号状态"
              clearable
              style="width: 120px"
              @change="onFilterChange"
            >
              <el-option label="封禁" :value="0" />
              <el-option label="正常" :value="1" />
              <el-option label="注销中" :value="2" />
            </el-select>

            <!-- 认证状态筛选 -->
            <el-select
              v-model="query.authStatus"
              placeholder="认证状态"
              clearable
              style="width: 120px"
              @change="onFilterChange"
            >
              <el-option label="未认证" :value="0" />
              <el-option label="审核中" :value="1" />
              <el-option label="已认证" :value="2" />
              <el-option label="已驳回" :value="3" />
            </el-select>

            <!-- 校区筛选 -->
            <el-select
              v-model="query.campusId"
              placeholder="校区"
              clearable
              style="width: 120px"
              @change="onFilterChange"
            >
              <el-option
                v-for="item in campusList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>

            <!-- 查询按钮 -->
            <el-button type="primary" @click="onQuery">查询</el-button>
          </div>
        </div>
      </template>

      <!-- 用户表格 -->
      <el-table :data="list" border stripe>
        <!-- ID列 -->
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <!-- 头像列 -->
        <el-table-column label="头像" width="70" align="center">
          <template #default="{ row }">
            <el-avatar
              :size="40"
              shape="circle"
              :src="getImageUrl(row.avatarUrl)"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>

        <!-- 昵称列 -->
        <el-table-column prop="nickName" label="昵称" width="130" show-overflow-tooltip />

        <!-- 手机号列 -->
        <el-table-column prop="phone" label="手机号" width="130" align="center" />

        <!-- 认证状态列 -->
        <el-table-column label="认证状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="authStatusTagType(row.authStatus)" size="small">
              {{ authStatusText(row.authStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 账号状态列 -->
        <el-table-column label="账号状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 所在校区列 -->
        <el-table-column label="所在校区" width="100" align="center">
          <template #default="{ row }">
            {{ row.campusName || '-' }}
          </template>
        </el-table-column>

        <!-- 综合评分列 -->
        <el-table-column label="综合评分" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined">
              ⭐ {{ row.score }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- 注册时间列 -->
        <el-table-column label="注册时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <!-- 封禁按钮：正常或注销中状态显示 -->
            <el-button
              v-if="row.status === 1 || row.status === 2"
              size="small"
              type="danger"
              @click="openBan(row)"
            >
              封禁
            </el-button>
            <!-- 解封按钮：封禁状态显示 -->
            <el-button
              v-if="row.status === 0"
              size="small"
              type="success"
              @click="onUnban(row)"
            >
              解封
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

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="640px">
      <div v-if="detail" class="detail-content">
        <!-- 用户基本信息区 -->
        <div class="detail-header">
          <el-avatar
            :size="80"
            shape="circle"
            :src="getImageUrl(detail.avatarUrl)"
          >
            <el-icon :size="32"><User /></el-icon>
          </el-avatar>
          <div class="detail-nickname">{{ detail.nickName }}</div>
          <div class="detail-meta">
            <span v-if="detail.score !== null && detail.score !== undefined">⭐ {{ detail.score }} 分</span>
            <span v-else>暂无评分</span>
            <span v-if="detail.ipRegion">| {{ detail.ipRegion }}</span>
          </div>
        </div>

        <!-- 详细信息区 -->
        <el-descriptions border :column="2">
          <el-descriptions-item label="用户ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ genderText(detail.gender) }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="statusTagType(detail.status)" size="small">
              {{ statusText(detail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="认证状态">
            <el-tag :type="authStatusTagType(detail.authStatus)" size="small">
              {{ authStatusText(detail.authStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所在校区">{{ detail.campusName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="认证学院">{{ detail.collegeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ detail.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detail.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detail.className || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布商品">{{ detail.publishCount || 0 }} 件</el-descriptions-item>
          <el-descriptions-item label="订单数量">{{ detail.orderCount || 0 }} 单</el-descriptions-item>
          <el-descriptions-item label="收藏数量">{{ detail.favoriteCount || 0 }} 件</el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">
            {{ formatTime(detail.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">
            {{ detail.bio || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 封禁弹窗 -->
    <el-dialog v-model="banVisible" title="封禁用户" width="420px">
      <div class="ban-content">
        <p class="ban-tip">
          确定封禁用户「{{ banTarget?.nickName }}」？封禁后该用户将无法登录和使用平台。
        </p>
        <el-input
          v-model="banReason"
          type="textarea"
          :rows="4"
          placeholder="请输入封禁原因（将记录在案）"
        />
      </div>
      <template #footer>
        <el-button @click="banVisible = false">取消</el-button>
        <el-button type="danger" @click="onBan">确认封禁</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getUserPage, getUserDetail, banUser, unbanUser, getCampusList } from '@/api/user'

// ==================== 数据定义 ====================

// 查询参数
const query = ref({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: null,
  authStatus: null,
  campusId: null
})
const total = ref(0)
const list = ref([])

// 校区列表（用于筛选下拉）
const campusList = ref([])

// 详情弹窗
const detailVisible = ref(false)
const detail = ref(null)

// 封禁弹窗
const banVisible = ref(false)
const banTarget = ref(null)   // 当前被封禁的用户行数据
const banReason = ref('')

// ==================== 加载函数 ====================

/**
 * 加载用户列表
 */
const loadList = async () => {
  const params = {
    page: query.value.page,
    pageSize: query.value.pageSize
  }
  // 过滤空值，避免传空字符串给后端
  if (query.value.keyword?.trim()) params.keyword = query.value.keyword.trim()
  if (query.value.status !== null && query.value.status !== '') params.status = query.value.status
  if (query.value.authStatus !== null && query.value.authStatus !== '') params.authStatus = query.value.authStatus
  if (query.value.campusId !== null && query.value.campusId !== '') params.campusId = query.value.campusId

  const res = await getUserPage(params)
  const page = res.data || {}
  list.value = page.records || []
  total.value = Number(page.total || 0)
}

/**
 * 加载校区列表
 */
const loadCampusList = async () => {
  const res = await getCampusList()
  campusList.value = res.data || []
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
 * 筛选条件变化
 */
const onFilterChange = () => {
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
 * 打开用户详情弹窗
 */
const openDetail = async (row) => {
  const res = await getUserDetail(row.id)
  detail.value = res.data || null
  detailVisible.value = true
}

// ==================== 封禁/解封函数 ====================

/**
 * 打开封禁弹窗
 */
const openBan = (row) => {
  banTarget.value = row
  banReason.value = ''
  banVisible.value = true
}

/**
 * 确认封禁
 */
const onBan = async () => {
  if (!banReason.value.trim()) {
    ElMessage.warning('请输入封禁原因')
    return
  }
  await banUser({ userId: banTarget.value.id, banReason: banReason.value.trim() })
  ElMessage.success(`用户「${banTarget.value.nickName}」已封禁`)
  banVisible.value = false
  await loadList()
}

/**
 * 解封用户
 */
const onUnban = async (row) => {
  await ElMessageBox.confirm(`确定解封用户「${row.nickName}」？`, '提示', { type: 'warning' })
  await unbanUser({ userId: row.id })
  ElMessage.success(`用户「${row.nickName}」已解封`)
  await loadList()
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
 * 性别映射
 * @param {number} gender - 性别值
 * @returns {string} 性别文字
 */
const genderText = (gender) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

/**
 * 账号状态映射
 * @param {number} status - 状态值
 * @returns {string} 状态文字
 */
const statusText = (status) => {
  if (status === 0) return '封禁'
  if (status === 1) return '正常'
  if (status === 2) return '注销中'
  return '未知'
}

/**
 * 账号状态标签类型
 * @param {number} status - 状态值
 * @returns {string} 标签类型
 */
const statusTagType = (status) => {
  if (status === 0) return 'danger'
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  return 'info'
}

/**
 * 认证状态映射
 * @param {number} authStatus - 认证状态值
 * @returns {string} 认证状态文字
 */
const authStatusText = (authStatus) => {
  if (authStatus === 0) return '未认证'
  if (authStatus === 1) return '审核中'
  if (authStatus === 2) return '已认证'
  if (authStatus === 3) return '已驳回'
  return '未知'
}

/**
 * 认证状态标签类型
 * @param {number} authStatus - 认证状态值
 * @returns {string} 标签类型
 */
const authStatusTagType = (authStatus) => {
  if (authStatus === 0) return 'info'
  if (authStatus === 1) return 'warning'
  if (authStatus === 2) return 'success'
  if (authStatus === 3) return 'danger'
  return 'info'
}

// ==================== 生命周期 ====================

onMounted(async () => {
  // 并行加载列表和校区数据
  await Promise.all([loadList(), loadCampusList()])
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
  width: 220px;
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

/* 详情弹窗内容 */
.detail-content {
  padding: 10px 0;
}

/* 详情头部 */
.detail-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

/* 详情昵称 */
.detail-nickname {
  font-size: 16px;
  font-weight: bold;
  margin-top: 8px;
}

/* 详情元信息（评分+IP） */
.detail-meta {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

/* 封禁弹窗内容 */
.ban-content {
  padding: 10px 0;
}

/* 封禁提示文字 */
.ban-tip {
  margin-bottom: 16px;
  color: #606266;
  line-height: 1.5;
}
</style>
