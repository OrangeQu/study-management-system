<template>
  <div class="grade-data-page">
    <div class="page-header">
      <div class="header-left">
        <h1>GPA 数据面板</h1>
        <div class="header-stats">
          <span class="stat-item">
            <span class="stat-label">当前 GPA：</span>
            <span class="stat-value">{{ currentGPA.toFixed(2) }}</span>
          </span>
          <span class="stat-item">
            <span class="stat-label">总学分：</span>
            <span class="stat-value">{{ totalCredits }}</span>
          </span>
          <span class="stat-item">
            <span class="stat-label">通过率：</span>
            <span class="stat-value">{{ passRate }}%</span>
          </span>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog" :icon="Plus">
          新增成绩
        </el-button>
        <el-button @click="exportData" :icon="Download">
          导出数据
        </el-button>
        <el-button @click="refreshData" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <div class="stats-section">
      <div class="section-title">
        <el-icon><DataAnalysis /></el-icon>
        <h3>GPA 总览</h3>
      </div>
      <div class="gpa-stats-grid">
        <div class="gpa-card card-tone card-tone--purple gpa-highlight">
          <div class="gpa-highlight-inner">
            <div class="gpa-header">
              <el-icon class="gpa-icon"><Trophy /></el-icon>
              <div class="gpa-title">本学期 GPA</div>
            </div>

            <div class="gpa-value">{{ currentSemesterGPA.toFixed(2) }}</div>
            <div class="gpa-sub">当前学期</div>
          </div>
        </div>

        <div class="gpa-card card-tone card-tone--peach gpa-highlight">
          <div class="gpa-highlight-inner">
            <div class="gpa-header">
              <el-icon class="gpa-icon"><Star /></el-icon>
              <div class="gpa-title">累计 GPA</div>
            </div>
            <div class="gpa-value">{{ totalGPA.toFixed(2) }}</div>
            <div class="gpa-sub">全部学期 · 课程数：{{ totalCourses }}</div>
          </div>
        </div>

        <div class="gpa-card card-tone card-tone--green gpa-highlight">
          <div class="gpa-highlight-inner">
            <div class="gpa-header">
              <el-icon class="gpa-icon"><Checked /></el-icon>
              <div class="gpa-title">学分完成情况</div>
            </div>
            <div class="gpa-value">{{ passedCredits }}</div>
            <div class="gpa-sub">已修学分 · 共 {{ totalCredits }}</div>
          </div>
        </div>

        <div class="gpa-card card-tone card-tone--peach gpa-highlight">
          <div class="gpa-highlight-inner">
            <div class="gpa-header">
              <el-icon class="gpa-icon"><SuccessFilled /></el-icon>
              <div class="gpa-title">通过率</div>
            </div>
            <div class="gpa-value">{{ passRate }}%</div>
            <div class="gpa-sub">已通过 · 共 {{ passedCourses }} / {{ totalCourses }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon><TrendCharts /></el-icon>
          <h3>GPA 趋势分析</h3>
        </div>
      </div>

      <div class="trend-card card-tone card-tone--green">
        <GpaTrendChart
          :title="'学期 GPA 趋势'"
          :gpa-data="gpaTrendData"
          :height="'300px'"
          @semester-click="handleSemesterClick"
        />
      </div>
    </div>

    <div class="data-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon><List /></el-icon>
          <h3>成绩记录</h3>
          <span class="record-count">共 {{ totalRecords }} 条</span>
        </div>
        <div class="section-actions">
          <el-button size="small" @click="showBatchImport = true" :icon="Upload">
            批量导入
          </el-button>
        </div>
      </div>

      <GradeFilter
        ref="gradeFilter"
        :semesters="availableSemesters"
        :academic-years="availableAcademicYears"
        :initial-filters="initialFilters"
        @filter-change="handleFilterChange"
      />

      <div class="table-container card-tone card-tone--peach">
        <GradeTable
          ref="gradeTable"
          :grades="filteredGrades"
          :show-index="true"
          :show-selection="true"
          :show-actions="true"
          :show-pagination="true"
          :page-size="pageSize"
          :default-sort="{ prop: 'semester', order: 'descending' }"
          @edit="handleEditGrade"
          @delete="handleDeleteGrade"
          @add="showAddDialog"
          @page-change="handlePageChange"
          @search="handleSearch"
          @sort-change="handleSortChange"
          @selection-change="handleSelectionChange"
        >
          <template #toolbar>
            <div class="toolbar-left">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索课程名称或代码..."
                prefix-icon="Search"
                style="width: 300px"
                clearable
                @input="handleSearch"
              />
              <el-select
                v-model="tableSort"
                placeholder="排序方式"
                size="small"
                style="width: 180px"
                @change="handleSortChange"
              >
                <el-option label="按学期降序" value="semester_desc" />
                <el-option label="按学期升序" value="semester_asc" />
                <el-option label="按成绩降序" value="score_desc" />
                <el-option label="按成绩升序" value="score_asc" />
                <el-option label="按 GPA 降序" value="gpa_desc" />
                <el-option label="按 GPA 升序" value="gpa_asc" />
              </el-select>
            </div>
            <div class="toolbar-right">
              <el-button
                type="text"
                size="small"
                @click="exportSelected"
                :disabled="selectedCount === 0"
                :icon="Document"
              >
                导出已选({{ selectedCount }})
              </el-button>
            </div>
          </template>
        </GradeTable>
      </div>
    </div>

    <el-dialog
      v-model="showGradeDialog"
      :title="dialogTitle"
      width="500px"
      @close="resetGradeForm"
    >
      <el-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeFormRules"
        label-width="100px"
      >
        <el-form-item label="课程名称" prop="course_name">
          <el-input
            v-model="gradeForm.course_name"
            placeholder="请输入课程名称"
          />
        </el-form-item>

        <el-form-item label="课程代码" prop="course_code">
          <el-input
            v-model="gradeForm.course_code"
            placeholder="如：CS101"
          />
        </el-form-item>

        <el-form-item label="学分" prop="credit">
          <el-input-number
            v-model="gradeForm.credit"
            :min="0.5"
            :max="10"
            :step="0.5"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="成绩" prop="score">
          <el-input-number
            v-model="gradeForm.score"
            :min="0"
            :max="100"
            :step="0.5"
            controls-position="right"
            style="width: 100%"
            @change="handleScoreChange"
          />
          <div class="form-tip">成绩范围 0-100，GPA 会自动计算</div>
        </el-form-item>

        <el-form-item label="GPA" prop="grade_point">
          <el-input-number
            v-model="gradeForm.grade_point"
            :min="0"
            :max="5"
            :step="0.1"
            :precision="2"
            controls-position="right"
            style="width: 100%"
            disabled
          />
        </el-form-item>

        <el-form-item label="课程类型" prop="course_type">
          <el-select
            v-model="gradeForm.course_type"
            placeholder="请选择课程类型"
            style="width: 100%"
          >
            <el-option label="必修" value="required" />
            <el-option label="选修" value="elective" />
            <el-option label="通识" value="general" />
            <el-option label="实践" value="practice" />
          </el-select>
        </el-form-item>

        <el-form-item label="学期" prop="semester">
          <el-select
            v-model="gradeForm.semester"
            placeholder="请选择或输入学期"
            style="width: 100%"
            filterable
            allow-create
            default-first-option
          >
            <el-option
              v-for="semester in availableSemesters"
              :key="semester"
              :label="semester"
              :value="semester"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学年" prop="academic_year">
          <el-input
            v-model="gradeForm.academic_year"
            placeholder="如：2025-2026"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showGradeDialog = false">取消</el-button>
          <el-button type="primary" @click="submitGradeForm" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showBatchImport"
      title="批量导入"
      width="600px"
      custom-class="card-tone card-tone--purple"
    >
      <div class="import-guide">
        <p>请准备包含以下列的 Excel/CSV 文件：</p>
        <div class="format-example">
          <table>
            <thead>
              <tr>
                <th>课程代码</th>
                <th>课程名称</th>
                <th>学分</th>
                <th>成绩</th>
                <th>学期</th>
                <th>学年</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>CS101</td>
                <td>程序设计基础</td>
                <td>3.0</td>
                <td>92</td>
                <td>2025-2026-1</td>
                <td>2025-2026</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="import-actions">
          <el-upload
            action="#"
            :multiple="false"
            :show-file-list="false"
            :before-upload="handleFileUpload"
            accept=".xlsx,.xls,.csv"
          >
            <el-button type="primary" :icon="Upload">选择文件</el-button>
          </el-upload>
          <el-button :icon="Download" @click="downloadTemplate">
            下载模板
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Download,
  Refresh,
  Upload,
  Document,
  DataAnalysis,
  TrendCharts,
  List,
  Trophy,
  Star,
  Checked,
  SuccessFilled
} from '@element-plus/icons-vue'
import { listGrades, createGrade, updateGrade, deleteGrade, statsGrades } from '@/api/grades'

import GpaTrendChart from '@/components/gpa/GpaTrendChart.vue'
import GradeTable from '@/components/gpa/GradeTable.vue'
import GradeFilter from '@/components/gpa/GradeFilter.vue'

const searchKeyword = ref('')
const pageSize = ref(20)
const currentPage = ref(1)
const showGradeDialog = ref(false)
const showBatchImport = ref(false)
const submitting = ref(false)
const tableSort = ref('semester_desc')
const selectedRows = ref([])
const initialFilters = ref({})
const activeFilters = ref({
  semester: '',
  academicYear: '',
  status: '',
  courseType: '',
  creditRange: '',
  scoreRange: '',
  gpaRange: ''
})
const gradeFilter = ref(null)
const gradeTable = ref(null)

const dialogMode = ref('create')
const currentGradeId = ref(null)
const gradeFormRef = ref(null)

const grades = ref([])
const stats = ref({
  currentSemesterGPA: 0,
  totalGPA: 0,
  passRate: 0,
  totalCredits: 0,
  passedCredits: 0,
  totalCourses: 0,
  passedCourses: 0,
  trend: []
})
const availableSemesters = ref([])
const availableAcademicYears = ref([])

const gradeForm = ref({
  course_name: '',
  course_code: '',
  credit: 2.0,
  score: 0,
  grade_point: 0,
  course_type: 'required',
  semester: '',
  academic_year: ''
})

const gradeFormRules = {
  course_name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  course_code: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }]
}

const dialogTitle = computed(() => {
  return dialogMode.value === 'create' ? '添加成绩' : '编辑成绩'
})

const totalRecords = computed(() => grades.value.length || stats.value.totalCourses || 0)

const selectedCount = computed(() => selectedRows.value.length)

const filteredGrades = computed(() => {
  let filtered = [...grades.value]
  const filters = activeFilters.value

  if (filters.semester) {
    filtered = filtered.filter((g) => g.semester === filters.semester)
  }

  if (filters.academicYear) {
    filtered = filtered.filter((g) => g.academic_year === filters.academicYear)
  }

  if (filters.status === 'passed') {
    filtered = filtered.filter((g) => g.status === '通过')
  } else if (filters.status === 'failed') {
    filtered = filtered.filter((g) => g.status === '未通过')
  }

  if (filters.courseType) {
    filtered = filtered.filter((g) => g.course_type === filters.courseType)
  }

  if (filters.creditRange) {
    const [min, max] = filters.creditRange.split('-')
    filtered = filtered.filter((g) => {
      if (filters.creditRange === '4+') return g.credit >= 4
      return g.credit >= Number(min) && g.credit <= Number(max)
    })
  }

  if (filters.scoreRange) {
    const [min, max] = filters.scoreRange.split('-')
    filtered = filtered.filter((g) => {
      if (filters.scoreRange === '0-59') return g.score < 60
      return g.score >= Number(min) && g.score <= Number(max)
    })
  }

  if (filters.gpaRange) {
    const [min, max] = filters.gpaRange.split('-')
    filtered = filtered.filter((g) => {
      if (filters.gpaRange === '0-1.9') return g.grade_point < 2
      return g.grade_point >= Number(min) && g.grade_point <= Number(max)
    })
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter((g) =>
      g.course_name.toLowerCase().includes(keyword) ||
      g.course_code.toLowerCase().includes(keyword)
    )
  }

  switch (tableSort.value) {
    case 'semester_desc':
      filtered.sort((a, b) => b.semester.localeCompare(a.semester))
      break
    case 'semester_asc':
      filtered.sort((a, b) => a.semester.localeCompare(b.semester))
      break
    case 'score_desc':
      filtered.sort((a, b) => b.score - a.score)
      break
    case 'score_asc':
      filtered.sort((a, b) => a.score - b.score)
      break
    case 'gpa_desc':
      filtered.sort((a, b) => b.grade_point - a.grade_point)
      break
    case 'gpa_asc':
      filtered.sort((a, b) => a.grade_point - b.grade_point)
      break
  }

  return filtered
})

const currentSemester = computed(() => {
  return availableSemesters.value[0] || ''
})

const currentSemesterGPA = computed(() => {
  if (stats.value.currentSemesterGPA !== undefined) {
    return Number(stats.value.currentSemesterGPA) || 0
  }
  const semesterGrades = grades.value.filter((g) => g.semester === currentSemester.value)
  return calculateGPA(semesterGrades)
})

const totalGPA = computed(() => {
  if (stats.value.totalGPA !== undefined) {
    return Number(stats.value.totalGPA) || 0
  }
  return calculateGPA(grades.value)
})

const currentGPA = computed(() => currentSemesterGPA.value)

const totalCredits = computed(() => {
  if (stats.value.totalCredits !== undefined) {
    return Number(stats.value.totalCredits) || 0
  }
  return grades.value.reduce((sum, grade) => sum + grade.credit, 0)
})

const passedCredits = computed(() => {
  if (stats.value.passedCredits !== undefined) {
    return Number(stats.value.passedCredits) || 0
  }
  return grades.value
    .filter(g => g.status === '通过')
    .reduce((sum, grade) => sum + grade.credit, 0)
})

const totalCourses = computed(() => {
  if (stats.value.totalCourses !== undefined) {
    return Number(stats.value.totalCourses) || 0
  }
  return grades.value.length
})

const passedCourses = computed(() => {
  if (stats.value.passedCourses !== undefined) {
    return Number(stats.value.passedCourses) || 0
  }
  return grades.value.filter(g => g.status === '通过').length
})

const passRate = computed(() => {
  if (stats.value.passRate !== undefined) {
    return Number(stats.value.passRate) || 0
  }
  if (totalCourses.value === 0) return 0
  return Math.round((passedCourses.value / totalCourses.value) * 100)
})

const gpaTrendData = computed(() => {
  if (stats.value.trend && stats.value.trend.length) {
    return [...stats.value.trend].sort((a, b) => a.semester.localeCompare(b.semester))
  }

  const semesterGroups = {}
  grades.value.forEach((grade) => {
    if (!semesterGroups[grade.semester]) {
      semesterGroups[grade.semester] = []
    }
    semesterGroups[grade.semester].push(grade)
  })

  return Object.keys(semesterGroups)
    .sort()
    .map(semester => ({
      semester,
      gpa: calculateGPA(semesterGroups[semester])
    }))
})

const calculateGPA = (gradeList) => {
  if (gradeList.length === 0) return 0

  const totalPoints = gradeList.reduce((sum, grade) => {
    return sum + (grade.grade_point * grade.credit)
  }, 0)

  const totalCreditsValue = gradeList.reduce((sum, grade) => sum + grade.credit, 0)

  return totalCreditsValue > 0 ? totalPoints / totalCreditsValue : 0
}

const calculateGradePoint = (score) => {
  if (score < 60) return 0
  const extra = Math.max(score - 60, 0) * 0.1
  return Math.min(5, Number((1 + extra).toFixed(2)))
}

const handleScoreChange = (score) => {
  gradeForm.value.grade_point = calculateGradePoint(score ?? gradeForm.value.score)
}

// color helpers removed (not used after card simplification)

const showAddDialog = () => {
  dialogMode.value = 'create'
  resetGradeForm()
  showGradeDialog.value = true
}

const handleEditGrade = (grade) => {
  dialogMode.value = 'edit'
  currentGradeId.value = grade.id
  gradeForm.value = { ...grade }
  showGradeDialog.value = true
}

const handleDeleteGrade = async (grade) => {
  try {
    await ElMessageBox.confirm(
      `确定删除「${grade.course_name}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteGrade(grade.id)
    ElMessage.success('已删除')
    await Promise.all([loadGrades(), loadStats()])
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const resetGradeForm = () => {
  if (gradeFormRef.value) {
    gradeFormRef.value.resetFields()
  }
  gradeForm.value = {
    course_name: '',
    course_code: '',
    credit: 2.0,
    score: 0,
    grade_point: 0,
    course_type: 'required',
    semester: currentSemester.value,
    academic_year: getCurrentAcademicYear()
  }
  currentGradeId.value = null
}

const submitGradeForm = async () => {
  if (!gradeFormRef.value) return

  try {
    await gradeFormRef.value.validate()
    submitting.value = true

    const gradePoint = calculateGradePoint(gradeForm.value.score)
    gradeForm.value.grade_point = gradePoint

    const payload = {
      ...gradeForm.value,
      grade_point: gradePoint,
      status: gradeForm.value.score >= 60 ? '通过' : '未通过'
    }

    if (dialogMode.value === 'create') {
      await createGrade(payload)
      ElMessage.success('已创建')
    } else {
      await updateGrade(currentGradeId.value, payload)
      ElMessage.success('已更新')
    }

    await Promise.all([loadGrades(), loadStats()])

    showGradeDialog.value = false
    resetGradeForm()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提交失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

const handleFilterChange = (filters) => {
  initialFilters.value = filters
  activeFilters.value = { ...filters }
}

const handlePageChange = ({ page, size }) => {
  currentPage.value = page
  pageSize.value = size
}

const handleSearch = (keyword = '') => {
  searchKeyword.value = keyword
}

const handleSortChange = (sort) => {
  if (!sort) return
  if (typeof sort === 'string') {
    tableSort.value = sort
    return
  }

  if (!sort.order) {
    tableSort.value = 'semester_desc'
    return
  }

  if (sort.prop === 'score') {
    tableSort.value = sort.order === 'ascending' ? 'score_asc' : 'score_desc'
  } else if (sort.prop === 'grade_point') {
    tableSort.value = sort.order === 'ascending' ? 'gpa_asc' : 'gpa_desc'
  } else if (sort.prop === 'semester') {
    tableSort.value = sort.order === 'ascending' ? 'semester_asc' : 'semester_desc'
  }
}

const handleSemesterClick = (semester) => {
  initialFilters.value = { ...initialFilters.value, semester }
  activeFilters.value = { ...activeFilters.value, semester }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const exportData = () => {
  ElMessage.info('导出功能开发中...')
}

const exportSelected = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条数据')
    return
  }
  ElMessage.info(`正在导出 ${selectedRows.value.length} 条数据`)
}

const refreshData = async () => {
  await Promise.all([loadGrades(), loadStats()])
  ElMessage.success('数据已刷新')
}

const handleFileUpload = (file) => {
  ElMessage.info(`文件 ${file.name} 已上传，解析中...`)
  return false
}

const downloadTemplate = () => {
  ElMessage.info('正在下载模板')
}

const getCurrentAcademicYear = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  if (month >= 9) {
    return `${year}-${year + 1}`
  }
  return `${year - 1}-${year}`
}

const updateSemesterData = () => {
  const semesters = new Set()
  const academicYears = new Set()

  grades.value.forEach(grade => {
    if (grade.semester) semesters.add(grade.semester)
    if (grade.academic_year) academicYears.add(grade.academic_year)
  })

  availableSemesters.value = Array.from(semesters).sort().reverse()
  availableAcademicYears.value = Array.from(academicYears).sort().reverse()
}

const loadGrades = async () => {
  try {
    const resp = await listGrades()
    grades.value = resp?.data?.data?.list || resp?.data?.data || []
    updateSemesterData()
  } catch (error) {
    ElMessage.error(error.message || '加载成绩失败')
  }
}

const loadStats = async () => {
  try {
    const resp = await statsGrades()
    if (resp?.data?.code === 0) {
      stats.value = resp.data.data || {}
    }
  } catch (error) {
    ElMessage.error(error.message || '加载统计数据失败')
  }
}

onMounted(async () => {
  await Promise.all([loadGrades(), loadStats()])
  gradeForm.value.semester = currentSemester.value
  gradeForm.value.academic_year = getCurrentAcademicYear()
})
</script>

<style scoped>
.grade-data-page {
  width: 1100px;
  margin: 0 auto;
  padding: 20px 0;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  padding: 0 10px;
  width: 100%;
}

.header-left h1 {
  margin: 0 0 15px 0;
  font-size: 24px;
  color: #333;
}

.header-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.stats-section,
.chart-section,
.data-section {
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 20px;
  margin-bottom: 20px;
  width: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  width: 100%;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.section-title .el-icon {
  color: #1890ff;
  font-size: 18px;
}

.record-count {
  font-size: 14px;
  color: #666;
  margin-left: 15px;
}

.gpa-stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 10px;
  width: 100%;
}

.gpa-stats-grid :deep(.gpa-stat-card) {
  height: 180px !important;
}

.chart-section {
  min-height: 400px;
  width: 100%;
}

.data-section {
  flex: 1;
  width: 100%;
}

.table-container {
  margin-top: 20px;
  width: 100%;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.import-guide {
  padding: 10px;
}

.format-example {
  margin: 15px 0;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  overflow-x: auto;
}

.format-example table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.format-example th,
.format-example td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}

.format-example th {
  background: #f0f0f0;
  font-weight: 600;
}

.import-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

/* GPA highlight card (附件样式) */
.gpa-highlight {
  padding: 24px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gpa-highlight-inner {
  width: 100%;
  max-width: 420px;
  background: transparent;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: none;
  border: none;
}

.gpa-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 4px;
  border-bottom: none;
}

.gpa-icon {
  color: var(--accent);
  font-size: 20px;
}

.gpa-title {
  font-size: 18px;
  color: #666;
  font-weight: 600;
}

.gpa-value {
  margin-top: 18px;
  font-size: 48px;
  font-weight: 800;
  color: var(--accent);
  line-height: 1;
}

.gpa-sub {
  margin-top: 8px;
  color: #9aa0a6;
}
</style>
