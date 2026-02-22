package com.hkt.iot.rule.domain.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 规则执行结果
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleExecutionResult {
    private final ExecutionStatus status;
    private final String message;
    private final List<ActionResult> actionResults;
    private final LocalDateTime executedAt;

    private RuleExecutionResult(ExecutionStatus status, String message,
                               List<ActionResult> actionResults) {
        this.status = status;
        this.message = message;
        this.actionResults = actionResults != null ? actionResults : Collections.emptyList();
        this.executedAt = LocalDateTime.now();
    }

    /**
     * 成功执行
     */
    public static RuleExecutionResult success(List<ActionResult> actionResults) {
        return new RuleExecutionResult(ExecutionStatus.SUCCESS,
                "Rule executed successfully", actionResults);
    }

    /**
     * 部分成功
     */
    public static RuleExecutionResult partial(List<ActionResult> actionResults) {
        return new RuleExecutionResult(ExecutionStatus.PARTIAL,
                "Rule executed with partial success", actionResults);
    }

    /**
     * 执行失败
     */
    public static RuleExecutionResult failed(String errorMessage) {
        return new RuleExecutionResult(ExecutionStatus.FAILED, errorMessage, null);
    }

    /**
     * 条件不匹配
     */
    public static RuleExecutionResult notMatched() {
        return new RuleExecutionResult(ExecutionStatus.NOT_MATCHED,
                "Rule conditions not matched", null);
    }

    /**
     * 条件匹配
     */
    public static RuleExecutionResult matched() {
        return new RuleExecutionResult(ExecutionStatus.MATCHED,
                "Rule conditions matched", null);
    }

    /**
     * 跳过执行
     */
    public static RuleExecutionResult skipped(String reason) {
        return new RuleExecutionResult(ExecutionStatus.SKIPPED, reason, null);
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<ActionResult> getActionResults() {
        return actionResults;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public boolean isSuccess() {
        return status == ExecutionStatus.SUCCESS;
    }

    public boolean isFailed() {
        return status == ExecutionStatus.FAILED;
    }

    public boolean isMatched() {
        return status == ExecutionStatus.MATCHED ||
               status == ExecutionStatus.SUCCESS ||
               status == ExecutionStatus.PARTIAL;
    }

    /**
     * 执行状态
     */
    public enum ExecutionStatus {
        SUCCESS,      // 执行成功
        FAILED,       // 执行失败
        PARTIAL,      // 部分成功
        NOT_MATCHED,  // 条件不匹配
        MATCHED,      // 条件匹配
        SKIPPED       // 跳过执行
    }
}
