<template>
  <div class="admin-users">
    <header class="page-header">
      <div>
        <p class="eyebrow">管理后台</p>
        <h2>用户管理</h2>
        <p class="subtitle">仅管理员可见，可在此创建管理员或普通用户，并对角色和密码进行维护。</p>
      </div>
    </header>

    <section class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="query"
          size="large"
          class="search-input"
          placeholder="搜索用户名或邮箱"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button
          type="warning"
          size="large"
          class="search-button"
          @click="handleSearch"
        >
          查询
        </el-button>
      </div>

      <el-button type="success" size="large" class="create-button" @click="openCreate">
        新建用户
      </el-button>
    </section>

    <section class="table-card">
      <el-table
        :data="users"
        style="width: 100%;"
        :loading="loading"
        stripe
        border
      >
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="密码">
          <template #default>
            <span class="masked">••••••••••</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="140">
          <template #default="{ row }">
            <el-tag
              :type="row.role === 'admin' ? 'danger' : 'info'"
              effect="plain"
              class="role-badge"
              @click="openRoleDialog(row)"
            >
              {{ row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="resetPassword(row)">重置密码</el-button>
            <el-button type="text" size="small" class="danger" @click="confirmDelete(row)">删除账号</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="pageSize"
          v-model:current-page="page"
          :total="total"
          @current-change="loadUsers"
        />
      </div>
    </section>

    <el-dialog
      :model-value="roleDialogVisible"
      title="修改角色"
      @close="closeRoleDialog"
      width="420px"
      :destroy-on-close="true"
    >
      <div class="dialog-content">
        <p>请选择新的角色：</p>
        <el-select v-model="roleSelection" placeholder="请选择角色" style="width: 100%">
          <el-option label="普通用户" value="user" />
          <el-option label="管理员" value="admin" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="closeRoleDialog">取消</el-button>
        <el-button type="primary" @click="confirmRoleChange">保存</el-button>
      </template>
    </el-dialog>

    <user-form v-model:visible="showForm" :user="selectedUser" @saved="onSaved" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import UserForm from '@/components/admin/UserForm.vue'
import { listUsers, deleteUser, resetUserPassword, updateUser } from '@/api/admin'

const users = ref([])
const loading = ref(false)
const query = ref('')
const page = ref(1)
const pageSize = 8
const total = ref(0)

const showForm = ref(false)
const selectedUser = ref(null)
const roleDialogVisible = ref(false)
const roleDialogUser = ref(null)
const roleSelection = ref('user')
const loadUsers = async (current = page.value) => {
  loading.value = true
  try {
    const resp = await listUsers({ q: query.value, page: current, pageSize })
    const payload = resp.data?.data || {}
    users.value = payload.list || []
    total.value = payload.total || 0
    page.value = current
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => loadUsers(1)

const openCreate = () => {
  selectedUser.value = null
  showForm.value = true
}

const confirmDelete = (row) => {
  ElMessageBox.confirm(`确认删除用户 ${row.username} 吗？`, '删除用户', {
    confirmButtonText: '删除账号',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadUsers()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

const openRoleDialog = (row) => {
  roleDialogUser.value = { ...row }
  roleSelection.value = row.role || 'user'
  roleDialogVisible.value = true
}

const closeRoleDialog = () => {
  roleDialogVisible.value = false
  roleDialogUser.value = null
}

const confirmRoleChange = async () => {
  if (!roleDialogUser.value) return
  try {
    await updateUser(roleDialogUser.value.id, { role: roleSelection.value })
    ElMessage.success('角色更新成功')
    loadUsers()
    closeRoleDialog()
  } catch (error) {
    ElMessage.error(error.message || '更新角色失败')
  }
}

const resetPassword = (row) => {
  ElMessageBox.confirm('确认将该账号密码重置为 123456 吗？', '重置密码', {
    confirmButtonText: '确认重置',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await resetUserPassword(row.id, '123456')
      ElMessage.success('密码已重置为 123456')
    } catch (error) {
      ElMessage.error(error.message || '重置失败')
    }
  }).catch(() => {})
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
.admin-users {
  background: var(--page-bg);
  padding: 32px;
  border-radius: 20px;
  box-shadow: var(--soft-shadow);
}

.page-header {
  margin-bottom: 18px;
}

.page-header .eyebrow {
  font-size: 13px;
  color: var(--muted);
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: var(--nav-active);
  font-weight: 700;
}

.page-header .subtitle {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  min-width: 320px;
  flex: 1;
  max-width: 420px;
}

.toolbar .search-button {
  min-width: 110px;
  border-radius: 12px;
  box-shadow: 0 6px 14px rgba(255, 165, 84, 0.32);
}

.create-button {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 10px 25px rgba(94, 181, 74, 0.25);
}

.table-card {
  background: var(--card-bg);
  border-radius: 18px;
  padding: 20px;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.04);
}

.table-card ::v-deep(.el-table__header-wrapper) {
  font-weight: 600;
  background: transparent;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.masked {
  letter-spacing: 4px;
  color: var(--muted);
}

.danger {
  color: var(--accent);
}

.role-badge {
  cursor: pointer;
}

@media (max-width: 768px) {
  .admin-users {
    padding: 24px;
  }

  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-card {
    padding: 16px;
  }
}
</style>
