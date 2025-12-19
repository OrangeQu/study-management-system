<template>
  <el-dialog :model-value="visible" title="用户" width="480px" @close="close">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="form.role" placeholder="选择角色">
          <el-option label="管理员" value="admin" />
          <el-option label="用户" value="user" />
        </el-select>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" placeholder="不填保持不变" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { createUser, updateUser } from '@/api/admin'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  user: { type: Object, default: null }
})
const emits = defineEmits(['update:visible', 'saved'])

const formRef = ref(null)
const form = ref({ username: '', nickname: '', email: '', role: 'user', password: '' })

const rules = {
  username: [ { required: true, message: '用户名不能为空', trigger: 'blur' } ],
  email: [ { type: 'email', message: '邮箱格式不正确', trigger: 'blur' } ]
}

watch(() => props.user, (v) => {
  if (v) {
    form.value = { ...v, password: '' }
  } else {
    form.value = { username: '', nickname: '', email: '', role: 'user', password: '' }
  }
}, { immediate: true })

const close = () => {
  emits('update:visible', false)
}

const save = async () => {
  try {
    await formRef.value.validate()
    if (props.user && props.user.id) {
      await updateUser(props.user.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createUser(form.value)
      ElMessage.success('创建成功')
    }
    emits('saved')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}
</script>

<style scoped>
</style>
