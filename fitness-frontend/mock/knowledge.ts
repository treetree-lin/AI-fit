import type { MockMethod } from 'vite-plugin-mock'

const files = [
  {
    id: 1,
    fileMd5: 'a1b2c3d4e5f6',
    fileName: '健身饮食指南.pdf',
    totalSize: 2048000,
    status: 1,
    userId: '1',
    isPublic: true,
    createdAt: '2024-03-01T10:00:00',
    mergedAt: '2024-03-01T10:05:00'
  },
  {
    id: 2,
    fileMd5: 'b2c3d4e5f6a7',
    fileName: '增肌训练计划.docx',
    totalSize: 512000,
    status: 1,
    userId: '1',
    isPublic: true,
    createdAt: '2024-03-05T14:00:00',
    mergedAt: '2024-03-05T14:02:00'
  },
  {
    id: 3,
    fileMd5: 'c3d4e5f6a7b8',
    fileName: '减脂食谱大全.xlsx',
    totalSize: 1024000,
    status: 1,
    userId: '1',
    isPublic: false,
    createdAt: '2024-03-10T09:00:00',
    mergedAt: '2024-03-10T09:03:00'
  },
  {
    id: 4,
    fileMd5: 'd4e5f6a7b8c9',
    fileName: '运动损伤预防.pptx',
    totalSize: 3072000,
    status: 1,
    userId: '1',
    isPublic: true,
    createdAt: '2024-03-12T16:00:00',
    mergedAt: '2024-03-12T16:10:00'
  },
  {
    id: 5,
    fileMd5: 'e5f6a7b8c9d0',
    fileName: '训练动作视频合集.zip',
    totalSize: 52428800,
    status: 1,
    userId: '1',
    isPublic: false,
    createdAt: '2024-03-15T11:00:00',
    mergedAt: '2024-03-15T11:30:00'
  }
]

export default [
  {
    url: '/api/v1/documents/accessible',
    method: 'get',
    response: () => {
      return { code: 200, message: 'success', data: files.filter(f => f.isPublic), traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/v1/documents/uploads',
    method: 'get',
    response: () => {
      return { code: 200, message: 'success', data: files, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/v1/documents/:fileMd5',
    method: 'delete',
    response: () => {
      return { code: 200, message: '删除成功', data: 'success', traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/v1/documents/preview',
    method: 'get',
    response: () => {
      return { code: 200, message: 'success', data: '这是一个模拟的文档预览内容。\n\n在实际环境中，这里会显示文档的文本内容或转换后的预览格式。', traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/upload/chunk',
    method: 'post',
    response: () => {
      return { code: 200, message: '分片上传成功', data: { chunkIndex: 1 }, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/upload/merge',
    method: 'post',
    response: () => {
      return { code: 200, message: '合并成功', data: { fileName: 'merged-file.pdf' }, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/upload/status',
    method: 'get',
    response: () => {
      return { code: 200, message: 'success', data: { uploadedChunks: [1, 2, 3] }, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/upload/supported-types',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: {
          supportedExtensions: ['.pdf', '.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx', '.txt', '.md', '.zip', '.rar'],
          supportedTypes: ['PDF文档', 'Word文档', 'PowerPoint演示文稿', 'Excel表格', '文本文件', 'Markdown文档', 'ZIP压缩包', 'RAR压缩包']
        },
        traceId: 'mock-' + Date.now()
      }
    }
  }
] as MockMethod[]
