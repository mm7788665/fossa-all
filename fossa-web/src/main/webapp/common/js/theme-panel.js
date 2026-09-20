/* ============================================================
 * 灰塔之下 · 管理后台 主题切换气泡 (theme-panel.js)
 * 顶栏点击 → 弹出气泡：三预设 + 自定义灰阶 6 色 + 更多
 * ============================================================ */
(function (global) {
'use strict';

function el(id) { return (typeof document !== 'undefined' && document.getElementById) ? document.getElementById(id) : null; }
function hasDocument() { return typeof document !== 'undefined' && !!document.body; }

/* 自定义灰阶：当前 theme 的灰色直接驱动顶栏色块 + 当前选中态 */
function graySwatchHTML(g) {
  return `<span style="display:inline-block;width:14px;height:14px;border-radius:4px;border:1px solid var(--line);background:${g.bg}"></span>
          <span style="display:inline-block;width:14px;height:14px;border-radius:4px;border:1px solid var(--line);background:${g.panel}"></span>
          <span style="display:inline-block;width:14px;height:14px;border-radius:4px;border:1px solid var(--line);background:${g.line}"></span>
          <span style="display:inline-block;width:14px;height:14px;border-radius:4px;border:1px solid var(--line);background:${g.primary}"></span>`;
}

function buildHTML() {
  const cur = Theme.getState();
  const g = cur.gray;
  const presets = Object.keys(Theme.PRESETS).map(m => `
    <div class="tp-item ${cur.mode===m?'on':''}" onclick="Theme.setMode('${m}')">
      <span class="tp-dot" style="background:linear-gradient(135deg,${Theme.PRESETS[m].accent},${Theme.PRESETS[m].primary})"></span>
      <span class="tp-lbl">${Theme.PRESETS[m].label}</span>
      ${m==='light'?'<span class="tp-tag">默认</span>':''}
    </div>`).join('');

  const quicks = Object.keys(Theme.GRAY_QUICK).map(n => `
    <button class="tp-chip ${isGrayMatch(g,n)?'on':''}" onclick="Theme.applyQuick('${n}')">${n}</button>`).join('');

  return `
    <div class="tp-section">
      <div class="tp-title">主题预设</div>
      <div class="tp-list">${presets}</div>
    </div>
    <div class="tp-section">
      <div class="tp-title">自定义灰阶 <span class="tp-sub">亮色 + 自选灰色</span></div>
      <div class="tp-gray-row">${graySwatchHTML(g)}<span class="tp-hex" id="tpHex">${g.primary}</span></div>
      ${['bg','panel','line','primary','txt','accent'].map(k => `
        <div class="tp-color">
          <span class="tp-clbl">${ ({bg:'背景',panel:'面板',line:'边框',primary:'主色',txt:'文字',accent:'强调'})[k] }</span>
          <input type="color" value="${g[k]}" oninput="Theme.setGray('${k}',this.value)">
          <input type="text" class="tp-chex" value="${g[k]}" onchange="Theme.setGray('${k}',this.value)">
        </div>`).join('')}
      <div class="tp-chips">${quicks}</div>
      <button class="tp-reset" onclick="Theme.resetGray()">恢复默认灰阶</button>
    </div>
    <div class="tp-section">
      <div class="tp-title">更多</div>
      <div class="tp-list">
        <div class="tp-item" onclick="location.href='#setting';navigate('setting')">
          <span class="tp-dot" style="background:var(--primary)"></span><span class="tp-lbl">完整主题设置（系统设置页）</span>
        </div>
      </div>
    </div>`;
}

function isGrayMatch(g, name) {
  const q = Theme.GRAY_QUICK[name]; if (!q) return false;
  /* 只比对影响最大的 4 项 */
  return ['bg','panel','line','primary'].every(k => (g[k]||'').toLowerCase() === (q[k]||'').toLowerCase());
}

function toggle() {
  const pop = el('themePop');
  if (!pop) return;
  if (pop.classList.contains('hidden')) {
    pop.innerHTML = buildHTML();
    pop.classList.remove('hidden');
    syncTop();
    if (hasDocument()) document.addEventListener('click', outside, true);
  } else { close(); }
}
function close() {
  const pop = el('themePop');
  if (pop) pop.classList.add('hidden');
  if (hasDocument()) document.removeEventListener('click', outside, true);
}
function outside(e) {
  const pop = el('themePop'); const btn = el('themeTrigger');
  if (pop && !pop.contains(e.target) && btn && !btn.contains(e.target)) close();
}

/* 顶栏色块：实时反映当前主题/自定义灰阶 */
function syncTop() {
  const sw = el('topSwatch'); const nm = el('topThemeName');
  if (!sw) return;
  const cur = Theme.getState();
  const g = cur.gray;
  sw.style.background = cur.mode === 'custom'
    ? `linear-gradient(135deg,${g.accent},${g.primary})`
    : `linear-gradient(135deg,${Theme.PRESETS[cur.mode].accent},${Theme.PRESETS[cur.mode].primary})`;
  if (nm) {
    nm.textContent = cur.mode === 'custom'
      ? '自定义灰' : Theme.PRESETS[cur.mode].label;
  }
}

/* 气泡内输入变化后，顶栏也要跟着变 */
function watch() {
  if (!hasDocument()) return;
  document.addEventListener('theme:change', () => { syncTop(); refreshPop(); });
}
function refreshPop() {
  const pop = el('themePop');
  if (pop && !pop.classList.contains('hidden')) pop.innerHTML = buildHTML();
}

const api = { toggle, close, syncTop, refreshPop, watch, buildHTML };
if (typeof window !== 'undefined') { window.ThemePanel = api; }
if (typeof global !== 'undefined') { global.ThemePanel = api; }
watch();
syncTop();

})(typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : {}));
