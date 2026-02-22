package com.hkt.iot.order.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkt.iot.order.infrastructure.persistence.po.InvoicePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发票Mapper
 *
 * @author HKT IoT Team
 */
@Mapper
public interface InvoiceMapper extends BaseMapper<InvoicePO> {
}
