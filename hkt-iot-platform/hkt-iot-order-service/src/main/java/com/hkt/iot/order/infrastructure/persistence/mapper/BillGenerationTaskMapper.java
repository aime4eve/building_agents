package com.hkt.iot.order.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkt.iot.order.infrastructure.persistence.po.BillGenerationTaskPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单生成任务Mapper
 *
 * @author HKT IoT Team
 */
@Mapper
public interface BillGenerationTaskMapper extends BaseMapper<BillGenerationTaskPO> {
}
