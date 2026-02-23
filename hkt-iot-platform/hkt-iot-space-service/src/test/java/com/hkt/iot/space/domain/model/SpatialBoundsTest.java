package com.hkt.iot.space.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * 空间边界值对象测试
 */
@DisplayName("空间边界值对象测试")
class SpatialBoundsTest {

    @Test
    @DisplayName("测试有效边界返回true")
    void testIsValid_有效边界返回true() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);

        boolean result = SpatialBounds.isValid(northeast, southwest);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试东北角纬度小于西南角返回false")
    void testIsValid_东北角纬度小于西南角返回false() {
        Coordinate northeast = Coordinate.of(38.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);

        boolean result = SpatialBounds.isValid(northeast, southwest);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试东北角经度小于西南角返回false")
    void testIsValid_东北角经度小于西南角返回false() {
        Coordinate northeast = Coordinate.of(40.0, 115.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);

        boolean result = SpatialBounds.isValid(northeast, southwest);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试边界内坐标返回true")
    void testContains_边界内坐标返回true() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        Coordinate insideCoordinate = Coordinate.of(39.5, 116.5);

        boolean result = bounds.contains(insideCoordinate);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试边界外坐标返回false")
    void testContains_边界外坐标返回false() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        Coordinate outsideCoordinate = Coordinate.of(41.0, 118.0);

        boolean result = bounds.contains(outsideCoordinate);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试计算中心点")
    void testGetCenter_计算中心点() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        Coordinate center = bounds.getCenter();

        assertThat(center).isNotNull();
        assertThat(center.getLatitude()).isEqualByComparingTo(new BigDecimal("39.5"));
        assertThat(center.getLongitude()).isEqualByComparingTo(new BigDecimal("116.5"));
    }

    @Test
    @DisplayName("测试计算面积")
    void testGetArea_计算面积() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        BigDecimal area = bounds.getArea();

        assertThat(area).isNotNull();
        assertThat(area).isPositive();
    }

    @Test
    @DisplayName("测试创建边界-使用Coordinate")
    void testCreateBounds_WithCoordinate() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);

        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        assertThat(bounds).isNotNull();
        assertThat(bounds.getNortheast()).isEqualTo(northeast);
        assertThat(bounds.getSouthwest()).isEqualTo(southwest);
    }

    @Test
    @DisplayName("测试创建边界-使用BigDecimal")
    void testCreateBounds_WithBigDecimal() {
        BigDecimal neLatitude = new BigDecimal("40.0");
        BigDecimal neLongitude = new BigDecimal("117.0");
        BigDecimal swLatitude = new BigDecimal("39.0");
        BigDecimal swLongitude = new BigDecimal("116.0");

        SpatialBounds bounds = SpatialBounds.of(neLatitude, neLongitude, swLatitude, swLongitude);

        assertThat(bounds).isNotNull();
        assertThat(bounds.getNortheast().getLatitude()).isEqualTo(neLatitude);
        assertThat(bounds.getNortheast().getLongitude()).isEqualTo(neLongitude);
        assertThat(bounds.getSouthwest().getLatitude()).isEqualTo(swLatitude);
        assertThat(bounds.getSouthwest().getLongitude()).isEqualTo(swLongitude);
    }

    @Test
    @DisplayName("测试创建边界-空东北角抛出异常")
    void testCreateBounds_NullNortheast_ThrowsException() {
        Coordinate southwest = Coordinate.of(39.0, 116.0);

        assertThatThrownBy(() -> SpatialBounds.of(null, southwest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("东北角坐标和西南角坐标不能为空");
    }

    @Test
    @DisplayName("测试创建边界-空西南角抛出异常")
    void testCreateBounds_NullSouthwest_ThrowsException() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);

        assertThatThrownBy(() -> SpatialBounds.of(northeast, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("东北角坐标和西南角坐标不能为空");
    }

    @Test
    @DisplayName("测试创建边界-无效边界抛出异常")
    void testCreateBounds_InvalidBounds_ThrowsException() {
        Coordinate northeast = Coordinate.of(38.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);

        assertThatThrownBy(() -> SpatialBounds.of(northeast, southwest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效的边界");
    }

    @Test
    @DisplayName("测试contains-空坐标返回false")
    void testContains_NullCoordinate_ReturnsFalse() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        boolean result = bounds.contains(null);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试边界相等性")
    void testBoundsEquality() {
        Coordinate northeast1 = Coordinate.of(40.0, 117.0);
        Coordinate southwest1 = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds1 = SpatialBounds.of(northeast1, southwest1);

        Coordinate northeast2 = Coordinate.of(40.0, 117.0);
        Coordinate southwest2 = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds2 = SpatialBounds.of(northeast2, southwest2);

        Coordinate northeast3 = Coordinate.of(41.0, 118.0);
        Coordinate southwest3 = Coordinate.of(40.0, 117.0);
        SpatialBounds bounds3 = SpatialBounds.of(northeast3, southwest3);

        assertThat(bounds1).isEqualTo(bounds2);
        assertThat(bounds1).isNotEqualTo(bounds3);
    }

    @Test
    @DisplayName("测试contains-边界上的坐标")
    void testContains_CoordinateOnBoundary() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        Coordinate onBoundary = Coordinate.of(39.0, 116.0);

        boolean result = bounds.contains(onBoundary);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试contains-东北角坐标")
    void testContains_NortheastCorner() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        boolean result = bounds.contains(northeast);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试contains-西南角坐标")
    void testContains_SouthwestCorner() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);
        Coordinate southwest = Coordinate.of(39.0, 116.0);
        SpatialBounds bounds = SpatialBounds.of(northeast, southwest);

        boolean result = bounds.contains(southwest);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试isValid-空东北角返回false")
    void testIsValid_NullNortheast_ReturnsFalse() {
        Coordinate southwest = Coordinate.of(39.0, 116.0);

        boolean result = SpatialBounds.isValid(null, southwest);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试isValid-空西南角返回false")
    void testIsValid_NullSouthwest_ReturnsFalse() {
        Coordinate northeast = Coordinate.of(40.0, 117.0);

        boolean result = SpatialBounds.isValid(northeast, null);

        assertThat(result).isFalse();
    }
}
