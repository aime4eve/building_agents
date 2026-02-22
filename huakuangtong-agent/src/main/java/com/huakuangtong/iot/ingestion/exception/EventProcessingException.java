package com.huakuangtong.iot.ingestion.service.impl;

/**
 * 事件处理异常
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
public class EventProcessingException extends RuntimeException {

    public EventProcessingException(String message) {
        super(message);
    }

    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
