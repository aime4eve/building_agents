package com.hkt.iot.space.interfaces.feign;

import com.hkt.iot.common.result.PageResult;
import com.hkt.iot.common.result.Result;
import com.hkt.iot.space.application.dto.SpaceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 空间管理服务 Feign Client 降级处理
 * 当空间服务不可用时提供降级响应
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class SpaceFeignClientFallback implements FallbackFactory<SpaceFeignClient> {

    @Override
    public SpaceFeignClient create(Throwable cause) {
        log.error("空间服务调用失败，触发降级处理: {}", cause.getMessage(), cause);

        return new SpaceFeignClient() {

            private final String SERVICE_UNAVAILABLE = "503";
            private final String SERVICE_UNAVAILABLE_MESSAGE = "空间服务暂时不可用，请稍后重试";

            @Override
            public Result<SpaceDTO> getSpace(Long spaceId) {
                log.warn("获取空间详情降级: spaceId={}", spaceId);
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }

            @Override
            public Result<PageResult<SpaceDTO>> querySpaces(java.util.Map<String, Object> params) {
                log.warn("查询空间列表降级: params={}", params);
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }

            @Override
            public Result<Long> createSpace(CreateSpaceFeignRequest request) {
                log.warn("创建空间降级: spaceCode={}", request.getSpaceCode());
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }

            @Override
            public Result<Void> updateSpace(Long spaceId, UpdateSpaceFeignRequest request) {
                log.warn("更新空间降级: spaceId={}", spaceId);
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }

            @Override
            public Result<Void> deleteSpace(Long spaceId) {
                log.warn("删除空间降级: spaceId={}", spaceId);
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }

            @Override
            public Result<List<SpaceDTO>> getChildSpaces(Long spaceId) {
                log.warn("获取子空间降级: spaceId={}", spaceId);
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }

            @Override
            public Result<List<SpaceDTO>> getSpaceTree(Long rootSpaceId) {
                log.warn("获取空间树降级: rootSpaceId={}", rootSpaceId);
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }

            @Override
            public Result<List<SpaceResourceFeignDTO>> getSpaceResources(Long spaceId) {
                log.warn("获取空间资源降级: spaceId={}", spaceId);
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }

            @Override
            public Result<Long> bindResource(Long spaceId, BindResourceFeignRequest request) {
                log.warn("绑定资源降级: spaceId={}, resourceId={}", spaceId, request.getResourceId());
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }

            @Override
            public Result<Void> unbindResource(Long resourceLinkId) {
                log.warn("解绑资源降级: resourceLinkId={}", resourceLinkId);
                return Result.error(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            }
        };
    }
}
