package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 坐标值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {

    /**
     * 纬度
     */
    private double latitude;

    /**
     * 经度
     */
    private double longitude;

    public static Coordinate of(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("纬度必须在-90到90之间");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("经度必须在-180到180之间");
        }
        return new Coordinate(latitude, longitude);
    }

    @JsonValue
    public String toString() {
        return latitude + "," + longitude;
    }
}
