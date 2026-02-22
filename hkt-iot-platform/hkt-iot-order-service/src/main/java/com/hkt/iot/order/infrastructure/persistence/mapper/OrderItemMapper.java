package com.hkt.iot.order.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkt.iot.order.infrastructure.persistence.po.OrderItemPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项Mapper
 *
 * @author HKT IoT Team
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemPO> {
}
