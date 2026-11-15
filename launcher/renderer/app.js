const $ = (id) => document.getElementById(id);
const S = { state: null, busy: false, playing: false };
let toastTimer, saveTimer;

function toast(msg) {
  const t = $('toast');
  t.textContent = msg;
  t.classList.add('on');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.remove('on'), 1600);
}

// ---------- viste ----------
function show(view) {
  document.querySelectorAll('.nav').forEach((n) => n.classList.toggle('on', n.dataset.view === view));
  document.querySelectorAll('.view').forEach((v) => v.classList.toggle('on', v.id === view));
}
document.querySelectorAll('.nav').forEach((n) => (n.onclick = () => show(n.dataset.view)));
$('avatar').onclick = () => show('settings');
$('min').onclick = () => ya.win('min');
$('close').onclick = () => ya.win('close');

// ---------- account ----------
function renderAccount() {
  const a = S.state && S.state.account;
  const initial = a ? a.name[0].toUpperCase() : '?';
  $('avatar').textContent = initial;
  $('avatar2').textContent = initial;
  $('an').textContent = a ? a.name : 'Non connesso';
  $('at').textContent = a ? (a.type === 'microsoft' ? 'Account Microsoft' : 'Account offline') : '';
  $('logout').hidden = !a;
  $('login').classList.toggle('on', !a);
  $('playsub').textContent = a ? a.name : 'YA CLIENT';
}

$('ms').onclick = async () => {
  $('lerr').textContent = '';
  $('ms').disabled = true;
  const r = await ya.loginMicrosoft();
  $('ms').disabled = false;
  if (r.error) { $('lerr').textContent = r.error; return; }
  S.state = await ya.getState();
  renderAccount();
};
async function offlineLogin() {
  $('lerr').textContent = '';
  const r = await ya.loginOffline($('offname').value);
  if (r.error) { $('lerr').textContent = r.error; return; }
  S.state = await ya.getState();
  renderAccount();
}
$('off').onclick = offlineLogin;
$('offname').addEventListener('keydown', (e) => { if (e.key === 'Enter') offlineLogin(); });
$('logout').onclick = async () => {
  await ya.logout();
  S.state = await ya.getState();
  renderAccount();
};

// ---------- impostazioni (si salvano da sole) ----------
function setRam(v) {
  $('ram').value = v;
  $('ramv').textContent = v + ' MB';
  document.querySelectorAll('#ramseg button').forEach((b) => b.classList.toggle('on', +b.dataset.ram === +v));
}
function queueSave() {
  clearTimeout(saveTimer);
  saveTimer = setTimeout(async () => {
    await ya.saveSettings({
      ram: +$('ram').value,
      javaPath: $('java').value.trim() || 'java',
      minimizeOnLaunch: $('minimize').checked
    });
    toast('Salvato');
  }, 350);
}
$('ram').oninput = (e) => { setRam(e.target.value); queueSave(); };
document.querySelectorAll('#ramseg button').forEach((b) => (b.onclick = () => { setRam(b.dataset.ram); queueSave(); }));
$('java').oninput = queueSave;
$('minimize').onchange = queueSave;
$('folder').onclick = () => ya.openFolder();
$('detect').onclick = async () => {
  const p = await ya.detectJava();
  const hint = $('javastat');
  if (p) {
    $('java').value = p;
    hint.textContent = 'Java 8 trovato.';
    hint.className = 'hint ok';
    queueSave();
  } else {
    hint.textContent = 'Java 8 non trovato: installa Temurin 8 da adoptium.net.';
    hint.className = 'hint';
  }
};

// ---------- gioco ----------
function setChip(text, busy) {
  const c = document.querySelector('.chip');
  c.lastChild.textContent = text;
  c.classList.toggle('busy', !!busy);
}
function setPlay(label, disabled) {
  $('playlabel').textContent = label;
  $('playbtn').disabled = disabled;
}
function resetPlay() {
  S.busy = false;
  S.playing = false;
  setPlay('GIOCA', false);
  setChip('Pronto', false);
}

$('playbtn').onclick = async () => {
  if (S.busy) return;
  S.busy = true;
  $('err').textContent = '';
  setPlay('AVVIO', true);
  setChip('Avvio in corso', true);
  const r = await ya.play();
  if (r && r.error) {
    $('err').textContent = r.error;
    $('stat').textContent = 'Pronto a giocare';
    $('pct').textContent = '';
    $('fill').style.width = '0';
    resetPlay();
  }
};

ya.on('status', (s) => {
  $('stat').textContent = s.text;
  $('pct').textContent = s.pct ? s.pct + '%' : '';
  $('fill').style.width = (s.pct || 0) + '%';
  if (s.playing) { S.playing = true; setPlay('IN GIOCO', true); setChip('In gioco', false); }
  if (s.done) {
    resetPlay();
    $('pct').textContent = '';
    if (s.crashed) { $('err').textContent = s.text; show('console'); }
  }
});

// ---------- console ----------
const logEl = $('log');
let lines = 0;
ya.on('log', (m) => {
  const stick = logEl.scrollTop + logEl.clientHeight >= logEl.scrollHeight - 30;
  logEl.appendChild(document.createTextNode(String(m).replace(/\s+$/, '') + '\n'));
  if (++lines > 800) { logEl.removeChild(logEl.firstChild); lines--; }
  if (stick) logEl.scrollTop = logEl.scrollHeight;
});
$('clearlog').onclick = () => { logEl.textContent = ''; lines = 0; };
$('copylog').onclick = async () => {
  try { await navigator.clipboard.writeText(logEl.textContent); toast('Log copiato'); }
  catch (_) { toast('Seleziona il testo e copia con Ctrl+C'); }
};

// ---------- avvio ----------
(async () => {
  S.state = await ya.getState();
  setRam(S.state.ram);
  $('java').value = S.state.javaPath === 'java' ? '' : S.state.javaPath;
  $('minimize').checked = !!S.state.minimizeOnLaunch;
  renderAccount();
})();
