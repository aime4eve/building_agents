package com.hkt.iot.notification.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkt.iot.notification.infrastructure.persistence.po.NotificationRequestPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知请求Mapper
 *
 * @author HKT IoT Team
 */
@Mapper
public interface NotificationRequestMapper extends BaseMapper<NotificationRequestPO> {
}
