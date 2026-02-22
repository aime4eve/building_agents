package com.hkt.iot.space.interfaces.feign;

import com.hkt.iot.common.result.PageResult;
import com.hkt.iot.common.result.Result;
import com.hkt.iot.space.application.dto.SpaceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 空间管理服务 Feign Client
 * 提供跨上下文调用空间服务的接口
 *
 * @author HKT IoT Team
 */
@FeignClient(
        name = "space-service",
        path = "/api/v1",
        contextId = "spaceFeignClient",
        fallbackFactory = SpaceFeignClientFallback.class
)
@Tag(name = "空间管理Feign", description = "空间服务跨上下文调用接口")
public interface SpaceFeignClient {

    // ==================== 空间 CRUD 操作 ====================

    /**
     * 获取空间详情
     *
     * @param spaceId 空间ID
     * @return 空间详情
     */
    @GetMapping("/spaces/{spaceId}")
    @Operation(summary = "获取空间详情", description = "根据ID查询空间详细信息")
    Result<SpaceDTO> getSpace(@Parameter(description = "空间ID") @PathVariable("spaceId") Long spaceId);

    /**
     * 查询空间列表（分页）
     *
     * @param params 查询参数
     * @return 空间列表
     */
    @GetMapping("/spaces")
    @Operation(summary = "查询空间列表", description = "根据条件分页查询空间列表")
    Result<PageResult<SpaceDTO>> querySpaces(@RequestParam Map<String, Object> params);

    /**
     * 创建空间
     *
     * @param request 创建空间请求
     * @return 空间ID
     */
    @PostMapping("/spaces")
    @Operation(summary = "创建空间", description = "创建新的空间")
    Result<Long> createSpace(@Valid @RequestBody CreateSpaceFeignRequest request);

    /**
     * 更新空间
     *
     * @param spaceId 空间ID
     * @param request 更新空间请求
     * @return 无返回数据
     */
    @PutMapping("/spaces/{spaceId}")
    @Operation(summary = "更新空间", description = "更新空间信息")
    Result<Void> updateSpace(
            @Parameter(description = "空间ID") @PathVariable("spaceId") Long spaceId,
            @Valid @RequestBody UpdateSpaceFeignRequest request);

    /**
     * 删除空间（软删除）
     *
     * @param spaceId 空间ID
     * @return 无返回数据
     */
    @DeleteMapping("/spaces/{spaceId}")
    @Operation(summary = "删除空间", description = "软删除空间")
    Result<Void> deleteSpace(@Parameter(description = "空间ID") @PathVariable("spaceId") Long spaceId);

    // ==================== 空间层级操作 ====================

    /**
     * 获取子空间列表
     *
     * @param spaceId 父空间ID
     * @return 子空间列表
     */
    @GetMapping("/spaces/{spaceId}/children")
    @Operation(summary = "获取子空间", description = "获取指定空间的直接子空间列表")
    Result<List<SpaceDTO>> getChildSpaces(@Parameter(description = "父空间ID") @PathVariable("spaceId") Long spaceId);

    /**
     * 获取空间树
     *
     * @param rootSpaceId 根空间ID
     * @return 空间树结构
     */
    @GetMapping("/spaces/tree/{rootSpaceId}")
    @Operation(summary = "获取空间树", description = "获取以指定空间为根的完整空间树结构")
    Result<List<SpaceDTO>> getSpaceTree(@Parameter(description = "根空间ID") @PathVariable("rootSpaceId") Long rootSpaceId);

    // ==================== 空间资源操作 ====================

    /**
     * 获取空间资源列表
     *
     * @param spaceId 空间ID
     * @return 空间资源列表
     */
    @GetMapping("/spaces/{spaceId}/resources")
    @Operation(summary = "获取空间资源列表", description = "获取指定空间绑定的所有资源")
    Result<List<SpaceResourceFeignDTO>> getSpaceResources(@Parameter(description = "空间ID") @PathVariable("spaceId") Long spaceId);

    /**
     * 绑定资源到空间
     *
     * @param spaceId 空间ID
     * @param request 绑定资源请求
     * @return 资源关联ID
     */
    @PostMapping("/spaces/{spaceId}/resources")
    @Operation(summary = "绑定资源", description = "将资源绑定到指定空间")
    Result<Long> bindResource(
            @Parameter(description = "空间ID") @PathVariable("spaceId") Long spaceId,
            @Valid @RequestBody BindResourceFeignRequest request);

    /**
     * 解绑资源
     *
     * @param resourceLinkId 资源关联ID
     * @return 无返回数据
     */
    @DeleteMapping("/spaces/resources/{resourceLinkId}")
    @Operation(summary = "解绑资源", description = "解除资源与空间的绑定关系")
    Result<Void> unbindResource(@Parameter(description = "资源关联ID") @PathVariable("resourceLinkId") Long resourceLinkId);

    // ==================== Feign 请求 DTO ====================

    /**
     * 创建空间请求
     */
    class CreateSpaceFeignRequest {
        private Long tenantId;
        private String spaceCode;
        private String spaceName;
        private String spaceType;
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

        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public String getSpaceCode() { return spaceCode; }
        public void setSpaceCode(String spaceCode) { this.spaceCode = spaceCode; }
        public String getSpaceName() { return spaceName; }
        public void setSpaceName(String spaceName) { this.spaceName = spaceName; }
        public String getSpaceType() { return spaceType; }
        public void setSpaceType(String spaceType) { this.spaceType = spaceType; }
        public Integer getSpaceLevel() { return spaceLevel; }
        public void setSpaceLevel(Integer spaceLevel) { this.spaceLevel = spaceLevel; }
        public Long getParentSpaceId() { return parentSpaceId; }
        public void setParentSpaceId(Long parentSpaceId) { this.parentSpaceId = parentSpaceId; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public java.math.BigDecimal getLongitude() { return longitude; }
        public void setLongitude(java.math.BigDecimal longitude) { this.longitude = longitude; }
        public java.math.BigDecimal getLatitude() { return latitude; }
        public void setLatitude(java.math.BigDecimal latitude) { this.latitude = latitude; }
        public java.math.BigDecimal getAltitude() { return altitude; }
        public void setAltitude(java.math.BigDecimal altitude) { this.altitude = altitude; }
        public List<List<java.math.BigDecimal>> getBoundary() { return boundary; }
        public void setBoundary(List<List<java.math.BigDecimal>> boundary) { this.boundary = boundary; }
        public java.math.BigDecimal getArea() { return area; }
        public void setArea(java.math.BigDecimal area) { this.area = area; }
        public Integer getFloorNumber() { return floorNumber; }
        public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
        public Map<String, Object> getExtProperties() { return extProperties; }
        public void setExtProperties(Map<String, Object> extProperties) { this.extProperties = extProperties; }
    }

    /**
     * 更新空间请求
     */
    class UpdateSpaceFeignRequest {
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

        public String getSpaceName() { return spaceName; }
        public void setSpaceName(String spaceName) { this.spaceName = spaceName; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public java.math.BigDecimal getLongitude() { return longitude; }
        public void setLongitude(java.math.BigDecimal longitude) { this.longitude = longitude; }
        public java.math.BigDecimal getLatitude() { return latitude; }
        public void setLatitude(java.math.BigDecimal latitude) { this.latitude = latitude; }
        public java.math.BigDecimal getAltitude() { return altitude; }
        public void setAltitude(java.math.BigDecimal altitude) { this.altitude = altitude; }
        public List<List<java.math.BigDecimal>> getBoundary() { return boundary; }
        public void setBoundary(List<List<java.math.BigDecimal>> boundary) { this.boundary = boundary; }
        public java.math.BigDecimal getArea() { return area; }
        public void setArea(java.math.BigDecimal area) { this.area = area; }
        public Integer getFloorNumber() { return floorNumber; }
        public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
        public Map<String, Object> getExtProperties() { return extProperties; }
        public void setExtProperties(Map<String, Object> extProperties) { this.extProperties = extProperties; }
        public Long getVersion() { return version; }
        public void setVersion(Long version) { this.version = version; }
    }

    /**
     * 绑定资源请求
     */
    class BindResourceFeignRequest {
        private Long tenantId;
        private String spaceCode;
        private String resourceType;
        private Long resourceId;
        private String resourceCode;
        private String relationType;
        private Boolean primaryRelation;
        private String locationDetail;
        private Integer floorNumber;
        private String roomNumber;
        private java.time.LocalDateTime startDate;
        private java.time.LocalDateTime endDate;
        private Map<String, Object> extProperties;

        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public String getSpaceCode() { return spaceCode; }
        public void setSpaceCode(String spaceCode) { this.spaceCode = spaceCode; }
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public Long getResourceId() { return resourceId; }
        public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
        public String getResourceCode() { return resourceCode; }
        public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }
        public String getRelationType() { return relationType; }
        public void setRelationType(String relationType) { this.relationType = relationType; }
        public Boolean getPrimaryRelation() { return primaryRelation; }
        public void setPrimaryRelation(Boolean primaryRelation) { this.primaryRelation = primaryRelation; }
        public String getLocationDetail() { return locationDetail; }
        public void setLocationDetail(String locationDetail) { this.locationDetail = locationDetail; }
        public Integer getFloorNumber() { return floorNumber; }
        public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        public java.time.LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDateTime startDate) { this.startDate = startDate; }
        public java.time.LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDateTime endDate) { this.endDate = endDate; }
        public Map<String, Object> getExtProperties() { return extProperties; }
        public void setExtProperties(Map<String, Object> extProperties) { this.extProperties = extProperties; }
    }

    /**
     * 空间资源响应 DTO
     */
    class SpaceResourceFeignDTO {
        private Long id;
        private Long spaceId;
        private String spaceCode;
        private String resourceType;
        private Long resourceId;
        private String resourceCode;
        private String relationType;
        private Boolean primaryRelation;
        private String locationDetail;
        private Integer floorNumber;
        private String roomNumber;
        private java.time.LocalDateTime startDate;
        private java.time.LocalDateTime endDate;
        private Map<String, Object> extProperties;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        public String getSpaceCode() { return spaceCode; }
        public void setSpaceCode(String spaceCode) { this.spaceCode = spaceCode; }
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public Long getResourceId() { return resourceId; }
        public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
        public String getResourceCode() { return resourceCode; }
        public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }
        public String getRelationType() { return relationType; }
        public void setRelationType(String relationType) { this.relationType = relationType; }
        public Boolean getPrimaryRelation() { return primaryRelation; }
        public void setPrimaryRelation(Boolean primaryRelation) { this.primaryRelation = primaryRelation; }
        public String getLocationDetail() { return locationDetail; }
        public void setLocationDetail(String locationDetail) { this.locationDetail = locationDetail; }
        public Integer getFloorNumber() { return floorNumber; }
        public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        public java.time.LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDateTime startDate) { this.startDate = startDate; }
        public java.time.LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDateTime endDate) { this.endDate = endDate; }
        public Map<String, Object> getExtProperties() { return extProperties; }
        public void setExtProperties(Map<String, Object> extProperties) { this.extProperties = extProperties; }
    }
}
