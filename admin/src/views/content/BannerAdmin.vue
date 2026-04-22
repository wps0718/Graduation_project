<template>
  <div class="banner-admin-page">
    <el-card class="main-card">
      <!-- 顶部筛选表单 -->
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="所属校区">
          <el-select v-model="filterForm.campusId" placeholder="请选择校区" clearable>
            <el-option label="全局/无校区" :value="null" />
            <el-option
              v-for="campus in campusList"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>

        <el-form-item style="float: right">
          <el-button type="success" @click="handleAdd">+ 新增 Banner</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        :data="bannerList"
        v-loading="tableLoading"
        stripe
        style="width: 100%; margin-top: 20px"
      >
        <!-- 缩略图 -->
        <el-table-column label="缩略图" width="100" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              style="width: 80px; height: 60px; object-fit: cover; border-radius: 4px"
              preview-teleported
            />
          </template>
        </el-table-column>

        <!-- 所属校区 -->
        <el-table-column prop="campusName" label="所属校区" width="120" />

        <!-- 链接类型 -->
        <el-table-column label="链接类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLinkTypeTagType(row.linkType)">
              {{ getLinkTypeName(row.linkType) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 跳转目标 -->
        <el-table-column prop="linkValue" label="跳转目标" min-width="150" show-overflow-tooltip />

        <!-- 排序值 -->
        <el-table-column prop="sort" label="排序" width="80" align="center" />

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
      :title="isEdit ? '编辑 Banner' : '新增 Banner'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        @submit.prevent
      >
        <!-- 上传图片 -->
        <el-form-item label="Banner 图片" prop="imageUrl">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :limit="1"
            list-type="picture-card"
            :auto-upload="true"
          >
            <template #default>
              <el-icon><Plus /></el-icon>
            </template>
            <template #file="{ file }">
              <div>
                <img :src="file.url" alt="preview" />
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 所属校区 -->
        <el-form-item label="所属校区">
          <el-select v-model="formData.campusId" placeholder="请选择校区（可为空）" clearable>
            <el-option
              v-for="campus in campusList"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </el-form-item>

        <!-- 链接类型 -->
        <el-form-item label="链接类型" prop="linkType">
          <el-radio-group v-model="formData.linkType">
            <el-radio :label="1">商品详情</el-radio>
            <el-radio :label="2">活动页</el-radio>
            <el-radio :label="3">外部链接</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 跳转目标 -->
        <el-form-item
          :label="getLinkTargetLabel()"
          prop="linkValue"
          :rules="getLinkValueRules()"
        >
          <el-input
            v-model="formData.linkValue"
            :placeholder="getLinkTargetPlaceholder()"
            clearable
          />
        </el-form-item>

        <!-- 排序 -->
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
        </el-form-item>

        <!-- 状态 -->
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '更新' : '新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import {
  getBannerList,
  addBanner,
  updateBanner,
  deleteBanner,
  getCampusList,
  UPLOAD_URL
} from '@/api/content'

// ==================== 数据定义 ====================

const filterForm = reactive({
  campusId: null,
  status: undefined
})

const formData = reactive({
  id: null,
  imageUrl: '',
  campusId: null,
  linkType: 1,
  linkValue: '',
  sort: 0,
  status: 1
})

const formRules = {
  imageUrl: [{ required: true, message: '请上传 Banner 图片', trigger: 'change' }],
  linkType: [{ required: true, message: '请选择链接类型', trigger: 'change' }],
  linkValue: [{ required: true, message: '请输入跳转目标', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const bannerList = ref([])
const campusList = ref([])
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

const uploadUrl = computed(() => UPLOAD_URL)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

// ==================== 方法定义 ====================

/**
 * 获取链接类型名称
 */
const getLinkTypeName = (type) => {
  const typeMap = {
    1: '商品详情',
    2: '活动页',
    3: '外部链接'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取链接类型标签类型
 */
const getLinkTypeTagType = (type) => {
  const typeMap = {
    1: 'success',
    2: 'primary',
    3: 'info'
  }
  return typeMap[type] || 'info'
}

/**
 * 获取链接目标标签
 */
const getLinkTargetLabel = () => {
  const labelMap = {
    1: '商品 ID',
    2: '活动页 URL',
    3: '外部链接'
  }
  return labelMap[formData.linkType] || '跳转目标'
}

/**
 * 获取链接目标占位符
 */
const getLinkTargetPlaceholder = () => {
  const placeholderMap = {
    1: '请输入商品 ID',
    2: '请输入活动页 URL',
    3: '请输入完整的外部链接 URL'
  }
  return placeholderMap[formData.linkType] || '请输入跳转目标'
}

/**
 * 获取链接值校验规则
 */
const getLinkValueRules = () => {
  const baseRule = { required: true, message: '请输入跳转目标', trigger: 'blur' }

  if (formData.linkType === 1) {
    return [
      baseRule,
      { pattern: /^\d+$/, message: '商品 ID 必须为数字', trigger: 'blur' }
    ]
  }

  if (formData.linkType === 3) {
    return [
      baseRule,
      { pattern: /^https?:\/\/.+/, message: '请输入有效的 URL', trigger: 'blur' }
    ]
  }

  return [baseRule]
}

/**
 * 加载 Banner 列表
 */
const loadBannerList = async () => {
  tableLoading.value = true
  try {
    const res = await getBannerList(
      pagination.page,
      pagination.pageSize,
      filterForm.status,
      filterForm.campusId
    )
    if (res.code === 1) {
      bannerList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载 Banner 列表失败')
  } finally {
    tableLoading.value = false
  }
}

/**
 * 加载校区列表
 */
const loadCampusList = async () => {
  try {
    const res = await getCampusList()
    if (res.code === 1) {
      campusList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载校区列表失败')
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.page = 1
  loadBannerList()
}

/**
 * 重置筛选
 */
const handleReset = () => {
  filterForm.campusId = null
  filterForm.status = undefined
  pagination.page = 1
  loadBannerList()
}

/**
 * 新增 Banner
 */
const handleAdd = () => {
  isEdit.value = false
  formData.id = null
  formData.imageUrl = ''
  formData.campusId = null
  formData.linkType = 1
  formData.linkValue = ''
  formData.sort = 0
  formData.status = 1
  dialogVisible.value = true
}

/**
 * 编辑 Banner
 */
const handleEdit = (row) => {
  isEdit.value = true
  formData.id = row.id
  formData.imageUrl = row.imageUrl
  formData.campusId = row.campusId
  formData.linkType = row.linkType
  formData.linkValue = row.linkValue
  formData.sort = row.sort
  formData.status = row.status
  dialogVisible.value = true
}

/**
 * 删除 Banner
 */
const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该 Banner 吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const res = await deleteBanner(row.id)
        if (res.code === 1) {
          ElMessage.success('删除成功')
          loadBannerList()
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
    const res = await updateBanner({
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
 * 上传成功回调
 */
const handleUploadSuccess = (response) => {
  if (response.code === 1) {
    formData.imageUrl = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '图片上传失败')
  }
}

/**
 * 上传失败回调
 */
const handleUploadError = () => {
  ElMessage.error('图片上传失败')
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
      imageUrl: formData.imageUrl,
      campusId: formData.campusId,
      linkType: formData.linkType,
      linkValue: formData.linkValue,
      sort: formData.sort,
      status: formData.status
    }

    if (isEdit.value) {
      submitData.id = formData.id
      const res = await updateBanner(submitData)
      if (res.code === 1) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadBannerList()
      }
    } else {
      const res = await addBanner(submitData)
      if (res.code === 1) {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        loadBannerList()
      }
    }
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '新增失败')
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
  loadBannerList()
}

const handlePageSizeChange = () => {
  pagination.page = 1
  loadBannerList()
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadCampusList()
  loadBannerList()
})
</script>

<style scoped>
.banner-admin-page {
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
