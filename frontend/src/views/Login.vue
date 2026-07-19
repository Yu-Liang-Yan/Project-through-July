<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import { Shield, User, Lock, Phone, Camera, Fingerprint, Mic, Eye, EyeOff } from '@lucide/vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const { toast } = useToast()

const activeTab = ref<'login' | 'register'>('login')
const showPassword = ref(false)
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  userType: 'guardian' as 'guardian' | 'protected',
  ageGroup: ''
})

onMounted(() => {
  userStore.loadFromStorage()
  if (userStore.isLoggedIn) {
    router.push('/dashboard')
  }
})

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    toast('请填写完整信息', 'warning')
    return
  }

  loading.value = true
  try {
    const res = await api.auth.login(loginForm.value.username, loginForm.value.password)
    if (res.success && res.data) {
      const d = res.data
      userStore.login({
        id: d.id,
        username: d.username,
        phone: d.phone,
        userType: d.userType.toLowerCase() as 'guardian' | 'protected',
        ageGroup: d.ageGroup,
        createdAt: d.createdAt
      }, d.token)
      toast('登录成功！', 'success')
      router.push('/dashboard')
    } else {
      toast(res.message || '登录失败', 'error')
    }
  } catch (e) {
    toast('网络错误，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.phone || !registerForm.value.password) {
    toast('请填写完整信息', 'warning')
    return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    toast('两次密码不一致', 'error')
    return
  }

  const ageGroupMap: Record<string, string> = {
    '6岁以下': '0-6', '6-12岁': '6-12', '12-15岁': '12-15', '15-18岁': '15-18'
  }

  loading.value = true
  try {
    const res = await api.auth.register({
      username: registerForm.value.username,
      phone: registerForm.value.phone,
      password: registerForm.value.password,
      userType: registerForm.value.userType.toUpperCase(),
      ageGroup: registerForm.value.userType === 'protected' ? (ageGroupMap[registerForm.value.ageGroup] || '') : undefined
    })
    if (res.success && res.data) {
      // 注册后自动登录
      const loginRes = await api.auth.login(registerForm.value.username, registerForm.value.password)
      if (loginRes.success && loginRes.data) {
        userStore.login({
          id: loginRes.data.id,
          username: loginRes.data.username,
          phone: loginRes.data.phone,
          userType: loginRes.data.userType.toLowerCase() as 'guardian' | 'protected',
          ageGroup: loginRes.data.ageGroup,
          createdAt: loginRes.data.createdAt
        }, loginRes.data.token)
        toast('注册成功！', 'success')
        router.push('/dashboard')
      }
    } else {
      toast(res.message || '注册失败', 'error')
    }
  } catch (e) {
    toast('网络错误，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

const startBiometric = (type: string) => {
  if (!loginForm.value.username) {
    toast('请先输入用户名', 'warning')
    return
  }
  toast(`${type === 'face' ? '人脸' : type === 'fingerprint' ? '指纹' : '声纹'}识别中...`, 'info')
  setTimeout(() => {
    userStore.login({
      id: 1,
      username: loginForm.value.username,
      phone: '13800138000',
      userType: 'guardian',
      createdAt: new Date().toISOString()
    }, '')
    toast('生物识别验证成功！', 'success')
    router.push('/dashboard')
  }, 2000)
}

const ageGroups = ['6岁以下', '6-12岁', '12-15岁', '15-18岁']
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-500 via-purple-500 to-secondary-500 p-4">
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
      <div class="text-center mb-8">
        <div class="w-16 h-16 bg-primary-500 rounded-xl flex items-center justify-center mx-auto mb-4">
          <Shield class="w-8 h-8 text-white" />
        </div>
        <h1 class="text-2xl font-bold text-gray-800">守护宝贝</h1>
        <p class="text-gray-500 mt-2">智能监护系统</p>
      </div>

      <div class="flex mb-6">
        <button
          @click="activeTab = 'login'"
          :class="[
            'flex-1 py-3 font-medium rounded-lg transition-colors',
            activeTab === 'login' ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          登录
        </button>
        <button
          @click="activeTab = 'register'"
          :class="[
            'flex-1 py-3 font-medium rounded-lg transition-colors',
            activeTab === 'register' ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          注册
        </button>
      </div>

      <div v-if="activeTab === 'login'" class="space-y-4">
        <div class="relative">
          <User class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="loginForm.username"
            type="text"
            placeholder="用户名/手机号"
            class="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          />
        </div>

        <div class="relative">
          <Lock class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="loginForm.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="密码"
            class="w-full pl-12 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          />
          <button
            @click="showPassword = !showPassword"
            class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            <Eye v-if="showPassword" class="w-5 h-5" />
            <EyeOff v-else class="w-5 h-5" />
          </button>
        </div>

        <div class="pt-2">
          <p class="text-sm text-gray-500 mb-3">生物识别登录：</p>
          <div class="grid grid-cols-3 gap-3">
            <button
              @click="startBiometric('face')"
              class="flex flex-col items-center gap-2 p-4 border border-gray-200 rounded-lg hover:bg-primary-50 hover:border-primary-200 transition-colors"
            >
              <Camera class="w-6 h-6 text-primary-500" />
              <span class="text-sm text-gray-600">人脸</span>
            </button>
            <button
              @click="startBiometric('fingerprint')"
              class="flex flex-col items-center gap-2 p-4 border border-gray-200 rounded-lg hover:bg-primary-50 hover:border-primary-200 transition-colors"
            >
              <Fingerprint class="w-6 h-6 text-primary-500" />
              <span class="text-sm text-gray-600">指纹</span>
            </button>
            <button
              @click="startBiometric('voice')"
              class="flex flex-col items-center gap-2 p-4 border border-gray-200 rounded-lg hover:bg-primary-50 hover:border-primary-200 transition-colors"
            >
              <Mic class="w-6 h-6 text-primary-500" />
              <span class="text-sm text-gray-600">声纹</span>
            </button>
          </div>
        </div>

        <button
          @click="handleLogin"
          :disabled="loading"
          class="w-full py-3 bg-primary-500 text-white font-medium rounded-lg hover:bg-primary-600 transition-colors disabled:opacity-50"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <p class="text-center text-sm text-gray-500">
          演示账号：admin / 123456
        </p>
      </div>

      <div v-else class="space-y-4">
        <div class="relative">
          <User class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="registerForm.username"
            type="text"
            placeholder="用户名"
            class="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          />
        </div>

        <div class="relative">
          <Phone class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="registerForm.phone"
            type="tel"
            placeholder="手机号"
            class="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          />
        </div>

        <div class="relative">
          <Lock class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="registerForm.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="密码"
            class="w-full pl-12 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          />
          <button
            @click="showPassword = !showPassword"
            class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            <Eye v-if="showPassword" class="w-5 h-5" />
            <EyeOff v-else class="w-5 h-5" />
          </button>
        </div>

        <div class="relative">
          <Lock class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            class="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          />
        </div>

        <div>
          <p class="text-sm text-gray-500 mb-2">注册身份：</p>
          <div class="flex gap-4">
            <label class="flex items-center gap-2">
              <input
                v-model="registerForm.userType"
                type="radio"
                value="guardian"
                class="text-primary-500"
              />
              <span class="text-gray-600">监护人</span>
            </label>
            <label class="flex items-center gap-2">
              <input
                v-model="registerForm.userType"
                type="radio"
                value="protected"
                class="text-primary-500"
              />
              <span class="text-gray-600">被保护对象</span>
            </label>
          </div>
        </div>

        <div v-if="registerForm.userType === 'protected'">
          <p class="text-sm text-gray-500 mb-2">年龄层：</p>
          <select v-model="registerForm.ageGroup" class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none">
            <option value="">请选择年龄层</option>
            <option v-for="group in ageGroups" :key="group" :value="group">{{ group }}</option>
          </select>
        </div>

        <button
          @click="handleRegister"
          :disabled="loading"
          class="w-full py-3 bg-primary-500 text-white font-medium rounded-lg hover:bg-primary-600 transition-colors disabled:opacity-50"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </div>
    </div>
  </div>
</template>
