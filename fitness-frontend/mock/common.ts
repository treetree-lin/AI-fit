import type { MockMethod } from 'vite-plugin-mock'

export default [
  {
    url: '/api/common/upload/image',
    method: 'post',
    response: () => {
      return {
        code: 200,
        message: '上传成功',
        data: { url: 'https://picsum.photos/400/300?random=' + Date.now() },
        traceId: 'mock-' + Date.now()
      }
    }
  },
  {
    url: '/api/common/upload/video',
    method: 'post',
    response: () => {
      return {
        code: 200,
        message: '上传成功',
        data: { url: 'https://www.w3schools.com/html/mov_bbb.mp4' },
        traceId: 'mock-' + Date.now()
      }
    }
  }
] as MockMethod[]
