package com.hkt.iot.device.domain.repository;

import com.hkt.iot.device.domain.model.DeviceCommand;
import com.hkt.iot.device.domain.model.DeviceCommand.CommandStatus;
import com.hkt.iot.device.domain.model.DeviceCommand.CommandType;
import com.hkt.iot.domain.repository.OptimisticLockRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 设备命令仓储接口
 *
 * @author HKT IoT Team
 */
public interface DeviceCommandRepository extends OptimisticLockRepository<DeviceCommand, Long> {

    /**
     * 根据请求ID查找
     *
     * @param requestId 请求ID
     * @return 设备命令
     */
    Optional<DeviceCommand> findByRequestId(String requestId);

    /**
     * 根据设备ID查找命令
     *
     * @param deviceId 设备ID
     * @return 命令列表
     */
    List<DeviceCommand> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);

    /**
     * 根据设备和状态查找命令
     *
     * @param deviceId     设备ID
     * @param commandStatus 命令状态
     * @return 命令列表
     */
    List<DeviceCommand> findByDeviceIdAndCommandStatus(Long deviceId, CommandStatus commandStatus);

    /**
     * 查找待发送的命令
     *
     * @return 命令列表
     */
    List<DeviceCommand> findByCommandStatusOrderByPriorityAscCreatedAtAsc(CommandStatus commandStatus);

    /**
     * 查找超时的命令
     *
     * @param sentTime   发送时间阈值
     * @param commandStatus 命令状态
     * @return 命令列表
     */
    List<DeviceCommand> findByCommandStatusAndSentTimeBefore(CommandStatus commandStatus, LocalDateTime sentTime);

    /**
     * 统计设备命令数量
     *
     * @param deviceId 设备ID
     * @return 命令数量
     */
    long countByDeviceId(Long deviceId);

    /**
     * 统计按状态的命令数量
     *
     * @param commandStatus 命令状态
     * @return 命令数量
     */
    long countByCommandStatus(CommandStatus commandStatus);

    /**
     * 删除已完成的旧命令
     *
     * @param beforeTime 时间阈值
     * @return 删除数量
     */
    long deleteByCreatedAtBefore(LocalDateTime beforeTime);
}
