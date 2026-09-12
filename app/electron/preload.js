const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('kb', {
  openMain: () => ipcRenderer.send('open-main'),
  hideWidget: () => ipcRenderer.send('hide-widget'),
})
