const { contextBridge, ipcRenderer } = require('electron');
contextBridge.exposeInMainWorld('ya', {
  getState: () => ipcRenderer.invoke('state'),
  saveSettings: (s) => ipcRenderer.invoke('settings', s),
  loginMicrosoft: () => ipcRenderer.invoke('login-ms'),
  loginOffline: (name) => ipcRenderer.invoke('login-offline', name),
  logout: () => ipcRenderer.invoke('logout'),
  play: () => ipcRenderer.invoke('play'),
  win: (a) => ipcRenderer.send('win', a),
  on: (ch, fn) => ipcRenderer.on(ch, (_e, d) => fn(d))
});
