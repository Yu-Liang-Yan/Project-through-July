<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  title: string
  value: string | number
  icon: Component
  subtitle?: string
  variant?: 'default' | 'success' | 'warning' | 'danger'
}>()

const getVariantClass = (variant?: string) => {
  switch (variant) {
    case 'success': return 'bg-green-50 border-green-200'
    case 'warning': return 'bg-yellow-50 border-yellow-200'
    case 'danger': return 'bg-red-50 border-red-200'
    default: return 'bg-white border-gray-200'
  }
}

const getIconClass = (variant?: string) => {
  switch (variant) {
    case 'success': return 'text-green-500 bg-green-100'
    case 'warning': return 'text-yellow-500 bg-yellow-100'
    case 'danger': return 'text-red-500 bg-red-100'
    default: return 'text-primary-500 bg-primary-100'
  }
}
</script>

<template>
  <div :class="[getVariantClass(variant), 'rounded-xl border p-6 shadow-sm']">
    <div class="flex items-center justify-between">
      <div>
        <p class="text-sm font-medium text-gray-500">{{ title }}</p>
        <h3 class="text-3xl font-bold text-gray-800 mt-1">{{ value }}</h3>
        <p v-if="subtitle" class="text-xs text-gray-400 mt-2">{{ subtitle }}</p>
      </div>
      <div :class="[getIconClass(variant), 'w-12 h-12 rounded-lg flex items-center justify-center']">
        <component :is="icon" class="w-6 h-6" />
      </div>
    </div>
  </div>
</template>
