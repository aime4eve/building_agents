package com.hkt.iot.order.domain.model;

import com.hkt.iot.domain.model.ValueObject;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * 金额值对象
 * 处理货币金额，使用BigDecimal确保精度
 *
 * @author HKT IoT Team
 */
@Getter
public class Money extends ValueObject {

    private static final long serialVersionUID = 1L;

    /**
     * 默认货币（人民币）
     */
    public static final Currency CNY = Currency.getInstance("CNY");

    /**
     * 零金额
     */
    public static final Money ZERO = new Money(BigDecimal.ZERO, CNY);

    /**
     * 金额
     */
    private final BigDecimal amount;

    /**
     * 货币
     */
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (currency == null) {
            throw new IllegalArgumentException("货币不能为空");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public Money(BigDecimal amount) {
        this(amount, CNY);
    }

    public Money(double amount) {
        this(BigDecimal.valueOf(amount), CNY);
    }

    /**
     * 创建人民币金额
     */
    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount), CNY);
    }

    /**
     * 创建人民币金额
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount, CNY);
    }

    /**
     * 创建指定货币金额
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    /**
     * 加法
     */
    public Money add(Money other) {
        checkSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * 减法
     */
    public Money subtract(Money other) {
        checkSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * 乘法
     */
    public Money multiply(double multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    /**
     * 乘法
     */
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    /**
     * 除法
     */
    public Money divide(double divisor) {
        return new Money(this.amount.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP), this.currency);
    }

    /**
     * 比较：大于
     */
    public boolean isGreaterThan(Money other) {
        checkSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * 比较：大于等于
     */
    public boolean isGreaterThanOrEqual(Money other) {
        checkSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    /**
     * 比较：小于
     */
    public boolean isLessThan(Money other) {
        checkSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * 比较：小于等于
     */
    public boolean isLessThanOrEqual(Money other) {
        checkSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }

    /**
     * 是否为零
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 是否为正数
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 是否为负数
     */
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 取绝对值
     */
    public Money abs() {
        return new Money(this.amount.abs(), this.currency);
    }

    /**
     * 取反
     */
    public Money negate() {
        return new Money(this.amount.negate(), this.currency);
    }

    /**
     * 检查货币是否相同
     */
    private void checkSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("货币类型不同，无法进行计算");
        }
    }

    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{amount, currency};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return currency.getSymbol() + amount;
    }
}
