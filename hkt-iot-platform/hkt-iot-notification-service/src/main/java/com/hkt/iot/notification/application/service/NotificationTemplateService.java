package com.hkt.iot.notification.application.service;

import com.hkt.iot.common.web.Result;
import com.hkt.iot.notification.application.dto.NotificationTemplateDTO;
import com.hkt.iot.notification.domain.model.NotificationTemplate;
import com.hkt.iot.notification.domain.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 通知模板应用服务
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;

    /**
     * 创建模板
     */
    @Transactional
    public Result<Long> createTemplate(NotificationTemplateDTO dto, String tenantId) {
        try {
            // 检查模板编码是否存在
            if (templateRepository.existsByTemplateCode(dto.getTemplateCode())) {
                return Result.error(400, "模板编码已存在");
            }

            NotificationTemplate template = NotificationTemplate.builder()
                    .templateCode(dto.getTemplateCode())
                    .templateName(dto.getTemplateName())
                    .templateType(NotificationTemplate.TemplateType.valueOf(dto.getTemplateType()))
                    .channelType(NotificationTemplate.ChannelType.valueOf(dto.getChannelType()))
                    .titleTemplate(dto.getTitleTemplate())
                    .contentTemplate(dto.getContentTemplate())
                    .variables(dto.getVariables())
                    .tenantId(tenantId)
                    .enabled(dto.getEnabled())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            templateRepository.save(template);
            log.info("创建通知模板成功: {}", template.getTemplateCode());
            return Result.success(template.getId());
        } catch (Exception e) {
            log.error("创建通知模板失败", e);
            return Result.error(500, "创建模板失败: " + e.getMessage());
        }
    }

    /**
     * 更新模板
     */
    @Transactional
    public Result<Void> updateTemplate(Long id, NotificationTemplateDTO dto) {
        try {
            Optional<NotificationTemplate> templateOpt = templateRepository.findById(id);
            if (templateOpt.isEmpty()) {
                return Result.error(404, "模板不存在");
            }

            // 检查模板编码是否被其他模板使用
            if (templateRepository.existsByTemplateCodeAndIdNot(dto.getTemplateCode(), id)) {
                return Result.error(400, "模板编码已被其他模板使用");
            }

            NotificationTemplate template = templateOpt.get();
            template.setTemplateName(dto.getTemplateName());
            template.setTemplateType(NotificationTemplate.TemplateType.valueOf(dto.getTemplateType()));
            template.setChannelType(NotificationTemplate.ChannelType.valueOf(dto.getChannelType()));
            template.updateContent(dto.getTitleTemplate(), dto.getContentTemplate(), dto.getVariables());
            template.setEnabled(dto.getEnabled());

            templateRepository.save(template);
            log.info("更新通知模板成功: {}", template.getTemplateCode());
            return Result.success();
        } catch (Exception e) {
            log.error("更新通知模板失败", e);
            return Result.error(500, "更新模板失败: " + e.getMessage());
        }
    }

    /**
     * 删除模板
     */
    @Transactional
    public Result<Void> deleteTemplate(Long id) {
        try {
            if (!templateRepository.findById(id).isPresent()) {
                return Result.error(404, "模板不存在");
            }

            templateRepository.deleteById(id);
            log.info("删除通知模板成功: {}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除通知模板失败", e);
            return Result.error(500, "删除模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取模板详情
     */
    public Result<NotificationTemplateDTO> getTemplate(Long id) {
        try {
            Optional<NotificationTemplate> templateOpt = templateRepository.findById(id);
            if (templateOpt.isEmpty()) {
                return Result.error(404, "模板不存在");
            }

            return Result.success(toDTO(templateOpt.get()));
        } catch (Exception e) {
            log.error("获取模板详情失败", e);
            return Result.error(500, "获取模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取租户的所有模板
     */
    public Result<List<NotificationTemplateDTO>> listTemplates(String tenantId) {
        try {
            List<NotificationTemplate> templates = templateRepository.findByTenantId(tenantId);
            return Result.success(templates.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return Result.error(500, "获取模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 启用模板
     */
    @Transactional
    public Result<Void> enableTemplate(Long id) {
        try {
            Optional<NotificationTemplate> templateOpt = templateRepository.findById(id);
            if (templateOpt.isEmpty()) {
                return Result.error(404, "模板不存在");
            }

            templateOpt.get().enable();
            templateRepository.save(templateOpt.get());
            return Result.success();
        } catch (Exception e) {
            log.error("启用模板失败", e);
            return Result.error(500, "启用模板失败: " + e.getMessage());
        }
    }

    /**
     * 禁用模板
     */
    @Transactional
    public Result<Void> disableTemplate(Long id) {
        try {
            Optional<NotificationTemplate> templateOpt = templateRepository.findById(id);
            if (templateOpt.isEmpty()) {
                return Result.error(404, "模板不存在");
            }

            templateOpt.get().disable();
            templateRepository.save(templateOpt.get());
            return Result.success();
        } catch (Exception e) {
            log.error("禁用模板失败", e);
            return Result.error(500, "禁用模板失败: " + e.getMessage());
        }
    }

    /**
     * 转换为DTO
     */
    private NotificationTemplateDTO toDTO(NotificationTemplate template) {
        return NotificationTemplateDTO.builder()
                .id(template.getId())
                .templateCode(template.getTemplateCode())
                .templateName(template.getTemplateName())
                .templateType(template.getTemplateType().name())
                .channelType(template.getChannelType().name())
                .titleTemplate(template.getTitleTemplate())
                .contentTemplate(template.getContentTemplate())
                .variables(template.getVariables())
                .enabled(template.getEnabled())
                .build();
    }
}
