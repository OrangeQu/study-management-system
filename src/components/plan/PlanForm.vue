<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="dialogWidth"
    :close-on-click-modal="false"
    @close="handleClose"
    custom-class="card-tone card-tone--peach"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
      class="plan-form"
    >
      <el-form-item label="时间安排" required>
        <div class="time-range">
          <el-time-select
            v-model="formData.time_start"
            placeholder="开始时间"
            start="08:00"
            step="00:30"
            end="22:00"
            style="width: 48%"
          />
          <span class="time-separator">至</span>
          <el-time-select
            v-model="formData.time_end"
            :min-time="formData.time_start"
            placeholder="结束时间"
            start="08:00"
            step="00:30"
            end="22:00"
            style="width: 48%"
          />
        </div>
      </el-form-item>

      <el-form-item label="计划标题" prop="task_title">
        <el-input
          v-model="formData.task_title"
          placeholder="请输入计划标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="关联任务">
        <el-select
          v-model="formData.task_id"
          placeholder="选择关联任务（可选）"
          filterable
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="task in availableTasks"
            :key="task.id"
            :label="task.title"
            :value="task.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="课程名称">
        <el-input
          v-model="formData.course"
          placeholder="请输入课程名称（可选）"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="计划描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入计划描述（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ submitText }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, defineProps, defineEmits } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  plan: {
    type: Object,
    default: null
  },
  mode: {
    type: String,
    default: 'create'
  },
  tasks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'update:modelValue',
  'submit',
  'close'
])

const visible = ref(props.modelValue)
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const formData = ref({
  time_start: '08:00',
  time_end: '09:00',
  task_title: '',
  task_id: null,
  course: '',
  description: ''
})

// 验证规则
const rules = {
  task_title: [
    { required: true, message: '请输入计划标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在2到100个字符', trigger: 'blur' }
  ]
}

// 计算属性
const title = computed(() => {
  return props.mode === 'create' ? '添加计划' : '编辑计划'
})

const submitText = computed(() => {
  return props.mode === 'create' ? '添加' : '更新'
})

const dialogWidth = computed(() => {
  return window.innerWidth < 768 ? '90%' : '500px'
})

// 可用任务（未完成的任务）
const availableTasks = computed(() => {
  return props.tasks.filter(task => task.status !== 'done')
})

// 监听visible变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    initFormData()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 初始化表单数据
const initFormData = () => {
  if (props.plan && props.mode === 'edit') {
    formData.value = {
      time_start: props.plan.time_start || '08:00',
      time_end: props.plan.time_end || '09:00',
      task_title: props.plan.task_title || '',
      task_id: props.plan.task_id || null,
      course: props.plan.course || '',
      description: props.plan.description || ''
    }
  } else {
    formData.value = {
      time_start: '08:00',
      time_end: '09:00',
      task_title: '',
      task_id: null,
      course: '',
      description: ''
    }
  }
}

// 事件处理
const handleClose = () => {
  visible.value = false
  emit('close')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 验证时间
    if (formData.value.time_start && formData.value.time_end) {
      const startTime = dayjs(`2000-01-01 ${formData.value.time_start}`)
      const endTime = dayjs(`2000-01-01 ${formData.value.time_end}`)
      
      if (endTime.isBefore(startTime)) {
        ElMessage.error('结束时间不能早于开始时间')
        return
      }
      
      if (endTime.diff(startTime, 'minute') < 30) {
        ElMessage.error('持续时间至少30分钟')
        return
      }
    }
    
    submitting.value = true
    
    // 准备提交数据
    const submitData = {
      ...formData.value,
      date: dayjs().format('YYYY-MM-DD')
    }
    
    emit('submit', submitData)
    handleClose()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 初始化
initFormData()

</script>

<style scoped>

.plan-form {
  padding-right: 10px;
}

.time-range {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.time-separator {
  margin: 0 8px;
  color: #999;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .plan-form {
    padding-right: 0;
  }
  
  .time-range {
    flex-direction: column;
    gap: 10px;
  }
  
  .time-separator {
    margin: 0;
  }
  
  .time-range .el-time-select {
    width: 100% !important;
  }
}
</style>
