package com.hkt.iot.smartapps.moldprevention.domain.service;

import com.hkt.iot.smartapps.moldprevention.domain.model.EnvironmentData;
import com.hkt.iot.smartapps.moldprevention.domain.model.MoldRiskEvaluationResult;
import com.hkt.iot.smartapps.moldprevention.domain.model.MoldRiskLevel;
import com.hkt.iot.smartapps.moldprevention.domain.model.MoldRiskStatistics;
import com.hkt.iot.smartapps.moldprevention.domain.model.ZoneId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 霉菌风险评估领域服务接口
 *
 * 职责：处理霉菌风险评估相关的业务逻辑
 */
public interface MoldRiskEvaluationService {

    /**
     * 评估霉菌风险
     *
     * @param zoneId 区域ID
     * @param data 环境数据
     * @return 风险评估结果
     */
    MoldRiskEvaluationResult evaluateRisk(ZoneId zoneId, EnvironmentData data);

    /**
     * 计算风险分数 (0-100)
     *
     * @param data 环境数据
     * @return 风险分数
     */
    int calculateRiskScore(EnvironmentData data);

    /**
     * 预测未来风险 (准确率>85%)
     *
     * @param zoneId 区域ID
     * @param currentData 当前环境数据
     * @param hoursAhead 预测小时数
     * @return 预测的风险等级
     */
    MoldRiskLevel predictRisk(ZoneId zoneId, EnvironmentData currentData, int hoursAhead);

    /**
     * 获取风险趋势
     *
     * @param zoneId 区域ID
     * @param from 开始时间
     * @param to 结束时间
     * @return 风险统计数据列表
     */
    List<MoldRiskStatistics> getRiskTrend(ZoneId zoneId, LocalDateTime from, LocalDateTime to);

    /**
     * 批量评估风险
     *
     * @param zoneIds 区域ID列表
     * @param dataList 环境数据列表
     * @return 风险评估结果列表
     */
    List<MoldRiskEvaluationResult> batchEvaluateRisk(List<ZoneId> zoneIds, List<EnvironmentData> dataList);
}
