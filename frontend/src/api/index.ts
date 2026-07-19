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
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`
  return headers
}

async function request<T>(url: string, options?: RequestInit): Promise<ApiResponse<T>> {
  const res = await fetch(url, options)
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    return { success: false, message: body.message || `请求失败 (${res.status})` }
  }
  return res.json()
}

export const api = {
  // ======================== auth ========================
  auth: {
    login: (username: string, password: string) =>
      request<{ id: number; username: string; phone: string; userType: string; ageGroup: string; token: string; createdAt: string }>(
        `${BASE_URL}/auth/login`,
        { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }) }
      ),

    register: (dto: { username: string; phone: string; password: string; userType: string; ageGroup?: string }) =>
      request<{ id: number; username: string; phone: string; userType: string; ageGroup: string }>(
        `${BASE_URL}/auth/register`,
        { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dto) }
      ),

    verifyPassword: (userId: number, password: string) =>
      request<boolean>(
        `${BASE_URL}/auth/verify`,
        { method: 'POST', headers: authHeaders(), body: JSON.stringify({ userId, password }) }
      ),

    changePassword: (userId: number, oldPassword: string, newPassword: string) =>
      request<null>(
        `${BASE_URL}/auth/change-password`,
        { method: 'POST', headers: authHeaders(), body: JSON.stringify({ userId, oldPassword, newPassword }) }
      ),
  },

  // ======================== users ========================
  users: {
    getById: (id: number) =>
      request<{ id: number; username: string; phone: string; userType: string; ageGroup: string }>(
        `${BASE_URL}/users/${id}`,
        { headers: authHeaders() }
      ),

    listProtected: (guardianId: number) =>
      request<{ id: number; username: string; phone: string; userType: string; ageGroup: string }[]>(
        `${BASE_URL}/users/protected?guardianId=${guardianId}`,
        { headers: authHeaders() }
      ),

    updateProfile: (userId: number, data: { phone?: string; ageGroup?: string }) =>
      request<null>(
        `${BASE_URL}/users/profile`,
        { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ userId, ...data }) }
      ),
  },

  // ======================== devices ========================
  devices: {
    list: (userId: number) =>
      request<Device[]>(`${BASE_URL}/devices?userId=${userId}`, { headers: authHeaders() }),

    add: (userId: number, name: string, type: string) =>
      request<Device>(
        `${BASE_URL}/devices?userId=${userId}`,
        { method: 'POST', headers: authHeaders(), body: JSON.stringify({ name, type }) }
      ),

    remove: (id: number, userId: number) =>
      request<void>(
        `${BASE_URL}/devices/${id}?userId=${userId}`,
        { method: 'DELETE', headers: authHeaders() }
      ),
  },

  // ======================== timeSettings ========================
  timeSettings: {
    get: (userId: number) =>
      request<TimeSettings>(`${BASE_URL}/time-settings?userId=${userId}`, { headers: authHeaders() }),

    update: (userId: number, settings: Partial<TimeSettings>) =>
      request<TimeSettings>(
        `${BASE_URL}/time-settings?userId=${userId}`,
        { method: 'PUT', headers: authHeaders(), body: JSON.stringify(settings) }
      ),
  },

  // ======================== blockList ========================
  blockList: {
    list: (userId: number, type: string) =>
      request<BlockItem[]>(`${BASE_URL}/block-list?userId=${userId}&type=${type}`, { headers: authHeaders() }),

    add: (userId: number, type: string, name: string) =>
      request<BlockItem>(
        `${BASE_URL}/block-list?userId=${userId}`,
        { method: 'POST', headers: authHeaders(), body: JSON.stringify({ type, name }) }
      ),

    remove: (id: number, userId: number) =>
      request<void>(
        `${BASE_URL}/block-list/${id}?userId=${userId}`,
        { method: 'DELETE', headers: authHeaders() }
      ),
  },

  // ======================== statistics ========================
  statistics: {
    usage: (userId: number, period: string) =>
      request<UsageRecord[]>(`${BASE_URL}/statistics/usage?userId=${userId}&period=${period}`, { headers: authHeaders() }),

    dashboard: (userId: number) =>
      request<{ todayUsage: number; weekUsage: number; deviceCount: number; alertCount: number; bindingCount: number; onlineDeviceCount: number; recentAlerts: any[] }>(
        `${BASE_URL}/statistics/dashboard?userId=${userId}`,
        { headers: authHeaders() }
      ),
  },

  // ======================== approvals ========================
  approvals: {
    submit: (dto: { requesterId: number; type: string; description: string; extraMinutes?: number; targetName?: string }) =>
      request<ApprovalRequest>(
        `${BASE_URL}/approvals`,
        { method: 'POST', headers: authHeaders(), body: JSON.stringify(dto) }
      ),

    pending: (guardianId: number) =>
      request<ApprovalRequest[]>(`${BASE_URL}/approvals/pending?guardianId=${guardianId}`, { headers: authHeaders() }),

    myRequests: (requesterId: number) =>
      request<ApprovalRequest[]>(`${BASE_URL}/approvals/my?requesterId=${requesterId}`, { headers: authHeaders() }),

    approve: (id: number, reviewerId: number, message?: string) =>
      request<ApprovalRequest>(
        `${BASE_URL}/approvals/${id}/approve?reviewerId=${reviewerId}`,
        { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ message: message || '已同意' }) }
      ),

    reject: (id: number, reviewerId: number, reason?: string) =>
      request<ApprovalRequest>(
        `${BASE_URL}/approvals/${id}/reject?reviewerId=${reviewerId}`,
        { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ reason: reason || '已拒绝' }) }
      ),

    pendingCount: () =>
      request<number>(`${BASE_URL}/approvals/pending-count`, { headers: authHeaders() }),
  },

  // ======================== bindings ========================
  bindings: {
    bind: (guardianId: number, protectedUserId: number) =>
      request<GuardianBinding>(
        `${BASE_URL}/bindings?guardianId=${guardianId}&protectedUserId=${protectedUserId}`,
        { method: 'POST', headers: authHeaders() }
      ),

    unbind: (id: number) =>
      request<void>(`${BASE_URL}/bindings/${id}`, { method: 'DELETE', headers: authHeaders() }),

    listProtectedUsers: (guardianId: number) =>
      request<GuardianBinding[]>(`${BASE_URL}/bindings/protected-users?guardianId=${guardianId}`, { headers: authHeaders() }),

    listGuardians: (protectedUserId: number) =>
      request<GuardianBinding[]>(`${BASE_URL}/bindings/guardians?protectedUserId=${protectedUserId}`, { headers: authHeaders() }),
  },

  // ======================== alerts ========================
  alerts: {
    create: (dto: { protectedUserId: number; type: string; severity: string; title: string; message: string }) =>
      request<Alert>(
        `${BASE_URL}/alerts`,
        { method: 'POST', headers: authHeaders(), body: JSON.stringify(dto) }
      ),

    listForGuardian: (guardianId: number) =>
      request<Alert[]>(`${BASE_URL}/alerts/guardian?guardianId=${guardianId}`, { headers: authHeaders() }),

    listForProtectedUser: (protectedUserId: number) =>
      request<Alert[]>(`${BASE_URL}/alerts/protected-user?protectedUserId=${protectedUserId}`, { headers: authHeaders() }),

    markRead: (id: number) =>
      request<Alert>(`${BASE_URL}/alerts/${id}/read`, { method: 'PUT', headers: authHeaders() }),

    resolve: (id: number) =>
      request<Alert>(`${BASE_URL}/alerts/${id}/resolve`, { method: 'PUT', headers: authHeaders() }),

    unreadCount: (guardianId: number) =>
      request<number>(`${BASE_URL}/alerts/count?guardianId=${guardianId}`, { headers: authHeaders() }),
  },

  // ======================== filters ========================
  filters: {
    add: (userId: number, category: string, pattern: string, action: string, priority?: number) =>
      request<ContentFilterRule>(
        `${BASE_URL}/filters`,
        { method: 'POST', headers: authHeaders(), body: JSON.stringify({ userId: String(userId), category, pattern, action, priority: String(priority || 1) }) }
      ),

    list: (userId: number, category?: string) => {
      let url = `${BASE_URL}/filters?userId=${userId}`
      if (category) url += `&category=${category}`
      return request<ContentFilterRule[]>(url, { headers: authHeaders() })
    },

    remove: (id: number) =>
      request<void>(`${BASE_URL}/filters/${id}`, { method: 'DELETE', headers: authHeaders() }),
  },

  // ======================== biometrics ========================
  biometrics: {
    register: (userId: number, type: string, confidenceThreshold?: number) =>
      request<BiometricRecord>(
        `${BASE_URL}/biometrics`,
        { method: 'POST', headers: authHeaders(), body: JSON.stringify({ userId, type, confidenceThreshold: confidenceThreshold || 0.85 }) }
      ),

    list: (userId: number) =>
      request<BiometricRecord[]>(`${BASE_URL}/biometrics?userId=${userId}`, { headers: authHeaders() }),

    setStatus: (id: number, active: boolean) =>
      request<BiometricRecord>(`${BASE_URL}/biometrics/${id}/status?active=${active}`, { method: 'PUT', headers: authHeaders() }),

    verify: (userId: number, type: string) =>
      request<boolean>(
        `${BASE_URL}/biometrics/verify`,
        { method: 'POST', headers: authHeaders(), body: JSON.stringify({ userId: String(userId), type }) }
      ),
  },

  // ======================== auditLogs ========================
  auditLogs: {
    listByUser: (userId: number) =>
      request<AuditLogEntry[]>(`${BASE_URL}/audit-logs/user?userId=${userId}`, { headers: authHeaders() }),

    listAll: () =>
      request<AuditLogEntry[]>(`${BASE_URL}/audit-logs`, { headers: authHeaders() }),
  },

  // ======================== data ========================
  data: {
    export: (userId: number) =>
      request<Record<string, any>>(`${BASE_URL}/data/export?userId=${userId}`, { headers: authHeaders() }),

    clear: (userId: number) =>
      request<Record<string, number>>(`${BASE_URL}/data/clear?userId=${userId}`, { method: 'DELETE', headers: authHeaders() }),
  },
}
