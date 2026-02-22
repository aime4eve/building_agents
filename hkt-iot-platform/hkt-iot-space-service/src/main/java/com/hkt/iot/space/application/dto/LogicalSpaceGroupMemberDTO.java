package com.hkt.iot.space.application.dto;

import com.hkt.iot.space.domain.model.LogicalSpaceGroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 逻辑空间分组成员传输对象
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogicalSpaceGroupMemberDTO {

    private Long id;
    private Long tenantId;
    private Long groupId;
    private String groupCode;
    private Long spaceId;
    private String spaceCode;
    private String spaceName;
    private Integer memberOrder;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    /**
     * 从领域模型转换为DTO
     */
    public static LogicalSpaceGroupMemberDTO from(LogicalSpaceGroupMember member) {
        return LogicalSpaceGroupMemberDTO.builder()
                .id(member.getId())
                .tenantId(member.getTenantId())
                .groupId(member.getGroupId())
                .groupCode(member.getGroupCode())
                .spaceId(member.getSpaceId())
                .spaceCode(member.getSpaceCode())
                .spaceName(member.getSpaceName())
                .memberOrder(member.getMemberOrder())
                .isPinned(member.getIsPinned())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .createdBy(member.getCreatedBy())
                .updatedBy(member.getUpdatedBy())
                .build();
    }
}
