<template>
  <div class="dispute-manage-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>纠纷管理</span>
          <div class="toolbar">
            <el-select
              v-model="query.status"
              placeholder="纠纷状态"
              clearable
              style="width: 130px"
              @change="onFilterChange"
            >
              <el-option label="待回应" :value="0" />
              <el-option label="已回应" :value="1" />
              <el-option label="自动胜诉" :value="2" />
              <el-option label="已裁决" :value="3" />
              <el-option label="已撤回" :value="4" />
            </el-select>
            <el-select
              v-model="query.disputeType"
              placeholder="纠纷类型"
              clearable
              style="width: 130px"
              @change="onFilterChange"
            >
              <el-option label="未送达" :value="1" />
              <el-option label="物品损坏" :value="2" />
              <el-option label="超时未完成" :value="3" />
              <el-option label="价格争议" :value="4" />
              <el-option label="其他" :value="5" />
            </el-select>
            <el-button type="primary" @click="onQuery">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border stripe :row-class-name="tableRowClassName">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="订单号" width="200">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="纠纷类型" width="110" align="center">
          <template #default="{ row }">{{ disputeTypeText(row.disputeType) }}</template>
        </el-table-column>
        <el-table-column label="申诉方" prop="initiatorNickName" width="110" align="center" />
        <el-table-column label="被申诉方" prop="responderNickName" width="110" align="center">
          <template #default="{ row }">{{ row.responderNickName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="disputeTagType(row.status)" size="small">{{ disputeStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160" align="center">
          <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openDetail(row)">处理</el-button>
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

    <!-- 纠纷详情弹窗 -->
    <el-dialog v-model="detailVisible" title="纠纷详情 & 裁决" width="800px">
      <div v-if="dispute" class="detail-content">
        <div class="section-title">纠纷信息</div>
        <el-descriptions border :column="2">
          <el-descriptions-item label="订单号" :span="2">
            <span class="order-no">{{ dispute.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="纠纷类型">{{ disputeTypeText(dispute.disputeType) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="disputeTagType(dispute.status)" size="small">{{ disputeStatusText(dispute.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(dispute.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="回应截止">{{ formatTime(dispute.responseDeadline) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <div class="section-title">申诉方：{{ dispute.initiatorNickName }}（{{ dispute.initiatorRole === 1 ? '需求者' : '代拿者' }}）</div>
        <div class="desc-box">{{ dispute.description }}</div>
        <div v-if="dispute.initiatorEvidence?.length" class="evidence-section">
          <div v-for="(item, i) in dispute.initiatorEvidence" :key="i" class="evidence-item">
            <el-image v-if="item.type === 1" :src="item.url" :preview-src-list="[item.url]" fit="cover" style="width: 80px; height: 80px; border-radius: 4px" />
            <div v-else class="text-evidence">{{ item.content }}</div>
          </div>
        </div>

        <template v-if="dispute.responseDescription">
          <el-divider />
          <div class="section-title">回应方：{{ dispute.responderNickName }}</div>
          <div class="desc-box">{{ dispute.responseDescription }}</div>
          <div v-if="dispute.responderEvidence?.length" class="evidence-section">
            <div v-for="(item, i) in dispute.responderEvidence" :key="i" class="evidence-item">
              <el-image v-if="item.type === 1" :src="item.url" :preview-src-list="[item.url]" fit="cover" style="width: 80px; height: 80px; border-radius: 4px" />
              <div v-else class="text-evidence">{{ item.content }}</div>
            </div>
          </div>
        </template>

        <!-- 裁决区域 -->
        <template v-if="dispute.status === 0 || dispute.status === 1">
          <el-divider />
          <div class="section-title">平台裁决</div>
          <el-form :model="handleForm" label-width="100px">
            <el-form-item label="裁决结果">
              <el-radio-group v-model="handleForm.judgmentResult">
                <el-radio :value="1">申诉方胜诉</el-radio>
                <el-radio :value="2">被申诉方胜诉</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="裁决说明">
              <el-input v-model="handleForm.judgmentDetail" type="textarea" :rows="3" placeholder="请输入裁决说明" maxlength="500" show-word-limit />
            </el-form-item>
            <el-form-item label="处罚用户">
              <el-select v-model="handleForm.penaltyUserId" clearable placeholder="选择被处罚用户" style="width: 240px">
                <el-option :label="dispute.initiatorNickName + '（申诉方）'" :value="dispute.initiatorId" />
                <el-option :label="dispute.responderNickName + '（回应方）'" :value="dispute.responderId" />
              </el-select>
            </el-form-item>
            <el-form-item label="扣分">
              <el-input-number v-model="handleForm.penaltyScore" :min="0" :max="5" :step="0.5" :precision="1" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitHandle">提交裁决</el-button>
            </el-form-item>
          </el-form>
        </template>

        <!-- 已裁决 -->
        <template v-if="dispute.status === 3">
          <el-divider />
          <div class="section-title">裁决结果</div>
          <el-descriptions border :column="2">
            <el-descriptions-item label="裁决结果">{{ dispute.judgmentResult === 1 ? '申诉方胜诉' : '被申诉方胜诉' }}</el-descriptions-item>
            <el-descriptions-item label="裁决说明">{{ dispute.judgmentDetail || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处罚用户ID">{{ dispute.penaltyUserId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="扣分">{{ dispute.penaltyScore || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { onMounted } from 'vue'
import { getDisputePage, getDisputeDetail, handleDispute } from '@/api/pickup'
import { ElMessage } from 'element-plus'

const query = ref({ page: 1, pageSize: 10, status: null, disputeType: null })
const total = ref(0)
const list = ref([])
const detailVisible = ref(false)
const dispute = ref(null)
const handleForm = ref({ judgmentResult: 1, judgmentDetail: '', penaltyUserId: null, penaltyScore: 0 })

const loadList = async () => {
  const params = { page: query.value.page, pageSize: query.value.pageSize }
  if (query.value.status !== null && query.value.status !== '') params.status = query.value.status
  if (query.value.disputeType !== null && query.value.disputeType !== '') params.disputeType = query.value.disputeType
  const res = await getDisputePage(params)
  const page = res.data || {}
  list.value = page.records || []
  total.value = Number(page.total || 0)
}

const onFilterChange = () => { query.value.page = 1; loadList() }
const onQuery = () => { query.value.page = 1; loadList() }

const openDetail = async (row) => {
  const res = await getDisputeDetail(row.orderId)
  dispute.value = res.data || null
  handleForm.value = { judgmentResult: 1, judgmentDetail: '', penaltyUserId: null, penaltyScore: 0 }
  detailVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.value.judgmentDetail.trim()) { ElMessage.warning('请输入裁决说明'); return }
  try {
    await handleDispute({
      disputeId: dispute.value.id,
      ...handleForm.value,
      penaltyUserId: handleForm.value.penaltyUserId || null,
      penaltyScore: handleForm.value.penaltyScore || 0
    })
    ElMessage.success('裁决已提交')
    detailVisible.value = false
    loadList()
  } catch (e) { /* handled */ }
}

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 19)
  return date.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false })
}

const disputeTypeText = (t) => {
  const map = { 1: '未送达', 2: '物品损坏', 3: '超时未完成', 4: '价格争议', 5: '其他' }
  return map[t] || '未知'
}

const disputeStatusText = (s) => {
  const map = { 0: '待回应', 1: '已回应', 2: '自动胜诉', 3: '已裁决', 4: '已撤回' }
  return map[s] || '未知'
}

const disputeTagType = (s) => {
  const map = { 0: 'danger', 1: 'warning', 2: 'success', 3: 'success', 4: 'info' }
  return map[s] ?? 'info'
}

const tableRowClassName = ({ row }) => {
  if (row.status === 0) return 'pending-row'
  return ''
}

onMounted(() => loadList())
</script>

<style scoped>
.header-row { display: flex; align-items: center; justify-content: space-between; }
.toolbar { display: flex; gap: 10px; align-items: center; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
.order-no { font-family: 'Courier New', monospace; font-size: 12px; color: #606266; }
.section-title { font-weight: 600; font-size: 14px; color: #303133; margin-bottom: 12px; }
.detail-content { padding: 10px 0; }
.desc-box { background: #f5f7fa; border-radius: 6px; padding: 12px; font-size: 13px; color: #606266; line-height: 1.6; margin-bottom: 12px; }
.evidence-section { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.evidence-item { display: inline-block; }
.text-evidence { background: #f0f2f5; border-radius: 4px; padding: 8px 12px; font-size: 13px; color: #333; max-width: 300px; }
</style>

<style>
.pending-row { background-color: #fef0f0 !important; }
</style>
