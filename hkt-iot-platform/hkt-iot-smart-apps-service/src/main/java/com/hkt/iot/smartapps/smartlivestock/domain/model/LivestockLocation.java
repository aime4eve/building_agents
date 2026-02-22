package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 牲畜位置值对象
 *
 * 用于批量检查围栏违规时携带牲畜ID和位置信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivestockLocation {

    private LivestockId livestockId;
    private Location location;

    public static LivestockLocation of(LivestockId livestockId, Location location) {
        return LivestockLocation.builder()
                .livestockId(livestockId)
                .location(location)
                .build();
    }

    public static LivestockLocation of(LivestockId livestockId, double latitude, double longitude) {
        return LivestockLocation.builder()
                .livestockId(livestockId)
                .location(Location.of(latitude, longitude))
                .build();
    }
}
