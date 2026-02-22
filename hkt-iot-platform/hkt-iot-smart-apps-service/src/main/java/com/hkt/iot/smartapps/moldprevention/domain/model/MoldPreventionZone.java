package com.hkt.iot.smartapps.moldprevention.domain.model;

import com.hkt.iot.domain.shared.AuditLog;
import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.TenantId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 防霉管控区域聚合根
 *
 * 职责：管理防霉管控区域的定义、风险监测和湿度控制
 * 业务规则：
 * - 根据温湿度数据评估霉菌风险等级
 * - 根据风险等级自动触发控制策略
 * - 记录监测历史和风险变化
 */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoldPreventionZone {

    private ZoneId id;
    private ZoneName name;
    private ZoneCode code;
    private SpaceId spaceId;
    private TenantId tenantId;
    private ZoneStatus status;
    private MoldRiskThreshold threshold;
    private HumidityControlStrategy strategy;
    private List<SensorDevice> sensors;
    private List<ControlDevice> controllers;
    private MoldRiskLevel currentRiskLevel;
    private EnvironmentData lastEnvironmentData;
    private LocalDateTime lastEvaluatedAt;
    private String description;
    private AuditLog auditLog;
    private Long version;

    /**
     * 激活防霉管控区域
     */
    public void activate() {
        if (this.status == ZoneStatus.ACTIVE) {
            return;
        }
        if (this.sensors == null || this.sensors.isEmpty()) {
            throw new IllegalStateException("防霉管控区域必须配置传感器后才能激活");
        }
        this.status = ZoneStatus.ACTIVE;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "防霉管控区域激活");
    }

    /**
     * 停用防霉管控区域
     */
    public void deactivate() {
        if (this.status == ZoneStatus.INACTIVE) {
            return;
        }
        this.status = ZoneStatus.INACTIVE;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "防霉管控区域停用");
    }

    /**
     * 进入维护模式
     */
    public void enterMaintenance() {
        this.status = ZoneStatus.MAINTENANCE;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "防霉管控区域进入维护模式");
    }

    /**
     * 添加传感器
     */
    public void addSensor(SensorDevice sensor) {
        if (this.sensors == null) {
            this.sensors = new ArrayList<>();
        }
        if (this.sensors.stream().anyMatch(s -> s.getId().equals(sensor.getId()))) {
            throw new IllegalArgumentException("传感器已存在");
        }
        this.sensors.add(sensor);
        this.auditLog = AuditLog.create(LocalDateTime.now(), "添加传感器: " + sensor.getId().getValue());
    }

    /**
     * 移除传感器
     */
    public void removeSensor(SensorId sensorId) {
        if (this.sensors == null) {
            return;
        }
        this.sensors.removeIf(s -> s.getId().equals(sensorId));
        this.auditLog = AuditLog.create(LocalDateTime.now(), "移除传感器: " + sensorId.getValue());
    }

    /**
     * 添加控制器
     */
    public void addController(ControlDevice controller) {
        if (this.controllers == null) {
            this.controllers = new ArrayList<>();
        }
        if (this.controllers.stream().anyMatch(c -> c.getId().equals(controller.getId()))) {
            throw new IllegalArgumentException("控制器已存在");
        }
        this.controllers.add(controller);
        this.auditLog = AuditLog.create(LocalDateTime.now(), "添加控制器: " + controller.getId().getValue());
    }

    /**
     * 移除控制器
     */
    public void removeController(ControllerId controllerId) {
        if (this.controllers == null) {
            return;
        }
        this.controllers.removeIf(c -> c.getId().equals(controllerId));
        this.auditLog = AuditLog.create(LocalDateTime.now(), "移除控制器: " + controllerId.getValue());
    }

    /**
     * 更新风险阈值
     */
    public void updateThreshold(MoldRiskThreshold threshold) {
        this.threshold = threshold;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "更新风险阈值");
        // 重新评估当前风险
        if (this.lastEnvironmentData != null) {
            evaluateRisk(this.lastEnvironmentData);
        }
    }

    /**
     * 更新湿度控制策略
     */
    public void updateStrategy(HumidityControlStrategy strategy) {
        this.strategy = strategy;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "更新湿度控制策略");
    }

    /**
     * 评估霉菌风险
     */
    public MoldRiskEvaluationResult evaluateRisk(EnvironmentData data) {
        if (this.status != ZoneStatus.ACTIVE) {
            throw new IllegalStateException("防霉管控区域未激活，无法评估风险");
        }

        this.lastEnvironmentData = data;
        this.lastEvaluatedAt = LocalDateTime.now();

        // 计算风险等级
        MoldRiskLevel riskLevel = calculateRiskLevel(data);

        // 检查风险等级是否发生变化
        boolean riskChanged = !riskLevel.equals(this.currentRiskLevel);
        this.currentRiskLevel = riskLevel;

        // 如果风险等级高，自动触发控制策略
        List<ControlCommand> commands = new ArrayList<>();
        if (riskLevel.isHighRisk() && this.strategy != null) {
            commands = this.strategy.generateCommands(data, riskLevel);
        }

        return MoldRiskEvaluationResult.builder()
                .zoneId(this.id)
                .riskLevel(riskLevel)
                .previousRiskLevel(riskChanged ? null : riskLevel)
                .riskChanged(riskChanged)
                .environmentData(data)
                .evaluatedAt(this.lastEvaluatedAt)
                .controlCommands(commands)
                .build();
    }

    /**
     * 计算风险等级（核心算法）
     */
    private MoldRiskLevel calculateRiskLevel(EnvironmentData data) {
        if (this.threshold == null) {
            return MoldRiskLevel.LOW;
        }

        double temperature = data.getTemperature();
        double humidity = data.getHumidity();

        // 霉菌滋生条件：温度20-35°C，湿度>60%
        // 风险等级计算基于温湿度组合

        if (humidity > 85 || (temperature > 30 && humidity > 75)) {
            return MoldRiskLevel.CRITICAL;  // 极高风险
        }

        if (humidity > 75 || (temperature > 25 && humidity > 70)) {
            return MoldRiskLevel.HIGH;  // 高风险
        }

        if (humidity > 65 || (temperature > 20 && humidity > 60)) {
            return MoldRiskLevel.MEDIUM;  // 中风险
        }

        return MoldRiskLevel.LOW;  // 低风险
    }

    /**
     * 执行控制命令
     */
    public void executeControl(List<ControlCommand> commands) {
        if (this.controllers == null || this.controllers.isEmpty()) {
            throw new IllegalStateException("没有可用的控制器");
        }

        for (ControlCommand command : commands) {
            // 查找对应的控制器并执行命令
            this.controllers.stream()
                    .filter(c -> c.getId().equals(command.getControllerId()))
                    .findFirst()
                    .ifPresent(controller -> {
                        controller.execute(command);
                        this.auditLog = AuditLog.create(LocalDateTime.now(),
                                "执行控制命令: " + controller.getType() + " - " + command.getCommandType());
                    });
        }
    }

    /**
     * 检查是否需要生成防霉报告
     */
    public boolean shouldGenerateReport(LocalDateTime lastReportTime, LocalDateTime now) {
        // 默认每天生成一次报告
        return lastReportTime == null ||
                lastReportTime.plusDays(1).isBefore(now);
    }

    /**
     * 更新基本信息
     */
    public void updateInfo(ZoneName name, String description) {
        this.name = name;
        this.description = description;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "更新防霉管控区域信息");
    }

    /**
     * 创建新的防霉管控区域
     */
    public static MoldPreventionZone create(
            ZoneName name,
            ZoneCode code,
            SpaceId spaceId,
            TenantId tenantId,
            String description,
            MoldRiskThreshold threshold,
            HumidityControlStrategy strategy) {

        return MoldPreventionZone.builder()
                .id(ZoneId.generate())
                .name(name)
                .code(code)
                .spaceId(spaceId)
                .tenantId(tenantId)
                .status(ZoneStatus.INACTIVE)
                .threshold(threshold)
                .strategy(strategy)
                .sensors(new ArrayList<>())
                .controllers(new ArrayList<>())
                .currentRiskLevel(MoldRiskLevel.LOW)
                .description(description)
                .auditLog(AuditLog.create(LocalDateTime.now(), "创建防霉管控区域"))
                .version(0L)
                .build();
    }
}
