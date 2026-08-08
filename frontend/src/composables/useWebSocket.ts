import { ref, onUnmounted } from 'vue'

type WsMessageType = 'APPROVAL_NEW' | 'APPROVAL_RESULT' | 'ALERT' | 'DEVICE_STATUS'
type WsHandler = (type: WsMessageType, payload: any) => void

const handlers = new Set<WsHandler>()
let ws: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let intentionalClose = false

export const pendingApprovalCount = ref(0)
export const unreadAlertCount = ref(0)

function notify(type: WsMessageType, payload: any) {
  handlers.forEach(h => h(type, payload))
}

function connect() {
  const token = localStorage.getItem('token')
  if (!token) return

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${protocol}//${window.location.hostname}:8080/ws?token=${token}`

  try {
    ws = new WebSocket(url)
  } catch {
    scheduleReconnect()
    return
  }

  ws.onopen = () => {
    console.log('[WS] Connected')
  }

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      const type = msg.type as WsMessageType

      if (type === 'APPROVAL_NEW') pendingApprovalCount.value++
      if (type === 'ALERT') unreadAlertCount.value++

      notify(type, msg.payload)
    } catch { /* ignore malformed */ }
  }

  ws.onclose = () => {
    if (!intentionalClose) scheduleReconnect()
  }

  ws.onerror = () => {
    ws?.close()
  }
}

function scheduleReconnect() {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connect()
  }, 5000)
}

export function useWebSocket() {
  const onMessage = (handler: WsHandler) => {
    handlers.add(handler)
    onUnmounted(() => handlers.delete(handler))
  }

  return {
    onMessage,
    connectWs: () => { intentionalClose = false; connect() },
    disconnectWs: () => { intentionalClose = true; ws?.close(); ws = null },
    pendingApprovalCount,
    unreadAlertCount
  }
}
