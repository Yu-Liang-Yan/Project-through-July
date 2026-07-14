import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TimeSettings, BlockItem } from '@/types'

export const useSettingsStore = defineStore('settings', () => {
  const timeSettings = ref<TimeSettings>({
    id: 0,
    dailyHours: 2,
    dailyMinutes: 0,
    startTime: '09:00',
    endTime: '21:00',
    weeklyLimit: 14,
    monthlyLimit: 60
  })

  const blockList = ref<{ websites: BlockItem[]; games: BlockItem[]; apps: BlockItem[] }>({
    websites: [],
    games: [],
    apps: []
  })

  const setTimeSettings = (settings: TimeSettings) => {
    timeSettings.value = settings
    localStorage.setItem('timeSettings', JSON.stringify(settings))
  }

  const setBlockList = (type: 'websites' | 'games' | 'apps', list: BlockItem[]) => {
    blockList.value[type] = list
    localStorage.setItem('blockList', JSON.stringify(blockList.value))
  }

  const addBlockItem = (type: 'websites' | 'games' | 'apps', item: BlockItem) => {
    blockList.value[type].push(item)
    localStorage.setItem('blockList', JSON.stringify(blockList.value))
  }

  const removeBlockItem = (type: 'websites' | 'games' | 'apps', id: number) => {
    blockList.value[type] = blockList.value[type].filter(item => item.id !== id)
    localStorage.setItem('blockList', JSON.stringify(blockList.value))
  }

  const loadFromStorage = () => {
    const storedTime = localStorage.getItem('timeSettings')
    if (storedTime) {
      timeSettings.value = JSON.parse(storedTime)
    }
    const storedBlock = localStorage.getItem('blockList')
    if (storedBlock) {
      blockList.value = JSON.parse(storedBlock)
    }
  }

  return {
    timeSettings,
    blockList,
    setTimeSettings,
    setBlockList,
    addBlockItem,
    removeBlockItem,
    loadFromStorage
  }
})
