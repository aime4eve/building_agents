package com.huakuangtong.iot.ingestion.service.impl;

/**
 * 遥测数据处理异常
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
public class TelemetryProcessingException extends RuntimeException {

    public TelemetryProcessingException(String message) {
        super(message);
    }

    public TelemetryProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
