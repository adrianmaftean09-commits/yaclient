const { app, BrowserWindow, ipcMain, shell } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');
const https = require('https');
const { Client, Authenticator } = require('minecraft-launcher-core');
const AdmZip = require('adm-zip');
const cfg = require('./config.json');

const dataDir = path.join(app.getPath('appData'), '.yaclient');
const gameDir = path.join(dataDir, 'game');
const weaveModsDir = path.join(os.homedir(), '.weave', 'mods');
const stateFile = path.join(dataDir, 'state.json');
fs.mkdirSync(gameDir, { recursive: true });
fs.mkdirSync(weaveModsDir, { recursive: true });

let state = { account: null, ram: 2048, javaPath: 'java', minimizeOnLaunch: false };
try { state = { ...state, ...JSON.parse(fs.readFileSync(stateFile, 'utf8')) }; } catch (_) {}
const save = () => fs.writeFileSync(stateFile, JSON.stringify(state, null, 2));

let win;
function createWindow() {
  win = new BrowserWindow({
    width: 1000, height: 620, frame: false, resizable: false, backgroundColor: '#09090b',
    title: 'YA CLIENT',
    webPreferences: { preload: path.join(__dirname, 'preload.js'), contextIsolation: true }
  });
  win.removeMenu();
  win.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}
app.whenReady().then(createWindow);
app.on('window-all-closed', () => app.quit());

ipcMain.on('win', (_e, a) => { if (a === 'min') win.minimize(); if (a === 'close') win.close(); });
const send = (ch, d) => win && !win.isDestroyed() && win.webContents.send(ch, d);
// non mostrare mai il token di accesso nella console del launcher
const clean = (s) => String(s).replace(/token:[^\s)]+/g, 'token:***');

// ---------- Java 8 ----------
function findJava8() {
  const roots = [
    process.env['ProgramFiles'], process.env['ProgramFiles(x86)'],
    process.env['LOCALAPPDATA'] && path.join(process.env['LOCALAPPDATA'], 'Programs')
  ].filter(Boolean);
  const vendors = ['Eclipse Adoptium', 'Java', 'Zulu', 'Amazon Corretto', 'Microsoft', 'BellSoft', 'AdoptOpenJDK', 'Eclipse Foundation'];
  const re = /(jdk|jre)[-_ ]?(1\.)?8([._-]|$)|zulu-?8|jdk8|jre8|1\.8\.0/i;
  for (const r of roots) {
    for (const v of vendors) {
      const dir = path.join(r, v);
      let names;
      try { names = fs.readdirSync(dir); } catch (_) { continue; }
      for (const n of names) {
        if (!re.test(n)) continue;
        const exe = path.join(dir, n, 'bin', 'java.exe');
        if (fs.existsSync(exe)) return exe;
      }
    }
  }
  return null;
}
function resolveJava() {
  if (state.javaPath && state.javaPath !== 'java' && fs.existsSync(state.javaPath)) return state.javaPath;
  return findJava8() || 'java';
}

ipcMain.handle('state', () => ({ ...state, version: cfg.minecraftVersion, gameDir }));
ipcMain.handle('settings', (_e, s) => { state = { ...state, ...s }; save(); return state; });
ipcMain.handle('logout', () => { state.account = null; save(); return true; });
ipcMain.handle('detect-java', () => findJava8());
ipcMain.handle('open-folder', () => shell.openPath(gameDir));

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

// Sostituisce il logo Mojang della schermata di caricamento con quello di YA CLIENT.
// (Il gioco usa il pixel in alto a sinistra come colore di sfondo a tutto schermo.)
function patchSplash() {
  const jar = path.join(gameDir, 'versions', cfg.minecraftVersion, cfg.minecraftVersion + '.jar');
  if (!fs.existsSync(jar)) return;
  const png = fs.readFileSync(path.join(__dirname, 'assets', 'splash.png'));
  const name = 'assets/minecraft/textures/gui/title/mojang.png';
  const zip = new AdmZip(jar);
  const entry = zip.getEntry(name);
  const sigs = zip.getEntries().filter((e) => /^META-INF\/[^/]+\.(SF|DSA|RSA)$/i.test(e.entryName));
  if (entry && sigs.length === 0 && Buffer.compare(entry.getData(), png) === 0) return;
  // il jar originale e' firmato: senza rimuovere la firma, Java rifiuta il file modificato
  sigs.forEach((e) => zip.deleteFile(e.entryName));
  if (entry) zip.deleteFile(name);
  zip.addFile(name, png);
  zip.writeZip(jar);
}

ipcMain.handle('play', async () => {
  if (!state.account) return { error: 'Accedi prima di giocare.' };
  try {
    send('status', { text: 'Aggiorno YA CLIENT da GitHub…', pct: 2 });
    const clientJar = path.join(weaveModsDir, 'YAClient.jar');
    const tag = await fetchRelease(cfg.repo, (a) => a.name === cfg.jarAsset, clientJar);
    send('log', `YA CLIENT ${tag} aggiornato.`);

    send('status', { text: 'Controllo il loader…', pct: 6 });
    const loaderJar = path.join(dataDir, 'weave-loader.jar');
    if (!fs.existsSync(loaderJar)) {
      await fetchRelease(cfg.weaveRepo,
        (a) => /\.jar$/.test(a.name) && !/sources|javadoc/i.test(a.name), loaderJar);
    }

    const javaPath = resolveJava();
    send('log', `Java: ${javaPath}`);

    const launcher = new Client();
    const startedAt = Date.now();
    launcher.on('progress', (p) => send('status', { text: `Scarico ${p.type}…`, pct: 6 + Math.round((p.task / p.total) * 70) }));
    launcher.on('debug', (m) => send('log', clean(m)));
    launcher.on('data', (m) => send('log', clean(m)));
    launcher.on('arguments', () => {
      try { patchSplash(); } catch (e) { send('log', 'splash: ' + e.message); }
    });
    launcher.on('close', (code) => {
      if (win && !win.isDestroyed() && win.isMinimized()) win.restore();
      const crashed = code !== 0 && Date.now() - startedAt < 60000;
      send('status', {
        text: crashed ? `Il gioco si è chiuso (codice ${code}). Apri la Console per i dettagli.` : 'Gioco chiuso.',
        pct: 0, done: true, crashed
      });
    });

    const opts = {
      authorization: state.account.auth,
      root: gameDir,
      javaPath,
      version: { number: cfg.minecraftVersion, type: 'release' },
      memory: { max: `${state.ram}M`, min: '1024M' },
      customArgs: [`-javaagent:${loaderJar}`]
    };
    send('status', { text: 'Avvio Minecraft 1.8.9…', pct: 80 });
    const proc = await launcher.launch(opts);
    if (!proc) return { error: 'Avvio non riuscito. Controlla che Java 8 sia installato.' };
    send('status', { text: 'In gioco', pct: 100, playing: true });
    if (state.minimizeOnLaunch && win) win.minimize();
    return { ok: true };
  } catch (e) {
    return { error: 'Errore: ' + e.message };
  }
});
