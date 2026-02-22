package com.hkt.iot.user.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户相关DTO
 *
 * @author HKT IoT Team
 */
public class TenantDTO {

    /**
     * 租户创建请求
     */
    @Data
    @Builder
    public static class TenantCreateRequest {
        /**
         * 租户编码
         */
        private String tenantCode;

        /**
         * 租户名称
         */
        private String tenantName;

        /**
         * 租户类型
         */
        private String tenantType;

        /**
         * 父租户ID
         */
        private Long parentTenantId;

        /**
         * 联系人
         */
        private String contactPerson;

        /**
         * 联系电话
         */
        private String contactPhone;

        /**
         * 联系邮箱
         */
        private String contactEmail;

        /**
         * 地址
         */
        private String address;

        /**
         * 行业
         */
        private String industry;

        /**
         * 公司规模
         */
        private String companySize;

        /**
         * 营业执照号
         */
        private String businessLicense;

        /**
         * 最大用户数
         */
        private Integer maxUsers;

        /**
         * 最大设备数
         */
        private Integer maxDevices;

        /**
         * 最大空间数
         */
        private Integer maxSpaces;

        /**
         * 存储配额(字节)
         */
        private Long storageQuota;
    }

    /**
     * 租户更新请求
     */
    @Data
    @Builder
    public static class TenantUpdateRequest {
        private String tenantName;
        private String contactPerson;
        private String contactPhone;
        private String contactEmail;
        private String address;
        private String industry;
        private String companySize;
        private String businessLicense;
    }

    /**
     * 租户配额请求
     */
    @Data
    @Builder
    public static class TenantQuotaRequest {
        private Integer maxUsers;
        private Integer maxDevices;
        private Integer maxSpaces;
        private Long storageQuota;
    }

    /**
     * 租户查询请求
     */
    @Data
    @Builder
    public static class TenantQueryRequest {
        private String tenantCode;
        private String tenantName;
        private String tenantType;
        private String tenantStatus;
        private Long parentTenantId;
    }

    /**
     * 租户响应
     */
    @Data
    @Builder
    public static class TenantResponse {
        private Long id;
        private String tenantCode;
        private String tenantName;
        private String tenantType;
        private Long parentTenantId;
        private String tenantPath;
        private Integer tenantLevel;
        private String contactPerson;
        private String contactPhone;
        private String contactEmail;
        private String address;
        private String industry;
        private String companySize;
        private String businessLicense;
        private Integer maxUsers;
        private Integer maxDevices;
        private Integer maxSpaces;
        private Long storageQuota;
        private String tenantStatus;
        private Integer currentUserCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime activateDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime expireDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
    }
}
