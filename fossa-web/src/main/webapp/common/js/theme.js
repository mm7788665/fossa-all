/* ============================================================
 * 灰塔之下 · 管理后台 主题引擎 (theme.js)
 * - 三套预设：light（默认）/ dark / classic
 * - 自定义灰阶：亮色 + 自选灰色，9 个灰阶实时驱动 CSS 变量
 * - 持久化到 localStorage，登录页/主界面均生效
 * ============================================================ */
(function (global) {
'use strict';

const KEY = 'huita_admin_theme_v1';

/* 三套预设的预览色（顶栏色块用） */
const PRESETS = {
  light:  { label:'亮色',   desc:'浅灰底 + 深灰文字，默认清晰', bg:'#f0f2f5', panel:'#ffffff', primary:'#344054', accent:'#3b6ea5' },
  dark:   { label:'暗夜',   desc:'深色护眼，夜间运营',           bg:'#0f1218', panel:'#181d27', primary:'#c2cad8', accent:'#6b8cff' },
  classic:{ label:'经典紫', desc:'早期版本紫调',                 bg:'#f6f4fb', panel:'#ffffff', primary:'#7c4dff', accent:'#7c4dff' }
};

/* 自定义灰阶默认值（亮色中性灰） */
const DEFAULT_GRAY = {
  bg:'#f0f2f5', panel:'#ffffff', panel2:'#f7f8fa', line:'#e4e7ec',
  txt:'#101828', sub:'#69707c', muted:'#9aa4b2',
  primary:'#344054', primary2:'#69707c', accent:'#3b6ea5'
};

/* 灰阶快捷方案 */
const GRAY_QUICK = {
  '浅灰（默认）': { bg:'#f0f2f5', panel:'#ffffff', line:'#e4e7ec', primary:'#344054' },
  '中灰':         { bg:'#e4e7ec', panel:'#f5f6f8', line:'#d0d5dd', primary:'#344054' },
  '深灰':         { bg:'#2b2f36', panel:'#363b44', line:'#4b5563', primary:'#e6e9f0' },
  '冷灰蓝':       { bg:'#eef1f6', panel:'#ffffff', line:'#dde3ec', primary:'#33475b' },
  '暖灰':         { bg:'#f2f0ec', panel:'#ffffff', line:'#e2ddd4', primary:'#4a4136' },
  '石墨':         { bg:'#1c1f24', panel:'#262a30', line:'#3a3f47', primary:'#d8dde3' }
};

let state = load();

function load() {
  try {
    const ls = typeof localStorage !== 'undefined' ? localStorage : null;
    const raw = ls ? ls.getItem(KEY) : null;
    if (raw) { const s = JSON.parse(raw); if (s && s.mode) return s; }
  } catch (e) {}
  return { mode:'light', gray: {...DEFAULT_GRAY} };
}
function persist() { try { if (typeof localStorage !== 'undefined') localStorage.setItem(KEY, JSON.stringify(state)); } catch (e) {} }

/* 把自定义灰阶对象铺成 CSS 变量（--cust-*） */
function grayToVars(g) {
  const map = {
    'bg':'--cust-bg','panel':'--cust-panel','panel2':'--cust-panel2','line':'--cust-line',
    'txt':'--cust-txt','sub':'--cust-sub','muted':'--cust-muted',
    'primary':'--cust-primary','primary2':'--cust-primary2'
  };
  const vars = {};
  for (const k in map) if (g[k]) vars[map[k]] = g[k];
  /* 派生变量：悬停/斑马线 = panel 的明度微调 */
  vars['--cust-hover']  = lighten(g.panel, g.txt, 0.04);
  vars['--cust-stripe'] = lighten(g.panel, g.txt, 0.02);
  return vars;
}

/* 简易明度混合（无需导入 color 库） */
function lighten(hex, txtHex, ratio) {
  const c = parseHex(hex) || [240,242,245];
  const t = parseHex(txtHex) || [16,24,40];
  /* 向文字色方向混一点，模拟 hover；深色背景则提亮 */
  const isDark = (c[0] + c[1] + c[2]) / 3 < 128;
  const dir = isDark ? 1 : -1;
  const out = c.map((v, i) => clamp(v + dir * Math.abs(t[i] - v) * ratio * 0.6 + (isDark ? 6 : -2)));
  return `rgb(${out[0]},${out[1]},${out[2]})`;
}
function parseHex(hex) {
  if (!hex || !/^#[0-9a-fA-F]{6}$/.test(hex)) return null;
  const n = parseInt(hex.slice(1), 16);
  return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
}
function clamp(v) { return Math.max(0, Math.min(255, Math.round(v))); }

/* 应用主题：设置 <html> 的 data-theme + 自定义变量 */
function apply() {
  const doc = typeof document !== 'undefined' ? document : null;
  if (!doc) return;
  const html = doc.documentElement;
  if (!html) return;
  const setAttr = el => { try { if (el && typeof el.setAttribute === 'function') el.setAttribute('data-theme', state.mode === 'custom' ? 'custom' : state.mode); } catch(e){} };
  if (state.mode === 'custom') {
    setAttr(html);
    const vars = grayToVars(state.gray);
    for (const k in vars) { try { html.style.setProperty(k, vars[k]); } catch(e){} }
  } else {
    /* 切回预设时清掉自定义变量，避免残留 */
    setAttr(html);
    try { const all = [...html.style]; all.forEach(p => { if (p && p.startsWith('--cust-')) html.style.removeProperty(p); }); } catch(e){}
  }
  /* 登录页也跟着变：同步到 body 一个 class 便于覆盖 */
  if (doc.body) setAttr(doc.body);
  /* 触发事件，供设置页/顶栏刷新预览 */
  try { doc.dispatchEvent(new CustomEvent('theme:change', { detail:{ state: getState() } })); } catch(e){}
}

function getState() { return JSON.parse(JSON.stringify(state)); }
function getMode() { return state.mode; }
function getGray() { return {...state.gray}; }

function setMode(mode) {
  if (!PRESETS[mode] && mode !== 'custom') mode = 'light';
  state.mode = mode;
  persist(); apply();
}

/* 更新自定义灰阶的单个字段（实时，无需点保存） */
function setGray(key, value) {
  if (!/^#[0-9a-fA-F]{6}$/.test(value)) return;
  state.gray[key] = value;
  /* 切到自定义模式：只要用户开始调灰色，就自动切 custom */
  if (state.mode !== 'custom') state.mode = 'custom';
  persist(); apply();
}

/* 一键套用某套灰阶快捷方案（仍保持 custom 模式） */
function applyQuick(name) {
  const g = GRAY_QUICK[name]; if (!g) return;
  Object.assign(state.gray, g);
  state.mode = 'custom';
  persist(); apply();
}

/* 顶部色块小三角指示当前选中 */
function presetThumb(mode) { return PRESETS[mode] || PRESETS.light; }

/* 生成"主题设置"区块的 HTML（供 renderSetting 调用） */
function themeSettingHTML() {
  const cur = getState();
  const presets = Object.keys(PRESETS).map(m => `
    <div class="preset ${cur.mode===m?'active':''}" data-mode="${m}">
      <div class="p-thumb" style="background:linear-gradient(135deg,${PRESETS[m].panel} 0%,${PRESETS[m].panel} 55%,${PRESETS[m].primary} 55%,${PRESETS[m].accent} 100%)"></div>
      <div class="p-name">${PRESETS[m].label}</div>
      <div class="p-desc">${PRESETS[m].desc}</div>
    </div>`).join('');

  const g = cur.gray;
  const control = (key, label) => `
    <div class="gray-control" data-key="${key}">
      <label>${label}</label>
      <div class="gc-row">
        <input type="color" value="${g[key]}" data-gray-key="${key}" style="width:40px;height:34px;padding:2px;border-radius:6px;cursor:pointer">
        <input type="text" value="${g[key]}" data-gray-key="${key}" style="width:90px;font-family:monospace;font-size:12px">
        <span class="gc-val" id="gc_${key}" style="background:${g[key]}"></span>
      </div>
    </div>`;

  const quicks = Object.keys(GRAY_QUICK).map(n => `<button type="button" data-quick="${n}">${n}</button>`).join('');

  return `
    <div class="theme-panel" id="themePanel">
      <div class="page-head" style="margin-bottom:14px">
        <div><h1 style="font-size:16px">🎨 界面主题</h1><div class="desc">亮色为基础，灰色可自行选取 · 修改即时生效</div></div>
      </div>
      <div class="theme-presets">${presets}</div>
      <div class="gray-preview" id="grayPreview">
        <div class="gp-swatch" id="gpSwatch" style="background:${g.primary}"></div>
        <div class="gp-info">
          <div class="gp-name" id="gpName">${cur.mode==='custom'?'自定义灰阶':'当前：'+PRESETS[cur.mode].label}</div>
          <div class="gp-hex" id="gpHex">主色 ${g.primary} · 背景 ${g.bg} · 边框 ${g.line}</div>
        </div>
        <button type="button" id="resetGrayBtn" style="flex-shrink:0">恢复默认</button>
      </div>
      <div class="gray-controls">
        ${control('bg','页面背景')}
        ${control('panel','面板/卡片')}
        ${control('line','边框/分割线')}
        ${control('primary','主色/标题')}
        ${control('txt','正文文字')}
        ${control('accent','强调色(按钮/焦点)')}
      </div>
      <div class="gray-presets"><span style="font-size:12px;color:var(--sub);align-self:center">快捷方案：</span>${quicks}</div>
      <div class="hint-inline">提示：自定义灰色会自动切换到「自定义灰阶」模式。想快速对比，可先点上方三套预设，再微调色值。</div>
    </div>`;
}

/* 每次主题变化刷新设置页的预览块（避免重绘整个页） */
function refreshPreview() {
  const doc = typeof document !== 'undefined' ? document : null;
  if (!doc || !doc.getElementById) return;
  const sw = doc.getElementById('gpSwatch');
  const nm = doc.getElementById('gpName');
  const hx = doc.getElementById('gpHex');
  const g = getGray();
  if (sw && sw.style) sw.style.background = g.primary;
  if (nm && 'textContent' in nm) nm.textContent = state.mode === 'custom' ? '自定义灰阶' : '当前：' + (PRESETS[state.mode]||{}).label;
  if (hx && 'textContent' in hx) hx.textContent = `主色 ${g.primary} · 背景 ${g.bg} · 边框 ${g.line}`;
  /* 色值小方块 */
  ['bg','panel','line','primary','txt','accent'].forEach(k => {
    const el = doc.getElementById('gc_' + k); if (el && el.style) el.style.background = g[k];
  });
  /* 预设卡高亮 */
  try { doc.querySelectorAll('.preset').forEach(el => { if(el&&el.classList) el.classList.toggle('active', el.dataset && el.dataset.mode === state.mode); }); } catch(e){}
}

function resetGray() {
  state.gray = {...DEFAULT_GRAY};
  persist(); apply(); refreshPreview();
  if (typeof globalThis !== 'undefined' && typeof globalThis.toast === 'function') globalThis.toast('已恢复默认灰阶');
}

/* 前置声明，供下方函数引用（函数在后，但会被 hoist） */
let api;

/* 启动：尽早应用（在 DOMContentLoaded 前也可安全调用） */
function init() {
  if (typeof document === 'undefined' || !document.documentElement) return api; // node 测试环境安全跳过
  try {
    apply();
    document.addEventListener('theme:change', refreshPreview);
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', apply);
    }
    /* 事件委托：替代内联 onclick，避免引号/转义问题，也便于动态重绘后不丢绑定 */
    document.addEventListener('click', onDelegate);
  } catch(e) { /* 非浏览器环境静默 */ }
  return api;
}

/* 事件委托：捕获预设卡 / 快捷方案 / 重置 / 色值输入 */
function onDelegate(e) {
  const t = e.target;
  if (!t || !t.closest) return;
  const preset = t.closest('.preset[data-mode]');
  if (preset && preset.dataset) { setMode(preset.dataset.mode); renderSettingTheme(); return; }
  const quick = t.closest('[data-quick]');
  if (quick && quick.dataset) { applyQuick(quick.dataset.quick); renderSettingTheme(); return; }
  if (t.id === 'resetGrayBtn') { resetGray(); renderSettingTheme(); return; }
  const colorInput = t.closest('input[data-gray-key]');
  if (colorInput && colorInput.dataset && t.value && /^#[0-9a-fA-F]{6}$/.test(t.value)) {
    setGray(colorInput.dataset.grayKey, t.value); renderSettingTheme();
  }
}

/* 重绘设置页的主题区块（setMode/setGray 后调用，保持预览实时） */
function renderSettingTheme() {
  const panel = typeof document !== 'undefined' ? document.getElementById('themePanel') : null;
  if (!panel) return;
  const html = themeSettingHTML();
  panel.outerHTML = html; // 替换整块，事件由 document 委托接管
}

api = { init, apply, getState, getMode, getGray, setMode, setGray, applyQuick, resetGray,
  themeSettingHTML, refreshPreview, PRESETS, GRAY_QUICK, DEFAULT_GRAY };

/* 兼容浏览器 / jsdom / node 测试三种环境 */
if (typeof window !== 'undefined') { window.Theme = api; }
if (typeof global !== 'undefined') { global.Theme = api; }
if (typeof window === 'undefined' && typeof global !== 'undefined') {
  global.localStorage = global.localStorage || { _s:{}, getItem(k){return this._s[k]||null;}, setItem(k,v){this._s[k]=String(v);}, removeItem(k){delete this._s[k];}, clear(){this._s={};} };
}
init();

})(typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : {}));
