<template>
  <div class="college-list-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>学院管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增学院</el-button>
        </div>
      </template>

      <!-- 搜索区 -->
      <div class="search-bar">
        <el-input
          v-model="searchName"
          placeholder="请输入学院名称搜索"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="filteredList"
        border
        stripe
        v-loading="tableLoading"
        empty-text="暂无学院数据"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="学院名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态（禁用/启用）" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column v-if="hasCreateTime" prop="createTime" label="创建时间" width="180" align="center">
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
    </el-card>

    <!-- 新增/编辑 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="440px"
      :close-on-click-modal="false"
      @closed="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="学院名称" prop="name">
          <el-input
            v-model="formData.name"
            maxlength="50"
            show-word-limit
            placeholder="请输入学院名称"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { getCollegeList, addCollege, updateCollege, deleteCollege } from '@/api/college'

// 搜索相关
const searchName = ref('')

// 表格相关
const allList = ref([])
const tableLoading = ref(false)

// 计算属性：本地过滤
const filteredList = computed(() => {
  if (!searchName.value.trim()) return allList.value
  return allList.value.filter(item =>
    item.name.includes(searchName.value.trim())
  )
})

// 计算属性：判断是否有 createTime 字段
const hasCreateTime = computed(() => {
  return allList.value.length > 0 && 'createTime' in allList.value[0]
})

// Dialog 相关
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑学院' : '新增学院')
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({
  id: null,
  name: '',
  sort: 0,
  status: 1
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入学院名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取学院列表
const fetchList = async () => {
  tableLoading.value = true
  try {
    const res = await getCollegeList()
    if (res.code === 1) {
      allList.value = res.data || []
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
    ElMessage.error('获取学院列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 搜索（前端本地过滤）
const handleSearch = () => {
  // filteredList 是 computed，searchName 变化自动响应
}

// 重置
const handleReset = () => {
  searchName.value = ''
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
    name: row.name,
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.name = ''
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
    const api = formData.id ? updateCollege : addCollege
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
    `确认删除学院「${row.name}」吗？删除后不可恢复。`,
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
        const res = await deleteCollege(row.id)
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
    .catch(() => {
      // 取消删除
    })
}

// 状态切换
const handleStatusChange = async (row, newVal) => {
  try {
    const res = await updateCollege({
      id: row.id,
      name: row.name,
      sort: row.sort,
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

// 初始化
onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.college-list-page {
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
}
</style>
