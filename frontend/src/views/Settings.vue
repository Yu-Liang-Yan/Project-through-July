<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { BiometricRecord } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { Camera, Fingerprint, Mic, Keyboard, Shield, Download, Trash2, ToggleLeft, ToggleRight } from '@lucide/vue'

const router = useRouter()
const userStore = useUserStore()
const biometrics = ref<BiometricRecord[]>([])
const passwordInput = ref('')
const showPasswordVerify = ref(false)

const biometricTypes = [
  { type: 'FACE', label: '人脸识别', icon: Camera, priority: 1 },
  { type: 'FINGERPRINT', label: '指纹识别', icon: Fingerprint, priority: 3 },
  { type: 'VOICEPRINT', label: '声纹识别', icon: Mic, priority: 4 },
  { type: 'KEYSTROKE', label: '按键习惯', icon: Keyboard, priority: 5 }
]

const getBiometricStatus = (type: string) => {
  const record = biometrics.value.find(b => b.type === type)
  return record?.status === 'ACTIVE'
}

const toast = (msg: string, type: string) => {
  ;(window as any).showToast?.(msg, type)
}

const loadBiometrics = async () => {
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.biometrics.list(uid)
    if (res.success && res.data) biometrics.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const toggleBiometric = async (type: string) => {
  const uid = userStore.currentUser?.id ?? 1
  const current = biometrics.value.find(b => b.type === type)
  try {
    if (current) {
      const newActive = current.status !== 'ACTIVE'
      const res = await api.biometrics.setStatus(current.id, newActive)
      if (res.success) {
        toast(`${newActive ? '已启用' : '已禁用'}${biometricTypes.find(t => t.type === type)?.label}`, 'success')
        await loadBiometrics()
      }
    } else {
      // Register new
      const res = await api.biometrics.register(uid, type, 0.85)
      if (res.success) {
        toast(`${biometricTypes.find(t => t.type === type)?.label}已注册`, 'success')
        await loadBiometrics()
      }
    }
  } catch (e) {
    toast('操作失败', 'error')
  }
}

const verifyPasswordThenAct = async (action: () => void) => {
  if (!passwordInput.value) {
    toast('请输入密码', 'warning')
    return
  }
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.auth.verifyPassword(uid, passwordInput.value)
    if (res.success && res.data) {
      passwordInput.value = ''
      showPasswordVerify.value = false
      action()
    } else {
      toast('密码验证失败', 'error')
    }
  } catch (e) {
    toast('验证失败', 'error')
  }
}

const exportData = () => {
  showPasswordVerify.value = true
}

const clearAllData = () => {
  if (!confirm('此操作将清除所有本地数据，且不可恢复。确定继续吗？')) return
  showPasswordVerify.value = true
}

onMounted(async () => {
  userStore.loadFromStorage()
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }
  await loadBiometrics()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Sidebar />
    <div class="lg:ml-64">
      <Header title="设置" subtitle="管理生物特征、安全配置和数据" />

      <div class="p-6 space-y-6">
        <!-- 生物特征管理 -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-2 flex items-center gap-2">
            <Camera class="w-5 h-5 text-primary-600" />
            生物特征管理
          </h3>
          <p class="text-sm text-gray-500 mb-4">按优先级排序：人脸 &gt; 虹膜 &gt; 指纹 &gt; 声纹 &gt; 按键习惯</p>

          <div class="space-y-3">
            <div
              v-for="bio in biometricTypes"
              :key="bio.type"
              class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <div class="flex items-center gap-3">
                <div :class="['w-10 h-10 rounded-lg flex items-center justify-center', getBiometricStatus(bio.type) ? 'bg-green-100' : 'bg-gray-100']">
                  <component :is="bio.icon" :class="['w-5 h-5', getBiometricStatus(bio.type) ? 'text-green-600' : 'text-gray-400']" />
                </div>
                <div>
                  <p class="font-medium text-gray-800">{{ bio.label }}</p>
                  <p class="text-xs text-gray-500">优先级权重: #{{ bio.priority }}</p>
                </div>
              </div>
              <button @click="toggleBiometric(bio.type)" class="text-gray-400 hover:text-primary-600 transition-colors">
                <ToggleRight v-if="getBiometricStatus(bio.type)" class="w-7 h-7 text-green-500" />
                <ToggleLeft v-else class="w-7 h-7" />
              </button>
            </div>
          </div>
        </div>

        <!-- 安全设置 -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <Shield class="w-5 h-5 text-primary-600" />
            安全设置
          </h3>

          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
              <div>
                <p class="font-medium text-gray-800">双重验证</p>
                <p class="text-sm text-gray-500">修改管控规则需要密码 + 生物特征双重确认</p>
              </div>
              <ToggleRight class="w-7 h-7 text-green-500" />
            </div>

            <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
              <div>
                <p class="font-medium text-gray-800">防反控制</p>
                <p class="text-sm text-gray-500">连续验证失败自动锁定设备并提示"非法操作"</p>
              </div>
              <ToggleRight class="w-7 h-7 text-green-500" />
            </div>

            <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
              <div>
                <p class="font-medium text-gray-800">自动告警</p>
                <p class="text-sm text-gray-500">检测到禁止操作时自动向监护人发送告警</p>
              </div>
              <ToggleRight class="w-7 h-7 text-green-500" />
            </div>
          </div>
        </div>

        <!-- 数据管理 -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <Download class="w-5 h-5 text-primary-600" />
            数据管理
          </h3>
          <div class="space-y-3">
            <button @click="exportData" class="w-full flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
              <div class="flex items-center gap-3">
                <Download class="w-5 h-5 text-blue-500" />
                <div class="text-left">
                  <p class="font-medium text-gray-800">导出诊断信息包</p>
                  <p class="text-sm text-gray-500">端到端加密，通过用户自主选择方式发送</p>
                </div>
              </div>
              <span class="text-sm text-blue-500">导出</span>
            </button>
            <button @click="clearAllData" class="w-full flex items-center justify-between p-4 border border-red-200 rounded-lg hover:bg-red-50 transition-colors">
              <div class="flex items-center gap-3">
                <Trash2 class="w-5 h-5 text-red-500" />
                <div class="text-left">
                  <p class="font-medium text-red-600">清除所有数据</p>
                  <p class="text-sm text-gray-500">清除本地存储的所有使用记录、日志和缓存</p>
                </div>
              </div>
              <span class="text-sm text-red-500">清除</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 密码验证弹窗 -->
      <div v-if="showPasswordVerify" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
        <div class="bg-white rounded-xl p-6 w-full max-w-sm">
          <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
            <Shield class="w-5 h-5 text-primary-600" />
            二次密码验证
          </h3>
          <p class="text-sm text-gray-500 mb-4">敏感操作需要密码确认</p>
          <input v-model="passwordInput" type="password" placeholder="输入密码" class="w-full px-4 py-3 border border-gray-300 rounded-lg mb-4 outline-none focus:ring-2 focus:ring-primary-500" />
          <div class="flex gap-3">
            <button @click="showPasswordVerify = false; passwordInput = ''" class="flex-1 py-3 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50">取消</button>
            <button @click="verifyPasswordThenAct(() => { passwordInput = ''; showPasswordVerify = false; toast('操作已确认', 'success') })" class="flex-1 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700">确认</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
