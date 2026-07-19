import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null)
  const isLoggedIn = ref(false)
  const token = ref<string | null>(null)

  const login = (user: User, jwtToken: string) => {
    currentUser.value = user
    token.value = jwtToken
    isLoggedIn.value = true
    localStorage.setItem('token', jwtToken)
    localStorage.setItem('isLoggedIn', 'true')
    localStorage.setItem('currentUser', JSON.stringify(user))
  }

  const logout = () => {
    currentUser.value = null
    token.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('currentUser')
  }

  const loadFromStorage = () => {
    const storedUser = localStorage.getItem('currentUser')
    const storedToken = localStorage.getItem('token')
    if (storedUser && storedToken) {
      currentUser.value = JSON.parse(storedUser)
      token.value = storedToken
      isLoggedIn.value = true
    }
  }

  return {
    currentUser,
    isLoggedIn,
    token,
    login,
    logout,
    loadFromStorage
  }
})
