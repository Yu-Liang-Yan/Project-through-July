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
  status: 'online' | 'offline' | 'LOCKED'
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
  bindingCount: number
  onlineDeviceCount: number
  recentAlerts: AlertSummary[]
}

export interface AlertSummary {
  id: number
  title: string
  severity: string
  type: string
  protectedUserName: string
  createdAt: string
}

export interface AuditLogEntry {
  id: number
  userId: number
  username: string
  action: string
  detail: string
  ip: string
  createdAt: string
}

export interface ApprovalRequest {
  id: number
  type: 'TIME_EXTENSION' | 'UNBLOCK' | 'ACCESS'
  description: string
  extraMinutes?: number
  targetName?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'EXPIRED'
  responseMessage?: string
  requesterName: string
  requesterId: number
  createdAt: string
  reviewedAt?: string
}

// Phase 2 types
export interface GuardianBinding {
  id: number
  guardianId: number
  guardianName: string
  protectedUserId: number
  protectedUserName: string
  protectedAgeGroup: string
  status: string
  createdAt: string
}

export interface Alert {
  id: number
  protectedUserId: number
  protectedUserName: string
  guardianId?: number
  type: string
  severity: string
  title: string
  message: string
  status: 'NEW' | 'READ' | 'RESOLVED'
  createdAt: string
  resolvedAt?: string
}

export interface ContentFilterRule {
  id: number
  userId: number
  category: 'KEYWORD' | 'WEBSITE' | 'DOMAIN' | 'APP'
  pattern: string
  action: 'BLOCK' | 'WARN' | 'LOG'
  priority: number
  enabled: boolean
  createdAt: string
}

export interface BiometricRecord {
  id: number
  userId: number
  type: 'FACE' | 'FINGERPRINT' | 'VOICEPRINT' | 'KEYSTROKE'
  confidenceThreshold: number
  status: string
  registeredAt: string
  lastVerifiedAt?: string
}
