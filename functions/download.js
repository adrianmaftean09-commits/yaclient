// /download -> latest installer for the visitor's OS (Windows .exe, macOS .dmg, Linux AppImage), straight to the file.
export async function onRequest(ctx) {
  const repo = 'adrianmaftean09-commits/yaclient';
  const u = new URL(ctx.request.url);
  const ua = ctx.request.headers.get('user-agent') || '';
  let os = (u.searchParams.get('os') || '').toLowerCase();
  if (!os) os = /Macintosh|Mac OS X/i.test(ua) && !/iPhone|iPad/i.test(ua) ? 'mac' : /Linux/i.test(ua) && !/Android/i.test(ua) ? 'linux' : 'win';
  const arm = u.searchParams.get('arch') === 'arm64';
  const pick = {
    win: (as) => as.find(x => /setup.*\.exe$/i.test(x.name)) || as.find(x => /\.exe$/i.test(x.name)),
    mac: (as) => as.find(x => new RegExp('mac-' + (arm ? 'arm64' : 'x64') + '\\.dmg$', 'i').test(x.name)) || as.find(x => /\.dmg$/i.test(x.name)),
    linux: (as) => as.find(x => /\.AppImage$/i.test(x.name))
  }[os] || null;
  try {
    const r = await fetch('https://api.github.com/repos/' + repo + '/releases/latest', { headers: { 'User-Agent': 'yaclient-site', Accept: 'application/vnd.github+json' }, cf: { cacheTtl: 120, cacheEverything: true } });
    if (r.ok && pick) {
      const j = await r.json();
      const a = pick(j.assets || []);
      if (a) return Response.redirect(a.browser_download_url, 302);
    }
  } catch (e) {}
  return Response.redirect('https://github.com/' + repo + '/releases/latest', 302);
}
