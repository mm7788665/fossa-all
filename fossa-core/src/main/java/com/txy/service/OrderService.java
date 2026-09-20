package com.txy.service;

import com.txy.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.txy.mapper.OrderMapper;

import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    public List<OrderVO> list() {
        return orderMapper.selectOrderList();
    }

    /** 改状态（逻辑删除的订单改不了） */
    public boolean updateStatus(String id, String status) {
        return orderMapper.updateStatus(id, status) > 0;
    }

    /** 逻辑删除 */
    public boolean delete(String id) {
        return orderMapper.logicDelete(id) > 0;
    }

    /** 订单状态枚举（供前端下拉） */
    public List<String> statusOptions() {
        return Arrays.asList("待付款", "已支付", "待核销", "已核销", "已退款", "已取消");
    }
}
