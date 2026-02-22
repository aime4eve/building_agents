package com.huakuangtong.iot.ingestion.service.impl;

/**
 * 状态同步异常
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
public class StatusSyncException extends RuntimeException {

    public StatusSyncException(String message) {
        super(message);
    }

    public StatusSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
