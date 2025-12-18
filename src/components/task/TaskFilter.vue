<template>
  <div class="task-filter card-tone card-tone--purple">
    <!-- 搜索框 -->
    <div class="search-box">
      <el-input
        v-model="search"
        placeholder="搜索任务..."
        clearable
        size="small"
        @input="handleSearchChange"
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-conditions">
      <el-select
        v-model="status"
        placeholder="状态"
        clearable
        size="small"
        @change="handleFilterChange"
        class="filter-select"
      >
        <el-option label="未开始" value="todo" />
        <el-option label="进行中" value="doing" />
        <el-option label="已完成" value="done" />
      </el-select>

      <el-select
        v-model="priority"
        placeholder="优先级"
        clearable
        size="small"
        @change="handleFilterChange"
        class="filter-select"
      >
        <el-option label="高" :value="1" />
        <el-option label="中" :value="2" />
        <el-option label="低" :value="3" />
      </el-select>

      <el-select
        v-model="type"
        placeholder="类型"
        clearable
        size="small"
        @change="handleFilterChange"
        class="filter-select"
      >
        <el-option label="作业" value="作业" />
        <el-option label="复习" value="复习" />
        <el-option label="考试" value="考试" />
        <el-option label="生活" value="生活" />
      </el-select>
    </div>

    <!-- 清空筛选 -->
    <div v-if="hasActiveFilters" class="clear-filters">
      <el-button size="small" type="text" @click="clearAllFilters">
        清空筛选
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, defineProps, defineEmits } from 'vue'

const props = defineProps({
  search: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    default: ''
  },
  priority: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'update:search',
  'update:status',
  'update:priority',
  'update:type',
  'change'
])

// 本地状态
const search = ref(props.search)
const status = ref(props.status)
const priority = ref(props.priority)
const type = ref(props.type)

// 监听props变化
watch(() => props.search, (val) => { search.value = val })
watch(() => props.status, (val) => { status.value = val })
watch(() => props.priority, (val) => { priority.value = val })
watch(() => props.type, (val) => { type.value = val })

// 计算属性
const hasActiveFilters = computed(() => {
  return search.value || status.value || priority.value || type.value
})

// 事件处理
const handleSearchChange = () => {
  emit('update:search', search.value)
  emit('change', getFilters())
}

const handleFilterChange = () => {
  emit('update:status', status.value)
  emit('update:priority', priority.value)
  emit('update:type', type.value)
  emit('change', getFilters())
}

const getFilters = () => {
  return {
    search: search.value,
    status: status.value,
    priority: priority.value,
    type: type.value
  }
}

const clearAllFilters = () => {
  search.value = ''
  status.value = ''
  priority.value = ''
  type.value = ''
  
  emit('update:search', '')
  emit('update:status', '')
  emit('update:priority', '')
  emit('update:type', '')
  emit('change', getFilters())
}
</script>

<style scoped>
.task-filter {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 200px;
}

.search-input {
  width: 100%;
}

.filter-conditions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-select {
  width: 100px;
}

.clear-filters {
  margin-left: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .task-filter {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-conditions {
    width: 100%;
  }
  
  .filter-select {
    flex: 1;
  }
  
  .clear-filters {
    align-self: flex-end;
    margin-left: 0;
  }
}
</style>