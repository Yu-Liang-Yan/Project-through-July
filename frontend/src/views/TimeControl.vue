<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useSettingsStore } from '@/stores/settings'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { Clock, Save, ChevronDown, ChevronUp } from '@lucide/vue'

const router = useRouter()
const userStore = useUserStore()
const settingsStore = useSettingsStore()

const expandedSections = ref({
  dailyLimit: true,
  timeRange: true,
  periodLimit: true
})

const toggleSection = (section: string) => {
  expandedSections.value[section as keyof typeof expandedSections.value] = !expandedSections.value[section as keyof typeof expandedSections.value]
}

const timeSettings = ref({
  dailyHours: 2,
  dailyMinutes: 0,
  startTime: '09:00',
  endTime: '21:00',
  weeklyLimit: 14,
  monthlyLimit: 60
})

const saveSettings = () => {
  settingsStore.setTimeSettings({
    id: 0,
    ...timeSettings.value
  })
  ;(window as any).showToast('时间设置已保存', 'success')
}

onMounted(() => {
  userStore.loadFromStorage()
  settingsStore.loadFromStorage()
  
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }
  
  const saved = settingsStore.timeSettings
  timeSettings.value = {
    dailyHours: saved.dailyHours,
    dailyMinutes: saved.dailyMinutes,
    startTime: saved.startTime,
    endTime: saved.endTime,
    weeklyLimit: saved.weeklyLimit,
    monthlyLimit: saved.monthlyLimit
  }
})
</script>

<template>
  <div class="page-container">
    <Sidebar />
    <main class="content-area lg:ml-64">
      <Header title="时间控制" subtitle="设置设备使用时间限制" />

      <div class="max-w-3xl mx-auto">
        <div class="card mb-4">
          <button
            @click="toggleSection('dailyLimit')"
            class="w-full flex items-center justify-between p-4 hover:bg-gray-50 rounded-xl transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                <Clock class="w-5 h-5 text-primary-600" />
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-gray-800">每日使用限制</h3>
                <p class="text-sm text-gray-500">设置每日最大使用时长</p>
              </div>
            </div>
            <component :is="expandedSections.dailyLimit ? ChevronUp : ChevronDown" class="w-5 h-5 text-gray-400" />
          </button>
          
          <Transition name="collapse">
            <div v-if="expandedSections.dailyLimit" class="px-4 pb-4">
              <div class="flex items-center gap-4">
                <div class="flex-1">
                  <label class="block text-sm text-gray-500 mb-1">小时</label>
                  <input
                    v-model.number="timeSettings.dailyHours"
                    type="number"
                    min="0"
                    max="24"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                  />
                </div>
                <div class="flex-1">
                  <label class="block text-sm text-gray-500 mb-1">分钟</label>
                  <input
                    v-model.number="timeSettings.dailyMinutes"
                    type="number"
                    min="0"
                    max="59"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                  />
                </div>
              </div>
              <p class="mt-3 text-sm text-gray-500">
                当前设置：{{ timeSettings.dailyHours }}小时{{ timeSettings.dailyMinutes }}分钟
              </p>
            </div>
          </Transition>
        </div>

        <div class="card mb-4">
          <button
            @click="toggleSection('timeRange')"
            class="w-full flex items-center justify-between p-4 hover:bg-gray-50 rounded-xl transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                <Clock class="w-5 h-5 text-blue-600" />
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-gray-800">允许使用时间段</h3>
                <p class="text-sm text-gray-500">设置允许使用设备的时间段</p>
              </div>
            </div>
            <component :is="expandedSections.timeRange ? ChevronUp : ChevronDown" class="w-5 h-5 text-gray-400" />
          </button>
          
          <Transition name="collapse">
            <div v-if="expandedSections.timeRange" class="px-4 pb-4">
              <div class="flex items-center gap-4">
                <div class="flex-1">
                  <label class="block text-sm text-gray-500 mb-1">开始时间</label>
                  <input
                    v-model="timeSettings.startTime"
                    type="time"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                  />
                </div>
                <div class="flex items-center text-gray-400 text-xl">-</div>
                <div class="flex-1">
                  <label class="block text-sm text-gray-500 mb-1">结束时间</label>
                  <input
                    v-model="timeSettings.endTime"
                    type="time"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                  />
                </div>
              </div>
              <p class="mt-3 text-sm text-gray-500">
                当前设置：{{ timeSettings.startTime }} - {{ timeSettings.endTime }}
              </p>
            </div>
          </Transition>
        </div>

        <div class="card mb-4">
          <button
            @click="toggleSection('periodLimit')"
            class="w-full flex items-center justify-between p-4 hover:bg-gray-50 rounded-xl transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                <Clock class="w-5 h-5 text-green-600" />
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-gray-800">周期限制</h3>
                <p class="text-sm text-gray-500">设置每周和每月使用时长上限</p>
              </div>
            </div>
            <component :is="expandedSections.periodLimit ? ChevronUp : ChevronDown" class="w-5 h-5 text-gray-400" />
          </button>
          
          <Transition name="collapse">
            <div v-if="expandedSections.periodLimit" class="px-4 pb-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm text-gray-500 mb-1">每周限制（小时）</label>
                  <input
                    v-model.number="timeSettings.weeklyLimit"
                    type="number"
                    min="0"
                    max="168"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                  />
                </div>
                <div>
                  <label class="block text-sm text-gray-500 mb-1">每月限制（小时）</label>
                  <input
                    v-model.number="timeSettings.monthlyLimit"
                    type="number"
                    min="0"
                    max="720"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                  />
                </div>
              </div>
              <p class="mt-3 text-sm text-gray-500">
                当前设置：每周 {{ timeSettings.weeklyLimit }} 小时，每月 {{ timeSettings.monthlyLimit }} 小时
              </p>
            </div>
          </Transition>
        </div>

        <div class="flex justify-center">
          <button
            @click="saveSettings"
            class="flex items-center gap-2 px-8 py-3 bg-primary-500 text-white font-medium rounded-lg hover:bg-primary-600 transition-colors"
          >
            <Save class="w-5 h-5" />
            保存设置
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  opacity: 0;
  max-height: 0;
}

.collapse-enter-to,
.collapse-leave-from {
  opacity: 1;
  max-height: 200px;
}
</style>
