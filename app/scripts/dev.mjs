// 一键开发启动：vite dev server + electron 窗口
import { createServer } from 'vite'
import { spawn } from 'child_process'
import electronPath from 'electron'

const server = await createServer()
await server.listen()

const addr = server.httpServer.address()
const url = `http://localhost:${addr.port}`
console.log('[kb] vite dev server:', url)

const child = spawn(electronPath, ['.'], {
  stdio: 'inherit',
  env: { ...process.env, VITE_DEV_SERVER_URL: url },
})

child.on('exit', () => {
  server.close()
  process.exit(0)
})
