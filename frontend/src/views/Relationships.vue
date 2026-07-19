<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { GuardianBinding } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { Users, Plus, Trash2, UserCircle, Shield } from '@lucide/vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const protectedUsers = ref<GuardianBinding[]>([])
const guardians = ref<GuardianBinding[]>([])
const showBindModal = ref(false)
const bindUserId = ref('')
const loading = ref(false)
const dataLoading = ref(true)

const isGuardian = computed(() => userStore.currentUser?.userType === 'guardian')

const { toast } = useToast()

const loadData = async () => {
  const uid = userStore.currentUser?.id ?? 1
  try {
    const [pu, gu] = await Promise.all([
      api.bindings.listProtectedUsers(uid),
      api.bindings.listGuardians(uid)
    ])
    if (pu.success && pu.data) protectedUsers.value = pu.data
    if (gu.success && gu.data) guardians.value = gu.data
  } catch (e) {
    console.error(e)
  } finally {
    dataLoading.value = false
  }
}

const handleBind = async () => {
  if (!bindUserId.value) {
    toast('请输入被保护用户ID', 'warning')
    return
  }
  loading.value = true
  try {
    const uid = userStore.currentUser?.id ?? 1
    const res = await api.bindings.bind(uid, Number(bindUserId.value))
    if (res.success) {
      toast('绑定成功', 'success')
      showBindModal.value = false
      bindUserId.value = ''
      await loadData()
    } else {
      toast(res.message || '绑定失败', 'error')
    }
  } catch (e) {
    toast('绑定失败', 'error')
  } finally {
    loading.value = false
  }
}

const handleUnbind = async (id: number) => {
  if (!confirm('确定要解除该绑定关系吗？')) return
  try {
    await api.bindings.unbind(id)
    toast('已解绑', 'info')
    await loadData()
  } catch (e) {
    toast('解绑失败', 'error')
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
      <Header title="关系管理" subtitle="管理监护人与被保护对象之间的绑定关系" />

      <div class="p-6 space-y-6">
        <div v-if="dataLoading" class="text-center py-16 text-gray-500">
          <Activity class="w-12 h-12 mx-auto mb-4 animate-spin text-primary-500" />
          <p>加载关系数据...</p>
        </div>

        <template v-else>
        <!-- 我被保护的用户（监护人视角） -->
        <div v-if="isGuardian" class="bg-white rounded-xl shadow-sm p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
              <Users class="w-5 h-5 text-primary-600" />
              我监护的用户
            </h3>
            <button @click="showBindModal = true" class="inline-flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors">
              <Plus class="w-4 h-4" /> 绑定新用户
            </button>
          </div>

          <div v-if="protectedUsers.length === 0" class="text-center py-8 text-gray-500">
            暂无绑定关系，点击"绑定新用户"添加
          </div>

          <div v-else class="grid gap-4 md:grid-cols-2">
            <div
              v-for="b in protectedUsers"
              :key="b.id"
              class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                  <UserCircle class="w-6 h-6 text-blue-600" />
                </div>
                <div>
                  <p class="font-medium text-gray-800">{{ b.protectedUserName }}</p>
                  <p class="text-sm text-gray-500">年龄段：{{ b.protectedAgeGroup || '未设置' }} | {{ b.createdAt?.substring(0, 10) }}</p>
                </div>
              </div>
              <button @click="handleUnbind(b.id)" class="p-2 text-gray-400 hover:text-red-500 transition-colors">
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        <!-- 我的监护人（被保护用户视角） -->
        <div v-else class="bg-white rounded-xl shadow-sm p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
              <Shield class="w-5 h-5 text-primary-600" />
              我的监护人
            </h3>
          </div>

          <div v-if="guardians.length === 0" class="text-center py-8 text-gray-500">
            暂无监护关系
          </div>

          <div v-else class="grid gap-4 md:grid-cols-2">
            <div
              v-for="b in guardians"
              :key="b.id"
              class="flex items-center gap-3 p-4 border border-gray-200 rounded-lg"
            >
              <div class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                <UserCircle class="w-6 h-6 text-green-600" />
              </div>
              <div>
                <p class="font-medium text-gray-800">{{ b.guardianName }}</p>
                <p class="text-sm text-gray-500">{{ b.createdAt?.substring(0, 10) }}</p>
              </div>
            </div>
          </div>
        </div>
        </template>
      </div>

      <!-- 绑定模态框 -->
      <div v-if="showBindModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
        <div class="bg-white rounded-xl p-6 w-full max-w-sm">
          <h3 class="text-lg font-semibold mb-4">绑定被保护用户</h3>
          <input v-model="bindUserId" type="number" placeholder="输入被保护用户ID" class="w-full px-4 py-3 border border-gray-300 rounded-lg mb-4 outline-none focus:ring-2 focus:ring-primary-500" />
          <p class="text-xs text-gray-500 mb-4">提示：现将admin(监护人)与 child612(6-12岁, ID=3)、child(ID=5)、child1518(ID=6) 绑定</p>
          <div class="flex gap-3">
            <button @click="showBindModal = false" class="flex-1 py-3 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50">取消</button>
            <button @click="handleBind" :disabled="loading" class="flex-1 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50">{{ loading ? '绑定中...' : '确认绑定' }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
