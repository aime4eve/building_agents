package com.hkt.iot.space.domain.model;

import com.hkt.iot.domain.model.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 坐标值对象
 * 表示地球表面的地理坐标（纬度和经度）
 *
 * <p>值对象特点：
 * <ul>
 *   <li>不可变：创建后属性不可修改</li>
 *   <li>基于值判断相等性</li>
 *   <li>可以被安全共享</li>
 * </ul>
 *
 * @author HKT IoT Team
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class Coordinate extends ValueObject {

    private static final long serialVersionUID = 1L;

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");
    private static final BigDecimal EARTH_RADIUS_KM = new BigDecimal("6371");
    private static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);

    private final BigDecimal latitude;
    private final BigDecimal longitude;

    private Coordinate(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Coordinate of(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("纬度和经度不能为空");
        }
        if (!coordinateValid(latitude, longitude)) {
            throw new IllegalArgumentException(
                    String.format("无效的坐标：纬度必须在%s到%s之间，经度必须在%s到%s之间",
                            MIN_LATITUDE, MAX_LATITUDE, MIN_LONGITUDE, MAX_LONGITUDE));
        }
        return new Coordinate(latitude, longitude);
    }

    public static Coordinate of(double latitude, double longitude) {
        return of(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    }

    public static boolean coordinateValid(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        return latitude.compareTo(MIN_LATITUDE) >= 0
                && latitude.compareTo(MAX_LATITUDE) <= 0
                && longitude.compareTo(MIN_LONGITUDE) >= 0
                && longitude.compareTo(MAX_LONGITUDE) <= 0;
    }

    public boolean coordinateValid() {
        return coordinateValid(this.latitude, this.longitude);
    }

    public boolean isNorthOf(Coordinate other) {
        if (other == null) {
            throw new IllegalArgumentException("比较坐标不能为空");
        }
        return this.latitude.compareTo(other.latitude) > 0;
    }

    public boolean isEastOf(Coordinate other) {
        if (other == null) {
            throw new IllegalArgumentException("比较坐标不能为空");
        }
        return this.longitude.compareTo(other.longitude) > 0;
    }

    public BigDecimal distanceTo(Coordinate other) {
        if (other == null) {
            throw new IllegalArgumentException("目标坐标不能为空");
        }

        double lat1Rad = Math.toRadians(this.latitude.doubleValue());
        double lat2Rad = Math.toRadians(other.latitude.doubleValue());
        double deltaLat = Math.toRadians(other.latitude.subtract(this.latitude).doubleValue());
        double deltaLon = Math.toRadians(other.longitude.subtract(this.longitude).doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS_KM.doubleValue() * c;

        return BigDecimal.valueOf(distance).setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{latitude, longitude};
    }
}
