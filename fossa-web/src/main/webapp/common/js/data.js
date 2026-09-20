/* ============================================================
 * 灰塔之下 · 管理后台 数据层 (data.js)
 * 内容完全来自小程序：npcs.js / intimacy.js 真实数据 + 后台业务数据
 * 持久化：localStorage（刷新不丢，模拟后端）
 * ============================================================ */
(function(global){
'use strict';

const KEY = 'huita_admin_db_v1';

/* ---------- 1. NPC 角色（小程序 npcs.js 真实数据） ---------- */
const NPCS = [
  { id:'linshen', name:'林深', icon:'🌑', cover:'common/assets/npcs/linshen.png', role:'逃亡实验体 · 破晓反抗军', faction:'破晓反抗军', campLabel:'破晓', power:'痛觉共享', tags:['青梅竹马','逃亡者','温柔危险'],
    desc:'全世界最温柔的人，有着全世界最危险的秘密。他在灰塔地下被关了两年，黑暗是他的日常。逃出后的487天，他每天都在想见到你第一句话该说什么。',
    quotes:['哪怕变成石头，只要我的心还在竭力跳动，我就还在爱你。','我数过了。你从门口走到我这里，正好十一步。和以前一样。'],
    hooks:{ action:'数数。他数秒、数你的呼吸、数从门口走到你面前的步数。嘴唇偶尔在动——不是在自言自语，是在数。', contrast:'全世界最温柔的人，有着全世界最危险的秘密。他知道举报你是谁，但永远不会说。', fragile:'他的手套破了一个洞，露出灰白色的结晶皮肤。你想碰，他缩了一下，又把手伸回来。', speech:'话少，但每一句都重。声音比想象中低，偏着头从低处看你。' },
    status:'启用', sort:1 },
  { id:'shenzhe', name:'沈铎', icon:'⚜️', cover:'common/assets/npcs/shenzhe.png', role:'灰塔执行官 · W 的竹马', faction:'灰塔官方派', campLabel:'灰塔', power:'秩序锁定', tags:['追妻火葬场','强硬派','克制失控'],
    desc:'灰塔最冷酷高效的外勤执行官，专门追捕异能者。但真正该被抓的人是自己——每次见到W，他都在用全部意志力克制不要当众失控。',
    quotes:['你可以恨我。但你得活着恨我。','我抓过很多人。唯独一个人，我永远抓不住，也永远不该抓。'],
    hooks:{ action:'摸自己的左手无名指。那里有一小片矿化——恰好长在戴婚戒的位置。', contrast:'冷酷高效的追捕者，却是全场最失控的人。', fragile:'W和陆征站在一起的时候，他看别的地方。但左手拇指在疯狂摩擦那块结晶。', speech:'句子短，尾音沉。从不解释，只陈述。' },
    status:'启用', sort:2 },
  { id:'guyan', name:'顾衍', icon:'📿', cover:'common/assets/npcs/guyan.png', role:'灰塔实验员 · 动摇派', faction:'灰塔官方派', campLabel:'灰塔', power:'记忆读取', tags:['初恋白月光','道德深渊','温柔残忍'],
    desc:'他有一张最温柔的脸，做着一件最残忍的事——每天从活人身上提取体液制成稳定剂。信佛，抽屉里有一串念珠，每次做完提取会念一遍。',
    quotes:['我认出你了。从档案里。但我没说破……我有什么资格说。','你是不是也觉得，温柔可以用来赎罪？'],
    hooks:{ action:'碰东西之前会先缩手。唯独对玩家，他不缩。', contrast:'最温柔的脸，做最残忍的事。温柔不是伪装，温柔是他的铠甲。', fragile:'林深摘下手套，顾衍看到了人造结晶。他把手缩了回去，像被自己打了一巴掌。', speech:'句子很长，中间带很多"……"。说到一半会沉默。' },
    status:'启用', sort:3 },
  { id:'subai', name:'苏白', icon:'🪶', cover:'common/assets/npcs/subai.png', role:'完美异能者 · 零号', faction:'无阵营', campLabel:'密', power:'生命共鸣', tags:['零号','社会常识为零','纯真抄作业'],
    desc:'灰塔实验室里长大的"完美样本"——异能强大、零矿化。他不会开门、不会系鞋带。他能感知你的难过，然后模仿你的表情——不是在演，是他的身体在抄作业。',
    quotes:['你给我买了一根冰棍。那是全世界最甜的东西。','利用我吧。我不在乎被利用。只要是你。'],
    hooks:{ action:'他碰所有东西。不是顾衍那种缩手——苏白是不缩手。', contrast:'全场最强的异能者，也是最没用的人。', fragile:'他第一次自己打开了一瓶水。看了瓶盖很久，然后抬头看你，笑了。', speech:'短句，直球，没有修饰词。他不会开玩笑，但经常"说错话"。' },
    status:'启用', sort:4 },
  { id:'jiangyu', name:'江屿', icon:'🎭', cover:'common/assets/npcs/jiangyu.png', role:'破晓反抗军 · 情报员', faction:'破晓反抗军', campLabel:'破晓', power:'拟态模仿', tags:['阴湿病娇','多重人格','伪装者'],
    desc:'潜伏在灰塔内部的破晓情报员，能完美复制任何人的声音和外貌3分钟。他在匿名论坛认识了玩家，是任务开始，却陷进去了。',
    quotes:['宝宝，你比我想象中还要可爱……不要讨厌我好不好？','我可以变成任何人的样子。但你说好看的那个，到底是我？'],
    hooks:{ action:'他哼歌。永远在哼，声音很小。紧张时哼得快，放松时哼得慢。', contrast:'他能变成任何人，却不知道自己是谁。', fragile:'拟态失效了。三分钟到了，他扮演的"监管员"的脸开始融化。', speech:'甜到腻，尾音往上飘。但甜话后面都藏着另一个版本。' },
    status:'启用', sort:5 },
  { id:'luzheng', name:'陆征', icon:'🪞', cover:'common/assets/npcs/luzheng.png', role:'集会召集人 · W 的丈夫（X）', faction:'无阵营', campLabel:'观察', power:'情绪共振', tags:['X','全局观察者','阴暗三角'],
    desc:'今晚集会的召集人、公共戏主持、全场最冷静的人。他能感知半径10米内所有人的情绪波动，却控制着自己的。他把W和沈铎分到同一组。',
    quotes:['我组织这场集会，名目是公开讨论稳定剂。但你知道我真正想要的是什么吗？','结婚五年。我第一次看你这样笑。是因为他吗？'],
    hooks:{ action:'不停摘戒指。左手无名指上有一枚素圈婚戒。感觉到W情绪波动时会转戒指。', contrast:'全场的召集人、最冷静的人。却同时感知着W的开心、沈铎的开心、自己的痛苦。', fragile:'独自站在角落，手里握着终于摘下来的戒指，肩膀在抖。', speech:'声线低沉平稳，像机场广播。对W说话时语气会突然变软。' },
    status:'启用', sort:6 }
];

/* ---------- 2. 亲密度（小程序 intimacy.js 真实数据） ---------- */
const INTIMACY = {
  mine:{ linshen:45, shenzhe:62, guyan:28, subai:78, jiangyu:35, luzheng:51 },
  players:[
    { id:'me', name:'我',     tag:'你',           avatar:'🎮', npc:{ linshen:45, shenzhe:62, guyan:28, subai:78, jiangyu:35, luzheng:51 } },
    { id:'p1', name:'林深',   tag:'剧本杀老手',    avatar:'🦊', npc:{ linshen:92, shenzhe:40, guyan:55, subai:60, jiangyu:48, luzheng:70 } },
    { id:'p2', name:'阿酱',   tag:'推理担当',      avatar:'🐰', npc:{ linshen:66, shenzhe:58, guyan:72, subai:44, jiangyu:80, luzheng:33 } },
    { id:'p3', name:'老周',   tag:'沉浸狂魔',      avatar:'🐻', npc:{ linshen:38, shenzhe:85, guyan:41, subai:52, jiangyu:29, luzheng:88 } },
    { id:'p4', name:'小满',   tag:'新手入坑',      avatar:'🐱', npc:{ linshen:74, shenzhe:30, guyan:25, subai:90, jiangyu:42, luzheng:38 } },
    { id:'p5', name:'陈默',   tag:'稳定剂囤积者',  avatar:'🐺', npc:{ linshen:53, shenzhe:77, guyan:68, subai:31, jiangyu:64, luzheng:45 } },
    { id:'p6', name:'阿蓝',   tag:'暗室探险家',    avatar:'🦉', npc:{ linshen:81, shenzhe:49, guyan:37, subai:67, jiangyu:73, luzheng:56 } },
    { id:'p7', name:'夜行',   tag:'灰塔逃犯',      avatar:'🐍', npc:{ linshen:88, shenzhe:35, guyan:60, subai:40, jiangyu:55, luzheng:42 } },
    { id:'p8', name:'绯樱',   tag:'月光剧院',      avatar:'🌸', npc:{ linshen:47, shenzhe:90, guyan:53, subai:58, jiangyu:61, luzheng:76 } }
  ]
};

/* ---------- 3. 场次 / 发车信息 ---------- */
const SHOWS = [
  { id:'S001', title:'灰塔之下 · 暗室共振', npc:'linshen', date:'2026-09-12', time:'19:00-22:00', city:'北京', venue:'角渡沉浸剧场（798店）', price:328, capacity:24, taken:18, status:'在售' },
  { id:'S002', title:'灰塔之下 · 秩序崩解', npc:'shenzhe', date:'2026-09-13', time:'14:00-17:00', city:'上海', venue:'磐渡戏剧工厂（静安）', price:368, capacity:20, taken:20, status:'满员' },
  { id:'S003', title:'灰塔之下 · 记忆渎取', npc:'guyan',   date:'2026-09-14', time:'19:30-22:30', city:'北京', venue:'角渡沉浸剧场（798店）', price:328, capacity:24, taken:11, status:'在售' },
  { id:'S004', title:'灰塔之下 · 纯真抄作业', npc:'subai',  date:'2026-09-19', time:'19:00-22:00', city:'广州', venue:'磐渡戏剧工厂（天河）', price:298, capacity:30, taken:7,  status:'在售' },
  { id:'S005', title:'灰塔之下 · 拟态失效', npc:'jiangyu', date:'2026-09-20', time:'19:00-22:00', city:'深圳', venue:'角渡沉浸剧场（南山）', price:328, capacity:24, taken:24, status:'满员' },
  { id:'S006', title:'灰塔之下 · 戒指与共振', npc:'luzheng', date:'2026-09-26', time:'19:00-22:00', city:'北京', venue:'角渡沉浸剧场（798店）', price:398, capacity:20, taken:5,  status:'预售' }
];

/* ---------- 4. 订单 ---------- */
const ORDERS = [
  { id:'DH20260908001', user:'小满', show:'S004', amount:298, qty:2, channel:'微信支付', created:'2026-09-08 10:24', status:'已支付' },
  { id:'DH20260908002', user:'老周', show:'S002', amount:368, qty:1, channel:'微信支付', created:'2026-09-08 11:02', status:'已支付' },
  { id:'DH20260907001', user:'阿酱', show:'S001', amount:328, qty:1, channel:'小程序',   created:'2026-09-07 20:15', status:'已退款' },
  { id:'DH20260907002', user:'陈默', show:'S003', amount:328, qty:3, channel:'微信支付', created:'2026-09-07 22:40', status:'已支付' },
  { id:'DH20260906001', user:'林深', show:'S005', amount:328, qty:2, channel:'小程序',   created:'2026-09-06 15:33', status:'待核销' },
  { id:'DH20260905001', user:'绯樱', show:'S006', amount:398, qty:1, channel:'微信支付', created:'2026-09-05 09:11', status:'已支付' },
  { id:'DH20260904001', user:'夜行', show:'S002', amount:368, qty:1, channel:'小程序',   created:'2026-09-04 18:00', status:'已核销' },
  { id:'DH20260903001', user:'阿蓝', show:'S004', amount:298, qty:2, channel:'微信支付', created:'2026-09-03 12:45', status:'待付款' }
];

/* ---------- 5. 公告 ---------- */
const NOTICES = [
  { id:'N001', title:'【重要】9月新本「戒指与共振」场次开放预约', cat:'重要', top:1, content:'陆征线全新主线场次将于9月26日首演，含隐藏结局分支，敬请期待。', author:'磐渡戏剧工厂', created:'2026-09-08', status:'发布' },
  { id:'N002', title:'关于稳定剂与异能设定的玩家须知', cat:'规则', top:0, content:'为提升沉浸体验，请玩家在入场前阅读完整世界观设定手册。', author:'admin', created:'2026-09-05', status:'发布' },
  { id:'N003', title:'「纯真抄作业」苏白线开放早鸟票', cat:'活动', top:1, content:'9月19日广州场早鸟票享9折，仅限前30名。', author:'磐渡戏剧工厂', created:'2026-09-03', status:'发布' },
  { id:'N004', title:'中秋节假期加开夜场公告', cat:'通知', top:0, content:'9月25-27日每晚加开19:00、21:30两场。', author:'admin', created:'2026-09-02', status:'草稿' }
];

/* ---------- 6. 玩家列表 ---------- */
const PLAYERS = INTIMACY.players.map(p => ({
  id:p.id, name:p.name, tag:p.tag, avatar:p.avatar,
  phone:'138****' + String(Math.floor(Math.random()*9000+1000)),
  reg:'2026-0' + (Math.floor(Math.random()*6)+2) + '-1' + Math.floor(Math.random()*5+1),
  vip: p.id==='me' || p.id==='p4' ? 'VIP' : '普通',
  status:'正常'
}));

/* ---------- 7. 管理员账号 ---------- */
const ACCOUNTS = [
  { user:'admin', pass:'123456', role:'超级管理员', name:'超级管理员' },
  { user:'editor', pass:'123456', role:'运营编辑', name:'内容编辑' }
];

/* ---------- 8. 系统设置 ---------- */
const SETTINGS = {
  storeName:'磐渡戏剧工厂', appName:'灰塔之下',
  contactPhone:'400-888-0482', contactWechat:'灰塔之下官方',
  announcement:'欢迎来到磐渡戏剧工厂 · 灰塔之下沉浸式异能剧本',
  bookStart:'提前7天', refundRule:'开场前48小时可全额退款',
  theme:'灰塔冷灰'
};

/* ---------- 9. 人气榜种子（剧情权重） ---------- */
const POPULARITY = { luzheng:96, shenzhe:94, linshen:92, subai:85, guyan:78, jiangyu:72 };

/* ===================== 数据引擎 ===================== */
/* ★ 核心：DB 是一个「数据壳对象」，业务方法挂在原型 DBproto 上。
 *  这样无论 load()/reset() 怎么替换 DB 的数据，方法永远不丢——
 *  彻底解决「DB.npcById is not a function」（JSON 反序列化会丢失方法）。 */
const DBproto = {};

function defaultData(){
  return { NPCS, SHOWS, ORDERS, NOTICES, PLAYERS, ACCOUNTS, SETTINGS, POPULARITY, INTIMACY, _seeded:true };
}

// 创建一个干净的数据壳，原型指向 DBproto（方法都在原型上）
function makeDB(){
  const db = Object.create(DBproto);
  Object.assign(db, defaultData());
  return db;
}

// 用存档数据填充壳（只覆盖数据字段，不动原型上的方法）
function hydrate(db, saved){
  const def = defaultData();
  for (const k in def){ db[k] = (saved && saved[k] !== undefined) ? saved[k] : def[k]; }
  return db;
}

let DB = makeDB();
(function init(){
  const saved = load();
  if (saved && typeof saved === 'object') hydrate(DB, saved); // 只填数据，方法不丢
  // 结构升级：补齐缺失字段
  const def = defaultData();
  for (const k in def){ if (DB[k] === undefined) DB[k] = def[k]; }
})();

function load(){ try{ const ls = typeof localStorage !== 'undefined' ? localStorage : null; const s = ls ? ls.getItem(KEY) : null; return s?JSON.parse(s):null; }catch(e){return null;} }
function save(){ try{ if (typeof localStorage !== 'undefined') localStorage.setItem(KEY, JSON.stringify(DB)); }catch(e){} }
function reset(){ localStorage.removeItem(KEY); DB = makeDB(); }

/* ===================== 业务 API（全部挂在 DBproto 上，永不丢失） ===================== */
const MAX = 100;
DBproto.levelOf = function(v){
  v=+v||0;
  if(v>=76)return{lv:4,name:'不可分割',desc:'哪怕变成石头，我也还在爱你'};
  if(v>=51)return{lv:3,name:'心跳共振',desc:'他开始把不能说的事说给你听'};
  if(v>=26)return{lv:2,name:'暗室同行',desc:'在黑暗里，他愿意让你靠近一步'};
  return{lv:1,name:'萍水相逢',desc:'你还只是档案里的一个名字'};
};
DBproto.npcById  = function(id){ return this.NPCS.find(n=>n.id===id)||null; };
DBproto.showById  = function(id){ return this.SHOWS.find(s=>s.id===id)||null; };
DBproto.orderById = function(id){ return this.ORDERS.find(o=>o.id===id)||null; };

// 统计
DBproto.stats = function(){
  const revenue = this.ORDERS.filter(o=>o.status==='已支付'||o.status==='已核销').reduce((s,o)=>s+o.amount*o.qty,0);
  const paid = this.ORDERS.filter(o=>o.status==='已支付').length;
  const seats = this.SHOWS.reduce((s,sh)=>s+sh.taken,0);
  const cap   = this.SHOWS.reduce((s,sh)=>s+sh.capacity,0);
  return {
    npc:this.NPCS.length, show:this.SHOWS.length, showOnSale:this.SHOWS.filter(s=>s.status!=='满员').length,
    order:this.ORDERS.length, paid, revenue,
    player:this.PLAYERS.length,
    seats, cap, rate: cap?Math.round(seats/cap*100):0,
    notice:this.NOTICES.filter(n=>n.status==='发布').length,
    intimacy: Object.values(this.INTIMACY.mine).reduce((a,b)=>a+b,0)
  };
};

// 排行榜
DBproto.rankPlayers = function(){
  return this.INTIMACY.players.map(p=>({...p, total:Object.values(p.npc).reduce((a,b)=>a+(b||0),0)}))
    .sort((a,b)=>b.total-a.total)
    .map((p,i)=>({...p,rank:i+1,medal:i<3?['🥇','🥈','🥉'][i]:i+1}));
};
DBproto.rankByNpc = function(npcId){
  return this.INTIMACY.players.map(p=>({...p,value:(p.npc&&p.npc[npcId])||0}))
    .filter(p=>p.value>0).sort((a,b)=>b.value-a.value)
    .map((p,i)=>({...p,rank:i+1,medal:i<3?['🥇','🥈','🥉'][i]:i+1}));
};
DBproto.rankNpcs = function(){
  return this.NPCS.map(n=>{
    const total=this.INTIMACY.players.reduce((s,p)=>s+((p.npc&&p.npc[n.id])||0),0);
    return {...n, total, pop:this.POPULARITY[n.id]||0};
  }).sort((a,b)=>b.total-a.total);
};
// NPC 人气榜（剧情权重）
DBproto.npcPopularityRank = function(){ return [...this.NPCS].map(n=>({...n,pop:this.POPULARITY[n.id]||0})).sort((a,b)=>b.pop-a.pop); };

// CRUD 通用
DBproto.addItem    = function(key, item){ this[key].push(item); save(); };
DBproto.updateItem  = function(key, id, patch){
  const i=this[key].findIndex(x=>x.id===id); if(i>=0){ this[key][i]={...this[key][i],...patch}; save(); } };
DBproto.deleteItem  = function(key, id){ this[key]=this[key].filter(x=>x.id!==id); save(); };
DBproto.nextId = function(key, prefix){
  const nums=this[key].map(x=>parseInt(String(x.id).replace(/[^0-9]/g,''))||0);
  return prefix + String((Math.max(0,...nums)+1)).padStart(3,'0');
};

// 亲密度操作
DBproto.setIntimacy = function(npcId, value){
  value=Math.max(0,Math.min(MAX,+value||0));
  this.INTIMACY.mine[npcId]=value;
  const me=this.INTIMACY.players.find(p=>p.id==='me'); if(me)me.npc[npcId]=value;
  save(); return this.levelOf(value);
};

// 持久化方法也挂到原型
DBproto.save = save;
DBproto.load = load;
DBproto.reset = reset;

// 暴露给全局（app.js 通过 window.DB 调用）
global.DB = DB;
global.HT = DB; // 兼容旧写法：HT.npcById 等同 DB.npcById

})(typeof window !== 'undefined' ? window : global);
