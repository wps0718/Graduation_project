<template>
  <div class="notice-admin-page">
    <el-card class="main-card">
      <!-- 顶部筛选表单 -->
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="公告类型">
          <el-select v-model="filterForm.type" placeholder="请选择类型" clearable>
            <el-option label="系统公告" :value="1" />
            <el-option label="活动公告" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="显示状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
            <el-option label="显示" :value="1" />
            <el-option label="隐藏" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>

        <el-form-item style="float: right">
          <el-button type="success" @click="handleAdd">+ 发布公告</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        :data="noticeList"
        v-loading="tableLoading"
        stripe
        style="width: 100%; margin-top: 20px"
      >
        <!-- 标题 -->
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />

        <!-- 类型 -->
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 排序 -->
        <el-table-column prop="sort" label="排序" width="80" align="center" />

        <!-- 创建时间 -->
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />

        <!-- 状态 -->
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; text-align: right"
        @size-change="handlePageSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '发布公告'"
      width="650px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        @submit.prevent
      >
        <!-- 公告标题 -->
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入公告标题（最多 50 字）"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>

        <!-- 公告类型 -->
        <el-form-item label="公告类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择公告类型">
            <el-option label="系统公告" :value="1" />
            <el-option label="活动公告" :value="2" />
          </el-select>
        </el-form-item>

        <!-- 公告正文 -->
        <el-form-item label="公告正文" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容（最多 500 字）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 排序 -->
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
        </el-form-item>

        <!-- 状态 -->
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">立即发布</el-radio>
            <el-radio :label="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '更新' : '发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getNoticeList,
  addNotice,
  updateNotice,
  deleteNotice
} from '@/api/content'

// ==================== 数据定义 ====================

const filterForm = reactive({
  type: undefined,
  status: undefined
})

const formData = reactive({
  id: null,
  title: '',
  content: '',
  type: 1,
  sort: 0,
  status: 1
})

const formRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 50, message: '标题最多 50 字', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { max: 500, message: '内容最多 500 字', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const noticeList = ref([])
const tableLoading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// ==================== 方法定义 ====================

/**
 * 获取公告类型名称
 */
const getTypeName = (type) => {
  const typeMap = {
    1: '系统公告',
    2: '活动公告'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取公告类型标签类型
 */
const getTypeTagType = (type) => {
  const typeMap = {
    1: 'danger',
    2: 'primary'
  }
  return typeMap[type] || 'info'
}

/**
 * 加载公告列表
 */
const loadNoticeList = async () => {
  tableLoading.value = true
  try {
    const res = await getNoticeList(
      pagination.page,
      pagination.pageSize,
      filterForm.type,
      filterForm.status
    )
    if (res.code === 1) {
      noticeList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载公告列表失败')
  } finally {
    tableLoading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.page = 1
  loadNoticeList()
}

/**
 * 重置筛选
 */
const handleReset = () => {
  filterForm.type = undefined
  filterForm.status = undefined
  pagination.page = 1
  loadNoticeList()
}

/**
 * 新增公告
 */
const handleAdd = () => {
  isEdit.value = false
  formData.id = null
  formData.title = ''
  formData.content = ''
  formData.type = 1
  formData.sort = 0
  formData.status = 1
  dialogVisible.value = true
}

/**
 * 编辑公告
 */
const handleEdit = (row) => {
  isEdit.value = true
  formData.id = row.id
  formData.title = row.title
  formData.content = row.content
  formData.type = row.type
  formData.sort = row.sort
  formData.status = row.status
  dialogVisible.value = true
}

/**
 * 删除公告
 */
const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const res = await deleteNotice(row.id)
        if (res.code === 1) {
          ElMessage.success('删除成功')
          loadNoticeList()
        }
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

/**
 * 状态切换
 */
const handleStatusChange = async (row) => {
  try {
    const res = await updateNotice({
      id: row.id,
      status: row.status
    })
    if (res.code === 1) {
      ElMessage.success('状态更新成功')
    } else {
      row.status = row.status === 1 ? 0 : 1
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  submitLoading.value = true
  try {
    const submitData = {
      title: formData.title,
      content: formData.content,
      type: formData.type,
      sort: formData.sort,
      status: formData.status
    }

    if (isEdit.value) {
      submitData.id = formData.id
      const res = await updateNotice(submitData)
      if (res.code === 1) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadNoticeList()
      }
    } else {
      const res = await addNotice(submitData)
      if (res.code === 1) {
        ElMessage.success('发布成功')
        dialogVisible.value = false
        loadNoticeList()
      }
    }
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 弹窗关闭回调
 */
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

/**
 * 分页变化
 */
const handlePageChange = () => {
  loadNoticeList()
}

const handlePageSizeChange = () => {
  pagination.page = 1
  loadNoticeList()
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadNoticeList()
})
</script>

<style scoped>
.notice-admin-page {
  padding: 20px;
}

.main-card {
  background: #f0f2f5;
  border-radius: 8px;
}

.filter-form {
  margin-bottom: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-input-number) {
  width: 100%;
}
</style>
