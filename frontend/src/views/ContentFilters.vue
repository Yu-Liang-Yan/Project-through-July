<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/api'
import type { ContentFilterRule } from '@/types'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import { Filter, Plus, Trash2, AlertTriangle, Activity } from '@lucide/vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const rules = ref<ContentFilterRule[]>([])
const loading = ref(true)
const activeCategory = ref('ALL')
const newPattern = ref('')
const newAction = ref('BLOCK')
const newCategory = ref('KEYWORD')

const categories = [
  { value: 'ALL', label: '全部' },
  { value: 'KEYWORD', label: '关键词' },
  { value: 'WEBSITE', label: '网站' },
  { value: 'DOMAIN', label: '域名' },
  { value: 'APP', label: '应用' }
]

const actions = [
  { value: 'BLOCK', label: '拦截' },
  { value: 'WARN', label: '警告' },
  { value: 'LOG', label: '记录' }
]

const { toast } = useToast()

const loadRules = async () => {
  const uid = userStore.currentUser?.id ?? 1
  try {
    const cat = activeCategory.value === 'ALL' ? undefined : activeCategory.value
    const res = await api.filters.list(uid, cat)
    if (res.success && res.data) rules.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const addRule = async () => {
  if (!newPattern.value.trim()) {
    toast('请输入过滤内容', 'warning')
    return
  }
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.filters.add(uid, newCategory.value, newPattern.value.trim(), newAction.value, 1)
    if (res.success) {
      toast('过滤规则已添加', 'success')
      newPattern.value = ''
      await loadRules()
    } else {
      toast(res.message || '添加失败', 'error')
    }
  } catch (e) {
    toast('添加失败', 'error')
  }
}

const removeRule = async (id: number) => {
  if (!confirm('确定要删除该过滤规则吗？')) return
  try {
    await api.filters.remove(id)
    toast('已删除', 'info')
    await loadRules()
  } catch (e) {
    toast('删除失败', 'error')
  }
}

onMounted(async () => {
  userStore.loadFromStorage()
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }
  await loadRules()
  loading.value = false
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Sidebar />
    <div class="lg:ml-64">
      <Header title="内容过滤" subtitle="配置网站/关键词/应用过滤规则" />

      <div v-if="loading" class="text-center py-16 text-gray-500">
        <Activity class="w-12 h-12 mx-auto mb-4 animate-spin text-primary-500" />
        <p>加载过滤规则...</p>
      </div>

      <template v-else>
      <div class="p-6 space-y-6">
        <!-- 添加规则 -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <Plus class="w-5 h-5 text-primary-600" />
            添加过滤规则
          </h3>
          <div class="grid gap-4 md:grid-cols-4">
            <select v-model="newCategory" class="px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary-500">
              <option value="KEYWORD">关键词</option>
              <option value="WEBSITE">网站</option>
              <option value="DOMAIN">域名</option>
              <option value="APP">应用</option>
            </select>
            <input
              v-model="newPattern"
              type="text"
              :placeholder="newCategory === 'KEYWORD' ? '输入关键词' : newCategory === 'WEBSITE' ? '输入网址' : '输入域名'"
              class="px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary-500 md:col-span-2"
              @keyup.enter="addRule"
            />
            <div class="flex gap-2">
              <select v-model="newAction" class="flex-1 px-3 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary-500">
                <option v-for="a in actions" :key="a.value" :value="a.value">{{ a.label }}</option>
              </select>
              <button @click="addRule" class="px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors">添加</button>
            </div>
          </div>
        </div>

        <!-- 规则列表 -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
              <Filter class="w-5 h-5 text-primary-600" />
              过滤规则列表
            </h3>
            <div class="flex gap-2">
              <button
                v-for="cat in categories"
                :key="cat.value"
                @click="activeCategory = cat.value; loadRules()"
                :class="[
                  'px-3 py-1.5 text-sm rounded-lg transition-colors',
                  activeCategory === cat.value ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                ]"
              >
                {{ cat.label }}
              </button>
            </div>
          </div>

          <div v-if="rules.length === 0" class="text-center py-12 text-gray-500">
            <AlertTriangle class="w-12 h-12 mx-auto mb-3 text-gray-300" />
            <p>暂无过滤规则</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b border-gray-200 text-left text-sm text-gray-500">
                  <th class="py-3 px-4 font-medium">类型</th>
                  <th class="py-3 px-4 font-medium">匹配内容</th>
                  <th class="py-3 px-4 font-medium">动作</th>
                  <th class="py-3 px-4 font-medium">优先级</th>
                  <th class="py-3 px-4 font-medium">状态</th>
                  <th class="py-3 px-4 font-medium">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="rule in rules" :key="rule.id" class="border-b border-gray-100 hover:bg-gray-50">
                  <td class="py-3 px-4">
                    <span :class="[
                      'px-2 py-1 rounded text-xs font-medium',
                      rule.category === 'KEYWORD' ? 'bg-blue-100 text-blue-700' :
                      rule.category === 'WEBSITE' ? 'bg-purple-100 text-purple-700' :
                      rule.category === 'DOMAIN' ? 'bg-orange-100 text-orange-700' :
                      'bg-green-100 text-green-700'
                    ]">
                      {{ rule.category }}
                    </span>
                  </td>
                  <td class="py-3 px-4 text-sm text-gray-800 font-mono">{{ rule.pattern }}</td>
                  <td class="py-3 px-4">
                    <span :class="[
                      'px-2 py-1 rounded text-xs font-medium',
                      rule.action === 'BLOCK' ? 'bg-red-100 text-red-700' :
                      rule.action === 'WARN' ? 'bg-yellow-100 text-yellow-700' : 'bg-gray-100 text-gray-700'
                    ]">{{ rule.action === 'BLOCK' ? '拦截' : rule.action === 'WARN' ? '警告' : '记录' }}</span>
                  </td>
                  <td class="py-3 px-4 text-sm text-gray-600">{{ rule.priority }}</td>
                  <td class="py-3 px-4">
                    <span :class="rule.enabled ? 'text-green-600' : 'text-gray-400'" class="text-sm">{{ rule.enabled ? '启用' : '禁用' }}</span>
                  </td>
                  <td class="py-3 px-4">
                    <button @click="removeRule(rule.id)" class="p-2 text-gray-400 hover:text-red-500 transition-colors">
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      </template>
    </div>
  </div>
</template>
