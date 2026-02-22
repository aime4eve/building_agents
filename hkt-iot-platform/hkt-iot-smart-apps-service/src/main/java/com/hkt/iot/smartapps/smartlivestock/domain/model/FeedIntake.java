package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 饲料摄入量值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedIntake {

    /**
     * 摄入量
     */
    private BigDecimal amount;

    /**
     * 单位（kg, lb）
     */
    private String unit;

    /**
     * 摄入时间
     */
    private LocalDateTime feedingTime;

    /**
     * 饲料类型
     */
    private String feedType;

    public static FeedIntake ofKilograms(double amount) {
        return FeedIntake.builder()
                .amount(BigDecimal.valueOf(amount))
                .unit("kg")
                .feedingTime(LocalDateTime.now())
                .build();
    }

    /**
     * 转换为公斤
     */
    public FeedIntake toKilograms() {
        if ("kg".equals(this.unit)) {
            return this;
        }
        if ("lb".equals(this.unit)) {
            return FeedIntake.builder()
                    .amount(this.amount.multiply(BigDecimal.valueOf(0.453592)))
                    .unit("kg")
                    .feedingTime(this.feedingTime)
                    .feedType(this.feedType)
                    .build();
        }
        throw new UnsupportedOperationException("不支持的单位转换: " + this.unit);
    }
}
