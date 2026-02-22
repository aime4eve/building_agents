package com.hkt.iot.smartapps.moldprevention.domain.service;

import com.hkt.iot.smartapps.moldprevention.domain.model.ControlCommand;
import com.hkt.iot.smartapps.moldprevention.domain.model.ControlEffectiveness;
import com.hkt.iot.smartapps.moldprevention.domain.model.MoldRiskLevel;
import com.hkt.iot.smartapps.moldprevention.domain.model.ZoneId;

import java.util.List;

/**
 * 湿度控制领域服务接口
 *
 * 职责：处理湿度控制相关的业务逻辑
 */
public interface HumidityControlService {

    /**
     * 生成控制命令
     *
     * @param zoneId 区域ID
     * @param riskLevel 风险等级
     * @return 控制命令列表
     */
    List<ControlCommand> generateControlCommands(ZoneId zoneId, MoldRiskLevel riskLevel);

    /**
     * 执行控制
     *
     * @param zoneId 区域ID
     * @param commands 控制命令列表
     */
    void executeControl(ZoneId zoneId, List<ControlCommand> commands);

    /**
     * 评估控制效果
     *
     * @param zoneId 区域ID
     * @return 控制效果
     */
    ControlEffectiveness evaluateControlEffectiveness(ZoneId zoneId);

    /**
     * 自动调节湿度
     *
     * @param zoneId 区域ID
     */
    void autoAdjust(ZoneId zoneId);

    /**
     * 停止所有控制设备
     *
     * @param zoneId 区域ID
     */
    void stopAllControllers(ZoneId zoneId);

    /**
     * 获取控制历史
     *
     * @param zoneId 区域ID
     * @param limit 限制数量
     * @return 控制命令历史列表
     */
    List<ControlCommand> getControlHistory(ZoneId zoneId, int limit);
}
