package com.hkt.iot.scene.interfaces.rest;

import com.hkt.iot.common.web.Result;
import com.hkt.iot.scene.application.dto.*;
import com.hkt.iot.scene.application.service.SceneApplicationService;
import com.hkt.iot.scene.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 场景管理REST接口
 *
 * @author HKT IoT Team
 */
@Slf4j
@Tag(name = "场景管理", description = "场景联动相关接口")
@RequiredArgsConstructor
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/v1/scenes")
public class SceneController {

    private final SceneApplicationService sceneApplicationService;

    @Operation(summary = "创建场景")
    @org.springframework.web.bind.annotation.PostMapping
    public Result<SceneDTO> createScene(
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateSceneRequest request,
            @Parameter(hidden = true) com.hkt.iot.domain.shared.TenantId tenantId) {
        SceneDTO scene = sceneApplicationService.createScene(request, tenantId);
        return Result.success(scene);
    }

    @Operation(summary = "更新场景")
    @org.springframework.web.bind.annotation.PutMapping("/{sceneId}")
    public Result<SceneDTO> updateScene(
            @PathVariable String sceneId,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateSceneRequest request) {
        SceneDTO scene = sceneApplicationService.updateScene(SceneId.of(sceneId), request);
        return Result.success(scene);
    }

    @Operation(summary = "删除场景")
    @org.springframework.web.bind.annotation.DeleteMapping("/{sceneId}")
    public Result<Void> deleteScene(
            @PathVariable String sceneId) {
        sceneApplicationService.deleteScene(SceneId.of(sceneId));
        return Result.success();
    }

    @Operation(summary = "获取场景详情")
    @org.springframework.web.bind.annotation.GetMapping("/{sceneId}")
    public Result<SceneDTO> getScene(
            @PathVariable String sceneId) {
        SceneDTO scene = sceneApplicationService.getScene(SceneId.of(sceneId));
        return Result.success(scene);
    }

    @Operation(summary = "获取租户下的场景列表")
    @org.springframework.web.bind.annotation.GetMapping
    public Result<List<SceneDTO>> getScenesByTenant(
            @Parameter(hidden = true) com.hkt.iot.domain.shared.TenantId tenantId) {
        List<SceneDTO> scenes = sceneApplicationService.getScenesByTenant(tenantId);
        return Result.success(scenes);
    }

    @Operation(summary = "获取空间下的场景列表")
    @org.springframework.web.bind.annotation.GetMapping("/by-space/{spaceId}")
    public Result<List<SceneDTO>> getScenesBySpace(
            @PathVariable String spaceId) {
        List<SceneDTO> scenes = sceneApplicationService.getScenesBySpace(SpaceId.of(spaceId));
        return Result.success(scenes);
    }

    @Operation(summary = "激活场景")
    @org.springframework.web.bind.annotation.PostMapping("/{sceneId}/activate")
    public Result<Void> activateScene(
            @PathVariable String sceneId) {
        sceneApplicationService.activateScene(SceneId.of(sceneId));
        return Result.success();
    }

    @Operation(summary = "停用场景")
    @org.springframework.web.bind.annotation.PostMapping("/{sceneId}/deactivate")
    public Result<Void> deactivateScene(
            @PathVariable String sceneId) {
        sceneApplicationService.deactivateScene(SceneId.of(sceneId));
        return Result.success();
    }

    @Operation(summary = "添加触发条件")
    @org.springframework.web.bind.annotation.PostMapping("/{sceneId}/triggers")
    public Result<Void> addTrigger(
            @PathVariable String sceneId,
            @org.springframework.web.bind.annotation.RequestBody SceneTriggerDTO trigger) {
        sceneApplicationService.addTrigger(SceneId.of(sceneId), trigger);
        return Result.success();
    }

    @Operation(summary = "移除触发条件")
    @org.springframework.web.bind.annotation.DeleteMapping("/{sceneId}/triggers/{triggerId}")
    public Result<Void> removeTrigger(
            @PathVariable String sceneId,
            @PathVariable String triggerId) {
        sceneApplicationService.removeTrigger(SceneId.of(sceneId), TriggerId.of(triggerId));
        return Result.success();
    }

    @Operation(summary = "添加执行动作")
    @org.springframework.web.bind.annotation.PostMapping("/{sceneId}/actions")
    public Result<Void> addAction(
            @PathVariable String sceneId,
            @org.springframework.web.bind.annotation.RequestBody SceneActionDTO action) {
        sceneApplicationService.addAction(SceneId.of(sceneId), action);
        return Result.success();
    }

    @Operation(summary = "移除执行动作")
    @org.springframework.web.bind.annotation.DeleteMapping("/{sceneId}/actions/{actionId}")
    public Result<Void> removeAction(
            @PathVariable String sceneId,
            @PathVariable String actionId) {
        sceneApplicationService.removeAction(SceneId.of(sceneId), ActionId.of(actionId));
        return Result.success();
    }

    @Operation(summary = "手动执行场景")
    @org.springframework.web.bind.annotation.PostMapping("/{sceneId}/execute")
    public Result<SceneExecutionResultDTO> executeScene(
            @PathVariable String sceneId,
            @org.springframework.web.bind.annotation.RequestBody ExecuteSceneRequest request) {
        SceneExecutionResultDTO result = sceneApplicationService.executeScene(SceneId.of(sceneId), request);
        return Result.success(result);
    }

    @Operation(summary = "获取场景执行日志")
    @org.springframework.web.bind.annotation.GetMapping("/{sceneId}/execution-logs")
    public Result<List<SceneExecutionLogDTO>> getSceneExecutionLogs(
            @PathVariable String sceneId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        List<SceneExecutionLogDTO> logs = sceneApplicationService.getSceneExecutionLogs(
                SceneId.of(sceneId), page, size);
        return Result.success(logs);
    }

    @Operation(summary = "复制场景")
    @org.springframework.web.bind.annotation.PostMapping("/{sceneId}/copy")
    public Result<SceneDTO> copyScene(
            @PathVariable String sceneId,
            @org.springframework.web.bind.annotationRequestParam String newName,
            @org.springframework.web.bind.annotationRequestParam String newCode) {
        SceneDTO scene = sceneApplicationService.copyScene(
                SceneId.of(sceneId),
                SceneName.of(newName),
                SceneCode.of(newCode));
        return Result.success(scene);
    }
}
