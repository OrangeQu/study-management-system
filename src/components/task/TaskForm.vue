<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="dialogWidth"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
      class="task-form"
    >
      <el-form-item label="任务标题" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="请输入任务标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="任务类型" prop="type">
        <el-select
          v-model="formData.type"
          placeholder="请选择任务类型"
          style="width: 100%"
        >
          <el-option label="作业" value="作业" />
          <el-option label="复习" value="复习" />
          <el-option label="考试" value="考试" />
          <el-option label="生活" value="生活" />
        </el-select>
      </el-form-item>

      <el-form-item label="优先级" prop="priority">
        <el-select
          v-model="formData.priority"
          placeholder="请选择优先级"
          style="width: 100%"
        >
          <el-option label="高" :value="1" />
          <el-option label="中" :value="2" />
          <el-option label="低" :value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="截止时间" prop="deadline">
        <el-date-picker
          v-model="formData.deadline"
          type="datetime"
          placeholder="选择截止时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="任务描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入任务描述（可选）"
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


const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  task: {
    type: Object,
    default: null
  },
  mode: {
    type: String,
    default: 'create'
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
  title: '',
  type: '作业',
  priority: 2,
  deadline: '',
  description: ''
})

// 验证规则
const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在2到100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  deadline: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ]
}

// 计算属性
const title = computed(() => {
  return props.mode === 'create' ? '新建任务' : '编辑任务'
})

const submitText = computed(() => {
  return props.mode === 'create' ? '创建' : '更新'
})

const dialogWidth = computed(() => {
  return window.innerWidth < 768 ? '90%' : '500px'
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
  if (props.task && props.mode === 'edit') {
    formData.value = {
      title: props.task.title || '',
      type: props.task.type || '作业',
      priority: props.task.priority || 2,
      deadline: props.task.deadline || '',
      description: props.task.description || ''
    }
  } else {
    formData.value = {
      title: '',
      type: '作业',
      priority: 2,
      deadline: '',
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
    
    submitting.value = true
    
    // 准备提交数据
    const submitData = { ...formData.value }
    
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
.task-form {
  padding-right: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .task-form {
    padding-right: 0;
  }
}
</style>
