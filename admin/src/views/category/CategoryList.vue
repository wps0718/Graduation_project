<template>
  <div class="category-list-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>分类管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增分类</el-button>
        </div>
      </template>

      <!-- 搜索区 -->
      <div class="search-bar">
        <el-input
          v-model="searchName"
          placeholder="请输入分类名称搜索"
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
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="icon" label="图标" width="150" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.icon" type="info" size="small">{{ row.icon }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
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
      width="480px"
      :close-on-click-modal="false"
      @closed="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="formData.name"
            maxlength="20"
            show-word-limit
            placeholder="请输入分类名称"
          />
        </el-form-item>
        <el-form-item label="图标标识" prop="icon">
          <el-input
            v-model="formData.icon"
            maxlength="50"
            placeholder="可选，如 icon-book"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="999"
            :controls="true"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import {
  getCategoryPage,
  addCategory,
  updateCategory,
  deleteCategory
} from '@/api/category'

// 搜索相关
const searchName = ref('')

// 表格相关
const tableData = ref([])
const tableLoading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Dialog 相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({
  id: null,
  name: '',
  icon: '',
  sort: 0,
  status: 1
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  icon: [
    { max: 50, message: '图标标识最长 50 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' },
    { type: 'number', min: 0, max: 999, message: '排序值范围为 0-999', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取分类列表
const fetchList = async () => {
  tableLoading.value = true
  try {
    const res = await getCategoryPage({
      page: currentPage.value,
      pageSize: pageSize.value,
      name: searchName.value || undefined
    })
    if (res.code === 1) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
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
  searchName.value = ''
  currentPage.value = 1
  fetchList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增分类'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  resetForm()
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    icon: row.icon || '',
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.name = ''
  formData.icon = ''
  formData.sort = 0
  formData.status = 1
  formRef.value?.resetFields()
}

// Dialog 关闭回调
const handleDialogClose = () => {
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const api = formData.id ? updateCategory : addCategory
    const res = await api({ ...formData })
    if (res.code === 1) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchList()
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认删除分类「${row.name}」吗？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
      type: 'warning'
    }
  )
    .then(async () => {
      const res = await deleteCategory(row.id)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        fetchList()
      }
    })
    .catch(() => {
      // 取消删除
    })
}

// 状态切换
const handleStatusChange = async (row, newVal) => {
  try {
    const res = await updateCategory({
      id: row.id,
      name: row.name,
      icon: row.icon,
      sort: row.sort,
      status: newVal
    })
    if (res.code === 1) {
      ElMessage.success('状态更新成功')
    } else {
      // 失败时还原状态
      row.status = newVal === 1 ? 0 : 1
    }
  } catch (error) {
    // 请求失败还原状态
    row.status = newVal === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
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
.category-list-page {
  padding: 20px;
}
.search-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 10px;
  align-items: center;
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
