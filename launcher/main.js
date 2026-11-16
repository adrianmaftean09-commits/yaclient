const { app, BrowserWindow, ipcMain, shell } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');
const https = require('https');
const http = require('http');
const { Client, Authenticator } = require('minecraft-launcher-core');
const AdmZip = require('adm-zip');
const cfg = require('./config.json');

const dataDir = path.join(app.getPath('appData'), '.yaclient');
const gameDir = path.join(dataDir, 'game');
const weaveModsDir = path.join(os.homedir(), '.weave', 'mods');
const stateFile = path.join(dataDir, 'state.json');
const propsFile = path.join(os.homedir(), '.yaclient', 'config.properties'); // shared with the game client
fs.mkdirSync(gameDir, { recursive: true });
fs.mkdirSync(weaveModsDir, { recursive: true });

let state = { account: null, ram: 2048, javaPath: 'java', minimizeOnLaunch: false, lang: 'en', version: '' };
try { state = { ...state, ...JSON.parse(fs.readFileSync(stateFile, 'utf8')) }; } catch (_) {}
const save = () => fs.writeFileSync(stateFile, JSON.stringify(state, null, 2));

let win;
function createWindow() {
  win = new BrowserWindow({
    width: 1120, height: 700, minWidth: 980, minHeight: 620, frame: false, backgroundColor: '#09090b',
    title: 'YA CLIENT',
    icon: path.join(__dirname, 'assets', 'icon.png'),
    webPreferences: { preload: path.join(__dirname, 'preload.js'), contextIsolation: true }
  });
  win.removeMenu();
  win.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}
let splash = null;
function showSplash() {
  try {
    closeSplash();
    splash = new BrowserWindow({ width: 640, height: 400, frame: false, resizable: false, show: false, backgroundColor: '#07070a',
      title: 'YA CLIENT', icon: path.join(__dirname, 'assets', 'icon.png'), alwaysOnTop: false });
    splash.removeMenu();
    splash.loadFile(path.join(__dirname, 'renderer', 'splash.html'));
    splash.once('ready-to-show', () => { if (splash) splash.show(); });
    splash.on('closed', () => { splash = null; });
  } catch (_) { splash = null; }
}
function splashSet(text, pct) {
  if (!splash || splash.isDestroyed()) return;
  splash.webContents.executeJavaScript(`document.getElementById('t').textContent=${JSON.stringify(String(text))};document.getElementById('b').style.width=${Math.max(2, Math.min(100, pct | 0))}+'%'`).catch(() => {});
}
function closeSplash() { if (splash && !splash.isDestroyed()) splash.close(); splash = null; }
app.whenReady().then(createWindow);
app.on('window-all-closed', () => app.quit());

ipcMain.on('win', (_e, a) => {
  if (!win) return;
  if (a === 'min') win.minimize();
  if (a === 'max') (win.isMaximized() ? win.unmaximize() : win.maximize());
  if (a === 'close') win.close();
});
const send = (ch, d) => win && !win.isDestroyed() && win.webContents.send(ch, d);
// never show the access token in the launcher console
const clean = (s) => String(s).replace(/token:[^\s)]+/g, 'token:***');

// ---------- shared config file (~/.yaclient/config.properties) ----------
function readProps() {
  const out = {};
  try {
    for (const line of fs.readFileSync(propsFile, 'utf8').split(/\r?\n/)) {
      if (!line || line[0] === '#' || line[0] === '!') continue;
      const i = line.search(/[=:]/);
      if (i < 0) continue;
      out[line.slice(0, i).trim()] = line.slice(i + 1).trim().replace(/\\([:=#!])/g, '$1');
    }
  } catch (_) {}
  return out;
}
function writeProps(updates) {
  let lines = [];
  try { lines = fs.readFileSync(propsFile, 'utf8').split(/\r?\n/); } catch (_) {}
  const done = new Set();
  lines = lines.map((l) => {
    const i = l.search(/[=:]/);
    if (l && l[0] !== '#' && l[0] !== '!' && i > 0) {
      const k = l.slice(0, i).trim();
      if (Object.prototype.hasOwnProperty.call(updates, k)) { done.add(k); return k + '=' + updates[k]; }
    }
    return l;
  });
  for (const k of Object.keys(updates)) if (!done.has(k)) lines.push(k + '=' + updates[k]);
  fs.mkdirSync(path.dirname(propsFile), { recursive: true });
  fs.writeFileSync(propsFile, lines.filter((l, i, a) => l !== '' || i < a.length - 1).join('\n') + '\n');
}

// ---------- content (news, servers, ads, store) ----------
function getJson(url) {
  return new Promise((resolve, reject) => {
    const mod = url.startsWith('https') ? https : http;
    mod.get(url, { headers: { 'User-Agent': 'YA-CLIENT-Launcher' }, timeout: 8000 }, (r) => {
      if (r.statusCode >= 300 && r.statusCode < 400 && r.headers.location) return resolve(getJson(r.headers.location));
      if (r.statusCode !== 200) { r.resume(); return reject(new Error('HTTP ' + r.statusCode)); }
      let b = ''; r.on('data', (c) => (b += c)); r.on('end', () => {
        try { resolve(JSON.parse(b)); } catch (e) { reject(e); }
      });
    }).on('timeout', function () { this.destroy(new Error('timeout')); }).on('error', reject);
  });
}
let contentCache = null;
async function loadContent() {
  let c = {};
  try { c = JSON.parse(fs.readFileSync(path.join(__dirname, 'content.json'), 'utf8')); } catch (_) {}
  if (c.remote) { try { c = { ...c, ...(await getJson(c.remote)) }; } catch (_) {} }
  contentCache = c;
  return c;
}
ipcMain.handle('content', () => loadContent());
ipcMain.handle('catalog', async () => {
  const c = contentCache || (await loadContent());
  if (!c.apiBase) return { error: 'not_configured' };
  try { return { data: await getJson(c.apiBase.replace(/\/+$/, '') + '/v1/catalog'), store: c.storeUrl || '' }; }
  catch (_) { return { error: 'offline' }; }
});
ipcMain.handle('open-external', (_e, url) => {
  if (/^https?:\/\//i.test(String(url || ''))) shell.openExternal(url);
});

// ---------- versions and mods ----------
const FALLBACK_VERSIONS = ['1.21.4', '1.21.1', '1.20.6', '1.20.4', '1.20.1', '1.19.4', '1.19.2', '1.18.2', '1.17.1', '1.16.5', '1.15.2',
  '1.14.4', '1.13.2', '1.12.2', '1.11.2', '1.10.2', '1.9.4', '1.8.9', '1.8', '1.7.10'];
let versionsCache = null;
ipcMain.handle('versions', async () => {
  if (versionsCache) return versionsCache;
  try {
    const m = await getJson('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json');
    versionsCache = { online: true, latest: m.latest, versions: m.versions.map((v) => ({ id: v.id, type: v.type, time: v.releaseTime })) };
  } catch (_) {
    versionsCache = { online: false, latest: { release: FALLBACK_VERSIONS[0], snapshot: FALLBACK_VERSIONS[0] },
      versions: FALLBACK_VERSIONS.map((id) => ({ id, type: 'release', time: '' })) };
    setTimeout(() => { versionsCache = null; }, 30000);   // try again later
  }
  return versionsCache;
});
ipcMain.handle('mods', () => {
  try { return JSON.parse(fs.readFileSync(path.join(__dirname, 'mods.json'), 'utf8')); } catch (_) { return { mods: [] }; }
});
function versionType(id) {
  const v = versionsCache && versionsCache.versions.find((x) => x.id === id);
  return v ? v.type : 'release';
}
// Java major version a Minecraft version needs
function javaMajorFor(v) {
  const m = String(v).match(/^1\.(\d+)(?:\.(\d+))?/);
  if (!m) return /^\d\dw/.test(String(v)) ? 21 : 8;
  const minor = +m[1], patch = +(m[2] || 0);
  if (minor <= 16) return 8;
  if (minor <= 19 || (minor === 20 && patch < 5)) return 17;
  return 21;
}

// ---------- Java ----------
function findJava(major) {
  const roots = [
    process.env['ProgramFiles'], process.env['ProgramFiles(x86)'],
    process.env['LOCALAPPDATA'] && path.join(process.env['LOCALAPPDATA'], 'Programs')
  ].filter(Boolean);
  const vendors = ['Eclipse Adoptium', 'Java', 'Zulu', 'Amazon Corretto', 'Microsoft', 'BellSoft', 'AdoptOpenJDK', 'Eclipse Foundation', 'Semeru'];
  const re = major === 8
    ? /(jdk|jre)[-_ ]?(1\.)?8([._-]|$)|zulu-?8|jdk8|jre8|1\.8\.0/i
    : new RegExp('(jdk|jre|zulu|java)[-_ ]?' + major + '([._-]|$)|(^|[-_ ])' + major + '([._]|$)', 'i');
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
const findJava8 = () => findJava(8);
function resolveJava(major) {
  if (major === 8) {
    if (state.javaPath && state.javaPath !== 'java' && fs.existsSync(state.javaPath)) return state.javaPath;
    return findJava(8) || 'java';
  }
  return findJava(major);
}

// ---------- state / account ----------
ipcMain.handle('state', () => ({ ...state, version: state.version || cfg.minecraftVersion, ya: cfg.minecraftVersion, gameDir }));
ipcMain.handle('settings', (_e, s) => { state = { ...state, ...s }; save(); return state; });
ipcMain.handle('logout', () => { state.account = null; save(); return true; });
ipcMain.handle('detect-java', () => findJava8());
ipcMain.handle('open-folder', () => shell.openPath(gameDir));
ipcMain.handle('set-lang', (_e, code) => {
  state.lang = code; save();
  try { writeProps({ lang: code }); } catch (_) {}
  return true;
});

// module toggles (same file the game reads)
ipcMain.handle('mods-get', () => {
  const p = readProps(); const out = {};
  for (const k of Object.keys(p)) {
    const m = k.match(/^mod\.(.+)\.enabled$/);
    if (m) out[m[1]] = p[k] === 'true';
  }
  return out;
});
ipcMain.handle('mods-set', (_e, id, on) => {
  if (!/^[a-z_]+$/.test(String(id))) return false;
  writeProps({ ['mod.' + id + '.enabled']: on ? 'true' : 'false' });
  return true;
});

ipcMain.handle('login-offline', (_e, name) => {
  const n = String(name || '').replace(/[^A-Za-z0-9_]/g, '').slice(0, 16);
  if (n.length < 3) return { error: 'name_short' };
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
    return { error: 'ms_failed' };
  }
});

// ---------- downloads ----------
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
  if (!asset) throw new Error(`No file found in the release of ${repo}`);
  await download(asset.browser_download_url, dest);
  return rel.tag_name;
}

// Replaces the Mojang logo of the loading screen with the YA CLIENT one.
// (The game uses the top-left pixel of the image as the full-screen background color.)
function patchSplash() {
  const jar = path.join(gameDir, 'versions', cfg.minecraftVersion, cfg.minecraftVersion + '.jar');
  if (!fs.existsSync(jar)) return;
  const png = fs.readFileSync(path.join(__dirname, 'assets', 'splash.png'));
  const name = 'assets/minecraft/textures/gui/title/mojang.png';
  const zip = new AdmZip(jar);
  const entry = zip.getEntry(name);
  const sigs = zip.getEntries().filter((e) => /^META-INF\/[^/]+\.(SF|DSA|RSA)$/i.test(e.entryName));
  if (entry && sigs.length === 0 && Buffer.compare(entry.getData(), png) === 0) return;
  // the original jar is signed: without removing the signature Java rejects the modified file
  sigs.forEach((e) => zip.deleteFile(e.entryName));
  if (entry) zip.deleteFile(name);
  zip.addFile(name, png);
  zip.writeZip(jar);
}

// ---------- Fabric (Minecraft 1.16+) ----------
const vcmp = (a, b) => {
  const pa = String(a).split('.').map((n) => parseInt(n, 10) || 0), pb = String(b).split('.').map((n) => parseInt(n, 10) || 0);
  for (let i = 0; i < 3; i++) { const d = (pa[i] || 0) - (pb[i] || 0); if (d) return d; }
  return 0;
};
const fabricSupported = (v) => /^\d+\.\d+(\.\d+)?$/.test(v) && vcmp(v, '1.16') >= 0;

// Installs the Fabric profile for this Minecraft version and, when published, the YA CLIENT Fabric jar.
async function setupFabric(mc) {
  const loaders = await getJson(`https://meta.fabricmc.net/v2/versions/loader/${mc}`);
  const pick = (Array.isArray(loaders) ? loaders : []).find((l) => l.loader && l.loader.stable) || (Array.isArray(loaders) ? loaders[0] : null);
  if (!pick) throw new Error(`Fabric is not available for Minecraft ${mc}`);
  const profile = await getJson(`https://meta.fabricmc.net/v2/versions/loader/${mc}/${pick.loader.version}/profile/json`);
  const id = profile.id;
  const dir = path.join(gameDir, 'versions', id);
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, id + '.json'), JSON.stringify(profile));

  const inst = path.join(gameDir, 'instances', mc);
  const mods = path.join(inst, 'mods');
  fs.mkdirSync(mods, { recursive: true });
  let hasYa = false;
  try {
    const yaJar = path.join(mods, 'YAClient-fabric.jar');
    const tag = await fetchRelease(cfg.repo, (a) => a.name === `yaclient-fabric-${mc}.jar`, yaJar);
    send('log', `YA CLIENT (Fabric ${mc}) ${tag} updated.`);
    hasYa = true;
    // the mod needs Fabric API
    const api = await getJson(`https://api.modrinth.com/v2/project/fabric-api/version?game_versions=${encodeURIComponent(JSON.stringify([mc]))}&loaders=${encodeURIComponent(JSON.stringify(['fabric']))}`);
    const f = Array.isArray(api) && api[0] && api[0].files && (api[0].files.find((x) => x.primary) || api[0].files[0]);
    if (f) {
      fs.readdirSync(mods).filter((n) => /^fabric-api/i.test(n)).forEach((n) => fs.unlinkSync(path.join(mods, n)));
      await download(f.url, path.join(mods, f.filename));
    } else send('log', `Fabric API for ${mc} not found.`);
  } catch (e) {
    send('log', `YA CLIENT for ${mc} is not published yet (${e.message}). Starting Fabric without it.`);
    try { fs.unlinkSync(path.join(mods, 'YAClient-fabric.jar')); } catch (_) {}
  }
  return { id, dir: inst, hasYa };
}

// ---------- launch ----------
ipcMain.handle('play', async (_e, opts) => {
  if (!state.account) return { error: 'need_login' };
  try {
    const content = contentCache || (await loadContent());
    // tell the game where the store is and which language to use
    try {
      writeProps({ lang: state.lang || 'en', 'api.base': (content.apiBase || '').replace(/\/+$/, ''), 'store.url': content.storeUrl || '' });
    } catch (_) {}

    const version = state.version || cfg.minecraftVersion;
    const yaMode = version === cfg.minecraftVersion;   // YA CLIENT (Weave + mods) only exists for this version
    const loaderJar = path.join(dataDir, 'weave-loader.jar');
    let fabric = null;
    if (yaMode) {
      send('status', { k: 'updating', pct: 2 });
      const clientJar = path.join(weaveModsDir, 'YAClient.jar');
      const tag = await fetchRelease(cfg.repo, (a) => a.name === cfg.jarAsset, clientJar);
      send('log', `YA CLIENT ${tag} updated.`);

      send('status', { k: 'loader', pct: 6 });
      if (!fs.existsSync(loaderJar)) {
        await fetchRelease(cfg.weaveRepo,
          (a) => /\.jar$/.test(a.name) && !/sources|javadoc/i.test(a.name), loaderJar);
      }
    } else if (fabricSupported(version)) {
      send('status', { k: 'loader', pct: 6 });
      fabric = await setupFabric(version);
    } else {
      send('log', `Minecraft ${version}: vanilla launch (YA CLIENT mods are available on ${cfg.minecraftVersion}).`);
    }

    const major = yaMode ? 8 : javaMajorFor(version);
    const javaPath = resolveJava(major);
    if (!javaPath) return { error: 'java_missing', detail: String(major) };
    send('log', `Java ${major}: ${javaPath}`);

    const launcher = new Client();
    showSplash(); splashSet('Starting Minecraft', 8);
    const startedAt = Date.now();
    launcher.on('progress', (p) => { splashSet(p.type === 'assets' ? 'Downloading assets' : 'Downloading Minecraft', 8 + Math.round((p.task / p.total) * 60)); send('status', { k: 'download', arg: p.type, pct: 6 + Math.round((p.task / p.total) * 70) }); });
    launcher.on('debug', (m) => send('log', clean(m)));
    launcher.on('data', (m) => {
      const l = clean(m); send('log', l);
      if (/Setting user|LWJGL|Loading/i.test(l)) splashSet('Starting Minecraft', 78);
      if (/OpenAL initialized|textures-atlas|Sound engine started|Reloading ResourceManager/i.test(l)) { splashSet('Starting Minecraft', 96); setTimeout(closeSplash, 1800); }
    });
    setTimeout(closeSplash, 150000);
    launcher.on('arguments', () => {
      if (!yaMode) return;
      try { patchSplash(); } catch (e) { send('log', 'splash: ' + e.message); }
    });
    launcher.on('close', (code) => {
      closeSplash();
      if (win && !win.isDestroyed() && win.isMinimized()) win.restore();
      const crashed = code !== 0 && Date.now() - startedAt < 60000;
      send('status', { k: crashed ? 'crashed' : 'closed', arg: code, pct: 0, done: true, crashed });
    });

    const launchOpts = {
      authorization: state.account.auth,
      root: gameDir,
      javaPath,
      version: fabric ? { number: version, type: versionType(version), custom: fabric.id } : { number: version, type: versionType(version) },
      overrides: fabric ? { gameDirectory: fabric.dir } : {},
      memory: { max: `${state.ram}M`, min: '1024M' },
      customArgs: yaMode ? [`-javaagent:${loaderJar}`] : []
    };
    if (opts && opts.server && opts.server.host) {
      launchOpts.server = { host: String(opts.server.host), port: String(opts.server.port || 25565) };
    }
    send('status', { k: 'starting', pct: 80 });
    const proc = await launcher.launch(launchOpts);
    if (!proc) { closeSplash(); return { error: 'launch_failed' }; }
    send('status', { k: 'ingame', pct: 100, playing: true });
    if (state.minimizeOnLaunch && win) win.minimize();
    return { ok: true };
  } catch (e) {
    closeSplash();
    return { error: 'launch_error', detail: e.message };
  }
});
