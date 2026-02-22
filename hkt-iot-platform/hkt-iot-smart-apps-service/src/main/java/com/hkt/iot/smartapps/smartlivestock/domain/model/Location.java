package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 位置值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private Coordinate coordinate;
    private LocalDateTime timestamp;

    /**
     * 计算与另一个位置的距离（米，使用Haversine公式）
     */
    public double distanceTo(Location other) {
        if (this.coordinate == null || other.getCoordinate() == null) {
            throw new IllegalArgumentException("坐标不能为空");
        }

        final int EARTH_RADIUS = 6371000;  // 地球半径，单位：米

        double lat1 = Math.toRadians(this.coordinate.getLatitude());
        double lat2 = Math.toRadians(other.getCoordinate().getLatitude());
        double deltaLat = Math.toRadians(other.getCoordinate().getLatitude() - this.coordinate.getLatitude());
        double deltaLon = Math.toRadians(other.getCoordinate().getLongitude() - this.coordinate.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 创建位置
     */
    public static Location of(double latitude, double longitude) {
        return Location.builder()
                .coordinate(Coordinate.of(latitude, longitude))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
