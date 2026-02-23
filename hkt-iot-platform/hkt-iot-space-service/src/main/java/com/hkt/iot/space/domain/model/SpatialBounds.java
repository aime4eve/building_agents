package com.hkt.iot.space.domain.model;

import com.hkt.iot.domain.model.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 空间边界值对象
 * 表示地球表面上的矩形区域边界（由东北角和西南角坐标定义）
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
public class SpatialBounds extends ValueObject {

    private static final long serialVersionUID = 1L;

    private final Coordinate northeast;
    private final Coordinate southwest;

    private SpatialBounds(Coordinate northeast, Coordinate southwest) {
        this.northeast = northeast;
        this.southwest = southwest;
    }

    public static SpatialBounds of(Coordinate northeast, Coordinate southwest) {
        if (northeast == null || southwest == null) {
            throw new IllegalArgumentException("东北角坐标和西南角坐标不能为空");
        }
        if (!isValid(northeast, southwest)) {
            throw new IllegalArgumentException("无效的边界：东北角经纬度必须大于西南角");
        }
        return new SpatialBounds(northeast, southwest);
    }

    public static SpatialBounds of(BigDecimal neLatitude, BigDecimal neLongitude,
                                   BigDecimal swLatitude, BigDecimal swLongitude) {
        return of(Coordinate.of(neLatitude, neLongitude), Coordinate.of(swLatitude, swLongitude));
    }

    public static boolean isValid(Coordinate northeast, Coordinate southwest) {
        if (northeast == null || southwest == null) {
            return false;
        }
        return northeast.isNorthOf(southwest) && northeast.isEastOf(southwest);
    }

    public boolean isValid() {
        return isValid(this.northeast, this.southwest);
    }

    public boolean contains(Coordinate coordinate) {
        if (coordinate == null) {
            return false;
        }
        boolean latitudeInRange = coordinate.getLatitude().compareTo(southwest.getLatitude()) >= 0
                && coordinate.getLatitude().compareTo(northeast.getLatitude()) <= 0;
        boolean longitudeInRange = coordinate.getLongitude().compareTo(southwest.getLongitude()) >= 0
                && coordinate.getLongitude().compareTo(northeast.getLongitude()) <= 0;
        return latitudeInRange && longitudeInRange;
    }

    public Coordinate getCenter() {
        BigDecimal centerLatitude = northeast.getLatitude().add(southwest.getLatitude())
                .divide(BigDecimal.valueOf(2), 7, RoundingMode.HALF_UP);
        BigDecimal centerLongitude = northeast.getLongitude().add(southwest.getLongitude())
                .divide(BigDecimal.valueOf(2), 7, RoundingMode.HALF_UP);
        return Coordinate.of(centerLatitude, centerLongitude);
    }

    public BigDecimal getArea() {
        BigDecimal latDistance = northeast.distanceTo(Coordinate.of(southwest.getLatitude(), northeast.getLongitude()));
        BigDecimal lonDistance = northeast.distanceTo(Coordinate.of(northeast.getLatitude(), southwest.getLongitude()));
        return latDistance.multiply(lonDistance).setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{northeast, southwest};
    }
}
