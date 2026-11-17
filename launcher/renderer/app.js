'use strict';
const $ = (s, r = document) => r.querySelector(s);
const esc = (s) => String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const safeImg = (u) => (/^(https:\/\/|\.\.\/assets\/[\w\/.-]+$)/i.test(u || '') ? u : '');

const I18N = {
  en: { launchpad: 'Launchpad', mods: 'Modules', servers: 'Servers', cosmetics: 'Cosmetics', settings: 'Settings', launch: 'LAUNCH GAME', launching: 'LAUNCHING...',
    welcome: 'Minecraft 1.8.9 PvP client', news: 'News', noAcc: 'Not signed in', signIn: 'Sign in', switchAcc: 'Switch account', advert: 'Ad space',
    play: 'Play', noServers: 'No servers yet. Add them in launcher/content.json', modsHint: 'Enable or disable modules. Changes apply the next time you launch.',
    store: 'Open store', storeHint: 'Cosmetics are bought on our website and equipped in game (Right Shift > Cosmetics).', notConf: 'The store is not configured yet.', offline: 'Store unreachable.',
    language: 'Language', ram: 'Memory (MB)', java: 'Java 8 path', detect: 'Detect', minimize: 'Minimize when game starts', folder: 'Open game folder', logout: 'Sign out', console: 'Console',
    microsoft: 'Microsoft login', offlineL: 'Offline name', offlineBtn: 'Continue offline', cancel: 'Close', name_short: 'Name must be at least 3 characters.', ms_failed: 'Microsoft login failed.',
    need_login: 'Sign in first.', session_expired: 'Your Microsoft session expired. Sign in again (Account > Sign in).', launch_failed: 'Launch failed.', launch_error: 'Error', crashed: 'The game closed unexpectedly. Check the console.', closed: 'Game closed.', ingame: 'In game',
    updating: 'Updating client...', loader: 'Preparing loader...', starting: 'Starting game...', download: 'Downloading game files...', wings: 'Wings', cape: 'Capes', pet: 'Pets', bandana: 'Bandanas', all: 'All', price: 'Price', online: 'Online', javaHint: 'Java 8 is required' },
  it: { launchpad: 'Launchpad', mods: 'Moduli', servers: 'Server', cosmetics: 'Cosmetici', settings: 'Impostazioni', launch: 'AVVIA GIOCO', launching: 'AVVIO...',
    welcome: 'Client PvP per Minecraft 1.8.9', news: 'Novità', noAcc: 'Non connesso', signIn: 'Accedi', switchAcc: 'Cambia account', advert: 'Spazio pubblicitario',
    play: 'Gioca', noServers: 'Nessun server. Aggiungili in launcher/content.json', modsHint: 'Attiva o disattiva i moduli. Le modifiche valgono al prossimo avvio.',
    store: 'Apri lo store', storeHint: 'I cosmetici si comprano sul nostro sito e si equipaggiano in gioco (Shift destro > Cosmetici).', notConf: 'Lo store non è ancora configurato.', offline: 'Store non raggiungibile.',
    language: 'Lingua', ram: 'Memoria (MB)', java: 'Percorso Java 8', detect: 'Rileva', minimize: 'Riduci a icona all\'avvio', folder: 'Apri cartella di gioco', logout: 'Esci', console: 'Console',
    microsoft: 'Accesso Microsoft', offlineL: 'Nome offline', offlineBtn: 'Continua offline', cancel: 'Chiudi', name_short: 'Il nome deve avere almeno 3 caratteri.', ms_failed: 'Accesso Microsoft non riuscito.',
    need_login: 'Accedi prima.', session_expired: 'La sessione Microsoft è scaduta. Accedi di nuovo (Account > Accedi).', launch_failed: 'Avvio non riuscito.', launch_error: 'Errore', crashed: 'Il gioco si è chiuso all\'improvviso. Controlla la console.', closed: 'Gioco chiuso.', ingame: 'In gioco',
    updating: 'Aggiorno il client...', loader: 'Preparo il loader...', starting: 'Avvio il gioco...', download: 'Scarico i file di gioco...', wings: 'Ali', cape: 'Mantelli', pet: 'Animaletti', bandana: 'Bandane', all: 'Tutti', price: 'Prezzo', online: 'Online', javaHint: 'Serve Java 8' },
  es: { launchpad: 'Launchpad', mods: 'Módulos', servers: 'Servidores', cosmetics: 'Cosméticos', settings: 'Ajustes', launch: 'INICIAR JUEGO', launching: 'INICIANDO...',
    welcome: 'Cliente PvP de Minecraft 1.8.9', news: 'Noticias', noAcc: 'Sin sesión', signIn: 'Iniciar sesión', switchAcc: 'Cambiar cuenta', advert: 'Espacio publicitario',
    play: 'Jugar', noServers: 'Aún no hay servidores. Añádelos en launcher/content.json', modsHint: 'Activa o desactiva módulos. Los cambios se aplican al iniciar.',
    store: 'Abrir tienda', storeHint: 'Los cosméticos se compran en nuestra web y se equipan en el juego (Shift der. > Cosméticos).', notConf: 'La tienda aún no está configurada.', offline: 'Tienda no disponible.',
    language: 'Idioma', ram: 'Memoria (MB)', java: 'Ruta de Java 8', detect: 'Detectar', minimize: 'Minimizar al iniciar', folder: 'Abrir carpeta del juego', logout: 'Cerrar sesión', console: 'Consola',
    microsoft: 'Cuenta Microsoft', offlineL: 'Nombre sin conexión', offlineBtn: 'Continuar sin conexión', cancel: 'Cerrar', name_short: 'El nombre necesita 3 caracteres como mínimo.', ms_failed: 'Error al iniciar sesión con Microsoft.',
    need_login: 'Inicia sesión primero.', session_expired: 'La sesión de Microsoft caducó. Inicia sesión de nuevo.', launch_failed: 'Error al iniciar.', launch_error: 'Error', crashed: 'El juego se cerró inesperadamente. Revisa la consola.', closed: 'Juego cerrado.', ingame: 'En juego',
    updating: 'Actualizando cliente...', loader: 'Preparando loader...', starting: 'Iniciando juego...', download: 'Descargando archivos...', wings: 'Alas', cape: 'Capas', pet: 'Mascotas', bandana: 'Bandanas', all: 'Todo', price: 'Precio', online: 'En línea', javaHint: 'Se necesita Java 8' },
  de: { launchpad: 'Launchpad', mods: 'Module', servers: 'Server', cosmetics: 'Kosmetik', settings: 'Einstellungen', launch: 'SPIEL STARTEN', launching: 'STARTE...',
    welcome: 'PvP-Client für Minecraft 1.8.9', news: 'Neuigkeiten', noAcc: 'Nicht angemeldet', signIn: 'Anmelden', switchAcc: 'Konto wechseln', advert: 'Werbefläche',
    play: 'Spielen', noServers: 'Noch keine Server. Trage sie in launcher/content.json ein', modsHint: 'Module ein-/ausschalten. Gilt ab dem nächsten Start.',
    store: 'Shop öffnen', storeHint: 'Kosmetik wird auf unserer Website gekauft und im Spiel ausgerüstet (Rechts-Shift > Kosmetik).', notConf: 'Der Shop ist noch nicht eingerichtet.', offline: 'Shop nicht erreichbar.',
    language: 'Sprache', ram: 'Speicher (MB)', java: 'Java-8-Pfad', detect: 'Suchen', minimize: 'Beim Start minimieren', folder: 'Spielordner öffnen', logout: 'Abmelden', console: 'Konsole',
    microsoft: 'Microsoft-Login', offlineL: 'Offline-Name', offlineBtn: 'Offline fortfahren', cancel: 'Schließen', name_short: 'Name braucht mindestens 3 Zeichen.', ms_failed: 'Microsoft-Login fehlgeschlagen.',
    need_login: 'Bitte zuerst anmelden.', launch_failed: 'Start fehlgeschlagen.', launch_error: 'Fehler', crashed: 'Das Spiel wurde unerwartet beendet. Siehe Konsole.', closed: 'Spiel beendet.', ingame: 'Im Spiel',
    updating: 'Client wird aktualisiert...', loader: 'Loader wird vorbereitet...', starting: 'Spiel startet...', download: 'Lade Spieldateien...', wings: 'Flügel', cape: 'Umhänge', pet: 'Haustiere', bandana: 'Bandanas', all: 'Alle', price: 'Preis', online: 'Online', javaHint: 'Java 8 erforderlich' }
};
Object.assign(I18N.en, { back: 'Welcome back', versions: 'Versions', latest: 'Latest', releases: 'Releases', snapshots: 'Snapshots', old: 'Old', search: 'Search version...', modsFor: 'Modules for Minecraft', avail: 'available on this version',
  needs: 'Only on', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Soon', vanillaNote: 'This version starts without YA CLIENT mods. The mods below work on', java_missing: 'Java {n} is required for this version. Install it (Temurin {n}) and try again.', latestRelease: 'Latest release', change: 'Change version', noRes: 'No versions found.', offlineList: 'Offline: showing a short list of versions.' });
Object.assign(I18N.it, { back: 'Bentornato', versions: 'Versioni', latest: 'Ultima', releases: 'Release', snapshots: 'Snapshot', old: 'Vecchie', search: 'Cerca versione...', modsFor: 'Mod per Minecraft', avail: 'disponibili in questa versione',
  needs: 'Solo su', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Presto', vanillaNote: 'Questa versione parte senza le mod di YA CLIENT. Le mod qui sotto funzionano su', java_missing: 'Per questa versione serve Java {n}. Installalo (Temurin {n}) e riprova.', latestRelease: 'Ultima release', change: 'Cambia versione', noRes: 'Nessuna versione trovata.', offlineList: 'Offline: mostro un elenco ridotto di versioni.' });
Object.assign(I18N.es, { back: 'Bienvenido de nuevo', versions: 'Versiones', latest: 'Última', releases: 'Releases', snapshots: 'Snapshots', old: 'Antiguas', search: 'Buscar versión...', modsFor: 'Mods para Minecraft', avail: 'disponibles en esta versión',
  needs: 'Solo en', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Pronto', vanillaNote: 'Esta versión inicia sin los mods de YA CLIENT. Los mods de abajo funcionan en', java_missing: 'Esta versión necesita Java {n}. Instálalo (Temurin {n}) e inténtalo de nuevo.', latestRelease: 'Última release', change: 'Cambiar versión', noRes: 'No hay versiones.', offlineList: 'Sin conexión: lista reducida de versiones.' });
Object.assign(I18N.de, { session_expired: 'Die Microsoft-Sitzung ist abgelaufen. Bitte neu anmelden.', back: 'Willkommen zurück', versions: 'Versionen', latest: 'Neueste', releases: 'Releases', snapshots: 'Snapshots', old: 'Alt', search: 'Version suchen...', modsFor: 'Mods für Minecraft', avail: 'in dieser Version verfügbar',
  needs: 'Nur in', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Bald', vanillaNote: 'Diese Version startet ohne YA-CLIENT-Mods. Die Mods unten funktionieren in', java_missing: 'Für diese Version wird Java {n} benötigt. Installiere es (Temurin {n}) und versuche es erneut.', latestRelease: 'Neueste Release', change: 'Version ändern', noRes: 'Keine Versionen gefunden.', offlineList: 'Offline: gekürzte Versionsliste.' });
const LANGS = { en: 'English', it: 'Italiano', es: 'Español', de: 'Deutsch' };
const SLOT = { ya_cape: 'cape', ya_wings: 'wings', ya_bandana: 'bandana', ya_pet: 'pet', mono_cape: 'cape', mono_wings: 'wings', mono_bandana: 'bandana', mono_pet: 'pet' };
const NAMES = { ya_cape: 'YA Cape', ya_wings: 'Ember Wings', ya_bandana: 'YA Bandana', ya_pet: 'Ember (pet)', mono_cape: 'Infinity Cape', mono_wings: 'Shadow Dragon Wings', mono_bandana: 'Monochrome Bandana', mono_pet: 'Yin Yang (pet)' };
const ICON = {
  home: '<path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/>',
  versions: '<path d="M12 3l9 5-9 5-9-5z"/><path d="M3 13l9 5 9-5"/><path d="M3 17.5l9 5 9-5"/>',
  mods: '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
  servers: '<rect x="3" y="4" width="18" height="7" rx="2"/><rect x="3" y="13" width="18" height="7" rx="2"/><path d="M7 7.5h.01M7 16.5h.01"/>',
  cosmetics: '<path d="M8 3L3.5 5.5 2 10l3.5 1.5L7 10v11h10V10l1.5 1.5L22 10l-1.5-4.5L16 3"/><path d="M8 3c.5 2 2 3.5 4 3.5S15.5 5 16 3"/>',
  console: '<rect x="3" y="4" width="18" height="16" rx="2.5"/><path d="M7 9l3 3-3 3M12 16h5"/>',
  settings: '<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1L7 17M17 7l2.1-2.1"/>',
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21c1-4 4-6 8-6s7 2 8 6"/>'
};
const ic = (n) => `<svg viewBox="0 0 24 24">${ICON[n]}</svg>`;

let S = { lang: 'en', account: null, ram: 2048, javaPath: 'java', minimizeOnLaunch: false };
let C = {}; let MODSTATE = {}; let MODS = []; let VERS = { versions: [], latest: {}, online: true }; let vtab = 'release'; let vq = ''; let page = 'home'; let busy = false; let cosTab = 'all';
let logBuf = []; let status = { text: '', pct: 0, show: false };
const t = (k) => (I18N[S.lang] && I18N[S.lang][k]) || I18N.en[k] || k;

const PAGES = ['home', 'versions', 'servers', 'cosmetics', 'console', 'settings'];
const PKEY = { home: 'launchpad', versions: 'versions', servers: 'servers', cosmetics: 'cosmetics', console: 'console', settings: 'settings' };

function renderNav() {
  $('#nav').innerHTML = PAGES.map((p) => `<button class="nav ${p === page ? 'on' : ''}" data-p="${p}" title="${esc(t(PKEY[p]))}">${ic(p)}</button>`).join('');
  $('#btnAcc').title = S.account ? S.account.name : t('signIn');
  $('#accIcon').innerHTML = ic('user');
}
function go(p) { page = p; renderNav(); render(); }

function adHtml(a) {
  if (a && safeImg(a.image)) return `<div class="card ad ${a.url ? 'link' : ''}" data-url="${esc(a.url || '')}"><img src="${esc(safeImg(a.image))}" alt=""></div>`;
  return `<div class="card ad">${esc(t('advert'))}</div>`;
}
function newsHtml(list) {
  if (!list || !list.length) return '';
  return `<h2>${esc(t('news'))}</h2><div class="news">` + list.map((n) => {
    const im = safeImg(n.image);
    return `<div class="card nc ${n.url ? 'link' : ''}" data-url="${esc(n.url || '')}"><div class="im" ${im ? `style="background-image:url('${esc(im)}')"` : ''}></div>
      <div class="tx"><b>${esc(n.title)}</b><p>${esc(n.text)}</p><small>${esc(n.date || '')}</small></div></div>`;
  }).join('') + '</div>';
}

function viewHome() {
  const ads = C.ads || [];
  const nm = S.account ? S.account.name : '';
  const tag = isYa(S.version) ? 'YA CLIENT' : t('vanilla');
  return `<div class="lp"><div>
    <div class="welcome"><span class="wt">${esc(t('back'))}${nm ? ',' : ''}</span>${nm ? ' <b class="wn">' + esc(nm) + '</b>' : ''}</div>
    <div class="card hero2"><img class="hlogo" src="../assets/logo.png" alt="">
      <button class="launch" id="play" ${busy ? 'disabled' : ''}><b>${esc(busy ? t('launching') : t('launch'))}</b>
        <span>${esc(tag)} ${esc(S.version)}</span></button>
      <button class="gear" data-p="versions" title="${esc(t('change'))}">${ic('versions')}</button>
      <div class="bar" id="bar" style="display:${status.show ? 'block' : 'none'}"><i id="barI" style="width:${status.pct}%"></i></div><div class="stat" id="stat">${esc(status.text)}</div></div>
    ${newsHtml(C.news)}</div>
    <div class="side"><div class="card acc"><div class="av">${esc(S.account ? S.account.name[0].toUpperCase() : '?')}</div>
      <div style="flex:1"><b>${esc(S.account ? S.account.name : t('noAcc'))}</b><small>${esc(S.account ? S.account.type : '')}</small></div>
      <button class="btn sm" id="accBtn">${esc(S.account ? t('switchAcc') : t('signIn'))}</button></div>
    ${adHtml(ads[0])}${adHtml(ads[1])}</div></div>`;
}
function cmpV(a, b) {
  const pa = String(a).split('.').map((n) => parseInt(n, 10) || 0), pb = String(b).split('.').map((n) => parseInt(n, 10) || 0);
  for (let i = 0; i < 3; i++) { const d = (pa[i] || 0) - (pb[i] || 0); if (d) return d; }
  return 0;
}
function specOk(spec, v) {
  spec = String(spec);
  if (spec === '*') return true;
  if (spec.indexOf('>=') === 0) return /^\d+\.\d+/.test(v) && cmpV(v, spec.slice(2)) >= 0;
  if (/\.x$/.test(spec)) return v === spec.slice(0, -2) || v.indexOf(spec.slice(0, -1)) === 0;
  return v === spec;
}
const modOk = (m, v) => !m.soon && (m.versions || []).some((sp) => specOk(sp, v));
const isYa = (v) => v === (S.ya || '1.8.9');

function versionRows() {
  const q = vq.trim().toLowerCase();
  let list = VERS.versions.filter((v) => (vtab === 'release' ? v.type === 'release' : vtab === 'snapshot' ? v.type === 'snapshot' : (v.type === 'old_beta' || v.type === 'old_alpha')));
  if (q) list = list.filter((v) => v.id.toLowerCase().indexOf(q) >= 0);
  const lat = VERS.latest && VERS.latest.release;
  const rows = [];
  const row = (v) => `<button class="vrow ${v.id === S.version ? 'on' : ''}" data-ver="${esc(v.id)}"><b>${esc(v.id)}</b>
    ${v.id === lat ? `<span class="badge">${esc(t('latest'))}</span>` : ''}${isYa(v.id) ? `<span class="badge ya">${esc(t('ya'))}</span>` : ''}
    <small>${esc((v.time || '').slice(0, 10))}</small></button>`;
  list.slice(0, 250).forEach((v) => rows.push(row(v)));
  return rows.join('') || `<div class="note" style="padding:14px">${esc(t('noRes'))}</div>`;
}
function modsGrid() {
  const v = S.version;
  const ok = MODS.filter((m) => modOk(m, v)).length;
  const items = MODS.slice().sort((a, b) => (modOk(b, v) ? 1 : 0) - (modOk(a, v) ? 1 : 0));
  return { ok, html: items.map((m) => {
    const on = modOk(m, v);
    const need = m.soon ? t('soon') : t('needs') + ' ' + (m.versions || []).join(', ');
    return `<div class="card mod ${on ? '' : 'off'}"><div><span>${esc(m.name)}</span><small>${esc(on ? m.desc : need)}</small></div>
      ${on ? `<button class="sw ${MODSTATE[m.id] === false ? '' : 'on'}" data-mod="${esc(m.id)}" aria-label="${esc(m.name)}"></button>` : '<span class="lockic">' + ic('lock') + '</span>'}</div>`;
  }).join('') };
}
function viewVersions() {
  const g = modsGrid();
  const tabs = [['release', 'releases'], ['snapshot', 'snapshots'], ['old', 'old']];
  return `<div class="vp"><div class="card vl"><input type="text" id="vq" placeholder="${esc(t('search'))}" value="${esc(vq)}">
    <div class="tabs" id="vtabs" style="margin:10px 0">${tabs.map((x) => `<button class="tab ${vtab === x[0] ? 'on' : ''}" data-vt="${x[0]}">${esc(t(x[1]))}</button>`).join('')}</div>
    ${VERS.online ? '' : `<div class="note">${esc(t('offlineList'))}</div>`}
    <div class="vlist" id="vlist">${versionRows()}</div></div>
    <div><h1>${esc(t('modsFor'))} ${esc(S.version)}</h1>
    <p class="sub" id="vsub">${esc(g.ok + ' / ' + MODS.length + ' ' + t('avail'))}${isYa(S.version) ? '' : ' · ' + esc(t('vanillaNote')) + ' ' + esc(S.ya || '1.8.9')}</p>
    <div class="mods" id="mgrid">${g.html}</div></div></div>`;
}
function viewServers() {
  const list = C.servers || [];
  let h = `<h1>${esc(t('servers'))}</h1><p class="sub"></p><div class="srv">`;
  if (!list.length) h += `<div class="card sv ph">${esc(t('noServers'))}</div>`;
  list.forEach((s, i) => {
    const im = safeImg(s.image);
    h += `<div class="card sv"><div class="im" ${im ? `style="background-image:url('${esc(im)}')"` : ''}>${im ? '' : esc((s.name || '?')[0].toUpperCase())}</div>
      <div class="tx"><b>${esc(s.name)}</b><small>${esc(s.description || (s.host + (s.port && s.port != 25565 ? ':' + s.port : '')))}</small>
      <button class="btn sm" data-srv="${i}" ${busy ? 'disabled' : ''}>${esc(t('play'))}</button></div></div>`;
  });
  return h + '</div>';
}
function viewCosmetics() {
  return `<h1>${esc(t('cosmetics'))}</h1><p class="sub">${esc(t('storeHint'))}</p>
    <div class="playrow" style="margin-bottom:18px"><button class="btn" id="openStore">${esc(t('store'))}</button></div>
    <div class="tabs" id="ctabs"></div><div id="cmsg" class="note"></div><div class="cos" id="cgrid"></div>`;
}
async function fillCosmetics() {
  const r = await window.ya.catalog();
  const msg = $('#cmsg'); if (!msg) return;
  if (r.error) { msg.textContent = r.error === 'offline' ? t('offline') : t('notConf'); }
  const prices = (r.data) || {};
  const slots = ['all', 'cape', 'wings', 'bandana', 'pet'];
  $('#ctabs').innerHTML = slots.map((s) => `<button class="tab ${s === cosTab ? 'on' : ''}" data-ct="${s}">${esc(t(s))}</button>`).join('');
  $('#cgrid').innerHTML = Object.keys(SLOT).filter((id) => cosTab === 'all' || SLOT[id] === cosTab).map((id) =>
    `<div class="card ci"><div class="pv">${esc(t(SLOT[id]))}</div><b>${esc(NAMES[id])}</b><small>${esc(prices[id] ? t('price') + ': ' + prices[id] : '')}</small></div>`).join('');
  S._store = r.store || '';
}
function viewSettings() {
  return `<h1>${esc(t('settings'))}</h1><p class="sub"></p><div class="set">
   <div class="card row"><label>${esc(t('language'))}</label><select id="sLang">${Object.keys(LANGS).map((k) => `<option value="${k}" ${k === S.lang ? 'selected' : ''}>${LANGS[k]}</option>`).join('')}</select></div>
   <div class="card row"><label>${esc(t('ram'))}<small id="ramv">${S.ram} MB</small></label><input type="range" id="sRam" min="1024" max="8192" step="256" value="${S.ram}"></div>
   <div class="card row"><label>${esc(t('java'))}<small>${esc(t('javaHint'))}</small></label><div style="display:flex;gap:8px"><input type="text" id="sJava" value="${esc(S.javaPath || 'java')}"><button class="btn sm" id="sDet">${esc(t('detect'))}</button></div></div>
   <div class="card row"><label>${esc(t('minimize'))}</label><button class="sw ${S.minimizeOnLaunch ? 'on' : ''}" id="sMin"></button></div>
   <div class="card row"><label>${esc(t('folder'))}</label><button class="btn sm" id="sFolder">&#8594;</button></div>
   ${S.account ? `<div class="card row"><label>${esc(S.account.name)}</label><button class="btn sm" id="sOut">${esc(t('logout'))}</button></div>` : ''}
   </div>`;
}
function viewConsole() {
  return `<h1>${esc(t('console'))}</h1><p class="sub"></p><div class="con conp" id="con">${esc(logBuf.join('\n'))}</div>`;
}
function render() {
  const v = $('#view');
  v.innerHTML = { home: viewHome, versions: viewVersions, servers: viewServers, cosmetics: viewCosmetics, console: viewConsole, settings: viewSettings }[page]();
  if (page === 'cosmetics') fillCosmetics();
}

function showLogin() {
  const m = $('#modal'); m.hidden = false;
  m.innerHTML = `<div class="dlg"><div class="mark">YA</div><button class="btn" id="lMs">${esc(t('microsoft'))}</button>
    <label style="color:var(--mute);font-size:12px">${esc(t('offlineL'))}</label><input type="text" id="lName" maxlength="16" style="width:100%">
    <button class="btn" id="lOff">${esc(t('offlineBtn'))}</button><div class="err" id="lErr"></div><button class="btn" id="lClose">${esc(t('cancel'))}</button></div>`;
  const done = (r) => {
    if (r && r.error) { $('#lErr').textContent = t(r.error); return; }
    S.account = r; m.hidden = true; renderNav(); render();
  };
  $('#lClose').onclick = () => (m.hidden = true);
  $('#lMs').onclick = async () => { $('#lErr').textContent = '...'; done(await window.ya.loginMicrosoft()); };
  $('#lOff').onclick = async () => done(await window.ya.loginOffline($('#lName').value));
}

async function launch(server) {
  if (busy) return;
  if (!S.account) { showLogin(); return; }
  busy = true; status = { text: '', pct: 2, show: true }; go('home');
  const r = await window.ya.play(server ? { server } : {});
  if (r && r.error) {
    busy = false; status = { text: r.error === 'java_missing' ? t('java_missing').replace(/\{n\}/g, r.detail) : t(r.error) + (r.detail ? ': ' + r.detail : ''), pct: 0, show: false };
    if (r.error === 'need_login') showLogin();
    if (page === 'home') render();
  }
}

document.addEventListener('click', async (e) => {
  const el = e.target.closest('button, .nc, .ad');
  if (!el) return;
  if (el.dataset.w) return window.ya.win(el.dataset.w);
  if (el.dataset.p) return go(el.dataset.p);
  if (el.id === 'btnAcc' || el.id === 'accBtn') return showLogin();
  if (el.id === 'play') return launch();
  if (el.dataset.srv != null) { const s = (C.servers || [])[+el.dataset.srv]; if (s) launch({ host: s.host, port: s.port || 25565 }); return; }
  if (el.dataset.url) return window.ya.openExternal(el.dataset.url);
  if (el.dataset.ver) {
    S.version = el.dataset.ver; window.ya.saveSettings({ version: S.version });
    const g = modsGrid(); $('#vlist').innerHTML = versionRows(); $('#mgrid').innerHTML = g.html;
    $('#vsub').textContent = g.ok + ' / ' + MODS.length + ' ' + t('avail') + (isYa(S.version) ? '' : ' · ' + t('vanillaNote') + ' ' + (S.ya || '1.8.9'));
    $('#view h1').textContent = t('modsFor') + ' ' + S.version; return;
  }
  if (el.dataset.vt) { vtab = el.dataset.vt; document.querySelectorAll('#vtabs .tab').forEach((b) => b.classList.toggle('on', b.dataset.vt === vtab)); $('#vlist').innerHTML = versionRows(); return; }
  if (el.dataset.mod) {
    const id = el.dataset.mod; const on = MODSTATE[id] === false; MODSTATE[id] = on;
    el.classList.toggle('on', on); return window.ya.modsSet(id, on);
  }
  if (el.dataset.ct) { cosTab = el.dataset.ct; return fillCosmetics(); }
  if (el.id === 'openStore') { const u = S._store || C.storeUrl; if (u) window.ya.openExternal(u); return; }
  if (el.id === 'sMin') { S.minimizeOnLaunch = !S.minimizeOnLaunch; el.classList.toggle('on', S.minimizeOnLaunch); return window.ya.saveSettings({ minimizeOnLaunch: S.minimizeOnLaunch }); }
  if (el.id === 'sFolder') return window.ya.openFolder();
  if (el.id === 'sDet') { const p = await window.ya.detectJava(); if (p) { $('#sJava').value = p; S.javaPath = p; window.ya.saveSettings({ javaPath: p }); } return; }
  if (el.id === 'sOut') { await window.ya.logout(); S.account = null; renderNav(); render(); }
});
document.addEventListener('change', async (e) => {
  if (e.target.id === 'sLang') { S.lang = e.target.value; await window.ya.setLang(S.lang); renderNav(); render(); }
  if (e.target.id === 'sJava') { S.javaPath = e.target.value || 'java'; window.ya.saveSettings({ javaPath: S.javaPath }); }
  if (e.target.id === 'sRam') window.ya.saveSettings({ ram: S.ram });
});
document.addEventListener('input', (e) => {
  if (e.target.id === 'vq') { vq = e.target.value; $('#vlist').innerHTML = versionRows(); }
  if (e.target.id === 'sRam') { S.ram = +e.target.value; $('#ramv').textContent = S.ram + ' MB'; }
});

window.ya.on('status', (d) => {
  const txt = t(d.k) + (d.k === 'crashed' ? '' : '');
  status = { text: d.done ? t(d.k) : txt, pct: d.pct || 0, show: !d.done };
  if (d.done) busy = false;
  if (page === 'home') {
    const bar = $('#bar'); if (bar) { bar.style.display = status.show ? 'block' : 'none'; $('#barI').style.width = status.pct + '%'; $('#stat').textContent = status.text; }
    const p = $('#play'); if (p) { p.disabled = busy; p.textContent = busy ? t('launching') : t('launch'); }
  }
});
window.ya.on('log', (m) => {
  logBuf.push(String(m)); if (logBuf.length > 400) logBuf.shift();
  const c = $('#con'); if (c) { c.textContent = logBuf.join('\n'); c.scrollTop = c.scrollHeight; }
});

(async function init() {
  S = { ...S, ...(await window.ya.getState()) };
  if (!I18N[S.lang]) S.lang = 'en';
  try { C = await window.ya.content(); } catch (_) { C = {}; }
  try { MODSTATE = await window.ya.modsGet(); } catch (_) { MODSTATE = {}; }
  try { MODS = (await window.ya.mods()).mods || []; } catch (_) { MODS = []; }
  try { VERS = await window.ya.versions(); } catch (_) { }
  $('#ver').textContent = '';
  renderNav(); render();
})();
