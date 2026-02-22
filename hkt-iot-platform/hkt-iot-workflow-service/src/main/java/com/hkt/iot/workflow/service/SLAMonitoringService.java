package com.hkt.iot.workflow.service;

import com.hkt.iot.workflow.listener.AuditLogListener.AuditLogEntry;
import com.hkt.iot.workflow.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志服务