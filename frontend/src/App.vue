<script setup lang="ts">
import { RouterView } from 'vue-router'
import Toast from '@/components/Toast.vue'
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
