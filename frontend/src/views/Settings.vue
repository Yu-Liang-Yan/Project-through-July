<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { BiometricRecord } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { Camera, Fingerprint, Mic, Keyboard, Shield, Download, Trash2, ToggleLeft, ToggleRight, Key, User, Phone } from '@lucide/vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const biometrics = ref<BiometricRecord[]>([])
const passwordInput = ref('')
const showPasswordVerify = ref(false)

// Password change
const showChangePassword = ref(false)
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

// Profile edit
const showEditProfile = ref(false)
const editPhone = ref('')
const editAgeGroup = ref('')

const biometricTypes = [
  { type: 'FACE', label: '人脸识别', icon: Camera, priority: 1 },
  { type: 'FINGERPRINT', label: '指纹识别', icon: Fingerprint, priority: 3 },
  { type: 'VOICEPRINT', label: '声纹识别', icon: Mic, priority: 4 },
  { type: 'KEYSTROKE', label: '按键习惯', icon: Keyboard, priority: 5 }
]

const getBiometricStatus = (type: string) => {
  return biometrics.value.find(b => b.type === type)?.status === 'ACTIVE'
}

const { toast } = useToast()

const loadBiometrics = async () => {
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.biometrics.list(uid)
    if (res.success && res.data) biometrics.value = res.data
  } catch (e) { console.error(e) }
}

const toggleBiometric = async (type: string) => {
  const uid = userStore.currentUser?.id ?? 1
  const current = biometrics.value.find(b => b.type === type)
  try {
    if (current) {
      const res = await api.biometrics.setStatus(current.id, current.status !== 'ACTIVE')
      if (res.success) { toast('操作成功', 'success'); await loadBiometrics() }
    } else {
      const res = await api.biometrics.register(uid, type, 0.85)
      if (res.success) { toast('已注册', 'success'); await loadBiometrics() }
    }
  } catch (e) { toast('操作失败', 'error') }
}

const handleChangePassword = async () => {
  if (!oldPassword.value || !newPassword.value) { toast('请填写所有字段', 'warning'); return }
  if (newPassword.value !== confirmPassword.value) { toast('两次密码不一致', 'warning'); return }
  if (newPassword.value.length < 6) { toast('密码至少6位', 'warning'); return }
  try {
    const uid = userStore.currentUser?.id ?? 1
    const res = await api.auth.changePassword(uid, oldPassword.value, newPassword.value)
    if (res.success) {
      toast('密码修改成功', 'success')
      showChangePassword.value = false
      oldPassword.value = ''; newPassword.value = ''; confirmPassword.value = ''
    } else {
      toast(res.message || '修改失败', 'error')
    }
  } catch (e) { toast('修改失败', 'error') }
}

const loadProfileData = () => {
  editPhone.value = userStore.currentUser?.phone || ''
  editAgeGroup.value = userStore.currentUser?.ageGroup || ''
  showEditProfile.value = true
}

const handleSaveProfile = async () => {
  try {
    const uid = userStore.currentUser?.id ?? 1
    const res = await api.users.updateProfile(uid, { phone: editPhone.value, ageGroup: editAgeGroup.value })
    if (res.success) {
      toast('资料更新成功', 'success')
      userStore.loadFromStorage()
      showEditProfile.value = false
    } else {
      toast(res.message || '更新失败', 'error')
    }
  } catch (e) { toast('更新失败', 'error') }
}

const verifyPasswordThenAct = async (action: () => void) => {
  if (!passwordInput.value) { toast('请输入密码', 'warning'); return }
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.auth.verifyPassword(uid, passwordInput.value)
    if (res.success && res.data) {
      passwordInput.value = ''; showPasswordVerify.value = false
      action()
    } else { toast('密码验证失败', 'error') }
  } catch (e) { toast('验证失败', 'error') }
}

onMounted(async () => {
  userStore.loadFromStorage()
  if (!userStore.isLoggedIn) { router.push('/'); return }
  await loadBiometrics()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Sidebar />
    <div class="lg:ml-64">
      <Header title="设置" subtitle="管理生物特征、安全配置和个人资料" />

      <div class="p-4 md:p-6 space-y-6">
        <!-- 个人资料 -->
        <div class="bg-white rounded-xl shadow-sm p-4 md:p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <User class="w-5 h-5 text-primary-600" /> 个人资料
          </h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
              <User class="w-5 h-5 text-gray-400" />
              <div><p class="text-xs text-gray-500">用户名</p><p class="font-medium">{{ userStore.currentUser?.username }}</p></div>
            </div>
            <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
              <Phone class="w-5 h-5 text-gray-400" />
              <div><p class="text-xs text-gray-500">手机号</p><p class="font-medium">{{ userStore.currentUser?.phone || '未设置' }}</p></div>
            </div>
          </div>
          <button @click="loadProfileData" class="mt-4 px-4 py-2 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700">
            编辑资料
          </button>
        </div>

        <!-- 密码修改 -->
        <div class="bg-white rounded-xl shadow-sm p-4 md:p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <Key class="w-5 h-5 text-primary-600" /> 账户安全
          </h3>
          <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div><p class="font-medium text-gray-800">登录密码</p><p class="text-sm text-gray-500">修改账户登录密码</p></div>
            <button @click="showChangePassword = true" class="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200">修改</button>
          </div>
        </div>

        <!-- 生物特征管理 -->
        <div class="bg-white rounded-xl shadow-sm p-4 md:p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-2 flex items-center gap-2">
            <Camera class="w-5 h-5 text-primary-600" /> 生物特征管理
          </h3>
          <p class="text-sm text-gray-500 mb-4">按优先级排序：人脸 &gt; 虹膜 &gt; 指纹 &gt; 声纹 &gt; 按键习惯</p>
          <div class="space-y-3">
            <div v-for="bio in biometricTypes" :key="bio.type" class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50">
              <div class="flex items-center gap-3">
                <div :class="['w-10 h-10 rounded-lg flex items-center justify-center', getBiometricStatus(bio.type) ? 'bg-green-100' : 'bg-gray-100']">
                  <component :is="bio.icon" :class="['w-5 h-5', getBiometricStatus(bio.type) ? 'text-green-600' : 'text-gray-400']" />
                </div>
                <div><p class="font-medium text-gray-800">{{ bio.label }}</p><p class="text-xs text-gray-500">优先级权重: #{{ bio.priority }}</p></div>
              </div>
              <button @click="toggleBiometric(bio.type)" class="text-gray-400 hover:text-primary-600">
                <ToggleRight v-if="getBiometricStatus(bio.type)" class="w-7 h-7 text-green-500" />
                <ToggleLeft v-else class="w-7 h-7" />
              </button>
            </div>
          </div>
        </div>

        <!-- 安全设置 -->
        <div class="bg-white rounded-xl shadow-sm p-4 md:p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <Shield class="w-5 h-5 text-primary-600" /> 安全设置
          </h3>
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
              <div><p class="font-medium text-gray-800">双重验证</p><p class="text-sm text-gray-500">修改管控规则需要密码 + 生物特征双重确认</p></div>
              <ToggleRight class="w-7 h-7 text-green-500" />
            </div>
            <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
              <div><p class="font-medium text-gray-800">防反控制</p><p class="text-sm text-gray-500">连续验证失败自动锁定设备</p></div>
              <ToggleRight class="w-7 h-7 text-green-500" />
            </div>
            <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
              <div><p class="font-medium text-gray-800">自动告警</p><p class="text-sm text-gray-500">检测到禁止操作时自动告警</p></div>
              <ToggleRight class="w-7 h-7 text-green-500" />
            </div>
          </div>
        </div>

        <!-- 数据管理 -->
        <div class="bg-white rounded-xl shadow-sm p-4 md:p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <Download class="w-5 h-5 text-primary-600" /> 数据管理
          </h3>
          <div class="space-y-3">
            <button @click="showPasswordVerify = true" class="w-full flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50">
              <div class="flex items-center gap-3">
                <Download class="w-5 h-5 text-blue-500" />
                <div class="text-left"><p class="font-medium text-gray-800">导出诊断信息包</p><p class="text-sm text-gray-500">端到端加密导出</p></div>
              </div>
              <span class="text-sm text-blue-500">导出</span>
            </button>
            <button @click="showPasswordVerify = true" class="w-full flex items-center justify-between p-4 border border-red-200 rounded-lg hover:bg-red-50">
              <div class="flex items-center gap-3">
                <Trash2 class="w-5 h-5 text-red-500" />
                <div class="text-left"><p class="font-medium text-red-600">清除所有数据</p><p class="text-sm text-gray-500">清除所有使用记录和缓存</p></div>
              </div>
              <span class="text-sm text-red-500">清除</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 密码修改弹窗 -->
      <div v-if="showChangePassword" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
        <div class="bg-white rounded-xl p-6 w-full max-w-sm">
          <h3 class="text-lg font-semibold mb-4">修改密码</h3>
          <div class="space-y-3">
            <input v-model="oldPassword" type="password" placeholder="当前密码" class="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary-500" />
            <input v-model="newPassword" type="password" placeholder="新密码（至少6位）" class="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary-500" />
            <input v-model="confirmPassword" type="password" placeholder="确认新密码" class="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary-500" />
          </div>
          <div class="flex gap-3 mt-4">
            <button @click="showChangePassword = false" class="flex-1 py-3 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50">取消</button>
            <button @click="handleChangePassword" class="flex-1 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700">确认修改</button>
          </div>
        </div>
      </div>

      <!-- 资料编辑弹窗 -->
      <div v-if="showEditProfile" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
        <div class="bg-white rounded-xl p-6 w-full max-w-sm">
          <h3 class="text-lg font-semibold mb-4">编辑个人资料</h3>
          <div class="space-y-3">
            <div>
              <label class="block text-sm text-gray-600 mb-1">手机号</label>
              <input v-model="editPhone" type="text" placeholder="手机号" class="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label class="block text-sm text-gray-600 mb-1">年龄段</label>
              <select v-model="editAgeGroup" class="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary-500">
                <option value="">不设置</option>
                <option value="0-6">0-6岁</option>
                <option value="6-12">6-12岁</option>
                <option value="12-15">12-15岁</option>
                <option value="15-18">15-18岁</option>
              </select>
            </div>
          </div>
          <div class="flex gap-3 mt-4">
            <button @click="showEditProfile = false" class="flex-1 py-3 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50">取消</button>
            <button @click="handleSaveProfile" class="flex-1 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700">保存</button>
          </div>
        </div>
      </div>

      <!-- 密码验证弹窗 -->
      <div v-if="showPasswordVerify" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
        <div class="bg-white rounded-xl p-6 w-full max-w-sm">
          <h3 class="text-lg font-semibold mb-4 flex items-center gap-2"><Shield class="w-5 h-5 text-primary-600" /> 二次密码验证</h3>
          <p class="text-sm text-gray-500 mb-4">敏感操作需要密码确认</p>
          <input v-model="passwordInput" type="password" placeholder="输入密码" class="w-full px-4 py-3 border border-gray-300 rounded-lg mb-4 outline-none focus:ring-2 focus:ring-primary-500" />
          <div class="flex gap-3">
            <button @click="showPasswordVerify = false; passwordInput = ''" class="flex-1 py-3 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50">取消</button>
            <button @click="verifyPasswordThenAct(() => toast('操作已确认', 'success'))" class="flex-1 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700">确认</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
