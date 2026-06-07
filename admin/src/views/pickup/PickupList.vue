<template>
  <div class="pickup-manage-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>代拿订单管理</span>
          <div class="toolbar">
            <div class="search-box">
              <el-input
                v-model="query.keyword"
                placeholder="搜索订单号"
                clearable
                class="search-input"
                @keyup.enter="onSearch"
              />
            </div>
            <el-select
              v-model="query.status"
              placeholder="订单状态"
              clearable
              style="width: 130px"
              @change="onFilterChange"
            >
              <el-option label="待接单" :value="0" />
              <el-option label="已接单" :value="1" />
              <el-option label="价格已确认" :value="2" />
              <el-option label="代拿中" :value="3" />
              <el-option label="待确认" :value="4" />
              <el-option label="已完成" :value="5" />
              <el-option label="已评价" :value="6" />
              <el-option label="已取消" :value="7" />
              <el-option label="纠纷中" :value="8" />
            </el-select>
            <el-button type="primary" @click="onQuery">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="订单号" width="200">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="取件地点" prop="pickupLocation" width="140" show-overflow-tooltip />
        <el-table-column label="送达地点" prop="deliveryLocation" width="140" show-overflow-tooltip />
        <el-table-column label="需求者" prop="requesterNickName" width="110" align="center" />
        <el-table-column label="代拿者" prop="pickerNickName" width="110" align="center">
          <template #default="{ row }">
            {{ row.pickerNickName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="报酬" width="100" align="center">
          <template #default="{ row }">
            <span class="price-text">¥ {{ formatPrice(row.agreedPrice || row.proposedPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

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
    <el-dialog v-model="detailVisible" title="代拿订单详情" width="720px">
      <div v-if="detail" class="detail-content">
        <div class="section-title">订单信息</div>
        <el-descriptions border :column="2">
          <el-descriptions-item label="订单号" :span="2">
            <span class="order-no">{{ detail.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报酬">
            <span class="price-text">¥ {{ formatPrice(detail.agreedPrice || detail.proposedPrice) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="取件地点">{{ detail.pickupLocation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="送达地点">{{ detail.deliveryLocation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="补充说明" :span="2">{{ detail.pickupDetail || '-' }}</el-descriptions-item>
          <el-descriptions-item label="期望送达">{{ formatTime(detail.expectedDeliveryTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ detail.completeTime ? formatTime(detail.completeTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="取消原因">{{ detail.cancelReason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <div class="buyer-seller-grid">
          <div>
            <div class="section-title">需求者</div>
            <el-descriptions border :column="1">
              <el-descriptions-item label="昵称">{{ detail.requesterNickName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ detail.requesterPhone || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div>
            <div class="section-title">代拿者</div>
            <el-descriptions border :column="1">
              <el-descriptions-item label="昵称">{{ detail.pickerNickName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ detail.pickerPhone || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <template v-if="evidenceImages.length">
          <el-divider />
          <div class="section-title">送达证据</div>
          <div class="evidence-list">
            <el-image
              v-for="(img, i) in evidenceImages"
              :key="i"
              :src="img"
              :preview-src-list="evidenceImages"
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 6px; margin-right: 8px"
            />
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onMounted } from 'vue'
import { getPickupPage, getPickupDetail } from '@/api/pickup'

const query = ref({ page: 1, pageSize: 10, keyword: '', status: null })
const total = ref(0)
const list = ref([])
const detailVisible = ref(false)
const detail = ref(null)

const evidenceImages = computed(() => {
  if (!detail.value || !detail.value.evidenceImages) return []
  try { return JSON.parse(detail.value.evidenceImages) } catch { return [] }
})

const loadList = async () => {
  const params = { page: query.value.page, pageSize: query.value.pageSize }
  if (query.value.keyword?.trim()) params.keyword = query.value.keyword.trim()
  if (query.value.status !== null && query.value.status !== '') params.status = query.value.status
  const res = await getPickupPage(params)
  const page = res.data || {}
  list.value = page.records || []
  total.value = Number(page.total || 0)
}

const onSearch = () => { query.value.page = 1; loadList() }
const onFilterChange = () => { query.value.page = 1; loadList() }
const onQuery = () => { query.value.page = 1; loadList() }

const openDetail = async (row) => {
  const res = await getPickupDetail(row.id)
  detail.value = res.data || null
  detailVisible.value = true
}

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 19)
  return date.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false })
}

const formatPrice = (value) => value != null ? Number(value).toFixed(2) : '0.00'

const statusText = (s) => {
  const map = { 0: '待接单', 1: '已接单', 2: '价格已确认', 3: '代拿中', 4: '待确认', 5: '已完成', 6: '已评价', 7: '已取消', 8: '纠纷中' }
  return map[s] || '未知'
}

const statusTagType = (s) => {
  const map = { 0: 'warning', 1: 'warning', 2: 'info', 3: 'info', 4: 'warning', 5: 'success', 6: 'success', 7: 'danger', 8: 'danger' }
  return map[s] ?? 'info'
}

onMounted(() => loadList())
</script>

<style scoped>
.header-row { display: flex; align-items: center; justify-content: space-between; }
.toolbar { display: flex; gap: 10px; align-items: center; }
.search-box { width: 200px; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
.order-no { font-family: 'Courier New', monospace; font-size: 12px; color: #606266; }
.price-text { color: #f56c6c; font-weight: 600; }
.section-title { font-weight: 600; font-size: 14px; color: #303133; margin-bottom: 12px; }
.detail-content { padding: 10px 0; }
.buyer-seller-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.evidence-list { display: flex; flex-wrap: wrap; }
</style>
