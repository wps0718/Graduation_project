<template>
  <div class="banner-admin-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>Banner管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增Banner</el-button>
        </div>
      </template>

      <!-- 搜索区 -->
      <div class="search-bar">
        <el-select v-model="searchCampusId" placeholder="所属校区" clearable style="width: 130px">
          <el-option label="全部校区" :value="0" />
          <el-option
            v-for="item in campusOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 100px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        border
        stripe
        v-loading="tableLoading"
        empty-text="暂无Banner数据"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="Banner图片" width="150" align="center">
          <template #default="{ row }">
            <template v-if="row.image">
              <el-image
                :src="getImageUrl(row.image)"
                style="width: 100px; height: 60px; border-radius: 4px"
                fit="cover"
                :preview-src-list="[getImageUrl(row.image)]"
                preview-teleported
                lazy
              />
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column label="链接类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.linkType === 1" type="primary">商品详情</el-tag>
            <el-tag v-else-if="row.linkType === 2" type="success">活动页</el-tag>
            <el-tag v-else-if="row.linkType === 3" type="warning">外部链接</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="所属校区" width="110" align="center">
          <template #default="{ row }">
            <template v-if="row.campusId">
              {{ getCampusName(row.campusId) }}
            </template>
            <el-tag v-else type="info">全部校区</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="展示时间" width="200">
          <template #default="{ row }">
            <template v-if="row.startTime && row.endTime">
              {{ row.startTime }} ~ {{ row.endTime }}
            </template>
            <template v-else-if="row.startTime">
              {{ row.startTime }} 起
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          @size-change="onSizeChange"
          @current-change="onCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="580px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" maxlength="100" show-word-limit placeholder="请输入Banner标题" />
        </el-form-item>

        <el-form-item label="Banner图片" prop="image">
          <el-upload
            class="banner-uploader"
            :show-file-list="false"
            accept="image/jpeg,image/png,image/gif"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
          >
            <div v-if="formData.image" class="image-preview-wrapper" @click.stop>
              <el-image
                :src="getImageUrl(formData.image)"
                style="width: 200px; height: 120px; border-radius: 4px"
                fit="cover"
              />
              <div class="image-actions">
                <span @click.stop="handleRemoveImage">
                  <el-icon><Delete /></el-icon> 删除
                </span>
                <span>点击更换</span>
              </div>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon" :class="{ 'is-loading': uploadLoading }">
                <Loading v-if="uploadLoading" />
                <Plus v-else />
              </el-icon>
              <div class="upload-text">{{ uploadLoading ? '上传中...' : '点击上传图片' }}</div>
              <div class="upload-tip">支持JPG/PNG/GIF，最大5MB</div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="所属校区" prop="campusId">
          <el-select v-model="formData.campusId" clearable placeholder="请选择校区（不选则适用全部校区）" style="width: 100%">
            <el-option
              v-for="item in campusOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="链接类型" prop="linkType">
          <el-select v-model="formData.linkType" placeholder="请选择链接类型" style="width: 100%">
            <el-option label="商品详情" :value="1" />
            <el-option label="活动页" :value="2" />
            <el-option label="外部链接" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item :label="linkUrlLabel" prop="linkUrl" v-if="formData.linkType">
          <el-input
            v-model="formData.linkUrl"
            :placeholder="linkUrlPlaceholder"
            clearable
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="展示时间">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
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
import { ref, reactive, nextTick, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, Loading } from '@element-plus/icons-vue'
import {
  getBannerPage,
  addBanner,
  updateBanner,
  deleteBanner,
  getCampusListForBanner,
  uploadBannerImage
} from '@/api/banner'

// ==================== 工具方法 ====================
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path.startsWith('/') ? '' : '/'}${path}`
}

// ==================== 校区选项 ====================
const campusOptions = ref([])

const fetchCampusOptions = async () => {
  try {
    const res = await getCampusListForBanner()
    if (res.code === 1) {
      campusOptions.value = res.data || []
    }
  } catch (error) {
    console.error('获取校区列表失败:', error)
    ElMessage.error('获取校区列表失败')
  }
}

const getCampusName = (campusId) => {
  if (!campusId) return null
  const campus = campusOptions.value.find(c => c.id === campusId)
  return campus ? campus.name : campusId
}

// ==================== 搜索条件 ====================
const searchCampusId = ref(null)
const searchStatus = ref(null)

// ==================== 表格数据 ====================
const tableData = ref([])
const tableLoading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const fetchList = async () => {
  tableLoading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (searchCampusId.value !== null && searchCampusId.value !== undefined && searchCampusId.value !== 0) {
      params.campusId = searchCampusId.value
    }
    if (searchStatus.value !== null && searchStatus.value !== undefined && searchStatus.value !== '') {
      params.status = searchStatus.value
    }
    const res = await getBannerPage(params)
    if (res.code === 1) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取Banner列表失败:', error)
    ElMessage.error('获取Banner列表失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchList()
}

const handleReset = () => {
  searchCampusId.value = null
  searchStatus.value = null
  currentPage.value = 1
  fetchList()
}

const onSizeChange = () => {
  currentPage.value = 1
  fetchList()
}

const onCurrentChange = () => {
  fetchList()
}

// ==================== Dialog ====================
const dialogVisible = ref(false)
const dialogTitle = ref('新增Banner')
const formRef = ref(null)
const submitLoading = ref(false)
const uploadLoading = ref(false)
const timeRange = ref([])

const formData = reactive({
  id: null,
  title: '',
  image: '',
  linkType: null,
  linkUrl: '',
  campusId: null,
  sort: 0,
  status: 1,
  startTime: null,
  endTime: null
})

// 计算 linkUrl 的 label
const linkUrlLabel = computed(() => {
  const labelMap = {
    1: '商品ID',
    2: '活动路径',
    3: '外部链接'
  }
  return labelMap[formData.linkType] || '链接地址'
})

// 计算 linkUrl 的 placeholder
const linkUrlPlaceholder = computed(() => {
  const placeholderMap = {
    1: '请输入商品ID（数字）',
    2: '请输入活动页路径，如 /pages/activity/xxx',
    3: '请输入完整URL，如 https://...'
  }
  return placeholderMap[formData.linkType] || '请输入链接地址'
})

const formRules = {
  title: [
    { required: true, message: '请输入Banner标题', trigger: 'blur' },
    { max: 100, message: '标题不能超过100个字符', trigger: 'blur' }
  ],
  image: [
    {
      validator: (rule, value, callback) => {
        if (!formData.image) {
          return callback(new Error('请上传Banner图片'))
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  linkType: [
    { required: true, message: '请选择链接类型', trigger: 'change' }
  ],
  linkUrl: [
    {
      validator: (rule, value, callback) => {
        if (!formData.linkType) {
          return callback()
        }
        if (!value || !value.trim()) {
          return callback(new Error('请填写链接地址'))
        }
        if (formData.linkType === 1 && !/^\d+$/.test(value)) {
          return callback(new Error('商品ID必须为纯数字'))
        }
        if (formData.linkType === 3 && !/^https?:\/\/.+/.test(value)) {
          return callback(new Error('外部链接必须以 http:// 或 https:// 开头'))
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// ==================== 图片上传 ====================
const beforeUpload = (file) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG/PNG/GIF 格式的图片')
    return false
  }
  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleUpload = async ({ file }) => {
  uploadLoading.value = true
  try {
    const formDataObj = new FormData()
    formDataObj.append('file', file)
    formDataObj.append('type', 'banner')
    const res = await uploadBannerImage(formDataObj)
    if (res.code === 1) {
      formData.image = res.data.url
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('图片上传失败，请重试')
  } finally {
    uploadLoading.value = false
  }
}

const handleRemoveImage = () => {
  formData.image = ''
}

// ==================== 表单操作 ====================
const handleAdd = () => {
  formData.id = null
  formData.title = ''
  formData.image = ''
  formData.linkType = null
  formData.linkUrl = ''
  formData.campusId = null
  formData.sort = 0
  formData.status = 1
  formData.startTime = null
  formData.endTime = null
  timeRange.value = []
  dialogTitle.value = '新增Banner'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  formData.id = row.id
  formData.title = row.title
  formData.image = row.image
  formData.linkType = row.linkType
  formData.linkUrl = row.linkUrl
  formData.campusId = row.campusId
  formData.sort = row.sort
  formData.status = row.status
  formData.startTime = row.startTime
  formData.endTime = row.endTime
  timeRange.value = row.startTime && row.endTime ? [row.startTime, row.endTime] : []
  dialogTitle.value = '编辑Banner'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    // 处理时间范围
    if (timeRange.value && timeRange.value.length === 2) {
      formData.startTime = timeRange.value[0]
      formData.endTime = timeRange.value[1]
    } else {
      formData.startTime = null
      formData.endTime = null
    }

    // 处理 campusId（空字符串/null转null）
    if (!formData.campusId) {
      formData.campusId = null
    }

    const payload = { ...formData }
    delete payload.id

    let res
    if (formData.id) {
      res = await updateBanner(formData.id, payload)
    } else {
      res = await addBanner(payload)
    }

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

const handleDialogClose = () => {
  timeRange.value = []
  nextTick(() => {
    formRef.value?.resetFields()
    // resetFields 不会清除非表单绑定的字段，手动清空
    formData.image = ''
  })
}

// ==================== 删除操作 ====================
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认删除Banner「${row.title}」吗？`,
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
        const res = await deleteBanner(row.id)
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
    .catch(() => {})
}

// ==================== 状态切换 ====================
const handleStatusChange = async (row, newVal) => {
  try {
    const payload = {
      title: row.title,
      image: row.image,
      linkType: row.linkType,
      linkUrl: row.linkUrl,
      campusId: row.campusId,
      sort: row.sort,
      status: newVal,
      startTime: row.startTime,
      endTime: row.endTime
    }
    const res = await updateBanner(row.id, payload)
    if (res.code === 1) {
      ElMessage.success('状态更新成功')
    } else {
      row.status = newVal === 1 ? 0 : 1
      ElMessage.error(res.msg || '状态更新失败')
    }
  } catch (error) {
    row.status = newVal === 1 ? 0 : 1
    ElMessage.error(error.msg || '状态更新失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchCampusOptions()
  fetchList()
})
</script>

<style scoped>
.banner-admin-page {
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
.banner-uploader {
  width: 200px;
}
.banner-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  width: 200px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: border-color 0.3s;
}
.banner-uploader :deep(.el-upload):hover {
  border-color: #409eff;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  width: 200px;
  height: 120px;
}
.upload-icon {
  font-size: 28px;
  margin-bottom: 6px;
}
.upload-icon.is-loading {
  animation: rotating 2s linear infinite;
}
@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
.upload-text {
  font-size: 13px;
  margin-bottom: 4px;
}
.upload-tip {
  font-size: 11px;
  color: #c0c4cc;
}
.image-preview-wrapper {
  position: relative;
  width: 200px;
  height: 120px;
}
.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: none;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
}
.image-preview-wrapper:hover .image-actions {
  display: flex;
}
.image-actions span {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
