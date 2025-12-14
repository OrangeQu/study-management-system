
<template>
  <div class="grade-data-page">
    <div class="page-header">
      <div class="header-left">
        <h1>GPA Dashboard</h1>
        <div class="header-stats">
          <span class="stat-item">
            <span class="stat-label">Current GPA:</span>
            <span class="stat-value">{{ currentGPA.toFixed(2) }}</span>
          </span>
          <span class="stat-item">
            <span class="stat-label">Total Credits:</span>
            <span class="stat-value">{{ totalCredits }}</span>
          </span>
          <span class="stat-item">
            <span class="stat-label">Pass Rate:</span>
            <span class="stat-value">{{ passRate }}%</span>
          </span>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog" :icon="Plus">
          Add Grade
        </el-button>
        <el-button @click="exportData" :icon="Download">
          Export Data
        </el-button>
        <el-button @click="refreshData" :icon="Refresh">
          Refresh
        </el-button>
      </div>
    </div>

    <div class="stats-section">
      <div class="section-title">
        <el-icon><DataAnalysis /></el-icon>
        <h3>GPA Overview</h3>
      </div>
      <div class="gpa-stats-grid">
        <GpaStats
          title="Current Semester GPA"
          :value="currentSemesterGPA"
          label="This Semester"
          icon="Trophy"
          :icon-color="getGpaColor(currentSemesterGPA)"
          :tag="currentSemester"
        />
        
        <GpaStats
          title="Cumulative GPA"
          :value="totalGPA"
          label="All Semesters"
          icon="Star"
          :icon-color="getGpaColor(totalGPA)"
          :sub-info="`Courses: ${totalCourses}`"
        />
        
        <GpaStats
          title="Credit Progress"
          :value="passedCredits"
          label="Earned Credits"
          icon="Checked"
          icon-color="#52c41a"
          :show-progress="true"
          :percentage="creditProgress"
          :progress-current="passedCredits"
          :progress-total="totalCredits"
          :progress-color="getProgressColor(creditProgress)"
        />
        
        <GpaStats
          title="Pass Rate"
          :value="passRate"
          label="Passed"
          icon="SuccessFilled"
          icon-color="#1890ff"
          :show-progress="true"
          :percentage="passRate"
          :progress-current="passedCourses"
          :progress-total="totalCourses"
          :progress-color="getPassRateColor(passRate)"
        />
      </div>
    </div>

    <div class="chart-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon><TrendCharts /></el-icon>
          <h3>GPA Trend Analysis</h3>
        </div>
      </div>
      
      <GpaTrendChart
        :title="'Semester GPA Trend'"
        :gpa-data="gpaTrendData"
        :height="'300px'"
        @semester-click="handleSemesterClick"
      />
    </div>

    <div class="data-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon><List /></el-icon>
          <h3>Grade Records</h3>
          <span class="record-count">Total {{ totalRecords }} records</span>
        </div>
        <div class="section-actions">
          <el-button size="small" @click="showBatchImport = true" :icon="Upload">
            Batch Import
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

      <div class="table-container">
        <GradeTable
          ref="gradeTable"
          :grades="filteredGrades"
          :show-index="true"
          :show-selection="true"
          :show-actions="true"
          :show-pagination="true"
          :page-size="pageSize"
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
                placeholder="Search course name or code..."
                prefix-icon="Search"
                style="width: 300px"
                clearable
                @input="handleSearch"
              />
              <el-select 
                v-model="tableSort" 
                placeholder="Sort"
                size="small"
                style="width: 150px"
              >
                <el-option label="Semester Desc" value="semester_desc" />
                <el-option label="Semester Asc" value="semester_asc" />
                <el-option label="Score Desc" value="score_desc" />
                <el-option label="GPA Desc" value="gpa_desc" />
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
                Export Selected ({{ selectedCount }})
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
        <el-form-item label="Course Name" prop="course_name">
          <el-input 
            v-model="gradeForm.course_name" 
            placeholder="Enter course name"
          />
        </el-form-item>

        <el-form-item label="Course Code" prop="course_code">
          <el-input 
            v-model="gradeForm.course_code" 
            placeholder="e.g. CS101"
          />
        </el-form-item>

        <el-form-item label="Credit" prop="credit">
          <el-input-number 
            v-model="gradeForm.credit" 
            :min="0.5" 
            :max="10" 
            :step="0.5"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Score" prop="score">
          <el-input-number 
            v-model="gradeForm.score" 
            :min="0" 
            :max="100" 
            :step="0.5"
            controls-position="right"
            style="width: 100%"
            @change="handleScoreChange"
          />
          <div class="form-tip">Enter score (0-100), GPA will be calculated automatically</div>
        </el-form-item>

        <el-form-item label="GPA" prop="grade_point">
          <el-input-number 
            v-model="gradeForm.grade_point" 
            :min="0" 
            :max="4.0" 
            :step="0.01"
            :precision="2"
            controls-position="right"
            style="width: 100%"
            disabled
          />
        </el-form-item>

        <el-form-item label="Course Type" prop="course_type">
          <el-select 
            v-model="gradeForm.course_type" 
            placeholder="Select course type"
            style="width: 100%"
          >
            <el-option label="Required" value="required" />
            <el-option label="Elective" value="elective" />
            <el-option label="General" value="general" />
            <el-option label="Practice" value="practice" />
          </el-select>
        </el-form-item>

        <el-form-item label="Semester" prop="semester">
          <el-select 
            v-model="gradeForm.semester" 
            placeholder="Select semester"
            style="width: 100%"
          >
            <el-option 
              v-for="semester in availableSemesters" 
              :key="semester" 
              :label="semester" 
              :value="semester"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Academic Year" prop="academic_year">
          <el-input 
            v-model="gradeForm.academic_year" 
            placeholder="e.g. 2023-2024"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showGradeDialog = false">Cancel</el-button>
          <el-button type="primary" @click="submitGradeForm" :loading="submitting">
            Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showBatchImport"
      title="Batch Import"
      width="600px"
    >
      <div class="import-guide">
        <p>Please prepare an Excel/CSV file with the following columns:</p>
        <div class="format-example">
          <table>
            <thead>
              <tr>
                <th>Course Code</th>
                <th>Course Name</th>
                <th>Credit</th>
                <th>Score</th>
                <th>Semester</th>
                <th>Academic Year</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>CS101</td>
                <td>Programming Basics</td>
                <td>3.0</td>
                <td>92</td>
                <td>2023-2024-1</td>
                <td>2023-2024</td>
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
            <el-button type="primary" :icon="Upload">Select File</el-button>
          </el-upload>
          <el-button :icon="Download" @click="downloadTemplate">
            Download Template
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
  List
} from '@element-plus/icons-vue'
import { listGrades, createGrade, updateGrade, deleteGrade, statsGrades } from '@/api/grades'

import GpaStats from '@/components/gpa/GpaStats.vue'
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
  course_name: [{ required: true, message: 'Please enter course name', trigger: 'blur' }],
  course_code: [{ required: true, message: 'Please enter course code', trigger: 'blur' }],
  credit: [{ required: true, message: 'Please enter credit', trigger: 'blur' }],
  score: [{ required: true, message: 'Please enter score', trigger: 'blur' }],
  semester: [{ required: true, message: 'Please select semester', trigger: 'change' }]
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
    filtered = filtered.filter(g => 
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
    case 'gpa_desc':
      filtered.sort((a, b) => b.grade_point - a.grade_point)
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

const creditProgress = computed(() => {
  if (totalCredits.value === 0) return 0
  return Math.round((passedCredits.value / totalCredits.value) * 100)
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
  if (score >= 90) return 4.0
  if (score >= 85) return 3.7
  if (score >= 82) return 3.3
  if (score >= 78) return 3.0
  if (score >= 75) return 2.7
  if (score >= 72) return 2.3
  if (score >= 68) return 2.0
  if (score >= 64) return 1.5
  if (score >= 60) return 1.0
  return 0
}

const handleScoreChange = (score) => {
  gradeForm.value.grade_point = calculateGradePoint(score ?? gradeForm.value.score)
}

const getGpaColor = (gpa) => {
  if (gpa >= 3.5) return '#52c41a'
  if (gpa >= 3.0) return '#1890ff'
  if (gpa >= 2.5) return '#faad14'
  return '#f5222d'
}

const getProgressColor = (percentage) => {
  if (percentage >= 90) return '#52c41a'
  if (percentage >= 70) return '#1890ff'
  if (percentage >= 50) return '#faad14'
  return '#f5222d'
}

const getPassRateColor = (rate) => {
  if (rate >= 95) return '#52c41a'
  if (rate >= 85) return '#1890ff'
  if (rate >= 70) return '#faad14'
  return '#f5222d'
}

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
      `Are you sure to delete "${grade.course_name}"?`,
      'Confirm',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await deleteGrade(grade.id)
    ElMessage.success('Deleted')
    await Promise.all([loadGrades(), loadStats()])
  } catch (error) {
    console.log('Delete cancelled or failed', error)
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
      ElMessage.success('Created')
    } else {
      await updateGrade(currentGradeId.value, payload)
      ElMessage.success('Updated')
    }

    await Promise.all([loadGrades(), loadStats()])

    showGradeDialog.value = false
    resetGradeForm()
  } catch (error) {
    console.error('Submit failed:', error)
    ElMessage.error(error.message || 'Submit failed, please try again')
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

const handleSearch = (keyword) => {
  searchKeyword.value = keyword || ''
}

const handleSortChange = (sort) => {
  console.log('Sort change:', sort)
}

const handleSemesterClick = (semester) => {
  if (gradeFilter.value) {
    const filters = gradeFilter.value.getFilters()
    filters.semester = semester
    activeFilters.value = { ...filters }
    gradeFilter.value.handleFilterChange()
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const exportData = () => {
  ElMessage.info('Export is under development...')
}

const exportSelected = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('Please select at least one row')
    return
  }
  ElMessage.info(`Exporting ${selectedRows.value.length} rows`)
}

const refreshData = async () => {
  await Promise.all([loadGrades(), loadStats()])
  ElMessage.success('Data refreshed')
}

const handleFileUpload = (file) => {
  ElMessage.info(`File ${file.name} uploaded, parsing...`)
  return false
}

const downloadTemplate = () => {
  ElMessage.info('Downloading template')
}

const getCurrentAcademicYear = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  if (month >= 9) {
    return year + '-' + (year + 1)
  } else {
    return (year - 1) + '-' + year
  }
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
    ElMessage.error(error.message || 'Failed to load grades')
  }
}

const loadStats = async () => {
  try {
    const resp = await statsGrades()
    if (resp?.data?.code === 0) {
      stats.value = resp.data.data || {}
    }
  } catch (error) {
    ElMessage.error(error.message || 'Failed to load stats')
  }
}

onMounted(async () => {
  await Promise.all([loadGrades(), loadStats()])
  gradeForm.value.semester = currentSemester.value
  gradeForm.value.academic_year = getCurrentAcademicYear()
})
</script>
<style scoped>
/* 固定宽度布局，参考首页Dashboard */
.grade-data-page {
  width: 1200px;
  margin: 0 auto;
  padding: 20px 0;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 页面标题 */
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

/* 各区域通用样式 - 使用固定布局 */
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
  font-weight: normal;
}

/* GPA统计卡片 - 固定宽度网格 */
.gpa-stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 10px;
  width: 100%;
}
.gpa-stats-grid :deep(.gpa-stat-card) {
  height: 180px !important; /* 增加高度，原来是140px */
}


/* 图表区域 */
.chart-section {
  min-height: 400px;
  width: 100%;
}

/* 数据管理区域 */
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

/* 对话框样式 */
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

/* 批量导入样式 */
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


</style>
