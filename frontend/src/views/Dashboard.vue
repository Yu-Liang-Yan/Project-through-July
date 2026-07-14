<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useDevicesStore } from '@/stores/devices'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import Card from '@/components/Card.vue'
import { Clock, Laptop, AlertTriangle, Calendar, ArrowRight } from '@lucide/vue'

const router = useRouter()
const userStore = useUserStore()
const devicesStore = useDevicesStore()

const stats = ref({
  todayUsage: '2h 30m',
  deviceCount: 3,
  alertCount: 0,
  weekUsage: '14h 20m'
})

const recentActivities = ref([
  { id: 1, time: '10:30', device: '小明的手机', app: '抖音', duration: '30分钟', action: '正常使用' },
  { id: 2, time: '09:15', device: '客厅电脑', app: '作业帮', duration: '45分钟', action: '正常使用' },
  { id: 3, time: '昨天', device: '平板设备', app: '王者荣耀', duration: '1小时', action: '正常使用' },
  { id: 4, time: '昨天', device: '小明的手机', app: '快手', duration: '20分钟', action: '正常使用' },
  { id: 5, time: '前天', device: '客厅电脑', app: '浏览器', duration: '15分钟', action: '正常使用' }
])

onMounted(() => {
  userStore.loadFromStorage()
  devicesStore.loadFromStorage()
  
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }
  
  stats.value.deviceCount = devicesStore.devices.length
})
</script>

<template>
  <div class="page-container">
    <Sidebar />
    <main class="content-area lg:ml-64">
      <Header title="仪表盘" subtitle="实时监控概览" />

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <Card title="今日使用时长" :value="stats.todayUsage" :icon="Clock" />
        <Card title="监控设备" :value="stats.deviceCount" :icon="Laptop" />
        <Card title="异常警告" :value="stats.alertCount" :icon="AlertTriangle" variant="danger" />
        <Card title="本周使用时长" :value="stats.weekUsage" :icon="Calendar" />
      </div>

      <div class="card mb-8">
        <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
          <Clock class="w-5 h-5 text-primary-500" />
          最近活动
        </h3>
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">时间</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">设备</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">应用/网站</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">时长</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">状态</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="activity in recentActivities"
                :key="activity.id"
                class="border-b border-gray-100 hover:bg-gray-50 transition-colors"
              >
                <td class="py-3 px-4 text-sm text-gray-600">{{ activity.time }}</td>
                <td class="py-3 px-4 text-sm text-gray-800">{{ activity.device }}</td>
                <td class="py-3 px-4 text-sm text-gray-800">{{ activity.app }}</td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ activity.duration }}</td>
                <td class="py-3 px-4">
                  <span class="inline-flex items-center px-2 py-1 text-xs font-medium bg-green-100 text-green-800 rounded">
                    {{ activity.action }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="mt-4 text-right">
          <button class="text-primary-600 hover:text-primary-700 text-sm font-medium flex items-center gap-1 ml-auto">
            查看全部记录
            <ArrowRight class="w-4 h-4" />
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="card">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">使用时长分布</h3>
          <div class="h-64 flex items-center justify-center bg-gray-50 rounded-lg">
            <div class="text-center text-gray-400">
              <Calendar class="w-12 h-12 mx-auto mb-2" />
              <p>图表加载中...</p>
            </div>
          </div>
        </div>

        <div class="card">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">应用使用排行</h3>
          <div class="space-y-4">
            <div class="flex items-center gap-4">
              <div class="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center text-primary-600 font-bold">1</div>
              <div class="flex-1">
                <div class="flex justify-between">
                  <span class="font-medium text-gray-800">抖音</span>
                  <span class="text-gray-500">2h 15m</span>
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
                  <span class="font-medium text-gray-800">王者荣耀</span>
                  <span class="text-gray-500">1h 45m</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                  <div class="bg-yellow-500 h-2 rounded-full" style="width: 65%"></div>
                </div>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <div class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-600 font-bold">3</div>
              <div class="flex-1">
                <div class="flex justify-between">
                  <span class="font-medium text-gray-800">作业帮</span>
                  <span class="text-gray-500">45m</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                  <div class="bg-green-500 h-2 rounded-full" style="width: 35%"></div>
                </div>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <div class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-600 font-bold">4</div>
              <div class="flex-1">
                <div class="flex justify-between">
                  <span class="font-medium text-gray-800">浏览器</span>
                  <span class="text-gray-500">30m</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                  <div class="bg-blue-500 h-2 rounded-full" style="width: 20%"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
