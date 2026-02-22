package com.hkt.iot.order.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkt.iot.order.infrastructure.persistence.po.OrderPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper
 *
 * @author HKT IoT Team
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderPO> {
}
