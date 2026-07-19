<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { GuardianBinding } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { Users, UserCircle, Shield, Clock, Phone, Activity } from '@lucide/vue'

const router = useRouter()
const userStore = useUserStore()
const protectedUsers = ref<GuardianBinding[]>([])
const loading = ref(true)

const isGuardian = computed(() => userStore.currentUser?.userType === 'guardian')

const ageGroupLabel = (group?: string) => {
  const map: Record<string, string> = { '0-6': '0-6岁', '6-12': '6-12岁', '12-15': '12-15岁', '15-18': '15-18岁' }
  return map[group || ''] || group || '未设置'
}

const loadData = async () => {
  loading.value = true
  const uid = userStore.currentUser?.id ?? 1
  try {
    if (isGuardian.value) {
      const res = await api.bindings.listProtectedUsers(uid)
      if (res.success && res.data) protectedUsers.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
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
      <Header title="用户管理" :subtitle="isGuardian ? '查看被保护用户的详细信息' : '个人信息'" />

      <div class="p-6">
        <div v-if="loading" class="text-center py-16 text-gray-500">
          <Activity class="w-12 h-12 mx-auto mb-4 animate-spin text-primary-500" />
          <p>加载用户数据...</p>
        </div>

        <!-- 监护人视角 -->
        <template v-else-if="isGuardian">
          <div v-if="protectedUsers.length === 0" class="bg-white rounded-xl shadow-sm p-12 text-center text-gray-500">
            <Users class="w-12 h-12 mx-auto mb-3 text-gray-300" />
            <p>暂无绑定的被保护用户</p>
          </div>

          <div v-else class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            <div
              v-for="u in protectedUsers"
              :key="u.id"
              class="bg-white rounded-xl shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div class="flex items-center gap-4 mb-4">
                <div class="w-14 h-14 bg-primary-100 rounded-full flex items-center justify-center">
                  <UserCircle class="w-8 h-8 text-primary-600" />
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-gray-800">{{ u.protectedUserName }}</h3>
                  <span class="px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-700 rounded">
                    {{ ageGroupLabel(u.protectedAgeGroup) }}
                  </span>
                </div>
              </div>

              <div class="space-y-3 text-sm">
                <div class="flex items-center gap-2 text-gray-600">
                  <Shield class="w-4 h-4 text-gray-400" />
                  <span>状态：<span class="font-medium text-green-600">活跃</span></span>
                </div>
                <div class="flex items-center gap-2 text-gray-600">
                  <Clock class="w-4 h-4 text-gray-400" />
                  <span>绑定日期：{{ u.createdAt?.substring(0, 10) }}</span>
                </div>
                <div class="flex items-center gap-2 text-gray-600">
                  <Phone class="w-4 h-4 text-gray-400" />
                  <span>用户ID：{{ u.protectedUserId }}</span>
                </div>
              </div>

              <!-- 年龄分层默认管控展示 -->
              <div class="mt-4 pt-4 border-t border-gray-100">
                <p class="text-xs text-gray-500 mb-2">默认管控策略</p>
                <div class="grid grid-cols-2 gap-2 text-xs">
                  <div class="bg-gray-50 rounded p-2">
                    <span class="text-gray-500">每日时长</span>
                    <p class="font-medium text-gray-800">
                      {{ u.protectedAgeGroup === '0-6' ? '0.5h' : u.protectedAgeGroup === '6-12' ? '1h' : u.protectedAgeGroup === '12-15' ? '1.5h' : '2h' }}
                    </p>
                  </div>
                  <div class="bg-gray-50 rounded p-2">
                    <span class="text-gray-500">可用时段</span>
                    <p class="font-medium text-gray-800">
                      {{ u.protectedAgeGroup === '0-6' ? '9:00-18:00' : u.protectedAgeGroup === '6-12' ? '8:00-20:00' : u.protectedAgeGroup === '12-15' ? '7:00-21:00' : '6:00-22:00' }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 被保护用户视角 -->
        <template v-else>
          <div class="bg-white rounded-xl shadow-sm p-6 max-w-md mx-auto">
            <div class="text-center mb-4">
              <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
                <UserCircle class="w-8 h-8 text-green-600" />
              </div>
              <h3 class="text-lg font-semibold text-gray-800">{{ userStore.currentUser?.username }}</h3>
              <span class="px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-700 rounded">
                {{ ageGroupLabel(userStore.currentUser?.ageGroup) }}
              </span>
            </div>
            <div class="space-y-3 text-sm text-center text-gray-600">
              <p>手机号：{{ userStore.currentUser?.phone }}</p>
              <p>注册时间：{{ userStore.currentUser?.createdAt?.substring(0, 10) }}</p>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
