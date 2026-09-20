package com.txy.service;

import com.txy.dto.DrawItem;
import com.txy.dto.DrawResult;
import com.txy.entity.*;
import com.txy.mapper.GachaMiniMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 小程序抽卡业务
 *
 * 出货规则（与后台「抽卡奖池」页面配置保持一致）：
 *   1. 先判保底：累计抽数达到 pity_ssr → 必出 SSR；达到 pity_sr → 至少 SR
 *   2. 未触发保底：按 gacha_rarity.rate 随机稀有度
 *   3. 选定稀有度后，按「有效权重」在该稀有度内随机一张卡
 *      有效权重 = weight × (is_up 且命中 up_npc_id ? up_bonus : 1)
 *   4. 出货后：pity_ssr/pity_sr 计数按 reset_on_hit 决定是否清零
 *   5. 库存：stock = -1 不限量；stock = 0 该卡不再出现；> 0 出一次减一
 */
@Service
public class GachaMiniService {

    private final GachaMiniMapper mapper;
    private static final Random RND = new Random();

    public GachaMiniService(GachaMiniMapper mapper){ this.mapper = mapper; }

    /* ==================== 查询类 ==================== */

    /** 启用中的奖池列表 */
    public List<Map<String, Object>> listOpenPools(){
        List<GachaPool> pools = mapper.selectOpenPools();
        List<Map<String, Object>> out = new ArrayList<>();
        for (GachaPool p : pools){
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("currency", p.getCurrency());
            m.put("costSingle", p.getCostSingle());
            m.put("costTen", p.getCostTen());
            m.put("pitySsr", nz(p.getPitySsr()));
            m.put("pitySr", nz(p.getPitySr()));
            m.put("upNpcId", safe(p.getUpNpcId()));
            m.put("upBonus", p.getUpBonus() == null ? 2.0 : p.getUpBonus());
            m.put("cardCount", mapper.countCards(p.getId()));
            out.add(m);
        }
        return out;
    }

    /** 奖池详情 + 每张卡概率（小程序概率公示用） */
    public Map<String, Object> poolDetail(String poolId){
        GachaPool pool = mapper.selectOpenPoolById(poolId);
        if (pool == null) return null;
        List<GachaCard> cards = mapper.selectCards(poolId);
        Map<String, Double> rate = rarityRate();

        /* 每个稀有度的有效权重之和 */
        Map<String, Double> wsum = new HashMap<>();
        for (GachaCard c : cards){
            if (!inStock(c)) continue;
            wsum.merge(c.getRarity(), effWeight(pool, c), Double::sum);
        }

        List<Map<String, Object>> cardList = new ArrayList<>();
        for (GachaCard c : cards){
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("npcId", safe(c.getNpcId()));
            m.put("rarity", c.getRarity());
            m.put("weight", c.getWeight());
            m.put("isUp", c.getIsUp() == null ? 0 : c.getIsUp());
            m.put("stock", c.getStock());
            double r = rate.getOrDefault(c.getRarity(), 0d);
            double ws = wsum.getOrDefault(c.getRarity(), 0d);
            double prob = (ws > 0 && inStock(c)) ? r * effWeight(pool, c) / ws : 0d;
            m.put("prob", round2(prob));                       // 综合概率 %
            double share = ws > 0 ? effWeight(pool, c) / ws * 100 : 0;
            m.put("share", round2(share));                     // 稀有度内占比 %
            cardList.add(m);
        }

        /* 稀有度概率 */
        List<Map<String, Object>> rarityList = new ArrayList<>();
        List<GachaRarity> rs = mapper.selectRarity();
        Map<String, GachaRarity> rarityMeta = new LinkedHashMap<>();
        for (GachaRarity r : rs){ rarityMeta.put(r.getRarityKey(), r); }
        for (GachaRarity r : rs){
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", r.getRarityKey());
            m.put("name", r.getRarityName());
            m.put("label", r.getLabel());
            m.put("color", r.getColor());
            m.put("rate", round2(r.getRate() == null ? 0 : r.getRate().doubleValue()));
            int k = "SSR".equals(r.getRarityKey()) ? nz(pool.getPitySsr())
                  : "SR".equals(r.getRarityKey()) ? nz(pool.getPitySr()) : 0;
            m.put("pity", k);
            if (k > 0) m.put("expect", round2(expect(k, r.getRate() == null ? 0 : r.getRate().doubleValue())));
            rarityList.add(m);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pool", pool);
        out.put("rarity", rarityList);
        out.put("cards", cardList);
        return out;
    }

    public List<GachaUserCard> myCards(String userId, String rarity){
        return mapper.selectUserCards(userId, rarity);
    }

    public DrawResult.PityInfo myPity(String userId, String poolId){
        GachaPool pool = mapper.selectOpenPoolById(poolId);
        if (pool == null) throw new IllegalArgumentException("奖池不存在");
        GachaUserPity p = loadPity(userId, poolId);
        /* ★ 用户级覆盖优先：effectiveSsrPity 已处理 NULL=跟随奖池 */
        int ssrPity = p.effectiveSsrPity(nz(pool.getPitySsr()));
        int srPity  = p.effectiveSrPity(nz(pool.getPitySr()));
        DrawResult.PityInfo info = DrawResult.PityInfo.of(p.getSsrCount(), ssrPity,
                                      p.getSrCount(), srPity, p.getTotalCount());
        info.setSsrStart(p.effectiveSsrStart(nz(pool.getPitySsrStart())));
        info.setSsrWindowRemain(Math.max(0, p.effectiveSsrStart(nz(pool.getPitySsrStart())) - p.getSsrCount()));
        /* 最低门槛：累计抽数还没到门槛时，告诉前端"还需再抽几抽才解锁 SSR" */
        int minDraws = p.getMinSsrDraws();
        info.setMinSsrDraws(minDraws);
        info.setUnlockRemain(minDraws > 0 ? Math.max(0, minDraws - p.getTotalCount()) : 0);
        info.setLocked(minDraws > 0 && p.getTotalCount() < minDraws ? 1 : 0);
        info.setPersonal(ssrPity != nz(pool.getPitySsr()) ? 1 : 0);
        return info;
    }

    /** 设置某个用户的保底规则（运营后台用） */
    @Transactional(rollbackFor = Exception.class)
    public void setUserPity(String userId, String poolId, Integer ssrOverride,
                            Integer srOverride, Integer minDraws, String remark){
        if (mapper.selectOpenPoolById(poolId) == null) throw new IllegalArgumentException("奖池不存在");
        GachaUserPity p = loadPity(userId, poolId);
        p.setPitySsrOverride(ssrOverride == null || ssrOverride <= 0 ? null : ssrOverride);
        p.setPitySrOverride(srOverride == null || srOverride <= 0 ? null : srOverride);
        p.setMinSsrDraws(minDraws == null || minDraws < 0 ? 0 : minDraws);
        if (remark != null) p.setRemark(remark);
        if (mapper.selectPity(userId, poolId) == null) mapper.insertPity(p);
        else mapper.updatePityConfig(p);
    }

    public List<GachaDrawRecord> myRecords(String userId, String poolId, int limit){
        return mapper.selectRecords(userId, poolId, limit);
    }

    /* ==================== 抽卡 ==================== */

    @Transactional(rollbackFor = Exception.class)
    public DrawResult draw(String userId, String poolId, int times){
        GachaPool pool = mapper.selectOpenPoolById(poolId);
        if (pool == null) throw new IllegalArgumentException("奖池不存在或未启用");

        List<GachaCard> cards = mapper.selectCards(poolId);
        if (cards.isEmpty()) throw new IllegalStateException("奖池暂无卡牌");

        Map<String, Double> rate = rarityRate();
        GachaUserPity pity = loadPity(userId, poolId);
        Set<String> owned = new HashSet<>(mapper.selectOwnedCardIds(userId));

        String batchNo = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        int costEach = times == 10 ? nz(pool.getCostTen()) / 10 : nz(pool.getCostSingle());
        int costTotal = times == 10 ? nz(pool.getCostTen()) : nz(pool.getCostSingle());

        List<DrawItem> items = new ArrayList<>();
        List<GachaDrawRecord> records = new ArrayList<>();

        for (int i = 1; i <= times; i++){
            /* ① 计数 +1 */
            pity.setSsrCount(pity.getSsrCount() + 1);
            pity.setSrCount(pity.getSrCount() + 1);
            pity.setTotalCount(pity.getTotalCount() + 1);

            /* ② 定稀有度：先判保底（用户级覆盖优先） */
            String rarity;
            int isPity = 0;
            int pitySsr = pity.effectiveSsrPity(nz(pool.getPitySsr()));
            int pitySr  = pity.effectiveSrPity(nz(pool.getPitySr()));

            /* ★ 出货窗口：距上次出货的计数没到「起点」时，该稀有度不进入候选。
             *   例：start=250, pity=300 → 前 250 抽绝不出 SSR，只在 250~300 抽之间出，第 300 抽必出。 */
            int ssrStart = pity.effectiveSsrStart(nz(pool.getPitySsrStart()));
            int srStart  = nz(pool.getPitySrStart());
            if (srStart  > pitySr ) srStart  = pitySr;      // 起点不能超过保底
            boolean ssrInWindow = pity.getSsrCount() >= ssrStart;
            boolean srInWindow  = pity.getSrCount()  >= srStart;

            /* 最低门槛：累计抽数没到 min_ssr_draws 时，SSR 也完全不进入候选（按总抽数计，出货后不归零） */
            boolean ssrLocked = pity.getMinSsrDraws() > 0 && pity.getTotalCount() < pity.getMinSsrDraws();

            boolean ssrOk = ssrInWindow && !ssrLocked;      // 窗口已进 + 门槛已过，才可能出 SSR

            if (ssrOk && pitySsr > 0 && pity.getSsrCount() >= pitySsr){
                rarity = "SSR"; isPity = 1;
            }else if (srInWindow && pitySr > 0 && pity.getSrCount() >= pitySr){
                rarity = "SR"; isPity = 1;
            }else{
                rarity = randomRarity(rate, cards, pool, !ssrOk, !srInWindow);
            }

            /* 双保险：不该出却抽到了，降级 */
            if (!ssrOk && "SSR".equals(rarity)){
                rarity = cards.stream().anyMatch(c -> "SR".equals(c.getRarity()) && inStock(c)) ? "SR" : "R";
                isPity = 0;
            }
            if (!srInWindow && "SR".equals(rarity)){
                rarity = cards.stream().anyMatch(c -> "R".equals(c.getRarity()) && inStock(c)) ? "R" : "N";
                isPity = 0;
            }

            /* 若该稀有度无可用卡，降级到有卡的稀有度，避免空指针 */
            rarity = fallbackRarity(rarity, cards, pool);

            /* ③ 选卡（按有效权重） */
            GachaCard card = randomCard(cards, rarity, pool);
            if (card == null) throw new IllegalStateException("无可出货卡牌");

            /* ④ 保底计数重置 */
            int reset = pool.getResetOnHit() == null ? 1 : pool.getResetOnHit();
            if (reset == 1){
                if ("SSR".equals(rarity)){ pity.setSsrCount(0); pity.setSrCount(0); pity.setSsrTotal(pity.getSsrTotal() + 1); }
                else if ("SR".equals(rarity)){ pity.setSrCount(0); }
            }else if ("SSR".equals(rarity)){
                pity.setSsrTotal(pity.getSsrTotal() + 1);
            }

            /* ⑤ 扣库存 */
            if (card.getStock() != null && card.getStock() > 0){
                mapper.decrStock(card.getId());
                card.setStock(card.getStock() - 1);
            }

            /* ⑥ 是否首次获得 */
            int isNew = owned.contains(card.getId()) ? 0 : 1;
            if (isNew == 1) owned.add(card.getId());

            DrawItem item = DrawItem.of(card.getId(), card.getName(), card.getRarity(),
                    safe(card.getNpcId()), card.getIsUp() == null ? 0 : card.getIsUp(),
                    isPity, i, isNew);
            /* ★ 卡面资源：缩略图 + 大图，小程序抽到就能直接渲染，不用再查一次 */
            item.setThumb(safe(card.getThumb()));
            item.setImage(safe(card.getImage()));
            GachaRarity rm = rarityMeta.get(card.getRarity());
            if (rm != null){
                item.setRarityName(safe(rm.getRarityName()));
                item.setRarityLabel(safe(rm.getLabel()));
                item.setRarityColor(safe(rm.getColor()));
            }
            items.add(item);

            records.add(GachaDrawRecord.of(UUID.randomUUID().toString().replace("-", "").substring(0, 24),
                    batchNo, userId, poolId, card.getId(), card.getName(), card.getRarity(),
                    card.getIsUp() == null ? 0 : card.getIsUp(), i, isPity, costEach));

            /* ⑦ 写用户卡牌（存在则 count+1） */
            GachaUserCard uc = mapper.selectUserCard(userId, card.getId());
            if (uc == null){
                mapper.insertUserCard(GachaUserCard.of(
                        UUID.randomUUID().toString().replace("-", "").substring(0, 24),
                        userId, card.getId(), poolId, card.getName(), card.getRarity()));
            }else{
                mapper.incrUserCard(userId, card.getId());
            }
        }

        /* 批量写记录 + 更新保底 */
        mapper.batchInsertRecords(records);
        if (mapper.selectPity(userId, poolId) == null) mapper.insertPity(pity);
        else mapper.updatePity(pity);

        DrawResult res = new DrawResult();
        res.setBatchNo(batchNo);
        res.setPoolId(poolId);
        res.setPoolName(pool.getName());
        res.setItems(items);
        res.setCost(costTotal);
        int effSsr = pity.effectiveSsrPity(nz(pool.getPitySsr()));
        int effSr  = pity.effectiveSrPity(nz(pool.getPitySr()));
        DrawResult.PityInfo pi = DrawResult.PityInfo.of(pity.getSsrCount(), effSsr,
                                           pity.getSrCount(), effSr, pity.getTotalCount());
        int minDraws = pity.getMinSsrDraws();
        pi.setMinSsrDraws(minDraws);
        pi.setUnlockRemain(minDraws > 0 ? Math.max(0, minDraws - pity.getTotalCount()) : 0);
        pi.setLocked(minDraws > 0 && pity.getTotalCount() < minDraws ? 1 : 0);
        pi.setPersonal(effSsr != nz(pool.getPitySsr()) ? 1 : 0);
        res.setPity(pi);
        return res;
    }

    /* ==================== 内部方法 ==================== */

    private GachaUserPity loadPity(String userId, String poolId){
        GachaUserPity p = mapper.selectPity(userId, poolId);
        if (p == null){
            p = GachaUserPity.init(UUID.randomUUID().toString().replace("-", "").substring(0, 24), userId, poolId);
        }
        return p;
    }

    private Map<String, Double> rarityRate(){
        Map<String, Double> m = new LinkedHashMap<>();
        List<GachaRarity> rs = mapper.selectRarity();
        for (GachaRarity r : rs) m.put(r.getRarityKey(), r.getRate() == null ? 0d : r.getRate().doubleValue());
        if (m.isEmpty()){ m.put("SSR", 2d); m.put("SR", 12d); m.put("R", 36d); m.put("N", 50d); }
        return m;
    }

    /** 有效权重：UP 且命中 up_npc_id 时 × bonus */
    private double effWeight(GachaPool pool, GachaCard c){
        int w = c.getWeight() == null ? 0 : c.getWeight();
        boolean up = c.getIsUp() != null && c.getIsUp() == 1;
        if (up){
            String upNpc = safe(pool.getUpNpcId());
            /* up_npc_id 为空时，所有 is_up=1 的卡都算 UP；否则需 npc 匹配 */
            boolean hit = upNpc.isEmpty() || upNpc.equals(safe(c.getNpcId()));
            if (hit){
                double bonus = pool.getUpBonus() == null ? 2.0 : pool.getUpBonus().doubleValue();
                return w * bonus;
            }
        }
        return w;
    }

    private boolean inStock(GachaCard c){
        return c.getStock() == null || c.getStock() != 0;
    }

    /** 按概率随机稀有度。
     *  ssrLocked=true 时把 SSR 从候选中剔除（窗口未进 / 门槛未达成）
     *  srLocked=true  时把 SR  从候选中剔除（SR 窗口未进） */
    private String randomRarity(Map<String, Double> rate, List<GachaCard> cards, GachaPool pool,
                                boolean ssrLocked, boolean srLocked){
        double total = 0;
        Map<String, Double> usable = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : rate.entrySet()){
            if (ssrLocked && "SSR".equals(e.getKey())) continue;   // ★ SSR 窗口未进，不参与
            if (srLocked  && "SR".equals(e.getKey()))  continue;   // ★ SR 窗口未进，不参与
            boolean has = cards.stream().anyMatch(c -> e.getKey().equals(c.getRarity()) && inStock(c));
            if (has && e.getValue() > 0){ usable.put(e.getKey(), e.getValue()); total += e.getValue(); }
        }
        if (usable.isEmpty()) return "N";
        double r = RND.nextDouble() * total;
        for (Map.Entry<String, Double> e : usable.entrySet()){
            r -= e.getValue();
            if (r <= 0) return e.getKey();
        }
        return new ArrayList<>(usable.keySet()).get(usable.size() - 1);
    }

    /** 该稀有度没卡（或全无库存）时，降级到有卡的稀有度 */
    private String fallbackRarity(String rarity, List<GachaCard> cards, GachaPool pool){
        boolean has = cards.stream().anyMatch(c -> rarity.equals(c.getRarity()) && inStock(c));
        if (has) return rarity;
        String[] order = {"SSR", "SR", "R", "N"};
        for (String r : order){
            if (cards.stream().anyMatch(c -> r.equals(c.getRarity()) && inStock(c))) return r;
        }
        return "N";
    }

    /** 该稀有度内按有效权重随机一张 */
    private GachaCard randomCard(List<GachaCard> cards, String rarity, GachaPool pool){
        List<GachaCard> poolCards = new ArrayList<>();
        double total = 0;
        for (GachaCard c : cards){
            if (!rarity.equals(c.getRarity()) || !inStock(c)) continue;
            poolCards.add(c);
            total += effWeight(pool, c);
        }
        if (poolCards.isEmpty()) return null;
        if (total <= 0) return poolCards.get(RND.nextInt(poolCards.size()));
        double r = RND.nextDouble() * total;
        for (GachaCard c : poolCards){
            r -= effWeight(pool, c);
            if (r <= 0) return c;
        }
        return poolCards.get(poolCards.size() - 1);
    }

    /** 期望抽数：E = Σ(i=1..K-1) i·(1-p)^(i-1)·p + K·(1-p)^(K-1)；无保底 E = 1/p */
    private double expect(int k, double ratePct){
        double p = ratePct / 100d;
        if (p <= 0) return k > 0 ? k : 0;
        if (k <= 0) return 1d / p;
        double e = 0, miss = 1;
        for (int i = 1; i < k; i++){ e += i * miss * p; miss *= (1 - p); }
        return e + k * miss;
    }

    private int nz(Integer v){ return v == null ? 0 : v; }
    private String safe(String s){ return s == null ? "" : s; }
    private double round2(double v){ return BigDecimal.valueOf(v).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue(); }
}
