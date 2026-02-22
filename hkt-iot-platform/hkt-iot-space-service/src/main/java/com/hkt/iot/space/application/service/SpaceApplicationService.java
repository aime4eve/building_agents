package com.hkt.iot.space.application.service;

import com.hkt.iot.common.exception.BizException;
import com.hkt.iot.common.exception.ErrorCode;
import com.hkt.iot.common.result.PageResult;
import com.hkt.iot.space.application.command.*;
import com.hkt.iot.space.application.dto.LogicalSpaceGroupDTO;
import com.hkt.iot.space.application.dto.SpaceDTO;
import com.hkt.iot.space.application.dto.SpaceResourceDTO;
import com.hkt.iot.space.application.query.SpaceQuery;
import com.hkt.iot.space.domain.model.*;
import com.hkt.iot.space.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空间应用服务
 * 负责空间管理的应用层业务逻辑编排
 *
 * @author HKT IoT Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpaceApplicationService {

    private final SpaceRepository spaceRepository;
    private final SpaceResourceRepository spaceResourceRepository;
    private final LogicalSpaceGroupRepository logicalSpaceGroupRepository;
    private final LogicalSpaceGroupMemberRepository logicalSpaceGroupMemberRepository;

    // ==================== 空间 CRUD 操作 ====================

    /**
     * 创建空间
     *
     * @param command 创建空间命令
     * @return 空间ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createSpace(CreateSpaceCommand command) {
        log.info("创建空间: tenantId={}, spaceCode={}, spaceName={}",
                command.getTenantId(), command.getSpaceCode(), command.getSpaceName());

        // 验证命令对象
        command.validate();

        // 检查空间编码是否已存在
        if (spaceRepository.existsByTenantIdAndSpaceCode(command.getTenantId(), command.getSpaceCode())) {
            throw new BizException(ErrorCode.RESOURCE_ALREADY_EXISTS, "空间编码已存在: " + command.getSpaceCode());
        }

        // 验证父空间
        String parentPath = null;
        if (command.getParentSpaceId() != null) {
            Space parentSpace = spaceRepository.findById(command.getParentSpaceId())
                    .orElseThrow(() -> new BizException(ErrorCode.SPACE_PARENT_INVALID, "父空间不存在: " + command.getParentSpaceId()));

            // 验证父空间状态
            if (parentSpace.getSpaceStatus() == Space.SpaceStatus.INACTIVE) {
                throw new BizException(ErrorCode.SPACE_PARENT_INVALID, "父空间已停用，无法添加子空间");
            }

            // 验证空间层级
            parentSpace.addChildSpace(command.getParentSpaceId());

            parentPath = parentSpace.getSpacePath();
        }

        // 转换空间类型
        Space.SpaceType spaceType = convertSpaceType(command.getSpaceType());

        // 创建空间
        Space space = Space.create(
                command.getTenantId(),
                command.getSpaceCode(),
                command.getSpaceName(),
                spaceType,
                command.getSpaceLevel(),
                command.getCreatedBy()
        );

        // 设置父空间
        if (command.getParentSpaceId() != null) {
            space.setParentSpaceId(command.getParentSpaceId());
            space.updateSpacePath(parentPath);
        }

        // 设置位置信息
        if (command.getAddress() != null || command.getLongitude() != null) {
            space.setLocation(
                    command.getAddress(),
                    command.getProvince(),
                    command.getCity(),
                    command.getDistrict(),
                    command.getLongitude(),
                    command.getLatitude(),
                    command.getAltitude()
            );
        }

        // 设置边界信息
        if (command.getBoundary() != null) {
            space.setBoundary(command.getBoundary(), command.getArea());
        }

        // 设置楼层数据
        if (command.getFloorNumber() != null) {
            space.setFloorNumber(command.getFloorNumber());
        }

        // 设置房间号
        if (command.getRoomNumber() != null) {
            space.setRoomNumber(command.getRoomNumber());
        }

        // 设置容量
        if (command.getCapacity() != null) {
            space.setCapacity(command.getCapacity());
        }

        // 保存空间
        Space savedSpace = spaceRepository.save(space);

        log.info("空间创建成功: spaceId={}, spaceCode={}", savedSpace.getId(), savedSpace.getSpaceCode());
        return savedSpace.getId();
    }

    /**
     * 更新空间
     *
     * @param command 更新空间命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSpace(UpdateSpaceCommand command) {
        log.info("更新空间: spaceId={}", command.getSpaceId());

        // 验证命令对象
        command.validate();

        // 获取空间
        Space space = spaceRepository.findById(command.getSpaceId())
                .orElseThrow(() -> new BizException(ErrorCode.SPACE_NOT_FOUND, "空间不存在: " + command.getSpaceId()));

        // 验证版本号（乐观锁）
        if (!space.getVersion().equals(command.getVersion())) {
            throw new BizException(ErrorCode.DB_OPTIMISTIC_LOCK_FAILED, "数据已被其他用户修改，请刷新后重试");
        }

        // 更新空间名称
        space.setSpaceName(command.getSpaceName());

        // 更新位置信息
        if (command.getAddress() != null || command.getLongitude() != null) {
            space.setLocation(
                    command.getAddress(),
                    command.getProvince(),
                    command.getCity(),
                    command.getDistrict(),
                    command.getLongitude(),
                    command.getLatitude(),
                    command.getAltitude()
            );
        }

        // 更新边界信息
        if (command.getBoundary() != null) {
            space.setBoundary(command.getBoundary(), command.getArea());
        }

        // 更新楼层数据
        if (command.getFloorNumber() != null) {
            space.setFloorNumber(command.getFloorNumber());
        }

        // 更新房间号
        if (command.getRoomNumber() != null) {
            space.setRoomNumber(command.getRoomNumber());
        }

        // 更新容量
        if (command.getCapacity() != null) {
            space.setCapacity(command.getCapacity());
        }

        space.setUpdatedBy(command.getUpdatedBy());

        // 保存更新
        spaceRepository.save(space);

        log.info("空间更新成功: spaceId={}", command.getSpaceId());
    }

    /**
     * 删除空间
     *
     * @param spaceId 空间ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpace(Long spaceId) {
        log.info("删除空间: spaceId={}", spaceId);

        // 获取空间
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new BizException(ErrorCode.SPACE_NOT_FOUND, "空间不存在: " + spaceId));

        // 检查是否有子空间
        List<Space> childSpaces = spaceRepository.findByParentSpaceId(spaceId);
        if (!childSpaces.isEmpty()) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "空间存在子空间，无法删除");
        }

        // 检查是否有关联资源
        long resourceCount = spaceResourceRepository.countBySpaceId(spaceId);
        if (resourceCount > 0) {
            throw new BizException(ErrorCode.SPACE_HAS_DEVICES, "空间存在关联资源，无法删除");
        }

        // 软删除空间
        space.softDelete(space.getUpdatedBy());
        spaceRepository.save(space);

        // 删除空间的所有分组关联
        logicalSpaceGroupMemberRepository.deleteBySpaceId(spaceId);

        log.info("空间删除成功: spaceId={}", spaceId);
    }

    /**
     * 获取空间详情
     *
     * @param spaceId 空间ID
     * @return 空间DTO
     */
    @Transactional(readOnly = true)
    public SpaceDTO getSpace(Long spaceId) {
        log.info("获取空间详情: spaceId={}", spaceId);

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new BizException(ErrorCode.SPACE_NOT_FOUND, "空间不存在: " + spaceId));

        return toSpaceDTO(space);
    }

    /**
     * 分页查询空间列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<SpaceDTO> listSpaces(SpaceQuery query) {
        log.info("查询空间列表: tenantId={}, pageNum={}, pageSize={}",
                query.getTenantId(), query.getPageNum(), query.getPageSize());

        // 构建分页参数
        Sort sort = buildSort(query);
        Pageable pageable = PageRequest.of(query.getPageNum() - 1, query.getPageSize(), sort);

        // 查询数据（这里简化处理，实际应该使用Specification或QueryDSL）
        Page<Space> page = spaceRepository.findAll(pageable);

        // 转换DTO
        List<SpaceDTO> dtos = page.getContent().stream()
                .map(this::toSpaceDTO)
                .collect(Collectors.toList());

        return PageResult.of(
                (long) page.getNumber() + 1,
                (long) page.getSize(),
                page.getTotalElements(),
                dtos
        );
    }

    // ==================== 空间层级管理 ====================

    /**
     * 获取子空间列表
     *
     * @param parentSpaceId 父空间ID
     * @return 子空间列表
     */
    @Transactional(readOnly = true)
    public List<SpaceDTO> getChildSpaces(Long parentSpaceId) {
        log.info("获取子空间列表: parentSpaceId={}", parentSpaceId);

        // 验证父空间存在
        if (!spaceRepository.existsById(parentSpaceId)) {
            throw new BizException(ErrorCode.SPACE_NOT_FOUND, "父空间不存在: " + parentSpaceId);
        }

        List<Space> childSpaces = spaceRepository.findByParentSpaceId(parentSpaceId);

        return childSpaces.stream()
                .map(this::toSpaceDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取空间树
     *
     * @param rootSpaceId 根空间ID
     * @return 空间树列表
     */
    @Transactional(readOnly = true)
    public List<SpaceDTO> getSpaceTree(Long rootSpaceId) {
        log.info("获取空间树: rootSpaceId={}", rootSpaceId);

        // 获取根空间
        Space rootSpace = spaceRepository.findById(rootSpaceId)
                .orElseThrow(() -> new BizException(ErrorCode.SPACE_NOT_FOUND, "根空间不存在: " + rootSpaceId));

        // 递归构建空间树
        List<SpaceDTO> tree = new ArrayList<>();
        tree.add(buildSpaceTree(rootSpace));

        return tree;
    }

    // ==================== 空间资源管理 ====================

    /**
     * 绑定资源到空间
     *
     * @param command 绑定资源命令
     * @return 关联ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long bindResource(BindResourceCommand command) {
        log.info("绑定资源到空间: spaceId={}, resourceType={}, resourceId={}",
                command.getSpaceId(), command.getResourceType(), command.getResourceId());

        // 验证命令对象
        command.validate();

        // 验证空间存在
        Space space = spaceRepository.findById(command.getSpaceId())
                .orElseThrow(() -> new BizException(ErrorCode.SPACE_NOT_FOUND, "空间不存在: " + command.getSpaceId()));

        // 转换资源类型和关联类型
        SpaceResource.ResourceType resourceType = convertResourceType(command.getResourceType());
        SpaceResource.RelationType relationType = convertRelationType(command.getRelationType());

        // 创建空间资源关联
        SpaceResource spaceResource = SpaceResource.create(
                command.getTenantId(),
                command.getSpaceId(),
                command.getSpaceCode(),
                resourceType,
                command.getResourceId(),
                relationType,
                command.getCreatedBy()
        );

        // 设置资源编码
        if (command.getResourceCode() != null) {
            spaceResource.setResourceCode(command.getResourceCode());
        }

        // 设置位置详情
        if (command.getLocationDetail() != null || command.getFloorNumber() != null) {
            spaceResource.setLocationDetail(
                    command.getLocationDetail(),
                    command.getFloorNumber(),
                    command.getRoomNumber()
            );
        }

        // 设置生效时间
        if (command.getStartDate() != null || command.getEndDate() != null) {
            spaceResource.setDateRange(command.getStartDate(), command.getEndDate());
        }

        // 设置是否主关联
        if (command.getPrimaryRelation() != null && command.getPrimaryRelation()) {
            spaceResource.setAsPrimary();
        }

        // 保存
        SpaceResource savedResource = spaceResourceRepository.save(spaceResource);

        log.info("资源绑定成功: resourceLinkId={}", savedResource.getId());
        return savedResource.getId();
    }

    /**
     * 解绑资源
     *
     * @param resourceLinkId 资源关联ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbindResource(Long resourceLinkId) {
        log.info("解绑资源: resourceLinkId={}", resourceLinkId);

        SpaceResource spaceResource = spaceResourceRepository.findById(resourceLinkId)
                .orElseThrow(() -> new BizException(ErrorCode.RESOURCE_NOT_FOUND, "资源关联不存在: " + resourceLinkId));

        spaceResource.softDelete(spaceResource.getUpdatedBy());
        spaceResourceRepository.save(spaceResource);

        log.info("资源解绑成功: resourceLinkId={}", resourceLinkId);
    }

    /**
     * 获取空间的资源列表
     *
     * @param spaceId 空间ID
     * @return 资源列表
     */
    @Transactional(readOnly = true)
    public List<SpaceResourceDTO> getSpaceResources(Long spaceId) {
        log.info("获取空间资源列表: spaceId={}", spaceId);

        // 验证空间存在
        if (!spaceRepository.existsById(spaceId)) {
            throw new BizException(ErrorCode.SPACE_NOT_FOUND, "空间不存在: " + spaceId);
        }

        List<SpaceResource> resources = spaceResourceRepository.findBySpaceId(spaceId);

        return resources.stream()
                .filter(r -> !r.getDeleted())
                .map(this::toSpaceResourceDTO)
                .collect(Collectors.toList());
    }

    // ==================== 逻辑空间分组 ====================

    /**
     * 创建逻辑空间分组
     *
     * @param command 创建分组命令
     * @return 分组ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createLogicalSpaceGroup(CreateLogicalSpaceGroupCommand command) {
        log.info("创建逻辑空间分组: tenantId={}, groupCode={}, groupName={}",
                command.getTenantId(), command.getGroupCode(), command.getGroupName());

        // 验证命令对象
        command.validate();

        // 检查分组编码是否已存在
        if (logicalSpaceGroupRepository.existsByTenantIdAndGroupCode(command.getTenantId(), command.getGroupCode())) {
            throw new BizException(ErrorCode.RESOURCE_ALREADY_EXISTS, "分组编码已存在: " + command.getGroupCode());
        }

        // 转换分组类型
        LogicalSpaceGroup.GroupType groupType = convertGroupType(command.getGroupType());

        // 创建分组
        LogicalSpaceGroup group = LogicalSpaceGroup.create(
                command.getTenantId(),
                command.getGroupCode(),
                command.getGroupName(),
                groupType,
                command.getDescription(),
                command.getCreatedBy()
        );

        // 设置颜色和图标
        if (command.getGroupColor() != null) {
            group.setGroupColor(command.getGroupColor());
        }
        if (command.getGroupIcon() != null) {
            group.setGroupIcon(command.getGroupIcon());
        }

        // 设置分组规则
        if (command.getGroupRule() != null) {
            group.updateGroupRule(command.getGroupRule());
        }

        // 设置显示顺序
        if (command.getDisplayOrder() != null) {
            group.updateDisplayOrder(command.getDisplayOrder());
        }

        // 保存分组
        LogicalSpaceGroup savedGroup = logicalSpaceGroupRepository.save(group);

        log.info("逻辑空间分组创建成功: groupId={}, groupCode={}", savedGroup.getId(), savedGroup.getGroupCode());
        return savedGroup.getId();
    }

    /**
     * 添加空间到分组
     *
     * @param groupId 分组ID
     * @param spaceId 空间ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addSpaceToGroup(Long groupId, Long spaceId) {
        log.info("添加空间到分组: groupId={}, spaceId={}", groupId, spaceId);

        // 获取分组
        LogicalSpaceGroup group = logicalSpaceGroupRepository.findById(groupId)
                .orElseThrow(() -> new BizException(ErrorCode.RESOURCE_NOT_FOUND, "分组不存在: " + groupId));

        // 获取空间
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new BizException(ErrorCode.SPACE_NOT_FOUND, "空间不存在: " + spaceId));

        // 检查是否已存在
        List<LogicalSpaceGroupMember> existingMembers = logicalSpaceGroupMemberRepository.findByGroupId(groupId);
        boolean alreadyExists = existingMembers.stream()
                .anyMatch(m -> m.getSpaceId().equals(spaceId) && !m.getDeleted());

        if (alreadyExists) {
            throw new BizException(ErrorCode.RESOURCE_ALREADY_EXISTS, "空间已在该分组中");
        }

        // 创建分组成员
        LogicalSpaceGroupMember member = LogicalSpaceGroupMember.create(
                group.getTenantId(),
                groupId,
                group.getGroupCode(),
                spaceId,
                space.getSpaceCode(),
                space.getSpaceName(),
                group.getUpdatedBy()
        );

        logicalSpaceGroupMemberRepository.save(member);

        log.info("空间添加到分组成功: groupId={}, spaceId={}", groupId, spaceId);
    }

    /**
     * 从分组移除空间
     *
     * @param groupId 分组ID
     * @param spaceId 空间ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeSpaceFromGroup(Long groupId, Long spaceId) {
        log.info("从分组移除空间: groupId={}, spaceId={}", groupId, spaceId);

        // 查找分组成员
        List<LogicalSpaceGroupMember> members = logicalSpaceGroupMemberRepository.findByGroupId(groupId);
        LogicalSpaceGroupMember member = members.stream()
                .filter(m -> m.getSpaceId().equals(spaceId) && !m.getDeleted())
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.RESOURCE_NOT_FOUND, "空间不在此分组中"));

        member.softDelete(member.getUpdatedBy());
        logicalSpaceGroupMemberRepository.save(member);

        log.info("空间从分组移除成功: groupId={}, spaceId={}", groupId, spaceId);
    }

    /**
     * 获取租户的所有分组
     *
     * @param tenantId 租户ID
     * @return 分组列表
     */
    @Transactional(readOnly = true)
    public List<LogicalSpaceGroupDTO> getGroupsByTenant(Long tenantId) {
        log.info("获取租户分组列表: tenantId={}", tenantId);

        List<LogicalSpaceGroup> groups = logicalSpaceGroupRepository.findByTenantId(tenantId);

        return groups.stream()
                .filter(g -> !g.getDeleted())
                .map(this::toLogicalSpaceGroupDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取分组详情
     *
     * @param groupId 分组ID
     * @return 分组DTO
     */
    @Transactional(readOnly = true)
    public LogicalSpaceGroupDTO getGroupById(Long groupId) {
        log.info("获取分组详情: groupId={}", groupId);

        LogicalSpaceGroup group = logicalSpaceGroupRepository.findById(groupId)
                .orElseThrow(() -> new BizException(ErrorCode.RESOURCE_NOT_FOUND, "分组不存在: " + groupId));

        return toLogicalSpaceGroupDTO(group);
    }

    /**
     * 更新逻辑分组
     *
     * @param groupId 分组ID
     * @param groupName 分组名称
     * @param description 分组描述
     * @param groupColor 分组颜色
     * @param groupIcon 分组图标
     * @param displayOrder 显示顺序
     * @param updatedBy 更新人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLogicalSpaceGroup(Long groupId, String groupName, String description,
                                        String groupColor, String groupIcon, Integer displayOrder, Long updatedBy) {
        log.info("更新逻辑分组: groupId={}", groupId);

        LogicalSpaceGroup group = logicalSpaceGroupRepository.findById(groupId)
                .orElseThrow(() -> new BizException(ErrorCode.RESOURCE_NOT_FOUND, "分组不存在: " + groupId));

        // 更新分组信息
        if (groupName != null || description != null || groupColor != null || groupIcon != null) {
            group.updateGroupInfo(
                    groupName != null ? groupName : group.getGroupName(),
                    description != null ? description : group.getDescription(),
                    groupColor,
                    groupIcon
            );
        }

        if (displayOrder != null) {
            group.updateDisplayOrder(displayOrder);
        }

        group.setUpdatedBy(updatedBy);
        logicalSpaceGroupRepository.save(group);

        log.info("逻辑分组更新成功: groupId={}", groupId);
    }

    /**
     * 删除逻辑分组
     *
     * @param groupId 分组ID
     * @param deletedBy 删除人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteLogicalSpaceGroup(Long groupId, Long deletedBy) {
        log.info("删除逻辑分组: groupId={}", groupId);

        LogicalSpaceGroup group = logicalSpaceGroupRepository.findById(groupId)
                .orElseThrow(() -> new BizException(ErrorCode.RESOURCE_NOT_FOUND, "分组不存在: " + groupId));

        // 检查是否有成员
        long memberCount = logicalSpaceGroupMemberRepository.countByGroupId(groupId);
        if (memberCount > 0) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "分组存在成员，无法删除");
        }

        // 软删除
        group.softDelete(deletedBy);
        logicalSpaceGroupRepository.save(group);

        log.info("逻辑分组删除成功: groupId={}", groupId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换为空间DTO
     */
    private SpaceDTO toSpaceDTO(Space space) {
        return SpaceDTO.builder()
                .id(space.getId())
                .tenantId(space.getTenantId())
                .spaceCode(space.getSpaceCode())
                .spaceName(space.getSpaceName())
                .spaceType(convertSpaceTypeDTO(space.getSpaceType()))
                .spaceLevel(space.getSpaceLevel())
                .parentSpaceId(space.getParentSpaceId())
                .rootSpaceId(space.getRootSpaceId())
                .spacePath(space.getSpacePath())
                .address(space.getAddress())
                .province(space.getProvince())
                .city(space.getCity())
                .district(space.getDistrict())
                .longitude(space.getLongitude())
                .latitude(space.getLatitude())
                .altitude(space.getAltitude())
                .boundary(space.getBoundary())
                .area(space.getArea())
                .floorNumber(space.getFloorNumber())
                .roomNumber(space.getRoomNumber())
                .capacity(space.getCapacity())
                .spaceStatus(convertSpaceStatusDTO(space.getSpaceStatus()))
                .usageStatus(convertUsageStatusDTO(space.getUsageStatus()))
                .extProperties(space.getExtProperties())
                .version(space.getVersion())
                .createdAt(space.getCreatedAt())
                .updatedAt(space.getUpdatedAt())
                .createdBy(space.getCreatedBy())
                .updatedBy(space.getUpdatedBy())
                .build();
    }

    /**
     * 转换为空间资源DTO
     */
    private SpaceResourceDTO toSpaceResourceDTO(SpaceResource resource) {
        return SpaceResourceDTO.builder()
                .id(resource.getId())
                .tenantId(resource.getTenantId())
                .spaceId(resource.getSpaceId())
                .spaceCode(resource.getSpaceCode())
                .resourceType(convertResourceTypeDTO(resource.getResourceType()))
                .resourceId(resource.getResourceId())
                .resourceCode(resource.getResourceCode())
                .relationType(convertRelationTypeDTO(resource.getRelationType()))
                .primaryRelation(resource.getPrimaryRelation())
                .locationDetail(resource.getLocationDetail())
                .floorNumber(resource.getFloorNumber())
                .roomNumber(resource.getRoomNumber())
                .startDate(resource.getStartDate())
                .endDate(resource.getEndDate())
                .status(convertResourceStatusDTO(resource.getStatus()))
                .extProperties(resource.getExtProperties())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .createdBy(resource.getCreatedBy())
                .updatedBy(resource.getUpdatedBy())
                .build();
    }

    /**
     * 转换为逻辑空间分组DTO
     */
    private LogicalSpaceGroupDTO toLogicalSpaceGroupDTO(LogicalSpaceGroup group) {
        // 统计成员数量
        long memberCount = logicalSpaceGroupMemberRepository.countByGroupId(group.getId());

        return LogicalSpaceGroupDTO.builder()
                .id(group.getId())
                .tenantId(group.getTenantId())
                .groupCode(group.getGroupCode())
                .groupName(group.getGroupName())
                .groupType(convertGroupTypeDTO(group.getGroupType()))
                .description(group.getDescription())
                .groupColor(group.getGroupColor())
                .groupIcon(group.getGroupIcon())
                .groupRule(group.getGroupRule())
                .status(convertGroupStatusDTO(group.getStatus()))
                .displayOrder(group.getDisplayOrder())
                .version(group.getVersion())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .createdBy(group.getCreatedBy())
                .updatedBy(group.getUpdatedBy())
                .memberCount((int) memberCount)
                .build();
    }

    /**
     * 递归构建空间树
     */
    private SpaceDTO buildSpaceTree(Space space) {
        SpaceDTO dto = toSpaceDTO(space);

        // 获取子空间
        List<Space> childSpaces = spaceRepository.findByParentSpaceId(space.getId());
        if (!childSpaces.isEmpty()) {
            // 这里可以扩展为在DTO中添加children字段
            // 暂时仅返回当前层级的DTO
        }

        return dto;
    }

    /**
     * 构建排序条件
     */
    private Sort buildSort(SpaceQuery query) {
        String sortField = query.getSortField();
        if (sortField == null) {
            sortField = "createdAt";
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(query.getSortOrder())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortField);
    }

    // ==================== 枚举转换方法 ====================

    private Space.SpaceType convertSpaceType(CreateSpaceCommand.SpaceTypeCommand type) {
        return Space.SpaceType.valueOf(type.name());
    }

    private SpaceDTO.SpaceTypeDTO convertSpaceTypeDTO(Space.SpaceType type) {
        return SpaceDTO.SpaceTypeDTO.valueOf(type.name());
    }

    private SpaceDTO.SpaceStatusDTO convertSpaceStatusDTO(Space.SpaceStatus status) {
        return SpaceDTO.SpaceStatusDTO.valueOf(status.name());
    }

    private SpaceDTO.UsageStatusDTO convertUsageStatusDTO(Space.UsageStatus status) {
        return SpaceDTO.UsageStatusDTO.valueOf(status.name());
    }

    private SpaceResource.ResourceType convertResourceType(BindResourceCommand.ResourceTypeCommand type) {
        return SpaceResource.ResourceType.valueOf(type.name());
    }

    private SpaceResourceDTO.ResourceTypeDTO convertResourceTypeDTO(SpaceResource.ResourceType type) {
        return SpaceResourceDTO.ResourceTypeDTO.valueOf(type.name());
    }

    private SpaceResource.RelationType convertRelationType(BindResourceCommand.RelationTypeCommand type) {
        return SpaceResource.RelationType.valueOf(type.name());
    }

    private SpaceResourceDTO.RelationTypeDTO convertRelationTypeDTO(SpaceResource.RelationType type) {
        return SpaceResourceDTO.RelationTypeDTO.valueOf(type.name());
    }

    private SpaceResourceDTO.ResourceStatusDTO convertResourceStatusDTO(SpaceResource.ResourceStatus status) {
        return SpaceResourceDTO.ResourceStatusDTO.valueOf(status.name());
    }

    private LogicalSpaceGroup.GroupType convertGroupType(CreateLogicalSpaceGroupCommand.GroupTypeCommand type) {
        return LogicalSpaceGroup.GroupType.valueOf(type.name());
    }

    private LogicalSpaceGroupDTO.GroupTypeDTO convertGroupTypeDTO(LogicalSpaceGroup.GroupType type) {
        return LogicalSpaceGroupDTO.GroupTypeDTO.valueOf(type.name());
    }

    private LogicalSpaceGroupDTO.GroupStatusDTO convertGroupStatusDTO(LogicalSpaceGroup.GroupStatus status) {
        return LogicalSpaceGroupDTO.GroupStatusDTO.valueOf(status.name());
    }
}
