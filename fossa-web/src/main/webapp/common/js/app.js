/* ============================================================
 * 灰塔之下 · 管理后台 主逻辑 (app.js)
 * 登录 / 路由 / Dashboard / NPC / 场次 / 订单 / 玩家 / 亲密度 / 榜单 / 公告 / 设置
 * ============================================================ */
(function(){
  'use strict';

  /* ==================== 工具 ==================== */
  const $ = (s, el=document)=>{
    if (!el) return nullProxy;
    const e = el.querySelector(s);
    return e || nullProxy;
  };
  const $$ = (s, el=document)=> el ? [...el.querySelectorAll(s)] : [];

  /* 找不到元素时的安全代理：所有赋值/调用静默成功，避免「Cannot set of null」级联白屏 */
  /* nullProxy 的 .value/.checked 等一律返回安全原始值，
     杜绝 "valOf($('##x'))" 拿到 function 后传给 includes 崩溃 */
  const nullProxy = new Proxy(function(){}, {
    get(t,k){
      if(k==='style'||k==='classList')return nullProxy;
      if(k==='value'||k==='checked'||k==='selectedIndex'||k==='length')return '';
      return nullProxy;
    },
    set(){ return true; },
    apply(){ return nullProxy; }
  });
  /* 安全取值工具：统一把 "valOf($('##x'))" 这类用法规范化 */
  function valOf(el, def=''){ return (el && typeof el.value !== 'undefined' && typeof el !== 'function') ? String(el.value) : def; }
  function toStr(v, def=''){ return (v == null || (typeof v === 'function') || (v instanceof RegExp)) ? def : String(v); }
  function toast(msg, type='ok'){
    const t=$('#toast'); if(!t || t===nullProxy) return;
    t.textContent=msg; t.className='toast '+type;
    clearTimeout(t._t); t._t=setTimeout(()=>{ if(t)t.className='toast hidden'; }, 2200);
  }
  function esc(s){ return String(s==null?'':s).replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])); }
  function campBadge(c){
    const map={ '灰塔官方派':'<span class="badge badge-tower">灰塔</span>', '破晓反抗军':'<span class="badge badge-dawn">破晓</span>', '无阵营':'<span class="badge">密</span>' };
    return map[c]||'<span class="badge">'+esc(c||'')+'</span>';
  }
  function statusBadge(s){
    const map={ '启用':'badge-ok','在售':'badge-ok','已支付':'badge-ok','发布':'badge-ok','预售':'badge-warn','满员':'badge-warn','草稿':'badge-draft','待付款':'badge-warn','待核销':'badge-warn','已退款':'badge','已核销':'badge' };
    const cls=map[s]||'badge';
    return `<span class="badge ${cls}">${esc(s||'')}</span>`;
  }
  function bar(v){ v=Math.max(0,Math.min(100,+v||0)); return `<div class="bar"><i style="width:${v}%"></i></div> <b>${v}</b>`; }
  /* 后端字段类型可能不一致（如 tags 有时是 "a,b" 有时是数组），统一成数组 */
  function toArr(v, sep){
    if (Array.isArray(v)) return v;
    if (v == null || v === '') return [];
    if (typeof v === 'string') return v.split(sep||/,/).map(s=>s.trim()).filter(Boolean);
    return [v];
  }
  function fmtDate(d){ return new Date(d).toLocaleString('zh-CN',{hour12:false}); }

  /* ==================== 导航配置 ==================== */
  const NAV = [
    { group:'核心概览', items:[
        { key:'dashboard', icon:'📊', label:'控制台' }
      ]},
    { group:'内容管理', items:[
        { key:'npc', icon:'🎭', label:'角色管理' },
        { key:'show', icon:'🎬', label:'场次管理' },
        { key:'notice', icon:'📢', label:'公告管理' }
      ]},
    { group:'业务运营', items:[
        { key:'order', icon:'🧾', label:'订单管理' },
        { key:'player', icon:'👥', label:'玩家管理' },
        { key:'gacha', icon:'🎰', label:'抽卡奖池' }
      ]},
    { group:'数据与榜单', items:[
        { key:'intimacy', icon:'💞', label:'亲密度管理' },
        { key:'rank', icon:'🏆', label:'排行榜数据' }
      ]},
    { group:'系统', items:[
        { key:'setting', icon:'⚙️', label:'系统设置', perm:'setting:view' },
        { key:'role',    icon:'🔐', label:'角色权限', perm:'role:view' },
        { key:'audit',   icon:'📜', label:'操作日志', perm:'audit:view' }
      ]}
  ];
  /* 每个页面需要的查看权限（用于菜单显示与访问拦截） */
  const PAGE_PERM = {
    dashboard:'dashboard:view', npc:'npc:view', show:'show:view', notice:'notice:view',
    order:'order:view', player:'player:view', gacha:'gacha:view',
    intimacy:'intimacy:view', rank:'dashboard:view', setting:'setting:view',
    role:'role:view', audit:'audit:view'
  };
  /* 当前登录者的权限集合（登录后填充；含 '*' 表示超级管理员） */
  let MY_PERMS = [];
  function canView(key){
    const need = PAGE_PERM[key];
    if (!need) return true;
    if (!MY_PERMS.length) return true;   // ★ 权限未加载时不锁死，宁可先放行
    if (MY_PERMS.indexOf('*') >= 0) return true;
    return MY_PERMS.indexOf(need) >= 0;
  }
  function hasPerm(code){
    if (!code) return true;
    if (!MY_PERMS.length) return true;   // ★ 同上
    if (MY_PERMS.indexOf('*') >= 0) return true;
    if (MY_PERMS.indexOf(code) >= 0) return true;
    const i = code.indexOf(':');
    return i > 0 && MY_PERMS.indexOf(code.substring(0, i) + ':*') >= 0;
  }

  /* ==================== 路由 ==================== */
  const ROUTES = {
    dashboard:renderDashboard,
    npc:renderNpc, show:renderShow, notice:renderNotice,
    order:renderOrder, player:renderPlayer,
    intimacy:renderIntimacy, rank:renderRank,
    gacha:renderGacha,
    setting:renderSetting,
    role:renderRole,
    audit:renderAudit
  };

  let CURRENT = 'dashboard';

  /* ==================== 后端数据同步层 ==================== */
  /* 思路：渲染层完全不动，只把「DB.* 的数据来源」从 localStorage 换成后端接口，
   * 并把「增删改」从本地写数组换成调用接口 + 回拉最新数据。 */
  /* ★ 关键：必须「运行时动态判定」，不能在加载时快照。
   * 曾踩坑：api.js 晚于 app.js 加载时，快照会永久判成 false，
   * 导致弹窗显示 enabled=true 却显示 useApi()=否 的自相矛盾现象。 */
  /* ---------- 头像渲染：图片链接显示图片，否则按 Emoji/文字显示 ----------
   * player.avatar 在库里可能是 Emoji（🐱），也可能是图片地址：
   *   https://.../a.png 、 //cdn/a.jpg 、 /uploads/a.png 、 assets/avatar/1.png 、 data:image/...
   * 统一在这里判断，一处改动全站生效。图片加载失败自动回退成文字/Emoji。 */
  const IMG_EXT = /\.(png|jpe?g|gif|webp|svg|bmp|avif|ico)(\?.*)?$/i;
  function isImgUrl(v){
    const s = String(v == null ? '' : v).trim();
    if (!s) return false;
    if (/^(https?:)?\/\//i.test(s)) return true;   // http(s):// 或协议相对 //
    if (/^data:image\//i.test(s))    return true;   // base64 内联图片
    if (IMG_EXT.test(s))             return true;   // 带图片扩展名（含相对路径）
    return false;
  }
  /* ---------- 头像尺寸（想再大/再小，只改这里，全站生效） ---------- */
  const AVATAR_SIZE = {
    table: 40,   // 玩家管理表格（主列表，最大）
    list : 32,   // 亲密度「攻略者排行」列表
    rank : 28    // 排行榜两个榜单
  };

  /* 渲染头像。size = 像素边长；fallback = 图片加载失败时显示的文字（默认原值或 🙂） */
  function avatarHtml(v, size, fallback){
    const n = +size || AVATAR_SIZE.table;
    const raw = String(v == null ? '' : v).trim();
    /* 回退文案：显式传了就用传的；否则图片用 🙂（避免出现一长串 URL），非图片用原值 */
    const fbRaw = (fallback == null || fallback === '')
        ? (isImgUrl(raw) ? '🙂' : (raw || '🙂'))
        : fallback;
    const fb  = esc(String(fbRaw));
    const style = `width:${n}px;height:${n}px;border-radius:50%;object-fit:cover;`
        + `display:inline-block;vertical-align:middle;background:var(--panel2);`
        + `border:1px solid var(--line);box-shadow:0 1px 3px rgba(0,0,0,.12)`;
    if (isImgUrl(raw)){
      return `<img src="${esc(raw)}" alt="${fb}" style="${style}"
      onerror="this.outerHTML='<span style=\'font-size:'+Math.round(${n}*0.9)+'px;line-height:1;display:inline-block;vertical-align:middle\'>${fb}</span>'">`;
    }
    const txt = esc(raw || '🙂');
    return `<span style="font-size:${n}px;line-height:1;display:inline-block;vertical-align:middle">${txt}</span>`;
  }
  function useApi(){ return !!(typeof API !== 'undefined' && API && API.enabled !== false); }
  function pickF(){ return (useApi() && API.helpers) ? API.helpers.pick : ()=>undefined; }

  const SYNCED = {};              // 资源是否已拉过（避免切页重复请求）
  const ALWAYS = { stats:true };  // 每次进入都要重新拉的资源
  let OFFLINE  = false;           // 后端不可达标记

  /* 每个页面需要哪些资源 */
  const NEED = {
    dashboard:['stats','npcs','shows','orders','notices','players'],
    npc      :['npcs'],
    show     :['shows','npcs'],
    notice   :['notices'],
    order    :['orders','shows','statusOptions'],
    player   :['players','npcs','intimacy'],
    intimacy :['npcs','players','intimacy'],
    rank     :['npcs','players','intimacy'],
    gacha    :['gacha','npcs'],
    setting  :['settings']
  };

  function loadingHtml(txt){ return `<div class="card"><div class="empty">⏳ ${esc(txt||'加载中')}…</div></div>`; }

  /* 顶栏接口状态灯：一眼看出数据到底来自后端还是本地 */
  function updateApiState(state){
    if (typeof document === 'undefined' || !document.getElementById) return;
    let el = document.getElementById('apiState');
    if (!el){
      /* index.html 未同步（没有 #apiState）时动态插一个。
         注意：这里必须用原生 DOM API——$() 找不到元素会返回 nullProxy，
         它的 appendChild 是 truthy 的，会让"插入失败"被静默吞掉。 */
      const bar = document.querySelector('.topbar-actions') || document.querySelector('.topbar') || null;
      if (!bar) return;
      el = document.createElement('span');
      el.id = 'apiState';
      el.style.cssText = 'display:flex;align-items:center;gap:5px;font-size:12px;padding:4px 9px;border-radius:8px;border:1px solid var(--line);background:var(--panel2)';
      bar.insertBefore(el, bar.firstChild);
    }
    const map = {
      on    : ['🟢', '接口已连接', 'var(--ok)',      '数据来自后端 /fossa/api'],
      off   : ['🔴', '接口未连接', 'var(--danger)',  '已回退本地演示数据（点击可重试）'],
      mixed : ['🟡', '部分接口失败', 'var(--warn)',  '部分数据来自本地（点击可重试）'],
      off_  : ['⚪', '接口未启用', 'var(--muted)',   'API.enabled=false，纯静态模式'],
      wait  : ['⏳', '正在拉取…',  'var(--sub)',     '正在请求后端接口']
    };
    const [ic, txt, color, tip] = map[state] || map.off_;
    el.textContent = ic + ' ' + txt;
    el.style.color = color;
    el.title = tip + '｜点击查看接口自检结果';
    el.style.cursor = 'pointer';
    el.onclick = ()=>{
      const has = (typeof API!=='undefined' && !!API);
      const s = has ? API : {};
      /* 注意：绝不能写 (s.enabled!==false)——api.js 没加载时 s 是空对象，
         会得出 enabled=true，把"未加载"误报成"已启用"，误导排查。 */
      const cnt = (typeof DB!=='undefined' && DB) ? DB : {};
      alert(
          (has ? '' : '❌ api.js 未加载！页面是纯静态数据，不会请求后端。\n\n') +
          '接口层已加载：' + (has ? '是' : '否 ← 先补传 api.js 并改 index.html') +
          '\n实际上路(USE_API)：' + (useApi() ? '是' : '否') +
          '\n接口基址：' + (has ? s.base : '—') +
          '\n启用 enabled：' + (has ? (s.enabled !== false) : '—') +
          '\n失败回退 fallback：' + (has ? (s.fallback !== false) : '—') +
          '\n后端不可达：' + (OFFLINE ? '是' : '否') +
          '\n最近错误：' + (has ? (s.lastError || '无') : '—') +
          '\n\n当前页面数据量：NPC ' + ((cnt.NPCS||[]).length) + ' · 场次 ' + ((cnt.SHOWS||[]).length) +
          ' · 订单 ' + ((cnt.ORDERS||[]).length) + ' · 公告 ' + ((cnt.NOTICES||[]).length) +
          ' · 玩家 ' + ((cnt.PLAYERS||[]).length) +
          '\n已同步：' + Object.keys(SYNCED).filter(k=>SYNCED[k]).join(',') +
          '\n\n判断：' + (!has ? '补传 api.js'
              : (OFFLINE ? '接口调用失败（看上面"最近错误"）'
                  : '数据来自后端 ✅')) +
          '\n\n详细自检：/fossa/common/api-check.html'
      );
    };
  }

  function markOffline(e){
    OFFLINE = true;
    if (useApi() && API && !API.fallback) throw e;   // 严格模式：向上抛，由调用方 toast
  }

  /* 把后端返回的玩家行补成本地结构（缺字段用本地/默认值兜底） */
  function mergePlayer(p){
    const old = DB.PLAYERS.find(x=>x.id===String(pickF()(p,['id','playerId'],'')) )||{};
    return Object.assign({}, old, {
      id    : String(pickF()(p,['id','playerId'],old.id||'')),
      name  : pickF()(p,['name','nickname','userName'],old.name||''),
      tag   : pickF()(p,['tag','playerTag'],old.tag||''),
      avatar: pickF()(p,['avatar','avatarUrl'],old.avatar||'🙂'),
      phone : pickF()(p,['phone','mobile'],old.phone||''),
      reg   : pickF()(p,['reg','createTime','created','regTime'],old.reg||''),
      vip   : pickF()(p,['vip','vipLevel'],old.vip||'普通'),
      status: pickF()(p,['status','state'],old.status||'正常')
    });
  }

  /* 各资源的拉取 + 写入 DB */
  /* 后端「表是空的」标记：接口通了但没数据。
   * 与「接口不通」是两种完全不同的情况，必须分开提示，
   * 否则用户会一直以为没接上后端（本次实际就是这个坑：/npcs 返回 0 条）。 */
  const EMPTY = {};
  function markEmpty(name, isEmpty){
    if (isEmpty) EMPTY[name]=true; else delete EMPTY[name];
  }

  const PULLERS = {
    /* ★ 纯后端模式：无条件覆盖。后端返回空数组就显示空，
     *   绝不保留 data.js 里的本地演示数据（曾因此误判为"没接上后端"）。 */
    npcs : async ()=>{ const l=await API.pull.npcs();      markEmpty('npcs', !(l&&l.length));    DB.NPCS=l||[]; },
    shows: async ()=>{ const l=await API.pull.shows();     markEmpty('shows', !(l&&l.length));   DB.SHOWS=l||[]; },
    orders:async ()=>{ const l=await API.pull.orders();    markEmpty('orders', !(l&&l.length));  DB.ORDERS=l||[]; },
    notices:async()=>{ const l=await API.pull.notices();   markEmpty('notices', !(l&&l.length)); DB.NOTICES=l||[]; },
    players:async()=>{ const l=await API.pull.players();   markEmpty('players', !(l&&l.length)); DB.PLAYERS=(l||[]).map(mergePlayer); },
    settings:async()=>{ const s=await API.pull.settings(); markEmpty('settings', !(s&&Object.keys(s).length)); DB.SETTINGS=(s&&typeof s==='object')?s:{}; },
    statusOptions:async()=>{ const l=await API.pull.statusOptions(); if(l && l.length) DB.STATUS_OPTIONS=l; },
    gacha: async ()=>{
      /* 旧版 api.js 没有 API.pull.gacha —— 直接判为不支持，交给页面提示，不抛错 */
      if (typeof API.pull.gacha !== 'function'){ DB.GACHA_POOLS=[]; return; }
      const l=await API.pull.gacha(); markEmpty('gacha', !(l&&l.length)); DB.GACHA_POOLS=l||[];
      if (typeof API.pull.gachaRarity === 'function'){
        const r=await API.pull.gachaRarity().catch(()=>null);
        if(r) DB.GACHA_RARITY_RATE=r;
      }
      /* ★ 稀有度定义：卡牌表单的「稀有度」下拉、概率罗盘、模拟器都要用。
       *   必须在这里加载，否则下拉是空的（配置源是 gacha_rarity 表）。 */
      await loadGachaRarity();
    },
    stats: async ()=>{ const s=await API.pull.stats();     if(s) DB.STATS=API.normStats(s); },
    intimacy: async ()=>{
      /* ★ 亲密度依赖 NPC 列表（要按每个 npcId 拉 by-npc）。
       * 并发拉取时若 npcs 还没回来，npcIds 会是空数组 → 被误判成"后端亲密度表为空"。
       * 所以这里先确保 NPC 已同步。 */
      if (!SYNCED.npcs) await pull('npcs').catch(()=>{});
      const npcIds = DB.NPCS.map(n=>n.id);
      const r = await API.pull.intimacy(npcIds, DB.PLAYERS);
      const ids = Object.keys(r.map||{});
      if (!ids.length){                       // 后端暂无数据 → 清空，不保留本地种子
        markEmpty('intimacy', true);
        DB.INTIMACY.players=[]; DB.INTIMACY.mine={}; return;
      }
      markEmpty('intimacy', false);
      const base = DB.INTIMACY.players;
      DB.INTIMACY.players = ids.map(pid=>{
        const old = base.find(p=>p.id===pid)||{};
        const meta = (r.meta&&r.meta[pid])||{};
        return Object.assign({}, old, {
          id:pid, name:meta.name||old.name||pid,
          avatar:meta.avatar||old.avatar||'🙂', tag:meta.tag||old.tag||'',
          npc: Object.assign({}, old.npc, r.map[pid])
        });
      }).concat(base.filter(p=>!r.map[p.id]));
      const me = DB.INTIMACY.players.find(p=>p.id==='me');
      if (me) DB.INTIMACY.mine = Object.assign({}, me.npc);
    }
  };

  /* ★ 纯后端模式：清空 data.js 的本地演示数据 + 禁用 localStorage 持久化。
   * 目的：页面上的每一条数据都来自数据库，避免"后端 0 条却显示 6 条演示数据"的误判。
   * 注意 mergePlayer 等依赖旧值做字段兜底，所以这里只清「业务数据」，不动原型方法。 */
  function purgeLocalSeed(){
    DB.NPCS=[]; DB.SHOWS=[]; DB.ORDERS=[]; DB.NOTICES=[]; DB.PLAYERS=[];
    DB.INTIMACY={ mine:{}, players:[] };
    DB.STATUS_OPTIONS=[]; DB.STATS=null;
    DB.SETTINGS={};
    /* 清掉历史 localStorage 存档，防止下次刷新又把演示数据 seed 回来 */
    try{ if(typeof localStorage!=='undefined') localStorage.removeItem('huita_admin_db_v1'); }catch(e){}
    /* 后端模式下不再把数据写回 localStorage（否则会留下与数据库不一致的脏缓存） */
    try{ DB.save=function(){}; if(typeof HT!=='undefined'&&HT) HT.save=function(){}; }catch(e){}
  }

  /* 拉单个资源（带本地回退），返回是否成功 */
  async function pull(name){
    if (!useApi()) return false;
    try{
      await PULLERS[name]();
      SYNCED[name]=true;
      return true;              // 成功：不在这里清 OFFLINE（并发会互相覆盖），由调用方统一结算
    }catch(e){
      markOffline(e);
      if (useApi() && API && API.fallback){
        SYNCED[name]=false; console.warn('[sync] '+name+' 失败：'+e.message); return false;
      }
      throw e;
    }
  }

  /* 进入页面前，把该页需要的资源拉齐 */
  async function syncFor(key){
    if (!useApi()) return;
    const need = (NEED[key]||[]).filter(n=>!SYNCED[n] || ALWAYS[n]);
    if (!need.length) return;
    await Promise.all(need.map(n=>pull(n).catch(()=>{})));
  }

  /* 写操作后，把受影响资源标脏并重新拉一次 */
  async function resync(){
    let okAll = true;
    for (let i=0;i<arguments.length;i++) SYNCED[arguments[i]]=false;
    if (!useApi()) return;
    for (let i=0;i<arguments.length;i++){
      const ok = await pull(arguments[i]).catch(()=>false);
      if (!ok) okAll = false;
    }
    OFFLINE = !okAll;
    return okAll;
  }

  /* ==================== 后端空表提示 ====================
   * 纯后端模式下，空表就是空表：页面显示"暂无数据"，不再用本地演示数据填充。
   * 这里只做提示，告诉用户去数据库补数据。 */
  const EMPTY_LABEL = { npcs:'角色', shows:'场次', orders:'订单', notices:'公告', players:'玩家', intimacy:'亲密度', settings:'系统设置' };

  /* 控制台顶部：后端空表提示卡 */
  function emptyTableCard(){
    const names = Object.keys(EMPTY).filter(k=>EMPTY_LABEL[k]);
    if (!useApi() || !names.length) return '';
    return `<div class="card" style="margin-top:16px;border-color:var(--warn);background:rgba(181,71,8,.06)">
    <div class="page-head" style="margin-bottom:10px">
      <div><h1 style="font-size:15px">🟠 接口已连通，但数据库中这些表还没有数据</h1>
        <div class="desc">接口返回 200 无报错，记录数为 0 —— 页面不会用演示数据填充，请往数据库写入数据后点「重新拉取后端数据」</div></div>
    </div>
    <div style="font-size:13px;margin-bottom:12px;color:var(--txt)">
      空表：${names.map(k=>`<span class="badge badge-warn" style="margin-right:6px">${EMPTY_LABEL[k]||k}</span>`).join('')}
    </div>
    <div style="display:flex;gap:10px;flex-wrap:wrap">
      <button class="btn-ghost" onclick="refreshData()">重新拉取后端数据</button>
      <span style="align-self:center;font-size:12px;color:var(--sub)">提示：可直接执行 INSERT，或在对应 Controller 增加 POST 新增接口后用页面表单录入</span>
    </div>
  </div>`;
  }

  /* 通用空态：纯后端模式下统一显示"数据库中暂无数据" */
  function emptyRow(txt){
    return `<div class="empty">${esc(txt||'数据库中暂无数据')}</div>`;
  }

  /* 写操作统一兜底：失败时 toast 后端返回的 msg，不改动页面数据 */
  async function withApi(label, fn){
    try{ await fn(); return true; }
    catch(e){ toast(e && e.message ? e.message : (label+'失败'), 'err'); return false; }
  }

  /* 登录后一次性全量同步 */
  async function bootSync(){
    if (!useApi()){ updateApiState('off_'); return; }
    updateApiState('wait');
    const all = ['npcs','shows','orders','notices','players','settings','statusOptions','intimacy','stats'];
    const res = await Promise.all(all.map(n=>pull(n).catch(()=>false)));
    const failed = all.filter((n,i)=>!res[i]);
    OFFLINE = failed.length > 0;
    if (!failed.length){
      updateApiState('on');
      const empt = Object.keys(EMPTY).filter(k=>EMPTY_LABEL[k]);
      toast(empt.length ? ('接口已连接，但后端空表：'+empt.map(k=>EMPTY_LABEL[k]).join('/')+'（显示本地演示数据）')
          : ('已连接后端接口，'+all.length+' 项数据已同步'), empt.length?'err':'ok');
    }
    else if (failed.length === all.length){
      updateApiState('off');
      toast('后端接口全部未通（'+(API.lastError||'请检查服务是否启动')+'），当前显示本地数据','err');
    } else {
      updateApiState('mixed');
      toast('部分接口未通：'+failed.join('/')+'，这些数据回退本地','err');
    }
  }

  function renderNav(){
    const box=$('#nav'); box.innerHTML='';
    NAV.forEach(g=>{
      const gt=document.createElement('div'); gt.className='nav-group-title'; gt.textContent=g.group;
      box.appendChild(gt);
      /* ★ 按权限过滤菜单：没有查看权限的模块直接不显示 */
      const items = g.items.filter(it => !it.perm || canView(it.key));
      if (!items.length) return;          // 整组都无权限时连分组标题也不显示
      items.forEach(it=>{
        const a=document.createElement('div');
        a.className='nav-item'+(it.key===CURRENT?' active':'');
        a.innerHTML=`<span class="ni-ic">${it.icon}</span><span>${it.label}</span>`;
        a.onclick=()=>navigate(it.key);
        box.appendChild(a);
      });
    });
  }

  let NAV_TOKEN=0;
  async function navigate(key){
    if(!ROUTES[key])key='dashboard';
    /* ★ 无查看权限时直接挡回控制台，防止手动改 hash 越权 */
    if(!canView(key)){ toast('没有该模块的查看权限','err'); key='dashboard'; }
    CURRENT=key;
    renderNav();
    const titles={ dashboard:'控制台', npc:'角色管理', show:'场次管理', notice:'公告管理',
      order:'订单管理', player:'玩家管理', intimacy:'亲密度管理', rank:'排行榜数据', setting:'系统设置',
      role:'角色权限', audit:'操作日志' };
    $('#pageTitle').textContent=titles[key]||'';
    const view=$('#view');
    view.scrollTop=0;
    closeModal();

    const token=++NAV_TOKEN;
    // 有未拉取的资源时先显示 loading（已拉过则直接渲染，不闪屏）
    const pending=(NEED[key]||[]).some(n=>!SYNCED[n]||ALWAYS[n]);
    if(useApi() && pending) view.innerHTML=loadingHtml('加载中');
    await syncFor(key);
    if(token!==NAV_TOKEN) return; // 已被更新的导航覆盖，丢弃本次渲染

    try{ ROUTES[key](); }catch(e){ console.error(e); view.innerHTML=`<div class="card">渲染错误：${esc(e.message)}</div>`; }
  }

  /* ==================== 登录（对接后端 /fossa/api/login） ==================== */
  /* 后端：AuthController —— @RestController @RequestMapping("/api") + @PostMapping("/login")
   *      入参 LoginRequest { user, pass }（javax.validation 校验，空值会返回 400）
   *      出参 R<?> —— 统一结构 { code, msg, data }，data = { user, role, name }
   *      校验失败 / 账号密码错误：AuthService 抛异常 → 由全局异常处理返回对应 msg
   */
  const LOGIN_API = '/fossa/api/login'; // 上下文路径 /fossa + /api/login
  const LOGIN_TIMEOUT = 8000;           // 请求超时（毫秒）
  const LOGIN_METHOD = 'POST';
  /* 离线兜底：true = 接口不可用（后端没起/网络不通）时，回退到 data.js 的本地演示账号；
   * 正式联调与生产环境请设为 false，避免出现"绕过后端校验"的假登录 */
  const LOGIN_FALLBACK_LOCAL = false;

  /* 调用登录接口，返回 { user, role, name }；失败抛 Error（message 直接展示给用户） */
  async function requestLogin(user, pass){
    const ctrl = (typeof AbortController !== 'undefined') ? new AbortController() : null;
    const timer = ctrl ? setTimeout(()=>{ try{ ctrl.abort(); }catch(e){} }, LOGIN_TIMEOUT) : null;
    try{
      const res = await fetch(LOGIN_API, {
        method: LOGIN_METHOD,
        credentials: 'same-origin',
        headers: { 'Content-Type':'application/json', 'Accept':'application/json' },
        body: JSON.stringify({ user:user, pass:pass }),
        signal: ctrl ? ctrl.signal : undefined
      });
      /* 后端返回的不是 JSON（如网关 502 HTML、未配置的 404 页面）时降级为 null */
      const json = await res.json().catch(()=>null);

      if(!res.ok){
        throw new Error((json && (json.msg || json.message || json.error)) || ('登录失败（HTTP ' + res.status + '）'));
      }
      if(json && typeof json === 'object'){
        /* 兼容多种 R 结构：{code:200,data:{...}} / {code:0,...} / {success:true,...} / 直接返回 data 本体 */
        const code = json.code;
        const failed = (code !== undefined && code !== null && code !== 0 && code !== 200 && String(code) !== '200') && json.success !== true;
        if(failed) throw new Error(json.msg || json.message || '账号或密码错误');
        const acc = (json.data && typeof json.data === 'object') ? json.data : json;
        if(!acc || (!acc.user && !acc.name)) throw new Error('登录响应缺少用户信息');
        return { user: acc.user || user, role: acc.role || '运营编辑', name: acc.name || acc.user || user };
      }
      throw new Error('登录响应格式异常');
    }catch(e){
      if(e && e.name === 'AbortError') throw new Error('登录超时，请检查网络或后端服务');
      if(e instanceof TypeError || (e && e.name==='TypeError')) throw new Error('无法连接服务器：' + LOGIN_API);
      /* 接口不可用时可选回退本地演示账号（默认关闭） */
      if(LOGIN_FALLBACK_LOCAL){
        const local = DB.ACCOUNTS.find(a=>a.user===user && a.pass===pass);
        if(local) return { user:local.user, role:local.role, name:local.name };
      }
      throw e;
    }finally{
      if(timer) clearTimeout(timer);
    }
  }

  /* ---------- 修改密码 ---------- */
  function openChangePwd(force){
    openModal(`<div class="modal-head"><h2>修改密码</h2><button class="modal-close" onclick="${force?'':'closeModal()'}">${force?'':'×'}</button></div>
    ${force?`<div class="rate-warn" style="margin-bottom:12px">⚠ 你正在使用默认密码，建议立即修改。</div>`:''}
    <div class="field"><label>原密码 *</label><input id="cp_old" type="password" placeholder="当前使用的密码"></div>
    <div class="field"><label>新密码 *</label><input id="cp_new" type="password" placeholder="至少 6 位">
      <span class="gacha-hint">不要用 123456、admin 这类弱密码</span></div>
    <div class="field"><label>确认新密码 *</label><input id="cp_new2" type="password" placeholder="再输一次"></div>
    <div class="modal-foot">
      ${force?'<button class="btn-ghost" onclick="tryCloseModal()">稍后再说</button>':'<button class="btn-ghost" onclick="tryCloseModal()">取消</button>'}
      <button class="btn-primary" id="cp_ok" onclick="submitChangePwd(${force?1:0})">保存</button>
    </div>`);
  }
  async function submitChangePwd(force){
    const o = $('#cp_old').value.trim();
    const n = $('#cp_new').value.trim();
    const n2= $('#cp_new2').value.trim();
    if (!o){ toast('请输入原密码','err'); return; }
    if (!n){ toast('请输入新密码','err'); return; }
    if (n.length < 6){ toast('新密码至少 6 位','err'); return; }
    if (n !== n2){ toast('两次输入的新密码不一致','err'); return; }
    if (n === o){ toast('新密码不能与原密码相同','err'); return; }
    const btn = $('#cp_ok');
    if (btn){ btn.disabled = true; btn.textContent = '保存中…'; }
    try{
      await API.auth.changePwd(o, n);
      closeModal();
      toast('密码已修改，其它设备已下线','ok');
    }catch(e){
      toast(e.message || '修改失败','err');
      if (btn){ btn.disabled = false; btn.textContent = '保存'; }
    }
  }

  /* 401：会话失效 → 清登录态并回登录页 */
  function onUnauthorized(msg){
    if (typeof toast === 'function') toast(msg || '登录已失效，请重新登录', 'err');
    try{ sessionStorage.removeItem('ht_login'); sessionStorage.removeItem('ht_perms'); }catch(e){}
    MY_PERMS = [];
    setTimeout(()=>{ if (typeof location !== 'undefined' && location.reload) location.reload(); }, 800);
  }
  if (typeof API !== 'undefined' && API) API.onUnauthorized = onUnauthorized;

  /* ---------- 权限自查：控制台执行 __diagPerm() ---------- */
  function __diagPerm(){
    const lines = [];
    const P = MY_PERMS || [];
    lines.push('权限数量：' + P.length + (P.length ? '' : '   ← 空！页面会被挡'));
    lines.push('含通配 * ：' + (P.indexOf('*') >= 0 ? '是（超级管理员）' : '否'));
    lines.push('权限列表：' + (P.length ? P.join(', ') : '(无)'));
    lines.push('');
    lines.push('页面可见性：');
    Object.keys(PAGE_PERM).forEach(k=>{
      lines.push('  ' + (canView(k) ? 'OK  ' : 'NO  ') + k + '  (需要 ' + PAGE_PERM[k] + ')');
    });
    let sp = '(无)';
    try{ sp = sessionStorage.getItem('ht_perms') || '(无)'; }catch(e){ sp = '(读取失败)'; }
    lines.push('');
    lines.push('sessionStorage.ht_perms = ' + sp);
    lines.push('');
    lines.push('排查建议：');
    if (!P.length){
      lines.push('  1) 退出重新登录一次（权限在登录时下发）');
      lines.push('  2) 查账号角色是不是英文（跑过迁移 SQL 才是 super）：');
      lines.push('     SELECT user, role FROM admin_account;');
      lines.push('  3) 查该角色有没有权限点：');
      lines.push('     SELECT * FROM admin_role_perm WHERE role_key=(SELECT role FROM admin_account WHERE user=\'admin\');');
    } else if (P.indexOf('*') < 0 && P.indexOf('role:view') < 0){
      lines.push('  当前角色没有 role:view / audit:view，执行：');
      lines.push('  INSERT INTO admin_role_perm VALUES ((SELECT role FROM admin_account WHERE user=\'admin\'),\'role:view\');');
      lines.push('  INSERT INTO admin_role_perm VALUES ((SELECT role FROM admin_account WHERE user=\'admin\'),\'audit:view\');');
    } else {
      lines.push('  权限已加载，若仍看不到菜单请强制刷新（Ctrl+F5）');
    }
    try{
      if (typeof API !== 'undefined' && API.auth) API.auth.current()
          .then(r=>console.log('后端 /auth/current →', r))
          .catch(e=>console.log('后端 /auth/current 失败：', e.message));
    }catch(e){}
    const txt = lines.join('\n');
    console.log(txt);
    return txt;
  }

  /* ESC 关闭弹窗：同样走二次确认，不会误丢内容 */
  document.addEventListener('keydown', e=>{
    if (e.key !== 'Escape' && e.key !== 'Esc') return;
    const m = $('#modal');
    if (!m || m.className.indexOf('hidden') >= 0) return;
    e.preventDefault();
    tryCloseModal();
  });

  function bindLogin(){
    const loginBtn=$('#loginBtn'); if(!loginBtn) return; // 无 DOM（node 测试）安全跳过
    let loading=false;
    const setLoading=on=>{
      loading=!!on;
      try{
        loginBtn.disabled=loading;
        loginBtn.textContent=loading?'登录中…':'进入控制台';
        loginBtn.style.opacity=loading?'.7':'';
      }catch(e){}
    };
    const doLogin=async ()=>{
      if(loading) return; // 防重复提交
      const u=$('#loginUser').value.trim(), p=$('#loginPass').value;
      if(!u || !p){ toast('请输入账号和密码','err'); return; }
      setLoading(true);
      try{
        let acc;
        if (typeof API !== 'undefined' && API.auth && typeof API.auth.login === 'function'){
          /* ★ 新认证：后端下发 token，后续请求自动带 X-Token */
          const r = await API.auth.login(u, p);
          acc = { user:r.user || u, name:r.name || u, role:r.roleName || r.role || '', roleKey:r.role || '' };
          MY_PERMS = Array.isArray(r.perms) ? r.perms : [];
          try{ sessionStorage.setItem('ht_perms', JSON.stringify(MY_PERMS)); }catch(e){}
        }else{
          acc = await requestLogin(u, p);
          MY_PERMS = ['*'];   // 旧接口没有权限概念，放行全部
        }
        try{ sessionStorage.setItem('ht_login', JSON.stringify(acc)); }catch(e){}
        toast('登录成功','ok');
        /* ★ 后端提示仍在用默认弱密码 → 登录后弹窗提醒修改 */
        if (typeof r !== 'undefined' && r && r.mustChangePwd){
          setTimeout(()=>{ try{ openChangePwd(true); }catch(e){} }, 700);
        }
        enter();
      }catch(e){
        toast((e && e.message) ? e.message : '登录失败，请稍后重试','err');
      }finally{
        setLoading(false);
      }
    };
    loginBtn.onclick=doLogin;
    /* 修改密码 */
    const cpBtn=$('#changePwdBtn'); if(cpBtn) cpBtn.onclick=()=>{ try{ openChangePwd(false); }catch(e){} };
    /* 退出：清掉会话（后端暂无 logout 接口，后续可在此调用 /fossa/api/logout） */
    const logoutBtn=$('#logoutBtn'); if(logoutBtn) logoutBtn.onclick=async ()=>{
      try{
        if (typeof API !== 'undefined' && API.auth && typeof API.auth.logout === 'function'){
          await API.auth.logout().catch(()=>{});     // 通知后端销毁会话
        }
      }catch(e){}
      try{ sessionStorage.removeItem('ht_login'); sessionStorage.removeItem('ht_perms'); }catch(e){}
      MY_PERMS = [];
      if(typeof location!=='undefined')location.reload();
    };
    ['#loginUser','#loginPass'].forEach(s=>{ const el=$(s); if(el)el.addEventListener('keydown',e=>{ if(e.key==='Enter')doLogin(); }); });
  }
  /* ★ 恢复权限集合
   * 登录时已存进 sessionStorage，但【刷新页面后 MY_PERMS 会重置为空】，
   * 不恢复的话所有页面都会被权限过滤掉 → 菜单空、页面进不去。
   * 三级兜底：sessionStorage → 后端 /auth/current → 全放行（接口不可用时宁可放开也不锁死） */
  async function restorePerms(){
    if (MY_PERMS.length) return MY_PERMS;
    try{ const p = JSON.parse(sessionStorage.getItem('ht_perms')||'[]'); if (Array.isArray(p)) MY_PERMS = p; }
    catch(e){ MY_PERMS = []; }
    if (!MY_PERMS.length && typeof API !== 'undefined' && API && API.auth && typeof API.auth.current === 'function'){
      try{
        const r = await API.auth.current();
        const p = (r && r.perms) || [];
        /* ⚠️ 空数组必须兜底：[] 是 truthy，|| 兜不住，会变成"一个权限都没有" */
        MY_PERMS = (Array.isArray(p) && p.length) ? p : ['*'];
        try{ sessionStorage.setItem('ht_perms', JSON.stringify(MY_PERMS)); }catch(e){}
      }catch(e){ MY_PERMS = ['*']; }   // 接口不通 → 放行，不锁死自己
    }
    if (!MY_PERMS.length) MY_PERMS = ['*'];
    return MY_PERMS;
  }

  async function enter(){
    $('#login').classList.add('hidden');
    $('#app').classList.remove('hidden');
    const me=JSON.parse(sessionStorage.getItem('ht_login')||'{}');
    $('#sideName').textContent=me.name||me.user||'admin';
    await restorePerms();        // ★ 必须在 renderNav() 之前
    renderNav();
    if(useApi()){
      purgeLocalSeed();                 // 先清空演示数据，确保页面只显示数据库内容
      $('#view').innerHTML=loadingHtml('正在拉取后端数据');
      await bootSync();
    }
    ROUTES[CURRENT]();
  }

  /* ==================== 模态框 ==================== */
  /* ==================== 弹窗防误关 ====================
   * 需求：填写到一半时不会因为误触（点空白 / ESC）丢失内容。
   * 规则：
   *   ① 点遮罩空白处 —— 永不关闭，只抖动提示（要关闭请点「取消」或右上角 ×）
   *   ② ESC / 取消 / × —— 表单有改动时，第一次点只弹警告条（内容原样保留），
   *                        再点一次才真正放弃
   *   ③ 表单无改动（或纯详情弹窗）—— 直接关闭，不打扰
   */
  let MODAL_SNAPSHOT = null;
  let MODAL_ARMED    = false;      // 是否已弹过放弃警告

  function _scanModal(m){
    const out = {}; let i = 0;
    $$('input,textarea,select', m).forEach(el=>{
      const t = (el.type || '').toLowerCase();
      if (t === 'button' || t === 'submit' || t === 'hidden' || t === 'file') return;
      const k = el.id || el.name || ('__i' + (i++));
      out[k] = (t === 'checkbox' || t === 'radio')
          ? (el.checked ? '1' : '')
          : String(el.value == null ? '' : el.value);
    });
    return out;
  }

  /* 打开后立刻（同步赋值完成后）拍快照，用于判断"是否改过" */
  function snapshotModal(){
    const m = $('#modal');
    MODAL_ARMED = false;
    if (!m) { MODAL_SNAPSHOT = null; return; }
    MODAL_SNAPSHOT = _scanModal(m);
  }

  /* 与快照比对，有差异 = 用户改过内容 */
  function modalDirty(){
    const m = $('#modal');
    if (!m || !MODAL_SNAPSHOT) return false;
    const cur = _scanModal(m);
    const ks = Object.keys(cur).concat(Object.keys(MODAL_SNAPSHOT));
    for (let i = 0; i < ks.length; i++){
      const k = ks[i];
      if ((cur[k] || '') !== (MODAL_SNAPSHOT[k] || '')) return true;
    }
    return false;
  }

  /* 用户主动关闭（取消按钮 / × / ESC）：有改动则二次确认，不丢内容 */
  function tryCloseModal(){
    const m = $('#modal');
    if (!m || m.className.indexOf('hidden') >= 0){ closeModal(); return; }
    if (!modalDirty() || MODAL_ARMED){ closeModal(); return; }
    MODAL_ARMED = true;
    const box = $('.modal-box', m);
    if (box){
      const bar = document.createElement('div');
      bar.className = 'discard-bar';
      bar.innerHTML = '⚠ 内容还没保存，关闭后会丢失。'
          + '<b>再点一次</b>「取消 / ×」确认放弃，或继续编辑。';
      box.insertBefore(bar, box.firstChild);
    }
    const ghost = $('.modal-foot .btn-ghost', m);
    if (ghost) ghost.textContent = '确认放弃';
  }

  /* 点遮罩：不关闭，只给反馈 */
  function onMaskClick(){
    const m = $('#modal');
    if (!m) return;
    const box = $('.modal-box', m);
    if (box){
      box.classList.remove('shake');
      void box.offsetWidth;          // 重排，保证连续点击都能重播动画
      box.classList.add('shake');
      setTimeout(()=>{ try{ box.classList.remove('shake'); }catch(e){} }, 450);
    }
    if (typeof toast === 'function') toast('如需关闭请点「取消」或右上角 ×', 'err');
  }

  function openModal(html, wide){
    const m=$('#modal'); m.className='modal';
    m.innerHTML=`<div class="modal-box ${wide?'wide':''}">${html}</div>`;
    /* ★ 点空白不再关闭，只抖动提示 */
    m.onclick = e => { if (e.target === m) onMaskClick(); };
    /* ★ 等同步赋值（表单回显）跑完再拍快照，
     *   这样"打开编辑弹窗但没改任何东西"时不会误报未保存 */
    setTimeout(snapshotModal, 0);
    return m;
  }
  function closeModal(){ const m=$('#modal'); if(m)m.className='modal hidden';
    MODAL_ARMED=false; MODAL_SNAPSHOT=null; }

  /* ==================== 控制台 Dashboard ==================== */
  /* 后端 /api/dashboard/stats 优先，缺失字段用本地计算结果兜底 */
  function dashboardStats(){
    const local = HT.stats();
    const remote = (useApi() && DB.STATS) ? DB.STATS : null;
    if (!remote) return local;
    const s = {};
    for (const k in local) s[k] = (remote[k] !== undefined && remote[k] !== null) ? remote[k] : local[k];
    if (s.cap && s.seats != null && remote.rate == null) s.rate = Math.round(s.seats / s.cap * 100);
    return s;
  }
  function renderDashboard(){
    const s=dashboardStats();
    const src = (useApi() && DB.STATS) ? '后端接口 /api/dashboard/stats' : '本地数据';
    const cards = [
      {ic:'🎭', num:s.npc, lbl:'NPC 角色', sub:'全部关键人物', cls:''},
      {ic:'🎬', num:s.show, lbl:'场次', sub:`在售 ${s.showOnSale}`, cls:''},
      {ic:'🧾', num:s.paid, lbl:'已支付订单', sub:`共 ${s.order} 单`, cls:''},
      {ic:'💰', num:'¥'+s.revenue.toLocaleString(), lbl:'累计营收', sub:'已支付+已核销', cls:''},
      {ic:'👥', num:s.player, lbl:'注册玩家', sub:'含全服种子', cls:''},
      {ic:'📢', num:s.notice, lbl:'已发布公告', sub:'含置顶', cls:''},
      {ic:'💞', num:s.intimacy, lbl:'我的亲密度总和', sub:'6 NPC', cls:''},
      {ic:'🪑', num:s.rate+'%', lbl:'上座率', sub:`${s.seats}/${s.cap} 席`, cls:''}
    ];
    let html=`<div class="page-head"><div><h1>控制台</h1><div class="desc">磐渡戏剧工厂 · ${esc(DB.SETTINGS.appName)} · 实时数据概览 · 数据源：${esc(src)}</div></div></div>`;
    html+=`<div class="grid g4">${cards.map(c=>`<div class="stat">${c.ic&&`<span class="st-ic">${c.ic}</span>`||''}<div class="st-num">${c.num}</div><div class="st-lbl">${c.lbl}</div><div class="st-up">${c.sub}</div></div>`).join('')}</div>`;

    // 后端空表提示（醒目，避免误以为没接上后端）
    html+=emptyTableCard();

    // 快捷操作
    html+=`<div class="card" style="margin-top:16px"><div class="page-head" style="margin-bottom:12px"><h1 style="font-size:16px">快捷操作</h1></div><div style="display:flex;gap:10px;flex-wrap:wrap">
    <button class="btn-primary" onclick="navigate('npc')">＋ 新增角色</button>
    <button class="btn-primary" onclick="navigate('show')">＋ 新增场次</button>
    <button class="btn-ghost" onclick="navigate('notice')">发布公告</button>
    <button class="btn-ghost" onclick="navigate('rank')">查看榜单</button>
    <button class="btn-ghost" onclick="exportData()">导出数据 (JSON)</button>
    <button class="btn-ghost" onclick="refreshData()">${useApi()?'重新拉取后端数据':'重置数据'}</button>
  </div></div>`;

    // 两栏：NPC 人气 / 上座
    html+=`<div class="grid g2" style="margin-top:16px">`;
    // NPC 人气榜
    const pop=HT.npcPopularityRank();
    html+=`<div class="card"><div class="page-head" style="margin-bottom:12px"><h1 style="font-size:16px">NPC 人气榜（剧情权重）</h1><span class="desc">数据源：小程序榜单页</span></div>
    ${pop.map((n,i)=>`<div style="display:flex;align-items:center;gap:12px;margin-bottom:12px">
      <span style="font-size:18px;width:24px">${i<3?['🥇','🥈','🥉'][i]:i+1}</span>
      <img class="thumb" src="${esc(n.cover)}" onerror="this.src='data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%2246%22 height=%2258%22%3E%3Crect width=%2246%22 height=%2258%22 fill=%22%23222%22/%3E%3C/svg%3E'">
      <div style="flex:1"><div style="font-weight:600">${n.icon} ${esc(n.name)}</div><div style="font-size:11px;color:var(--sub)">${esc(n.power)}</div></div>
      ${bar(n.pop)}
    </div>`).join('')}
  </div>`;
    // 场次上座
    html+=`<div class="card"><div class="page-head" style="margin-bottom:12px"><h1 style="font-size:16px">场次上座情况</h1><span class="desc">实时</span></div>
    ${[...DB.SHOWS].sort((a,b)=>b.taken/a.capacity-a.taken/a.capacity).map(sh=>{
      const r=Math.round(sh.taken/sh.capacity*100);
      return `<div style="display:flex;align-items:center;gap:12px;margin-bottom:12px">
        <div style="flex:1"><div style="font-weight:600;font-size:13px">${esc(sh.title)}</div><div style="font-size:11px;color:var(--sub)">${esc(sh.date)} · ${sh.taken}/${sh.capacity}席</div></div>
        ${bar(r)}
      </div>`;
    }).join('')}
  </div>`;
    html+=`</div>`;

    // 最近订单
    html+=`<div class="card" style="margin-top:16px"><div class="page-head" style="margin-bottom:12px"><h1 style="font-size:16px">最近订单</h1><button class="btn-ghost" onclick="navigate('order')">查看全部</button></div>
    <table><thead><tr><th>订单号</th><th>玩家</th><th>场次</th><th>金额</th><th>状态</th></tr></thead><tbody>
    ${DB.ORDERS.slice(0,6).map(o=>`<tr><td>${esc(o.id)}</td><td>${esc(o.user)}</td><td>${esc(o.show)}</td><td>¥${o.amount}</td><td>${statusBadge(o.status)}</td></tr>`).join('')}    ${DB.ORDERS.length?'':`<tr><td colspan="5">${emptyRow('数据库中暂无订单数据')}</td></tr>`}
    </tbody></table></div>`;

    $('#view').innerHTML=html;
  }

  /* ==================== 通用 CRUD 表格页辅助 ==================== */
  function pageShell(title, desc, toolbarHtml, tableHtml){
    return `<div class="page-head"><div><h1>${title}</h1><div class="desc">${desc}</div></div></div>
    <div class="toolbar">${toolbarHtml}</div>
    <div class="card" style="padding:0;overflow:hidden">${tableHtml}</div>`;
  }

  /* ==================== 角色管理 ==================== */
  /* 查询条件存这里（不依赖 DOM），重渲染也不会丢 */
  const NPC_FILTER = { kw:'', faction:'' };
  let NPC_SEARCH_TIMER = null;

  /* 命中条件：关键词（名称/身份/异能/阵营简称/标签，支持空格分词）+ 阵营 */
  function matchNpc(n){
    if (NPC_FILTER.faction && String(n.faction||'') !== NPC_FILTER.faction) return false;
    const kw = String(NPC_FILTER.kw||'').trim().toLowerCase();
    if (!kw) return true;
    const hay = [n.name, n.role, n.power, n.campLabel].concat(toArr(n.tags))
        .filter(Boolean).join(' ').toLowerCase();
    return kw.split(/\s+/).filter(Boolean).every(w => hay.indexOf(w) >= 0);
  }

  /* 带条件向后端查询：GET /npcs?keyword=&faction=
   * 后端当前是全量列表（会忽略这两个参数），前端再过滤一次兜底，结果同样正确；
   * 后端以后加了 @RequestParam 支持条件查，这里不用改就自动变成真正的后端查询。 */
  async function fetchNpcs(){
    if (!useApi()) return;
    const params = {};
    if (String(NPC_FILTER.kw||'').trim()) params.keyword = String(NPC_FILTER.kw).trim();
    if (NPC_FILTER.faction) params.faction = NPC_FILTER.faction;
    try{
      const l = await API.npcs.query(params);
      DB.NPCS = Array.isArray(l) ? l : [];
    }catch(e){
      /* 后端不接受查询参数（如返回 400）时退回全量列表，由前端过滤兜底 */
      await pull('npcs').catch(()=>{});
    }
  }
  function resetNpcFilter(){
    NPC_FILTER.kw=''; NPC_FILTER.faction='';
    fetchNpcs().then(renderNpc);
  }

  async function renderNpc(){
    const kw = NPC_FILTER.kw, fac = NPC_FILTER.faction;
    const list = [...DB.NPCS].filter(matchNpc);
    const cards=list.map(n=>`
    <div class="npc-card">
      <div class="npc-cover">
        <img src="${esc(n.cover)}" onerror="this.onerror=null;this.src='data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22400%22 height=%22200%22%3E%3Crect width=%22400%22 height=%22200%22 fill=%22%23222b3d%22/%3E%3Ctext x=%2250%25%22 y=%2250%22 fill=%22%23a6b0c0%22 font-size=%2240%22 text-anchor=%22middle%22%3E${esc(n.icon)}%3C/text%3E%3C/svg%3E'">
        ${campBadge(n.faction)}
        <span class="ic">${esc(n.icon)}</span>
      </div>
      <div class="info">
        <div class="nn">${esc(n.name)} <span style="font-size:11px;color:var(--muted)">${esc(n.campLabel)}</span></div>
        <div class="nr">${esc(n.role)}</div>
        <div class="na">⚡ ${esc(n.power)} · 排序 ${n.sort}</div>
        <div style="margin-top:6px">${statusBadge(n.status)}</div>
      </div>
    </div>`).join('');

    const factions = ['灰塔官方派','破晓反抗军','无阵营'];
    const filtering = !!(String(kw).trim() || fac);
    const tip = filtering
        ? `筛选出 <b style="color:var(--txt)">${list.length}</b> / ${DB.NPCS.length} 个角色`
        : `共 <b style="color:var(--txt)">${DB.NPCS.length}</b> 个角色`;

    $('#view').innerHTML=pageShell('角色管理', '对应小程序「角色图鉴」模块 · 数据来自 /npcs',
        `<input id="npcKw" placeholder="🔍 搜索名称 / 身份 / 异能 / 标签" value="${esc(kw)}">
     <select id="npcFaction">
       <option value="">全阵营</option>
       ${factions.map(f=>`<option ${f===fac?'selected':''}>${esc(f)}</option>`).join('')}
     </select>
     ${filtering?`<button class="btn-ghost" onclick="resetNpcFilter()">清除筛选</button>`:''}
     <span style="align-self:center;font-size:12px;color:var(--sub)">${tip}</span>
     <button class="btn-primary" onclick="openNpcForm()">＋ 新增角色</button>`,
        `<div class="npc-grid" style="padding:18px">${cards||emptyRow(
            filtering ? '没有符合条件的角色，换个关键词或选「全阵营」试试'
                : '数据库中暂无角色数据')}</div>`
    );

    /* 搜索：停止输入 300ms 后才查（防抖，避免每敲一个字都发请求） */
    const kwEl=$('#npcKw');
    if (kwEl && kwEl.addEventListener){
      kwEl.oninput=e=>{
        NPC_FILTER.kw = e.target.value;
        clearTimeout(NPC_SEARCH_TIMER);
        NPC_SEARCH_TIMER = setTimeout(async ()=>{
          await fetchNpcs();
          await renderNpc();
          // 重渲染后把焦点和光标还给搜索框，输入不被打断
          const el=$('#npcKw');
          if (el && el.focus){ el.focus(); try{ el.setSelectionRange(el.value.length, el.value.length); }catch(_e){} }
        }, 300);
      };
      kwEl.onkeydown=e=>{ if(e.key==='Enter'){ clearTimeout(NPC_SEARCH_TIMER); fetchNpcs().then(renderNpc); } };
    }
    /* 阵营：选中即查 */
    const sel=$('#npcFaction');
    if (sel && sel.addEventListener){
      sel.onchange=async e=>{
        NPC_FILTER.faction = e.target.value;
        await fetchNpcs();
        await renderNpc();
      };
    }
    // 给每张卡底部加操作按钮
    $$('.npc-card').forEach((el,i)=>{
      const n=list[i]; if(!n) return;
      const actions=document.createElement('div'); actions.className='row-actions'; actions.style.padding='0 14px 14px';
      actions.innerHTML=`<button class="btn-ghost" onclick="openNpcForm('${n.id}')">编辑</button>
      <button class="btn-ghost" onclick="viewNpc('${n.id}')">详情</button>
      <button class="btn-danger" onclick="delNpc('${n.id}')">删除</button>`;
      el.appendChild(actions);
    });
  }

  /* 演绎提示四个字段，对应 npc 表的 hook_action / hook_contrast / hook_fragile / hook_speech */
  const HOOK_FIELDS = [
    { key:'action',   label:'招牌动作', ph:'他重复做的小动作，如：数数、摸戒指' },
    { key:'contrast', label:'矛盾感',   ph:'表面与内在的反差，一句话说清' },
    { key:'fragile',  label:'脆弱时刻', ph:'他绷不住的那一刻发生了什么' },
    { key:'speech',   label:'说话方式', ph:'语速、句式、尾音等特征' }
  ];

  function npcForm(n){
    return `
    <div class="field-row">
      <div class="field"><label>名称 *</label><input id="f_name" value="${esc(n?n.name:'')}"></div>
      <div class="field"><label>头像 Emoji</label><input id="f_icon" value="${esc(n?n.icon:'🜲')}"></div>
    </div>
    <div class="field"><label>角色立绘路径</label><input id="f_cover" value="${esc(n?n.cover:'assets/npcs/linshen.png')}" placeholder="assets/npcs/xxx.png"></div>
    <div class="field"><label>扮演角色 *</label><input id="f_role" value="${esc(n?n.role:'')}" placeholder="如：逃亡实验体 · 破晓反抗军"></div>
    <div class="field-row">
      <div class="field"><label>阵营</label>
        <select id="f_faction"><option>灰塔官方派</option><option>破晓反抗军</option><option>无阵营</option></select></div>
      <div class="field"><label>阵营简称</label><input id="f_campLabel" value="${esc(n?n.campLabel:'灰塔')}"></div>
    </div>
    <div class="field-row">
      <div class="field"><label>异能</label><input id="f_power" value="${esc(n?n.power:'')}"></div>
      <div class="field"><label>排序</label><input id="f_sort" type="number" value="${n?n.sort:DB.NPCS.length+1}"></div>
    </div>
    <div class="field"><label>标签（逗号分隔）</label><input id="f_tags" value="${esc(n?toArr(n.tags).join(','):'')}"></div>
    <div class="field"><label>人物小传</label><textarea id="f_desc">${esc(n?n.desc:'')}</textarea></div>
    <div class="field"><label>经典台词（一行一句）</label><textarea id="f_quotes">${esc(n?toArr(n.quotes, '\n').join('\n'):'')}</textarea></div>
    <div class="field"><label>状态</label><select id="f_status"><option>启用</option><option>停用</option></select></div>
    <div style="margin:14px 0 4px;font-size:13px;font-weight:600;border-top:1px solid var(--line);padding-top:14px">🎭 演绎提示（写手看详情弹窗）</div>
    ${HOOK_FIELDS.map(h=>`<div class="field"><label>${h.label}</label><textarea id="f_hook_${h.key}" placeholder="${esc(h.ph)}">${esc(n?((n.hooks&&n.hooks[h.key])||''):'')}</textarea></div>`).join('')}`;
  }
  /* 角色表单快照（判断用户是否改过） */
  const NPC_FORM_FIELDS = ['name','icon','cover','role','faction','campLabel','power','sort','tags','desc','quotes',
    'hook_action','hook_contrast','hook_fragile','hook_speech','status'];
  let NPC_FORM_SNAPSHOT = null;
  function snapshotNpcForm(){
    const o = {};
    NPC_FORM_FIELDS.forEach(k=>{ const el=$('#f_'+k); if(el) o[k]=String(el.value||''); });
    return o;
  }
  function npcFormUnchanged(snap){
    if (!snap) return true;
    return NPC_FORM_FIELDS.every(k=>{
      const el=$('#f_'+k);
      return !el || String(el.value||'')===String(snap[k]||'');
    });
  }
  function renderNpcModal(n){
    openModal(`
    <div class="modal-head"><h2>${n?'编辑角色':'新增角色'}</h2><button class="modal-close" onclick="tryCloseModal()">×</button></div>
    ${npcForm(n)}
    <div class="modal-foot">
      <button class="btn-ghost" onclick="tryCloseModal()">取消</button>
      <button class="btn-primary" onclick="saveNpc('${n?n.id:''}')">保存</button>
    </div>`, true);
    setTimeout(()=>{
      const sel=$('#f_faction'); if(sel&&n) sel.value=n.faction;
      const st=$('#f_status');   if(st&&n)   st.value=n.status;
      NPC_FORM_SNAPSHOT = snapshotNpcForm();
    },0);
  }
  /* 打开角色表单：本地秒开 + 后端详情覆盖（hook_* 四个字段最容易脏，走详情最稳） */
  async function openNpcForm(id){
    const local = id ? DB.npcById(id) : null;
    renderNpcModal(local || (id ? { id:id } : null));
    if (!id || !useApi() || !API.npcs || !API.npcs.get) return;
    try{
      const fresh = await API.npcs.get(id);
      if (!fresh) return;
      if (npcFormUnchanged(NPC_FORM_SNAPSHOT)){
        DB.NPCS = DB.NPCS.map(x => String(x.id)===String(id) ? Object.assign({}, x, fresh) : x);
        renderNpcModal(fresh);
      }else{
        toast('已获取最新数据，但保留你正在编辑的内容','warn');
      }
    }catch(e){
      console.warn('[npc] 拉取详情失败，使用列表数据', e && e.message);
    }
  }
  async function saveNpc(id){
    const get=v=>$('#f_'+v).value.trim();
    const data={
      name:get('name'), icon:get('icon')||'🜲', cover:get('cover')||'assets/npcs/linshen.png',
      role:get('role'), faction:$('#f_faction').value, campLabel:get('campLabel'),
      power:get('power'), sort:+get('sort')||DB.NPCS.length+1,
      tags:get('tags').split(/[,，]/).map(s=>s.trim()).filter(Boolean),   // 后端可能存成 "a,b" 字符串
      desc:get('desc'), status:$('#f_status').value,
      quotes:get('quotes').split('\n').map(s=>s.trim()).filter(Boolean),
      hooks:Object.fromEntries(HOOK_FIELDS.map(h=>[h.key, $('#f_hook_'+h.key).value.trim()])),
      id:''
    };
    if(!data.name||!data.role){ toast('请填写名称与扮演角色','err'); return; }
    if(useApi()){
      const ok=await withApi('保存', async ()=>{
        if(id){ data.id=id; await API.npcs.update(id,data); }
        else  { data.id=HT.nextId('NPCS','npc_'); data.sort=DB.NPCS.length+1; await API.npcs.create(data); }
        await resync('npcs');   // 回拉最新列表（后端可能重算 id/排序）
      });
      if(!ok) return;
      toast((id?'已更新 ':'已新增 ')+data.name);
    }else{
      if(id){
        data.id=id;
        HT.updateItem('NPCS',id,data); toast('已更新 '+data.name);
      } else {
        data.id=HT.nextId('NPCS','npc_'); data.sort=DB.NPCS.length+1;
        HT.addItem('NPCS',data); toast('已新增 '+data.name);
      }
    }
    closeModal(); renderNpc();
  }
  async function delNpc(id){
    const n=DB.npcById(id); if(!n)return;
    if(!confirm(`确定删除角色「${n.name}」？`))return;
    if(useApi()){
      const ok=await withApi('删除', async ()=>{ await API.npcs.remove(id); await resync('npcs'); });
      if(!ok) return;
    }else{
      HT.deleteItem('NPCS',id);
    }
    toast('已删除'); renderNpc();
  }
  function viewNpc(id){
    const n=DB.npcById(id); if(!n)return;
    openModal(`
    <div class="modal-head"><h2>${n.icon} ${esc(n.name)}</h2><button class="modal-close" onclick="tryCloseModal()">×</button></div>
    <div style="display:flex;gap:20px;flex-wrap:wrap">
      <img class="detail-cover" src="${esc(n.cover)}" onerror="this.style.background='var(--panel2)'">
      <div style="flex:1;min-width:260px">
        <div style="font-size:13px;color:var(--primary2);margin-bottom:10px">${esc(n.power)}</div>
        <div class="kv"><span>扮演角色</span><b>${esc(n.role)}</b></div>
        <div class="kv"><span>阵营</span><b>${campBadge(n.faction)}</b></div>
        <div class="kv"><span>标签</span><span>${toArr(n.tags).map(t=>`<span class="tag">${esc(t)}</span>`).join('')}</span></div>
        <div class="kv"><span>状态</span><b>${statusBadge(n.status)}</b></div>
        <div style="margin-top:12px;color:var(--sub);font-size:13px;line-height:1.7">${esc(n.desc)}</div>
      </div>
    </div>
    <div class="hooks" style="margin-top:16px">
      ${HOOK_FIELDS.map(h=>`<div class="hook"><h4>${h.label}</h4><p>${esc((n.hooks&&n.hooks[h.key])||'—')}</p></div>`).join('')}
    </div>
  `, true);
  }

  /* ==================== 抽卡奖池 ==================== */
  /* 说明：权重是「同稀有度内的相对权重」。
   *   卡概率 = 稀有度概率 × (该卡有效权重 / 同稀有度有效权重之和)
   *   有效权重：UP 卡会乘以 rateUp.bonus（概率提升）
   *   稀有度概率：来自 DB.GACHA_RARITY_RATE（后端可下发），单位是百分比 */

  /* 有效权重（含 UP 加成） */
  function gachaEffWeight(pool, c){
    const w = +c.weight || 0;
    if (!c.up) return w;
    const bonus = (pool && pool.rateUp && +pool.rateUp.bonus) || 1;
    const upNpc = (pool && pool.rateUp && (pool.rateUp.npcId || '')) || '';
    /* ★ 与后端 GachaMiniService.effWeight 保持一致：
     *   up_npc_id 为空 → 所有 is_up=1 的卡都加成；
     *   否则只有 npcId 匹配的卡才加成（不匹配则按原权重）。 */
    const hit = !upNpc || upNpc === String(c.npc || '');
    return w * (hit ? bonus : 1);
  }
  /* 某稀有度下所有卡的有效权重之和 */
  function gachaRarityWeight(pool, rarity){
    return (pool.cards||[]).filter(c=>c.rarity===rarity).reduce((n,c)=>n+gachaEffWeight(pool,c),0);
  }
  /* 稀有度定义（order 从大到小排列） */
  /* ---------- 稀有度定义加载 ----------
   * 卡牌表单的「稀有度」下拉、概率罗盘、模拟器都依赖它。
   * 配置源是后端的 gacha_rarity 表（/gacha/rarity/list）。
   * 三级兜底，保证下拉永远不为空：
   *   ① /gacha/rarity/list  完整定义（名称/标签/颜色/排序）
   *   ② /gacha/rarity       只有 {SSR:2,...} 概率 → 推导出名称/颜色
   *   ③ 都没有              用内置默认四档 */
  const DEFAULT_RARITY = [
    { key:'SSR', name:'SSR', label:'特典', color:'#f59e0b', rate:2,  order:4 },
    { key:'SR',  name:'SR',  label:'稀有', color:'#a855f7', rate:12, order:3 },
    { key:'R',   name:'R',   label:'精良', color:'#3b82f6', rate:36, order:2 },
    { key:'N',   name:'N',   label:'普通', color:'#6b7280', rate:50, order:1 }
  ];
  async function loadGachaRarity(){
    /* ① 完整定义 */
    if (typeof API.gacha.rarityList === 'function'){
      try{
        const l = await API.gacha.rarityList();
        if (Array.isArray(l) && l.length){ DB.GACHA_RARITY = l; DB.GACHA_RARITY_RATE = rateMapOf(l); return l; }
      }catch(e){ /* 接口没实现，往下走 */ }
    }
    /* ② 只有概率 map → 推导 */
    const rate = DB.GACHA_RARITY_RATE;
    if (rate && typeof rate === 'object' && Object.keys(rate).length){
      DB.GACHA_RARITY = Object.keys(rate).map(k=>{
        const d = DEFAULT_RARITY.find(x=>x.key===k);
        return { key:k, name:k, label:d?d.label:'', color:d?d.color:'#6b7280', rate:Number(rate[k])||0, order:d?d.order:0 };
      });
      return DB.GACHA_RARITY;
    }
    /* ③ 内置默认 */
    DB.GACHA_RARITY = DEFAULT_RARITY.slice();
    DB.GACHA_RARITY_RATE = { SSR:2, SR:12, R:36, N:50 };
    return DB.GACHA_RARITY;
  }
  function rateMapOf(list){
    const m = {};
    (list||[]).forEach(r=>{ m[r.key] = Number(r.rate)||0; });
    return m;
  }

  function gachaRarities(){
    const list = Array.isArray(DB.GACHA_RARITY) ? DB.GACHA_RARITY.slice() : [];
    return list.sort((a,b)=>(+b.order||0)-(+a.order||0));
  }
  function gachaRarityMeta(key){
    return gachaRarities().find(r=>r.key===key) || { key:key, name:key, color:'#6b7280', order:0 };
  }
  /* 稀有度基础概率（百分比，0-100） */
  function gachaRarityRate(key){
    const r = DB.GACHA_RARITY_RATE || {};
    return +r[key] || 0;
  }
  /* 单张卡的综合概率（百分比） */
  function gachaCardProb(pool, c){
    const total = gachaRarityWeight(pool, c.rarity);
    if (!total) return 0;
    return gachaRarityRate(c.rarity) * gachaEffWeight(pool, c) / total;
  }
  /* 期望抽数：单抽概率 p(%) + 第 K 抽保底必出。
   *   E = Σ(i=1..K-1) i·(1-p)^(i-1)·p  +  K·(1-p)^(K-1)
   *   无保底（K<=0）时 E = 1/p */
  /* 期望出货抽数
   *
   * ★ 支持「出货窗口」：start 之前概率为 0，start ~ pity 之间才可能出，第 pity 抽必出。
   *   例：start=250, pity=300 → 前 250 抽绝不出 SSR，只在 250~300 抽之间出，第 300 抽必出。
   *
   * E = start + E[min(X, W)]，其中 X~几何分布(p)，W = pity - start（窗口长度）
   *   E[min(X,W)] = Σ_{i=1..W} P(X≥i) = (1-(1-p)^W)/p
   *
   * start=0 时退化为原来的公式 E = (1-(1-p)^K)/p，行为不变。
   */
  function gachaExpect(pityK, ratePct, startN){
    const p = (+ratePct || 0) / 100;
    const K = +pityK || 0;
    const start = +startN || 0;

    /* 概率为 0：只能靠保底，期望恰为 pity（窗口模型下就是第 K 抽） */
    if (p <= 0) return K > 0 ? K : 0;
    /* 无保底：前 start 抽不出，之后按 p 出货 → start + 1/p */
    if (K <= 0) return start + 1 / p;

    /* 起点不能超过保底，否则永远出不了 */
    const st = start > K ? K : start;
    const W  = Math.max(0, K - st);            // 窗口长度

    let miss = 1;                              // (1-p)^(W-1) 的累积
    let e = 0;
    for (let i = 1; i <= W; i++){
      e += miss;                               // P(X ≥ i)
      miss *= (1 - p);
    }
    return st + e;
  }
  /* 综合出货率（含保底后） */
  function gachaEffectiveRate(pityK, ratePct, startN){
    const e = gachaExpect(pityK, ratePct, startN);
    return e > 0 ? 100 / e : 0;
  }
  function fmtPct(v, digits){
    const d = digits == null ? 2 : digits;
    const n = +v || 0;
    return (n < 0.01 && n > 0) ? '<0.01' : n.toFixed(d);
  }

  /* ---------- 接口层守卫 ----------
   * 抽卡接口定义在 api.js 的 API.gacha 上。若服务器上的 api.js 是旧版（未上传/被浏览器缓存），
   * API.gacha 会是 undefined，直接调用就会抛
   *   "Cannot read properties of undefined (reading 'createPool')"
   * 这里统一做一次存在性检查，缺了就给明确提示，而不是让页面崩掉。 */
  function gachaApi(){
    try{
      const a = (typeof API !== 'undefined' && API) ? API.gacha : null;
      return (a && typeof a.listPools === 'function') ? a : null;
    }catch(e){ return null; }
  }
  function gachaApiMissing(where){
    const msg = 'api.js 版本过旧，缺少抽卡接口（API.gacha）。请上传最新的 api.js 并强制刷新（Ctrl+F5）。';
    console.error('[gacha] ' + (where||'') + ' ' + msg);
    if (typeof toast === 'function') toast(msg, 'err');
    return false;
  }
  /* 兼容旧 api.js：没这个字段就当"不支持" */
  function gachaSupported(){ return !!gachaApi(); }

  /* 当前选中的奖池 id */
  let GACHA_CURRENT = '';
  let GACHA_SAVE_TIMER = null;

  function gachaPools(){ return Array.isArray(DB.GACHA_POOLS) ? DB.GACHA_POOLS : []; }
  function gachaPoolById(id){ return gachaPools().find(p=>String(p.id)===String(id)) || null; }
  function gachaCurrentPool(){
    return gachaPoolById(GACHA_CURRENT) || gachaPools()[0] || null;
  }

  /* ---------- 可视化组件（纯 SVG / CSS，无外部依赖） ---------- */

  /* 环形概率图：data=[{key,label,value,color}]，value 为百分比 */
  function donutChart(data, centerTop, centerBottom){
    const total = data.reduce((n,d)=>n+(+d.value||0), 0) || 1;
    const R = 54, C = 2 * Math.PI * R;
    let acc = 0;
    const segs = data.map(d=>{
      const frac = (+d.value||0) / total;
      const len  = frac * C;
      const s = `<circle class="donut-seg" r="${R}" cx="70" cy="70"
      stroke="${esc(d.color)}" stroke-width="26" fill="none"
      stroke-dasharray="${len.toFixed(2)} ${(C-len).toFixed(2)}"
      stroke-dashoffset="${(-acc).toFixed(2)}"
      transform="rotate(-90 70 70)"><title>${esc(d.label)} ${fmtPct(d.value)}%</title></circle>`;
      acc += len;
      return s;
    }).join('');
    return `<svg class="donut" viewBox="0 0 140 140">
    <circle r="${R}" cx="70" cy="70" fill="none" stroke="var(--panel2)" stroke-width="26"/>
    ${segs}
    <text class="donut-top" x="70" y="66" text-anchor="middle">${esc(centerTop)}</text>
    <text class="donut-bot" x="70" y="86" text-anchor="middle">${esc(centerBottom||'')}</text>
  </svg>`;
  }

  /* 水平占比条 */
  function shareBar(pct, color){
    return `<div class="share-bar"><i style="width:${Math.max(0,Math.min(100,pct))}%;background:${esc(color)}"></i></div>`;
  }

  /* ---------- 实际概率（归一化） ----------
   * 若某个稀有度配置了 rate 但奖池里一张该稀有度的卡都没有，
   * 这部分概率会摊到其它稀有度上（否则玩家会抽到"空"）。
   * 所以「配置概率」和「实际概率」可能不一致，这里统一算实际值。 */
  function gachaRealRates(pool){
    const raw = {}, usable = {};
    let total = 0; const missing = [];
    gachaRarities().forEach(r=>{
      const rate = gachaRarityRate(r.key);
      raw[r.key] = rate;
      const has = (pool.cards||[]).some(c=>c.rarity===r.key && inStockSim(c));
      if (has && rate > 0){ usable[r.key] = rate; total += rate; }
      else if (rate > 0) missing.push(r.key);
    });
    const real = {};
    for (const k in raw) real[k] = total > 0 ? raw[k] / total * 100 : 0;
    return { raw, real, total, missing };
  }

  /* ---------- 蒙特卡洛模拟器 ----------
   * 前端复刻后端 GachaMiniService 的出货逻辑（保底优先 → 按概率随机稀有度 → 稀有度内按有效权重选卡），
   * 让运营在改完权重后能立刻看到"实际跑起来是什么样"，而不只是看配置数字。 */
  function gachaSimulate(pool, n, minSsrDraws){
    const rate = {};
    gachaRarities().forEach(r=>{ rate[r.key] = gachaRarityRate(r.key); });
    const cards = (pool.cards||[]).filter(c=>inStockSim(c));
    if (!cards.length) return null;
    const pitySsr = +pool.pity.ssr || 0, pitySr = +pool.pity.sr || 0;
    /* 出货窗口起点（不能超过保底，否则永远出不了） */
    const ssrStart = Math.min(+pool.pity.ssrStart || 0, pitySsr || Infinity);
    const srStart  = Math.min(+pool.pity.srStart  || 0, pitySr  || Infinity);
    const minDraws = +minSsrDraws || 0;
    const reset = pool.pity.resetOnHit !== false;

    const cnt = {};                 // 稀有度计数
    const cardCnt = {};             // 单卡计数
    let ssrCount = 0, srCount = 0, totalCount = 0;
    let gap = 0, maxGap = 0, gaps = [];
    let firstSsrAt = -1, pityHits = 0;

    for (let i = 0; i < n; i++){
      ssrCount++; srCount++; totalCount++; gap++;
      /* 窗口：距上次出货的计数没到起点 → 该稀有度不参与；最低门槛按总抽数另算 */
      const locked = minDraws > 0 && totalCount < minDraws;
      const ssrWin = ssrCount >= ssrStart;
      const srWin  = srCount  >= srStart;
      const ssrOk  = ssrWin && !locked;
      let rarity, isPity = 0;
      if (ssrOk && pitySsr > 0 && ssrCount >= pitySsr){ rarity = 'SSR'; isPity = 1; }
      else if (srWin && pitySr > 0 && srCount >= pitySr){ rarity = 'SR'; isPity = 1; }
      else { rarity = simRarity(rate, cards, !ssrOk, !srWin); }
      if (!ssrOk && rarity === 'SSR'){
        rarity = cards.some(c=>c.rarity==='SR') ? 'SR' : (cards.some(c=>c.rarity==='R') ? 'R' : 'N');
        isPity = 0;
      }
      if (!srWin && rarity === 'SR'){
        rarity = cards.some(c=>c.rarity==='R') ? 'R' : 'N';
        isPity = 0;
      }
      if (!cards.some(c=>c.rarity===rarity)) rarity = simFallback(cards);
      const card = simCard(cards, rarity, pool);
      if (!card) break;
      if (reset){
        if (rarity === 'SSR'){ ssrCount = 0; srCount = 0; }
        else if (rarity === 'SR'){ srCount = 0; }
      }
      cnt[rarity] = (cnt[rarity]||0) + 1;
      cardCnt[card.id] = (cardCnt[card.id]||0) + 1;
      if (rarity === 'SSR'){
        if (firstSsrAt < 0) firstSsrAt = totalCount;
        if (isPity) pityHits++;
        gaps.push(gap); maxGap = Math.max(maxGap, gap); gap = 0;
      }
    }
    const avgGap = gaps.length ? gaps.reduce((a,b)=>a+b,0)/gaps.length : 0;
    return { n, cnt, cardCnt, maxGap, avgGap, firstSsrAt, pityHits,
      pityRate: gaps.length ? pityHits/gaps.length*100 : 0 };
  }
  function inStockSim(c){ return c.stock == null || c.stock !== 0; }
  function simRarity(rate, cards, ssrLocked, srLocked){
    let total = 0; const usable = [];
    for (const k in rate){
      if (ssrLocked && k === 'SSR') continue;
      if (srLocked  && k === 'SR')  continue;
      if (rate[k] > 0 && cards.some(c=>c.rarity===k)){ usable.push([k, rate[k]]); total += rate[k]; }
    }
    if (!usable.length) return 'N';
    let r = Math.random() * total;
    for (const [k,v] of usable){ r -= v; if (r <= 0) return k; }
    return usable[usable.length-1][0];
  }
  function simFallback(cards){
    for (const k of ['SSR','SR','R','N']) if (cards.some(c=>c.rarity===k)) return k;
    return 'N';
  }
  function simCard(cards, rarity, pool){
    const pc = cards.filter(c=>c.rarity===rarity);
    let total = 0; pc.forEach(c=>total += gachaEffWeight(pool, c));
    if (total <= 0) return pc[Math.floor(Math.random()*pc.length)];
    let r = Math.random() * total;
    for (const c of pc){ r -= gachaEffWeight(pool, c); if (r <= 0) return c; }
    return pc[pc.length-1];
  }

  /* 当前模拟结果（切页/改权重后清空，避免展示过期数据） */
  let GACHA_SIM = null;

  function renderGacha(){
    const pools = gachaPools();
    const pool  = gachaCurrentPool();
    if (pool) GACHA_CURRENT = pool.id;
    const set = html => { $('#view').innerHTML = html; };
    const head = `<div class="page-head"><div><h1>抽奖管理</h1><div class="desc">配置奖池权重、稀有度与保底规则 · 数据来自 /gacha/pools</div></div>
    <div class="page-head-act">
      <button class="btn-ghost" onclick="openGachaPoolForm('${esc(pool?pool.id:'')}')">${pool?'编辑奖池':'新增奖池'}</button>
      <button class="btn-primary" onclick="openGachaPoolForm('')">+ 新增奖池</button>
    </div></div>`;

    const missTip = gachaApi() ? '' : `
    <div class="card" style="border-color:var(--danger)">
      <div style="color:var(--danger);font-weight:600;margin-bottom:6px">⚠ api.js 版本过旧，抽卡接口未加载</div>
      <div style="font-size:12px;color:var(--sub);line-height:1.7">
        当前 <code>api.js</code> 里没有 <code>API.gacha</code>，无法读写奖池。<br>
        请把最新的 <code>api.js</code> 上传到 <code>/fossa/common/js/api.js</code>，<b>Ctrl+F5</b> 强制刷新后再试。
      </div>
    </div>`;

    if (!pools.length){
      return set(`${head}${missTip}
      <div class="card"><div class="empty">数据库中暂无奖池数据。<br>请先建 gacha_pool / gacha_card 表并写入数据，或<button class="btn-primary" style="margin-left:8px" onclick="openGachaPoolForm('')">+ 新增奖池</button></div></div>`);
    }
    if (!pool){
      return set(`${head}<div class="card"><div class="empty">未选中奖池</div></div>`);
    }

    const rarities = gachaRarities();
    const ssrPity  = +pool.pity.ssr || 0;
    const ssrStart = Math.min(+pool.pity.ssrStart || 0, ssrPity || Infinity);   /* 起点不能超过保底 */
    const ssrRate  = gachaRealRates(pool).real.SSR;   /* 实际概率（含缺卡摊算） */
    const ssrExp   = gachaExpect(ssrPity, ssrRate, ssrStart);
    const ssrEff   = gachaEffectiveRate(ssrPity, ssrRate, ssrStart);
    const cardN    = (pool.cards||[]).length;

    /* ================= ① 奖池卡片 ================= */
    const poolCards = `
    <div class="pool-cards">
      ${pools.map(p=>{
      const on = String(p.id) === String(pool.id);
      const n  = (p.cards||[]).length;
      const k  = +p.pity.ssr || 0;
      return `<div class="pool-card ${on?'on':''}" onclick="switchGachaPool('${esc(p.id)}')">
          <div class="pc-top">
            <span class="pc-name">${esc(p.name)}</span>
            ${statusBadge(p.status)}
          </div>
          <div class="pc-meta">
            <span>${n} 张卡</span>
            <span>SSR 保底 ${k || '—'} 抽</span>
            <span>${money(p.costSingle||0)} ${esc(p.currency||'')} / 抽</span>
          </div>
        </div>`;
    }).join('')}
    </div>`;

    /* ================= ② 概率罗盘 + 关键指标 ================= */
    const rates = gachaRealRates(pool);
    const donutData = rarities.map(r=>({
      key:r.key, label:r.name, color:r.color, value:rates.real[r.key]
    }));
    const legend = rarities.map(r=>{
      const real = rates.real[r.key], raw = rates.raw[r.key];
      const n    = (pool.cards||[]).filter(c=>c.rarity===r.key).length;
      const k    = r.key==='SSR' ? ssrPity : (r.key==='SR' ? (+pool.pity.sr||0) : 0);
      const st   = r.key==='SSR' ? ssrStart : (r.key==='SR' ? Math.min(+pool.pity.srStart||0, k||Infinity) : 0);
      const exp  = gachaExpect(k, real, st);
      const diff = Math.abs(real - raw) > 0.05;
      return `<div class="lg-row ${n?'':'empty-rarity'}">
      <i style="background:${n?esc(r.color):'var(--muted)'}"></i>
      <span class="lg-name">${esc(r.name)}<em>${esc(r.label||'')}</em></span>
      <span class="lg-bar">${shareBar(real, n?r.color:'var(--muted)')}</span>
      <span class="lg-pct">${fmtPct(real)}%</span>
      <span class="lg-meta">${n}张${k>0?` · 保底${k}`:''} · 期望 ${exp.toFixed(1)}抽${diff?` <u>配置${fmtPct(raw)}%</u>`:''}</span>
    </div>`;
    }).join('');

    /* 缺卡告警：配置了概率却没有卡，概率被摊走 */
    const missRateTip = rates.missing.length ? `
    <div class="rate-warn">
      ⚠ <b>${rates.missing.join(' / ')}</b> 配置了概率（${rates.missing.map(k=>fmtPct(rates.raw[k])+'%').join(' + ')}）
      但奖池里没有该稀有度的卡，这部分概率已<b>摊到其它稀有度</b>。罗盘显示的是<b>实际概率</b>。
    </div>` : '';

    const overview = `
    <div class="card">
      <div class="card-head"><h3>概率罗盘</h3><span class="gacha-note">权重改动后此处实时重算</span></div>
      ${ssrStart>0?`<div class="rate-warn">
        ⏳ <b>出货窗口 ${ssrStart} ~ ${ssrPity} 抽</b>：距上次出 SSR 不满 ${ssrStart} 抽时，
        SSR 概率为 <b>0</b>；进入窗口后按 ${fmtPct(ssrRate)}% 抽，第 ${ssrPity} 抽必出。
        平均 <b>${ssrExp.toFixed(1)}</b> 抽出货。
      </div>`:''}
      <div class="overview">
        <div class="ov-chart">
          ${donutChart(donutData, ssrExp.toFixed(1), '抽 / SSR')}
        </div>
        <div class="ov-legend">${legend}</div>
        ${missRateTip}
        <div class="ov-kpi">
          <div><span>SSR 基础概率</span><b>${fmtPct(ssrRate)}%</b></div>
          <div><span>含保底综合出货率</span><b class="hi">${fmtPct(ssrEff)}%</b></div>
          <div><span>平均出 SSR</span><b>${ssrExp.toFixed(1)} 抽</b></div>
          ${ssrStart>0?`<div><span>出货窗口</span><b class="hi">${ssrStart} ~ ${ssrPity} 抽</b>
            <span style="font-size:10px;color:var(--muted)">前 ${ssrStart} 抽绝不出</span></div>`:''}
          <div><span>平均花费</span><b>${money(Math.round(ssrExp*(pool.costSingle||0)))} ${esc(pool.currency||'')}</b></div>
          <div><span>卡牌总数</span><b>${cardN}</b></div>
          <div><span>UP 角色</span><b>${pool.rateUp.npcId ? esc((DB.NPCS.find(n=>String(n.id)===String(pool.rateUp.npcId))||{}).name || pool.rateUp.npcId) : '无'}</b></div>
        </div>
      </div>
    </div>`;

    /* ================= ③ 保底规则 ================= */
    const pity = `
    <div class="card">
      <div class="card-head"><h3>保底规则</h3><span class="gacha-note">改动后点保存</span></div>
      <div class="gacha-pity">
        <div class="field"><label>SSR 保底（抽）</label>
          <input id="g_ssrPity" type="number" min="0" value="${ssrPity}">
          <span class="gacha-hint">距上次出 SSR 满 N 抽，第 N 抽必出</span></div>
        <div class="field"><label>SSR 出货窗口起点（抽）</label>
          <input id="g_ssrStart" type="number" min="0" value="${+pool.pity.ssrStart||0}">
          <span class="gacha-hint">★ 满 N 抽后才<b>可能</b>出 SSR。例：起点 250 + 保底 300 → 前 250 抽绝不出，只在 250~300 出。0=不限</span></div>
        <div class="field"><label>SR 保底（抽）</label>
          <input id="g_srPity" type="number" min="0" value="${+pool.pity.sr||0}"></div>
        <div class="field"><label>SR 出货窗口起点（抽）</label>
          <input id="g_srStart" type="number" min="0" value="${+pool.pity.srStart||0}">
          <span class="gacha-hint">满 N 抽后才可能出 SR，0=不限</span></div>
        <div class="field"><label>最低门槛（抽）</label>
          <input id="g_minDraws" type="number" min="0" value="${+pool.minSsrDraws||0}">
          <span class="gacha-hint">全局默认门槛，单个玩家可在下方单独设</span></div>
        <div class="field"><label>UP 角色</label>
          <select id="g_upNpc">
            <option value="">无</option>
            ${DB.NPCS.map(n=>`<option value="${esc(n.id)}" ${String(pool.rateUp.npcId)===String(n.id)?'selected':''}>${esc(n.icon+' '+n.name)}</option>`).join('')}
          </select></div>
        <div class="field"><label>UP 权重加成</label>
          <input id="g_upBonus" type="number" min="1" step="0.5" value="${+pool.rateUp.bonus||2}">
          <span class="gacha-hint">UP 卡权重 × 该倍数</span></div>
        <div class="field gacha-check">
          <label><input id="g_resetOnHit" type="checkbox" ${pool.pity.resetOnHit===false?'':'checked'}> 出货后重置保底计数</label>
          <label style="margin-top:8px"><input id="g_status" type="checkbox" ${pool.status==='启用'?'checked':''}> 奖池启用</label>
        </div>
      </div>
      <div class="modal-foot" style="padding:12px 0 0">
        <button class="btn-primary" onclick="saveGachaPity('${esc(pool.id)}')">保存规则</button>
        <button class="btn-danger" onclick="delGachaPool('${esc(pool.id)}')">删除奖池</button>
      </div>
    </div>`;

    /* ================= ④ 卡牌权重（可视化条 + 网格） ================= */
    const groups = rarities.map(r=>{
      const cards = (pool.cards||[]).filter(c=>c.rarity===r.key);
      if (!cards.length) return '';
      const wsum = gachaRarityWeight(pool, r.key);
      const rows = cards.map(c=>{
        const prob  = gachaCardProb(pool, c);
        const share = wsum ? gachaEffWeight(pool,c)/wsum*100 : 0;
        const npc   = DB.NPCS.find(n=>String(n.id)===String(c.npc));
        /* 卡面缩略图：有图显示图，没图用稀有度配色的文字占位，一眼看出哪些还没配图 */
        const thumbHtml = c.thumb
            ? `<img class="gw-thumb" src="${esc(c.thumb)}" alt="" loading="lazy"
             onerror="this.outerHTML='<span class=\'gw-thumb ph\' style=\'--rc:${esc(r.color)}\'>${esc((c.name||'?').slice(0,1))}</span>'">`
            : `<span class="gw-thumb ph" style="--rc:${esc(r.color)}" title="未配置缩略图">${esc((c.name||'?').slice(0,1))}</span>`;
        return `<div class="gw-card" style="--rc:${esc(r.color)}">
        <div class="gw-head">
          ${thumbHtml}
          <span class="gacha-rarity" style="background:${esc(r.color)}">${esc(r.name)}</span>
          <span class="gw-name">${esc(c.name)}</span>
          ${c.up?'<span class="badge badge-warn">UP</span>':''}
          <span class="gw-act">
            <button class="btn-ghost" onclick="openGachaCardForm('${esc(pool.id)}','${esc(c.id)}')">编辑</button>
            <button class="btn-danger" onclick="delGachaCard('${esc(pool.id)}','${esc(c.id)}')">删</button>
          </span>
        </div>
        <div class="gw-sub">${npc?esc(npc.icon+' '+npc.name):'无关联角色'}${c.stock>=0?` · 库存 ${c.stock}`:' · 不限量'}
          ${c.thumb&&c.image?' · <b class="ok">图✓</b>':(c.thumb||c.image?' · <b class="warn">图半配</b>':' · <b class="bad">无图</b>')}</div>
        <div class="gw-slider">
          <input type="range" min="0" max="100" step="1" value="${+c.weight||0}"
            oninput="gachaWeightInput('${esc(pool.id)}','${esc(c.id)}',this.value)"
            onchange="gachaWeightCommit('${esc(pool.id)}','${esc(c.id)}',this.value)">
          <div class="gw-nums">
            <span>权重 <b id="gw_${esc(c.id)}">${+c.weight||0}</b></span>
            <span>稀有度内 <b id="gs_${esc(c.id)}">${fmtPct(share,1)}</b>%</span>
            <span class="gw-prob">综合 <b id="gp_${esc(c.id)}">${fmtPct(prob)}</b>%</span>
          </div>
        </div>
        ${shareBar(share, r.color)}
      </div>`;
      }).join('');
      return `<div class="gw-group">
      <div class="gw-group-head">
        <span class="gacha-rarity" style="background:${esc(r.color)}">${esc(r.name)}</span>
        <span class="gw-group-meta">${cards.length} 张 · 总权重 ${Math.round(wsum)} · 稀有度概率 ${fmtPct(gachaRarityRate(r.key))}%</span>
      </div>
      <div class="gw-grid">${rows}</div>
    </div>`;
    }).join('');

    /* 未配图统计：小程序抽到没图的卡会显示空白，这里提前提醒 */
    const noImg = (pool.cards||[]).filter(c=>!c.thumb && !c.image).length;
    const halfImg = (pool.cards||[]).filter(c=>!!c.thumb !== !!c.image).length;
    const imgTip = (noImg || halfImg) ? `
    <div class="rate-warn" style="margin-bottom:12px">
      🖼 共 ${cardN} 张卡：<b>${noImg}</b> 张未配图、<b>${halfImg}</b> 张只配了单张。
      小程序抽到未配图的卡会显示空白卡面，建议补齐缩略图与大图。
    </div>` : '';
    const cardsHtml = `
    <div class="card">
      <div class="card-head"><h3>卡牌与权重</h3>
        <button class="btn-primary" onclick="openGachaCardForm('${esc(pool.id)}','')">+ 新增卡牌</button></div>
      <div class="gacha-hint" style="margin-bottom:12px">拖动滑块调整权重，概率实时重算；松手后自动保存。</div>
      ${imgTip}
      ${groups || '<div class="empty">该奖池暂无卡牌</div>'}
    </div>`;

    /* ================= ⑤ 出货模拟器 ================= */
    const simBody = GACHA_SIM ? simResultHtml(GACHA_SIM, pool) : `
    <div class="sim-empty">
      <div class="sim-empty-ic">🎲</div>
      <div>输入模拟次数，跑一遍蒙特卡洛，看改完权重后<b>实际</b>出货分布</div>
      <div class="sim-empty-sub">会完整复刻保底、UP 加成、库存耗尽，结果可直接对照上方配置</div>
    </div>`;
    const simulator = `
    <div class="card">
      <div class="card-head"><h3>出货模拟器</h3><span class="gacha-note">不改数据库，纯本地演算</span></div>
      <div class="sim-bar">
        <label>模拟次数</label>
        <select id="simN">
          <option value="10000">1 万抽</option>
          <option value="50000" selected>5 万抽</option>
          <option value="100000">10 万抽</option>
        </select>
        <label>最低门槛</label>
        <input id="simMin" type="number" min="0" value="0" style="width:90px">
        <button class="btn-primary" onclick="runSim('${esc(pool.id)}')">开始模拟</button>
        <button class="btn-ghost" onclick="clearSim('${esc(pool.id)}')">清空</button>
      </div>
      <div id="simBox">${simBody}</div>
    </div>`;

    /* ================= ⑥ 玩家保底 ================= */
    const playerPity = `
    <div class="card">
      <div class="card-head"><h3>玩家保底设置</h3>
        <button class="btn-primary" onclick="openUserPityForm('${esc(pool.id)}','')">+ 设置玩家</button></div>
      <div class="gacha-hint" style="margin-bottom:10px">
        为单个玩家单独指定保底抽数（留空 = 跟随奖池的 <b>${ssrPity}</b> 抽）；
        「最低门槛」表示累计抽够该抽数后才<b>可能</b>出 SSR。
      </div>
      <div id="userPityBox"><div class="empty" style="padding:14px">选一个玩家查看他的保底规则 →
        <select id="upPlayer" onchange="loadUserPity('${esc(pool.id)}',this.value)" style="margin-left:8px;min-width:150px">
          <option value="">请选择玩家</option>
          ${DB.PLAYERS.map(p=>`<option value="${esc(p.id)}">${esc(p.name)}</option>`).join('')}
        </select></div></div>
    </div>`;

    set(`${head}${missTip}${poolCards}${overview}${pity}${cardsHtml}${simulator}${playerPity}`);
  }

  /* 模拟器结果渲染 */
  function simResultHtml(sim, pool){
    const rows = gachaRarities().map(r=>{
      const got  = sim.cnt[r.key] || 0;
      const real = got / sim.n * 100;
      const k    = r.key==='SSR' ? (+pool.pity.ssr||0) : (r.key==='SR' ? (+pool.pity.sr||0) : 0);
      /* 窗口起点（不能超过保底） */
      const st   = r.key==='SSR' ? Math.min(+pool.pity.ssrStart||0, k||Infinity)
          : (r.key==='SR' ? Math.min(+pool.pity.srStart ||0, k||Infinity) : 0);
      /* 理论出货率用「实际概率」（缺卡已摊算）+ 保底折算 */
      const cfg  = gachaRealRates(pool).real[r.key];
      const theo = k > 0 ? gachaEffectiveRate(k, cfg, st) : cfg;
      const diff = real - theo;
      /* ★ 阈值分档：SSR/SR 有保底折算，理论值精确，可严判；
       *   R/N 无保底，但会被 SR 保底「挤压」，实测必然低于配置值，属正常，放宽判定。 */
      const cls  = k > 0
          ? (Math.abs(diff) < 0.3 ? 'ok' : (Math.abs(diff) < 1 ? 'warn' : 'bad'))
          : (Math.abs(diff) < 2 ? 'ok' : (Math.abs(diff) < 4 ? 'warn' : 'bad'));
      return `<div class="sim-row">
      <span class="gacha-rarity" style="background:${esc(r.color)}">${esc(r.name)}</span>
      <span class="sim-bar-wrap">${shareBar(real, r.color)}</span>
      <span class="sim-real">${fmtPct(real)}%</span>
      <span class="sim-theo">理论 ${fmtPct(theo)}%</span>
      <span class="sim-diff ${cls}">${diff>=0?'+':''}${fmtPct(diff)}</span>
      <span class="sim-cnt">${got.toLocaleString()} 次</span>
    </div>`;
    }).join('');

    /* 出货最多的卡 TOP5 */
    const top = Object.keys(sim.cardCnt).map(id=>({
      id, n: sim.cardCnt[id],
      card: (pool.cards||[]).find(c=>String(c.id)===String(id))
    })).filter(x=>x.card).sort((a,b)=>b.n-a.n).slice(0,5);
    const topHtml = top.map(x=>{
      const meta = gachaRarityMeta(x.card.rarity);
      const pct  = x.n / sim.n * 100;
      return `<div class="sim-top-row">
      <span class="gacha-rarity" style="background:${esc(meta.color)}">${esc(meta.name)}</span>
      <span class="sim-top-name">${esc(x.card.name)}</span>
      <span class="sim-bar-wrap">${shareBar(pct, meta.color)}</span>
      <span class="sim-real">${fmtPct(pct)}%</span>
      <span class="sim-cnt">${x.n.toLocaleString()}</span>
    </div>`;
    }).join('');

    return `<div class="sim-result">
    <div class="sim-kpi">
      <div><span>SSR 平均间隔</span><b>${sim.avgGap ? sim.avgGap.toFixed(1) : '—'} 抽</b></div>
      <div><span>SSR 最长空窗</span><b class="${sim.maxGap > (+pool.pity.ssr||0) && (+pool.pity.ssr||0)>0 ? 'bad' : ''}">${sim.maxGap} 抽</b></div>
      <div><span>首次 SSR</span><b>${sim.firstSsrAt > 0 ? '第 '+sim.firstSsrAt+' 抽' : '未出货'}</b></div>
      <div><span>保底触发占比</span><b>${fmtPct(sim.pityRate)}%</b></div>
    </div>
    <div class="sim-head">
      <span style="width:52px">稀有度</span>
      <span>实测分布</span>
      <span style="width:70px;text-align:right">实测</span>
      <span style="width:92px;text-align:right">理论（含保底）</span>
      <span style="width:66px;text-align:right">偏差</span>
      <span style="width:90px;text-align:right">出货次数</span>
    </div>
    <div class="sim-table">${rows}</div>
    ${topHtml ? `<div class="sim-top"><div class="sim-top-title">出货最多的卡 TOP5</div>${topHtml}</div>` : ''}
    <div class="sim-note">
      SSR / SR 有保底，理论值已含保底折算，实测应与其高度吻合。<br>
      R / N 本身没有保底，但会被 SR 保底<b>挤压</b>一部分（每 ${(+pool.pity.sr||0)||'—'} 抽必出 SR 及以上），
      所以实测略低于配置值属<b>正常现象</b>，差值越大说明保底越"强势"。
    </div>
    <div class="sim-foot">模拟 ${sim.n.toLocaleString()} 抽 · 与后端同一套算法（保底优先 → 概率随机 → 权重选卡）</div>
  </div>`;
  }

  /* 跑模拟 */
  function runSim(poolId){
    const pool = gachaPoolById(poolId);
    if (!pool) return;
    const n   = +($('#simN') ? $('#simN').value : 50000) || 50000;
    const min = +($('#simMin') ? $('#simMin').value : 0) || 0;
    const box = document.getElementById ? document.getElementById('simBox') : null;
    if (box) box.innerHTML = '<div class="empty">⏳ 模拟中…</div>';
    /* 让浏览器先画出 loading 再算，避免卡 UI */
    setTimeout(()=>{
      const t0 = Date.now();
      const sim = gachaSimulate(pool, n, min);
      if (!sim){ if (box) box.innerHTML = '<div class="empty">该奖池没有可出货的卡牌</div>'; return; }
      GACHA_SIM = sim;
      if (box) box.innerHTML = simResultHtml(sim, pool) +
          `<div class="sim-foot">耗时 ${Date.now()-t0} ms</div>`;
      toast(`模拟完成：${n.toLocaleString()} 抽`);
    }, 30);
  }
  function clearSim(poolId){
    GACHA_SIM = null;
    const box = document.getElementById ? document.getElementById('simBox') : null;
    if (box) box.innerHTML = `
    <div class="sim-empty">
      <div class="sim-empty-ic">🎲</div>
      <div>输入模拟次数，跑一遍蒙特卡洛，看改完权重后<b>实际</b>出货分布</div>
      <div class="sim-empty-sub">会完整复刻保底、UP 加成、库存耗尽，结果可直接对照上方配置</div>
    </div>`;
  }


  /* ---------- 玩家保底覆盖 ---------- */
  async function loadUserPity(poolId, userId){
    const box = document.getElementById ? document.getElementById('userPityBox') : null;
    if (!box) return;
    if (!userId){ box.innerHTML = '<div class="empty" style="padding:14px">请选择玩家</div>'; return; }
    if (!gachaApi()){ box.innerHTML = '<div class="empty">接口未加载</div>'; return; }
    box.innerHTML = '<div class="empty" style="padding:14px">⏳ 查询中…</div>';
    try{
      const info = await API.gacha.userPity(userId, poolId);
      const pool = gachaPoolById(poolId) || {};
      const defSsr = +pool.pity.ssr || 0;
      const effSsr = info && info.ssrPity != null ? info.ssrPity : defSsr;
      const isPersonal = info && info.personal === 1;
      box.innerHTML = `
      <div class="gacha-userpity">
        <div class="up-row"><span>生效 SSR 保底</span>
          <b>${effSsr} 抽</b>
          ${isPersonal?`<span class="badge badge-warn">个人设置</span>`:`<span class="badge">跟随奖池 ${defSsr}</span>`}</div>
        <div class="up-row"><span>距上次出 SSR</span><b>${info?info.ssrCount:0} 抽</b>
          <span class="up-sub">再抽 ${info&&info.ssrRemain>=0?info.ssrRemain:'∞'} 抽必出</span></div>
        <div class="up-row"><span>最低门槛</span>
          <b>${info&&info.minSsrDraws>0?info.minSsrDraws+' 抽':'未设置'}</b>
          ${info&&info.locked===1?`<span class="badge badge-warn">锁定中，还需 ${info.unlockRemain} 抽</span>`:''}</div>
        <div class="up-row"><span>累计抽数</span><b>${info?info.totalCount:0} 抽</b></div>
        <div class="up-act">
          <button class="btn-ghost" onclick="openUserPityForm('${esc(poolId)}','${esc(userId)}')">修改</button>
          <button class="btn-danger" onclick="resetUserPity('${esc(poolId)}','${esc(userId)}')">恢复跟随奖池</button>
        </div>
      </div>`;
    }catch(e){
      box.innerHTML = '<div class="empty" style="padding:14px">查询失败：' + esc(e && e.message || '') + '</div>';
    }
  }

  function openUserPityForm(poolId, userId){
    const pool = gachaPoolById(poolId) || {};
    const defSsr = +pool.pity.ssr || 0;
    openModal(`<div class="modal-head"><h2>设置玩家保底</h2><button class="modal-close" onclick="tryCloseModal()">×</button></div>
    <div class="field"><label>玩家 *</label>
      <select id="upf_user">
        <option value="">请选择</option>
        ${DB.PLAYERS.map(p=>`<option value="${esc(p.id)}" ${String(p.id)===String(userId)?'selected':''}>${esc(p.name)}</option>`).join('')}
      </select></div>
    <div class="field"><label>个人 SSR 保底（抽）</label>
      <input id="upf_ssr" type="number" min="0" placeholder="留空或 0 = 跟随奖池（${defSsr} 抽）">
      <span class="gacha-hint">累计 N 抽未出 SSR，第 N 抽必出</span></div>
    <div class="field"><label>个人 SR 保底（抽）</label>
      <input id="upf_sr" type="number" min="0" placeholder="留空或 0 = 跟随奖池"></div>
    <div class="field"><label>最低门槛（抽）</label>
      <input id="upf_min" type="number" min="0" value="0">
      <span class="gacha-hint">累计抽够该抽数后才<b>可能</b>出 SSR，0 = 不限制</span></div>
    <div class="field"><label>备注</label><input id="upf_remark" placeholder="如：内部测试号"></div>
    <div class="modal-foot"><button class="btn-ghost" onclick="tryCloseModal()">取消</button>
      <button class="btn-primary" onclick="saveUserPity('${esc(poolId)}')">保存</button></div>`, true);
  }
  async function saveUserPity(poolId){
    const userId = $('#upf_user') ? $('#upf_user').value : '';
    if (!userId){ toast('请选择玩家','err'); return; }
    const req = {
      poolId: poolId,
      ssrPity: +($('#upf_ssr').value||0) || 0,
      srPity : +($('#upf_sr').value ||0) || 0,
      minSsrDraws: +($('#upf_min').value||0) || 0,
      remark: $('#upf_remark').value || ''
    };
    if (!gachaApi()) { gachaApiMissing('设置玩家保底'); return; }
    const ok = await withApi('保存', async ()=>{ await API.gacha.setUserPity(userId, req); });
    if (!ok) return;
    closeModal(); toast('已保存');
    const sel = document.getElementById ? document.getElementById('upPlayer') : null;
    if (sel) sel.value = userId;
    await loadUserPity(poolId, userId);
  }
  async function resetUserPity(poolId, userId){
    if (!confirm('确定恢复该玩家跟随奖池默认保底？')) return;
    if (!gachaApi()) { gachaApiMissing('恢复'); return; }
    const ok = await withApi('恢复', async ()=>{
      await API.gacha.setUserPity(userId, { poolId, ssrPity:0, srPity:0, minSsrDraws:0, remark:'' });
    });
    if (!ok) return;
    toast('已恢复'); await loadUserPity(poolId, userId);
  }

  /* 切换奖池 */
  function switchGachaPool(id){
    GACHA_CURRENT = id;
    GACHA_SIM = null;      /* 换奖池后旧模拟结果就失效了，清掉避免误导 */
    renderGacha();
  }

  /* 滑块拖动中：只改本地 + 实时重算概率，不发请求 */
  function gachaWeightInput(poolId, cardId, val){
    const pool = gachaPoolById(poolId); if (!pool) return;
    const c = (pool.cards||[]).find(x=>String(x.id)===String(cardId)); if (!c) return;
    c.weight = +val || 0;
    /* 只更新受影响的三处数字，避免整页重绘打断拖动 */
    const put=(id,txt)=>{ const el=document.getElementById?document.getElementById(id):null; if(el) el.textContent=txt; };
    put('gw_'+cardId, String(c.weight));
    put('gp_'+cardId, fmtPct(gachaCardProb(pool, c)));
    const wsum = gachaRarityWeight(pool, c.rarity);
    put('gs_'+cardId, fmtPct(wsum ? gachaEffWeight(pool,c)/wsum*100 : 0, 1));
    /* 同稀有度其它卡的占比会变，一并刷新 */
    (pool.cards||[]).filter(x=>x.rarity===c.rarity).forEach(x=>{
      if (String(x.id)===String(cardId)) return;
      const ws = gachaRarityWeight(pool, x.rarity);
      put('gs_'+x.id, fmtPct(ws ? gachaEffWeight(pool,x)/ws*100 : 0, 1));
      put('gp_'+x.id, fmtPct(gachaCardProb(pool, x)));
    });
  }
  /* 松手后：防抖保存（优先走单卡权重接口，后端不支持则整池提交） */
  function gachaWeightCommit(poolId, cardId, val){
    const pool = gachaPoolById(poolId); if (!pool) return;
    const c = (pool.cards||[]).find(x=>String(x.id)===String(cardId)); if (!c) return;
    c.weight = +val || 0;
    clearTimeout(GACHA_SAVE_TIMER);
    GACHA_SAVE_TIMER = setTimeout(async ()=>{
      if (!useApi()){ toast('本地模式：权重已改（接口未启用）'); return; }
      const G = gachaApi();
      if (!G) return gachaApiMissing('保存权重');
      const ok = await withApi('保存权重', async ()=>{
        try{ if (G.setWeight) await G.setWeight(cardId, c.weight); else throw new Error('no setWeight'); }
        catch(e){ await G.updatePool(poolId, pool); }   // 单卡接口不存在 → 整池提交
      });
      if (ok){ toast('权重已保存'); }
      else { toast('保存失败，已回滚','err'); await resync('gacha').catch(()=>{}); renderGacha(); }
    }, 400);
  }

  /* 保存保底规则 */
  async function saveGachaPity(poolId){
    const pool = gachaPoolById(poolId); if (!pool) return;
    const d = {
      pity:{
        ssr:+($('#g_ssrPity')?$('#g_ssrPity').value:0)||0,
        sr :+($('#g_srPity') ?$('#g_srPity').value :0)||0,
        ssrStart:+($('#g_ssrStart')?$('#g_ssrStart').value:0)||0,
        srStart :+($('#g_srStart') ?$('#g_srStart').value :0)||0,
        resetOnHit: !!($('#g_resetOnHit') && $('#g_resetOnHit').checked)
      },
      rateUp:{ npcId:$('#g_upNpc')?$('#g_upNpc').value:'', bonus:+($('#g_upBonus')?$('#g_upBonus').value:2)||2 },
      /* 全局默认最低门槛：新玩家默认沿用这个值，单个玩家可在下方单独覆盖 */
      minSsrDraws: +($('#g_minDraws')?$('#g_minDraws').value:0)||0,
      status: ($('#g_status') && $('#g_status').checked) ? '启用' : '未启用'
    };
    Object.assign(pool, d);
    if (useApi()){
      const G = gachaApi();
      if (!G) return gachaApiMissing('保存规则');
      const ok = await withApi('保存规则', async ()=>{ await G.updatePool(poolId, Object.assign({}, pool, d)); });
      if (!ok){ await resync('gacha').catch(()=>{}); renderGacha(); return; }
    }
    toast('已保存');
    renderGacha();
  }

  /* 奖池表单 */
  function openGachaPoolForm(id){
    const p = id ? gachaPoolById(id) : null;
    openModal(`<div class="modal-head"><h2>${p?'编辑奖池':'新增奖池'}</h2><button class="modal-close" onclick="tryCloseModal()">×</button></div>
    <div class="field"><label>奖池名称 *</label><input id="gp_name" value="${esc(p?p.name:'')}"></div>
    <div class="field-row">
      <div class="field"><label>单抽价格</label><input id="gp_single" type="number" value="${p?p.costSingle:30}"></div>
      <div class="field"><label>十连价格</label><input id="gp_ten" type="number" value="${p?p.costTen:300}"></div>
    </div>
    <div class="field-row">
      <div class="field"><label>货币名</label><input id="gp_currency" value="${esc(p?p.currency:'星尘')}"></div>
      <div class="field"><label>状态</label><select id="gp_status"><option>启用</option><option>未启用</option></select></div>
    </div>
    <div class="modal-foot"><button class="btn-ghost" onclick="tryCloseModal()">取消</button><button class="btn-primary" onclick="saveGachaPool('${esc(p?p.id:'')}')">保存</button></div>`, true);
    setTimeout(()=>{ const st=$('#gp_status'); if(st&&p) st.value=p.status; },0);
  }
  async function saveGachaPool(id){
    const name = $('#gp_name') ? $('#gp_name').value.trim() : '';
    if (!name){ toast('请填写奖池名称','err'); return; }
    const d = {
      name:name,
      costSingle:+$('#gp_single').value||0,
      costTen:+$('#gp_ten').value||0,
      currency:$('#gp_currency').value.trim()||'星尘',
      status:$('#gp_status').value
    };
    if (useApi()){
      const G = gachaApi();
      if (!G) return gachaApiMissing('保存奖池');
      const ok = await withApi('保存', async ()=>{
        if (id) await G.updatePool(id, d);
        else   { const np = await G.createPool(Object.assign({}, d, {cards:[], pity:{ssr:80,sr:10,resetOnHit:true}, rateUp:{npcId:'',bonus:2}})); if(np&&np.id) GACHA_CURRENT=np.id; }
        await resync('gacha');
      });
      if (!ok) return;
    }else{
      if (id){ Object.assign(gachaPoolById(id)||{}, d); }
      else { const nums=(DB.GACHA_POOLS||[]).map(x=>parseInt(String(x.id).replace(/[^0-9]/g,''))||0);
        const np = Object.assign({ id:'P'+String(Math.max(0,...nums)+1).padStart(3,'0'), cards:[], pity:{ssr:80,sr:10,resetOnHit:true}, rateUp:{npcId:'',bonus:2} }, d); DB.GACHA_POOLS.push(np); GACHA_CURRENT=np.id; }
    }
    closeModal(); toast(id?'已更新':'已新增'); renderGacha();
  }
  async function delGachaPool(id){
    if (!confirm('确定删除该奖池？奖池内卡牌也会一并删除')) return;
    if (useApi()){
      const G = gachaApi();
      if (!G) return gachaApiMissing('删除奖池');
      const ok = await withApi('删除', async ()=>{ await G.removePool(id); await resync('gacha'); });
      if (!ok) return;
    }else{
      DB.GACHA_POOLS = gachaPools().filter(p=>String(p.id)!==String(id));
    }
    GACHA_CURRENT=''; toast('已删除'); renderGacha();
  }

  /* 卡牌表单 */
  /* 卡面图实时预览：输入 URL 后立刻看效果，不用保存才知道填错没 */
  function previewCardImg(inputId, pvId){
    const el = document.getElementById ? document.getElementById(inputId) : null;
    const pv = document.getElementById ? document.getElementById(pvId)    : null;
    if (!el || !pv) return;
    const v = (el.value || '').trim();
    if (!v){ pv.innerHTML = ''; return; }
    pv.innerHTML = `<img src="${esc(v)}" onerror="this.parentNode.innerHTML='<span class=img-pv-err>加载失败，检查 URL</span>'">`;
  }

  function openGachaCardForm(poolId, cardId){
    const pool = gachaPoolById(poolId); if (!pool) return;
    const c = cardId ? (pool.cards||[]).find(x=>String(x.id)===String(cardId)) : null;
    let rarities = gachaRarities();
    if (!rarities.length) rarities = DEFAULT_RARITY.slice();   /* 兜底：至少让下拉能用 */
    /* 新增时默认选第一个（最高稀有度），避免"没选"导致存进去是空 */
    const defKey = c ? '' : rarities[0].key;
    const emptyTip = rarities.length ? '' : `
    <div class="rate-warn" style="margin-bottom:10px">
      ⚠ 未读到稀有度定义。请在数据库 <code>gacha_rarity</code> 表写入数据，SQL 见 <code>01_建表.sql</code> 第 3 段。
    </div>`;
    openModal(`${emptyTip}<div class="modal-head"><h2>${c?'编辑卡牌':'新增卡牌'}</h2><button class="modal-close" onclick="tryCloseModal()">×</button></div>
    <div class="field"><label>卡牌名称 *</label><input id="gc_name" value="${esc(c?c.name:'')}"></div>
    <div class="field-row">
      <div class="field"><label>稀有度</label><select id="gc_rarity">${rarities.map(r=>`<option value="${esc(r.key)}" ${(c&&c.rarity===r.key)||(!c&&r.key===defKey)?'selected':''}>${esc(r.name)} · ${esc(r.label||'')}${r.rate?` (${fmtPct(r.rate)}%)`:''}</option>`).join('')}</select></div>
      <div class="field"><label>关联 NPC</label><select id="gc_npc"><option value="">无</option>${DB.NPCS.map(n=>`<option value="${esc(n.id)}" ${c&&String(c.npc)===String(n.id)?'selected':''}>${esc(n.icon+' '+n.name)}</option>`).join('')}</select></div>
    </div>
    <div class="field"><label>缩略图 URL</label>
      <input id="gc_thumb" value="${esc(c?(c.thumb||''):'')}" placeholder="https://…/card_300.jpg"
        oninput="previewCardImg('gc_thumb','gc_thumb_pv')">
      <span class="gacha-hint">列表 / 十连九宫格 / 我的卡牌用。建议 300×300，&lt;50KB</span>
      <div class="img-pv" id="gc_thumb_pv">${(c&&c.thumb)?`<img src="${esc(c.thumb)}" onerror="this.parentNode.innerHTML='<span class=img-pv-err>加载失败</span>'">`:''}</div>
    </div>
    <div class="field"><label>大图 URL</label>
      <input id="gc_image" value="${esc(c?(c.image||''):'')}" placeholder="https://…/card_750.jpg"
        oninput="previewCardImg('gc_image','gc_image_pv')">
      <span class="gacha-hint">奖品详情 / 单抽全屏立绘 / 分享海报用。建议 750×1000，&lt;200KB</span>
      <div class="img-pv" id="gc_image_pv">${(c&&c.image)?`<img src="${esc(c.image)}" onerror="this.parentNode.innerHTML='<span class=img-pv-err>加载失败</span>'">`:''}</div>
    </div>
    <div class="field-row">
      <div class="field"><label>权重</label><input id="gc_weight" type="number" min="0" value="${c?c.weight:10}"></div>
      <div class="field"><label>库存（-1 不限量）</label><input id="gc_stock" type="number" value="${c?c.stock:-1}"></div>
    </div>
    <div class="field gacha-check"><label><input id="gc_up" type="checkbox" ${c&&c.up?'checked':''}> 设为 UP（权重 × ${+pool.rateUp.bonus||2}）</label></div>
    <div class="modal-foot"><button class="btn-ghost" onclick="tryCloseModal()">取消</button><button class="btn-primary" onclick="saveGachaCard('${esc(poolId)}','${esc(c?c.id:'')}')">保存</button></div>`, true);
  }
  async function saveGachaCard(poolId, cardId){
    /* 防空：稀有度为空时给明确提示，而不是存进去一个空稀有度 */
    if (!$('#gc_rarity') || !$('#gc_rarity').value){
      toast('请选择稀有度。若下拉为空，请检查 gacha_rarity 表是否有数据','err'); return;
    }
    const pool = gachaPoolById(poolId); if (!pool) return;
    const name = $('#gc_name') ? $('#gc_name').value.trim() : '';
    if (!name){ toast('请填写卡牌名称','err'); return; }
    const d = {
      name:name,
      rarity:$('#gc_rarity').value,
      thumb:($('#gc_thumb')?$('#gc_thumb').value:'').trim(),
      image:($('#gc_image')?$('#gc_image').value:'').trim(),
      npc:$('#gc_npc').value,
      weight:+$('#gc_weight').value||0,
      stock:+$('#gc_stock').value,
      up:!!($('#gc_up') && $('#gc_up').checked)
    };
    if (cardId){
      const c = (pool.cards||[]).find(x=>String(x.id)===String(cardId));
      if (c) Object.assign(c, d);
    }else{
      /* id 不能依赖 DB.GACHA_CARDS（表不存在），改为在奖池内现有卡牌上自增 */
      const nums = (pool.cards||[]).map(x=>parseInt(String(x.id).replace(/[^0-9]/g,''))||0);
      d.id = 'C' + String(Math.max(0,...nums)+1).padStart(3,'0');
      (pool.cards = pool.cards || []).push(d);
    }
    if (useApi()){
      const G = gachaApi();
      if (!G) return gachaApiMissing('保存卡牌');
      const ok = await withApi('保存', async ()=>{ await G.updatePool(poolId, pool); });
      if (!ok){ await resync('gacha').catch(()=>{}); renderGacha(); return; }
    }
    closeModal(); toast(cardId?'已更新':'已新增'); renderGacha();
  }
  async function delGachaCard(poolId, cardId){
    if (!confirm('确定删除该卡牌？')) return;
    const pool = gachaPoolById(poolId); if (!pool) return;
    pool.cards = (pool.cards||[]).filter(x=>String(x.id)!==String(cardId));
    if (useApi()){
      const G = gachaApi();
      if (!G) return gachaApiMissing('删除卡牌');
      const ok = await withApi('删除', async ()=>{ await G.updatePool(poolId, pool); });
      if (!ok){ await resync('gacha').catch(()=>{}); renderGacha(); return; }
    }
    toast('已删除'); renderGacha();
  }

  /* ==================== 场次管理 ==================== */
  /* 把后端任意时间格式转成 <input type="datetime-local"> 唯一能识别的 'YYYY-MM-DDTHH:mm'。
   * ★ 关键：该控件只认带 T 的格式，后端 @JsonFormat("yyyy-MM-dd HH:mm:ss") 返回
   *   '2026-09-25 12:00:00'（空格分隔）时浏览器会判为无效值 → 输入框显示空白（就是"没回显"的原因）。
   * 兼容：'2026-09-25 12:00:00' / '2026-09-25T12:00:00' / '2026-09-25T12:00' / 时间戳 / Date */
  function toDatetimeLocal(v){
    if (v == null || v === '') return '';
    const raw = String(v).trim();
    if (!raw) return '';
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(raw)) return raw.slice(0, 16);   // 已是标准格式
    const d = new Date(raw.replace(/-/g, '/').replace('T', ' '));              // 兼容空格分隔
    if (isNaN(d.getTime())) return '';
    const p = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
  }
  /* 取场次的开抢时间（后端可能给驼峰字段，也可能给下划线列名） */
  function showSaleStart(sh){ return (sh && (sh.saleStartAt || sh.sale_start_at || sh.saleStart)) || ''; }

  /* ---------- 「今天 / 现在」默认值 ----------
   * ★ 必须用本地时区拼字符串，不能用 toISOString()：
   *   toISOString 按 UTC 取，东八区凌晨 0~8 点会算成「昨天」，日期就错一天。 */
  function todayStr(){
    const d = new Date(), p = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}`;
  }
  /* 本地时区的「现在」，返回 'YYYY-MM-DDTHH:mm'（给 datetime-local 用） */
  function nowLocalStr(){
    const d = new Date(), p = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  /* 开抢时间（预售专用）：返回 未开抢(倒计时) / 已开抢 / 空 */
  function saleStartInfo(sh){
    const raw = showSaleStart(sh);
    if (!raw) return null;
    const t = new Date(String(raw).replace(/-/g,'/').replace('T',' '));
    if (isNaN(t.getTime())) return { text: String(raw), started:false, soon:false };
    const now = new Date(), diff = t.getTime() - now.getTime();
    const fmt = d => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
    if (diff <= 0) return { text: fmt(t), started:true, soon:false };
    const day = Math.floor(diff/86400000);
    const hr  = Math.floor(diff%86400000/3600000);
    const mi  = Math.floor(diff%3600000/60000);
    const left = day > 0 ? `${day}天${hr}小时` : (hr > 0 ? `${hr}小时${mi}分` : `${mi}分钟`);
    return { text: fmt(t), started:false, soon:true, left:left };
  }

  /* 查询条件（不依赖 DOM，重渲染不丢） */
  const SHOW_FILTER = {
    kw:'', status:'', npcId:'', city:'',
    dateFrom:'', dateTo:'', priceMin:'', priceMax:''
  };
  let SHOW_SEARCH_TIMER = null;
  const SHOW_STATUS = ['在售','预售','满员','已结束','取消'];
  let SHOW_PANEL_OPEN = false;   // 高级筛选是否展开
  /* 视图：list=列表 / city=按城市 / status=按状态 / date=按日期 */
  let SHOW_VIEW = 'list';       /* 列表模式下的分组方式：list / city / status / date */
  let SHOW_MODE = 'list';       /* ★ 主展示模式：list 列表 / month 月历 / week 周历 */
  let SHOW_CAL_M  = '';         /* 月历当前月份 'YYYY-MM'，空=自动（今天或首个场次所在月） */
  let SHOW_CAL_D  = '';         /* 月历选中日期 'YYYY-MM-DD'，空=今天 */
  let SHOW_WEEK_S = '';
  let SHOW_PREFILL_DATE = '';   /* 从日历某天点「新增」时的预填日期 */

  /* 排序：date=开演时间 / seat=上座率 / sale=销量 / price=票价 */
  let SHOW_SORT = 'date';
  /* 快捷筛选（互斥场景标签，与 SHOW_FILTER 独立） */
  let SHOW_QUICK = '';

  /* 数字安全转换：避免 "328.00" / null / "" 造成误判 */
  function num(v){ const n = Number(v); return isFinite(n) ? n : null; }

  /* 命中条件：关键词 + 状态 + 角色 + 城市 + 日期区间 + 价格区间 */
  function matchShow(sh){
    const F = SHOW_FILTER;
    if (F.status && String(sh.status||'') !== F.status) return false;
    if (F.npcId && String(sh.npc||'') !== String(F.npcId)) return false;
    if (F.city  && String(sh.city||'') !== F.city) return false;

    /* 日期区间：show.date 形如 '2026-09-12'，字符串比较即为日期先后 */
    const d = String(sh.date||'');
    if (F.dateFrom && d && d < F.dateFrom) return false;
    if (F.dateTo   && d && d > F.dateTo)   return false;

    /* 价格区间 */
    const p = num(sh.price);
    if (F.priceMin !== '' && F.priceMin != null){
      const lo = num(F.priceMin); if (lo != null && (p == null || p <  lo)) return false;
    }
    if (F.priceMax !== '' && F.priceMax != null){
      const hi = num(F.priceMax); if (hi != null && (p == null || p >  hi)) return false;
    }

    const kw = String(F.kw||'').trim().toLowerCase();
    if (!kw) return true;
    const hay = [sh.id, sh.title, sh.city, sh.venue, sh.date].filter(Boolean).join(' ').toLowerCase();
    return kw.split(/\s+/).filter(Boolean).every(w => hay.indexOf(w) >= 0);
  }

  /* 带条件向后端查询；后端不支持这些参数时退回全量，前端兜底过滤 */
  async function fetchShows(){
    if (!useApi()) return;
    const F = SHOW_FILTER, params = {};
    if (String(F.kw||'').trim())  params.keyword  = String(F.kw).trim();
    if (F.status)   params.status   = F.status;
    if (F.npcId)    params.npcId    = F.npcId;
    if (F.city)     params.city     = F.city;
    if (F.dateFrom) params.dateFrom = F.dateFrom;
    if (F.dateTo)   params.dateTo   = F.dateTo;
    if (F.priceMin !== '' && F.priceMin != null) params.priceMin = F.priceMin;
    if (F.priceMax !== '' && F.priceMax != null) params.priceMax = F.priceMax;
    try{
      const l = await API.shows.query(params);
      DB.SHOWS = Array.isArray(l) ? l : [];
    }catch(e){
      /* 后端不接受这些参数（如返回 400）→ 退回全量，由前端过滤兜底 */
      await pull('shows').catch(()=>{});
    }
  }
  function resetShowFilter(){
    Object.assign(SHOW_FILTER, { kw:'', status:'', npcId:'', city:'', dateFrom:'', dateTo:'', priceMin:'', priceMax:'' });
    SHOW_QUICK = '';          // ★ 快捷标签也要一起清，否则"清除筛选"看起来没生效
    fetchShows().then(renderShow);
  }
  /* 高级筛选里的即时输入（防抖 300ms） */
  function showFilterInput(key, val){
    SHOW_FILTER[key] = val;
    clearTimeout(SHOW_SEARCH_TIMER);
    SHOW_SEARCH_TIMER = setTimeout(async ()=>{
      await fetchShows(); await renderShow();
    }, 300);
  }

  /* 金额显示：328 / 328.00 → ¥328；328.5 → ¥328.50 */
  function money(v){
    const n = Number(v);
    if (!isFinite(n)) return '¥' + esc(v);
    return '¥' + (Number.isInteger(n) ? n : n.toFixed(2));
  }

  /* 高级筛选面板（可折叠） */
  function showFilterPanel(){
    const F = SHOW_FILTER;
    const cities = [...new Set(DB.SHOWS.map(s=>String(s.city||'')))].filter(Boolean).sort();
    const has = k => (F[k] !== '' && F[k] != null);
    const advCount = ['city','dateFrom','dateTo','priceMin','priceMax'].filter(has).length;
    return `<div class="card" style="margin:-6px 0 14px;padding:12px 14px">
    <div style="display:flex;align-items:center;gap:10px;margin-bottom:${SHOW_PANEL_OPEN?'12px':'0'}">
      <b style="font-size:13px">🔧 筛选条件</b>
      <span style="font-size:12px;color:var(--sub)">已设置 ${advCount + (F.status?1:0) + (F.npcId?1:0) + (String(F.kw||'').trim()?1:0)} 项</span>
      <span style="flex:1"></span>
      <button class="btn-ghost" onclick="SHOW_PANEL_OPEN=!SHOW_PANEL_OPEN;renderShow()">
        ${SHOW_PANEL_OPEN?'收起 ▲':'展开 ▼'}
      </button>
    </div>
    ${SHOW_PANEL_OPEN ? `
    <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:10px">
      <div class="field"><label>城市</label>
        <select id="sf_city" onchange="showFilterInput('city',this.value)">
          <option value="">全部城市</option>
          ${cities.map(c=>`<option ${c===F.city?'selected':''}>${esc(c)}</option>`).join('')}
        </select></div>
      <div class="field"><label>开演日期（起）</label>
        <input id="sf_dateFrom" type="date" value="${esc(F.dateFrom)}" onchange="showFilterInput('dateFrom',this.value)"></div>
      <div class="field"><label>开演日期（止）</label>
        <input id="sf_dateTo" type="date" value="${esc(F.dateTo)}" onchange="showFilterInput('dateTo',this.value)"></div>
      <div class="field"><label>最低票价 ¥</label>
        <input id="sf_priceMin" type="number" min="0" value="${esc(F.priceMin)}" placeholder="不限"
               oninput="showFilterInput('priceMin',this.value)"></div>
      <div class="field"><label>最高票价 ¥</label>
        <input id="sf_priceMax" type="number" min="0" value="${esc(F.priceMax)}" placeholder="不限"
               oninput="showFilterInput('priceMax',this.value)"></div>
    </div>
    <div style="margin-top:10px;display:flex;gap:8px;align-items:center;flex-wrap:wrap">
      <button class="btn-ghost" onclick="quickShowRange(7)">近 7 天</button>
      <button class="btn-ghost" onclick="quickShowRange(30)">近 30 天</button>
      <button class="btn-ghost" onclick="quickShowRange(0)">清空日期</button>
      <span style="font-size:12px;color:var(--sub)">日期区间按「开演日期」过滤</span>
    </div>` : ''}
  </div>`;
  }
  /* 快捷日期：0 = 清空 */
  function quickShowRange(days){
    if (!days){ SHOW_FILTER.dateFrom=''; SHOW_FILTER.dateTo=''; }
    else{
      const t = new Date();
      const fmt = d => d.toISOString().slice(0,10);
      SHOW_FILTER.dateFrom = fmt(t);
      const to = new Date(t.getTime() + days*86400000);
      SHOW_FILTER.dateTo = fmt(to);
    }
    fetchShows().then(renderShow);
  }

  /* ---------- 场次统计与视图 ---------- */
  function showStats(list){
    const st = { total:0, onSale:0, pre:0, full:0, ended:0, seats:0, sold:0, revenue:0, soon:0 };
    const today = todayStr();
    (list||[]).forEach(sh=>{
      const cap = +sh.capacity || 0, taken = +sh.taken || 0;
      st.total++;
      st.seats += cap; st.sold += taken;
      st.revenue += taken * (num(sh.price) || 0);
      const s = String(sh.status||'');
      if (s === '在售') st.onSale++;
      else if (s === '预售') st.pre++;
      else if (s === '满员') st.full++;
      else if (s === '已结束' || s === '取消') st.ended++;
      if (sh.date && sh.date >= today && sh.date <= addDays(today, 7) && taken < cap) st.soon++;
    });
    st.rate = st.seats > 0 ? st.sold * 100 / st.seats : 0;
    st.left = st.seats - st.sold;
    return st;
  }
  /* ---------- 日历工具 ---------- */
  function monthOf(dateStr){
    const d = String(dateStr||'');
    return /^\d{4}-\d{2}/.test(d) ? d.slice(0,7) : '';
  }
  function monthAdd(m, delta){
    const y = +String(m).slice(0,4), mo = +String(m).slice(5,7) - 1 + delta;
    const d = new Date(y, mo, 1);
    const p = x => String(x).padStart(2,'0');
    return `${d.getFullYear()}-${p(d.getMonth()+1)}`;
  }
  function daysInMonth(m){
    if (!/^\d{4}-\d{2}$/.test(String(m))) return 31;
    const y = +m.slice(0,4), mo = +m.slice(5,7);
    return new Date(y, mo, 0).getDate();
  }
  /* 周一为一周起点 */
  function weekStartOf(dateStr){
    const base = /^\d{4}-\d{2}-\d{2}$/.test(String(dateStr)) ? dateStr : todayStr();
    const d = new Date(base + 'T00:00:00');
    if (isNaN(d.getTime())) return todayStr();
    const wd = d.getDay();              /* 0=周日 */
    return addDays(base, wd === 0 ? -6 : 1 - wd);
  }
  const WD_CN = ['一','二','三','四','五','六','日'];
  /* 把 "2026-09-12" 变成 { y, m, d } 数字，用于日历格子 */
  function dateParts(ds){
    const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(String(ds||''));
    return m ? { y:+m[1], m:+m[2], d:+m[3] } : null;
  }
  function addDays(dateStr, n){
    const d = new Date(dateStr + 'T00:00:00');
    if (isNaN(d.getTime())) return dateStr;
    d.setDate(d.getDate() + n);
    const p = x => String(x).padStart(2,'0');
    return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}`;
  }
  /* 快捷筛选：互斥场景标签 */
  function matchShowQuick(sh){
    const q = SHOW_QUICK; if (!q) return true;
    const cap = +sh.capacity || 0, taken = +sh.taken || 0;
    const today = todayStr();
    const d = String(sh.date||'');
    switch(q){
      case 'soon':    return d && d >= today && d <= addDays(today,7) && String(sh.status)!=='已结束';
      case 'unsold':  return String(sh.status)==='预售';
      case 'full':    return String(sh.status)==='满员' || (cap>0 && taken>=cap);
      case 'hasSeat': return taken < cap && String(sh.status)!=='已结束';
      case 'soldout': return cap>0 && taken>=cap;
      case 'empty':   return taken === 0;
      case 'week':    return d && d >= today && d <= addDays(today,7);
      default:        return true;
    }
  }
  function showSorted(list){
    const arr = (list||[]).slice();
    const seatOf = sh => (+sh.capacity||0) > 0 ? (+sh.taken||0)/(+sh.capacity) : 0;
    const priceOf = sh => num(sh.price) || 0;
    arr.sort((a,b)=>{
      switch(SHOW_SORT){
        case 'seat':   return seatOf(b)  - seatOf(a);
        case 'price':  return priceOf(b) - priceOf(a);
        case 'sale':   return (+b.taken||0) - (+a.taken||0);
        case 'date':
        default: {
          const da = String(a.date||''), db2 = String(b.date||'');
          if (da !== db2) return da < db2 ? -1 : 1;
          return String(a.time||'') < String(b.time||'') ? -1 : 1;
        }
      }
    });
    return arr;
  }
  function showGroups(list){
    const map = new Map();
    (list||[]).forEach(sh=>{
      let k = '';
      if (SHOW_VIEW === 'city')        k = String(sh.city||'未填城市');
      else if (SHOW_VIEW === 'status') k = String(sh.status||'未填状态');
      else if (SHOW_VIEW === 'date')   k = String(sh.date||'未填日期');
      if (!map.has(k)) map.set(k, { key:k, label:k, list:[] });
      map.get(k).list.push(sh);
    });
    return [...map.values()];
  }

  /* ==================== 月历 / 周历 ==================== */

  /* 按日期归组：{ 'YYYY-MM-DD': [show,...] } */
  function showsByDate(list){
    const map = {};
    (list||[]).forEach(sh=>{
      const d = String(sh.date||'').slice(0,10);
      if (!/^\d{4}-\d{2}-\d{2}$/.test(d)) return;
      (map[d] = map[d] || []).push(sh);
    });
    Object.keys(map).forEach(k=>{
      map[k].sort((a,b)=> String(a.time||'') < String(b.time||'') ? -1 : 1);
    });
    return map;
  }

  /* 当前应显示的月份：优先用户翻到的月份 → 选中日 → 首个场次 → 今天 */
  function calMonth(list){
    if (/^\d{4}-\d{2}$/.test(SHOW_CAL_M)) return SHOW_CAL_M;
    if (SHOW_CAL_D) return monthOf(SHOW_CAL_D);
    const first = (list||[]).map(x=>String(x.date||'')).filter(d=>/^\d{4}-\d{2}-\d{2}$/.test(d)).sort()[0];
    return first ? monthOf(first) : monthOf(todayStr());
  }

  function statusDotCls(sh){
    const st = String(sh.status||'');
    if (st === '满员') return 'full';
    if (st === '预售') return 'pre';
    if (st === '在售') return 'on';
    if (st === '已结束') return 'end';
    return 'other';
  }

  /* 月历主体 */
  function showMonthHtml(list){
    const m   = calMonth(list);
    const map = showsByDate(list);
    const today = todayStr();
    const sel   = SHOW_CAL_D || today;

    /* 6 行 × 7 列。周一为一周起点 */
    const first = new Date(m + '-01T00:00:00');
    const lead  = first.getDay() === 0 ? 6 : first.getDay() - 1;   /* 前面要补几格 */
    const total = daysInMonth(m);
    const y = +m.slice(0,4), mo = +m.slice(5,7);
    const p = x => String(x).padStart(2,'0');

    let cells = '';
    for (let i = 0; i < lead; i++){
      cells += `<div class="sc-cell out"><span class="sc-dnum"></span></div>`;
    }
    for (let d = 1; d <= total; d++){
      const key = `${y}-${p(mo)}-${p(d)}`;
      const arr = map[key] || [];
      const isToday = key === today;
      const isSel   = key === sel;
      const wd      = new Date(key + 'T00:00:00').getDay();   /* 0=日 */
      const weekend = wd === 0 || wd === 6;
      const showN   = 3;
      const items = arr.slice(0, showN).map(sh=>{
        const cap = +sh.capacity||0, tk = +sh.taken||0;
        const rate = cap>0 ? Math.round(tk/cap*100) : 0;
        return `<div class="sc-ev ${statusDotCls(sh)}" title="${esc((sh.time||'')+' '+(sh.title||''))}"
        onclick="event.stopPropagation();openShowForm('${esc(sh.id)}')">
        <i class="sc-dot"></i>
        <span class="sc-ev-t">${esc(sh.time||'')}</span>
        <span class="sc-ev-n">${esc(sh.title||'未命名')}</span>
        <span class="sc-ev-s">${tk}/${cap} ${rate}%</span>
      </div>`;
      }).join('');
      const more = arr.length > showN ? `<div class="sc-more">+${arr.length - showN} 场</div>` : '';
      cells += `<div class="sc-cell ${isToday?'today':''} ${isSel?'sel':''} ${weekend?'we':''}"
        onclick="pickCalDay('${key}')">
      <span class="sc-dnum">${d}${isToday?'<em>今天</em>':''}</span>
      <div class="sc-evs">${items}${more}</div>
    </div>`;
    }
    /* 尾部补齐到整行 */
    const used = lead + total;
    const tail = (7 - used % 7) % 7;
    for (let i = 0; i < tail; i++){
      cells += `<div class="sc-cell out"><span class="sc-dnum"></span></div>`;
    }

    /* 当月小结 */
    const monthList = (list||[]).filter(sh=>monthOf(String(sh.date||'')) === m);
    const seats = monthList.reduce((a,x)=>a+(+x.capacity||0),0);
    const sold  = monthList.reduce((a,x)=>a+(+x.taken||0),0);

    const head = `
    <div class="sc-cal-head">
      <button class="btn-ghost" onclick="calGo(-1)">‹ 上月</button>
      <b>${y} 年 ${mo} 月</b>
      <button class="btn-ghost" onclick="calGo(1)">下月 ›</button>
      <button class="btn-ghost" onclick="calToday()">回到今天</button>
      <span class="sc-cal-sum">本月 ${monthList.length} 场 · 售 ${sold}/${seats}</span>
      <span style="flex:1"></span>
      <button class="btn-primary" onclick="openShowForm('', '${sel}')">＋ 在 ${sel.slice(5)} 新增</button>
    </div>
    <div class="sc-wd-bar">${WD_CN.map(w=>`<div class="sc-wd">${w}</div>`).join('')}</div>`;

    return `<div class="sc-cal">${head}<div class="sc-cal-grid">${cells}</div></div>`
        + showDayPanelHtml(map, sel);
  }

  /* 选中日的详情面板 */
  function showDayPanelHtml(map, day){
    const arr = (map[day] || []);
    const wd  = WD_CN[(new Date(day + 'T00:00:00').getDay() + 6) % 7];
    if (!arr.length){
      return `<div class="sc-day">
      <div class="sc-day-h"><b>${day}</b><span>周${wd}</span><span class="sc-day-n">无场次</span>
        <span style="flex:1"></span>
        <button class="btn-primary" onclick="openShowForm('', '${day}')">＋ 新增场次</button>
      </div>
      <div class="empty" style="padding:26px;text-align:center">这一天没有场次</div>
    </div>`;
    }
    const seats = arr.reduce((a,x)=>a+(+x.capacity||0),0);
    const sold  = arr.reduce((a,x)=>a+(+x.taken||0),0);
    const rate  = seats>0 ? Math.round(sold/seats*100) : 0;
    const rev   = arr.reduce((a,x)=>a+(+x.taken||0)*(num(x.price)||0),0);
    const rows = arr.map(sh=>{
      const npc = DB.npcById(sh.npc);
      const cap = +sh.capacity||0, tk = +sh.taken||0;
      const r = cap>0 ? Math.round(tk/cap*100) : 0;
      const si = saleStartInfo(sh);
      return `<div class="sc-day-card ${statusDotCls(sh)}">
      <div class="sdc-l">
        <div class="sdc-t">${esc(sh.title||'未命名场次')}</div>
        <div class="sdc-m">
          <code>${esc(sh.id)}</code>
          <span>${esc(sh.time||'')}</span>
          ${npc?`<span>${esc(npc.icon||'')} ${esc(npc.name)}</span>`:''}
          <span>${esc(sh.city||'')} ${esc(sh.venue||'')}</span>
        </div>
      </div>
      <div class="sdc-c">
        <div class="s-seat ${r>=100?'full':(r>=70?'high':(r===0?'zero':''))}">
          <div class="s-seat-bar"><i style="width:${Math.min(100,r)}%"></i></div>
          <div class="s-seat-num"><b>${tk}</b><span>/${cap}</span><em>${r}%</em></div>
        </div>
      </div>
      <div class="sdc-r">
        ${money(sh.price)}
        ${statusBadge(sh.status)}
        ${si?`<div class="s-sale ${si.started?'on':''}">⏰ ${esc(si.started?'已开抢':si.text)}</div>`:''}
      </div>
      <div class="sdc-a">
        <button class="btn-ghost" onclick="openShowForm('${esc(sh.id)}')">编辑</button>
        <button class="btn-danger" onclick="delShow('${esc(sh.id)}')">删除</button>
      </div>
    </div>`;
    }).join('');
    return `<div class="sc-day">
    <div class="sc-day-h">
      <b>${day}</b><span>周${wd}</span>
      <span class="sc-day-n">${arr.length} 场 · 售 ${sold}/${seats} (${rate}%) · ${money(rev)}</span>
      <span style="flex:1"></span>
      <button class="btn-primary" onclick="openShowForm('', '${day}')">＋ 新增场次</button>
    </div>
    ${rows}
  </div>`;
  }

  /* 周历主体 */
  function showWeekHtml(list){
    const start = /^\d{4}-\d{2}-\d{2}$/.test(SHOW_WEEK_S) ? SHOW_WEEK_S : weekStartOf(SHOW_CAL_D || todayStr());
    const map   = showsByDate(list);
    const today = todayStr();
    const days  = [];
    for (let i = 0; i < 7; i++) days.push(addDays(start, i));
    const p = x => String(x).padStart(2,'0');

    const cols = days.map(key=>{
      const arr = map[key] || [];
      const d   = new Date(key + 'T00:00:00');
      const isToday = key === today;
      const wd = WD_CN[(d.getDay() + 6) % 7];
      const cards = arr.map(sh=>{
        const cap=+sh.capacity||0, tk=+sh.taken||0;
        const r = cap>0?Math.round(tk/cap*100):0;
        return `<div class="sc-ev ${statusDotCls(sh)}" onclick="openShowForm('${esc(sh.id)}')">
        <span class="sc-ev-t">${esc(sh.time||'')}</span>
        <span class="sc-ev-n">${esc(sh.title||'未命名')}</span>
        <span class="sc-ev-s">${tk}/${cap} · ${esc(sh.city||'')}</span>
      </div>`;
      }).join('') || `<div class="sc-wk-empty">—</div>`;
      return `<div class="sc-wk-col ${isToday?'today':''}" ondblclick="openShowForm('', '${key}')">
      <div class="sc-wk-h"><b>${d.getMonth()+1}/${d.getDate()}</b><span>周${wd}</span>
        ${arr.length?`<em>${arr.length}场</em>`:''}</div>
      <div class="sc-wk-body">${cards}</div>
    </div>`;
    }).join('');

    const end = days[6];
    return `
    <div class="sc-cal-head">
      <button class="btn-ghost" onclick="weekGo(-7)">‹ 上周</button>
      <b>${start} ~ ${end}</b>
      <button class="btn-ghost" onclick="weekGo(7)">下周 ›</button>
      <button class="btn-ghost" onclick="weekThis()">本周</button>
      <span style="flex:1"></span>
      <span class="sc-cal-sum">双击空白处可在该日新增</span>
    </div>
    <div class="sc-wk">${cols}</div>`;
  }

  /* ---- 日历交互 ---- */
  function setShowMode(m){
    SHOW_MODE = m;
    if (m === 'month' && !SHOW_CAL_D) SHOW_CAL_D = todayStr();
    if (m === 'week'  && !SHOW_WEEK_S) SHOW_WEEK_S = weekStartOf(SHOW_CAL_D || todayStr());
    renderShow();
  }
  async function calGo(delta){
    SHOW_CAL_M = monthAdd(calMonth(DB.SHOWS), delta);
    await renderShow();
  }
  async function calToday(){
    const t = todayStr();
    SHOW_CAL_M = monthOf(t); SHOW_CAL_D = t;
    await renderShow();
  }
  async function pickCalDay(day){
    SHOW_CAL_D = day;
    await renderShow();
  }
  async function weekGo(delta){
    const base = /^\d{4}-\d{2}-\d{2}$/.test(SHOW_WEEK_S) ? SHOW_WEEK_S : weekStartOf(todayStr());
    SHOW_WEEK_S = addDays(base, delta);
    await renderShow();
  }
  async function weekThis(){
    SHOW_WEEK_S = weekStartOf(todayStr());
    await renderShow();
  }
  /* 新增/编辑保存后跳到日历并定位到该场次当天 */
  function focusShowOnCal(dateStr){
    const d = String(dateStr||'').slice(0,10);
    if (!/^\d{4}-\d{2}-\d{2}$/.test(d)) return;
    SHOW_MODE  = 'month';
    SHOW_CAL_M = monthOf(d);
    SHOW_CAL_D = d;
    SHOW_WEEK_S = weekStartOf(d);
  }

  async function renderShow(){
    const all = [...DB.SHOWS].filter(sh => matchShow(sh) && matchShowQuick(sh));
    const list = showSorted(all);
    const st   = showStats(list);

    /* ========== ① 统计概览 ========== */
    const kpis = `
    <div class="sk-grid">
      <div class="sk-card"><span>总场次</span><b>${st.total}</b><i>筛选结果</i></div>
      <div class="sk-card ok"><span>在售</span><b>${st.onSale}</b><i>可直接购买</i></div>
      <div class="sk-card warn"><span>预售</span><b>${st.pre}</b><i>待开抢</i></div>
      <div class="sk-card bad"><span>满员</span><b>${st.full}</b><i>已售完</i></div>
      <div class="sk-card"><span>总座位</span><b>${st.seats}</b><i>余 ${st.left} 位</i></div>
      <div class="sk-card"><span>已售</span><b>${st.sold}</b><i>上座 ${st.rate.toFixed(1)}%</i></div>
      <div class="sk-card hi"><span>营收</span><b>${money(st.revenue)}</b><i>已售票数 × 单价</i></div>
      <div class="sk-card ${st.soon>0?'warn':''}"><span>7天内待推</span><b>${st.soon}</b><i>${st.soon>0?'有余位，可加推':'无'}</i></div>
    </div>`;

    /* ========== ② 快捷筛选（互斥场景标签） ========== */
    const QUICKS = [
      ['','全部'],['soon','🔥 7天内'],['unsold','待开抢'],['hasSeat','有余位'],
      ['soldout','已售完'],['empty','零销量'],['full','满员']
    ];
    const quicks = `<div class="sq-bar">
    ${QUICKS.map(([k,label])=>`<button class="sq ${SHOW_QUICK===k?'on':''}" onclick="setShowQuick('${k}')">${label}</button>`).join('')}
  </div>`;

    /* ========== ③ 行渲染（精简：去掉编号列，信息单行化） ========== */
    const rowHtml = sh => {
      const npc  = DB.npcById(sh.npc);
      const cap  = +sh.capacity || 0, taken = +sh.taken || 0;
      const rate = cap > 0 ? Math.round(taken / cap * 100) : 0;
      const si   = saleStartInfo(sh);
      const seatCls = rate >= 100 ? 'full' : (rate >= 70 ? 'high' : (rate === 0 ? 'zero' : ''));
      return `<tr>
      <td class="s-cell-title">
        <div class="s-title">${esc(sh.title||'未命名场次')}</div>
        <div class="s-meta">
          <code>${esc(sh.id)}</code>
          ${npc?`<span class="s-npc">${esc(npc.icon||'')} ${esc(npc.name)}</span>`:''}
        </div>
      </td>
      <td class="s-cell-time">
        <div class="s-date">${esc(sh.date||'—')}</div>
        <div class="s-meta">${esc(sh.time||'')}</div>
      </td>
      <td class="s-cell-place">
        <div class="s-city">${esc(sh.city||'—')}</div>
        <div class="s-meta">${esc(sh.venue||'')}</div>
      </td>
      <td class="s-cell-price"><b>${money(sh.price)}</b></td>
      <td class="s-cell-seat">
        <div class="s-seat ${seatCls}">
          <div class="s-seat-bar"><i style="width:${Math.min(100,rate)}%"></i></div>
          <div class="s-seat-num"><b>${taken}</b><span>/${cap}</span><em>${rate}%</em></div>
        </div>
      </td>
      <td class="s-cell-status">
        ${statusBadge(sh.status)}
        ${si?`<div class="s-sale ${si.started?'on':''}">⏰ ${esc(si.started?'已开抢':si.text)}</div>`:''}
        ${rate===0&&String(sh.status)!=='已结束'?`<div class="s-sale warn">⚠ 零销量</div>`:''}
      </td>
      <td class="row-actions">
        <button class="btn-ghost" onclick="openShowForm('${esc(sh.id)}')">编辑</button>
        <button class="btn-danger" onclick="delShow('${esc(sh.id)}')">删除</button>
      </td>
    </tr>`;
    };

    /* ========== ④ 主体：列表 or 分组 ========== */
    let bodyHtml;
    const emptyMsg = (DB.SHOWS.length === 0)
        ? '数据库中暂无场次数据'
        : '没有符合条件的场次，试试放宽筛选条件';
    if (!list.length){
      bodyHtml = `<div class="empty" style="padding:40px;text-align:center">${emptyMsg}</div>`;
    }else if (SHOW_MODE === 'month'){
      bodyHtml = showMonthHtml(list);
    }else if (SHOW_MODE === 'week'){
      bodyHtml = showWeekHtml(list);
    }else if (SHOW_VIEW === 'list'){
      bodyHtml = `<table class="s-table"><thead><tr>
        <th>场次</th><th>开演</th><th>城市 / 场馆</th><th>票价</th><th>上座</th><th>状态</th><th>操作</th>
      </tr></thead><tbody>${list.map(rowHtml).join('')}</tbody></table>`;
    }else{
      const gs = showGroups(list);
      bodyHtml = gs.map(g=>`
      <div class="sg-block">
        <div class="sg-head">
          <b>${esc(g.label || '全部')}</b>
          <span>${g.list.length} 场</span>
          <span class="sg-sep"></span>
          <span>售 ${g.list.reduce((a,x)=>a+(+x.taken||0),0)} / ${g.list.reduce((a,x)=>a+(+x.capacity||0),0)}</span>
        </div>
        <table class="s-table"><thead><tr>
          <th>场次</th><th>开演</th><th>城市 / 场馆</th><th>票价</th><th>上座</th><th>状态</th><th>操作</th>
        </tr></thead><tbody>${g.list.map(rowHtml).join('')}</tbody></table>
      </div>`).join('');
    }

    /* ========== ⑤ 工具栏 ========== */
    const F = SHOW_FILTER;
    const filtering = !!(String(F.kw).trim() || F.status || F.npcId || F.city
        || F.dateFrom || F.dateTo || (F.priceMin!==''&&F.priceMin!=null)
        || (F.priceMax!==''&&F.priceMax!=null) || SHOW_QUICK);
    const tip = filtering
        ? `筛选出 <b style="color:var(--txt)">${list.length}</b> / ${DB.SHOWS.length} 个场次`
        : `共 <b style="color:var(--txt)">${DB.SHOWS.length}</b> 个场次`;

    const VIEWS = [['list','列表'],['city','按城市'],['status','按状态'],['date','按日期']];
    const SORTS = [['date','开演时间'],['seat','上座率'],['sale','销量'],['price','票价']];
    const MODES = [['list','☰ 列表'],['month','📅 月历'],['week','🗓 周历']];

    const toolbar = `
    <input id="showKw" placeholder="🔍 搜索名称 / 城市 / 场馆" value="${esc(F.kw)}">
    <select id="showStatus">
      <option value="">全部状态</option>
      ${SHOW_STATUS.map(x=>`<option ${x===F.status?'selected':''}>${esc(x)}</option>`).join('')}
    </select>
    <select id="showNpc">
      <option value="">全部角色</option>
      ${DB.NPCS.map(n=>`<option value="${esc(n.id)}" ${String(n.id)===String(F.npcId)?'selected':''}>${esc(n.icon+' '+n.name)}</option>`).join('')}
    </select>
    ${filtering?`<button class="btn-ghost" onclick="resetShowFilter()">清除筛选</button>`:''}
    <span style="align-self:center;font-size:12px;color:var(--sub)">${tip}</span>
    <span style="flex:1"></span>
    <div class="sv-bar">
      ${MODES.map(([k,l])=>`<button class="sv ${SHOW_MODE===k?'on':''}" onclick="setShowMode('${k}')">${l}</button>`).join('')}
    </div>
    ${SHOW_MODE==='list'?`<div class="sv-bar">
      ${VIEWS.map(([k,l])=>`<button class="sv sm ${SHOW_VIEW===k?'on':''}" onclick="setShowView('${k}')">${l}</button>`).join('')}
    </div>
    <select id="showSort" class="s-sort" title="排序方式">
      ${SORTS.map(([k,l])=>`<option value="${k}" ${SHOW_SORT===k?'selected':''}>${l} ↓</option>`).join('')}
    </select>`:''}
    <button class="btn-primary" onclick="openShowForm()">＋ 新增场次</button>`;

    $('#view').innerHTML = pageShell('场次管理','对应小程序「发车/预约」模块 · 数据来自 /shows',
        toolbar,
        kpis + quicks + showFilterPanel() + bodyHtml
    );

    /* ========== ⑥ 事件绑定 ========== */
    const kwEl=$('#showKw');
    if (kwEl && kwEl.addEventListener){
      kwEl.oninput=e=>{
        SHOW_FILTER.kw = e.target.value;
        clearTimeout(SHOW_SEARCH_TIMER);
        SHOW_SEARCH_TIMER = setTimeout(async ()=>{
          await fetchShows(); await renderShow();
          const el=$('#showKw');
          if (el && el.focus){ el.focus(); try{ el.setSelectionRange(el.value.length, el.value.length); }catch(_e){} }
        }, 300);
      };
      kwEl.onkeydown=e=>{ if(e.key==='Enter'){ clearTimeout(SHOW_SEARCH_TIMER); fetchShows().then(renderShow); } };
    }
    const stt=$('#showStatus');
    if (stt && stt.addEventListener){
      stt.onchange=async e=>{ SHOW_FILTER.status = e.target.value; await fetchShows(); await renderShow(); };
    }
    const np=$('#showNpc');
    if (np && np.addEventListener){
      np.onchange=async e=>{ SHOW_FILTER.npcId = e.target.value; await fetchShows(); await renderShow(); };
    }
    const so=$('#showSort');
    if (so && so.addEventListener){
      so.onchange=async e=>{ SHOW_SORT = e.target.value; await renderShow(); };
    }
  }

  /* 快捷筛选 / 视图切换 */
  async function setShowQuick(k){
    SHOW_QUICK = (SHOW_QUICK === k) ? '' : k;
    await renderShow();
  }
  async function setShowView(k){
    SHOW_VIEW = k;
    await renderShow();
  }

  function showForm(sh){
    return `
    <div class="field"><label>场次名称 *</label><input id="s_title" value="${esc(sh?sh.title:'灰塔之下 · ')}"></div>
    <div class="field-row">
      <div class="field"><label>关联 NPC</label><select id="s_npc">${DB.NPCS.map(n=>`<option value="${n.id}">${esc(n.icon+' '+n.name)}</option>`).join('')}</select></div>
      <div class="field"><label>价格 (¥)</label><input id="s_price" type="number" value="${sh?sh.price:328}"></div>
    </div>
    <div class="field-row">
      <div class="field"><label>日期</label><input id="s_date" type="date" value="${sh?sh.date:(SHOW_PREFILL_DATE||todayStr())}"></div>
      <div class="field"><label>时间</label><input id="s_time" value="${esc(sh?sh.time:'19:00-22:00')}"></div>
    </div>
    <div class="field-row">
      <div class="field"><label>城市</label><input id="s_city" value="${esc(sh?sh.city:'北京')}"></div>
      <div class="field"><label>场馆</label><input id="s_venue" value="${esc(sh?sh.venue:'角渡沉浸剧场')}"></div>
    </div>
    <div class="field-row">
      <div class="field"><label>容量</label><input id="s_capacity" type="number" value="${sh?sh.capacity:24}"></div>
      <div class="field"><label>已订</label><input id="s_taken" type="number" value="${sh?sh.taken:0}"></div>
    </div>
    <div class="field-row">
      <div class="field"><label>状态</label>
        <select id="s_status" onchange="toggleSaleStart()">
          <option>在售</option><option>预售</option><option>满员</option>
          <option>已结束</option><option>取消</option>
        </select></div>
      <div class="field" id="s_saleWrap" style="display:none">
        <label>开抢时间（小程序抢车用）</label>
        <input id="s_saleStartAt" type="datetime-local" value="${esc(toDatetimeLocal(showSaleStart(sh)))}">
        <span style="font-size:11px;color:var(--sub)">仅「预售」需要填；到点后小程序才开放抢购</span>
      </div>
    </div>`;
  }
  /* 只有「预售」才显示开抢时间，切换状态时同步显隐 */
  function toggleSaleStart(){
    const st = $('#s_status'), wrap = $('#s_saleWrap');
    if (!st || !wrap) return;
    const on = (st.value === '预售');
    wrap.style.display = on ? '' : 'none';
    /* 非预售时清空，避免把开抢时间带到「在售」场次上 */
    if (!on){ const el = $('#s_saleStartAt'); if (el) el.value = ''; }
  }
  /* 表单当前值的快照 —— 用来判断"用户有没有动过表单" */
  const SHOW_FORM_FIELDS = ['title','npc','price','date','time','city','venue','capacity','taken','status','saleStartAt'];
  let SHOW_FORM_SNAPSHOT = null;
  function snapshotShowForm(){
    const o = {};
    SHOW_FORM_FIELDS.forEach(k=>{ const el = $('#s_'+k); if (el) o[k] = String(el.value || ''); });
    return o;
  }
  function showFormUnchanged(snap){
    if (!snap) return true;
    return SHOW_FORM_FIELDS.every(k=>{
      const el = $('#s_'+k);
      return !el || String(el.value || '') === String(snap[k] || '');
    });
  }

  /* 渲染（新增/编辑）弹窗；afterRender 用于填充 select 等控件 */
  function renderShowModal(sh){
    openModal(`<div class="modal-head"><h2>${sh?'编辑场次':'新增场次'}</h2><button class="modal-close" onclick="tryCloseModal()">×</button></div>${showForm(sh)}
    <div class="modal-foot"><button class="btn-ghost" onclick="tryCloseModal()">取消</button><button class="btn-primary" onclick="saveShow('${sh?sh.id:''}')">保存</button></div>`, true);
    setTimeout(()=>{
      const s=$('#s_npc'); if(s&&sh) s.value=sh.npc;
      const st=$('#s_status'); if(st&&sh) st.value=sh.status;
      toggleSaleStart();
      SHOW_FORM_SNAPSHOT = snapshotShowForm();   // 记录本次渲染的值
    },0);
  }

  /* 打开表单：先用本地数据秒开，再向后端要详情覆盖（保证拿到最新值） */
  async function openShowForm(id, presetDate){
    /* presetDate：从日历某天点「新增」进来时预填开演日期，省一次输入 */
    if (!id && presetDate && /^\d{4}-\d{2}-\d{2}$/.test(String(presetDate))){
      SHOW_PREFILL_DATE = String(presetDate).slice(0,10);
    } else {
      SHOW_PREFILL_DATE = '';
    }
    const local = id ? DB.showById(id) : null;
    /* 本地没有（如刚筛选过、DB 被替换）→ 先给一个空壳，拉到数据再填 */
    renderShowModal(local || (id ? { id:id } : null));

    if (!id || !useApi()) return;
    try{
      const fresh = await API.shows.get(id);
      if (!fresh) return;
      if (showFormUnchanged(SHOW_FORM_SNAPSHOT)){
        DB.SHOWS = DB.SHOWS.map(x => String(x.id)===String(id) ? Object.assign({}, x, fresh) : x);
        renderShowModal(fresh);                     // 用户没动 → 用后端数据刷新
      }else{
        toast('已获取最新数据，但保留你正在编辑的内容','warn');
      }
    }catch(e){
      console.warn('[show] 拉取详情失败，使用列表数据', e && e.message);
    }
  }
  async function saveShow(id){
    const get=v=>$('#s_'+v).value.trim();
    const status=$('#s_status').value;
    /* 开抢时间：仅「预售」生效；其他状态一律置空，避免脏数据 */
    let saleStartAt = '';
    if (status === '预售'){
      const el = $('#s_saleStartAt');
      if (el) saleStartAt = String(el.value || '').trim();
      /* datetime-local 给的是 '2026-09-12T19:00'，后端 LocalDateTime 两种都能解析，这里补上秒更稳 */
      if (saleStartAt && saleStartAt.length === 16) saleStartAt += ':00';
    }
    const d={ title:get('title'), npc:$('#s_npc').value, price:+get('price')||0, date:get('date'), time:get('time'), city:get('city'), venue:get('venue'), capacity:+get('capacity')||0, taken:+get('taken')||0, status:status, saleStartAt:saleStartAt, id:'' };
    if(!d.title){toast('请填写场次名称','err');return;}
    if(status==='预售' && !saleStartAt){ toast('预售场次必须填写开抢时间','err'); return; }
    if(useApi()){
      const ok=await withApi('保存', async ()=>{
        if(id){ d.id=id; await API.shows.update(id,d); }
        else  { d.id=HT.nextId('SHOWS','S'); await API.shows.create(d); }
        await resync('shows');
      });
      if(!ok) return;
      toast(id?'已更新':'已新增');
    }else{
      if(id){ d.id=id; HT.updateItem('SHOWS',id,d); toast('已更新'); }
      else{ d.id=HT.nextId('SHOWS','S'); HT.addItem('SHOWS',d); toast('已新增'); }
    }
    /* ★ 保存后自动进入月历并定位到该场次当天 */
    focusShowOnCal(d.date);
    closeModal(); renderShow();
  }
  async function delShow(id){
    if(!confirm('确定删除该场次？'))return;
    if(useApi()){
      const ok=await withApi('删除', async ()=>{ await API.shows.remove(id); await resync('shows'); });
      if(!ok) return;
    }else{
      HT.deleteItem('SHOWS',id);
    }
    toast('已删除'); renderShow();
  }

  /* ==================== 公告管理 ==================== */
  function renderNotice(){
    const rows=DB.NOTICES.map(n=>`<tr>
    <td><b>${esc(n.id)}</b>${n.top?' <span style="color:var(--warn)">🔝</span>':''}</td>
    <td><b>${esc(n.title)}</b><br><span style="color:var(--sub);font-size:11px">${esc(n.content)}</span></td>
    <td><span class="tag">${esc(n.cat)}</span></td>
    <td>${esc(n.author)}<br><span style="color:var(--sub);font-size:11px">${esc(n.created)}</span></td>
    <td>${statusBadge(n.status)}</td>
    <td class="row-actions"><button class="btn-ghost" onclick="openNoticeForm('${n.id}')">编辑</button><button class="btn-danger" onclick="delNotice('${n.id}')">删除</button></td>
  </tr>`).join('');
    $('#view').innerHTML=pageShell('公告管理','对应小程序「公告」模块',
        `<button class="btn-primary" onclick="openNoticeForm()">＋ 发布公告</button>`,
        `<table><thead><tr><th>编号</th><th>标题</th><th>分类</th><th>发布人/日期</th><th>状态</th><th>操作</th></tr></thead><tbody>${rows||`<tr><td colspan="6">${emptyRow('数据库中暂无公告数据')}</td></tr>`}</tbody></table>`
    );
  }
  function noticeForm(n){
    return `
    <div class="field"><label>标题 *</label><input id="n_title" value="${esc(n?n.title:'')}"></div>
    <div class="field-row">
      <div class="field"><label>分类</label><select id="n_cat"><option>重要</option><option>活动</option><option>规则</option><option>通知</option></select></div>
      <div class="field"><label>发布人</label><input id="n_author" value="${esc(n?n.author:'磐渡戏剧工厂')}"></div>
    </div>
    <div class="field"><label>内容</label><textarea id="n_content">${esc(n?n.content:'')}</textarea></div>
    <div class="field-row">
      <div class="field"><label>状态</label><select id="n_status"><option>发布</option><option>草稿</option></select></div>
      <div class="field"><label style="display:flex;align-items:center;gap:6px"><input type="checkbox" id="n_top" ${n&&n.top?'checked':''} style="width:auto"> 置顶</label></div>
    </div>`;
  }
  function openNoticeForm(id){
    const n=id?DB.NOTICES.find(x=>x.id===id):null;
    openModal(`<div class="modal-head"><h2>${n?'编辑公告':'发布公告'}</h2><button class="modal-close" onclick="tryCloseModal()">×</button></div>${noticeForm(n)}
    <div class="modal-foot"><button class="btn-ghost" onclick="tryCloseModal()">取消</button><button class="btn-primary" onclick="saveNotice('${n?n.id:''}')">保存</button></div>`, true);
    if(n)setTimeout(()=>{const c=$('#n_cat');if(c)c.value=n.cat;const s=$('#n_status');if(s)s.value=n.status;},0);
  }
  async function saveNotice(id){
    const get=v=>$('#n_'+v).value.trim();
    const d={ title:get('title'), cat:$('#n_cat').value, author:get('author'), content:get('content'), status:$('#n_status').value, top:$('#n_top').checked, created:'', id:'' };
    if(!d.title){toast('请填写标题','err');return;}
    if(useApi()){
      const ok=await withApi('保存', async ()=>{
        if(id){ d.id=id; const o=DB.NOTICES.find(x=>x.id===id); d.created=o.created; await API.notices.update(id,d); }
        else  { d.id=HT.nextId('NOTICES','N'); d.created=new Date().toISOString().slice(0,10); await API.notices.create(d); }
        await resync('notices');
      });
      if(!ok) return;
      toast(id?'已更新':'已发布');
    }else{
      if(id){ d.id=id; const o=DB.NOTICES.find(x=>x.id===id); d.created=o.created; HT.updateItem('NOTICES',id,d); toast('已更新'); }
      else{ d.id=HT.nextId('NOTICES','N'); d.created=new Date().toISOString().slice(0,10); HT.addItem('NOTICES',d); toast('已发布'); }
    }
    closeModal(); renderNotice();
  }
  async function delNotice(id){
    if(!confirm('确定删除该公告？'))return;
    if(useApi()){
      const ok=await withApi('删除', async ()=>{ await API.notices.remove(id); await resync('notices'); });
      if(!ok) return;
    }else{
      HT.deleteItem('NOTICES',id);
    }
    toast('已删除'); renderNotice();
  }

  /* ==================== 订单管理 ==================== */
  function renderOrder(){
    const opts=(DB.STATUS_OPTIONS&&DB.STATUS_OPTIONS.length)?DB.STATUS_OPTIONS:['待付款','已支付','待核销','已核销','已退款'];
    const rows=DB.ORDERS.map(o=>{
      const sh=DB.showById(o.show);
      return `<tr>
      <td><b>${esc(o.id)}</b></td>
      <td>${esc(o.user)}</td>
      <td>${esc(o.show)}<br><span style="color:var(--sub);font-size:11px">${esc(sh?sh.title:'')}</span></td>
      <td>¥${o.amount} × ${o.qty}</td>
      <td>${esc(o.channel)}</td>
      <td>${esc(o.created)}</td>
      <td>${statusBadge(o.status)}</td>
      <td class="row-actions">
        <select onchange="changeOrderStatus('${esc(o.id)}',this.value)" style="padding:5px 8px;font-size:12px">
          ${opts.map(s=>`<option ${s===o.status?'selected':''}>${esc(s)}</option>`).join('')}
        </select>
        <button class="btn-danger" onclick="delOrder('${esc(o.id)}')">删除</button>
      </td>
    </tr>`;}).join('');
    const total=DB.ORDERS.reduce((s,o)=>s+o.amount*o.qty,0);
    $('#view').innerHTML=pageShell('订单管理','含待付款/已支付/待核销/已核销/已退款全流程',
        `<span style="color:var(--sub)">订单总额：<b style="color:var(--txt)">¥${total.toLocaleString()}</b> · 共 ${DB.ORDERS.length} 单</span>
     <button class="btn-primary" onclick="toast('演示：此处对接微信支付订单导出','ok')">导出订单</button>`,
        `<table><thead><tr><th>订单号</th><th>玩家</th><th>场次</th><th>金额</th><th>渠道</th><th>下单时间</th><th>状态</th><th>操作</th></tr></thead><tbody>${rows||`<tr><td colspan="8">${emptyRow('数据库中暂无订单数据')}</td></tr>`}</tbody></table>`
    );
  }
  async function changeOrderStatus(id,status){
    if(useApi()){
      const ok=await withApi('改状态', async ()=>{ await API.orders.updateStatus(id,status); await resync('orders'); });
      if(!ok){ renderOrder(); return; }
    }else{
      HT.updateItem('ORDERS',id,{status});
    }
    toast('订单 '+id+' → '+status);
    renderOrder();
  }
  async function delOrder(id){
    if(!confirm('确定删除订单 '+id+'？'))return;
    if(useApi()){
      const ok=await withApi('删除', async ()=>{ await API.orders.remove(id); await resync('orders'); });
      if(!ok) return;
    }else{
      HT.deleteItem('ORDERS',id);
    }
    toast('已删除'); renderOrder();
  }

  /* ==================== 玩家管理 ==================== */
  function renderPlayer(){
    const rows=DB.PLAYERS.map(p=>{
      const ip=(DB.INTIMACY.players.find(x=>x.id===p.id)||{}).npc||{};
      const total=Object.values(ip).reduce((a,b)=>a+(b||0),0);
      return `<tr>
      <td style="width:1%;white-space:nowrap;text-align:center">${avatarHtml(p.avatar,AVATAR_SIZE.table)}</td>
      <td><b>${esc(p.name)}</b><br><span style="color:var(--sub);font-size:11px">${esc(p.tag)}</span></td>
      <td>${esc(p.phone)}</td>
      <td>${esc(p.reg)}</td>
      <td>${bar(Math.round(total/6))}</td>
      <td>${statusBadge(p.vip)}</td>
      <td>${statusBadge(p.status)}</td>
    </tr>`;}).join('');
    $('#view').innerHTML=pageShell('玩家管理','数据源：小程序亲密度系统的全服玩家',
        `<input placeholder="🔍 搜索玩家" style="max-width:220px">`,
        `<table><thead><tr><th>头像</th><th>昵称</th><th>手机</th><th>注册</th><th>亲密度均值</th><th>等级</th><th>状态</th></tr></thead><tbody>${rows||`<tr><td colspan="7">${emptyRow('数据库中暂无玩家数据')}</td></tr>`}</tbody></table>`
    );
  }

  /* ==================== 亲密度管理（核心：玩家 × NPC） ==================== */
  let INT_NPC='linshen';
  function renderIntimacy(){
    // 后端数据可能与本地默认 id 不一致：自动对齐到现有 NPC，空数据时不崩
    if(!DB.NPCS.length){ $('#view').innerHTML='<div class="card"><div class="empty">暂无 NPC 数据</div></div>'; return; }
    if(!DB.npcById(INT_NPC)) INT_NPC=DB.NPCS[0].id;
    const npc=DB.npcById(INT_NPC)||DB.NPCS[0];
    const list=HT.rankByNpc(INT_NPC);
    // 全部玩家 × NPC 矩阵
    const npcIds=DB.NPCS.map(n=>n.id);
    const matrixHead=`<th>玩家</th>`+DB.NPCS.map(n=>`<th style="cursor:pointer" onclick="INT_NPC='${n.id}';renderIntimacy()">${esc(n.icon)} ${esc(n.name)}</th>`).join('');
    const matrixRows=DB.INTIMACY.players.map(p=>{
      const total=Object.values(p.npc).reduce((a,b)=>a+(b||0),0);
      return `<tr${p.id==='me'?' style="background:rgba(107,140,255,.1)"':''}>
      <td><b>${esc(p.name)}</b> ${p.id==='me'?'<span class="badge badge-ok">我</span>':''}<br><span style="color:var(--sub);font-size:11px">${esc(p.tag)}</span></td>
      ${npcIds.map(nid=>`<td style="text-align:center">
        <input type="number" min="0" max="100" value="${p.npc[nid]||0}" onchange="updateMatrix('${p.id}','${nid}',this.value)"
          style="width:60px;padding:4px;text-align:center;${p.id==='me'?'border-color:var(--accent)':''}"></td>`).join('')}
      <td><b>${total}</b></td>
    </tr>`;}).join('');

    const html=`<div class="page-head"><div><h1>亲密度管理</h1><div class="desc">玩家 × NPC 亲密度矩阵 · 修改即时生效（小程序「💞 亲密度榜」数据源）</div></div></div>`;

    // 当前选中 NPC 排行 + 我的羁绊
    let h=`<div class="grid g2" style="margin-bottom:16px">`;
    h+=`<div class="card"><div class="page-head" style="margin-bottom:12px"><h1 style="font-size:16px">「${esc(npc.name)}」的攻略者排行</h1><span class="desc">点击表头切换 NPC</span></div>
    ${list.map(p=>`<div style="display:flex;align-items:center;gap:12px;margin-bottom:12px">
      <span class="medals">${p.medal}</span>
      ${avatarHtml(p.avatar,AVATAR_SIZE.list)}
      <div style="flex:1"><div style="font-weight:600">${esc(p.name)} ${p.isMe?'<span class="badge badge-ok">我</span>':''}</div><span style="color:var(--sub);font-size:11px">${esc(p.tag)}</span></div>
      ${bar(p.value)}
    </div>`).join('')||'<div class="empty">暂无数据</div>'}
  </div>`;
    // 我的羁绊
    h+=`<div class="card"><div class="page-head" style="margin-bottom:12px"><h1 style="font-size:16px">我的羁绊</h1><span class="desc">当前玩家对 6 NPC</span></div>
    ${DB.NPCS.map(n=>{
      const v=DB.INTIMACY.mine[n.id]||0; const lv=HT.levelOf(v);
      return `<div style="display:flex;align-items:center;gap:12px;margin-bottom:12px">
        <img class="thumb" src="${esc(n.cover)}" onerror="this.style.visibility='hidden'">
        <div style="flex:1"><div style="font-weight:600">${esc(n.icon)} ${esc(n.name)}</div><span class="level-pill">Lv.${lv.lv} ${esc(lv.name)}</span></div>
        <input type="range" min="0" max="100" value="${v}" oninput="pushIntimacy('${n.id}',this.value)" style="width:120px">
        <b style="width:36px;text-align:right">${v}</b>
      </div>`;
    }).join('')}
  </div>`;
    h+=`</div>`;

    // 全服玩家 × NPC 可编辑矩阵
    h+=`<div class="card" style="padding:0;overflow:auto">
    <div class="page-head" style="margin:14px 18px 12px"><h1 style="font-size:16px">全服玩家 × NPC 亲密度矩阵</h1><span class="desc">单元格可直接编辑（0-100）</span></div>
    <table style="min-width:800px"><thead><tr>${matrixHead}<th>总和</th></tr></thead><tbody>${matrixRows}</tbody></table>
  </div>`;

    $('#view').innerHTML=h;
  }
  /* 本地先改（乐观更新），再推后端 */
  function applyIntimacyLocal(pid, nid, v){
    const p=DB.INTIMACY.players.find(x=>x.id===pid);
    if(p){ p.npc[nid]=v; if(pid==='me')DB.INTIMACY.mine[nid]=v; HT.save(); }
  }
  async function updateMatrix(pid, nid, val){
    const v=Math.max(0,Math.min(100,+val||0));
    applyIntimacyLocal(pid,nid,v);
    if(useApi()){
      const ok=await withApi('保存亲密度', ()=>API.intimacy.update(pid,nid,v));
      if(ok) SYNCED.intimacy=false;          // 标记脏，下次进入重新拉
      else { await resync('intimacy'); renderIntimacy(); return; }
    }
    renderIntimacy();
  }
  /* 「我的羁绊」滑块：本地即时生效 + 400ms 防抖后调 PUT /api/intimacy/{playerId}/{npcId} */
  let INT_TIMER=null;
  function pushIntimacy(npcId, val){
    HT.setIntimacy(npcId,val); renderIntimacy();
    if(!useApi()) return;
    clearTimeout(INT_TIMER);
    INT_TIMER=setTimeout(async ()=>{
      const v=Math.max(0,Math.min(100,+val||0));
      const meId=(DB.INTIMACY.players.find(p=>p.id==='me')||{}).id||'me';
      const ok=await withApi('保存亲密度', ()=>API.intimacy.update(meId,npcId,v));
      if(ok) SYNCED.intimacy=false;
    },400);
  }

  /* ==================== 排行榜数据 ==================== */
  function renderRank(){
    const html=`<div class="page-head"><div><h1>排行榜数据</h1><div class="desc">对应小程序「榜单」三榜：NPC 人气榜 / 玩家活跃榜 / 亲密度榜</div></div></div>`;
    let h=`<div class="tab-nav">
    <button class="on" data-t="pop">🎭 NPC 人气榜</button>
    <button data-t="active">🔥 玩家活跃榜</button>
    <button data-t="intimacy">💞 亲密度榜（总榜）</button>
    <button data-t="bynpc">💞 按 NPC 维度</button>
  </div>`;
    h+=`<div id="rankBody"></div>`;
    $('#view').innerHTML=html+h;

    function bodyPop(){
      const list=HT.npcPopularityRank();
      return table(`<tr><th>#</th><th>NPC</th><th>阵营</th><th>异能</th><th>人气值</th></tr>`,
          list.map((n,i)=>`<tr><td>${medal(i)}</td><td><img class="thumb" src="${esc(n.cover)}" onerror="this.style.visibility='hidden'"> ${esc(n.icon)} ${esc(n.name)}</td><td>${campBadge(n.faction)}</td><td>${esc(n.power)}</td><td>${bar(n.pop)}</td></tr>`).join(''));
    }
    function bodyActive(){
      // 玩家活跃榜：以亲密度总和为活跃度代理（同小程序 rankPlayers）
      const list=HT.rankPlayers();
      return table(`<tr><th>#</th><th>玩家</th><th>标签</th><th>亲密度总和</th><th>场次推测</th></tr>`,
          list.map(p=>`<tr${p.isMe?' style="background:rgba(107,140,255,.1)"':''}><td>${medal(p.rank-1)}</td><td>${avatarHtml(p.avatar,AVATAR_SIZE.rank)} <span style="margin-left:6px">${esc(p.name)}</span> ${p.isMe?'<span class="badge badge-ok">我</span>':''}</td><td>${esc(p.tag)}</td><td><b>${p.total}</b></td><td>约 ${Math.round(p.total/20)} 场</td></tr>`).join(''));
    }
    function bodyIntimacy(){
      const list=HT.rankPlayers();
      return table(`<tr><th>#</th><th>玩家</th><th>对全部NPC亲密度之和</th><th>均值</th><th>最高羁绊</th></tr>`,
          list.map(p=>{ const npc=p.npc||{}; const vals=Object.values(npc); const max=vals.length?Math.max(...vals):0; const top=DB.NPCS.find(n=>npc[n.id]===max); return `<tr${p.isMe?' style="background:rgba(107,140,255,.1)"':''}><td>${medal(p.rank-1)}</td><td>${avatarHtml(p.avatar,AVATAR_SIZE.rank)} <span style="margin-left:6px">${esc(p.name)}</span></td><td>${bar(p.total)}</td><td>${Math.round(p.total/6)}</td><td>${esc(top?top.icon+' '+top.name:'—')}</td></tr>`; }).join(''));
    }
    function bodyByNpc(){
      const list=HT.rankNpcs();
      return table(`<tr><th>NPC</th><th>全服总亲密度</th><th>参与玩家数</th><th>均值</th></tr>`,
          list.map(n=>{ const cnt=DB.INTIMACY.players.filter(p=>(p.npc[n.id]||0)>0).length; return `<tr><td><img class="thumb" src="${esc(n.cover)}" onerror="this.style.visibility='hidden'"> ${esc(n.icon)} ${esc(n.name)}</td><td>${bar(n.total)}</td><td>${cnt}</td><td>${Math.round(n.total/cnt)||0}</td></tr>`; }).join(''));
    }
    function table(head,body){ return `<div class="card" style="padding:0;overflow:hidden"><table>${head}<tbody>${body}</tbody></table></div>`; }
    function medal(i){ return `<span class="medals">${i<3?['🥇','🥈','🥉'][i]:i+1}</span>`; }

    const map={ pop:bodyPop, active:bodyActive, intimacy:bodyIntimacy, bynpc:bodyByNpc };
    function show(t){ $('#rankBody').innerHTML=(map[t]||bodyPop)(); $$('.tab-nav button').forEach(b=>b.classList.toggle('on',b.dataset.t===t)); }
    $$('.tab-nav button').forEach(b=>b.onclick=()=>show(b.dataset.t));
    show('pop');
  }

  /* ==================== 系统设置 ==================== */
  /* ==================== 角色权限 ==================== */
  const ROLE_PENDING = {};
  async function renderRole(){
    $('#view').innerHTML = loadingHtml('正在加载角色与权限');
    let roles = [], grouped = [];
    try{
      roles   = await API.roles.list();
      grouped = await API.roles.permsGrouped();
    }catch(e){
      $('#view').innerHTML = pageShell('角色权限','', '', '<div class="empty">加载失败：'+esc(e.message)+'</div>');
      return;
    }
    const canEdit = hasPerm('role:edit');
    const roleCards = roles.map(r=>{
      const owned = new Set(r.perms || []);
      const isSuper = r.roleKey === 'super';
      return `<div class="card rp-card" data-role="${esc(r.roleKey)}">
      <div class="card-head">
        <h3>${esc(r.roleName)} <code class="rp-key">${esc(r.roleKey)}</code>
          ${r.builtin?'<span class="badge">内置</span>':''}</h3>
        <span style="font-size:12px;color:var(--sub)">${esc(r.remark||'')}</span>
      </div>
      <div class="rp-perms">
        ${grouped.map(g=>`
          <div class="rp-group">
            <div class="rp-gname">${esc(g.moduleName)}</div>
            <div class="rp-items">
              ${g.items.map(p=>`
                <label class="rp-item ${owned.has(p.permCode)||isSuper?'on':''}">
                  <input type="checkbox" value="${esc(p.permCode)}"
                    ${owned.has(p.permCode)?'checked':''} ${(isSuper||!canEdit)?'disabled':''}
                    onchange="toggleRolePerm('${esc(r.roleKey)}','${esc(p.permCode)}',this.checked)">
                  <span>${esc(p.permName)}</span>
                </label>`).join('')}
            </div>
          </div>`).join('')}
      </div>
      <div class="card-foot rp-foot">
        <span class="rp-count">已选 <b>${isSuper?'全部':(r.perms||[]).length}</b> 项</span>
        ${canEdit && !isSuper ? `<button class="btn-ghost" onclick="saveRolePerms('${esc(r.roleKey)}')">保存权限</button>
          <button class="btn-danger" onclick="delRole('${esc(r.roleKey)}')">删除角色</button>` : ''}
        ${isSuper ? '<span style="font-size:12px;color:var(--sub)">超级管理员固定拥有全部权限</span>' : ''}
      </div>
    </div>`;
    }).join('');

    $('#view').innerHTML = pageShell('角色权限','按模块勾选权限 · 保存后立即生效',
        canEdit ? `<button class="btn-primary" onclick="openRoleForm()">＋ 新建角色</button>` : '',
        `<div class="rp-wrap">${roleCards || '<div class="empty">暂无角色</div>'}</div>`);
  }
  function toggleRolePerm(roleKey, permCode, on){
    if (!ROLE_PENDING[roleKey]) ROLE_PENDING[roleKey] = new Set();
    if (on) ROLE_PENDING[roleKey].add(permCode); else ROLE_PENDING[roleKey].delete(permCode);
  }
  async function saveRolePerms(roleKey){
    const box = document.querySelector('.rp-card[data-role="'+roleKey+'"]');
    if (!box){ toast('未找到角色区块','err'); return; }
    const picked = [...box.querySelectorAll('input[type=checkbox]')].filter(i=>i.checked).map(i=>i.value);
    try{
      await API.roles.update(roleKey, { roleKey:roleKey, perms:picked });
      delete ROLE_PENDING[roleKey];
      toast('权限已保存','ok');
      await renderRole();
    }catch(e){ toast(e.message||'保存失败','err'); }
  }
  async function delRole(roleKey){
    if (!confirm('确定删除角色「'+roleKey+'」？')) return;
    try{ await API.roles.remove(roleKey); toast('已删除','ok'); await renderRole(); }
    catch(e){ toast(e.message||'删除失败','err'); }
  }
  function openRoleForm(){
    openModal(`<div class="modal-head"><h2>新建角色</h2><button class="modal-close" onclick="tryCloseModal()">×</button></div>
    <div class="field"><label>角色标识 *</label><input id="r_key" placeholder="如 ops（英文，创建后不可改）"></div>
    <div class="field"><label>角色名称 *</label><input id="r_name" placeholder="如 客服专员"></div>
    <div class="field"><label>说明</label><input id="r_remark" placeholder="选填"></div>
    <div class="modal-foot"><button class="btn-ghost" onclick="tryCloseModal()">取消</button>
      <button class="btn-primary" onclick="saveNewRole()">保存</button></div>`);
  }
  async function saveNewRole(){
    const k = $('#r_key').value.trim(), n = $('#r_name').value.trim();
    if (!k){ toast('请填写角色标识','err'); return; }
    if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(k)){ toast('角色标识只能用英文字母/数字/下划线，且以字母开头','err'); return; }
    if (!n){ toast('请填写角色名称','err'); return; }
    try{
      await API.roles.create({ roleKey:k, roleName:n, remark:$('#r_remark').value.trim(), perms:[] });
      closeModal(); toast('已创建，请勾选权限后保存','ok'); await renderRole();
    }catch(e){ toast(e.message||'创建失败','err'); }
  }

  /* ==================== 操作日志 ==================== */
  let AUDIT_PAGE = 1;
  const AUDIT_FILTER = { user:'', module:'', action:'', success:'', kw:'', dateFrom:'', dateTo:'' };
  async function renderAudit(){
    $('#view').innerHTML = loadingHtml('正在加载操作日志');
    let res;
    try{
      res = await API.audit.list(AUDIT_FILTER, AUDIT_PAGE, 20);
    }catch(e){
      $('#view').innerHTML = pageShell('操作日志','','','<div class="empty">加载失败：'+esc(e.message)+'</div>');
      return;
    }
    const rows = res.rows || [], total = res.total || 0;
    const pages = Math.max(1, Math.ceil(total / 20));
    const ACTION_NAME = { add:'新增', update:'修改', delete:'删除', login:'登录' };
    const MODULES = ['npc','show','order','notice','player','intimacy','gacha','setting','account','role','audit'];
    const table = rows.length ? `<table><thead><tr>
      <th>时间</th><th>操作人</th><th>模块</th><th>动作</th><th>对象</th><th>摘要</th><th>IP</th><th>耗时</th><th>结果</th>
    </tr></thead><tbody>${rows.map(l=>`
      <tr>
        <td style="white-space:nowrap;font-size:12px">${esc(fmtDate(l.createdAt))}</td>
        <td>${esc(l.name||l.user||'')}<div style="font-size:11px;color:var(--sub)">${esc(l.user||'')}</div></td>
        <td>${esc(l.moduleName||l.module||'')}</td>
        <td>${esc(ACTION_NAME[l.action]||l.action||'')}</td>
        <td><code style="font-size:11px">${esc(l.targetId||'')}</code></td>
        <td class="au-sum">${esc(l.summary||'')}</td>
        <td style="font-size:12px;color:var(--sub)">${esc(l.ip||'')}</td>
        <td style="font-size:12px;color:var(--sub)">${l.costMs||0}ms</td>
        <td>${l.success?'<span class="badge badge-ok">成功</span>':'<span class="badge" style="color:var(--danger)">失败</span>'}</td>
      </tr>`).join('')}</tbody></table>`
        : '<div class="empty">暂无操作记录</div>';

    $('#view').innerHTML = pageShell('操作日志',
        '记录所有新增/修改/删除操作 · 共 '+total+' 条',
        `<input id="au_kw" placeholder="🔍 搜索操作人 / 摘要 / 对象" value="${esc(AUDIT_FILTER.kw)}">
     <select id="au_module" onchange="setAuditFilter('module',this.value)">
       <option value="">全部模块</option>
       ${MODULES.map(m=>`<option value="${m}" ${AUDIT_FILTER.module===m?'selected':''}>${esc(m)}</option>`).join('')}
     </select>
     <select id="au_action" onchange="setAuditFilter('action',this.value)">
       <option value="">全部动作</option>
       <option value="add" ${AUDIT_FILTER.action==='add'?'selected':''}>新增</option>
       <option value="update" ${AUDIT_FILTER.action==='update'?'selected':''}>修改</option>
       <option value="delete" ${AUDIT_FILTER.action==='delete'?'selected':''}>删除</option>
     </select>
     <button class="btn-ghost" onclick="resetAuditFilter()">重置</button>
     <span style="flex:1"></span>
     <button class="btn-ghost" onclick="cleanAudit()">清理 90 天前</button>`,
        table + (pages>1 ? `<div class="au-pager">
      <button class="btn-ghost" ${AUDIT_PAGE<=1?'disabled':''} onclick="gotoAuditPage(${AUDIT_PAGE-1})">上一页</button>
      <span>第 ${AUDIT_PAGE} / ${pages} 页</span>
      <button class="btn-ghost" ${AUDIT_PAGE>=pages?'disabled':''} onclick="gotoAuditPage(${AUDIT_PAGE+1})">下一页</button>
    </div>` : '')
    );
    const kw=$('#au_kw');
    if (kw && kw.addEventListener){
      let t=null;
      kw.oninput=e=>{ AUDIT_FILTER.kw=e.target.value; clearTimeout(t);
        t=setTimeout(()=>{ AUDIT_PAGE=1; renderAudit(); },300); };
    }
  }
  async function setAuditFilter(k,v){ AUDIT_FILTER[k]=v; AUDIT_PAGE=1; await renderAudit(); }
  async function resetAuditFilter(){
    Object.assign(AUDIT_FILTER,{user:'',module:'',action:'',success:'',kw:'',dateFrom:'',dateTo:''});
    AUDIT_PAGE=1; await renderAudit();
  }
  async function gotoAuditPage(p){ if(p<1) return; AUDIT_PAGE=p; await renderAudit(); }
  async function cleanAudit(){
    if(!confirm('确定清理 90 天前的操作日志？')) return;
    try{ const n=await API.audit.clean(90); toast('已清理 '+n+' 条','ok'); await renderAudit(); }
    catch(e){ toast(e.message||'清理失败','err'); }
  }

  function renderSetting(){
    const s=DB.SETTINGS;
    const html=`<div class="page-head"><div><h1>系统设置</h1><div class="desc">小程序全局配置 · 保存后实时生效</div></div></div>
    <div class="grid g2">
      <div class="card">
        <h1 style="font-size:16px;margin-bottom:16px">🏭 品牌与小程序</h1>
        <div class="field"><label>品牌名称</label><input id="c_store" value="${esc(s.storeName)}"></div>
        <div class="field"><label>小程序名称</label><input id="c_app" value="${esc(s.appName)}"></div>
        <div class="field"><label>客服电话</label><input id="c_phone" value="${esc(s.contactPhone)}"></div>
        <div class="field"><label>官方微信</label><input id="c_wechat" value="${esc(s.contactWechat)}"></div>
        <div class="field"><label>首页公告</label><textarea id="c_announce">${esc(s.announcement)}</textarea></div>
      </div>
      <div class="card">
        <h1 style="font-size:16px;margin-bottom:16px">📋 业务规则</h1>
        <div class="field"><label>开放预约</label><input id="c_book" value="${esc(s.bookStart)}"></div>
        <div class="field"><label>退款规则</label><input id="c_refund" value="${esc(s.refundRule)}"></div>
        <div class="field"><label>主题</label><select id="c_theme"><option>灰塔冷灰</option><option>暗夜蓝</option><option>经典紫</option></select></div>
        <div class="field"><label>管理员账号</label>
          <table style="margin-top:8px"><thead><tr><th>账号</th><th>角色</th><th>姓名</th></tr></thead><tbody>
            ${DB.ACCOUNTS.map(a=>`<tr><td>${esc(a.user)}</td><td>${esc(a.role)}</td><td>${esc(a.name)}</td></tr>`).join('')}
          </tbody></table>
        </div>
      </div>
    </div>
    <div class="card" style="margin-top:16px;text-align:right">
      <button class="btn-ghost" onclick="toast('演示：此处对接云函数同步','ok')">同步到云开发</button>
      <button class="btn-primary" onclick="saveSetting()">保存设置</button>
    </div>`;
    $('#view').innerHTML=html;
    setTimeout(()=>{const t=$('#c_theme');if(t)t.value=s.theme;},0);
  }
  async function saveSetting(){
    const kv={
      storeName:$('#c_store').value.trim(), appName:$('#c_app').value.trim(),
      contactPhone:$('#c_phone').value.trim(), contactWechat:$('#c_wechat').value.trim(),
      announcement:$('#c_announce').value.trim(), bookStart:$('#c_book').value.trim(),
      refundRule:$('#c_refund').value.trim(), theme:$('#c_theme').value
    };
    if(useApi()){
      const ok=await withApi('保存设置', async ()=>{ await API.settings.update(kv); await resync('settings'); });
      if(!ok) return;
    }else{
      HT.save();
    }
    Object.assign(DB.SETTINGS,kv);
    toast('设置已保存');
  }

  /* ==================== 刷新 / 重置 ==================== */
  async function refreshData(){
    if(useApi()){
      toast('正在拉取后端数据…');
      updateApiState('wait');
      await window.resyncAll();
      updateApiState(OFFLINE ? 'off' : 'on');
      toast(OFFLINE?'部分接口未通，已回退本地数据':'已同步最新数据', OFFLINE?'err':'ok');
      navigate(CURRENT); return;
    }
    if(confirm('确定重置全部数据为初始状态？')){ HT.reset(); toast('已重置'); setTimeout(()=>location.reload(),600); }
  }

  /* ==================== 导出 ==================== */
  function exportData(){
    if (typeof document === 'undefined') return;
    const blob=new Blob([JSON.stringify(DB,null,2)],{type:'application/json'});
    const a=document.createElement('a'); a.href=URL.createObjectURL(blob);
    a.download='huita_export_'+new Date().toISOString().slice(0,10)+'.json'; a.click();
    toast('已导出全部数据');
  }

  /* ==================== 全局暴露（供 onclick） ==================== */
  function exposeGlobals(){
    if (typeof window === 'undefined') return;
    window.navigate=navigate; window.closeModal=closeModal;
    window.openNpcForm=openNpcForm; window.saveNpc=saveNpc; window.delNpc=delNpc; window.viewNpc=viewNpc;
    window.openShowForm=openShowForm; window.saveShow=saveShow; window.delShow=delShow;
    window.openNoticeForm=openNoticeForm; window.saveNotice=saveNotice; window.delNotice=delNotice;
    window.changeOrderStatus=changeOrderStatus; window.delOrder=delOrder;
    window.updateMatrix=updateMatrix; window.pushIntimacy=pushIntimacy;
    window.saveSetting=saveSetting;
    window.refreshData=refreshData;
    window.emptyInfo=function(){ return { 后端为空: Object.keys(EMPTY).map(k=>EMPTY_LABEL[k]||k), 已同步: Object.keys(SYNCED).filter(k=>SYNCED[k]) }; };
    window.exportData=exportData;
    /* 暴露关键渲染/导航函数，供测试 & 控制台调试 */
    window.enter=enter;
    window.navigate=navigate;
    window.renderShow=renderShow;
    window.resetShowFilter=resetShowFilter; window.focusShowOnCal=focusShowOnCal; window.setShowMode=setShowMode; window.calGo=calGo; window.calToday=calToday; window.pickCalDay=pickCalDay; window.weekGo=weekGo; window.weekThis=weekThis; window.showsByDate=showsByDate; window.monthAdd=monthAdd; window.weekStartOf=weekStartOf; window.daysInMonth=daysInMonth; window.calMonth=calMonth; window.getShowMode=function(){return SHOW_MODE;}; window.setCalDay=function(d){SHOW_CAL_D=d;}; window.getCalDay=function(){return SHOW_CAL_D;}; window.fetchShows=fetchShows; window.setShowQuick=setShowQuick; window.setShowView=setShowView; window.showStats=showStats; window.showSorted=showSorted; window.showGroups=showGroups;
    window.showFilter=function(v){ if(v) Object.assign(SHOW_FILTER,v); return SHOW_FILTER; };
    window.showFilterInput=showFilterInput; window.quickShowRange=quickShowRange;
    window.money=money;
    window.pull=pull; window.resync=resync;
    window.toggleSaleStart=toggleSaleStart; window.saleStartInfo=saleStartInfo;
    window.toDatetimeLocal=toDatetimeLocal; window.showSaleStart=showSaleStart;
    window.todayStr=todayStr; window.nowLocalStr=nowLocalStr;
    window.gachaExpect=gachaExpect;
    window.gachaApi=gachaApi; window.gachaRarities=gachaRarities; window.loadGachaRarity=loadGachaRarity; window.DEFAULT_RARITY=DEFAULT_RARITY; window.gachaRarityRate=gachaRarityRate; window.gachaCardProb=gachaCardProb; window.gachaEffWeight=gachaEffWeight; window.gachaCurrentPool=gachaCurrentPool; window.gachaPoolById=gachaPoolById;
    window.gachaSimulate=gachaSimulate; window.getGachaSim=function(){ return GACHA_SIM; }; window.setGachaSim=function(v){ GACHA_SIM=v; }; window.runSim=runSim; window.clearSim=clearSim; window.donutChart=donutChart; window.loadUserPity=loadUserPity; window.saveUserPity=saveUserPity; window.resetUserPity=resetUserPity; window.openUserPityForm=openUserPityForm; window.gachaApiMissing=gachaApiMissing;
    window.switchGachaPool=switchGachaPool; window.saveGachaCard=saveGachaCard; window.delGachaCard=delGachaCard;
    window.saveGachaPool=saveGachaPool; window.delGachaPool=delGachaPool; window.openGachaCardForm=openGachaCardForm; window.openGachaPoolForm=openGachaPoolForm; window.gachaWeightInput=gachaWeightInput; window.gachaWeightCommit=gachaWeightCommit; window.saveGachaPity=saveGachaPity; window.renderGacha=renderGacha; window.gachaCurrentPool=gachaCurrentPool; window.gachaEffectiveRate=gachaEffectiveRate; window.gachaCardProb=gachaCardProb; window.gachaRarityWeight=gachaRarityWeight; window.gachaEffWeight=gachaEffWeight; window.renderGacha=renderGacha;
    /* 运行时改头像大小：setAvatarSize({table:56, list:44, rank:36}) 立即生效 */
    window.AVATAR_SIZE=AVATAR_SIZE;
    window.setAvatarSize=function(o){ Object.assign(AVATAR_SIZE,o||{});
      const m={player:renderPlayer, intimacy:renderIntimacy, rank:renderRank};
      if(m[CURRENT]) m[CURRENT](); };
    window.avatarHtml=avatarHtml; window.isImgUrl=isImgUrl;
    window.renderOrder=renderOrder;
    window.renderNpc=renderNpc;
    window.resetNpcFilter=resetNpcFilter; window.fetchNpcs=fetchNpcs;
    window.npcFilter=function(v){ if(v) Object.assign(NPC_FILTER,v); return NPC_FILTER; };
    window.renderDashboard=renderDashboard;
    window.renderIntimacy=renderIntimacy;
    window.renderNotice=renderNotice;
    window.renderPlayer=renderPlayer;
    window.renderRank=renderRank;
    window.renderSetting=renderSetting; window.openModal=openModal; window.closeModal=closeModal; window.tryCloseModal=tryCloseModal; window.modalDirty=modalDirty; window.snapshotModal=snapshotModal; window.onMaskClick=onMaskClick; window.renderNav=renderNav; window.ROUTES=ROUTES; window.PAGE_PERM=PAGE_PERM; window.NAV=NAV; window.restorePerms=restorePerms; window.__diagPerm=__diagPerm; window.openChangePwd=openChangePwd; window.submitChangePwd=submitChangePwd; window.getCurrent=function(){return CURRENT;}; window.renderRole=renderRole; window.renderAudit=renderAudit; window.hasPerm=hasPerm; window.canView=canView; window.setMyPerms=function(p){MY_PERMS=p||[];}; window.getMyPerms=function(){return MY_PERMS;}; window.saveRolePerms=saveRolePerms; window.toggleRolePerm=toggleRolePerm; window.delRole=delRole; window.openRoleForm=openRoleForm; window.saveNewRole=saveNewRole; window.setAuditFilter=setAuditFilter; window.gotoAuditPage=gotoAuditPage; window.cleanAudit=cleanAudit;
    window.resyncAll=function(){ return resync('npcs','shows','orders','notices','players','settings','statusOptions','intimacy','stats'); };
    /* 一键诊断：控制台执行 __diag() 即可看到"到底有没有走后端" */
    window.__diag=function(){
      const d={
        '1_接口层已加载'  : (typeof API!=='undefined' && !!API),
        '2_USE_API(实际上路)'      : useApi(),
        '3_接口基址'      : (typeof API!=='undefined' && API) ? API.base : '(api.js 未加载！)',
        '4_失败回退fallback': (typeof API!=='undefined' && API) ? API.fallback : '-',
        '5_后端不可达'    : OFFLINE,
        '6_最近错误'      : (typeof API!=='undefined' && API) ? (API.lastError||'无') : '-',
        '7_已同步资源'    : Object.keys(SYNCED).filter(k=>SYNCED[k]),
        '8_未同步资源'    : Object.keys(SYNCED).filter(k=>!SYNCED[k]),
        '9_数据量'        : { NPC:DB.NPCS.length, SHOW:DB.SHOWS.length, ORDER:DB.ORDERS.length,
          NOTICE:DB.NOTICES.length, PLAYER:DB.PLAYERS.length },
        '10_后端统计'     : DB.STATS || '(未拉到 /dashboard/stats)',
        '11_当前登录'     : (()=>{ try{ return JSON.parse(sessionStorage.getItem('ht_login')||'{}'); }catch(e){ return '无'; } })()
      };
      console.log('%c接口诊断','font-weight:700;font-size:14px'); console.table ? console.log(d) : 0;
      if(!d['1_接口层已加载']){
        console.error('❌ api.js 没有加载！请确认：1) api.js 已上传到 /fossa/common/js/  2) index.html 里有 <script src="/fossa/common/js/api.js">  3) 强制刷新(Ctrl+F5)');
      }else if(!d['2_USE_API(实际上路)']){
        console.error('❌ API.enabled=false，前端处于纯静态模式。执行 API.enabled=true;location.reload()');
      }else if(d['5_后端不可达']){
        console.error('❌ 接口调用失败：'+d['6_最近错误']+'（后端通但前端请求失败，多为跨域或 Cookie）');
      }else{
        console.log('%c✅ 数据来自后端，数据量见上','color:#137752;font-weight:600');
      }
      return d;
    };
    /* 手动重拉一次（排查用） */
    window.__reload=async function(){ await window.resyncAll(); updateApiState(OFFLINE?'off':'on'); navigate(CURRENT); return window.__diag(); };
  }
  exposeGlobals();

  /* ==================== 启动 ==================== */
  /* api.js 没加载时给出醒目横幅——否则会静默退回纯静态模式，最难排查 */
  function warnApiMissing(){
    if (typeof API !== 'undefined' && API) return false;
    if (typeof document === 'undefined' || !document.body) return true;
    if (document.getElementById('apiMissingTip')) return true;
    const tip=document.createElement('div');
    tip.id='apiMissingTip';
    tip.style.cssText='position:fixed;left:0;right:0;top:0;z-index:9999;background:#b42318;color:#fff;'+
        'font-size:13px;line-height:1.8;padding:10px 18px;box-shadow:0 2px 12px rgba(0,0,0,.25)';
    tip.innerHTML='⚠️ <b>api.js 未加载</b>，当前页面是纯静态演示数据，不会请求后端接口。'+
        '请检查：① api.js 是否已上传到 <code>/fossa/common/js/api.js</code>；'+
        '② index.html 是否在 data.js 之后加了 <code>&lt;script src="/fossa/common/js/api.js"&gt;&lt;/script&gt;</code>；'+
        '③ 强制刷新（Ctrl+F5）。<span style="float:right;cursor:pointer" onclick="this.parentNode.remove()">关闭 ✕</span>';
    document.body.appendChild(tip);
    return true;
  }

  function init(){
    bindLogin();
    warnApiMissing();
    updateApiState((typeof API!=='undefined' && API && API.enabled) ? 'wait' : 'off_');
    if (typeof sessionStorage !== 'undefined' && sessionStorage.getItem('ht_login')) enter();
    else { const login=$('#login'); if(login) login.classList.remove('hidden'); }
  }
  if (typeof document !== 'undefined') {
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded',init);
    else init();
  }

})(typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : {}));
