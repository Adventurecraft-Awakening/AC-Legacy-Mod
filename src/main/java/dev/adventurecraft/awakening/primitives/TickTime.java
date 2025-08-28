package dev.adventurecraft.awakening.primitives;

import org.jetbrains.annotations.NotNull;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

public record TickTime(long ticks) {

    public static final int TICKS_PER_SECOND = 20;

    public float seconds() {
        return ticks / (float) TICKS_PER_SECOND;
    }

    public int ticks32() {
        return (int) this.ticks;
    }

    public static TickTime fromSeconds(double seconds) {
        return new TickTime(Math.round(seconds * TICKS_PER_SECOND));
    }

    public static class LongFormat extends Format {
        private final NumberFormat numberFormat;

        public LongFormat(NumberFormat numberFormat) {
            this.numberFormat = numberFormat;
        }

        @Override
        public StringBuffer format(Object o, @NotNull StringBuffer stringBuffer, @NotNull FieldPosition fieldPosition) {
            if (o instanceof TickTime(long ticks)) {
                return this.numberFormat.format(ticks, stringBuffer, fieldPosition);
            }
            return stringBuffer;
        }

        @Override
        public Object parseObject(String s, @NotNull ParsePosition parsePosition) {
            if (this.numberFormat.parseObject(s, parsePosition) instanceof Number num) {
                if (!Double.isNaN(num.doubleValue())) {
                    return new TickTime(num.longValue());
                }
                else {
                    parsePosition.setErrorIndex(parsePosition.getIndex());
                }
            }
            return null;
        }
    }

    public static class TimeFormat extends Format {
        private final NumberFormat numberFormat;

        public TimeFormat(NumberFormat numberFormat) {
            this.numberFormat = numberFormat;
        }

        @Override
        public StringBuffer format(Object o, @NotNull StringBuffer stringBuffer, @NotNull FieldPosition fieldPosition) {
            if (o instanceof TickTime value) {
                return this.numberFormat.format(value.seconds(), stringBuffer, fieldPosition);
            }
            return stringBuffer;
        }

        @Override
        public Object parseObject(String s, @NotNull ParsePosition parsePosition) {
            if (this.numberFormat.parseObject(s, parsePosition) instanceof Number num) {
                double value = num.doubleValue();
                if (!Double.isNaN(value)) {
                    return TickTime.fromSeconds(value);
                }
                else {
                    parsePosition.setErrorIndex(parsePosition.getIndex());
                }
            }
            return null;
        }
    }
}
