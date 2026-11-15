const $ = (id) => document.getElementById(id);
let busy = false;

function showAccount(a) {
  $('an').textContent = a ? a.name : 'Non connesso';
  $('avatar').textContent = a ? a.name[0].toUpperCase() : '?';
  $('logout').hidden = !a;
  $('login').hidden = !!a;
  $('launch').hidden = !a;
}
const err = (m) => ($('err').textContent = m || '');

(async () => {
  const s = await ya.getState();
  showAccount(s.account);
  $('ram').value = s.ram; $('ramv').textContent = s.ram;
  $('java').value = s.javaPath === 'java' ? '' : s.javaPath;
})();

document.querySelectorAll('.tab').forEach((t) => t.onclick = () => {
  document.querySelectorAll('.tab').forEach((x) => x.classList.toggle('on', x === t));
  document.querySelectorAll('.view').forEach((v) => v.classList.toggle('on', v.id === t.dataset.tab));
});

$('min').onclick = () => ya.win('min');
$('close').onclick = () => ya.win('close');
$('ram').oninput = (e) => ($('ramv').textContent = e.target.value);
$('savebtn').onclick = async () => {
  await ya.saveSettings({ ram: +$('ram').value, javaPath: $('java').value.trim() || 'java' });
  $('savebtn').textContent = 'Salvato'; setTimeout(() => ($('savebtn').textContent = 'Salva'), 1200);
};

$('ms').onclick = async () => {
  err(''); const r = await ya.loginMicrosoft();
  r.error ? err(r.error) : showAccount(r);
};
$('off').onclick = async () => {
  err(''); const r = await ya.loginOffline($('offname').value);
  r.error ? err(r.error) : showAccount(r);
};
$('logout').onclick = async () => { await ya.logout(); showAccount(null); };

$('playbtn').onclick = async () => {
  if (busy) return; busy = true; err('');
  $('playbtn').disabled = true;
  const r = await ya.play();
  if (r.error) { err(r.error); busy = false; $('playbtn').disabled = false; $('stat').textContent = 'Pronto'; $('fill').style.width = '0'; }
};

ya.on('status', (s) => {
  $('stat').textContent = s.text;
  $('fill').style.width = s.pct + '%';
  if (s.done) { busy = false; $('playbtn').disabled = false; }
});
