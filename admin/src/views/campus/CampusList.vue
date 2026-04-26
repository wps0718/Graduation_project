<template>
  <div class="campus-page">
    <div class="campus-layout">
      <!-- 左侧：校区管理 -->
      <el-card class="campus-card">
        <template #header>
          <div class="header-row">
            <span>校区管理</span>
            <el-button type="primary" :icon="Plus" @click="handleAddCampus">新增校区</el-button>
          </div>
        </template>

        <el-table
          :data="campusList"
          border
          stripe
          highlight-current-row
          v-loading="campusLoading"
          empty-text="暂无校区数据"
          style="width: 100%"
          @current-change="handleCampusSelect"
        >
          <el-table-column prop="id" label="ID" width="60" align="center" />
          <el-table-column prop="name" label="校区名称" min-width="100" />
          <el-table-column label="校区代码" min-width="120">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ row.code }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="60" align="center" />
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="(val) => handleCampusStatusChange(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link :icon="Edit" @click.stop="handleEditCampus(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 右侧：面交地点管理 -->
      <el-card class="point-card">
        <template #header>
          <div class="header-row">
            <span>{{ selectedCampus ? `「${selectedCampus.name}」的面交地点` : '面交地点管理' }}</span>
            <el-button
              type="primary"
              size="small"
              :icon="Plus"
              :disabled="!selectedCampus"
              @click="handleAddPoint"
            >
              新增地点
            </el-button>
          </div>
        </template>

        <!-- 未选中校区时显示空状态 -->
        <div v-if="!selectedCampus" class="point-empty">
          <el-empty description="请点击左侧校区查看面交地点" />
        </div>

        <!-- 已选中校区时显示地点列表 -->
        <el-table
          v-else
          :data="pointList"
          border
          stripe
          size="small"
          v-loading="pointLoading"
          empty-text="该校区暂无面交地点"
          style="width: 100%"
        >
          <el-table-column prop="name" label="地点名称" min-width="100" show-overflow-tooltip />
          <el-table-column label="描述" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.description || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="55" align="center" />
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                size="small"
                @change="(val) => handlePointStatusChange(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" :icon="Edit" @click="handleEditPoint(row)">编辑</el-button>
              <el-button type="danger" link size="small" :icon="Delete" @click="handleDeletePoint(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 校区 Dialog（新增/编辑校区） -->
    <el-dialog
      v-model="campusDialogVisible"
      :title="campusDialogTitle"
      width="460px"
      :close-on-click-modal="false"
      @closed="handleCampusDialogClose"
    >
      <el-form
        ref="campusFormRef"
        :model="campusForm"
        :rules="campusRules"
        label-width="90px"
      >
        <el-form-item label="校区名称" prop="name">
          <el-input
            v-model="campusForm.name"
            maxlength="20"
            show-word-limit
            placeholder="如：南海北"
          />
        </el-form-item>
        <el-form-item label="校区代码" prop="code">
          <el-input
            v-model="campusForm.code"
            maxlength="50"
            placeholder="如：nanhai_north（英文+下划线）"
            :disabled="!!campusForm.id"
          />
          <div v-if="!!campusForm.id" class="tip-text">校区代码创建后不可修改</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="campusForm.sort"
            :min="0"
            :max="999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="campusForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="campusDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="campusSubmitLoading" @click="handleCampusSubmit">确 认</el-button>
      </template>
    </el-dialog>

    <!-- 面交地点 Dialog（新增/编辑面交地点） -->
    <el-dialog
      v-model="pointDialogVisible"
      :title="pointDialogTitle"
      width="460px"
      :close-on-click-modal="false"
      @closed="handlePointDialogClose"
    >
      <el-form
        ref="pointFormRef"
        :model="pointForm"
        :rules="pointRules"
        label-width="90px"
      >
        <el-form-item label="地点名称" prop="name">
          <el-input
            v-model="pointForm.name"
            maxlength="50"
            show-word-limit
            placeholder="如：图书馆门口"
          />
        </el-form-item>
        <el-form-item label="地点描述" prop="description">
          <el-input
            v-model="pointForm.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="可选，如：一楼大厅入口处"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="pointForm.sort"
            :min="0"
            :max="999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="pointForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="pointSubmitLoading" @click="handlePointSubmit">确 认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getCampusList,
  addCampus,
  updateCampus,
  getMeetingPointList,
  addMeetingPoint,
  updateMeetingPoint,
  deleteMeetingPoint
} from '@/api/campus'

// ==================== 校区相关 ====================

const campusList = ref([])
const campusLoading = ref(false)
const selectedCampus = ref(null) // 当前选中的校区

// 校区 Dialog
const campusDialogVisible = ref(false)
const campusDialogTitle = ref('新增校区')
const campusFormRef = ref(null)
const campusSubmitLoading = ref(false)
const campusForm = reactive({
  id: null,
  name: '',
  code: '',
  sort: 0,
  status: 1
})

const campusRules = {
  name: [
    { required: true, message: '请输入校区名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入校区代码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含英文字母、数字、下划线', trigger: 'blur' }
  ],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 获取校区列表
const fetchCampusList = async () => {
  campusLoading.value = true
  try {
    const res = await getCampusList()
    if (res.code === 1) {
      campusList.value = res.data || []
      // 若 selectedCampus 有值，检查是否还在列表中，不在则清空
      if (selectedCampus.value) {
        const stillExists = campusList.value.find(c => c.id === selectedCampus.value.id)
        if (!stillExists) {
          selectedCampus.value = null
          pointList.value = []
        }
      }
    }
  } catch (error) {
    console.error('获取校区列表失败:', error)
    ElMessage.error('获取校区列表失败')
  } finally {
    campusLoading.value = false
  }
}

// 选中校区行
const handleCampusSelect = (row) => {
  if (!row) return
  selectedCampus.value = row
  fetchMeetingPoints(row.id)
}

// 新增校区
const handleAddCampus = () => {
  campusForm.id = null
  campusForm.name = ''
  campusForm.code = ''
  campusForm.sort = 0
  campusForm.status = 1
  campusDialogTitle.value = '新增校区'
  campusDialogVisible.value = true
}

// 编辑校区
const handleEditCampus = (row) => {
  campusForm.id = row.id
  campusForm.name = row.name
  campusForm.code = row.code
  campusForm.sort = row.sort
  campusForm.status = row.status
  campusDialogTitle.value = '编辑校区'
  campusDialogVisible.value = true
}

// 校区 Dialog 关闭回调
const handleCampusDialogClose = () => {
  nextTick(() => {
    campusFormRef.value?.resetFields()
  })
}

// 提交校区表单
const handleCampusSubmit = async () => {
  const valid = await campusFormRef.value?.validate().catch(() => false)
  if (!valid) return

  campusSubmitLoading.value = true
  try {
    const api = campusForm.id ? updateCampus : addCampus
    const res = await api({ ...campusForm })
    if (res.code === 1) {
      ElMessage.success(campusForm.id ? '修改成功' : '新增成功')
      campusDialogVisible.value = false
      fetchCampusList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.msg || '操作失败，请稍后重试')
  } finally {
    campusSubmitLoading.value = false
  }
}

// 校区状态切换
const handleCampusStatusChange = async (row, newVal) => {
  try {
    const res = await updateCampus({
      id: row.id,
      name: row.name,
      code: row.code,
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

// ==================== 面交地点相关 ====================

const pointList = ref([])
const pointLoading = ref(false)

// 面交地点 Dialog
const pointDialogVisible = ref(false)
const pointDialogTitle = ref('新增面交地点')
const pointFormRef = ref(null)
const pointSubmitLoading = ref(false)
const pointForm = reactive({
  id: null,
  campusId: null,
  name: '',
  description: '',
  sort: 0,
  status: 1
})

const pointRules = {
  name: [
    { required: true, message: '请输入地点名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 获取面交地点列表
const fetchMeetingPoints = async (campusId) => {
  pointLoading.value = true
  try {
    const res = await getMeetingPointList(campusId)
    if (res.code === 1) {
      pointList.value = res.data || []
    }
  } catch (error) {
    console.error('获取面交地点列表失败:', error)
    ElMessage.error('获取面交地点列表失败')
  } finally {
    pointLoading.value = false
  }
}

// 新增面交地点
const handleAddPoint = () => {
  if (!selectedCampus.value) {
    ElMessage.warning('请先选择一个校区')
    return
  }
  pointForm.id = null
  pointForm.campusId = selectedCampus.value.id
  pointForm.name = ''
  pointForm.description = ''
  pointForm.sort = 0
  pointForm.status = 1
  pointDialogTitle.value = '新增面交地点'
  pointDialogVisible.value = true
}

// 编辑面交地点
const handleEditPoint = (row) => {
  pointForm.id = row.id
  pointForm.campusId = row.campusId
  pointForm.name = row.name
  pointForm.description = row.description || ''
  pointForm.sort = row.sort
  pointForm.status = row.status
  pointDialogTitle.value = '编辑面交地点'
  pointDialogVisible.value = true
}

// 面交地点 Dialog 关闭回调
const handlePointDialogClose = () => {
  nextTick(() => {
    pointFormRef.value?.resetFields()
  })
}

// 提交面交地点表单
const handlePointSubmit = async () => {
  const valid = await pointFormRef.value?.validate().catch(() => false)
  if (!valid) return

  pointSubmitLoading.value = true
  try {
    const payload = { ...pointForm }
    // description 为空时不传该字段
    if (!payload.description) {
      delete payload.description
    }
    const api = pointForm.id ? updateMeetingPoint : addMeetingPoint
    const res = await api(payload)
    if (res.code === 1) {
      ElMessage.success(pointForm.id ? '修改成功' : '新增成功')
      pointDialogVisible.value = false
      fetchMeetingPoints(selectedCampus.value.id)
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.msg || '操作失败，请稍后重试')
  } finally {
    pointSubmitLoading.value = false
  }
}

// 删除面交地点
const handleDeletePoint = (row) => {
  ElMessageBox.confirm(
    `确认删除面交地点「${row.name}」吗？`,
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
        const res = await deleteMeetingPoint(row.id)
        if (res.code === 1) {
          ElMessage.success('删除成功')
          fetchMeetingPoints(selectedCampus.value.id)
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

// 面交地点状态切换
const handlePointStatusChange = async (row, newVal) => {
  try {
    const res = await updateMeetingPoint({
      id: row.id,
      campusId: row.campusId,
      name: row.name,
      description: row.description,
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

// 初始化
onMounted(() => {
  fetchCampusList()
})
</script>

<style scoped>
.campus-page {
  padding: 20px;
}
.campus-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.campus-card {
  flex: 0 0 55%;
  min-width: 0;
}
.point-card {
  flex: 1;
  min-width: 0;
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.point-empty {
  padding: 60px 20px;
  text-align: center;
}
.tip-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
