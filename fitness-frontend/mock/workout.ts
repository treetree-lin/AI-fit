import type { MockMethod } from 'vite-plugin-mock'

const workouts = [
  {
    id: 1,
    title: '全身燃脂训练',
    description: '适合新手的全身燃脂课程，无需器械，在家即可完成。包含热身、主训练和拉伸放松三个环节。',
    difficulty: '初级',
    durationMinutes: 30,
    targetMuscle: '全身',
    equipmentNeeded: ['瑜伽垫'],
    caloriesBurned: 250,
    isActive: true,
    createdBy: 1,
    coverImageUrl: '',
    videoUrl: '',
    favoriteCount: 128,
    commentCount: 15,
    viewCount: 3420,
    createdAt: '2024-01-15T10:00:00',
    updatedAt: '2024-03-10T14:30:00',
    steps: [
      { id: 1, stepOrder: 1, exerciseName: '开合跳', sets: 3, reps: '30秒', restSeconds: 30, tips: '保持核心收紧，膝盖微屈', videoUrl: '' },
      { id: 2, stepOrder: 2, exerciseName: '高抬腿', sets: 3, reps: '30秒', restSeconds: 30, tips: '大腿抬至与地面平行', videoUrl: '' },
      { id: 3, stepOrder: 3, exerciseName: '登山跑', sets: 3, reps: '20秒', restSeconds: 40, tips: '臀部不要抬太高', videoUrl: '' },
      { id: 4, stepOrder: 4, exerciseName: '平板支撑', sets: 3, reps: '30秒', restSeconds: 30, tips: '身体呈一条直线', videoUrl: '' }
    ]
  },
  {
    id: 2,
    title: '胸肌强化训练',
    description: '针对胸大肌的专项训练，包含俯卧撑和哑铃动作，适合有一定基础的人群。',
    difficulty: '中级',
    durationMinutes: 45,
    targetMuscle: '胸',
    equipmentNeeded: ['哑铃', '卧推凳'],
    caloriesBurned: 320,
    isActive: true,
    createdBy: 1,
    coverImageUrl: '',
    videoUrl: '',
    favoriteCount: 256,
    commentCount: 32,
    viewCount: 5600,
    createdAt: '2024-01-20T10:00:00',
    updatedAt: '2024-03-12T14:30:00',
    steps: [
      { id: 5, stepOrder: 1, exerciseName: '俯卧撑', sets: 4, reps: '12-15次', restSeconds: 60, tips: '胸部贴近地面', videoUrl: '' },
      { id: 6, stepOrder: 2, exerciseName: '哑铃卧推', sets: 4, reps: '10-12次', restSeconds: 90, tips: '控制下放速度', videoUrl: '' },
      { id: 7, stepOrder: 3, exerciseName: '哑铃飞鸟', sets: 3, reps: '12次', restSeconds: 60, tips: '肘部微屈，感受胸部拉伸', videoUrl: '' },
      { id: 8, stepOrder: 4, exerciseName: '窄距俯卧撑', sets: 3, reps: '10次', restSeconds: 60, tips: '双手间距与肩同宽', videoUrl: '' }
    ]
  },
  {
    id: 3,
    title: '背部力量训练',
    description: '打造倒三角身材，强化背阔肌和斜方肌。',
    difficulty: '中级',
    durationMinutes: 50,
    targetMuscle: '背',
    equipmentNeeded: ['引体向上杆', '哑铃'],
    caloriesBurned: 350,
    isActive: true,
    createdBy: 1,
    coverImageUrl: '',
    videoUrl: '',
    favoriteCount: 189,
    commentCount: 24,
    viewCount: 4100,
    createdAt: '2024-02-01T10:00:00',
    updatedAt: '2024-03-15T14:30:00',
    steps: [
      { id: 9, stepOrder: 1, exerciseName: '引体向上', sets: 4, reps: '6-10次', restSeconds: 120, tips: '下巴过杆', videoUrl: '' },
      { id: 10, stepOrder: 2, exerciseName: '哑铃划船', sets: 4, reps: '10-12次', restSeconds: 90, tips: '背部发力，不要耸肩', videoUrl: '' },
      { id: 11, stepOrder: 3, exerciseName: '超人式', sets: 3, reps: '15次', restSeconds: 60, tips: '同时抬起双手双腿', videoUrl: '' }
    ]
  },
  {
    id: 4,
    title: '腿部轰炸训练',
    description: '高强度腿部训练，强化股四头肌和臀大肌。',
    difficulty: '高级',
    durationMinutes: 60,
    targetMuscle: '腿',
    equipmentNeeded: ['杠铃', '深蹲架'],
    caloriesBurned: 450,
    isActive: true,
    createdBy: 1,
    coverImageUrl: '',
    videoUrl: '',
    favoriteCount: 312,
    commentCount: 45,
    viewCount: 7800,
    createdAt: '2024-02-10T10:00:00',
    updatedAt: '2024-03-18T14:30:00',
    steps: [
      { id: 12, stepOrder: 1, exerciseName: '深蹲', sets: 5, reps: '8-10次', restSeconds: 120, tips: '膝盖朝向脚尖方向', videoUrl: '' },
      { id: 13, stepOrder: 2, exerciseName: '箭步蹲', sets: 4, reps: '12次/腿', restSeconds: 90, tips: '前膝不要超过脚尖', videoUrl: '' },
      { id: 14, stepOrder: 3, exerciseName: '腿举', sets: 4, reps: '12次', restSeconds: 90, tips: '控制下落速度', videoUrl: '' },
      { id: 15, stepOrder: 4, exerciseName: '提踵', sets: 4, reps: '15次', restSeconds: 60, tips: '顶峰收缩1秒', videoUrl: '' }
    ]
  },
  {
    id: 5,
    title: '肩部塑形训练',
    description: '打造饱满三角肌，改善肩部线条。',
    difficulty: '中级',
    durationMinutes: 40,
    targetMuscle: '肩',
    equipmentNeeded: ['哑铃'],
    caloriesBurned: 280,
    isActive: true,
    createdBy: 1,
    coverImageUrl: '',
    videoUrl: '',
    favoriteCount: 145,
    commentCount: 18,
    viewCount: 3200,
    createdAt: '2024-02-15T10:00:00',
    updatedAt: '2024-03-20T14:30:00',
    steps: [
      { id: 16, stepOrder: 1, exerciseName: '哑铃推举', sets: 4, reps: '10次', restSeconds: 90, tips: '核心收紧', videoUrl: '' },
      { id: 17, stepOrder: 2, exerciseName: '侧平举', sets: 4, reps: '12次', restSeconds: 60, tips: '肘部微屈', videoUrl: '' },
      { id: 18, stepOrder: 3, exerciseName: '前平举', sets: 3, reps: '12次', restSeconds: 60, tips: '控制动作节奏', videoUrl: '' },
      { id: 19, stepOrder: 4, exerciseName: '俯身飞鸟', sets: 3, reps: '15次', restSeconds: 60, tips: '感受后束发力', videoUrl: '' }
    ]
  },
  {
    id: 6,
    title: '手臂增粗计划',
    description: '针对肱二头肌和肱三头肌的专项训练。',
    difficulty: '初级',
    durationMinutes: 35,
    targetMuscle: '手臂',
    equipmentNeeded: ['哑铃', '杠铃'],
    caloriesBurned: 220,
    isActive: true,
    createdBy: 1,
    coverImageUrl: '',
    videoUrl: '',
    favoriteCount: 98,
    commentCount: 12,
    viewCount: 2800,
    createdAt: '2024-02-20T10:00:00',
    updatedAt: '2024-03-22T14:30:00',
    steps: [
      { id: 20, stepOrder: 1, exerciseName: '杠铃弯举', sets: 4, reps: '10次', restSeconds: 60, tips: '大臂贴紧身体', videoUrl: '' },
      { id: 21, stepOrder: 2, exerciseName: '锤式弯举', sets: 3, reps: '12次', restSeconds: 60, tips: '掌心相对', videoUrl: '' },
      { id: 22, stepOrder: 3, exerciseName: '仰卧臂屈伸', sets: 4, reps: '10次', restSeconds: 60, tips: '控制杠铃下落', videoUrl: '' },
      { id: 23, stepOrder: 4, exerciseName: '绳索下压', sets: 3, reps: '15次', restSeconds: 60, tips: '手臂完全伸直', videoUrl: '' }
    ]
  },
  {
    id: 7,
    title: 'HIIT 高强度间歇',
    description: '20分钟高效燃脂，适合时间紧张的人群。',
    difficulty: '高级',
    durationMinutes: 20,
    targetMuscle: '全身',
    equipmentNeeded: [],
    caloriesBurned: 300,
    isActive: true,
    createdBy: 1,
    coverImageUrl: '',
    videoUrl: '',
    favoriteCount: 420,
    commentCount: 56,
    viewCount: 9200,
    createdAt: '2024-03-01T10:00:00',
    updatedAt: '2024-03-25T14:30:00',
    steps: [
      { id: 24, stepOrder: 1, exerciseName: '波比跳', sets: 4, reps: '30秒', restSeconds: 30, tips: '保持节奏', videoUrl: '' },
      { id: 25, stepOrder: 2, exerciseName: '深蹲跳', sets: 4, reps: '30秒', restSeconds: 30, tips: '落地缓冲', videoUrl: '' },
      { id: 26, stepOrder: 3, exerciseName: '俯卧撑跳', sets: 4, reps: '20秒', restSeconds: 40, tips: '核心收紧', videoUrl: '' }
    ]
  },
  {
    id: 8,
    title: '核心力量强化',
    description: '打造钢铁核心，改善腰腹力量。',
    difficulty: '初级',
    durationMinutes: 25,
    targetMuscle: '全身',
    equipmentNeeded: ['瑜伽垫'],
    caloriesBurned: 180,
    isActive: true,
    createdBy: 1,
    coverImageUrl: '',
    videoUrl: '',
    favoriteCount: 76,
    commentCount: 8,
    viewCount: 2100,
    createdAt: '2024-03-05T10:00:00',
    updatedAt: '2024-03-28T14:30:00',
    steps: [
      { id: 27, stepOrder: 1, exerciseName: '卷腹', sets: 4, reps: '20次', restSeconds: 30, tips: '下背部贴地', videoUrl: '' },
      { id: 28, stepOrder: 2, exerciseName: '俄罗斯转体', sets: 3, reps: '30次', restSeconds: 30, tips: '双脚离地增加难度', videoUrl: '' },
      { id: 29, stepOrder: 3, exerciseName: '死虫式', sets: 3, reps: '10次/侧', restSeconds: 30, tips: '腰部始终贴地', videoUrl: '' },
      { id: 30, stepOrder: 4, exerciseName: '鸟狗式', sets: 3, reps: '10次/侧', restSeconds: 30, tips: '保持身体平衡', videoUrl: '' }
    ]
  }
]

let favorites = [1, 2, 4] // 用户收藏的 workoutId 列表

const commentsMap: Record<number, any[]> = {
  1: [
    { id: 1, userId: 2, username: '健身小白', workoutId: 1, parentId: null, content: '非常适合新手，跟练一周已经瘦了两斤！', likeCount: 23, replyCount: 2, createdAt: '2024-03-01T10:00:00', updatedAt: '2024-03-01T10:00:00', isLiked: false, replies: [] },
    { id: 2, userId: 3, username: '运动达人', workoutId: 1, parentId: null, content: '建议增加一些拉伸动作', likeCount: 8, replyCount: 1, createdAt: '2024-03-02T14:00:00', updatedAt: '2024-03-02T14:00:00', isLiked: true, replies: [] }
  ],
  2: [
    { id: 3, userId: 4, username: '举铁狂魔', workoutId: 2, parentId: null, content: '胸肌泵感很强，推荐！', likeCount: 45, replyCount: 3, createdAt: '2024-03-05T09:00:00', updatedAt: '2024-03-05T09:00:00', isLiked: false, replies: [] }
  ],
  4: [
    { id: 4, userId: 5, username: '腿王', workoutId: 4, parentId: null, content: '练完腿酸了三天，效果杠杠的', likeCount: 67, replyCount: 5, createdAt: '2024-03-10T16:00:00', updatedAt: '2024-03-10T16:00:00', isLiked: false, replies: [] }
  ]
}

export default [
  {
    url: '/api/workouts',
    method: 'get',
    response: () => {
      return { code: 200, message: 'success', data: workouts, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/:id',
    method: 'get',
    response: ({ query }: any) => {
      const id = Number(query.id)
      const w = workouts.find(item => item.id === id) || workouts[0]
      return { code: 200, message: 'success', data: w, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/difficulty/:difficulty',
    method: 'get',
    response: ({ query }: any) => {
      const d = query.difficulty
      const list = workouts.filter(w => w.difficulty === d)
      return { code: 200, message: 'success', data: list, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/target/:targetMuscle',
    method: 'get',
    response: ({ query }: any) => {
      const m = query.targetMuscle
      const list = workouts.filter(w => w.targetMuscle.includes(m))
      return { code: 200, message: 'success', data: list, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts',
    method: 'post',
    response: () => {
      return { code: 200, message: '创建成功', data: workouts[0], traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/:id',
    method: 'put',
    response: () => {
      return { code: 200, message: '更新成功', data: workouts[0], traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/:id',
    method: 'delete',
    response: () => {
      return { code: 200, message: '删除成功', data: 'success', traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/:id/toggle-status',
    method: 'patch',
    response: () => {
      return { code: 200, message: 'success', data: workouts[0], traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/cover/upload',
    method: 'post',
    response: () => {
      return { code: 200, message: '上传成功', data: { url: 'https://picsum.photos/400/300?random=' + Date.now() }, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/video/upload',
    method: 'post',
    response: () => {
      return { code: 200, message: '上传成功', data: { url: 'https://www.w3schools.com/html/mov_bbb.mp4' }, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/comments/:workoutId',
    method: 'get',
    response: ({ query }: any) => {
      const id = Number(query.workoutId)
      return { code: 200, message: 'success', data: commentsMap[id] || [], traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/comments',
    method: 'post',
    response: ({ body }: any) => {
      const newComment = {
        id: Date.now(),
        userId: 1,
        username: 'fitness_user',
        workoutId: body.workoutId || 1,
        parentId: body.parentId || null,
        content: body.content,
        likeCount: 0,
        replyCount: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        isLiked: false,
        replies: []
      }
      const list = commentsMap[newComment.workoutId] || []
      list.unshift(newComment)
      commentsMap[newComment.workoutId] = list
      return { code: 200, message: '评论成功', data: newComment, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/comments/:commentId',
    method: 'delete',
    response: () => {
      return { code: 200, message: '删除成功', data: 'success', traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/comments/:commentId/like',
    method: 'post',
    response: ({ query }: any) => {
      const cid = Number(query.commentId)
      for (const key in commentsMap) {
        const c = commentsMap[key].find((x: any) => x.id === cid)
        if (c) {
          c.likeCount += 1
          c.isLiked = true
          return { code: 200, message: 'success', data: c, traceId: 'mock-' + Date.now() }
        }
      }
      return { code: 200, message: 'success', data: {}, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/favorites/:workoutId',
    method: 'post',
    response: ({ query }: any) => {
      const wid = Number(query.workoutId)
      if (!favorites.includes(wid)) favorites.push(wid)
      return { code: 200, message: '收藏成功', data: { success: true }, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/favorites/:workoutId',
    method: 'delete',
    response: ({ query }: any) => {
      const wid = Number(query.workoutId)
      favorites = favorites.filter(id => id !== wid)
      return { code: 200, message: '取消收藏', data: 'success', traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/favorites/:workoutId/check',
    method: 'get',
    response: ({ query }: any) => {
      const wid = Number(query.workoutId)
      return { code: 200, message: 'success', data: { favorited: favorites.includes(wid) }, traceId: 'mock-' + Date.now() }
    }
  },
  {
    url: '/api/workouts/favorites/user/:userId',
    method: 'get',
    response: () => {
      const favList = favorites.map(wid => ({
        id: wid,
        userId: 1,
        workoutId: wid,
        createdAt: '2024-03-15T10:00:00'
      }))
      return { code: 200, message: 'success', data: favList, traceId: 'mock-' + Date.now() }
    }
  }
] as MockMethod[]
