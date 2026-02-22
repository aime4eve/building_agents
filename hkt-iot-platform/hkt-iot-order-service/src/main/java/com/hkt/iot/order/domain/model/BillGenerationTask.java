package com.hkt.iot.order.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 账单生成任务实体
 * 用于跟踪批量生成账单的任务状态
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "bill_generation_task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BillGenerationTask extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_no", nullable = false, unique = true, length = 50)
    private String taskNo;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "energy_type", length = 20)
    @Enumerated(EnumType.STRING)
    private EnergyType energyType;

    @Column(name = "task_status", nullable = false, length = 20)
    private TaskStatus taskStatus;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "failed_count")
    private Integer failedCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "remark", length = 500)
    private String remark;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING("待执行"),
        RUNNING("执行中"),
        COMPLETED("已完成"),
        FAILED("失败"),
        PARTIAL_SUCCESS("部分成功");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 工厂方法：创建账单生成任务
     */
    public static BillGenerationTask create(
            String taskNo,
            Long tenantId,
            Integer billingYear,
            Integer billingMonth,
            EnergyType energyType,
            Long createdBy) {
        BillGenerationTask task = new BillGenerationTask();
        task.taskNo = taskNo;
        task.tenantId = tenantId;
        task.billingYear = billingYear;
        task.billingMonth = billingMonth;
        task.energyType = energyType;
        task.taskStatus = TaskStatus.PENDING;
        task.totalCount = 0;
        task.successCount = 0;
        task.failedCount = 0;
        task.createdAt = LocalDateTime.now();
        task.updatedAt = LocalDateTime.now();
        task.createdBy = createdBy;
        task.updatedBy = createdBy;
        task.version = 0L;
        return task;
    }

    /**
     * 开始执行
     */
    public void start(int totalCount) {
        if (this.taskStatus != TaskStatus.PENDING) {
            throw new IllegalStateException("只有待执行状态的任务可以开始");
        }
        this.taskStatus = TaskStatus.RUNNING;
        this.totalCount = totalCount;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 记录成功
     */
    public void recordSuccess() {
        this.successCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 记录失败
     */
    public void recordFailure() {
        this.failedCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完成任务
     */
    public void complete() {
        if (this.failedCount == 0) {
            this.taskStatus = TaskStatus.COMPLETED;
        } else if (this.successCount > 0) {
            this.taskStatus = TaskStatus.PARTIAL_SUCCESS;
        } else {
            this.taskStatus = TaskStatus.FAILED;
        }
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记失败
     */
    public void markAsFailed(String errorMessage) {
        this.taskStatus = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return this.taskStatus == TaskStatus.COMPLETED 
                || this.taskStatus == TaskStatus.PARTIAL_SUCCESS 
                || this.taskStatus == TaskStatus.FAILED;
    }

    /**
     * 获取进度百分比
     */
    public int getProgressPercentage() {
        if (totalCount == null || totalCount == 0) {
            return 0;
        }
        return (successCount + failedCount) * 100 / totalCount;
    }
}
