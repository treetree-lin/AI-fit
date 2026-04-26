import type { MockMethod } from 'vite-plugin-mock'

const conversations = [
  {
    id: 1,
    question: '请帮我制定一个减脂计划',
    answer: '好的，根据您的情况，我为您制定了以下减脂计划：\n\n1. 饮食控制：每日热量缺口 300-500 大卡\n2. 有氧运动：每周 4-5 次，每次 30-45 分钟\n3. 力量训练：每周 3 次，保持肌肉量\n4. 睡眠管理：保证 7-8 小时优质睡眠\n\n建议您从低强度开始，循序渐进。',
    timestamp: '2024-03-20 10:00:00'
  },
  {
    id: 2,
    question: '深蹲时膝盖内扣怎么办？',
    answer: '膝盖内扣是深蹲中常见的问题，可能由以下原因导致：\n\n1. 臀中肌力量不足\n2. 髋关节活动度受限\n3. 踝关节灵活性差\n\n改善方法：\n- 加入蚌式开合、侧卧抬腿等动作强化臀中肌\n- 深蹲前做髋关节热身\n- 穿举重鞋或脚跟垫片改善踝背屈\n- 有意识地将膝盖向外推，对准脚尖方向',
    timestamp: '2024-03-21 14:30:00'
  },
  {
    id: 3,
    question: '增肌期每天需要多少蛋白质？',
    answer: '增肌期建议每日蛋白质摄入量为 1.6-2.2 克/公斤体重。\n\n例如：如果您体重 70 公斤，每天需要摄入 112-154 克蛋白质。\n\n优质蛋白质来源：\n- 鸡胸肉、鱼肉、牛肉\n- 鸡蛋、牛奶、希腊酸奶\n- 乳清蛋白粉（方便补充）\n\n建议将蛋白质均匀分配到每餐中，每餐 20-40 克为宜。',
    timestamp: '2024-03-22 09:15:00'
  }
]

export default [
  {
    url: '/api/v1/users/conversation',
    method: 'get',
    response: () => {
      return { code: 200, message: 'success', data: conversations, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/v1/chat/websocket-token',
    method: 'get',
    response: () => {
      return { code: 200, message: 'success', data: 'mock-websocket-token-' + Date.now(), traceId: 'mock-' + Date.now() }
    }
  }
] as MockMethod[]
