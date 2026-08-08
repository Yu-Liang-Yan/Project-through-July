<script setup lang="ts">
import { RouterView } from 'vue-router'
import Toast from '@/components/Toast.vue'
import { useUserStore } from '@/stores/user'
import { useWebSocket } from '@/composables/useWebSocket'
import { useToast } from '@/composables/useToast'
import { watch } from 'vue'

const userStore = useUserStore()
const { onMessage, connectWs, disconnectWs } = useWebSocket()
const { toast } = useToast()

// 登录后连接 WebSocket，登出断开
watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) connectWs()
  else disconnectWs()
}, { immediate: true })

// 实时消息处理
onMessage((type, payload) => {
  if (type === 'APPROVAL_NEW') {
    toast(`${payload.requesterName} 发起了新的${payload.type === 'TIME_EXTENSION' ? '时长延长' : payload.type === 'UNBLOCK' ? '解除封锁' : '访问'}请求`, 'info')
  } else if (type === 'APPROVAL_RESULT') {
    const status = payload.status === 'APPROVED' ? '已批准' : '已拒绝'
    toast(`你的${payload.type === 'TIME_EXTENSION' ? '时长延长' : '解除封锁'}请求${status}`, payload.status === 'APPROVED' ? 'success' : 'warning')
  } else if (type === 'ALERT') {
    toast(`告警: ${payload.title}`, payload.severity === 'HIGH' ? 'error' : 'warning')
  } else if (type === 'DEVICE_STATUS') {
    toast(`设备 ${payload.name} ${payload.status === 'LOCKED' ? '已锁定' : '已解锁'}`, 'info')
  }
})
</script>

<template>
  <div class="app">
    <RouterView />
    <Toast />
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
}
</style>
