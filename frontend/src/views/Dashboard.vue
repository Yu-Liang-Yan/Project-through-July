<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { DashboardStats } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import Card from '@/components/Card.vue'
import { Clock, ArrowRight, Users, Wifi, Bell, Activity, Shield, AlertTriangle } from '@lucide/vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)

function formatDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return h > 0 ? `${h}h ${m}m` : `${m}m`
}

const stats = ref<DashboardStats>({
  todayUsage: 0,
  deviceCount: 0,
  alertCount: 0,
  weekUsage: 0,
  bindingCount: 0,
  onlineDeviceCount: 0,
  recentAlerts: []
})

const recentActivities = ref<{ id: number; time: string; device: string; app: string; duration: string; action: string }[]>([])

const severityColor = (s: string) => {
  const map: Record<string, string> = { LOW: 'bg-gray-100 text-gray-600', MEDIUM: 'bg-yellow-100 text-yellow-700', HIGH: 'bg-orange-100 text-orange-700', CRITICAL: 'bg-red-100 text-red-700' }
  return map[s] || 'bg-gray-100 text-gray-600'
}

const typeLabel = (t: string) => {
  const map: Record<string, string> = { UNAUTHORIZED_ACCESS: '未授权访问', BLOCKED_CONTENT: '禁止内容', TIME_EXCEEDED: '时间超标', SYSTEM: '系统通知' }
  return map[t] || t
}

onMounted(async () => {
  userStore.loadFromStorage()
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }

  const uid = userStore.currentUser?.id ?? 1
  try {
    const [dashRes, usageRes] = await Promise.all([
      api.statistics.dashboard(uid),
      api.statistics.usage(uid, 'day')
    ])

    if (dashRes.success && dashRes.data) {
      stats.value = dashRes.data as DashboardStats
    }

    if (usageRes.success && usageRes.data) {
      recentActivities.value = usageRes.data.slice(0, 10).map(r => ({
        id: r.id as number,
        time: new Date(r.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        device: r.device,
        app: r.app,
        duration: formatDuration(r.duration),
        action: '正常使用'
      }))
    }
  } catch (e) {
    console.error('Dashboard fetch error:', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Sidebar />
    <div class="lg:ml-64">
      <Header title="仪表盘" subtitle="实时监控概览" />

      <div class="p-6 space-y-6">
        <!-- 加载状态 -->
        <div v-if="loading" class="text-center py-16 text-gray-500">
          <Activity class="w-12 h-12 mx-auto mb-4 animate-spin text-primary-500" />
          <p>加载仪表盘数据...</p>
        </div>

        <template v-else>
          <!-- 关键指标卡片 -->
          <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <Card title="今日使用时长" :value="formatDuration(stats.todayUsage)" :icon="Clock" />
            <Card title="绑定用户" :value="stats.bindingCount" :icon="Users" />
            <Card title="在线设备" :value="`${stats.onlineDeviceCount}/${stats.deviceCount}`" :icon="Wifi" />
            <Card title="未读告警" :value="stats.alertCount" :icon="AlertTriangle" :variant="stats.alertCount > 0 ? 'danger' : undefined" />
          </div>

          <!-- 绑定概览 + 告警摘要 -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- 绑定概览 -->
            <div class="bg-white rounded-xl shadow-sm p-6">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
                  <Shield class="w-5 h-5 text-primary-500" />
                  监护概览
                </h3>
                <button @click="router.push('/relationships')" class="text-primary-600 hover:text-primary-700 text-sm flex items-center gap-1">
                  管理 <ArrowRight class="w-3.5 h-3.5" />
                </button>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div class="bg-gray-50 rounded-lg p-4 text-center">
                  <p class="text-2xl font-bold text-primary-600">{{ stats.bindingCount }}</p>
                  <p class="text-sm text-gray-500">被保护用户</p>
                </div>
                <div class="bg-gray-50 rounded-lg p-4 text-center">
                  <p class="text-2xl font-bold text-green-600">{{ stats.onlineDeviceCount }}</p>
                  <p class="text-sm text-gray-500">在线设备</p>
                </div>
                <div class="bg-gray-50 rounded-lg p-4 text-center">
                  <p class="text-2xl font-bold text-blue-600">{{ stats.deviceCount }}</p>
                  <p class="text-sm text-gray-500">全部设备</p>
                </div>
                <div class="bg-gray-50 rounded-lg p-4 text-center">
                  <p class="text-2xl font-bold text-gray-600">{{ formatDuration(stats.weekUsage) }}</p>
                  <p class="text-sm text-gray-500">本周使用</p>
                </div>
              </div>
            </div>

            <!-- 告警摘要 -->
            <div class="bg-white rounded-xl shadow-sm p-6">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
                  <Bell class="w-5 h-5 text-red-500" />
                  最近告警
                </h3>
                <button @click="router.push('/alerts')" class="text-primary-600 hover:text-primary-700 text-sm flex items-center gap-1">
                  全部 <ArrowRight class="w-3.5 h-3.5" />
                </button>
              </div>

              <div v-if="stats.recentAlerts.length === 0" class="text-center py-6 text-gray-400">
                <Bell class="w-8 h-8 mx-auto mb-2" />
                <p class="text-sm">暂无告警</p>
              </div>

              <div v-else class="space-y-3">
                <div
                  v-for="alert in stats.recentAlerts.slice(0, 4)"
                  :key="alert.id"
                  class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg"
                >
                  <span :class="['px-2 py-0.5 rounded text-xs font-medium', severityColor(alert.severity)]">
                    {{ alert.severity }}
                  </span>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-gray-800 truncate">{{ alert.title }}</p>
                    <p class="text-xs text-gray-500">{{ alert.protectedUserName }} · {{ typeLabel(alert.type) }}</p>
                  </div>
                  <span class="text-xs text-gray-400 whitespace-nowrap">{{ new Date(alert.createdAt).toLocaleDateString('zh-CN') }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 最近活动 + 应用使用排行 -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- 最近活动 -->
            <div class="bg-white rounded-xl shadow-sm p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
                <Activity class="w-5 h-5 text-primary-500" />
                最近活动
              </h3>
              <div class="overflow-x-auto">
                <table class="w-full">
                  <thead>
                    <tr class="border-b border-gray-200">
                      <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">时间</th>
                      <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">设备</th>
                      <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">应用</th>
                      <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">时长</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="recentActivities.length === 0">
                      <td colspan="4" class="text-center py-8 text-gray-400">暂无活动记录</td>
                    </tr>
                    <tr
                      v-for="activity in recentActivities.slice(0, 5)"
                      :key="activity.id"
                      class="border-b border-gray-100 hover:bg-gray-50"
                    >
                      <td class="py-3 px-4 text-sm text-gray-600">{{ activity.time }}</td>
                      <td class="py-3 px-4 text-sm text-gray-800">{{ activity.device }}</td>
                      <td class="py-3 px-4 text-sm text-gray-800">{{ activity.app }}</td>
                      <td class="py-3 px-4 text-sm text-gray-600">{{ activity.duration }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="mt-3 text-right">
                <button @click="router.push('/statistics')" class="text-primary-600 hover:text-primary-700 text-sm font-medium flex items-center gap-1 ml-auto">
                  查看全部记录 <ArrowRight class="w-4 h-4" />
                </button>
              </div>
            </div>

            <!-- 应用使用排行 -->
            <div class="bg-white rounded-xl shadow-sm p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">应用使用排行</h3>
              <div class="space-y-4">
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center text-primary-600 font-bold">1</div>
                  <div class="flex-1">
                    <div class="flex justify-between">
                      <span class="font-medium text-gray-800">学习软件</span>
                      <span class="text-gray-500">30m</span>
                    </div>
                    <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                      <div class="bg-primary-500 h-2 rounded-full" style="width: 80%"></div>
                    </div>
                  </div>
                </div>
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-600 font-bold">2</div>
                  <div class="flex-1">
                    <div class="flex justify-between">
                      <span class="font-medium text-gray-800">视频App</span>
                      <span class="text-gray-500">20m</span>
                    </div>
                    <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                      <div class="bg-yellow-500 h-2 rounded-full" style="width: 55%"></div>
                    </div>
                  </div>
                </div>
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-600 font-bold">3</div>
                  <div class="flex-1">
                    <div class="flex justify-between">
                      <span class="font-medium text-gray-800">游戏</span>
                      <span class="text-gray-500">15m</span>
                    </div>
                    <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                      <div class="bg-red-500 h-2 rounded-full" style="width: 40%"></div>
                    </div>
                  </div>
                </div>
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-600 font-bold">4</div>
                  <div class="flex-1">
                    <div class="flex justify-between">
                      <span class="font-medium text-gray-800">浏览器</span>
                      <span class="text-gray-500">10m</span>
                    </div>
                    <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                      <div class="bg-blue-500 h-2 rounded-full" style="width: 25%"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
