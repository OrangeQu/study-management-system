<template>
  <div class="grade-table-container">
    <!-- 表格操作栏 -->
    <div class="table-toolbar">
      <slot name="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程..."
            prefix-icon="Search"
            style="width: 250px"
            clearable
            @input="handleSearch"
          />
        </div>
        <div class="toolbar-right">
          <slot name="actions"></slot>
        </div>
      </slot>
    </div>
    
    <!-- 成绩表格 -->
    <el-table 
      ref="tableRef"
      :data="filteredGrades" 
      stripe 
      style="width: 100%"
      :default-sort="defaultSort"
      @sort-change="handleSortChange"
      @selection-change="handleSelectionChange"
      v-bind="$attrs"
    >
      <!-- 选择列（如果启用） -->
      <el-table-column 
        v-if="showSelection" 
        type="selection" 
        width="55" 
        align="center"
      />
      
      <!-- 序号列 -->
      <el-table-column 
        v-if="showIndex" 
        type="index" 
        label="序号" 
        width="70" 
        align="center"
      />
      
      <!-- 课程信息列 -->
      <el-table-column prop="course_code" label="课程代码" width="120">
        <template #default="{ row }">
          <div class="course-code">{{ row.course_code }}</div>
        </template>
      </el-table-column>
      
      <el-table-column prop="course_name" label="课程名称" min-width="180">
        <template #default="{ row }">
          <div class="course-info">
            <div class="course-name">{{ row.course_name }}</div>
            <div class="course-meta">
              <span class="credit">学分: {{ row.credit }}</span>
              <el-tag v-if="row.is_required" size="mini" type="info">必修</el-tag>
            </div>
          </div>
        </template>
      </el-table-column>
      
      <!-- 成绩列 -->
      <el-table-column prop="score" label="成绩" width="100" align="center" sortable="custom">
        <template #default="{ row }">
          <el-tag 
            :type="getScoreType(row.score)"
            size="small"
            class="score-tag"
          >
            {{ row.score }}
          </el-tag>
        </template>
      </el-table-column>
      
      <!-- 绩点列 -->
      <el-table-column prop="grade_point" label="绩点" width="100" align="center" sortable="custom">
        <template #default="{ row }">
          <div class="grade-point-cell">
            <span class="grade-point-value">{{ row.grade_point.toFixed(2) }}</span>
            <div v-if="showGpaLevel" class="gpa-level">
              {{ getGpaLevel(row.grade_point) }}
            </div>
          </div>
        </template>
      </el-table-column>
      
      <!-- 学期信息列 -->
      <el-table-column prop="semester" label="学期" width="120" sortable="custom">
        <template #default="{ row }">
          <div class="semester-cell">
            <el-tag size="small" type="info" class="semester-tag">
              {{ row.semester }}
            </el-tag>
            <div class="academic-year">{{ row.academic_year }}</div>
          </div>
        </template>
      </el-table-column>
      
      <!-- 状态列 -->
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag 
            :type="row.status === '通过' ? 'success' : 'danger'"
            size="small"
            effect="light"
          >
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      
      <!-- 操作列 -->
      <el-table-column 
        v-if="showActions" 
        label="操作" 
        width="150" 
        align="center" 
        fixed="right"
      >
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button 
              type="text" 
              size="small" 
              @click="handleEdit(row)"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button 
              type="text" 
              size="small" 
              @click="handleDelete(row)"
              :icon="Delete"
              class="delete-btn"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
      
      <!-- 空状态 -->
      <template #empty>
        <div class="empty-table">
          <el-empty description="暂无成绩数据" :image-size="80">
            <template #description>
              <p>暂无成绩数据</p>
              <el-button type="primary" size="small" @click="handleAdd">
                添加第一条成绩
              </el-button>
            </template>
          </el-empty>
        </div>
      </template>
    </el-table>
    
    <!-- 分页 -->
    <div v-if="showPagination" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { Edit, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  // 数据
  grades: {
    type: Array,
    default: () => []
  },
  
  // 表格配置
  showIndex: {
    type: Boolean,
    default: true
  },
  showSelection: {
    type: Boolean,
    default: false
  },
  showActions: {
    type: Boolean,
    default: true
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  showGpaLevel: {
    type: Boolean,
    default: true
  },
  
  // 分页配置
  pageSize: {
    type: Number,
    default: 20
  },
  
  // 默认排序
  defaultSort: {
    type: Object,
    default: () => ({ prop: 'semester', order: 'descending' })
  }
})

const emit = defineEmits([
  'edit',
  'delete', 
  'add',
  'selection-change',
  'page-change',
  'search',
  'sort-change'
])

// 响应式数据
const tableRef = ref(null)
const searchKeyword = ref('')
const currentPage = ref(1)
const currentSort = ref(props.defaultSort)
const selectedRows = ref([])

// 使用本地变量来处理分页大小变化
const localPageSize = ref(props.pageSize)

// 计算属性
const filteredGrades = computed(() => {
  let filtered = [...props.grades]
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(g => 
      g.course_name.toLowerCase().includes(keyword) ||
      g.course_code.toLowerCase().includes(keyword) ||
      g.semester.toLowerCase().includes(keyword)
    )
  }
  
  // 排序
  if (currentSort.value.prop) {
    filtered.sort((a, b) => {
      const aValue = a[currentSort.value.prop]
      const bValue = b[currentSort.value.prop]
      
      if (currentSort.value.order === 'ascending') {
        return aValue > bValue ? 1 : -1
      } else {
        return aValue < bValue ? 1 : -1
      }
    })
  }
  
  // 分页
   if (props.showPagination) {
    const start = (currentPage.value - 1) * props.pageSize  // 这里使用props.pageSize
    const end = start + props.pageSize  // 这里使用props.pageSize
    return filtered.slice(start, end)
  }
  
  return filtered
})

const total = computed(() => props.grades.length)

// 方法
const getScoreType = (score) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'warning'
  if (score >= 60) return 'info'
  return 'danger'
}

const getGpaLevel = (gpa) => {
  if (gpa >= 3.7) return '优秀'
  if (gpa >= 3.3) return '良好'
  if (gpa >= 3.0) return '中等'
  if (gpa >= 2.0) return '及格'
  return '不及格'
}

const handleEdit = (row) => {
  emit('edit', row)
}

const handleDelete = (row) => {
  emit('delete', row)
}

const handleAdd = () => {
  emit('add')
}

const handleSearch = () => {
  emit('search', searchKeyword.value)
  currentPage.value = 1
}

const handleSortChange = (sort) => {
  currentSort.value = sort
  emit('sort-change', sort)
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

const handlePageSizeChange = (size) => {
  localPageSize.value = size  // 修改本地变量，而不是props
  emit('page-change', { page: currentPage.value, size })
}

const handlePageChange = (page) => {
  currentPage.value = page
  emit('page-change', { page, size: localPageSize.value })
}

const clearSelection = () => {
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

const getSelectedRows = () => {
  return selectedRows.value
}

const applySort = (sort) => {
  if (!sort || !sort.prop) return
  currentSort.value = {
    prop: sort.prop,
    order: sort.order || 'descending'
  }
  nextTick(() => {
    if (tableRef.value?.sort) {
      tableRef.value.sort(currentSort.value.prop, currentSort.value.order)
    }
  })
}

// 暴露方法给父组件
defineExpose({
  clearSelection,
  getSelectedRows,
  applySort
})
</script>

<!-- 样式部分保持不变 -->

<style scoped>
.grade-table-container {
  width: 100%;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px 0;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.course-info {
  display: flex;
  flex-direction: column;
}

.course-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.course-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

.course-code {
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #666;
}

.score-tag {
  font-weight: 600;
  min-width: 50px;
}

.grade-point-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.grade-point-value {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.gpa-level {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.semester-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.semester-tag {
  margin-bottom: 4px;
}

.academic-year {
  font-size: 11px;
  color: #999;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.delete-btn {
  color: #f56c6c;
}

.delete-btn:hover {
  color: #f78989;
}

.empty-table {
  padding: 40px 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 20px 0;
}
</style>
