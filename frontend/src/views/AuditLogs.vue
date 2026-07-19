<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { AuditLogEntry } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { FileText, Clock } from '@lucide/vue'

const router = useRouter()
const userStore = useUserStore()
const logs = ref<AuditLogEntry[]>([])
const loading = ref(true)

const actionLabels: Record<string, string> = {
  LOGIN: '登录',
  LOGOUT: '退出',
  APPROVE: '审批通过',
  REJECT: '审批拒绝',
  SUBMIT_REQUEST: '提交请求',
  BIND_USER: '绑定用户',
  UNBIND_USER: '解绑用户',
  ADD_FILTER: '添加过滤规则',
  REMOVE_FILTER: '删除过滤规则',
  REGISTER_BIOMETRIC: '注册生物特征',
  UPDATE_SETTINGS: '更新设置',
  VIEW_DASHBOARD: '查看仪表盘'
}

const loadLogs = async () => {
  loading.value = true
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.auditLogs.listByUser(uid)
    if (res.success && res.data) logs.value = res.data
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
  await loadLogs()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Sidebar />
    <div class="lg:ml-64">
      <Header title="操作日志" subtitle="审计记录与操作追踪" />

      <div class="p-6">
        <div v-if="loading" class="text-center py-16 text-gray-500">
          <Clock class="w-12 h-12 mx-auto mb-4 animate-spin text-primary-500" />
          <p>加载日志记录...</p>
        </div>

        <div v-else-if="logs.length === 0" class="bg-white rounded-xl shadow-sm p-12 text-center text-gray-500">
          <FileText class="w-12 h-12 mx-auto mb-3 text-gray-300" />
          <p>暂无操作日志</p>
        </div>

        <div v-else class="bg-white rounded-xl shadow-sm overflow-hidden">
          <table class="w-full">
            <thead class="bg-gray-50 border-b">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">时间</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">详情</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200">
              <tr v-for="log in logs" :key="log.id" class="hover:bg-gray-50">
                <td class="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">
                  {{ new Date(log.createdAt).toLocaleString('zh-CN') }}
                </td>
                <td class="px-6 py-4">
                  <span class="px-2 py-1 rounded text-xs font-medium bg-blue-100 text-blue-700">
                    {{ actionLabels[log.action] || log.action }}
                  </span>
                </td>
                <td class="px-6 py-4 text-sm text-gray-600 max-w-xs">{{ log.detail }}</td>
                <td class="px-6 py-4 text-sm text-gray-400 font-mono">{{ log.ip }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
