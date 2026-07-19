export interface User {
  id: number
  username: string
  phone: string
  userType: 'guardian' | 'protected'
  ageGroup?: string
  createdAt: string
}

export interface Device {
  id: number
  name: string
  type: 'phone' | 'tablet' | 'computer' | 'watch' | 'reader'
  status: 'online' | 'offline'
  lastActive: string
  registeredAt: string
}

export interface TimeSettings {
  id: number
  dailyHours: number
  dailyMinutes: number
  startTime: string
  endTime: string
  weeklyLimit: number
  monthlyLimit: number
}

export interface BlockItem {
  id: number
  type: 'websites' | 'games' | 'apps'
  name: string
  keywords?: string[]
  createdAt: string
}

export interface UsageRecord {
  id: number
  device: string
  app: string
  startTime: string
  endTime: string
  duration: number
  createdAt: string
}

export interface ToastMessage {
  id: number
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
}

export interface DashboardStats {
  todayUsage: number
  weekUsage: number
  deviceCount: number
  alertCount: number
}

export interface ApprovalRequest {
  id: number
  type: 'TIME_EXTENSION' | 'UNBLOCK' | 'ACCESS'
  description: string
  extraMinutes?: number
  targetName?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  responseMessage?: string
  requesterName: string
  requesterId: number
  createdAt: string
  reviewedAt?: string
}
