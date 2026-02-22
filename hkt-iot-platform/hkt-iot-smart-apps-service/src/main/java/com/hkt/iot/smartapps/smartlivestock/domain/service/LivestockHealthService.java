package com.hkt.iot.smartapps.smartlivestock.domain.service;

import com.hkt.iot.smartapps.smartlivestock.domain.model.HealthRecord;
import com.hkt.iot.smartapps.smartlivestock.domain.model.HealthScore;
import com.hkt.iot.smartapps.smartlivestock.domain.model.HealthStatistics;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockHealthReport;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.ReportPeriod;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 牲畜健康领域服务接口
 *
 * 职责：处理牲畜健康相关的业务逻辑
 */
public interface LivestockHealthService {

    /**
     * 计算健康评分
     *
     * @param livestockId 牲畜ID
     * @param record 健康记录
     * @return 健康评分
     */
    HealthScore calculateHealthScore(LivestockId livestockId, HealthRecord record);

    /**
     * 分析健康趋势
     *
     * @param livestockId 牲畜ID
     * @param from 开始时间
     * @param to 结束时间
     * @return 健康统计数据
     */
    HealthStatistics analyzeHealthTrend(LivestockId livestockId, LocalDateTime from, LocalDateTime to);

    /**
     * 检测异常
     *
     * @param livestockId 牲畜ID
     * @param record 健康记录
     * @return 是否存在异常
     */
    boolean detectAnomaly(LivestockId livestockId, HealthRecord record);

    /**
     * 生成健康报告
     *
     * @param livestockId 牲畜ID
     * @param period 报告周期
     * @return 健康报告
     */
    LivestockHealthReport generateHealthReport(LivestockId livestockId, ReportPeriod period);

    /**
     * 批量计算健康评分
     *
     * @param livestockIds 牲畜ID列表
     * @return 健康评分列表
     */
    List<HealthScore> batchCalculateHealthScore(List<LivestockId> livestockIds);
}
