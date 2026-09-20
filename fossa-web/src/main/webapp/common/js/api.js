/* ============================================================
 * 灰塔之下 · 管理后台 接口层 (api.js)
 * 对接后端 Controller（统一返回 R<?> = { code, msg, data }）
 *   /fossa/api/dashboard/stats
 *   /fossa/api/npcs      GET POST /{id} GET PUT DELETE
 *   /fossa/api/shows     GET(?npcId) POST /{id} GET PUT DELETE
 *   /fossa/api/orders    GET /status-options  PUT /{id}/status  DELETE /{id}
 *   /fossa/api/notices   GET POST /{id} GET PUT DELETE
 *   /fossa/api/players   GET /{id}
 *   /fossa/api/intimacy  /by-npc/{npcId} /matrix/{playerId} /rank
 *                        PUT /{playerId}/{npcId}?value=  DELETE /{playerId}/{npcId}
 *   /fossa/api/settings  GET PUT
 * ============================================================ */
(function(global){
'use strict';

/* ---------- 配置 ---------- */
const BASE    = '/fossa/api';  // 上下文路径 /fossa + Controller 的 /api
const TIMEOUT = 10000;         // 单次请求超时（毫秒）

const API = {
  base: BASE,
  timeout: TIMEOUT,
  enabled : true,  // 总开关：false = 纯本地静态数据（原来的行为）
  /* 接口失败时是否回退本地演示数据。
   * false（默认/纯后端模式）：接口失败就报错并显示空态，绝不用演示数据冒充数据库数据。
   * true：断网时用 data.js 的演示数据兜底（仅调试用，容易误判数据已接上）。 */
  fallback: false,
  verbose : false, // true = 控制台打印每次请求/响应，便于联调
  lastError: null,
  online  : null   // null=未知 true=通 false=断
};

/* ---------- 通用请求 ---------- */
function buildUrl(path, params){
  let u = BASE + path;
  const qs = [];
  if (params && typeof params === 'object'){
    for (const k in params){
      const v = params[k];
      if (v === undefined || v === null || v === '') continue;
      qs.push(encodeURIComponent(k) + '=' + encodeURIComponent(v));
    }
  }
  return qs.length ? u + (u.indexOf('?') >= 0 ? '&' : '?') + qs.join('&') : u;
}

/* ---------- 登录令牌 ----------
 * 后端下发的 token 存在 sessionStorage，每个请求通过 X-Token 头带上。
 * 401 会触发 API.onUnauthorized（app.js 里挂到跳登录页）。 */
const TOKEN_KEY = 'ht_token';
function getToken(){
  try{
    if (typeof sessionStorage !== 'undefined') return sessionStorage.getItem(TOKEN_KEY) || '';
  }catch(e){}
  return '';
}
function setToken(t){
  try{
    if (typeof sessionStorage === 'undefined') return;
    if (t) sessionStorage.setItem(TOKEN_KEY, t); else sessionStorage.removeItem(TOKEN_KEY);
  }catch(e){}
}
/* 未登录回调，app.js 会覆盖它 */
API.onUnauthorized = function(){};

/* 发请求，成功返回 R.data，失败抛 Error（message 可直接 toast 给用户） */
async function request(method, path, opts){
  opts = opts || {};
  const url = buildUrl(path, opts.params);
  const hasBody = (method === 'POST' || method === 'PUT' || method === 'PATCH') && opts.body !== undefined;

  const ctrl  = (typeof AbortController !== 'undefined') ? new AbortController() : null;
  const timer = ctrl ? setTimeout(()=>{ try{ ctrl.abort(); }catch(e){} }, API.timeout) : null;

  try{
    const res = await fetch(url, {
      method: method,
      credentials: 'same-origin',
      headers: Object.assign(
        { 'Accept':'application/json' },
        /* ★ 每个请求都带上登录令牌 */
        (function(){ const t = getToken(); return t ? { 'X-Token': t } : {}; })(),
        hasBody ? { 'Content-Type':'application/json' } : {}
      ),
      body: hasBody ? JSON.stringify(opts.body) : undefined,
      signal: ctrl ? ctrl.signal : undefined
    });

    // 后端返回 HTML（网关 502 / 未配置路由）时降级为 null
    const json = await res.json().catch(()=>null);

    if (API.verbose) console.log('[API]', method, url, '→', res.status, json);

    if (!res.ok){
      API.online = (res.status !== 502 && res.status !== 503 && res.status !== 504);
      /* ★ 401 未登录 / 403 无权限：单独抛，便于上层区分处理 */
      if (res.status === 401){
        try{ API.onUnauthorized(pickMsg(json) || '登录已失效'); }catch(e){}
        const e401 = new Error(pickMsg(json) || '登录已失效，请重新登录');
        e401.code = 401; throw e401;
      }
      if (res.status === 403){
        const e403 = new Error(pickMsg(json) || '没有操作权限');
        e403.code = 403; throw e403;
      }
      throw new Error(pickMsg(json) || ('请求失败（HTTP ' + res.status + '）'));
    }
    if (json && typeof json === 'object'){
      // R 结构：{ code, msg, data }；也兼容 { success } / 直接返回 data 本体
      const code = json.code;
      const ok = (code === undefined || code === null || code === 0 || code === 200 || String(code) === '200') || json.success === true;
      if (!ok) throw new Error(json.msg || json.message || '操作失败');
      API.online = true;
      API.lastError = null;
      // R<?> 结构优先取 data；裸 data 响应（无 data 字段）返回整体
      return Object.prototype.hasOwnProperty.call(json, 'data') ? json.data : json;
    }
    throw new Error('响应格式异常（非 JSON 对象）');
  }catch(e){
    let msg, code = e && e.code;      // ★ 保留 401/403 业务码，供上层区分处理
    if (e && e.name === 'AbortError'){ msg = '请求超时：' + path; API.online = false; }
    else if (e instanceof TypeError || (e && e.name === 'TypeError')){ msg = '无法连接服务器：' + BASE; API.online = false; }
    else msg = e.message || '请求失败';
    API.lastError = msg;
    if (API.verbose) console.warn('[API]', method, url, '×', msg);
    const err = new Error(msg);
    if (code) err.code = code;
    throw err;
  }finally{
    if (timer) clearTimeout(timer);
  }
}

function pickMsg(json){
  if (!json || typeof json !== 'object') return '';
  return json.msg || json.message || json.error || '';
}

const get  = (p, params)        => request('GET', p, { params: params });
const post = (p, body)          => request('POST', p, { body: body });
const put  = (p, body, params)  => request('PUT', p, { body: body, params: params });
const del  = (p, params)        => request('DELETE', p, { params: params });

/* ---------- 字段归一化 ----------
 * 后端实体字段名可能与前端不一致（如 npcId / npc_id / id）。
 * 如需调整，只改下面的 pick / norm* 函数即可，业务层不用动。 */
function pick(row, keys, def){
  if (row == null) return def;
  if (typeof row !== 'object') return row;
  for (const k of keys){
    if (row[k] !== undefined && row[k] !== null) return row[k];
  }
  // 下划线转驼峰再试一次
  for (const k of keys){
    const u = k.replace(/([A-Z])/g, '_$1').toLowerCase();
    if (row[u] !== undefined && row[u] !== null) return row[u];
  }
  return def;
}
function asArray(v){ return Array.isArray(v) ? v : (v == null ? [] : [v]); }

/* 通用列表行 → { id, ...原样字段 } */
function normRow(row){
  if (!row || typeof row !== 'object') return row;
  if (row.id === undefined){
    const id = pick(row, ['id','npcId','showId','orderId','playerId','noticeId','code','no']);
    if (id !== undefined) row.id = id;
  }
  return row;
}
function normList(v){ return asArray(v).map(normRow); }

/* 亲密度行 → { playerId, playerName, value } */
function normIntimacyRow(row){
  return {
    playerId  : String(pick(row, ['playerId','player_id','pid','userId','id'], '')),
    playerName: pick(row, ['playerName','player_name','name','nickname','userName'], ''),
    avatar    : pick(row, ['avatar','avatarUrl'], ''),
    tag       : pick(row, ['tag','playerTag'], ''),
    value     : Number(pick(row, ['value','score','intimacy','total','amount'], 0)) || 0,
    _raw      : row
  };
}

/* ---------- 资源 API ---------- */
API.dashboard = {
  stats: () => get('/dashboard/stats')
};

API.npcs = {
  list  : ()            => get('/npcs').then(normList).then(l=>l.map(x=>normNpc(deserializeFor('npcs',x)))),
  /* 条件查询：GET /npcs?keyword=xxx&faction=xxx */
  query : (params)      => get('/npcs', params).then(normList).then(l=>l.map(x=>normNpc(deserializeFor('npcs',x)))),
  get   : id            => get('/npcs/' + encodeURIComponent(id)).then(v=>normNpc(deserializeFor('npcs', v))),
  create: npc           => writeWith('npcs','POST','/npcs', serializeNpc(npc)),
  update: (id, npc)     => writeWith('npcs','PUT','/npcs/' + encodeURIComponent(id), serializeNpc(npc)),
  remove: id            => del('/npcs/' + encodeURIComponent(id))
};

/* 场次：后端可能返回驼峰 saleStartAt（实体字段）或下划线 sale_start_at（列名），统一成 saleStartAt */
function normShow(row){
  if (!row || typeof row !== 'object') return row;
  const out = Object.assign({}, row);
  if (out.saleStartAt === undefined){
    const v = out.sale_start_at !== undefined ? out.sale_start_at
            : (out.saleStart !== undefined ? out.saleStart : undefined);
    if (v !== undefined) out.saleStartAt = v;
  }
  return out;
}

/* ---------- 开抢时间：前后端格式转换 ----------
 * <input type="datetime-local"> 只认 'YYYY-MM-DDTHH:mm'（带 T）
 * 后端 @JsonFormat("yyyy-MM-dd HH:mm:ss") 只认 'YYYY-MM-DD HH:mm:ss'（空格）
 * 两边在这里对齐：读取时 T 化（给控件），提交时空格化（给后端）。 */
let   TIME_SEP_API = ' ';   // 提交给后端用的分隔符（默认空格，匹配 @JsonFormat）

/* 把任意时间值转成后端要的字符串；失败则原样返回 */
function saleStartForApi(v){
  if (v == null || v === '') return v;
  const raw = String(v).trim();
  if (!raw) return '';
  const p = n => String(n).padStart(2, '0');
  const d = new Date(raw.replace(/-/g, '/').replace('T', ' '));
  if (isNaN(d.getTime())) return raw;
  return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}`
       + TIME_SEP_API
       + `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

/* 提交前把 saleStartAt 转成后端能解析的格式 */
function serializeShow(obj){
  if (!obj || typeof obj !== 'object') return obj;
  const out = Object.assign({}, obj);
  if (out.saleStartAt !== undefined) out.saleStartAt = saleStartForApi(out.saleStartAt);
  /* 保险：清掉下划线列名，避免与实体字段冲突 */
  ['sale_start_at','saleStart'].forEach(k=>{ if (out[k] !== undefined) delete out[k]; });
  return out;
}

/* 带"时间格式自适应"的写请求：
 * 后端若报错 LocalDateTime 解析失败，自动换另一种分隔符重试一次，并记住。 */
async function writeShow(method, path, payload){
  const send = p => (method === 'POST') ? post(path, p) : put(path, p);
  let cur = serializeShow(payload);
  for (let i = 0; i < 3; i++){
    try{
      return await send(cur);
    }catch(e){
      const msg = String((e && e.message) || '');
      if (!/LocalDateTime|saleStartAt|DateTimeParse/i.test(msg)) throw e;
      /* 换分隔符重试：空格 ↔ T，并记住，后续请求直接用 */
      TIME_SEP_API = (TIME_SEP_API === ' ') ? 'T' : ' ';
      console.warn('[api] 开抢时间格式后端无法解析，改用分隔符 '
                   + JSON.stringify(TIME_SEP_API) + ' 重试');
      const next = saleStartForApi(payload && payload.saleStartAt);
      if (!next || next === cur.saleStartAt) throw e;   // 换不了就别死循环
      cur = Object.assign({}, cur, { saleStartAt: next });
    }
  }
  return await send(cur);
}

API.shows = {
  list  : npcId         => get('/shows', npcId ? { npcId: npcId } : null).then(normList).then(l=>l.map(normShow)),
  /* 条件查询：GET /shows?keyword=xxx&status=xxx&npcId=xxx
   * 后端当前只支持 npcId；keyword / status 加上后即为真正的数据库查询。
   * 前端保留一次本地过滤兜底，后端未支持时功能同样正确。 */
  query : (params)      => get('/shows', params).then(normList).then(l=>l.map(normShow)),
  get   : id            => get('/shows/' + encodeURIComponent(id)).then(normShow),
  create: show          => writeShow('POST', '/shows', show),
  update: (id, show)    => writeShow('PUT', '/shows/' + encodeURIComponent(id), show),
  remove: id            => del('/shows/' + encodeURIComponent(id))
};

/* ---------- 小程序模块接口（/fossa/mini/**） ----------
 * 抽卡的「用户级保底设置」「我的卡牌/记录」在 fossa-mini 模块，
 * 路径前缀与后台不同，所以单独封装。同源部署，无需跨域。 */
const MINI_BASE = '/fossa/mini';
API._serializeGachaPool = serializeGachaPool;
function miniGet(path, params){ return get(MINI_BASE + path, params); }
function miniPut(path, body){   return put(MINI_BASE + path, body); }

/* ---------- 抽卡奖池 ----------
 * 后端接口（需新增，见 gacha-backend 包）：
 *   GET    /api/gacha/pools            奖池列表
 *   GET    /api/gacha/pools/{id}       单个奖池（含 cards）
 *   POST   /api/gacha/pools            新建奖池
 *   PUT    /api/gacha/pools/{id}       更新奖池（整池提交：规则 + 保底 + 卡牌）
 *   DELETE /api/gacha/pools/{id}       删除奖池
 *   PUT    /api/gacha/cards/{id}/weight?value=NN   单独改某张卡权重（滑块高频调用走这个，更轻）
 *   GET    /api/gacha/rarity           稀有度定义与基础概率
 */
function normGachaPool(row){
  if (!row || typeof row !== 'object') return row;
  const out = Object.assign({}, row);
  /* cards 可能在 cards / items / entries 字段里 */
  if (!Array.isArray(out.cards)){
    const alt = out.items || out.entries || out.poolItems;
    if (Array.isArray(alt)) out.cards = alt;
  }
  if (!Array.isArray(out.cards)) out.cards = [];
  out.cards = out.cards.map(c => Object.assign({}, c, {
    id:      c.id      != null ? String(c.id)      : '',
    /* 卡面图：缩略图 + 大图（小程序抽卡结果直接渲染） */
    thumb:   String(c.thumb || c.thumbUrl || c.icon || ''),
    image:   String(c.image || c.img     || c.cover || ''),
    npc:     c.npc     != null ? String(c.npc)     : (c.npcId != null ? String(c.npcId) : ''),
    rarity:  String(c.rarity || c.rarityKey || 'N').toUpperCase(),
    weight:  Number(c.weight != null ? c.weight : (c.prob != null ? c.prob : 0)) || 0,
    up:      !!(c.up || c.isUp || c.rateUp),
    stock:   c.stock != null ? Number(c.stock) : -1
  }));
  /* ★ 后端实体是扁平字段（pitySsr / pitySr / upNpcId / upBonus），也可能有接口返回嵌套结构，两种都要认。
   *   注意：不能写成 `out.pity && out.pity.ssr` —— pity 为 undefined 时整个表达式短路成 undefined，
   *   会永远取不到扁平的 pitySsr，导致保底被解析成 0（表现为模拟器空窗远超保底）。 */
  const pickG = (...vals) => { for (const v of vals){ if (v !== undefined && v !== null && v !== '') return v; } return undefined; };
  const pIn = out.pity || {};
  out.pity = {
    ssr: Number(pickG(pIn.ssr, out.pitySsr, out.pity_ssr) || 0) || 0,
    sr : Number(pickG(pIn.sr,  out.pitySr,  out.pity_sr ) || 0) || 0,
    /* 出货窗口起点：满 N 抽后才可能出（0=不限） */
    ssrStart: Number(pickG(pIn.ssrStart, out.pitySsrStart, out.pity_ssr_start) || 0) || 0,
    srStart : Number(pickG(pIn.srStart,  out.pitySrStart,  out.pity_sr_start ) || 0) || 0,
    resetOnHit: !(pickG(pIn.resetOnHit, out.resetOnHit, out.reset_on_hit) === false
               || String(pickG(pIn.resetOnHit, out.resetOnHit, out.reset_on_hit)) === '0')
  };
  /* 全局默认最低门槛：兼容 minSsrDraws / min_ssr_draws */
  out.minSsrDraws = Number(out.minSsrDraws != null ? out.minSsrDraws
                    : (out.min_ssr_draws != null ? out.min_ssr_draws : 0)) || 0;
  const rIn = out.rateUp || {};
  out.rateUp = {
    npcId: String(pickG(rIn.npcId, out.upNpcId, out.up_npc_id) || ''),
    bonus: Number(pickG(rIn.bonus, out.upBonus, out.up_bonus) || 2) || 2
  };
  return out;
}

/* ---------- 提交前序列化 ----------
 * 前端页面用的是嵌套结构：pool.pity.ssr / pool.rateUp.npcId / card.npc / card.up，
 * 但后端实体是扁平字段：pitySsr / upNpcId / npcId / isUp。
 * Jackson 按实体属性名匹配，收到嵌套对象会【静默丢弃】——不报错但值存不进去。
 * 所以提交前必须摊平。 */
/* 稀有度定义归一化：后端可能返回 rarity_key / rarityKey，rate 可能是字符串 */
function normGachaRarity(r){
  if (!r || typeof r !== 'object') return null;
  const key = String(r.rarityKey || r.rarity_key || r.key || r.rarity || '').toUpperCase();
  if (!key) return null;
  return {
    key:   key,
    name:  String(r.rarityName || r.rarity_name || r.name || key),
    label: String(r.label || ''),
    color: String(r.color || '#6b7280'),
    rate:  Number(r.rate != null ? r.rate : 0) || 0,
    order: Number(r.sortOrder != null ? r.sortOrder : (r.sort_order != null ? r.sort_order : 0)) || 0
  };
}

function serializeGachaPool(pool){
  if (!pool || typeof pool !== 'object') return pool;
  const out = Object.assign({}, pool);
  const pity   = pool.pity   || {};
  const rateUp = pool.rateUp || {};
  out.pitySsr    = Number(pity.ssr != null ? pity.ssr : (pool.pitySsr != null ? pool.pitySsr : 0)) || 0;
  out.pitySr     = Number(pity.sr  != null ? pity.sr  : (pool.pitySr  != null ? pool.pitySr  : 0)) || 0;
  out.pitySsrStart = Number(pity.ssrStart != null ? pity.ssrStart : (pool.pitySsrStart != null ? pool.pitySsrStart : 0)) || 0;
  out.pitySrStart  = Number(pity.srStart  != null ? pity.srStart  : (pool.pitySrStart  != null ? pool.pitySrStart  : 0)) || 0;
  out.resetOnHit = (pity.resetOnHit === false || pool.resetOnHit === false) ? 0 : 1;
  out.upNpcId    = rateUp.npcId != null ? rateUp.npcId : (pool.upNpcId || '');
  out.upBonus    = Number(rateUp.bonus != null ? rateUp.bonus : (pool.upBonus != null ? pool.upBonus : 2)) || 2;
  out.minSsrDraws= Number(pool.minSsrDraws != null ? pool.minSsrDraws : 0) || 0;
  /* 删掉前端专属的嵌套对象，避免后端遇到未知属性 */
  delete out.pity; delete out.rateUp;
  if (Array.isArray(out.cards)){
    out.cards = out.cards.map(c => {
      const o = Object.assign({}, c);
      o.npcId = String(o.npc != null ? o.npc : (o.npcId != null ? o.npcId : ''));
      o.isUp  = (o.up || o.isUp || o.rateUp) ? 1 : 0;
      o.rarity = String(o.rarity || 'N').toUpperCase();
      o.thumb  = String(o.thumb || '');
      o.image  = String(o.image || '');
      o.weight = Number(o.weight != null ? o.weight : 0) || 0;
      o.stock  = o.stock != null ? Number(o.stock) : -1;
      delete o.npc; delete o.up; delete o.rateUp; delete o.prob; delete o.share;
      return o;
    });
  }
  return out;
}

API.gacha = {
  listPools  : ()          => get('/gacha/pools').then(normList).then(l=>l.map(normGachaPool)),
  getPool    : id          => get('/gacha/pools/' + encodeURIComponent(id)).then(normGachaPool),
  createPool : pool        => post('/gacha/pools', serializeGachaPool(pool)),
  updatePool : (id, pool)  => put('/gacha/pools/' + encodeURIComponent(id), serializeGachaPool(pool)),
  removePool : id          => del('/gacha/pools/' + encodeURIComponent(id)),
  /* 单独改权重：滑块高频拖动时走这个，避免整池提交 */
  setWeight  : (cardId, w) => put('/gacha/cards/' + encodeURIComponent(cardId) + '/weight', null, { value: w }),
  rarity     : ()          => get('/gacha/rarity'),
  /* 稀有度完整定义（名称/标签/颜色/排序），卡牌表单的「稀有度」下拉就是读这个。
   * 后端没实现 /rarity/list 时返回 null，前端会自行推导（见 app.js loadGachaRarity）。 */
  rarityList : ()          => get('/gacha/rarity/list')
                                .then(l => Array.isArray(l) ? l.map(normGachaRarity).filter(Boolean) : null)
                                .catch(() => null),

  /* ---- 玩家保底覆盖 ----
   * 这些接口在 fossa-mini 模块（/fossa/mini/**），不是后台的 /fossa/api/**。
   * 所以这里要显式传完整路径：miniGet 内部会用 MINI_BASE='/fossa/mini'。 */
  userPity   : (userId, poolId) => miniGet('/user/' + encodeURIComponent(userId) + '/pity', { poolId }),
  setUserPity: (userId, req)    => miniPut('/user/' + encodeURIComponent(userId) + '/pity', req),
  userCards  : (userId, rarity) => miniGet('/user/' + encodeURIComponent(userId) + '/cards', rarity ? { rarity } : {}),
  userRecords: (userId, poolId, limit) =>
      miniGet('/user/' + encodeURIComponent(userId) + '/records', { poolId: poolId || '', limit: limit || 20 })
};

API.orders = {
  list         : ()              => get('/orders').then(normList),
  statusOptions: ()              => get('/orders/status-options').then(asArray),
  updateStatus : (id, status)    => put('/orders/' + encodeURIComponent(id) + '/status', null, { status: status }),
  remove       : id              => del('/orders/' + encodeURIComponent(id))
};

API.notices = {
  list  : ()            => get('/notices').then(normList),
  get   : id            => get('/notices/' + encodeURIComponent(id)),
  create: n             => post('/notices', n),
  update: (id, n)       => put('/notices/' + encodeURIComponent(id), n),
  remove: id            => del('/notices/' + encodeURIComponent(id))
};

API.players = {
  list: ()     => get('/players').then(normList),
  get : id     => get('/players/' + encodeURIComponent(id))
};

API.intimacy = {
  byNpc : npcId            => get('/intimacy/by-npc/' + encodeURIComponent(npcId)).then(v => asArray(v).map(normIntimacyRow)),
  matrix: playerId         => get('/intimacy/matrix/' + encodeURIComponent(playerId)),
  rank  : ()               => get('/intimacy/rank').then(v => asArray(v).map(normIntimacyRow)),
  update: (playerId, npcId, value) =>
    put('/intimacy/' + encodeURIComponent(playerId) + '/' + encodeURIComponent(npcId), null, { value: value }),
  remove: (playerId, npcId) =>
    del('/intimacy/' + encodeURIComponent(playerId) + '/' + encodeURIComponent(npcId))
};

API.settings = {
  get   : ()    => get('/settings'),
  update: kv    => put('/settings', kv)
};

/* ---------- 健康检查 ---------- */
API.health = async function(){
  try{ await API.dashboard.stats(); return true; }
  catch(e){ return false; }
};

/* ============================================================
 * 数组字段 ↔ 字符串 适配层
 * 背景：后端 Npc 实体的 tags 是 java.lang.String（如 "青梅竹马,逃亡者"），
 *       而前端一直当数组用（渲染 .map / 表单 .join），
 *       直接把数组 POST 过去会报：
 *       Cannot deserialize value of type `java.lang.String` from Array value … Npc["tags"]
 * 方案：① 配置式（listFields）：声明哪些字段要"数组→字符串"
 *       ② 自动兜底（autoFix）：首次报 400 时从异常里解析出字段名，
 *          转成字符串重试一次，并记住配置，后续不再踩坑。
 *       ③ 读取时反向还原（fromWire）：字符串 → 数组，保证页面渲染逻辑不用改。
 * ============================================================ */
API.listSep   = ',';      // 数组转字符串的默认连接符
API.autoFix   = true;     // 400 报错时自动识别字段并重试
API.listFields = {
  /* 资源名: { 字段名: 分隔符 } —— 根据你的实体类型补充即可
   * 全部来自 npc 表定义：tags text、quotes text → 都是 java.lang.String */
  npcs   : { tags: API.listSep, quotes: '\n' },  // Npc.tags / Npc.quotes 均为 String
  notices: {},
  shows  : {},
  orders : {},
  players: {}
};

function toArr(v, sep){
  if (Array.isArray(v)) return v;
  if (v == null || v === '') return [];
  if (typeof v === 'string') return v.split(sep || API.listSep).map(s=>s.trim()).filter(Boolean);
  return [v];
}
/* 前端对象 → 后端 JSON（数组按配置转成字符串） */
function serializeFor(resource, obj){
  if (!obj || typeof obj !== 'object') return obj;
  const cfg = (API.listFields && API.listFields[resource]) || {};
  const out = Object.assign({}, obj);
  for (const k in cfg){
    if (Array.isArray(out[k])) out[k] = out[k].join(cfg[k] || API.listSep);
  }
  return out;
}
/* 后端 JSON → 前端对象（字符串还原成数组） */
function deserializeFor(resource, obj){
  if (!obj || typeof obj !== 'object') return obj;
  const cfg = (API.listFields && API.listFields[resource]) || {};
  const out = Object.assign({}, obj);
  for (const k in cfg){ if (out[k] !== undefined) out[k] = toArr(out[k], cfg[k]); }
  return out;
}
API.toArr = toArr;
API.serializeFor = serializeFor;
API.deserializeFor = deserializeFor;

/* ---------- NPC 专用字段映射 ----------
 * com.txy.entity.Npc 的字段名是「驼峰」：campLabel / hookAction / hookContrast / hookFragile / hookSpeech
 *   （DB 列名才是下划线的 camp_label / hook_action…，由 MyBatis 驼峰转换负责映射）
 * 前端一直用「嵌套结构」：campLabel / hooks.{action,contrast,fragile,speech}
 * 两边在这里对齐：发请求时摊平+保持驼峰，收响应时还原成 hooks。
 * ⚠️ 提交时绝不能把 campLabel 改成 camp_label —— Jackson 按实体字段名匹配，
 *    名字对不上会静默丢弃，值根本进不了实体（就是之前 hook_* 为空的原因）。 */
const NPC_HOOKS = [
  /* [前端 hooks 的 key, 实体字段名(驼峰), DB列名(下划线)] */
  ['action',   'hookAction',   'hook_action'],
  ['contrast', 'hookContrast', 'hook_contrast'],
  ['fragile',  'hookFragile',  'hook_fragile'],
  ['speech',   'hookSpeech',   'hook_speech']
];

/* 后端行 → 前端对象：把实体的驼峰字段还原成页面用的结构 */
function normNpc(row){
  if (!row || typeof row !== 'object') return row;
  const out = Object.assign({}, row);
  /* campLabel：实体就叫 campLabel；也兼容万一返回 camp_label 的情况 */
  if (out.campLabel === undefined && out.camp_label !== undefined) out.campLabel = out.camp_label;
  /* hookAction/hookContrast/hookFragile/hookSpeech（或 hook_*）→ 前端的 hooks 对象 */
  const hooks = {};
  NPC_HOOKS.forEach(([k, camel, snake])=>{
    const v = (out.hooks && out.hooks[k]) || out[camel] || out[snake];
    if (v !== undefined && v !== null && v !== '') hooks[k] = String(v);
  });
  out.hooks = hooks;
  return out;
}

/* 前端对象 → 后端 JSON：只做两件事
 *  ① hooks 对象摊平成实体的驼峰字段（hookAction / hookContrast / hookFragile / hookSpeech）
 *  ② 删掉前端专属的 hooks（实体里没有这个属性，发过去会被丢弃）
 * 注意：campLabel 保持原名不动 —— 实体字段就叫 campLabel，
 *      不能改成 camp_label，否则 Jackson 匹配不上，值会静默丢失。 */
function serializeNpc(obj){
  if (!obj || typeof obj !== 'object') return obj;
  const out = Object.assign({}, obj);
  const h = out.hooks || {};
  NPC_HOOKS.forEach(([k, camel])=>{ out[camel] = (h[k] == null ? '' : String(h[k])); });
  delete out.hooks;
  /* 保险起见：万一之前误加过下划线字段，一并清掉，避免干扰 */
  NPC_HOOKS.forEach(([k, camel, snake])=>{ if (out[snake] !== undefined) delete out[snake]; });
  if (out.camp_label !== undefined) delete out.camp_label;
  return out;
}
API.normNpc = normNpc;
API.serializeNpc = serializeNpc;

/* 从 Jackson 异常里抠出字段名：… Npc["tags"]) → tags */
function fieldFromErr(msg){
  if (!msg || typeof msg !== 'string') return null;
  /* 优先取"异常末尾"的字段：… (through reference chain: com.txy.entity.Npc["tags"]) */
  const tail = msg.match(/\["([\w$]+)"\]\s*\)?\s*$/);
  if (tail) return tail[1];                       // ★ 用捕获组 m[1]，不能用最后一个元素
  const all = msg.match(/\["([\w$]+)"\]/g);
  if (all && all.length){
    const mm = all[all.length-1].match(/\["([\w$]+)"\]/);
    if (mm) return mm[1];
  }
  return null;
}

/* 带"数组→字符串自动修正"的写请求。
 * 注意：一次请求里可能有多个字段类型不匹配（先报 tags、修好后又报 quotes），
 * 所以这里循环重试（最多 4 次），而不是只修一个字段就放弃。 */
async function writeWith(resource, method, path, payload){
  const send = p => (method === 'POST') ? post(path, p) : put(path, p);
  let cur = Object.assign({}, payload);
  for (let attempt = 0; attempt < 4; attempt++){
    try{
      return await send(serializeFor(resource, cur));
    }catch(e){
      if (!API.autoFix) throw e;
      const f = fieldFromErr(e && e.message);
      if (!f || !Array.isArray(cur[f])) throw e;
      /* 记下这个字段，后续请求直接按字符串发，不再试错 */
      API.listFields[resource] = API.listFields[resource] || {};
      API.listFields[resource][f] = (f === 'quotes') ? '\n' : API.listSep;
      const sep = API.listFields[resource][f];
      console.warn('[api] 字段 ' + f + ' 后端是 String，已自动转为字符串重试（分隔符 ' +
                   JSON.stringify(sep) + '）');
      cur = Object.assign({}, cur);
      cur[f] = cur[f].join(sep);
    }
  }
  /* 兜底：还有未解决的字段，直接发最后一次 */
  return await send(serializeFor(resource, cur));
}

/* ---------- 批量同步：把后端数据灌进前端 DB ---------- */
/* 只写数据字段，不动 DB 原型上的方法（与 data.js 的 hydrate 同思路） */
/* ---------- 认证 / 角色 / 日志 ---------- */
API.auth = {
  /* 登录：返回 {token,user,name,role,roleName,perms,expireAt} */
  login    : (user, pass) => post('/auth/login', { user: user, pass: pass })
                                .then(r => { if (r && r.token) setToken(r.token); return r; }),
  logout   : ()           => post('/auth/logout').then(r => { setToken(''); return r; })
                                .catch(() => { setToken(''); }),
  current  : ()           => get('/auth/current'),
  perms    : ()           => get('/auth/perms'),
  /* 改密码：{oldPass, newPass}；成功后后端会踢掉其它会话 */
  changePwd: (oldPass, newPass) => post('/auth/password', { oldPass: oldPass, newPass: newPass }),
  sessions : ()           => get('/auth/sessions'),
  kick     : token        => del('/auth/sessions/' + encodeURIComponent(token))
};
API.setToken   = setToken;
API.getToken   = getToken;
API.hasToken   = () => !!getToken();

/* 图片上传：返回可直接使用的 URL
 * ⚠️ 后端路径已从 /web/upload/image 改为 /api/upload/image（纳入登录校验） */
API.upload = {
  image: async function(file){
    if (typeof FormData === 'undefined') throw new Error('当前环境不支持文件上传');
    const fd = new FormData();
    fd.append('file', file);
    const t = getToken();
    const res = await fetch(BASE + '/upload/image', {
      method: 'POST',
      credentials: 'same-origin',
      headers: t ? { 'X-Token': t } : {},     // ★ 不带 Content-Type，让浏览器自动填 boundary
      body: fd
    });
    const json = await res.json().catch(()=>null);
    if (!res.ok){
      if (res.status === 401){ try{ API.onUnauthorized('登录已失效'); }catch(e){}
        const e1 = new Error('登录已失效'); e1.code = 401; throw e1; }
      if (res.status === 403){ const e2 = new Error('没有上传权限'); e2.code = 403; throw e2; }
      throw new Error((json && (json.msg || json.message)) || ('上传失败（HTTP ' + res.status + '）'));
    }
    const d = (json && Object.prototype.hasOwnProperty.call(json, 'data')) ? json.data : json;
    const url = (d && (d.url || d.data)) || null;
    if (!url) throw new Error('上传响应缺少 url 字段');
    return String(url);
  }
};

API.roles = {
  list          : ()        => get('/roles'),
  perms         : ()        => get('/roles/perms'),
  permsGrouped  : ()        => get('/roles/perms/grouped'),
  create        : r         => post('/roles', r),
  update        : (key, r)  => put('/roles/' + encodeURIComponent(key), r),
  remove        : key       => del('/roles/' + encodeURIComponent(key))
};

API.audit = {
  /* 返回 {rows,total,page,size} */
  list  : (params, page, size) => get('/audit', Object.assign({}, params || {},
            { page: page || 1, size: size || 20 })),
  clean : days => del('/audit/clean', { days: days || 90 })
};

API.pull = {
  /* 后端 tags 可能是逗号字符串 → 统一还原成数组，页面渲染逻辑不用改 */
  npcs: async () => {
    const l = await API.npcs.list();
    return Array.isArray(l) ? l.map(x=>deserializeFor('npcs', x)) : [];
  },
  shows: async () => { const l = await API.shows.list(); return Array.isArray(l) ? l : []; },
  orders: async () => { const l = await API.orders.list(); return Array.isArray(l) ? l : []; },
  notices: async () => { const l = await API.notices.list(); return Array.isArray(l) ? l : []; },
  players: async () => { const l = await API.players.list(); return Array.isArray(l) ? l : []; },
  settings: async () => { const s = await API.settings.get(); return (s && typeof s === 'object') ? s : null; },
  statusOptions: async () => { const l = await API.orders.statusOptions(); return Array.isArray(l) ? l : []; },
  stats: async () => { const s = await API.dashboard.stats(); return (s && typeof s === 'object') ? s : null; },
  /* 抽卡奖池：整池拉取（含 cards）。后端无该接口时返回 null，页面显示空态 */
  gacha: async () => {
    const l = await API.gacha.listPools();
    return Array.isArray(l) ? l : [];
  },
  gachaRarity: async () => {
    const r = await API.gacha.rarity();
    return (r && typeof r === 'object') ? r : null;
  },
  /* 亲密度：按 NPC 维度逐个拉取，重建「玩家 × NPC」矩阵 */
  intimacy: async (npcIds, players) => {
    /* 先探测一次 /intimacy/rank：即使后端还没有 NPC（npcIds 为空），
     * 也能真实反映"亲密度接口通不通"，否则会被误判成同步成功。 */
    await API.intimacy.rank();
    let fail = 0;
    const res = await Promise.all(npcIds.map(id =>
      API.intimacy.byNpc(id).catch(()=>{ fail++; return []; })
    ));
    // 全部失败说明接口根本不通，必须抛错，否则会被前端误判为"同步成功"
    if (npcIds.length && fail === npcIds.length) throw new Error('亲密度接口不可用：/intimacy/by-npc');
    const map = {};   // playerId -> { npcId: value }
    const meta = {};  // playerId -> { name, avatar, tag }
    players.forEach(p => { map[p.id] = {}; });
    npcIds.forEach((nid, i) => {
      (res[i] || []).forEach(r => {
        const pid = r.playerId; if (!pid) return;
        if (!map[pid]) map[pid] = {};
        map[pid][nid] = r.value;
        if (!meta[pid]) meta[pid] = { name: r.playerName || pid, avatar: r.avatar, tag: r.tag };
      });
    });
    return { map: map, meta: meta };
  }
};

/* R.data 里的字段名可能不同，做一层宽松映射 */
function normStats(s){
  if (!s || typeof s !== 'object') return {};
  const n = (keys, def) => {
    for (const k of keys){
      if (typeof s[k] === 'number') return s[k];
      const u = k.replace(/([A-Z])/g, '_$1').toLowerCase();
      if (typeof s[u] === 'number') return s[u];
    }
    return def;
  };
  return {
    npc       : n(['npc','npcCount','npcTotal'], undefined),
    show      : n(['show','showCount','showTotal'], undefined),
    showOnSale: n(['showOnSale','onSale'], undefined),
    order     : n(['order','orderCount','orderTotal'], undefined),
    paid      : n(['paid','paidCount','paidOrder'], undefined),
    revenue   : n(['revenue','income','totalRevenue'], undefined),
    player    : n(['player','playerCount','userCount'], undefined),
    seats     : n(['seats','taken','seatTaken'], undefined),
    cap       : n(['cap','capacity','seatTotal'], undefined),
    rate      : n(['rate','seatRate','occupancy'], undefined),
    notice    : n(['notice','noticeCount'], undefined),
    intimacy  : n(['intimacy','intimacyTotal'], undefined),
    _raw      : s
  };
}
API.normStats = normStats;

/* 暴露给业务层（app.js）做字段容错 */
API.helpers = { pick: pick, asArray: asArray, normRow: normRow };

global.API = API;

})(typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : {}));
