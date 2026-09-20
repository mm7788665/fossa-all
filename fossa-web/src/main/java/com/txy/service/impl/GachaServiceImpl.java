package com.txy.service.impl;

import com.txy.entity.GachaCard;
import com.txy.entity.GachaPool;
import com.txy.entity.GachaRarity;
import com.txy.mapper.GachaCardMapper;
import com.txy.mapper.GachaPoolMapper;
import com.txy.mapper.GachaRarityMapper;
import com.txy.service.GachaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 后台抽卡配置业务
 *
 * 说明：卡牌是「奖池的子表」，前端编辑后是整池提交的，
 *      所以 saveWithCards 采用「先全量软删，再重新插入」的策略，
 *      好处是不用逐个 diff，坏处是 id 会变（前端生成的 id 会保留）。
 */
@Service
public class GachaServiceImpl implements GachaService {

    private final GachaPoolMapper poolMapper;
    private final GachaCardMapper cardMapper;
    private final GachaRarityMapper rarityMapper;

    public GachaServiceImpl(GachaPoolMapper poolMapper, GachaCardMapper cardMapper, GachaRarityMapper rarityMapper){
        this.poolMapper = poolMapper;
        this.cardMapper = cardMapper;
        this.rarityMapper = rarityMapper;
    }

    /* ==================== 查询 ==================== */

    @Override
    public List<GachaPool> listWithCards(){
        List<GachaPool> pools = poolMapper.selectAll();
        fillCards(pools);
        return pools;
    }

    @Override
    public List<GachaPool> listOpenWithCards(){
        List<GachaPool> pools = poolMapper.selectByStatus("启用");
        fillCards(pools);
        return pools;
    }

    @Override
    public GachaPool getWithCards(String id){
        GachaPool pool = poolMapper.selectById(id);
        if (pool == null) return null;
        pool.setCards(cardMapper.selectByPool(id));
        return pool;
    }

    private void fillCards(List<GachaPool> pools){
        if (pools == null || pools.isEmpty()) return;
        for (GachaPool p : pools){
            List<GachaCard> cards = cardMapper.selectByPool(p.getId());
            p.setCards(cards == null ? new ArrayList<>() : cards);
        }
    }

    /* ==================== 写入 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GachaPool save(GachaPool pool){
        if (pool == null) return null;
        if (pool.getId() == null || pool.getId().trim().isEmpty()){
            pool.setId(uuid());
        }
        if (pool.getStatus() == null || pool.getStatus().trim().isEmpty()) pool.setStatus("未启用");
        if (pool.getCurrency() == null) pool.setCurrency("星尘");

        if (poolMapper.selectById(pool.getId()) == null){
            poolMapper.insert(pool);
        }else{
            poolMapper.update(pool);
        }
        return pool;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithCards(GachaPool pool){
        if (pool == null || pool.getId() == null || pool.getId().trim().isEmpty()) return false;

        /* ① 奖池本体：用 selectByIdAny 判断行是否存在（含已软删的），
         *    否则对已软删的 id 再 INSERT 会撞主键。update 里会把 deleted 置 0 复活。 */
        if (poolMapper.selectByIdAny(pool.getId()) == null){
            poolMapper.insert(pool);
        }else{
            poolMapper.update(pool);
        }

        /* ② 卡牌：差量保存（前端整池提交，id 是原有的）
         *    ★ 不能「先软删再 INSERT」：软删只置 deleted=1，行还在，
         *      INSERT 同 id 必然 Duplicate entry。改成 upsert + 差量软删。 */
        List<GachaCard> cards = pool.getCards();
        List<String> keepIds = new ArrayList<>();
        if (cards != null){
            int sort = 1;
            for (GachaCard c : cards){
                if (c == null) continue;
                if (c.getName() == null || c.getName().trim().isEmpty()) continue;
                c.setPoolId(pool.getId());
                if (c.getId() == null || c.getId().trim().isEmpty()) c.setId(uuid());
                if (c.getRarity() == null || c.getRarity().trim().isEmpty()) c.setRarity("N");
                if (c.getStock() == null) c.setStock(-1);
                if (c.getWeight() == null) c.setWeight(10);
                if (c.getIsUp() == null) c.setIsUp(0);
                if (c.getNpcId() == null) c.setNpcId("");
                c.setSort(sort++);
                cardMapper.upsert(c);
                keepIds.add(c.getId());
            }
        }
        /* 没提交上来的卡软删（保留行，便于误删恢复） */
        cardMapper.softDeleteExcept(pool.getId(), keepIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeWithCards(String id){
        if (id == null || id.trim().isEmpty()) return false;
        cardMapper.softDeleteByPool(id);
        return poolMapper.softDelete(id) > 0;
    }

    @Override
    public boolean updateWeight(String cardId, int weight){
        if (cardId == null || cardId.trim().isEmpty()) return false;
        if (weight < 0) weight = 0;
        return cardMapper.updateWeight(cardId, weight) > 0;
    }

    /* ==================== 稀有度 ==================== */

    @Override
    public Map<String, Object> rarityRateMap(){
        Map<String, Object> map = new LinkedHashMap<>();
        List<GachaRarity> list = rarityMapper.selectAll();
        if (list == null || list.isEmpty()){
            /* 表里没配就给默认值，避免前端拿不到概率直接崩 */
            map.put("SSR", 2); map.put("SR", 12); map.put("R", 36); map.put("N", 50);
            return map;
        }
        for (GachaRarity r : list){
            map.put(r.getRarityKey(), r.getRate() == null ? 0 : r.getRate().doubleValue());
        }
        return map;
    }

    @Override
    public List<GachaRarity> rarityList(){
        List<GachaRarity> list = rarityMapper.selectAll();
        return list == null ? new ArrayList<>() : list;
    }

    private String uuid(){ return UUID.randomUUID().toString().replace("-", "").substring(0, 24); }
}
