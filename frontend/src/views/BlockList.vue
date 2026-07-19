<script setup lang="ts">import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { api } from '@/api';
import type { BlockItem } from '@/types';
import Sidebar from '@/components/Sidebar.vue';
import Header from '@/components/Header.vue';
import { Plus, Trash2, Globe, Gamepad2, Smartphone } from '@lucide/vue';

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref<'websites' | 'games' | 'apps'>('websites');
const newItem = ref('');
const blockData = ref<{ websites: BlockItem[]; games: BlockItem[]; apps: BlockItem[] }>({
  websites: [], games: [], apps: []
});

const tabs = [
 { value: 'websites', label: '网站', icon: Globe },
 { value: 'games', label: '游戏', icon: Gamepad2 },
 { value: 'apps', label: '应用', icon: Smartphone }
];

const currentList = computed(() => blockData.value[activeTab.value] || []);

const toast = (msg: string, type: string) => {
  ;(window as any).showToast?.(msg, type)
};

const loadBlockList = async (type: string) => {
  const uid = userStore.currentUser?.id ?? 1;
  try {
    const res = await api.blockList.list(uid, type);
    if (res.success && res.data) {
      blockData.value[type as keyof typeof blockData.value] = res.data;
    }
  } catch (e) {
    console.error(e);
  }
};

const addBlockItem = async () => {
  if (!newItem.value.trim()) {
    toast('请输入要禁止的内容', 'warning');
    return;
  }
  const uid = userStore.currentUser?.id ?? 1;
  try {
    const res = await api.blockList.add(uid, activeTab.value, newItem.value.trim());
    if (res.success) {
      newItem.value = '';
      toast('已添加到禁止列表', 'success');
      await loadBlockList(activeTab.value);
    } else {
      toast(res.message || '添加失败', 'error');
    }
  } catch (e) {
    toast('添加失败，请重试', 'error');
  }
};

const removeBlockItem = async (id: number) => {
  if (!confirm('确定要移除该禁止项吗？')) return;
  const uid = userStore.currentUser?.id ?? 1;
  try {
    await api.blockList.remove(id, uid);
    toast('已从禁止列表移除', 'info');
    await loadBlockList(activeTab.value);
  } catch (e) {
    toast('移除失败', 'error');
  }
};

onMounted(async () => {
  userStore.loadFromStorage();
  if (!userStore.isLoggedIn) {
    router.push('/');
    return;
  }
  await Promise.all([loadBlockList('websites'), loadBlockList('games'), loadBlockList('apps')]);
});
</script>

<template>
  <div class="page-container">
    <Sidebar />
    <main class="content-area lg:ml-64">
      <Header title="禁止列表" subtitle="管理禁止访问的网站、游戏和应用" />

      <div class="flex border-b border-gray-200 mb-6">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          @click="activeTab = tab.value as any"
          :class="[
            'flex items-center gap-2 px-6 py-3 font-medium transition-colors border-b-2',
            activeTab === tab.value ? 'border-primary-500 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700'
          ]"
        >
          <component :is="tab.icon" class="w-5 h-5" />
          {{ tab.label }}
          <span
            :class="[
              'ml-2 px-2 py-0.5 text-xs rounded-full',
              activeTab === tab.value ? 'bg-primary-100 text-primary-600' : 'bg-gray-100 text-gray-500'
            ]"
          >
            {{ currentList.length }}
          </span>
        </button>
      </div>

      <div class="card mb-6">
        <div class="flex gap-3">
          <input
            v-model="newItem"
            type="text"
            :placeholder="`输入要禁止的${tabs.find(t => t.value === activeTab)?.label}名称`"
            class="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            @keyup.enter="addBlockItem"
          />
          <button
            @click="addBlockItem"
            class="flex items-center gap-2 px-6 py-3 bg-primary-500 text-white font-medium rounded-lg hover:bg-primary-600 transition-colors"
          >
            <Plus class="w-5 h-5" />
            添加
          </button>
        </div>
      </div>

      <div class="card">
        <div v-if="currentList.length === 0" class="text-center py-12">
          <component :is="tabs.find(t => t.value === activeTab)?.icon" class="w-16 h-16 mx-auto text-gray-300 mb-4" />
          <p class="text-gray-500">暂无禁止项</p>
          <p class="text-sm text-gray-400 mt-2">在上方输入框中添加要禁止的{{ tabs.find(t => t.value === activeTab)?.label }}</p>
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="item in currentList"
            :key="item.id"
            class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center">
                <component :is="tabs.find(t => t.value === activeTab)?.icon" class="w-5 h-5 text-red-600" />
              </div>
              <div>
                <p class="font-medium text-gray-800">{{ item.name }}</p>
                <p class="text-xs text-gray-400">添加于 {{ new Date(item.createdAt).toLocaleDateString() }}</p>
              </div>
            </div>
            <button
              @click="removeBlockItem(item.id)"
              class="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
            >
              <Trash2 class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
