package com.hkt.iot.order.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 能耗账单相关DTO
 *
 * @author HKT IoT Team
 */
public class EnergyBillDTO {

    /**
     * 创建账单请求
     */
    @Data
    @Builder
    public static class CreateBillRequest {
        private Long tenantId;
        private String tenantCode;
        private Long spaceId;
        private String spaceName;
        private String energyType;
        private Integer billingYear;
        private Integer billingMonth;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        private BigDecimal previousReading;
        private BigDecimal currentReading;
        private BigDecimal unitPrice;
        private Long meterId;
        private String meterNo;
        private String remark;
    }

    /**
     * 更新账单请求
     */
    @Data
    @Builder
    public static class UpdateBillRequest {
        private BigDecimal previousReading;
        private BigDecimal currentReading;
        private BigDecimal unitPrice;
        private BigDecimal baseCharge;
        private BigDecimal adjustmentAmount;
        private String remark;
    }

    /**
     * 账单查询请求
     */
    @Data
    @Builder
    public static class BillQueryRequest {
        private Long tenantId;
        private Long spaceId;
        private String billNo;
        private String energyType;
        private String billStatus;
        private Integer billingYear;
        private Integer billingMonth;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    /**
     * 生成月度账单请求
     */
    @Data
    @Builder
    public static class GenerateMonthlyBillsRequest {
        private Long tenantId;
        private Integer billingYear;
        private Integer billingMonth;
        private String energyType;
    }

    /**
     * 账单响应
     */
    @Data
    @Builder
    public static class BillResponse {
        private Long id;
        private String billNo;
        private Long tenantId;
        private Long spaceId;
        private String spaceName;
        private String energyType;
        private String billStatus;
        private Integer billingYear;
        private Integer billingMonth;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        private BigDecimal previousReading;
        private BigDecimal currentReading;
        private BigDecimal usageAmount;
        private BigDecimal unitPrice;
        private BigDecimal baseCharge;
        private BigDecimal adjustmentAmount;
        private BigDecimal totalAmount;
        private String currency;
        private Long orderId;
        private Long meterId;
        private String meterNo;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime confirmedAt;
        private Long confirmedBy;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime paidAt;
        private String remark;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        private List<BillItemResponse> items;
    }

    /**
     * 账单明细响应
     */
    @Data
    @Builder
    public static class BillItemResponse {
        private Long id;
        private String itemType;
        private String itemName;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String description;
        private Integer sortOrder;
    }

    /**
     * 账单生成任务响应
     */
    @Data
    @Builder
    public static class BillTaskResponse {
        private Long id;
        private String taskNo;
        private Long tenantId;
        private Integer billingYear;
        private Integer billingMonth;
        private String energyType;
        private String taskStatus;
        private Integer totalCount;
        private Integer successCount;
        private Integer failedCount;
        private Integer progressPercentage;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startedAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime completedAt;
        private String errorMessage;
        private String remark;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }

    /**
     * 账单统计响应
     */
    @Data
    @Builder
    public static class BillStatisticsResponse {
        private Long tenantId;
        private Integer billingYear;
        private Integer billingMonth;
        private Long totalBills;
        private Long pendingBills;
        private Long confirmedBills;
        private Long paidBills;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal unpaidAmount;
    }
}
