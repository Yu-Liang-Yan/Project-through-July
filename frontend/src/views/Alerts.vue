<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { Alert } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { Bell, AlertTriangle, Clock, Shield, CheckCircle, Eye } from '@lucide/vue'

const router = useRouter()
const userStore = useUserStore()
const alerts = ref<Alert[]>([])
const unreadCount = ref(0)

const isGuardian = userStore.currentUser?.userType === 'guardian'

const toast = (msg: string, type: string) => {
  ;(window as any).showToast?.(msg, type)
}

const typeIcon = (type: string) => {
  const map: Record<string, any> = {
    UNAUTHORIZED_ACCESS: Shield,
    BLOCKED_CONTENT: AlertTriangle,
    TIME_EXCEEDED: Clock,
    SYSTEM: Bell
  }
  return map[type] || Bell
}

const severityColor = (severity: string) => {
  const map: Record<string, string> = {
    LOW: 'bg-gray-100 text-gray-600',
    MEDIUM: 'bg-yellow-100 text-yellow-700',
    HIGH: 'bg-orange-100 text-orange-700',
    CRITICAL: 'bg-red-100 text-red-700'
  }
  return map[severity] || ''
}

const typeLabel = (type: string) => {
  const map: Record<string, string> = {
    UNAUTHORIZED_ACCESS: '未授权访问',
    BLOCKED_CONTENT: '禁止内容',
    TIME_EXCEEDED: '时间超标',
    SYSTEM: '系统通知'
  }
  return map[type] || type
}

const loadData = async () => {
  const uid = userStore.currentUser?.id ?? 1
  try {
    if (isGuardian) {
      const [res, countRes] = await Promise.all([
        api.alerts.listForGuardian(uid),
        api.alerts.unreadCount(uid)
      ])
      if (res.success && res.data) alerts.value = res.data
      if (countRes.success && countRes.data != null) unreadCount.value = countRes.data
    } else {
      const res = await api.alerts.listForProtectedUser(uid)
      if (res.success && res.data) alerts.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const markRead = async (id: number) => {
  try {
    await api.alerts.markRead(id)
    await loadData()
  } catch (e) {
    toast('操作失败', 'error')
  }
}

const resolve = async (id: number) => {
  try {
    await api.alerts.resolve(id)
    toast('告警已解决', 'success')
    await loadData()
  } catch (e) {
    toast('操作失败', 'error')
  }
}

onMounted(async () => {
  userStore.loadFromStorage()
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }
  await loadData()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Sidebar />
    <div class="lg:ml-64">
      <Header title="告警通知" :subtitle="isGuardian ? `共 ${unreadCount} 条未读告警` : '系统通知记录'" />

      <div class="p-6">
        <div v-if="alerts.length === 0" class="text-center py-16 text-gray-500">
          <Bell class="w-16 h-16 mx-auto mb-4 text-gray-300" />
          <p class="text-lg">暂无告警通知</p>
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="alert in alerts"
            :key="alert.id"
            :class="[
              'bg-white rounded-xl shadow-sm p-5 border-l-4 transition-all',
              alert.status === 'NEW' ? 'border-l-red-500' : alert.status === 'READ' ? 'border-l-blue-500' : 'border-l-green-500'
            ]"
          >
            <div class="flex items-start gap-4">
              <div :class="['w-10 h-10 rounded-lg flex items-center justify-center', severityColor(alert.severity).split(' ')[0]]">
                <component :is="typeIcon(alert.type)" class="w-5 h-5" />
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <span :class="['px-2 py-0.5 rounded text-xs font-medium', severityColor(alert.severity)]">
                    {{ alert.severity }}
                  </span>
                  <span class="text-xs text-gray-500">{{ typeLabel(alert.type) }}</span>
                  <span class="text-xs text-gray-400">{{ new Date(alert.createdAt).toLocaleString('zh-CN') }}</span>
                </div>
                <h4 class="font-semibold text-gray-800">{{ alert.title }}</h4>
                <p class="text-sm text-gray-600 mt-1">{{ alert.message }}</p>
                <p v-if="isGuardian" class="text-xs text-gray-500 mt-1">被保护用户：{{ alert.protectedUserName }}</p>
              </div>

              <div v-if="isGuardian && alert.status !== 'RESOLVED'" class="flex gap-2 flex-shrink-0">
                <button
                  v-if="alert.status === 'NEW'"
                  @click="markRead(alert.id)"
                  class="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors"
                  title="标记已读"
                >
                  <Eye class="w-4 h-4" />
                </button>
                <button
                  @click="resolve(alert.id)"
                  class="p-2 text-green-500 hover:bg-green-50 rounded-lg transition-colors"
                  title="标记已解决"
                >
                  <CheckCircle class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
