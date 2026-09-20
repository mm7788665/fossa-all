package com.txy.controller;

import com.txy.common.R;
import com.txy.entity.*;
import com.txy.mapper.GachaMiniMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 小程序 · 抽卡（路径 /fossa/api/mp/gacha/**）
 *
 * ★ 与后台管理共用同一套 gacha_pool / gacha_card / gacha_rarity 表与算法，
 *   后台改权重/保底/出货窗口，小程序立即生效。
 *
 * 接口：
 *   GET  /mp/gacha/pools          奖池列表（游客可读）
 *   GET  /mp/gacha/pool/detail    奖池详情（含每张卡实时概率，概率公示页用）
 *   GET  /mp/gacha/rarity         稀有度定义（名称/颜色/概率）
 *   POST /mp/gacha/draw           抽卡（★ 兼容老参数 group/count 与新参数 poolId/times）
 *   GET  /mp/gacha/cards          我的卡牌
 *   GET  /mp/gacha/pity           保底进度
 *   GET  /mp/gacha/records        抽卡记录
 */
@RestController
@RequestMapping("/mp/gacha")
public class MpGachaController {

    @Autowired private GachaMiniMapper mapper;

    /* ==================== 奖池 ==================== */

    @GetMapping("/pools")
    public R<List<Map<String, Object>>> pools(){
        List<GachaPool> ps;
        try{ ps = mapper.selectOpenPools(); }catch(Exception e){ ps = new ArrayList<>(); }
        if (ps == null) ps = new ArrayList<>();
        List<Map<String, Object>> out = new ArrayList<>();
        for (GachaPool p : ps){
            List<GachaCard> cards = safeCards(p.getId());
            out.add(toPool(p, cards));
        }
        return R.ok(out);
    }

    @GetMapping("/pool/detail")
    public R<Map<String, Object>> poolDetail(@RequestParam(required = false) String poolId,
                                             @RequestParam(required = false) String id){
        String pid = poolId != null && !poolId.isEmpty() ? poolId : id;
        if (pid == null || pid.isEmpty()){
            List<GachaPool> ps = mapper.selectOpenPools();
            if (ps == null || ps.isEmpty()) return R.fail(400, "暂无可用奖池");
            pid = ps.get(0).getId();
        }
        GachaPool p = mapper.selectOpenPoolById(pid);
        if (p == null) return R.fail(400, "奖池不存在或未启用");
        return R.ok(toPool(p, safeCards(pid)));
    }

    @GetMapping("/rarity")
    public R<List<Map<String, Object>>> rarity(){
        List<GachaRarity> rs;
        try{ rs = mapper.selectRarity(); }catch(Exception e){ rs = new ArrayList<>(); }
        if (rs == null) rs = new ArrayList<>();
        List<Map<String, Object>> out = new ArrayList<>();
        for (GachaRarity r : rs){
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rarityKey",  r.getRarityKey());
            m.put("rarityName", r.getRarityName());
            m.put("label",      r.getLabel());
            m.put("color",      r.getColor());
            m.put("rate",       r.getRate());
            m.put("sortOrder",  r.getSortOrder());
            out.add(m);
        }
        return R.ok(out);
    }

    /* ==================== 抽卡 ==================== */

    public static class DrawReq {
        private String poolId;
        private String group;      // 老参数：self / npc
        private Integer times;
        private Integer count;     // 老参数
        public String getPoolId(){ return poolId; }
        public void setPoolId(String v){ this.poolId = v; }
        public String getGroup(){ return group; }
        public void setGroup(String v){ this.group = v; }
        public Integer getTimes(){ return times; }
        public void setTimes(Integer v){ this.times = v; }
        public Integer getCount(){ return count; }
        public void setCount(Integer v){ this.count = v; }
    }

    /**
     * 抽卡
     * 兼容两种调用：
     *   新：{ poolId:'P001', times:10 }
     *   老：{ group:'self', count:1 }   ← group 按名字模糊匹配奖池，匹配不到就用第一个
     */
    @PostMapping("/draw")
    public R<Map<String, Object>> draw(@RequestBody(required = false) DrawReq req){
        String userId = MiniContext.userId();
        if (userId == null || userId.isEmpty()) return R.fail(401, "请先登录");

        int times = 1;
        if (req != null){
            if (req.getTimes() != null && req.getTimes() > 0) times = req.getTimes();
            else if (req.getCount() != null && req.getCount() > 0) times = req.getCount();
        }
        if (times != 1 && times != 10) times = (times > 10 ? 10 : 1);

        /* 定位奖池 */
        GachaPool pool = null;
        if (req != null && req.getPoolId() != null && !req.getPoolId().isEmpty()){
            pool = mapper.selectOpenPoolById(req.getPoolId());
        }
        if (pool == null && req != null && req.getGroup() != null && !req.getGroup().isEmpty()){
            pool = matchPoolByGroup(req.getGroup());
        }
        if (pool == null){
            List<GachaPool> ps = mapper.selectOpenPools();
            if (ps == null || ps.isEmpty()) return R.fail(400, "暂无可用奖池");
            pool = ps.get(0);
        }

        final String poolId = pool.getId();
        List<GachaCard> cards = safeCards(poolId);
        if (cards.isEmpty()) return R.fail(400, "该奖池暂无卡牌");

        /* 保底状态 */
        GachaUserPity pity = mapper.selectPity(userId, poolId);
        if (pity == null){
            pity = new GachaUserPity();
            pity.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 24));
            pity.setUserId(userId);
            pity.setPoolId(poolId);
            pity.setSsrCount(0); pity.setSrCount(0);
            pity.setTotalCount(0); pity.setSsrTotal(0);
            mapper.insertPity(pity);
        }

        /* 稀有度概率表 */
        Map<String, Double> rateMap = new LinkedHashMap<>();
        try{
            List<GachaRarity> rs = mapper.selectRarity();
            if (rs != null) for (GachaRarity r : rs){
                rateMap.put(String.valueOf(r.getRarityKey()).toUpperCase(),
                            r.getRate() == null ? 0.0 : r.getRate());
            }
        }catch(Exception ignore){}
        if (rateMap.isEmpty()){ rateMap.put("SSR", 2.0); rateMap.put("SR", 12.0);
                                rateMap.put("R", 36.0);  rateMap.put("N", 50.0); }

        Set<String> owned = new HashSet<>();
        try{ Set<String> o = mapper.selectOwnedCardIds(userId); if (o != null) owned = o; }
        catch(Exception ignore){}

        List<GachaDrawRecord> records = new ArrayList<>();
        List<Map<String, Object>> items = new ArrayList<>();
        String batchNo = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        int ssrCount = nz(pity.getSsrCount());
        int srCount  = nz(pity.getSrCount());
        int total    = nz(pity.getTotalCount());

        int pitySsr = pool.getPitySsr() == null ? 0 : pool.getPitySsr();
        int pitySr  = pool.getPitySr()  == null ? 0 : pool.getPitySr();
        if (pity.getPitySsrOverride() != null && pity.getPitySsrOverride() > 0) pitySsr = pity.getPitySsrOverride();
        if (pity.getPitySrOverride()  != null && pity.getPitySrOverride()  > 0) pitySr  = pity.getPitySrOverride();
        /* 出货窗口起点：0=不限 */
        int ssrStart = pool.getPitySsrStart() == null ? 0 : pool.getPitySsrStart();
        if (pity.getPitySsrStartOverride() != null && pity.getPitySsrStartOverride() > 0){
            ssrStart = pity.getPitySsrStartOverride();
        }
        if (ssrStart > pitySsr && pitySsr > 0) ssrStart = pitySsr;   // 起点不能超过保底
        /* 最低门槛 */
        int minDraws = pool.getMinSsrDraws() == null ? 0 : pool.getMinSsrDraws();
        if (pity.getMinSsrDraws() != null && pity.getMinSsrDraws() > 0) minDraws = pity.getMinSsrDraws();

        for (int i = 0; i < times; i++){
            total++; ssrCount++; srCount++;
            final int curTotal = total;

            boolean isPity = false;
            String rarity;

            boolean locked = (minDraws > 0 && curTotal < minDraws);   // 门槛未达成
            List<String> cand = new ArrayList<>(rateMap.keySet());
            if (locked) cand.remove("SSR");                            // 门槛内不可能出 SSR

            /* ★ 保底也要受门槛约束：门槛未达成时，即使到了保底抽数也不能出 SSR，
             *   否则"最低抽数"会被保底击穿（门槛100+保底50 会在第50抽就出货）。 */
            if (pitySsr > 0 && ssrCount >= pitySsr && !locked){
                rarity = "SSR"; isPity = true;                         // 保底必出
            } else if (pitySr > 0 && srCount >= pitySr){
                rarity = "SR";  isPity = true;
            } else {
                rarity = randomRarity(cand, rateMap);
                if ("SSR".equals(rarity) && locked) rarity = "SR";     // 双保险
                if ("SSR".equals(rarity) && ssrStart > 0 && ssrCount < ssrStart){
                    rarity = "SR";                                     // 出货窗口未到
                }
            }

            GachaCard card = pickCard(cards, rarity, pool);
            if (card == null) continue;

            boolean isNew = !owned.contains(card.getId());
            if (isNew) owned.add(card.getId());

            /* 入库：持有 + 记录 */
            try{
                GachaUserCard uc = mapper.selectUserCard(userId, card.getId());
                if (uc == null){
                    uc = new GachaUserCard();
                    uc.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 24));
                    uc.setUserId(userId);
                    uc.setCardId(card.getId());
                    uc.setPoolId(poolId);
                    uc.setCount(1);
                    uc.setRarity(card.getRarity());
                    uc.setFirstAt(LocalDateTime.now());
                    uc.setUpdatedAt(LocalDateTime.now());
                    mapper.insertUserCard(uc);
                } else {
                    mapper.incrUserCard(userId, card.getId());
                }
                if (card.getStock() != null && card.getStock() > 0) mapper.decrStock(card.getId());
            }catch(Exception ignore){}

            GachaDrawRecord rec = new GachaDrawRecord();
            rec.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 24));
            rec.setUserId(userId);
            rec.setPoolId(poolId);
            rec.setCardId(card.getId());
            rec.setCardName(card.getName());
            rec.setRarity(card.getRarity());
            rec.setBatchNo(batchNo);
            rec.setIsPity(isPity ? 1 : 0);
            rec.setCreatedAt(LocalDateTime.now());
            records.add(rec);

            Map<String, Object> it = new LinkedHashMap<>();
            it.put("id", card.getId());
            it.put("name", card.getName());
            it.put("rarity", card.getRarity());
            it.put("rarityKey", card.getRarity());
            it.put("rarityLabel", rarityLabel(card.getRarity()));
            it.put("rarityColor", rarityColor(card.getRarity()));
            it.put("image", card.getImage() == null ? "" : card.getImage());
            it.put("thumb", card.getThumb() == null ? "" : card.getThumb());
            it.put("tag", rarityLabel(card.getRarity()));
            it.put("npcId", card.getNpcId() == null ? "" : card.getNpcId());
            it.put("isNew", isNew);
            it.put("isPity", isPity);
            items.add(it);

            if ("SSR".equals(rarity)){ ssrCount = 0; srCount = 0; }
            else if ("SR".equals(rarity)){ srCount = 0; }
        }

        if (!records.isEmpty()){
            try{ mapper.batchInsertRecords(records); }catch(Exception ignore){}
        }

        pity.setSsrCount(ssrCount);
        pity.setSrCount(srCount);
        pity.setTotalCount(total);
        pity.setUpdatedAt(LocalDateTime.now());
        try{ mapper.updatePity(pity); }catch(Exception ignore){}

        Map<String, Object> pityOut = new LinkedHashMap<>();
        pityOut.put("ssrCount", ssrCount);
        pityOut.put("ssrPity", pitySsr);
        pityOut.put("ssrRemain", Math.max(0, pitySsr - ssrCount));
        pityOut.put("srCount", srCount);
        pityOut.put("srPity", pitySr);
        pityOut.put("srRemain", Math.max(0, pitySr - srCount));
        pityOut.put("totalCount", total);
        pityOut.put("locked", minDraws > 0 && total < minDraws);
        pityOut.put("unlockRemain", Math.max(0, minDraws - total));
        pityOut.put("personal", pity.getPitySsrOverride() != null && pity.getPitySsrOverride() > 0);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("pity", pityOut);
        return R.ok(out);
    }

    /* ==================== 我的 ==================== */

    @GetMapping("/cards")
    public R<List<Map<String, Object>>> myCards(@RequestParam(required = false) String rarity){
        String userId = MiniContext.requireUserId();
        List<GachaUserCard> list;
        try{ list = mapper.selectUserCards(userId, rarity); }catch(Exception e){ list = new ArrayList<>(); }
        if (list == null) list = new ArrayList<>();
        List<Map<String, Object>> out = new ArrayList<>();
        for (GachaUserCard c : list){
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getCardId());
            m.put("cardId", c.getCardId());
            m.put("name", c.getCardName());
            m.put("rarity", c.getRarity());
            m.put("rarityKey", c.getRarity());
            m.put("rarityLabel", rarityLabel(c.getRarity()));
            m.put("rarityColor", rarityColor(c.getRarity()));
            m.put("count", c.getCount());
            out.add(m);
        }
        return R.ok(out);
    }

    @GetMapping("/pity")
    public R<Map<String, Object>> pity(@RequestParam(required = false) String poolId){
        String userId = MiniContext.requireUserId();
        String pid = poolId;
        if (pid == null || pid.isEmpty()){
            List<GachaPool> ps = mapper.selectOpenPools();
            if (ps == null || ps.isEmpty()) return R.fail(400, "暂无奖池");
            pid = ps.get(0).getId();
        }
        GachaPool pool = mapper.selectOpenPoolById(pid);
        GachaUserPity p = null;
        try{ p = mapper.selectPity(userId, pid); }catch(Exception ignore){}
        int ssrCount = p == null ? 0 : nz(p.getSsrCount());
        int srCount  = p == null ? 0 : nz(p.getSrCount());
        int total    = p == null ? 0 : nz(p.getTotalCount());
        int pitySsr = pool == null || pool.getPitySsr() == null ? 0 : pool.getPitySsr();
        int pitySr  = pool == null || pool.getPitySr()  == null ? 0 : pool.getPitySr();
        if (p != null){
            if (p.getPitySsrOverride() != null && p.getPitySsrOverride() > 0) pitySsr = p.getPitySsrOverride();
            if (p.getPitySrOverride()  != null && p.getPitySrOverride()  > 0) pitySr  = p.getPitySrOverride();
        }
        int minDraws = pool == null || pool.getMinSsrDraws() == null ? 0 : pool.getMinSsrDraws();
        if (p != null && p.getMinSsrDraws() != null && p.getMinSsrDraws() > 0) minDraws = p.getMinSsrDraws();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ssrCount", ssrCount);
        m.put("ssrPity", pitySsr);
        m.put("ssrRemain", Math.max(0, pitySsr - ssrCount));
        m.put("srCount", srCount);
        m.put("srPity", pitySr);
        m.put("srRemain", Math.max(0, pitySr - srCount));
        m.put("totalCount", total);
        m.put("locked", minDraws > 0 && total < minDraws);
        m.put("unlockRemain", Math.max(0, minDraws - total));
        m.put("personal", p != null && p.getPitySsrOverride() != null && p.getPitySsrOverride() > 0);
        return R.ok(m);
    }

    @GetMapping("/records")
    public R<List<Map<String, Object>>> records(@RequestParam(required = false) String poolId,
                                                @RequestParam(required = false, defaultValue = "20") int limit){
        String userId = MiniContext.requireUserId();
        List<GachaDrawRecord> list;
        try{ list = mapper.selectRecords(userId, poolId, Math.min(limit, 100)); }
        catch(Exception e){ list = new ArrayList<>(); }
        if (list == null) list = new ArrayList<>();
        List<Map<String, Object>> out = new ArrayList<>();
        for (GachaDrawRecord r : list){
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("cardId", r.getCardId());
            m.put("cardName", r.getCardName());
            m.put("rarity", r.getRarity());
            m.put("isPity", r.getIsPity() != null && r.getIsPity() == 1);
            m.put("createdAt", r.getCreatedAt() == null ? "" : r.getCreatedAt().toString());
            out.add(m);
        }
        return R.ok(out);
    }

    /* ==================== 辅助 ==================== */

    private List<GachaCard> safeCards(String poolId){
        try{ List<GachaCard> l = mapper.selectCards(poolId); return l == null ? new ArrayList<>() : l; }
        catch(Exception e){ return new ArrayList<>(); }
    }

    private GachaPool matchPoolByGroup(String group){
        try{
            List<GachaPool> ps = mapper.selectOpenPools();
            if (ps == null) return null;
            for (GachaPool p : ps){
                if (p.getId() != null && p.getId().equalsIgnoreCase(group)) return p;
            }
            for (GachaPool p : ps){
                if (p.getName() != null && p.getName().contains(group)) return p;
            }
            return ps.isEmpty() ? null : ps.get(0);
        }catch(Exception e){ return null; }
    }

    private String randomRarity(List<String> cand, Map<String, Double> rateMap){
        double sum = 0;
        for (String k : cand) sum += rateMap.getOrDefault(k, 0.0);
        if (sum <= 0) return cand.isEmpty() ? "N" : cand.get(0);
        double r = Math.random() * sum;
        for (String k : cand){
            r -= rateMap.getOrDefault(k, 0.0);
            if (r <= 0) return k;
        }
        return cand.get(cand.size() - 1);
    }

    private GachaCard pickCard(List<GachaCard> cards, String rarity, GachaPool pool){
        List<GachaCard> same = new ArrayList<>();
        for (GachaCard c : cards){
            if (c == null) continue;
            if (!rarity.equalsIgnoreCase(String.valueOf(c.getRarity()))) continue;
            if (c.getStock() != null && c.getStock() == 0) continue;     // 无库存跳过
            same.add(c);
        }
        if (same.isEmpty()){                                             // 该稀有度无卡 → 降级到全部有库存的卡
            List<GachaCard> all = new ArrayList<>();
            for (GachaCard c : cards){
                if (c == null) continue;
                if (c.getStock() != null && c.getStock() == 0) continue;
                all.add(c);
            }
            if (all.isEmpty()){
                /* ★ 全部售罄：不能返回 null（否则玩家扣了券却什么都没抽到）。
                 *   改为忽略库存限制继续出卡 —— 抽卡系统应保证"每一抽都有产出"。 */
                all = new ArrayList<>();
                for (GachaCard c : cards){ if (c != null) all.add(c); }
            }
            if (all.isEmpty()) return null;                                // 奖池一张卡都没有，真的没救
            same = all;
        }
        /* 按权重（UP 加成）随机 */
        double bonus = pool.getUpBonus() == null ? 1.0 : pool.getUpBonus().doubleValue();
        String upNpc = pool.getUpNpcId();
        double sum = 0;
        for (GachaCard c : same){
            double w = c.getWeight() == null ? 1 : c.getWeight();
            if (upNpc != null && !upNpc.isEmpty()
                && c.getNpcId() != null && upNpc.equals(c.getNpcId())) w *= bonus;
            sum += Math.max(0, w);
        }
        if (sum <= 0) return same.get(0);
        double r = Math.random() * sum;
        for (GachaCard c : same){
            double w = c.getWeight() == null ? 1 : c.getWeight();
            if (upNpc != null && !upNpc.isEmpty()
                && c.getNpcId() != null && upNpc.equals(c.getNpcId())) w *= bonus;
            r -= Math.max(0, w);
            if (r <= 0) return c;
        }
        return same.get(same.size() - 1);
    }

    private Map<String, Object> toPool(GachaPool p, List<GachaCard> cards){
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("currency", p.getCurrency() == null ? "契约券" : p.getCurrency());
        m.put("costSingle", p.getCostSingle() == null ? 1 : p.getCostSingle());
        m.put("costTen", p.getCostTen() == null ? 10 : p.getCostTen());
        m.put("pitySsr", p.getPitySsr() == null ? 0 : p.getPitySsr());
        m.put("pitySr", p.getPitySr() == null ? 0 : p.getPitySr());
        m.put("upNpcId", p.getUpNpcId() == null ? "" : p.getUpNpcId());
        m.put("upBonus", p.getUpBonus() == null ? 1 : p.getUpBonus().doubleValue());
        List<Map<String, Object>> cs = new ArrayList<>();
        for (GachaCard c : cards){
            Map<String, Object> x = new LinkedHashMap<>();
            x.put("id", c.getId());
            x.put("name", c.getName());
            x.put("rarity", c.getRarity());
            x.put("rarityKey", c.getRarity());
            x.put("rarityLabel", rarityLabel(c.getRarity()));
            x.put("rarityColor", rarityColor(c.getRarity()));
            x.put("image", c.getImage() == null ? "" : c.getImage());
            x.put("thumb", c.getThumb() == null ? "" : c.getThumb());
            x.put("tag", "");
            x.put("npcId", c.getNpcId() == null ? "" : c.getNpcId());
            x.put("weight", c.getWeight() == null ? 0 : c.getWeight());
            x.put("stock", c.getStock() == null ? -1 : c.getStock());
            x.put("isUp", c.getIsUp() != null && c.getIsUp() == 1);
            cs.add(x);
        }
        m.put("cards", cs);
        return m;
    }

    private static String rarityLabel(String k){
        String s = String.valueOf(k == null ? "" : k).toUpperCase();
        switch (s){
            case "SSR": return "特典";
            case "SR":  return "稀有";
            case "R":   return "精良";
            default:    return "普通";
        }
    }
    private static String rarityColor(String k){
        String s = String.valueOf(k == null ? "" : k).toUpperCase();
        switch (s){
            case "SSR": return "#f59e0b";
            case "SR":  return "#a855f7";
            case "R":   return "#3b82f6";
            default:    return "#9ca3af";
        }
    }
    private static int nz(Integer v){ return v == null ? 0 : v; }
}
