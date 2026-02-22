package com.hkt.iot.order.domain.service;

import com.hkt.iot.order.domain.model.Invoice;
import com.hkt.iot.order.domain.model.InvoiceStatus;
import com.hkt.iot.order.domain.model.Order;
import com.hkt.iot.order.domain.repository.InvoiceRepository;
import com.hkt.iot.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 发票服务
 * 领域服务，负责发票相关业务逻辑
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    /**
     * 申请开票
     *
     * @param orderId 订单ID
     * @param invoiceType 发票类型
     * @param titleType 抬头类型
     * @param title 发票抬头
     * @param taxNo 税号
     * @param operatorId 操作人ID
     * @return 发票
     */
    @Transactional
    public Invoice applyInvoice(Long orderId, String invoiceType, String titleType,
                                 String title, String taxNo, Long operatorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));

        if (invoiceRepository.existsByOrderIdAndInvoiceStatus(orderId, InvoiceStatus.ISSUED)) {
            throw new IllegalStateException("订单已开具发票");
        }

        Invoice invoice = Invoice.create(
                orderId,
                order.getOrderNo(),
                order.getTenantId(),
                invoiceType,
                titleType,
                title,
                taxNo,
                order.getTotalAmount(),
                operatorId
        );

        invoice = invoiceRepository.save(invoice);

        log.info("申请开票成功: orderId={}, invoiceId={}", orderId, invoice.getId());
        return invoice;
    }

    /**
     * 开具发票
     *
     * @param invoiceId 发票ID
     * @param pdfUrl PDF文件URL
     * @param operatorId 操作人ID
     * @return 发票
     */
    @Transactional
    public Invoice issueInvoice(Long invoiceId, String pdfUrl, Long operatorId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("发票不存在: " + invoiceId));

        if (invoice.isIssued()) {
            log.info("发票已开具，幂等处理: invoiceId={}", invoiceId);
            return invoice;
        }

        String invoiceNo = generateInvoiceNo();
        invoice.issue(invoiceNo, pdfUrl, operatorId);

        invoice = invoiceRepository.save(invoice);

        log.info("开具发票成功: invoiceId={}, invoiceNo={}", invoiceId, invoiceNo);
        return invoice;
    }

    /**
     * 作废发票
     *
     * @param invoiceId 发票ID
     * @param reason 作废原因
     * @param operatorId 操作人ID
     * @return 发票
     */
    @Transactional
    public Invoice voidInvoice(Long invoiceId, String reason, Long operatorId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("发票不存在: " + invoiceId));

        if (!invoice.canVoid()) {
            throw new IllegalStateException("发票状态不允许作废: " + invoice.getInvoiceStatus().getDescription());
        }

        invoice.voidInvoice(reason, operatorId);

        invoice = invoiceRepository.save(invoice);

        log.info("作废发票成功: invoiceId={}, reason={}", invoiceId, reason);
        return invoice;
    }

    /**
     * 设置发票银行信息
     *
     * @param invoiceId 发票ID
     * @param bankName 银行名称
     * @param bankAccount 银行账号
     * @return 发票
     */
    @Transactional
    public Invoice setBankInfo(Long invoiceId, String bankName, String bankAccount) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("发票不存在: " + invoiceId));

        invoice.setBankInfo(bankName, bankAccount);

        return invoiceRepository.save(invoice);
    }

    /**
     * 设置发票地址电话
     *
     * @param invoiceId 发票ID
     * @param address 地址
     * @param phone 电话
     * @return 发票
     */
    @Transactional
    public Invoice setAddressAndPhone(Long invoiceId, String address, String phone) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("发票不存在: " + invoiceId));

        invoice.setAddressAndPhone(address, phone);

        return invoiceRepository.save(invoice);
    }

    /**
     * 生成发票编号
     */
    private String generateInvoiceNo() {
        return "INV" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
