import type { Device, TimeSettings, BlockItem, UsageRecord, ApprovalRequest } from '@/types'

const BASE_URL = '/api'

export interface ApiResponse<T> {
  success: boolean
  message: string
  data?: T
}

function getToken(): string | null {
  return localStorage.getItem('token')
}

function authHeaders(): Record<string, string> {
  const token = getToken()
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

export const api = {
  auth: {
    login: (username: string, password: string): Promise<ApiResponse<{ id: number; username: string; phone: string; userType: string; ageGroup: string; token: string; createdAt: string }>> => {
      return fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      }).then(r => r.json())
    },
    register: (dto: { username: string; phone: string; password: string; userType: string; ageGroup?: string }): Promise<ApiResponse<{ id: number; username: string; phone: string; userType: string; ageGroup: string }>> => {
      return fetch(`${BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dto)
      }).then(r => r.json())
    },
    verifyPassword: (userId: number, password: string): Promise<ApiResponse<boolean>> => {
      return fetch(`${BASE_URL}/auth/verify`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ userId, password })
      }).then(r => r.json())
    }
  },
  devices: {
    list: (userId: number): Promise<ApiResponse<Device[]>> => {
      return fetch(`${BASE_URL}/devices?userId=${userId}`, { headers: authHeaders() }).then(r => r.json())
    },
    add: (userId: number, name: string, type: string): Promise<ApiResponse<Device>> => {
      return fetch(`${BASE_URL}/devices?userId=${userId}`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ name, type })
      }).then(r => r.json())
    },
    remove: (id: number, userId: number): Promise<ApiResponse<void>> => {
      return fetch(`${BASE_URL}/devices/${id}?userId=${userId}`, { method: 'DELETE', headers: authHeaders() }).then(r => r.json())
    }
  },
  timeSettings: {
    get: (userId: number): Promise<ApiResponse<TimeSettings>> => {
      return fetch(`${BASE_URL}/time-settings?userId=${userId}`, { headers: authHeaders() }).then(r => r.json())
    },
    update: (userId: number, settings: Partial<TimeSettings>): Promise<ApiResponse<TimeSettings>> => {
      return fetch(`${BASE_URL}/time-settings?userId=${userId}`, {
        method: 'PUT',
        headers: authHeaders(),
        body: JSON.stringify(settings)
      }).then(r => r.json())
    }
  },
  blockList: {
    list: (userId: number, type: string): Promise<ApiResponse<BlockItem[]>> => {
      return fetch(`${BASE_URL}/block-list?userId=${userId}&type=${type}`, { headers: authHeaders() }).then(r => r.json())
    },
    add: (userId: number, type: string, name: string): Promise<ApiResponse<BlockItem>> => {
      return fetch(`${BASE_URL}/block-list?userId=${userId}`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ type, name })
      }).then(r => r.json())
    },
    remove: (id: number, userId: number): Promise<ApiResponse<void>> => {
      return fetch(`${BASE_URL}/block-list/${id}?userId=${userId}`, { method: 'DELETE', headers: authHeaders() }).then(r => r.json())
    }
  },
  statistics: {
    usage: (userId: number, period: string): Promise<ApiResponse<UsageRecord[]>> => {
      return fetch(`${BASE_URL}/statistics/usage?userId=${userId}&period=${period}`, { headers: authHeaders() }).then(r => r.json())
    },
    dashboard: (userId: number): Promise<ApiResponse<{ todayUsage: number; weekUsage: number; deviceCount: number; alertCount: number }>> => {
      return fetch(`${BASE_URL}/statistics/dashboard?userId=${userId}`, { headers: authHeaders() }).then(r => r.json())
    }
  },
  approvals: {
    submit: (dto: { requesterId: number; type: string; description: string; extraMinutes?: number; targetName?: string }): Promise<ApiResponse<ApprovalRequest>> => {
      return fetch(`${BASE_URL}/approvals`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify(dto)
      }).then(r => r.json())
    },
    pending: (guardianId: number): Promise<ApiResponse<ApprovalRequest[]>> => {
      return fetch(`${BASE_URL}/approvals/pending?guardianId=${guardianId}`, { headers: authHeaders() }).then(r => r.json())
    },
    myRequests: (requesterId: number): Promise<ApiResponse<ApprovalRequest[]>> => {
      return fetch(`${BASE_URL}/approvals/my?requesterId=${requesterId}`, { headers: authHeaders() }).then(r => r.json())
    },
    approve: (id: number, reviewerId: number, message?: string): Promise<ApiResponse<ApprovalRequest>> => {
      return fetch(`${BASE_URL}/approvals/${id}/approve?reviewerId=${reviewerId}`, {
        method: 'PUT',
        headers: authHeaders(),
        body: JSON.stringify({ message: message || '已同意' })
      }).then(r => r.json())
    },
    reject: (id: number, reviewerId: number, reason?: string): Promise<ApiResponse<ApprovalRequest>> => {
      return fetch(`${BASE_URL}/approvals/${id}/reject?reviewerId=${reviewerId}`, {
        method: 'PUT',
        headers: authHeaders(),
        body: JSON.stringify({ reason: reason || '已拒绝' })
      }).then(r => r.json())
    }
  }
}
