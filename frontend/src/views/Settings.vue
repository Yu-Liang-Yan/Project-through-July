<script setup lang="ts">import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import Sidebar from '@/components/Sidebar.vue';
import Header from '@/components/Header.vue';
import { Camera, Fingerprint, Mic, Keyboard, Shield, Database, Download, Trash2 } from '@lucide/vue';
const router = useRouter();
const userStore = useUserStore();
const biometricSettings = ref({
 face: true,
 fingerprint: true,
 voice: false,
 keystroke: false
});
const securitySettings = ref({
 doubleVerify: true,
 antiCrack: true
});
const manageBiometric = () => {
 (window as any).showToast('正在打开生物特征管理...', 'info');
 setTimeout(() => {
 (window as any).showToast('生物特征已更新', 'success');
 }, 1500);
};
const exportData = () => {
 const data = {
 user: userStore.currentUser,
 devices: JSON.parse(localStorage.getItem('devices') || '[]'),
 timeSettings: JSON.parse(localStorage.getItem('timeSettings') || '{}'),
 blockList: JSON.parse(localStorage.getItem('blockList') || '{}')
 };
 const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
 const url = URL.createObjectURL(blob);
 const a = document.createElement('a');
 a.href = url;
 a.download = 'guardian-baby-data.json';
 a.click();
 URL.revokeObjectURL(url);
 (window as any).showToast('数据已导出', 'success');
};
const clearData = () => {
 if (!confirm('确定要清除所有数据吗？此操作不可恢复！'))
 return;
 if (!confirm('再次确认：清除所有数据？'))
 return;
 localStorage.clear();
 (window as any).showToast('所有数据已清除', 'info');
 setTimeout(() => {
 userStore.logout();
 router.push('/');
 }, 1000);
};
onMounted(() => {
 userStore.loadFromStorage();
 if (!userStore.isLoggedIn) {
 router.push('/');
 }
});
</script>

<template>
  <div class="page-container">
    <Sidebar />
    <main class="content-area lg:ml-64">
      <Header title="设置" subtitle="管理账户和系统设置" />

      <div class="max-w-3xl mx-auto space-y-6">
        <div class="card">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <Camera class="w-5 h-5 text-primary-500" />
            生物识别设置
          </h3>
          
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                  <Camera class="w-5 h-5 text-primary-600" />
                </div>
                <div>
                  <p class="font-medium text-gray-800">人脸识别</p>
                  <p class="text-sm text-gray-500">使用摄像头进行身份验证</p>
                </div>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input v-model="biometricSettings.face" type="checkbox" class="sr-only peer" />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
              </label>
            </div>

            <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                  <Fingerprint class="w-5 h-5 text-blue-600" />
                </div>
                <div>
                  <p class="font-medium text-gray-800">指纹识别</p>
                  <p class="text-sm text-gray-500">使用指纹传感器进行身份验证</p>
                </div>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input v-model="biometricSettings.fingerprint" type="checkbox" class="sr-only peer" />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
              </label>
            </div>

            <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                  <Mic class="w-5 h-5 text-green-600" />
                </div>
                <div>
                  <p class="font-medium text-gray-800">声纹识别</p>
                  <p class="text-sm text-gray-500">使用声音进行身份验证</p>
                </div>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input v-model="biometricSettings.voice" type="checkbox" class="sr-only peer" />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
              </label>
            </div>

            <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                  <Keyboard class="w-5 h-5 text-purple-600" />
                </div>
                <div>
                  <p class="font-medium text-gray-800">按键习惯识别</p>
                  <p class="text-sm text-gray-500">根据按键节奏进行身份验证</p>
                </div>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input v-model="biometricSettings.keystroke" type="checkbox" class="sr-only peer" />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
              </label>
            </div>
          </div>

          <button
            @click="manageBiometric"
            class="mt-4 w-full py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            管理生物特征
          </button>
        </div>

        <div class="card">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <Shield class="w-5 h-5 text-primary-500" />
            安全设置
          </h3>

          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div>
                <p class="font-medium text-gray-800">双重验证（修改设置时）</p>
                <p class="text-sm text-gray-500">修改重要设置时需要密码+生物特征验证</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input v-model="securitySettings.doubleVerify" type="checkbox" class="sr-only peer" />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
              </label>
            </div>

            <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div>
                <p class="font-medium text-gray-800">防破解保护</p>
                <p class="text-sm text-gray-500">检测并阻止非法卸载或绕过行为</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input v-model="securitySettings.antiCrack" type="checkbox" class="sr-only peer" />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
              </label>
            </div>
          </div>
        </div>

        <div class="card">
          <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <Database class="w-5 h-5 text-primary-500" />
            数据管理
          </h3>

          <p class="text-sm text-gray-500 mb-4">所有数据存储在本地设备上，仅基本信息同步至云端</p>

          <div class="flex gap-4">
            <button
              @click="exportData"
              class="flex-1 flex items-center justify-center gap-2 py-3 bg-gray-500 text-white font-medium rounded-lg hover:bg-gray-600 transition-colors"
            >
              <Download class="w-5 h-5" />
              导出数据
            </button>
            <button
              @click="clearData"
              class="flex-1 flex items-center justify-center gap-2 py-3 bg-red-500 text-white font-medium rounded-lg hover:bg-red-600 transition-colors"
            >
              <Trash2 class="w-5 h-5" />
              清除所有数据
            </button>
          </div>
        </div>

        <div class="card">
          <div class="text-center py-4">
            <p class="text-sm text-gray-400">守护宝贝 v1.0.0</p>
            <p class="text-xs text-gray-300 mt-1">智能监护系统</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
