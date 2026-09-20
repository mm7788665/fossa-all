package com.txy.service;

import com.txy.mapper.*;
import com.txy.vo.DashboardVO;
import com.txy.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.txy.entity.Setting;


import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private NpcMapper npcMapper;
    @Autowired
    private ShowMapper showMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PlayerMapper playerMapper;
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private SettingMapper settingMapper;
    @Autowired
    private IntimacyMapper intimacyMapper;

    public DashboardVO stats() {
        DashboardVO v = new DashboardVO();

        v.setNpc(npcMapper.countAll());
        v.setShow(showMapper.countAll());
        v.setShowOnSale(showMapper.countOnSale());
        v.setOrder(orderMapper.countAll());
        v.setPlayer(playerMapper.countAll());
        v.setNotice(noticeMapper.countPublished());
        v.setRevenue(orderMapper.sumRevenue());
        v.setSeats(showMapper.sumTaken());
        v.setCap(showMapper.sumCapacity());

        // 已支付/已核销订单数（Java 8 写法，不用 stream().toList()）
        int paid = 0;
        List<OrderVO> orders = orderMapper.selectOrderList();
        for (OrderVO o : orders) {
            if ("已支付".equals(o.getStatus()) || "已核销".equals(o.getStatus())) {
                paid++;
            }
        }
        v.setPaid(paid);

        // 我的亲密度总和
        int intimacy = 0;
        List<java.util.Map<String, Object>> rank = intimacyMapper.selectTotalRank();
        for (java.util.Map<String, Object> r : rank) {
            if ("me".equals(String.valueOf(r.get("playerId")))) {
                Object t = r.get("total");
                intimacy = (t == null) ? 0 : ((Number) t).intValue();
                break;
            }
        }
        v.setIntimacy(intimacy);

        int seats = v.getSeats();
        int cap = v.getCap();
        v.setRate(cap == 0 ? 0 : Math.round((float) seats / cap * 100));

        Setting app = settingMapper.selectByKey("appName");
        Setting store = settingMapper.selectByKey("storeName");
        v.setAppName(app != null ? app.getV() : "");
        v.setStoreName(store != null ? store.getV() : "");

        return v;
    }
}
