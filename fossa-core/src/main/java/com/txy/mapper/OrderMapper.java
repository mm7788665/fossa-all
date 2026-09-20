package com.txy.mapper;

import com.txy.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.txy.entity.Order;

import java.util.List;

/**
 * 订单 Mapper（原生 MyBatis）
 */
@Mapper
public interface OrderMapper {

    List<Order> selectAll();

    Order selectById(@Param("id") String id);

    int countAll();

    /** 关联玩家名 + 场次标题 */
    List<OrderVO> selectOrderList();

    /** 已支付+已核销订单总额 */
    double sumRevenue();

    int insert(Order order);

    int update(Order order);

    /** 单独改状态 */
    int updateStatus(@Param("id") String id, @Param("status") String status);

    /** 逻辑删除 */
    int logicDelete(@Param("id") String id);
}
