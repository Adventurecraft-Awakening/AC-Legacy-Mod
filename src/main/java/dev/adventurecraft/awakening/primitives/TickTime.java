package dev.adventurecraft.awakening.primitives;

import org.jetbrains.annotations.NotNull;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public record TickTime(long ticks) {

    public static final int TICKS_PER_SECOND = 20;

    public static final TickFormat FULL_TIME_FORMAT;
    public static final TickFormat TIME_FORMAT;
    public static final TickFormat LONG_FORMAT;

    static {
        var numFormat = (NumberFormat) NumberFormat.getInstance(Locale.ROOT).clone();
        numFormat.setGroupingUsed(false);
        numFormat.setMaximumFractionDigits(2);

        var fullFormat = (NumberFormat) numFormat.clone();
        fullFormat.setMinimumFractionDigits(2);
        FULL_TIME_FORMAT = new TimeFormat(fullFormat);

        TIME_FORMAT = new TimeFormat(numFormat);
        LONG_FORMAT = new LongFormat(numFormat);
    }

    public float seconds() {
        return ticks / (float) TICKS_PER_SECOND;
    }

    public int ticks32() {
        return (int) this.ticks;
    }

    public static TickTime fromSeconds(double seconds) {
        return new TickTime(Math.round(seconds * TICKS_PER_SECOND));
    }

    public static abstract class TickFormat extends Format {
        protected final NumberFormat numberFormat;

        public TickFormat(NumberFormat numberFormat) {
            this.numberFormat = numberFormat;
        }

        public abstract StringBuffer format(TickTime o, StringBuffer buffer, FieldPosition position);

        public abstract TickTime fromNumber(Number n);

        @Override
        public StringBuffer format(Object o, @NotNull StringBuffer buffer, @NotNull FieldPosition position) {
            if (o instanceof TickTime value) {
                return this.format(value, buffer, position);
            }
            throw new IllegalArgumentException("Cannot format given Object as TickTime.");
        }

        @Override
        public TickTime parseObject(String s, @NotNull ParsePosition position) {
            if (this.numberFormat.parseObject(s, position) instanceof Number num) {
                if (!Double.isNaN(num.doubleValue())) {
                    return this.fromNumber(num);
                }
                else {
                    position.setErrorIndex(position.getIndex());
                }
            }
            return null;
        }
    }

    static class LongFormat extends TickFormat {
        public LongFormat(NumberFormat numberFormat) {
            super(numberFormat);
        }

        public @Override StringBuffer format(TickTime o, StringBuffer buffer, FieldPosition position) {
            return this.numberFormat.format(o.ticks, buffer, position);
        }

        public @Override TickTime fromNumber(Number n) {
            return new TickTime(n.longValue());
        }
    }

    static class TimeFormat extends TickFormat {
        public TimeFormat(NumberFormat numberFormat) {
            super(numberFormat);
        }

        public @Override StringBuffer format(TickTime o, StringBuffer buffer, FieldPosition position) {
            return this.numberFormat.format(o.seconds(), buffer, position);
        }

        public @Override TickTime fromNumber(Number n) {
            return TickTime.fromSeconds(n.doubleValue());
        }
    }
}
