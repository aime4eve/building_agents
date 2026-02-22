package com.hkt.iot.notification.interfaces.rest;

import com.hkt.iot.common.web.Result;
import com.hkt.iot.notification.application.dto.NotificationTemplateDTO;
import com.hkt.iot.notification.application.service.NotificationTemplateService;
import com.hkt.iot.notification.infrastructure.persistence.po.NotificationTemplatePO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知模板控制器
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications/templates")
@RequiredArgsConstructor
@Tag(name = "通知模板管理", description = "通知模板相关接口")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    /**
     * 创建模板
     */
    @PostMapping
    @Operation(summary = "创建通知模板", description = "创建新的通知模板")
    public Result<Long> createTemplate(
            @RequestBody @Valid NotificationTemplateDTO dto,
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {
        return templateService.createTemplate(dto, tenantId);
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新通知模板", description = "更新已存在的通知模板")
    public Result<Void> updateTemplate(
            @PathVariable Long id,
            @RequestBody @Valid NotificationTemplateDTO dto
    ) {
        return templateService.updateTemplate(id, dto);
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知模板", description = "删除指定的通知模板")
    public Result<Void> deleteTemplate(
            @PathVariable Long id
    ) {
        return templateService.deleteTemplate(id);
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情", description = "根据ID获取模板详情")
    public Result<NotificationTemplateDTO> getTemplate(
            @PathVariable Long id
    ) {
        return templateService.getTemplate(id);
    }

    /**
     * 获取模板列表
     */
    @GetMapping
    @Operation(summary = "获取模板列表", description = "获取租户的所有通知模板")
    public Result<List<NotificationTemplateDTO>> listTemplates(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "模板类型")
            @RequestParam(required = false) String templateType,
            @Parameter(description = "通知渠道")
            @RequestParam(required = false) String channelType
    ) {
        return templateService.listTemplates(tenantId);
    }

    /**
     * 启用模板
     */
    @PostMapping("/{id}/enable")
    @Operation(summary = "启用通知模板", description = "启用指定的通知模板")
    public Result<Void> enableTemplate(
            @PathVariable Long id
    ) {
        return templateService.enableTemplate(id);
    }

    /**
     * 禁用模板
     */
    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用通知模板", description = "禁用指定的通知模板")
    public Result<Void> disableTemplate(
            @PathVariable Long id
    ) {
        return templateService.disableTemplate(id);
    }
}
