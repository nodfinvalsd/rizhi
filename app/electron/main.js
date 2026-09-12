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

function startBackend() {
  const jar = path.resolve(__dirname, '../../backend/target/kb-backend-1.0.0.jar')
  if (!fs.existsSync(jar)) {
    console.log('[kb] backend jar not found:', jar)
    return
  }
  // 本地密钥配置（不入版本库），例如 {"KB_DB_PASSWORD": "root"}
  const env = { ...process.env }
  const envFile = path.join(__dirname, 'backend-env.json')
  if (fs.existsSync(envFile)) {
    try {
      Object.assign(env, JSON.parse(fs.readFileSync(envFile, 'utf8')))
    } catch (e) {
      console.log('[kb] failed to parse backend-env.json:', e.message)
    }
  }
  backendProcess = spawn('java', ['-jar', jar], { stdio: 'ignore', env })
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
    { label: '打开主窗口', click: () => { mainWindow.show(); mainWindow.focus() } },
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
  app.on('second-instance', () => {
    if (mainWindow) {
      mainWindow.show()
      mainWindow.focus()
    }
  })

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

ipcMain.on('open-main', () => {
  if (mainWindow) {
    mainWindow.show()
    mainWindow.focus()
  }
})

ipcMain.on('hide-widget', () => {
  if (widgetWindow) widgetWindow.hide()
})
