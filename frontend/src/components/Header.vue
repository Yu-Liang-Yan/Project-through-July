<script setup lang="ts">
import { ref } from 'vue'
import { Bell, Search } from '@lucide/vue'

defineProps<{
  title: string
  subtitle?: string
}>()

const searchQuery = ref('')
const showNotifications = ref(false)

const notifications = [
  { id: 1, message: '小明今天使用时间已达80%', time: '5分钟前', type: 'warning' },
  { id: 2, message: '新设备已加入监控', time: '1小时前', type: 'success' },
  { id: 3, message: '检测到违规访问', time: '2小时前', type: 'error' }
]

const hasNewNotif = ref(true)
</script>

<template>
  <header class="mb-6">
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">{{ title }}</h1>
        <p v-if="subtitle" class="text-gray-500">{{ subtitle }}</p>
      </div>

      <div class="flex items-center gap-4">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索设备、应用..."
            class="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none w-64"
          />
        </div>

        <div class="relative">
          <button
            @click="showNotifications = !showNotifications"
            class="relative w-10 h-10 bg-white border border-gray-300 rounded-lg flex items-center justify-center hover:bg-gray-50 transition-colors"
          >
            <Bell class="w-5 h-5 text-gray-600" />
            <span class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" v-if="hasNewNotif"></span>
          </button>

          <Transition name="dropdown">
            <div
              v-if="showNotifications"
              class="absolute right-0 top-full mt-2 w-80 bg-white rounded-lg shadow-xl border border-gray-100 overflow-hidden z-50"
            >
              <div class="p-3 border-b border-gray-100">
                <h3 class="font-medium text-gray-800">通知（演示）</h3>
              </div>
              <ul class="max-h-64 overflow-y-auto">
                <li
                  v-for="notification in notifications"
                  :key="notification.id"
                  class="p-3 hover:bg-gray-50 border-b border-gray-50 last:border-0"
                >
                  <div class="flex items-start gap-3">
                    <span
                      :class="[
                        'w-2 h-2 rounded-full mt-2 flex-shrink-0',
                        notification.type === 'error' ? 'bg-red-500' : notification.type === 'warning' ? 'bg-yellow-500' : 'bg-green-500'
                      ]"
                    ></span>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm text-gray-800">{{ notification.message }}</p>
                      <p class="text-xs text-gray-400 mt-1">{{ notification.time }}</p>
                    </div>
                  </div>
                </li>
              </ul>
              <div class="p-3 border-t border-gray-100">
                <button class="w-full text-sm text-primary-600 hover:text-primary-700">查看全部</button>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
