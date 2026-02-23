package com.hkt.iot.space.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * 空间边界测试
 * 测试Space聚合根中与空间边界相关的方法
 */
@DisplayName("空间边界测试")
class SpaceBoundsTest {

    @Test
    @DisplayName("测试设置有效边界")
    void testSetSpatialBounds_设置有效边界() {
        Space space = createTestSpace();
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        space.setSpatialBounds(bounds);

        assertThat(space.getSpatialBounds()).isNotNull();
        assertThat(space.getSpatialBounds()).isEqualTo(bounds);
    }

    @Test
    @DisplayName("测试设置无效边界抛出异常")
    void testSetSpatialBounds_设置无效边界抛出异常() {
        Space space = createTestSpace();
        Coordinate northeast = Coordinate.of(38.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds invalidBounds = SpatialBounds.of(northeast, southwest);

        assertThatThrownBy(() -> space.setSpatialBounds(invalidBounds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效的空间边界");
    }

    @Test
    @DisplayName("测试边界内返回true")
    void testContainsCoordinate_边界内返回true() {
        Space space = createTestSpace();
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);
        space.setSpatialBounds(bounds);

        Coordinate insideCoordinate = Coordinate.of(39.5, 116.5);

        boolean result = space.containsCoordinate(insideCoordinate);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试边界外返回false")
    void testContainsCoordinate_边界外返回false() {
        Space space = createTestSpace();
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);
        space.setSpatialBounds(bounds);

        Coordinate outsideCoordinate = Coordinate.of(41.0, 118.0);

        boolean result = space.containsCoordinate(outsideCoordinate);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试无边界返回false")
    void testContainsCoordinate_无边界返回false() {
        Space space = createTestSpace();

        Coordinate coordinate = Coordinate.of(39.5, 116.5);

        boolean result = space.containsCoordinate(coordinate);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试设置空边界")
    void testSetSpatialBounds_NullBounds() {
        Space space = createTestSpace();
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);
        space.setSpatialBounds(bounds);

        space.setSpatialBounds(null);

        assertThat(space.getSpatialBounds()).isNull();
    }

    @Test
    @DisplayName("测试边界更新后领域事件被注册")
    void testSetSpatialBounds_DomainEventRegistered() {
        Space space = createTestSpace();
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        space.setSpatialBounds(bounds);

        assertThat(space.getDomainEvents()).isNotEmpty();
    }

    @Test
    @DisplayName("测试containsCoordinate-空坐标返回false")
    void testContainsCoordinate_NullCoordinate_ReturnsFalse() {
        Space space = createTestSpace();
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);
        space.setSpatialBounds(bounds);

        boolean result = space.containsCoordinate(null);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试边界上的坐标返回true")
    void testContainsCoordinate_CoordinateOnBoundary_ReturnsTrue() {
        Space space = createTestSpace();
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);
        space.setSpatialBounds(bounds);

        boolean result = space.containsCoordinate(southwest);

        assertThat(result).isTrue();
    }

    private Space createTestSpace() {
        return Space.create(
                1L,
                "SPACE-001",
                "测试空间",
                Space.SpaceType.BUILDING,
                1,
                1L
        );
    }
}
