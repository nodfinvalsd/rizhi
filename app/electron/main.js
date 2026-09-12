const { app, BrowserWindow, Tray, Menu, globalShortcut, ipcMain, screen, nativeImage } = require('electron')
const { spawn } = require('child_process')
const path = require('path')
const fs = require('fs')

const DEV_URL = process.env.VITE_DEV_SERVER_URL

let widgetWindow = null
let mainWindow = null
let tray = null
let backendProcess = null
let quitting = false

function widgetBounds() {
  const { workArea } = screen.getPrimaryDisplay()
  const w = 380
  const h = 560
  return {
    x: workArea.x + workArea.width - w - 16,
    y: workArea.y + workArea.height - h - 16,
    width: w,
    height: h,
  }
}

function loadUrl(win, route) {
  if (DEV_URL) {
    win.loadURL(DEV_URL + '#/' + route)
  } else {
    win.loadFile(path.join(__dirname, '../dist/index.html'), { hash: route })
  }
}

function createWidgetWindow() {
  widgetWindow = new BrowserWindow({
    ...widgetBounds(),
    frame: false,
    alwaysOnTop: true,
    skipTaskbar: true,
    resizable: false,
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  })
  loadUrl(widgetWindow, 'widget')
  widgetWindow.on('close', (e) => {
    if (!quitting) {
      e.preventDefault()
      widgetWindow.hide()
    }
  })
}

function createMainWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  })
  loadUrl(mainWindow, '')
  mainWindow.on('close', (e) => {
    if (!quitting) {
      e.preventDefault()
      mainWindow.hide()
    }
  })
}

/** 把主窗口拉到前台：restore 处理最小化，moveTop 绕过 Windows 前台锁 */
function focusMainWindow() {
  if (!mainWindow) return
  if (mainWindow.isMinimized()) mainWindow.restore()
  if (!mainWindow.isVisible()) mainWindow.show()
  mainWindow.focus()
  mainWindow.moveTop()
}

function toggleWidget() {
  if (!widgetWindow) return
  if (widgetWindow.isVisible()) {
    widgetWindow.hide()
  } else {
    widgetWindow.setBounds(widgetBounds())
    widgetWindow.show()
    widgetWindow.focus()
  }
}

function appDir() {
  // portable 模式下 execPath 指向临时解压副本，真实 exe 目录在环境变量里
  if (app.isPackaged) {
    if (process.env.PORTABLE_EXECUTABLE_DIR) return process.env.PORTABLE_EXECUTABLE_DIR
    return path.dirname(process.env.PORTABLE_EXECUTABLE_FILE || process.execPath)
  }
  return path.resolve(__dirname, '../..')
}

function dataDir() {
  // upload/ 与 backup/ 的落点，也就是后端的 cwd
  // 便携版：跟 exe 同目录；安装版：放用户数据目录，避免重装/升级时覆盖安装目录导致附件与备份丢失
  const dir = app.isPackaged
    ? process.env.PORTABLE_EXECUTABLE_DIR || path.join(app.getPath('userData'), 'data')
    : path.resolve(__dirname, '../..')
  try {
    fs.mkdirSync(dir, { recursive: true })
  } catch (e) {
    console.log('[kb] failed to create data dir:', e.message)
  }
  return dir
}

function startBackend() {
  // 打包后 jar 在 resources 里；开发时在 backend/target
  const jar = app.isPackaged
    ? path.join(process.resourcesPath, 'kb-backend-1.0.0.jar')
    : path.resolve(__dirname, '../../backend/target/kb-backend-1.0.0.jar')
  if (!fs.existsSync(jar)) {
    console.log('[kb] backend jar not found:', jar)
    return
  }
  // 本地密钥配置（不入版本库），例如 {"KB_DB_PASSWORD": "root"}
  // 打包后放在 exe 同目录；开发时在 electron 目录
  const env = { ...process.env }
  const envFile = app.isPackaged
    ? path.join(appDir(), 'backend-env.json')
    : path.join(__dirname, 'backend-env.json')
  if (fs.existsSync(envFile)) {
    try {
      Object.assign(env, JSON.parse(fs.readFileSync(envFile, 'utf8')))
    } catch (e) {
      console.log('[kb] failed to parse backend-env.json:', e.message)
    }
  }
  // 工作目录决定 upload/ 与 backup/ 的位置
  backendProcess = spawn('java', ['-jar', jar], { stdio: 'ignore', env, cwd: dataDir() })
  backendProcess.on('exit', (code) => {
    backendProcess = null
    if (!quitting) console.log('[kb] backend exited with code', code)
  })
}

function stopBackend() {
  if (!backendProcess) return
  if (process.platform === 'win32') {
    spawn('taskkill', ['/pid', String(backendProcess.pid), '/f', '/t'])
  } else {
    backendProcess.kill()
  }
  backendProcess = null
}

function createTray() {
  const icon = nativeImage.createFromPath(path.join(__dirname, 'icon.png'))
  tray = new Tray(icon)
  tray.setToolTip('个人知识库')
  const menu = Menu.buildFromTemplate([
    { label: '显示悬浮窗', click: () => { widgetWindow.show(); widgetWindow.focus() } },
    { label: '打开主窗口', click: focusMainWindow },
    { type: 'separator' },
    { label: '退出', click: () => { quitting = true; app.quit() } },
  ])
  tray.setContextMenu(menu)
  tray.on('click', toggleWidget)
}

const gotLock = app.requestSingleInstanceLock()
if (!gotLock) {
  app.quit()
} else {
  app.on('second-instance', focusMainWindow)

  app.whenReady().then(() => {
    startBackend()
    createWidgetWindow()
    createMainWindow()
    createTray()
    globalShortcut.register('CommandOrControl+Shift+Space', toggleWidget)
    setTimeout(() => {
      if (!quitting) {
        mainWindow.show()
      }
    }, 2000)
  })

  app.on('before-quit', () => {
    quitting = true
  })

  app.on('will-quit', () => {
    globalShortcut.unregisterAll()
    stopBackend()
  })
}

ipcMain.on('open-main', focusMainWindow)

ipcMain.on('hide-widget', () => {
  if (widgetWindow) widgetWindow.hide()
})
