package com.txy.mp.service;

import com.txy.fossa.web.common.R;
import com.txy.fossa.web.entity.Player;
import com.txy.fossa.web.mp.mapper.MpMapper;
import com.txy.fossa.web.mp.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 小程序玩法 Service
 *  - 所有读接口对异常做降级：出错时返回 null，由 Controller 决定兜底
 *  - 抽卡在服务端按 weight 加权随机，客户端无法篡改
 */
@Service
public class MpService {

    @Autowired
    private MpMapper mpMapper;

    // ================= NPC =================

    public R<List<MpNpcVO>> npcList() {
        return R.ok(mpMapper.selectNpcList());
    }

    public R<MpNpcVO> npcDetail(String id) {
        MpNpcVO vo = mpMapper.selectNpcById(id);
        if (vo == null) return R.fail(404, "角色不存在");
        return R.ok(vo);
    }

    // ================= 公告 =================

    public R<List<MpNoticeVO>> noticeList() {
        return R.ok(mpMapper.selectNoticeList());
    }

    // ================= 场次 / 预约 =================

    public R<List<MpShowVO>> showList() {
        return R.ok(mpMapper.selectShowList());
    }

    /**
     * 预约下单：扣减余座 + 生成订单（同一事务）
     */
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> book(String playerId, String showId, Integer qty, String userName) {
        if (showId == null || showId.isEmpty()) return R.fail(400, "请选择场次");
        int n = (qty == null || qty <= 0) ? 1 : Math.min(qty, 6);

        MpShowVO show = mpMapper.selectShowById(showId);
        if (show == null) return R.fail(404, "场次不存在");
        if (show.getRemain() == null || show.getRemain() < n) {
            return R.fail(400, "余座不足，仅剩 " + (show.getRemain() == null ? 0 : show.getRemain()) + " 座");
        }

        int updated = mpMapper.incrShowTaken(showId, n);
        if (updated <= 0) return R.fail(400, "余座不足，请刷新后重试");

        BigDecimal amount = show.getPrice() == null ? BigDecimal.ZERO : show.getPrice();
        String orderId = "DH" + System.currentTimeMillis() + (int) (Math.random() * 900 + 100);
        String name = (userName == null || userName.isEmpty()) ? "" : userName;

        mpMapper.insertOrder(orderId, playerId, showId, name, amount, n, "待支付");

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("orderId", orderId);
        data.put("qty", n);
        data.put("amount", amount.multiply(new BigDecimal(n)));
        return R.ok(data);
    }

    public R<List<MpOrderVO>> myOrders(String playerId) {
        return R.ok(mpMapper.selectOrdersOf(playerId));
    }

    // ================= 抽卡（服务端算概率） =================

    public R<Map<String, List<MpGachaPrizeVO>>> gachaPool() {
        Map<String, List<MpGachaPrizeVO>> data = new HashMap<String, List<MpGachaPrizeVO>>();
        data.put("self", mpMapper.selectGachaPool("self"));
        data.put("npc", mpMapper.selectGachaPool("npc"));
        return R.ok(data);
    }

    /**
     * 抽卡：按 weight 加权随机抽 count 次
     * @param group self / npc
     * @param count 1 或 10
     */
    @Transactional(rollbackFor = Exception.class)
    public R<List<MpGachaPrizeVO>> gachaDraw(String playerId, String group, Integer count) {
        String g = (group == null || group.isEmpty()) ? "self" : group;
        int n = (count == null || count <= 0) ? 1 : Math.min(count, 10);

        List<MpGachaPrizeVO> pool = mpMapper.selectGachaPool(g);
        if (pool == null || pool.isEmpty()) return R.fail(404, "奖池为空，请先在后台配置");

        int totalWeight = 0;
        for (MpGachaPrizeVO p : pool) {
            totalWeight += (p.getWeight() == null ? 0 : p.getWeight());
        }
        if (totalWeight <= 0) return R.fail(500, "奖池权重配置异常");

        Random rnd = new Random();
        List<MpGachaPrizeVO> result = new ArrayList<MpGachaPrizeVO>();
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(totalWeight);
            int acc = 0;
            MpGachaPrizeVO hit = pool.get(pool.size() - 1);
            for (MpGachaPrizeVO p : pool) {
                acc += (p.getWeight() == null ? 0 : p.getWeight());
                if (r < acc) { hit = p; break; }
            }
            result.add(copy(hit));
            if (playerId != null && !playerId.isEmpty()) {
                try {
                    mpMapper.insertGachaRecord(UUID.randomUUID().toString().replace("-", ""),
                            playerId, g, hit.getId(), hit.getName(),
                            hit.getRarity() == null ? 1 : hit.getRarity());
                } catch (Exception ignored) {
                    // 记录失败不影响抽奖结果
                }
            }
        }
        return R.ok(result);
    }

    private MpGachaPrizeVO copy(MpGachaPrizeVO src) {
        MpGachaPrizeVO v = new MpGachaPrizeVO();
        v.setId(src.getId());
        v.setName(src.getName());
        v.setTag(src.getTag());
        v.setRarity(src.getRarity());
        v.setIcon(src.getIcon());
        return v;
    }

    // ================= 玩家 / 排行 =================

    public R<MpPlayerVO> playerMe(String playerId) {
        Player p = mpMapper.selectPlayerById(playerId);
        if (p == null) return R.fail(404, "玩家不存在");

        MpPlayerVO vo = new MpPlayerVO();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setTag(p.getTag());
        vo.setAvatar(p.getAvatar());
        vo.setPhone(p.getPhone());
        vo.setVip(p.getVip());
        vo.setStatus(p.getStatus());

        Map<String, Integer> intimacy = new LinkedHashMap<String, Integer>();
        List<Map<String, Object>> rows = mpMapper.selectIntimacyOf(playerId);
        int total = 0;
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object k = row.get("npcId");
                Object v = row.get("value");
                if (k == null) continue;
                int val = v == null ? 0 : ((Number) v).intValue();
                intimacy.put(String.valueOf(k), val);
                total += val;
            }
        }
        vo.setIntimacy(intimacy);
        vo.setIntimacyTotal(total);
        return R.ok(vo);
    }

    /**
     * 亲密度排行：
     *   mine    —— 当前玩家对各 NPC
     *   players —— 全服玩家 × NPC 矩阵（按总和降序）
     *   byNpc   —— 每个 NPC 的攻略者 TOP（按值降序）
     */
    public R<MpRankVO> rank(String playerId) {
        MpRankVO vo = new MpRankVO();

        // 1. 我的
        Map<String, Integer> mine = new LinkedHashMap<String, Integer>();
        List<Map<String, Object>> myRows = mpMapper.selectIntimacyOf(playerId);
        if (myRows != null) {
            for (Map<String, Object> row : myRows) {
                Object k = row.get("npcId");
                Object v = row.get("value");
                if (k == null) continue;
                mine.put(String.valueOf(k), v == null ? 0 : ((Number) v).intValue());
            }
        }
        vo.setMine(mine);

        // 2. 全服明细
        List<Map<String, Object>> all = mpMapper.selectIntimacyAll();
        Map<String, MpRankVO.PlayerRow> playerMap = new LinkedHashMap<String, MpRankVO.PlayerRow>();
        Map<String, List<MpRankVO.NpcRankRow>> byNpc = new LinkedHashMap<String, List<MpRankVO.NpcRankRow>>();

        if (all != null) {
            for (Map<String, Object> row : all) {
                String pid = str(row.get("playerId"));
                String nid = str(row.get("npcId"));
                int val = row.get("value") == null ? 0 : ((Number) row.get("value")).intValue();
                if (pid == null || nid == null) continue;

                MpRankVO.PlayerRow pr = playerMap.get(pid);
                if (pr == null) {
                    pr = new MpRankVO.PlayerRow();
                    pr.setId(pid);
                    pr.setName(str(row.get("playerName")));
                    pr.setTag(str(row.get("playerTag")));
                    pr.setAvatar(str(row.get("avatar")));
                    pr.setNpc(new LinkedHashMap<String, Integer>());
                    pr.setTotal(0);
                    playerMap.put(pid, pr);
                }
                pr.getNpc().put(nid, val);
                pr.setTotal(pr.getTotal() + val);

                List<MpRankVO.NpcRankRow> list = byNpc.get(nid);
                if (list == null) {
                    list = new ArrayList<MpRankVO.NpcRankRow>();
                    byNpc.put(nid, list);
                }
                MpRankVO.NpcRankRow nr = new MpRankVO.NpcRankRow();
                nr.setPlayerId(pid);
                nr.setPlayerName(pr.getName());
                nr.setPlayerTag(pr.getTag());
                nr.setAvatar(pr.getAvatar());
                nr.setValue(val);
                list.add(nr);
            }
        }

        // 3. 排序
        List<MpRankVO.PlayerRow> players = new ArrayList<MpRankVO.PlayerRow>(playerMap.values());
        Collections.sort(players, new Comparator<MpRankVO.PlayerRow>() {
            @Override
            public int compare(MpRankVO.PlayerRow a, MpRankVO.PlayerRow b) {
                return b.getTotal() - a.getTotal();
            }
        });
        // 限制 50 条，避免包体过大
        if (players.size() > 50) players = new ArrayList<MpRankVO.PlayerRow>(players.subList(0, 50));
        vo.setPlayers(players);

        for (Map.Entry<String, List<MpRankVO.NpcRankRow>> e : byNpc.entrySet()) {
            Collections.sort(e.getValue(), new Comparator<MpRankVO.NpcRankRow>() {
                @Override
                public int compare(MpRankVO.NpcRankRow a, MpRankVO.NpcRankRow b) {
                    return b.getValue() - a.getValue();
                }
            });
            List<MpRankVO.NpcRankRow> l = e.getValue();
            if (l.size() > 20) e.setValue(new ArrayList<MpRankVO.NpcRankRow>(l.subList(0, 20)));
        }
        vo.setByNpc(byNpc);
        return R.ok(vo);
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
