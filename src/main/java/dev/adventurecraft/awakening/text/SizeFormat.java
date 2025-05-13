package dev.adventurecraft.awakening.text;

import dev.adventurecraft.awakening.util.MathF;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.*;

public abstract class SizeFormat extends Format {

    private final NumberFormat numberFormat;

    private final int numberBase;

    public SizeFormat(NumberFormat numberFormat, int numberBase) {
        this.numberFormat = numberFormat;
        this.numberBase = numberBase;
    }

    public final int getNumberBase() {
        return this.numberBase;
    }

    @Override
    public StringBuffer format(Object value, @NotNull StringBuffer toAppendTo, @NotNull FieldPosition pos) {
        if (value instanceof Number number) {
            double log = MathF.log(number, this.getNumberBase());
            return this.format(number, log, toAppendTo, pos);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Number.");
    }

    @Override
    public Object parseObject(String source, @NotNull ParsePosition pos) {
        return null;
    }

    public String getSeparator() {
        return " ";
    }

    public String getSuffix(double log) {
        String[] suffixes = this.getNumberBase() == 1024 ? this.getBinarySuffixes() : this.getMetricSuffixes();
        int index = (int) Math.floor(log);
        return index > suffixes.length ? suffixes[suffixes.length - 1] : suffixes[index];
    }

    protected abstract String[] getBinarySuffixes();

    protected abstract String[] getMetricSuffixes();

    private StringBuffer format(
        Number value,
        double log,
        @NotNull StringBuffer toAppendTo,
        @NotNull FieldPosition pos
    ) {
        Number scaledValue = getScaledValue(value, log, this.getNumberBase());
        return this.numberFormat.format(scaledValue, toAppendTo, pos)
            .append(this.getSeparator())
            .append(this.getSuffix(log));
    }

    private static Number getScaledValue(Number value, double log, int base) {
        int fLog = (int) Math.floor(log);
        if (fLog == 0) {
            return value;
        }

        if (value instanceof BigDecimal bigDec) {
            return getScaledValue(bigDec, fLog, base);
        }
        else if (value instanceof BigInteger bigInt) {
            return getScaledValue(new BigDecimal(bigInt), fLog, base);
        }
        return value.doubleValue() / Math.pow(base, fLog);
    }

    private static BigDecimal getScaledValue(BigDecimal value, int fLog, int base) {
        return value.divide(BigDecimal.valueOf(base).pow(fLog), RoundingMode.FLOOR);
    }
}
