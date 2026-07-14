import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null)
  const isLoggedIn = ref(false)

  const login = (user: User) => {
    currentUser.value = user
    isLoggedIn.value = true
    localStorage.setItem('isLoggedIn', 'true')
    localStorage.setItem('currentUser', JSON.stringify(user))
  }

  const logout = () => {
    currentUser.value = null
    isLoggedIn.value = false
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('currentUser')
  }

  const loadFromStorage = () => {
    const stored = localStorage.getItem('currentUser')
    if (stored) {
      currentUser.value = JSON.parse(stored)
      isLoggedIn.value = true
    }
  }

  return {
    currentUser,
    isLoggedIn,
    login,
    logout,
    loadFromStorage
  }
})
