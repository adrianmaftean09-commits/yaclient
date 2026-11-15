const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');
const https = require('https');
const { Client, Authenticator } = require('minecraft-launcher-core');
const cfg = require('./config.json');

const dataDir = path.join(app.getPath('appData'), '.yaclient');
const gameDir = path.join(dataDir, 'game');
const weaveModsDir = path.join(os.homedir(), '.weave', 'mods');
const stateFile = path.join(dataDir, 'state.json');
fs.mkdirSync(gameDir, { recursive: true });
fs.mkdirSync(weaveModsDir, { recursive: true });

let state = { account: null, ram: 2048, javaPath: 'java' };
try { state = { ...state, ...JSON.parse(fs.readFileSync(stateFile, 'utf8')) }; } catch (_) {}
const save = () => fs.writeFileSync(stateFile, JSON.stringify(state, null, 2));

let win;
function createWindow() {
  win = new BrowserWindow({
    width: 980, height: 600, frame: false, resizable: false, backgroundColor: '#000000',
    webPreferences: { preload: path.join(__dirname, 'preload.js'), contextIsolation: true }
  });
  win.removeMenu();
  win.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}
app.whenReady().then(createWindow);
app.on('window-all-closed', () => app.quit());

ipcMain.on('win', (_e, a) => { if (a === 'min') win.minimize(); if (a === 'close') win.close(); });
const send = (ch, d) => win && win.webContents.send(ch, d);

ipcMain.handle('state', () => ({ ...state, version: cfg.minecraftVersion }));
ipcMain.handle('settings', (_e, s) => { state = { ...state, ...s }; save(); return state; });
ipcMain.handle('logout', () => { state.account = null; save(); return true; });

ipcMain.handle('login-offline', (_e, name) => {
  const n = String(name || '').replace(/[^A-Za-z0-9_]/g, '').slice(0, 16);
  if (n.length < 3) return { error: 'Il nome deve avere almeno 3 caratteri (lettere, numeri, _).' };
  state.account = { type: 'offline', name: n, auth: Authenticator.getAuth(n) };
  save();
  return { name: n, type: 'offline' };
});

ipcMain.handle('login-ms', async () => {
  try {
    const { Auth } = require('msmc');
    const auth = new Auth('select_account');
    const xbox = await auth.launch('electron');
    const mc = await xbox.getMinecraft();
    state.account = { type: 'microsoft', name: mc.profile.name, auth: mc.mclc() };
    save();
    return { name: mc.profile.name, type: 'microsoft' };
  } catch (e) {
    return { error: 'Accesso Microsoft non riuscito. Riprova.' };
  }
});

function getJson(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'YA-CLIENT-Launcher' } }, (r) => {
      if (r.statusCode >= 300 && r.statusCode < 400 && r.headers.location) return resolve(getJson(r.headers.location));
      let b = ''; r.on('data', (c) => (b += c)); r.on('end', () => {
        try { resolve(JSON.parse(b)); } catch (e) { reject(e); }
      });
    }).on('error', reject);
  });
}
function download(url, dest) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'YA-CLIENT-Launcher' } }, (r) => {
      if (r.statusCode >= 300 && r.statusCode < 400 && r.headers.location) return resolve(download(r.headers.location, dest));
      if (r.statusCode !== 200) return reject(new Error('HTTP ' + r.statusCode));
      const f = fs.createWriteStream(dest);
      r.pipe(f); f.on('finish', () => f.close(resolve)); f.on('error', reject);
    }).on('error', reject);
  });
}

async function fetchRelease(repo, matcher, dest) {
  const rel = await getJson(`https://api.github.com/repos/${repo}/releases/latest`);
  const asset = (rel.assets || []).find(matcher);
  if (!asset) throw new Error(`Nessun file trovato nella release di ${repo}`);
  await download(asset.browser_download_url, dest);
  return rel.tag_name;
}

ipcMain.handle('play', async () => {
  if (!state.account) return { error: 'Accedi prima di giocare.' };
  try {
    send('status', { text: 'Aggiorno YA CLIENT da GitHub…', pct: 2 });
    const clientJar = path.join(weaveModsDir, 'YAClient.jar');
    await fetchRelease(cfg.repo, (a) => a.name === cfg.jarAsset, clientJar);

    send('status', { text: 'Controllo il loader…', pct: 6 });
    const loaderJar = path.join(dataDir, 'weave-loader.jar');
    if (!fs.existsSync(loaderJar)) {
      await fetchRelease(cfg.weaveRepo,
        (a) => /\.jar$/.test(a.name) && !/sources|javadoc/i.test(a.name), loaderJar);
    }

    const launcher = new Client();
    launcher.on('progress', (p) => send('status', { text: `Scarico ${p.type}…`, pct: 6 + Math.round((p.task / p.total) * 70) }));
    launcher.on('debug', (m) => send('log', String(m)));
    launcher.on('data', (m) => send('log', String(m)));
    launcher.on('close', () => send('status', { text: 'Gioco chiuso.', pct: 0, done: true }));

    const opts = {
      authorization: state.account.auth,
      root: gameDir,
      javaPath: state.javaPath || 'java',
      version: { number: cfg.minecraftVersion, type: 'release' },
      memory: { max: `${state.ram}M`, min: '1024M' },
      customArgs: [`-javaagent:${loaderJar}`]
    };
    send('status', { text: 'Avvio Minecraft 1.8.9…', pct: 80 });
    const proc = await launcher.launch(opts);
    if (!proc) return { error: 'Avvio non riuscito. Controlla che Java 8 sia installato.' };
    send('status', { text: 'In gioco', pct: 100, playing: true });
    return { ok: true };
  } catch (e) {
    return { error: 'Errore: ' + e.message };
  }
});
