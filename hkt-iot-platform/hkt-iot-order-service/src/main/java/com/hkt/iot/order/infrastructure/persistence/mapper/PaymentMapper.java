package com.hkt.iot.order.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkt.iot.order.infrastructure.persistence.po.PaymentPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付Mapper
 *
 * @author HKT IoT Team
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentPO> {
}
