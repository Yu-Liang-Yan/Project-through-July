import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Device } from '@/types'

export const useDevicesStore = defineStore('devices', () => {
  const devices = ref<Device[]>([])

  const setDevices = (list: Device[]) => {
    devices.value = list
    localStorage.setItem('devices', JSON.stringify(list))
  }

  const addDevice = (device: Device) => {
    devices.value.push(device)
    localStorage.setItem('devices', JSON.stringify(devices.value))
  }

  const removeDevice = (id: number) => {
    devices.value = devices.value.filter(d => d.id !== id)
    localStorage.setItem('devices', JSON.stringify(devices.value))
  }

  const loadFromStorage = () => {
    const stored = localStorage.getItem('devices')
    if (stored) {
      devices.value = JSON.parse(stored)
    }
  }

  return {
    devices,
    setDevices,
    addDevice,
    removeDevice,
    loadFromStorage
  }
})
