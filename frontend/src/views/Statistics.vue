<script setup lang="ts">import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { api } from '@/api';
import type { UsageRecord } from '@/types';
import Sidebar from '@/components/Sidebar.vue';
import Header from '@/components/Header.vue';
import { Bar } from 'vue-chartjs';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const router = useRouter();
const userStore = useUserStore();
const activePeriod = ref<'day' | 'week' | 'month'>('day');
const periods = [
 { value: 'day', label: '今日' },
 { value: 'week', label: '本周' },
 { value: 'month', label: '本月' }
];
const usageRecords = ref<UsageRecord[]>([]);

const chartData = computed(() => {
 const labels = activePeriod.value === 'day'
 ? ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
 : activePeriod.value === 'week'
 ? ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
 : ['1日', '5日', '10日', '15日', '20日', '25日', '30日'];
 const data = [30, 60, 90, 120, 80, 100, 70].slice(0, labels.length);
 return {
 labels,
 datasets: [
 {
 label: '使用时长（分钟）',
 data,
 backgroundColor: 'rgba(59, 130, 246, 0.6)',
 borderColor: 'rgba(59, 130, 246, 1)',
 borderWidth: 1,
 borderRadius: 8
 }
 ]
 };
});

const chartOptions = {
 responsive: true,
 maintainAspectRatio: false,
 plugins: {
 legend: { position: 'top' as const }
 },
 scales: {
 y: {
 beginAtZero: true,
 ticks: { callback: (value: number | string) => `${value}分钟` }
 }
 }
};

const totalUsage = computed(() => {
 let totalMin = 0;
 usageRecords.value.forEach(r => { totalMin += Math.round((r.duration || 0) / 60); });
 const hours = Math.floor(totalMin / 60);
 const mins = totalMin % 60;
 return `${hours}小时${mins}分钟`;
});

const loadRecords = async () => {
  const uid = userStore.currentUser?.id ?? 1;
  try {
    const res = await api.statistics.usage(uid, activePeriod.value);
    if (res.success && res.data) {
      usageRecords.value = res.data;
    }
  } catch (e) {
    console.error(e);
  }
};

onMounted(async () => {
 userStore.loadFromStorage();
 if (!userStore.isLoggedIn) {
 router.push('/');
 return;
 }
 await loadRecords();
});
</script>

<template>
  <div class="page-container">
    <Sidebar />
    <main class="content-area lg:ml-64">
      <Header title="使用统计" subtitle="查看设备使用情况" />

      <div class="flex gap-3 mb-6">
        <button
          v-for="period in periods"
          :key="period.value"
          @click="activePeriod = period.value as any"
          :class="[
            'px-4 py-2 font-medium rounded-lg transition-colors',
            activePeriod === period.value ? 'bg-primary-500 text-white' : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
          ]"
        >
          {{ period.label }}
        </button>
      </div>

      <div class="card mb-6">
        <div class="h-80">
          <Bar :data="chartData" :options="chartOptions" />
        </div>
      </div>

      <div class="card mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-800">使用统计摘要</h3>
          <div class="text-right">
            <p class="text-sm text-gray-500">总使用时长</p>
            <p class="text-2xl font-bold text-primary-600">{{ totalUsage }}</p>
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div class="bg-blue-50 rounded-lg p-4">
            <p class="text-sm text-gray-500">最多使用应用</p>
            <p class="text-lg font-semibold text-gray-800">抖音</p>
            <p class="text-sm text-blue-600">2h 30m</p>
          </div>
          <div class="bg-green-50 rounded-lg p-4">
            <p class="text-sm text-gray-500">最常用设备</p>
            <p class="text-lg font-semibold text-gray-800">小明的手机</p>
            <p class="text-sm text-green-600">3台设备</p>
          </div>
          <div class="bg-yellow-50 rounded-lg p-4">
            <p class="text-sm text-gray-500">平均每日时长</p>
            <p class="text-lg font-semibold text-gray-800">2h 15m</p>
            <p class="text-sm text-yellow-600">较上周 +10%</p>
          </div>
        </div>
      </div>

      <div class="card">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">详细记录</h3>
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">日期</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">设备</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">应用/网站</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">开始时间</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">结束时间</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">时长</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="record in usageRecords"
                :key="record.id"
                class="border-b border-gray-100 hover:bg-gray-50 transition-colors"
              >
                <td class="py-3 px-4 text-sm text-gray-600">{{ new Date(record.startTime).toLocaleDateString() }}</td>
                <td class="py-3 px-4 text-sm text-gray-800">{{ record.device }}</td>
                <td class="py-3 px-4 text-sm text-gray-800">{{ record.app }}</td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ new Date(record.startTime).toLocaleTimeString('zh-CN', { hour:'2-digit', minute:'2-digit' }) }}</td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ new Date(record.endTime).toLocaleTimeString('zh-CN', { hour:'2-digit', minute:'2-digit' }) }}</td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ Math.round(record.duration / 60) }}分钟</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  </div>
</template>
