package com.hkt.iot.notification.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkt.iot.notification.infrastructure.persistence.po.NotificationLogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知日志Mapper
 *
 * @author HKT IoT Team
 */
@Mapper
public interface NotificationLogMapper extends BaseMapper<NotificationLogPO> {
}
