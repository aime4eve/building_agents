package com.hkt.iot.order.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票实体
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "invoice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_no", unique = true, length = 50)
    private String invoiceNo;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "invoice_type", length = 20)
    private String invoiceType;

    @Column(name = "invoice_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus invoiceStatus;

    @Column(name = "title_type", length = 20)
    private String titleType;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "tax_no", length = 50)
    private String taxNo;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "void_reason", length = 500)
    private String voidReason;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

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

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 工厂方法：创建发票
     */
    public static Invoice create(
            Long orderId,
            String orderNo,
            Long tenantId,
            String invoiceType,
            String titleType,
            String title,
            String taxNo,
            BigDecimal amount,
            Long createdBy) {
        Invoice invoice = new Invoice();
        invoice.orderId = orderId;
        invoice.orderNo = orderNo;
        invoice.tenantId = tenantId;
        invoice.invoiceType = invoiceType;
        invoice.titleType = titleType;
        invoice.title = title;
        invoice.taxNo = taxNo;
        invoice.invoiceStatus = InvoiceStatus.PENDING;
        invoice.amount = amount;
        invoice.taxAmount = BigDecimal.ZERO;
        invoice.currency = "CNY";
        invoice.deleted = false;
        invoice.createdAt = LocalDateTime.now();
        invoice.updatedAt = LocalDateTime.now();
        invoice.createdBy = createdBy;
        invoice.updatedBy = createdBy;
        invoice.version = 0L;
        return invoice;
    }

    /**
     * 开具发票
     */
    public void issue(String invoiceNo, String pdfUrl, Long operatorId) {
        if (this.invoiceStatus != InvoiceStatus.PENDING) {
            throw new IllegalStateException("只有待开票状态的发票可以开具");
        }
        this.invoiceNo = invoiceNo;
        this.invoiceStatus = InvoiceStatus.ISSUED;
        this.pdfUrl = pdfUrl;
        this.issuedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 作废发票
     */
    public void voidInvoice(String reason, Long operatorId) {
        if (this.invoiceStatus != InvoiceStatus.ISSUED) {
            throw new IllegalStateException("只有已开票状态的发票可以作废");
        }
        this.invoiceStatus = InvoiceStatus.VOIDED;
        this.voidReason = reason;
        this.voidedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 设置银行信息
     */
    public void setBankInfo(String bankName, String bankAccount) {
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置地址电话
     */
    public void setAddressAndPhone(String address, String phone) {
        this.address = address;
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置税额
     */
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
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
     * 是否已开具
     */
    public boolean isIssued() {
        return this.invoiceStatus == InvoiceStatus.ISSUED;
    }

    /**
     * 是否可作废
     */
    public boolean canVoid() {
        return this.invoiceStatus == InvoiceStatus.ISSUED;
    }
}
