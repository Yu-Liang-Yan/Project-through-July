<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { UsageRecord } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { Activity } from '@lucide/vue'
import { useWebSocket } from '@/composables/useWebSocket'

use([BarChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const activePeriod = ref<'day' | 'week' | 'month'>('day')
const usageRecords = ref<UsageRecord[]>([])
const { onMessage } = useWebSocket()

const periods = [{ value: 'day' as const, label: '今日' }, { value: 'week' as const, label: '本周' }, { value: 'month' as const, label: '本月' }]

function formatDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return h > 0 ? `${h}h ${m}m` : `${m}m`
}

const totalUsage = computed(() => {
  let total = 0; usageRecords.value.forEach(r => total += r.duration || 0)
  return formatDuration(total)
})

// App usage bar chart
const appChartOption = computed(() => {
  const map = new Map<string, number>()
  usageRecords.value.forEach(r => {
    const min = Math.round((r.duration || 0) / 60) || 1
    map.set(r.app, (map.get(r.app) || 0) + min)
  })
  const sorted = Array.from(map.entries()).sort((a, b) => b[1] - a[1])
  return {
    tooltip: { trigger: 'axis' as const },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category' as const, data: sorted.map(e => e[0]) },
    yAxis: { type: 'value' as const, name: '分钟' },
    series: [{
      type: 'bar' as const, data: sorted.map(e => e[1]),
      itemStyle: { borderRadius: [6, 6, 0, 0], color: '#3b82f6' }
    }]
  }
})

// Device pie chart
const deviceChartOption = computed(() => {
  const map = new Map<string, number>()
  usageRecords.value.forEach(r => {
    map.set(r.device, (map.get(r.device) || 0) + 1)
  })
  const data = Array.from(map.entries()).map(([name, value]) => ({ name, value }))
  return {
    tooltip: { trigger: 'item' as const },
    series: [{
      type: 'pie' as const, radius: ['45%', '75%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data
    }]
  }
})

const loadRecords = async () => {
  loading.value = true
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.statistics.usage(uid, activePeriod.value)
    if (res.success && res.data) usageRecords.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const changePeriod = async (p: 'day' | 'week' | 'month') => {
  activePeriod.value = p; await loadRecords()
}

onMounted(async () => {
  userStore.loadFromStorage()
  if (!userStore.isLoggedIn) { router.push('/'); return }
  await loadRecords()

  onMessage((type) => {
    if (type === 'DEVICE_STATUS' || type === 'APPROVAL_RESULT') loadRecords()
  })
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Sidebar />
    <div class="lg:ml-64">
      <Header title="使用统计" subtitle="查看设备使用情况" />

      <div class="p-4 md:p-6 space-y-6">
        <div v-if="loading" class="text-center py-16 text-gray-500">
          <Activity class="w-12 h-12 mx-auto mb-4 animate-spin text-primary-500" />
          <p>加载统计数据...</p>
        </div>

        <template v-else>
          <!-- Period selector -->
          <div class="flex gap-3">
            <button v-for="p in periods" :key="p.value" @click="changePeriod(p.value)"
              :class="['px-4 py-2 font-medium rounded-lg transition-colors', activePeriod === p.value ? 'bg-primary-600 text-white' : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50']">
              {{ p.label }}
            </button>
          </div>

          <!-- Stats cards -->
          <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <div class="bg-white rounded-xl shadow-sm p-4 text-center">
              <p class="text-2xl font-bold text-primary-600">{{ usageRecords.length }}</p>
              <p class="text-sm text-gray-500">使用次数</p>
            </div>
            <div class="bg-white rounded-xl shadow-sm p-4 text-center">
              <p class="text-2xl font-bold text-green-600">{{ totalUsage }}</p>
              <p class="text-sm text-gray-500">总使用时长</p>
            </div>
            <div class="bg-white rounded-xl shadow-sm p-4 text-center">
              <p class="text-2xl font-bold text-blue-600">{{ new Set(usageRecords.map(r => r.app)).size }}</p>
              <p class="text-sm text-gray-500">使用应用数</p>
            </div>
            <div class="bg-white rounded-xl shadow-sm p-4 text-center">
              <p class="text-2xl font-bold text-yellow-600">{{ new Set(usageRecords.map(r => r.device)).size }}</p>
              <p class="text-sm text-gray-500">活跃设备</p>
            </div>
          </div>

          <!-- Charts -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div class="bg-white rounded-xl shadow-sm p-4 md:p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">应用使用排行</h3>
              <VChart :option="appChartOption" class="h-72" autoresize />
            </div>
            <div class="bg-white rounded-xl shadow-sm p-4 md:p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">设备使用分布</h3>
              <VChart :option="deviceChartOption" class="h-72" autoresize />
            </div>
          </div>

          <!-- Detail table -->
          <div class="bg-white rounded-xl shadow-sm p-4 md:p-6 overflow-x-auto">
            <h3 class="text-lg font-semibold text-gray-800 mb-4">详细记录</h3>
            <table class="w-full">
              <thead>
                <tr class="border-b border-gray-200">
                  <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">日期</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">设备</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">应用</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">开始</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">时长</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="usageRecords.length === 0">
                  <td colspan="5" class="text-center py-8 text-gray-400">暂无使用记录</td>
                </tr>
                <tr v-for="r in usageRecords" :key="r.id" class="border-b border-gray-100 hover:bg-gray-50">
                  <td class="py-3 px-4 text-sm text-gray-600">{{ new Date(r.startTime).toLocaleDateString() }}</td>
                  <td class="py-3 px-4 text-sm text-gray-800">{{ r.device }}</td>
                  <td class="py-3 px-4 text-sm text-gray-800">{{ r.app }}</td>
                  <td class="py-3 px-4 text-sm text-gray-600">{{ new Date(r.startTime).toLocaleTimeString('zh-CN', { hour:'2-digit', minute:'2-digit' }) }}</td>
                  <td class="py-3 px-4 text-sm text-gray-600">{{ formatDuration(r.duration) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
