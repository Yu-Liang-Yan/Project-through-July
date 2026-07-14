<script setup lang="ts">
import { ref } from 'vue'
import { CheckCircle, XCircle, AlertCircle, Info } from '@lucide/vue'

const toastList = ref<{ id: number; message: string; type: 'success' | 'error' | 'warning' | 'info' }[]>([])
let toastId = 0

const showToast = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') => {
  const id = ++toastId
  toastList.value.push({ id, message, type })
  setTimeout(() => {
    toastList.value = toastList.value.filter(t => t.id !== id)
  }, 3000)
}

defineExpose({ showToast })

const getIcon = (type: string) => {
  switch (type) {
    case 'success': return CheckCircle
    case 'error': return XCircle
    case 'warning': return AlertCircle
    default: return Info
  }
}

const getColorClass = (type: string) => {
  switch (type) {
    case 'success': return 'bg-green-500'
    case 'error': return 'bg-red-500'
    case 'warning': return 'bg-yellow-500'
    default: return 'bg-blue-500'
  }
}

;(window as any).showToast = showToast
</script>

<template>
  <div class="fixed bottom-6 right-6 z-50 flex flex-col gap-2">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toastList"
        :key="toast.id"
        :class="[getColorClass(toast.type), 'text-white px-4 py-3 rounded-lg shadow-lg flex items-center gap-3 min-w-[280px] max-w-[400px]']"
      >
        <component :is="getIcon(toast.type)" class="w-5 h-5 flex-shrink-0" />
        <span class="text-sm font-medium">{{ toast.message }}</span>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active {
  transition: all 0.3s ease-out;
}

.toast-leave-active {
  transition: all 0.3s ease-in;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
