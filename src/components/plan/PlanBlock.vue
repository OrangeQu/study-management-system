<template>
  <div 
    class="plan-block" 
    :class="[
      `source-${plan.source}`,
      toneClass,
      { 
        'is-completed': plan.completed,
        'is-editing': isEditing,
        'is-dragging': isDragging,
        'is-selected': isSelected
      }
    ]"
    :style="blockStyles"
    @click="handleClick"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    draggable="true"
    @dragstart="handleDragStart"
    @dragend="handleDragEnd"
    @dragover.prevent
    @drop="handleDrop"
  >
    <!-- 块内容 -->
    <div class="block-content">
      <!-- 拖拽手柄 -->
      <div v-if="draggable" class="drag-handle" @mousedown="startDrag">
        <el-icon><Menu /></el-icon>
      </div>
      
      <!-- 块标题和时间 -->
      <div class="block-header">
        <div class="block-title">
          <span v-if="plan.completed" class="completed-icon">
            <el-icon><CircleCheck /></el-icon>
          </span>
          <span class="title-text">{{ displayTitle }}</span>
        </div>
        
        <div class="block-time">
          <el-icon size="12"><Clock /></el-icon>
          <span>{{ formatTimeRange }}</span>
        </div>
      </div>
      
      <!-- 块描述 -->
      <div v-if="plan.description" class="block-description">
        {{ plan.description }}
      </div>
      
      <!-- 块标签 -->
      <div class="block-tags">
        <el-tag 
          size="mini" 
          :type="sourceTagType"
          class="source-tag"
        >
          {{ sourceText }}
        </el-tag>
        
        <el-tag 
          v-if="plan.course" 
          size="mini" 
          type="info"
          class="course-tag"
        >
          {{ plan.course }}
        </el-tag>
        
        <el-tag 
          v-if="plan.priority" 
          size="mini" 
          :type="priorityTagType"
          class="priority-tag"
        >
          {{ priorityText }}
        </el-tag>
      </div>
      
      <!-- 持续时间 -->
      <div class="block-duration">
        <el-icon size="12"><Timer /></el-icon>
        <span>{{ duration }} 分钟</span>
      </div>
    </div>
    
    <!-- 操作按钮（悬停显示） -->
    <div v-if="showActions && !isDragging" class="block-actions">
      <el-button 
        size="mini" 
        circle
        :icon="EditPen"
        @click.stop="handleEdit"
        class="action-btn"
      />
      <el-button 
        size="mini" 
        circle
        :icon="Delete"
        @click.stop="handleDelete"
        type="danger"
        class="action-btn"
      />
      <el-button 
        v-if="!plan.completed"
        size="mini" 
        circle
        :icon="VideoPlay"
        @click.stop="handleStart"
        type="success"
        class="action-btn"
      />
    </div>
    
    <!-- 调整大小手柄 -->
    <div 
      v-if="resizable && showActions" 
      class="resize-handle bottom"
      @mousedown="startResize('bottom')"
    >
      <el-icon size="10"><Sort /></el-icon>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>保存中...</span>
    </div>
    
    <!-- 确认删除 -->
    <div v-if="confirmingDelete" class="confirm-delete">
      <div class="confirm-text">确定删除？</div>
      <div class="confirm-actions">
        <el-button size="mini" @click.stop="cancelDelete">取消</el-button>
        <el-button size="mini" type="danger" @click.stop="confirmDelete">确定</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue'
// import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import {
  Menu,
  Clock,
  Timer,
  EditPen,
  Delete,
  VideoPlay,
  Sort,
  Loading,
  CircleCheck
} from '@element-plus/icons-vue'

const props = defineProps({
  plan: {
    type: Object,
    required: true,
    default: () => ({})
  },
  draggable: {
    type: Boolean,
    default: true
  },
  resizable: {
    type: Boolean,
    default: true
  },
  editable: {
    type: Boolean,
    default: true
  },
  selectable: {
    type: Boolean,
    default: true
  },
  showSource: {
    type: Boolean,
    default: true
  },
  compact: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'click',
  'edit',
  'delete',
  'start',
  'complete',
  'drag-start',
  'drag-end',
  'resize',
  'drop'
])

// 响应式数据
const isDragging = ref(false)
const isEditing = ref(false)
const isSelected = ref(false)
const showActions = ref(false)
const loading = ref(false)
const confirmingDelete = ref(false)
const resizeDirection = ref(null)

// 计算属性
const displayTitle = computed(() => {
  return props.plan.task_title || props.plan.title || '自由学习'
})

const formatTimeRange = computed(() => {
  return `${props.plan.time_start} - ${props.plan.time_end}`
})

const duration = computed(() => {
  if (!props.plan.time_start || !props.plan.time_end) return 0
  
  const startTime = dayjs(`2000-01-01 ${props.plan.time_start}`)
  const endTime = dayjs(`2000-01-01 ${props.plan.time_end}`)
  return endTime.diff(startTime, 'minute')
})

const sourceText = computed(() => {
  const texts = {
    'ai': 'AI生成',
    'manual': '手动调整',
    'fixed': '固定课程'
  }
  return texts[props.plan.source] || props.plan.source
})

const sourceTagType = computed(() => {
  const types = {
    'ai': 'success',
    'manual': 'info',
    'fixed': 'warning'
  }
  return types[props.plan.source] || 'info'
})

const priorityText = computed(() => {
  const texts = { 1: '高', 2: '中', 3: '低' }
  return texts[props.plan.priority] || '中'
})

const priorityTagType = computed(() => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[props.plan.priority] || 'info'
})

const blockStyles = computed(() => {
  const styles = {}
  
  // 如果已完成，降低透明度
  if (props.plan.completed) {
    styles.opacity = '0.7'
  }
  
  // 紧凑模式
  if (props.compact) {
    styles.padding = '6px'
    styles.fontSize = '12px'
  }
  
  return styles
})

const toneClass = computed(() => {
  const map = {
    ai: 'card-tone card-tone--green',
    manual: 'card-tone card-tone--purple',
    fixed: 'card-tone card-tone--peach'
  }
  return map[props.plan.source] || ''
})

// 事件处理
const handleClick = () => {
  if (props.selectable) {
    isSelected.value = !isSelected.value
  }
  emit('click', props.plan)
}

const handleMouseEnter = () => {
  showActions.value = true
}

const handleMouseLeave = () => {
  showActions.value = false
}

const handleEdit = () => {
  if (props.editable) {
    emit('edit', props.plan)
  }
}

const handleDelete = () => {
  if (confirmingDelete.value) {
    confirmDelete()
  } else {
    confirmingDelete.value = true
    setTimeout(() => {
      confirmingDelete.value = false
    }, 3000)
  }
}

const cancelDelete = () => {
  confirmingDelete.value = false
}

const confirmDelete = () => {
  confirmingDelete.value = false
  loading.value = true
  
  // 模拟删除操作
  setTimeout(() => {
    emit('delete', props.plan.id)
    loading.value = false
  }, 500)
}

const handleStart = () => {
  emit('start', props.plan)
}

const startDrag = (event) => {
  if (props.draggable) {
    isDragging.value = true
    event.stopPropagation()
  }
}

const handleDragStart = (event) => {
  if (props.draggable) {
    isDragging.value = true
    event.dataTransfer.setData('plan/id', props.plan.id)
    event.dataTransfer.setData('plan/type', props.plan.source)
    emit('drag-start', { plan: props.plan, event })
  }
}

const handleDragEnd = () => {
  isDragging.value = false
  emit('drag-end', props.plan)
}

const handleDrop = (event) => {
  event.preventDefault()
  const planId = event.dataTransfer.getData('plan/id')
  if (planId) {
    emit('drop', { targetPlan: props.plan, draggedPlanId: planId })
  }
}

const startResize = (direction) => {
  if (props.resizable) {
    resizeDirection.value = direction
    // 这里可以添加调整大小的逻辑
  }
}

// // 模拟完成操作
// const completePlan = () => {
//   loading.value = true
//   setTimeout(() => {
//     emit('complete', props.plan.id)
//     loading.value = false
//   }, 500)
// }
</script>

<style scoped>
.plan-block {
  position: absolute;
  left: 5px;
  right: 5px;
  border-radius: 8px;
  padding: 12px;
  color: var(--nav-active);
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  overflow: hidden;
  z-index: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
}

.plan-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 2;
}

.plan-block.is-dragging {
  opacity: 0.6;
  z-index: 999;
}

.plan-block.is-selected {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(43,50,80,0.06);
}

.plan-block.is-editing {
  border-color: var(--soft-green-600);
  box-shadow: 0 0 0 3px rgba(43,50,80,0.06);
}

.plan-block.is-completed {
  background: var(--page-bg) !important;
  color: var(--muted);
}

.plan-block.source-ai {
  background: linear-gradient(135deg, var(--soft-green-600) 0%, var(--soft-green) 100%);
}

.plan-block.source-manual {
  background: linear-gradient(135deg, var(--primary) 0%, rgba(44,199,183,0.18) 100%);
}

.plan-block.source-fixed {
  background: linear-gradient(135deg, var(--soft-peach-600) 0%, var(--soft-peach) 100%);
}

.block-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.drag-handle {
  position: absolute;
  top: 6px;
  left: 6px;
  opacity: 0.7;
  cursor: move;
  z-index: 1;
  transition: opacity 0.2s;
}

.drag-handle:hover {
  opacity: 1;
}

.block-header {
  margin-bottom: 8px;
  padding-left: 24px; /* 为拖拽手柄留出空间 */
}

.block-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 4px;
}

.completed-icon {
  color: var(--soft-green-600);
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.block-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  opacity: 0.9;
}

.block-description {
  flex: 1;
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.block-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.block-duration {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  opacity: 0.9;
  margin-top: auto;
}

.block-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.plan-block:hover .block-actions {
  opacity: 1;
}

.action-btn {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  color: var(--nav-active);
}

.action-btn:hover {
  background: white;
}

.resize-handle {
  position: absolute;
  cursor: ns-resize;
  opacity: 0;
  transition: opacity 0.3s;
}

.plan-block:hover .resize-handle {
  opacity: 0.8;
}

.resize-handle.bottom {
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.8);
  border-radius: 4px;
  padding: 2px;
  color: var(--nav-active);
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--nav-active);
  border-radius: 8px;
  z-index: 10;
}

.loading-icon {
  animation: rotate 1s linear infinite;
  margin-bottom: 8px;
  font-size: 20px;
  color: var(--primary);
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.confirm-delete {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  z-index: 10;
  color: var(--nav-active);
}

.confirm-text {
  font-weight: 500;
  margin-bottom: 12px;
  font-size: 14px;
}

.confirm-actions {
  display: flex;
  gap: 8px;
}

/* 紧凑模式 */
.plan-block.compact {
  padding: 6px;
}

.plan-block.compact .block-header {
  margin-bottom: 4px;
  padding-left: 20px;
}

.plan-block.compact .block-title {
  font-size: 12px;
}

.plan-block.compact .block-time,
.plan-block.compact .block-duration {
  font-size: 11px;
}

.plan-block.compact .block-description {
  font-size: 11px;
  -webkit-line-clamp: 2;
}

.plan-block.compact .block-tags {
  gap: 2px;
}

.plan-block.compact .block-tags .el-tag {
  height: 18px;
  line-height: 18px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .plan-block {
    padding: 8px;
  }
  
  .block-header {
    padding-left: 20px;
  }
  
  .block-actions {
    opacity: 1; /* 在移动端始终显示操作按钮 */
    top: 4px;
    right: 4px;
  }
  
  .block-tags {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding-bottom: 2px;
  }
  
  .block-tags::-webkit-scrollbar {
    height: 3px;
  }
}
</style>