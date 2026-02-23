package com.hkt.iot.space.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * 坐标值对象测试
 */
@DisplayName("坐标值对象测试")
class CoordinateTest {

    @Test
    @DisplayName("测试有效坐标返回true")
    void testCoordinateValid_有效坐标返回true() {
        BigDecimal latitude = new BigDecimal("39.9042");
        BigDecimal longitude = new BigDecimal("116.4074");

        boolean result = Coordinate.coordinateValid(latitude, longitude);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试无效经度返回false")
    void testCoordinateValid_无效经度返回false() {
        BigDecimal latitude = new BigDecimal("39.9042");
        BigDecimal invalidLongitude = new BigDecimal("200");

        boolean result = Coordinate.coordinateValid(latitude, invalidLongitude);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试无效纬度返回false")
    void testCoordinateValid_无效纬度返回false() {
        BigDecimal invalidLatitude = new BigDecimal("100");
        BigDecimal longitude = new BigDecimal("116.4074");

        boolean result = Coordinate.coordinateValid(invalidLatitude, longitude);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试北边坐标返回true")
    void testIsNorthOf_北边坐标返回true() {
        Coordinate coordinate1 = Coordinate.of(40.0, 116.0);
        Coordinate coordinate2 = Coordinate.of(39.0, 116.0);

        boolean result = coordinate1.isNorthOf(coordinate2);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试南边坐标返回false")
    void testIsNorthOf_南边坐标返回false() {
        Coordinate coordinate1 = Coordinate.of(39.0, 116.0);
        Coordinate coordinate2 = Coordinate.of(40.0, 116.0);

        boolean result = coordinate1.isNorthOf(coordinate2);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试东边坐标返回true")
    void testIsEastOf_东边坐标返回true() {
        Coordinate coordinate1 = Coordinate.of(39.0, 117.0);
        Coordinate coordinate2 = Coordinate.of(39.0, 116.0);

        boolean result = coordinate1.isEastOf(coordinate2);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试西边坐标返回false")
    void testIsEastOf_西边坐标返回false() {
        Coordinate coordinate1 = Coordinate.of(39.0, 116.0);
        Coordinate coordinate2 = Coordinate.of(39.0, 117.0);

        boolean result = coordinate1.isEastOf(coordinate2);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试计算两点距离")
    void testDistanceTo_计算两点距离() {
        Coordinate beijing = Coordinate.of(39.9042, 116.4074);
        Coordinate shanghai = Coordinate.of(31.2304, 121.4737);

        BigDecimal distance = beijing.distanceTo(shanghai);

        assertThat(distance).isPositive();
        assertThat(distance).isGreaterThan(new BigDecimal("1000"));
        assertThat(distance).isLessThan(new BigDecimal("1500"));
    }

    @Test
    @DisplayName("测试创建坐标-使用BigDecimal")
    void testCreateCoordinate_WithBigDecimal() {
        BigDecimal latitude = new BigDecimal("39.9042");
        BigDecimal longitude = new BigDecimal("116.4074");

        Coordinate coordinate = Coordinate.of(latitude, longitude);

        assertThat(coordinate).isNotNull();
        assertThat(coordinate.getLatitude()).isEqualTo(latitude);
        assertThat(coordinate.getLongitude()).isEqualTo(longitude);
    }

    @Test
    @DisplayName("测试创建坐标-使用double")
    void testCreateCoordinate_WithDouble() {
        double latitude = 39.9042;
        double longitude = 116.4074;

        Coordinate coordinate = Coordinate.of(latitude, longitude);

        assertThat(coordinate).isNotNull();
        assertThat(coordinate.getLatitude().doubleValue()).isEqualTo(latitude);
        assertThat(coordinate.getLongitude().doubleValue()).isEqualTo(longitude);
    }

    @Test
    @DisplayName("测试创建坐标-空纬度抛出异常")
    void testCreateCoordinate_NullLatitude_ThrowsException() {
        BigDecimal longitude = new BigDecimal("116.4074");

        assertThatThrownBy(() -> Coordinate.of(null, longitude))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("纬度和经度不能为空");
    }

    @Test
    @DisplayName("测试创建坐标-空经度抛出异常")
    void testCreateCoordinate_NullLongitude_ThrowsException() {
        BigDecimal latitude = new BigDecimal("39.9042");

        assertThatThrownBy(() -> Coordinate.of(latitude, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("纬度和经度不能为空");
    }

    @Test
    @DisplayName("测试创建坐标-无效坐标抛出异常")
    void testCreateCoordinate_InvalidCoordinate_ThrowsException() {
        BigDecimal invalidLatitude = new BigDecimal("100");
        BigDecimal longitude = new BigDecimal("116.4074");

        assertThatThrownBy(() -> Coordinate.of(invalidLatitude, longitude))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效的坐标");
    }

    @Test
    @DisplayName("测试isNorthOf-空坐标抛出异常")
    void testIsNorthOf_NullCoordinate_ThrowsException() {
        Coordinate coordinate = Coordinate.of(39.9042, 116.4074);

        assertThatThrownBy(() -> coordinate.isNorthOf(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("比较坐标不能为空");
    }

    @Test
    @DisplayName("测试isEastOf-空坐标抛出异常")
    void testIsEastOf_NullCoordinate_ThrowsException() {
        Coordinate coordinate = Coordinate.of(39.9042, 116.4074);

        assertThatThrownBy(() -> coordinate.isEastOf(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("比较坐标不能为空");
    }

    @Test
    @DisplayName("测试distanceTo-空坐标抛出异常")
    void testDistanceTo_NullCoordinate_ThrowsException() {
        Coordinate coordinate = Coordinate.of(39.9042, 116.4074);

        assertThatThrownBy(() -> coordinate.distanceTo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("目标坐标不能为空");
    }

    @Test
    @DisplayName("测试坐标相等性")
    void testCoordinateEquality() {
        Coordinate coordinate1 = Coordinate.of(39.9042, 116.4074);
        Coordinate coordinate2 = Coordinate.of(39.9042, 116.4074);
        Coordinate coordinate3 = Coordinate.of(31.2304, 121.4737);

        assertThat(coordinate1).isEqualTo(coordinate2);
        assertThat(coordinate1).isNotEqualTo(coordinate3);
    }

    @Test
    @DisplayName("测试边界值-最小纬度")
    void testBoundary_MinLatitude() {
        Coordinate coordinate = Coordinate.of(-90.0, 0.0);

        assertThat(coordinate.coordinateValid()).isTrue();
        assertThat(coordinate.getLatitude()).isEqualTo(new BigDecimal("-90.0"));
    }

    @Test
    @DisplayName("测试边界值-最大纬度")
    void testBoundary_MaxLatitude() {
        Coordinate coordinate = Coordinate.of(90.0, 0.0);

        assertThat(coordinate.coordinateValid()).isTrue();
        assertThat(coordinate.getLatitude()).isEqualTo(new BigDecimal("90.0"));
    }

    @Test
    @DisplayName("测试边界值-最小经度")
    void testBoundary_MinLongitude() {
        Coordinate coordinate = Coordinate.of(0.0, -180.0);

        assertThat(coordinate.coordinateValid()).isTrue();
        assertThat(coordinate.getLongitude()).isEqualTo(new BigDecimal("-180.0"));
    }

    @Test
    @DisplayName("测试边界值-最大经度")
    void testBoundary_MaxLongitude() {
        Coordinate coordinate = Coordinate.of(0.0, 180.0);

        assertThat(coordinate.coordinateValid()).isTrue();
        assertThat(coordinate.getLongitude()).isEqualTo(new BigDecimal("180.0"));
    }
}
