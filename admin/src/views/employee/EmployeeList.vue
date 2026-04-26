<template>
  <div class="employee-list-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>员工管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增员工</el-button>
        </div>
      </template>

      <!-- 搜索区 -->
      <div class="search-bar">
        <el-select
          v-model="searchRole"
          placeholder="员工角色"
          clearable
          style="width: 130px"
        >
          <el-option label="超级管理员" :value="1" />
          <el-option label="普通管理员" :value="2" />
        </el-select>
        <el-select
          v-model="searchStatus"
          placeholder="账号状态"
          clearable
          style="width: 110px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="姓名/账号搜索"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="filteredData"
        border
        stripe
        v-loading="tableLoading"
        empty-text="暂无员工数据"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="登录账号" width="140" />
        <el-table-column prop="name" label="真实姓名" width="120" />
        <el-table-column label="手机号" width="140" align="center">
          <template #default="{ row }">
            {{ formatPhone(row.phone) }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="130" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.role === 1" type="danger">超级管理员</el-tag>
            <el-tag v-else-if="row.role === 2" type="info">普通管理员</el-tag>
            <el-tag v-else type="warning">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.role === 1"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link :icon="Key" @click="handleResetPassword(row)">重置密码</el-button>
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
      width="500px"
      :close-on-click-modal="false"
      @closed="handleDialogClose"
    >
      <!-- 新增时提示初始密码 -->
      <el-alert
        v-if="!formData.id"
        title="初始密码为 123456，员工首次登录后请及时修改密码"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="登录账号" prop="username">
          <el-input
            v-model="formData.username"
            maxlength="30"
            placeholder="请输入登录账号"
            :disabled="!!formData.id"
          />
          <div v-if="!!formData.id" class="tip-text">登录账号创建后不可修改</div>
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input
            v-model="formData.name"
            maxlength="20"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            maxlength="11"
            placeholder="请输入手机号（选填）"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="超级管理员" :value="1" />
            <el-option label="普通管理员" :value="2" />
          </el-select>
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Key } from '@element-plus/icons-vue'
import { getEmployeePage, addEmployee, updateEmployee, resetEmployeePassword } from '@/api/employee'

// 搜索条件
const searchKeyword = ref('')
const searchRole = ref(null)
const searchStatus = ref(null)

// 表格数据（原始）
const tableData = ref([])
const tableLoading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 计算属性：本地过滤 role 和 status
const filteredData = computed(() => {
  let list = tableData.value
  if (searchRole.value !== null && searchRole.value !== undefined && searchRole.value !== '') {
    list = list.filter(item => item.role === searchRole.value)
  }
  if (searchStatus.value !== null && searchStatus.value !== undefined && searchStatus.value !== '') {
    list = list.filter(item => item.status === searchStatus.value)
  }
  return list
})

// Dialog
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  role: null,
  status: 1
})

// 表单校验规则
const formRules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { min: 4, max: 30, message: '账号长度在 4 到 30 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字、下划线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    {
      validator: (rule, value, callback) => {
        if (!value) return callback() // 选填，为空直接通过
        if (!/^1\d{10}$/.test(value)) return callback(new Error('请输入正确的手机号'))
        callback()
      },
      trigger: 'blur'
    }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
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

// 手机号脱敏
const formatPhone = (phone) => {
  if (!phone) return '-'
  return String(phone).replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 获取员工列表
const fetchList = async () => {
  tableLoading.value = true
  try {
    const res = await getEmployeePage({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    if (res.code === 1) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
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
  searchKeyword.value = ''
  searchRole.value = null
  searchStatus.value = null
  currentPage.value = 1
  fetchList()
}

// 新增
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增员工'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  resetForm()
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    name: row.name,
    phone: row.phone || '',
    role: row.role,
    status: row.status
  })
  dialogTitle.value = '编辑员工信息'
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.username = ''
  formData.name = ''
  formData.phone = ''
  formData.role = null
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
    const payload = { ...formData }
    // 手机号为空时不传该字段
    if (!payload.phone) {
      delete payload.phone
    }
    const api = formData.id ? updateEmployee : addEmployee
    const res = await api(payload)
    if (res.code === 1) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功，初始密码为 123456')
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

// 状态切换
const handleStatusChange = async (row, newVal) => {
  // 超级管理员账号不可禁用
  if (row.role === 1) {
    ElMessage.warning('超级管理员账号不可禁用')
    nextTick(() => {
      row.status = 1
    })
    return
  }
  try {
    const res = await updateEmployee({
      id: row.id,
      username: row.username,
      name: row.name,
      phone: row.phone,
      role: row.role,
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

// 重置密码
const handleResetPassword = (row) => {
  ElMessageBox.confirm(
    `确认将「${row.name}」的密码重置为初始密码 123456 吗？`,
    '重置密码确认',
    {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const res = await resetEmployeePassword(row.id)
        if (res.code === 1) {
          ElMessage.success('密码重置成功，初始密码为 123456')
        } else {
          ElMessage.error(res.msg || '重置失败')
        }
      } catch (error) {
        console.error('重置密码失败:', error)
        ElMessage.error(error.msg || '重置失败，请稍后重试')
      }
    })
    .catch((action) => {
      if (action !== 'cancel') {
        ElMessage.error('重置失败')
      }
    })
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
.employee-list-page {
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
.tip-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
