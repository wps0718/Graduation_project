import { getToken } from './auth'
import { BASE_URL } from './constant'

let socketTask = null
let isConnected = false
let heartbeatTimer = null
let reconnectTimer = null
let onMessageCallback = null
let reconnectAttempts = 0
const MAX_RECONNECT = 3

function getWsUrl() {
  const token = getToken()
  if (!token) return null
  const httpToWs = BASE_URL.replace('http://', 'ws://').replace('https://', 'wss://')
  return `${httpToWs}/ws/chat?token=${token}`
}

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (isConnected && socketTask) {
      socketTask.send({
        data: JSON.stringify({ type: 'ping' }),
        fail: () => {}
      })
    }
  }, 30000)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

function scheduleReconnect() {
  if (reconnectAttempts >= MAX_RECONNECT) return
  stopHeartbeat()
  if (reconnectTimer) return
  reconnectAttempts++
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connect(onMessageCallback)
  }, 2000 * reconnectAttempts)
}

export function connect(onMessage) {
  const url = getWsUrl()
  if (!url) return
  disconnect()
  onMessageCallback = onMessage
  socketTask = uni.connectSocket({
    url,
    success: () => {},
    fail: () => {
      isConnected = false
    }
  })
  socketTask.onOpen(() => {
    isConnected = true
    reconnectAttempts = 0
    startHeartbeat()
  })
  socketTask.onMessage((res) => {
    try {
      const msg = JSON.parse(res.data)
      if (msg.type === 'pong') return
      if (onMessageCallback) onMessageCallback(msg)
    } catch (e) {}
  })
  socketTask.onClose(() => {
    isConnected = false
    stopHeartbeat()
    scheduleReconnect()
  })
  socketTask.onError(() => {
    isConnected = false
    stopHeartbeat()
  })
}

export function send(type, data) {
  if (!isConnected || !socketTask) return false
  socketTask.send({
    data: JSON.stringify({ type, data }),
    fail: () => {}
  })
  return true
}

export function sendChatMessage(receiverId, content, msgType = 1, productId = null) {
  return send('chat', { receiverId, content, msgType, productId })
}

export function sendReadAck(sessionKey) {
  return send('read', { sessionKey })
}

export function isConnectedStatus() {
  return isConnected
}

export function disconnect() {
  stopHeartbeat()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  reconnectAttempts = MAX_RECONNECT
  if (socketTask) {
    socketTask.close({ fail: () => {} })
    socketTask = null
  }
  isConnected = false
  onMessageCallback = null
}
