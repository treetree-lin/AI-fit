import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getWebSocketToken, createWebSocketConnection } from '@/api/chat'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  status?: 'pending' | 'loading' | 'finished' | 'error'
  timestamp?: string
}

export const useChatStore = defineStore('chat', () => {
  const list = ref<ChatMessage[]>([])
  const input = ref('')
  const ws = ref<WebSocket | null>(null)
  const wsStatus = ref<'CLOSED' | 'CONNECTING' | 'OPEN'>('CLOSED')
  const cmdToken = ref<string>('')

  const isSending = computed(() => {
    const last = list.value[list.value.length - 1]
    return last?.role === 'assistant' && ['pending', 'loading'].includes(last.status || '')
  })

  async function connect() {
    if (ws.value?.readyState === WebSocket.OPEN) return
    wsStatus.value = 'CONNECTING'
    try {
      const res = await getWebSocketToken()
      // 后端返回结构: { code: 200, data: { cmdToken: '...' } }
      cmdToken.value = (res.data as any)?.cmdToken || ''
      const jwtToken = localStorage.getItem('token') || ''
      const socket = createWebSocketConnection(jwtToken)

      socket.onopen = () => {
        wsStatus.value = 'OPEN'
      }

      socket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          const last = list.value[list.value.length - 1]
          if (!last || last.role !== 'assistant') return

          if (data.type === 'completion' && data.status === 'finished') {
            last.status = 'finished'
          } else if (data.error) {
            last.status = 'error'
          } else if (data.chunk) {
            last.status = 'loading'
            last.content += data.chunk
          }
        } catch (e) {
          // ignore non-json messages
        }
      }

      socket.onclose = () => {
        wsStatus.value = 'CLOSED'
      }

      socket.onerror = () => {
        wsStatus.value = 'CLOSED'
      }

      ws.value = socket
    } catch (e) {
      wsStatus.value = 'CLOSED'
    }
  }

  async function send(message: string) {
    // 如果未连接，先尝试连接
    if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
      await connect()
    }
    // 连接后再次检查
    if (ws.value?.readyState === WebSocket.OPEN) {
      ws.value.send(message)
    } else {
      throw new Error('WebSocket 连接失败，请刷新页面后重试')
    }
  }

  function disconnect() {
    ws.value?.close()
    ws.value = null
    wsStatus.value = 'CLOSED'
  }

  return {
    list,
    input,
    ws,
    wsStatus,
    isSending,
    cmdToken,
    connect,
    send,
    disconnect
  }
})
