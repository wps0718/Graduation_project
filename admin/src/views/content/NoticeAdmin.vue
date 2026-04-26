<template>
  <div class="notice-admin-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>公告管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增公告</el-button>
        </div>
      </template>

      <!-- 搜索区 -->
      <div class="search-bar">
        <el-select
          v-model="searchType"
          placeholder="公告类型"
          clearable
          style="width: 120px"
        >
          <el-option label="系统公告" :value="1" />
          <el-option label="活动公告" :value="2" />
        </el-select>
        <el-select
          v-model="searchStatus"
          placeholder="状态"
          clearable
          style="width: 100px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-input
          v-model="searchTitle"
          placeholder="请输入公告标题搜索"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        border
        stripe
        v-loading="tableLoading"
        empty-text="暂无公告数据"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="公告类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" type="danger">系统公告</el-tag>
            <el-tag v-else-if="row.type === 2" type="success">活动公告</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="公告内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="onSizeChange"
          @current-change="onCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @closed="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model="formData.title"
            maxlength="100"
            show-word-limit
            placeholder="请输入公告标题"
          />
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择公告类型" style="width: 100%">
            <el-option label="系统公告" :value="1" />
            <el-option label="活动公告" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            maxlength="2000"
            show-word-limit
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { getNoticePage, addNotice, updateNotice, deleteNotice } from '@/api/notice'

// 搜索条件
const searchTitle = ref('')
const searchType = ref(null)
const searchStatus = ref(null)

// 表格数据
const tableData = ref([])
const tableLoading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Dialog
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑公告' : '新增公告')
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({
  id: null,
  title: '',
  content: '',
  type: null,
  status: 1
})

// 表单校验规则
const formRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择公告类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 1, max: 2000, message: '内容长度在 1 到 2000 个字符', trigger: 'blur' }
  ]
}

// 格式化日期时间为北京时间（东八区）
const formatDateTime = (val) => {
  if (!val) return '-'
  const date = new Date(val)
  if (isNaN(date.getTime())) return String(val).replace('T', ' ').slice(0, 19)
  const pad = (n) => String(n).padStart(2, '0')
  const y = date.getFullYear()
  const m = pad(date.getMonth() + 1)
  const d = pad(date.getDate())
  const h = pad(date.getHours())
  const min = pad(date.getMinutes())
  const s = pad(date.getSeconds())
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

// 获取公告列表
const fetchList = async () => {
  tableLoading.value = true
  try {
    const res = await getNoticePage({
      page: currentPage.value,
      pageSize: pageSize.value,
      type: searchType.value ?? undefined,
      status: searchStatus.value ?? undefined
    })
    if (res.code === 1) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchList()
}

// 重置
const handleReset = () => {
  searchTitle.value = ''
  searchType.value = null
  searchStatus.value = null
  currentPage.value = 1
  fetchList()
}

// 新增
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  resetForm()
  Object.assign(formData, {
    id: row.id,
    title: row.title,
    content: row.content,
    type: row.type,
    status: row.status
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.title = ''
  formData.content = ''
  formData.type = null
  formData.status = 1
  formRef.value?.resetFields()
}

// Dialog 关闭回调
const handleDialogClose = () => {
  nextTick(() => {
    formRef.value?.resetFields()
  })
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const api = formData.id ? updateNotice : addNotice
    const res = await api({ ...formData })
    if (res.code === 1) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.msg || '操作失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认删除公告「${row.title}」吗？删除后不可恢复。`,
    '警告',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const res = await deleteNotice(row.id)
        if (res.code === 1) {
          ElMessage.success('删除成功')
          fetchList()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        ElMessage.error(error.msg || '删除失败，请稍后重试')
      }
    })
    .catch((action) => {
      if (action !== 'cancel') {
        ElMessage.error('删除失败')
      }
    })
}

// 状态切换
const handleStatusChange = async (row, newVal) => {
  try {
    const res = await updateNotice({
      id: row.id,
      title: row.title,
      content: row.content,
      type: row.type,
      status: newVal
    })
    if (res.code === 1) {
      ElMessage.success('状态更新成功')
    } else {
      // 失败时还原状态
      row.status = newVal === 1 ? 0 : 1
      ElMessage.error(res.msg || '状态更新失败')
    }
  } catch (error) {
    // 请求失败还原状态
    row.status = newVal === 1 ? 0 : 1
    ElMessage.error(error.msg || '状态更新失败')
  }
}

// 分页大小变化
const onSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchList()
}

// 页码变化
const onCurrentChange = (val) => {
  currentPage.value = val
  fetchList()
}

// 初始化
onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.notice-admin-page {
  padding: 20px;
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.search-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
