package com.hkt.iot.order.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发票请求DTO
 *
 * @author HKT IoT Team
 */
public class InvoiceDTO {

    /**
     * 申请发票请求
     */
    @Data
    @Builder
    public static class ApplyInvoiceRequest {
        private Long orderId;
        private String invoiceType;
        private String titleType;
        private String title;
        private String taxNo;
        private String bankName;
        private String bankAccount;
        private String address;
        private String phone;
    }

    /**
     * 开具发票请求
     */
    @Data
    @Builder
    public static class IssueInvoiceRequest {
        private Long invoiceId;
        private String pdfUrl;
    }

    /**
     * 作废发票请求
     */
    @Data
    @Builder
    public static class VoidInvoiceRequest {
        private Long invoiceId;
        private String reason;
    }
}
