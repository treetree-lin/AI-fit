import request from '@/utils/request'
import type { Conversation } from '@/types'

export const getConversations = (startDate?: string, endDate?: string) => {
  return request.get<Conversation[]>('/v1/users/conversation', {
    params: { start_date: startDate, end_date: endDate }
  })
}

export const getWebSocketToken = () => {
  return request.get<string>('/v1/chat/websocket-token')
}

export const createWebSocketConnection = (jwtToken: string): WebSocket => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return new WebSocket(`${protocol}//${window.location.host}/api/v1/chat?token=${jwtToken}`)
}
