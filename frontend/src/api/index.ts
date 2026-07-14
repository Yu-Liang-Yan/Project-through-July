import type { User, Device, TimeSettings, BlockItem, UsageRecord } from '@/types'

const BASE_URL = '/api'

export interface ApiResponse<T> {
  success: boolean
  message: string
  data?: T
}

export const api = {
  auth: {
    login: (username: string, password: string): Promise<ApiResponse<User>> => {
      return fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      }).then(res => res.json())
    },
    register: (user: Omit<User, 'id'>): Promise<ApiResponse<User>> => {
      return fetch(`${BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user)
      }).then(res => res.json())
    }
  },
  devices: {
    list: (): Promise<ApiResponse<Device[]>> => {
      return fetch(`${BASE_URL}/devices`).then(res => res.json())
    },
    scan: (): Promise<ApiResponse<Device[]>> => {
      return fetch(`${BASE_URL}/devices/scan`, { method: 'POST' }).then(res => res.json())
    },
    add: (device: Omit<Device, 'id'>): Promise<ApiResponse<Device>> => {
      return fetch(`${BASE_URL}/devices`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(device)
      }).then(res => res.json())
    },
    remove: (id: number): Promise<ApiResponse<void>> => {
      return fetch(`${BASE_URL}/devices/${id}`, { method: 'DELETE' }).then(res => res.json())
    }
  },
  timeSettings: {
    get: (): Promise<ApiResponse<TimeSettings>> => {
      return fetch(`${BASE_URL}/time-settings`).then(res => res.json())
    },
    update: (settings: TimeSettings): Promise<ApiResponse<TimeSettings>> => {
      return fetch(`${BASE_URL}/time-settings`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(settings)
      }).then(res => res.json())
    }
  },
  blockList: {
    list: (type: string): Promise<ApiResponse<BlockItem[]>> => {
      return fetch(`${BASE_URL}/block-list?type=${type}`).then(res => res.json())
    },
    add: (item: BlockItem): Promise<ApiResponse<BlockItem>> => {
      return fetch(`${BASE_URL}/block-list`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(item)
      }).then(res => res.json())
    },
    remove: (id: number): Promise<ApiResponse<void>> => {
      return fetch(`${BASE_URL}/block-list/${id}`, { method: 'DELETE' }).then(res => res.json())
    }
  },
  statistics: {
    usage: (period: string): Promise<ApiResponse<UsageRecord[]>> => {
      return fetch(`${BASE_URL}/statistics/usage?period=${period}`).then(res => res.json())
    },
    dashboard: (): Promise<ApiResponse<{ todayUsage: number; weekUsage: number; deviceCount: number; alertCount: number }>> => {
      return fetch(`${BASE_URL}/statistics/dashboard`).then(res => res.json())
    }
  }
}
