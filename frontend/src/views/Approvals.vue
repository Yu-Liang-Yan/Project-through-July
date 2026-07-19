<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { ApprovalRequest } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { CheckCircle, XCircle, Plus, Clock, Ban, FileText } from '@lucide/vue'

const router = useRouter()
const userStore = useUserStore()
const isGuardian = computed(() => userStore.currentUser?.userType === 'guardian')

const pendingRequests = ref<ApprovalRequest[]>([])
const myRequests = ref<ApprovalRequest[]>([])
const loading = ref(false)

const showForm = ref(false)
const requestType = ref<'TIME_EXTENSION' | 'UNBLOCK'>('TIME_EXTENSION')
const description = ref('')
const extraMinutes = ref(30)
const targetName = ref('')

const typeLabels: Record<string, string> = {
  TIME_EXTENSION: '延长使用时间',
  UNBLOCK: '解除禁止访问',
  ACCESS: '访问特定内容'
}

const statusLabels: Record<string, string> = {
  PENDING: '待审批',
  APPROVED: '已同意',
  REJECTED: '已拒绝'
}

const statusClasses: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  APPROVED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800'
}

const toast = (msg: string, type: string) => {
  ;(window as any).showToast?.(msg, type)
}

const loadData = async () => {
  loading.value = true
  try {
    if (isGuardian.value) {
      const uid = userStore.currentUser?.id ?? 1
      const res = await api.approvals.pending(uid)
      pendingRequests.value = res.data || []
    } else {
      const uid = userStore.currentUser?.id ?? 2
      const res = await api.approvals.myRequests(uid)
      myRequests.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const submitRequest = async () => {
  if (!description.value) return
  try {
    await api.approvals.submit({
      requesterId: userStore.currentUser?.id ?? 2,
      type: requestType.value,
      description: description.value,
      extraMinutes: requestType.value === 'TIME_EXTENSION' ? extraMinutes.value : undefined,
      targetName: requestType.value === 'UNBLOCK' ? targetName.value : undefined
    })
    description.value = ''
    targetName.value = ''
    showForm.value = false
    toast('请求已提交', 'success')
    await loadData()
  } catch (e) {
    toast('提交失败', 'error')
  }
}

const handleApprove = async (req: ApprovalRequest) => {
  try {
    await api.approvals.approve(req.id, userStore.currentUser?.id ?? 1)
    toast('已同意该请求', 'success')
    await loadData()
  } catch (e) {
    toast('操作失败', 'error')
  }
}

const handleReject = async (req: ApprovalRequest) => {
  const reason = prompt('拒绝理由（可选）：') || '已拒绝'
  try {
    await api.approvals.reject(req.id, userStore.currentUser?.id ?? 1, reason)
    toast('已拒绝该请求', 'info')
    await loadData()
  } catch (e) {
    toast('操作失败', 'error')
  }
}

const formatDate = (s: string) => new Date(s).toLocaleString('zh-CN')

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
      <Header title="请求审批" :subtitle="isGuardian ? '审批被保护用户的请求' : '查看我的请求状态'" />

      <div class="p-6 space-y-6">
        <!-- 头部操作栏 -->
        <div class="flex items-center justify-between">
          <div></div>
          <button
            v-if="!isGuardian && !showForm"
            @click="showForm = true"
            class="inline-flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
          >
            <Plus class="w-4 h-4" /> 发起新请求
          </button>
        </div>

        <!-- 发起请求表单（被保护用户） -->
        <div v-if="!isGuardian && showForm" class="bg-white rounded-xl shadow-sm p-6 space-y-4">
          <h3 class="font-semibold text-gray-700">发起请求</h3>

          <div class="flex gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="radio" v-model="requestType" value="TIME_EXTENSION" class="text-primary-600" />
              <Clock class="w-4 h-4" /> 延长使用时间
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="radio" v-model="requestType" value="UNBLOCK" class="text-primary-600" />
              <Ban class="w-4 h-4" /> 解除禁止访问
            </label>
          </div>

          <div v-if="requestType === 'TIME_EXTENSION'">
            <label class="block text-sm text-gray-600 mb-1">额外时长（分钟）</label>
            <input type="number" v-model="extraMinutes" min="10" max="180" step="10"
              class="w-32 px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent" />
          </div>

          <div v-if="requestType === 'UNBLOCK'">
            <label class="block text-sm text-gray-600 mb-1">要解除的项目名称</label>
            <input type="text" v-model="targetName" placeholder="如：百度文库"
              class="w-64 px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent" />
          </div>

          <div>
            <label class="block text-sm text-gray-600 mb-1">请求理由</label>
            <textarea v-model="description" rows="3" placeholder="请说明需要的原因..."
              class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none" />
          </div>

          <div class="flex gap-3">
            <button @click="submitRequest"
              class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors">
              提交请求
            </button>
            <button @click="showForm = false"
              class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors">
              取消
            </button>
          </div>
        </div>

        <!-- 监护人：待审批列表 -->
        <template v-if="isGuardian">
          <div v-if="loading" class="text-center py-8 text-gray-500">加载中...</div>

          <div v-else-if="pendingRequests.length === 0" class="bg-white rounded-xl shadow-sm p-8 text-center text-gray-500">
            <CheckCircle class="w-12 h-12 mx-auto mb-3 text-gray-300" />
            <p>暂无待审批的请求</p>
          </div>

          <div v-else class="bg-white rounded-xl shadow-sm overflow-hidden">
            <table class="w-full">
              <thead class="bg-gray-50 border-b">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">发起人</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">详情</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">时间</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200">
                <tr v-for="req in pendingRequests" :key="req.id" class="hover:bg-gray-50">
                  <td class="px-6 py-4 text-sm font-medium text-gray-900">{{ req.requesterName }}</td>
                  <td class="px-6 py-4">
                    <span class="inline-flex items-center gap-1 text-sm text-blue-700">
                      <Clock v-if="req.type === 'TIME_EXTENSION'" class="w-4 h-4" />
                      <Ban v-else-if="req.type === 'UNBLOCK'" class="w-4 h-4" />
                      <FileText v-else class="w-4 h-4" />
                      {{ typeLabels[req.type] || req.type }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-600 max-w-xs">
                    {{ req.description }}
                    <span v-if="req.extraMinutes" class="text-primary-600 font-medium ml-1">(+{{ req.extraMinutes }}分钟)</span>
                    <span v-if="req.targetName" class="text-orange-600 font-medium ml-1">{{ req.targetName }}</span>
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-500">{{ formatDate(req.createdAt) }}</td>
                  <td class="px-6 py-4 text-sm space-x-2 whitespace-nowrap">
                    <button @click="handleApprove(req)"
                      class="inline-flex items-center gap-1 px-3 py-1.5 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors text-xs">
                      <CheckCircle class="w-3.5 h-3.5" /> 同意
                    </button>
                    <button @click="handleReject(req)"
                      class="inline-flex items-center gap-1 px-3 py-1.5 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors text-xs">
                      <XCircle class="w-3.5 h-3.5" /> 拒绝
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- 被保护用户：我的请求历史 -->
        <template v-if="!isGuardian">
          <div v-if="loading" class="text-center py-8 text-gray-500">加载中...</div>

          <div v-else-if="myRequests.length === 0" class="bg-white rounded-xl shadow-sm p-8 text-center text-gray-500">
            <FileText class="w-12 h-12 mx-auto mb-3 text-gray-300" />
            <p>暂无请求记录</p>
          </div>

          <div v-else class="bg-white rounded-xl shadow-sm overflow-hidden">
            <table class="w-full">
              <thead class="bg-gray-50 border-b">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">详情</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">回复</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">提交时间</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200">
                <tr v-for="req in myRequests" :key="req.id" class="hover:bg-gray-50">
                  <td class="px-6 py-4">
                    <span class="inline-flex items-center gap-1 text-sm text-blue-700">
                      <Clock v-if="req.type === 'TIME_EXTENSION'" class="w-4 h-4" />
                      <Ban v-else-if="req.type === 'UNBLOCK'" class="w-4 h-4" />
                      <FileText v-else class="w-4 h-4" />
                      {{ typeLabels[req.type] || req.type }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-600 max-w-xs">
                    {{ req.description }}
                    <span v-if="req.extraMinutes" class="text-primary-600 font-medium ml-1">(+{{ req.extraMinutes }}分钟)</span>
                    <span v-if="req.targetName" class="text-orange-600 font-medium ml-1">{{ req.targetName }}</span>
                  </td>
                  <td class="px-6 py-4">
                    <span :class="['inline-block px-2 py-1 rounded-full text-xs font-medium', statusClasses[req.status] || '']">
                      {{ statusLabels[req.status] || req.status }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-500 max-w-xs">{{ req.responseMessage || '-' }}</td>
                  <td class="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">{{ formatDate(req.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
