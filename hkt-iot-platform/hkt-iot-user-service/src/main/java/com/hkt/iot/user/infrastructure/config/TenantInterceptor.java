package com.hkt.iot.user.infrastructure.config;

import com.hkt.iot.common.core.context.TenantContext;
import com.hkt.iot.common.core.exception.BizException;
import com.hkt.iot.common.core.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 多租户拦截器
 * 从请求头中获取租户信息并设置到上下文中
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_ID_HEADER = "X-Tenant-Id";
    private static final String TENANT_CODE_HEADER = "X-Tenant-Code";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取租户信息
        String tenantIdStr = request.getHeader(TENANT_ID_HEADER);
        String tenantCode = request.getHeader(TENANT_CODE_HEADER);

        if (tenantIdStr != null && !tenantIdStr.isEmpty()) {
            try {
                Long tenantId = Long.parseLong(tenantIdStr);
                TenantContext.setTenantId(tenantId);
                log.debug("设置租户上下文: tenantId={}", tenantId);
            } catch (NumberFormatException e) {
                throw new BizException(ResultCode.INVALID_TENANT_ID);
            }
        }

        if (tenantCode != null && !tenantCode.isEmpty()) {
            TenantContext.setTenantCode(tenantCode);
            log.debug("设置租户编码: tenantCode={}", tenantCode);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        // 清除租户上下文
        TenantContext.clear();
    }
}
