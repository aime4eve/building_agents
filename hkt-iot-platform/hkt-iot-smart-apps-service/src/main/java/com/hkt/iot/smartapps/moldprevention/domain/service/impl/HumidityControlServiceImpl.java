package com.hkt.iot.smartapps.moldprevention.domain.service.impl;

import com.hkt.iot.smartapps.moldprevention.domain.model.*;
import com.hkt.iot.smartapps.moldprevention.domain.repository.MoldPreventionZoneRepository;
import com.hkt.iot.smartapps.moldprevention.domain.service.HumidityControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 湿度控制领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HumidityControlServiceImpl implements HumidityControlService {

    private final MoldPreventionZoneRepository zoneRepository;

    @Override
    public List<ControlCommand> generateControlCommands(ZoneId zoneId, MoldRiskLevel riskLevel) {
        log.debug("生成控制命令: zoneId={}, riskLevel={}", zoneId.getValue(), riskLevel);
        
        MoldPreventionZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + zoneId.getValue()));
        
        List<ControlDevice> controllers = zone.getControllers();
        if (controllers == null || controllers.isEmpty()) {
            log.warn("区域没有配置控制设备: zoneId={}", zoneId.getValue());
            return new ArrayList<>();
        }
        
        List<ControlCommand> commands = new ArrayList<>();
        
        switch (riskLevel) {
            case CRITICAL:
                commands.addAll(generateCriticalCommands(zone, controllers));
                break;
            case HIGH:
                commands.addAll(generateHighRiskCommands(zone, controllers));
                break;
            case MEDIUM:
                commands.addAll(generateMediumRiskCommands(zone, controllers));
                break;
            case LOW:
            default:
                break;
        }
        
        return commands;
    }

    @Override
    public void executeControl(ZoneId zoneId, List<ControlCommand> commands) {
        log.debug("执行控制: zoneId={}, commandCount={}", zoneId.getValue(), commands.size());
        
        MoldPreventionZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + zoneId.getValue()));
        
        zone.executeControl(commands);
        
        zoneRepository.save(zone);
    }

    @Override
    public ControlEffectiveness evaluateControlEffectiveness(ZoneId zoneId) {
        log.debug("评估控制效果: zoneId={}", zoneId.getValue());
        
        MoldPreventionZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + zoneId.getValue()));
        
        EnvironmentData lastData = zone.getLastEnvironmentData();
        MoldRiskLevel currentLevel = zone.getCurrentRiskLevel();
        
        return ControlEffectiveness.builder()
                .zoneId(zoneId)
                .currentRiskLevel(currentLevel)
                .currentHumidity(lastData != null ? lastData.getHumidity() : 0)
                .targetHumidity(55.0)
                .effectivenessScore(calculateEffectivenessScore(lastData))
                .evaluatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public void autoAdjust(ZoneId zoneId) {
        log.debug("自动调节湿度: zoneId={}", zoneId.getValue());
        
        MoldPreventionZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + zoneId.getValue()));
        
        if (zone.getStatus() != ZoneStatus.ACTIVE) {
            log.warn("区域未激活，跳过自动调节: zoneId={}", zoneId.getValue());
            return;
        }
        
        MoldRiskLevel riskLevel = zone.getCurrentRiskLevel();
        if (riskLevel == null) {
            return;
        }
        
        List<ControlCommand> commands = generateControlCommands(zoneId, riskLevel);
        if (!commands.isEmpty()) {
            executeControl(zoneId, commands);
        }
    }

    @Override
    public void stopAllControllers(ZoneId zoneId) {
        log.debug("停止所有控制设备: zoneId={}", zoneId.getValue());
        
        MoldPreventionZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + zoneId.getValue()));
        
        List<ControlDevice> controllers = zone.getControllers();
        if (controllers == null) {
            return;
        }
        
        List<ControlCommand> stopCommands = new ArrayList<>();
        for (ControlDevice controller : controllers) {
            ControlCommand stopCommand = ControlCommand.builder()
                    .controllerId(controller.getId())
                    .commandType("STOP")
                    .parameters(new java.util.HashMap<>())
                    .createdAt(LocalDateTime.now())
                    .build();
            stopCommands.add(stopCommand);
        }
        
        executeControl(zoneId, stopCommands);
    }

    @Override
    public List<ControlCommand> getControlHistory(ZoneId zoneId, int limit) {
        log.debug("获取控制历史: zoneId={}, limit={}", zoneId.getValue(), limit);
        
        return new ArrayList<>();
    }

    private List<ControlCommand> generateCriticalCommands(MoldPreventionZone zone, List<ControlDevice> controllers) {
        List<ControlCommand> commands = new ArrayList<>();
        
        for (ControlDevice controller : controllers) {
            ControlCommand command = null;
            
            switch (controller.getType()) {
                case DEHUMIDIFIER:
                    command = ControlCommand.builder()
                            .controllerId(controller.getId())
                            .commandType("SET_MODE")
                            .parameters(createParameters("mode", "TURBO", "targetHumidity", 45))
                            .createdAt(LocalDateTime.now())
                            .build();
                    break;
                case AIR_CONDITIONER:
                    command = ControlCommand.builder()
                            .controllerId(controller.getId())
                            .commandType("SET_MODE")
                            .parameters(createParameters("mode", "DEHUMIDIFY", "targetHumidity", 45, "temperature", 22))
                            .createdAt(LocalDateTime.now())
                            .build();
                    break;
                case VENTILATION:
                    command = ControlCommand.builder()
                            .controllerId(controller.getId())
                            .commandType("SET_SPEED")
                            .parameters(createParameters("speed", "HIGH"))
                            .createdAt(LocalDateTime.now())
                            .build();
                    break;
                default:
                    break;
            }
            
            if (command != null) {
                commands.add(command);
            }
        }
        
        return commands;
    }

    private List<ControlCommand> generateHighRiskCommands(MoldPreventionZone zone, List<ControlDevice> controllers) {
        List<ControlCommand> commands = new ArrayList<>();
        
        for (ControlDevice controller : controllers) {
            ControlCommand command = null;
            
            switch (controller.getType()) {
                case DEHUMIDIFIER:
                    command = ControlCommand.builder()
                            .controllerId(controller.getId())
                            .commandType("SET_MODE")
                            .parameters(createParameters("mode", "STANDARD", "targetHumidity", 50))
                            .createdAt(LocalDateTime.now())
                            .build();
                    break;
                case AIR_CONDITIONER:
                    command = ControlCommand.builder()
                            .controllerId(controller.getId())
                            .commandType("SET_MODE")
                            .parameters(createParameters("mode", "DEHUMIDIFY", "targetHumidity", 50))
                            .createdAt(LocalDateTime.now())
                            .build();
                    break;
                case VENTILATION:
                    command = ControlCommand.builder()
                            .controllerId(controller.getId())
                            .commandType("SET_SPEED")
                            .parameters(createParameters("speed", "MEDIUM"))
                            .createdAt(LocalDateTime.now())
                            .build();
                    break;
                default:
                    break;
            }
            
            if (command != null) {
                commands.add(command);
            }
        }
        
        return commands;
    }

    private List<ControlCommand> generateMediumRiskCommands(MoldPreventionZone zone, List<ControlDevice> controllers) {
        List<ControlCommand> commands = new ArrayList<>();
        
        for (ControlDevice controller : controllers) {
            ControlCommand command = null;
            
            switch (controller.getType()) {
                case DEHUMIDIFIER:
                    command = ControlCommand.builder()
                            .controllerId(controller.getId())
                            .commandType("SET_MODE")
                            .parameters(createParameters("mode", "QUIET", "targetHumidity", 55))
                            .createdAt(LocalDateTime.now())
                            .build();
                    break;
                case VENTILATION:
                    command = ControlCommand.builder()
                            .controllerId(controller.getId())
                            .commandType("SET_SPEED")
                            .parameters(createParameters("speed", "LOW"))
                            .createdAt(LocalDateTime.now())
                            .build();
                    break;
                default:
                    break;
            }
            
            if (command != null) {
                commands.add(command);
            }
        }
        
        return commands;
    }

    private java.util.Map<String, Object> createParameters(Object... keyValues) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i + 1 < keyValues.length) {
                params.put(keyValues[i].toString(), keyValues[i + 1]);
            }
        }
        return params;
    }

    private int calculateEffectivenessScore(EnvironmentData data) {
        if (data == null) {
            return 0;
        }
        
        double humidity = data.getHumidity();
        double targetHumidity = 55.0;
        
        double deviation = Math.abs(humidity - targetHumidity);
        
        if (deviation <= 5) {
            return 100;
        } else if (deviation <= 10) {
            return 80;
        } else if (deviation <= 15) {
            return 60;
        } else if (deviation <= 20) {
            return 40;
        } else {
            return 20;
        }
    }
}
