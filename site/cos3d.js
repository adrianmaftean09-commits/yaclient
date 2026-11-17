/* YA 3D: renders the real YA CLIENT cosmetics (geometry exported from the game code, see geo.js) on a 3D mannequin. No libraries. */
(function () {
  var D = Math.PI / 180;
  var VS = 'attribute vec3 p;attribute vec4 c;attribute vec2 t;uniform mat4 m;varying vec4 vc;varying vec2 vt;void main(){vc=c;vt=t;gl_Position=m*vec4(p,1.);}';
  var FS = 'precision mediump float;varying vec4 vc;varying vec2 vt;uniform sampler2D s;uniform float ut;void main(){vec4 col=vc;if(ut>.5)col*=texture2D(s,vt);gl_FragColor=col;}';

  // ---- tiny matrix library (column-major) ----
  function I() { return [1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1]; }
  function mul(a, b) { var o = new Array(16); for (var c = 0; c < 4; c++) for (var r = 0; r < 4; r++) { var s = 0; for (var k = 0; k < 4; k++) s += a[k * 4 + r] * b[c * 4 + k]; o[c * 4 + r] = s; } return o; }
  function T(x, y, z) { var m = I(); m[12] = x; m[13] = y; m[14] = z; return m; }
  function S(x, y, z) { var m = I(); m[0] = x; m[5] = y; m[10] = z; return m; }
  function RX(a) { var c = Math.cos(a), s = Math.sin(a), m = I(); m[5] = c; m[6] = s; m[9] = -s; m[10] = c; return m; }
  function RY(a) { var c = Math.cos(a), s = Math.sin(a), m = I(); m[0] = c; m[2] = -s; m[8] = s; m[10] = c; return m; }
  function RZ(a) { var c = Math.cos(a), s = Math.sin(a), m = I(); m[0] = c; m[1] = s; m[4] = -s; m[5] = c; return m; }
  function persp(fov, asp, n, f) { var t = 1 / Math.tan(fov / 2), m = new Array(16).fill(0); m[0] = t / asp; m[5] = t; m[10] = (f + n) / (n - f); m[11] = -1; m[14] = 2 * f * n / (n - f); return m; }
  function look(e, c) {
    var z = [e[0] - c[0], e[1] - c[1], e[2] - c[2]], l = Math.hypot(z[0], z[1], z[2]); z = z.map(function (v) { return v / l; });
    var x = [z[2], 0, -z[0]]; l = Math.hypot(x[0], x[1], x[2]); x = x.map(function (v) { return v / l; });
    var y = [z[1] * x[2] - z[2] * x[1], z[2] * x[0] - z[0] * x[2], z[0] * x[1] - z[1] * x[0]];
    return [x[0], y[0], z[0], 0, x[1], y[1], z[1], 0, x[2], y[2], z[2], 0, -(x[0] * e[0] + x[1] * e[1] + x[2] * e[2]), -(y[0] * e[0] + y[1] * e[1] + y[2] * e[2]), -(z[0] * e[0] + z[1] * e[1] + z[2] * e[2]), 1];
  }

  // ---- geometry helpers: stride 9 = x y z r g b a u v ----
  function quadsToTris(v, stride) {
    var n = v.length / stride, out = [];
    function push(i) { var o = i * stride; out.push(v[o], v[o + 1], v[o + 2], v[o + 3], v[o + 4], v[o + 5], v[o + 6], stride > 7 ? v[o + 7] : 0, stride > 7 ? v[o + 8] : 0); }
    for (var q = 0; q < n; q += 4) { push(q); push(q + 1); push(q + 2); push(q); push(q + 2); push(q + 3); }
    return new Float32Array(out);
  }
  var GEO = window.YA_GEO || {};
  function batches(key) { return GEO[key] || []; }

  // mannequin (model space: y down, 1 px = 1/16)
  function mannequin() {
    var out = [];
    function face(a, b, c, d, col, k) { [a, b, c, a, c, d].forEach(function (p) { out.push(p[0], p[1], p[2], col[0] * k, col[1] * k, col[2] * k, 1, 0, 0); }); }
    function box(x0, y0, z0, x1, y1, z1, col) {
      x0 /= 16; x1 /= 16; y0 /= 16; y1 /= 16; z0 /= 16; z1 /= 16;
      face([x0, y0, z0], [x1, y0, z0], [x1, y1, z0], [x0, y1, z0], col, .93);   // front (-z)
      face([x0, y0, z1], [x1, y0, z1], [x1, y1, z1], [x0, y1, z1], col, .72);   // back (+z)
      face([x0, y0, z0], [x0, y0, z1], [x0, y1, z1], [x0, y1, z0], col, .8);
      face([x1, y0, z0], [x1, y0, z1], [x1, y1, z1], [x1, y1, z0], col, .8);
      face([x0, y0, z0], [x1, y0, z0], [x1, y0, z1], [x0, y0, z1], col, 1);     // top (y up is -y)
      face([x0, y1, z0], [x1, y1, z0], [x1, y1, z1], [x0, y1, z1], col, .5);
    }
    var skin = [.74, .74, .78], torso = [.52, .52, .58], leg = [.4, .4, .46];
    box(-4, -8, -4, 4, 0, 4, skin);
    box(-4, 0, -2, 4, 12, 2, torso);
    box(-8, 0, -2, -4, 12, 2, skin); box(4, 0, -2, 8, 12, 2, skin);
    box(-4, 12, -2, 0, 24, 2, leg); box(0, 12, -2, 4, 24, 2, leg);
    // eyes
    function eye(x0, x1) { var z = -4.01 / 16; face([x0 / 16, -4.4 / 16, z], [x1 / 16, -4.4 / 16, z], [x1 / 16, -3.2 / 16, z], [x0 / 16, -3.2 / 16, z], [.08, .08, .1], 1); }
    eye(-2.8, -1.2); eye(1.2, 2.8);
    return new Float32Array(out);
  }

  var logoImg = null, logoReady = false;
  function loadLogo(cb) {
    if (logoImg) { if (logoReady) cb(); else logoImg.addEventListener('load', cb); return; }
    logoImg = new Image(); logoImg.onload = function () { logoReady = true; cb(); }; logoImg.src = '/logo.png';
  }

  var ITEM = { ya_cape: ['cape', 'ya'], ya_wings: ['wing', 'ya'], ya_bandana: ['bandana', 'ya'], ya_pet: ['pet', 'ya'],
               mono_cape: ['cape', 'mono'], mono_wings: ['wing', 'mono'], mono_bandana: ['bandana', 'mono'], mono_pet: ['pet', 'mono'] };

  function Scene(canvas, opts) {
    opts = opts || {};
    var gl = canvas.getContext('webgl', { alpha: true, premultipliedAlpha: false, antialias: true, preserveDrawingBuffer: !!opts.preserve }) || canvas.getContext('experimental-webgl');
    if (!gl) { this.failed = true; return; }
    var self = this; this.gl = gl; this.canvas = canvas;
    function sh(type, src) { var s = gl.createShader(type); gl.shaderSource(s, src); gl.compileShader(s); return s; }
    var pr = gl.createProgram(); gl.attachShader(pr, sh(gl.VERTEX_SHADER, VS)); gl.attachShader(pr, sh(gl.FRAGMENT_SHADER, FS)); gl.linkProgram(pr); gl.useProgram(pr);
    this.pr = pr; this.loc = { p: gl.getAttribLocation(pr, 'p'), c: gl.getAttribLocation(pr, 'c'), t: gl.getAttribLocation(pr, 't'), m: gl.getUniformLocation(pr, 'm'), ut: gl.getUniformLocation(pr, 'ut'), s: gl.getUniformLocation(pr, 's') };
    this.meshes = {}; this.items = []; this.plus = false; this.yaw = opts.yaw == null ? .5 : opts.yaw; this.pitch = opts.pitch == null ? .12 : opts.pitch;
    this.dist = opts.dist || 4.4; this.target = opts.target || [0, 1.0, 0]; this.fov = (opts.fov || 30) * D; this.auto = opts.auto !== false; this.t0 = performance.now();
    this.man = this.mesh(mannequin());
    var disc = [], N = 48; for (var i = 0; i < N; i++) { var a0 = i / N * 6.2832, a1 = (i + 1) / N * 6.2832;
      [[0, 0], [Math.cos(a0), Math.sin(a0)], [Math.cos(a1), Math.sin(a1)]].forEach(function (p) { disc.push(p[0] * .85, .002, p[1] * .85, 0, 0, 0, p[0] === 0 && p[1] === 0 ? .5 : 0, 0, 0); }); }
    this.shadow = this.mesh(new Float32Array(disc));
    this.tex = gl.createTexture();
    loadLogo(function () { gl.bindTexture(gl.TEXTURE_2D, self.tex); gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true); gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, logoImg);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE); self.texOk = true; });
    // drag to rotate
    if (opts.drag !== false) {
      var down = false, lx = 0, ly = 0;
      canvas.style.touchAction = 'none'; canvas.style.cursor = 'grab';
      canvas.addEventListener('pointerdown', function (e) { down = true; lx = e.clientX; ly = e.clientY; self.pause = true; canvas.setPointerCapture(e.pointerId); canvas.style.cursor = 'grabbing'; });
      canvas.addEventListener('pointermove', function (e) { if (!down) return; self.yaw -= (e.clientX - lx) * .009; self.pitch = Math.max(-.3, Math.min(.6, self.pitch + (e.clientY - ly) * .005)); lx = e.clientX; ly = e.clientY; });
      var up = function () { down = false; canvas.style.cursor = 'grab'; clearTimeout(self.rt); self.rt = setTimeout(function () { self.pause = false; }, 2200); };
      canvas.addEventListener('pointerup', up); canvas.addEventListener('pointercancel', up);
    }
  }
  Scene.prototype.mesh = function (arr, dynamic) { var gl = this.gl, b = gl.createBuffer(); gl.bindBuffer(gl.ARRAY_BUFFER, b); gl.bufferData(gl.ARRAY_BUFFER, arr, dynamic ? gl.DYNAMIC_DRAW : gl.STATIC_DRAW); return { b: b, n: arr.length / 9 }; };
  Scene.prototype.get = function (key, bi) {   // cached static mesh for batch bi of a geometry key
    var id = key + '#' + bi; if (!this.meshes[id]) { var bt = batches(key)[bi]; this.meshes[id] = bt ? { m: this.mesh(quadsToTris(bt.v, bt.t ? 9 : 7)), tex: !!bt.t } : { m: null }; } return this.meshes[id];
  };
  Scene.prototype.set = function (items, plus) { this.items = items.slice(); this.plus = !!plus; };
  Scene.prototype.drawMesh = function (m, mat, tex) {
    var gl = this.gl, L = this.loc; if (!m || !m.n) return;
    gl.bindBuffer(gl.ARRAY_BUFFER, m.b);
    gl.enableVertexAttribArray(L.p); gl.vertexAttribPointer(L.p, 3, gl.FLOAT, false, 36, 0);
    gl.enableVertexAttribArray(L.c); gl.vertexAttribPointer(L.c, 4, gl.FLOAT, false, 36, 12);
    gl.enableVertexAttribArray(L.t); gl.vertexAttribPointer(L.t, 2, gl.FLOAT, false, 36, 28);
    gl.uniformMatrix4fv(L.m, false, new Float32Array(mat)); gl.uniform1f(L.ut, tex ? 1 : 0);
    gl.drawArrays(gl.TRIANGLES, 0, m.n);
  };
  // lerp the animated plus cape between exported frames
  Scene.prototype.plusCape = function (age) {
    var F = batches('cape_ya_plus'), n = F.length, f = ((age % 64) / 64) * n, i = Math.floor(f) % n, j = (i + 1) % n, k = f - Math.floor(f);
    var A = F[i][0], B = F[j][0], out = new Float32Array(A.v.length / 7 * 9);
    if (!this.pcBuf) this.pcBuf = this.mesh(out, true);
    var src = quadsToTris(A.v, 7), dst = quadsToTris(B.v, 7);
    for (var q = 0; q < src.length; q++) { if (q % 9 > 6) continue; src[q] = src[q] + (dst[q] - src[q]) * k; }
    var gl = this.gl; gl.bindBuffer(gl.ARRAY_BUFFER, this.pcBuf.b); gl.bufferData(gl.ARRAY_BUFFER, src, gl.DYNAMIC_DRAW); this.pcBuf.n = src.length / 9;
    var em = F[Math.round(f) % n][1];
    if (em) { var id = 'plusem#' + (Math.round(f) % n); if (!this.meshes[id]) this.meshes[id] = { m: this.mesh(quadsToTris(em.v, 9)) }; this.pcEm = this.meshes[id].m; } else this.pcEm = null;
  };
  Scene.prototype.frame = function (now) {
    var gl = this.gl, c = this.canvas, dpr = Math.min(window.devicePixelRatio || 1, 2);
    var w = Math.round(c.clientWidth * dpr) || c.width, h = Math.round(c.clientHeight * dpr) || c.height;
    if (c.width !== w || c.height !== h) { c.width = w; c.height = h; }
    gl.viewport(0, 0, c.width, c.height); gl.clearColor(0, 0, 0, 0); gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    gl.enable(gl.DEPTH_TEST); gl.enable(gl.BLEND); gl.blendFuncSeparate(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA, gl.ONE, gl.ONE_MINUS_SRC_ALPHA); gl.disable(gl.CULL_FACE);
    var t = (now - this.t0) / 1000, age = t * 20;
    if (this.auto && !this.pause) this.yaw += .006;
    var ey = this.target[1] + Math.sin(this.pitch) * this.dist, hd = Math.cos(this.pitch) * this.dist;
    var eye = [this.target[0] + Math.sin(this.yaw) * hd, ey, this.target[2] + Math.cos(this.yaw) * hd];
    var VP = mul(persp(this.fov, c.width / c.height, .1, 50), look(eye, this.target));
    gl.useProgram(this.pr); gl.activeTexture(gl.TEXTURE0); gl.bindTexture(gl.TEXTURE_2D, this.tex); gl.uniform1i(this.loc.s, 0);
    this.drawMesh(this.shadow, VP, false);
    var base = mul(VP, mul(T(0, 1.5, 0), S(1, -1, 1)));   // model space (y down) -> world (y up), feet on the floor
    this.drawMesh(this.man, base, false);
    var items = this.items, self = this, has = function (id) { return items.indexOf(id) >= 0; };
    items.forEach(function (id) {
      var d = ITEM[id]; if (!d) return; var kind = d[0], mono = d[1] === 'mono';
      if (kind === 'wing') {
        var flap = Math.sin(age * .10) * (mono ? 17 : 7), raise = (mono ? -6 : -2) + flap, spread = 40;
        var g = self.get('wing_' + (mono ? 'mono' : 'ya'), 0);
        [-1, 1].forEach(function (s) {
          var m = mul(base, mul(T(s * .07, .08, .19), mul(RZ(-s * raise * D), mul(RY(-s * spread * D), S(s, -1, 1)))));
          // the geometry may be split into several batches
          for (var b = 0; b < batches('wing_' + (mono ? 'mono' : 'ya')).length; b++) { var gg = self.get('wing_' + (mono ? 'mono' : 'ya'), b); self.drawMesh(gg.m, m, gg.tex); }
        });
      } else if (kind === 'cape') {
        var m = mul(base, mul(T(0, 0, .125), RX(6 * D)));
        if (!mono && self.plus && GEO.cape_ya_plus) { self.plusCape(age); self.drawMesh(self.pcBuf, m, false); if (self.pcEm && self.texOk) self.drawMesh(self.pcEm, m, true); }
        else { var key = mono ? 'cape_mono' : 'cape_ya'; for (var b = 0; b < batches(key).length; b++) { var gg = self.get(key, b); if (gg.tex && !self.texOk) continue; self.drawMesh(gg.m, m, gg.tex); } }
      } else if (kind === 'bandana') {
        var key = mono ? 'bandana_mono' : 'bandana_ya'; for (var b = 0; b < batches(key).length; b++) { var gg = self.get(key, b); self.drawMesh(gg.m, base, gg.tex); }
      } else if (kind === 'pet') {
        var bob = Math.sin(age * .10) * .035, sx = Math.sin(age * .05) * .04, sz = Math.cos(age * .04) * .05;
        var rot = mono ? age * 6 * D : Math.sin(age * .05) * 12 * D;
        var m = mul(base, mul(T(-.64 + sx, -.20 + bob, .04 + sz), RY(rot)));
        var key = mono ? 'pet_mono' : 'pet_ya'; for (var b = 0; b < batches(key).length; b++) { var gg = self.get(key, b); self.drawMesh(gg.m, m, gg.tex); }
      }
    });
  };
  Scene.prototype.start = function () {
    var self = this; if (this.running) return; this.running = true;
    (function loop(now) { if (!self.running) return; if (self.canvas.isConnected && (self.canvas.getBoundingClientRect().bottom > 0)) self.frame(now); requestAnimationFrame(loop); })(performance.now());
  };

  // render one still image (used for the product cards)
  var snapScene = null, snapCanvas = null;
  function snapshot(items, o) {
    o = o || {};
    if (!snapCanvas) { snapCanvas = document.createElement('canvas'); snapCanvas.width = 560; snapCanvas.height = 420; snapCanvas.style.cssText = 'position:fixed;left:-9999px;width:560px;height:420px'; document.body.appendChild(snapCanvas); snapScene = new Scene(snapCanvas, { preserve: true, drag: false, auto: false }); }
    var s = snapScene; if (s.failed) return '';
    s.yaw = o.yaw == null ? .5 : o.yaw; s.pitch = o.pitch == null ? .1 : o.pitch; s.dist = o.dist || 4; s.target = o.target || [0, 1, 0]; s.fov = (o.fov || 30) * D;
    s.set(items, o.plus); s.t0 = performance.now() - (o.time || 1.2) * 1000; s.frame(performance.now());
    return snapCanvas.toDataURL('image/png');
  }

  window.YA3D = {
    ok: function () { try { var c = document.createElement('canvas'); return !!(c.getContext('webgl') || c.getContext('experimental-webgl')); } catch (e) { return false; } },
    viewer: function (canvas, opts) { var s = new Scene(canvas, opts); if (!s.failed) s.start(); return s; },
    snapshot: snapshot,
    whenReady: function (cb) { loadLogo(function () { setTimeout(cb, 30); }); }
  };
})();
