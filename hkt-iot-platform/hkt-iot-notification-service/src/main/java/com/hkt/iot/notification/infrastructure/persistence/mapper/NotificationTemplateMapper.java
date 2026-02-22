package com.hkt.iot.notification.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkt.iot.notification.infrastructure.persistence.po.NotificationTemplatePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知模板Mapper
 *
 * @author HKT IoT Team
 */
@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplatePO> {
}
