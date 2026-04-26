import type { MockMethod } from 'vite-plugin-mock'

// 生成最近 30 天的打卡记录
const generateRecords = () => {
  const records = []
  const workoutIds = [1, 2, 3, 4, 5]
  for (let i = 0; i < 15; i++) {
    const date = new Date()
    date.setDate(date.getDate() - i * 2)
    records.push({
      id: 100 + i,
      userId: 1,
      workoutId: workoutIds[i % workoutIds.length],
      recordDate: date.toISOString().split('T')[0],
      durationMinutes: 30 + (i % 5) * 10,
      caloriesBurned: 200 + (i % 8) * 50,
      completed: true,
      rating: 4 + (i % 2),
      notes: i % 3 === 0 ? '今天状态不错，超额完成目标' : '',
      createdAt: date.toISOString(),
      updatedAt: date.toISOString(),
      steps: [
        { id: 1000 + i, stepId: 1, exerciseName: '示例动作A', actualSets: 4, actualReps: '12次', weightUsed: 20 },
        { id: 1001 + i, stepId: 2, exerciseName: '示例动作B', actualSets: 3, actualReps: '10次', weightUsed: 15 }
      ]
    })
  }
  return records
}

let records = generateRecords()

// 生成近 7 天数据
const generateLast7Days = () => {
  const days = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    days.push({
      date: date.toISOString().split('T')[0],
      duration: 30 + Math.floor(Math.random() * 40),
      calories: 200 + Math.floor(Math.random() * 300)
    })
  }
  return days
}

export default [
  {
    url: '/api/records/checkin',
    method: 'post',
    response: ({ body }: any) => {
      const newRecord = {
        id: Date.now(),
        userId: body.userId || 1,
        workoutId: body.workoutId || 1,
        recordDate: body.date || new Date().toISOString().split('T')[0],
        durationMinutes: body.duration || 30,
        caloriesBurned: body.calories || 200,
        completed: true,
        rating: 5,
        notes: body.notes || '',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        steps: []
      }
      records.unshift(newRecord)
      return { code: 200, message: '打卡成功', data: newRecord, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/records/user/:userId',
    method: 'get',
    response: () => {
      return { code: 200, message: 'success', data: records, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/records/user/:userId/date-range',
    method: 'get',
    response: ({ query }: any) => {
      const start = query.startDate
      const end = query.endDate
      const list = records.filter((r: any) => r.recordDate >= start && r.recordDate <= end)
      return { code: 200, message: 'success', data: list, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/records/:id',
    method: 'get',
    response: ({ query }: any) => {
      const id = Number(query.id)
      const r = records.find((x: any) => x.id === id) || records[0]
      return { code: 200, message: 'success', data: r, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/records/:id',
    method: 'put',
    response: ({ body, query }: any) => {
      const id = Number(query.id)
      const idx = records.findIndex((x: any) => x.id === id)
      if (idx >= 0) {
        records[idx] = { ...records[idx], ...body, updatedAt: new Date().toISOString() }
        return { code: 200, message: '更新成功', data: records[idx], traceId: 'mock-' + Date.now() }
      }
      return { code: 200, message: 'success', data: records[0], traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/records/:id',
    method: 'delete',
    response: ({ query }: any) => {
      const id = Number(query.id)
      records = records.filter((x: any) => x.id !== id)
      return { code: 200, message: '删除成功', data: 'success', traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/records/stats/monthly/:userId',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: {
          totalWorkouts: records.length,
          totalDuration: records.reduce((sum: number, r: any) => sum + r.durationMinutes, 0),
          totalCalories: records.reduce((sum: number, r: any) => sum + r.caloriesBurned, 0)
        },
        traceId: 'mock-' + Date.now()
      }
    }
  },
  {
    url: '/api/records/stats/overview/:userId',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: {
          totalWorkouts: records.length,
          totalDuration: records.reduce((sum: number, r: any) => sum + r.durationMinutes, 0),
          totalCalories: records.reduce((sum: number, r: any) => sum + r.caloriesBurned, 0)
        },
        traceId: 'mock-' + Date.now()
      }
    }
  },
  {
    url: '/api/records/stats/last-7days/:userId',
    method: 'get',
    response: () => {
      return { code: 200, message: 'success', data: generateLast7Days(), traceId: 'mock-' + Date.now() }
    }
  }
] as MockMethod[]
