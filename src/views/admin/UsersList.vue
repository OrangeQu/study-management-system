<template>
  <div class="admin-users">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="actions">
        <el-input v-model="query" placeholder="搜索用户名或邮箱" size="small" @keyup.enter="loadUsers" style="width: 240px" />
        <el-button type="primary" size="small" @click="openCreate">新建用户</el-button>
      </div>
    </div>

    <el-table :data="users" style="width: 100%" :loading="loading" stripe>
      <el-table-column prop="id" label="#" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="text" size="small" @click="editUser(row)">编辑</el-button>
          <el-button type="text" size="small" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager" style="margin-top: 12px; text-align: right;">
      <el-pagination
        background
        layout="prev, pager, next"
        :page-size="pageSize"
        v-model:current-page="page"
        :total="total"
        @current-change="loadUsers"
      />
    </div>

    <user-form v-model:visible="showForm" :user="selectedUser" @saved="onSaved" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import UserForm from '@/components/admin/UserForm.vue'
import { listUsers, deleteUser } from '@/api/admin'
import { ElMessageBox, ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)
const query = ref('')
const page = ref(1)
const pageSize = 10
const total = ref(0)

const showForm = ref(false)
const selectedUser = ref(null)

const loadUsers = async (p = page.value) => {
  loading.value = true
  try {
    const resp = await listUsers({ q: query.value, page: p, pageSize })
    users.value = resp.data?.data?.list || resp.data?.data || []
    total.value = resp.data?.data?.total || users.value.length
    page.value = p
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  selectedUser.value = null
  showForm.value = true
}

const editUser = (row) => {
  selectedUser.value = { ...row }
  showForm.value = true
}

const confirmDelete = (row) => {
  ElMessageBox.confirm(`确认删除用户 ${row.username} 吗？`, '删除用户', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadUsers()
    } catch (e) {
      ElMessage.error(e.message || '删除失败')
    }
  })
}

const onSaved = () => {
  showForm.value = false
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-users .page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.admin-users .actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
