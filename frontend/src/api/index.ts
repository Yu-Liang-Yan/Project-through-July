import type { Device, TimeSettings, BlockItem, UsageRecord, ApprovalRequest, GuardianBinding, Alert, ContentFilterRule, BiometricRecord, AuditLogEntry } from '@/types'

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
  },
  bindings: {
    bind: (guardianId: number, protectedUserId: number): Promise<ApiResponse<GuardianBinding>> => {
      return fetch(`${BASE_URL}/bindings?guardianId=${guardianId}&protectedUserId=${protectedUserId}`, {
        method: 'POST',
        headers: authHeaders()
      }).then(r => r.json())
    },
    unbind: (id: number): Promise<ApiResponse<void>> => {
      return fetch(`${BASE_URL}/bindings/${id}`, { method: 'DELETE', headers: authHeaders() }).then(r => r.json())
    },
    listProtectedUsers: (guardianId: number): Promise<ApiResponse<GuardianBinding[]>> => {
      return fetch(`${BASE_URL}/bindings/protected-users?guardianId=${guardianId}`, { headers: authHeaders() }).then(r => r.json())
    },
    listGuardians: (protectedUserId: number): Promise<ApiResponse<GuardianBinding[]>> => {
      return fetch(`${BASE_URL}/bindings/guardians?protectedUserId=${protectedUserId}`, { headers: authHeaders() }).then(r => r.json())
    }
  },
  alerts: {
    listForGuardian: (guardianId: number): Promise<ApiResponse<Alert[]>> => {
      return fetch(`${BASE_URL}/alerts/guardian?guardianId=${guardianId}`, { headers: authHeaders() }).then(r => r.json())
    },
    listForProtectedUser: (protectedUserId: number): Promise<ApiResponse<Alert[]>> => {
      return fetch(`${BASE_URL}/alerts/protected-user?protectedUserId=${protectedUserId}`, { headers: authHeaders() }).then(r => r.json())
    },
    markRead: (id: number): Promise<ApiResponse<Alert>> => {
      return fetch(`${BASE_URL}/alerts/${id}/read`, { method: 'PUT', headers: authHeaders() }).then(r => r.json())
    },
    resolve: (id: number): Promise<ApiResponse<Alert>> => {
      return fetch(`${BASE_URL}/alerts/${id}/resolve`, { method: 'PUT', headers: authHeaders() }).then(r => r.json())
    },
    unreadCount: (guardianId: number): Promise<ApiResponse<number>> => {
      return fetch(`${BASE_URL}/alerts/count?guardianId=${guardianId}`, { headers: authHeaders() }).then(r => r.json())
    }
  },
  filters: {
    add: (userId: number, category: string, pattern: string, action: string, priority?: number): Promise<ApiResponse<ContentFilterRule>> => {
      return fetch(`${BASE_URL}/filters`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ userId: String(userId), category, pattern, action, priority: String(priority || 1) })
      }).then(r => r.json())
    },
    list: (userId: number, category?: string): Promise<ApiResponse<ContentFilterRule[]>> => {
      let url = `${BASE_URL}/filters?userId=${userId}`;
      if (category) url += `&category=${category}`;
      return fetch(url, { headers: authHeaders() }).then(r => r.json())
    },
    remove: (id: number): Promise<ApiResponse<void>> => {
      return fetch(`${BASE_URL}/filters/${id}`, { method: 'DELETE', headers: authHeaders() }).then(r => r.json())
    }
  },
  biometrics: {
    register: (userId: number, type: string, confidenceThreshold?: number): Promise<ApiResponse<BiometricRecord>> => {
      return fetch(`${BASE_URL}/biometrics`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ userId, type, confidenceThreshold: confidenceThreshold || 0.85 })
      }).then(r => r.json())
    },
    list: (userId: number): Promise<ApiResponse<BiometricRecord[]>> => {
      return fetch(`${BASE_URL}/biometrics?userId=${userId}`, { headers: authHeaders() }).then(r => r.json())
    },
    setStatus: (id: number, active: boolean): Promise<ApiResponse<BiometricRecord>> => {
      return fetch(`${BASE_URL}/biometrics/${id}/status?active=${active}`, { method: 'PUT', headers: authHeaders() }).then(r => r.json())
    },
    verify: (userId: number, type: string): Promise<ApiResponse<boolean>> => {
      return fetch(`${BASE_URL}/biometrics/verify`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ userId: String(userId), type })
      }).then(r => r.json())
    }
  },
  auditLogs: {
    listByUser: (userId: number): Promise<ApiResponse<AuditLogEntry[]>> => {
      return fetch(`${BASE_URL}/audit-logs/user?userId=${userId}`, { headers: authHeaders() }).then(r => r.json())
    },
    listAll: (): Promise<ApiResponse<AuditLogEntry[]>> => {
      return fetch(`${BASE_URL}/audit-logs`, { headers: authHeaders() }).then(r => r.json())
    }
  }
}
