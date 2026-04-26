<template>
  <div class="knowledge-page">
    <!-- 头部工具栏 -->
    <div class="page-toolbar">
      <h2 class="page-title">文件列表</h2>
      <div class="toolbar-actions">
        <el-button :icon="Search" @click="searchVisible = true">检索知识库</el-button>
        <el-button type="primary" :icon="Plus" @click="openUploadDialog">新增</el-button>
        <el-button :icon="Refresh" @click="loadFiles">刷新</el-button>
        <el-dropdown trigger="click" @command="handleColumnCommand">
          <el-button :icon="Setting">列设置</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="col in columnOptions" :key="col.key" :command="col.key">
                <el-checkbox v-model="col.visible" @click.stop>{{ col.label }}</el-checkbox>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 文件表格 -->
    <div class="table-card glow-card">
      <el-table
        :data="filteredFiles"
        style="width: 100%"
        v-loading="loading"
        :header-cell-style="{ background: 'transparent', fontWeight: 600, color: 'var(--text-secondary)' }"
      >
        <el-table-column v-if="isColumnVisible('fileName')" label="文件名" min-width="200">
          <template #default="{ row }">
            <div class="file-cell">
              <div class="file-icon" :class="getFileIconClass(row.fileName)">
                <el-icon size="20"><Document /></el-icon>
                <span class="file-ext">{{ getFileExt(row.fileName) }}</span>
              </div>
              <span class="file-name" :title="row.fileName">{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column v-if="isColumnVisible('fileMd5')" prop="fileMd5" label="MD5" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="md5-text">{{ row.fileMd5 }}</span>
          </template>
        </el-table-column>

        <el-table-column v-if="isColumnVisible('totalSize')" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatSize(row.totalSize) }}
          </template>
        </el-table-column>

        <el-table-column v-if="isColumnVisible('status')" label="上传状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ row.status === 1 ? '已完成' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column v-if="isColumnVisible('isPublic')" label="是否公开" width="100">
          <template #default="{ row }">
            <el-tag :type="getPublicFlag(row) ? 'success' : 'warning'" effect="light" round>
              {{ getPublicFlag(row) ? '公开' : '私有' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column v-if="isColumnVisible('createdAt')" label="上传时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-tooltip content="预览" placement="top">
                <el-button class="action-btn" circle plain type="primary" size="small" @click="preview(row.fileName)">
                  <el-icon size="14"><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="下载" placement="top">
                <el-button class="action-btn" circle plain type="info" size="small" @click="download(row.fileName)">
                  <el-icon size="14"><Download /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button class="action-btn" circle plain type="danger" size="small" @click="remove(row.fileMd5)">
                  <el-icon size="14"><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无文件" />
        </template>
      </el-table>
    </div>

    <!-- 检索弹窗 -->
    <el-dialog v-model="searchVisible" title="检索知识库" width="480px" :close-on-click-modal="false">
      <el-input
        v-model="searchKeyword"
        placeholder="输入文件名关键词搜索"
        :prefix-icon="Search"
        clearable
        @keyup.enter="searchVisible = false"
      />
      <template #footer>
        <el-button @click="searchVisible = false; searchKeyword = ''">重置</el-button>
        <el-button type="primary" @click="searchVisible = false">确定</el-button>
      </template>
    </el-dialog>

    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadVisible" title="文件上传" width="520px" :close-on-click-modal="false">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="是否公开" required>
          <el-radio-group v-model="uploadForm.isPublic">
            <el-radio :label="true">公开</el-radio>
            <el-radio :label="false">私有</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上传文件" required>
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileSelect"
            drag
            style="width: 100%"
          >
            <el-icon size="40" color="var(--text-tertiary)"><Upload /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持格式：{{ supportedExtensions.join('、') || 'PDF、Word、Excel、PPT、TXT 等' }}
              </div>
            </template>
          </el-upload>
          <div v-if="uploadFileName" class="selected-file">
            <el-icon><Document /></el-icon>
            <span>{{ uploadFileName }}</span>
            <el-icon class="remove-file" @click="clearSelectedFile"><Close /></el-icon>
          </div>
        </el-form-item>

        <!-- 上传进度 -->
        <el-form-item v-if="uploading" label="上传进度">
          <div class="upload-progress-area">
            <div class="upload-info">
              <span class="upload-filename">{{ uploadFileName }}</span>
              <span class="upload-status">{{ uploadStatusText }}</span>
            </div>
            <el-progress :percentage="uploadProgress" :status="uploadProgress >= 100 ? 'success' : ''" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="startUpload">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" :title="`预览：${previewFileName}`" width="70%">
      <div class="preview-header" v-if="previewUrl">
        <el-button link type="primary" :icon="Link" @click="openExternal(previewUrl)">
          查看原文件
        </el-button>
      </div>
      <div class="preview-container">
        <iframe v-if="isOfficeOrPdf && previewUrl" :src="previewUrl" style="width: 100%; height: 55vh; border: none" />
        <pre v-else class="preview-text">{{ previewContent }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Refresh, Setting, Upload, Document, Close, View, Download, Delete, Link
} from '@element-plus/icons-vue'
import {
  getAccessibleFiles, deleteDocument, previewFile, downloadFile,
  getSupportedTypes, uploadChunk, mergeChunks
} from '@/api/knowledge'
import type { FileUpload } from '@/types'

const files = ref<FileUpload[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const searchVisible = ref(false)
const supportedExtensions = ref<string[]>([])

// 列设置
const columnOptions = ref([
  { key: 'fileName', label: '文件名', visible: true },
  { key: 'fileMd5', label: 'MD5', visible: true },
  { key: 'totalSize', label: '文件大小', visible: true },
  { key: 'status', label: '上传状态', visible: true },
  { key: 'isPublic', label: '是否公开', visible: true },
  { key: 'createdAt', label: '上传时间', visible: true },
])

const isColumnVisible = (key: string) => {
  return columnOptions.value.find(c => c.key === key)?.visible ?? true
}

const handleColumnCommand = (cmd: string) => {
  const col = columnOptions.value.find(c => c.key === cmd)
  if (col) col.visible = !col.visible
}

// 过滤后的文件列表
const filteredFiles = computed(() => {
  if (!searchKeyword.value.trim()) return files.value
  const kw = searchKeyword.value.toLowerCase()
  return files.value.filter(f => f.fileName.toLowerCase().includes(kw))
})

// 上传相关
const uploadVisible = ref(false)
const uploadForm = ref({ isPublic: false })
const uploadRef = ref<any>(null)
const selectedFile = ref<File | null>(null)
const uploadFileName = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadStatusText = ref('')
const CHUNK_SIZE = 5 * 1024 * 1024

// 预览相关
const previewVisible = ref(false)
const previewUrl = ref('')
const previewContent = ref('')
const previewFileName = ref('')
const isOfficeOrPdf = ref(false)

const loadFiles = async () => {
  loading.value = true
  try {
    const res = await getAccessibleFiles()
    files.value = res.data || []
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

const loadSupportedTypes = async () => {
  try {
    const res = await getSupportedTypes()
    const data = res.data as any
    if (Array.isArray(data?.supportedExtensions)) {
      supportedExtensions.value = data.supportedExtensions
    } else if (Array.isArray(data?.supportedTypes)) {
      supportedExtensions.value = data.supportedTypes
    } else if (data && typeof data === 'object') {
      // mock 返回的是分类对象，提取所有值
      const exts: string[] = []
      Object.values(data).forEach((v: any) => {
        if (Array.isArray(v)) exts.push(...v)
      })
      supportedExtensions.value = exts
    }
  } catch (e) {
    supportedExtensions.value = ['.pdf', '.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx', '.txt']
  }
}

const openUploadDialog = () => {
  uploadVisible.value = true
  uploadForm.value.isPublic = false
  selectedFile.value = null
  uploadFileName.value = ''
  uploadProgress.value = 0
  uploadStatusText.value = ''
}

const handleFileSelect = (uploadFile: any) => {
  const file = uploadFile.raw as File
  if (!file) return
  selectedFile.value = file
  uploadFileName.value = file.name
}

const clearSelectedFile = () => {
  selectedFile.value = null
  uploadFileName.value = ''
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const simpleHash = async (str: string): Promise<string> => {
  const encoder = new TextEncoder()
  const data = encoder.encode(str)
  const hashBuffer = await crypto.subtle.digest('SHA-256', data)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  const fullHash = hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
  return fullHash.substring(0, 32)
}

const startUpload = async () => {
  const file = selectedFile.value
  if (!file) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  uploadProgress.value = 0
  uploadStatusText.value = '准备上传...'

  try {
    const fileMd5 = await simpleHash(file.name + file.size + Date.now())
    const totalChunks = Math.ceil(file.size / CHUNK_SIZE)

    for (let i = 0; i < totalChunks; i++) {
      const start = i * CHUNK_SIZE
      const end = Math.min(start + CHUNK_SIZE, file.size)
      const chunk = file.slice(start, end)
      uploadStatusText.value = `上传中 ${i + 1}/${totalChunks}...`

      await uploadChunk(fileMd5, i, file.size, file.name, totalChunks, uploadForm.value.isPublic, chunk)
      uploadProgress.value = Math.round(((i + 1) / totalChunks) * 100)
    }

    uploadStatusText.value = '合并文件中...'
    await mergeChunks(fileMd5, file.name)
    ElMessage.success('上传成功')
    uploadVisible.value = false
    loadFiles()
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
    uploadStatusText.value = ''
    selectedFile.value = null
    uploadFileName.value = ''
  }
}

const formatSize = (size: number) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / 1024 / 1024).toFixed(2) + ' MB'
  return (size / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return d.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  }).replace(/\//g, '-')
}

// 兼容后端返回 public / 前端类型 isPublic
const getPublicFlag = (row: FileUpload) => {
  return (row as any).isPublic ?? (row as any).public ?? false
}

const getFileExt = (fileName: string) => {
  const idx = fileName.lastIndexOf('.')
  return idx > -1 ? fileName.substring(idx + 1).toUpperCase() : 'FILE'
}

const getFileIconClass = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase() || ''
  if (['pdf'].includes(ext)) return 'icon-pdf'
  if (['doc', 'docx'].includes(ext)) return 'icon-doc'
  if (['xls', 'xlsx'].includes(ext)) return 'icon-xls'
  if (['ppt', 'pptx'].includes(ext)) return 'icon-ppt'
  if (['txt', 'md'].includes(ext)) return 'icon-txt'
  if (['zip', 'rar', '7z'].includes(ext)) return 'icon-zip'
  return 'icon-default'
}

const download = async (fileName: string) => {
  try {
    const res = await downloadFile(fileName)
    const data = res.data as any
    const downloadUrl = data?.downloadUrl || data?.url
    if (downloadUrl) {
      // 后端返回预签名 URL，直接打开下载
      const a = document.createElement('a')
      a.href = downloadUrl
      a.download = fileName
      a.target = '_blank'
      a.click()
      ElMessage.success('开始下载')
    } else {
      ElMessage.warning('未获取到下载链接')
    }
  } catch (e) {
    // 拦截器已弹出具体错误提示，此处静默处理避免重复
  }
}

const preview = async (fileName: string) => {
  try {
    // 1. 获取预览文本内容
    const previewRes = await previewFile(fileName)
    const previewData = previewRes.data as any
    previewContent.value = previewData?.content || previewData || '无预览内容'
    previewFileName.value = previewData?.fileName || fileName

    // 2. 尝试获取下载链接（用于 PDF/Office iframe 预览）
    try {
      const downloadRes = await downloadFile(fileName)
      const downloadData = downloadRes.data as any
      const url = downloadData?.downloadUrl || downloadData?.url
      if (url) {
        previewUrl.value = url
        isOfficeOrPdf.value = /\.(pdf|doc|docx|ppt|pptx|xls|xlsx)$/i.test(fileName)
      } else {
        isOfficeOrPdf.value = false
      }
    } catch (e) {
      isOfficeOrPdf.value = false
    }

    previewVisible.value = true
  } catch (e) {
    // 拦截器已弹出具体错误提示，此处静默处理避免重复
    previewVisible.value = false
  }
}

const openExternal = (url: string) => {
  window.open(url, '_blank')
}

const remove = async (fileMd5: string) => {
  try {
    await ElMessageBox.confirm('确定删除该文件吗？', '提示', { type: 'warning' })
    await deleteDocument(fileMd5)
    ElMessage.success('已删除')
    loadFiles()
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  loadFiles()
  loadSupportedTypes()
})
</script>

<style scoped>
.knowledge-page {
  padding: 20px;
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.table-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

/* 文件单元格 */
.file-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  position: relative;
  width: 40px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
  color: #475569;
}

.file-ext {
  position: absolute;
  bottom: 2px;
  right: 2px;
  font-size: 9px;
  font-weight: 700;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  padding: 0 3px;
  border-radius: 2px;
  line-height: 14px;
}

.icon-pdf { background: linear-gradient(135deg, #fee2e2, #fecaca); color: #dc2626; }
.icon-pdf .file-ext { background: #dc2626; }

.icon-doc { background: linear-gradient(135deg, #dbeafe, #bfdbfe); color: #2563eb; }
.icon-doc .file-ext { background: #2563eb; }

.icon-xls { background: linear-gradient(135deg, #dcfce7, #bbf7d0); color: #16a34a; }
.icon-xls .file-ext { background: #16a34a; }

.icon-ppt { background: linear-gradient(135deg, #ffedd5, #fed7aa); color: #ea580c; }
.icon-ppt .file-ext { background: #ea580c; }

.icon-txt { background: linear-gradient(135deg, #f3f4f6, #e5e7eb); color: #4b5563; }
.icon-txt .file-ext { background: #4b5563; }

.icon-zip { background: linear-gradient(135deg, #f5f3ff, #e9d5ff); color: #7c3aed; }
.icon-zip .file-ext { background: #7c3aed; }

.icon-default { background: linear-gradient(135deg, #e2e8f0, #cbd5e1); color: #475569; }
.icon-default .file-ext { background: #475569; }

.file-name {
  color: var(--text-primary);
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.md5-text {
  font-family: monospace;
  font-size: 12px;
  color: var(--text-secondary);
}

/* 选中文件 */
.selected-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 8px 12px;
  background: var(--bg-page);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-primary);
}

.selected-file .remove-file {
  margin-left: auto;
  cursor: pointer;
  color: var(--text-tertiary);
  transition: color 0.2s;
}
.selected-file .remove-file:hover {
  color: #ef4444;
}

/* 上传进度 */
.upload-progress-area {
  padding: 12px;
  background: var(--bg-page);
  border-radius: 8px;
  width: 100%;
}

.upload-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.upload-filename {
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 60%;
}

.upload-status {
  color: var(--text-tertiary);
}

/* 预览 */
.preview-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.preview-container {
  min-height: 300px;
}

.preview-text {
  background: var(--bg-page);
  padding: 16px;
  border-radius: 8px;
  max-height: 60vh;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--text-primary);
  font-family: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 13px;
  line-height: 1.7;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  align-items: center;
  gap: 6px;
}

.action-btn {
  transition: all 0.25s ease;
}
.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
}

/* Element Plus 覆盖 */
:deep(.el-upload-dragger) {
  background: var(--bg-page);
  border-color: var(--border-color);
  width: 100%;
}

:deep(.el-upload__text) {
  color: var(--text-secondary);
}

:deep(.el-upload__tip) {
  color: var(--text-tertiary);
}

:deep(.el-table) {
  background: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
}

:deep(.el-table th.el-table__cell) {
  background: transparent;
}

:deep(.el-table td.el-table__cell) {
  background: transparent;
}

:deep(.el-table tr) {
  background: transparent;
}

html.dark .file-icon {
  filter: brightness(0.85);
}
</style>
