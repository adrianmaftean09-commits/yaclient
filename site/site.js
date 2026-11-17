(function () {
  var Y = window.YA || {};
  document.querySelectorAll('[data-dc]').forEach(function (a) { a.href = Y.discord; a.target = '_blank'; a.rel = 'noopener'; });
  document.querySelectorAll('[data-dl]').forEach(function (a) { a.href = (Y.github || '#') + '/releases/latest'; a.target = '_blank'; a.rel = 'noopener'; });
  var eur = function (n) { return '€' + (Math.round(n * 100) / 100).toFixed(n % 1 ? 2 : 0); };
  window.YAeur = eur;

  // ---- checkout modal: username -> order text for a Discord ticket (+ optional PayPal.Me link) ----
  var mo = document.createElement('div'); mo.className = 'mo';
  mo.innerHTML = '<div class="md"><button class="x" aria-label="Close">×</button><div id="mdb"></div></div>';
  document.addEventListener('DOMContentLoaded', function () { document.body.appendChild(mo); });
  if (document.body) document.body.appendChild(mo);
  mo.addEventListener('click', function (e) { if (e.target === mo || e.target.className === 'x') mo.classList.remove('show'); });
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape') mo.classList.remove('show'); });

  /* order = { title, lines:[names], total:number, ids:'mono_cape,...' | 'plus', note } */
  window.YABuy = function (order) {
    var b = mo.querySelector('#mdb');
    var free = !order.total;
    b.innerHTML = '<h3>' + (free ? 'Get ' : 'Buy ') + order.title + '</h3><p>' + (free ? 'Free for beta testers.' : 'Delivered to your Minecraft account.') + '</p>' +
      '<div class="sum">' + order.lines.map(function (l) { return '• ' + l; }).join('<br>') + '<br><br>Total: <b>' + (free ? 'Free' : eur(order.total)) + '</b></div>' +
      '<input id="mdu" placeholder="Your Minecraft username" maxlength="16" autocomplete="off" spellcheck="false"><div class="err" id="mde"></div>' +
      '<div class="stack"><button class="btn pri block" id="mdgo">Continue</button></div>';
    mo.classList.add('show');
    var u = b.querySelector('#mdu'); u.focus();
    function go() {
      var name = u.value.trim();
      if (!/^[A-Za-z0-9_]{3,16}$/.test(name)) { b.querySelector('#mde').textContent = 'Enter your exact Minecraft username (3-16 letters, numbers or _).'; return; }
      var ticket = 'Order: ' + order.title + '\nItems: ' + order.ids + '\nMinecraft username: ' + name + '\nTotal: ' + (free ? 'Free' : eur(order.total)) + (order.note ? '\n' + order.note : '');
      var pay = '';
      if (!free && Y.paypalMe) pay = '<a class="btn pri block" target="_blank" rel="noopener" href="https://paypal.me/' + encodeURIComponent(Y.paypalMe) + '/' + order.total.toFixed(2) + (Y.currency || 'EUR') + '">1. Pay ' + eur(order.total) + ' with PayPal</a>';
      b.innerHTML = '<h3>Almost done</h3><p>' + (free ? 'Open a ticket on Discord and paste this text.' : (pay ? 'Pay, then open a ticket on Discord and paste this text with your payment receipt.' : 'Open a ticket on Discord and paste this text. We will send you the payment options.')) + '</p>' +
        '<code id="mdt"></code><div class="stack">' + pay + '<button class="btn block" id="mdc">Copy text</button><a class="btn ' + (pay ? '' : 'pri ') + 'block" target="_blank" rel="noopener" href="' + Y.discord + '">' + (pay ? '2. ' : '') + 'Open Discord</a></div>';
      b.querySelector('#mdt').textContent = ticket;
      b.querySelector('#mdc').onclick = function () { try { navigator.clipboard.writeText(ticket); this.textContent = 'Copied'; } catch (e) { var r = document.createRange(); r.selectNode(b.querySelector('#mdt')); getSelection().removeAllRanges(); getSelection().addRange(r); } };
    }
    b.querySelector('#mdgo').onclick = go;
    u.onkeydown = function (e) { if (e.key === 'Enter') go(); };
  };
})();
