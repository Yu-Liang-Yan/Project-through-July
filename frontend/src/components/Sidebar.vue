<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Shield, Home, Laptop, Clock, Ban, BarChart3, Settings, LogOut, UserCircle, CheckCircle, Bell, Users, Filter, FileText } from '@lucide/vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isMobileMenuOpen = ref(false)

const navItems = [
  { path: '/dashboard', label: '仪表盘', icon: Home },
  { path: '/devices', label: '设备管理', icon: Laptop },
  { path: '/time-control', label: '时间控制', icon: Clock },
  { path: '/block-list', label: '禁止列表', icon: Ban },
  { path: '/statistics', label: '使用统计', icon: BarChart3 },
  { path: '/settings', label: '设置', icon: Settings },
  { path: '/approvals', label: '请求审批', icon: CheckCircle },
  { path: '/alerts', label: '告警通知', icon: Bell },
  { path: '/relationships', label: '关系管理', icon: Users },
  { path: '/content-filters', label: '内容过滤', icon: Filter },
  { path: '/audit-logs', label: '操作日志', icon: FileText },
  { path: '/user-management', label: '用户管理', icon: UserCircle }
]

const currentPath = computed(() => route.path)

const isActive = (path: string) => currentPath.value === path

const navigate = (path: string) => {
  router.push(path)
  isMobileMenuOpen.value = false
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
    router.push('/')
  }
}

const userTypeLabel = computed(() => {
  return userStore.currentUser?.userType === 'guardian' ? '监护人' : '被保护对象'
})
</script>

<template>
  <aside
    :class="[
      'fixed top-0 left-0 h-screen w-64 bg-gray-800 text-white flex flex-col transition-all duration-300 z-40',
      isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
    ]"
  >
    <div class="p-6 border-b border-gray-700">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 bg-primary-500 rounded-lg flex items-center justify-center">
          <Shield class="w-6 h-6" />
        </div>
        <div>
          <h1 class="text-lg font-bold">守护宝贝</h1>
          <p class="text-xs text-gray-400">智能监护系统</p>
        </div>
      </div>
    </div>

    <div class="p-4 border-b border-gray-700">
      <div class="flex items-center gap-3">
        <div class="w-12 h-12 bg-gray-700 rounded-full flex items-center justify-center">
          <UserCircle class="w-8 h-8 text-gray-300" />
        </div>
        <div>
          <h3 class="font-medium">{{ userStore.currentUser?.username || '用户名' }}</h3>
          <p class="text-xs text-gray-400">{{ userTypeLabel }}</p>
        </div>
      </div>
    </div>

    <nav class="flex-1 p-4">
      <ul class="space-y-2">
        <li v-for="item in navItems" :key="item.path">
          <button
            @click="navigate(item.path)"
            :class="[
              'w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors duration-200',
              isActive(item.path) ? 'bg-primary-600 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'
            ]"
          >
            <component :is="item.icon" class="w-5 h-5" />
            <span class="font-medium">{{ item.label }}</span>
          </button>
        </li>
      </ul>
    </nav>

    <div class="p-4 border-t border-gray-700">
      <button
        @click="handleLogout"
        class="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-gray-300 hover:bg-red-600 hover:text-white transition-colors duration-200"
      >
        <LogOut class="w-5 h-5" />
        <span class="font-medium">退出登录</span>
      </button>
    </div>
  </aside>

  <button
    v-if="isMobileMenuOpen"
    @click="isMobileMenuOpen = false"
    class="fixed inset-0 bg-black/50 z-30 lg:hidden"
  ></button>

  <button
    @click="isMobileMenuOpen = !isMobileMenuOpen"
    class="fixed top-4 left-4 z-50 lg:hidden w-10 h-10 bg-gray-800 text-white rounded-lg flex items-center justify-center"
  >
    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path v-if="!isMobileMenuOpen" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
      <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
    </svg>
  </button>
</template>
