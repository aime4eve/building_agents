package com.hkt.iot.space.interfaces.rest;

import com.hkt.iot.common.result.Result;
import com.hkt.iot.space.application.command.*;
import com.hkt.iot.space.application.dto.LogicalSpaceGroupDTO;
import com.hkt.iot.space.application.dto.SpaceDTO;
import com.hkt.iot.space.application.dto.SpacePathDTO;
import com.hkt.iot.space.application.dto.SpaceResourceDTO;
import com.hkt.iot.space.application.dto.SpaceStatisticsDTO;
import com.hkt.iot.space.application.dto.SpaceTopologyDTO;
import com.hkt.iot.space.application.query.SpaceQuery;
import com.hkt.iot.space.interfaces.rest.dto.ContainsCoordinateRequest;
import com.hkt.iot.space.interfaces.rest.dto.SetBoundsRequest;
import com.hkt.iot.space.interfaces.rest.dto.SpatialBoundsDTO;
import com.hkt.iot.space.application.service.SpaceApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 空间管理REST控制器
 * 提供空间CRUD、层级管理、资源绑定、逻辑分组等接口
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "空间管理", description = "空间相关操作接口")
public class SpaceController {

    private final SpaceApplicationService spaceApplicationService;

    // ==================== 空间 CRUD 操作 ====================

    @PostMapping("/spaces")
    @Operation(summary = "创建空间", description = "创建新的空间")
    public Result<Long> createSpace(
            @Valid @RequestBody CreateSpaceRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            // 转换为命令对象
            CreateSpaceCommand command = new CreateSpaceCommand(
                    request.getTenantId(),
                    request.getSpaceCode(),
                    request.getSpaceName(),
                    request.getSpaceType() != null ?
                            CreateSpaceCommand.SpaceTypeCommand.valueOf(request.getSpaceType().name()) : null,
                    request.getSpaceLevel(),
                    request.getParentSpaceId(),
                    request.getAddress(),
                    request.getProvince(),
                    request.getCity(),
                    request.getDistrict(),
                    request.getLongitude(),
                    request.getLatitude(),
                    request.getAltitude(),
                    request.getBoundary(),
                    request.getArea(),
                    request.getFloorNumber(),
                    request.getRoomNumber(),
                    request.getCapacity(),
                    request.getExtProperties(),
                    userId
            );

            Long spaceId = spaceApplicationService.createSpace(command);
            return Result.success(spaceId);
        } catch (Exception e) {
            log.error("创建空间失败: error={}", e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @GetMapping("/spaces/{spaceId}")
    @Operation(summary = "获取空间详情", description = "根据ID查询空间详细信息")
    public Result<SpaceDTO> getSpace(
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            SpaceDTO space = spaceApplicationService.getSpace(spaceId);
            return Result.success(space);
        } catch (Exception e) {
            log.error("获取空间详情失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @PutMapping("/spaces/{spaceId}")
    @Operation(summary = "更新空间", description = "更新空间信息")
    public Result<Void> updateSpace(
            @Parameter(description = "空间ID") @PathVariable Long spaceId,
            @Valid @RequestBody UpdateSpaceRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            // 转换为命令对象
            UpdateSpaceCommand command = new UpdateSpaceCommand(
                    spaceId,
                    request.getSpaceName(),
                    request.getAddress(),
                    request.getProvince(),
                    request.getCity(),
                    request.getDistrict(),
                    request.getLongitude(),
                    request.getLatitude(),
                    request.getAltitude(),
                    request.getBoundary(),
                    request.getArea(),
                    request.getFloorNumber(),
                    request.getRoomNumber(),
                    request.getCapacity(),
                    request.getExtProperties(),
                    userId,
                    request.getVersion()
            );

            spaceApplicationService.updateSpace(command);
            return Result.success();
        } catch (Exception e) {
            log.error("更新空间失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/spaces/{spaceId}")
    @Operation(summary = "删除空间", description = "软删除空间")
    public Result<Void> deleteSpace(
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            spaceApplicationService.deleteSpace(spaceId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除空间失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @GetMapping("/spaces")
    @Operation(summary = "查询空间列表", description = "根据条件查询空间列表")
    public Result<List<SpaceDTO>> listSpaces(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String spaceCode,
            @RequestParam(required = false) String spaceName,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        try {
            // 构建查询对象
            SpaceQuery query = new SpaceQuery(
                    tenantId,
                    null,
                    spaceCode,
                    spaceName,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    pageNum != null ? pageNum : 1,
                    pageSize != null ? pageSize : 20,
                    null,
                    null
            );

            var pageResult = spaceApplicationService.listSpaces(query);
            return Result.success(pageResult.getRecords());
        } catch (Exception e) {
            log.error("查询空间列表失败: error={}", e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    // ==================== 空间层级操作 ====================

    @GetMapping("/spaces/{spaceId}/children")
    @Operation(summary = "获取子空间", description = "获取指定空间的直接子空间列表")
    public Result<List<SpaceDTO>> getChildSpaces(
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            List<SpaceDTO> children = spaceApplicationService.getChildSpaces(spaceId);
            return Result.success(children);
        } catch (Exception e) {
            log.error("获取子空间失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @GetMapping("/spaces/tree/{rootSpaceId}")
    @Operation(summary = "获取空间树", description = "获取以指定空间为根的完整空间树结构")
    public Result<List<SpaceDTO>> getSpaceTree(
            @Parameter(description = "根空间ID") @PathVariable Long rootSpaceId) {
        try {
            List<SpaceDTO> tree = spaceApplicationService.getSpaceTree(rootSpaceId);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取空间树失败: rootSpaceId={}, error={}", rootSpaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    // ==================== 空间资源操作 ====================

    @PostMapping("/spaces/{spaceId}/resources")
    @Operation(summary = "绑定资源", description = "将资源绑定到指定空间")
    public Result<Long> bindResource(
            @Parameter(description = "空间ID") @PathVariable Long spaceId,
            @Valid @RequestBody BindResourceRequestDTO request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            // 转换为命令对象
            BindResourceCommand command = new BindResourceCommand(
                    request.getTenantId(),
                    spaceId,
                    request.getSpaceCode(),
                    request.getResourceType() != null ?
                            BindResourceCommand.ResourceTypeCommand.valueOf(request.getResourceType().name()) : null,
                    request.getResourceId(),
                    request.getResourceCode(),
                    request.getRelationType() != null ?
                            BindResourceCommand.RelationTypeCommand.valueOf(request.getRelationType().name()) : null,
                    request.getPrimaryRelation(),
                    request.getLocationDetail(),
                    request.getFloorNumber(),
                    request.getRoomNumber(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getExtProperties(),
                    userId
            );

            Long linkId = spaceApplicationService.bindResource(command);
            return Result.success(linkId);
        } catch (Exception e) {
            log.error("绑定资源失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/spaces/resources/{resourceLinkId}")
    @Operation(summary = "解绑资源", description = "解除资源与空间的绑定关系")
    public Result<Void> unbindResource(
            @Parameter(description = "资源关联ID") @PathVariable Long resourceLinkId) {
        try {
            spaceApplicationService.unbindResource(resourceLinkId);
            return Result.success();
        } catch (Exception e) {
            log.error("解绑资源失败: resourceLinkId={}, error={}", resourceLinkId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @GetMapping("/spaces/{spaceId}/resources")
    @Operation(summary = "获取空间资源列表", description = "获取指定空间绑定的所有资源")
    public Result<List<SpaceResourceDTO>> getSpaceResources(
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            List<SpaceResourceDTO> resources = spaceApplicationService.getSpaceResources(spaceId);
            return Result.success(resources);
        } catch (Exception e) {
            log.error("获取空间资源列表失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    // ==================== 逻辑分组操作 ====================

    @PostMapping("/space-groups")
    @Operation(summary = "创建逻辑分组", description = "创建新的逻辑空间分组")
    public Result<Long> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            // 转换为命令对象
            CreateLogicalSpaceGroupCommand command = new CreateLogicalSpaceGroupCommand(
                    request.getTenantId(),
                    request.getGroupCode(),
                    request.getGroupName(),
                    request.getGroupType() != null ?
                            CreateLogicalSpaceGroupCommand.GroupTypeCommand.valueOf(request.getGroupType().name()) : null,
                    request.getDescription(),
                    request.getGroupColor(),
                    request.getGroupIcon(),
                    request.getGroupRule(),
                    request.getDisplayOrder(),
                    userId
            );

            Long groupId = spaceApplicationService.createLogicalSpaceGroup(command);
            return Result.success(groupId);
        } catch (Exception e) {
            log.error("创建逻辑分组失败: error={}", e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @GetMapping("/space-groups/tenant/{tenantId}")
    @Operation(summary = "获取租户分组列表", description = "获取指定租户下的所有逻辑分组")
    public Result<List<LogicalSpaceGroupDTO>> getGroupsByTenant(
            @Parameter(description = "租户ID") @PathVariable Long tenantId) {
        try {
            List<LogicalSpaceGroupDTO> groups = spaceApplicationService.getGroupsByTenant(tenantId);
            return Result.success(groups);
        } catch (Exception e) {
            log.error("获取租户分组列表失败: tenantId={}, error={}", tenantId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @GetMapping("/space-groups/{groupId}")
    @Operation(summary = "获取分组详情", description = "根据ID查询逻辑分组详细信息")
    public Result<LogicalSpaceGroupDTO> getGroup(
            @Parameter(description = "分组ID") @PathVariable Long groupId) {
        try {
            LogicalSpaceGroupDTO group = spaceApplicationService.getGroupById(groupId);
            return Result.success(group);
        } catch (Exception e) {
            log.error("获取分组详情失败: groupId={}, error={}", groupId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @PutMapping("/space-groups/{groupId}")
    @Operation(summary = "更新逻辑分组", description = "更新逻辑分组信息")
    public Result<Void> updateGroup(
            @Parameter(description = "分组ID") @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            spaceApplicationService.updateLogicalSpaceGroup(
                    groupId,
                    request.getGroupName(),
                    request.getDescription(),
                    request.getGroupColor(),
                    request.getGroupIcon(),
                    request.getDisplayOrder(),
                    userId
            );
            return Result.success();
        } catch (Exception e) {
            log.error("更新逻辑分组失败: groupId={}, error={}", groupId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/space-groups/{groupId}")
    @Operation(summary = "删除逻辑分组", description = "软删除逻辑分组")
    public Result<Void> deleteGroup(
            @Parameter(description = "分组ID") @PathVariable Long groupId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            spaceApplicationService.deleteLogicalSpaceGroup(groupId, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除逻辑分组失败: groupId={}, error={}", groupId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @PostMapping("/space-groups/{groupId}/spaces/{spaceId}")
    @Operation(summary = "添加空间到分组", description = "将空间添加到逻辑分组")
    public Result<Void> addSpaceToGroup(
            @Parameter(description = "分组ID") @PathVariable Long groupId,
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            spaceApplicationService.addSpaceToGroup(groupId, spaceId);
            return Result.success();
        } catch (Exception e) {
            log.error("添加空间到分组失败: groupId={}, spaceId={}, error={}", groupId, spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/space-groups/{groupId}/spaces/{spaceId}")
    @Operation(summary = "从分组移除空间", description = "将空间从逻辑分组中移除")
    public Result<Void> removeSpaceFromGroup(
            @Parameter(description = "分组ID") @PathVariable Long groupId,
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            spaceApplicationService.removeSpaceFromGroup(groupId, spaceId);
            return Result.success();
        } catch (Exception e) {
            log.error("从分组移除空间失败: groupId={}, spaceId={}, error={}", groupId, spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    // ==================== 空间边界操作 ====================

    @PutMapping("/spaces/{spaceId}/bounds")
    @Operation(summary = "设置空间边界", description = "设置空间的地理坐标边界范围")
    public Result<Void> setSpaceBounds(
            @Parameter(description = "空间ID") @PathVariable Long spaceId,
            @Valid @RequestBody SetBoundsRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            spaceApplicationService.setSpaceBounds(
                    spaceId,
                    request.getNortheastLatitude(),
                    request.getNortheastLongitude(),
                    request.getSouthwestLatitude(),
                    request.getSouthwestLongitude(),
                    userId
            );
            return Result.success();
        } catch (Exception e) {
            log.error("设置空间边界失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @GetMapping("/spaces/{spaceId}/bounds")
    @Operation(summary = "获取空间边界", description = "获取空间的地理坐标边界范围")
    public Result<SpatialBoundsDTO> getSpaceBounds(
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            SpatialBoundsDTO bounds = spaceApplicationService.getSpaceBounds(spaceId);
            return Result.success(bounds);
        } catch (Exception e) {
            log.error("获取空间边界失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @PostMapping("/spaces/{spaceId}/contains")
    @Operation(summary = "判断坐标是否在边界内", description = "判断指定坐标点是否在空间边界范围内")
    public Result<Boolean> containsCoordinate(
            @Parameter(description = "空间ID") @PathVariable Long spaceId,
            @Valid @RequestBody ContainsCoordinateRequest request) {
        try {
            boolean contains = spaceApplicationService.containsCoordinate(
                    spaceId,
                    request.getLatitude(),
                    request.getLongitude()
            );
            return Result.success(contains);
        } catch (Exception e) {
            log.error("判断坐标是否在边界内失败: spaceId={}, lat={}, lon={}, error={}",
                    spaceId, request.getLatitude(), request.getLongitude(), e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    // ==================== 空间拓扑操作 ====================

    @GetMapping("/spaces/{spaceId}/topology")
    @Operation(summary = "获取空间拓扑", description = "获取空间的拓扑结构树")
    public Result<SpaceTopologyDTO> getSpaceTopology(
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            SpaceTopologyDTO topology = spaceApplicationService.getSpaceTopology(spaceId);
            return Result.success(topology);
        } catch (Exception e) {
            log.error("获取空间拓扑失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @GetMapping("/spaces/{spaceId}/path")
    @Operation(summary = "获取空间路径", description = "获取从根空间到当前空间的完整路径")
    public Result<SpacePathDTO> getSpacePath(
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            SpacePathDTO path = spaceApplicationService.getSpacePath(spaceId);
            return Result.success(path);
        } catch (Exception e) {
            log.error("获取空间路径失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    @GetMapping("/spaces/statistics")
    @Operation(summary = "获取空间统计", description = "获取租户下空间的统计信息")
    public Result<SpaceStatisticsDTO> getSpaceStatistics(
            @RequestParam Long tenantId) {
        try {
            SpaceStatisticsDTO statistics = spaceApplicationService.getSpaceStatistics(tenantId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取空间统计失败: tenantId={}, error={}", tenantId, e.getMessage(), e);
            return Result.error("500", e.getMessage());
        }
    }

    // ==================== 请求DTO类 ====================

    /**
     * 创建空间请求
     */
    @lombok.Data
    public static class CreateSpaceRequest {
        private Long tenantId;
        private String spaceCode;
        private String spaceName;
        private SpaceType spaceType;
        private Integer spaceLevel;
        private Long parentSpaceId;
        private String address;
        private String province;
        private String city;
        private String district;
        private java.math.BigDecimal longitude;
        private java.math.BigDecimal latitude;
        private java.math.BigDecimal altitude;
        private List<List<java.math.BigDecimal>> boundary;
        private java.math.BigDecimal area;
        private Integer floorNumber;
        private String roomNumber;
        private Integer capacity;
        private Map<String, Object> extProperties;
    }

    /**
     * 更新空间请求
     */
    @lombok.Data
    public static class UpdateSpaceRequest {
        private String spaceName;
        private String address;
        private String province;
        private String city;
        private String district;
        private java.math.BigDecimal longitude;
        private java.math.BigDecimal latitude;
        private java.math.BigDecimal altitude;
        private List<List<java.math.BigDecimal>> boundary;
        private java.math.BigDecimal area;
        private Integer floorNumber;
        private String roomNumber;
        private Integer capacity;
        private Map<String, Object> extProperties;
        private Long version;
    }

    /**
     * 绑定资源请求
     */
    @lombok.Data
    public static class BindResourceRequestDTO {
        private Long tenantId;
        private String spaceCode;
        private ResourceType resourceType;
        private Long resourceId;
        private String resourceCode;
        private RelationType relationType;
        private Boolean primaryRelation;
        private String locationDetail;
        private Integer floorNumber;
        private String roomNumber;
        private java.time.LocalDateTime startDate;
        private java.time.LocalDateTime endDate;
        private Map<String, Object> extProperties;
    }

    /**
     * 创建分组请求
     */
    @lombok.Data
    public static class CreateGroupRequest {
        private Long tenantId;
        private String groupCode;
        private String groupName;
        private GroupType groupType;
        private String description;
        private String groupColor;
        private String groupIcon;
        private Map<String, Object> groupRule;
        private Integer displayOrder;
    }

    /**
     * 更新分组请求
     */
    @lombok.Data
    public static class UpdateGroupRequest {
        private String groupName;
        private String description;
        private String groupColor;
        private String groupIcon;
        private Integer displayOrder;
    }

    /**
     * 空间类型枚举
     */
    public enum SpaceType {
        PARK, BUILDING, FLOOR, ROOM
    }

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        DEVICE, USER, ASSET, EQUIPMENT
    }

    /**
     * 关联类型枚举
     */
    public enum RelationType {
        OWNER, OCCUPANT, MANAGER, TEMPORARY
    }

    /**
     * 分组类型枚举
     */
    public enum GroupType {
        APPLICATION, TENANT, BUSINESS
    }
}
