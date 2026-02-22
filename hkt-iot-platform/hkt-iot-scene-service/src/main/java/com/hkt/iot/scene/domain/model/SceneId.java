package com.hkt.iot.scene.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 场景ID值对象
 */
@Getter
@EqualsAndHashCode
public class SceneId {
    private final String value;

    private SceneId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("场景ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static SceneId of(String value) {
        return new SceneId(value);
    }

    public static SceneId generate() {
        return new SceneId("SCENE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
