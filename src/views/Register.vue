<template>
  <div class="auth-container">
    <div class="auth-wrapper">
      <div class="auth-decoration">
        <div class="decoration-content">
          <div class="decoration-logo">
            <div class="logo-circle">
              <el-icon size="40"><UserFilled /></el-icon>
            </div>
          </div>
          <h2 class="decoration-title">加入我们</h2>
          <p class="decoration-desc">注册账号开始管理学生信息，享受便捷的管理体验</p>
          <div class="decoration-features">
            <div class="feature-item">
              <el-icon size="20" color="var(--soft-green-600)"><CircleCheckFilled /></el-icon>
              <span>全面学生管理</span>
            </div>
            <div class="feature-item">
              <el-icon size="20" color="var(--soft-green-600)"><CircleCheckFilled /></el-icon>
              <span>智能数据分析</span>
            </div>
            <div class="feature-item">
              <el-icon size="20" color="var(--soft-green-600)"><CircleCheckFilled /></el-icon>
              <span>多角色权限控制</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="auth-card">
        <div class="auth-header">
          <h1 class="auth-title">创建账户</h1>
          <p class="auth-subtitle">填写信息完成注册</p>
        </div>
        
        <el-form 
          :model="registerForm" 
          :rules="rules"
          ref="registerFormRef"
          label-width="0"
          class="auth-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
              class="auth-input"
            />
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱"
              size="large"
              :prefix-icon="Message"
              class="auth-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码（至少6位）"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="auth-input"
              @keyup.enter="handleRegister"
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="auth-input"
              @keyup.enter="handleRegister"
            />
          </el-form-item>
          
          <el-form-item prop="agreement">
            <el-checkbox v-model="registerForm.agreement">
              我已阅读并同意
              <el-link type="primary" :underline="false">《服务协议》</el-link>
              和
              <el-link type="primary" :underline="false">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              @click="handleRegister"
              size="large"
              class="auth-button"
              :loading="loading"
            >
              注册
            </el-button>
          </el-form-item>
          
          <div class="auth-footer">
            <span class="auth-tip">已有账户？</span>
            <el-link type="primary" @click="goToLogin" class="auth-link">
              立即登录
            </el-link>
          </div>
        </el-form>
      </div>
    </div>
    
    <div class="wave-container">
      <svg class="wave" viewBox="0 0 1440 320" preserveAspectRatio="none">
        <path fill="var(--primary)" fill-opacity="0.1" d="M0,224L48,213.3C96,203,192,181,288,181.3C384,181,480,203,576,192C672,181,768,139,864,128C960,117,1056,139,1152,149.3C1248,160,1344,160,1392,160L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
      </svg>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, 
  Lock, 
  Message, 
  UserFilled, 
  CircleCheckFilled 
} from '@element-plus/icons-vue'
import { register as registerApi, login as loginApi } from '@/api/auth'
import { useMainStore } from '@/stores'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)
const store = useMainStore()

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

const validatePassword = (rule, value, callback) => {
  if (!value || value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    callback(new Error('请输入有效的邮箱地址'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意服务协议和隐私政策'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true

    const resp = await registerApi({
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email
    })
    if (resp.data.code !== 0) {
      throw new Error(resp.data.message || '注册失败')
    }

    await ElMessageBox.confirm(
      '注册成功，是否立即登录？',
      '注册成功',
      {
        confirmButtonText: '立即登录',
        cancelButtonText: '稍后登录',
        type: 'success',
        customClass: 'custom-message-box'
      }
    )

    // 自动登录
    const loginResp = await loginApi({
      username: registerForm.username,
      password: registerForm.password
    })
    if (loginResp.data.code !== 0) {
      throw new Error(loginResp.data.message || '自动登录失败')
    }
    const { token, user } = loginResp.data.data
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(user))
    store.setUserInfo(user)
    router.push('/dashboard')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--page-bg);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.auth-wrapper {
  display: flex;
  max-width: 900px;
  width: 100%;
  background: var(--card-bg);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--soft-shadow);
  position: relative;
  z-index: 2;
}

.auth-card {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-header {
  margin-bottom: 40px;
  text-align: center;
}

.auth-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--primary);
  margin-bottom: 8px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.auth-subtitle {
  font-size: 14px;
  color: var(--muted);
}

.auth-form {
  width: 100%;
}

.auth-input {
  width: 100%;
}

.auth-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 0 15px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s;
}

.auth-input :deep(.el-input__wrapper:hover),
.auth-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(44,199,183,0.08);
}

.auth-input :deep(.el-input__prefix) {
  margin-right: 10px;
  color: var(--primary);
}

.auth-button {
  width: 100%;
  border-radius: 10px;
  height: 48px;
  font-size: 16px;
  margin-top: 10px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
  border: none;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(44,199,183,0.3);
}

.auth-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}

.auth-button:active {
  transform: translateY(0);
}

.auth-footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.auth-tip {
  color: #7f8c8d;
  font-size: 14px;
}

.auth-link {
  margin-left: 5px;
  font-weight: 500;
}

.auth-decoration {
  flex: 0.8;
  background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
  color: white;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.auth-decoration::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 70% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.decoration-logo {
  margin-bottom: 30px;
}

.logo-circle {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.decoration-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 15px;
  color: white;
}

.decoration-desc {
  font-size: 15px;
  opacity: 0.9;
  margin-bottom: 40px;
  line-height: 1.6;
}

.decoration-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}

.feature-item span {
  font-size: 15px;
  font-weight: 500;
}

.wave-container {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 200px;
  overflow: hidden;
  z-index: 1;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

:deep(.custom-message-box .el-message-box__content) {
  text-align: center;
}

:deep(.custom-message-box .el-message-box__btns) {
  justify-content: center;
}

@media (max-width: 768px) {
  .auth-wrapper {
    flex-direction: column;
    max-width: 400px;
  }
  
  .auth-decoration {
    order: -1;
    padding: 40px 30px;
  }
  
  .auth-card {
    padding: 40px 30px;
  }
  
  .auth-title {
    font-size: 24px;
  }
  
  .decoration-title {
    font-size: 24px;
  }
}
</style>
