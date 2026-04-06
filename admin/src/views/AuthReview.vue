<template>
  <div class="auth-review-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>认证审核</span>
          <div class="toolbar">
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="loadList">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已驳回" :value="2" />
            </el-select>
            <el-button type="primary" @click="loadList">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="nickName" label="用户昵称" min-width="140" />
        <el-table-column prop="collegeName" label="学院" min-width="140" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="studentNo" label="学号" min-width="130" />
        <el-table-column prop="className" label="班级" min-width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="success" :disabled="row.status !== 0" @click="onApprove(row.id)">通过</el-button>
            <el-button size="small" type="danger" :disabled="row.status !== 0" @click="openReject(row.id)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="认证详情" width="980px">
      <div class="detail-grid" v-if="detail">
        <div class="timeline-panel">
          <div class="timeline-title">历史时间线</div>
          <el-timeline>
            <el-timeline-item
              v-for="item in historyList"
              :key="item.id"
              :timestamp="formatTime(item.createTime)"
              :type="timelineType(item.status)"
              @click="selectHistory(item)"
            >
              <div class="timeline-item" :class="{ active: selectedHistory && selectedHistory.id === item.id }">
                <div>{{ statusText(item.status) }} - {{ item.realName }}</div>
                <div class="timeline-sub">学号：{{ item.studentNo }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
        <div class="detail-panel">
          <div class="detail-title">提交内容</div>
          <div class="detail-row"><span>状态</span><span>{{ statusText(selectedHistory?.status) }}</span></div>
          <div class="detail-row"><span>姓名</span><span>{{ selectedHistory?.realName || '-' }}</span></div>
          <div class="detail-row"><span>学院</span><span>{{ selectedHistory?.collegeName || '-' }}</span></div>
          <div class="detail-row"><span>学号</span><span>{{ selectedHistory?.studentNo || '-' }}</span></div>
          <div class="detail-row"><span>班级</span><span>{{ selectedHistory?.className || '-' }}</span></div>
          <div class="detail-row"><span>审核时间</span><span>{{ formatTime(selectedHistory?.reviewTime) || '-' }}</span></div>
          <div class="detail-row"><span>驳回原因</span><span>{{ selectedHistory?.rejectReason || '-' }}</span></div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回认证" width="420px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="onReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { approveAuth, getAuthDetail, getAuthHistory, getAuthPage, rejectAuth } from '@/api/auth'

const query = ref({ page: 1, size: 10, status: null })
const total = ref(0)
const list = ref([])

const detailVisible = ref(false)
const detail = ref(null)
const historyList = ref([])
const selectedHistory = ref(null)

const rejectVisible = ref(false)
const rejectId = ref(null)
const rejectReason = ref('')

const statusText = (status) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '未知'
}

const timelineType = (status) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

const loadList = async () => {
  const res = await getAuthPage({ page: query.value.page, size: query.value.size, status: query.value.status })
  const page = res.data || {}
  list.value = page.records || []
  total.value = Number(page.total || 0)
}

const openDetail = async (row) => {
  const [detailRes, historyRes] = await Promise.all([getAuthDetail(row.id), getAuthHistory(row.id)])
  detail.value = detailRes.data || null
  historyList.value = historyRes.data || []
  selectedHistory.value = historyList.value[0] || null
  detailVisible.value = true
}

const selectHistory = (item) => {
  selectedHistory.value = item
}

const onApprove = async (id) => {
  await approveAuth(id)
  ElMessage.success('审核通过')
  await loadList()
}

const openReject = (id) => {
  rejectId.value = id
  rejectReason.value = ''
  rejectVisible.value = true
}

const onReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  await rejectAuth(rejectId.value, rejectReason.value.trim())
  ElMessage.success('已驳回')
  rejectVisible.value = false
  await loadList()
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.toolbar {
  display: flex;
  gap: 10px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.timeline-panel {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}
.detail-panel {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}
.timeline-title,
.detail-title {
  font-weight: 600;
  margin-bottom: 10px;
}
.timeline-item {
  cursor: pointer;
  border-radius: 6px;
  padding: 6px 8px;
}
.timeline-item.active {
  background: #ecf5ff;
}
.timeline-sub {
  color: #909399;
  font-size: 12px;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px dashed #f0f2f5;
}
</style>

