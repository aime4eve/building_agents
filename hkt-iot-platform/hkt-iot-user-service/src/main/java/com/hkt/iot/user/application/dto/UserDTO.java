package com.hkt.iot.user.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户相关DTO
 *
 * @author HKT IoT Team
 */
public class UserDTO {

    /**
     * 用户创建请求
     */
    @Data
    @Builder
    public static class UserCreateRequest {
        /**
         * 租户ID
         */
        private Long tenantId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 密码
         */
        private String password;

        /**
         * 部门
         */
        private String department;

        /**
         * 职位
         */
        private String position;

        /**
         * 员工编号
         */
        private String employeeId;

        /**
         * 角色ID列表
         */
        private List<Long> roleIds;
    }

    /**
     * 用户更新请求
     */
    @Data
    @Builder
    public static class UserUpdateRequest {
        private String realName;
        private String email;
        private String phone;
        private String department;
        private String position;
        private String employeeId;
        private String avatar;
    }

    /**
     * 用户查询请求
     */
    @Data
    @Builder
    public static class UserQueryRequest {
        private Long tenantId;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String userStatus;
        private String department;
    }

    /**
     * 用户响应
     */
    @Data
    @Builder
    public static class UserResponse {
        private Long id;
        private Long tenantId;
        private String tenantCode;
        private String tenantName;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String userStatus;
        private String accountType;
        private Boolean isMfaEnabled;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;
        private String lastLoginIp;
        private String avatar;
        private String department;
        private String position;
        private String employeeId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        private List<String> roles;
    }

    /**
     * 角色响应
     */
    @Data
    @Builder
    public static class RoleResponse {
        private Long id;
        private Long tenantId;
        private String roleCode;
        private String roleName;
        private String roleType;
        private String description;
        private Boolean isDefault;
        private String status;
    }
}
