import type { MockMethod } from 'vite-plugin-mock'
import userMocks from './user'
import workoutMocks from './workout'
import recordMocks from './record'
import knowledgeMocks from './knowledge'
import chatMocks from './chat'
import commonMocks from './common'

export default [
  ...userMocks,
  ...workoutMocks,
  ...recordMocks,
  ...knowledgeMocks,
  ...chatMocks,
  ...commonMocks
] as MockMethod[]
