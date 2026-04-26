<template>
  <div class="chat-page">
    <!-- 左侧会话列表（仿Kimi） -->
    <div class="chat-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <el-button type="primary" class="new-chat-btn" @click="startNewChat">
          <el-icon><Plus /></el-icon>
          <span v-if="!sidebarCollapsed">新对话</span>
        </el-button>
      </div>
      <div class="session-list" @scroll="handleSessionScroll">
        <div v-if="sessionLoading" class="session-loading"><el-icon class="is-loading"><Loading /></el-icon></div>
        <div v-for="session in sessions" :key="session.id" class="session-item" :class="{ active: currentSessionId === session.id }" @click="switchSession(session.id)">
          <div class="session-title">{{ session.title || '新对话' }}</div>
          <div class="session-meta">{{ formatTime(session.lastMessageTime) }}</div>
        </div>
        <div v-if="sessionHasMore" class="session-load-more" @click="loadMoreSessions">加载更多</div>
      </div>
      <div class="sidebar-footer">
        <el-button text class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon v-if="sidebarCollapsed"><Expand /></el-icon>
          <el-icon v-else><Fold /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 右侧聊天区 -->
    <div class="chat-main">
      <div class="chat-header">
        <div class="chat-title">
          <el-icon size="20" color="#3b82f6"><ChatDotRound /></el-icon>
          <span>AI 教练</span>
          <el-tag v-if="chatStore.wsStatus === 'OPEN'" size="small" type="success" effect="light">在线</el-tag>
          <el-tag v-else-if="chatStore.wsStatus === 'CONNECTING'" size="small" type="warning" effect="light">连接中...</el-tag>
          <el-tag v-else size="small" type="info" effect="light">离线</el-tag>
        </div>
      </div>

      <div ref="messageListRef" class="message-list">
        <div v-for="(msg, idx) in chatStore.list" :key="idx" class="message-row" :class="msg.role">
          <div class="message-avatar">
            <img v-if="msg.role === 'assistant'" src="/logo.png" alt="AI" class="ai-avatar" />
            <el-avatar v-else :size="36" :src="userStore.user?.avatarUrl" icon="UserFilled" />
          </div>
          <!-- 有内容时显示内容气泡 -->
          <div v-if="msg.content" class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div v-if="msg.timestamp" class="message-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
          <!-- 空内容且是 assistant pending 时显示 typing -->
          <div v-else-if="msg.role === 'assistant'" class="message-bubble typing-bubble">
            <div class="typing-dots"><span /><span /><span /></div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <div class="input-wrapper">
          <el-input v-model="chatStore.input" type="textarea" :rows="3" placeholder="输入你的健身问题..." class="chat-input" @keydown.enter.prevent="handleSend" />
          <div class="input-actions">
            <el-button type="primary" size="default" class="send-btn" :disabled="!chatStore.input.trim() || chatStore.isSending" @click="handleSend">
              <el-icon><Promotion /></el-icon>发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { ChatDotRound, Plus, Expand, Fold, Promotion, Loading } from '@element-plus/icons-vue'

const chatStore = useChatStore()
const userStore = useUserStore()

const messageListRef = ref<HTMLDivElement>()
const sidebarCollapsed = ref(false)

const sessions = ref<any[]>([])
const sessionPage = ref(0)
const sessionLoading = ref(false)
const sessionHasMore = ref(true)

const currentSessionId = ref<string>('')

const scrollToBottom = () => {
  nextTick(() => {
    const el = messageListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const handleSend = async () => {
  if (!chatStore.input.trim() || chatStore.isSending) return

  // 添加用户消息
  chatStore.list.push({
    role: 'user',
    content: chatStore.input,
    status: 'finished',
    timestamp: new Date().toISOString()
  })

  // 添加 AI 占位消息（显示 typing）
  chatStore.list.push({
    role: 'assistant',
    content: '',
    status: 'pending',
    timestamp: new Date().toISOString()
  })

  try {
    await chatStore.send(chatStore.input)
  } catch (e: any) {
    // 发送失败时，将最后一条 AI 消息改为错误提示
    const last = chatStore.list[chatStore.list.length - 1]
    if (last && last.role === 'assistant') {
      last.status = 'error'
      last.content = e.message || '发送失败，请检查网络连接后重试'
    }
  }
  chatStore.input = ''
  scrollToBottom()
}

const formatTime = (timestamp?: number | string) => {
  if (!timestamp) return ''
  const d = new Date(timestamp)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const startNewChat = () => {
  chatStore.list = []
  chatStore.input = ''
  currentSessionId.value = ''
}

const switchSession = (id: string) => {
  currentSessionId.value = id
  chatStore.list = []
}

const loadMoreSessions = async () => {
  if (sessionLoading.value) return
  sessionLoading.value = true
  setTimeout(() => { sessionLoading.value = false; sessionHasMore.value = false }, 500)
}

const handleSessionScroll = (e: Event) => {
  const el = e.target as HTMLDivElement
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 20) {
    loadMoreSessions()
  }
}

watch(() => chatStore.list.length, scrollToBottom)

onMounted(() => {
  chatStore.connect()
  loadMoreSessions()
})
</script>

<style scoped>
.chat-page { display: flex; height: calc(100vh - 70px); background: var(--bg-page); overflow: hidden; }
.chat-sidebar { width: 240px; background: var(--bg-card); border-right: 1px solid var(--border-color); display: flex; flex-direction: column; transition: width 0.3s ease; flex-shrink: 0; }
.chat-sidebar.collapsed { width: 60px; }
.sidebar-header { padding: 12px; border-bottom: 1px solid var(--border-color); }
.new-chat-btn { width: 100%; border-radius: 10px; font-weight: 600; background: var(--btn-primary-gradient); border: none; overflow: hidden; white-space: nowrap; }
.chat-sidebar.collapsed .new-chat-btn { width: 36px; padding: 8px; }
.chat-sidebar.collapsed .new-chat-btn span { display: none; }
.session-list { flex: 1; overflow-y: auto; padding: 8px; }
.session-item { padding: 10px 12px; border-radius: 10px; cursor: pointer; transition: all 0.2s; margin-bottom: 4px; }
.session-item:hover { background: var(--bg-hover); }
.session-item.active { background: var(--color-primary-bg); border-left: 3px solid var(--color-primary); }
.session-title { font-size: 13px; font-weight: 500; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.session-meta { font-size: 11px; color: var(--text-muted); margin-top: 2px; }
.session-loading, .session-load-more { text-align: center; padding: 12px; color: var(--text-muted); font-size: 12px; cursor: pointer; }
.sidebar-footer { padding: 12px; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; }
.collapse-btn { padding: 8px; }

.chat-main { flex: 1; display: flex; flex-direction: column; min-width: 0; position: relative; }
.chat-header { height: 56px; border-bottom: 1px solid var(--border-color); display: flex; align-items: center; padding: 0 20px; background: var(--bg-card); flex-shrink: 0; }

.message-list { flex: 1; overflow-y: auto; padding: 20px; scroll-behavior: smooth; }
.message-row { display: flex; gap: 12px; margin-bottom: 20px; align-items: flex-end; }
.message-row.user { flex-direction: row-reverse; }
.message-avatar { flex-shrink: 0; }
.ai-avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; }
.message-bubble { max-width: 70%; padding: 12px 16px; border-radius: 18px; font-size: 14px; line-height: 1.6; box-shadow: var(--shadow-sm); min-width: 48px; min-height: 40px; display: flex; flex-direction: column; justify-content: center; }
.message-row.user .message-bubble { background: linear-gradient(135deg, #3b82f6, #60a5fa); color: #fff; border-bottom-right-radius: 4px; }
.message-row.assistant .message-bubble { background: var(--bg-card); border: 1px solid var(--border-color); color: var(--text-primary); border-bottom-left-radius: 4px; }
.message-content { white-space: pre-wrap; word-break: break-word; }
.message-time { font-size: 11px; color: var(--text-muted); margin-top: 6px; text-align: right; }
.message-row.user .message-time { color: rgba(255,255,255,0.7); }

.typing-bubble { padding: 14px 20px; }
.typing-dots { display: flex; gap: 5px; align-items: center; justify-content: center; }
.typing-dots span { width: 8px; height: 8px; background: var(--color-primary); border-radius: 50%; animation: bounce 1.4s infinite ease-in-out both; }
.typing-dots span:nth-child(1) { animation-delay: -0.32s; }
.typing-dots span:nth-child(2) { animation-delay: -0.16s; }
@keyframes bounce { 0%, 80%, 100% { transform: scale(0.6); } 40% { transform: scale(1); } }

.chat-input-area { padding: 16px 20px; background: var(--bg-card); border-top: 1px solid var(--border-color); }
.input-wrapper { display: flex; gap: 12px; align-items: flex-end; background: var(--bg-page); border: 1px solid var(--border-color); border-radius: 16px; padding: 8px 12px; }
.chat-input { flex: 1; }
.chat-input :deep(.el-textarea__inner) { background: transparent; border: none; box-shadow: none; padding: 4px 8px; resize: none; font-size: 14px; color: var(--text-primary); }
.chat-input :deep(.el-textarea__inner::placeholder) { color: var(--text-muted); }
.send-btn { border-radius: 10px; background: var(--btn-primary-gradient); border: none; font-weight: 600; padding: 8px 16px; }
.send-btn:disabled { opacity: 0.5; }
</style>
