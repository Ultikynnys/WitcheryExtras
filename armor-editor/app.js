'use strict';
/* WitcheryExtras Beast Armor Editor
   - CSS-3D viewport of the beast model (bone hierarchy mirrors ModelWolfman)
   - Armor shells parented to bones, resizable, per-shell UV offsets, layer 1/2
   - Side-by-side pixel UV editor with paint/erase, PNG load/export, opacity validation
   - JSON import/export + Java codegen for ModelWolfmanArmor */

const TEX_W = 64, TEX_H = 32;
const SLOTS = ['helmet', 'chest', 'legs', 'boots'];

/* ---------- beast bone hierarchy (ModelWolfman geometry, MC coords +Y down) ---------- */
const BONES = {
  headMain:     { parent: null,      rot: [0, 0, -2],     baseRotX: 0 },
  bodyUpper:    { parent: null,      rot: [0, -0.1, -2],  baseRotX: 0.4098033 },
  bodyLowerFur: { parent: 'bodyUpper', rot: [0, 5, -1.5], baseRotX: 0 },
  legRightUpper:{ parent: null,      rot: [-2, 12, 0],    baseRotX: -0.4098033 },
  legLeftUpper: { parent: null,      rot: [2, 12, 0],     baseRotX: -0.4098033 },
  armRight:     { parent: null,      rot: [-5.8, 2, 0],   baseRotX: 0 },
  armLeft:      { parent: null,      rot: [6, 2, 0],      baseRotX: 0 },
  tail:         { parent: null,      rot: [0, 11.9, 3.6], baseRotX: 0.59184116 },
};

/* fur boxes: [bone, name, x,y,z, w,h,d, uv u,v, mirror] — from ModelWolfman 64x64 skin.
   NOTE: bone pivots define the limb origin; boxes are local to their bone, so left leg boxes
   use the same local coords as the right (mirroring is handled by mirror + pivot X). */
const FUR = [
  ['headMain', 'skull', -3, -6, -2, 6, 6, 4, 0, 0, false],
  ['headMain', 'earL', -3, -8, 1, 2, 2, 1, 16, 14, false],
  ['headMain', 'earR', 1, -8, 1, 2, 2, 1, 16, 14, false],
  ['headMain', 'snout', -1.5, -3.1, -5, 3, 3, 4, 0, 10, false],
  ['bodyUpper', 'chest', -5, 0, -3.9, 10, 7, 8, 0, 35, false],
  ['bodyLowerFur', 'belly', -4, 2, -2.3, 8, 7, 5, 3, 50, false],
  ['legRightUpper', 'thighR', -2, 0, -2, 4, 7, 4, 38, 0, false],
  ['legRightUpper', 'shinR', -2, 3.5, 2, 4, 8, 4, 38, 13, false],
  ['legLeftUpper', 'thighL', -2, 0, -2, 4, 7, 4, 38, 0, true],
  ['legLeftUpper', 'shinL', -2, 3.5, 2, 4, 8, 4, 38, 13, true],
  ['armRight', 'armR', -3, -2, -2, 4, 14, 4, 38, 46, false],
  ['armLeft', 'armL', -1, -2, -2, 4, 14, 4, 38, 46, true],
  ['tail', 'tailFur', -1, 0, -1, 2, 10, 2, 55, 52, false],
];

/* default shells mirror the current ModelWolfmanArmor.java */
const DEFAULT_SHELLS = [
  { id: 'skullShell', parent: 'headMain', layer: 1, uv: [4, 4], pos: [-3, -6, -2], size: [6, 6, 4], rot: [0, 0, 0], inflate: 0.5, slots: ['helmet'] },
  { id: 'earShellL', parent: 'headMain', layer: 1, uv: [16, 20], pos: [-3, -8, 1], size: [2, 2, 1], rot: [0, 0, 0], inflate: 0.5, slots: ['helmet'] },
  { id: 'earShellR', parent: 'headMain', layer: 1, uv: [16, 20], pos: [1, -8, 1], size: [2, 2, 1], rot: [0, 0, 0], inflate: 0.5, slots: ['helmet'] },
  { id: 'snoutShell', parent: 'headMain', layer: 1, uv: [16, 8], pos: [-1.5, -3.1, -5], size: [3, 3, 4], rot: [0, 0, 0], inflate: 0.55, slots: ['helmet'] },
  { id: 'torsoTop', parent: 'bodyUpper', layer: 1, uv: [16, 20], pos: [-4, -1.5, -4.4], size: [8, 4, 4], rot: [0, 0, 0], inflate: 1.3, slots: ['chest'] },
  { id: 'torsoFrontLow', parent: 'bodyUpper', layer: 1, uv: [16, 20], pos: [-4.1, 2, -4.55], size: [8, 5, 4], rot: [0, 0, 0], inflate: 1.3, slots: ['chest'] },
  { id: 'torsoBackTop', parent: 'bodyUpper', layer: 1, uv: [16, 20], pos: [-4.05, -1.55, 0.55], size: [8, 4, 4], rot: [0, 0, 0], inflate: 1.3, slots: ['chest'] },
  { id: 'torsoBackLow', parent: 'bodyUpper', layer: 1, uv: [16, 20], pos: [-3.95, 2.05, 0.4], size: [8, 5, 4], rot: [0, 0, 0], inflate: 1.3, slots: ['chest'] },
  { id: 'pelvisFrontLow', parent: 'bodyLowerFur', layer: 2, uv: [16, 27], pos: [-4, 0.9, -2.6], size: [8, 2, 3], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
  { id: 'pelvisRearLow', parent: 'bodyLowerFur', layer: 2, uv: [16, 27], pos: [-3.9, 0.95, 0.2], size: [8, 2, 3], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
  { id: 'pelvisFrontMid', parent: 'bodyLowerFur', layer: 2, uv: [16, 27], pos: [-4.1, 3, -2.72], size: [8, 2, 3], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
  { id: 'pelvisRearMid', parent: 'bodyLowerFur', layer: 2, uv: [16, 27], pos: [-4, 3.05, 0.32], size: [8, 2, 3], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
  { id: 'pelvisFrontHigh', parent: 'bodyLowerFur', layer: 2, uv: [16, 27], pos: [-4, 5.1, -2.84], size: [8, 2, 3], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
  { id: 'pelvisRearHigh', parent: 'bodyLowerFur', layer: 2, uv: [16, 27], pos: [-4.1, 5.15, 0.44], size: [8, 2, 3], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
  { id: 'pelvisFrontLowest', parent: 'bodyLowerFur', layer: 2, uv: [16, 27], pos: [-4.05, 7.2, -2.96], size: [8, 2, 3], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
  { id: 'pelvisRearLowest', parent: 'bodyLowerFur', layer: 2, uv: [16, 27], pos: [-3.95, 7.25, 0.56], size: [8, 2, 3], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
  { id: 'armShellR', parent: 'armRight', layer: 1, uv: [40, 16], pos: [-3, -2, -2], size: [4, 12, 4], rot: [0, 0, 0], inflate: 1.0, slots: ['chest'] },
  { id: 'armShellL', parent: 'armLeft', layer: 1, uv: [40, 16], pos: [-1, -2, -2], size: [4, 12, 4], rot: [0, 0, 0], inflate: 1.0, slots: ['chest'] },
  { id: 'thighShellR', parent: 'legRightUpper', layer: 2, uv: [0, 20], pos: [-2, -0.5, -2], size: [4, 5, 4], rot: [0, 0, 0], inflate: 0.7, slots: ['legs'] },
  { id: 'thighShellL', parent: 'legLeftUpper', layer: 2, uv: [0, 20], pos: [-2, -0.5, -2], size: [4, 5, 4], rot: [0, 0, 0], inflate: 0.7, slots: ['legs'] },
  { id: 'kneeShellR', parent: 'legRightUpper', layer: 2, uv: [0, 20], pos: [-2.15, 4.2, -1.85], size: [4, 3, 4], rot: [0, 0, 0], inflate: 0.7, slots: ['legs'] },
  { id: 'kneeShellL', parent: 'legLeftUpper', layer: 2, uv: [0, 20], pos: [-2.15, 4.2, -1.85], size: [4, 3, 4], rot: [0, 0, 0], inflate: 0.7, slots: ['legs'] },
  { id: 'footShellR', parent: 'legRightUpper', layer: 1, uv: [16, 20], pos: [-2, 3.5, 2], size: [4, 8, 4], rot: [0, 0, 0], inflate: 0.7, slots: ['boots'] },
  { id: 'footShellL', parent: 'legLeftUpper', layer: 1, uv: [16, 20], pos: [-2, 3.5, 2], size: [4, 8, 4], rot: [0, 0, 0], inflate: 0.7, slots: ['boots'] },
  { id: 'tailBaseShell', parent: 'tail', layer: 2, uv: [0, 20], pos: [-1, 0, -1], size: [2, 5, 2], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
  { id: 'tailTipShell', parent: 'tail', layer: 2, uv: [0, 20], pos: [-1.15, 5, -1.05], size: [2, 5, 2], rot: [0, 0, 0], inflate: 0.8, slots: ['legs'] },
];

/* ---------- state ---------- */
const state = {
  unit: 13, zoom: 1, orbitX: -15, orbitY: 30, pan: [0, 0],
  showFur: true, showL1: true, showL2: true,
  anim: 'idle',
  uvGrid: true, uvBg: '#111318', uvShowShells: true,
  selShell: null, selBone: null,
  curLayer: 1, visibleSlots: [],
  uvZoom: 8, uvPan: [0, 0],
  gizmoMode: 'translate', rotRing: null, selFace: null, mirrorAxis: 'x',
  shells: normalizeShells(JSON.parse(JSON.stringify(DEFAULT_SHELLS))),
  presets: {},   // name -> {1: canvas, 2: canvas}
  preset: 'placeholder',
};

/* ---------- helpers ---------- */
const $ = (s) => document.querySelector(s);
const rad = (d) => d * Math.PI / 180;
const el = (tag, cls, parent) => { const e = document.createElement(tag); if (cls) e.className = cls; if (parent) parent.appendChild(e); return e; };
function log(msg) { const l = $('#log'); l.textContent = msg; clearTimeout(log._t); log._t = setTimeout(() => l.textContent = '', 5000); }

/* every shell carries rot + uvScale defaults; uvScale = texel density (UV footprint per
   unit), independent of the mesh size so UV editing never resizes geometry. */
function normalizeShells(list) {
  for (const s of list) {
    if (!Array.isArray(s.rot)) s.rot = [0, 0, 0];
    if (typeof s.uvScale !== 'number' || !(s.uvScale > 0)) s.uvScale = 1;
    s.uv = [Math.round(s.uv[0] || 0), Math.round(s.uv[1] || 0)];
    if (!s.faceOffset || typeof s.faceOffset !== 'object') s.faceOffset = {};
    if (!s.faceScale || typeof s.faceScale !== 'object') s.faceScale = {};
    if (typeof s.uvMirror !== 'boolean') s.uvMirror = false;
  }
  return list;
}

function placeholderTex(kind) {
  const c = document.createElement('canvas'); c.width = TEX_W; c.height = TEX_H;
  const g = c.getContext('2d', { willReadFrequently: true });
  const hue = kind === 1 ? 210 : 280;
  for (let y = 0; y < TEX_H; y++) for (let x = 0; x < TEX_W; x++) {
    const light = (x + y) % 2 === 0;
    g.fillStyle = light ? `hsl(${hue},45%,45%)` : `hsl(${hue},45%,30%)`;
    g.fillRect(x, y, 1, 1);
  }
  g.fillStyle = '#000';
  for (let x = 0; x < TEX_W; x += 16) { g.fillRect(x, 0, 1, TEX_H); }
  for (let y = 0; y < TEX_H; y += 8) { g.fillRect(0, y, TEX_W, 1); }
  return c;
}
function getPreset(name) {
  if (!state.presets[name]) state.presets[name] = { 1: placeholderTex(1), 2: placeholderTex(2) };
  return state.presets[name];
}
function curTex() { return getPreset(state.preset)[state.curLayer]; }

function faceRects(uv, w, h, d) {
  const [u, v] = uv;
  return [
    { n: 'west', x: u, y: v + d, w: d, h },
    { n: 'east', x: u + d + w, y: v + d, w: d, h },
    { n: 'top', x: u + d, y: v, w, h: d },
    { n: 'bottom', x: u + d + w, y: v, w, h: d },
    { n: 'north', x: u + d, y: v + d, w, h },
    { n: 'south', x: u + d + w + d, y: v + d, w, h },
  ];
}
/* six faces of a shell's UV island: layout (with uvScale density and per-face offsets
   applied) as texel rects. Each face can be moved independently = separate UV islands. */
function shellRects(s) {
  const vs = s.uvScale || 1;
  const off = s.faceOffset || {};
  const fs = s.faceScale || {};
  // base layout dims snapped to whole texels so islands always sit on the grid
  const wT = Math.max(1, Math.round(s.size[0] * vs));
  const hT = Math.max(1, Math.round(s.size[1] * vs));
  const dT = Math.max(1, Math.round(s.size[2] * vs));
  const u0 = Math.round(s.uv[0]), v0 = Math.round(s.uv[1]);
  return faceRects([u0, v0], wT, hT, dT).map((r) => {
    const o = off[r.n] || [0, 0];
    const raw = fs[r.n];
    const k = Array.isArray(raw) ? raw : [typeof raw === 'number' ? raw : 1, typeof raw === 'number' ? raw : 1];
    // snap to whole texels so every island edge sits on the grid
    const rw = Math.max(1, Math.round(r.w * k[0]));
    const rh = Math.max(1, Math.round(r.h * k[1]));
    return { n: r.n, x: Math.round(r.x) + Math.round(o[0]), y: Math.round(r.y) + Math.round(o[1]), w: rw, h: rh };
  });
}
function faceUVMap(s) {
  const m = {};
  for (const r of shellRects(s)) m[r.n] = { u: r.x, v: r.y, w: r.w, h: r.h };
  return m;
}
/* a face's UV island rect WITHOUT faceScale (the base the resize drag snaps against) */
function baseFaceRect(s, name) {
  const vs = s.uvScale || 1;
  const wT = Math.max(1, Math.round(s.size[0] * vs));
  const hT = Math.max(1, Math.round(s.size[1] * vs));
  const dT = Math.max(1, Math.round(s.size[2] * vs));
  return faceRects([Math.round(s.uv[0]), Math.round(s.uv[1])], wT, hT, dT).find((r) => r.n === name);
}
function boxOpaque(uv, w, h, d, tex) {
  if (!tex) return true;
  const g = tex.getContext('2d');
  for (const f of faceRects(uv, w, h, d)) {
    const img = g.getImageData(f.x, f.y, f.w, f.h);
    for (let i = 3; i < img.data.length; i += 4) if (img.data[i] < 8) return false;
  }
  return true;
}

/* ---------- 3D viewport ---------- */
const vp = $('#viewport');
const world = el('div', '', vp);
world.style.cssText = 'position:absolute;left:50%;top:55%;transform-style:preserve-3d;';
vp.insertBefore(world, vp.firstChild.nextSibling);
const boneEls = {}, boneState = {};

for (const [id, b] of Object.entries(BONES)) {
  const d = el('div', '', world.parentElement === world ? world : world);
  d.style.cssText = 'position:absolute;transform-style:preserve-3d;left:0;top:0;';
  boneEls[id] = d;
  boneState[id] = { rx: b.baseRotX, walk: 0 };
  (b.parent ? boneEls[b.parent] : world).appendChild(d);
}
/* order: children must be inside parent's transformed frame — re-parent properly */
function ensureBoneHierarchy() {
  for (const [id, b] of Object.entries(BONES)) {
    const host = b.parent ? boneEls[b.parent] : world;
    if (boneEls[id].parentElement !== host) host.appendChild(boneEls[id]);
  }
}
for (const [id, b] of Object.entries(BONES)) {
  (b.parent ? boneEls[b.parent] : world).appendChild(boneEls[id]);
}

function mcToCss(x, y, z) { return [x, y, -z]; }

function addBox(parentEl, opts) {
  const { x, y, z, w, h, d, uvU, uvV, tex, alpha, clickable, shellId, mirror, texW, texH, boneId, rot, uvScale, faceUV, uvMirror } = opts;
  const U = state.unit;
  const TW = texW || TEX_W, TH = texH || TEX_H;
  const cx = (x + w / 2) * U, cy = (y + h / 2) * U, cz = -(z + d / 2) * U;
  const wrap = el('div', '', parentEl);
  const rr = rot ? `${-rot[1] * 180 / Math.PI}deg ${-rot[2] * 180 / Math.PI}deg ${-rot[0] * 180 / Math.PI}deg` : '';
  wrap.style.cssText = `position:absolute;left:0;top:0;transform-style:preserve-3d;transform:translate3d(${cx}px,${cy}px,${cz}px)` + (rr ? ` rotateY(${rr.split(' ')[0]}) rotateZ(${rr.split(' ')[1]}) rotateX(${rr.split(' ')[2]});` : ';');
  const W = w * U, H = h * U, D = d * U;
  const F = faceUV || null;
  const layout = {};
  for (const r of faceRects([uvU, uvV], w, h, d)) layout[r.n] = { u: r.x, v: r.y, w: r.w, h: r.h };
  const faces = [
    { n: 'north', w: W, h: H, t: `translateZ(${D / 2}px)` },
    { n: 'south', w: W, h: H, t: `rotateY(180deg) translateZ(${D / 2}px)` },
    { n: 'west', w: D, h: H, t: `rotateY(-90deg) translateZ(${W / 2}px)` },
    { n: 'east', w: D, h: H, t: `rotateY(90deg) translateZ(${W / 2}px)` },
    { n: 'top', w: W, h: D, t: `rotateX(90deg) translateZ(${H / 2}px)` },
    { n: 'bottom', w: W, h: D, t: `rotateX(-90deg) translateZ(${H / 2}px)` },
  ];
  for (const f of faces) {
    const rect = (F && F[f.n]) ? F[f.n] : layout[f.n];
    const Sx = f.w / Math.max(0.001, rect.w);   // px per texel on this face (x)
    const Sy = f.h / Math.max(0.001, rect.h);
    let tf = f.t;
    if (mirror && (f.n === 'east' || f.n === 'west')) tf = `rotateY(${f.n === 'east' ? -90 : 90}deg) translateZ(${W / 2}px)`;
    if (uvMirror) tf += ' scaleX(-1)';          // reflect the sampled texture horizontally
    const fe = el('div', '', wrap);
    fe.style.cssText = `position:absolute;left:${-f.w / 2}px;top:${-f.h / 2}px;width:${f.w}px;height:${f.h}px;` +
      `transform:${tf};backface-visibility:visible;` +
      `background-image:url(${tex});background-size:${TW * Sx}px ${TH * Sy}px;image-rendering:pixelated;` +
      `background-position:${-rect.u * Sx}px ${-rect.v * Sy}px;opacity:${alpha};`;
    fe.dataset.bone = boneId;
    if (clickable) {
      fe.dataset.shell = shellId; fe.dataset.face = f.n;
      fe.addEventListener('click', (ev) => {
        ev.stopPropagation();
        selectShell(shellId);
        if (ev.shiftKey) {
          state.selFace = { shell: shellId, face: f.n };
          drawUV(); refreshSelHighlight();
          log('Face: ' + shellId + '.' + f.n);
        }
      });
    }
  }
  return wrap;
}

/* ---------- texture data-URL cache (paint invalidates) ---------- */
const dataURLCache = { key: '', urls: {} };
function presetDataURL(layer) {
  const key = state.preset + ':' + layer;
  if (dataURLCache.key !== key) {
    dataURLCache.key = key;
    dataURLCache.url = getPreset(state.preset)[layer].toDataURL();
  }
  return dataURLCache.url;
}

/* ---------- transform gizmo (billboard projected to screen space) ---------- */
let gizmoScreenAnchor = null;

/* ---------- gizmo picking (screen-space, orientation-independent) ---------- */
function probeScreen(boneEl, x, y, z) {
  const p = document.createElement('div');
  p.style.cssText = `position:absolute;left:0;top:0;width:1px;height:1px;visibility:hidden;` +
    `transform:translate3d(${x}px,${y}px,${z}px);`;
  boneEl.appendChild(p);
  const r = p.getBoundingClientRect();
  p.remove();
  return [r.left + 0.5, r.top + 0.5];
}

function distSeg(px, py, a, b) {
  const dx = b[0] - a[0], dy = b[1] - a[1];
  const L2 = dx * dx + dy * dy || 1;
  let t = ((px - a[0]) * dx + (py - a[1]) * dy) / L2;
  t = Math.max(0, Math.min(1, t));
  return Math.hypot(px - (a[0] + t * dx), py - (a[1] + t * dy));
}

function perpBasis(dir) {
  const cross = (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]];
  const norm = (a) => { const l = Math.hypot(a[0], a[1], a[2]) || 1; return [a[0] / l, a[1] / l, a[2] / l]; };
  const seed = Math.abs(dir[0]) < 0.9 ? [1, 0, 0] : [0, 1, 0];
  const u = norm(cross(dir, seed));
  return [u, norm(cross(dir, u))];
}

function pickGizmo(cx, cy) {
  if (!gizmoAnchor || !gizmo3d.parentElement) return null;
  const live = state.shells.find((s) => s.id === gizmoAnchor.shell.id);
  if (!live) return null;
  const U = state.unit;
  const bone = boneEls[live.parent];
  const c = [(live.pos[0] + live.size[0] / 2) * U, (live.pos[1] + live.size[1] / 2) * U, -(live.pos[2] + live.size[2] / 2) * U];
  const fw = cameraForwardInBone(bone);
  const K = 900;
  const o = [c[0] + fw[0] * K, c[1] + fw[1] * K, c[2] + fw[2] * K];
  const origin = probeScreen(bone, o[0], o[1], o[2]);
  const mode = state.gizmoMode;
  const L = mode === 'rotate' ? GIZMO_L * 1.4 : GIZMO_L + GIZMO_STICK;
  let best = null;
  const dirOf = (axis) => { const m = axisDirBoneMC(axis, live); return [m[0], m[1], -m[2]]; };
  if (mode === 'rotate') {
    for (const axis of ['x', 'y', 'z']) {
      const [u, v] = perpBasis(dirOf(axis));
      let prev = null;
      for (let i = 0; i <= 16; i++) {
        const a = (i / 16) * Math.PI * 2, cs = Math.cos(a), sn = Math.sin(a);
        const p = [o[0] + (u[0] * cs + v[0] * sn) * L, o[1] + (u[1] * cs + v[1] * sn) * L, o[2] + (u[2] * cs + v[2] * sn) * L];
        const s = probeScreen(bone, p[0], p[1], p[2]);
        if (prev) { const d = distSeg(cx, cy, prev, s); if (d < 16 && (!best || d < best.dist)) best = { axis: 'r' + axis, dist: d }; }
        prev = s;
      }
    }
  } else {
    for (const axis of ['x', 'y', 'z']) {
      const dir = dirOf(axis);
      const tip = probeScreen(bone, o[0] + dir[0] * L, o[1] + dir[1] * L, o[2] + dir[2] * L);
      const d = distSeg(cx, cy, origin, tip);
      if (d < 20 && (!best || d < best.dist)) best = { axis: (mode === 'scale' ? 'p' : '') + axis, dist: d };
    }
  }
  // gizmo takes priority across its whole area — never let a click inside it orbit the camera
  const near = Math.hypot(cx - origin[0], cy - origin[1]) < L + 26;
  if (!best && near) best = { axis: mode === 'scale' ? 'su' : 'c', dist: 0 };
  return best;
}

/* gizmo drag handling lives on window so drags continue outside the viewport */
function setScaled(live, orig, newSize) {
  const c = [orig.pos[0] + orig.size[0] / 2, orig.pos[1] + orig.size[1] / 2, orig.pos[2] + orig.size[2] / 2];
  live.size = newSize;
  live.pos = [+(c[0] - newSize[0] / 2).toFixed(2), +(c[1] - newSize[1] / 2).toFixed(2), +(c[2] - newSize[2] / 2).toFixed(2)];
}

window.addEventListener('mousemove', (e) => {
  const g = vp._gizmoDrag;
  if (!g) return;
  const s = g.shell;
  const tdx = e.clientX - g.sx, tdy = e.clientY - g.sy;   // total delta from drag start
  const live = state.shells.find((x) => x.id === s.id);
  if (!live) { vp._gizmoDrag = null; return; }
  const U = state.unit * state.zoom;
  if (g.mode === 'ring') {
    const liveR = state.shells.find((x) => x.id === g.shell.id);
    if (!liveR) { vp._gizmoDrag = null; return; }
    const a = Math.atan2(e.clientY - g.cy, e.clientX - g.cx);
    let d = a - g.lastAngle;
    if (d > Math.PI) d -= Math.PI * 2; else if (d < -Math.PI) d += Math.PI * 2;
    g.accum += d; g.lastAngle = a;
    const sign = g.axis === 'y' ? -1 : 1;
    const i = idxOf(g.axis);
    liveR.rot = g.orig.rot.map((v, k) => (k === i ? snapRot(g.orig.rot[i] + g.accum * sign, e.shiftKey) : v));
    buildScene(); renderShellProps();
    return;
  }
  if (g.axis === 'x' || g.axis === 'y' || g.axis === 'z') {
    const [dirx, diry] = axisScreenDirLocal(g.axis, live);
    const d = (tdx * dirx + tdy * diry) / U;
    const bd = axisDirBoneMC(g.axis, live);
    live.pos[0] = +(g.orig.pos[0] + bd[0] * d).toFixed(2);
    live.pos[1] = +(g.orig.pos[1] + bd[1] * d).toFixed(2);
    live.pos[2] = +(g.orig.pos[2] + bd[2] * d).toFixed(2);
  } else if (g.axis === 'px' || g.axis === 'py' || g.axis === 'pz') {
    const ax = g.axis[1];
    const [dirx, diry] = axisScreenDirLocal(ax, live);
    const d = Math.round((tdx * dirx + tdy * diry) / U);
    const i = dominantAxis(axisDirBoneMC(ax, live));
    const ns = g.orig.size.slice(); ns[i] = Math.max(1, g.orig.size[i] + d);
    setScaled(live, g.orig, ns);
  } else if (g.axis === 'rx' || g.axis === 'ry' || g.axis === 'rz') {
    const i = g.axis === 'rx' ? 0 : g.axis === 'ry' ? 1 : 2;
    const d = i === 0 ? tdy : i === 1 ? -tdx : tdx;
    live.rot = g.orig.rot.map((v, k) => (k === i ? snapRot(g.orig.rot[i] + d * 0.01, e.shiftKey) : v));
  } else if (g.axis === 'su') {
    const f = (tdx - tdy) / 220;
    setScaled(live, g.orig, g.orig.size.map((v) => Math.max(1, Math.round(v * (1 + f)))));
  } else if (g.axis === 'c') {
    live.pos[0] = +(g.orig.pos[0] + tdx / U).toFixed(2);
    live.pos[1] = +(g.orig.pos[1] + tdy / U).toFixed(2);
    live.pos[2] = g.orig.pos[2];
  }
  buildScene(); renderShellProps();
});
window.addEventListener('mouseup', () => {
  if (vp._gizmoDrag) { vp._gizmoDrag = null; document.body.classList.remove('gizmo-dragging'); renderShellList(); drawUV(); saveSession(); }
  if (state.modal && state.modal.pressed) endModal(true);
});

/* ---------- Blender-style modal transforms: G/S/R + X/Y/Z (axis) / Shift+X/Y/Z (plane) ---------- */
let lastMouse = { x: 0, y: 0 };
state.modal = null;

function idxOf(axis) { return axis === 'x' ? 0 : axis === 'y' ? 1 : 2; }
function dominantAxis(v) { let i = 0, best = -1; for (let k = 0; k < 3; k++) { const a = Math.abs(v[k]); if (a > best) { best = a; i = k; } } return i; }
function solve2(u, v, w) {
  const det = u[0] * v[1] - u[1] * v[0];
  if (Math.abs(det) < 1e-6) return [0, 0];
  return [(w[0] * v[1] - w[1] * v[0]) / det, (u[0] * w[1] - u[1] * w[0]) / det];
}

function startModal(op) {
  const live = state.shells.find((s) => s.id === state.selShell);
  if (!live) { log('Select a shell first'); return; }
  pushUndo();
  const vr = vp.getBoundingClientRect();
  const inside = lastMouse.x >= vr.left && lastMouse.x <= vr.right && lastMouse.y >= vr.top && lastMouse.y <= vr.bottom;
  const start = inside ? { ...lastMouse } : { x: vr.left + vr.width / 2, y: vr.top + vr.height / 2 };
  state.modal = {
    op, shell: live,
    axis: null, plane: null,
    start,
    orig: { pos: live.pos.slice(), size: live.size.slice(), rot: (live.rot || [0, 0, 0]).slice() },
    moved: false,
  };
  state.gizmoMode = op;
  const sel = $('#gizmoSel'); if (sel) sel.value = op;
  document.body.classList.add('gizmo-dragging');
  mountGizmo(boneEls[live.parent], live);
  modalStatus();
}

function endModal(commit) {
  const m = state.modal;
  if (!m) return;
  const live = state.shells.find((s) => s.id === m.shell.id);
  if (!commit && live) { live.pos = m.orig.pos.slice(); live.size = m.orig.size.slice(); live.rot = m.orig.rot.slice(); }
  state.modal = null;
  document.body.classList.remove('gizmo-dragging');
  buildScene(); renderShellProps(); renderShellList(); drawUV();
  $('#statSel').textContent = 'Shell: ' + (state.selShell || 'none');
}

function modalStatusRing(axis) {
  $('#statSel').textContent = axis
    ? `Rotate [${axis.toUpperCase()}] — drag the gizmo to rotate in 15° steps · X/Y/Z change axis · Shift = free`
    : 'Rotate (free) — press X/Y/Z to lock an axis · drag the gizmo to rotate · Shift = free';
}

function modalStatus() {
  const m = state.modal;
  if (!m) return;
  const lock = m.axis ? `  [${m.axis.toUpperCase()}]` : m.plane ? `  [plane ${['x', 'y', 'z'].filter((a) => a !== m.plane).join('').toUpperCase()}]` : '  [pick axis]';
  const labels = { translate: 'Move', scale: 'Scale', rotate: 'Rotate' };
  $('#statSel').textContent = `${labels[m.op]}${lock}  —  press X/Y/Z for an axis (Shift = plane) · LMB/Enter confirm · RMB/Esc cancel`;
}

function applyModal(mx, my, fine) {
  const m = state.modal;
  if (!m) return;
  // inert until an axis (X/Y/Z) or plane (Shift+X/Y/Z) is chosen
  if (!m.axis && !m.plane) return;
  const live = state.shells.find((s) => s.id === m.shell.id);
  if (!live) return;
  const dx = mx - m.start.x, dy = my - m.start.y;
  const U = state.unit * state.zoom;
  const uv = (axis) => axisScreenDirLocal(axis, live);
  const other2 = () => ['x', 'y', 'z'].filter((a) => a !== m.plane);

  if (m.op === 'translate') {
    if (m.axis) {
      const [ux, uy] = uv(m.axis);
      const t = (dx * ux + dy * uy) / U;
      const bd = axisDirBoneMC(m.axis, live);
      live.pos[0] = +(m.orig.pos[0] + bd[0] * t).toFixed(2);
      live.pos[1] = +(m.orig.pos[1] + bd[1] * t).toFixed(2);
      live.pos[2] = +(m.orig.pos[2] + bd[2] * t).toFixed(2);
    } else if (m.plane) {
      const [a, b] = other2();
      const [ca, cb] = solve2(uv(a), uv(b), [dx, dy]);
      const da = axisDirBoneMC(a, live), db = axisDirBoneMC(b, live);
      live.pos[0] = +(m.orig.pos[0] + (da[0] * ca + db[0] * cb) / U).toFixed(2);
      live.pos[1] = +(m.orig.pos[1] + (da[1] * ca + db[1] * cb) / U).toFixed(2);
      live.pos[2] = +(m.orig.pos[2] + (da[2] * ca + db[2] * cb) / U).toFixed(2);
    } else {
      live.pos[0] = +(m.orig.pos[0] + dx / U).toFixed(2);
      live.pos[1] = +(m.orig.pos[1] + dy / U).toFixed(2);
      live.pos[2] = m.orig.pos[2];
    }
  } else if (m.op === 'scale') {
    if (m.axis) {
      const [ux, uy] = uv(m.axis);
      const t = (dx * ux + dy * uy) / U;
      const i = dominantAxis(axisDirBoneMC(m.axis, live));
      const ns = m.orig.size.slice(); ns[i] = Math.max(1, Math.round(m.orig.size[i] + t));
      setScaled(live, m.orig, ns);
    } else if (m.plane) {
      const [a, b] = other2();
      const t = (dx - dy) / U;
      const ns = m.orig.size.slice();
      for (const ax of [a, b]) { const i = dominantAxis(axisDirBoneMC(ax, live)); ns[i] = Math.max(1, Math.round(m.orig.size[i] + t)); }
      setScaled(live, m.orig, ns);
    } else {
      const f = (dx - dy) / 220;
      setScaled(live, m.orig, m.orig.size.map((v) => Math.max(1, Math.round(v * (1 + f)))));
    }
  } else {
    const ax = m.axis || 'z';
    const delta = (ax === 'x' ? dy : ax === 'y' ? -dx : dx) * 0.01;
    const i = idxOf(ax);
    live.rot = m.orig.rot.map((v, k) => (k === i ? snapRot(v + delta, fine) : v));
  }
  m.moved = true;
  buildScene();
  renderShellProps();
}

window.addEventListener('mousemove', (e) => {
  lastMouse = { x: e.clientX, y: e.clientY };
  if (state.modal) applyModal(e.clientX, e.clientY, e.shiftKey);
});

/* hover feedback: show a handle-specific cursor over the gizmo */
vp.addEventListener('mousemove', (e) => {
  if (state.modal || vp._gizmoDrag || !gizmoAnchor) return;
  const pick = pickGizmo(e.clientX, e.clientY);
  if (pick) {
    const ax = pick.axis;
    vp.style.cursor = ax === 'su' ? 'nesw-resize' : ax === 'c' ? 'move' : ax[0] === 'r' ? 'grab' : ax[ax.length - 1] === 'x' ? 'ew-resize' : 'ns-resize';
  } else {
    vp.style.cursor = '';
  }
});

function buildScene() {
  for (const b of Object.values(boneEls)) b.innerHTML = '';
  for (const [bone, name, x, y, z, w, h, d, u, v, mirror] of FUR) {
    addBox(boneEls[bone], { x, y, z, w, h, d, uvU: u, uvV: v, tex: furTexURL, alpha: state.showFur ? 1 : 0, clickable: false, mirror, texW: 64, texH: 64, boneId: bone });
  }
  for (const s of state.shells) {
    const vis = s.slots.some((sl) => state.visibleSlots.includes(sl)) && (s.layer === 1 ? state.showL1 : state.showL2);
    if (!vis) continue;
    const tex = presetDataURL(s.layer);
    addBox(boneEls[s.parent], { x: s.pos[0], y: s.pos[1], z: s.pos[2], w: s.size[0], h: s.size[1], d: s.size[2], uvU: s.uv[0], uvV: s.uv[1], tex, alpha: 1, clickable: true, shellId: s.id, boneId: s.parent, rot: s.rot || [0, 0, 0], uvScale: s.uvScale || 1, faceUV: faceUVMap(s), uvMirror: !!s.uvMirror });
    if (s.id === state.selShell) mountGizmo(boneEls[s.parent], s);
  }
  applyBoneTransforms();
  ensureBoneHierarchy();
  refreshSelHighlight();
}

let furTexURL;
async function loadFurTex() {
  const c = document.createElement('canvas'); c.width = 64; c.height = 64;
  const g = c.getContext('2d', { willReadFrequently: true });
  try {
    const img = new Image();
    img.src = 'textures/wolfman_fur.png';
    await img.decode();
    g.drawImage(img, 0, 0);
  } catch (e) {
    for (let y = 0; y < 64; y++) for (let x = 0; x < 64; x++) {
      const n = Math.sin(x * 12.9898 + y * 78.233) * 43758.5453;
      const v = 90 + ((n - Math.floor(n)) * 40);
      g.fillStyle = `rgb(${v | 0},${v | 0},${(v + 6) | 0})`;
      g.fillRect(x, y, 1, 1);
    }
  }
  furTexURL = c.toDataURL();
}

/* Mirror of ModelWolfman.setRotationAngles (+ our tail mixin). Angles in radians, MC convention:
   standing legs pivot (Y 12, Z 0.1); sneak pivots (Y 9, Z 4), body 0.5, arms +0.4; tail mixin
   overrides pitch: idle 15deg, walk velocity-driven, sneak 70deg; vanilla Z sway retained. */
const TAIL_IDLE = 15 * Math.PI / 180;
const TAIL_SNEAK = 70 * Math.PI / 180;

function poseAngles(tick) {
  const P = {};
  for (const [id, b] of Object.entries(BONES)) {
    P[id] = { rx: b.baseRotX, ry: 0, rz: 0, rot: b.rot.slice() };
  }
  const sneak = state.anim === 'sneak';
  const walk = state.anim === 'walk';
  const swing = walk ? 0.8 : 0;
  const limb = tick * 0.55;

  for (const side of ['Right', 'Left']) {
    const id = 'leg' + side + 'Upper';
    const phase = side === 'Right' ? 0 : Math.PI;
    P[id].rx = Math.max(-0.4098033 + Math.cos(limb * 0.6662 + phase) * 1.4 * swing, -0.8);
    P[id].rot = [side === 'Right' ? -2 : 2, sneak ? 9 : 12, sneak ? 4 : 0.1];
  }
  P.bodyUpper.rx = sneak ? 0.5 : 0.4098033;

  const sway = walk ? 1 : 0;
  const armSwayZ = 0.05 + sway * Math.cos(tick * 0.09) * 0.05;
  const armSwayX = sway * Math.sin(tick * 0.067) * 0.05;
  P.armRight.rx += armSwayX;
  P.armRight.rz = armSwayZ;
  P.armLeft.rx -= armSwayX;
  P.armLeft.rz = -armSwayZ;
  if (sneak) { P.armRight.rx += 0.4; P.armLeft.rx += 0.4; }

  P.tail.rx = sneak ? TAIL_SNEAK : (walk ? TAIL_IDLE + (90 - 15) * Math.PI / 180 * 0.75 : TAIL_IDLE);
  P.tail.rz = 0.05 + sway * 3.0 * Math.cos(tick * 0.09) * 0.05;
  return P;
}

function applyBoneTransforms() {
  const U = state.unit;
  const tick = performance.now() / 50;
  const P = poseAngles(tick);
  lastPose = P;
  for (const [id] of Object.entries(BONES)) {
    const p = P[id];
    const [rx, ry, rz] = mcToCss(...p.rot);
    const dx = -p.rx * 180 / Math.PI, dy = -p.ry * 180 / Math.PI, dz = -p.rz * 180 / Math.PI;
    boneEls[id].style.transform =
      `translate3d(${rx * U}px,${ry * U}px,${rz * U}px) rotateY(${dy}deg) rotateZ(${dz}deg) rotateX(${dx}deg)`;
  }
  world.style.transform = `translate(${state.pan[0]}px,${state.pan[1]}px) translate(-50%,-50%) scale3d(${state.zoom},${state.zoom},${state.zoom}) rotateX(${state.orbitX}deg) rotateY(${state.orbitY}deg)`;
  updateGizmo();
}

/* Transform gizmo: rebuilt each frame as a screen-facing billboard anchored at the
   selected shell's bbox center (model coords, projected via the same orbit math as
   the CSS-3D world). Three positive axis lines with end caps; drag maps mouse delta
   onto the axis' on-screen direction. */
const AXIS_DIR = {
  x: [0, -1, 0],   // MC +X → screen left at orbitY=0; we compute per-frame below
  y: [0, -1, 0],
  z: [0, -1, 0],
};



/* ---------- true 3D gizmo: lives inside the bone's preserve-3d frame ---------- */
/* orientation: 'world' = bone axes (model space), 'local' = shell's own rot applied */
const gizmo3d = el('div', '', null);
gizmo3d.style.cssText = 'position:absolute;left:0;top:0;transform-style:preserve-3d;pointer-events:none;';
let gizmoAnchor = null;
let lastPose = null;
const GIZMO_STICK = 34;
const GIZMO_L = 52;

function rotVec(v, axis, deg) {
  const a = deg * Math.PI / 180, c = Math.cos(a), s = Math.sin(a);
  let [x, y, z] = v;
  if (axis === 'x') { const y2 = y * c - z * s, z2 = y * s + z * c; y = y2; z = z2; }
  else if (axis === 'y') { const x2 = x * c + z * s, z2 = -x * s + z * c; x = x2; z = z2; }
  else { const x2 = x * c - y * s, y2 = x * s + y * c; x = x2; y = y2; }
  return [x, y, z];
}

function shellRotDeg(r) { return [-r[1] * 180 / Math.PI, -r[2] * 180 / Math.PI, -r[0] * 180 / Math.PI]; }

/* camera-forward (CSS +z) expressed in the bone's local frame, derived from the real DOM
   transform chain so it is exact for any bone rotations. Pushing the gizmo along this
   moves it toward the viewer without changing its screen position (orthographic). */
function cameraForwardInBone(boneEl) {
  const chain = [];
  let e = boneEl;
  while (e && e !== document.body) { chain.unshift(e); e = e.parentElement; }
  let m = new DOMMatrix();
  for (const el of chain) {
    const t = getComputedStyle(el).transform;
    if (t && t !== 'none') m = m.multiply(new DOMMatrix(t));
  }
  const norm = (x, y, z) => { const l = Math.hypot(x, y, z) || 1; return [x / l, y / l, z / l]; };
  const c0 = norm(m.m11, m.m12, m.m13), c1 = norm(m.m21, m.m22, m.m23), c2 = norm(m.m31, m.m32, m.m33);
  // Rot^T · ẑ — the view-forward direction expressed in the bone's frame. Its image under
  // the (orthonormal) rotation is exactly +ẑ (toward the viewer), so pushing along it always
  // brings the gizmo in front without changing its screen position.
  return norm(c0[2], c1[2], c2[2]);
}

/* screen-space direction of a gizmo axis under the current view.
   WORLD: axis is a model axis → only view rotation applies.
   LOCAL: axis is the shell's own axis → shell rot, then the bone chain, then view. */
function axisScreenDirLocal(axis, shell) {
  let v = axis === 'x' ? [1, 0, 0] : axis === 'y' ? [0, 1, 0] : [0, 0, -1];
  const [sry, srz, srx] = shellRotDeg(shell.rot || [0, 0, 0]);
  v = rotVec(v, 'x', srx); v = rotVec(v, 'z', srz); v = rotVec(v, 'y', sry);
  v = rotVec(v, 'y', state.orbitY);
  v = rotVec(v, 'x', state.orbitX);
  return [v[0], v[1]];
}

/* solid, shaded 3D box centred on its parent's origin (real depth, not a flat div) */
function solidBox(parent, w, h, d, color) {
  const g = el('div', '', parent);
  g.style.cssText = 'position:absolute;left:0;top:0;transform-style:preserve-3d;pointer-events:none;';
  const hx = w / 2, hy = h / 2, hz = d / 2;
  const faces = [
    [w, h, `translateZ(${hz}px)`, 1.0],
    [w, h, `rotateY(180deg) translateZ(${hz}px)`, 0.72],
    [d, h, `rotateY(90deg) translateZ(${hx}px)`, 0.86],
    [d, h, `rotateY(-90deg) translateZ(${hx}px)`, 0.86],
    [w, d, `rotateX(90deg) translateZ(${hy}px)`, 1.12],
    [w, d, `rotateX(-90deg) translateZ(${hy}px)`, 0.62],
  ];
  for (const [fw, fh, tf, shade] of faces) {
    const f = el('div', '', g);
    f.style.cssText = `position:absolute;left:${-fw / 2}px;top:${-fh / 2}px;width:${fw}px;height:${fh}px;` +
      `background:${color};transform:${tf};backface-visibility:visible;filter:brightness(${shade});`;
  }
  return g;
}

/* direction of a gizmo axis expressed in the shell's PARENT BONE frame (MC units).
   WORLD: undo the bone chain (R_chain^-1 · worldAxis). LOCAL: apply the shell's own rot.
   This is what makes world-space dragging actually move the piece along world axes. */
function axisDirBoneMC(axis, shell) {
  let v = axis === 'x' ? [1, 0, 0] : axis === 'y' ? [0, 1, 0] : [0, 0, -1];
  const [sry, srz, srx] = shellRotDeg(shell.rot || [0, 0, 0]);
  v = rotVec(v, 'x', srx); v = rotVec(v, 'z', srz); v = rotVec(v, 'y', sry);
  return [v[0], v[1], -v[2]]; // CSS basis → MC
}

function mountGizmo(parentEl, shell) {
  gizmo3d.remove();
  gizmo3d.innerHTML = '';
  parentEl.appendChild(gizmo3d);
  gizmoAnchor = { shell };
  const mode = state.gizmoMode;
  const rotMode = mode === 'rotate';
  const scaleMode = mode === 'scale';
  const len = rotMode ? GIZMO_L * 1.4 : GIZMO_L + GIZMO_STICK;
  const mkAxis = (axis, color, tf) => {
    if (rotMode) return;
    const grp = el('div', 'gizmo-arrow', gizmo3d);
    grp.style.cssText = `position:absolute;left:0;top:0;transform-style:preserve-3d;transform:${tf};pointer-events:none;`;
    grp.dataset.axis = scaleMode ? 'p' + axis : axis;
    const shaft = el('div', '', grp);
    shaft.style.cssText = `position:absolute;left:0;top:0;transform-style:preserve-3d;transform:translate3d(0,${len / 2}px,0);`;
    solidBox(shaft, 5, len, 5, color);
    const headSz = scaleMode ? [13, 13, 11] : [13, 9, 5];
    headSz.forEach((sz, i) => {
      const s = el('div', '', grp);
      s.style.cssText = `position:absolute;left:0;top:0;transform-style:preserve-3d;transform:translate3d(0,${len + i * 5 + 3}px,0);`;
      solidBox(s, sz, 6, sz, color);
    });
    return grp;
  };
  mkAxis('x', '#ff5f56', 'rotateZ(-90deg)');
  mkAxis('y', '#37d67a', '');
  mkAxis('z', '#4f9cff', 'rotateX(-90deg)');
  if (rotMode) {
    const R = GIZMO_L * 1.4;
    const mkRing = (axis, color, tf) => {
      const sel = state.rotRing === axis;
      const ring = el('div', 'gizmo-ring', gizmo3d);
      ring.style.cssText = `position:absolute;left:${-R / 2}px;top:${-R / 2}px;width:${R}px;height:${R}px;` +
        `border:${sel ? 3 : 2}px ${sel ? 'solid' : 'dashed'} ${color};border-radius:50%;pointer-events:none;` +
        `opacity:${sel ? 1 : 0.5};transform:${tf};${sel ? `box-shadow:0 0 8px ${color};` : ''}`;
      return ring;
    };
    mkRing('x', '#ff5f56', 'rotateY(90deg)');
    mkRing('y', '#37d67a', 'rotateX(90deg)');
    mkRing('z', '#4f9cff', '');
  }
  const core = el('div', 'gizmo-core', gizmo3d);
  core.style.cssText = `position:absolute;left:-4px;top:-4px;width:8px;height:8px;background:#fff;pointer-events:none;`;
  mountConstraintVisuals();
  updateGizmo();
}

/* Blender-style constraint feedback: dim non-locked axes, bright line on the locked
   axis, translucent quad for the locked plane. */
const AXCOL = { x: '#ff5f56', y: '#37d67a', z: '#4f9cff' };
const AXTF = { x: 'rotateZ(-90deg)', y: '', z: 'rotateX(-90deg)' };
function mountConstraintVisuals() {
  const m = state.modal;
  if (!m || (!m.axis && !m.plane)) return;
  if (m.axis) {
    for (const h of gizmo3d.querySelectorAll('.gizmo-arrow')) {
      h.style.opacity = h.dataset.axis === (m.axis) || h.dataset.axis === ('p' + m.axis) ? '1' : '0.12';
    }
    const HALF = 1200;
    const grp = el('div', 'gizmo-lock', gizmo3d);
    grp.style.cssText = `position:absolute;left:0;top:0;transform-style:preserve-3d;transform:${AXTF[m.axis]};pointer-events:none;`;
    solidBox(grp, 4, HALF * 2, 4, AXCOL[m.axis]);
    const hit = el('div', 'gizmo-lock', grp);
    hit.style.cssText = 'position:absolute;left:-4px;top:0;width:8px;height:100%;pointer-events:none;';
  } else if (m.plane) {
    const R = 150;
    const tf = m.plane === 'x' ? 'rotateY(90deg)' : m.plane === 'y' ? 'rotateX(90deg)' : '';
    const quad = el('div', 'gizmo-plane', gizmo3d);
    quad.style.cssText = `position:absolute;left:${-R / 2}px;top:${-R / 2}px;width:${R}px;height:${R}px;` +
      `background:${AXCOL[m.plane]}22;border:1px solid ${AXCOL[m.plane]};pointer-events:none;transform:${tf}` +
      `;background-image:linear-gradient(${AXCOL[m.plane]}33 1px,transparent 1px),linear-gradient(90deg,${AXCOL[m.plane]}33 1px,transparent 1px);background-size:15px 15px;`;
  }
}

function unmountGizmo() {
  gizmo3d.remove(); gizmo3d.innerHTML = '';
  gizmoAnchor = null;
}

function updateGizmo() {
  if (!gizmoAnchor || !gizmo3d.parentElement) return;
  const live = state.shells.find((x) => x.id === gizmoAnchor.shell.id);
  if (!live) { unmountGizmo(); return; }
  const U = state.unit;
  const fw = cameraForwardInBone(gizmo3d.parentElement);
  const K = 900;
  const cx = (live.pos[0] + live.size[0] / 2) * U + fw[0] * K;
  const cy = (live.pos[1] + live.size[1] / 2) * U + fw[1] * K;
  const cz = -(live.pos[2] + live.size[2] / 2) * U + fw[2] * K;
  const r = live.rot || [0, 0, 0];
  const [ry, rz, rx] = shellRotDeg(r);
  const orient = `rotateY(${ry}deg) rotateZ(${rz}deg) rotateX(${rx}deg)`;
  gizmo3d.style.transform =
    `translate3d(${cx}px,${cy}px,${cz}px) ${orient}`;
  for (const axis of ['x', 'y', 'z']) {
    const [sx, sy] = axisScreenDirLocal(axis, live);
    const cap = gizmo3d.querySelector(`[data-axis=${axis}]`);
    if (cap) { cap.dataset.dirx = sx; cap.dataset.diry = sy; }
  }
}



function frame() {
  applyBoneTransforms();
  requestAnimationFrame(frame);
}

/* keep the square UV canvas fitted to its panel */
let uvFitTimer = null;
window.addEventListener('resize', () => {
  clearTimeout(uvFitTimer);
  uvFitTimer = setTimeout(() => { fitUVZoom(); drawUV(); }, 120);
});

/* rebuild when textures change */
function scheduleRebuild() { clearTimeout(scheduleRebuild._t); scheduleRebuild._t = setTimeout(buildScene, 60); }

/* orbit: drag anywhere in the viewport; pan: middle-drag or Shift+drag */
let drag = null;
let panDrag = null;
vp.addEventListener('mousedown', (e) => {
  if (e.target.closest('.viewport-toolbar')) return;
  // pan: middle mouse button, or Shift + left
  if (e.button === 1 || (e.button === 0 && e.shiftKey)) {
    e.preventDefault();
    panDrag = { sx: e.clientX, sy: e.clientY, p0: state.pan.slice() };
    return;
  }
  if (e.button !== 0) return;
  const pick = pickGizmo(e.clientX, e.clientY);
  if (pick && state.selShell) {
    // clicking a handle cancels any armed modal, then takes over with direct manipulation
    if (state.modal) endModal(false);
    const live = state.shells.find((s) => s.id === state.selShell);
    if (live && state.gizmoMode === 'rotate') {
      const ringAxis = state.rotRing || (pick.axis.length > 1 ? pick.axis[1] : 'z');
      pushUndo();
      const U = state.unit;
      const bone = boneEls[live.parent];
      const cn = [(live.pos[0] + live.size[0] / 2) * U, (live.pos[1] + live.size[1] / 2) * U, -(live.pos[2] + live.size[2] / 2) * U];
      const fw = cameraForwardInBone(bone); const K = 900;
      const origin = probeScreen(bone, cn[0] + fw[0] * K, cn[1] + fw[1] * K, cn[2] + fw[2] * K);
      document.body.classList.add('gizmo-dragging');
      vp._gizmoDrag = {
        mode: 'ring', axis: ringAxis, cx: origin[0], cy: origin[1],
        lastAngle: Math.atan2(e.clientY - origin[1], e.clientX - origin[0]), accum: 0, shell: live,
        sx: e.clientX, sy: e.clientY,
        orig: { pos: live.pos.slice(), size: live.size.slice(), rot: (live.rot || [0, 0, 0]).slice() },
      };
      e.preventDefault();
      return;
    }
    if (live) {
      pushUndo();
      document.body.classList.add('gizmo-dragging');
      vp._gizmoDrag = {
        axis: pick.axis, x: e.clientX, y: e.clientY, sx: e.clientX, sy: e.clientY, shell: live,
        orig: { pos: live.pos.slice(), size: live.size.slice(), rot: (live.rot || [0, 0, 0]).slice() },
      };
      e.preventDefault();
      return;
    }
  }
  if (state.modal) { state.modal.pressed = true; e.preventDefault(); return; }
  drag = { x: e.clientX, y: e.clientY, ox: state.orbitX, oy: state.orbitY, moved: false };
  e.preventDefault();
});vp.addEventListener('contextmenu', (e) => {
  if (state.modal) { e.preventDefault(); endModal(false); }
});
window.addEventListener('mousemove', (e) => {
  if (panDrag) {
    state.pan = [panDrag.p0[0] + (e.clientX - panDrag.sx), panDrag.p0[1] + (e.clientY - panDrag.sy)];
    return;
  }
  if (!drag) return;
  const dx = e.clientX - drag.x, dy = e.clientY - drag.y;
  if (Math.abs(dx) + Math.abs(dy) > 3) drag.moved = true;
  state.orbitY = drag.oy + dx * 0.5;
  state.orbitX = Math.max(-89, Math.min(89, drag.ox + dy * 0.5));
});
window.addEventListener('mouseup', () => { panDrag = null; saveSession(); setTimeout(() => drag = null, 0); });
vp.addEventListener('click', (e) => {
  if (e.target.closest('.viewport-toolbar')) return;
  if (drag && drag.moved) { e.stopPropagation(); e.preventDefault(); }
}, true);
vp.addEventListener('wheel', (e) => {
  if (e.target.closest('.viewport-toolbar')) return;
  e.preventDefault();
  state.zoom = Math.max(0.25, Math.min(6, state.zoom * Math.pow(0.999, e.deltaY)));
  saveSession();
}, { passive: false });

/* ---------- left panel ---------- */
function renderBoneTree() {
  const t = $('#boneTree'); t.innerHTML = '';
  for (const [id, b] of Object.entries(BONES)) {
    const item = el('div', 'bone-item' + (state.selBone === id ? ' selected' : ''), t);
    item.innerHTML = `<span class="tag">◇</span> ${id}`;
    item.onclick = () => { state.selBone = id; renderBoneTree(); renderShellProps(); refreshSelHighlight(); };
    if (b.parent) t.lastChild && null;
  }
}
function renderShellList() {
  const l = $('#shellList'); l.innerHTML = '';
  for (const s of state.shells) {
    const tex = getPreset(state.preset)[s.layer];
    const ok = boxOpaque(s.uv, s.size[0], s.size[1], s.size[2], tex);
    const item = el('div', 'shell-item' + (state.selShell === s.id ? ' selected' : ''), l);
    item.innerHTML = `<span class="dot" style="background:${s.layer === 1 ? 'var(--accent)' : 'var(--warn)'}"></span>` +
      `<span class="name">${s.id}</span>` + (ok ? '' : '<span class="bad" title="samples transparent texels">⊘ alpha</span>');
    item.onclick = () => selectShell(s.id);
  }
  updateValidity();
}
function selectShell(id) {
  state.selShell = id;
  state.rotRing = null;
  if (!state.selFace || state.selFace.shell !== id) state.selFace = null;
  const s = state.shells.find((x) => x.id === id);
  if (s) { state.selBone = s.parent; state.curLayer = s.layer; }
  renderShellList(); renderBoneTree(); renderShellProps(); drawUV();
  $('#statSel').textContent = 'Shell: ' + (id || 'none');
  if (id) buildScene(); else unmountGizmo();
}

function refreshSelHighlight() {
  const fs = state.selFace;
  for (const b of Object.values(boneEls)) {
    for (const f of b.querySelectorAll('[data-bone]')) {
      const shellSel = f.dataset.shell === state.selShell;
      const boneSel = f.dataset.bone === state.selBone;
      const faceSel = !!fs && f.dataset.shell === fs.shell && f.dataset.face === fs.face;
      let outline = boneSel ? '2px solid #ffb340' : 'none';
      let shadow = 'none';
      if (shellSel) {
        outline = '3px solid #ff3b30';
        if (boneSel) shadow = 'inset 0 0 0 2px #ffb340';
      }
      if (faceSel) { outline = '4px solid #4f9cff'; shadow = 'inset 0 0 0 3px #4f9cff'; }
      f.style.outline = outline;
      f.style.boxShadow = shadow;
      f.style.filter = faceSel ? 'brightness(2) saturate(1.6)'
        : shellSel ? 'brightness(1.8) saturate(1.4)'
        : boneSel ? 'brightness(1.35) sepia(.35) saturate(2.2)'
        : '';
      f.style.zIndex = faceSel ? '7' : shellSel ? '5' : '';
    }
  }
}

/* ---------- session persistence (survives auto-reload) ---------- */
function saveSession() {
  clearTimeout(saveSession._t);
  saveSession._t = setTimeout(() => {
    try { sessionStorage.setItem('armor-editor', JSON.stringify(projectPayload())); } catch (e) { /* ignore */ }
  }, 250);
}
function restoreSession() {
  try {
    const raw = sessionStorage.getItem('armor-editor');
    if (!raw) return false;
    return applyProject(JSON.parse(raw));
  } catch (e) { return false; }
}

/* ---------- explicit save / load (localStorage, survives browser restart) ---------- */
const SAVE_KEY = 'armor-editor-project';
function projectPayload() {
  return {
    version: 1,
    shells: state.shells,
    visibleSlots: state.visibleSlots,
    preset: state.preset,
    sel: state.selShell,
    layer: state.curLayer,
    mirrorAxis: state.mirrorAxis,
    view: {
      showFur: state.showFur, showL1: state.showL1, showL2: state.showL2,
      gizmoMode: state.gizmoMode, rotRing: state.rotRing, selFace: state.selFace,
      anim: state.anim,
      cam: { orbitX: state.orbitX, orbitY: state.orbitY, zoom: state.zoom, pan: state.pan.slice() },
      uv: { grid: state.uvGrid, bg: state.uvBg, showShells: state.uvShowShells, zoom: state.uvZoom, pan: state.uvPan.slice() },
    },
  };
}
function applyProject(d) {
  if (!d || typeof d !== 'object') return false;
  if (Array.isArray(d.shells) && d.shells.length) state.shells = normalizeShells(d.shells);
  state.visibleSlots = Array.isArray(d.visibleSlots) ? d.visibleSlots : [];
  if (d.preset && state.presets[d.preset]) state.preset = d.preset;
  state.curLayer = d.layer || 1;
  state.selShell = state.shells.some((s) => s.id === d.sel) ? d.sel : null;
  if (d.mirrorAxis) state.mirrorAxis = d.mirrorAxis;
  const v = d.view || {};
  if (typeof v.showFur === 'boolean') state.showFur = v.showFur;
  if (typeof v.showL1 === 'boolean') state.showL1 = v.showL1;
  if (typeof v.showL2 === 'boolean') state.showL2 = v.showL2;
  if (v.gizmoMode) state.gizmoMode = v.gizmoMode;
  state.rotRing = null;
  state.selFace = (v.selFace && state.shells.some((s) => s.id === v.selFace.shell)) ? v.selFace : null;
  if (v.anim) state.anim = v.anim;
  const c = v.cam || {};
  if (typeof c.orbitX === 'number') state.orbitX = c.orbitX;
  if (typeof c.orbitY === 'number') state.orbitY = c.orbitY;
  if (typeof c.zoom === 'number') state.zoom = c.zoom;
  if (Array.isArray(c.pan)) state.pan = c.pan.slice();
  const u = v.uv || {};
  if (typeof u.grid === 'boolean') state.uvGrid = u.grid;
  if (typeof u.bg === 'string') state.uvBg = u.bg;
  if (typeof u.showShells === 'boolean') state.uvShowShells = u.showShells;
  if (typeof u.zoom === 'number') state.uvZoom = u.zoom;
  if (Array.isArray(u.pan)) state.uvPan = u.pan.slice();
  syncControls();
  if (!state.selShell) unmountGizmo();
  renderSlotTabs(); renderBoneTree(); renderShellList(); renderShellProps(); buildScene(); drawUV();
  return true;
}
/* push state back into the form controls */
function syncControls() {
  const set = (sel, val, prop) => { const e = $(sel); if (e) e[prop] = val; };
  set('#showFur', state.showFur, 'checked'); set('#showLayer1', state.showL1, 'checked'); set('#showLayer2', state.showL2, 'checked');
  set('#animSel', state.anim, 'value'); set('#gizmoSel', state.gizmoMode, 'value');
  set('#uvGrid', state.uvGrid, 'checked'); set('#uvBg', state.uvBg, 'value'); set('#uvShowShells', state.uvShowShells, 'checked');
  const ps = $('#armorPreset'); if (ps) ps.value = state.preset;
}
function saveProject() {
  try {
    localStorage.setItem(SAVE_KEY, JSON.stringify(projectPayload()));
    log('Saved — ' + state.shells.length + ' shells (' + new Date().toLocaleTimeString() + ')');
  } catch (e) { log('Save failed: ' + e.message); }
}
function loadProject() {
  let d = null;
  try { d = JSON.parse(localStorage.getItem(SAVE_KEY) || 'null'); } catch (e) { d = null; }
  if (!d) { log('No saved project found'); return; }
  pushUndo();
  applyProject(d);
  log('Loaded saved project (' + state.shells.length + ' shells)');
}

/* ---------- undo / redo (snapshot stack) ---------- */
let undoStack = [], redoStack = [];
const UNDO_LIMIT = 64;
function snapState() { return JSON.stringify({ shells: state.shells, sel: state.selShell, vis: state.visibleSlots, layer: state.curLayer }); }
function pushUndo() {
  const s = snapState();
  if (undoStack[undoStack.length - 1] === s) return;
  undoStack.push(s);
  if (undoStack.length > UNDO_LIMIT) undoStack.shift();
  redoStack.length = 0;
  saveSession();
}
function applySnapshot(str) {
  const d = JSON.parse(str);
  state.shells = d.shells;
  state.visibleSlots = d.vis || [];
  state.curLayer = d.layer || 1;
  state.selShell = state.shells.some((x) => x.id === d.sel) ? d.sel : null;
  if (!state.selShell) unmountGizmo();
  renderSlotTabs(); renderBoneTree(); buildScene(); renderShellList(); drawUV(); renderShellProps();
  $('#statSel').textContent = 'Shell: ' + (state.selShell || 'none');
}
function undo() {
  if (!undoStack.length) { log('Nothing to undo'); return; }
  redoStack.push(snapState());
  applySnapshot(undoStack.pop());
  log('Undo  (' + undoStack.length + ' left)');
}
function redo() {
  if (!redoStack.length) { log('Nothing to redo'); return; }
  undoStack.push(snapState());
  applySnapshot(redoStack.pop());
  log('Redo');
}

/* 15° rotation snapping (hold Shift for free rotation) */
const SNAP15 = Math.PI / 12;
function snapRot(v, fine) { return fine ? v : Math.round(v / SNAP15) * SNAP15; }

/* mirror counterpart of a shell id: swaps Left/Right or a trailing L/R */
function mirrorId(id) {
  if (/Right/.test(id)) return id.replace('Right', 'Left');
  if (/Left/.test(id)) return id.replace('Left', 'Right');
  if (/R$/.test(id)) return id.slice(0, -1) + 'L';
  if (/L$/.test(id)) return id.slice(0, -1) + 'R';
  return null;
}
function copyUVMirrored(target, src) {
  target.uv = src.uv.slice();
  target.uvScale = src.uvScale || 1;
  target.faceOffset = JSON.parse(JSON.stringify(src.faceOffset || {}));
  target.faceScale = JSON.parse(JSON.stringify(src.faceScale || {}));
  target.uvMirror = true;   // mirrored copy: same UVs, reflected on the texture
}
/* copy size + rotation + position from the twin, mirroring across `axis`
   (box spans [p, p+s] → its mirror spans [-(p+s), -p]; rotations about the other axes flip) */
function copyTransformMirrored(target, src, axis) {
  const s = src.size, p = src.pos.slice(), r = src.rot || [0, 0, 0];
  target.size = s.slice();
  target.rot = axis === 'x' ? [r[0], -r[1], -r[2]] : axis === 'y' ? [-r[0], r[1], -r[2]] : [-r[0], -r[1], r[2]];
  const i = axis === 'x' ? 0 : axis === 'y' ? 1 : 2;
  p[i] = -(p[i] + s[i]);
  target.pos = p.map((v) => +v.toFixed(2));
}
/* fabricate a mirrored twin for a shell that has none: new id (R/L swap or _m suffix),
   parented to the mirrored bone when one exists, with mirrored transform + UVs */
function createMirroredCounterpart(s, axis) {
  const newId = mirrorId(s.id) || (s.id + '_m');
  if (state.shells.some((x) => x.id === newId)) { log('Counterpart already exists: ' + newId); return null; }
  const c = JSON.parse(JSON.stringify(s));
  c.id = newId;
  const pb = mirrorId(s.parent);
  if (pb && BONES[pb]) c.parent = pb;
  copyTransformMirrored(c, s, axis);
  copyUVMirrored(c, s);
  state.shells.push(c);
  return c;
}

/* ---------- shell properties ---------- */
function numInput(v, cb, step) {  const i = document.createElement('input');
  i.type = 'number'; i.value = v; i.step = step || 0.05;
  i.onchange = () => { pushUndo(); cb(parseFloat(i.value)); };
  return i;
}
function renderShellProps() {
  const p = $('#shellProps'); p.innerHTML = '';
  const s = state.shells.find((x) => x.id === state.selShell);
  if (!s) { p.innerHTML = '<div class="hint">Select or add a shell.</div>'; return; }
  const row = (label, node) => { const l = el('label', '', p); l.textContent = label; p.appendChild(node); };

  const name = document.createElement('input'); name.type = 'text'; name.value = s.id; name.style.width = '100%';
  name.onchange = () => { pushUndo(); s.id = name.value.replace(/\s/g, ''); selectShell(s.id); };
  row('Name', name);

  const par = document.createElement('select'); par.style.width = '100%';
  for (const id of Object.keys(BONES)) {
    const o = document.createElement('option'); o.value = id; o.textContent = id; if (s.parent === id) o.selected = true; par.appendChild(o);
  }
  par.onchange = () => { pushUndo(); s.parent = par.value; buildScene(); renderBoneTree(); };
  row('Parent bone', par);

  const layer = document.createElement('select');
  [1, 2].forEach((n) => { const o = document.createElement('option'); o.value = n; o.textContent = 'layer_' + n; if (s.layer === n) o.selected = true; layer.appendChild(o); });
  layer.onchange = () => { pushUndo(); s.layer = parseInt(layer.value); buildScene(); renderShellList(); drawUV(); };
  row('Texture layer', layer);

  const mk3 = (label, arr, key, step, round) => {
    const box = el('div', 'row3', null);
    arr.forEach((val, i) => box.appendChild(numInput(val, (nv) => { s[key][i] = round ? Math.round(nv) : nv; buildScene(); renderShellList(); drawUV(); }, step)));
    row(label, box);
  };
  mk3('Position xyz', s.pos, 'pos');
  mk3('Size whd', s.size, 'size', 1);
  mk3('UV offset uv', s.uv, 'uv', 1, true);  row('UV scale', numInput(s.uvScale || 1, (nv) => { s.uvScale = Math.max(0.1, Math.min(16, nv)); buildScene(); drawUV(); }, 0.1));

  const slotsBox = el('div', '', null);
  slotsBox.style.cssText = 'display:flex;gap:8px;flex-wrap:wrap';
  for (const sl of SLOTS) {
    const lb = el('label', 'check', slotsBox);
    const cb = document.createElement('input'); cb.type = 'checkbox'; cb.checked = s.slots.includes(sl);
    cb.onchange = () => { pushUndo(); cb.checked ? s.slots.push(sl) : s.slots = s.slots.filter((x) => x !== sl); buildScene(); renderShellList(); };
    lb.appendChild(cb); lb.append(sl);
  }
  row('Slots', slotsBox);

  const del = el('button', '', null); del.textContent = 'Delete shell'; del.style.marginTop = '6px';
  del.onclick = () => { pushUndo(); state.shells = state.shells.filter((x) => x.id !== s.id); unmountGizmo(); selectShell(null); buildScene(); renderShellList(); };
  row('', del);

  const mirBox = el('label', 'check', null);
  const mirCb = document.createElement('input'); mirCb.type = 'checkbox'; mirCb.checked = !!s.uvMirror;
  mirCb.onchange = () => { pushUndo(); s.uvMirror = mirCb.checked; buildScene(); drawUV(); };
  mirBox.appendChild(mirCb); mirBox.append('Mirror UVs');
  row('', mirBox);

  const pairId = mirrorId(s.id);
  const pair = pairId ? state.shells.find((x) => x.id === pairId) : null;
  const mirBtn = el('button', '', null);
  mirBtn.textContent = pair ? `Copy UVs mirrored from ${pair.id}` : 'No mirrored counterpart';
  mirBtn.disabled = !pair;
  mirBtn.style.marginTop = '4px';
  if (pair) mirBtn.onclick = () => { pushUndo(); copyUVMirrored(s, pair); buildScene(); drawUV(); log('UVs mirrored from ' + pair.id); };
  row('', mirBtn);

  const axRow = el('div', '', null);
  axRow.style.cssText = 'display:flex;gap:8px;align-items:center;margin-top:4px';
  const axLbl = document.createElement('span'); axLbl.textContent = 'Flip axis'; axLbl.style.color = 'var(--dim)'; axLbl.style.fontSize = '12px';
  const axSel = document.createElement('select');
  for (const a of ['x', 'y', 'z']) { const o = document.createElement('option'); o.value = a; o.textContent = a.toUpperCase(); if (state.mirrorAxis === a) o.selected = true; axSel.appendChild(o); }
  axSel.onchange = () => { state.mirrorAxis = axSel.value; };
  axRow.appendChild(axLbl); axRow.appendChild(axSel);
  row('', axRow);

  const xfBtn = el('button', '', null);
  xfBtn.textContent = pair ? `Copy transform from ${pair.id}` : 'No mirrored counterpart';
  xfBtn.disabled = !pair;
  xfBtn.style.marginTop = '4px';
  if (pair) xfBtn.onclick = () => { pushUndo(); copyTransformMirrored(s, pair, state.mirrorAxis); buildScene(); renderShellProps(); log(`Transform mirrored from ${pair.id} (flip ${state.mirrorAxis.toUpperCase()})`); };
  row('', xfBtn);

  const newBtn = el('button', '', null);
  newBtn.textContent = 'Create mirrored counterpart';
  newBtn.disabled = !!pair;
  newBtn.title = pair ? `Already exists: ${pair.id}` : 'Create a mirrored twin of this shell';
  newBtn.style.marginTop = '4px';
  if (!pair) newBtn.onclick = () => {
    pushUndo();
    const c = createMirroredCounterpart(s, state.mirrorAxis);
    if (c) { buildScene(); renderShellList(); selectShell(c.id); log(`Created ${c.id} (flip ${state.mirrorAxis.toUpperCase()})`); }
  };
  row('', newBtn);

  if (s.rot) {
    const rr = el('div', 'row3', null);
    s.rot.forEach((val, i) => rr.appendChild(numInput(+(val * 180 / Math.PI).toFixed(1), (nv) => { s.rot[i] = nv * Math.PI / 180; buildScene(); }, 1)));
    row('Rotate xyz\u00b0', rr);
  }
}

/* ---------- UV editor ---------- */
const uvC = $('#uvCanvas');
const uvG = uvC.getContext('2d', { willReadFrequently: true });
/* UV canvas is square (1:1): the 64x32 texture occupies the top half, the rest is backdrop */
function uvSize() { const s = TEX_W * state.uvZoom; return [s, s]; }
function fitUVZoom() {
  const wrap = document.querySelector('#uvCanvasWrap');
  if (!wrap) return;
  const w = wrap.clientWidth - 16, h = wrap.clientHeight - 16;
  if (w > 32 && h > 32) state.uvZoom = Math.max(2, Math.min(24, Math.floor(Math.min(w, h) / TEX_W)));
}
function fitUVCanvas() {
  const [w, h] = uvSize();
  uvC.width = w; uvC.height = h;
  uvC.style.width = w + 'px'; uvC.style.height = h + 'px';
}
function drawUV() {
  fitUVCanvas();
  const Z = state.uvZoom;
  const panX = state.uvPan[0], panY = state.uvPan[1];
  uvG.setTransform(1, 0, 0, 1, 0, 0);
  uvG.clearRect(0, 0, uvC.width, uvC.height);
  uvG.translate(panX, panY);
  const showGrid = state.uvGrid;
  const bg = state.uvBg;
  uvG.fillStyle = bg; uvG.fillRect(0, 0, uvC.width, uvC.height);
  const tex = curTex();
  uvG.imageSmoothingEnabled = false;
  uvG.drawImage(tex, 0, 0, TEX_W * Z, TEX_H * Z);
  uvG.strokeStyle = 'rgba(255,255,255,.35)';
  uvG.strokeRect(0.5, 0.5, TEX_W * Z - 1, TEX_H * Z - 1);
  if (showGrid) {
    uvG.globalAlpha = 0.15; uvG.fillStyle = '#fff';
    for (let x = 0; x < TEX_W; x += 8) uvG.fillRect(x * Z, 0, 1, uvC.height);
    for (let y = 0; y < TEX_H; y += 8) uvG.fillRect(0, y * Z, uvC.width, 1);
    uvG.globalAlpha = 1;
    uvG.strokeStyle = 'rgba(255,255,255,.18)';
    for (let x = 0; x <= TEX_W; x++) { uvG.beginPath(); uvG.moveTo(x * Z, 0); uvG.lineTo(x * Z, uvC.height); uvG.stroke(); }
    for (let y = 0; y <= TEX_H; y++) { uvG.beginPath(); uvG.moveTo(0, y * Z); uvG.lineTo(uvC.width, y * Z); uvG.stroke(); }
  }
  if (state.uvShowShells) {
    const s = state.shells.find((x) => x.id === state.selShell);
    if (s && s.layer === state.curLayer) {
      for (const f of uvRectsOf(s)) {
        const fSel = state.selFace && state.selFace.shell === s.id && state.selFace.face === f.n;
        uvG.fillStyle = fSel ? 'rgba(255,210,28,.45)' : 'rgba(79,156,255,.35)';
        uvG.fillRect(f.x * Z, f.y * Z, f.w * Z, f.h * Z);
        uvG.strokeStyle = fSel ? '#ffd21c' : '#4f9cff';
        uvG.lineWidth = fSel ? 3 : 2;
        uvG.strokeRect(f.x * Z + .5, f.y * Z + .5, f.w * Z - 1, f.h * Z - 1);
        const hx = (f.x + f.w) * Z, hy = (f.y + f.h) * Z;
        uvG.fillStyle = fSel ? '#ffd21c' : '#4f9cff';
        uvG.fillRect(hx - 4, hy - 4, 8, 8);
        uvG.strokeStyle = '#fff';
        uvG.strokeRect(hx - 4, hy - 4, 8, 8);
      }
    }
  }
  uvG.setTransform(1, 0, 0, 1, 0, 0);
  $('#statUV').textContent = `layer_${state.curLayer}  ${TEX_W}x${TEX_H}  zoom ${Z}x`;
}
function uvRectsOf(s) {
  return shellRects(s);
}

/* UV interaction: drag face rects to move them (islands), corner handle = UV scale,
   drag empty space = pan, wheel = zoom. */
function uvHit(e) {
  const r = uvC.getBoundingClientRect();
  const px = (e.clientX - r.left - state.uvPan[0]) / state.uvZoom, py = (e.clientY - r.top - state.uvPan[1]) / state.uvZoom;
  const s = state.shells.find((x) => x.id === state.selShell);
  if (!s || s.layer !== state.curLayer) return null;
  const rects = uvRectsOf(s);
  // overlapping islands: the ACTIVE (shift-selected) face wins, else the topmost (drawn last)
  const active = state.selFace && state.selFace.shell === s.id ? rects.find((f) => f.n === state.selFace.face) : null;
  const order = [];
  if (active) order.push(active);
  for (let i = rects.length - 1; i >= 0; i--) if (rects[i] !== active) order.push(rects[i]);
  for (const f of order) {
    const hx = f.x + f.w, hy = f.y + f.h;
    if (Math.abs(px - hx) < 0.6 && Math.abs(py - hy) < 0.6) return { shell: s, face: f, mode: 'scale' };
    if (px >= f.x && px < hx && py >= f.y && py < hy) return { shell: s, face: f, mode: 'move' };
  }
  return null;
}
let uvPanDrag = null;
let uvDrag = null;
uvC.addEventListener('mousedown', (e) => {
  e.preventDefault();
  const hit = uvHit(e);
  if (hit) {
    if (state.selShell !== hit.shell.id) selectShell(hit.shell.id);
    if (e.shiftKey) {
      // Shift+click = select this face (highlight in 2D + 3D), no drag
      state.selFace = { shell: hit.shell.id, face: hit.face.n };
      drawUV(); refreshSelHighlight();
      log('Face: ' + hit.shell.id + '.' + hit.face.n);
      return;
    }
    pushUndo();
    const fs0 = hit.shell.faceScale || {};
    const raw = fs0[hit.face.n];
    const scale0 = Array.isArray(raw) ? raw.slice() : [typeof raw === 'number' ? raw : 1, typeof raw === 'number' ? raw : 1];
    const base = baseFaceRect(hit.shell, hit.face.n);
    uvDrag = { ...hit, startX: e.clientX, startY: e.clientY, uv0: hit.shell.uv.slice(), scale0, baseW: base ? base.w : 1, baseH: base ? base.h : 1, axisLock: null, off0: ((hit.shell.faceOffset || {})[hit.face.n] || [0, 0]).slice(), face0: hit.face };
    try { uvC.setPointerCapture(e.pointerId); } catch (err) { }
    return;
  }
  // empty space: pan the view
  uvPanDrag = { sx: e.clientX, sy: e.clientY, p0: state.uvPan.slice() };
  try { uvC.setPointerCapture(e.pointerId); } catch (err) { }
});
uvC.addEventListener('mousemove', (e) => {
  if (uvPanDrag) {
    state.uvPan = [uvPanDrag.p0[0] + (e.clientX - uvPanDrag.sx), uvPanDrag.p0[1] + (e.clientY - uvPanDrag.sy)];
    drawUV();
    return;
  }
  if (uvDrag) {
    const Z = state.uvZoom;
    const dpx = (e.clientX - uvDrag.startX) / Z, dpy = (e.clientY - uvDrag.startY) / Z;
    const s = uvDrag.shell;
    if (uvDrag.mode === 'move') {
      // move THIS face only (independent UV island); Ctrl-drag moves the whole shell
      if (e.ctrlKey || e.metaKey) {
        s.uv[0] = Math.round(uvDrag.uv0[0] + dpx);
        s.uv[1] = Math.round(uvDrag.uv0[1] + dpy);
      } else {
        if (!s.faceOffset) s.faceOffset = {};
        s.faceOffset[uvDrag.face0.n] = [Math.round(uvDrag.off0[0] + dpx), Math.round(uvDrag.off0[1] + dpy)];
      }
    } else {
      const f = uvDrag.face0;
      const baseW = Math.max(1, uvDrag.baseW), baseH = Math.max(1, uvDrag.baseH);
      const refW = baseW, refH = baseH;
      const ax = uvDrag.axisLock;
      let sx = uvDrag.scale0[0], sy = uvDrag.scale0[1];
      if (ax !== 'y') sx = Math.max(0.1, Math.min(16, sx + dpx / refW));
      if (ax !== 'x') sy = Math.max(0.1, Math.min(16, sy + dpy / refH));
      // snap the resulting texel extent to whole texels against the pre-faceScale base
      sx = Math.max(1, Math.round(baseW * sx)) / baseW;
      sy = Math.max(1, Math.round(baseH * sy)) / baseH;
      if (!s.faceScale) s.faceScale = {};
      s.faceScale[f.n] = [+sx.toFixed(4), +sy.toFixed(4)];
    }
    drawUV(); renderShellProps();
    return;
  }
  const hit = uvHit(e);
  uvC.style.cursor = hit ? (hit.mode === 'scale' ? 'nwse-resize' : 'move') : '';
});
uvC.addEventListener('mouseup', () => {
  if (uvPanDrag) { uvPanDrag = null; return; }
  if (uvDrag) { uvDrag = null; buildScene(); renderShellList(); updateValidity(); }
});
uvC.addEventListener('dblclick', () => { state.uvPan = [0, 0]; drawUV(); });
uvC.addEventListener('contextmenu', (e) => e.preventDefault());
uvC.addEventListener('wheel', (e) => {
  e.preventDefault();
  state.uvZoom = Math.max(2, Math.min(24, state.uvZoom * Math.pow(0.999, e.deltaY)));
  saveSession();
  drawUV();
}, { passive: false });
/* ---------- slot tabs (multi-toggle, all hidden by default) ---------- */
function renderSlotTabs() {
  const t = $('#slotTabs'); t.innerHTML = '';
  for (const sl of SLOTS) {
    const b = el('button', state.visibleSlots.includes(sl) ? 'active' : '', t);
    b.textContent = sl;
    b.onclick = () => {
      state.visibleSlots = state.visibleSlots.includes(sl)
        ? state.visibleSlots.filter((x) => x !== sl)
        : state.visibleSlots.concat(sl);
      if (state.selShell && !state.shells.some((s) => s.id === state.selShell && s.slots.some((x) => state.visibleSlots.includes(x)))) selectShell(null);
      renderSlotTabs(); buildScene(); renderShellList(); drawUV();
    };
  }
  const clear = el('button', '', t);
  clear.textContent = 'hide all';
  clear.onclick = () => { state.visibleSlots = []; selectShell(null); renderSlotTabs(); buildScene(); renderShellList(); drawUV(); };
}
$('#btnAddShell').onclick = () => {
  pushUndo();
  let i = 1, id = 'shell' + i;
  while (state.shells.some((s) => s.id === id)) id = 'shell' + (++i);
  state.shells.push({ id, parent: state.selBone || 'bodyUpper', layer: state.curLayer, uv: [16, 20], pos: [0, 4, 0], size: [4, 4, 4], rot: [0, 0, 0], inflate: 0.5, slots: state.visibleSlots.length ? state.visibleSlots.slice() : ['chest'] });
  selectShell(id); buildScene();
};
window.addEventListener('keydown', (e) => {
  if ((e.ctrlKey || e.metaKey) && (e.key === 's' || e.key === 'S')) { e.preventDefault(); saveProject(); return; }
  if ((e.ctrlKey || e.metaKey) && (e.key === 'o' || e.key === 'O')) { e.preventDefault(); loadProject(); return; }
  if ((e.ctrlKey || e.metaKey) && (e.key === 'z' || e.key === 'Z')) { e.preventDefault(); e.shiftKey ? redo() : undo(); return; }
  if ((e.ctrlKey || e.metaKey) && (e.key === 'y' || e.key === 'Y')) { e.preventDefault(); redo(); return; }
  const typing = ['INPUT', 'TEXTAREA'].includes(document.activeElement.tagName);
  if (typing) return;
  // while scaling a UV face: X / Y constrain to that axis (press again = uniform)
  if (uvDrag && uvDrag.mode === 'scale' && !state.modal) {
    const k = e.key.toLowerCase();
    if (k === 'x' || k === 'y') {
      uvDrag.axisLock = uvDrag.axisLock === k ? null : k;
      log('UV scale: ' + (uvDrag.axisLock ? uvDrag.axisLock.toUpperCase() + ' only' : 'uniform'));
      return;
    }
  }
  if (state.modal) {
    const k = e.key.toLowerCase();
    if (e.key === 'Escape') { endModal(false); return; }
    if (e.key === 'Enter') { endModal(true); return; }
    if (k === 'x' || k === 'y' || k === 'z') {
      if (e.shiftKey) { state.modal.plane = state.modal.plane === k ? null : k; state.modal.axis = null; }
      else { state.modal.axis = state.modal.axis === k ? null : k; state.modal.plane = null; }
      mountGizmo(boneEls[state.modal.shell.parent], state.modal.shell);
      applyModal(lastMouse.x, lastMouse.y, false);
      modalStatus();
      return;
    }
    if (k === 'g' || k === 's' || k === 'r') {
      startModal(k === 'g' ? 'translate' : k === 's' ? 'scale' : 'rotate');
      return;
    }
    return;
  }
  if (e.key === 'Delete' && state.selShell) {
    pushUndo();
    state.shells = state.shells.filter((s) => s.id !== state.selShell);
    selectShell(null); buildScene();
  }
  // rotate mode: X/Y/Z selects the rotation axis (ring)
  if (state.gizmoMode === 'rotate' && state.selShell && !e.ctrlKey && !e.metaKey) {    const k = e.key.toLowerCase();
    if (k === 'x' || k === 'y' || k === 'z') {
      state.rotRing = state.rotRing === k ? null : k;
      const s2 = state.shells.find((x) => x.id === state.selShell);
      if (s2) mountGizmo(boneEls[s2.parent], s2);
      modalStatusRing(state.rotRing);
      return;
    }
  }
  if (e.key === 'g' || e.key === 'G') startModal('translate');
  if (e.key === 's' || e.key === 'S') startModal('scale');
  if (e.key === 'r' || e.key === 'R') startModal('rotate');
  if ((e.ctrlKey || e.metaKey) && (e.key === 'c' || e.key === 'C') && state.selShell) {
    clipboard = JSON.parse(JSON.stringify(state.shells.find((s) => s.id === state.selShell)));
    log('Copied ' + clipboard.id);
  }
  if ((e.ctrlKey || e.metaKey) && (e.key === 'v' || e.key === 'V') && clipboard) {
    pushUndo();
    const copy = JSON.parse(JSON.stringify(clipboard));
    copy.id = clipboard.id + '_copy';
    let n = 2;
    while (state.shells.some((s) => s.id === copy.id)) copy.id = clipboard.id + '_copy' + (++n);
    copy.pos = copy.pos.slice();
    copy.pos[1] += 2;
    state.shells.push(copy);
    selectShell(copy.id); buildScene();
    log('Pasted as ' + copy.id);
  }
});
let clipboard = null;
$('#showFur').onchange = (e) => { state.showFur = e.target.checked; buildScene(); };
$('#showLayer1').onchange = (e) => { state.showL1 = e.target.checked; buildScene(); };
$('#showLayer2').onchange = (e) => { state.showL2 = e.target.checked; buildScene(); };
$('#animSel').onchange = (e) => state.anim = e.target.value;
$('#uvShowShells').onchange = (e) => { state.uvShowShells = e.target.checked; drawUV(); };
$('#uvGrid').onchange = (e) => { state.uvGrid = e.target.checked; drawUV(); };
$('#uvBg').oninput = (e) => { state.uvBg = e.target.value; drawUV(); };

/* any control change also updates the persisted session */
document.addEventListener('change', saveSession);
document.addEventListener('input', (e) => { if (e.target.type === 'color' || e.target.type === 'range') saveSession(); });
$('#btnResetIslands').onclick = () => {
  const s = state.shells.find((x) => x.id === state.selShell);
  if (!s) { log('Select a shell first'); return; }
  pushUndo();
  s.faceOffset = {};
  s.faceScale = {};
  buildScene(); drawUV();
  log('UV islands reset');
};

/* PNG export of the current texture layer */
const btnExportPng = el('button', '', $('#uvToolbar'));
btnExportPng.textContent = 'Export PNG';
btnExportPng.onclick = () => {
  const a = document.createElement('a');
  a.href = getPreset(state.preset)[state.curLayer].toDataURL();
  a.download = `${state.preset}_layer_${state.curLayer}.png`;
  a.click();
};

/* ---------- armor presets ---------- */
const PRESET_NAMES = ['placeholder', 'nano', 'diamond', 'iron', 'gold', 'thaumaturge'];
const presetSel = $('#armorPreset');
for (const n of PRESET_NAMES) {
  const o = document.createElement('option'); o.value = n; o.textContent = n; presetSel.appendChild(o);
}
presetSel.onchange = () => { state.preset = presetSel.value; buildScene(); renderShellList(); drawUV(); };

$('#texFile').onchange = async (e) => {
  for (const f of e.target.files) {
    const m = f.name.match(/layer[_-]?([12])/i);
    const layer = m ? parseInt(m[1]) : state.curLayer;
    const img = new Image();
    img.src = URL.createObjectURL(f);
    await img.decode();
    const c = getPreset(state.preset)[layer];
    c.getContext('2d').clearRect(0, 0, TEX_W, TEX_H);
    c.getContext('2d').drawImage(img, 0, 0, TEX_W, TEX_H);
    log(`Loaded ${f.name} -> ${state.preset} layer_${layer}`);
  }
  buildScene(); renderShellList(); drawUV();
};
$('#btnLoadPng').onclick = () => $('#texFile').click();

/* ---------- export / import ---------- */
function exportJSON() {
  return JSON.stringify({ version: 1, preset: state.preset, shells: state.shells }, null, 2);
}
function importJSON(txt) {
  const data = JSON.parse(txt);
  if (!Array.isArray(data.shells)) throw new Error('no shells array');
  pushUndo();
  state.shells = normalizeShells(data.shells);
  if (data.preset) { state.preset = data.preset; presetSel.value = data.preset; }
  buildScene(); renderShellList(); selectShell(null); drawUV();
}
const dlg = $('#dlgJson');
$('#btnSave').onclick = saveProject;
$('#btnLoad').onclick = loadProject;
$('#btnExportJson').onclick = () => {
  $('#dlgTitle').textContent = 'Export JSON';
  $('#dlgText').value = exportJSON();
  dlg.showModal();
};
$('#btnImportJson').onclick = () => {
  $('#dlgTitle').textContent = 'Import JSON (paste then press Apply)';
  $('#dlgText').value = '';
  const apply = el('button', 'primary', null); apply.textContent = 'Apply';
  apply.onclick = () => { try { importJSON($('#dlgText').value); dlg.close(); } catch (err) { log('Import failed: ' + err.message); } };
  $('#dlgText').after(apply);
  dlg.showModal();
};
$('#dlgCopy').onclick = () => { navigator.clipboard.writeText($('#dlgText').value); log('Copied'); };
$('#dlgClose').onclick = () => dlg.close();

const UV_FACE_ORDER = ['north', 'south', 'west', 'east', 'top', 'bottom'];
const javaName = (id) => id.replace(/_([a-z])/g, (_, c) => c.toUpperCase());

$('#btnExportJava').onclick = () => {
  const lines = [];
  lines.push('public ModelWolfmanArmor(float scale) {');
  lines.push('    super(scale);');
  lines.push('    this.textureWidth = 64;');
  lines.push('    this.textureHeight = 32;');
  for (const s of state.shells) {
    const p = s.pos, z = s.size, u = s.uv, r = s.rot || [0, 0, 0];
    const rotated = !!(r[0] || r[1] || r[2]);
    const rotArgs = rotated ? `, ${Number(r[0])}F, ${Number(r[1])}F, ${Number(r[2])}F` : '';
    const fn = rotated ? (s.uvMirror ? 'addMirroredCentred' : 'addChildCentred') : (s.uvMirror ? 'addMirrored' : 'addChild');
    const byName = {}; for (const rect of shellRects(s)) byName[rect.n] = rect;
    const uvArgs = UV_FACE_ORDER.flatMap((n) => [byName[n].x, byName[n].y, byName[n].w, byName[n].h]).join(', ');
    lines.push(`    this.${javaName(s.id)} = this.${fn}(this.${s.parent}, ${u[0]}, ${u[1]}, ${p[0]}F, ${p[1]}F, ${p[2]}F, ${z[0]}, ${z[1]}, ${z[2]}${rotArgs}, new int[] { ${uvArgs} });`);
  }
  lines.push('}');
  lines.push('');
  lines.push('public void setArmorSlot(int slot) {');
  for (const sl of SLOTS) {
    const on = state.shells.filter((s) => s.slots.includes(sl)).map((s) => s.id);
    lines.push(`    // ${sl}: ${on.length ? on.join(', ') : '(nothing)'}`);
  }
  for (const s of state.shells) {
    const cond = s.slots.map((x) => `slot == ${SLOTS.indexOf(x)}`).join(' || ');
    lines.push(`    this.${javaName(s.id)}.showModel = ${cond || 'false'};`);
  }
  lines.push('}');
  $('#dlgTitle').textContent = 'Generated Java';
  $('#dlgText').value = lines.join('\n');
  dlg.showModal();
};

/* ---------- validation ---------- */
function updateValidity() {
  const tex = getPreset(state.preset);
  const bad = state.shells.filter((s) => !boxOpaque(s.uv, s.size[0], s.size[1], s.size[2], tex[s.layer]));
  const v = $('#statValid');
  if (!bad.length) { v.className = 'ok'; v.textContent = 'All shells map opaque texels'; }
  else { v.className = 'bad'; v.textContent = `${bad.length} shell(s) sample transparent texels: ${bad.map((s) => s.id).join(', ')}`; }
}

/* ---------- built-in texture autoloader ---------- */
const BUILTIN_TEXTURES = ['nano', 'diamond', 'iron', 'gold', 'chainmail', 'leather'];

async function loadBuiltinTextures() {
  for (const name of BUILTIN_TEXTURES) {
    const set = { 1: document.createElement('canvas'), 2: document.createElement('canvas') };
    let ok = false;
    for (const layer of [1, 2]) {
      set[layer].width = TEX_W; set[layer].height = TEX_H;
      try {
        const img = new Image();
        img.src = `textures/${name}_layer_${layer}.png`;
        await img.decode();
        set[layer].getContext('2d', { willReadFrequently: true }).drawImage(img, 0, 0, TEX_W, TEX_H);
        ok = true;
      } catch (e) { set[layer] = placeholderTex(layer); }
    }
    if (ok) state.presets[name] = set;
  }
  const sel = $('#armorPreset');
  for (const o of [...sel.options]) if (BUILTIN_TEXTURES.includes(o.value) && !state.presets[o.value]) o.remove();
  state.preset = 'nano';
  sel.value = 'nano';
}

/* ---------- gizmo mode ---------- */
$('#btnResetCam').onclick = () => {
  state.orbitX = -15; state.orbitY = 30; state.zoom = 1; state.pan = [0, 0];
  log('Camera reset');
};

$('#gizmoSel').onchange = (e) => {  state.gizmoMode = e.target.value;
  state.rotRing = null;
  if (state.selShell) { const s = state.shells.find((x) => x.id === state.selShell); if (s) { buildScene(); } }
};

/* ---------- boot ---------- */
(async () => {
  await loadFurTex();
  await loadBuiltinTextures();
  const restored = restoreSession();
  fitUVZoom();
  renderSlotTabs();
  renderBoneTree();
  renderShellList();
  renderShellProps();
  buildScene();
  drawUV();
  requestAnimationFrame(frame);
  log(restored
    ? 'Session restored. Auto-reload active — edits persist across reloads.'
    : 'Real wolfman fur + vanilla/IC2 armor textures auto-loaded. Paint alpha in the UV editor to author coverage.');
})();
