package com.hkt.iot.order.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.order.domain.model.Invoice;
import com.hkt.iot.order.domain.model.InvoiceStatus;

import java.util.List;
import java.util.Optional;

/**
 * 发票仓储接口
 *
 * @author HKT IoT Team
 */
public interface InvoiceRepository extends BaseRepository<Invoice, Long> {

    /**
     * 根据发票编号查找
     */
    Optional<Invoice> findByInvoiceNo(String invoiceNo);

    /**
     * 根据订单ID查找发票
     */
    List<Invoice> findByOrderId(Long orderId);

    /**
     * 根据订单ID查找已开具的发票
     */
    Optional<Invoice> findIssuedInvoiceByOrderId(Long orderId);

    /**
     * 根据租户ID查找发票
     */
    List<Invoice> findByTenantId(Long tenantId);

    /**
     * 根据状态查找发票
     */
    List<Invoice> findByInvoiceStatus(InvoiceStatus invoiceStatus);

    /**
     * 检查发票编号是否存在
     */
    boolean existsByInvoiceNo(String invoiceNo);

    /**
     * 检查订单是否已开发票
     */
    boolean existsByOrderIdAndInvoiceStatus(Long orderId, InvoiceStatus status);

    /**
     * 根据税号查找发票
     */
    List<Invoice> findByTaxNo(String taxNo);
}
