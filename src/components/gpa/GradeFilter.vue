<template>
  <div class="grade-filter">
    <div class="filter-row">
      <!-- 学期筛选 -->
      <div class="filter-group">
        <span class="filter-label">学期:</span>
        <el-select 
          v-model="filters.semester" 
          placeholder="选择学期" 
          clearable
          size="small"
          @change="handleFilterChange"
        >
          <el-option
            v-for="semester in semesterOptions"
            :key="semester"
            :label="semester"
            :value="semester"
          />
        </el-select>
      </div>
      
      <!-- 学年筛选 -->
      <div class="filter-group">
        <span class="filter-label">学年:</span>
        <el-select 
          v-model="filters.academicYear" 
          placeholder="选择学年" 
          clearable
          size="small"
          @change="handleFilterChange"
        >
          <el-option
            v-for="year in academicYearOptions"
            :key="year"
            :label="year"
            :value="year"
          />
        </el-select>
      </div>
      
      <!-- 状态筛选 -->
      <div class="filter-group">
        <span class="filter-label">状态:</span>
        <el-select 
          v-model="filters.status" 
          placeholder="选择状态" 
          clearable
          size="small"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="已通过" value="passed" />
          <el-option label="未通过" value="failed" />
        </el-select>
      </div>
      
      <!-- 课程类型 -->
      <div class="filter-group">
        <span class="filter-label">类型:</span>
        <el-select 
          v-model="filters.courseType" 
          placeholder="课程类型" 
          clearable
          size="small"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="必修课" value="required" />
          <el-option label="选修课" value="elective" />
          <el-option label="通识课" value="general" />
        </el-select>
      </div>
    </div>
    
    <div class="filter-row">
      <!-- 学分范围 -->
      <div class="filter-group">
        <span class="filter-label">学分:</span>
        <el-select 
          v-model="filters.creditRange" 
          placeholder="学分范围" 
          clearable
          size="small"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="0-2学分" value="0-2" />
          <el-option label="2-4学分" value="2-4" />
          <el-option label="4学分以上" value="4+" />
        </el-select>
      </div>
      
      <!-- 成绩范围 -->
      <div class="filter-group">
        <span class="filter-label">成绩:</span>
        <el-select 
          v-model="filters.scoreRange" 
          placeholder="成绩范围" 
          clearable
          size="small"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="90分以上" value="90-100" />
          <el-option label="80-89分" value="80-89" />
          <el-option label="70-79分" value="70-79" />
          <el-option label="60-69分" value="60-69" />
          <el-option label="60分以下" value="0-59" />
        </el-select>
      </div>
      
      <!-- 绩点范围 -->
      <div class="filter-group">
        <span class="filter-label">绩点:</span>
        <el-select 
          v-model="filters.gpaRange" 
          placeholder="绩点范围" 
          clearable
          size="small"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="3.5-4.0" value="3.5-4.0" />
          <el-option label="3.0-3.4" value="3.0-3.4" />
          <el-option label="2.5-2.9" value="2.5-2.9" />
          <el-option label="2.0-2.4" value="2.0-2.4" />
          <el-option label="2.0以下" value="0-1.9" />
        </el-select>
      </div>
      
      <!-- 操作按钮 -->
      <div class="filter-actions">
        <el-button 
          type="text" 
          size="small" 
          @click="resetFilters"
          :icon="Refresh"
        >
          重置
        </el-button>
        <el-button 
          type="primary" 
          size="small" 
          @click="handleFilterChange"
          :icon="Search"
        >
          筛选
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  // 选项数据
  semesters: {
    type: Array,
    default: () => []
  },
  academicYears: {
    type: Array,
    default: () => []
  },
  // 初始筛选值
  initialFilters: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['filter-change'])

// 筛选条件
const filters = ref({
  semester: '',
  academicYear: '',
  status: '',
  courseType: '',
  creditRange: '',
  scoreRange: '',
  gpaRange: '',
  ...props.initialFilters
})

// 计算选项
const semesterOptions = computed(() => {
  return [...new Set(props.semesters)].sort().reverse()
})

const academicYearOptions = computed(() => {
  return [...new Set(props.academicYears)].sort().reverse()
})

// 方法
const handleFilterChange = () => {
  emit('filter-change', filters.value)
}

const resetFilters = () => {
  filters.value = {
    semester: '',
    academicYear: '',
    status: '',
    courseType: '',
    creditRange: '',
    scoreRange: '',
    gpaRange: ''
  }
  emit('filter-change', filters.value)
}

// 监听初始值变化
watch(() => props.initialFilters, (newFilters) => {
  filters.value = { ...filters.value, ...newFilters }
}, { deep: true })

// 暴露方法
defineExpose({
  getFilters: () => filters.value,
  resetFilters
})
</script>

<style scoped>
.grade-filter {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 10px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.filter-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .filter-row {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .filter-group {
    flex: 1;
    min-width: 150px;
  }
}
</style>