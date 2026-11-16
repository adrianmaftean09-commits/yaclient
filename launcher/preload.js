const { contextBridge, ipcRenderer } = require('electron');
contextBridge.exposeInMainWorld('ya', {
  getState: () => ipcRenderer.invoke('state'),
  saveSettings: (s) => ipcRenderer.invoke('settings', s),
  setLang: (c) => ipcRenderer.invoke('set-lang', c),
  loginMicrosoft: () => ipcRenderer.invoke('login-ms'),
  loginOffline: (name) => ipcRenderer.invoke('login-offline', name),
  logout: () => ipcRenderer.invoke('logout'),
  detectJava: () => ipcRenderer.invoke('detect-java'),
  openFolder: () => ipcRenderer.invoke('open-folder'),
  openExternal: (u) => ipcRenderer.invoke('open-external', u),
  content: () => ipcRenderer.invoke('content'),
  versions: () => ipcRenderer.invoke('versions'),
  mods: () => ipcRenderer.invoke('mods'),
  catalog: () => ipcRenderer.invoke('catalog'),
  modsGet: () => ipcRenderer.invoke('mods-get'),
  modsSet: (id, on) => ipcRenderer.invoke('mods-set', id, on),
  play: (opts) => ipcRenderer.invoke('play', opts),
  win: (a) => ipcRenderer.send('win', a),
  on: (ch, fn) => ipcRenderer.on(ch, (_e, d) => fn(d))
});
