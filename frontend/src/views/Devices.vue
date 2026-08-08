<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useDevicesStore } from '@/stores/devices'
import { api } from '@/api'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { Search, Plus, Smartphone, Tablet, Laptop, Watch, BookOpen, Trash2, RefreshCw, Lock, Unlock } from '@lucide/vue'
import { useToast } from '@/composables/useToast'
import { useWebSocket } from '@/composables/useWebSocket'

const router = useRouter()
const userStore = useUserStore()
const devicesStore = useDevicesStore()

const isScanning = ref(false)
const showAddModal = ref(false)
const searchQuery = ref('')
const newDevice = ref({
  name: '',
  type: 'phone' as 'phone' | 'tablet' | 'computer' | 'watch' | 'reader'
})

const deviceTypes = [
  { value: 'phone', label: '手机', icon: Smartphone },
  { value: 'tablet', label: '平板', icon: Tablet },
  { value: 'computer', label: '电脑', icon: Laptop },
  { value: 'watch', label: '手表', icon: Watch },
  { value: 'reader', label: '阅读器', icon: BookOpen }
]

const getDeviceIcon = (type: string) => {
  const device = deviceTypes.find(d => d.value === type)
  return device ? device.icon : Smartphone
}

const getDeviceLabel = (type: string) => {
  const device = deviceTypes.find(d => d.value === type)
  return device ? device.label : type
}

const statusClass = (s: string) => {
  return s === 'online' ? 'bg-green-100 text-green-800'
    : s === 'LOCKED' ? 'bg-orange-100 text-orange-800'
    : 'bg-red-100 text-red-800'
}

const statusDot = (s: string) => {
  return s === 'online' ? 'bg-green-500'
    : s === 'LOCKED' ? 'bg-orange-500'
    : 'bg-red-500'
}

const statusLabel = (s: string) => {
  return s === 'online' ? '在线' : s === 'LOCKED' ? '已锁定' : '离线'
}

const canToggleLock = (s: string) => s === 'online' || s === 'LOCKED'
const isLocked = (s: string) => s === 'LOCKED'

const { toast } = useToast()
const { onMessage } = useWebSocket()

const filteredDevices = computed(() => {
  const q = searchQuery.value.toLowerCase().trim()
  if (!q) return devicesStore.devices
  return devicesStore.devices.filter(d =>
    d.name.toLowerCase().includes(q) || getDeviceLabel(d.type).includes(q)
  )
})

const loadDevices = async () => {
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.devices.list(uid)
    if (res.success && res.data) {
      devicesStore.setDevices(res.data)
    }
  } catch (e) {
    console.error(e)
  }
}

const scanDevices = async () => {
  isScanning.value = true
  toast('正在局域网扫描设备...', 'info')
  try {
    const res = await api.devices.discover()
    if (res.success && res.data && res.data.length > 0) {
      const uid = userStore.currentUser?.id ?? 1
      for (const dev of res.data) {
        try {
          await api.devices.add(uid, dev.name, dev.type || 'phone')
        } catch { /* skip duplicates */ }
      }
      toast(`发现 ${res.data.length} 个设备`, 'success')
      await loadDevices()
    } else {
      toast('未发现设备（请确保 Agent 在同一局域网运行）', 'warning')
    }
  } catch (e) {
    toast('扫描失败，请检查网络连接', 'error')
  } finally {
    isScanning.value = false
  }
}

const addDevice = async () => {
  if (!newDevice.value.name) {
    toast('请输入设备名称', 'warning')
    return
  }
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.devices.add(uid, newDevice.value.name, newDevice.value.type)
    if (res.success) {
      toast('设备添加成功', 'success')
      showAddModal.value = false
      newDevice.value = { name: '', type: 'phone' }
      await loadDevices()
    }
  } catch (e) {
    toast('添加失败，请重试', 'error')
  }
}

const removeDevice = async (id: number) => {
  if (!confirm('确定要移除该设备吗？')) return
  const uid = userStore.currentUser?.id ?? 1
  try {
    await api.devices.remove(id, uid)
    devicesStore.removeDevice(id)
    toast('设备已移除', 'info')
  } catch (e) {
    toast('移除失败', 'error')
  }
}

const lockDevice = async (id: number) => {
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.devices.lock(id, uid)
    if (res.success && res.data) {
      const idx = devicesStore.devices.findIndex(d => d.id === id)
      if (idx >= 0) devicesStore.devices[idx] = res.data
      toast('设备已锁定', 'info')
    }
  } catch (e) { toast('锁定失败', 'error') }
}

const unlockDevice = async (id: number) => {
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.devices.unlock(id, uid)
    if (res.success && res.data) {
      const idx = devicesStore.devices.findIndex(d => d.id === id)
      if (idx >= 0) devicesStore.devices[idx] = res.data
      toast('设备已解锁', 'info')
    }
  } catch (e) { toast('解锁失败', 'error') }
}

onMounted(async () => {
  userStore.loadFromStorage()
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }
  devicesStore.loadFromStorage()
  await loadDevices()

  // WS auto-refresh
  onMessage((type) => {
    if (type === 'DEVICE_STATUS') loadDevices()
  })
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Sidebar />
    <div class="lg:ml-64">
      <Header title="设备管理" subtitle="管理所有受控设备" />

      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索设备..."
            class="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none w-full sm:w-64"
          />
        </div>
        <div class="flex gap-3">
          <button
            @click="scanDevices"
            :disabled="isScanning"
            class="flex items-center gap-2 px-4 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition-colors disabled:opacity-50"
          >
            <RefreshCw :class="['w-5 h-5', isScanning ? 'animate-spin' : '']" />
            {{ isScanning ? '扫描中...' : '扫描设备' }}
          </button>
          <button
            @click="showAddModal = true"
            class="flex items-center gap-2 px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
          >
            <Plus class="w-5 h-5" />
            手动添加
          </button>
        </div>
      </div>

      <div class="card">
        <div v-if="filteredDevices.length === 0" class="text-center py-12">
          <Laptop class="w-16 h-16 mx-auto text-gray-300 mb-4" />
          <p class="text-gray-500">{{ searchQuery ? '无匹配设备' : '暂无设备，请扫描或添加设备' }}</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">设备名称</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">类型</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">状态</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">最后活动</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="device in filteredDevices"
                :key="device.id"
                class="border-b border-gray-100 hover:bg-gray-50 transition-colors"
              >
                <td class="py-4 px-4">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center">
                      <component :is="getDeviceIcon(device.type)" class="w-5 h-5 text-gray-600" />
                    </div>
                    <span class="font-medium text-gray-800">{{ device.name }}</span>
                  </div>
                </td>
                <td class="py-4 px-4">
                  <span class="text-sm text-gray-600">{{ getDeviceLabel(device.type) }}</span>
                </td>
                <td class="py-4 px-4">
                  <span :class="['inline-flex items-center px-2 py-1 text-xs font-medium rounded', statusClass(device.status)]">
                    <span :class="['w-2 h-2 rounded-full mr-1', statusDot(device.status)]"></span>
                    {{ statusLabel(device.status) }}
                  </span>
                </td>
                <td class="py-4 px-4">
                  <span class="text-sm text-gray-500">{{ new Date(device.lastActive).toLocaleString() }}</span>
                </td>
                <td class="py-4 px-4">
                  <div class="flex items-center gap-1">
                    <button v-if="canToggleLock(device.status)"
                      @click="isLocked(device.status) ? unlockDevice(device.id) : lockDevice(device.id)"
                      :class="['p-2 rounded-lg transition-colors', isLocked(device.status) ? 'text-green-500 hover:bg-green-50' : 'text-orange-500 hover:bg-orange-50']"
                      :title="isLocked(device.status) ? '解锁' : '锁定'"
                    >
                      <Unlock v-if="isLocked(device.status)" class="w-5 h-5" />
                      <Lock v-else class="w-5 h-5" />
                    </button>
                    <button
                      @click="removeDevice(device.id)"
                      class="text-red-500 hover:text-red-600 p-2 hover:bg-red-50 rounded-lg transition-colors"
                      title="移除"
                    >
                      <Trash2 class="w-5 h-5" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="showAddModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-xl p-6 w-full max-w-md">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">添加设备</h3>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">设备名称</label>
              <input
                v-model="newDevice.name"
                type="text"
                placeholder="输入设备名称"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">设备类型</label>
              <div class="grid grid-cols-5 gap-2">
                <button
                  v-for="type in deviceTypes"
                  :key="type.value"
                  @click="newDevice.type = type.value as any"
                  :class="[
                    'flex flex-col items-center gap-1 p-3 border rounded-lg transition-colors',
                    newDevice.type === type.value ? 'border-primary-500 bg-primary-50 text-primary-600' : 'border-gray-200 hover:border-gray-300'
                  ]"
                >
                  <component :is="type.icon" class="w-6 h-6" />
                  <span class="text-xs">{{ type.label }}</span>
                </button>
              </div>
            </div>
          </div>

          <div class="flex gap-3 mt-6">
            <button
              @click="showAddModal = false"
              class="flex-1 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button
              @click="addDevice"
              class="flex-1 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition-colors"
            >
              添加
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
